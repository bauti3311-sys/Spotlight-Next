package dev.bautista.spotlight.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.*

@Composable fun AiSettings(config:Config,update:(Config)->Unit) {
    val context=LocalContext.current;val vault=remember{CredentialVault(context)};val ai=remember{DirectAi(context)};val scope=rememberCoroutineScope()
    var key by remember(config.aiProvider){mutableStateOf("")};var model by remember(config.aiModel){mutableStateOf(config.aiModel)}
    var models by remember(config.aiProvider){mutableStateOf(emptyList<String>())}
    var status by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)};var job by remember{mutableStateOf<Job?>(null)}
    DisposableEffect(Unit){onDispose{job?.cancel()}}
    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        listOf("openai" to "OpenAI","gemini" to "Gemini","backend" to "Backend avanzado").forEach{(id,label)->FilterChip(config.aiProvider==id,{update(config.copy(aiProvider=id,aiModel=AiProtocol.defaultModel(id)))},enabled=!busy,label={Text(label)})}
    }
    if(config.aiProvider!="backend") {
        OutlinedTextField(key,{key=it},label={Text(if(vault.contains(config.aiProvider))"Reemplazar API key guardada" else "API key")},enabled=!busy,visualTransformation=PasswordVisualTransformation(),singleLine=true,modifier=Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth()) { Text("Responder preguntas con Enter",Modifier.weight(1f));Switch(config.aiRootConsent,{update(config.copy(aiRootConsent=it))},enabled=!busy) }
        Text("Si lo habilitás, Enter envía preguntas al proveedor configurado. Archivos y contexto personal siempre requieren revisión.",style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(model,{model=it},label={Text("Modelo")},enabled=!busy,singleLine=true,modifier=Modifier.fillMaxWidth())
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Button(enabled=!busy,onClick={busy=true;job=scope.launch{try{require(model.matches(Regex("[A-Za-z0-9._-]{1,100}"))){"Modelo inválido"};if(key.isNotBlank())withContext(Dispatchers.IO){vault.save(config.aiProvider,key)};update(config.copy(aiModel=model));key="";status="Configuración guardada"}catch(e:Exception){status="No se pudo guardar: ${e.message}"}finally{busy=false}}}){Text("Guardar")}
            ConfirmedSettingsButton("Eliminar clave","Elimina únicamente la credencial del proveedor seleccionado en este dispositivo.",enabled=!busy){busy=true;job=scope.launch{try{withContext(Dispatchers.IO){vault.delete(config.aiProvider)};status="Clave eliminada";key=""}catch(e:CancellationException){throw e}catch(e:Exception){status="No se pudo eliminar la clave. La operación no se reintenta automáticamente."}finally{busy=false}}}
        }
        if(config.aiProvider=="gemini") {
            TextButton(enabled=!busy && vault.contains("gemini"),onClick={busy=true;status="Consultando modelos…";job=scope.launch{try{ai.availableGeminiModels(config).collect{models=it};status="Elegí un modelo y guardá la configuración."}catch(e:CancellationException){throw e}catch(e:Exception){status=e.message ?: "No se pudo consultar la lista."}finally{busy=false}}}){Text("Consultar modelos disponibles")}
            if(models.isNotEmpty()) {
                var expanded by remember{mutableStateOf(false)}
                Box {
                    OutlinedButton(onClick={expanded=true}){Text("Elegir entre ${models.size} modelos")}
                    DropdownMenu(expanded,{expanded=false},modifier=Modifier.heightIn(max=280.dp)) {
                        models.forEach { name->DropdownMenuItem(text={Text(name)},onClick={model=name;expanded=false}) }
                    }
                }
            }
            Text("La lista consulta metadatos con tu clave guardada; no envía una pregunta ni cambia el modelo automáticamente.",style=MaterialTheme.typography.bodySmall)
        }
        Text("La clave se cifra con Android Keystore y queda fuera de las copias de seguridad. Una conexión directa desde Android es adecuada para tu uso privado; para distribuir la app se recomienda un backend. Tu suscripción de ChatGPT no incluye el consumo de la API.",style=MaterialTheme.typography.bodySmall)
    } else Text("Usa el endpoint HTTPS avanzado conservado de la versión anterior.")
    Row {
        OutlinedButton(enabled=!busy && ai.configured(config),onClick={busy=true;status="Comprobando…";job=scope.launch{try{ai.answer(config,listOf("user" to "Respondé solamente OK")).collect{};status="Conexión disponible"}catch(e:CancellationException){status="Comprobación cancelada";throw e}catch(e:Exception){status=e.message ?: "No se pudo conectar"}finally{busy=false}}}){Text("Comprobar conexión")}
        if(busy)TextButton(onClick={job?.cancel();busy=false}){Text("Cancelar")}
    }
    Text("La comprobación envía una consulta breve al proveedor y puede consumir cuota.",style=MaterialTheme.typography.bodySmall)
    if(status.isNotBlank())Text(status)
}
