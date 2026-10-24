package dev.bautista.spotlight.integrations.cast

import java.net.InetAddress
import java.net.URI
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/** Pure HTTP policy for ONE explicitly granted SAF file. This does not start a server
 * or discovery. A Sender adapter must bind lifetime/address and close the lease. */
class LocalMediaRequestPolicy(
    val contentUri:String,val size:Long,val mimeType:String,private val receiverAddress:String,
    private val createdAt:Long,private val ttlMillis:Long=600000,
) : AutoCloseable {
    private val token=ByteArray(32).also{SecureRandom().nextBytes(it)}.let{Base64.getUrlEncoder().withoutPadding().encodeToString(it)}
    val path get()="/media/$token"
    @Volatile private var closed=false
    init {
        val authorizedUri=runCatching{URI(contentUri)}.getOrElse{error("URI autorizada inválida.")}
        require(authorizedUri.scheme=="content" && !authorizedUri.authority.isNullOrBlank())
        require(size>0 && ttlMillis in 1000..1800000 && createdAt>=0)
        require(mimeType in CastMediaPolicy.mimeTypes){"Formato no preparado para Cast."}
        require(receiverAddress.matches(Regex("[0-9]{1,3}(\\.[0-9]{1,3}){3}")) &&
            receiverAddress.split('.').all{it.toInt() in 0..255} && InetAddress.getByName(receiverAddress).isSiteLocalAddress){"Se requiere la dirección IPv4 privada del receptor seleccionado."}
    }
    override fun toString()="LocalMediaRequestPolicy(redacted)"
    override fun close(){closed=true}
    fun authorize(method:String,rawPath:String,remoteAddress:String,range:String?,now:Long):LocalMediaGrant {
        check(!closed && now>=createdAt && now-createdAt<ttlMillis){"El acceso temporal venció o se cerró."}
        require(method in setOf("GET","HEAD") && remoteAddress==receiverAddress){"Solicitud no autorizada."}
        require(rawPath.length==path.length && MessageDigest.isEqual(rawPath.toByteArray(),path.toByteArray())){"Ruta no autorizada."}
        val bytes=if(range==null)ByteRange(0,size-1) else ByteRange.parse(range,size)
        return LocalMediaGrant(if(range==null)200 else 206,bytes.start,bytes.length,method!="HEAD",
            mapOf("Content-Type" to mimeType,"Content-Length" to bytes.length.toString(),"Accept-Ranges" to "bytes",
                "Cache-Control" to "no-store","X-Content-Type-Options" to "nosniff")+
                if(range==null)emptyMap() else mapOf("Content-Range" to "bytes ${bytes.start}-${bytes.endInclusive}/$size"))
    }
}
data class LocalMediaGrant(val status:Int,val offset:Long,val length:Long,val includeBody:Boolean,val headers:Map<String,String>)
data class ByteRange(val start:Long,val endInclusive:Long) {
    init {require(start>=0 && endInclusive>=start)}
    val length get()=Math.addExact(endInclusive-start,1L)
    companion object {
        /** Single RFC7233 range only. A server must translate rejection to HTTP416,
         * never silently return the entire private file. */
        fun parse(header:String,size:Long):ByteRange {
            require(size>0 && header.length<=80)
            val match=Regex("bytes=([0-9]*)-([0-9]*)").matchEntire(header) ?: error("Rango no compatible.")
            val first=match.groupValues[1];val last=match.groupValues[2]
            require(first.isNotEmpty() || last.isNotEmpty())
            if(first.isEmpty()) {
                val suffix=last.toLongOrNull() ?: error("Rango inválido.");require(suffix>0)
                return ByteRange(if(suffix>=size)0 else size-suffix,size-1)
            }
            val start=first.toLongOrNull() ?: error("Rango inválido.");require(start<size)
            val end=if(last.isEmpty())size-1 else minOf(last.toLongOrNull() ?: error("Rango inválido."),size-1)
            require(end>=start);return ByteRange(start,end)
        }
    }
}
object CastMediaPolicy {
    // Transport eligibility only: actual codec/receiver support must still be observed.
    val mimeTypes=setOf("video/mp4","video/webm","audio/mpeg","audio/mp4","audio/ogg","audio/flac","image/jpeg","image/png","image/webp")
    fun directUrl(url:String,mimeType:String,explicitlyAuthorized:Boolean,protectedContent:Boolean=false):URI {
        require(explicitlyAuthorized && !protectedContent && mimeType in mimeTypes){"Contenido no autorizado o formato no preparado."}
        require(url.length<=4000)
        val uri=runCatching{URI(url)}.getOrElse{error("URL multimedia inválida.")}
        require(uri.scheme=="https" && !uri.host.isNullOrBlank() && uri.rawUserInfo==null && uri.rawFragment==null){"Se requiere una URL multimedia HTTPS directa."}
        require(uri.rawQuery.isNullOrBlank()){"Los enlaces con credenciales o parámetros privados requieren una integración específica."}
        require(uri.host !in setOf("localhost","127.0.0.1","::1"))
        return uri
    }
}
