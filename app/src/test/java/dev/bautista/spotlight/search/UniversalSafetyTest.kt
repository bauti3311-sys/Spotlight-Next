package dev.bautista.spotlight.search

import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.control.SearchScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.toList
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class UniversalSafetyTest {
    @Test fun consentUsesTypedProvenanceNotNewlinesOrRowIdentity(){
        val privateSingleLine=Action.Ask("private text",origin=AiInputOrigin.PRIVATE_CONTEXT)
        assertTrue(AiRequestPolicy.requiresReview(privateSingleLine))
        assertFalse(AiRequestPolicy.requiresReview(AiRequestPolicy.question("pregunta\nen dos líneas")))
        val frozen=AiRequestPolicy.prepare(privateSingleLine.copy(prompt="a".repeat(25000)))
        assertEquals(24000,frozen.prompt.length)
        assertEquals(frozen.prompt,AiRequestPolicy.authorize(frozen).prompt)
        assertFalse(AiRequestPolicy.requiresReview(AiRequestPolicy.authorize(frozen)))
    }
    @Test fun openedYesterdayNeverTurnsIntoModificationFilter()=runBlocking {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        for(scope in listOf(null,SearchScope("","files-mode"))) {
            val rows=app.search.search("Buscá el PDF de anatomía que abrí ayer",AppConfig(files=false),scope).toList().last()
            assertEquals("files:opened-unavailable",rows.single().id)
            val action=rows.single().action as Action.RestoreQuery
            assertEquals("kind:pdf anatomia",action.query)
            assertTrue(rows.single().subtitle.contains("No se filtra por modificación"))
        }
        val modified=QueryInterpreter.files("PDF de anatomía modificados ayer")!!
        assertEquals("modified",modified.dateField)
        assertTrue(modified.query.contains("modified-after:"))
    }
    @Test fun commandsRetainNativeNoteCreation()=runBlocking {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        val rows=app.search.search("",AppConfig(files=false),SearchScope("","commands")).toList().last()
        assertTrue(rows.joinToString { it.title },rows.any{it.title=="Crear nota" && it.action is Action.NewNote})
    }
    @Test fun chapterThreeIsBoundedAndCannotMatchThirty(){
        val text="Capítulo 30\nwrong\nCapítulo 3\ncorrect\nCapítulo 4\nprivate next chapter"
        val found=DocumentSectionLocator.locate(text,"capítulo 3")
        assertTrue(found.matchedSection)
        assertEquals("Capítulo 3\ncorrect",found.excerpt)
        assertFalse(DocumentSectionLocator.locate("Capítulo 30\nwrong","capítulo 3").matchedSection)
    }
}
