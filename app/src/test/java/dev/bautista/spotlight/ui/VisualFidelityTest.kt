package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.ComponentName
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Production composables and Android package fixtures, not installed services or physical One UI. */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class VisualFidelityTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private val c=AppConfig(theme="dark",preset="glass",animations=false,files=false,learning=false)
    private fun capture(name:String) { ui.onRoot().captureRoboImage("build/visual-fidelity/$name.png",
        roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record)) }
    private fun apps() {
        val launcher=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        listOf("com.whatsapp" to "WhatsApp","com.spotify.music" to "Spotify","com.android.chrome" to "Chrome").forEach { (pkg,label) ->
            val info=ActivityInfo().apply { packageName=pkg;name="Main";nonLocalizedLabel=label
                applicationInfo=ApplicationInfo().apply { packageName=pkg } }
            Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(launcher,ResolveInfo().apply {activityInfo=info})
        }
        app.search.invalidateApps()
    }
    private fun composition(query:String,theme:String="dark",preset:String="glass") {
        apps()
        val appearance=c.copy(theme=theme,preset=preset,favorites=setOf("com.whatsapp/Main"))
        ui.setContent { SpotlightTheme(theme,appearance) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                Column(Modifier.fillMaxSize().padding(32.dp)) {
                    Text("Spotlight Next",style=MaterialTheme.typography.headlineMedium)
                    Text("Tu barra de búsqueda y acciones, siempre a mano.",color=MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(24.dp))
                    SettingsLayout("Apariencia",{},Modifier.weight(1f)) { AppearanceSettings(appearance,{}) }
                }
                Box(Modifier.align(Alignment.Center).width(900.dp)) {
                    SpotlightPanel(app,appearance,{},query,availableHeightDp=640f)
                }
            }
        } }
        ui.waitUntil(15000) { ui.onAllNodesWithTag(if(query.isBlank())"result:suggest:app:com.whatsapp" else "result:com.whatsapp/Main").fetchSemanticsNodes().isNotEmpty() }
    }
    @Test fun whatsappOnAppearanceKeepsOneHeaderAndRealContext() {
        composition("WhatsApp")
        ui.onNodeWithTag("context-inspector").assertDoesNotExist()
        ui.onNodeWithTag("result:com.whatsapp/Main").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        assertNull(Shadows.shadowOf(app).peekNextStartedActivity())
        ui.onNodeWithTag("result:com.whatsapp/Main").performTouchInput{longClick()}
        ui.onNodeWithText("Información de la app").performScrollTo().performClick()
        val intent=Shadows.shadowOf(app).nextStartedActivity
        assertEquals(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,intent.action)
        assertEquals("package:com.whatsapp",intent.data.toString())
        capture("tablet-whatsapp-on-settings")
    }
    @Test fun homeCompositionKeepsContextualInspector() {
        composition("")
        ui.onNodeWithTag("result:suggest:app:com.whatsapp").assertIsDisplayed()
        ui.onNodeWithTag("context-inspector").assertDoesNotExist()
        capture("tablet-home-on-settings")
    }
    @Test fun lightCompositionRetainsHierarchy() { composition("WhatsApp","light","macos");capture("tablet-light-on-settings") }
    @Test fun inspectorUsesOnlyProvidedMediaAndNeverInventsPackageMetadata() {
        var opens=0;var infos=0;val media=mutableListOf<String>()
        val item=Result("fixture:app","Fixture app","","Aplicaciones",1,
            Action.Launch(Intent().setComponent(ComponentName("missing.fixture","Main"))))
        ui.setContent { SpotlightTheme("dark",c) { Column(Modifier.width(340.dp)) {
            ApplicationInspector(item,"missing.fixture",false,false,listOf("play","next"),
                {opens++},{},{infos++},{},{media+=it})
        } } }
        ui.onNodeWithTag("inspector-primary-action").performClick()
        ui.onNodeWithText("Ver información de la app").performClick()
        ui.onNodeWithText("Reproducir").performClick()
        ui.onNodeWithText("Siguiente canción").performClick()
        assertEquals(1,opens);assertEquals(1,infos);assertEquals(listOf("play","next"),media)
        ui.onNodeWithText("Pausar reproducción").assertDoesNotExist()
        ui.onNodeWithText("Versión").assertDoesNotExist()
        ui.onNodeWithText("missing.fixture").assertExists()
        capture("inspector-actions-metadata")
    }
}
