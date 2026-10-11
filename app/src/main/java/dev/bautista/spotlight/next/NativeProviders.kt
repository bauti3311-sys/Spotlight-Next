package dev.bautista.spotlight.next

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.*
import org.json.*
import java.util.concurrent.TimeUnit
import java.time.LocalDate
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class NativeSnapshot(val json:JSONObject,val fetchedAt:Long,val stale:Boolean=false,val warning:String="")
/** User-triggered, bounded HTTPS calls, cancellable and persistent per-request cache. */
class NativeProviders(context:Context,private val fetcher:(suspend (HttpUrl)->JSONObject)?=null,private val clock:()->Long={System.currentTimeMillis()}) {
    private val prefs=context.getSharedPreferences("native_provider_cache_v1",Context.MODE_PRIVATE)
    private val client=OkHttpClient.Builder().connectTimeout(10,TimeUnit.SECONDS).readTimeout(15,TimeUnit.SECONDS).callTimeout(20,TimeUnit.SECONDS).build()
    private val mutex=Mutex()
    private suspend fun json(url:HttpUrl):JSONObject=suspendCancellableCoroutine { continuation ->
        val call=client.newCall(Request.Builder().url(url).header("User-Agent","SpotlightNext/1.1 (personal Android client)").build())
        continuation.invokeOnCancellation{call.cancel()}
        call.enqueue(object:Callback {
            override fun onFailure(call:Call,e:java.io.IOException){if(continuation.isActive)continuation.resumeWithException(e)}
            override fun onResponse(call:Call,response:Response){try{response.use {check(it.isSuccessful){"El proveedor respondió HTTP ${it.code}."};val body=it.body ?: error("Respuesta vacía.");check(body.contentLength()<=1_000_000);val source=body.source();source.request(1_000_001);check(source.buffer.size<=1_000_000){"Respuesta demasiado grande."};val bytes=source.readByteArray();val value=JSONObject(bytes.toString(Charsets.UTF_8));if(continuation.isActive)continuation.resume(value)}}catch(e:Exception){if(continuation.isActive)continuation.resumeWithException(e)}}
        })
    }
    private suspend fun cached(key:String,ttl:Long,refresh:Boolean,url:HttpUrl):NativeSnapshot=mutex.withLock {
        val previous=runCatching{JSONObject(prefs.getString(key,null) ?: "")}.getOrNull()
        val time=previous?.optLong("saved") ?: 0L;val now=clock()
        if(previous!=null && now>=time && now-time<if(refresh)30_000L else ttl)return@withLock NativeSnapshot(previous.getJSONObject("body"),time)
        try {val data=fetcher?.invoke(url) ?: json(url);val saved=clock();prefs.edit().putString(key,JSONObject().put("saved",saved).put("body",data).toString()).apply();if(prefs.all.size>64){val older=prefs.all.keys.filter{it!=key}.sortedBy{runCatching{JSONObject(prefs.getString(it,"").orEmpty()).optLong("saved")}.getOrDefault(0)};val editor=prefs.edit();older.take(prefs.all.size-64).forEach{editor.remove(it)};editor.apply()};NativeSnapshot(data,saved)}
        catch(e:CancellationException){throw e}catch(e:Exception){if(previous!=null)NativeSnapshot(previous.getJSONObject("body"),time,true,"No se pudo actualizar. Se muestran datos guardados.")else throw IllegalStateException("No se pudo consultar el proveedor. Comprobá conexión y probá de nuevo.",e)}
    }
    suspend fun weather(city:String="",refresh:Boolean=false):NativeSnapshot {
        val name=city.trim().take(100);var lat=-38.0023;var lon=-57.5575;var label="Mar del Plata"
        if(name.isNotBlank() && dev.bautista.spotlight.search.normalize(name)!="mar del plata"){
            val geo=cached("geo:$name",7*86400_000L,false,HttpUrl.Builder().scheme("https").host("geocoding-api.open-meteo.com").addPathSegments("v1/search").addQueryParameter("name",name).addQueryParameter("count","1").addQueryParameter("language","es").build())
            val place=geo.json.optJSONArray("results")?.optJSONObject(0) ?: error("No se encontró esa localidad.")
            lat=place.getDouble("latitude");lon=place.getDouble("longitude");label=listOf(place.getString("name"),place.optString("admin1"),place.optString("country")).filter{it.isNotBlank()}.distinct().joinToString(", ")
        }
        val data=cached("weather:$lat:$lon",15*60_000L,refresh,HttpUrl.Builder().scheme("https").host("api.open-meteo.com").addPathSegments("v1/forecast")
            .addQueryParameter("latitude",lat.toString()).addQueryParameter("longitude",lon.toString()).addQueryParameter("timezone","auto")
            .addQueryParameter("current","temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,wind_gusts_10m")
            .addQueryParameter("hourly","temperature_2m,precipitation_probability")
            .addQueryParameter("daily","temperature_2m_max,temperature_2m_min,precipitation_probability_max,uv_index_max,sunrise,sunset")
            .addQueryParameter("forecast_days","5").build())
        return data.copy(json=JSONObject(data.json.toString()).put("location_name",label))
    }
    suspend fun sports(sport:String="Soccer",date:LocalDate=LocalDate.now(),refresh:Boolean=false):NativeSnapshot {
        require(sport in setOf("Soccer","Basketball","Motorsport","Tennis","Baseball"))
        return cached("sports:$sport:$date",5*60_000L,refresh,HttpUrl.Builder().scheme("https").host("www.thesportsdb.com").addPathSegments("api/v1/json/123/eventsday.php").addQueryParameter("d",date.toString()).addQueryParameter("s",sport).build())
    }
}
object ProviderValues {
    fun number(j:JSONObject,key:String,suffix:String=""):String=if(j.isNull(key) || !j.has(key))"—" else j.optDouble(key).takeIf{it.isFinite()}?.let {"${java.math.BigDecimal.valueOf(it).stripTrailingZeros().toPlainString()}$suffix"} ?: "—"
    fun score(j:JSONObject):String { val home=j.optString("intHomeScore").takeUnless{it.isBlank() || it=="null"};val away=j.optString("intAwayScore").takeUnless{it.isBlank() || it=="null"};return if(home!=null && away!=null)"$home — $away" else "Marcador no informado" }
    fun condition(code:Int)=when(code){0->"Despejado";1,2->"Parcialmente nublado";3->"Nublado";45,48->"Niebla";51,53,55,56,57->"Llovizna";61,63,65,66,67,80,81,82->"Lluvia";71,73,75,77,85,86->"Nieve";95,96,99->"Tormenta";else->"Condición no informada"}
}
