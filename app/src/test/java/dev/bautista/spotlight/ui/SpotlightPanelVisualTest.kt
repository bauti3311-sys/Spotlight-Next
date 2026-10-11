package dev.bautista.spotlight.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SpotlightPanelVisualTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun darkPinnedPanelLoadsQueryWithoutExecutingAction() {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        ui.setContent {
            Box(Modifier.width(700.dp).padding(24.dp)) {
                SpotlightPanel(app,dev.bautista.spotlight.data.Config(theme="dark",files=false,animations=false,pins=setOf("kind:pdf factura","temporizador de 25 minutos")),{},"fijados",availableHeightDp=500f)
            }
        }
        ui.waitUntil(15000){ui.onAllNodesWithText("kind:pdf factura").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("temporizador de 25 minutos").assertIsDisplayed()
        ui.runOnIdle {
            val view=ui.activity.window.decorView
            val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val output=File("build/spotlight-dark-panel.png");output.parentFile?.mkdirs()
            output.outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
            bitmap.recycle()
        }
        ui.onNodeWithText("kind:pdf factura").performClick()
        ui.onNode(hasSetTextAction()).assertTextEquals("kind:pdf factura")
        assertNull(org.robolectric.Shadows.shadowOf(app).nextStartedActivity)
    }
}
