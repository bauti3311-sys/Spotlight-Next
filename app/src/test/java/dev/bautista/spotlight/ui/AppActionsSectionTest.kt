package dev.bautista.spotlight.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.ActionCatalog
import dev.bautista.spotlight.search.Action
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class AppActionsSectionTest {
    @get:Rule val ui=createComposeRule()
    @Test fun whatsappReplyNavigationIsNotFilteredOutByActionType() {
        var action:Action?=null
        ui.setContent { AppCapabilityActions("com.whatsapp"){action=it} }
        ui.onNodeWithText("Acciones de app").assertIsDisplayed()
        ui.onNodeWithText("Responder mensajes recibidos").performClick()
        val navigation=action as Action.Navigate
        assertEquals("whatsapp",navigation.scope.topic);assertEquals("replies",navigation.scope.section)
    }
    @Test fun businessCompositionPreservesDestinationAndIncludesReplies() {
        val actions=ActionCatalog.applicationActions("com.whatsapp.w4b")
        assertEquals("whatsapp_business",(actions.first{it.action is Action.ComposeMessage}.action as Action.ComposeMessage).channel)
        assertTrue(actions.any{it.action is Action.Navigate})
    }
    @Test fun unsupportedAppHasAnHonestSectionWithoutImaginaryActions() {
        ui.setContent { AppCapabilityActions("fixture.noactions"){} }
        ui.onNodeWithText("Acciones de app").assertIsDisplayed()
        ui.onNodeWithText("La app no publica acciones adicionales compatibles.").assertIsDisplayed()
        assertTrue(ActionCatalog.applicationActions("fixture.noactions").isEmpty())
    }
}
