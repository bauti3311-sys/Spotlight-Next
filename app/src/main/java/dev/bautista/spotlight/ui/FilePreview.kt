package dev.bautista.spotlight.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.*
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import kotlinx.coroutines.*

private data class PreviewData(val text: String? = null, val bitmap: Bitmap? = null,val pages:Int=1)

@Composable
fun FilePreview(context: Context, item: Result, expanded:Boolean=false, page:Int?=null, onPage:((Int)->Unit)?=null) {
    var localPage by remember(item.id){mutableIntStateOf(0)}
    val pdfUri=(item.action as? Action.Launch)?.intent?.data
    if(item.mime=="application/pdf" && pdfUri!=null){PdfQuickLook(context,pdfUri,page ?: localPage,{localPage=it;onPage?.invoke(it)},expanded);return}
    if (item.mime?.startsWith("audio/") == true) {
        AudioPreview(item)
        return
    }
    if (item.mime?.startsWith("video/") == true) {
        VideoPreview(item)
        return
    }
    var pageIndex by remember(item.id){mutableIntStateOf(0)}
    var data by remember(item.id) { mutableStateOf<PreviewData?>(null) }
    LaunchedEffect(item.id,pageIndex) {
        data =
            withContext(Dispatchers.IO) {
                try {
                    val uri =
                        (item.action as? Action.Launch)?.intent?.data
                            ?: return@withContext PreviewData(text = "Sin vista previa")
                    when {
                        dev.bautista.spotlight.index.OfficeText.supports(item.mime,item.title)->PreviewData(text="Vista de texto Office · sin formato visual\n\n"+dev.bautista.spotlight.index.DocumentText.extract(context,uri,dev.bautista.spotlight.index.OfficeText.mime(item.title,item.mime)))
                        item.mime?.startsWith("text/") == true ->
                            context.contentResolver.openInputStream(uri)?.use {
                                val output = java.io.ByteArrayOutputStream()
                                val buffer = ByteArray(4096)
                                while (output.size() < 32769) {
                                    val count =
                                        it.read(
                                            buffer,
                                            0,
                                            minOf(buffer.size, 32769 - output.size()),
                                        )
                                    if (count < 0) break
                                    output.write(buffer, 0, count)
                                }
                                val bytes = output.toByteArray()
                                PreviewData(
                                    text =
                                        bytes.take(32768).toByteArray().toString(Charsets.UTF_8) +
                                            (if (bytes.size > 32768)
                                                "\n… Vista previa limitada a 32 KB"
                                            else "")
                                )
                            }
                        item.mime?.startsWith("image/") == true -> {
                            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            context.contentResolver.openInputStream(uri)?.use {
                                BitmapFactory.decodeStream(it, null, bounds)
                            }
                            val opts = BitmapFactory.Options().apply{inSampleSize=1}
                            while (
                                bounds.outWidth / opts.inSampleSize > 800 ||
                                    bounds.outHeight / opts.inSampleSize > 800
                            ) opts.inSampleSize *= 2
                            context.contentResolver.openInputStream(uri)?.use {
                                PreviewData(
                                    bitmap =
                                        BitmapFactory.decodeStream(it, null, opts)?.let { bitmap ->
                                            dev.bautista.spotlight.index.ImageMetadata.orient(
                                                bitmap,
                                                dev.bautista.spotlight.index.ImageMetadata.read(
                                                        context,
                                                        uri,
                                                    )
                                                    .orientation,
                                            )
                                        }
                                )
                            }
                        }
                        item.mime == "application/pdf" ->
                            context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                                PdfRenderer(fd).use { pdf ->
                                    if (pdf.pageCount == 0) PreviewData(text = "Documento vacío")
                                    else
                                        pdf.openPage(pageIndex.coerceIn(0,pdf.pageCount-1)).use { page ->
                                            val w = 600
                                            val h =
                                                (w.toDouble() * page.height / page.width)
                                                    .toInt()
                                                    .coerceIn(1, 1000)
                                            val bitmap =
                                                Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                                            bitmap.eraseColor(android.graphics.Color.WHITE)
                                            page.render(
                                                bitmap,
                                                null,
                                                null,
                                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY,
                                            )
                                            PreviewData(bitmap = bitmap, text = "Página ${pageIndex+1} de ${pdf.pageCount}",pages=pdf.pageCount)
                                        }
                                }
                            }
                        else ->
                            PreviewData(
                                text =
                                    "Abrí el archivo en una app compatible para ver su contenido."
                            )
                    } ?: PreviewData(text = "No se pudo leer el archivo")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    PreviewData(
                        text =
                            "No se pudo generar la vista previa. Podés intentar abrir el archivo."
                    )
                }
            }
    }
    if (data == null) LinearProgressIndicator(Modifier.fillMaxWidth())
    data?.bitmap?.let {
        Image(
            it.asImageBitmap(),
            "Vista previa del archivo",
            Modifier.fillMaxWidth().heightIn(max = if(expanded)540.dp else 320.dp),
        )
    }
    data?.text?.let { androidx.compose.foundation.text.selection.SelectionContainer {Text(it, fontSize = 13.sp)} }
    if(item.mime=="application/pdf" && (data?.pages ?: 1)>1)Row {TextButton(enabled=pageIndex>0,onClick={pageIndex--}){Text("Anterior")};TextButton(enabled=pageIndex<(data!!.pages-1),onClick={pageIndex++}){Text("Siguiente")}}
}
