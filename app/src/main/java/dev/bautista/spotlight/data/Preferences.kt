package dev.bautista.spotlight.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*

private val Context.store by preferencesDataStore("spotlight")

data class Config(
    val theme: String = "auto",
    val opacity: Float = .84f,
    val width: Int = 640,
    val position: Float = .16f,
    val contacts: Boolean = false,
    val files: Boolean = true,
    val animations: Boolean = true,
    val engine: String = "google",
    val endpoint: String = "",
    val favorites: Set<String> = emptySet(),
    val recent: List<String> = emptyList(),
    val trees: Set<String> = emptySet(),
    val clipboard: Boolean = false,
    val blur: Boolean = true,
    val shortcut: String = "ctrl_alt",
    val quickLinks: Set<String> = emptySet(),
    val contentIndex: Boolean = false,
    val visualIndex: Boolean = false,
    val videoIndex: Boolean = false,
    val online: Boolean = false,
    val liveEndpoint: String = "",
    val workflows: Set<String> = emptySet(),
    val aliases: Set<String> = emptySet(),
    val snippets: Set<String> = emptySet(),
    val pins: Set<String> = emptySet(),
    val accent: String = "blue",
    val appearance: String = "spotlight",
    val density: String = "auto",
    val messageApp: String = "whatsapp",
    val actionRecent: List<String> = emptyList(),
    val aiProvider:String="openai", val aiModel:String="gpt-4.1-mini",
    val learning:Boolean=true, val usageData:String="{}",
    val corner:Int=22, val textScale:Float=1f, val iconSize:Int=30, val maxHeight:Int=480, val adaptive:Boolean=true,
    val routines:String="[]", val onboarded:Boolean=false, val clipLimit:Int=20, val clipRetention:Int=15,
    val suggestions:Boolean=true, val hiddenSuggestions:Set<String> = emptySet(), val pinnedSuggestions:Set<String> = emptySet(),
    val preset:String="glass", val clipboardShortcut:String="ctrl_shift_v",
    val privateMode:Boolean=false, val autoKeyboard:String="adaptive",
    val glassIntensity:Float=.45f, val surfaceTone:String="neutral", val borderWidth:Float=1f,
    val shadowSize:Float=12f, val typeface:String="inter", val selectionStyle:String="glass",
    val landscapeWidth:Int=0, val landscapePosition:Float=-1f, val customThemes:String="[]",
    val sourcePriority:String="{}", val captureContext:Boolean=false, val learnedSequences:String="{}",
    val aiRootConsent:Boolean=false, val movement:String="liquid_precision",
    val manualWidth:Int=if(width>0)width else 640,
    val windowScale:Float=-1f, // -1 preserves legacy geometry until the user changes the new slider
    val semanticSearch:Boolean=false,
    val actionOpacity:Float=.92f, val settingsOpacity:Float=.94f,
    val keyBindings:Set<String> = emptySet(),

)

class Preferences(private val context: Context) {
    @Volatile var ready:Boolean=false
        private set
    @Volatile var isPrivate:Boolean=false
        private set
    @Volatile var current:Config?=null
        private set
    private val semanticFlag=booleanPreferencesKey("semanticSearch")
    suspend fun updateSemantic(enabled:Boolean){context.store.edit{it[semanticFlag]=enabled}}
    private val next20=stringPreferencesKey("next20")
    @Volatile private var privacyRevision=0L
    private val sequences=stringPreferencesKey("learnedSequences")
    private val lastActionTime=longPreferencesKey("lastActionTime")
    private val privateVersion=longPreferencesKey("privateVersion")
    private val privateFlag=booleanPreferencesKey("privateMode")

    private val theme = stringPreferencesKey("theme")
    private val opacity = floatPreferencesKey("opacity")
    private val width = intPreferencesKey("width")
    private val position = floatPreferencesKey("position")
    private val contacts = booleanPreferencesKey("contacts")
    private val files = booleanPreferencesKey("files")
    private val animations = booleanPreferencesKey("animations")
    private val engine = stringPreferencesKey("engine")
    private val endpoint = stringPreferencesKey("endpoint")
    private val favorites = stringSetPreferencesKey("favorites")
    private val recent = stringPreferencesKey("recent")
    private val trees = stringSetPreferencesKey("trees")
    private val clipboard = booleanPreferencesKey("clipboard")
    private val blur = booleanPreferencesKey("blur")
    private val shortcut = stringPreferencesKey("shortcut")
    private val quickLinks = stringSetPreferencesKey("quickLinks")
    private val contentIndex = booleanPreferencesKey("contentIndex")
    private val visualIndex = booleanPreferencesKey("visualIndex")
    private val videoIndex = booleanPreferencesKey("videoIndex")
    private val online = booleanPreferencesKey("online")
    private val liveEndpoint = stringPreferencesKey("liveEndpoint")
    private val workflows = stringSetPreferencesKey("workflows")
    private val aliases = stringSetPreferencesKey("aliases")
    private val snippets = stringSetPreferencesKey("snippets")
    private val pins = stringSetPreferencesKey("pins")
    private val accent = stringPreferencesKey("accent")
    private val appearance = stringPreferencesKey("appearance")
    private val density = stringPreferencesKey("density")
    private val messageApp = stringPreferencesKey("messageApp")
    private val actionRecent = stringPreferencesKey("actionRecent")
    private val aiProvider = stringPreferencesKey("aiProvider")
    private val aiModel = stringPreferencesKey("aiModel")
    private val learning = booleanPreferencesKey("learning")
    private val usageData = stringPreferencesKey("usageData")
    private val corner = intPreferencesKey("corner")
    private val textScale = floatPreferencesKey("textScale")
    private val iconSize = intPreferencesKey("iconSize")
    private val maxHeight = intPreferencesKey("maxHeight")
    private val adaptive = booleanPreferencesKey("adaptive")
    private val routines = stringPreferencesKey("routines")
    private val onboarded = booleanPreferencesKey("onboarded")
    private val clipLimit = intPreferencesKey("clipLimit")
    private val clipRetention = intPreferencesKey("clipRetention")
    private val preset = stringPreferencesKey("preset")
    private val keyboardBindings=stringSetPreferencesKey("keyboard_bindings")
    private val clipboardShortcut=stringPreferencesKey("clipboard_shortcut")
    private val suggestions = booleanPreferencesKey("suggestions")
    private val hiddenSuggestions = stringSetPreferencesKey("hiddenSuggestions")
    private val pinnedSuggestions = stringSetPreferencesKey("pinnedSuggestions")
    private val notes = stringPreferencesKey("notes")
    val noteData: Flow<String> = context.store.data.map { it[notes] ?: "[]" }

    suspend fun editNotes(transform: (String) -> String) {
        context.store.edit { it[notes] = transform(it[notes] ?: "[]") }
    }

    val config: Flow<Config> =
        context.store.data
            .catch { emit(emptyPreferences()) }
            .map { p ->
                val extra=runCatching{org.json.JSONObject(p[next20] ?: "{}")} .getOrDefault(org.json.JSONObject())
                Config(
                    p[theme] ?: "auto",
                    p[opacity] ?: .84f,
                    p[width] ?: 0,
                    p[position] ?: .16f,
                    p[contacts] ?: false,
                    p[files] ?: true,
                    p[animations] ?: true,
                    p[engine] ?: "google",
                    p[endpoint] ?: "",
                    p[favorites] ?: emptySet(),
                    p[recent]?.split("|")?.filter { it.isNotEmpty() } ?: emptyList(),
                    p[trees] ?: emptySet(),
                    p[clipboard] ?: false,
                    p[blur] ?: true,
                    p[shortcut] ?: "ctrl_alt",
                    p[quickLinks] ?: emptySet(),
                    p[contentIndex] ?: false,
                    p[visualIndex] ?: false,
                    p[videoIndex] ?: false,
                    p[online] ?: false,
                    p[liveEndpoint] ?: "",
                    p[workflows] ?: emptySet(),
                    p[aliases] ?: emptySet(),
                    p[snippets] ?: emptySet(),
                    p[pins] ?: emptySet(),
                    p[accent] ?: "blue",
                    p[appearance] ?: "spotlight",
                    p[density] ?: "auto",
                    p[messageApp] ?: "whatsapp",
                    p[actionRecent]?.split("|")?.filter { it.isNotBlank() } ?: emptyList(),
                    p[aiProvider] ?: if(p[endpoint].isNullOrBlank()) "openai" else "backend",
                    p[aiModel] ?: "gpt-4.1-mini", p[learning] ?: true, p[usageData] ?: "{}",
                    p[corner] ?: 22, p[textScale] ?: 1f, p[iconSize] ?: 30, p[maxHeight] ?: 480, p[adaptive] ?: true,
                    p[routines] ?: "[]", p[onboarded] ?: false, p[clipLimit] ?: 20, p[clipRetention] ?: 15,
                    p[suggestions] ?: true,p[hiddenSuggestions] ?: emptySet(),p[pinnedSuggestions] ?: emptySet(),p[preset] ?: when { p[appearance]==null && p[accent]==null && p[theme]==null && p[corner]==null -> "glass"; p[appearance]=="raycast" -> "raycast"; p[accent]=="graphite" -> "graphite"; p[theme]=="light" && (p[corner] ?: 22)>=28 -> "ios"; else -> "macos" },p[clipboardShortcut] ?: "ctrl_shift_v",
                ).copy(keyBindings=p[keyboardBindings]?.takeIf{dev.bautista.spotlight.input.KeyboardBindings.validEntries(it)} ?: emptySet(),privateMode=p[privateFlag] ?: false,semanticSearch=p[semanticFlag] ?: false,
                    autoKeyboard=extra.optString("autoKeyboard","adaptive").takeIf{it in setOf("adaptive","always","never")} ?: "adaptive",
                    glassIntensity=extra.optDouble("glassIntensity",.45).toFloat().coerceIn(0f,1f),
                    surfaceTone=extra.optString("surfaceTone","neutral"),borderWidth=extra.optDouble("borderWidth",1.0).toFloat().coerceIn(0f,2f),
                    shadowSize=extra.optDouble("shadowSize",12.0).toFloat().coerceIn(0f,20f),typeface=extra.optString("typeface","inter"),
                    selectionStyle=extra.optString("selectionStyle","glass"),landscapeWidth=extra.optInt("landscapeWidth",0).coerceIn(0,1100),
                    landscapePosition=extra.optDouble("landscapePosition",-1.0).toFloat(),customThemes=extra.optString("customThemes","[]"),
                    sourcePriority=extra.optString("sourcePriority","{}"),captureContext=extra.optBoolean("captureContext",false),learnedSequences=p[sequences] ?: "{}",aiRootConsent=extra.optBoolean("aiRootConsent",false),movement=extra.optString("movement","liquid_precision"),
                    manualWidth=extra.optInt("manualWidth",(p[width] ?: 0).takeIf{it>0} ?: 640).coerceIn(300,1100),
                    windowScale=extra.optDouble("windowScale",-1.0).toFloat().takeIf{it.isFinite() && it in 0f..1f} ?: -1f,
                    actionOpacity=extra.optDouble("actionOpacity",.92).toFloat().takeIf{it.isFinite() && it in .70f..1f} ?: .92f,
                    settingsOpacity=extra.optDouble("settingsOpacity",.94).toFloat().takeIf{it.isFinite() && it in .70f..1f} ?: .94f
                ).also{val v=p[privateVersion] ?: 0L;if(v>=privacyRevision){isPrivate=it.privateMode;privacyRevision=v;current=it;ready=true}}
            }

    /** A settings edit must not overwrite habits, privacy, files or concurrent changes. */
    suspend fun updateAppearance(c: Config) {
        context.store.edit { p ->
            p[theme] = c.theme
            p[opacity] = c.opacity.takeIf { it.isFinite() }?.coerceIn(.70f,1f) ?: .84f
            p[animations] = c.animations
            p[density] = c.density
            p[accent] = c.accent
            p[stringPreferencesKey("preset")] = c.preset
            val extra = runCatching { org.json.JSONObject(p[next20] ?: "{}") }.getOrDefault(org.json.JSONObject())
            extra.put("actionOpacity",c.actionOpacity.takeIf{it.isFinite()}?.coerceIn(.70f,1f) ?: .92f)
            extra.put("settingsOpacity",c.settingsOpacity.takeIf{it.isFinite()}?.coerceIn(.70f,1f) ?: .94f)
            extra.put("windowScale", c.windowScale.takeIf { it.isFinite() && it in 0f..1f } ?: -1f)
            p[next20] = extra.toString()
        }
    }

    /** Persist only the AI fields actually edited, preserving concurrent privacy/history/source changes. */
    suspend fun updateAi(before:Config,after:Config) {
        context.store.edit { p ->
            if(before.aiProvider!=after.aiProvider)p[aiProvider]=after.aiProvider
            if(before.aiModel!=after.aiModel)p[aiModel]=after.aiModel
            if(before.endpoint!=after.endpoint)p[endpoint]=after.endpoint
            if(before.aiRootConsent!=after.aiRootConsent) {
                val extra=runCatching{org.json.JSONObject(p[next20] ?: "{}")}.getOrDefault(org.json.JSONObject())
                extra.put("aiRootConsent",after.aiRootConsent);p[next20]=extra.toString()
            }
        }
    }

    suspend fun saveNativeCommand(command:dev.bautista.spotlight.automation.Workflow) {
        require(dev.bautista.spotlight.automation.NativeCommands.isNative(command)){"Comando nativo no válido."}
        context.store.edit { p ->
            val current=runCatching{org.json.JSONArray(p[routines] ?: "[]")}.getOrElse{error("No se pudieron leer los comandos existentes. Exportá la configuración antes de repararlos.")}
            check(current.length()<30){"Se alcanzó el límite de 30 elementos guardados. No se borró contenido anterior."}
            val item=org.json.JSONArray(dev.bautista.spotlight.automation.Workflows.encode(listOf(command))).getJSONObject(0)
            current.put(item);p[routines]=current.toString()
        }
    }

    suspend fun deleteNativeCommand(id:String) {
        context.store.edit { p ->
            val current=runCatching{org.json.JSONArray(p[routines] ?: "[]")}.getOrElse{error("No se pudieron leer los comandos existentes.")}
            val kept=org.json.JSONArray()
            for(i in 0 until current.length()) {
                val item=current.optJSONObject(i)
                if(item?.optString("id")==id && item.optString("kind")==dev.bautista.spotlight.automation.NativeCommands.KIND)continue
                kept.put(current.get(i))
            }
            p[routines]=kept.toString()
        }
    }

    suspend fun update(c: Config) {
        val previousPrivate=isPrivate;val previousRevision=privacyRevision
        val revision=maxOf(System.currentTimeMillis(),privacyRevision+1);privacyRevision=revision;isPrivate=c.privateMode
        try {context.store.edit { p ->
            p[privateVersion]=revision
            p[privateFlag]=c.privateMode
            p[next20]=org.json.JSONObject().put("autoKeyboard",c.autoKeyboard).put("glassIntensity",c.glassIntensity.toDouble())
                .put("surfaceTone",c.surfaceTone).put("borderWidth",c.borderWidth.toDouble()).put("shadowSize",c.shadowSize.toDouble())
                .put("typeface",c.typeface).put("selectionStyle",c.selectionStyle).put("landscapeWidth",c.landscapeWidth)
                .put("landscapePosition",c.landscapePosition.toDouble()).put("customThemes",c.customThemes)
                .put("actionOpacity",c.actionOpacity.toDouble()).put("settingsOpacity",c.settingsOpacity.toDouble()).put("windowScale",c.windowScale).put("sourcePriority",c.sourcePriority).put("captureContext",c.captureContext).put("aiRootConsent",c.aiRootConsent).put("movement",c.movement).put("manualWidth",if(c.width>0)c.width else c.manualWidth).toString()
            p[theme] = c.theme
            p[opacity] = c.opacity
            p[width] = c.width
            p[position] = c.position
            p[contacts] = c.contacts
            p[files] = c.files
            p[animations] = c.animations
            p[engine] = c.engine
            p[endpoint] = c.endpoint
            p[favorites] = c.favorites
            p[recent] = c.recent.joinToString("|")
            p[trees] = c.trees
            p[clipboard] = c.clipboard
            p[blur] = c.blur
            p[shortcut] = c.shortcut
            p[quickLinks] = c.quickLinks
            p[contentIndex] = c.contentIndex
            p[semanticFlag] = c.semanticSearch
            p[visualIndex] = c.visualIndex
            p[videoIndex] = c.videoIndex
            p[online] = c.online
            p[liveEndpoint] = c.liveEndpoint
            p[workflows] = c.workflows
            p[aliases] = c.aliases
            p[snippets] = c.snippets
            p[pins] = c.pins
            p[accent] = c.accent
            p[appearance] = c.appearance
            p[density] = c.density
            p[messageApp] = c.messageApp
            p[actionRecent] = c.actionRecent.joinToString("|")
            p[aiProvider] = c.aiProvider
            p[aiModel] = c.aiModel
            p[learning] = c.learning
            p[corner] = c.corner
            p[textScale] = c.textScale
            p[iconSize] = c.iconSize
            p[maxHeight] = c.maxHeight
            p[adaptive] = c.adaptive
            p[routines] = c.routines
            p[onboarded] = c.onboarded
            p[clipLimit] = c.clipLimit
            p[clipRetention] = c.clipRetention
            p[preset]=c.preset
            p[clipboardShortcut]=c.clipboardShortcut
            p[keyboardBindings]=c.keyBindings
            p[suggestions]=c.suggestions
            p[hiddenSuggestions]=c.hiddenSuggestions
            p[pinnedSuggestions]=c.pinnedSuggestions

        }
        }catch(e:Exception){if(privacyRevision==revision){isPrivate=previousPrivate;privacyRevision=previousRevision};throw e}
    }

    suspend fun togglePin(query: String) {
        val value=query.trim().take(4000)
        if(value.isBlank()) return
        context.store.edit { p ->
            val current=p[pins] ?: emptySet()
            p[pins]=if(value in current) current-value else (current+value).take(50).toSet()
        }
    }

    suspend fun favorite(id: String) {
        context.store.edit { p ->
            val s = p[favorites] ?: emptySet()
            p[favorites] = if (id in s) s - id else s + id
        }
    }

    suspend fun record(id: String) {
        context.store.edit { p ->
            if(p[privateFlag]==true)return@edit
            p[recent] =
                (listOf(id) + (p[recent]?.split("|") ?: emptyList()).filter { it != id })
                    .take(20)
                    .joinToString("|")
        }
    }

    suspend fun addTree(uri: String) {
        context.store.edit { it[trees] = (it[trees] ?: emptySet()) + uri }
    }

    suspend fun recordSelection(id:String) {
        context.store.edit { p ->
            if(p[learning] != false && p[privateFlag]!=true) {
                val data=runCatching { org.json.JSONObject(p[usageData] ?: "{}") }.getOrDefault(org.json.JSONObject())
                val previous=data.optJSONObject(id)
                data.put(id,org.json.JSONObject().put("count",((previous?.optInt("count") ?: 0)+1).coerceAtMost(10000)).put("time",System.currentTimeMillis()))
                while(data.length()>300) { val oldest=data.keys().asSequence().minByOrNull { data.optJSONObject(it)?.optLong("time") ?: 0 } ?: break;data.remove(oldest) }
                p[usageData]=data.toString()
            }
        }
    }

    suspend fun recordAction(query:String) {
        if(query.isBlank())return
        context.store.edit {p->if(p[privateFlag]==true)return@edit
            val old=p[actionRecent]?.split("|")?.filter{it.isNotBlank()}.orEmpty();val now=System.currentTimeMillis()
            if(p[learning]!=false && now-(p[lastActionTime] ?: 0L) in 0..120000L){
                val previous=old.firstOrNull().orEmpty();val pair="$previous y $query"
                if(dev.bautista.spotlight.search.LocalPlan.step(previous)!=null && dev.bautista.spotlight.search.LocalPlan.step(query)!=null && dev.bautista.spotlight.search.LocalPlan.parse(pair)!=null){
                    val data=runCatching{org.json.JSONObject(p[sequences] ?: "{}").let {j->org.json.JSONObject().apply{j.keys().asSequence().filter{now-j.optJSONObject(it)!!.optLong("last",0)<30L*86400000}.take(49).forEach{put(it,j.getJSONObject(it))}}}}.getOrDefault(org.json.JSONObject())
                    data.put(pair,org.json.JSONObject().put("count",(data.optJSONObject(pair)?.optInt("count",0) ?: 0)+1).put("last",now));p[sequences]=data.toString()
                }
            }
            p[actionRecent]=(listOf(query.take(4000))+old.filter{it!=query}).take(10).joinToString("|");p[lastActionTime]=now
        }
    }

    suspend fun clearHistory() {
        context.store.edit { it.remove(recent); it.remove(actionRecent); it.remove(usageData);it.remove(sequences);it.remove(lastActionTime) }
    }
}
