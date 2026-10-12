package dev.bautista.spotlight.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** One descriptor/renderer per document, at most three bounded page bitmaps. No network. */
class PdfPreviewSession private constructor(private val fd:ParcelFileDescriptor,private val renderer:PdfRenderer) {
    val pageCount=renderer.pageCount
    private val lock=Mutex()
    private val cache=object:LinkedHashMap<Int,Bitmap>(4,.75f,true){override fun removeEldestEntry(eldest:MutableMap.MutableEntry<Int,Bitmap>)=size>3}
    private var closed=false
    suspend fun page(index:Int):Bitmap=withContext(Dispatchers.IO){lock.withLock {
        check(!closed);require(index in 0 until pageCount);ensureActive()
        cache[index]?.let{return@withLock it}
        val bitmap=renderer.openPage(index).use{p->
            val width=800;val height=(width.toDouble()*p.height/p.width.coerceAtLeast(1)).toInt().coerceIn(1,1400)
            Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888).also{it.eraseColor(android.graphics.Color.WHITE);p.render(it,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)}
        }
        ensureActive();cache[index]=bitmap;bitmap
    }}
    suspend fun close()=withContext(Dispatchers.IO){lock.withLock{if(!closed){closed=true;cache.clear();try{renderer.close()}finally{fd.close()}}}}
    companion object {
        suspend fun open(context:Context,uri:Uri):PdfPreviewSession=withContext(Dispatchers.IO){
            val fd=context.contentResolver.openFileDescriptor(uri,"r") ?: error("No se pudo leer el PDF")
            try{PdfPreviewSession(fd,PdfRenderer(fd))}catch(e:Throwable){fd.close();throw e}
        }
    }
}

@Composable fun PdfQuickLook(context:Context,uri:Uri,page:Int,onPage:(Int)->Unit,expanded:Boolean,revision:Long?=null) {
    var session by remember(uri,revision){mutableStateOf<PdfPreviewSession?>(null)}
    var bitmap by remember(uri,revision){mutableStateOf<Bitmap?>(null)}
    var error by remember(uri,revision){mutableStateOf("")}
    var number by remember(uri,revision,page){mutableStateOf((page+1).toString())}
    LaunchedEffect(uri,revision){
        var opened:PdfPreviewSession?=null
        try {withContext(Dispatchers.IO){opened=PdfPreviewSession.open(context,uri)};session=opened;awaitCancellation()}
        catch(e:CancellationException){throw e}
        catch(e:Exception){error="No se pudo abrir el PDF. Revisá el permiso o probá abrirlo en otra app."}
        finally{withContext(NonCancellable){opened?.close()}}
    }
    LaunchedEffect(session,page){
        bitmap=null
        val active=session ?: return@LaunchedEffect
        if(active.pageCount==0){error="El PDF no contiene páginas";return@LaunchedEffect}
        try{bitmap=active.page(page.coerceIn(0,active.pageCount-1));error=""}catch(e:CancellationException){throw e}catch(e:Exception){error="No se pudo renderizar esta página."}
    }
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(6.dp)) {
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
        else if(bitmap==null)LinearProgressIndicator(Modifier.fillMaxWidth())
        bitmap?.let{Image(it.asImageBitmap(),"Página ${page+1} del PDF",Modifier.fillMaxWidth().heightIn(max=if(expanded)540.dp else 320.dp))}
        session?.let{s->if(s.pageCount>0){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                TextButton(enabled=page>0,onClick={onPage(page-1)}){Text("Anterior")}
                Text("${page+1} / ${s.pageCount}")
                TextButton(enabled=page<s.pageCount-1,onClick={onPage(page+1)}){Text("Siguiente")}
            }
            Row {
                OutlinedTextField(number,{number=it.filter(Char::isDigit).take(6)},label={Text("Página")},singleLine=true,modifier=Modifier.weight(1f))
                TextButton(enabled=number.toIntOrNull()?.let{it in 1..s.pageCount}==true,onClick={number.toIntOrNull()?.takeIf{it in 1..s.pageCount}?.let{onPage(it-1)}}){Text("Ir")}
            }
        }}
    }
}
