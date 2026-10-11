package dev.bautista.spotlight.personal

import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.Action
import java.time.LocalDateTime
import org.junit.Assert.*
import org.junit.Test

class PersonalCommandsTest {
    @Test fun aliasesPreserveArgumentsAndExpandNestedTargets() {
        assertEquals("youtube música para estudiar",PersonalCommands.expand("ESTUDIO para estudiar",setOf("estudio|musica","musica|youtube música")))
        assertEquals("whatsapp a Juan: Hola Juan",PersonalCommands.expand("juan Hola Juan",setOf("juan|whatsapp a Juan:")))
        assertEquals("desconocido hola",PersonalCommands.expand("desconocido hola",setOf("juan|whatsapp a Juan:")))
    }
    @Test fun cyclesAndExcessiveChainsNeverProducePartialExpansion() {
        assertEquals("a texto",PersonalCommands.expand("a texto",setOf("a|b","b|a")))
        assertEquals("a",PersonalCommands.expand("a",setOf("a|b","b|c","c|d","d|e","e|f","f|youtube")))
        assertFalse(PersonalCommands.validAlias("dos palabras"))
        assertTrue(PersonalCommands.validAlias("mañana_1"))
    }
    @Test fun snippetsRenderDateAndTimeAndPreserveLiteralSeparators() {
        assertEquals("Firma | 10/10/2026 14:07 {otro}",PersonalCommands.render("Firma | {fecha} {hora} {otro}",LocalDateTime.of(2026,10,10,14,7)))
        val result=PersonalCommands.results("snippet firma",Config(snippets=setOf("firma|Hola | Bautista"))).single()
        assertEquals(Action.Copy("Hola | Bautista"),result.action)
        assertEquals("Hola | Bautista",result.preview)
    }
    @Test fun pinsAndHelpOnlyLoadQueries() {
        assertEquals(Action.RestoreQuery("temporizador de 25 minutos"),PersonalCommands.results("",Config(pins=setOf("temporizador de 25 minutos"))).single().action)
        val help=PersonalCommands.results("ayuda",Config())
        assertTrue(help.isNotEmpty());assertTrue(help.all {it.action is Action.RestoreQuery})
        assertEquals(help,PersonalCommands.results("comandos",Config()))
    }
}
