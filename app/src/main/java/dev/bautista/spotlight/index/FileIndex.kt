package dev.bautista.spotlight.index

import android.content.*
import android.database.sqlite.*
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
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
        SQLiteOpenHelper(context, "search-index.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE files(uri TEXT PRIMARY KEY,tree TEXT,parent TEXT,name TEXT,mime TEXT,modified INTEGER,size INTEGER,author TEXT,created INTEGER,body TEXT,mode TEXT)"
            )
        }

        override fun onOpen(db:SQLiteDatabase) {
            super.onOpen(db)
            db.execSQL("CREATE INDEX IF NOT EXISTS files_tree_modified ON files(tree,modified DESC)")
            db.execSQL("CREATE INDEX IF NOT EXISTS files_tree_created ON files(tree,created)")
            db.execSQL("CREATE INDEX IF NOT EXISTS files_tree_mime ON files(tree,mime)")
        }
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
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

    private fun allowedTrees(c: Config): Set<String> =
        if (broadAllowed())
            c.trees
                .filterNot { Uri.parse(it).authority == "com.android.externalstorage.documents" }
                .toSet()
        else c.trees

    suspend fun ensure(c: Config) {
        observe(c.trees + if(broadAllowed())setOf(android.provider.MediaStore.Files.getContentUri("external").toString()) else emptySet())
        if (
            broadAllowed() != lastBroad ||
                c.trees != lastTrees ||
                System.currentTimeMillis() - refreshed > 600000
        )
            if (broadAllowed()) {
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
            } else refresh(c, false)
    }

    suspend fun hasImports():Boolean=withContext(Dispatchers.IO){helper.readableDatabase.rawQuery("SELECT 1 FROM files WHERE tree = ? LIMIT 1",arrayOf("__imports__")).use{it.moveToFirst()}}

    fun cancelRefresh() {
        refreshJob?.cancel()
    }

    suspend fun clear() =
        withContext(Dispatchers.IO) {
            refreshJob?.cancelAndJoin()
            lock.withLock {
                helper.writableDatabase.delete("files", null, null)
                lastTrees = emptySet()
                refreshed = 0L
                state.value = Status(message = "Índice borrado")
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

    suspend fun refresh(c: Config, analyze: Boolean = true) =
        withContext(Dispatchers.IO) {
            lock.withLock {
                val sharedSeen = mutableSetOf<String>()
                var count = 0
                var failures = 0
                var limited = false
                var failed = false
                var mediaCount = 0
                state.value =
                    Status(true, message = "Leyendo archivos compartidos y carpetas autorizadas…")
                val mode = "${c.contentIndex}:${c.visualIndex}:${c.videoIndex}:office1"
                val analyzer =
                    if (analyze && (c.visualIndex || c.videoIndex)) MediaAnalysis(context) else null
                if (analyze && c.contentIndex) PDFBoxResourceLoader.init(context)
                helper.writableDatabase.beginTransaction()
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
                                    else file.type ?: "application/octet-stream"
                                val modified = file.lastModified()
                                val size = file.length()
                                var body = ""
                                var author = ""
                                var created = 0L
                                var oldMode = ""
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
                                        ),
                                        "uri=?",
                                        arrayOf(uri),
                                        null,
                                        null,
                                        null,
                                    )
                                    .use { row ->
                                        if (
                                            row.moveToFirst() &&
                                                row.getLong(3) == modified &&
                                                row.getLong(4) == size
                                        ) {
                                            body = row.getString(0) ?: ""
                                            author = row.getString(1) ?: ""
                                            created = row.getLong(2)
                                            oldMode = row.getString(5) ?: ""
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
                                            context.contentResolver
                                                .openInputStream(contentUri)
                                                ?.use { stream ->
                                                    PDDocument.load(stream).use { pdf ->
                                                        val info = pdf.documentInformation
                                                        author = info.author ?: ""
                                                        created =
                                                            info.creationDate?.timeInMillis ?: 0L
                                                        body =
                                                            PDFTextStripper()
                                                                .apply {
                                                                    endPage =
                                                                        minOf(pdf.numberOfPages, 40)
                                                                }
                                                                .getText(pdf)
                                                                .take(100000)
                                                    }
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
                                            if (mediaCount >= 150 && body.isEmpty()) oldMode
                                            else mode
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: Exception) {
                                        failures++
                                        storedMode = oldMode
                                    }
                                }
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
                                        put("mime", mime)
                                        put("modified", modified)
                                        put("size", size)
                                        put("author", author)
                                        put("created", created)
                                        put("body", body)
                                        put("mode", storedMode)
                                    }
                                helper.writableDatabase.insertWithOnConflict(
                                    "files",
                                    null,
                                    values,
                                    SQLiteDatabase.CONFLICT_REPLACE,
                                )
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
                            "$count entradas${if(limited) " · índice parcial (límites alcanzados)" else ""}${if(failures>0) " · $failures archivos/carpetas sin leer" else ""}",
                            limited || failed,
                        )
                } catch (e: CancellationException) {
                    state.value =
                        Status(false, count, "Indexación cancelada; resultados parciales", true)
                    throw e
                } finally {
                    // Each complete row is useful even when a long scan is cancelled.
                    helper.writableDatabase.setTransactionSuccessful()
                    helper.writableDatabase.endTransaction()
                    analyzer?.close()
                }
            }
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
            helper.readableDatabase
                .query(
                    "files",
                    null,
                    "tree IN (" + sources.joinToString(",") { "?" } + ")" + (if(filter.selection.isBlank())"" else " AND ("+filter.selection+")") + if(recentDocuments)" AND modified > 0 AND (mime LIKE 'application/%' OR mime LIKE 'text/%')" else "",
                    (sources.toList()+filter.args).toTypedArray(),
                    null,
                    null,
                    "modified DESC",
                    if(recentDocuments)"32" else "200000",
                )
                .use { row ->
                    fun string(key: String) = row.getString(row.getColumnIndexOrThrow(key)) ?: ""
                    fun long(key: String) = row.getLong(row.getColumnIndexOrThrow(key))
                    while (row.moveToNext()) {
                        ensureActive()
                        if(sourceTopic.isNotBlank() && !dev.bautista.spotlight.search.normalize(Uri.decode(string("uri")+" "+string("parent")+" "+string("name"))).contains(dev.bautista.spotlight.search.normalize(sourceTopic)))continue
                        val mime = string("mime")
                        val name = string("name")
                        val body =
                            if (
                                c.contentIndex ||
                                    (c.visualIndex && mime.startsWith("image/")) ||
                                    (c.videoIndex && mime.startsWith("video/")) ||
                                    mime.startsWith("audio/")
                            )
                                string("body")
                            else ""
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
                                    body,
                                    string("author"),
                                    long("modified"),
                                    long("created"),
                                )
                            )
                        )
                            continue
                        val uri = Uri.parse(string("uri"))
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
                            "${if(string("author").isNotBlank()) "Autor: ${string("author")}\n" else ""}Modificado: $date\nTamaño: ${long("size")} bytes\n${body.take(3000)}"
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
            out
        }
}
