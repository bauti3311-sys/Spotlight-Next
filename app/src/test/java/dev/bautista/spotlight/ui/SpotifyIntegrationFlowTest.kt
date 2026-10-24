package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.integrations.spotify.SpotifyConnectionState
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w500dp-h1000dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SpotifyIntegrationFlowTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private val app get() = RuntimeEnvironment.getApplication() as SpotlightApp
    @Test fun savingPublicClientIdNeverStartsAuthorizationOrCreatesTokens() {
        ui.setContent { MaterialTheme { Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) { SpotifySettings(app) } } }
        ui.onNodeWithText("Conectar Spotify").assertIsNotEnabled()
        ui.onNodeWithTag("spotify-client-id").performTextInput("a".repeat(32))
        ui.onNodeWithText("Guardar ID").performClick()
        ui.waitUntil(5000) { app.spotify.clientId == "a".repeat(32) }
        ui.onNodeWithText("Conectar Spotify").assertIsEnabled()
        assertEquals(SpotifyConnectionState.DISCONNECTED, app.spotify.state.value)
        assertFalse(java.io.File(app.noBackupFilesDir,"credentials/spotify.enc").exists())
        ui.onNodeWithTag("spotify-client-id").performTextClearance()
        ui.onNodeWithTag("spotify-client-id").performTextInput("b".repeat(32))
        ui.onNodeWithText("Conectar Spotify").assertIsNotEnabled()
    }
    @Test fun disconnectedInspectorOffersConfigurationWithoutRemoteQueries() {
        ui.setContent { MaterialTheme { Column(Modifier.fillMaxSize()) { SpotifySearchPanel(app) } } }
        ui.onNodeWithText("Configurar Spotify").assertIsDisplayed()
        ui.onNodeWithTag("spotify-search-query").assertDoesNotExist()
        ui.onNodeWithText("Confirmar reproducción").assertDoesNotExist()
        assertEquals(SpotifyConnectionState.DISCONNECTED, app.spotify.state.value)
    }
}
