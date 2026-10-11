package dev.bautista.spotlight.control

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import dev.bautista.spotlight.search.*
import java.text.Collator
import java.util.Locale

object CommandCenter {
    val examples =
        listOf(
            "ayuda",
            "herramientas de texto",
            "mayúsculas: ",
            "limpiar espacios: ",
            "formatear JSON: ",
            "simbolos ",
            "preguntar a IA",
            "snippets",
            "snippet ",
            "notas",
            "whatsapp a ",
            "youtube ",
            "sofascore ",
            "spotify ",
            "mapa ",
            "navegar a ",
            "llamar ",
            "sms ",
            "email ",
            "temporizador de 5 minutos",
            "alarma 07:00",
            "crear una nota nueva",
            "evento ",
            "wifi",
            "bluetooth",
            "pantalla",
            "sonido",
            "bateria",
            "almacenamiento",
            "notificaciones",
            "accesibilidad",
            "ubicacion",
            "vpn",
            "fecha y hora",
            "seguridad",
            "modo avion",
            "kind:pdf",
            "kind:image",
            "kind:video",
            "kind:audio",
            "kind:folders",
            "subir volumen",
            "bajar volumen",
            "silenciar",
            "activar sonido",
            "pausar musica",
            "reproducir musica",
            "siguiente cancion",
            "cancion anterior",
        )

    fun complete(query: String, results: List<Result>): String? {
        if (query.isBlank()) return null
        val candidates =
            results
                .filter { it.category == "Aplicaciones" || it.category == "Archivos" }
                .map { it.title } + examples
        val match =
            candidates.firstOrNull {
                normalize(it).startsWith(normalize(query)) && it.length > query.length
            }
        return match?.let { query + it.drop(query.length) }
    }

    fun orderedApps(items: List<Result>, recent: List<String>): List<Result> {
        val collator = Collator.getInstance(Locale.forLanguageTag("es-AR"))
        val alphabetical =
            items.sortedWith { a, b ->
                collator.compare(a.title, b.title).let { if (it == 0) a.id.compareTo(b.id) else it }
            }
        val latest =
            recent.distinct().mapNotNull { id -> items.firstOrNull { it.id == id } }.take(5)
        // All applications remain available after the five recent entries.
        return latest.map { it.copy(subtitle = "Reciente · abrir aplicación") } + alphabetical
    }

    fun recent(context: Context, fallback: List<String>, apps: List<Result>): List<String> {
        return runCatching {
                val now = System.currentTimeMillis()
                val stats =
                    context
                        .getSystemService(UsageStatsManager::class.java)
                        .queryAndAggregateUsageStats(now - 7L * 86400000, now)
                val packages =
                    stats.values
                        .filter { it.lastTimeUsed > 0 }
                        .sortedByDescending { it.lastTimeUsed }
                        .map { it.packageName }
                val ids =
                    packages.mapNotNull { pkg ->
                        apps.firstOrNull { it.id.substringBefore('/') == pkg }?.id
                    }
                (ids + fallback).distinct()
            }
            .getOrDefault(fallback)
    }

    fun moveGrid(current: Int, down: Boolean, columns: Int, recentCount: Int, size: Int): Int {
        if (size <= 0) return 0
        val next =
            if (down) {
                if (current < recentCount && current + columns >= recentCount)
                    recentCount + current % columns
                else current + columns
            } else {
                if (recentCount > 0 && current >= recentCount && current - columns < recentCount)
                    minOf(
                        ((recentCount - 1) / columns) * columns + (current - recentCount) % columns,
                        recentCount - 1,
                    )
                else current - columns
            }
        return next.coerceIn(0, size - 1)
    }

    fun preferred(intent: Intent): Intent? {
        if(intent.`package`!=null || intent.component!=null)return null
        val host = intent.data?.host ?: return null
        return when {
            host.endsWith("youtube.com") && intent.data?.path == "/results" ->
                Intent(Intent.ACTION_SEARCH)
                    .setPackage("com.google.android.youtube")
                    .putExtra(
                        android.app.SearchManager.QUERY,
                        intent.data?.getQueryParameter("search_query"),
                    )
            host == "wa.me" -> Intent(intent).setPackage("com.whatsapp")
            host == "open.spotify.com" -> Intent(intent).setPackage("com.spotify.music")
            else -> null
        }
    }

    fun commands(query: String): List<Result> {
        val q = query.trim()
        val n = normalize(q)
        val out = mutableListOf<Result>()
        fun add(id: String, title: String, detail: String, intent: Intent) {
            out += Result("integration:$id", title, detail, "Comandos", 1850, Action.Launch(intent))
        }
        val searches =
            listOf(
                Triple(
                    listOf("youtube", "yt"),
                    "YouTube",
                    "https://www.youtube.com/results?search_query=",
                ),
                Triple(listOf("spotify"), "Spotify", "https://open.spotify.com/search/"),
                Triple(
                    listOf("wikipedia", "wiki"),
                    "Wikipedia",
                    "https://es.wikipedia.org/w/index.php?search=",
                ),
                Triple(
                    listOf("playstore", "play store"),
                    "Play Store",
                    "https://play.google.com/store/search?c=apps&q=",
                ),
                Triple(
                    listOf("navegar a"),
                    "Navegación en Maps",
                    "https://www.google.com/maps/dir/?api=1&destination=",
                ),
            )
        searches.forEach { (aliases, label, base) ->
            aliases
                .firstOrNull { n.startsWith("$it ") }
                ?.let { alias ->
                    val term = q.drop(alias.length).trim()
                    if (term.isNotEmpty())
                        add(
                            label,
                            "Buscar en $label",
                            term,
                            Intent(Intent.ACTION_VIEW, Uri.parse(base + Uri.encode(term))),
                        )
                }
        }
        if (n.startsWith("sofascore ") || n.startsWith("resultado ")) {
            val term = q.substringAfter(' ').trim()
            if (term.isNotBlank())
                out +=
                    Result(
                        "integration:sofascore",
                        "Buscar $term en Sofascore",
                        "Búsqueda en la app si admite la acción; alternativa web",
                        "Comandos",
                        1850,
                        Action.InApp(
                            Intent(Intent.ACTION_SEARCH)
                                .setPackage("com.sofascore.results")
                                .putExtra(android.app.SearchManager.QUERY, term),
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    "https://www.google.com/search?q=" +
                                        Uri.encode("site:sofascore.com $term resultado")
                                ),
                            ),
                        ),
                    )
        }
        val mediaCommands =
            mapOf(
                "subir volumen" to "volume_up",
                "bajar volumen" to "volume_down",
                "silenciar" to "mute",
                "activar sonido" to "unmute",
                "pausar musica" to "pause",
                "reproducir musica" to "play",
                "siguiente cancion" to "next",
                "cancion anterior" to "previous",
            )
        mediaCommands.forEach { (label, command) ->
            if (q.isNotBlank() && relevance(q, label) >= 500)
                out +=
                    Result(
                        "device:$command",
                        label.replaceFirstChar { it.uppercase() },
                        "Control de audio del dispositivo",
                        "Comandos",
                        1850,
                        Action.Device(command),
                    )
        }
        Regex("^(?:whatsapp|wa) (\\+?[0-9][0-9 ()-]{6,20})(?::\\s*(.*))?$", RegexOption.IGNORE_CASE)
            .matchEntire(q)
            ?.let {
                val number = it.groupValues[1].filter(Char::isDigit)
                if (number.length in 8..15 && number.first() != '0')
                    add(
                        "whatsapp-number",
                        "Preparar WhatsApp",
                        it.groupValues[2].ifBlank { "+$number" },
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://wa.me/$number?text=" + Uri.encode(it.groupValues[2])),
                        ),
                    )
            }
        listOf("llamar" to "tel", "sms" to "smsto").forEach { (alias, scheme) ->
            if (n.startsWith("$alias ")) {
                val parts = q.drop(alias.length).trim().split(':', limit = 2)
                if (parts[0].matches(Regex("[+0-9 ()-]{3,24}"))) {
                    val intent =
                        Intent(
                            if (scheme == "tel") Intent.ACTION_DIAL else Intent.ACTION_SENDTO,
                            Uri.fromParts(scheme, parts[0], null),
                        )
                    if (parts.size > 1) intent.putExtra("sms_body", parts[1].trim())
                    add(
                        alias,
                        if (scheme == "tel") "Preparar llamada" else "Preparar SMS",
                        parts[0],
                        intent,
                    )
                }
            }
        }
        val settings =
            mapOf(
                "almacenamiento" to Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
                "notificaciones" to "android.settings.ALL_APPS_NOTIFICATION_SETTINGS",
                "accesibilidad" to Settings.ACTION_ACCESSIBILITY_SETTINGS,
                "ubicacion" to Settings.ACTION_LOCATION_SOURCE_SETTINGS,
                "vpn" to Settings.ACTION_VPN_SETTINGS,
                "fecha y hora" to Settings.ACTION_DATE_SETTINGS,
                "seguridad" to Settings.ACTION_SECURITY_SETTINGS,
                "modo avion" to Settings.ACTION_AIRPLANE_MODE_SETTINGS,
                "idioma" to Settings.ACTION_LOCALE_SETTINGS,
                "impresion" to Settings.ACTION_PRINT_SETTINGS,
                "archivos completos" to
                    if (android.os.Build.VERSION.SDK_INT >= 30)
                        Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    else Settings.ACTION_SETTINGS,
                "recientes" to Settings.ACTION_USAGE_ACCESS_SETTINGS,
            )
        settings.forEach { (name, action) ->
            if (q.isNotBlank() && relevance(q, name) >= 500)
                add("setting:$name", "Abrir $name", "Panel del sistema", Intent(action))
        }
        if (n == "camara" || n == "sacar foto")
            add(
                "camera",
                "Abrir cámara",
                "Capturar en la app Cámara",
                Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
            )
        return out
    }
}
