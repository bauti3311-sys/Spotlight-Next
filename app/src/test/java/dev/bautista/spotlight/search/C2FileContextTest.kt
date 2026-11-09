package dev.bautista.spotlight.search

import android.content.*
import android.content.pm.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.index.FileIndex
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class C2FileContextTest {
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private val config=AppConfig(files=true,contacts=false,online=false,learning=false,contentIndex=true)
    @Test fun oldFilenameHitCannotBeCrowdedOutByEightyRecentContentHits()=runBlocking {
        val index=FileIndex(app)
        index.storeTranscript("content://fixture/old","rehabilitación vestibular.pdf","application/pdf","Referencia autorizada")
        repeat(90){index.storeTranscript("content://fixture/new$it","sesion$it.pdf","application/pdf","rehabilitación vestibular")}
        app.openOrCreateDatabase("search-index.db",0,null).use{db->db.execSQL("UPDATE files SET modified = CASE WHEN uri = 'content://fixture/old' THEN 1 ELSE 100 END")}
        val rows=index.search("rehabilitacion vestibular",config)
        assertEquals("rehabilitación vestibular.pdf",rows.first().title)
        assertTrue(rows.size<=80)
    }
    @Test fun shareFilePhraseSelectsLocalFilesInsteadOfLaunchingOrAskingAi() {
        val query=QueryInterpreter.interpret("compartir archivo vestibular")
        assertEquals(QueryIntent.FILES,query.intent)
        assertTrue(SearchQuery.parse(query.text).matches(SearchQuery.Document("vestibular.jpg","image","image/jpeg")))
        assertFalse(SearchQuery.parse(query.text).matches(SearchQuery.Document("otro.jpg","image","image/jpeg")))
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun exactApplicationNamedLikeAFileQueryStillWins()=runBlocking {
        val pkg="fixture.literal";val info=ApplicationInfo().apply{packageName=pkg;enabled=true;nonLocalizedLabel="PDFs de ayer"}
        val activity=ActivityInfo().apply{packageName=pkg;name="Main";applicationInfo=info;enabled=true;exported=true;nonLocalizedLabel="PDFs de ayer"}
        val pm=Shadows.shadowOf(app.packageManager)
        pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=info;activities=arrayOf(activity)})
        pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=activity})
        app.search.invalidateApps()
        val rows=app.search.search("PDFs de ayer",config).last()
        assertEquals("fixture.literal/Main",rows.first().id)
    }
    @Test fun documentsDatesAndUnknownMetadataRemainExplicit() {
        val q=QueryInterpreter.interpret("buscar documento de vestibular")
        assertTrue(SearchQuery.parse(q.text).matches(SearchQuery.Document("apuntes.pdf","document","application/pdf",text="vestibular")))
        assertFalse(SearchQuery.parse(q.text).matches(SearchQuery.Document("vestibular.jpg","image","image/jpeg")))
        val dated=QueryInterpreter.interpret("PDFs de ayer")
        assertNotNull(dated.files!!.start)
        assertFalse(SearchQuery.parse(dated.text).matches(SearchQuery.Document("ayer.pdf","document","application/pdf",modified=0)))
    }
    @Test fun quotedAndScopedShareQueriesPreserveLiteralNamesAndOperation() {
        for(scope in listOf(null,dev.bautista.spotlight.control.SearchScope("","files-mode"))) {
            val q=QueryInterpreter.interpret("compartir archivo \"vestibular 2026\"",scope)
            assertTrue(q.shareFiles)
            assertTrue(SearchQuery.parse(q.text).matches(SearchQuery.Document("vestibular 2026.png","image","image/png")))
            assertFalse(SearchQuery.parse(q.text).matches(SearchQuery.Document("vestibular otro 2026.png","image","image/png")))
            assertTrue(QueryInterpreter.interpret("compartir archivos",scope).shareFiles)
        }
    }

}
