package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.control.ExperimentalMessagingPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Keeps authorization and account linking separate; no linked transport is shipped. */
@Composable fun WhatsAppExperimentalSettings() {
    val context = LocalContext.current
    val preferences = remember { ExperimentalMessagingPreferences(context) }
    val scope = rememberCoroutineScope()
    var allowed by remember { mutableStateOf(preferences.allowed) }
    var reviewing by remember { mutableStateOf(false) }
    var risks by remember { mutableStateOf(false) }
    var privacy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    Text("Funciones experimentales", style = MaterialTheme.typography.titleSmall)
    Text(if (allowed) "Consentimiento guardado · motor no disponible" else "Experimental desactivado · modo normal", modifier = Modifier.testTag("whatsapp-experimental-state"))
    Text("Un dispositivo vinculado no oficial podría permitir mensajes nuevos sin abrir WhatsApp. Puede ocasionar restricciones de cuenta y manejar claves de sesión. No es una integración oficial de Meta.", style = MaterialTheme.typography.bodySmall)
    Text("En esta Preview no se incluye un motor vinculado: no se abren conexiones, sincronizan chats ni crean sesiones. RemoteInput e intents siguen disponibles. Vincular requerirá otra confirmación y validación con una cuenta secundaria.", style = MaterialTheme.typography.bodySmall)
    if (!allowed) OutlinedButton(onClick = { risks = false; privacy = false; reviewing = true }) { Text("Activar modo experimental") }
    else TextButton(onClick = { scope.launch {
        try { withContext(Dispatchers.IO) { preferences.revoke() }; allowed = false }
        catch (_: Exception) { error = "No se pudo retirar el consentimiento." }
    } }) { Text("Volver al modo normal") }
    OutlinedButton(enabled = false, onClick = {}, modifier = Modifier.testTag("whatsapp-link-device")) { Text("Vincular dispositivo · no disponible") }
    Text("No hay ninguna sesión vinculada desde Spotlight. Desconectar y eliminar sesiones estarán disponibles cuando exista un proveedor validado.", style = MaterialTheme.typography.bodySmall)
    if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
    if (reviewing) AlertDialog(onDismissRequest = { reviewing = false }, title = { Text("Consentimiento experimental") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Esta decisión guarda tu consentimiento para preparar el experimento. No vincula una cuenta ni autoriza envíos.")
            Row { Checkbox(risks, { risks = it }, modifier = Modifier.testTag("whatsapp-consent-risk")); Text("Entiendo que el cliente no es oficial y puede poner la cuenta en riesgo.") }
            Row { Checkbox(privacy, { privacy = it }, modifier = Modifier.testTag("whatsapp-consent-privacy")); Text("Entiendo que una futura vinculación manejará claves y sesiones locales y requerirá otra confirmación.") }
        }
    }, confirmButton = {
        TextButton(enabled = risks && privacy, onClick = { scope.launch {
            try { withContext(Dispatchers.IO) { preferences.confirmFromUi(risks, privacy) }; allowed = true; reviewing = false }
            catch (_: Exception) { error = "No se pudo guardar el consentimiento."; reviewing = false }
        } }) { Text("Acepto y autorizo preparar el experimento") }
    }, dismissButton = { TextButton(onClick = { reviewing = false }) { Text("Cancelar") } })
}
