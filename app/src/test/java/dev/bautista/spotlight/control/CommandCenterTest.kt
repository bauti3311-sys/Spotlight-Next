package dev.bautista.spotlight.control

import dev.bautista.spotlight.search.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = android.app.Application::class)
class CommandCenterTest {
    private fun app(id: String, title: String) =
        Result(id, title, "Abrir aplicación", "Aplicaciones", 0, Action.Copy(title))

    @Test
    fun fiveRecentThenEveryAppAlphabeticallyWithoutLosingDuplicates() {
        val apps =
            listOf(
                app("z", "Zulu"),
                app("b", "Beta"),
                app("a", "Álbum"),
                app("g", "Gamma"),
                app("d", "Delta"),
                app("e", "Eco"),
                app("f", "Foto"),
            )
        val actual =
            CommandCenter.orderedApps(apps, listOf("missing", "z", "z", "g", "b", "f", "d", "a"))
        assertEquals(listOf("z", "g", "b", "f", "d"), actual.take(5).map { it.id })
        assertEquals(listOf("a", "b", "d", "e", "f", "g", "z"), actual.drop(5).map { it.id })
        assertEquals(12, actual.size)
    }

    @Test
    fun arrowsCrossRecentSectionWithoutShiftingColumns() {
        assertEquals(5, CommandCenter.moveGrid(0, true, 6, 5, 30))
        assertEquals(9, CommandCenter.moveGrid(4, true, 6, 5, 30))
        assertEquals(0, CommandCenter.moveGrid(5, false, 6, 5, 30))
        assertEquals(4, CommandCenter.moveGrid(10, false, 6, 5, 30))
        assertEquals(3, CommandCenter.moveGrid(0, true, 3, 5, 30))
        assertEquals(6, CommandCenter.moveGrid(4, true, 3, 5, 30))
    }

    @Test
    fun suggestionsPreserveTypedPrefixAndDoNotCompleteUnrelatedText() {
        assertEquals("youtUbe ", CommandCenter.complete("youtU", emptyList()))
        assertEquals("WhatSApp", CommandCenter.complete("WhatS", listOf(app("wa", "WhatsApp"))))
        assertNull(CommandCenter.complete("", emptyList()))
        assertNull(CommandCenter.complete("texto que no es un comando", emptyList()))
    }

    @Test
    fun youtubeQueryAndWhatsappMessageAreEncodedWithoutLosingText() {
        val youtube = CommandCenter.commands("youtube rock & roll").single().action as Action.Launch
        assertEquals("rock & roll", youtube.intent.data!!.getQueryParameter("search_query"))
        val preferred = CommandCenter.preferred(youtube.intent)!!
        assertEquals("com.google.android.youtube", preferred.`package`)
        assertEquals("rock & roll", preferred.getStringExtra(android.app.SearchManager.QUERY))
        val whatsapp =
            CommandCenter.commands("whatsapp +5492231234567: hola & chau").single().action
                as Action.Launch
        assertEquals("hola & chau", whatsapp.intent.data!!.getQueryParameter("text"))
        assertEquals("/5492231234567", whatsapp.intent.data!!.path)
    }

    @Test
    fun sofascoreHasNativeSearchAndAWorkingSearchFallbackInsteadOfMissingRoute() {
        val action =
            CommandCenter.commands("sofascore Boca Juniors").single().action as Action.InApp
        assertEquals("com.sofascore.results", action.intent.`package`)
        assertEquals("Boca Juniors", action.intent.getStringExtra(android.app.SearchManager.QUERY))
        assertEquals("www.google.com", action.fallback.data!!.host)
        assertTrue(action.fallback.data!!.getQueryParameter("q")!!.contains("Boca Juniors"))
    }

    @Test
    fun localDeviceActionsAreNotWebSearches() {
        val action = CommandCenter.commands("subir volumen").single().action as Action.Device
        assertEquals("volume_up", action.command)
        assertTrue(CommandCenter.commands("temporizador inexistente").isEmpty())
    }
}
