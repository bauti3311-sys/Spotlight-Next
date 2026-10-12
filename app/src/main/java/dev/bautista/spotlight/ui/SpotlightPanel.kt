package dev.bautista.spotlight.ui

import android.content.*
import android.content.ClipboardManager
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import dev.bautista.spotlight.control.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.DirectAi
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class, ExperimentalFoundationApi::class)
@Composable
fun SpotlightPanel(
    app: SpotlightApp,
    config: Config,
    onClose: () -> Unit,
    initialQuery: String = "",
    availableHeightDp: Float? = null,
    invocation:String="touch",
) =
    SpotlightTheme(config.theme, config) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val keyboard = LocalSoftwareKeyboardController.current
        val focus = remember { FocusRequester() }
        val editorFocus = remember { FocusRequester() }
        var query by remember { mutableStateOf(initialQuery) }
        var searchScope by remember { mutableStateOf<SearchScope?>(null) }
        var systemPage by remember {mutableStateOf<Action.SystemPanel?>(null)}
        var messageForm by remember { mutableStateOf<Action.ComposeMessage?>(null) }
        var searchForm by remember { mutableStateOf<Action.ComposeSearch?>(null) }
        var notificationKey by remember {mutableStateOf<String?>(null)}
        var audioPanel by remember { mutableStateOf(false) }
        var showAi by remember { mutableStateOf(false) }
        var aiJob by remember { mutableStateOf<Job?>(null) }
        var pendingAsk by remember {mutableStateOf<Action.Ask?>(null)}
        var pendingWorkflow by remember {mutableStateOf<dev.bautista.spotlight.automation.Workflow?>(null)}
        var clipEditing by remember {mutableStateOf<String?>(null)}
        var snippetTemplate by remember {mutableStateOf<String?>(null)}
        var timerMinutes by remember {mutableStateOf<Int?>(null)}
        var deletingNote by remember {mutableStateOf<Result?>(null)}
        var snippetText by remember {mutableStateOf<String?>(null)}
        var snippetAlias by remember {mutableStateOf("")}
        var snippetError by remember {mutableStateOf("")}
        var followUp by remember{mutableStateOf("")}
        val replyTargets by Replies.targets.collectAsState()
        val replyConnected by Replies.connected.collectAsState()
        fun clearForms(){systemPage=null;clipEditing=null;snippetTemplate=null;timerMinutes=null;deletingNote=null;notificationKey=null;messageForm=null;searchForm=null;audioPanel=false;pendingAsk=null;pendingWorkflow=null;snippetText=null}
        var category by remember { mutableStateOf("Todo") }
        var searchOptions by remember { mutableStateOf(false) }
        var results by remember { mutableStateOf(emptyList<Result>()) }
        var selected by remember { mutableIntStateOf(0) }
        data class Location(val query: String, val category: String, val scope: SearchScope?, val selected: Int)
        val history = remember { mutableStateListOf<Location>() }
        fun push() { history.add(Location(query,category,searchScope,selected)) }
        var navigating by remember { mutableStateOf(false) }
        var preview by remember { mutableStateOf<Result?>(null) }
        var menu by remember { mutableStateOf(false) }
        var advancedActions by remember { mutableStateOf(false) }
        val motionEnabled=LocalMotion.current
        val actionFocus=remember {FocusRequester()}
        LaunchedEffect(menu,preview?.id) { if(menu) {withFrameNanos { };runCatching { actionFocus.requestFocus() }} }
        var previousSearch by remember {mutableStateOf<String?>(null)}
        var loading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf("") }
        var card by remember { mutableStateOf<InfoCard?>(null) }
        var cardLoading by remember { mutableStateOf(false) }
        var cardJob by remember { mutableStateOf<Job?>(null) }
        var editingNoteId by remember { mutableStateOf<String?>(null) }
        var pinned by remember { mutableStateOf(config.pins) }
        var noteEditor by remember { mutableStateOf(false) }
        var draft by remember { mutableStateOf("") }
        var aiMessageTarget by remember {mutableStateOf("")}
        var aiText by remember { mutableStateOf("") }
        var aiLoading by remember { mutableStateOf(false) }
        val ai = remember { DirectAi(context) }
        val conversation = remember { mutableListOf<Pair<String, String>>() }
        var favorites by remember { mutableStateOf(config.favorites) }
        val clips by app.local.clips.collectAsState()
        val clipFavorites by app.local.clipFavorites.collectAsState()
        val notes by app.local.notes.collectAsState()
        val activeContext by app.contexts.active.collectAsState()
        val pinnedContexts by app.contexts.pinned.collectAsState()
        val savedChats by app.contexts.conversations.collectAsState()
        val indexStatus by app.index.status.collectAsState()
        val indexRevision by app.index.revision.collectAsState()
        val colors = MaterialTheme.colorScheme
        val listState = rememberLazyListState()
        val gridState = rememberLazyGridState()
        fun back() {
            aiJob?.cancel()
            showAi=false
            clearForms();preview=null;menu=false;noteEditor=false;selected=0;navigating=false;cardJob?.cancel();card=null
            if(history.isNotEmpty()) {
                val previous=history.removeAt(history.lastIndex)
                query=previous.query;category=previous.category;searchScope=previous.scope;selected=previous.selected
            } else {searchScope=null;category="Todo"}
        }
        fun selectMode(mode:String,clearQuery:Boolean=false) {
            if(!clearQuery && mode==category && searchScope?.section !in setOf("files","results"))return
            push();clearForms();aiJob?.cancel();showAi=false;noteEditor=false;preview=null;menu=false;advancedActions=false
            cardJob?.cancel();card=null;selected=0;navigating=false;results=emptyList()
            if(clearQuery)query=""
            category=mode
            searchScope=when(mode){"Apps"->SearchScope("","apps");"Archivos"->SearchScope("","files-mode");"Acciones"->SearchScope("","commands");"IA"->SearchScope("","ai");"Portapapeles"->SearchScope("","clipboard");else->null}
            focus.requestFocus()
        }
        val filtered =
            results.filter {
                if(query.isNotBlank() && (it.id.startsWith("root:") || it.id.startsWith("registered:")))true else when (category) {
                    "Todo" -> true
                    "Apps" -> it.category == "Aplicaciones"
                    "Archivos" -> searchScope!=null || it.category == "Archivos"
                    "IA" -> it.category == "IA"
                    "Portapapeles" -> it.category == "Portapapeles"
                    else -> it.category !in setOf("Aplicaciones", "Archivos", "Portapapeles")
                }
            }
        var recentAppIds by remember { mutableStateOf(config.recent) }
        LaunchedEffect(results, config.recent) {
            val apps = results.filter { it.category == "Aplicaciones" }
            recentAppIds =
                withContext(Dispatchers.IO) {
                    dev.bautista.spotlight.control.CommandCenter.recent(
                        context,
                        config.recent,
                        apps,
                    )
                }
        }
        val displayed =
            if(systemPage!=null || messageForm!=null || searchForm!=null || audioPanel || pendingAsk!=null || pendingWorkflow!=null || snippetText!=null || clipEditing!=null || snippetTemplate!=null || timerMinutes!=null || deletingNote!=null || notificationKey!=null)emptyList() else if (category == "Apps" && query.isBlank())
                dev.bautista.spotlight.control.CommandCenter.orderedApps(filtered, recentAppIds)
            else if(query.isNotBlank() && searchScope==null && category=="Todo")ResultSections.unified(filtered)
            else ResultSections.grouped(filtered, expanded=searchScope!=null || category!="Todo" || query.isBlank(),query=query)
        val recentCount =
            if (category == "Apps") displayed.takeWhile { it.subtitle.startsWith("Reciente") }.size
            else 0
        val suggestion =
            if (!navigating && messageForm==null && searchForm==null && !audioPanel && pendingAsk==null && pendingWorkflow==null && snippetText==null && clipEditing==null && snippetTemplate==null && timerMinutes==null && deletingNote==null && notificationKey==null && searchScope==null) dev.bautista.spotlight.control.CommandCenter.complete(query, results + config.aliases.map { entry ->
                Result("alias:$entry",entry.substringBefore('|'),"","Aplicaciones",0,Action.Copy(""))
            })
            else null
        fun acceptSuggestion() {
            suggestion?.let {
                query = it
                results = emptyList()
                selected = 0
                preview = null
                menu = false
            }
        }
        val activeCard = card.takeIf { category in setOf("Todo", "Acciones", "IA") }
        val chosen =
            displayed.getOrNull(selected.coerceIn(0, (displayed.size - 1).coerceAtLeast(0)))
        fun saveDraft() {
            scope.launch {
                runCatching { app.local.saveNote(draft,editingNoteId) }
                    .onSuccess {
                        noteEditor = false
                        searchScope=null
                        query = "notas"
                        category = "Acciones"
                    }
                    .onFailure { error = "No se pudo guardar" }
            }
        }
        LaunchedEffect(noteEditor) {
            if (noteEditor) {
                delay(50)
                editorFocus.requestFocus()
            }
        }
        fun copy(text: String) {
            context
                .getSystemService(ClipboardManager::class.java)
                .setPrimaryClip(ClipData.newPlainText("Spotlight", text))
            Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
        }
        fun launch(intent: Intent, close: Boolean = true) {
            runCatching {
                    val preferred = dev.bautista.spotlight.control.CommandCenter.preferred(intent)
                    if (
                        preferred == null ||
                            runCatching {
                                    context.startActivity(
                                        preferred.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                                .isFailure
                    )
                        context.startActivity(
                            Intent(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    if (close) onClose()
                }
                .onFailure { error = "No hay una app disponible para esta acción." }
        }
        fun web() {
            if (query.isNotBlank())
                launch(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)),
                    )
                )
        }
        fun locate(item: Result) {
            val parent = item.parentUri ?: return
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .setDataAndType(Uri.parse(parent), DocumentsContract.Document.MIME_TYPE_DIR)
                        .addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                )
                onClose()
            } catch (e: Exception) {
                launch(
                    Intent(Intent.ACTION_OPEN_DOCUMENT)
                        .setType("*/*")
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(parent))
                )
            }
        }
        fun share(item: Result) {
            val uri =
                if (item.category == "Archivos") (item.action as? Action.Launch)?.intent?.data
                else null
            val send = Intent(Intent.ACTION_SEND)
            if (uri != null) {
                send
                    .setType(item.mime ?: "application/octet-stream")
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                send.clipData = ClipData.newRawUri("Archivo", uri)
            } else
                send.setType("text/plain").putExtra(Intent.EXTRA_TEXT, item.preview ?: item.title)
            launch(Intent.createChooser(send, "Compartir"))
        }
        fun lookup(action: Action.Lookup) {
            cardJob?.cancel()
            card = null
            cardLoading = true
            error = ""
            cardJob =
                scope.launch {
                    try {
                        card =
                            withTimeout(if (action.type == "translate") 150000 else 35000) {
                                app.online.lookup(action, config)
                            }
                    } catch (e: CancellationException) {
                        if (e is TimeoutCancellationException)
                            error =
                                "La consulta tardó demasiado; revisá la conexión y volvé a intentar."
                        else throw e
                    } catch (e: Exception) {
                        error = e.message ?: "No se pudo consultar el servicio"
                    } finally {
                        cardLoading = false
                    }
                }
        }
        fun toggleFavorite(item: Result) {
            if(item.category=="Aplicaciones" || item.id.startsWith("note:") || item.id.startsWith("snippet:") || item.id.startsWith("routine:")) {
                favorites=if(item.id in favorites) favorites-item.id else favorites+item.id
                scope.launch {app.preferences.favorite(item.id)}
            } else if(query.isNotBlank()) {
                val value=query.trim()
                pinned=if(value in pinned) pinned-value else pinned+value
                scope.launch {app.preferences.togglePin(value)}
            }
        }
        fun execute(item: Result) {
            error = ""
            val ask=item.action as? Action.Ask
            if(ask?.sensitive==true){clearForms();preview=null;menu=false;pendingAsk=ask;return}
            if(query.isNotBlank() && (item.action is Action.Device || item.category=="Aplicaciones" && item.action is Action.Launch)) app.scope.launch { app.preferences.recordAction(query) }
            if(item.action !is Action.Navigate && item.action !is Action.RestoreQuery && item.action !is Action.ReadNotification && (item.action as? Action.ComposeMessage)?.replyKey==null) app.scope.launch { app.preferences.recordSelection(item.id) }
            when (val action = item.action) {
                is Action.SystemPanel -> {push();clearForms();systemPage=action;preview=null;menu=false;card=null;noteEditor=false;keyboard?.hide()}
                is Action.ReadNotification -> {push();clearForms();notificationKey=action.key;preview=null;menu=false;card=null;noteEditor=false}
                is Action.EditClipboard -> {push();clearForms();clipEditing=action.text;preview=null;menu=false;card=null;noteEditor=false}
                is Action.ExpandSnippet -> {push();clearForms();snippetTemplate=action.template;preview=null;menu=false;card=null;noteEditor=false}
                is Action.TimerForm -> {push();clearForms();timerMinutes=action.minutes;preview=null;menu=false;card=null;noteEditor=false}
                is Action.SelectMode -> selectMode(action.mode,action.clearQuery)
                is Action.WorkflowRun -> {clearForms();preview=null;menu=false;pendingWorkflow=dev.bautista.spotlight.automation.Workflows.parse(config.routines).firstOrNull{it.id==action.id};if(pendingWorkflow==null)error="La rutina ya no existe."}
                is Action.Routine -> {
                    val timer=Intent(android.provider.AlarmClock.ACTION_SET_TIMER).putExtra(android.provider.AlarmClock.EXTRA_LENGTH,action.minutes*60).putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI,false)
                    if(context.packageManager.resolveActivity(timer,0)==null) error="No hay una app Reloj compatible; no se cambió el volumen."
                    else runCatching {
                        val audio=context.getSystemService(android.media.AudioManager::class.java)
                        audio.setStreamVolume(android.media.AudioManager.STREAM_MUSIC,audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)*action.volume/100,0)
                        launch(timer)
                    }.onFailure {error="No se pudo preparar la sesión."}
                }
                is Action.Shortcut -> runCatching { context.getSystemService(android.content.pm.LauncherApps::class.java).startShortcut(action.packageName,action.id,null,null,android.os.Process.myUserHandle());onClose() }.onFailure { error="El acceso directo ya no está disponible o Android no autoriza el lanzador." }
                is Action.Navigate -> {push();clearForms();searchScope=action.scope;if(action.scope.section!="results")query="";category=when(action.scope.section){"files","files-menu","files-mode"->"Archivos";"commands","topic"->"Acciones";"apps"->"Apps";"ai"->"IA";"clipboard"->"Portapapeles";else->"Todo"};results=emptyList();preview=null;menu=false;selected=0;focus.requestFocus()}
                is Action.ComposeMessage -> {push();clearForms();messageForm=action;preview=null;menu=false;card=null;noteEditor=false}
                is Action.ComposeSearch -> {push();clearForms();searchForm=action;preview=null;menu=false;card=null;noteEditor=false}
                is Action.AudioPanel -> {push();clearForms();audioPanel=true;preview=null;menu=false;card=null;noteEditor=false;keyboard?.hide()}
                is Action.Access -> when(action.access){
                    "contacts","calendar","base" -> launch(Intent(context,dev.bautista.spotlight.PermissionActivity::class.java).putExtra("access",action.access).putExtra("query",action.query))
                    "notifications" -> launch(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    else -> launch(Intent(context,dev.bautista.spotlight.MainActivity::class.java))
                }
                is Action.InApp -> {
                    runCatching {
                            context.startActivity(
                                Intent(action.intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                            onClose()
                        }
                        .onFailure { launch(action.fallback) }
                }
                is Action.Device -> {
                    runCatching {
                            val audio =
                                context.getSystemService(android.media.AudioManager::class.java)
                            val volume =
                                mapOf(
                                    "volume_up" to android.media.AudioManager.ADJUST_RAISE,
                                    "volume_down" to android.media.AudioManager.ADJUST_LOWER,
                                    "mute" to android.media.AudioManager.ADJUST_MUTE,
                                    "unmute" to android.media.AudioManager.ADJUST_UNMUTE,
                                )
                            if(action.command.startsWith("volume:"))audio.setStreamVolume(android.media.AudioManager.STREAM_MUSIC,(audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)*action.command.substringAfter(':').toInt().coerceIn(0,100)/100),android.media.AudioManager.FLAG_SHOW_UI)
                            else if (action.command in volume)
                                audio.adjustStreamVolume(
                                    android.media.AudioManager.STREAM_MUSIC,
                                    volume.getValue(action.command),
                                    android.media.AudioManager.FLAG_SHOW_UI,
                                )
                            else dev.bautista.spotlight.control.MediaControl.execute(context,action.command)
                            error = "Acción ejecutada"
                        }
                        .onFailure { error = "La app activa no admite esta acción" }
                }
                is Action.Copy -> copy(action.text)
                is Action.Launch -> {
                    launch(action.intent)
                    app.scope.launch { app.preferences.record(action.intent.component?.let { "${it.packageName}/${it.className}" } ?: item.id) }
                }
                is Action.RestoreQuery -> {
                    push();clearForms();searchScope=null;query=action.query;category="Todo";results=emptyList();selected=0;navigating=false;preview=null;menu=false
                    focus.requestFocus()
                }
                is Action.EditNote -> {
                    editingNoteId=action.id.takeIf{it.isNotBlank()};draft=action.text;noteEditor=true;preview=null;menu=false
                }
                is Action.NewNote -> {
                    editingNoteId=null
                    draft = ""
                    preview = null
                    menu = false
                    noteEditor = true
                }
                is Action.SaveNote ->
                    scope.launch {
                        runCatching { app.local.saveNote(action.text) }
                            .onSuccess {
                                searchScope=null
                                query = "notas"
                                category = "Acciones"
                            }
                            .onFailure { error = "No se pudo guardar la nota" }
                    }
                is Action.Lookup -> lookup(action)
                is Action.Ask -> {
                    if (!ai.configured(config)) {
                        error = "Guardá tu API key en Ajustes → Inteligencia artificial."
                    } else {
                        push();clearForms();showAi=true;category="Acciones";preview=null;menu=false
                        aiJob?.cancel()
                        aiText = ""
                        aiMessageTarget=action.messageTarget
                        aiLoading = true
                        aiJob =
                            scope.launch {
                                try {
                                    ai.answer(
                                            config,
                                            conversation.toList() + listOf("user" to action.prompt),
                                        )
                                        .collect { aiText += it }
                                    conversation.add("user" to action.prompt)
                                    conversation.add("assistant" to aiText)
                                    while (conversation.size > 12) conversation.removeAt(0)
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    error = e.message ?: "No se pudo consultar IA"
                                } finally {
                                    aiLoading = false
                                }
                            }
                    }
                }
            }
        }
        val snippetEditor:@Composable ()->Unit = {
            Column(Modifier.fillMaxWidth().padding(16.dp)){
                Text("Guardar fragmento",style=MaterialTheme.typography.titleMedium)
                OutlinedTextField(snippetAlias,{snippetAlias=it},label={Text("Nombre o alias")},modifier=Modifier.fillMaxWidth())
                Text("Variables: {fecha}, {hora}, {nombre} u otros campos (máximo 8)",style=MaterialTheme.typography.bodySmall)
                if(snippetError.isNotBlank())Text(snippetError,color=MaterialTheme.colorScheme.error)
                Row {
                    TextButton(onClick={if(!dev.bautista.spotlight.personal.PersonalCommands.validAlias(snippetAlias) || dev.bautista.spotlight.personal.PersonalCommands.fields(snippetText.orEmpty()).size>8)snippetError="Usá hasta 24 letras, números o guiones y hasta 8 campos." else scope.launch{val latest=app.preferences.config.first();app.preferences.update(latest.copy(snippets=latest.snippets.filterNot{it.substringBefore('|')==snippetAlias}.toSet()+"$snippetAlias|${snippetText.orEmpty().take(8000)}"));snippetText=null}}){Text("Guardar")}
                    TextButton(onClick={snippetText=null}){Text("Cancelar")}
                }
            }
        }
        val aiReview:@Composable ()->Unit = {
            pendingAsk?.let{ask->Column(Modifier.fillMaxWidth().padding(16.dp)){
                Text("Enviar texto a ${config.aiProvider}",style=MaterialTheme.typography.titleMedium)
                Text("Se enviarán ${ask.prompt.length} caracteres al proveedor elegido.",style=MaterialTheme.typography.bodySmall)
                SelectionContainer{Text(ask.prompt,Modifier.heightIn(max=220.dp).verticalScroll(rememberScrollState()),style=MaterialTheme.typography.bodySmall)}
                if(!ai.configured(config))AiSettings(config){next->app.scope.launch{runCatching{app.preferences.update(next)}}}
                Row{TextButton(enabled=ai.configured(config),onClick={pendingAsk=null;execute(Result("ai:confirmed","","","IA",0,ask.copy(sensitive=false)))}){Text("Enviar")};TextButton(onClick={pendingAsk=null}){Text("Cancelar")}}
            }}
        }
        LaunchedEffect(query){ if(showAi){aiJob?.cancel();showAi=false} }
        DisposableEffect(Unit){onDispose{aiJob?.cancel()}}
        LaunchedEffect(clips, clipFavorites, config.aliases, config.snippets, config.routines, notes, favorites, pinned, indexRevision, searchScope, replyTargets, replyConnected,activeContext,pinnedContexts,savedChats,config.privateMode,config.sourcePriority,config.suggestions,config.hiddenSuggestions,config.pinnedSuggestions,config.learning,config.recent,config.actionRecent) {
            snapshotFlow { query }
                .debounce(120)
                .distinctUntilChanged()
                .collectLatest { q ->
                    // Selection is reset by explicit edits/mode changes, never by a provider refresh.
                    if(previousSearch!=q){navigating=false;preview=null;menu=false;previousSearch=q}
                    cardJob?.cancel()
                    card = null
                    cardLoading = false
                    error = ""
                    loading = true
                    val searchStarted=android.os.SystemClock.elapsedRealtime()
                    val parsed = SearchQuery.parse(q)
                    if (parsed.problem != null) error = parsed.problem
                    try {
                        coroutineScope {
                            val localRequest=TextTools.results(q).mapNotNull{it.action as? Action.Lookup}.firstOrNull{it.type.startsWith("text:")}
                            val request = localRequest ?: app.online.request(q)
                            if (searchScope==null && ActionCatalog.topic(q)==null && (config.online || localRequest!=null) && request != null && request.type.startsWith("text:") && !config.privateMode)
                                launch {
                                    cardLoading = true
                                    try {
                                        card =
                                            withTimeout(35000) {
                                                app.online.lookup(request, config)
                                            }
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: Exception) {
                                        error = e.message ?: "No se pudo consultar"
                                    } finally {
                                        cardLoading = false
                                    }
                                }
                            app.search.search(q, config.copy(favorites = favorites,pins=pinned),searchScope).collect {
                                val selectedId=displayed.getOrNull(selected)?.id
                                results = StableResults.merge(results,it,navigating || preview!=null)
                                if(selectedId!=null && navigating){selected=results.indexOfFirst{it.id==selectedId}.coerceAtLeast(0)}
                                app.diagnostics.search(android.os.SystemClock.elapsedRealtime()-searchStarted)
                            }
                        }
                    } catch(e:CancellationException) {
                        throw e
                    } catch(e:Exception) {
                        error="Una fuente no respondió. Los resultados disponibles se conservan; probá de nuevo o revisá sus permisos."
                    } finally {
                        loading = false
                    }
                }
        }
        LaunchedEffect(Unit) {
            withFrameNanos{}
            focus.requestFocus()
            val device = context.resources.configuration
            if (
                device.keyboard == android.content.res.Configuration.KEYBOARD_NOKEYS ||
                    device.hardKeyboardHidden !=
                        android.content.res.Configuration.HARDKEYBOARDHIDDEN_NO
            )
                if(config.autoKeyboard=="always" || config.autoKeyboard=="adaptive" && invocation=="keyboard")keyboard?.show()
                else keyboard?.hide()
        }
        LaunchedEffect(selected, category, displayed.size, navigating) {
            if (displayed.isNotEmpty() && navigating) {
                val target = selected.coerceIn(0, displayed.lastIndex)
                if (category == "Apps") {
                    if (motionEnabled)
                        gridState.animateScrollToItem(
                            target + 1 + if (recentCount > 0 && target >= recentCount) 1 else 0
                        )
                    else
                        gridState.scrollToItem(
                            target + 1 + if (recentCount > 0 && target >= recentCount) 1 else 0
                        )
                } else {
                    if (motionEnabled) listState.animateScrollToItem(target)
                    else listState.scrollToItem(target)
                }
            }
        }
        BoxWithConstraints(
            Modifier.fillMaxWidth()
                .testTag("spotlight-root")
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val compact = maxWidth < 560.dp
            // WindowManager WRAP_CONTENT bounds change after every layout. Never
            // use this Box's previous height as the next result pane's limit.
            val viewport =
                (availableHeightDp
                        ?: (LocalConfiguration.current.screenHeightDp * (1f - config.position) -
                            24f))
                    .dp
            val widePreview=config.adaptive && maxWidth>=600.dp
            val panelLimit = (viewport - if(compact && config.adaptive)152.dp else 96.dp).coerceIn(0.dp, config.maxHeight.dp)
            val resultLimit = (panelLimit - 90.dp).coerceAtLeast(0.dp)
            val columns = (maxWidth.value / 96f).toInt().coerceIn(3, 7)
            Column(
                Modifier.fillMaxWidth()
                    .glassSurface(config)
                    .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) false
                    else if (
                        noteEditor &&
                            (event.isCtrlPressed || event.isMetaPressed) &&
                            event.key == Key.S
                    ) {
                        saveDraft()
                        true
                    } else if(menu && event.key in listOf(Key.Tab,Key.Enter,Key.NumPadEnter,Key.DirectionUp,Key.DirectionDown,Key.DirectionLeft,Key.DirectionRight)) false
                    else if ((noteEditor || systemPage!=null || messageForm!=null || searchForm!=null || pendingAsk!=null || pendingWorkflow!=null || snippetText!=null || clipEditing!=null || snippetTemplate!=null || timerMinutes!=null || deletingNote!=null || notificationKey!=null) && event.key !in listOf(Key.Escape, Key.Back)) false
                    else if (
                        (event.isCtrlPressed || event.isMetaPressed) &&
                            event.key in listOf(Key.One, Key.Two, Key.Three, Key.Four)
                    ) {
                        selectMode(listOf("Apps","Archivos","Acciones","IA")[listOf(Key.One,Key.Two,Key.Three,Key.Four).indexOf(event.key)])
                        selected = 0
                        preview = null
                        menu = false
                        true
                    } else if ((event.isCtrlPressed || event.isMetaPressed) && ((config.clipboardShortcut=="ctrl_shift_v" && event.isShiftPressed && event.key==Key.V) || (config.clipboardShortcut=="ctrl_p" && !event.isShiftPressed && event.key==Key.P))) {selectMode("Portapapeles",true);true
                    } else if ((event.isCtrlPressed || event.isMetaPressed) && event.key == Key.K) {
                        chosen?.let {
                            preview=it
                            menu=!menu
                            navigating=true
                        }
                        true
                    } else if ((event.isCtrlPressed || event.isMetaPressed) && event.key == Key.F) {
                        chosen?.let { toggleFavorite(it) }
                        true
                    } else if ((event.isCtrlPressed || event.isMetaPressed) && event.key == Key.C) {
                        copy(
                            activeCard?.copyText
                                ?: (chosen?.action as? Action.Copy)?.text
                                ?: chosen?.preview
                                ?: chosen?.title
                                ?: query
                        )
                        true
                    } else if ((event.isCtrlPressed || event.isMetaPressed) && event.key == Key.B) {
                        web()
                        true
                    } else if ((event.isCtrlPressed || event.isMetaPressed) && event.key == Key.R) {
                        chosen?.let { locate(it) }
                        true
                    } else
                        when (event.key) {
                            Key.Escape,
                            Key.Back -> {
                                if(searchOptions){searchOptions=false;focus.requestFocus()}
                                else if (preview != null) {preview=null;menu=false;focus.requestFocus()}
                                else if(systemPage!=null || messageForm!=null || searchForm!=null || audioPanel || pendingAsk!=null || pendingWorkflow!=null || snippetText!=null || clipEditing!=null || snippetTemplate!=null || timerMinutes!=null || deletingNote!=null || notificationKey!=null || searchScope!=null || history.isNotEmpty())back()
                                else if (noteEditor) noteEditor = false
                                else onClose()
                                true
                            }
                            Key.Tab -> {
                                if (suggestion != null && !event.isShiftPressed) {
                                    acceptSuggestion()
                                } else if (chosen != null) {
                                    preview = chosen
                                    menu = !menu
                                    navigating = true
                                }
                                true
                            }
                            Key.Spacebar -> {
                                if (navigating || event.isCtrlPressed) {
                                    preview = if (preview?.id == chosen?.id) null else chosen
                                    menu = false
                                    true
                                } else false
                            }
                            Key.DirectionDown -> {
                                selected =
                                    if (category == "Apps")
                                        dev.bautista.spotlight.control.CommandCenter.moveGrid(
                                            selected,
                                            true,
                                            columns,
                                            recentCount,
                                            displayed.size,
                                        )
                                    else
                                        (selected + 1).coerceAtMost(
                                            (displayed.size - 1).coerceAtLeast(0)
                                        )
                                navigating = true
                                true
                            }
                            Key.DirectionUp -> {
                                selected =
                                    if (category == "Apps")
                                        dev.bautista.spotlight.control.CommandCenter.moveGrid(
                                            selected,
                                            false,
                                            columns,
                                            recentCount,
                                            displayed.size,
                                        )
                                    else (selected - 1).coerceAtLeast(0)
                                navigating = true
                                true
                            }
                            Key.DirectionLeft,
                            Key.DirectionRight -> {
                                if (category == "Apps" && navigating) {
                                    selected =
                                        (selected + if (event.key == Key.DirectionRight) 1 else -1)
                                            .coerceIn(0, (displayed.size - 1).coerceAtLeast(0))
                                    true
                                } else false
                            }
                            Key.Enter,
                            Key.NumPadEnter -> {
                                chosen?.let { execute(it) }
                                true
                            }
                            else -> false
                        }
                }
            ) {
                SpotlightBar(
                    query,
                    {
                        showAi=false
                        clearForms()
                        query = it
                        selected=0
                        navigating = false
                        results = emptyList()
                        card = null
                        preview = null
                        menu = false
                    },
                    category,
                    {
                        selectMode(it)
                    },
                    {
                        history.clear();clearForms();searchScope=null;query = ""
                        card = null
                        aiText = ""
                        category = "Todo"
                    },
                    { chosen?.let { execute(it) } },
                    focus,
                    config.opacity,
                    compact,
                    suggestion,
                    { acceptSuggestion() },
                    motionEnabled,
                    onDismiss=onClose,
                    onOptionsChange={searchOptions=it},
                    optionsOpen=searchOptions,
                    onSearchFocus={app.diagnostics.markInteractive()},
                    quickMenu={
                        GlassQuickMenu(config,{next->app.scope.launch{runCatching{app.preferences.update(next)}.onFailure{error="No se pudo guardar la preferencia. Reintentá."}}},
                            {launch(Intent(context,dev.bautista.spotlight.MainActivity::class.java))},
                            {mode->searchOptions=false;selectMode(mode)})
                    },
                    optionsMaxHeight=minOf(320.dp,(viewport-61.dp).coerceAtLeast(0.dp)),
                    onDictate={launch(Intent(context,dev.bautista.spotlight.VoiceActivity::class.java).putExtra("prefix",if(category=="IA")"ia " else "").putExtra("fallback",query))},
                )
                if(config.privateMode)Text("Modo privado · aprendizaje y captura suspendidos",Modifier.padding(horizontal=18.dp,vertical=4.dp),style=MaterialTheme.typography.labelSmall,color=colors.primary)
                if(activeContext!=null && !searchOptions)ContextChip(app,activeContext!!){action->execute(Result("context:active","","","Contexto",0,action))}
                val expanded = systemPage!=null || query.isNotBlank() || searchScope!=null || messageForm!=null || searchForm!=null || audioPanel || pendingAsk!=null || pendingWorkflow!=null || snippetText!=null || clipEditing!=null || snippetTemplate!=null || timerMinutes!=null || deletingNote!=null || notificationKey!=null || category != "Todo" || noteEditor || (config.suggestions && displayed.isNotEmpty())
                val resultList:@Composable ()->Unit = {
                            LazyColumn(
                                state = listState,
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .heightIn(
                                            max =
                                                if (preview != null && !widePreview) resultLimit * .45f
                                                else minOf(resultLimit,(displayed.size*70+displayed.map{ResultSections.category(it)}.distinct().size*30+12).dp)
                                        )
                                        .padding(horizontal = 9.dp, vertical = 4.dp),
                            ) {
                                itemsIndexed(displayed, key = { _, item -> item.id }) { i, item ->
                                    if(query.isNotBlank() && searchScope==null && category=="Todo" && i in 0..1)Text(if(i==0)"Mejor coincidencia" else "Otros resultados",Modifier.padding(horizontal=12.dp,vertical=6.dp),style=MaterialTheme.typography.labelSmall,color=colors.onSurfaceVariant)
                                    val section=ResultSections.category(item)
                                    if(query.isBlank() && displayed.map{ResultSections.category(it)}.distinct().size>1 && displayed.take(i).none{ResultSections.category(it)==section}) SectionHeading(section)
                                    ResultRow(
                                        item,
                                        i == selected,
                                        if(item.category=="Aplicaciones" || item.id.startsWith("note:") || item.id.startsWith("snippet:") || item.id.startsWith("routine:"))item.id in favorites else query.trim() in pinned,
                                        {
                                            selected = i
                                            execute(item)
                                        },
                                        {
                                            selected = i
                                            preview = item
                                            menu = true
                                            navigating = true
                                        },
                                        { toggleFavorite(item) },
                                        animations=motionEnabled,
                                        onInteraction={selected=i;navigating=true},
                                    )
                                }
                            }
                }
                val details:@Composable ()->Unit = {
                        preview?.let { item ->
                            if(item.action is Action.ReadNotification)NotificationPreview(item.action.key,{action->execute(Result("message:context","","","Mensajes",0,action))},{copy(it)},{preview=null;menu=false}) else Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ResultIcon(item,Modifier.size(28.dp));Spacer(Modifier.width(10.dp))
                                    Text(
                                        item.title,
                                        Modifier.weight(1f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    IconButton(
                                        onClick = {
                                            preview = null
                                            menu = false
                                        },
                                        modifier = Modifier.size(28.dp),
                                    ) {
                                        Icon(
                                            CommandIcons.Close,
                                            "Cerrar vista rápida",
                                            Modifier.size(17.dp),
                                        )
                                    }
                                }
                                if (
                                    item.category == "Archivos" &&
                                        item.mime != DocumentsContract.Document.MIME_TYPE_DIR
                                )
                                    FilePreview(context, item)
                                SelectionContainer {
                                    Text(
                                        item.preview ?: item.subtitle,
                                        Modifier.heightIn(max = 120.dp)
                                            .verticalScroll(rememberScrollState()),
                                        fontSize = 12.sp,
                                        color = colors.onSurfaceVariant,
                                    )
                                }
                                QuickLookMetadata(item)
                                if(menu) {
                                    val actions=mutableListOf<ContextAction>()
                                    fun add(title:String,description:String,icon:androidx.compose.ui.graphics.vector.ImageVector=CommandIcons.Bolt,perform:()->Unit){actions+=ContextAction(title,description,icon,perform)}
                                    add(when(item.action){is Action.Copy->"Copiar resultado";is Action.EditNote->"Editar nota";is Action.WorkflowRun->"Revisar rutina";is Action.ComposeMessage->"Preparar mensaje";is Action.ComposeSearch->"Buscar contenido";else->if(item.category in setOf("Aplicaciones","Archivos"))"Abrir" else "Ejecutar acción"},item.subtitle,CommandIcons.Chevron){execute(item)}
                                    if(item.id.startsWith("suggest:")) {
                                        add(if(item.id in config.pinnedSuggestions)"Desfijar sugerencia" else "Fijar sugerencia","Prioridad en el inicio",CommandIcons.Star){scope.launch{val current=app.preferences.config.first();app.preferences.update(current.copy(pinnedSuggestions=if(item.id in current.pinnedSuggestions)current.pinnedSuggestions-item.id else current.pinnedSuggestions+item.id))};preview=null;menu=false}
                                        add("Ocultar sugerencia","Restablecer desde General",CommandIcons.Close){scope.launch{val current=app.preferences.config.first();app.preferences.update(current.copy(hiddenSuggestions=current.hiddenSuggestions+item.id))};preview=null;menu=false}
                                    }
                                    if(item.category=="Aplicaciones")add("Acciones de ${item.title}","Capacidades públicas disponibles",CommandIcons.Apps){execute(Result("app:actions",item.title,"","Acciones",0,Action.RestoreQuery(item.title)))}
                                    if(item.category=="Aplicaciones" || item.id.startsWith("note:") || item.id.startsWith("snippet:") || item.id.startsWith("routine:"))add(if(item.id in favorites)"Quitar favorita" else "Añadir favorita","Acceso desde la búsqueda principal",CommandIcons.Star){toggleFavorite(item)}
                                    if(item.action !is Action.Copy)add("Copiar texto","Texto de este resultado",CommandIcons.ContentCopy){copy(item.preview ?: item.title)}
                                    if(query.isNotBlank())add(if(query.trim() in pinned)"Desfijar consulta" else "Fijar consulta","Guardar esta búsqueda",CommandIcons.StarBorder){scope.launch{app.preferences.togglePin(query.trim())};pinned=if(query.trim() in pinned)pinned-query.trim() else pinned+query.trim()}
                                    if(item.category in setOf("Archivos","Notas","Portapapeles","Fragmentos de texto"))add("Compartir","Selector de aplicaciones Android",CommandIcons.Share){share(item)}
                                    if(item.category=="Archivos")add("Copiar nombre","Nombre del archivo",CommandIcons.Description){copy(item.title)}
                                    if(item.parentUri!=null)add("Mostrar carpeta","Ubicación del archivo",CommandIcons.Folder){locate(item)}
                                    if(item.category=="Aplicaciones")add("Permisos y componentes públicos","Consulta local · administración opcional con Shizuku",CommandIcons.Shield){execute(item.copy(action=Action.SystemPanel("app",item.id.substringBefore('/'))))}
                                    if(item.category=="Aplicaciones")add("Información de la app","Permisos y almacenamiento",CommandIcons.Info){launch(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+item.id.substringBefore('/'))))}
                                    if(item.category=="Archivos" && item.action is Action.Launch && item.action.intent.data!=null)add("Extraer texto","PDF, Office, imagen o texto · procesamiento local",CommandIcons.FindInPage){scope.launch{try{val extracted=dev.bautista.spotlight.index.DocumentText.extract(context,item.action.intent.data!!,dev.bautista.spotlight.index.OfficeText.mime(item.title,item.mime));preview=item.copy(preview=extracted);menu=true}catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "No se pudo extraer texto"}}}
                                    if(item.category=="Portapapeles") {val clip=item.preview ?: (item.action as? Action.Copy)?.text.orEmpty();add("Editar texto","Modificar el historial temporal",CommandIcons.EditNote){execute(item.copy(action=Action.EditClipboard(clip)))};add(if(clip in clipFavorites)"Quitar favorito temporal" else "Favorito temporal","Se elimina al caducar o cerrar el proceso",CommandIcons.Star){app.local.toggleClipFavorite(clip);preview=null;menu=false}}
                                    if(item.category=="Portapapeles")add("Eliminar del historial","Eliminar solo de Spotlight",CommandIcons.Delete){app.local.deleteClip(item.preview ?: (item.action as? Action.Copy)?.text.orEmpty());preview=null}
                                    val text=item.preview ?: (item.action as? Action.Copy)?.text
                                    if(!text.isNullOrBlank()) {
                                        add("Guardar fragmento","Texto reutilizable con alias",CommandIcons.Snippet){clearForms();preview=null;menu=false;snippetText=text;snippetAlias="";snippetError=""}
                                        add("Guardar como nota","Guardar texto en el dispositivo",CommandIcons.EditNote){scope.launch{app.local.saveNote(text)}}
                                        add("Traducir texto","Elegir idioma y procesar localmente",CommandIcons.Language){execute(Result("translate:text","","","Herramientas",0,Action.RestoreQuery("traducir en a es: "+text)))}
                                        listOf("Resumir" to "Resumí", "Corregir" to "Corregí", "Reescribir" to "Reescribí", "Explicar" to "Explicá", "Traducir" to "Traducí al español", "Simplificar" to "Simplificá", "Extraer ideas" to "Extraé las ideas principales de", "Crear lista" to "Convertí en una lista", "Preparar mensaje" to "Convertí en un mensaje breve").forEach{(label,prompt)->add("$label con IA",if(ai.configured(config))"Revisar antes de enviar al proveedor" else "Requiere configurar API key",CommandIcons.AutoAwesome){execute(Result("ai:text",label,"","IA",0,Action.Ask(prompt+" este texto:\n"+text,true)))}}
                                    }
                                    if(item.id.startsWith("note:"))add("Eliminar nota","Revisar antes de eliminar",CommandIcons.Delete){clearForms();deletingNote=item;preview=null;menu=false}
                                    ActionPanelView(actions,actionFocus)
                                }
                            }
                        }
                }
                if (!searchOptions && expanded && panelLimit >= 100.dp) {
                    Column(
                        Modifier.fillMaxWidth()
                            .testTag("spotlight-results")
                            .heightIn(max = panelLimit)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(Modifier.fillMaxWidth().height(2.dp)) {
                            if (loading || cardLoading)
                                LinearProgressIndicator(Modifier.fillMaxWidth())
                        }
                        HorizontalDivider(color=colors.outlineVariant)
                        if(systemPage!=null || messageForm!=null || searchForm!=null || audioPanel || pendingAsk!=null || pendingWorkflow!=null || snippetText!=null || clipEditing!=null || snippetTemplate!=null || timerMinutes!=null || deletingNote!=null || notificationKey!=null || searchScope!=null || category!="Todo" || noteEditor || history.isNotEmpty()) Row(
                            Modifier.fillMaxWidth().padding(start = 18.dp, end = 6.dp, top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                if(systemPage!=null || messageForm!=null || searchForm!=null || audioPanel || pendingAsk!=null || pendingWorkflow!=null || snippetText!=null || clipEditing!=null || snippetTemplate!=null || timerMinutes!=null || deletingNote!=null || notificationKey!=null || searchScope!=null) searchScope?.label?.uppercase() ?: systemPage?.let{if(it.page=="interpret")"INTERPRETACIÓN" else panelLabel(it.page)} ?: "ACCIONES" else ActionCatalog.topic(query)?.let{it.uppercase()+" · ACCIONES"} ?: if (category == "Todo" && query.isBlank()) "" else if (category == "Todo") ""
                                else category.uppercase(),
                                Modifier.weight(1f),
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                color = colors.onSurfaceVariant,
                            )
                            if(systemPage!=null || searchScope!=null || messageForm!=null || searchForm!=null || audioPanel || pendingAsk!=null || pendingWorkflow!=null || snippetText!=null || clipEditing!=null || snippetTemplate!=null || timerMinutes!=null || deletingNote!=null || notificationKey!=null || history.isNotEmpty())TextButton(onClick={preview=null;menu=false;back()}){Text("Volver",fontSize=11.sp)}
                            if (category != "Todo")
                                TextButton(onClick = { selectMode("Todo") }) {
                                    Text("Todo", fontSize = 11.sp)
                                }
                            IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
                                Icon(CommandIcons.Close, "Cerrar Spotlight", Modifier.size(18.dp))
                            }
                        }
                        if (error.isNotBlank())
                            Text(
                                error,
                                Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                                color = colors.error,
                                fontSize = 12.sp,
                            )
                        if(searchScope?.section=="files" && !loading && displayed.isEmpty())Text("No se encontraron archivos de este tipo. Probá otro filtro o actualizá el índice en Opciones.",Modifier.padding(horizontal=18.dp,vertical=12.dp),fontSize=13.sp,color=colors.onSurfaceVariant)
                        if(pendingAsk!=null)aiReview()
                        else if(pendingWorkflow!=null)WorkflowRunner(pendingWorkflow!!,app.local,onAction={action->execute(Result("routine:prepared","","","Acciones",0,action))}){pendingWorkflow=null}
                        else if(clipEditing!=null)ClipboardEditor(clipEditing!!,{app.local.editClip(clipEditing!!,it);back()},{back()})
                        else if(snippetTemplate!=null)SnippetForm(snippetTemplate!!,{copy(it);back()},{back()})
                        else if(timerMinutes!=null)TimerForm(timerMinutes!!){back()}
                        else if(deletingNote!=null)Column(Modifier.padding(16.dp)){Text("¿Eliminar esta nota?");Text(deletingNote!!.title.take(160));Row{TextButton(onClick={val id=deletingNote!!.id.removePrefix("note:");scope.launch{app.local.deleteNote(id);back()}}){Text("Eliminar")};TextButton(onClick={back()}){Text("Cancelar")}}}
                        else if(snippetText!=null)snippetEditor()
                        else if(category=="IA" && query.isBlank())Column(Modifier.fillMaxWidth().heightIn(max=resultLimit).verticalScroll(rememberScrollState()).padding(18.dp)) {Text("Preguntá o pegá un texto en la barra",style=MaterialTheme.typography.titleMedium);TextButton(onClick={execute(Result("ai:tools","","","IA",0,Action.SystemPanel("agent")))}){Text("Preparar acciones con IA")};Text("Las consultas se envían al proveedor que elegiste. Los archivos y mensajes requieren revisión.");if(!ai.configured(config))AiSettings(config){next->app.scope.launch{runCatching{app.preferences.update(next)}}} else Text("${config.aiProvider.replaceFirstChar{it.uppercase()}} · ${config.aiModel}",color=colors.primary)}
                        else if(notificationKey!=null)NotificationPreview(notificationKey!!,{action->execute(Result("message:context","","","Mensajes",0,action))},{copy(it)},{back()})
                        else if(systemPage!=null) {if(systemPage!!.page in setOf("context","context-ai","conversation","diagnostics"))ContextPanel(app,config,systemPage!!.page,systemPage!!.packageName){action->execute(Result("context:action","","","Contexto",0,action))} else if(systemPage!!.page=="interpret")InterpretationPanel(app,config,systemPage!!.packageName){action->execute(Result("plan:action","","","Comandos",0,action))} else if(systemPage!!.page in setOf("profiles","catalog","display-review","tasker","weather-native","sports-native"))NextPanel(app,systemPage!!.page,systemPage!!.packageName){action->execute(Result("next:action","","","Comandos",0,action))} else if(systemPage!!.page=="focus")ProductivityPanel{action->execute(Result("focus:action","","","Acciones",0,action))} else if(systemPage!!.page.startsWith("preferences:"))PreferencePanel(config,systemPage!!.page.substringAfter(':')){next->app.scope.launch{runCatching{app.preferences.update(next)}}} else if(systemPage!!.page=="agent")AiToolPanel(app,config,query){action->execute(Result("ai:tool","","","IA",0,action))} else if(systemPage!!.page=="extensions")ExtensionPanel(app,config){action->execute(Result("extension:execute","","","Comandos",0,action))} else if(systemPage!!.page=="app")ApplicationInspector(app,systemPage!!.packageName){action->execute(Result("app:inspect:action","","","Acciones",0,action))} else AdvancedPanel(app,systemPage!!.page){back()}}
                        else if(messageForm!=null)MessageEditor(messageForm!!,{launch(it)},{pending->launch(Intent(context,dev.bautista.spotlight.PermissionActivity::class.java).putExtra("access","contacts").putExtra("query",pending))},{back()})
                        else if(searchForm!=null)SearchEditor(searchForm!!,{execute(Result("form:search","Buscar","","Acciones",0,it))},{back()})
                        else if(audioPanel)AudioControl {back()}
                        else if (noteEditor)
                            Column(Modifier.padding(18.dp)) {
                                Text(if(editingNoteId==null) "Nueva nota" else "Editar nota");if(editingNoteId==null)TextButton(onClick={launch(Intent(context,dev.bautista.spotlight.VoiceActivity::class.java).putExtra("prefix","nota ").putExtra("fallback","nota $draft"))}){Text("Dictar nota · revisar antes de guardar")}
                                OutlinedTextField(
                                    draft,
                                    { draft = it.take(8000) },
                                    Modifier.fillMaxWidth()
                                        .heightIn(min = 140.dp)
                                        .focusRequester(editorFocus),
                                    label = { Text("Texto de la nota") },
                                )
                                Row {
                                    TextButton(
                                        onClick = { saveDraft() },
                                        enabled = draft.isNotBlank(),
                                    ) {
                                        Text("Guardar")
                                    }
                                    TextButton(onClick = { noteEditor = false }) {
                                        Text("Cancelar")
                                    }
                                }
                            }
                        else if (activeCard != null)
                            InfoCardView(
                                activeCard,
                                { copy(activeCard.copyText) },
                                {
                                    activeCard.url?.let {
                                        launch(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                                    }
                                },
                                {launch(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,activeCard.copyText),"Compartir resultado"))},
                            )
                        else if (
                            category in setOf("Todo", "Acciones", "IA") &&
                                (aiLoading || aiText.isNotBlank()) &&
                                showAi
                        )
                            Column(
                                Modifier.heightIn(max = resultLimit)
                                    .verticalScroll(rememberScrollState())
                                    .padding(18.dp)
                            ) {
                                Text("Inteligencia artificial", fontWeight = FontWeight.SemiBold)
                                SelectionContainer {
                                    Text(
                                        if (aiText.isBlank()) "Pensando…" else aiText,
                                        lineHeight = 23.sp,
                                    )
                                }
                                OutlinedTextField(followUp,{followUp=it},label={Text("Continuar conversación")},modifier=Modifier.fillMaxWidth())
                                TextButton(enabled=!aiLoading && followUp.isNotBlank(),onClick={val prompt=followUp;followUp="";execute(Result("ai:followup","","","IA",0,Action.Ask(prompt)))}){Text("Enviar") }
                                if(aiMessageTarget.isNotBlank() && aiText.isNotBlank() && !aiLoading)TextButton(onClick={execute(Result("ai:message","Preparar mensaje","","Comunicación",0,Action.ComposeMessage(config.messageApp,aiMessageTarget,aiText)))}){Text("Preparar mensaje para $aiMessageTarget")}
                                Row {
                                    TextButton(enabled=aiText.isNotBlank() && !aiLoading,onClick={scope.launch{app.contexts.saveConversation(conversation.lastOrNull{it.first=="user"}?.second.orEmpty(),aiText)}}){Text("Guardar conversación")}
                                    TextButton(onClick={share(Result("ai:share","Respuesta","","IA",0,Action.Copy(aiText),preview=aiText))}){Text("Compartir")}
                                    TextButton(onClick = { copy(aiText) }) { Text("Copiar") }
                                    TextButton(
                                        onClick = {
                                            aiJob?.cancel()
                                            aiText = ""
                                            conversation.clear();aiMessageTarget=""
                                        }
                                    ) {
                                        Text(if (aiLoading) "Detener" else "Nueva conversación")
                                    }
                                }
                            }
                        else if (category == "Apps")
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(columns),
                                state = gridState,
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .height(
                                            minOf(
                                                resultLimit,
                                                (maxOf(
                                                        1,
                                                        ((displayed.size - recentCount + columns -
                                                            1) / columns) +
                                                            if (recentCount > 0)
                                                                (recentCount + columns - 1) /
                                                                    columns
                                                            else 0,
                                                    ) * 96)
                                                    .dp + (if (recentCount > 0) 56.dp else 28.dp),
                                            )
                                        )
                                        .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                displayed.forEachIndexed { i, item ->
                                    if (i == 0 || (recentCount > 0 && i == recentCount)) {
                                        item(
                                            key = "heading:$i",
                                            span = { GridItemSpan(maxLineSpan) },
                                        ) {
                                            Text(
                                                if (recentCount > 0 && i == 0) "Recientes · hasta 5"
                                                else "Todas las aplicaciones · A–Z",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier =
                                                    Modifier.padding(top = 8.dp, bottom = 4.dp),
                                            )
                                        }
                                    }
                                    item(key = "$i:${item.id}") {
                                        Column(
                                            Modifier.clip(RoundedCornerShape(14.dp))
                                                .background(
                                                    if (i == selected && navigating)
                                                        colors.primary.copy(alpha = .12f)
                                                    else Color.Transparent
                                                )
                                                .combinedClickable(
                                                    onClick = {
                                                        selected = i
                                                        execute(item)
                                                    },
                                                    onLongClick = {
                                                        selected = i
                                                        preview = item
                                                        menu = true
                                                        navigating = true
                                                    },
                                                )
                                                .padding(vertical = 12.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            ResultIcon(item, Modifier.size(42.dp))
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                item.title,
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            if (item.id in favorites)
                                                Icon(
                                                    CommandIcons.Star,
                                                    "Favorita",
                                                    Modifier.size(12.dp),
                                                    tint = colors.primary,
                                                )
                                        }
                                    }
                                }
                            }
                        else if(widePreview && preview!=null) Row(Modifier.fillMaxWidth()){
                            Box(Modifier.weight(.52f)){resultList()}
                            VerticalDivider(Modifier.height(minOf(resultLimit,320.dp)))
                            Column(Modifier.weight(.48f)){details()}
                        } else if(menu && preview!=null && !widePreview) {
                            // On narrow windows, give contextual actions the full result area.
                            // Closing Quick Look restores the list and its selection.
                        } else resultList()
                        if (displayed.isEmpty() && activeCard == null && !loading && !noteEditor && systemPage==null && messageForm==null && searchForm==null && !audioPanel && pendingAsk==null && pendingWorkflow==null && snippetText==null && clipEditing==null && snippetTemplate==null && timerMinutes==null && deletingNote==null && notificationKey==null && category!="IA" && searchScope?.section!="files")
                            Text(
                                if(query.isBlank() && category=="Todo")"Escribí lo que querés encontrar o hacer." else when (category) {
                                    "Archivos" ->
                                        if (!app.index.broadAllowed() && config.trees.isEmpty())
                                            "Activá el acceso al almacenamiento compartido en Opciones."
                                        else "No se encontraron archivos con esos filtros."
                                    "Portapapeles" ->
                                        if (!config.clipboard)
                                            "Activá el historial optativo en Opciones."
                                        else "El historial está vacío."
                                    else -> "No hay resultados para esta consulta."
                                },
                                Modifier.padding(18.dp),
                                fontSize = 13.sp,
                                color = colors.onSurfaceVariant,
                            )
                        if(!widePreview || category=="Apps")details()
                        if (category == "Archivos" && (indexStatus.truncated || indexStatus.busy))
                            Text(
                                indexStatus.message,
                                Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                color = colors.onSurfaceVariant,
                            )
                        HorizontalDivider(color = colors.onSurface.copy(alpha = .07f))
                        Text(
                            if(systemPage!=null || messageForm!=null || searchForm!=null) "Tab siguiente campo   esc volver" else if (suggestion != null)
                                "Tab completar   ↑↓ elegir   ↵ abrir   esc cerrar"
                            else
                                "↑↓ elegir   ↵ abrir   espacio vista rápida   Ctrl+K acciones   esc cerrar",
                            Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
