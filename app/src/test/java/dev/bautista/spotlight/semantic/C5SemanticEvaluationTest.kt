package dev.bautista.spotlight.semantic

import android.app.Application
import android.net.Uri
import dev.bautista.spotlight.control.DeviceResourceState
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.index.*
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config as RoboConfig
import java.io.File
import java.security.MessageDigest

/** No injected embedding backend or invented vectors. This environment has no verified local model.
 * Missing-model semantics are NOT a zero-accuracy model evaluation. C5.2 fixtures remain labelled. */
@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class C5SemanticEvaluationTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private val config=MutableStateFlow(Config(files=true,contentIndex=true,semanticSearch=true,online=false))
    // Controlled resource inputs exercise policy gates; not physical resource measurements.
    private var resources=DeviceResourceState(80,false,false,2L*1024*1024*1024,128L*1024*1024,false,0)
    private lateinit var index:FileIndex
    private val temporary=mutableListOf<File>()
    private val corpusBytes get()=javaClass.getResourceAsStream("/c5-3/corpus.json")!!.use{it.readBytes()}
    private fun sha(bytes:ByteArray)=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
    @Before fun setup(){context.deleteDatabase("search-index.db");index=FileIndex(context,resourceState={resources},liveConfig={config.value})}
    @After fun cleanup(){temporary.forEach{it.delete()}}
    private fun vectorCount():Int=context.openOrCreateDatabase("search-index.db",0,null).use{db->db.rawQuery("SELECT COUNT(*) FROM semantic_vectors",null).use{it.moveToFirst();it.getInt(0)}}
    private suspend fun seed(name:String,text:String):String {
        val file=File(context.cacheDir,name).apply{writeText(text)};temporary+=file
        val uri=Uri.fromFile(file)
        index.storeTranscript(uri.toString(),name,"text/plain",text)
        assertTrue(index.cacheExtraction(uri.toString(),DocumentText.extractDetailed(context,uri,"text/plain"),config.value))
        return uri.toString()
    }
    private fun rasterFixture(file:File,text:String) {
        // Authentic image-only PDF with the declared fictitious glyphs. OCR output remains controlled.
        com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(context)
        val bitmap=android.graphics.Bitmap.createBitmap(640,120,android.graphics.Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(android.graphics.Color.WHITE)
            android.graphics.Canvas(bitmap).drawText(text,10f,55f,android.graphics.Paint().apply{color=android.graphics.Color.BLACK;textSize=18f})
            com.tom_roush.pdfbox.pdmodel.PDDocument().use { pdf ->
                pdf.document.documentID=com.tom_roush.pdfbox.cos.COSArray().apply{add(com.tom_roush.pdfbox.cos.COSString("C5.3-OCR-FIXTURE1"));add(com.tom_roush.pdfbox.cos.COSString("C5.3-OCR-FIXTURE1"))}
                val page=com.tom_roush.pdfbox.pdmodel.PDPage();pdf.addPage(page)
                val image=com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory.createFromImage(pdf,bitmap)
                com.tom_roush.pdfbox.pdmodel.PDPageContentStream(pdf,page).use{it.drawImage(image,10f,600f,580f,108.75f)}
                pdf.save(file)
            }
            File("build/c5-3").mkdirs();file.copyTo(File("build/c5-3/ocr-raster-fixture.pdf"),overwrite=true)
            File("build/c5-3/ocr-raster-fixture.png").outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
        } finally {bitmap.recycle()}
    }
    @Test fun actualLexicalCorpusAndCitationBaselineWithUnavailableSemanticModel()=runBlocking {
        val corpus=JSONObject(String(corpusBytes,Charsets.UTF_8));val docs=corpus.getJSONArray("documents")
        val bodies=mutableMapOf<String,ExtractedDocument>();val uris=mutableMapOf<String,String>();val original=mutableMapOf<String,File>()
        for(i in 0 until docs.length()) {
            val d=docs.getJSONObject(i);val id=d.getString("id");val name=d.getString("filename")
            val file=File(context.cacheDir,name);temporary+=file
            if(d.has("ocr_fixture_text"))rasterFixture(file,d.getString("ocr_fixture_text"))
            else if(d.has("fixture")) {
                val source=File("../docs/verification/c5-2-provenance-20261107/pdf",d.getString("fixture"))
                assertTrue(source.isFile);source.copyTo(file,overwrite=true)
                if(d.has("sha256"))assertEquals(d.getString("sha256"),sha(file.readBytes()))
            } else file.writeText(d.getString("text"))
            val uri=Uri.fromFile(file);val mime=if(name.endsWith(".pdf"))"application/pdf" else "text/plain"
            val extracted=if(d.has("ocr_fixture_text")) {
                val text=d.getString("ocr_fixture_text")
                ExtractedDocument(text,listOf(TextFragment(1,"ocr_local",0,text.length)),emptyList(),1,1,1,sha(file.readBytes()))
            } else DocumentText.extractDetailed(context,uri,mime,limits=PdfTextLimits(ocrPages=0))
            index.storeTranscript(uri.toString(),name,mime,extracted.text)
            assertTrue(index.cacheExtraction(uri.toString(),extracted,config.value))
            uris[uri.toString()]=id;bodies[id]=extracted;original[id]=file
        }
        val ready=index.semanticModel.ready()
        assertFalse("This availability-only lab must not acquire a model implicitly",ready)
        assertEquals(0,vectorCount())
        val rows=JSONArray();var tp=0;var fp=0;var fn=0;var rr=0.0;var positiveQueries=0;var hitQueries=0
        val queries=corpus.getJSONArray("queries")
        DocumentCitationSession(index){config.value}.use { citations ->
            for(i in 0 until queries.length()) {
                val q=queries.getJSONObject(i);val text=q.getString("query")
                val relevant=q.getJSONArray("relevant").let{a->(0 until a.length()).map{a.getString(it)}.toSet()}
                var started=System.nanoTime();val results=index.search(text,config.value);val lexicalNanos=System.nanoTime()-started
                val order=results.map{uris.getValue((it.action as Action.Launch).intent.data.toString())}
                val actual=order.toSet();val truePos=(actual intersect relevant).size
                tp+=truePos;fp+=(actual-relevant).size;fn+=(relevant-actual).size
                if(relevant.isNotEmpty()){positiveQueries++;if(truePos>0)hitQueries++;val first=order.indexOfFirst{it in relevant};if(first>=0)rr+=1.0/(first+1)}
                started=System.nanoTime();val semantic=index.semanticSearch(text,config.value);val gateNanos=System.nanoTime()-started
                assertTrue(semantic.isEmpty())
                val references=citations.retrieve(text);val validated=JSONArray()
                for(ref in references) {
                    val c=citations.resolve(ref)!!;val id=uris.getValue(c.documentUri);val source=bodies.getValue(id)
                    assertEquals(source.text.substring(c.start,c.end),c.quote);assertEquals(sha(original.getValue(id).readBytes()),c.sourceSha256)
                    assertEquals(id=="D6",c.uncertain)
                    c.page?.let{page->assertTrue(source.fragments.any{it.page==page && c.start>=it.start && c.end<=it.end})}
                    validated.put(JSONObject().put("id",id).put("quote",c.quote).put("page",c.page ?: JSONObject.NULL).put("uncertain",c.uncertain).put("start",c.start).put("end",c.end).put("source_sha256",c.sourceSha256))
                }
                rows.put(JSONObject().put("id",q.getString("id")).put("category",q.getString("category")).put("query",text)
                    .put("relevant",JSONArray(relevant.toList())).put("lexical_order",JSONArray(order)).put("tp",truePos).put("fp",(actual-relevant).size).put("fn",(relevant-actual).size)
                    .put("lexical_elapsed_ns",lexicalNanos).put("missing_semantic_gate_elapsed_ns",gateNanos)
                    .put("semantic_status","not_evaluated_model_missing_no_vectors").put("semantic_ranking",JSONObject.NULL).put("semantic_quality",JSONObject.NULL).put("citations",validated))
            }
        }
        assertEquals(0,vectorCount());assertFalse(index.semanticModel.directory.exists())
        val report=JSONObject().put("corpus_sha256",sha(corpusBytes)).put("model",SemanticPolicy.MODEL).put("model_ready",ready).put("semantic_vectors",0)
            .put("platform","Robolectric API33 / Linux x86_64 JVM; NOT Android device; healthy resource policy fixture")
            .put("new_model_downloads",0).put("embedding_inference_executed",false).put("semantic_quality",JSONObject.NULL)
            .put("lexical",JSONObject().put("tp",tp).put("fp",fp).put("fn",fn).put("micro_precision",if(tp+fp==0)JSONObject.NULL else tp.toDouble()/(tp+fp))
                .put("micro_recall",tp.toDouble()/(tp+fn)).put("positive_queries",positiveQueries).put("queries_with_relevant_hit",hitQueries).put("mrr_positive_queries",rr/positiveQueries))
            .put("cases",rows)
        File("build/c5-3").mkdirs();File("build/c5-3/evaluation.json").writeText(report.toString(2))
        println("C5_3 lexical tp=$tp fp=$fp fn=$fn; semantic NOT EVALUATED (no model), no inference or downloads")
    }
    @Test fun readinessDoesNotDownloadAndRejectsIncompleteAssets()=runBlocking {
        val store=SemanticModelStore(context){error("Network client must never be requested by ready")}
        assertFalse(store.ready());assertFalse(LocalDocumentEmbedder(store).available())
        assertFalse(store.directory.exists())
        store.directory.mkdirs();val partial=File(store.directory,"model.onnx").apply{writeBytes(byteArrayOf(1,2,3))}
        try{assertFalse(store.ready());assertEquals(3,partial.length())}finally{partial.delete();store.directory.delete()}
    }
    @Test fun explicitPreparationWithoutModelAndResourceDeferralKeepLexicalIndex()=runBlocking {
        seed("local.txt","Presupuesto aprobado del proyecto local.")
        index.buildSemantic(config.value)
        assertTrue(index.semanticStatus.value.message.contains("Instalá primero"));assertEquals(0,vectorCount())
        assertEquals(1,index.search("presupuesto aprobado",config.value).size)
        resources=resources.copy(lowMemory=true);index.buildSemantic(config.value)
        assertTrue(index.semanticStatus.value.message.contains("memoria"));assertEquals(0,vectorCount())
        config.value=config.value.copy(privateMode=true);assertTrue(index.semanticSearch("proyecto local",config.value).isEmpty())
        assertFalse(index.semanticModel.directory.exists())
    }
    @Test fun semanticPreferenceWithoutModelDoesNotChangeCitationLifetime()=runBlocking {
        seed("state.txt","Presupuesto aprobado del proyecto local.")
        val parent=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        DocumentCitationState(index,config,parent).use { state ->
            state.query("presupuesto aprobado").join();assertEquals(1,state.visible.value.size)
            val ref=CitationRef(state.visible.value.single().sourceId)
            state.query("dinero asignado a la iniciativa").join();assertTrue(state.visible.value.isEmpty())
            assertFalse(state.withValidated(ref){fail("Old query reference")})
            state.query("presupuesto aprobado").join();assertEquals(1,state.visible.value.size)
            config.value=config.value.copy(privateMode=true);assertTrue(state.visible.value.isEmpty())
        }
        parent.cancel()
    }
}
