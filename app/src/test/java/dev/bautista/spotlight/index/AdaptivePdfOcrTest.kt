package dev.bautista.spotlight.index

import android.app.Application
import android.graphics.Bitmap
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class AdaptivePdfOcrTest {
    private class Pages(override val pages:Int=2):PdfPageSource {
        val images=mutableListOf<Bitmap>();val limits=mutableListOf<PdfTextLimits>()
        var page=0;var afterRender:()->Unit={}
        override fun text(page:Int)=""
        override fun render(page:Int,limits:PdfTextLimits):Bitmap {
            this.page=page;this.limits+=limits
            val (w,h)=limits.dimensions(595,842)
            return Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888).also{images+=it;afterRender()}
        }
        override fun close(){}
    }
    private suspend fun extract(source:Pages,budget:()->OcrBudgetDecision,previous:CachedPdfText?=null,sha:String="a".repeat(64),recognize:suspend()->String={"Texto reconocido página ${source.page+1}"}):ExtractedDocument {
        val lease=OcrWorkLease{}
        return try{PdfTextPipeline.extract(source,PdfTextLimits(),sha,lease,start=0,clock={0},budget=budget,previous=previous){image,done->try{recognize()}finally{image.recycle();done()}}}finally{lease.close()}
    }
    @Test fun blockedPagesNeverRenderOrBecomeRecognizedFragments()=runBlocking {
        val pages=Pages()
        val result=extract(pages,{OcrBudgetDecision(OcrDeferral.POWER_SAVE,pages=0)}){fail("No recognition while deferred");""}
        assertTrue(result.partial);assertTrue(result.fragments.isEmpty());assertEquals(0,result.ocrAttempts)
        assertEquals("Omitted pages have not even been examined",0,result.examined)
        assertEquals(listOf(1,2),result.issues.map{it.page});assertTrue(pages.images.isEmpty())
    }
    @Test fun pressureAfterRenderRecyclesFrameWithoutStartingNativeRecognition()=runBlocking {
        val pages=Pages(1);var pressured=false
        pages.afterRender={pressured=true}
        val result=extract(pages,{if(pressured)OcrBudgetDecision(OcrDeferral.MEMORY,pages=0) else OcrBudgetDecision()}){fail("Memory pressure must stop admission");""}
        assertTrue(pages.images.single().isRecycled);assertTrue(result.fragments.isEmpty())
        assertEquals(listOf("memory"),result.deferrals)
    }
    @Test fun resumeReusesOnlyAlreadyRecognizedPagesFromIdenticalBytes()=runBlocking {
        val first=Pages();var calls=0
        val partial=extract(first,{if(calls>0)OcrBudgetDecision(OcrDeferral.THERMAL,pages=0) else OcrBudgetDecision()}){calls++;"Primer fragmento reconocido"}
        assertEquals(1,partial.fragments.size);assertTrue(partial.partial)
        val second=Pages()
        val resumed=extract(second,{OcrBudgetDecision()},CachedPdfText(partial.text,partial.metadata())){calls++;"Segundo fragmento recuperado"}
        assertEquals(2,calls);assertEquals(1,resumed.ocrAttempts);assertEquals(1,second.images.size)
        assertFalse(resumed.partial);assertEquals(listOf(1,2),resumed.fragments.map{it.page})
        assertTrue(resumed.text.contains("Primer fragmento"));assertTrue(resumed.text.contains("Segundo fragmento"))
    }
    @Test fun changedBytesOrTruncatedOldRecognitionCannotBeReused()=runBlocking {
        val old=extract(Pages(1),{OcrBudgetDecision()})
        val next=Pages(1)
        extract(next,{OcrBudgetDecision()},CachedPdfText(old.text,old.metadata()),"b".repeat(64))
        assertEquals(1,next.images.size)
        val truncated=old.copy(issues=listOf(ExtractionIssue("character_limit",1)))
        val another=Pages(1)
        extract(another,{OcrBudgetDecision()},CachedPdfText(truncated.text,truncated.metadata()))
        assertEquals(1,another.images.size)
        val unknownVersion=org.json.JSONObject(old.metadata()).put("version",999).toString()
        val unknown=Pages(1)
        extract(unknown,{OcrBudgetDecision()},CachedPdfText(old.text,unknownVersion))
        assertEquals(1,unknown.images.size)
    }
    @Test fun reducedBudgetBoundsAllocationAndLeavesRemainingPagesPending()=runBlocking {
        val pages=Pages()
        val result=extract(pages,{OcrBudgetDecision(pages=1,longEdge=1024,pixels=786432,pageMillis=8000)})
        assertEquals(1,result.ocrAttempts);assertEquals(1,result.fragments.size)
        assertTrue(result.issues.any{it.page==2 && it.code=="ocr_deferred:reduced"})
        val image=pages.images.single()
        assertTrue(image.width<=1024 && image.height<=1024);assertTrue(image.width.toLong()*image.height<=786432)
        assertTrue(image.isRecycled)
    }
}
