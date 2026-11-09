package dev.bautista.spotlight.c5

import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.*
import android.provider.DocumentsContract
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bautista.spotlight.control.DeviceResources
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.index.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Prepared for an explicitly authorized, matching test installation; not run by C5.4. */
@RunWith(AndroidJUnit4::class)
class C5DocumentDeviceTest {
    @Test fun authenticPdfCitationsAndExplicitNativePreview()=runBlocking {
        val lab=C5PhysicalLab()
        lab.runCase("pdf") {
            val c=Config(files=true,contentIndex=true,online=false)
            val index=lab.index{c};val file=lab.file("three-pages.pdf",lab.asset("real-three-pages.pdf"))
            assertEquals("2efb85528330d78458d9f427466fd2ae7c1990b698879bbca12a28b204f89784",lab.sha(file.readBytes()))
            val pss=lab.pssKiB();val start=SystemClock.elapsedRealtimeNanos()
            val extracted=lab.seed(index,file,"application/pdf",c)
            assertEquals(3,extracted.pages);assertTrue(extracted.fragments.all{it.method=="pdf_text"})
            for(token in listOf("José y María","¿cuándo?","2400 € —"))assertTrue(extracted.text.contains(token))
            DocumentCitationSession(index){c}.use { session ->
                for((query,page) in listOf("presupuesto café" to 1,"reunión martes" to 2,"conclusión técnica" to 3,"cláusula común" to 1)) {
                    val citation=session.resolve(session.retrieve(query).single())!!
                    assertEquals(page,citation.page);assertEquals(extracted.text.substring(citation.start,citation.end),citation.quote)
                    assertEquals(extracted.sourceSha256,citation.sourceSha256)
                    assertTrue(extracted.fragments.any{it.page==page && citation.start>=it.start && citation.end<=it.end})
                }
            }
            // This is an explicit test action. Scroll test below must never invoke this opening.
            val preview=dev.bautista.spotlight.ui.PdfPreviewSession.open(lab.context,Uri.fromFile(file))
            try{assertEquals(3,preview.pageCount);assertTrue(preview.page(1).width>0)}finally{preview.close()}
            JSONObject().put("pages",3).put("elapsed_ns",SystemClock.elapsedRealtimeNanos()-start)
                .put("process_pss_kib_before",pss).put("process_pss_kib_after",lab.pssKiB())
                .put("pss_is_whole_test_process_not_peak_or_model_only",true)
        }
    }
    @Test fun realOcrOnlyWhenExplicitlyEnabledAndResourcesAllow()=runBlocking {
        val lab=C5PhysicalLab()
        lab.runCase("ocr") {
            lab.gate("ocr",lab.args.getString("c5_ocr")=="true","OCR execution not explicitly enabled")
            val resources=DeviceResources.read(lab.target);val budget=OcrBudget.evaluate(resources)
            lab.gate("ocr",budget.deferred==null,"OCR resource gate: "+budget.deferred)
            val c=Config(files=true,contentIndex=true,online=false)
            val index=lab.index{c};val file=lab.file("scan.pdf",lab.asset("ocr-raster-fixture.pdf"))
            val start=SystemClock.elapsedRealtimeNanos();val before=lab.pssKiB()
            val result=DocumentText.extractDetailed(lab.context,Uri.fromFile(file),"application/pdf",
                PdfTextLimits(textPages=1,ocrPages=1),budget={OcrBudget.evaluate(DeviceResources.read(lab.target))})
            lab.report("ocr-extraction",JSONObject(result.metadata()).put("text",result.text))
            assertTrue("OCR was attempted but did not produce a fragment: "+result.issues,result.fragments.any{it.method=="ocr_local"})
            assertTrue(result.text.contains("dosis",ignoreCase=true))
            lab.seed(index,file,"application/pdf",c,result)
            DocumentCitationSession(index){c}.use { session ->
                val citation=session.resolve(session.retrieve("dosis").single())!!
                assertTrue(citation.uncertain);assertEquals(1,citation.page)
                assertEquals(result.text.substring(citation.start,citation.end),citation.quote)
            }
            JSONObject().put("elapsed_ns",SystemClock.elapsedRealtimeNanos()-start).put("pss_kib_before",before)
                .put("pss_kib_after",lab.pssKiB()).put("no_numeric_normalization_performed",true)
        }
    }
    @Test fun documentsUiPersistedGrantMutationBlockingCancelAndRevocation()=runBlocking {
        val lab=C5PhysicalLab()
        lab.runCase("saf") {
            val tree=DocumentsContract.buildTreeDocumentUri(C5FixtureProvider.AUTHORITY,"root")
            lab.gate("saf",lab.args.getString("c5_saf")=="true","Operator-assisted SAF scenario not selected")
            lab.gate("saf",lab.target.contentResolver.persistedUriPermissions.any{it.uri==tree && it.isReadPermission},
                "Select ONLY SPOTLIGHT C5 fixtures in DocumentsUI before this test")
            val c=MutableStateFlow(Config(files=true,contentIndex=true,online=false,trees=setOf(tree.toString())))
            val index=lab.index{c.value};val resolver=lab.target.contentResolver
            fun control(method:String,millis:Long=0)=resolver.call(tree,"c5:$method",null,Bundle().apply{putLong("millis",millis)})!!
            control("reset")
            index.refresh(c.value,false) // Real SAF traversal of only the controlled provider. Broad access disabled.
            val uri=DocumentsContract.buildDocumentUriUsingTree(tree,"doc-01.txt")
            assertTrue(index.cacheExtraction(uri.toString(),DocumentText.extractDetailed(lab.context,uri,"text/plain"),c.value))
            val parent=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
            try { DocumentCitationState(index,c,parent).use { state ->
                val observer=object:ContentObserver(Handler(Looper.getMainLooper())){override fun onChange(selfChange:Boolean){state.invalidate()}}
                resolver.registerContentObserver(tree,true,observer)
                try {
                    suspend fun show():CitationRef {
                        state.query("presupuesto aprobado").join();assertEquals(1,state.visible.value.size)
                        return CitationRef(state.visible.value.single().sourceId)
                    }
                    suspend fun blocked(){withTimeout(1200){while(control("status").getInt("writers")==0)delay(10)}}
                    var effects=0;var ref=show()
                    control("block",2000)
                    val pending=async(Dispatchers.Default){state.withValidated(ref){effects++}}
                    blocked();val cancelAt=SystemClock.elapsedRealtimeNanos();pending.cancel()
                    withTimeout(1200){state.visible.first{it.isEmpty()}}
                    lab.report("saf-cancel-observation",JSONObject().put("clear_ns",SystemClock.elapsedRealtimeNanos()-cancelAt).put("writers_before_release",control("status").getInt("writers")))
                    pending.cancelAndJoin();assertEquals(0,effects);control("block",0)
                    ref=show();control("replace");withTimeout(1200){state.visible.first{it.isEmpty()}}
                    assertFalse(state.withValidated(ref){effects++});control("reset")
                    ref=show();control("delete");withTimeout(1200){state.visible.first{it.isEmpty()}}
                    assertFalse(state.withValidated(ref){effects++});control("reset")
                    ref=show()
                    resolver.call(tree,"c5:unavailable",null,Bundle().apply{putBoolean("enabled",true)})
                    withTimeout(1200){state.visible.first{it.isEmpty()}}
                    assertFalse(state.withValidated(ref){effects++})
                    control("reset")
                    ref=show();control("block",2000)
                    val revoked=async(Dispatchers.Default){state.withValidated(ref){effects++}}
                    blocked()
                    val granted=resolver.persistedUriPermissions.first{it.uri==tree}
                    val revokeFlags=Intent.FLAG_GRANT_READ_URI_PERMISSION or (if(granted.isWritePermission)Intent.FLAG_GRANT_WRITE_URI_PERMISSION else 0)
                    resolver.releasePersistableUriPermission(tree,revokeFlags)
                    state.invalidate() // Explicit access/lifecycle signal; Android promises no universal revoke callback.
                    assertTrue(state.visible.value.isEmpty())
                    try{revoked.await()}catch(_:CancellationException){}
                    assertFalse(resolver.persistedUriPermissions.any{it.uri==tree && it.isReadPermission})
                    assertEquals(0,effects)
                } finally {resolver.unregisterContentObserver(observer)}
            }
            } finally {parent.cancel()}
            JSONObject().put("persisted_grant_removed_only_for_fixture_tree",true)
                .put("already_open_fd_or_transient_grant_may_outlive_persisted_revocation",true)
                .put("controlled_pipe_delay_ms",2000).put("no_personal_sources_accessed",true)
        }
    }
}
