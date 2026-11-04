package dev.bautista.spotlight.sharing

import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import dev.bautista.spotlight.control.IntegrationRegistry
import dev.bautista.spotlight.data.FileBuffer
import dev.bautista.spotlight.index.OfficeText
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*

/** Validation/metadata for the existing FileBuffer and NativeLaunchSession, never an executor. */
internal object FileShareContract {
    const val MAX_FILES=12
    private const val UNSAFE_GRANTS=Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
    enum class Problem(val label:String) {
        INVALID("Revisá la selección de archivos, su origen y su tipo."),
        ACCESS("El permiso de lectura venció. Elegí el archivo nuevamente con Android."),
        MISSING("El archivo ya no admite lectura autorizada. Seleccionalo nuevamente con Android."),
        NO_TARGET("No hay aplicaciones accesibles para compartir este tipo de archivo.")
    }
    class Failure(val problem:Problem):Exception(problem.label)
    fun mime(name:String,declared:String?):String {
        val value=declared?.trim()?.lowercase(java.util.Locale.ROOT)
        val concrete=value?.takeIf{it.matches(Regex("[a-z0-9!#$&^_.+-]+/[a-z0-9!#$&^_.+-]+"))}
        return if(concrete!=null && concrete !in setOf("application/octet-stream","binary/octet-stream"))concrete
            else OfficeText.mime(name,concrete) ?: "application/octet-stream"
    }
    fun canRead(context:Context,uri:Uri):Boolean=runCatching {
        if(uri.scheme!="content" || uri.authority.isNullOrBlank())return@runCatching false
        if(context.checkUriPermission(uri,android.os.Process.myPid(),android.os.Process.myUid(),Intent.FLAG_GRANT_READ_URI_PERMISSION)==PackageManager.PERMISSION_GRANTED)return@runCatching true
        context.packageManager.resolveContentProvider(uri.authority!!,0)?.let {
            it.packageName==context.packageName && it.enabled && it.grantUriPermissions
        }==true
    }.getOrDefault(false)
    fun canKeep(context:Context,uri:Uri):Boolean=runCatching {
        val own=context.packageManager.resolveContentProvider(uri.authority.orEmpty(),0)?.let{it.packageName==context.packageName && it.grantUriPermissions}==true
        own || context.contentResolver.persistedUriPermissions.any{it.isReadPermission &&
            (it.uri==uri || android.provider.DocumentsContract.isTreeUri(it.uri) && it.uri.authority==uri.authority && uri.pathSegments.take(it.uri.pathSegments.size)==it.uri.pathSegments)}
    }.getOrDefault(false)
    fun forwardClip(context:Context,clip:ClipData):ClipData {
        require(clip.itemCount in 1..MAX_FILES)
        val uris=(0 until clip.itemCount).map{index->val item=clip.getItemAt(index);require(item.intent==null);requireNotNull(item.uri)}
        require(uris.all{canRead(context,it)})
        return canonicalClip(uris.distinct())
    }
    @Suppress("DEPRECATION")
    fun chooserPayload(intent:Intent):Intent? {
        if(intent.action!=Intent.ACTION_CHOOSER)return null
        if(listOf(Intent.EXTRA_INITIAL_INTENTS,Intent.EXTRA_ALTERNATE_INTENTS,Intent.EXTRA_REPLACEMENT_EXTRAS).any{intent.hasExtra(it)})return null
        return (intent.extras?.get(Intent.EXTRA_INTENT) as? Intent)?.takeIf{it.action in setOf(Intent.ACTION_SEND,Intent.ACTION_SEND_MULTIPLE) && it.selector==null}
    }
    @Suppress("DEPRECATION")
    fun streams(intent:Intent):List<Uri> {
        val extra=intent.extras?.get(Intent.EXTRA_STREAM)
        val values=when(intent.action) {
            Intent.ACTION_SEND->listOf(requireNotNull(extra as? Uri){"Archivo no válido."})
            Intent.ACTION_SEND_MULTIPLE->{require(extra is ArrayList<*> && extra.size in 1..MAX_FILES){"Selección no válida."};extra.map{requireNotNull(it as? Uri){"Archivo no válido."}}}
            else->error("Acción no compatible.")
        }
        require(values.distinct().size==values.size && values.all{it.scheme=="content" && !it.authority.isNullOrBlank()}){"Usá documentos autorizados de Android."}
        return values
    }
    fun validFilePayload(intent:Intent):Boolean=runCatching {
        val uris=streams(intent)
        require(intent.selector==null && intent.data==null && !intent.type.isNullOrBlank())
        require(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION!=0 && intent.flags and UNSAFE_GRANTS==0)
        val clip=requireNotNull(intent.clipData)
        require(clip.itemCount==uris.size)
        require(uris.indices.all{val item=clip.getItemAt(it);item.uri==uris[it] && item.intent==null && item.text==null})
        true
    }.getOrDefault(false)
    fun canonicalClip(uris:List<Uri>,mimes:List<String> = listOf("application/octet-stream")):ClipData {
        require(uris.size in 1..MAX_FILES && uris.distinct().size==uris.size && uris.all{it.scheme=="content" && !it.authority.isNullOrBlank()})
        return ClipData("Archivos seleccionados",mimes.distinct().toTypedArray(),ClipData.Item(uris.first())).apply{uris.drop(1).forEach{addItem(ClipData.Item(it))}}
    }
    fun canonicalRows(context:Context,items:List<Result>):List<Result> {
        val buffer=FileBuffer();items.forEach{require(buffer.add(it))}
        val uris=streams(buffer.share(context))
        return buffer.items.zip(uris).map{(item,uri)->item.copy(action=Action.Launch(Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri,item.mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)))}
    }
    fun selectionId(items:List<Result>):String {
        val value=org.json.JSONArray(items.map{listOf(it.id,(it.action as? Action.Launch)?.intent?.data?.toString(),it.title,it.mime,it.sizeBytes,it.modifiedAt)}).toString()
        return java.security.MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8)).joinToString(""){"%02x".format(it)}
    }
    data class Document(val uri:Uri,val name:String,val mime:String,val size:Long?,val origin:String)
    data class Prepared(val documents:List<Document>,val send:Intent,val excluded:List<ComponentName> = emptyList()) {
        fun chooser()=Intent.createChooser(Intent(send),"Compartir archivos").also {
            if(excluded.isNotEmpty())it.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS,excluded.toTypedArray())
        }
    }
    suspend fun describe(context:Context,item:Result,uri:Uri,checkOpen:Boolean):Document=withContext(Dispatchers.IO) {
        if(!canRead(context,uri))throw Failure(Problem.ACCESS)
        currentCoroutineContext().ensureActive()
        val providerMime=context.contentResolver.getType(uri)
        currentCoroutineContext().ensureActive()
        val metadata=context.contentResolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE),null,null,null)?.use { row ->
            require(row.moveToFirst()){"El archivo ya no está disponible."}
            val nameIndex=row.getColumnIndex(OpenableColumns.DISPLAY_NAME);val sizeIndex=row.getColumnIndex(OpenableColumns.SIZE)
            val name=if(nameIndex>=0 && !row.isNull(nameIndex))row.getString(nameIndex) else item.title
            (name?.takeIf{it.isNotBlank()} ?: "Nombre no disponible").take(300) to
                if(sizeIndex>=0 && !row.isNull(sizeIndex))row.getLong(sizeIndex).takeIf{it>=0} else null
        } ?: error("No se pudieron revisar los metadatos del archivo.")
        currentCoroutineContext().ensureActive()
        val mime=mime(metadata.first,providerMime?.takeIf{it.isNotBlank()} ?: item.mime)
        require(mime!=android.provider.DocumentsContract.Document.MIME_TYPE_DIR && mime.matches(Regex("[a-z0-9!#$&^_.+*-]+/[a-z0-9!#$&^_.+*-]+"))){"Tipo de archivo no compatible."}
        if(checkOpen)context.contentResolver.openFileDescriptor(uri,"r")?.use{} ?: error("El archivo no admite lectura autorizada. Seleccionalo nuevamente con Android.")
        currentCoroutineContext().ensureActive()
        val persisted=context.contentResolver.persistedUriPermissions.any{it.isReadPermission && (it.uri==uri || android.provider.DocumentsContract.isTreeUri(it.uri) && it.uri.authority==uri.authority && uri.pathSegments.take(it.uri.pathSegments.size)==it.uri.pathSegments)}
        Document(uri,metadata.first,mime,metadata.second,(if(persisted)"SAF persistente" else "Lectura temporal o propia")+" · "+uri.authority)
    }
    suspend fun prepare(context:Context,items:List<Result>,packageName:String?=null):Prepared=withContext(Dispatchers.IO) {
        try {
        require(items.size in 1..MAX_FILES){"Seleccioná entre1 y12 archivos."}
        val buffer=FileBuffer();items.forEach{require(buffer.add(it)){"La selección contiene un elemento no compatible."}}
        val outgoing=streams(buffer.share(context,packageName))
        val documents=buffer.items.zip(outgoing).map{(item,uri)->currentCoroutineContext().ensureActive();describe(context,item,uri,true)}
        val refreshed=FileBuffer()
        documents.forEach{doc->refreshed.add(result(doc))}
        val send=refreshed.share(context,packageName)
        if(!IntegrationRegistry(context).hasCompatibleShareTarget(send))throw Failure(Problem.NO_TARGET)
        currentCoroutineContext().ensureActive()
        val excluded=context.packageManager.queryIntentActivities(send,PackageManager.MATCH_DEFAULT_ONLY)
            .mapNotNull{it.activityInfo}.filter{it.packageName==context.packageName}
            .map{ComponentName(it.packageName,it.name)}.distinct().sortedBy{it.flattenToString()}
        Prepared(documents,send,excluded)
        }catch(e:CancellationException){throw e}
        catch(e:Failure){throw e}
        catch(_:SecurityException){throw Failure(Problem.ACCESS)}
        catch(_:java.io.FileNotFoundException){throw Failure(Problem.MISSING)}
        catch(_:Exception){throw Failure(Problem.INVALID)}
    }
    fun result(doc:Document)=Result("file:${doc.uri}",doc.name,"${doc.mime} · origen seleccionado","Archivos",400,
        Action.Launch(Intent(Intent.ACTION_VIEW).setDataAndType(doc.uri,doc.mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)),
        mime=doc.mime,sizeBytes=doc.size)
}
