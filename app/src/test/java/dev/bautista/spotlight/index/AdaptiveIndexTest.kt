package dev.bautista.spotlight.index

import android.app.Application
import android.graphics.Bitmap
import dev.bautista.spotlight.control.DeviceResourceState
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.security.MessageDigest

/** Actual refresh -> PDF pipeline -> SQLite -> search, with recognizer/device fixtures only. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class AdaptiveIndexTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private val config=AppConfig(contentIndex=true)
    private val healthy=DeviceResourceState(80,false,false,1024L*1024*1024,64L*1024*1024,false,0)
    private lateinit var root:java.io.File
    @Before fun setup(){context.deleteDatabase("search-index.db");root=java.nio.file.Files.createTempDirectory("adaptive-pdf").toFile();java.io.File(root,"adaptativo.pdf").writeText("frozen PDF fixture")}
    @After fun cleanup(){root.deleteRecursively()}
    private inner class Harness {
        @Volatile var state=healthy.copy(batteryPercent=25)
        @Volatile var allowed=true
        @Volatile var hold=false
        @Volatile var revoke=false
        var calls=0
        val started=CompletableDeferred<Unit>();val released=CompletableDeferred<Unit>()
        fun index()=FileIndex(context,{allowed},{listOf(root)},{state},pdfExtraction={_,limits,budget,previous->
            var current=0
            val pages=object:PdfPageSource {
                override val pages=2
                override fun text(page:Int)=""
                override fun render(page:Int,limits:PdfTextLimits)=Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888).also{current=page}
                override fun close(){}
            }
            val sha=MessageDigest.getInstance("SHA-256").digest(java.io.File(root,"adaptativo.pdf").readBytes()).joinToString(""){"%02x".format(it)}
            val lease=OcrWorkLease{}
            try{PdfTextPipeline.extract(pages,limits,sha,lease,start=0,clock={0},budget=budget,previous=previous){image,done->
                try {calls++;if(hold){started.complete(Unit);released.await()};if(revoke)allowed=false
                    if(current==0)"Vestibularconservado primera página" else "Equilibriorecuperado segunda página"
                } finally{image.recycle();done()}
            }}finally{lease.close()}
        })
    }
    @Test fun reducedThenPausedThenRecoveredSurvivesIndexRecreationWithoutRepeatingOcr()=runBlocking {
        val h=Harness();var index=h.index()
        index.refresh(config)
        assertEquals(1,h.calls);assertEquals(1,index.search("Vestibularconservado",config).size)
        assertTrue(index.status.value.message.contains("presupuesto reducido"))
        context.openOrCreateDatabase("search-index.db",0,null).use{db->db.rawQuery("SELECT mode,extraction FROM files",null).use{row->
            assertTrue(row.moveToFirst());assertTrue(row.getString(0).endsWith(":pending"))
            val metadata=org.json.JSONObject(row.getString(1));assertEquals(1,metadata.getJSONArray("fragments").length())
            assertTrue(metadata.getJSONArray("issues").toString().contains("ocr_deferred:reduced"))
        }}
        h.state=healthy.copy(powerSave=true);index=h.index();index.refresh(config)
        assertEquals(1,h.calls);assertTrue(index.search("Vestibularconservado",config).single().preview!!.contains("ahorro de energía"))
        assertTrue(index.status.value.message.contains("ahorro de energía"))
        h.state=healthy;index=h.index();index.refresh(config)
        assertEquals(2,h.calls);assertEquals(1,index.search("Equilibriorecuperado",config).size)
        assertFalse(index.status.value.truncated)
        assertFalse(index.search("Vestibularconservado",config).single().preview!!.contains("aplazado"))
    }
    @Test fun searchAndEnsureRemainAvailableWhileOcrIsSuspendedAndCancellationKeepsOldText()=runBlocking {
        val h=Harness();val index=h.index();index.refresh(config)
        h.state=healthy;h.hold=true
        val job=launch(Dispatchers.Default){index.refresh(config)}
        withTimeout(5000){h.started.await()}
        try {
            withTimeout(2000){index.ensure(config);assertEquals(1,index.search("Vestibularconservado",config).size)}
            index.cancelRefresh();job.cancelAndJoin()
            assertEquals(1,index.search("Vestibularconservado",config).size)
            assertTrue(index.search("Equilibriorecuperado",config).isEmpty())
        } finally{index.cancelRefresh();job.cancelAndJoin();h.released.complete(Unit)}
        h.hold=false;index.refresh(config)
        assertEquals(1,index.search("Equilibriorecuperado",config).size)
    }
    @Test fun revokedAccessPreventsLateCommitAndPrivateRefreshDoesNotReadOrErase()=runBlocking {
        val h=Harness();val index=h.index();index.refresh(config)
        val before=h.calls
        index.refresh(config.copy(privateMode=true));assertEquals(before,h.calls)
        assertEquals(1,index.search("Vestibularconservado",config).size)
        h.state=healthy;h.revoke=true;index.refresh(config)
        assertTrue(index.search("Vestibularconservado",config).isEmpty())
        h.allowed=true;h.revoke=false
        assertEquals(1,index.search("Vestibularconservado",config).size)
        assertTrue(index.search("Equilibriorecuperado",config).isEmpty())
        index.refresh(config);assertEquals(1,index.search("Equilibriorecuperado",config).size)
    }
    @Test fun changedFileDeferredKeepsLastKnownTextButClearlyLabelsItStale()=runBlocking {
        val h=Harness();val index=h.index();index.refresh(config)
        java.io.File(root,"adaptativo.pdf").writeText("changed PDF fixture with different size")
        h.state=healthy.copy(lowMemory=true);index.refresh(config)
        val result=index.search("Vestibularconservado",config).single()
        assertTrue(result.preview!!.contains("Versión anterior indexada"))
        assertTrue(result.preview!!.contains("presión de memoria"));assertEquals(1,h.calls)
    }
}
