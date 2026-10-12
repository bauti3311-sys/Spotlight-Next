package dev.bautista.spotlight.search

import dev.bautista.spotlight.control.*
import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import androidx.core.content.ContextCompat
import dev.bautista.spotlight.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class SearchEngine(
    private val context: Context,
    private val prefs: Preferences,
    private val local: LocalStore,
    private val index: dev.bautista.spotlight.index.FileIndex,
) {
    private val integrations = IntegrationRegistry(context)
    private data class App(val id: String, val label: String, val intent: Intent,val category:Int=-1)

    @Volatile private var apps: List<App>? = null

    private suspend fun appList(): List<App> =
        withContext(Dispatchers.IO) {
            apps
                ?: context.packageManager
                    .queryIntentActivities(
                        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
                        0,
                    )
                    .map { info ->
                        App(
                            "${info.activityInfo.packageName}/${info.activityInfo.name}",
                            info.loadLabel(context.packageManager).toString(),
                            Intent(Intent.ACTION_MAIN)
                                .addCategory(Intent.CATEGORY_LAUNCHER)
                                .setComponent(
                                    ComponentName(
                                        info.activityInfo.packageName,
                                        info.activityInfo.name,
                                    )
                                ),
                            info.activityInfo.applicationInfo?.category ?: -1,
                        )
                    }
                    .distinctBy { it.id }
                    .also { apps = it }
        }

    fun invalidateApps() {
        apps = null
    }

    fun search(input:String,c:Config,scope:SearchScope?=null):Flow<List<Result>> = channelFlow {
        val interpretation=QueryInterpreter.interpret(input,scope)
        val effectiveScope=interpretation.scope
        val canonical=LocalLanguage.canonical(input)
        val query=interpretation.text
        val root=effectiveScope==null || effectiveScope.section in setOf("apps","commands","ai","files-mode") && input.isNotBlank()
        val natural=if(root && input.isNotBlank())RootActions.results(context,input,c) else emptyList()
        val registered=if(root && input.length>=3)dev.bautista.spotlight.extensions.ExtensionRegistry.searchCommands(context,canonical,c) else emptyList()
        val saved=if(root && input.isNotBlank())(context.applicationContext as? dev.bautista.spotlight.SpotlightApp)?.contexts?.results(input).orEmpty() else emptyList()
        searchSources(query,c,effectiveScope).collect { rows ->
            val all=if(root && input.isNotBlank())StableResults.unique(ResultRanker.rank(query,natural+rows.map{item->when(val action=item.action){is Action.SystemPanel->if(action.page=="interpret")item.copy(action=action.copy(packageName=input)) else item;is Action.ComposeMessage->if(action.channel.startsWith("whatsapp") && context.packageManager.getLaunchIntentForPackage(if(action.channel=="whatsapp_business")"com.whatsapp.w4b" else "com.whatsapp")==null)item.copy(title="WhatsApp no está instalado",subtitle="Revisar integración · no se cambia el canal automáticamente",action=Action.SystemPanel("extensions")) else item;else->item}}+registered+saved,c)) else rows
            send(all)
        }
    }.flowOn(Dispatchers.Default)

    private fun searchSources(input: String, c: Config, scope:SearchScope?=null): Flow<List<Result>> = channelFlow {
        val query=ActionCatalog.canonical(dev.bautista.spotlight.personal.PersonalCommands.expand(input,c.aliases))
        if(scope==null && query.isBlank()){send(withContext(Dispatchers.IO){HomeSuggestions.results(context,c,c.files && (index.broadAllowed() || c.trees.isNotEmpty()),local.clips.value.isNotEmpty())});return@channelFlow}
        if(scope?.section == "results") {
            search(input,c).collect { send(it.filter { item -> scope.topic=="Todo" || ResultSections.category(item) == scope.topic }) };return@channelFlow
        }
        if(scope!=null) {
            val scoped=when(scope.section) {
                "apps"->withContext(Dispatchers.IO){appList().filter{scope.kind!="music" || it.category==android.content.pm.ApplicationInfo.CATEGORY_AUDIO || it.id.substringBefore('/') in setOf("com.spotify.music","com.google.android.apps.youtube.music")}.mapNotNull{a->val score=relevance(query,a.label);if(score==0)null else Result(a.id,a.label,if(a.id in c.favorites)"Favorita" else "Abrir aplicación","Aplicaciones",score,Action.Launch(a.intent),runCatching{context.packageManager.getActivityIcon(a.intent)}.getOrNull())}}
                "commands"->(listOfNotNull(dev.bautista.spotlight.next.IntentRouter.route(input,c.messageApp)?.let{Result("intent:local",dev.bautista.spotlight.next.IntentRouter.title(input,it),"Ver opciones y requisitos antes de ejecutar","Comandos",2600,it)})+dev.bautista.spotlight.next.NativeCatalog.results(query,context)+(if(query.isBlank())dev.bautista.spotlight.extensions.ExtensionRegistry.results("extensiones")+dev.bautista.spotlight.advanced.AdvancedCommands.results("",true)+ActionCatalog.menu("acciones")+dev.bautista.spotlight.automation.Workflows.results("rutinas",c.routines,c.favorites) else dev.bautista.spotlight.extensions.ExtensionRegistry.results(query)+dev.bautista.spotlight.advanced.AdvancedCommands.results(query)+instant(query,c)+dev.bautista.spotlight.personal.PersonalCommands.results(query,c)+dev.bautista.spotlight.automation.Workflows.results(query,c.routines,c.favorites)+ActionCatalog.menu("acciones").filter{relevance(query,it.title)>0})).distinctBy{it.id}.sortedByDescending { if(it.action is Action.NewNote) 3000 else it.score }
                "ai"->if(query.isBlank())emptyList() else listOf(Result("ai:mode","Preguntar a la IA",query.take(120),"IA",2400,Action.Ask(query.removePrefix("ia "))))
                "clipboard"->local.clips.value.filter{normalize(it).contains(normalize(query))}.mapIndexed{i,text->Result(LocalStore.clipId(text),text.take(100),if(text in local.clipFavorites.value)"Favorito temporal · copiar" else "Copiar texto capturado","Portapapeles",if(text in local.clipFavorites.value)1400-i else 1000-i,Action.Copy(text),preview=text)}
                "topic"->integrations.menu(scope.topic,ActionCatalog.packages[scope.topic]?.let{context.packageManager.getLaunchIntentForPackage(it)},ActionCatalog.packages[scope.topic])
                "files-mode"->if(query.isBlank())ActionCatalog.fileMenu(scope) else if(c.files && (index.broadAllowed() || c.trees.isNotEmpty())){index.ensure(c);index.search(query,c)}else listOf(Result("files:access","Activar búsqueda de archivos","Requiere acceso al almacenamiento","Archivos",2000,Action.Access("files")))
                "files-menu"->ActionCatalog.fileMenu(scope)
                "files"->if(c.files && (index.broadAllowed() || c.trees.isNotEmpty())){index.ensure(c);index.search(ActionCatalog.fileQuery(scope,query),c,scope.topic)} else listOf(Result("files:access","Activar búsqueda de archivos","Requiere acceso al almacenamiento en Opciones","Archivos",2000,Action.Access("files")))
                "contacts"->if(c.contacts && ContextCompat.checkSelfPermission(context,Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED){if(query.isBlank())listOf(Result("contacts:help","Escribí un nombre","Elegí entre contactos coincidentes","Contactos",2000,Action.RestoreQuery("/contacts ")))else contacts(query)}else listOf(Result("contacts:access","Permitir buscar contactos","Permiso opcional de Android","Contactos",2000,Action.Access("contacts",input)))
                "calendar"->Agenda.search(context,query,scope.kind)
                "replies"->if(!Replies.connected.value)listOf(Result("reply:access","Permitir leer notificaciones","Acceso especial de Android · opcional","Acciones",2000,Action.Access("notifications")))else Replies.results(query,ActionCatalog.packages[scope.topic] ?: "com.whatsapp")

                else->emptyList()
            }
            send(scoped);return@channelFlow
        }
        val interpreted=LocalPlan.parse(input)!=null || normalize(input) in setOf("quiero estudiar sin distracciones","explicame este pdf","explica este pdf","traduci este texto","traducir este texto","traduce este texto")
        if(interpreted) {
            send(listOf(Result("smart:plan","Revisar interpretación",if(LocalPlan.parse(input)!=null)"Plan local · comprobar permisos y confirmar cada paso" else "Elegir contexto y revisar acciones disponibles","Comandos",3000,Action.SystemPanel("interpret",input))))
            return@channelFlow
        }
        if(normalize(input) in setOf("escuchar musica","quiero escuchar musica","reproducir musica")) {
            val music=withContext(Dispatchers.IO){appList().filter { it.category==android.content.pm.ApplicationInfo.CATEGORY_AUDIO || it.id.substringBefore('/') in setOf("com.spotify.music","com.google.android.apps.youtube.music","com.sec.android.app.music","com.apple.android.music","deezer.android.app") }.map { a ->Result(a.id,a.label,"Abrir app de música instalada · la reproducción depende de la app","Aplicaciones",2400,Action.Launch(a.intent),runCatching{context.packageManager.getActivityIcon(a.intent)}.getOrNull()) }}
            val controls=Result("smart:media","Controles multimedia","Volumen y controles de la sesión activa; no inicia música automáticamente","Acciones",1800,Action.AudioPanel())
            send(ResultRanker.rank(input,music+controls,c));return@channelFlow
        }
        ActionCatalog.settings(input)?.let {send(listOf(Result("settings:natural",input.trim(),"Abrir panel de Android","Comandos",2400,Action.Launch(it))));return@channelFlow}
        // Intent suggestions stay in the same result list; no remote work while typing.
        UniversalIntent.recognize(query)?.let { intent ->
            when (intent) {
                is UniversalIntent.OpenApp -> search(intent.name,c,SearchScope("","apps")).collect { apps ->
                    val rows = apps.map { it.copy(subtitle="Acción local · abrir aplicación instalada", score=it.score+2000) }
                    val fallback = instant(intent.name,c).filter { it.id=="web" }.map { if(rows.isEmpty()) it.copy(subtitle="Aplicación no encontrada · buscar en la web") else it }
                    send(ResultRanker.rank(intent.name,rows+fallback,c))
                }
                is UniversalIntent.Photos -> {
                    if (!c.files || (!index.broadAllowed() && c.trees.isEmpty())) {
                        send(listOf(Result("smart:photos:access","Activar búsqueda de fotos","Requiere acceso a archivos o una carpeta autorizada","Archivos",2500,Action.Access("files",input))))
                    } else {
                        try {
                            index.ensure(c)
                            val rows=index.search(intent.query,c)
                            send(ResultRanker.rank(intent.query,rows,c).ifEmpty { listOf(Result("smart:photos:empty","No hay fotos coincidentes en el índice","Se buscan nombres y contenido indexado; el análisis visual es opcional","Archivos",100,Action.RestoreQuery(input))) })
                        } catch(e:CancellationException) { throw e } catch(e:Exception) {
                            send(listOf(Result("smart:photos:error","No se pudo leer el índice de fotos","Revisá el acceso a archivos y reintentá", "Archivos",100,Action.Access("files",input))))
                        }
                    }
                }
                is UniversalIntent.Information -> search(intent.query,c).collect { rows ->
                    val web=instant(intent.query,c).filter { it.id=="web" }.map { it.copy(id="smart:web",score=2000,subtitle="Buscar en la web · requiere conexión") }
                    val ai=Result("smart:ai","Consultar con IA", "Solo al seleccionar · configuración y consentimiento existentes", "IA",1500,Action.Ask(intent.query,true))
                    send(ResultRanker.ordered(ResultRanker.rank(intent.query,web+ai,c)+rows.filterNot { it.id=="web" }))
                }
                is UniversalIntent.Volume -> send(listOf(Result("smart:volume:${intent.percent}","Volumen al ${intent.percent}%","Acción local · volumen multimedia", "Acciones",2600,Action.Device("volume:${intent.percent}"))))
                is UniversalIntent.InvalidLevel -> send(listOf(Result("smart:invalid","Usá un porcentaje entre 0 y 100","El ${intent.device} no se modifica con este valor", "Información",2600,Action.RestoreQuery(input))))
            }
            return@channelFlow
        }
        dev.bautista.spotlight.next.IntentRouter.route(input,c.messageApp)?.let { action ->
            if(action is Action.Navigate && action.scope.section=="files") {
                search(action.scope.topic,c,action.scope.copy(topic="")).collect{send(it)}
            } else send(listOf(Result("intent:local",dev.bautista.spotlight.next.IntentRouter.title(input,action),when(action) {
                is Action.ComposeMessage -> "Revisar destinatario · requiere app compatible y número; contactos opcionales"
                is Action.SystemPanel -> if(action.page=="display-review") "Requiere Shizuku conectado y autorizado · confirmar cambio" else "Ver opciones y requisitos antes de ejecutar"
                else -> "Ver opciones y requisitos antes de ejecutar"
            },"Comandos",2600,action)))
            return@channelFlow
        }
        Regex("^(?:ver|consultar|mostrar) (?:detalles|permisos|componentes)(?: disponibles)?(?: de)? (.+)$").matchEntire(normalize(query))?.let { match ->
            val name=match.groupValues[1]
            val found=withContext(Dispatchers.IO){appList().filter{relevance(name,it.label)>=650}.map { a ->Result("inspector:"+a.id,a.label,"Permisos y componentes públicos","Sistema",2400,Action.SystemPanel("app",a.intent.component!!.packageName),runCatching{context.packageManager.getActivityIcon(a.intent)}.getOrNull()) }}
            send(found.ifEmpty{listOf(Result("inspector:missing","No hay una aplicación instalada con ese nombre","Buscar otra aplicación","Sistema",2400,Action.SelectMode("Apps",true)))});return@channelFlow
        }
        val advanced=dev.bautista.spotlight.advanced.AdvancedCommands.results(query)+dev.bautista.spotlight.extensions.ExtensionRegistry.results(query)
        if(advanced.isNotEmpty()){send(advanced);return@channelFlow}
        val localTools=TextTools.results(query)+SymbolTools.results(query)
        if(localTools.isNotEmpty()){send(localTools);return@channelFlow}
        val normalized=normalize(query)
        val replyMatch=Regex("^responder(?: a)? (.+)$").matchEntire(normalized)
        if(normalized in setOf("mensajes","mensajes pendientes","mostrar mensajes pendientes","notificaciones de whatsapp","notificaciones","responder el ultimo mensaje visible de whatsapp") || replyMatch!=null){
            if(!Replies.connected.value)send(listOf(Result("reply:access","Permitir leer notificaciones","Acceso especial de Android · opcional","Acciones",2400,Action.Access("notifications"))))
            else {val term=replyMatch?.groupValues?.get(1)?.takeUnless{normalized.contains("ultimo mensaje")}.orEmpty();val items=Replies.results(term,reply=replyMatch!=null);send(if(normalized.contains("ultimo mensaje"))items.take(1) else items)}
            return@channelFlow
        }
        NaturalIntents.callName(query)?.let{send(NaturalIntents.calls(context,it));return@channelFlow}
        NaturalIntents.event(query)?.let{send(listOf(Result("event:natural","Crear evento","Revisar en Calendario","Acciones",2400,Action.Launch(it))));return@channelFlow}
        ActionCatalog.message(query,c.messageApp)?.let { it ->send(listOf(Result("message:compose","Enviar mensaje a ${it.target}","Completar y elegir destinatario","Acciones",2400,it)));return@channelFlow}
        Regex("^(youtube|spotify|sofascore|google|mapas|chrome) (.+)$",RegexOption.IGNORE_CASE).matchEntire(query)?.let { match ->
            val topic=normalize(match.groupValues[1]);val term=match.groupValues[2]
            val pkg=ActionCatalog.packages[topic].orEmpty()
            val installed=pkg.isNotBlank() && context.packageManager.getLaunchIntentForPackage(pkg)!=null
            val action=ActionCatalog.searchAction(topic,pkg,term)!!
            send(listOf(Result("search:natural:$topic","Buscar en ${ActionCatalog.displayName(topic)}",term+(if(installed) " · app compatible o respaldo web" else " · búsqueda web"),if(installed) "Acciones" else "Web",2300,action)))
            return@channelFlow
        }
        val known=ActionCatalog.topic(query)
        val groups = mutableMapOf<String, List<Result>>()
        val lock = kotlinx.coroutines.sync.Mutex()
        var applicationsReady=false
        suspend fun publish(key: String, items: List<Result>) {
            lock.lock()
            try {
                groups[key] = items
                if(key=="apps")applicationsReady=true
                // Exact installed apps must be present in the first root snapshot.
                if(applicationsReady)send(groups.values.flatten())
            } finally {
                lock.unlock()
            }
        }
        if(known!=null) launch(Dispatchers.IO) { publish("integration",integrations.menu(known,ActionCatalog.packages[known]?.let{context.packageManager.getLaunchIntentForPackage(it)},ActionCatalog.packages[known])) }
        launch(Dispatchers.Default) {
            // A known integration already supplies its command menu. Avoid repeating
            // those actions from the generic command providers.
            val instantRows=if(known!=null) instant(query,c).filter { it.id=="web" } else
                instant(query,c) + dev.bautista.spotlight.next.NativeCatalog.results(query,context) + dev.bautista.spotlight.personal.PersonalCommands.results(query,c) + dev.bautista.spotlight.automation.Workflows.results(query,c.routines,c.favorites)
            publish("instant",instantRows)
        }
        launch(Dispatchers.Default) {
            val needle = normalize(query.trim().removePrefix("notas "))
            val parsed = SearchQuery.parse(query)
            publish(
                "notes",
                local.notes.value
                    .filter {
                        needle.isBlank() ||
                            normalize(it.text).contains(needle) ||
                            normalize(query) == "notas" ||
                            parsed.matches(SearchQuery.Document(it.text, "note", text = it.text))
                    }
                    .map { note ->
                        Result(
                            "note:${note.id}",
                            note.text.lineSequence().first().take(100),
                            "Nota local · editar",
                            "Notas",
                            650,
                            Action.EditNote(note.id,note.text),
                            preview = note.text,
                        )
                    },
            )
            if (c.clipboard && !c.privateMode)
                publish(
                    "clipboard",
                    local.clips.value
                        .filter {
                            query.isBlank() ||
                                parsed.matches(SearchQuery.Document(it, "clipboard", text = it))
                        }
                        .mapIndexed { i, text ->
                            Result(
                                LocalStore.clipId(text),
                                text.take(100),
                                "Portapapeles · tocar para copiar",
                                "Portapapeles",
                                (if(text in local.clipFavorites.value)750 else 500) - i,
                                Action.Copy(text),
                                preview = text,
                            )
                        },
                )
        }
        launch(Dispatchers.IO) {
            appList().firstOrNull { normalize(it.label)==normalize(query) }?.let {
                publish("app-actions", integrations.menu(it.label,it.intent,it.intent.component?.packageName))
            }
            publish(
                "apps",
                appList()
                    .mapNotNull { a ->
                        ensureActive()
                        val parsed = SearchQuery.parse(query)
                        val score =
                            if (
                                query.contains(':') ||
                                    query.contains('"') ||
                                    Regex("\\b(AND|OR|NOT)\\b").containsMatchIn(query)
                            ) {
                                if (parsed.matches(SearchQuery.Document(a.label, "app"))) 400 else 0
                            } else relevance(query, a.label)
                        if (score <= 0) null
                        else
                            Result(
                                a.id,
                                a.label,
                                if (a.id in c.favorites) "Favorita" else "Abrir aplicación",
                                "Aplicaciones",
                                score +
                                    (if (a.id in c.favorites) 120 else 0) +
                                    (20 - c.recent.indexOf(a.id)).coerceAtMost(20).let {
                                        if (a.id in c.recent) it else 0
                                    },
                                Action.Launch(a.intent),
                                runCatching { context.packageManager.getActivityIcon(a.intent) }
                                    .getOrNull(),
                            )
                    }
                    .sortedByDescending { it.score },
            )
        }
        if (c.contacts && query.length >= 2)
            launch(Dispatchers.IO) { publish("contacts", contacts(query)) }
        if (c.files && known==null)
            launch(Dispatchers.IO) {
                try {
                    index.ensure(c)
                    publish("files", index.search(query,c))
                } catch(e:CancellationException) {throw e} catch(e:Exception) {
                    publish("files",listOf(Result("files:error","No se pudo leer el índice","Reintentar búsqueda o revisar el acceso a archivos","Archivos",100,Action.RestoreQuery(input))))
                }
            }
    }.map { rows -> val allowed=rows.filter { dev.bautista.spotlight.extensions.ExtensionStore.allowed(context,it) };if(rows.isNotEmpty() && allowed.isEmpty())listOf(Result("extensions:disabled","Extensión desactivada","Activá el módulo para usar esta acción","Comandos",2400,Action.SystemPanel("extensions"))) else allowed }

    private fun instant(query: String, c: Config): List<Result> {
        val q = query.trim()
        val n = normalize(q)
        val out = mutableListOf<Result>()
        if(n in setOf("historial","historial de comandos")) out += c.actionRecent.mapIndexed { i,saved->Result("recent:$i:$saved",saved,"Cargar consulta anterior","Historial",2000-i,Action.RestoreQuery(saved)) }
        if(n in setOf("modo estudio","concentracion","sesion de concentracion")) out += Result("routine:focus","Sesión de concentración","Volumen al 20 % + preparar temporizador de 25 minutos","Acciones",2000,Action.Routine(25,20))
        if(n in setOf("portapapeles","historial del portapapeles"))out+=Result("clipboard:open","Abrir portapapeles","Historial temporal · buscar y editar","Acciones",2800,Action.SelectMode("Portapapeles",true))
        if(n in setOf("preguntar a ia","preguntar a la ia","inteligencia artificial"))out+=Result("ai:open","Preguntar a IA","Abrir consulta contextual","IA",2800,Action.SelectMode("IA",true))
        val timer=Regex("^(?:configurar (?:un )?)?temporizador(?: de)?(?: (\\d+) (?:minutos?|min))?$").matchEntire(n)
        if(timer!=null)out+=Result("timer:form","Preparar temporizador","Revisar duración · app Reloj","Acciones",2900,Action.TimerForm(timer.groupValues[1].toIntOrNull()?.coerceIn(1,1440) ?: 5))
        out += TextTools.results(q)
        out += SymbolTools.results(q)
        out += dev.bautista.spotlight.control.CommandCenter.commands(q)
        if (n.startsWith("whatsapp a ") && !c.contacts)
            out +=
                Result(
                    "contacts-permission",
                    "Activar contactos para WhatsApp",
                    "Permitir buscar por nombre en Opciones",
                    "Comandos",
                    1900,
                    Action.Launch(Intent(context, dev.bautista.spotlight.MainActivity::class.java)),
                )
        appOnline(q)?.let { lookup ->
            out +=
                Result(
                    "lookup:${lookup.type}",
                    when (lookup.type) {
                        "weather" -> "Consultar clima"
                        "currency" -> "Convertir divisas"
                        "dictionary" -> "Consultar diccionario"
                        "translate" -> "Traducir texto"
                        "flight" -> "Consultar vuelo"
                        "sports" -> "Consultar deportes"
                        else -> "Consultar Wikipedia"
                    },
                    q,
                    "Información",
                    1750,
                    lookup,
                )
        }
        if (Regex("^[\\p{L}]{3,40}$").matches(q) && appOnline(q) == null)
            out +=
                Result(
                    "concept-definition",
                    "Definición de «$q»",
                    "Consultar entrada en Wikcionario",
                    "Información",
                    120,
                    Action.Lookup("dictionary", q),
                )
        if (n in setOf("crear una nota nueva", "crear nota", "nueva nota", "nota"))
            out +=
                Result(
                    "new-note",
                    "Crear una nota nueva",
                    "Guardar localmente",
                    "Notas",
                    1800,
                    Action.NewNote(),
                )
        if (n in setOf("apagar equipo", "apagar", "reiniciar"))
            out +=
                Result(
                    "power-settings",
                    "Opciones del sistema",
                    "Android requiere usar el menú de encendido del dispositivo",
                    "Comandos",
                    1800,
                    Action.Launch(Intent(Settings.ACTION_SETTINGS)),
                    preview =
                        "Para apagar o reiniciar mantené presionado Encendido con Bajar volumen según la configuración de tu Samsung. Esta app no usa root ni privilegios de administrador.",
                )
        c.workflows.forEach { entry ->
            val parts = entry.split('|', limit = 2)
            if (parts.size == 2 && relevance(q, parts[0]) > 0)
                out +=
                    Result(
                        "workflow:${parts[0]}",
                        parts[0],
                        "Ejecutar enlace de automatización configurado",
                        "Comandos",
                        1200,
                        Action.Launch(Intent(Intent.ACTION_VIEW, Uri.parse(parts[1]))),
                    )
        }
        val commands =
            listOf(
                Triple("ajustes", "Ajustes del sistema", Settings.ACTION_SETTINGS),
                Triple("wifi", "Ajustes de Wi-Fi", Settings.ACTION_WIFI_SETTINGS),
                Triple("bluetooth", "Ajustes de Bluetooth", Settings.ACTION_BLUETOOTH_SETTINGS),
                Triple("pantalla", "Ajustes de pantalla", Settings.ACTION_DISPLAY_SETTINGS),
                Triple("sonido", "Ajustes de sonido", Settings.ACTION_SOUND_SETTINGS),
                Triple("bateria", "Ajustes de batería", Settings.ACTION_BATTERY_SAVER_SETTINGS),
                Triple("apps", "Administrar aplicaciones", Settings.ACTION_APPLICATION_SETTINGS),
                Triple("spotlight", "Configurar Spotlight", "spotlight"),
            )
        for ((key, title, action) in commands) {
            val score = maxOf(relevance(q, key), relevance(q, title))
            if (score > 0)
                out +=
                    Result(
                        "command:$key",
                        title,
                        "Comando del sistema",
                        "Comandos",
                        score,
                        Action.Launch(
                            if (action == "spotlight")
                                Intent(context, dev.bautista.spotlight.MainActivity::class.java)
                            else Intent(action)
                        ),
                    )
        }
        Calculator.evaluate(q)?.let {
            out +=
                Result("calc", it, "$q · tocar para copiar", "Calculadora", 2000, Action.Copy(it))
        }
        listOfNotNull(LocalTools.convert(q), LocalTools.clock(q), LocalTools.dateDifference(q))
            .forEachIndexed { i, answer ->
                out +=
                    Result(
                        "tool:$i",
                        answer.title,
                        answer.subtitle + " · copiar",
                        "Conversión",
                        2000,
                        Action.Copy(answer.title),
                    )
            }
        fun command(id: String, title: String, subtitle: String, intent: Intent) {
            out += Result("action:$id", title, subtitle, "Comandos", 1600, Action.Launch(intent))
        }
        if (n.startsWith("nota ") && q.length > 5)
            out +=
                Result(
                    "save-note",
                    "Guardar nota",
                    q.substring(5),
                    "Notas",
                    1800,
                    Action.SaveNote(q.substring(5)),
                )
        if (n == "evento" || n.startsWith("evento "))
            command(
                "event",
                "Crear evento",
                q.drop(7).ifBlank { "Abrir calendario" },
                Intent(Intent.ACTION_INSERT)
                    .setData(android.provider.CalendarContract.Events.CONTENT_URI)
                    .putExtra(android.provider.CalendarContract.Events.TITLE, q.drop(7)),
            )
        if (n.startsWith("email ") && q.length > 6)
            command(
                "email",
                "Redactar correo",
                q.drop(6),
                Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", q.drop(6).trim(), null)),
            )
        Regex("^(?:iniciar )?temporizador(?: de)? (\\d{1,4})(?: minutos?)?$")
            .matchEntire(n)
            ?.groupValues
            ?.get(1)
            ?.toIntOrNull()
            ?.takeIf { it in 1..1440 }
            ?.takeIf { timer==null }
            ?.let { minutes ->
                command(
                    "timer",
                    "Temporizador de $minutes minutos",
                    "Preparar en la app Reloj",
                    Intent(android.provider.AlarmClock.ACTION_SET_TIMER)
                        .putExtra(android.provider.AlarmClock.EXTRA_LENGTH, minutes * 60)
                        .putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, false),
                )
            }
        Regex("^alarma (\\d{1,2}):(\\d{2})$").matchEntire(n)?.let { m ->
            val h = m.groupValues[1].toInt()
            val minute = m.groupValues[2].toInt()
            if (h in 0..23 && minute in 0..59)
                command(
                    "alarm",
                    "Preparar alarma ${m.groupValues[1]}:${m.groupValues[2]}",
                    "Confirmar en la app Reloj",
                    Intent(android.provider.AlarmClock.ACTION_SET_ALARM)
                        .putExtra(android.provider.AlarmClock.EXTRA_HOUR, h)
                        .putExtra(android.provider.AlarmClock.EXTRA_MINUTES, minute)
                        .putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, false),
                )
        }
        val webCommands =
            mapOf(
                "mapa " to
                    ("Buscar en Mapas" to "https://www.google.com/maps/search/?api=1&query="),
                "definir " to
                    ("Consultar definición en la web" to
                        "https://www.google.com/search?q=definicion%20"),
                "traducir " to
                    ("Traducir en la web" to
                        "https://translate.google.com/?sl=auto&tl=es&op=translate&text="),
                "moneda " to
                    ("Consultar cambio actualizado en la web" to "https://www.google.com/search?q="),
            )
        webCommands.forEach { (prefix, value) ->
            if (n.startsWith(prefix) && q.length > prefix.length)
                command(
                    prefix,
                    value.first,
                    q.drop(prefix.length),
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(value.second + Uri.encode(q.drop(prefix.length))),
                    ),
                )
        }
        mapOf("mayusculas " to true, "minusculas " to false).forEach { (prefix, upper) ->
            if (n.startsWith(prefix)) {
                val text =
                    q.drop(prefix.length).let { if (upper) it.uppercase() else it.lowercase() }
                out +=
                    Result(
                        "case:$upper",
                        text,
                        "Transformar y copiar",
                        "Comandos",
                        1700,
                        Action.Copy(text),
                    )
            }
        }
        val links =
            mapOf(
                "g" to "https://www.google.com/search?q={q}",
                "yt" to "https://www.youtube.com/results?search_query={q}",
                "scholar" to "https://scholar.google.com/scholar?q={q}",
                "wikipedia" to "https://es.wikipedia.org/w/index.php?search={q}",
                "github" to "https://github.com/search?q={q}",
                "map" to "https://www.google.com/maps/search/?api=1&query={q}",
            ) +
                c.quickLinks
                    .mapNotNull { entry ->
                        val parts = entry.split('|', limit = 2)
                        if (parts.size == 2 && LocalTools.validQuickLink(parts[0], parts[1]))
                            parts[0] to parts[1]
                        else null
                    }
                    .toMap()
        links.forEach { (alias, url) ->
            if (n == alias || n.startsWith("$alias "))
                command(
                    "quick:$alias",
                    "Abrir $alias",
                    q.drop(alias.length).trim(),
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url.replace("{q}", Uri.encode(q.drop(alias.length).trim()))),
                    ),
                )
        }
        if (q.isNotEmpty()) {
            if (n.startsWith("copiar "))
                out +=
                    Result(
                        "copy",
                        q.substring(7),
                        "Copiar al portapapeles",
                        "Comandos",
                        1500,
                        Action.Copy(q.substring(7)),
                    )
            val url =
                if (q.startsWith("https://") || q.startsWith("http://")) q
                else if (Regex("^[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(/\\S*)?$").matches(q)) "https://$q"
                else null
            if (url != null)
                out +=
                    Result(
                        "url",
                        q,
                        "Abrir dirección web",
                        "Web",
                        1300,
                        Action.Launch(Intent(Intent.ACTION_VIEW, Uri.parse(url))),
                    )
            val base =
                when (c.engine) {
                    "duckduckgo" -> "https://duckduckgo.com/?q="
                    "bing" -> "https://www.bing.com/search?q="
                    else -> "https://www.google.com/search?q="
                }
            out +=
                Result(
                    "web",
                    "Buscar «$q»",
                    c.engine.replaceFirstChar { it.uppercase() },
                    "Web",
                    100,
                    Action.Launch(Intent(Intent.ACTION_VIEW, Uri.parse(base + Uri.encode(q)))),
                )
            if (n.startsWith("ia "))
                out +=
                    Result(
                        "ai",
                        "Preguntar a la IA",
                        q.substring(3),
                        "IA",
                        1800,
                        Action.Ask(q.substring(3)),
                    )
        }
        return out
    }

    private fun appOnline(query: String) = OnlineTools().request(query)

    private suspend fun contacts(query: String): List<Result> {
        if (
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED
        )
            return emptyList()
        val command =
            Regex("^(?:enviar )?whatsapp a (.+?)(?::\\s*(.*))?$", RegexOption.IGNORE_CASE)
                .matchEntire(query.trim())
        val target = command?.groupValues?.get(1) ?: query
        val uri =
            Uri.withAppendedPath(
                ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
                Uri.encode(target),
            )
        return runCatching {
                val out = mutableListOf<Result>()
                context.contentResolver
                    .query(
                        uri,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ),
                        null,
                        null,
                        null,
                    )
                    ?.use { cursor ->
                        while (cursor.moveToNext() && out.size < 30) {
                            currentCoroutineContext().ensureActive()
                            val id = cursor.getLong(0)
                            val name = cursor.getString(1) ?: "Contacto"
                            val number = cursor.getString(2) ?: continue
                            val score = if (command != null) 1700 else 600
                            if (command != null) {
                                val digits = number.filter { it.isDigit() }
                                if (
                                    number.startsWith("+") &&
                                        digits.length in 8..15 &&
                                        digits.first() != '0'
                                )
                                    out +=
                                        Result(
                                            "wa:$id:$number",
                                            "WhatsApp a $name",
                                            command.groupValues[2].ifBlank { number },
                                            "Contactos",
                                            score,
                                            Action.Launch(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(
                                                        "https://wa.me/$digits?text=" +
                                                            Uri.encode(command.groupValues[2])
                                                    ),
                                                )
                                            ),
                                        )
                                else
                                    out +=
                                        Result(
                                            "contact:$id",
                                            "$name · revisar número",
                                            "WhatsApp requiere número internacional con +",
                                            "Contactos",
                                            score,
                                            Action.Launch(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    ContentUris.withAppendedId(
                                                        ContactsContract.Contacts.CONTENT_URI,
                                                        id,
                                                    ),
                                                )
                                            ),
                                        )
                            } else {
                                out +=
                                    Result(
                                        "contact:$id",
                                        name,
                                        "Abrir contacto · $number",
                                        "Contactos",
                                        score,
                                        Action.Launch(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                ContentUris.withAppendedId(
                                                    ContactsContract.Contacts.CONTENT_URI,
                                                    id,
                                                ),
                                            )
                                        ),
                                    )
                                out +=
                                    Result(
                                        "dial:$id:$number",
                                        "Llamar a $name",
                                        number,
                                        "Contactos",
                                        score - 5,
                                        Action.Launch(
                                            Intent(
                                                Intent.ACTION_DIAL,
                                                Uri.fromParts("tel", number, null),
                                            )
                                        ),
                                    )
                                out +=
                                    Result(
                                        "sms:$id:$number",
                                        "SMS a $name",
                                        number,
                                        "Contactos",
                                        score - 10,
                                        Action.Launch(
                                            Intent(
                                                Intent.ACTION_SENDTO,
                                                Uri.fromParts("smsto", number, null),
                                            )
                                        ),
                                    )
                            }
                        }
                    }
                out.distinctBy { it.id }
            }
            .getOrElse {
                if (it is CancellationException) throw it
                emptyList()
            }
    }
}
