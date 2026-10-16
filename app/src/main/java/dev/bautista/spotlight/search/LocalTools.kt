package dev.bautista.spotlight.search

import java.time.*
import java.time.temporal.ChronoUnit
import kotlin.math.abs

object LocalTools {
    data class Answer(val title: String, val subtitle: String)

    private data class UnitDef(val family: String, val factor: Double)

    private val units =
        buildMap<String, UnitDef> {
            fun add(f: String, k: Double, vararg aliases: String) {
                for (a in aliases) put(a, UnitDef(f, k))
            }
            add("longitud", 1.0, "m", "metro", "metros")
            add("longitud", 1000.0, "km", "kilometros")
            add("longitud", .01, "cm", "centimetros")
            add("longitud", .001, "mm")
            add("longitud", 1609.344, "mi", "milla", "millas", "miles")
            add("longitud", .3048, "ft", "pie", "pies", "feet")
            add("longitud", .0254, "in", "pulgadas")
            add("longitud", .9144, "yd", "yardas")
            add("masa", 1.0, "kg", "kilogramos")
            add("masa", .001, "g", "gramos")
            add("masa", .45359237, "lb", "libras", "lbs")
            add("masa", .028349523125, "oz", "onzas")
            add("volumen", 1.0, "l", "litros")
            add("volumen", .001, "ml")
            add("volumen", 3.785411784, "gal", "galones")
            add("area",1.0,"m2","m²");add("area",.0001,"cm2","cm²");add("area",1e6,"km2","km²");add("area",10000.0,"ha","hectareas");add("area",.09290304,"ft2","ft²");add("area",4046.8564224,"acre","acres")
            add("velocidad",1.0,"m/s");add("velocidad",1.0/3.6,"km/h","kmh");add("velocidad",.44704,"mph");add("velocidad",.5144444444,"nudos","knots")
            add("volumen",1000.0,"m3","m³")
            add("tiempo", 1.0, "s", "segundos")
            add("tiempo", 60.0, "min", "minutos")
            add("tiempo", 3600.0, "h", "horas")
            add("tiempo", 86400.0, "dias")
            add("datos", 1.0, "b", "bytes")
            add("datos", 1000.0, "kb")
            add("datos", 1e6, "mb")
            add("datos", 1e9, "gb")
            add("datos", 1024.0, "kib")
            add("datos", 1048576.0, "mib")
            add("datos", 1073741824.0, "gib")
        }

    fun convert(query: String): Answer? {
        if (query.length > 240) return null
        val spoken = normalize(query.trim()).trim('¿', '?').trim()
            .replace(Regex("^(?:converti|convertir|convierte|cuanto es)\\s+"), "")
        val question = Regex("^cuant[oa]s?\\s+([a-z°²³0-9/]+)\\s+son\\s+([+-]?\\d+(?:[.,]\\d+)?)\\s*([a-z°²³0-9/]+)$")
            .matchEntire(spoken)
        val expression = question?.let { "${it.groupValues[2]} ${it.groupValues[3]} a ${it.groupValues[1]}" } ?: spoken
        val m =
            Regex("^([+-]?\\d+(?:[.,]\\d+)?)\\s*([a-z°²³0-9/]+)\\s+(?:a|en|to|in)\\s+([a-z°²³0-9/]+)$")
                .matchEntire(expression) ?: return null
        val n = m.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
        val a = m.groupValues[2].removePrefix("°")
        val b = m.groupValues[3].removePrefix("°")
        val temps = setOf("c", "f", "k", "celsius", "fahrenheit", "kelvin")
        val value =
            if (a in temps && b in temps) {
                val c =
                    when (a) {
                        "f",
                        "fahrenheit" -> (n - 32) * 5 / 9
                        "k",
                        "kelvin" -> n - 273.15
                        else -> n
                    }
                if (c < -273.15) return null
                when (b) {
                    "f",
                    "fahrenheit" -> c * 9 / 5 + 32
                    "k",
                    "kelvin" -> c + 273.15
                    else -> c
                }
            } else {
                val from = units[a] ?: return null
                val to = units[b] ?: return null
                if (from.family != to.family) return null
                n * from.factor / to.factor
            }
        if (!value.isFinite()) return null
        return Answer("${format(value)} ${m.groupValues[3]}", "Conversión · $query")
    }

    fun format(v: Double): String =
        java.math.BigDecimal.valueOf(if (abs(v) < 1e-12) 0.0 else v)
            .setScale(8, java.math.RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()

    private val zones =
        mapOf(
            "buenos aires" to "America/Argentina/Buenos_Aires",
            "argentina" to "America/Argentina/Buenos_Aires",
            "madrid" to "Europe/Madrid",
            "paris" to "Europe/Paris",
            "londres" to "Europe/London",
            "nueva york" to "America/New_York",
            "new york" to "America/New_York",
            "tokio" to "Asia/Tokyo",
            "japon" to "Asia/Tokyo",
            "sydney" to "Australia/Sydney",
            "mexico" to "America/Mexico_City",
            "santiago" to "America/Santiago",
            "roma" to "Europe/Rome",
            "berlin" to "Europe/Berlin",
            "los angeles" to "America/Los_Angeles",
            "beijing" to "Asia/Shanghai",
        )

    fun clock(query: String, instant: Instant = Instant.now()): Answer? {
        val q = normalize(query.trim())
        if (q == "fecha" || q == "hoy")
            return Answer(
                instant.atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                "Fecha local",
            )
        val city =
            if (q.startsWith("hora en ")) q.removePrefix("hora en ")
            else if (q == "hora") "local" else return null
        val zone =
            if (city == "local") ZoneId.systemDefault()
            else
                zones[city]?.let { ZoneId.of(it) }
                    ?: runCatching { ZoneId.of(query.trim().substring(8)) }.getOrNull()
                    ?: return null
        val time = instant.atZone(zone)
        return Answer(
            time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
            "${zone.id} · ${time.toLocalDate()}",
        )
    }

    fun dateDifference(query: String): Answer? {
        val m =
            Regex("^dias entre (\\d{4}-\\d{2}-\\d{2}) y (\\d{4}-\\d{2}-\\d{2})$")
                .matchEntire(normalize(query.trim())) ?: return null
        return runCatching {
                Answer(
                    "${ChronoUnit.DAYS.between(LocalDate.parse(m.groupValues[1]),LocalDate.parse(m.groupValues[2]))} días",
                    "Diferencia entre fechas",
                )
            }
            .getOrNull()
    }

    fun validQuickLink(alias: String, url: String): Boolean {
        if (
            !Regex("^[a-z][a-z0-9]{0,15}$").matches(alias) || url.length > 2000 || url.contains('|')
        )
            return false
        return runCatching {
                val uri = java.net.URI(url.replace("{q}", "query"))
                uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null
            }
            .getOrDefault(false)
    }
}
