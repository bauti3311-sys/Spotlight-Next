package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.integrations.spotify.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33], qualifiers="w330dp-h1200dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SpotifySelectionControlsTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private val item = SpotifySearchItem(SpotifyItemType.TRACK,"a".repeat(22),"Tema de prueba", "Artista")
    private var confirms = 0
    private var observations = 0
    private var route = false
    private fun content(dark: Boolean, scale: Float, busy: Boolean = false) {
        ui.setContent {
            var web by remember { mutableStateOf(false) }
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                    Column(Modifier.width(330.dp).fillMaxHeight().verticalScroll(rememberScrollState())) {
                        SpotifySelectionControls(item, web, busy,
                            onRoute = { web = it; route = it },
                            onConfirm = { confirms++ }, onObserve = { observations++ })
                    }
                }
            }
        }
    }
    @Test fun lightRouteSelectionNeverPlaysAndConfirmationAndObservationAreSeparate() {
        content(false, 1f)
        ui.onNodeWithText("Reproducción local (App Remote)").assertIsSelected()
        ui.onNodeWithText("Reproducir en dispositivo activo").performClick().assertIsSelected()
        ui.runOnIdle { assertTrue(route); assertEquals(0, confirms); assertEquals(0, observations) }
        ui.onNodeWithText("Confirmar reproducción").performScrollTo().performClick()
        ui.runOnIdle { assertEquals(1, confirms); assertEquals(0, observations) }
        ui.onNodeWithText("Comprobar reproducción").performScrollTo().performClick()
        ui.runOnIdle { assertEquals(1, confirms); assertEquals(1, observations) }
    }
    @Test fun darkEnlargedTextKeepsBothRoutesAndActionsReachableAt330dp() {
        content(true, 2f)
        ui.onNodeWithText("Reproducir en dispositivo activo").performScrollTo().performClick().assertIsSelected()
        ui.onNodeWithText("Reproducción local (App Remote)").performScrollTo().performClick().assertIsSelected()
        ui.onNodeWithText("Confirmar reproducción").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Comprobar reproducción").performScrollTo().assertIsDisplayed()
        ui.runOnIdle { assertFalse(route); assertEquals(0, confirms); assertEquals(0, observations) }
    }
    @Test fun busyRequestDisablesRouteChangesConfirmationAndObservation() {
        content(false, 2f, busy = true)
        for (label in listOf("Reproducir en dispositivo activo", "Reproducción local (App Remote)", "Confirmar reproducción", "Comprobar reproducción")) {
            ui.onNodeWithText(label).performScrollTo().assertIsNotEnabled().performClick()
        }
        ui.runOnIdle { assertFalse(route); assertEquals(0, confirms); assertEquals(0, observations) }
    }
}
