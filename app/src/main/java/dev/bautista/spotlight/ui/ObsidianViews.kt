package dev.bautista.spotlight.ui

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.session.MediaSessionManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.MediaControl
import dev.bautista.spotlight.control.Replies
import dev.bautista.spotlight.control.ReplyService
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.next.IntentRouter
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import dev.bautista.spotlight.search.SearchScope
import dev.bautista.spotlight.search.normalize
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect

private val Obsidian = Color(0xFF0E131A)
private val ObsidianCard = Color(0xFF181F29)
private val ObsidianBorder = Color.White.copy(alpha = .10f)
private val RaycastGreen = Color(0xFF10B981)

/** Modes are kept in Compose state so the existing overlay/search session is not recreated. */
internal enum class SpotlightView { Suggestions, SplitInspector, Dashboard }

@Composable
internal fun ObsidianViews(
    view: SpotlightView,
    query: String,
    category: String,
    config: Config,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    compact: Boolean,
) {
    val context = LocalContext.current
    val app = context.applicationContext as? SpotlightApp
    var rows by remember { mutableStateOf(emptyList<Result>()) }
    LaunchedEffect(query, config, category, app) {
        if (app == null) { rows = emptyList(); return@LaunchedEffect }
        val scope = when (category) {
            "Apps" -> SearchScope("", "apps")
            "Archivos" -> SearchScope("", "files-mode")
            "Portapapeles" -> SearchScope("", "clipboard")
            "Sistema" -> SearchScope("", "commands")
            else -> null
        }
        try {
            app.search.search(query, config, scope).collect { rows = it }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { rows = emptyList() }
    }
    Column(Modifier.fillMaxWidth().background(Obsidian.copy(alpha = .98f))) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 7.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("Todo", "Apps", "Archivos", "Portapapeles", "Sistema").forEach { filter ->
                val selected = category == filter || (filter == "Todo" && category == "Todo")
                Surface(onClick = { onCategoryChange(filter) }, shape = RoundedCornerShape(18.dp), color = if (selected) RaycastGreen.copy(alpha = .20f) else ObsidianCard, border = BorderStroke(1.dp, if (selected) RaycastGreen.copy(alpha = .6f) else ObsidianBorder)) {
                    Text(if (filter == "Archivos") "Archivos / APK" else filter, Modifier.padding(horizontal = 11.dp, vertical = 7.dp), color = if (selected) RaycastGreen else Color(0xFFB8C0CC), fontSize = 12.sp)
                }
            }
        }
        when (view) {
            SpotlightView.Suggestions -> Unit
            SpotlightView.SplitInspector -> SplitInspector(context, app, query, rows, compact, onQueryChange, onSubmit)
            SpotlightView.Dashboard -> Dashboard(context, app, config, rows, onQueryChange, onSubmit)
        }
    }
}

@Composable
private fun SurfaceCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier, shape = RoundedCornerShape(15.dp), color = ObsidianCard, border = BorderStroke(1.dp, ObsidianBorder)) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp), content = content)
    }
}

@Composable
private fun SplitInspector(context: Context, app: SpotlightApp?, query: String, rows: List<Result>, compact: Boolean, onQueryChange: (String) -> Unit, onSubmit: () -> Unit) {
    val selected = rows.firstOrNull()
    var phone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var expense by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("2") }
    Column(Modifier.fillMaxWidth().heightIn(max = 410.dp).padding(horizontal = 12.dp, vertical = 5.dp)) {
        if (compact) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SurfaceCard(Modifier.weight(1f)) { Text("Spotlight", color = RaycastGreen, fontWeight = FontWeight.SemiBold); rows.take(5).forEach { item -> Text(item.title, Modifier.fillMaxWidth().clickable { openResult(context, item) }.padding(vertical = 5.dp), maxLines = 1, overflow = TextOverflow.Ellipsis) } }
                SurfaceCard(Modifier.weight(1f)) { InspectorDetail(context, app, selected, query, onQueryChange, onSubmit, phone, { phone = it }, message, { message = it }, expense, { expense = it }, people, { people = it }) }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SurfaceCard(Modifier.weight(.95f)) {
                    Text("SPOTLIGHT", color = RaycastGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    BasicTextField(query, onQueryChange, Modifier.fillMaxWidth().background(Obsidian, RoundedCornerShape(12.dp)).padding(12.dp), singleLine = true, textStyle = TextStyle(color = Color.White, fontSize = 14.sp), cursorBrush = SolidColor(RaycastGreen), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { onSubmit() }), decorationBox = { inner -> Box { if (query.isBlank()) Text("Buscar aplicaciones y comandos…", color = Color.Gray); inner() } })
                    Column(Modifier.heightIn(max = 255.dp)) {
                        if (rows.isEmpty()) Text("Sin resultados disponibles", color = Color.Gray, fontSize = 12.sp)
                        rows.take(8).forEachIndexed { index, item ->
                            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (index == 0) Color.White.copy(alpha = .06f) else Color.Transparent).clickable { openResult(context, item) }.padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.width(3.dp).height(22.dp).background(if (index == 0) RaycastGreen else Color.Transparent, RoundedCornerShape(3.dp)))
                                Spacer(Modifier.width(9.dp)); Column(Modifier.weight(1f)) { Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(item.subtitle.ifBlank { item.category }, color = Color(0xFF9AA4B2), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                Text("☆", color = Color(0xFF9AA4B2))
                            }
                        }
                    }
                    Text("••• Spotlight     ·     Ajustes     ·     Inspector (Tab)     ·     Abrir (Enter)", color = Color(0xFF929BA8), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                SurfaceCard(Modifier.weight(1.05f)) { InspectorDetail(context, app, selected, query, onQueryChange, onSubmit, phone, { phone = it }, message, { message = it }, expense, { expense = it }, people, { people = it }) }
            }
        }
    }
}

@Composable
private fun InspectorDetail(context: Context, app: SpotlightApp?, selected: Result?, query: String, onQueryChange: (String) -> Unit, onSubmit: () -> Unit, phone: String, setPhone: (String) -> Unit, message: String, setMessage: (String) -> Unit, expense: String, setExpense: (String) -> Unit, people: String, setPeople: (String) -> Unit) {
    val pkg = (selected?.action as? Action.Launch)?.intent?.component?.packageName.orEmpty()
    val music = selected?.category == "Aplicaciones" && (pkg == "com.spotify.music" || normalize(selected.title).contains("spotify") || normalize(selected.title).contains("musica"))
    val whatsapp = normalize(selected?.title.orEmpty()).contains("whatsapp") || pkg.startsWith("com.whatsapp")
    val finance = normalize(selected?.title.orEmpty()).contains("gasto") || normalize(selected?.title.orEmpty()).contains("finanz")
    if (music) MusicInspector(context, query, onQueryChange, onSubmit)
    else if (whatsapp) {
        Text("WHATSAPP · CLICK TO CHAT", color = RaycastGreen, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(phone, setPhone, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Número internacional") })
        OutlinedTextField(message, setMessage, Modifier.fillMaxWidth(), label = { Text("Mensaje · revisar antes de abrir") })
        Button(onClick = { val digits = phone.filter(Char::isDigit); if (digits.length in 8..15) runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits?text=${Uri.encode(message)}")).setPackage("com.whatsapp").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }) { Text("Revisar en WhatsApp") }
        Text("Spotlight no envía el mensaje; confirmar el envío dentro de WhatsApp.", color = Color(0xFF9AA4B2), fontSize = 11.sp)
    } else if (finance) {
        Text("DIVIDIR GASTOS", color = RaycastGreen, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(expense, setExpense, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Importe total") })
        OutlinedTextField(people, setPeople, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Personas") })
        val share = expense.replace(',', '.').toDoubleOrNull()?.div(people.toIntOrNull()?.coerceAtLeast(1) ?: 1)
        Text(share?.let { "Por persona: ${"%.2f".format(it)}" } ?: "Ingresá un importe válido", color = Color.White)
        TextButton(onClick = { share?.let { copyText(context, "Total ${expense}: ${"%.2f".format(it)} por persona × ${people}") } }, enabled = share != null) { Text("Copiar resumen") }
    } else {
        Text(selected?.title ?: "Inspector", color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(selected?.subtitle ?: "Seleccioná un resultado para ver detalles disponibles.", color = Color(0xFF9AA4B2), fontSize = 12.sp)
        selected?.let { Text("${it.category} · datos del proveedor local", color = RaycastGreen, fontSize = 11.sp) }
    }
}

@Composable
private fun MusicInspector(context: Context, query: String, onQueryChange: (String) -> Unit, onSubmit: () -> Unit) {
    val controller = remember { runCatching { context.getSystemService(MediaSessionManager::class.java).getActiveSessions(android.content.ComponentName(context, ReplyService::class.java)).firstOrNull() }.getOrNull() }
    val metadata = controller?.metadata
    val title = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE).orEmpty()
    val artist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST).orEmpty()
    Text("INSPECTOR  ·  REPRODUCTOR", color = RaycastGreen, fontWeight = FontWeight.Bold)
    if (controller == null || title.isBlank()) Text("Sin reproducción activa", color = Color(0xFFADB5C1))
    else {
        Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(artist.ifBlank { "Artista no informado por la sesión" }, color = Color(0xFFADB5C1))
        val audio = context.getSystemService(AudioManager::class.java)
        val output = runCatching { audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS).firstOrNull()?.productName?.toString() }.getOrNull()
        Text("Salida: ${output ?: "No informada por Android"}", color = RaycastGreen, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { runCatching { MediaControl.execute(context, "previous") } }) { Text("Anterior") }
            Button(onClick = { runCatching { MediaControl.execute(context, if (controller.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING) "pause" else "play") } }) { Text(if (controller.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING) "Pausar" else "Reanudar") }
            OutlinedButton(onClick = { runCatching { MediaControl.execute(context, "next") } }) { Text("Siguiente") }
        }
    }
    OutlinedTextField(query, onQueryChange, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Buscar en Spotify") })
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { onSubmit(); val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com/search/${Uri.encode(query)}")).setPackage("com.spotify.music").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); runCatching { context.startActivity(intent) } }) { Text("Buscar / abrir Spotify") }
    }
}

@Composable
private fun Dashboard(context: Context, app: SpotlightApp?, config: Config, rows: List<Result>, onQueryChange: (String) -> Unit, onSubmit: () -> Unit) {
    val clips by (app?.local?.clips?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    val favorites = rows.filter { it.category == "Aplicaciones" }.sortedByDescending { it.score }.take(6)
    val files = rows.filter { it.category == "Archivos" }.take(4)
    val am = context.getSystemService(ActivityManager::class.java)
    val memory = remember { ActivityManager.MemoryInfo() }.also { runCatching { am.getMemoryInfo(it) } }
    val totalGb = memory.totalMem / 1024.0 / 1024 / 1024
    val usedGb = (memory.totalMem - memory.availMem) / 1024.0 / 1024 / 1024
    Column(Modifier.fillMaxWidth().heightIn(max = 390.dp).padding(horizontal = 12.dp, vertical = 6.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            SurfaceCard(Modifier.weight(1.5f)) {
                Text("APLICACIONES Y EXTENSIONES", color = RaycastGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                if (favorites.isEmpty()) Text("Escribí en el buscador para cargar aplicaciones instaladas desde PackageManager.", color = Color(0xFF9AA4B2), fontSize = 11.sp)
                favorites.chunked(2).forEach { pair -> Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { pair.forEach { item -> Text(item.title, Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Obsidian).clickable { openResult(context, item) }.padding(9.dp), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp) } } }
                Text("Favoritos y resultados de aplicaciones instaladas", color = Color(0xFF8994A2), fontSize = 10.sp)
            }
            SurfaceCard(Modifier.weight(1f)) {
                Text("CONTROL DEL DISPOSITIVO", color = RaycastGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                listOf("Wi-Fi" to Settings.ACTION_WIFI_SETTINGS, "Bluetooth" to Settings.ACTION_BLUETOOTH_SETTINGS, "No molestar" to Settings.ACTION ZEN_MODE_SETTINGS, "Pantalla" to Settings.ACTION_DISPLAY_SETTINGS).forEach { (label, action) ->
                    Text(label, Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Obsidian).clickable { launchSystem(context, action) }.padding(9.dp), color = Color.White, fontSize = 12.sp)
                }
                Text("RAM  ${"%.1f".format(usedGb)} / ${"%.1f".format(totalGb)} GB · ${100 - memory.availMem * 100 / memory.totalMem}%", color = Color(0xFFB8C0CC), fontSize = 11.sp)
                LinearProgressIndicator(progress = { (1f - memory.availMem.toFloat() / memory.totalMem.coerceAtLeast(1)).coerceIn(0f, 1f) }, color = RaycastGreen, trackColor = Color.White.copy(alpha = .10f), modifier = Modifier.fillMaxWidth())
                TextButton(onClick = { runCatching { app?.cacheDir?.listFiles()?.filter { it.isFile }?.forEach { it.delete() } }; runCatching { am.getMemoryInfo(memory) } }) { Text("Liberar caché local y actualizar RAM") }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            SurfaceCard(Modifier.weight(1f)) {
                Text("CLIPBOARD HISTORY", color = RaycastGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                if (clips.isEmpty()) Text("No hay elementos en el historial temporal de Spotlight.", color = Color(0xFF9AA4B2), fontSize = 11.sp)
                clips.take(3).forEach { clip ->
                    val sensitive = sensitiveText(clip)
                    var reveal by remember(clip) { mutableStateOf(false) }
                    Text(if (sensitive && !reveal) "••••••  ·  Confidencial" else clip.take(110), color = if (sensitive) Color(0xFFFFA2AD) else Color(0xFFCFD5DE), fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Row { TextButton(onClick = { copyText(context, clip) }) { Text("Copiar") }; if (sensitive) TextButton(onClick = { reveal = !reveal }) { Text(if (reveal) "Ocultar" else "Mostrar") } }
                }
            }
            SurfaceCard(Modifier.weight(1f)) {
                Text("ARCHIVOS RECIENTES · QUICK LOOK", color = RaycastGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                if (files.isEmpty()) Text("Los archivos aparecen al buscarlos con el índice autorizado.", color = Color(0xFF9AA4B2), fontSize = 11.sp)
                files.forEach { item -> Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(item.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp); Text(item.subtitle, color = Color(0xFF9AA4B2), maxLines = 1, fontSize = 10.sp) }; TextButton(onClick = { openResult(context, item) }) { Text("Quick Look") } } }
            }
        }
    }
}

private fun sensitiveText(value: String): Boolean = Regex("(?i)(api[_-]?key|secret|token|password|passwd|authorization|bearer|private[_-]?key|sk_live|sp_live)\\s*[:=]?\\s*\\S{8,}").containsMatchIn(value) || Regex("(?i)\\b(bearer\\s+|sk_live_|sp_live_|gh[pousr]_)[a-z0-9_\\-]{12,}").containsMatchIn(value)
private fun copyText(context: Context, value: String) { runCatching { context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Spotlight", value)) } }
private fun launchSystem(context: Context, action: String) { runCatching { context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
private fun openResult(context: Context, result: Result) { val intent = (result.action as? Action.Launch)?.intent ?: return; runCatching { context.startActivity(Intent(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
