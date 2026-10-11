package dev.bautista.spotlight.index

import android.app.Application
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.*
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class DocumentTextTest {
    @Test fun extractsTextFromActualPdf():Unit=runBlocking {
        val app=RuntimeEnvironment.getApplication();PDFBoxResourceLoader.init(app)
        val file=File(app.cacheDir,"test-next.pdf")
        PDDocument().use{doc->val page=PDPage();doc.addPage(page);PDPageContentStream(doc,page).use{content->content.beginText();content.setFont(PDType1Font.HELVETICA,12f);content.newLineAtOffset(20f,700f);content.showText("Documento de anatomia");content.endText()};doc.save(file)}
        val uri=FileProvider.getUriForFile(app,"dev.bautista.spotlight.next.files",file)
        assertTrue(DocumentText.extract(app,uri,"application/pdf").contains("Documento de anatomia"));file.delete()
    }
    @Test fun unsupportedBinaryIsNotTreatedAsText():Unit=runBlocking {
        val app=RuntimeEnvironment.getApplication();val file=File(app.cacheDir,"test-next.bin").apply{writeBytes(byteArrayOf(1,2,3))}
        val uri=FileProvider.getUriForFile(app,"dev.bautista.spotlight.next.files",file)
        assertTrue(runCatching{DocumentText.extract(app,uri,"application/octet-stream")}.exceptionOrNull() is IllegalArgumentException);file.delete()
    }
}
