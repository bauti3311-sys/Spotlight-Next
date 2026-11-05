package dev.bautista.spotlight.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.MainActivity
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.integrations.spotify.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable fun SpotifySettings(app: SpotlightApp) {
    val connection = app.spotify
    val state by connection.state.collectAsState()
    val scope = rememberCoroutineScope()
    var clientId by rememberSaveable { mutableStateOf(connection.clientId) }
    var message by remember { mutableStateOf("") }
    var job by remember { mutableStateOf<Job?>(null) }
    var busy by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { connection.inspect() }
    Text("Conectá tu cuenta para buscar y controlar Spotify desde el inspector. El audio lo reproduce Spotify.")
    Text(when(state) {
        SpotifyConnectionState.CONNECTING -> "Conectando · esperando tu autorización"
        SpotifyConnectionState.AUTHORIZED -> "Autorizado · cuenta conectada"
        SpotifyConnectionState.ERROR -> "No se pudo completar la conexión"
        else -> "Desconectado"
    }, modifier = Modifier.testTag("spotify-connection-state"))
    OutlinedTextField(clientId, { clientId = it.take(32) }, singleLine = true,
        enabled = !busy, label = { Text("Client ID de Spotify") },
        modifier = Modifier.fillMaxWidth().testTag("spotify-client-id"))
    Text("No ingreses el Client Secret. Se guardan tokens cifrados en este dispositivo; no se incluyen en respaldos.", style = MaterialTheme.typography.bodySmall)
    Text("En Spotify Dashboard → tu app → Settings → Redirect URIs, registrá exactamente:\nhttp://127.0.0.1/spotify/callback\nGuardá los cambios. Spotlight agrega un puerto temporal, admitido por Spotify para retornos locales.", style = MaterialTheme.typography.bodySmall)
    Text("La búsqueda usa la autorización de tu cuenta. La reproducción local usa el cliente Spotify instalado en segundo plano; no exige activar previamente un reproductor Web API. Las canciones a demanda dependen de las capacidades de tu cuenta.", style = MaterialTheme.typography.bodySmall)
    fun action(block: suspend () -> Unit) {
        if (busy) return
        message = ""; busy = true
        job = scope.launch {
            try { block() } catch (e: CancellationException) { throw e }
            catch (e: Exception) { message = e.message ?: "No se pudo completar la operación." }
            finally { busy = false }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(enabled = !busy && clientId.trim().matches(Regex("[A-Za-z0-9]{32}")),
            onClick = { action { connection.configure(clientId); message = "Client ID guardado. Podés conectar." } }) { Text("Guardar ID") }
        Button(enabled = !busy && connection.clientId.isNotBlank() && connection.clientId==clientId.trim(),
            onClick = { action { connection.connect(); message = "Autorización recibida de Spotify." } }) { Text("Conectar Spotify") }
    }
    OutlinedButton(enabled=!busy && connection.clientId==clientId.trim() && clientId.trim().length==32,
        onClick={action{connection.authorizeLocalPlayback();message="Reproducción local autorizada. Ya podés confirmar una canción desde Spotlight."}}) {
        Text("Autorizar reproducción local")
    }
    Text("Para App Remote, registrá este paquete Android en Spotify Dashboard: "+app.packageName+
        ". La autorización inicial puede mostrar Spotify; después el SDK controla su servicio en segundo plano.",
        style=MaterialTheme.typography.bodySmall)
    val fingerprint=remember(app){spotifySigningFingerprint(app)}
    androidx.compose.foundation.text.selection.SelectionContainer {
        Text("Paquete: "+app.packageName+ "\nSHA-1: "+(fingerprint ?: "No disponible"),
            style=MaterialTheme.typography.bodySmall)
    }
    if(busy && state!=SpotifyConnectionState.CONNECTING) TextButton(onClick={job?.cancel()}) { Text("Cancelar solicitud") }
    if (state == SpotifyConnectionState.CONNECTING) TextButton(onClick = { job?.cancel() }) { Text("Cancelar conexión") }
    if (state != SpotifyConnectionState.CONNECTING) TextButton(onClick = { action { connection.disconnect(); message = "Tokens eliminados de este dispositivo. También podés revocar el acceso en tu cuenta de Spotify." } }) { Text("Desconectar y eliminar tokens") }
    if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodySmall)
}

/** Persist an API string: the overlay's durable codec deliberately rejects enum objects. */
@Composable internal fun rememberSpotifyFilterType(): MutableState<String> =
    rememberSaveable { mutableStateOf(SpotifyItemType.TRACK.api) }

/** Explicit remote queries only; selecting a result never starts playback. */
@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun SpotifySearchPanel(app: SpotlightApp, initialQuery: String = "", externalQuery:String?=null, searchTrigger:Int=0) {
    val connection = app.spotify
    val state by connection.state.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var query by rememberSaveable(initialQuery) { mutableStateOf(initialQuery.take(240)) }
    var typeKey by rememberSpotifyFilterType()
    val type = SpotifyItemType.entries.firstOrNull { it.api == typeKey } ?: SpotifyItemType.TRACK
    var results by remember { mutableStateOf<List<SpotifySearchItem>>(emptyList()) }
    var selected by remember { mutableStateOf<SpotifySearchItem?>(null) }
    var message by remember { mutableStateOf("") }
    var job by remember { mutableStateOf<Job?>(null) }
    var busy by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { connection.inspect() }
    LaunchedEffect(state) { if (state != SpotifyConnectionState.AUTHORIZED) { job?.cancel(); results = emptyList(); selected = null } }
    fun action(block: suspend () -> Unit) {
        if (busy) return
        message = ""; busy = true
        job = scope.launch {
            try { block() } catch (e: CancellationException) { throw e }
            catch (e: Exception) { message = e.message ?: "Spotify no pudo completar la operación." }
            finally { busy = false }
        }
    }
    fun searchNow() {
                val approvedQuery = query; val approvedType = type
                action {
                    val page = connection.search(approvedQuery, setOf(approvedType))
                    if (query == approvedQuery && type == approvedType) { results = page.items; selected = null; if (results.isEmpty()) message = "Sin coincidencias." }
                }
    }
    LaunchedEffect(externalQuery){if(externalQuery!=null && externalQuery!=query){query=externalQuery.take(240);results=emptyList();selected=null;job?.cancel()}}
    LaunchedEffect(searchTrigger){if(searchTrigger>0 && state==SpotifyConnectionState.AUTHORIZED)searchNow()}
    Column(Modifier.fillMaxWidth().testTag("spotify-search-panel"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Buscar en Spotify", style = MaterialTheme.typography.titleSmall)
        if (state != SpotifyConnectionState.AUTHORIZED) {
            Text("Conectá Spotify para buscar dentro de Spotlight. Los controles de sesión Android siguen disponibles cuando están autorizados.", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { context.startActivity(Intent(context, MainActivity::class.java).putExtra("group", "Integraciones").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }) { Text("Configurar Spotify") }
        } else {
            if(externalQuery==null)OutlinedTextField(query, { query = it.take(240); selected = null; results = emptyList() }, singleLine = true,
                enabled = !busy, label = { Text("Canción, artista, álbum o playlist") }, modifier = Modifier.fillMaxWidth().testTag("spotify-search-query"))
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SpotifyItemType.entries.forEach { option ->
                    FilterChip(enabled = !busy, selected = option == type, onClick = { job?.cancel(); typeKey = option.api; selected = null; results = emptyList() },
                        label = { Text(when(option) { SpotifyItemType.TRACK -> "Tema"; SpotifyItemType.ARTIST -> "Artista"; SpotifyItemType.ALBUM -> "Álbum"; else -> "Lista" }, style = MaterialTheme.typography.labelSmall) })
                }
            }
            Button(enabled = query.isNotBlank() && !busy, onClick = { searchNow() }) { Text("Buscar en Spotify") }
            if (busy) TextButton(onClick = { job?.cancel() }) { Text("Cancelar solicitud") }
            results.forEach { item ->
                SpotifyResultRow(item,selected?.uri==item.uri,!busy,onSelect={if(!busy)selected=item})
            }
            selected?.let { item ->
                Text("Seleccionado: ${item.name} · ${item.subtitle}", style = MaterialTheme.typography.bodySmall)
                Text("Se solicitará la reproducción al cliente Spotify de este dispositivo. Requiere autorización local inicial.", style = MaterialTheme.typography.bodySmall)
                Button(enabled = !busy, onClick = { action { message = connection.playback("play", item) } }) { Text("Confirmar reproducción") }
                TextButton(enabled = !busy, onClick = { action {
                    val observed = connection.observe()
                    message = observed?.diagnose(item) ?: "Spotify no informó un dispositivo activo."
                } }) { Text("Comprobar reproducción") }
            }
        }
        if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("spotify-result-status"))
    }
}
