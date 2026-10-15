package dev.bautista.spotlight.ui

import org.junit.Assert.*
import org.junit.Test

class AiMarkdownTest {
    @Test fun headingsListsAndIncompleteStreamingCodeStayReadable() {
        val blocks=markdownBlocks("# Título\n- Primer punto\n```kotlin\nval x = 2")
        assertEquals(1,blocks[0].heading);assertEquals("Título",blocks[0].text)
        assertEquals("• Primer punto",blocks[1].text)
        assertTrue(blocks[2].code);assertEquals("val x = 2",blocks[2].text)
    }
    @Test fun emphasisHasNativeSpansAndDoesNotLoadLinksOrHtml() {
        val text=markdownInline("**Foco** y `código` <img src='https://example.invalid'>")
        assertEquals("Foco y código <img src='https://example.invalid'>",text.text)
        assertEquals(2,text.spanStyles.size);assertTrue(text.getStringAnnotations(0,text.length).isEmpty())
    }
}
