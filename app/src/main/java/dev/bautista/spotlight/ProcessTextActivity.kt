package dev.bautista.spotlight

import android.content.*
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bautista.spotlight.index.MediaAnalysis
import dev.bautista.spotlight.ui.SpotlightTheme
import kotlinx.coroutines.*

/** Public Android text-selection/share integration; does not inspect the active app's screen. */
class ProcessTextActivity : ComponentActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as SpotlightApp
        val original =
            (intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
                    ?: intent.getCharSequenceExtra(Intent.EXTRA_TEXT))
                ?.toString()
                ?.take(8000) ?: ""
        val image =
            if (intent.type?.startsWith("image/") == true)
                (if (android.os.Build.VERSION.SDK_INT >= 33)
                        intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    else intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
                    ?.takeIf { it.scheme == "content" }
            else null
        val editable =
            intent.action == Intent.ACTION_PROCESS_TEXT &&
                !intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
        setContent {
            var text by remember { mutableStateOf(original) }
            var busy by remember { mutableStateOf(false) }
            var message by remember { mutableStateOf("") }
            val scope = rememberCoroutineScope()
            val config by app.preferences.config.collectAsState(initial=dev.bautista.spotlight.data.Config())
            SpotlightTheme(config.theme,config) {
                Surface(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp)
                    ) {
                        Text(
                            "Spotlight · texto seleccionado",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            "Solo usa el texto o la imagen que compartiste. No lee el resto de la pantalla.",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                        if (image != null) {
                            Text("Imagen compartida · OCR local", fontSize = 14.sp)
                            Button(
                                onClick = {
                                    busy = true
                                    scope.launch {
                                        try {
                                            text =
                                                withContext(Dispatchers.IO) {
                                                    MediaAnalysis(this@ProcessTextActivity).use {
                                                        it.image(image, false)
                                                    }
                                                }
                                            message = "Texto reconocido en el dispositivo"
                                        } catch (e: CancellationException) {
                                            throw e
                                        } catch (e: Exception) {
                                            message = "No se pudo leer la imagen"
                                        } finally {
                                            busy = false
                                        }
                                    }
                                },
                                enabled = !busy,
                            ) {
                                Text("Reconocer texto")
                            }
                        }
                        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                        OutlinedTextField(
                            text,
                            { text = it.take(8000) },
                            Modifier.fillMaxWidth().heightIn(min = 180.dp),
                            label = { Text("Texto") },
                        )
                        Row(Modifier.horizontalScroll(rememberScrollState())) {
                            TextButton(onClick = { text = text.uppercase() }) { Text("Mayúsculas") }
                            TextButton(onClick = { text = text.lowercase() }) { Text("Minúsculas") }
                            TextButton(onClick = { text = "**$text**" }) {
                                Text("Negrita Markdown")
                            }
                        }
                        Text(
                            "Negrita agrega **marcas Markdown**. Su formato visual depende del editor receptor.",
                            fontSize = 12.sp,
                        )
                        Row(Modifier.horizontalScroll(rememberScrollState())) {
                            TextButton(
                                onClick = {
                                    getSystemService(ClipboardManager::class.java)
                                        .setPrimaryClip(ClipData.newPlainText("Spotlight", text))
                                    message = "Copiado"
                                }
                            ) {
                                Text("Copiar")
                            }
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        runCatching { app.local.saveNote(text) }
                                            .onSuccess { message = "Nota guardada" }
                                            .onFailure { message = "No se pudo guardar" }
                                    }
                                },
                                enabled = text.isNotBlank(),
                            ) {
                                Text("Guardar nota")
                            }
                        }
                        Button(
                            onClick = {
                                startActivity(
                                    Intent(this@ProcessTextActivity, InvokeActivity::class.java)
                                        .putExtra("query", text.take(4000))
                                )
                                finish()
                            },
                            enabled = text.isNotBlank(),
                        ) {
                            Text("Buscar en Spotlight")
                        }
                        if (editable)
                            Button(
                                onClick = {
                                    setResult(
                                        RESULT_OK,
                                        Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, text),
                                    )
                                    finish()
                                }
                            ) {
                                Text("Devolver al editor")
                            }
                        if (message.isNotBlank()) Text(message, Modifier.padding(vertical = 12.dp))
                        TextButton(onClick = { finish() }) { Text("Cerrar") }
                    }
                }
            }
        }
    }
}
