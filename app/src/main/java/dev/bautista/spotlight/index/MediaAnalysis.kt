package dev.bautista.spotlight.index

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.*

suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { c ->
    addOnSuccessListener { if (c.isActive) c.resume(it) }
    addOnFailureListener { if (c.isActive) c.resumeWithException(it) }
    addOnCanceledListener { c.cancel() }
}

class MediaAnalysis(private val context: Context) : java.io.Closeable {
    private val ocr = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val labels =
        ImageLabeling.getClient(ImageLabelerOptions.Builder().setConfidenceThreshold(.65f).build())
    private val spanish =
        mapOf(
            "dog" to "perro perros",
            "cat" to "gato gatos",
            "beach" to "playa playas",
            "sea" to "mar",
            "ocean" to "mar oceano",
            "water" to "agua",
            "food" to "comida",
            "car" to "auto coche",
            "flower" to "flor flores",
            "bird" to "pajaro aves",
            "mountain" to "montana",
            "sky" to "cielo",
            "snow" to "nieve",
            "sunset" to "atardecer",
            "building" to "edificio",
            "tree" to "arbol arboles",
            "plant" to "planta plantas",
            "animal" to "animal animales",
            "person" to "persona personas",
            "document" to "documento documentos",
            "cathedral" to "catedral",
            "boat" to "barco",
            "sand" to "arena",
            "screenshot" to "captura pantalla",
        )

    private suspend fun analyze(bitmap: Bitmap, visual: Boolean): String {
        val input = InputImage.fromBitmap(bitmap, 0)
        val text = ocr.process(input).awaitResult().text.take(16000)
        val tags =
            if (visual)
                labels.process(input).awaitResult().joinToString(" ") {
                    it.text + " " + (spanish[it.text.lowercase()] ?: "")
                }
            else ""
        return if (visual) text + "\nEtiquetas aproximadas: " + tags else text
    }

    suspend fun image(uri: Uri, visual: Boolean): String {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        val opts = BitmapFactory.Options()
        while (
            bounds.outWidth / opts.inSampleSize > 1600 ||
                bounds.outHeight / opts.inSampleSize > 1600
        ) opts.inSampleSize *= 2
        val bitmap =
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            } ?: return ""
        return try {
            withTimeout(30000) {
                analyze(
                    ImageMetadata.orient(bitmap, ImageMetadata.read(context, uri).orientation),
                    visual,
                )
            }
        } finally {
            /* managed lifetime: ML Tasks can outlive coroutine cancellation */
        }
    }

    suspend fun video(uri: Uri): String {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val duration =
                retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L
            val output = StringBuilder()
            for (ms in 0L until minOf(duration, 300000L) step 15000L) {
                currentCoroutineContext().ensureActive()
                val original =
                    if (android.os.Build.VERSION.SDK_INT >= 27)
                        retriever.getScaledFrameAtTime(
                            ms * 1000,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                            640,
                            360,
                        )
                    else
                        retriever.getFrameAtTime(
                            ms * 1000,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                        )
                val frame =
                    original?.let {
                        if (it.width > 1600 || it.height > 1600)
                            Bitmap.createScaledBitmap(
                                it,
                                (it.width * minOf(1600f / it.width, 1600f / it.height))
                                    .toInt()
                                    .coerceAtLeast(1),
                                (it.height * minOf(1600f / it.width, 1600f / it.height))
                                    .toInt()
                                    .coerceAtLeast(1),
                                true,
                            )
                        else it
                    } ?: continue
                try {
                    output
                        .append("[${ms/1000}] ")
                        .append(withTimeout(30000) { analyze(frame, true) }.replace('\n', ' '))
                        .append('\n')
                } finally {
                    /* managed lifetime: ML Tasks can outlive coroutine cancellation */
                }
            }
            output.toString().take(40000)
        } finally {
            retriever.release()
        }
    }

    override fun close() {
        ocr.close()
        labels.close()
    }
}
