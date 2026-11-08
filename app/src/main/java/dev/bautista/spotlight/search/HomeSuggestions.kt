package dev.bautista.spotlight.search

import android.content.Context
import android.content.pm.ApplicationInfo
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.Replies
import dev.bautista.spotlight.control.SearchScope
import dev.bautista.spotlight.data.Config
import kotlin.math.exp
import kotlin.math.ln

/**
 * Contextual, privacy-preserving local suggestion engine.
 * Rules:
 * - Zero alphabetical app filler: an app is suggested only if backed by explicit signal
 *   (favorite, recent launch, or active habit within TTL).
 * - Maximum 1 application suggestion on the initial screen.
 * - Prioritizes active context continuity and repeated/recent system actions.
 * - Applies time decay/TTL to usage and context; returns an empty list if nothing is relevant.
 */
object HomeSuggestions {
    const val MIN_RELEVANCE_SCORE = 300
    const val MAX_SUGGESTED_APPS = 1
    const val MAX_TOTAL_SUGGESTIONS = 3
    private const val HABIT_TTL_MS = 14L * 86_400_000L
    private const val HALF_LIFE_MS = 7.0 * 86_400_000.0

    data class ScoredCandidate(val result: Result, val contextualScore: Int)

    fun scoreAppUsage(count: Int, lastTimeMs: Long, nowMs: Long = System.currentTimeMillis()): Int {
        if (count <= 0 || lastTimeMs <= 0L) return 0
        if (lastTimeMs > 0L && nowMs - lastTimeMs > HABIT_TTL_MS) return 0
        val decay = if (lastTimeMs > 0L) exp(-(nowMs - lastTimeMs).coerceAtLeast(0L) / HALF_LIFE_MS) else 1.0
        return (260 + ln(1.0 + count) * 85.0 * decay).toInt()
    }

    fun results(
        context: Context,
        c: Config,
        filesAllowed: Boolean,
        hasClips: Boolean,
        nowMs: Long = System.currentTimeMillis(),
    ): List<Result> {
        if (!c.suggestions) return emptyList()
        val candidates = mutableListOf<ScoredCandidate>()
        fun addCandidate(id: String, title: String, subtitle: String, action: Action, baseScore: Int, icon: android.graphics.drawable.Drawable? = null) {
            if (baseScore >= MIN_RELEVANCE_SCORE) {
                candidates += ScoredCandidate(
                    Result("suggest:$id", title, subtitle, "Sugerencias", baseScore, action, icon),
                    baseScore,
                )
            }
        }

        // 1. Active authorized context continuity (with TTL expiration enforced)
        val app = context.applicationContext as? SpotlightApp
        app?.contexts?.expire(nowMs)
        val activeCtx = app?.contexts?.active?.value
        if (activeCtx != null && !c.privateMode && (activeCtx.kind != "document" || filesAllowed)) {
            addCandidate(
                "context:${activeCtx.id}",
                "Continuar con «${activeCtx.title}»",
                "Contexto autorizado activo · explicar o resumir con IA",
                Action.SystemPanel("context-ai", "Explicame este documento"),
                baseScore = 680,
            )
        }

        // 2. Repeated local action sequences (with 30-day TTL)
        if (c.learning && !c.privateMode) {
            runCatching {
                val sequences = org.json.JSONObject(c.learnedSequences)
                sequences.keys().asSequence()
                    .filter { key ->
                        val entry = sequences.optJSONObject(key) ?: return@filter false
                        entry.optInt("count", 0) >= 3 && nowMs - entry.optLong("last", 0L) < 30L * 86_400_000L
                    }
                    .take(1)
                    .forEach { raw ->
                        if (LocalPlan.parse(raw) != null) {
                            addCandidate(
                                "sequence:${normalize(raw)}",
                                "Repetir secuencia",
                                "Hábitos locales · revisar y guardar como comando",
                                Action.SystemPanel("interpret", raw),
                                baseScore = 640,
                            )
                        }
                    }
            }
        }

        // 3. Recent system actions (deduplicated by canonical target)
        if (c.learning && !c.privateMode) {
            val seenActionTargets = mutableSetOf<String>()
            c.actionRecent.take(8).forEachIndexed { idx, raw ->
                val action = when (val intent = UniversalIntent.recognize(raw)) {
                    is UniversalIntent.Volume -> Action.Device("volume:${intent.percent}")
                    else -> dev.bautista.spotlight.next.IntentRouter.route(raw, c.messageApp)
                        ?.takeIf { it is Action.SystemPanel && it.page == "display-review" }
                } ?: return@forEachIndexed
                if(action is Action.Device && context.getSystemService(android.media.AudioManager::class.java).isVolumeFixed)return@forEachIndexed
                val targetKey = StableResults.actionSignature(action)
                if (seenActionTargets.add(targetKey) && seenActionTargets.size <= 2) {
                    addCandidate(
                        "action:${normalize(raw)}",
                        raw,
                        if (action is Action.Device) "Acción reciente · aplicar al seleccionar" else "Acción reciente · abrir revisión",
                        action,
                        baseScore = (580 - idx * 30).coerceAtLeast(MIN_RELEVANCE_SCORE),
                    )
                }
            }
        }

        // 4. Single Most Relevant Application (MAX_SUGGESTED_APPS = 1, zero alphabetical filler)
        val pm = context.packageManager
        val launcher = android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        val available = runCatching { pm.queryIntentActivities(launcher, 0) }.getOrDefault(emptyList())
            .filter { it.activityInfo?.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }
        val learned = if (c.learning && !c.privateMode) c.recent else emptyList()
        val usage = if (c.learning && !c.privateMode) {
            runCatching { org.json.JSONObject(c.usageData) }.getOrDefault(org.json.JSONObject())
        } else org.json.JSONObject()

        val scoredApps = available.mapNotNull { info ->
            val a = info.activityInfo ?: return@mapNotNull null
            val pkg = a.packageName
            val componentId = "$pkg/${a.name}"
            val isFavorite = c.favorites.any { it.removePrefix("suggest:app:").substringBefore('/') == pkg }
            val recentIndex = learned.indexOfFirst { it.removePrefix("suggest:app:").substringBefore('/') == pkg }
            val usageObj = usage.optJSONObject(componentId)
                ?: usage.optJSONObject(pkg)
                ?: usage.optJSONObject("suggest:app:$pkg")
            val usageCount = usageObj?.optInt("count", 0) ?: 0
            val usageTime = usageObj?.optLong("time", 0L) ?: 0L
            val usageScore = scoreAppUsage(usageCount, usageTime, nowMs)

            val appScore = when {
                isFavorite -> 500 + (if (recentIndex >= 0) 20 else 0)
                recentIndex >= 0 -> (470 - recentIndex * 35).coerceAtLeast(MIN_RELEVANCE_SCORE)
                usageScore >= MIN_RELEVANCE_SCORE -> usageScore.coerceAtMost(480)
                else -> 0 // Strictly exclude alphabetical non-relevant apps
            }
            if (appScore < MIN_RELEVANCE_SCORE) return@mapNotNull null
            val isAudioApp = a.applicationInfo?.category == ApplicationInfo.CATEGORY_AUDIO ||
                pkg in setOf("com.spotify.music", "com.google.android.apps.youtube.music", "deezer.android.app", "com.apple.android.music")
            val detail = when {
                isAudioApp && recentIndex == 0 -> "Aplicación multimedia reciente"
                isFavorite -> "Aplicación favorita"
                recentIndex >= 0 -> "Aplicación reciente · aprendizaje local"
                else -> "Aplicación frecuente · aprendizaje local"
            }
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN)
                .addCategory(android.content.Intent.CATEGORY_LAUNCHER)
                .setComponent(android.content.ComponentName(pkg, a.name))
            ScoredCandidate(
                Result(
                    "suggest:app:$pkg",
                    info.loadLabel(pm).toString(),
                    detail,
                    "Sugerencias",
                    appScore,
                    Action.Launch(intent),
                    runCatching { info.loadIcon(pm) }.getOrNull(),
                ),
                appScore,
            )
        }
            .sortedWith(compareByDescending<ScoredCandidate> { it.contextualScore }.thenBy { normalize(it.result.title) }.thenBy { it.result.id })
            .take(MAX_SUGGESTED_APPS)

        candidates += scoredApps

        // 5. Authorized WhatsApp notifications continuity
        if (!c.privateMode && Replies.connected.value && Replies.targets.value.any { it.packageName == "com.whatsapp" && it.messages.any{message->message.text.isNotBlank()} }) {
            addCandidate(
                "replies",
                "Leer mensajes de WhatsApp",
                "Notificaciones actuales autorizadas",
                Action.Navigate(SearchScope("whatsapp", "replies")),
                baseScore = 520,
            )
        }

        // 6. Authorized Clipboard continuity
        if (c.clipboard && hasClips && !c.privateMode) {
            addCandidate(
                "clipboard",
                "Consultar portapapeles",
                "Textos capturados con Spotlight en foco",
                Action.SelectMode("Portapapeles"),
                baseScore = 420,
            )
        }

        return candidates
            .distinctBy { it.result.id }
            .filter { candidate ->
                val item = candidate.result
                val module = when {
                    item.id.startsWith("suggest:app:") || item.id == "suggest:apps" -> "apps"
                    item.action is Action.Device -> "media"
                    else -> dev.bautista.spotlight.extensions.ExtensionStore.module(item)
                }
                item.id !in c.hiddenSuggestions &&
                    (module == null || dev.bautista.spotlight.extensions.ExtensionStore.enabled(context, module))
            }
            .sortedWith(
                compareByDescending<ScoredCandidate> { it.result.id in c.pinnedSuggestions }
                    .thenByDescending { it.contextualScore }
                    .thenBy { it.result.id }
            )
            .take(MAX_TOTAL_SUGGESTIONS)
            .mapIndexed { i, candidate -> candidate.result.copy(score = 2200 - i) }
    }
}
