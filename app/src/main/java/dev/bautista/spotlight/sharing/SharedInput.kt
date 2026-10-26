package dev.bautista.spotlight.sharing

import android.content.Intent
import android.net.Uri

internal data class SharedInput(val text: String, val documents: List<Uri>) {
    companion object {
        const val MAX_DOCUMENTS = 12
        const val MAX_TEXT = 16000

        /** Inspect explicit input only. Never coerce ClipData text through a resolver:
         * doing so can dereference a URI before validation or read unrelated content. */
        @Suppress("DEPRECATION")
        fun parse(intent: Intent): SharedInput {
            require(intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_SEND_MULTIPLE) {
                "Acción de compartir no compatible."
            }
            val streams = when (val extra = intent.extras?.get(Intent.EXTRA_STREAM)) {
                null -> emptyList()
                is Uri -> listOf(extra)
                is ArrayList<*> -> {
                    require(intent.action == Intent.ACTION_SEND_MULTIPLE && extra.size <= MAX_DOCUMENTS) {
                        "La selección de documentos no es válida."
                    }
                    extra.map { requireNotNull(it as? Uri) { "Documento compartido no válido." } }
                }
                else -> error("Documento compartido no válido.")
            }
            val clip = intent.clipData
            require((clip?.itemCount ?: 0) <= MAX_DOCUMENTS) { "Compartí hasta 12 documentos." }
            val items = (0 until (clip?.itemCount ?: 0)).map { clip!!.getItemAt(it) }
            val documents = (streams + items.mapNotNull { it.uri }).distinct()
            require(documents.size <= MAX_DOCUMENTS && documents.all {
                it.scheme == "content" && !it.authority.isNullOrBlank()
            }) { "Usá documentos con acceso autorizado de Android." }
            val extraText = intent.extras?.get(Intent.EXTRA_TEXT)
            require(extraText == null || extraText is CharSequence) { "Texto compartido no válido." }
            val text = (extraText as? CharSequence)?.toString()?.takeIf { it.isNotBlank() }
                ?: items.mapNotNull { it.text?.toString()?.take(MAX_TEXT) }.joinToString("\n")
            require(documents.isNotEmpty() || text.isNotBlank()) { "No hay contenido compartido." }
            return SharedInput(text.take(MAX_TEXT), documents)
        }
    }
}
