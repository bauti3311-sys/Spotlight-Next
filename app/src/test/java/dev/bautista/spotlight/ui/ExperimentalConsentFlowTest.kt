package dev.bautista.spotlight.ui

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.control.ExperimentalMessagingPreferences
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w600dp-h1000dp",application=Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ExperimentalConsentFlowTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private val context get() = RuntimeEnvironment.getApplication()
    private fun show() = ui.setContent { MaterialTheme { Column(Modifier.fillMaxSize()) { WhatsAppExperimentalSettings() } } }
    @Test fun defaultAndCancelledConsentLeaveLinkingUnavailable() {
        assertFalse(ExperimentalMessagingPreferences(context).allowed)
        show()
        ui.onNodeWithTag("whatsapp-link-device").assertIsNotEnabled()
        ui.onNodeWithText("Activar modo experimental").performClick()
        ui.onNodeWithText("Acepto y autorizo preparar el experimento").assertIsNotEnabled()
        ui.onNodeWithText("Cancelar",useUnmergedTree=true).performClick()
        assertFalse(ExperimentalMessagingPreferences(context).allowed)
    }
    @Test fun bothAcknowledgementsAreRequiredAndAccountLinkingRemainsSeparate() {
        show()
        ui.onNodeWithText("Activar modo experimental").performClick()
        ui.onNodeWithTag("whatsapp-consent-risk").performClick()
        ui.onNodeWithText("Acepto y autorizo preparar el experimento").assertIsNotEnabled()
        ui.onNodeWithTag("whatsapp-consent-privacy").performClick()
        ui.onNodeWithText("Acepto y autorizo preparar el experimento").performClick()
        ui.waitUntil(5000) { ExperimentalMessagingPreferences(context).allowed }
        ui.onNodeWithTag("whatsapp-link-device").assertIsNotEnabled()
        ui.onNodeWithText("Volver al modo normal").performClick()
        ui.waitUntil(5000) { !ExperimentalMessagingPreferences(context).allowed }
        ui.onNodeWithText("Activar modo experimental").assertIsDisplayed()
    }
    @Test fun missingConsentCannotBePersistedByPartialAcknowledgement() {
        val preferences = ExperimentalMessagingPreferences(context)
        assertThrows(IllegalArgumentException::class.java) { preferences.confirmFromUi(true,false) }
        assertFalse(ExperimentalMessagingPreferences(context).allowed)
        preferences.confirmFromUi(true,true)
        assertTrue(ExperimentalMessagingPreferences(context).allowed)
        preferences.revoke(); assertFalse(ExperimentalMessagingPreferences(context).allowed)
    }
}
