package dev.bautista.spotlight.index

import android.app.Application
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Real SQLite search/cache regression; document contents and hashes are local fixtures. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class DocumentCacheCoverageTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val config = AppConfig(contentIndex = true)
    private val uri = "content://selected/coverage/pdf"
    @Before fun clear() { context.deleteDatabase("search-index.db") }

    private fun document(parts: List<String>, hash: String = "a".repeat(64), partial: Boolean = false, method: String = "pdf_text"): ExtractedDocument {
        var offset = 0
        val fragments = parts.mapIndexed { i, text ->
            TextFragment(i + 1, method, offset, offset + text.length).also { offset += text.length + 1 }
        }
        return ExtractedDocument(parts.joinToString("\n"), fragments,
            if (partial) listOf(ExtractionIssue("character_limit", parts.size)) else emptyList(),
            pages = 3, examined = parts.size, ocrAttempts = if (method == "ocr_local") parts.size else 0, sourceSha256 = hash)
    }
    private suspend fun index(): FileIndex = FileIndex(context).also {
        it.storeTranscript(uri, "cobertura.pdf", "application/pdf", "")
    }

    @Test fun shorterManualExcerptOfSameBytesDoesNotEraseIndexedPages() = runBlocking {
        val index = index()
        val first = "Introducción vestibular suficientemente larga para este documento de prueba."
        assertTrue(index.cacheExtraction(uri, document(listOf(first, "Contenido intermedio", "Continuidadterapeutica exclusiva al final")), config))
        val revision = index.revision.value
        assertTrue(index.cacheExtraction(uri, document(listOf(first.take(24)), partial = true), config))
        assertEquals(1, index.search("Continuidadterapeutica", config).size)
        assertEquals("Preserving equivalent richer coverage must not restart search", revision, index.revision.value)
    }

    @Test fun changedSourceBytesReplaceOldTextEvenWhenNewExtractionIsPartial() = runBlocking {
        val index = index()
        index.cacheExtraction(uri, document(listOf("Contenidoanteriorexclusivo", "Otra página")), config)
        assertTrue(index.cacheExtraction(uri, document(listOf("Contenidocambiadoactual"), "b".repeat(64), partial = true), config))
        assertTrue(index.search("Contenidoanteriorexclusivo", config).isEmpty())
        assertEquals(1, index.search("Contenidocambiadoactual", config).size)
    }

    @Test fun broaderExtractionOfSameSourceUpdatesSearchAndProvenance() = runBlocking {
        val index = index()
        index.cacheExtraction(uri, document(listOf("Introducción"), partial = true), config)
        assertTrue(index.cacheExtraction(uri, document(listOf("Introducción ampliada", "Segunda página", "Equilibrioposterior reconocido")), config))
        val result = index.search("Equilibrioposterior", config).single()
        assertTrue(result.preview!!.contains("página 3"))
    }

    @Test fun missingOrInvalidOldProvenanceCannotBlockAnAuthorizedRefresh() = runBlocking {
        val index = index()
        index.storeTranscript(uri, "cobertura.pdf", "application/pdf", "Textoobsoletolegacy")
        context.openOrCreateDatabase("search-index.db", 0, null).use {
            it.execSQL("UPDATE files SET extraction=? WHERE uri=?", arrayOf("{invalid", uri))
        }
        assertTrue(index.cacheExtraction(uri, document(listOf("Textovigenteautorizado")), config))
        assertTrue(index.search("Textoobsoletolegacy", config).isEmpty())
        assertEquals(1, index.search("Textovigenteautorizado", config).size)
    }

    @Test fun correctedOcrIsNotDiscardedJustBecauseAnOldFragmentWasLonger() = runBlocking {
        val index = index()
        index.cacheExtraction(uri, document(listOf("Equilidrio con error previo y texto más largo"), partial = true, method = "ocr_local"), config)
        assertTrue(index.cacheExtraction(uri, document(listOf("Equilibriocorregido"), partial = true, method = "ocr_local"), config))
        assertTrue(index.search("Equilidrio", config).isEmpty())
        assertEquals(1, index.search("Equilibriocorregido", config).size)
    }
}
