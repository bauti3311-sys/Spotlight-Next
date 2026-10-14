package dev.bautista.spotlight.ui

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.widthIn
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
class OverlayWidthVisualSnapshotTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private fun capture(name:String) {
        val config=AppConfig(width=0,landscapeWidth=380,animations=false)
        ui.setContent{SpotlightTheme("dark"){Surface(Modifier.widthIn(max=680.dp)){
            OverlayWidthSettings(config){}
        }}}
        ui.onNodeWithContentDescription("Ancho general automático").assertIsOn()
        ui.onNodeWithContentDescription("Ancho horizontal específico").assertIsOn()
        ui.onNodeWithText("Horizontal: 380 dp · ancho específico").assertIsDisplayed()
        ui.onRoot().captureRoboImage("src/test/snapshots/$name.png")
    }
    @Test fun tabletHorizontalOverride(){capture("width-tablet-active-override")}
    @Test @Config(qualifiers="w390dp-h844dp") fun phoneHorizontalOverride(){capture("width-phone-active-override")}
}
