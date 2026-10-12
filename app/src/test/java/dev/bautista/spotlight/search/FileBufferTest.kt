package dev.bautista.spotlight.search

import android.content.Intent
import android.net.Uri
import dev.bautista.spotlight.data.FileBuffer
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[26,35])
class FileBufferTest {
    private fun row(id:Int)=Result("file:$id","$id.pdf","","Archivos",1,Action.Launch(Intent(Intent.ACTION_VIEW,Uri.parse("content://files/$id"))),mime="application/pdf")
    @Test fun threeFilesStaySelectedAndDuplicateUrisDoNotMultiply(){
        val b=FileBuffer();(1..3).forEach{assertTrue(b.add(row(it)))}
        assertTrue(b.add(row(2).copy(id="duplicate")));assertEquals(3,b.items.size)
        QueryInterpreter.interpret("otra consulta");assertEquals(3,b.items.size)
        b.remove("file:2");assertEquals(listOf("file:1","file:3"),b.items.map{it.id})
        b.clear();assertTrue(b.items.isEmpty())
    }
    @Test fun shareUsesAndroidMultipleIntentWithEveryUriAndReadGrant(){
        val b=FileBuffer();(1..3).forEach{b.add(row(it))};val intent=b.share()
        assertEquals(Intent.ACTION_SEND_MULTIPLE,intent.action);assertEquals("application/pdf",intent.type)
        @Suppress("DEPRECATION") val uris=intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)!!
        assertEquals(3,uris.size);assertEquals(3,intent.clipData!!.itemCount)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION!=0)
    }
    @Test fun foldersAppsUnsupportedSchemesAndUnboundedSelectionsAreRejected(){
        val b=FileBuffer();assertFalse(b.add(row(1).copy(category="Aplicaciones")))
        assertFalse(b.add(row(1).copy(mime=android.provider.DocumentsContract.Document.MIME_TYPE_DIR)))
        assertFalse(b.add(row(1).copy(action=Action.Launch(Intent(Intent.ACTION_VIEW,Uri.parse("https://example.com"))))))
        (1..12).forEach{assertTrue(b.add(row(it)))};assertFalse(b.add(row(13)));assertEquals(12,b.items.size)
    }
}
