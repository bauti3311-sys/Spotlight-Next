package dev.bautista.spotlight.ui

import android.provider.DocumentsContract
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import org.junit.Assert.*
import org.junit.Test

class PreviewPresentationTest {
    private fun result(category:String,mime:String?=null,preview:String?=null)=
        Result("test","Resultado","Descripción",category,0,Action.Copy("4"),mime=mime,preview=preview)
    @Test fun calculatorApplicationAndCommandsNeverDuplicateTheirValue() {
        listOf("Calculadora","Aplicaciones","Acciones").forEach { assertFalse(PreviewPresentation.needsInspector(result(it,preview="4"))) }
        assertFalse(PreviewPresentation.needsInspector(null))
    }
    @Test fun pdfAndDocumentsKeepAutomaticQuickLook() {
        assertTrue(PreviewPresentation.needsInspector(result("Archivos","application/pdf")))
        assertTrue(PreviewPresentation.needsInspector(result("Archivos","text/plain")))
        assertFalse(PreviewPresentation.needsInspector(result("Archivos",DocumentsContract.Document.MIME_TYPE_DIR)))
    }
    @Test fun shortNotesStayInListAndLongContentCanExpand() {
        assertFalse(PreviewPresentation.needsInspector(result("Notas",preview="Nota breve")))
        assertTrue(PreviewPresentation.needsInspector(result("Notas",preview="a".repeat(161))))
    }
}
