package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.search.Action
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w390dp-h844dp",application=SpotlightApp::class)
class C1InspectorContractTest {
    @get:Rule val ui=createComposeRule()
    @Test fun disclosureOnlyRevealsExistingActionsAndDoesNotDispatch() {
        var effects=0
        ui.setContent { SpotlightTheme("light") { Box(Modifier.width(320.dp)) {
            InspectorSecondaryActions(true,"doc-a") { TextButton(onClick={effects++}){Text("Acción local de prueba")} }
        } } }
        ui.onNodeWithText("Acción local de prueba").assertDoesNotExist()
        ui.onNodeWithTag("inspector-more-actions").assertHeightIsAtLeast(48.dp).performClick()
        ui.runOnIdle{assertEquals(0,effects)}
        ui.onNodeWithText("Acción local de prueba").assertIsDisplayed().performClick()
        ui.onNodeWithTag("inspector-more-actions").performClick()
        ui.onNodeWithText("Acción local de prueba").assertDoesNotExist()
        ui.runOnIdle{assertEquals(1,effects)}
    }
    @Test fun changingIdentityCollapsesTheOldPresentationWithoutEffects() {
        var identity by mutableStateOf("app-a");var effects=0
        ui.setContent { SpotlightTheme("dark") {
            InspectorSecondaryActions(true,identity) { TextButton(onClick={effects++}){Text("Acción de $identity")} }
        } }
        ui.onNodeWithTag("inspector-more-actions").performClick()
        ui.onNodeWithText("Acción de app-a").assertIsDisplayed()
        ui.runOnIdle{identity="app-b"}
        ui.onNodeWithText("Acción de app-a").assertDoesNotExist()
        ui.onNodeWithText("Acción de app-b").assertDoesNotExist()
        ui.runOnIdle{assertEquals(0,effects)}
    }
    @Test fun runningExtractionKeepsItsCancellationVisible() {
        var canceled=0
        ui.setContent { SpotlightTheme("dark") {
            InspectorSecondaryActions(true,"doc",forceExpanded=true) { TextButton(onClick={canceled++}){Text("Cancelar extracción")} }
        } }
        ui.onNodeWithTag("inspector-more-actions").assertIsNotEnabled()
        ui.onNodeWithText("Cancelar extracción").assertIsDisplayed().performClick()
        ui.runOnIdle{assertEquals(1,canceled)}
    }
    @Test fun completingExtractionDoesNotCollapseTheVisibleActions() {
        var running by mutableStateOf(true)
        ui.setContent { SpotlightTheme("dark") {
            InspectorSecondaryActions(true,"doc",forceExpanded=running) {
                TextButton(onClick={}){Text(if(running)"Cancelar extracción" else "Extraer texto local")}
            }
        } }
        ui.onNodeWithText("Cancelar extracción").assertIsDisplayed()
        ui.runOnIdle{running=false}
        ui.onNodeWithText("Extraer texto local").assertIsDisplayed()
    }
    private fun destination(channel:String,name:String) {
        var launched=0
        ui.setContent { SpotlightTheme("light") { Column(Modifier.width(360.dp).height(720.dp).verticalScroll(rememberScrollState())) {
            MessageEditor(Action.ComposeMessage(channel,"+00000000001","Borrador local de prueba"),{launched++},{},{})
        } } }
        ui.onNodeWithText("Mensaje · $name").assertIsDisplayed()
        ui.onNodeWithText("Cambiar a Business").assertDoesNotExist()
        ui.waitUntil(15000){runCatching{ui.onNodeWithText("Revisar mensaje").assertIsEnabled();true}.getOrDefault(false)}
        ui.onNodeWithText("Revisar mensaje").performScrollTo().assertIsDisplayed().performClick()
        ui.onNodeWithText("Confirmar · $name").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Preparar en la app").assertExists()
        ui.runOnIdle{assertEquals(0,launched)}
    }
    @Test fun personalDestinationIsNamedExplicitlyAndReviewDoesNotSend()=destination("whatsapp","WhatsApp personal")
    @Test fun businessUsesTheFullApplicationNameWithoutAChannelSwitch()=destination("whatsapp_business","WhatsApp Business")
}
