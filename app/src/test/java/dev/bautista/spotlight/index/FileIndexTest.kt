package dev.bautista.spotlight.index

import android.content.ContentValues
import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config as RoboConfig

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk = [28], application = android.app.Application::class)
class FileIndexTest {
    @Test
    fun importedTranscriptIsSearchableAndSurvivesIndexRecreation() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("search-index.db")
        val index = FileIndex(context)
        index.storeTranscript(
            "content://selected/audio/1",
            "reunion.m4a",
            "audio/mp4",
            "En esta reunión hablamos de rehabilitación vestibular",
        )
        val found = FileIndex(context).search("kind:audio \"rehabilitación vestibular\"", Config())
        assertEquals(1, found.size)
        assertEquals("reunion.m4a", found.single().title)
        assertNull(found.single().parentUri)
        assertTrue(index.search("kind:image", Config()).isEmpty())
    }

    @Test
    fun revokedTreeEntriesCannotAppearOrCrowdOutAuthorizedImports() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("search-index.db")
        val index = FileIndex(context)
        index.storeTranscript(
            "content://selected/audio/1",
            "autorizado.m4a",
            "audio/mp4",
            "consulta permitida",
        )
        context.openOrCreateDatabase("search-index.db", 0, null).use { db ->
            val values =
                ContentValues().apply {
                    put("uri", "content://revoked/doc/1")
                    put("tree", "content://revoked/tree/root")
                    put("name", "secreto.pdf")
                    put("mime", "application/pdf")
                    put("modified", 9999999999999L)
                    put("body", "contenido privado")
                }
            db.insert("files", null, values)
        }
        assertTrue(index.search("secreto", Config()).isEmpty())
        assertEquals(1, index.search("permitida", Config()).size)
        assertEquals(
            1,
            index.search("kind:pdf", Config(trees = setOf("content://revoked/tree/root"))).size,
        )
    }
}
