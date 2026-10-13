package dev.bautista.spotlight.ui

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OverlayAppearanceVisualSnapshotTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private fun capture(name:String, c:AppConfig) {
        ui.setContent{SpotlightTheme("dark",c){Surface(Modifier.widthIn(max=680.dp)){
            Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                OverlayDensitySettings(c){}
                OverlayAppearancePreview(c)
            }
        }}}
        ui.onNodeWithText("La densidad cambia el espacio dentro de las filas. No cambia el ancho de la ventana.").assertIsDisplayed()
        ui.onNodeWithTag("preview-width-label").assertIsDisplayed()
        ui.onRoot().captureRoboImage("src/test/snapshots/$name.png")
    }
    private val base=AppConfig(width=0,landscapeWidth=460,theme="dark",animations=false,density="balanced")
    @Test fun tabletCompact(){capture("appearance-tablet-compact",base.copy(density="compact"))}
    @Test fun tabletSpacious(){capture("appearance-tablet-spacious",base.copy(density="spacious"))}
    @Test @Config(qualifiers="w800dp-h1280dp") fun tabletPortrait(){capture("appearance-tablet-vertical",base)}
    @Test @Config(qualifiers="w390dp-h844dp") fun phonePortrait(){capture("appearance-phone-vertical",base.copy(width=820))}
    @Test @Config(qualifiers="w844dp-h390dp") fun phoneLandscape(){capture("appearance-phone-horizontal",base)}
}
