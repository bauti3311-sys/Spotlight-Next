package dev.bautista.spotlight.search

import android.net.Uri
import android.text.Html
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.*
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.index.awaitResult
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.*

data class InfoCard(
    val title: String,
    val subtitle: String,
    val body: String,
    val copyText: String = body,
    val url: String? = null,
    val rows: List<Pair<String, String>> = emptyList(),
)

class OnlineTools {
    private val client =
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    private val cache = mutableMapOf<String, Pair<Long, InfoCard>>()

    suspend fun get(url: String): String = suspendCancellableCoroutine { c ->
        val call =
            client.newCall(
                Request.Builder()
                    .url(url)
                    .header("User-Agent", "SpotlightAndroid/0.3 (personal native search)")
                    .build()
            )
        c.invokeOnCancellation { call.cancel() }
        call.enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: java.io.IOException) {
                    if (c.isActive) c.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use { r ->
                        try {
                            require(r.isSuccessful) { "Servicio respondió ${r.code}" }
                            val body = r.body ?: error("Respuesta vacía")
                            val input = body.byteStream()
                            val output = java.io.ByteArrayOutputStream()
                            val buffer = ByteArray(8192)
                            while (output.size() < 1_500_000) {
                                val n =
                                    input.read(
                                        buffer,
                                        0,
                                        minOf(buffer.size, 1_500_000 - output.size()),
                                    )
                                if (n < 0) break
                                output.write(buffer, 0, n)
                            }
                            require(output.size() < 1_500_000) { "Respuesta demasiado extensa" }
                            if (c.isActive) c.resume(output.toString("UTF-8"))
                        } catch (e: Exception) {
                            if (c.isActive) c.resumeWithException(e)
                        }
                    }
                }
            }
        )
    }

    fun request(query: String): Action.Lookup? {
        val q = query.trim()
        val n = normalize(q)
        val currency =
            Regex(
                    "^([+-]?\\d+(?:[.,]\\d+)?)\\s*([a-z]{3})\\s+(?:a|en|to)\\s+([a-z]{3})$",
                    RegexOption.IGNORE_CASE,
                )
                .matchEntire(q)
        if (currency != null && currency.groupValues.drop(2).all { it.uppercase() in currencies })
            return Action.Lookup("currency", q)
        for ((prefix, type) in
            listOf(
                "clima en " to "weather",
                "clima " to "weather",
                "definir " to "dictionary",
                "wiki " to "wiki",
                "wikipedia " to "wiki",
                "deportes " to "sports",
            )) if (n.startsWith(prefix) && q.length > prefix.length)
            return Action.Lookup(type, q.drop(prefix.length))
        if (Regex("^[A-Z]{2}\\d{1,4}$", RegexOption.IGNORE_CASE).matches(q))
            return Action.Lookup("flight", q.uppercase())
        if (n.startsWith("vuelo ")) return Action.Lookup("flight", q.drop(6).uppercase())
        if (n.startsWith("traducir ")) return Action.Lookup("translate", q.drop(9))
        return null
    }

    suspend fun lookup(action: Action.Lookup, c: Config): InfoCard {
        if(action.type.startsWith("text:")) return withContext(Dispatchers.Default) { val op=action.type.removePrefix("text:");val output=TextTools.transform(op,action.query);InfoCard(TextTools.operations.getValue(op),"Procesado localmente",output,output) }
        if(action.type=="uuid") {val id=java.util.UUID.randomUUID().toString();return InfoCard("UUID","Generado localmente",id,id)}
        val key = "${action.type}:${action.query}:${c.liveEndpoint}"
        synchronized(cache) {
            cache[key]
                ?.takeIf { System.currentTimeMillis() - it.first < 600000 }
                ?.let {
                    return it.second
                }
        }
        val card =
            when (action.type) {
                "currency" -> currency(action.query)
                "weather" -> weather(action.query)
                "dictionary" -> dictionary(action.query)
                "wiki" -> wiki(action.query)
                "translate" -> translate(action.query)
                "flight",
                "sports" -> live(action, c)
                else -> error("Consulta no compatible")
            }
        synchronized(cache) {
            if (cache.size >= 30) cache.remove(cache.keys.first())
            cache[key] = System.currentTimeMillis() to card
        }
        return card
    }

    private suspend fun currency(q: String): InfoCard {
        val m =
            Regex(
                    "^([+-]?\\d+(?:[.,]\\d+)?)\\s*([a-z]{3})\\s+(?:a|en|to)\\s+([a-z]{3})$",
                    RegexOption.IGNORE_CASE,
                )
                .matchEntire(q) ?: error("Usá 100 USD a EUR")
        val amount = m.groupValues[1].replace(',', '.').toDouble()
        val from = m.groupValues[2].uppercase()
        val to = m.groupValues[3].uppercase()
        if (from == to)
            return InfoCard(
                "${LocalTools.format(amount)} $to",
                "Misma moneda",
                "No requiere conversión",
                LocalTools.format(amount),
            )
        val data = JSONObject(get("https://api.frankfurter.dev/v2/rate/$from/$to"))
        return parseCurrency(data, amount, from, to)
    }

    private suspend fun weather(city: String): InfoCard {
        val places =
            JSONObject(
                    get(
                        "https://geocoding-api.open-meteo.com/v1/search?name=${Uri.encode(city)}&count=5&language=es&format=json"
                    )
                )
                .optJSONArray("results") ?: error("No se encontró esa ubicación")
        if (places.length() == 0) error("No se encontró esa ubicación")
        val p = places.getJSONObject(0)
        val lat = p.getDouble("latitude")
        val lon = p.getDouble("longitude")
        val data =
            JSONObject(
                get(
                    "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m&hourly=temperature_2m,precipitation_probability&daily=temperature_2m_max,temperature_2m_min,weather_code&forecast_days=5&timezone=auto"
                )
            )
        return parseWeather(
            data,
            listOf(p.optString("name"), p.optString("admin1"), p.optString("country"))
                .filter { it.isNotBlank() }
                .joinToString(", "),
            lat,
            lon,
        )
    }

    private suspend fun dictionary(word: String): InfoCard {
        val url =
            "https://es.wiktionary.org/w/api.php?action=parse&page=${Uri.encode(word.trim())}&prop=text&format=json&formatversion=2&redirects=1"
        val data = JSONObject(get(url))
        val page =
            data.optJSONObject("parse")
                ?: error("Sin entrada en Wikcionario; probá buscar en la web")
        val html = page.getString("text").replace(Regex("<table[\\s\\S]*?</table>"), "")
        val text =
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
                .toString()
                .replace(Regex("\\n{3,}"), "\n\n")
                .trim()
                .take(6000)
        return InfoCard(
            page.optString("title", word),
            "Wikcionario · CC BY-SA · contenido de la entrada",
            text,
            url = "https://es.wiktionary.org/wiki/" + Uri.encode(word),
        )
    }

    private suspend fun wiki(term: String): InfoCard {
        val search =
            JSONObject(
                get(
                    "https://es.wikipedia.org/w/api.php?action=query&list=search&srsearch=${Uri.encode(term)}&srlimit=1&format=json"
                )
            )
        val title =
            search
                .getJSONObject("query")
                .getJSONArray("search")
                .optJSONObject(0)
                ?.getString("title") ?: error("Sin resultados en Wikipedia")
        val data =
            JSONObject(
                get("https://es.wikipedia.org/api/rest_v1/page/summary/${Uri.encode(title)}")
            )
        return InfoCard(
            data.optString("title", title),
            "Wikipedia · CC BY-SA",
            data.optString("extract").take(6000),
            url = data.optJSONObject("content_urls")?.optJSONObject("desktop")?.optString("page"),
        )
    }

    private suspend fun translate(query: String): InfoCard {
        val natural=Regex("^(.+) al? (ingles|espanol|frances|portugues|italiano|aleman)$",RegexOption.DOT_MATCHES_ALL).matchEntire(normalize(query.trim()))
        val naturalText=if(natural!=null)query.trim().take(natural.groupValues[1].length).trim() else null
        val naturalTo=natural?.groupValues?.get(2)?.let{mapOf("ingles" to "en","espanol" to "es","frances" to "fr","portugues" to "pt","italiano" to "it","aleman" to "de")[it]}
        val match =
            Regex("^([a-z]{2}) a ([a-z]{2}):?\\s+(.+)$", RegexOption.DOT_MATCHES_ALL)
                .matchEntire(query.trim())
        val explicitFrom = match?.groupValues?.get(1)
        val identifier = com.google.mlkit.nl.languageid.LanguageIdentification.getClient()
        val from =
            try {
                explicitFrom ?: identifier.identifyLanguage(naturalText ?: query).awaitResult()
            } finally {
                identifier.close()
            }
        val to = match?.groupValues?.get(2) ?: naturalTo ?: "es"
        val text = match?.groupValues?.get(3) ?: naturalText ?: query
        require(
            from in TranslateLanguage.getAllLanguages() && to in TranslateLanguage.getAllLanguages()
        ) {
            "No se pudo identificar un idioma compatible. Usá traducir en a es: hello"
        }
        if (from == to) return InfoCard(text, "Mismo idioma", text)
        val translator =
            Translation.getClient(
                TranslatorOptions.Builder().setSourceLanguage(from).setTargetLanguage(to).build()
            )
        return try {
            translator
                .downloadModelIfNeeded(DownloadConditions.Builder().requireWifi().build())
                .awaitResult()
            val answer = translator.translate(text.take(4000)).awaitResult()
            InfoCard(
                answer,
                "Traducción local $from → $to · ML Kit",
                answer,
                answer,
                url = "https://translate.google.com/?sl=$from&tl=$to&text=${Uri.encode(text)}",
            )
        } finally {
            translator.close()
        }
    }

    private suspend fun live(action: Action.Lookup, c: Config): InfoCard {
        val base =
            c.liveEndpoint.toHttpUrlOrNull()
                ?: error(
                    "Configurá un backend de datos en vivo en Opciones; también podés buscarlo en la web"
                )
        require(base.isHttps && base.username.isEmpty() && base.password.isEmpty())
        val url =
            base
                .newBuilder()
                .addQueryParameter("type", action.type)
                .addQueryParameter("q", action.query)
                .build()
                .toString()
        val data = JSONObject(get(url))
        val rows =
            data.optJSONArray("rows")?.let { a ->
                (0 until minOf(a.length(), 40)).map {
                    a.getJSONObject(it).getString("label") to a.getJSONObject(it).getString("value")
                }
            } ?: emptyList()
        return InfoCard(
            data.optString("title", action.query),
            data.optString("source") + " · " + data.optString("updated_at"),
            data.optString("body"),
            url = data.optString("url").takeIf { it.startsWith("https://") },
            rows = rows,
        )
    }

    companion object {
        private val currencies =
            java.util.Currency.getAvailableCurrencies().map { it.currencyCode }.toSet()

        fun parseCurrency(data: JSONObject, amount: Double, from: String, to: String): InfoCard {
            val rate = data.getDouble("rate")
            require(rate > 0 && rate.isFinite() && amount.isFinite())
            val result = LocalTools.format(amount * rate)
            return InfoCard(
                "$result $to",
                "Frankfurter · referencia ${data.getString("date")}",
                "$amount $from = $result $to\n1 $from = ${LocalTools.format(rate)} $to\nCotización de referencia diaria; no es una cotización bancaria ni de mercado informal.",
                result,
                url = "https://frankfurter.dev/",
            )
        }

        fun parseWeather(data: JSONObject, place: String, lat: Double, lon: Double): InfoCard {
            val current = data.getJSONObject("current")
            val hourly = data.getJSONObject("hourly")
            val daily = data.getJSONObject("daily")
            val rows = mutableListOf<Pair<String, String>>()
            val hours = hourly.getJSONArray("time")
            val now = current.getString("time")
            var start =
                (0 until hours.length()).firstOrNull { hours.getString(it) >= now.take(13) + ":00" }
                    ?: 0
            for (i in start until minOf(start + 12, hours.length())) rows +=
                hours.getString(i).substringAfter('T') to
                    "${hourly.getJSONArray("temperature_2m").getDouble(i)} °C · ${hourly.getJSONArray("precipitation_probability").optInt(i)}% lluvia"
            val days = daily.getJSONArray("time")
            for (i in 0 until minOf(5, days.length())) rows +=
                days.getString(i) to
                    "${daily.getJSONArray("temperature_2m_min").getDouble(i)} / ${daily.getJSONArray("temperature_2m_max").getDouble(i)} °C · ${weatherLabel(daily.getJSONArray("weather_code").getInt(i))}"
            return InfoCard(
                "${current.getDouble("temperature_2m")} °C · ${weatherLabel(current.getInt("weather_code"))}",
                place,
                "Sensación ${current.getDouble("apparent_temperature")} °C · humedad ${current.getInt("relative_humidity_2m")}%\nViento ${current.getDouble("wind_speed_10m")} km/h\nOpen-Meteo · pronóstico, no medición personal · ${current.getString("time")} ${data.optString("timezone")}",
                url = "https://www.google.com/maps/search/?api=1&query=$lat,$lon",
                rows = rows,
            )
        }

        private fun weatherLabel(code: Int) =
            when (code) {
                0 -> "Despejado"
                1,
                2 -> "Parcialmente nublado"
                3 -> "Nublado"
                45,
                48 -> "Niebla"
                51,
                53,
                55,
                56,
                57 -> "Llovizna"
                61,
                63,
                65,
                66,
                67,
                80,
                81,
                82 -> "Lluvia"
                71,
                73,
                75,
                77,
                85,
                86 -> "Nieve"
                95,
                96,
                99 -> "Tormenta"
                else -> "Condiciones variables"
            }
    }
}
