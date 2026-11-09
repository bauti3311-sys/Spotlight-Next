package dev.bautista.spotlight.index

import android.content.*
import android.database.sqlite.*
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FileIndex(
    private val context: Context,
    private val sharedAccess: (() -> Boolean)? = null,
    private val storageRoots: (() -> List<java.io.File>)? = null,
    private val resourceState: () -> dev.bautista.spotlight.control.DeviceResourceState = {dev.bautista.spotlight.control.DeviceResources.read(context)},
    private val liveConfig:(()->Config?)? = null,
    private val semanticBackend:dev.bautista.spotlight.semantic.DocumentEmbedder? = null,
    private val pdfExtraction: (suspend (Uri,PdfTextLimits,()->OcrBudgetDecision,CachedPdfText?)->ExtractedDocument)? = null,
) {
    data class Status(
        val busy: Boolean = false,
        val count: Int = 0,
        val message: String = "Elegí carpetas para indexar",
        val truncated: Boolean = false,
    )

    private val state = MutableStateFlow(Status())
    val status = state.asStateFlow()
    private val lock = Mutex()
    private val refreshScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var refreshJob: Job? = null
    private val changes = MutableStateFlow(0L)
    val revision = changes.asStateFlow()
    private var observed: Set<String> = emptySet()
    private val observer =
        object :
            android.database.ContentObserver(
                android.os.Handler(android.os.Looper.getMainLooper())
            ) {
            override fun onChange(selfChange: Boolean) {
                refreshed = 0L
                changes.value = changes.value + 1
            }
        }

    private fun observe(trees: Set<String>) {
        synchronized(observer) {
            if (trees == observed) return
            runCatching { context.contentResolver.unregisterContentObserver(observer) }
            trees.forEach { tree ->
                runCatching {
                    context.contentResolver.registerContentObserver(Uri.parse(tree), true, observer)
                }
            }
            observed = trees
        }
    }

    private var lastBroad = false
    private var lastTrees: Set<String> = emptySet()
    @Volatile private var refreshed = 0L

    private class Database(context: Context) :
        SQLiteOpenHelper(context, "search-index.db", null, 4) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE files(uri TEXT PRIMARY KEY,tree TEXT,parent TEXT,name TEXT,mime TEXT,modified INTEGER,size INTEGER,author TEXT,created INTEGER,body TEXT,mode TEXT,extraction TEXT NOT NULL DEFAULT '',name_key TEXT NOT NULL DEFAULT '')"
            )
        }

        override fun onOpen(db:SQLiteDatabase) {
            super.onOpen(db)
            db.execSQL("CREATE TABLE IF NOT EXISTS semantic_vectors(uri TEXT NOT NULL,model TEXT NOT NULL,fingerprint TEXT NOT NULL,part INTEGER NOT NULL,start INTEGER NOT NULL,end INTEGER NOT NULL,excerpt TEXT NOT NULL,vector BLOB NOT NULL,PRIMARY KEY(uri,model,fingerprint,part))")
            db.execSQL("CREATE INDEX IF NOT EXISTS files_tree_name ON files(tree,name_key)")
            db.execSQL("CREATE INDEX IF NOT EXISTS files_tree_modified ON files(tree,modified DESC)")
            db.execSQL("CREATE INDEX IF NOT EXISTS files_tree_created ON files(tree,created)")
            db.execSQL("CREATE INDEX IF NOT EXISTS files_tree_mime ON files(tree,mime)")
        }
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if(oldVersion<2)db.execSQL("ALTER TABLE files ADD COLUMN extraction TEXT NOT NULL DEFAULT ''")
            if(oldVersion<4) {
                db.execSQL("ALTER TABLE files ADD COLUMN name_key TEXT NOT NULL DEFAULT ''")
                // Cache-only additive migration; preserve URI, body, grants and all metadata.
                db.compileStatement("UPDATE files SET name_key=? WHERE uri=?").use { update ->
                    db.query("files",arrayOf("uri","name"),null,null,null,null,null).use { rows ->
                        while(rows.moveToNext()) {
                            update.bindString(1,normalize(rows.getString(1).orEmpty()))
                            update.bindString(2,rows.getString(0));update.executeUpdateDelete()
                        }
                    }
                }
            }
        }
    }

    private val helper = Database(context).apply { setWriteAheadLoggingEnabled(true) }

    fun broadAllowed(): Boolean =
        runCatching { sharedAccess?.invoke()
            ?: (android.os.Build.VERSION.SDK_INT >= 30 && android.os.Environment.isExternalStorageManager()) }.getOrDefault(false)

    private fun sharedRoots(): List<java.io.File> {
        storageRoots?.let {
            return it()
        }
        val manager = context.getSystemService(android.os.storage.StorageManager::class.java)
        return (listOf(android.os.Environment.getExternalStorageDirectory()) +
                if (android.os.Build.VERSION.SDK_INT >= 30)
                    manager.storageVolumes.mapNotNull { it.directory }
                else emptyList())
            .distinctBy { it.absolutePath }
            .filter { it.isDirectory }
    }

    private fun blockedPath(path: String): Boolean =
        Regex("/Android/(data|obb|sandbox)(/|$)").containsMatchIn(path)

    private fun allowedTrees(c: Config): Set<String> {
        // Config is not proof of authorization: Android can revoke SAF independently.
        val granted=runCatching { context.contentResolver.persistedUriPermissions.filter{it.isReadPermission}.map{it.uri.toString()}.toSet() }.getOrDefault(emptySet())
        return c.trees.filter { it in granted && !(broadAllowed() && Uri.parse(it).authority=="com.android.externalstorage.documents") }.toSet()
    }
    private fun sourceAllowed(tree:String,c:Config)=c.files && !c.privateMode && if(tree=="__shared__")broadAllowed() else tree in allowedTrees(c)

    suspend fun ensure(c: Config) {
        // A refused refresh must not trigger a revision -> search -> refresh loop.
        if(c.privateMode || !c.files)return
        observe(c.trees + if(broadAllowed())setOf(android.provider.MediaStore.Files.getContentUri("external").toString()) else emptySet())
        if (
            broadAllowed() != lastBroad ||
                c.trees != lastTrees ||
                System.currentTimeMillis() - refreshed > 600000
        )
            {
                synchronized(this) {
                    if (refreshJob?.isActive != true)
                        refreshJob =
                            refreshScope.launch {
                                try {
                                    refresh(c, false)
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    refreshed = System.currentTimeMillis()
                                    lastTrees = c.trees
                                    lastBroad = broadAllowed()
                                    state.value =
                                        Status(
                                            message = "No se pudo actualizar el índice",
                                            truncated = true,
                                        )
                                }
                                changes.value = changes.value + 1
                            }
                }
            }
    }

    suspend fun hasImports():Boolean=withContext(Dispatchers.IO){helper.readableDatabase.rawQuery("SELECT 1 FROM files WHERE tree = ? LIMIT 1",arrayOf("__imports__")).use{it.moveToFirst()}}

    fun cancelRefresh() {
        refreshJob?.cancel()
    }

    suspend fun clear() =
        withContext(Dispatchers.IO) {
            refreshJob?.cancelAndJoin()
            val semantic=semanticJob;cancelSemanticWork();semantic?.cancelAndJoin()
            lock.withLock {
                helper.writableDatabase.delete("files", null, null)
                helper.writableDatabase.delete("semantic_vectors",null,null)
                lastTrees = emptySet()
                refreshed = 0L
                state.value = Status(message = "Índice borrado")
                changes.update{it+1} // Invalidate ephemeral document evidence immediately after deletion.
            }
        }

    suspend fun storeTranscript(uri: String, name: String, mime: String, text: String) =
        withContext(Dispatchers.IO) {
            val values =
                ContentValues().apply {
                    put("uri", uri)
                    put("tree", "__imports__")
                    put("parent", "")
                    put("name", name)
                    put("name_key", normalize(name))
                    put("mime", mime)
                    put("modified", 0L)
                    put("created", 0L)
                    put("size", 0L)
                    put("author", "")
                    put("body", text.take(100000))
                    put("mode", "transcript")
                }
            helper.writableDatabase.insertWithOnConflict(
                "files",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE,
            )
        }

    /** Update only an already-authorized row; never turn a revoked SAF cache into an import. */
    suspend fun cacheExtraction(uri:String,document:ExtractedDocument,c:Config):Boolean=withContext(Dispatchers.IO) {
        if(!c.files || !c.contentIndex || c.privateMode)return@withContext false
        val sources=allowedTrees(c)+"__imports__"+if(broadAllowed())setOf("__shared__") else emptySet()
        val values=ContentValues().apply{put("body",document.text.take(100000));put("extraction",document.metadata())}
        val selection="uri=? AND tree IN ("+sources.joinToString(","){"?"}+")"
        val arguments=(listOf(uri)+sources).toTypedArray()
        val db=helper.writableDatabase
        db.beginTransaction()
        val outcome=try {
            var cleaned:String?=null
            val preserved=db.rawQuery("SELECT body,extraction FROM files WHERE $selection LIMIT 1",arguments).use { row ->
                if(!row.moveToFirst())false else {
                    val old=row.getString(1) ?: ""
                    document.coveredByCache(old,row.getString(0) ?: "").also { if(it){val next=ExtractedDocument.clearDeferral(old);if(next!=old)cleaned=next} }
                }
            }
            val updated=if(preserved)cleaned?.let{db.update("files",ContentValues().apply{put("extraction",it)},selection,arguments)>0} ?: false else db.update("files",values,selection,arguments)>0
            db.setTransactionSuccessful()
            preserved to updated
        } finally {db.endTransaction()}
        if(outcome.second)changes.value=changes.value+1
        outcome.first || outcome.second
    }

    suspend fun refresh(c: Config, analyze: Boolean = true) =
        withContext(Dispatchers.IO) {
            if(c.privateMode || !c.files){state.value=Status(message="Indexación desactivada por privacidad o preferencias",truncated=true);return@withContext}
            lock.withLock {
                val sharedSeen = mutableSetOf<String>()
                var count = 0
                var failures = 0
                var limited = false
                var failed = false
                var mediaCount = 0
                var ocrRemaining=12
                val ocrUntil=android.os.SystemClock.elapsedRealtime()+120000L
                var contentPartial=false
                val deferrals=linkedSetOf<String>()
                state.value =
                    Status(true, message = "Leyendo archivos compartidos y carpetas autorizadas…")
                val mode = "${c.contentIndex}:${c.visualIndex}:${c.videoIndex}:office1:pdfocr2:mime2"
                val analyzer =
                    if (analyze && (c.visualIndex || c.videoIndex)) MediaAnalysis(context) else null
                try {
                    val roots =
                        allowedTrees(c).map {
                            it to
                                runCatching { DocumentFile.fromTreeUri(context, Uri.parse(it)) }
                                    .getOrNull()
                        } +
                            if (broadAllowed())
                                sharedRoots().map { "__shared__" to DocumentFile.fromFile(it) }
                            else emptyList()
                    for ((tree, rootFolder) in roots) {
                        val seen = mutableSetOf<String>()
                        suspend fun walk(folder: DocumentFile, depth: Int) {
                            if (depth > 64 || count >= 200000) {
                                limited = true
                                return
                            }
                            val children = folder.listFiles()
                            if (
                                folder.uri.scheme == "file" &&
                                    java.io.File(folder.uri.path!!).listFiles() == null
                            ) {
                                failures++
                                failed = true
                                return
                            }
                            for (file in children) {
                                currentCoroutineContext().ensureActive()
                                check(sourceAllowed(tree,c)){"Permiso de carpeta revocado durante la indexación"}
                                if (count >= 200000) {
                                    limited = true
                                    return
                                }
                                if (file.uri.scheme == "file") {
                                    val native = java.io.File(file.uri.path!!)
                                    if (java.nio.file.Files.isSymbolicLink(native.toPath()))
                                        continue
                                }
                                val name = file.name ?: continue
                                if (
                                    file.uri.scheme == "file" &&
                                        file.uri.path?.let { blockedPath(it) } == true
                                )
                                    continue
                                val contentUri =
                                    if (file.uri.scheme == "file")
                                        androidx.core.content.FileProvider.getUriForFile(
                                            context,
                                            context.packageName + ".files",
                                            java.io.File(file.uri.path!!),
                                        )
                                    else file.uri
                                val uri = contentUri.toString()
                                seen += uri
                                if (tree == "__shared__") sharedSeen += uri
                                count++
                                val mime =
                                    if (file.isDirectory) DocumentsContract.Document.MIME_TYPE_DIR
                                    else OfficeText.mime(name,file.type) ?: "application/octet-stream"
                                val modified = file.lastModified()
                                val size = file.length()
                                var body = ""
                                var author = ""
                                var created = 0L
                                var oldMode = ""
                                var extraction = ""
                                var extractionPartial=false
                                var sourceUnchanged=false
                                var initialBody=""
                                var initialExtraction=""
                                helper.readableDatabase
                                    .query(
                                        "files",
                                        arrayOf(
                                            "body",
                                            "author",
                                            "created",
                                            "modified",
                                            "size",
                                            "mode",
                                            "extraction",
                                        ),
                                        "uri=?",
                                        arrayOf(uri),
                                        null,
                                        null,
                                        null,
                                    )
                                    .use { row ->
                                        if (row.moveToFirst()) {
                                            sourceUnchanged=row.getLong(3)==modified && row.getLong(4)==size
                                            body = row.getString(0) ?: ""
                                            author = row.getString(1) ?: ""
                                            created = row.getLong(2)
                                            oldMode = if(sourceUnchanged)row.getString(5) ?: "" else ""
                                            extraction = row.getString(6) ?: ""
                                            initialBody=body;initialExtraction=extraction
                                            if(!sourceUnchanged && body.isNotBlank())extraction=ExtractedDocument.deferredMetadata(extraction,"pending",stale=true)
                                        }
                                    }
                                var storedMode = oldMode
                                if (analyze && oldMode != mode && !file.isDirectory) {
                                    try {
                                        if (
                                            c.contentIndex &&
                                                size <= 8_000_000 &&
                                                mime == "application/pdf"
                                        ) {
                                            val initial=OcrBudget.evaluate(resourceState())
                                            if(initial.deferred!=null) {
                                                val reason=initial.deferred.code
                                                extraction=ExtractedDocument.deferredMetadata(extraction,reason,!sourceUnchanged && body.isNotBlank())
                                                extractionPartial=true;contentPartial=true;deferrals+=reason
                                            } else {
                                                val remaining=ocrUntil-android.os.SystemClock.elapsedRealtime()
                                                val limits=PdfTextLimits(ocrPages=if(remaining>0)minOf(3,ocrRemaining) else 0,
                                                    characters=100000,totalMillis=if(remaining>0)minOf(45000,remaining) else 45000)
                                                val budget={if(!sourceAllowed(tree,c))OcrBudgetDecision(deferred=OcrDeferral.PERMISSION,pages=0) else OcrBudget.evaluate(resourceState())}
                                                val previous=CachedPdfText(body,extraction)
                                                val doc=pdfExtraction?.invoke(contentUri,limits,budget,previous)
                                                    ?: DocumentText.extractDetailed(context,contentUri,mime,limits,budget,previous)
                                                currentCoroutineContext().ensureActive()
                                                if(doc.text.isBlank() && doc.partial && body.isNotBlank())extraction=ExtractedDocument.deferredMetadata(extraction,doc.deferrals.firstOrNull() ?: "pending",!sourceUnchanged)
                                                else if(!doc.coveredByCache(extraction,body)){body=doc.text;extraction=doc.metadata()}
                                                else extraction=ExtractedDocument.clearDeferral(extraction)
                                                author=doc.author;created=doc.created
                                                ocrRemaining=(ocrRemaining-doc.ocrAttempts).coerceAtLeast(0)
                                                extractionPartial=doc.partial;contentPartial=contentPartial || doc.partial
                                                deferrals+=doc.deferrals
                                                doc.deferrals.firstOrNull()?.let{extraction=ExtractedDocument.deferredMetadata(extraction,it)}
                                            }
                                        } else if (c.contentIndex && size<=8_000_000 && OfficeText.supports(mime,name)) {
                                            val content=DocumentText.extract(context,contentUri,OfficeText.mime(name,mime))
                                            body=content
                                        } else if (
                                            c.contentIndex &&
                                                (mime.startsWith("text/") ||
                                                    name.endsWith(".json", true) ||
                                                    name.endsWith(".xml", true) ||
                                                    name.endsWith(".sms", true) ||
                                                    name.endsWith(".eml", true) ||
                                                    name.endsWith(".srt", true) ||
                                                    name.endsWith(".vtt", true))
                                        ) {
                                            context.contentResolver
                                                .openInputStream(contentUri)
                                                ?.use { stream ->
                                                    val out = java.io.ByteArrayOutputStream()
                                                    val buffer = ByteArray(4096)
                                                    while (out.size() < 100000) {
                                                        val n =
                                                            stream.read(
                                                                buffer,
                                                                0,
                                                                minOf(
                                                                    buffer.size,
                                                                    100000 - out.size(),
                                                                ),
                                                            )
                                                        if (n < 0) break
                                                        out.write(buffer, 0, n)
                                                    }
                                                    body = out.toString("UTF-8")
                                                    author =
                                                        Regex(
                                                                "(?im)^(?:author|autor|from):\\s*(.+)$"
                                                            )
                                                            .find(body)
                                                            ?.groupValues
                                                            ?.get(1)
                                                            ?.take(300) ?: ""
                                                }
                                        } else if (
                                            analyzer != null &&
                                                mime.startsWith("image/") &&
                                                c.visualIndex &&
                                                mediaCount < 150
                                        ) {
                                            val metadata = ImageMetadata.read(context, contentUri)
                                            author = metadata.author
                                            created = metadata.created
                                            body = analyzer.image(contentUri, true)
                                            mediaCount++
                                        } else if (
                                            analyzer != null &&
                                                mime.startsWith("video/") &&
                                                c.videoIndex &&
                                                mediaCount < 150
                                        ) {
                                            body = analyzer.video(contentUri)
                                            mediaCount++
                                        } else if (
                                            (mime.startsWith("image/") && c.visualIndex) ||
                                                (mime.startsWith("video/") && c.videoIndex)
                                        ) {
                                            limited = true
                                        }
                                        storedMode =
                                            if (extractionPartial || mediaCount >= 150 && body.isEmpty()) "$mode:pending"
                                            else mode
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: Exception) {
                                        failures++
                                        storedMode = oldMode
                                    }
                                }
                                currentCoroutineContext().ensureActive()
                                if(!sourceAllowed(tree,c)){failed=true;failures++;continue}
                                val values =
                                    ContentValues().apply {
                                        put("uri", uri)
                                        put("tree", tree)
                                        put(
                                            "parent",
                                            if (folder.uri.scheme == "file")
                                                androidx.core.content.FileProvider.getUriForFile(
                                                        context,
                                                        context.packageName + ".files",
                                                        java.io.File(folder.uri.path!!),
                                                    )
                                                    .toString()
                                            else folder.uri.toString(),
                                        )
                                        put("name", name)
                    put("name_key", normalize(name))
                                        put("mime", mime)
                                        put("modified", modified)
                                        put("size", size)
                                        put("author", author)
                                        put("created", created)
                                        put("body", body)
                                        put("mode", storedMode)
                                        put("extraction", extraction)
                                    }
                                val db=helper.writableDatabase
                                db.beginTransaction()
                                try {
                                    db.rawQuery("SELECT body,extraction,author,created FROM files WHERE uri=?",arrayOf(uri)).use { live ->
                                        if(live.moveToFirst() && ((live.getString(0) ?: "")!=initialBody || (live.getString(1) ?: "")!=initialExtraction)) {
                                            // A manual extraction completed while this refresh suspended.
                                            // Preserve that committed result; retry from its newer snapshot.
                                            values.put("body",live.getString(0) ?: "")
                                            values.put("extraction",live.getString(1) ?: "")
                                            values.put("author",live.getString(2) ?: "")
                                            values.put("created",live.getLong(3))
                                            values.put("mode","$mode:pending")
                                            if(mime=="application/pdf")contentPartial=true
                                        }
                                    }
                                    db.insertWithOnConflict("files",null,values,SQLiteDatabase.CONFLICT_REPLACE)
                                    db.setTransactionSuccessful()
                                } finally {db.endTransaction()}
                                state.value =
                                    Status(true, count, "$count entradas · $name", limited)
                                if (file.isDirectory) walk(file, depth + 1)
                            }
                        }
                        try {
                            val root = rootFolder ?: error("Carpeta no disponible")
                            walk(root, 0)
                            // Prune only after a complete scan; keep entries if a provider fails or
                            // limits are reached.
                            if (!limited && tree != "__shared__") {
                                val stale = mutableListOf<String>()
                                helper.readableDatabase
                                    .query(
                                        "files",
                                        arrayOf("uri"),
                                        "tree=?",
                                        arrayOf(tree),
                                        null,
                                        null,
                                        null,
                                    )
                                    .use {
                                        while (it.moveToNext()) if (it.getString(0) !in seen)
                                            stale += it.getString(0)
                                    }
                                helper.writableDatabase.beginTransaction()
                                try {
                                    stale.forEach {
                                        helper.writableDatabase.delete(
                                            "files",
                                            "uri=?",
                                            arrayOf(it),
                                        )
                                    }
                                    helper.writableDatabase.setTransactionSuccessful()
                                } finally {
                                    helper.writableDatabase.endTransaction()
                                }
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            failures++
                            failed = true
                        }
                    }
                    if (broadAllowed() && !limited && !failed) {
                        val stale = mutableListOf<String>()
                        helper.readableDatabase
                            .query(
                                "files",
                                arrayOf("uri"),
                                "tree=?",
                                arrayOf("__shared__"),
                                null,
                                null,
                                null,
                            )
                            .use { rows ->
                                while (rows.moveToNext()) if (rows.getString(0) !in sharedSeen)
                                    stale += rows.getString(0)
                            }
                        stale.forEach {
                            helper.writableDatabase.delete("files", "uri=?", arrayOf(it))
                        }
                    }
                    lastBroad = broadAllowed()
                    lastTrees = c.trees
                    refreshed = System.currentTimeMillis()
                    state.value =
                        Status(
                            false,
                            count,
                            "$count entradas${if(contentPartial) " · contenido PDF parcial" else ""}${if(deferrals.isNotEmpty())" · OCR aplazado: "+deferrals.joinToString { OcrBudget.label(it) }+"; reintentar al mejorar los recursos" else ""}${if(limited) " · índice parcial (límites alcanzados)" else ""}${if(failures>0) " · $failures archivos/carpetas sin leer" else ""}",
                            limited || failed || contentPartial,
                        )
                } catch (e: CancellationException) {
                    state.value =
                        Status(false, count, "Indexación cancelada; resultados parciales", true)
                    throw e
                } finally {
                    // Completed rows commit independently. Never hold a DB transaction while
                    // awaiting native OCR or coroutine suspension; readers remain available.
                    analyzer?.close()
                    changes.value=changes.value+1
                }
            }
        }

    data class SemanticStatus(val busy:Boolean=false,val fragments:Int=0,val message:String="Búsqueda por significado sin preparar",val partial:Boolean=false)
    private val semanticMutable=MutableStateFlow(SemanticStatus())
    val semanticStatus=semanticMutable.asStateFlow()
    val semanticModel=dev.bautista.spotlight.semantic.SemanticModelStore(context)
    private val embedding:dev.bautista.spotlight.semantic.DocumentEmbedder=semanticBackend ?: dev.bautista.spotlight.semantic.LocalDocumentEmbedder(semanticModel)
    private val semanticLock=Mutex()
    private val semanticEpoch=java.util.concurrent.atomic.AtomicLong()
    @Volatile private var semanticJob:Job?=null
    private fun semanticConfig(c:Config)=if(liveConfig!=null)liveConfig.invoke() else c
    private fun semanticAllowed(c:Config?)=c!=null && c.files && c.contentIndex && c.semanticSearch && !c.privateMode
    private fun semanticSource(tree:String,c:Config)=semanticAllowed(c) && (tree=="__imports__" || sourceAllowed(tree,c))
    fun cancelSemanticWork(){semanticEpoch.incrementAndGet();semanticJob?.cancel();embedding.invalidate()}
    suspend fun clearSemantic()=withContext(Dispatchers.IO) {
        val job=semanticJob;cancelSemanticWork();job?.cancelAndJoin()
        helper.writableDatabase.delete("semantic_vectors",null,null)
        semanticMutable.value=SemanticStatus(message="Índice de significado eliminado; los archivos y el texto original se conservan")
        changes.value=changes.value+1
    }
    suspend fun refreshSemanticStatus()=withContext(Dispatchers.IO){if(!semanticMutable.value.busy){val count=semanticCount();semanticMutable.value=SemanticStatus(fragments=count,message="$count fragmentos locales conservados; requieren fuentes y modelo válidos")}}
    private fun semanticCount()=helper.readableDatabase.rawQuery("SELECT COUNT(*) FROM semantic_vectors WHERE model=?",arrayOf(dev.bautista.spotlight.semantic.SemanticPolicy.MODEL)).use{it.moveToFirst();it.getInt(0)}
    private data class SemanticFile(val uri:String,val tree:String,val name:String,val mime:String,val body:String,val extraction:String,val parent:String,val modified:Long,val created:Long,val author:String,val size:Long) {
        val text by lazy(LazyThreadSafetyMode.NONE){ExtractedDocument.searchableText(extraction,body)}
        val fingerprint by lazy(LazyThreadSafetyMode.NONE){dev.bautista.spotlight.semantic.SemanticPolicy.hash(name+"\u0000"+mime+"\u0000"+text+"\u0000"+extraction)}
    }
    private fun semanticFile(uri:String):SemanticFile?=helper.readableDatabase.query("files",null,"uri=?",arrayOf(uri),null,null,null,"1").use{row->
        if(!row.moveToFirst())return@use null
        fun s(k:String)=row.getString(row.getColumnIndexOrThrow(k)) ?: ""
        fun n(k:String)=row.getLong(row.getColumnIndexOrThrow(k))
        SemanticFile(s("uri"),s("tree"),s("name"),OfficeText.mime(s("name"),s("mime")) ?: "application/octet-stream",s("body"),s("extraction"),s("parent"),n("modified"),n("created"),s("author"),n("size"))
    }
    /** C5 read seam: reuse this index and its live SAF authority; never extract or open Quick Look. */
    internal suspend fun citationDocument(uri:String,c:Config):CitationDocument?=withContext(Dispatchers.IO) {
        fun allowed(info:SemanticFile):Boolean {
            val current=semanticConfig(c) ?: return false
            return current.files && current.contentIndex && !current.privateMode &&
                (info.tree=="__imports__" || sourceAllowed(info.tree,current))
        }
        val info=semanticFile(uri) ?: return@withContext null
        if(!allowed(info))return@withContext null
        val metadata=runCatching{org.json.JSONObject(info.extraction)}.getOrNull() ?: return@withContext null
        val sha=metadata.optString("sha256")
        if(metadata.optBoolean("stale") || !sha.matches(Regex("[0-9a-f]{64}")))return@withContext null
        try {
            val digest=java.security.MessageDigest.getInstance("SHA-256");var bytes=0
            context.contentResolver.openInputStream(Uri.parse(uri))?.use { input ->
                val buffer=ByteArray(8192)
                while(true){currentCoroutineContext().ensureActive();val n=input.read(buffer);if(n<0)break
                    bytes+=n;if(bytes>8_000_000)return@withContext null;digest.update(buffer,0,n)}
            } ?: return@withContext null
            if(digest.digest().joinToString(""){"%02x".format(it)}!=sha)return@withContext null
            val fresh=semanticFile(uri) ?: return@withContext null
            if(!allowed(fresh) || fresh.fingerprint!=info.fingerprint)return@withContext null
            CitationDocument(uri,info.name,info.mime,info.body,info.extraction,sha,info.fingerprint)
        } catch(e:CancellationException){throw e} catch(e:Exception){null}
    }

    private fun semanticCached(info:SemanticFile,part:dev.bautista.spotlight.semantic.WordPiece.Fragment)=helper.readableDatabase.rawQuery("SELECT start,end,excerpt,vector FROM semantic_vectors WHERE uri=? AND model=? AND fingerprint=? AND part=?",arrayOf(info.uri,dev.bautista.spotlight.semantic.SemanticPolicy.MODEL,info.fingerprint,part.index.toString())).use{r->r.moveToFirst() && r.getInt(0)==part.start && r.getInt(1)==part.end && r.getString(2)==part.text && dev.bautista.spotlight.semantic.SemanticPolicy.vector(r.getBlob(3))!=null}

    /** Explicit bounded preparation; never called by typing. Completed fragments survive cancellation. */
    suspend fun buildSemantic(c:Config)=withContext(Dispatchers.IO) {
        if(!semanticLock.tryLock())return@withContext
        val epoch=semanticEpoch.get();semanticJob=currentCoroutineContext()[Job]
        var partial=false;var reason="";val started=android.os.SystemClock.elapsedRealtime()
        try {
            check(semanticAllowed(semanticConfig(c))){"Activá archivos, contenido y búsqueda por significado fuera del modo privado."}
            dev.bautista.spotlight.semantic.SemanticPolicy.deferred(resourceState())?.let{error("Semántica aplazada: $it")}
            check(embedding.available()){"Instalá primero el modelo local verificado."}
            semanticMutable.value=SemanticStatus(true,semanticCount(),"Preparando fragmentos locales; sin enviar datos")
            val config=requireNotNull(semanticConfig(c));val sources=allowedTrees(config)+"__imports__"+if(broadAllowed())setOf("__shared__") else emptySet()
            val uris=helper.readableDatabase.rawQuery("SELECT uri FROM files WHERE tree IN ("+sources.joinToString(","){"?"}+") ORDER BY modified DESC LIMIT 201",sources.toTypedArray()).use{r->buildList{while(r.moveToNext())add(r.getString(0))}}
            helper.writableDatabase.delete("semantic_vectors","model<>? OR uri NOT IN (SELECT uri FROM files WHERE tree IN ("+sources.joinToString(","){"?"}+"))",(listOf(dev.bautista.spotlight.semantic.SemanticPolicy.MODEL)+sources).toTypedArray())
            if(uris.size>200)partial=true
            for(uri in uris.take(200)) {
                currentCoroutineContext().ensureActive()
                if(epoch!=semanticEpoch.get())throw CancellationException()
                val info=semanticFile(uri) ?: continue
                val fresh=semanticConfig(c)
                if(fresh==null || !semanticSource(info.tree,fresh)){partial=true;reason="permisos o preferencias cambiaron";break}
                if(!info.mime.startsWith("text/") && !OfficeText.supports(info.mime,info.name) && info.mime!="application/pdf")continue
                val text=info.text
                if(text.isBlank())continue
                helper.writableDatabase.delete("semantic_vectors","uri=? AND fingerprint<>?",arrayOf(uri,info.fingerprint))
                val fragments=embedding.fragments(text.take(100000))
                partial=partial || fragments.truncated || text.length>100000
                for(part in fragments.items) {
                    currentCoroutineContext().ensureActive()
                    val live=semanticConfig(c)
                    val deferred=dev.bautista.spotlight.semantic.SemanticPolicy.deferred(resourceState())
                    if(live==null || !semanticSource(info.tree,live) || epoch!=semanticEpoch.get()){partial=true;reason="permisos o preferencias cambiaron";break}
                    if(deferred!=null || android.os.SystemClock.elapsedRealtime()-started>60000){partial=true;reason=deferred ?: "límite de 60 segundos";break}
                    if(semanticCached(info,part))continue
                    if(semanticCount()>=dev.bautista.spotlight.semantic.SemanticPolicy.MAX_FRAGMENTS){partial=true;reason="límite de 1000 fragmentos";break}
                    val vector=embedding.embed(part.ids)
                    currentCoroutineContext().ensureActive()
                    if(vector==null){partial=true;reason="inferencia ocupada o plazo agotado";break}
                    val now=semanticFile(uri);val latest=semanticConfig(c)
                    if(now==null || now.fingerprint!=info.fingerprint || latest==null || !semanticSource(now.tree,latest) || epoch!=semanticEpoch.get()){partial=true;reason="fuente, permiso o contexto cambió";break}
                    val values=ContentValues().apply{put("uri",uri);put("model",dev.bautista.spotlight.semantic.SemanticPolicy.MODEL);put("fingerprint",info.fingerprint);put("part",part.index);put("start",part.start);put("end",part.end);put("excerpt",part.text);put("vector",dev.bautista.spotlight.semantic.SemanticPolicy.bytes(vector))}
                    val db=helper.writableDatabase;db.beginTransaction()
                    try {
                        currentCoroutineContext().ensureActive()
                        db.delete("semantic_vectors","uri=? AND (model<>? OR fingerprint<>?)",arrayOf(uri,dev.bautista.spotlight.semantic.SemanticPolicy.MODEL,info.fingerprint))
                        db.insertWithOnConflict("semantic_vectors",null,values,SQLiteDatabase.CONFLICT_REPLACE)
                        db.setTransactionSuccessful()
                    }finally{db.endTransaction()}
                    semanticMutable.value=SemanticStatus(true,semanticCount(),"${semanticCount()} fragmentos locales preparados",partial)
                }
                if(reason.isNotBlank())break
            }
            semanticMutable.value=SemanticStatus(false,semanticCount(),"${semanticCount()} fragmentos · "+(if(partial)"cobertura parcial; reanudar con Actualizar" else "preparación completada dentro de los límites")+(if(reason.isNotBlank())" · $reason" else ""),partial)
        }catch(e:CancellationException){semanticMutable.value=SemanticStatus(false,semanticCount(),"Preparación cancelada; fragmentos completados conservados",true);throw e}
        catch(e:Exception){semanticMutable.value=SemanticStatus(false,semanticCount(),e.message ?: "No se pudo preparar la búsqueda por significado",true)}
        catch(e:LinkageError){semanticMutable.value=SemanticStatus(false,semanticCount(),"Motor local no disponible en este dispositivo",true)}
        finally{semanticJob=null;semanticLock.unlock();changes.value=changes.value+1}
    }

    /** Returns only still-authorized, byte-matching fragments. Lexical results are published first. */
    suspend fun semanticSearch(input:String,c:Config,sourceTopic:String=""):List<Result> =withContext(Dispatchers.IO) {
        val epoch=semanticEpoch.get();val current=semanticConfig(c)
        if(!semanticAllowed(current))return@withContext emptyList()
        val parsed=dev.bautista.spotlight.semantic.SemanticPolicy.query(input) ?: return@withContext emptyList()
        if(dev.bautista.spotlight.semantic.SemanticPolicy.deferred(resourceState())!=null){embedding.invalidate();return@withContext emptyList()}
        if(semanticCount()==0)return@withContext emptyList()
        try {
            if(!embedding.available())return@withContext emptyList()
            val queryVector=embedding.query(parsed.text) ?: return@withContext emptyList()
            currentCoroutineContext().ensureActive()
            val fresh=semanticConfig(c) ?: return@withContext emptyList()
            if(!semanticAllowed(fresh) || epoch!=semanticEpoch.get())return@withContext emptyList()
            val sources=allowedTrees(fresh)+"__imports__"+if(broadAllowed())setOf("__shared__") else emptySet()
            data class Hit(val uri:String,val fingerprint:String,val start:Int,val end:Int,val excerpt:String,val score:Float)
            val hits=mutableMapOf<String,Hit>();val infoCache=mutableMapOf<String,SemanticFile?>()
            helper.readableDatabase.rawQuery("SELECT v.uri,v.fingerprint,v.start,v.end,v.excerpt,v.vector FROM semantic_vectors v JOIN files f ON f.uri=v.uri WHERE v.model=? AND f.tree IN ("+sources.joinToString(","){"?"}+") LIMIT 1000",(listOf(dev.bautista.spotlight.semantic.SemanticPolicy.MODEL)+sources).toTypedArray()).use{rows->
                while(rows.moveToNext()) {
                    currentCoroutineContext().ensureActive()
                    val uri=rows.getString(0);val info=infoCache.getOrPut(uri){semanticFile(uri)} ?: continue
                    if(info.fingerprint!=rows.getString(1) || !parsed.filter.matches(SearchQuery.Document(info.name,"document",info.mime,author=info.author,modified=info.modified,created=info.created)))continue
                    if(sourceTopic.isNotBlank() && !normalize(Uri.decode(info.uri+" "+info.parent+" "+info.name)).contains(normalize(sourceTopic)))continue
                    val start=rows.getInt(2);val end=rows.getInt(3);val excerpt=rows.getString(4)
                    if(start<0 || end<=start || end>info.text.length || info.text.substring(start,end)!=excerpt)continue
                    val vector=dev.bautista.spotlight.semantic.SemanticPolicy.vector(rows.getBlob(5)) ?: continue
                    val score=dev.bautista.spotlight.semantic.SemanticPolicy.dot(queryVector,vector)
                    if(score>=dev.bautista.spotlight.semantic.SemanticPolicy.MIN_SCORE && score>(hits[uri]?.score ?: -1f))hits[uri]=Hit(uri,info.fingerprint,start,end,excerpt,score)
                }
            }
            val best=hits.values.maxOfOrNull{it.score} ?: return@withContext emptyList()
            hits.values.sortedByDescending{it.score}.filter{it.score>=maxOf(dev.bautista.spotlight.semantic.SemanticPolicy.MIN_SCORE,best-.08f)}.take(8).mapNotNull{hit->
                currentCoroutineContext().ensureActive()
                val info=semanticFile(hit.uri) ?: return@mapNotNull null
                val latest=semanticConfig(c)
                if(latest==null || !semanticSource(info.tree,latest) || epoch!=semanticEpoch.get() || info.fingerprint!=hit.fingerprint)return@mapNotNull null
                val uri=Uri.parse(info.uri)
                val intent=Intent(Intent.ACTION_VIEW).setDataAndType(uri,info.mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                Result("file:$uri",info.name,"Por significado · fragmento local aproximado","Archivos",350+(hit.score*40).toInt(),Action.Launch(intent),preview=ExtractedDocument.provenance(info.extraction,info.body,hit.excerpt)+"Fragmento ${hit.start}–${hit.end} del texto indexado; cobertura acotada, no lectura completa.\n"+hit.excerpt,mime=info.mime,parentUri=info.parent.takeIf{it.isNotBlank()},sizeBytes=info.size,modifiedAt=info.modified.takeIf{it>0},semanticMatch=true)
            }
        }catch(e:CancellationException){throw e}catch(_:Exception){emptyList()}catch(_:LinkageError){emptyList()}
    }

    suspend fun search(input: String, c: Config, sourceTopic:String="",recentDocuments:Boolean=false): List<Result> =
        withContext(Dispatchers.IO) {
            val normalizedQuery = SearchQuery.natural(input)
            val query = SearchQuery.parse(normalizedQuery)
            val filter = dev.bautista.spotlight.search.FileSqlFilter.from(normalizedQuery)
            if (query.problem != null) return@withContext emptyList()

            val sources =
                allowedTrees(c) +
                    "__imports__" +
                    if (broadAllowed()) setOf("__shared__") else emptySet()
            val out = mutableListOf<Result>()
            val seen=mutableSetOf<String>()
            val selection="tree IN ("+sources.joinToString(","){"?"}+")"+
                (if(filter.selection.isBlank())"" else " AND ("+filter.selection+")")+
                if(recentDocuments)" AND modified > 0 AND (mime LIKE 'application/%' OR mime LIKE 'text/%')" else ""
            val arguments=sources.toList()+filter.args
            data class Pass(val selection:String,val arguments:List<String>,val order:String)
            fun like(text:String)=text.replace("\\","\\\\").replace("%","\\%").replace("_","\\_")
            val nameWords=normalize(query.plain).split(Regex("\\s+")).filter{it.isNotBlank()}.take(12)
            val passes=buildList {
                if(!recentDocuments && query.plain.length in 2..128 && nameWords.isNotEmpty()) {
                    // Filename candidates first, before the bounded content/date fallback.
                    // Unicode keys live in this same index, never another file catalogue.
                    val names=nameWords.joinToString(" AND "){"name_key LIKE ? ESCAPE '\\'"}
                    add(Pass("$selection AND ($names)",arguments+nameWords.map{"%${like(it)}%"}+
                        listOf(normalize(query.plain),like(normalize(query.plain))+"%"),
                        "CASE WHEN name_key = ? THEN 0 WHEN name_key LIKE ? ESCAPE '\\' THEN 1 ELSE 2 END, modified DESC, uri ASC"))
                }
                add(Pass(selection,arguments,"modified DESC, uri ASC"))
            }
            for(pass in passes) {
                ensureActive()
                if(out.size>=80)break
                helper.readableDatabase.query("files",null,pass.selection,pass.arguments.toTypedArray(),null,null,
                    pass.order,if(recentDocuments)"32" else "200000")
                .use { row ->
                    fun string(key: String) = row.getString(row.getColumnIndexOrThrow(key)) ?: ""
                    fun long(key: String) = row.getLong(row.getColumnIndexOrThrow(key))
                    while (row.moveToNext()) {
                        ensureActive()
                        if(sourceTopic.isNotBlank() && !dev.bautista.spotlight.search.normalize(Uri.decode(string("uri")+" "+string("parent")+" "+string("name"))).contains(dev.bautista.spotlight.search.normalize(sourceTopic)))continue
                        val name = string("name")
                        val mime = OfficeText.mime(name,string("mime")) ?: "application/octet-stream"
                        val body =
                            if (
                                c.contentIndex ||
                                    (c.visualIndex && mime.startsWith("image/")) ||
                                    (c.videoIndex && mime.startsWith("video/")) ||
                                    mime.startsWith("audio/")
                            )
                                string("body")
                            else ""
                        val contentBody=ExtractedDocument.searchableText(string("extraction"),body)
                        val kind =
                            when {
                                mime == DocumentsContract.Document.MIME_TYPE_DIR -> "folder"
                                mime.startsWith("image/") -> "image"
                                mime.startsWith("video/") -> "video"
                                mime.startsWith("audio/") -> "audio"
                                name.endsWith(".eml", true) -> "email"
                                name.endsWith(".sms", true) ||
                                    name.endsWith(".messages.txt", true) -> "message"
                                else -> "document"
                            }
                        if (
                            !query.matches(
                                SearchQuery.Document(
                                    name,
                                    kind,
                                    mime,
                                    contentBody,
                                    string("author"),
                                    long("modified"),
                                    long("created"),
                                )
                            )
                        )
                            continue
                        val uri = Uri.parse(string("uri"))
                        if(!seen.add(uri.toString()))continue
                        val intent =
                            Intent(Intent.ACTION_VIEW)
                                .setDataAndType(uri, mime)
                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        val date =
                            long("modified")
                                .takeIf { it > 0 }
                                ?.let {
                                    java.time.Instant.ofEpochMilli(it)
                                        .atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDate()
                                        .toString()
                                } ?: "fecha no disponible"
                        val preview =
                            ExtractedDocument.provenance(string("extraction"),body,query.plain)+"${if(string("author").isNotBlank()) "Autor: ${string("author")}\n" else ""}Modificado: $date\nTamaño: ${long("size")} bytes\n${DocumentExcerpt.around(contentBody,query.plain)}"
                        val firstMoment =
                            if (kind == "video")
                                body
                                    .lineSequence()
                                    .firstOrNull { line ->
                                        SearchQuery.parse(query.plain)
                                            .matches(SearchQuery.Document("", kind, text = line))
                                    }
                                    ?.let {
                                        Regex("^\\[(\\d+)\\]")
                                            .find(it)
                                            ?.groupValues
                                            ?.get(1)
                                            ?.toLongOrNull()
                                            ?.times(1000)
                                    }
                            else null
                        out +=
                            Result(
                                "file:$uri",
                                name,
                                "$date · ${if(kind=="folder") "Carpeta" else mime}",
                                "Archivos",
                                maxOf(400, relevance(query.plain, name)),
                                Action.Launch(intent),
                                preview = preview,
                                mime = mime,
                                parentUri = string("parent").takeIf { it.isNotBlank() },
                                positionMs = firstMoment,
                                sizeBytes=long("size"),modifiedAt=long("modified").takeIf{it>0},
                            )
                        if (out.size >= 80) break
                    }
                }
            }
            out.sortedWith(compareByDescending<Result>{it.score}.thenByDescending{it.modifiedAt ?: 0L}.thenBy{it.id})
        }
}
