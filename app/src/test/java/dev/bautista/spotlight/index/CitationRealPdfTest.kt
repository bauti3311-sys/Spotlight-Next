package dev.bautista.spotlight.index

import android.app.Application
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.cos.*
import com.tom_roush.pdfbox.pdmodel.*
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config as RoboConfig
import java.io.File

/** Actual PDFBox -> DocumentText -> FileIndex -> citations, no injected extractor/page metadata. */
@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class CitationRealPdfTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private val config=dev.bautista.spotlight.data.Config(files=true,contentIndex=true,online=false)
    private lateinit var index:FileIndex
    private val files=mutableListOf<File>()
    @Before fun setup(){context.deleteDatabase("search-index.db");index=FileIndex(context);PDFBoxResourceLoader.init(context)}
    @After fun cleanup(){files.forEach{it.delete()}}
    private fun pdf(name:String,pages:List<List<String>>):File {
        val file=File(context.cacheDir,name);files+=file
        PDDocument().use { doc ->
            doc.document.documentID=COSArray().apply{add(COSString("C5.2-fixture-0001"));add(COSString("C5.2-fixture-0001"))}
            for(lines in pages) {
                val page=PDPage();doc.addPage(page)
                PDPageContentStream(doc,page).use { stream ->
                    stream.beginText();stream.setFont(PDType1Font.HELVETICA,12f);stream.newLineAtOffset(30f,740f)
                    for(line in lines){stream.showText(line);stream.newLineAtOffset(0f,-18f)}
                    stream.endText()
                }
            }
            doc.save(file)
        }
        return file
    }
    private suspend fun extracted(file:File):ExtractedDocument {
        val uri=Uri.fromFile(file)
        val result=DocumentText.extractDetailed(context,uri,"application/pdf",limits=PdfTextLimits(ocrPages=0))
        index.storeTranscript(uri.toString(),file.name,"application/pdf",result.text)
        assertTrue(index.cacheExtraction(uri.toString(),result,config));return result
    }
    @Test fun realThreePagePdfKeepsUnicodeLiteralOffsetsAndCorrectPageWithRepeatedText()=runBlocking {
        val repeated="Cláusula común: conservar documento original y verificar su procedencia."
        val file=pdf("c5-real-three-pages.pdf",listOf(
            listOf("Presupuesto café: 2400 € — revisión número uno.",repeated),
            listOf("Reunión del martes: José y María — sección 2.",repeated),
            listOf("Conclusión técnica: piñata, acción, ¿cuándo? y símbolo ©.",repeated)))
        val result=extracted(file)
        assertEquals(3,result.pages);assertEquals(0,result.ocrAttempts);assertFalse(result.partial)
        assertEquals(listOf(1,2,3),result.fragments.map{it.page});assertTrue(result.fragments.all{it.method=="pdf_text"})
        File("build/c5-2").mkdirs();file.copyTo(File("build/c5-2/real-three-pages.pdf"),overwrite=true)
        File("build/c5-2/extraction.json").writeText(JSONObject(result.metadata()).put("text",result.text).toString(2))
        DocumentCitationSession(index){config}.use { session ->
            for((query,page,token) in listOf(Triple("presupuesto café",1,"2400 € —"),Triple("reunión martes",2,"José y María"),Triple("conclusión técnica",3,"¿cuándo?"),Triple("cláusula común",1,repeated))) {
                val citation=session.resolve(session.retrieve(query).single())!!
                assertEquals(page,citation.page);assertEquals(result.text.substring(citation.start,citation.end),citation.quote)
                assertTrue(citation.quote.contains(token));assertEquals(result.sourceSha256,citation.sourceSha256)
                val part=result.fragments.single{it.page==page};assertTrue(citation.start>=part.start && citation.end<=part.end)
                println("REAL_PDF query=$query page=${citation.page} offsets=${citation.start}:${citation.end} sha=${citation.sourceSha256} quote=${citation.quote}")
            }
        }
    }
    @Test fun ambiguousRepeatedPassageInsideOneRealPageIsRejected()=runBlocking {
        // A long identical substring repeats within one page; it cannot have a unique offset.
        val file=pdf("ambiguous.pdf",listOf(List(36){"repetido repetido repetido repetido repetido repetido repetido"}))
        val result=extracted(file);assertTrue(result.text.length>600)
        DocumentCitationSession(index){config}.use{assertTrue(it.retrieve("repetido").isEmpty())}
    }
    @Test fun actualExtractionWithMissingPageFragmentsCannotProduceCitations()=runBlocking {
        val file=pdf("incomplete.pdf",listOf(listOf("Procedencia incompleta debe impedir citar este documento local.")))
        extracted(file)
        context.openOrCreateDatabase("search-index.db",0,null).use{it.execSQL("UPDATE files SET extraction='{}'")}
        DocumentCitationSession(index){config}.use{assertTrue(it.retrieve("procedencia incompleta").isEmpty())}
    }
    @Test fun imageOnlyAuthenticPdfHasNoInventedTextOrPageWhenOcrUnavailable()=runBlocking {
        val file=File(context.cacheDir,"image-only.pdf");files+=file
        PDDocument().use { doc ->
            val page=PDPage();doc.addPage(page)
            // A real embedded raster (no PDF text layer), constructed without private data.
            val bitmap=android.graphics.Bitmap.createBitmap(120,80,android.graphics.Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            android.graphics.Canvas(bitmap).drawText("SCANNED FIXTURE",5f,40f,android.graphics.Paint().apply{color=android.graphics.Color.BLACK;textSize=12f})
            val image=com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory.createFromImage(doc,bitmap)
            PDPageContentStream(doc,page).use{it.drawImage(image,30f,600f)};doc.save(file);bitmap.recycle()
        }
        val result=extracted(file)
        assertEquals(1,result.pages);assertEquals(0,result.ocrAttempts);assertTrue(result.fragments.isEmpty())
        assertTrue(result.issues.any{it.code=="ocr_page_limit"})
        DocumentCitationSession(index){config}.use{assertTrue(it.retrieve("scanned fixture").isEmpty())}
        file.copyTo(File("build/c5-2/image-only.pdf"),overwrite=true)
        println("IMAGE_ONLY real raster PDF; OCR disabled explicitly, no citation; real recognition pending")
    }
}
