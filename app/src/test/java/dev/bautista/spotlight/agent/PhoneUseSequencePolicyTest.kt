package dev.bautista.spotlight.agent

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PhoneUseSequencePolicyTest {
    @Test fun parsesStrictConfirmedSteps() {
        val steps = PhoneUseSequencePolicy.parse(
            """[{"op":"tap","selector":"text:Buscar","expect":"id:com.spotify.music:id/query"},
                {"op":"type","selector":"id:com.spotify.music:id/query","text":"Coldplay"}]"""
        )
        assertEquals(2, steps.size)
        assertEquals("tap", steps[0].operation)
        assertEquals("id:com.spotify.music:id/query", steps[0].expected)
        assertEquals("Coldplay", steps[1].text)
    }

    @Test fun rejectsUnknownOperationsAndUnexpectedFields() {
        listOf(
            """[{"op":"shell","selector":"text:Buscar","expect":"text:Listo"}]""",
            """[{"op":"tap","selector":"text:Buscar"}]""",
            """[{"op":"tap","selector":"text:Buscar","expect":"text:Buscar"}]""",
            """[{"op":"tap","selector":"tap:500,200","expect":"text:Listo"}]""",
            """[{"op":"tap","selector":"text:Buscar","expect":"text:Listo","shell":"rm"}]""",
            """[{"op":"type","selector":"text:Campo","text":""}]""",
            """[{"op":"type","selector":"text:Campo","text":"Hola","expect":"text:Listo"}]""",
            "[]",
            """[{"op":"tap","selector":"text:a","expect":"text:b"},
                {"op":"tap","selector":"text:b","expect":"text:c"},
                {"op":"tap","selector":"text:c","expect":"text:d"},
                {"op":"tap","selector":"text:d","expect":"text:e"},
                {"op":"tap","selector":"text:e","expect":"text:f"}]"""
        ).forEach { input ->
            assertThrows(Exception::class.java) { PhoneUseSequencePolicy.parse(input) }
        }
    }

    @Test fun postconditionMustRepresentObservedTransition() {
        val step = PhoneUseStep("tap","text:Buscar",expected="text:Resultados")
        assertTrue(PhoneUseSequencePolicy.matches(step,setOf("text:Buscar"),setOf("text:Resultados")))
        assertFalse(PhoneUseSequencePolicy.matches(step,setOf("text:Buscar","text:Resultados"),setOf("text:Resultados")))
        assertFalse(PhoneUseSequencePolicy.matches(step,setOf("text:Buscar"),setOf("text:Buscar")))
        assertFalse(PhoneUseSequencePolicy.matches(step,setOf("text:Otra"),setOf("text:Resultados")))
    }
}
