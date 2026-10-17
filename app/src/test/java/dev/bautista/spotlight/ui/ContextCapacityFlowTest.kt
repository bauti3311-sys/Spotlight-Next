package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
class ContextCapacityFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    @Test fun fullContextStorageReportsCapacityWithoutCrashingOrEvictingUserData() {
        runBlocking{repeat(30){app.contexts.setText("Contexto guardado $it");app.contexts.pinActive()}}
        app.contexts.setText("Nuevo contexto no guardado")
        val ids=app.contexts.pinned.value.map{it.id}
        val file=File(app.noBackupFilesDir,"spotlight-context-v1.json");val before=file.readText()
        val config=AppConfig(animations=false)
        ui.setContent{SpotlightTheme(config.theme,config){ContextPanel(app,config,"context","",{})}}
        ui.onNodeWithText("Fijar").performClick()
        ui.waitUntil(10000){ui.onAllNodesWithText("Ya hay 30 contextos fijados. Eliminá uno antes de fijar otro.").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Ya hay 30 contextos fijados. Eliminá uno antes de fijar otro.").performScrollTo().assertIsDisplayed()
        assertEquals(ids,app.contexts.pinned.value.map{it.id});assertFalse(app.contexts.active.value!!.pinned)
        assertEquals(before,file.readText())
    }
}
