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
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { c ->
    addOnSuccessListener { if (c.isActive) c.resume(it) }
    addOnFailureListener { if (c.isActive) c.resumeWithException(it) }
    addOnCanceledListener { c.cancel() }
}

/** Cancellation stops waiting, not ML Kit. The completion callback releases its image/lease. */
internal suspend fun <T> awaitOwnedTask(start:()->Task<T>,release:()->Unit):T = suspendCancellableCoroutine { c ->
    val once=AtomicBoolean()
    fun finish(){if(once.compareAndSet(false,true))release()}
    try {
        if(!c.isActive){finish();return@suspendCancellableCoroutine}
        start().addOnCompleteListener(Executor{it.run()}) { task ->
            finish()
            if(c.isActive){when {task.isCanceled->c.cancel();task.isSuccessful->c.resume(task.result);else->c.resumeWithException(task.exception ?: IllegalStateException("OCR local no disponible."))}}
        }
    }catch(e:Throwable){finish();if(c.isActive)c.resumeWithException(e)}
}

/** Share bitmap lifetime across sequential OCR/label tasks, including cancellation between them. */
internal suspend fun <T> withOwnedBitmap(bitmap:Bitmap,action:suspend(OcrWorkLease)->T):T {
    val owner=OcrWorkLease{if(!bitmap.isRecycled)bitmap.recycle()}
    return try{action(owner)}finally{owner.close()}
}

class MediaAnalysis(private val context: Context) : java.io.Closeable {
    private val ocrClient=lazy {TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)}
    private val ocr get()=ocrClient.value
    private val labelsClient=lazy {
        ImageLabeling.getClient(ImageLabelerOptions.Builder().setConfidenceThreshold(.65f).build())
    }
    private val labels get()=labelsClient.value
    private val clientLock=Any()
    private var pending=0
    private var closing=false
    private var clientsClosed=false
    private fun closeIfIdle(){if(closing && pending==0 && !clientsClosed){clientsClosed=true;if(ocrClient.isInitialized())runCatching{ocr.close()};if(labelsClient.isInitialized())runCatching{labels.close()}}}
    private fun <T> tracked(start:()->Task<T>):Task<T> {
        synchronized(clientLock){check(!closing);pending++}
        val completed=AtomicBoolean()
        fun finish(){if(completed.compareAndSet(false,true))synchronized(clientLock){pending--;closeIfIdle()}}
        return try{start().also{it.addOnCompleteListener(Executor{r->r.run()}){finish()}}}
        catch(e:Throwable){finish();throw e}
    }
    /** Takes bitmap ownership, including cancellation before submission. No image labels. */
    internal suspend fun pdfPage(bitmap:Bitmap,onFinished:()->Unit):String =
        awaitOwnedTask(start={tracked{ocr.process(InputImage.fromBitmap(bitmap,0))}},release={try{bitmap.recycle()}finally{onFinished()}}).text
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

    private suspend fun analyze(bitmap: Bitmap, visual: Boolean): String = withOwnedBitmap(bitmap) { owner ->
        val input=InputImage.fromBitmap(bitmap,0)
        val text=awaitOwnedTask(start={tracked{ocr.process(input)}},release=owner.retain()).text.take(16000)
        val tags=if(visual)awaitOwnedTask(start={tracked{labels.process(input)}},release=owner.retain()).joinToString(" "){it.text+" "+(spanish[it.text.lowercase()] ?: "")} else ""
        if(visual)text+"\nEtiquetas aproximadas: "+tags else text
    }

    suspend fun image(uri: Uri, visual: Boolean): String {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if(bounds.outWidth<=0 || bounds.outHeight<=0)return ""
        val opts = BitmapFactory.Options().apply{inSampleSize=1}
        while (
            bounds.outWidth / opts.inSampleSize > 1600 ||
                bounds.outHeight / opts.inSampleSize > 1600
        ) opts.inSampleSize *= 2
        val bitmap =
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            } ?: return ""
        var normalized:Bitmap?=null
        var transferred=false
        return try {
            withTimeout(30000) {
                val oriented=ImageMetadata.orient(bitmap,ImageMetadata.read(context,uri).orientation)
                normalized=oriented
                if(oriented!==bitmap)bitmap.recycle()
                transferred=true
                analyze(oriented,visual)
            }
        } finally {
            if(!transferred){normalized?.let{if(!it.isRecycled)it.recycle()};if(!bitmap.isRecycled)bitmap.recycle()}
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
                        try { if (it.width > 1600 || it.height > 1600)
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
                        else it } catch(e:Throwable){it.recycle();throw e}
                    } ?: continue
                if(original!==frame && original?.isRecycled==false)original.recycle()
                var transferred=false
                try {
                    output
                        .append("[${ms/1000}] ")
                        .append(withTimeout(30000) { transferred=true;analyze(frame, true) }.replace('\n', ' '))
                        .append('\n')
                } finally {
                    if(!transferred && !frame.isRecycled)frame.recycle()
                }
            }
            output.toString().take(40000)
        } finally {
            retriever.release()
        }
    }

    override fun close() {
        synchronized(clientLock){closing=true;closeIfIdle()}
    }
}
