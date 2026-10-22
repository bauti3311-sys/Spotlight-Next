package dev.bautista.spotlight

import android.Manifest
import android.app.StatusBarManager
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.lifecycleScope
import dev.bautista.spotlight.overlay.SpotlightTile
import dev.bautista.spotlight.ui.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    private val app
        get() = application as SpotlightApp

    private var transferMessage by mutableStateOf("")
    private var importSummary by mutableStateOf("")
    private var importPreview by mutableStateOf<String?>(null)
    private val configExport=registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")){uri->if(uri!=null)lifecycleScope.launch{runCatching{val raw=dev.bautista.spotlight.data.ConfigTransfer.encode(app.preferences.config.first());withContext(Dispatchers.IO){contentResolver.openOutputStream(uri,"wt")?.use{it.write(raw.toByteArray())} ?: error("No se pudo guardar.")}}.onSuccess{transferMessage="Configuración exportada."}.onFailure{transferMessage=it.message ?: "No se pudo exportar."}}}
    private val configImport=registerForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)lifecycleScope.launch{runCatching{val raw=withContext(Dispatchers.IO){contentResolver.openInputStream(uri)?.use{val out=java.io.ByteArrayOutputStream();val buffer=ByteArray(8192);while(true){val n=it.read(buffer);if(n<0)break;require(out.size()+n<=2_000_000){"Archivo demasiado grande."};out.write(buffer,0,n)};out.toString("UTF-8")} ?: error("No se pudo leer.")};val current=app.preferences.config.first();importSummary=withContext(Dispatchers.Default){dev.bautista.spotlight.data.ConfigTransfer.summary(dev.bautista.spotlight.data.ConfigTransfer.decode(raw,current))};importPreview=raw}.onFailure{transferMessage=it.message ?: "No se pudo importar."}}}
    private var permissionRevision by mutableIntStateOf(0)
    private var requestedSettingsGroup by mutableStateOf<String?>(null)
    private var broadGranted by mutableStateOf(false)
    private var overlayGranted by mutableStateOf(false)
    private var audioUri by mutableStateOf<Uri?>(null)
    private val audioPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                runCatching {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
                audioUri = uri
            }
        }
    private val contactPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            lifecycleScope.launch {
                val c = app.preferences.config.first()
                app.preferences.update(c.copy(contacts = granted))
            }
        }
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    private val folder =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null)
                runCatching {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION,
                        )
                        lifecycleScope.launch { app.preferences.addTree(uri.toString()) }
                    }
                    .onFailure {
                        Toast.makeText(
                                this,
                                "No se pudo conservar el acceso a esa carpeta",
                                Toast.LENGTH_LONG,
                            )
                            .show()
                    }
        }

    override fun onResume() {
        super.onResume()
        permissionRevision++
        broadGranted = app.index.broadAllowed()
        overlayGranted = Settings.canDrawOverlays(this)
        app.search.invalidateApps()
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedSettingsGroup=intent.getStringExtra("group")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Public wallpaper-backed activity. No screen capture or wallpaper read permission.
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setFormat(android.graphics.PixelFormat.TRANSLUCENT)
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        if(Build.VERSION.SDK_INT>=34)window.attributes=window.attributes.apply{setWallpaperTouchEventsEnabled(false)}
        setContent {
            val loadedConfig by app.preferences.config.collectAsState(initial=null)
            if(loadedConfig==null) {
                SpotlightTheme { Surface(Modifier.fillMaxSize()){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("Cargando preferencias…")}} }
                return@setContent
            }
            val config=requireNotNull(loadedConfig)
            val scope = rememberCoroutineScope()
            var endpoint by remember(config.endpoint) { mutableStateOf(config.endpoint) }
            var settingsGroup by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(SettingsGroups.canonical(intent.getStringExtra("group"))) }
            LaunchedEffect(requestedSettingsGroup) { requestedSettingsGroup?.let { settingsGroup=SettingsGroups.canonical(it);requestedSettingsGroup=null } }
            val notificationAccess by dev.bautista.spotlight.control.Replies.connected.collectAsState()
            var endpointError by remember { mutableStateOf("") }
            var showShizukuAccess by remember { mutableStateOf(false) }
            fun update(c: dev.bautista.spotlight.data.Config) {
                scope.launch { app.preferences.update(c) }
            }
            fun updateAi(c: dev.bautista.spotlight.data.Config) {
                scope.launch { app.preferences.updateAi(config,c) }
            }
            // One route for all Android access requests. Never pretend a special
            // permission was granted by toggling local Compose state.
            fun configureAccess(access:String) {
                when(access) {
                    "overlay"->startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName")))
                    "files"->folder.launch(null)
                    "storage_broad"->if(Build.VERSION.SDK_INT>=30) {
                        runCatching{startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,Uri.parse("package:$packageName")))}
                            .onFailure{startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))}
                    } else settingsGroup="Búsqueda y archivos"
                    "contacts","calendar","location"->startActivity(Intent(this@MainActivity,PermissionActivity::class.java).putExtra("access",access).putExtra("reopen",false))
                    "notifications"->startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    "post_notifications"->if(Build.VERSION.SDK_INT>=33)notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    "usage"->startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    "keyboard","phone_use"->startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    "shizuku"->{settingsGroup="Permisos y accesos";showShizukuAccess=true}
                }
            }
            SpotlightTheme(config.theme, config) {
                val colors = MaterialTheme.colorScheme
                Surface(Modifier.fillMaxSize(), color = androidx.compose.ui.graphics.Color.Transparent) {
                    Column(
                        Modifier.fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = if(androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp>=760)32.dp else 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Column(Modifier.widthIn(max = 960.dp).fillMaxSize().glassSurface(config.copy(opacity=GlassTokens.settingsOpacity(config)))) {
                            SettingsLayout(settingsGroup,{settingsGroup=it},Modifier.weight(1f),onInvoke={
                                if(overlayGranted)startActivity(Intent(this@MainActivity,InvokeActivity::class.java))
                                else startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName")))
                            },overview={details->
                                SettingsHome(app,config,permissionRevision,{settingsGroup=it},{access->configureAccess(access)},details)
                            }){
                            CompositionLocalProvider(LocalSettingsGroup provides settingsGroup){
                            Section("Contexto autorizado · Inteligencia") { Text("Elegí documentos, fijá contextos y guardá conversaciones manualmente.");TextButton(onClick={startActivity(Intent(this@MainActivity,InvokeActivity::class.java).putExtra("query","contextos"))}){Text("Gestionar contexto")} }
                            Section("Permisos y accesos") {
                                SettingsAccessSummary(app,config,permissionRevision){access->configureAccess(access)}
                                if(config.trees.isNotEmpty()) ConfirmedSettingsButton(
                                    "Revocar acceso a carpetas",
                                    "Se revocarán las carpetas autorizadas en Spotlight. No se borrarán archivos."
                                ) {
                                    config.trees.forEach { tree ->
                                        runCatching { contentResolver.releasePersistableUriPermission(
                                            Uri.parse(tree),Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        ) }
                                    }
                                    update(config.copy(trees=emptySet()))
                                }
                                if(showShizukuAccess) {
                                    TextButton(onClick={showShizukuAccess=false}){Text("Ocultar configuración de Shizuku")}
                                    dev.bautista.spotlight.ui.AdvancedPanel(app,"shizuku")
                                }
                                Toggle("Buscar contactos dentro de Spotlight",config.contacts){enabled->
                                    if(!enabled)update(config.copy(contacts=false))
                                    else if(checkSelfPermission(Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED)
                                        update(config.copy(contacts=true))
                                    else contactPermission.launch(Manifest.permission.READ_CONTACTS)
                                }
                            }
                            Section("Privacidad · Spotlight Glass") { PrivacySettings(app,config,::update) }
                            Section("Invocación · teclado y posición") { InvocationSettings(config,::update) }
                            Section("Búsqueda · prioridades") { SearchTuning(config,::update) }
                            Section("Diagnóstico de Spotlight") { DiagnosticsPanel(app) }

                            if(!config.onboarded)Section("Bienvenido a Spotlight Next"){Text("Abrí la barra desde ajustes rápidos o un acceso en Inicio. Escribí una app, una cuenta o un comando. Tocá ⋯ o Ctrl+K para ver acciones; Tab abre y recorre acciones. Los permisos y la IA son opcionales.");TextButton(onClick={update(config.copy(onboarded=true))}){Text("Entendido")}}
                            Section("Centro de comandos y perfiles") { Text("Packs listos para usar, perfiles manuales y acciones revisables desde la barra.");OutlinedButton(onClick={startActivity(Intent(this@MainActivity,InvokeActivity::class.java).putExtra("query","catálogo"))}){Text("Abrir centro de comandos")} }
                            Section("Tasker · automatización externa") { Text("Descubrí tareas sin copiar enlaces. Tasker necesita su permiso de acceso externo y una confirmación antes de ejecutar.");OutlinedButton(onClick={startActivity(Intent(this@MainActivity,InvokeActivity::class.java).putExtra("query","tasker"))}){Text("Abrir tareas de Tasker")} }
                            Section("Clima y agenda deportiva") { Text("Clima de Open-Meteo y agenda gratuita de TheSportsDB, consultados directamente desde la barra.");Row{OutlinedButton(onClick={startActivity(Intent(this@MainActivity,InvokeActivity::class.java).putExtra("query","clima"))}){Text("Clima")};TextButton(onClick={startActivity(Intent(this@MainActivity,InvokeActivity::class.java).putExtra("query","deportes"))}){Text("Deportes")}} }
                            Section("Extensiones e integraciones") { dev.bautista.spotlight.ui.ExtensionPanel(app,config) }
                            Section("Integraciones · capacidades disponibles") { CapabilitySettings(app,config,permissionRevision) }
                            Section("Transferir configuración") {
                                Text("Preferencias, favoritos y alias. Los datos heredados se conservan; las claves, historiales, permisos y accesos a archivos permanecen en cada dispositivo.")
                                Row{OutlinedButton(onClick={configExport.launch("Spotlight-configuracion.json")}){Text("Exportar")};TextButton(onClick={configImport.launch(arrayOf("application/json","text/plain"))}){Text("Importar")}}
                                ConfirmedSettingsButton("Descartar sesión recuperable guardada","Elimina solo la recuperación del overlay. No elimina tus contextos, conversaciones guardadas ni preferencias; la tarea abierta se conserva.") {
                                    scope.launch {runCatching{app.contexts.discardOverlaySnapshot()}.onSuccess{transferMessage="Sesión recuperable descartada."}.onFailure{transferMessage=it.message ?: "No se pudo descartar la sesión."}}
                                }
                                if(transferMessage.isNotBlank())Text(transferMessage)
                                importPreview?.let{raw->run{Text("Revisar importación");Text(importSummary);Row{Button(onClick={scope.launch{runCatching{app.preferences.importPortable(raw)}.onSuccess{transferMessage="Configuración importada. Revisá las apps disponibles en este dispositivo.";importPreview=null}.onFailure{transferMessage=it.message.orEmpty()}}}){Text("Confirmar importación")};TextButton(onClick={importPreview=null}){Text("Cancelar")}}}}
                            }
                            Section("General · inicio") {
                                Toggle("Mostrar sugerencias",config.suggestions){update(config.copy(suggestions=it))}
                                Text("Sugerencias locales sin relleno: como máximo una app relevante, acciones y continuidad autorizada.",fontSize=12.sp,color=colors.onSurfaceVariant)
                                ConfirmedSettingsButton("Restablecer sugerencias","Se quitarán las sugerencias fijadas y ocultas. Tus favoritos y guardados se conservan."){update(config.copy(hiddenSuggestions=emptySet(),pinnedSuggestions=emptySet()))}
                            }
                            Section("WhatsApp y notificaciones") {
                                Text(if(notificationAccess)"Acceso a notificaciones activo. Los mensajes visibles permanecen solo en memoria." else "Acceso a notificaciones desactivado. Podés habilitarlo para leer y responder mensajes disponibles.")
                                TextButton(onClick={settingsGroup="Permisos y accesos"}){Text("Gestionar contactos y notificaciones →")}
                                Text("Android separa contactos, calendario, archivos y notificaciones. Las respuestas directas funcionan con mensajes que tengan una notificación compatible; una conversación nueva se prepara y se envía desde WhatsApp.",fontSize=12.sp,color=colors.onSurfaceVariant)
                            }
                            Section("Acceso rápido") {
                                Text(
                                    "Agregá Spotlight al panel de ajustes rápidos o creá un acceso en Inicio. La ventana aparece sobre otras apps compatibles.",
                                    fontSize = 14.sp,
                                    color = colors.onSurfaceVariant,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { addTile() }) {
                                        Text("Ajustes rápidos")
                                    }
                                    OutlinedButton(onClick = { pinShortcut() }) {
                                        Text("Acceso en Inicio")
                                    }
                                }
                                if (Build.VERSION.SDK_INT >= 33)
                                    TextButton(onClick={settingsGroup="Permisos y accesos"}) {Text("Configurar avisos de Spotlight →")}
                                Text(
                                    "El atajo global requiere teclado físico y activar el servicio optativo de Accesibilidad. Su disponibilidad depende de One UI y de otros servicios de accesibilidad.",
                                    fontSize = 12.sp,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                            Section("Teclado físico · acceso global optativo") {
                                KeyboardShortcutEditor(config,::update)
                                GlobalKeyboardShortcutOptions(config,::update)
                                Text(
                                    "Para reconocer el atajo, Android entrega eventos de teclas al servicio. Spotlight no guarda lo escrito, no recibe eventos de contenido y no lee pantallas. El servicio solo se usa si lo activás manualmente.",
                                    fontSize = 12.sp,
                                    color = colors.onSurfaceVariant,
                                )
                                OutlinedButton(onClick={settingsGroup="Permisos y accesos"}) {
                                    Text("Configurar servicio de teclado en Permisos →")
                                }
                            }
                            Section("Apariencia") { GlassAppearance(config,update={draft->scope.launch{app.preferences.updateAppearance(draft)}},showHeading=false) }
                            Section("Aprendizaje local") {
                                Toggle("Aprender del uso · solo en este dispositivo",config.learning){update(config.copy(learning=it))}
                            }
                            Section("Portapapeles temporal") {
                                Toggle("Historial de portapapeles · optativo", config.clipboard) {
                                    if (!it) app.local.clearClips()
                                    update(config.copy(clipboard = it))
                                }
                                Text(
                                    "Solo captura texto mientras Spotlight tiene el foco. Historial limitado en memoria con caducidad; se pierde al cerrar el proceso. Se omiten elementos marcados como sensibles.",
                                    fontSize = 12.sp,
                                    color = colors.onSurfaceVariant,
                                )
                                Text("Límite · ${config.clipLimit} elementos");GlassSlider(config.clipLimit.toFloat(),{update(config.copy(clipLimit=it.toInt()))},valueRange=5f..50f)
                                Text("Retención · ${config.clipRetention} minutos");GlassSlider(config.clipRetention.toFloat(),{update(config.copy(clipRetention=it.toInt()))},valueRange=1f..60f)
                                ConfirmedSettingsButton("Vaciar portapapeles de Spotlight","Se borrará únicamente el historial temporal de Spotlight."){app.local.clearClips()}
                            }
                            Section("Búsqueda y archivos") {
                                Toggle("Buscar archivos", config.files) {
                                    update(config.copy(files = it))
                                }
                                Text(
                                    if (broadGranted) "Acceso a archivos compartidos: activado"
                                    else "Acceso amplio: pendiente de permiso",
                                    fontSize = 12.sp,
                                )
                                Text("El acceso a carpetas, almacenamiento ampliado y uso de apps se gestiona en Permisos y accesos.",fontSize=12.sp,color=colors.onSurfaceVariant)
                                TextButton(onClick={settingsGroup="Permisos y accesos"}){Text("Abrir permisos de archivos y actividad →")}
                                TextButton(onClick={settingsGroup="Permisos y accesos"}) {
                                    Text("Gestionar acceso a carpetas →")
                                }
                                Text(
                                    "${config.trees.size} carpetas autorizadas. Índice de hasta 200.000 entradas y 64 niveles; se avisa si queda parcial.",
                                    fontSize = 12.sp,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                            Section("Búsqueda web") {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("google", "duckduckgo", "bing").forEach { engine ->
                                        FilterChip(
                                            config.engine == engine,
                                            { update(config.copy(engine = engine)) },
                                            label = {
                                                Text(engine.replaceFirstChar { it.uppercase() })
                                            },
                                        )
                                    }
                                }

                            }
                            Section("Texto e imágenes de otras apps") {
                                Text(
                                    "Seleccioná texto en una app compatible y elegí Spotlight en su menú. Podés transformarlo, copiarlo, guardar una nota o buscarlo. En editores que aceptan ACTION_PROCESS_TEXT podés devolver el texto. Para texto de una imagen de la web, usá Compartir → Spotlight → Reconocer texto; el OCR es local.",
                                    fontSize = 12.sp,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                            Section("Índice y reconocimiento local") {
                                val status by app.index.status.collectAsState()
                                var indexJob by remember { mutableStateOf<Job?>(null) }
                                Toggle(
                                    "Contenido de texto, PDF y correos .eml",
                                    config.contentIndex,
                                ) {
                                    update(config.copy(contentIndex = it))
                                }
                                Toggle(
                                    "Texto de imágenes y etiquetas visuales",
                                    config.visualIndex,
                                ) {
                                    update(config.copy(visualIndex = it))
                                }
                                Toggle(
                                    "Momentos de videos · análisis por muestras",
                                    config.videoIndex,
                                ) {
                                    update(config.copy(videoIndex = it))
                                }
                                Text(
                                    "Procesa almacenamiento compartido con acceso amplio o las carpetas autorizadas. PDF: hasta 40 páginas y 8 MB; texto: 100 KB. OCR/etiquetas locales: hasta 150 imágenes o videos por ejecución. Videos: una muestra cada 15 segundos de los primeros 5 minutos. Las etiquetas son aproximadas y no reconocen todos los objetos o escenas.",
                                    fontSize = 12.sp,
                                    color = colors.onSurfaceVariant,
                                )
                                Text(status.message, fontSize = 12.sp)
                                if (status.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                                Row {
                                    TextButton(
                                        onClick = {
                                            indexJob =
                                                scope.launch { app.index.refresh(config, true) }
                                        },
                                        enabled = !status.busy,
                                    ) {
                                        Text("Actualizar índice")
                                    }
                                    TextButton(
                                        onClick = {
                                            indexJob?.cancel()
                                            app.index.cancelRefresh()
                                        },
                                        enabled = status.busy,
                                    ) {
                                        Text("Cancelar")
                                    }
                                    ConfirmedSettingsButton("Borrar índice","Elimina el índice local, no tus archivos. Podés reconstruirlo con los accesos autorizados.",enabled=!status.busy){scope.launch{app.index.clear()}}
                                }
                                Text(
                                    "Usá kind:pdf, kind:image, kind:folders, kind:email, author:Nombre, modified:2026-10, created:2026 o frases entre comillas. AND / OR / NOT deben estar en mayúsculas. Autor y creación solo se filtran si el documento tiene esos datos. Los cambios de nombre/metadatos se revisan al volver a buscar; para reindexar contenido tocá Actualizar índice.",
                                    fontSize = 12.sp,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                            Section("Búsqueda por significado · local") { SemanticSettings(app,config) }
                            Section("Audio · transcripción optativa") {
                                var busy by remember { mutableStateOf(false) }
                                var transcriptError by remember { mutableStateOf("") }
                                var audioJob by remember { mutableStateOf<Job?>(null) }
                                Text(
                                    "Elegí un audio MP3, M4A, WAV, OGG o WebM de hasta 20 MB. Al confirmar se envía únicamente ese archivo al proveedor de IA elegido. La transcripción queda en el índice local. OpenAI: hasta 20 MB; Gemini: hasta 12 MB. Puede consumir cuota del proveedor.",
                                    fontSize = 12.sp,
                                    color = colors.onSurfaceVariant,
                                )
                                OutlinedButton(
                                    onClick = { audioPicker.launch(arrayOf("audio/*")) },
                                    enabled = !busy,
                                ) {
                                    Text("Elegir audio")
                                }
                                if (transcriptError.isNotEmpty())
                                    Text(transcriptError, color = colors.error, fontSize = 12.sp)
                                if (busy) {
                                    LinearProgressIndicator(Modifier.fillMaxWidth())
                                    TextButton(onClick = { audioJob?.cancel() }) {
                                        Text("Cancelar transcripción")
                                    }
                                }
                                audioUri?.let { uri ->
                                    AlertDialog(
                                        onDismissRequest = { audioUri = null },
                                        title = { Text("Enviar audio al backend") },
                                        text = {
                                            Text(
                                                "Este archivo saldrá del dispositivo para ser transcrito por tu backend y su proveedor. No se enviará ningún otro archivo. ¿Continuar?"
                                            )
                                        },
                                        confirmButton = {
                                            TextButton(
                                                onClick = {
                                                    audioUri = null
                                                    busy = true
                                                    transcriptError = ""
                                                    audioJob =
                                                        scope.launch {
                                                            try {
                                                                val name =
                                                                    contentResolver
                                                                        .query(
                                                                            uri,
                                                                            arrayOf(
                                                                                android.provider
                                                                                    .OpenableColumns
                                                                                    .DISPLAY_NAME
                                                                            ),
                                                                            null,
                                                                            null,
                                                                            null,
                                                                        )
                                                                        ?.use {
                                                                            if (it.moveToFirst())
                                                                                it.getString(0)
                                                                            else "audio.m4a"
                                                                        } ?: "audio.m4a"
                                                                val text =
                                                                    dev.bautista.spotlight.ai
                                                                        .AudioClient()
                                                                        .transcribe(
                                                                            this@MainActivity,
                                                                            uri,
                                                                            config,
                                                                            name,
                                                                        )
                                                                app.index.storeTranscript(
                                                                    uri.toString(),
                                                                    name,
                                                                    contentResolver.getType(uri)
                                                                        ?: "audio/mpeg",
                                                                    text,
                                                                )
                                                                Toast.makeText(
                                                                        this@MainActivity,
                                                                        "Transcripción indexada",
                                                                        Toast.LENGTH_LONG,
                                                                    )
                                                                    .show()
                                                            } catch (e: CancellationException) {
                                                                throw e
                                                            } catch (e: Exception) {
                                                                transcriptError =
                                                                    e.message
                                                                        ?: "No se pudo transcribir"
                                                            } finally {
                                                                busy = false
                                                            }
                                                        }
                                                },
                                                enabled = dev.bautista.spotlight.ai.DirectAi(this@MainActivity).configured(config),
                                            ) {
                                                Text("Enviar y transcribir")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { audioUri = null }) {
                                                Text("Cancelar")
                                            }
                                        },
                                    )
                                }
                            }
                            Section("Tarjetas de información") {
                                Toggle(
                                    "Consultar al escribir clima, monedas y diccionario",
                                    config.online,
                                ) {
                                    update(config.copy(online = it))
                                }
                                Text(
                                    "Optativo: envía únicamente la consulta explícita a Open-Meteo, Frankfurter, Wikipedia o Wikcionario. También podés consultar manualmente tocando el resultado. Frankfurter ofrece referencias diarias, no cambio blue ni precios bancarios. Open-Meteo: CC BY 4.0; Wikimedia: CC BY-SA. Traducción local ML Kit: la primera vez descarga modelos mediante Wi-Fi; usá traducir en a es: hello.",
                                    fontSize = 12.sp,
                                    color = colors.onSurfaceVariant,
                                )
                                var liveEndpoint by
                                    remember(config.liveEndpoint) {
                                        mutableStateOf(config.liveEndpoint)
                                    }
                                OutlinedTextField(
                                    liveEndpoint,
                                    { liveEndpoint = it },
                                    Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("Backend HTTPS de vuelos/deportes · /live") },
                                )
                                TextButton(
                                    onClick = {
                                        val parsed = Uri.parse(liveEndpoint)
                                        if (
                                            liveEndpoint.isBlank() ||
                                                (parsed.scheme == "https" &&
                                                    !parsed.host.isNullOrBlank() &&
                                                    parsed.userInfo == null)
                                        )
                                            update(config.copy(liveEndpoint = liveEndpoint.trim()))
                                        else
                                            Toast.makeText(
                                                    this@MainActivity,
                                                    "Usá HTTPS sin credenciales en la URL",
                                                    Toast.LENGTH_SHORT,
                                                )
                                                .show()
                                    }
                                ) {
                                    Text("Guardar datos en vivo")
                                }
                                Text(
                                    "El proyecto incluye el conector de backend. Sin claves/configuración de proveedor no se muestran vuelos o marcadores en vivo. Las claves quedan en el servidor; no en la APK.",
                                    fontSize = 12.sp,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                            Section("Alias personales") { AliasSettings(config, ::update) }
                            Section("Integraciones automáticas") { dev.bautista.spotlight.ui.IntegrationSettings(config, ::update) }
                            Section("Herramientas incluidas") {
                                Text(
                                    "Probá: 10 km a mi · 32 f a c · hora en Tokio · días entre 2026-10-01 y 2026-10-09 · nota comprar café · notas · alarma 08:30 · temporizador 5 · evento reunión · email alguien@ejemplo.com · mapa café · definir palabra · traducir texto · mayúsculas texto · moneda 100 USD a ARS.",
                                    fontSize = 14.sp,
                                )
                                Text(
                                    "Conversiones, reloj y notas funcionan sin internet. Clima, diccionario, Wikipedia y divisas tienen tarjetas con datos externos. Traducciones usan modelos locales descargables. Los eventos, correos y alarmas se preparan para confirmar en su app. No se leen mensajes o correos privados de otras apps.",
                                    fontSize = 12.sp,
                                    color = colors.onSurfaceVariant,
                                )
                                Text(
                                    "Dentro de Spotlight: Ctrl/⌘+1…4 abre categorías. Flechas navegan al salir de la edición; Espacio abre vista rápida; Tab abre y recorre acciones; Ctrl/⌘+K abre acciones; Ctrl/⌘+C copia; Ctrl/⌘+R muestra la carpeta; Ctrl/⌘+B busca en la web; Enter abre; Escape cierra. El espacio se conserva al escribir hasta navegar con flechas.",
                                    fontSize = 12.sp,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                            Section("Rutinas y automatizaciones") {WorkflowSettings(config, ::update)}
                            Section("Inteligencia artificial · opcional") {
                                AiSettings(config, ::updateAi)
                                if(config.aiProvider=="backend") {
                                Text(
                                    "Escribí «ia tu pregunta» en Spotlight. Las respuestas aparecen en la ventana y la conversación vive solo hasta cerrarla.",
                                    fontSize = 14.sp,
                                    color = colors.onSurfaceVariant,
                                )
                                OutlinedTextField(
                                    endpoint,
                                    {
                                        endpoint = it
                                        endpointError = ""
                                    },
                                    Modifier.fillMaxWidth(),
                                    label = { Text("URL de tu backend HTTPS") },
                                    singleLine = true,
                                    isError = endpointError.isNotEmpty(),
                                )
                                if (endpointError.isNotEmpty())
                                    Text(endpointError, color = colors.error, fontSize = 12.sp)
                                TextButton(
                                    onClick = {
                                        if (
                                            endpoint.isBlank() ||
                                                runCatching {
                                                        val u = Uri.parse(endpoint)
                                                        u.scheme == "https" &&
                                                            !u.host.isNullOrBlank() &&
                                                            u.userInfo == null
                                                    }
                                                    .getOrDefault(false)
                                        )
                                            updateAi(config.copy(endpoint = endpoint.trim()))
                                        else
                                            endpointError =
                                                "Usá una URL HTTPS sin credenciales en la dirección."
                                    }
                                ) {
                                    Text("Guardar backend")
                                }
                                Text(
                                    "No incluyas claves de OpenAI en la URL. El proyecto incluye un backend de referencia para desplegar y proteger. ChatGPT Plus no incluye créditos de API.",
                                    fontSize = 12.sp,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                            }
                            }
                            }
                            Spacer(Modifier.height(16.dp))
                            val installedVersion=remember { runCatching { packageManager.getPackageInfo(packageName,0).versionName }.getOrNull().orEmpty() }
                            Text(
                                "Spotlight Next $installedVersion",
                                fontSize = 12.sp,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun pinShortcut() {
        val manager = getSystemService(ShortcutManager::class.java)
        if (manager.isRequestPinShortcutSupported)
            manager.requestPinShortcut(
                ShortcutInfo.Builder(this, "invoke")
                    .setShortLabel("Spotlight Next")
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_spotlight))
                    .setIntent(
                        Intent(this, InvokeActivity::class.java).setAction(Intent.ACTION_VIEW)
                    )
                    .build(),
                null,
            )
        else Toast.makeText(this, "Tu launcher no permite fijar accesos", Toast.LENGTH_LONG).show()
    }

    private fun addTile() {
        if (Build.VERSION.SDK_INT >= 33)
            getSystemService(StatusBarManager::class.java).requestAddTileService(
                ComponentName(this, SpotlightTile::class.java),
                "Spotlight Next",
                Icon.createWithResource(this, R.drawable.ic_spotlight),
                mainExecutor,
            ) {}
        else
            Toast.makeText(
                    this,
                    "Editá el panel de ajustes rápidos y agregá Spotlight",
                    Toast.LENGTH_LONG,
                )
                .show()
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    SettingsPreferenceSection(title,content)
}

@Composable
private fun Toggle(title: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min=48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium)
        Switch(value, onChange)
    }
}
