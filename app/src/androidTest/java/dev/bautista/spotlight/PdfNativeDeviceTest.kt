package dev.bautista.spotlight

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.bautista.spotlight.ui.PdfPreviewSession
import java.io.File
import kotlinx.coroutines.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Runs on Android/emulator, not a PdfRenderer shadow. Synthetic documents contain no user data. */
@RunWith(AndroidJUnit4::class)
class PdfNativeDeviceTest {
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
    private fun fixture():File {
        val file=File(context.cacheDir,"native-two-pages.pdf")
        val pdf=PdfDocument()
        try {for(number in 1..2){val page=pdf.startPage(PdfDocument.PageInfo.Builder(300,400,number).create());page.canvas.drawColor(Color.WHITE);page.canvas.drawText("Página $number",20f,40f,Paint().apply{color=Color.BLACK;textSize=18f});pdf.finishPage(page)};file.outputStream().use{pdf.writeTo(it)}}
        finally {pdf.close()}
        return file
    }
    @Test fun actualNativeRendererOpensTwoPagesAndClosesResources()=runBlocking {
        val file=fixture()
        try {repeat(5){val session=PdfPreviewSession.open(context,Uri.fromFile(file));try{assertEquals(2,session.pageCount);val first=session.page(0);val second=session.page(1);assertTrue(first.width>0 && first.height>0);assertTrue(second.width>0);assertNotSame(first,second);assertSame(first,session.page(0));assertEquals(Color.WHITE,first.getPixel(first.width-1,0))}finally{session.close()}}}
        finally{file.delete()}
    }
    @Test fun cancellationAndDocumentSwitchDoNotKeepRendererAlive()=runBlocking {
        val file=fixture();val session=PdfPreviewSession.open(context,Uri.fromFile(file))
        try{coroutineScope{val job=launch{repeat(30){session.page(it%2)}};yield();job.cancelAndJoin()}}finally{session.close()}
        val second=PdfPreviewSession.open(context,Uri.fromFile(file));try{assertEquals(2,second.pageCount);assertTrue(second.page(1).width>0)}finally{second.close();file.delete()}
    }
    @Test fun corruptAndMissingFilesFailWithoutProducingFakePreview()=runBlocking {
        val file=File(context.cacheDir,"corrupt.pdf").apply{writeText("not a PDF")}
        try{var failed=false;try{PdfPreviewSession.open(context,Uri.fromFile(file)).close()}catch(e:Exception){failed=true};assertTrue(failed)}finally{file.delete()}
        var missing=false;try{PdfPreviewSession.open(context,Uri.fromFile(file)).close()}catch(e:Exception){missing=true};assertTrue(missing)
    }
}
