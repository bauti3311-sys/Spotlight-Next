package dev.bautista.spotlight.index

import android.app.Application
import android.content.ContentValues
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class FileIndexUpgradeTest {
    private val context get()=RuntimeEnvironment.getApplication()
    @Test fun versionThreeCacheKeepsDocumentsAndExtractionDuringAdditiveUpgrade()=runBlocking {
        context.openOrCreateDatabase("search-index.db",0,null).use{db->
            db.execSQL("CREATE TABLE files(uri TEXT PRIMARY KEY,tree TEXT,parent TEXT,name TEXT,mime TEXT,modified INTEGER,size INTEGER,author TEXT,created INTEGER,body TEXT,mode TEXT,extraction TEXT NOT NULL DEFAULT '')")
            db.version=3
            repeat(90){n->db.insertOrThrow("files",null,ContentValues().apply{
                put("uri","content://fixture/$n");put("tree","__imports__");put("parent","content://fixture/parent")
                put("name",if(n==0)"rehabilitación vestibular.pdf" else "sesión$n.pdf");put("mime","application/pdf")
                put("modified",if(n==0)1L else 100L);put("size",100L);put("author","Autor local");put("created",1L)
                put("body",if(n==0)"Contenido conservado" else "rehabilitación vestibular");put("mode","transcript");put("extraction","")
            })}
        }
        val result=FileIndex(context).search("rehabilitacion vestibular",AppConfig(contentIndex=true))
        assertEquals("rehabilitación vestibular.pdf",result.first().title)
        context.openOrCreateDatabase("search-index.db",0,null).use{db->
            assertEquals(4,db.version)
            db.rawQuery("SELECT uri,parent,body,author,created,size FROM files WHERE uri=?",arrayOf("content://fixture/0")).use{row->
                assertTrue(row.moveToFirst());assertEquals("content://fixture/0",row.getString(0))
                assertEquals("content://fixture/parent",row.getString(1));assertEquals("Contenido conservado",row.getString(2))
                assertEquals("Autor local",row.getString(3));assertEquals(1L,row.getLong(4));assertEquals(100L,row.getLong(5))
            }
            db.rawQuery("SELECT COUNT(*) FROM files",null).use{it.moveToFirst();assertEquals(90,it.getInt(0))}
        }
    }
    @Test fun literalLikeCharactersNeverBecomeSqlWildcards()=runBlocking {
        val index=FileIndex(context)
        index.storeTranscript("content://fixture/yes","100%_vestibular.pdf","application/pdf","")
        index.storeTranscript("content://fixture/no","100x_vestibular.pdf","application/pdf","")
        assertEquals(listOf("100%_vestibular.pdf"),index.search("100%_",AppConfig()).map{it.title})
    }
}
