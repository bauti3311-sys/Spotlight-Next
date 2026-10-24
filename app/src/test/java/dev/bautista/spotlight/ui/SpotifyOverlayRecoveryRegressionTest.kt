package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.OverlaySnapshotCodec
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

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
}
