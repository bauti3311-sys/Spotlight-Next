package dev.bautista.spotlight.ui

import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Next100AppearanceTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private var originalMotionScale=1f
    @Before fun preserveSystemMotion(){originalMotionScale=Settings.Global.getFloat(ui.activity.contentResolver,Settings.Global.ANIMATOR_DURATION_SCALE,1f)}
    @After fun restoreSystemMotion(){Settings.Global.putFloat(ui.activity.contentResolver,Settings.Global.ANIMATOR_DURATION_SCALE,originalMotionScale)}
    @Test fun fourPresetsRenderAtFloatingTabletWidth(){
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        var preset by mutableStateOf(AppearancePresets.names.first())
        ui.setContent{Box(Modifier.fillMaxSize(),contentAlignment=Alignment.TopCenter){Box(Modifier.width(760.dp)){SpotlightPanel(app,AppearancePresets.apply(preset,dev.bautista.spotlight.data.Config(files=false,animations=false)),{},availableHeightDp=700f)}}}
        AppearancePresets.names.forEach{name->ui.runOnIdle{preset=name};ui.waitUntil(15000){ui.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()};ui.runOnIdle{capture091(ui.activity,"next100-floating-"+name.substringBefore(' ').lowercase())}}
    }
    @Test fun deviceAndShizukuPanelsExposeActualMissingState(){
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        var page by mutableStateOf("device")
        ui.setContent{SpotlightTheme{Box(Modifier.width(760.dp)){Surface{AdvancedPanel(app,page)}}}}
        ui.onNodeWithText("Dispositivo").assertExists()
        ui.runOnIdle{capture091(ui.activity,"next100-device-tablet");page="shizuku"}
        ui.onNodeWithText("No instalado").assertExists()
        ui.runOnIdle{capture091(ui.activity,"next100-shizuku-not-installed-tablet")}
    }
    @Test fun tabletSidebarChangesCategory(){
        ui.setContent{SpotlightTheme{Surface{var group by remember{mutableStateOf("General")};SettingsLayout(group,{group=it}){Text("Contenido: $group")}}}}
        ui.onNodeWithText("Apariencia").performClick()
        ui.onNodeWithText("Contenido: Apariencia").assertExists()
        ui.onNodeWithText("Cambiar").assertDoesNotExist()
        ui.runOnIdle{capture091(ui.activity,"next100-settings-sidebar-tablet")}
    }
    @Test @Config(qualifiers="w360dp-h800dp") fun phoneCategorySelectorPreservesContent(){
        ui.setContent{SpotlightTheme{Surface{var group by remember{mutableStateOf("General")};SettingsLayout(group,{group=it}){Text("Contenido: $group")}}}}
        ui.onNodeWithText("Cambiar").performClick()
        ui.onNodeWithText("Apariencia").performClick()
        ui.onNodeWithText("Contenido: Apariencia").assertExists()
        ui.runOnIdle{capture091(ui.activity,"next100-settings-selector-phone")}
    }
    @Test fun systemReducedMotionIsRespected(){
        Settings.Global.putFloat(ui.activity.contentResolver,Settings.Global.ANIMATOR_DURATION_SCALE,0f)
        ui.setContent{SpotlightTheme{Text("Animaciones: ${LocalMotion.current}")}}
        ui.onNodeWithText("Animaciones: false").assertExists()
    }
}
