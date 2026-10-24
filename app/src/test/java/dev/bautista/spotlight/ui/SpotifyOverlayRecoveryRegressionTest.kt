package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.OverlaySnapshotCodec
import dev.bautista.spotlight.ai.CredentialVault
import dev.bautista.spotlight.integrations.spotify.SpotifyItemType
import dev.bautista.spotlight.integrations.spotify.SpotifyConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.util.ReflectionHelpers

/** Uses the production panel and the overlay's restricted registry, not Activity Bundle saving. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SpotifyOverlayRecoveryRegressionTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private val app get() = RuntimeEnvironment.getApplication() as SpotlightApp

    @Test fun spotifyPanelCanOpenUnderDurableOverlayRegistry() {
        val registry = SaveableStateRegistry(null, OverlaySnapshotCodec::canSave)
        ui.setContent {
            CompositionLocalProvider(LocalSaveableStateRegistry provides registry) {
                SpotifySearchPanel(app)
            }
        }
        ui.onNodeWithTag("spotify-search-panel").assertIsDisplayed()
        ui.runOnIdle { OverlaySnapshotCodec.encode(registry.performSave()) }
    }

    @Test fun historicalEnumIsRejectedWithoutRelaxingTheRecoveryCodec() {
        val registry=SaveableStateRegistry(null,OverlaySnapshotCodec::canSave)
        assertFalse(OverlaySnapshotCodec.canSave(mutableStateOf(SpotifyItemType.TRACK)))
        registry.registerProvider("legacy-filter"){mutableStateOf(SpotifyItemType.ALBUM)}
        assertTrue(runCatching{registry.performSave()}.isFailure)
    }

    /** No account/token is installed: the marker only makes the actual UI exercise its error path. */
    private fun authorizeFixtureWithoutCredentials() {
        ui.waitForIdle()
        runBlocking{app.spotify.inspect()}
        assertFalse(CredentialVault(app).contains("spotify"))
        ui.runOnIdle {
            ReflectionHelpers.getField<MutableStateFlow<SpotifyConnectionState>>(app.spotify,"mutableState").value=SpotifyConnectionState.AUTHORIZED
        }
        ui.waitUntil(5000){ui.onAllNodesWithTag("spotify-search-query").fetchSemanticsNodes().isNotEmpty()}
    }

    @Test fun filtersAndQueryRestoreAfterProviderErrorCloseAndReopen() {
        var visible by mutableStateOf(true)
        var registry by mutableStateOf(SaveableStateRegistry(null,OverlaySnapshotCodec::canSave))
        ui.setContent {
            if(visible)CompositionLocalProvider(LocalSaveableStateRegistry provides registry){SpotifySearchPanel(app)}
            else androidx.compose.material3.Text("Overlay cerrado")
        }
        authorizeFixtureWithoutCredentials()
        for(label in listOf("Artista","Álbum","Lista","Tema","Álbum")) {
            ui.onNodeWithText(label).performClick().assertIsSelected()
            ui.runOnIdle{OverlaySnapshotCodec.encode(registry.performSave())}
        }
        ui.onNodeWithTag("spotify-search-query").performTextInput("Cerati")
        ui.onNode(hasText("Buscar en Spotify") and hasClickAction()).performClick()
        ui.waitUntil(5000){ui.onAllNodesWithTag("spotify-result-status").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithTag("spotify-result-status").assertTextContains("Conectá tu cuenta")
        ui.onNodeWithTag("spotify-search-panel").assertIsDisplayed()
        lateinit var snapshot:Map<String,List<Any?>>
        ui.runOnIdle{snapshot=registry.performSave()}
        val recovered=runBlocking {app.contexts.saveOverlaySnapshot(snapshot);app.contexts.overlaySnapshot()!!}
        ui.runOnIdle{visible=false}
        ui.onNodeWithText("Overlay cerrado").assertIsDisplayed()
        ui.runOnIdle{registry=SaveableStateRegistry(recovered,OverlaySnapshotCodec::canSave);visible=true}
        authorizeFixtureWithoutCredentials()
        ui.onNodeWithTag("spotify-search-query").assertTextEquals("Cerati")
        ui.onNodeWithText("Álbum").assertIsSelected()
        ui.onNodeWithTag("spotify-result-status").assertDoesNotExist()
        ui.onNodeWithText("Confirmar reproducción").assertDoesNotExist()
        ui.runOnIdle{OverlaySnapshotCodec.encode(registry.performSave())}
    }

    @Test fun unknownStringFilterFallsBackWithoutCrashingOrExecuting() {
        var visible by mutableStateOf(true)
        var registry by mutableStateOf(SaveableStateRegistry(null,OverlaySnapshotCodec::canSave))
        ui.setContent { if(visible)CompositionLocalProvider(LocalSaveableStateRegistry provides registry){SpotifySearchPanel(app)} }
        authorizeFixtureWithoutCredentials()
        ui.onNodeWithText("Álbum").performClick()
        lateinit var snapshot:Map<String,List<Any?>>
        ui.runOnIdle{snapshot=registry.performSave()}
        var replaced=0
        val future=snapshot.mapValues{(_,values)->values.map{value->
            if(value is MutableState<*> && value.value=="album"){replaced++;mutableStateOf("future-type")} else value
        }}
        assertEquals(1,replaced)
        val recovered=OverlaySnapshotCodec.decode(OverlaySnapshotCodec.encode(future))
        ui.runOnIdle{visible=false};ui.waitForIdle()
        ui.runOnIdle{registry=SaveableStateRegistry(recovered,OverlaySnapshotCodec::canSave);visible=true}
        authorizeFixtureWithoutCredentials()
        ui.onNodeWithText("Tema").assertIsSelected()
        ui.onNodeWithText("Confirmar reproducción").assertDoesNotExist()
        ui.runOnIdle{OverlaySnapshotCodec.encode(registry.performSave())}
    }
}
