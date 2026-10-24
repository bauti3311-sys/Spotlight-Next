package dev.bautista.spotlight.integrations.spotify

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject

enum class SpotifyItemType(val api:String,val collection:String) {
    TRACK("track","tracks"),ARTIST("artist","artists"),ALBUM("album","albums"),PLAYLIST("playlist","playlists")
}
data class SpotifySearchItem(val type:SpotifyItemType,val id:String,val name:String,val subtitle:String) {
    init {require(id.matches(Regex("[A-Za-z0-9]{22}")) && name.isNotBlank() && name.length<=300 && subtitle.length<=600)}
    val uri get()="spotify:${type.api}:$id"
    val url get()="https://open.spotify.com/${type.api}/$id"
}
data class SpotifySearchPage(val items:List<SpotifySearchItem>,val observedAt:Long,val query:String)
class SpotifyApiException(val status:Int,val retryAfterSeconds:Int?):IOException(when(status){
    401->"Spotify requiere volver a autorizar el acceso.";403->"Spotify no habilitó esta operación para la cuenta o aplicación."
    429->"Spotify limitó las consultas. Esperá antes de reintentar.";else->"Spotify no pudo completar la consulta ($status)."
})

/** Explicit invocation only. No credentials in URL, redirects, caching or automatic retries. */
class SpotifySearchClient internal constructor(client:OkHttpClient,private val apiBase:HttpUrl) {
    constructor():this(OkHttpClient(),"https://api.spotify.com/".toHttpUrl())
    private val http=client.newBuilder().followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false)
        .callTimeout(15,TimeUnit.SECONDS).build()
    init {
        require(apiBase.toString()=="https://api.spotify.com/" || apiBase.scheme=="http" && apiBase.host in setOf("127.0.0.1","localhost","::1"))
    }
    internal fun request(query:String,types:Set<SpotifyItemType>,accessToken:String,offset:Int):Request {
        require(query.isNotBlank() && query.length<=240 && '\u0000' !in query)
        require(types.isNotEmpty() && offset in 0..1000)
        require(accessToken.length in 1..4096 && accessToken.matches(Regex("[A-Za-z0-9._~+/-]+=*"))){"Autorización Spotify inválida."}
        val url=apiBase.newBuilder().addPathSegments("v1/search").addQueryParameter("q",query.trim())
            .addQueryParameter("type",types.sortedBy{it.ordinal}.joinToString(","){it.api})
            .addQueryParameter("limit","10").addQueryParameter("offset",offset.toString()).build()
        return Request.Builder().url(url).header("Authorization","Bearer $accessToken").header("Accept","application/json").build()
    }
    suspend fun search(query:String,types:Set<SpotifyItemType>,accessToken:String,offset:Int=0):SpotifySearchPage {
        val text=read(request(query,types,accessToken,offset))
        return withContext(Dispatchers.Default){SpotifySearchPage(parse(text,types),System.currentTimeMillis(),query.trim())}
    }
    private suspend fun read(request:Request):String=suspendCancellableCoroutine { continuation ->
        val call=http.newCall(request)
        continuation.invokeOnCancellation{call.cancel()}
        call.enqueue(object:Callback {
            override fun onFailure(call:Call,e:IOException){if(continuation.isActive)continuation.resumeWithException(IOException("No se pudo conectar con Spotify."))}
            override fun onResponse(call:Call,response:Response) {
                try {
                    val text=response.use { r ->
                        if(!r.isSuccessful)throw SpotifyApiException(r.code,r.header("Retry-After")?.toIntOrNull()?.coerceIn(0,3600))
                        val body=r.body ?: throw IOException("Spotify devolvió una respuesta vacía.")
                        require(body.contentLength()<=1048576){"Respuesta Spotify demasiado grande."}
                        body.byteStream().use { input ->
                            val output=ByteArrayOutputStream();val buffer=ByteArray(8192)
                            while(true){val count=input.read(buffer);if(count<0)break;require(output.size()+count<=1048576){"Respuesta Spotify demasiado grande."};output.write(buffer,0,count)}
                            output.toString("UTF-8")
                        }
                    }
                    if(continuation.isActive)continuation.resume(text)
                } catch(e:Exception){if(continuation.isActive)continuation.resumeWithException(e)}
            }
        })
    }
    companion object {
        internal fun parse(raw:String,types:Set<SpotifyItemType>):List<SpotifySearchItem> {
            require(raw.length<=1048576)
            val root=JSONObject(raw);val results=mutableListOf<SpotifySearchItem>()
            for(type in types.sortedBy{it.ordinal}) {
                val items=root.optJSONObject(type.collection)?.optJSONArray("items") ?: continue
                for(i in 0 until minOf(items.length(),10)) {
                    val item=items.optJSONObject(i) ?: continue
                    val id=item.optString("id");val name=item.optString("name").take(300)
                    if(!id.matches(Regex("[A-Za-z0-9]{22}")) || name.isBlank() || item.optString("type")!=type.api || item.optString("uri")!="spotify:${type.api}:$id")continue
                    val subtitle=when(type) {
                        SpotifyItemType.PLAYLIST->item.optJSONObject("owner")?.optString("display_name").orEmpty()
                        SpotifyItemType.TRACK,SpotifyItemType.ALBUM->item.optJSONArray("artists")?.let{a->(0 until minOf(a.length(),3)).mapNotNull{a.optJSONObject(it)?.optString("name")}.joinToString(", ")}.orEmpty()
                        else->"Artista"
                    }.take(600)
                    results+=SpotifySearchItem(type,id,name,subtitle)
                }
            }
            return results.distinctBy{it.uri}
        }
    }
}
