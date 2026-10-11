package dev.bautista.spotlight.index

import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config as RoboConfig

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk = [33], application = android.app.Application::class)
class SharedStorageTest {
    @Test
    fun sharedFilesWithoutSelectedFoldersRespectRevocationAndDeletion() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("search-index.db")
        org.robolectric.Shadows.shadowOf(android.webkit.MimeTypeMap.getSingleton()).addExtensionMimeTypeMapping("jpg","image/jpeg")
        val root = java.nio.file.Files.createTempDirectory("spotlight-shared").toFile()
        try {
            java.io.File(root, "Download").mkdirs()
            val document = java.io.File(root, "Download/informe.pdf").apply { writeText("fixture") }
            java.io.File(root, "Android/data/private").mkdirs()
            java.io.File(root, "Android/data/private/secreto.pdf").writeText("private")
            java.io.File(root,"Android/media/com.whatsapp/WhatsApp/Media").mkdirs()
            java.io.File(root,"Android/media/com.whatsapp/WhatsApp/Media/IMG-123.jpg").writeText("fixture")
            java.io.File(root,"Pictures").mkdirs()
            java.io.File(root,"Pictures/IMG-456.jpg").writeText("fixture")
            var allowed = true
            val index = FileIndex(context, { allowed }, { listOf(root) })
            index.ensure(Config())
            withTimeout(10000) { index.revision.first { it > 0 } }
            val found = index.search("kind:pdf informe", Config())
            assertEquals(1, found.size)
            val scoped=index.search("kind:image",Config(),"whatsapp")
            assertEquals(listOf("IMG-123.jpg"),scoped.map {it.title})
            assertEquals(2,index.search("kind:image",Config()).size)
            assertEquals("content", (found.single().action as Action.Launch).intent.data!!.scheme)
            assertTrue(index.search("secreto", Config()).isEmpty())
            allowed = false
            assertTrue(index.search("informe", Config()).isEmpty())
            allowed = true
            document.delete()
            index.refresh(Config(), false)
            assertTrue(index.search("informe", Config()).isEmpty())
        } finally {
            root.deleteRecursively()
        }
    }
}
