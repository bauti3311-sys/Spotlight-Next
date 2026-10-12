package dev.bautista.spotlight.index

import android.app.Application
import android.content.ContentValues
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.*
import java.time.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config as RoboConfig

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[28],application=Application::class)
class FileDateIndexTest {
    @Test fun dateQueryReturnsRealMetadataAndPrunesSqlCandidates()=runBlocking {
        val app=RuntimeEnvironment.getApplication();app.deleteDatabase("search-index.db")
        val index=FileIndex(app);index.storeTranscript("content://files/unknown","ayer.pdf","application/pdf","")
        val file=QueryInterpreter.files("PDF de ayer",LocalDate.of(2026,10,12),ZoneId.of("America/Argentina/Buenos_Aires"))!!
        val start=file.start!!
        app.openOrCreateDatabase("search-index.db",0,null).use{db->
            db.beginTransaction();try{for(i in 0 until 3000){db.insert("files",null,ContentValues().apply{put("uri","content://files/$i");put("tree","__imports__");put("name","documento$i.pdf");put("mime","application/pdf");put("modified",if(i<10)start+i else start-86400000);put("created",0);put("body","")})};db.setTransactionSuccessful()}finally{db.endTransaction()}
            val filter=FileSqlFilter.from(file.query)
            val fullStart=System.nanoTime();val all=db.rawQuery("SELECT uri FROM files WHERE tree=?",arrayOf("__imports__")).use{c->var n=0;while(c.moveToNext())n++;n};val fullNanos=System.nanoTime()-fullStart
            val sqlStart=System.nanoTime();val candidates=db.rawQuery("SELECT uri FROM files WHERE tree=? AND ("+filter.selection+")",(listOf("__imports__")+filter.args).toTypedArray()).use{c->var n=0;while(c.moveToNext())n++;n};val sqlNanos=System.nanoTime()-sqlStart
            assertEquals(3001,all);assertEquals(10,candidates)
            println("Index fixture: previous candidates=$all, filtered candidates=$candidates; cursor-only previousNs=$fullNanos, filteredNs=$sqlNanos (Robolectric, not device benchmark)")
        }
        val results=index.search(file.query,Config())
        assertEquals(10,results.size);assertTrue(results.none{it.title=="ayer.pdf"})
        assertTrue(results.all{it.modifiedAt!!>=start && it.modifiedAt!!<file.end!!})
    }
}
