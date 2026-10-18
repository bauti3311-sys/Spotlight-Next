package dev.bautista.spotlight.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.Action
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RenewalInteractionTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun expandingSectionAndBackPreservesQuery() {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        ui.setContent { SpotlightPanel(app,AppConfig(files=false,animations=false,theme="dark",appearance="raycast",accent="purple"),{},initialQuery="sonido",availableHeightDp=640f) }
        ui.waitUntil(15000) { ui.onAllNodesWithText("Ver más",substring=true).fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Ver más",substring=true).performClick()
        ui.waitUntil(15000) { ui.onAllNodesWithText("Activar sonido").fetchSemanticsNodes().isNotEmpty() }
        ui.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Silenciar"))
        ui.onNodeWithText("Silenciar").assertExists()
        ui.onNodeWithTag("results-expand").performClick()
        ui.onNodeWithText("sonido").assertExists()
        ui.waitUntil(15000) { ui.onAllNodesWithText("Ver más",substring=true).fetchSemanticsNodes().isNotEmpty() }
        ui.runOnIdle {
            val view=ui.activity.window.decorView
            val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888);view.draw(Canvas(bitmap))
            File("build/spotlight-renewal-raycast.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        }
    }
    @Test fun preparingMessageRequiresRecipientAndContentReview() {
        var launched:Intent?=null
        ui.setContent {SpotlightTheme("light") { MessageEditor(Action.ComposeMessage("sms","+5491112345678","Hola Juan"),{launched=it},{},{}) } }
        ui.waitUntil(15000) { ui.onAllNodesWithText("Revisar mensaje").fetchSemanticsNodes().isNotEmpty() && ui.onNodeWithText("Revisar mensaje").isEnabled() }
        ui.onNodeWithText("Revisar mensaje").performClick()
        assertNull(launched)
        ui.onNodeWithText("Confirmar · SMS").assertExists()
        ui.onNodeWithText("Preparar en la app").performClick()
        ui.runOnIdle {assertEquals(Intent.ACTION_SENDTO,launched!!.action);assertEquals("Hola Juan",launched!!.getStringExtra("sms_body"))}
    }
    private fun SemanticsNodeInteraction.isEnabled(): Boolean = try {assertIsEnabled();true} catch(e:AssertionError){false}
}
