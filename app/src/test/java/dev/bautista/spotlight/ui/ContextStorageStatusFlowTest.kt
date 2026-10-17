package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.ContextStore
import dev.bautista.spotlight.data.Preferences
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
class ContextStorageStatusFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
    @After fun stop(){scope.cancel()}
    @Test fun retryControlReadsRepairedLocalFileWithoutSavingOrSendingAnything() {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        val file=File(app.noBackupFilesDir,"spotlight-context-v1.json")
        val original="{unreadable";file.writeText(original)
        val store=ContextStore(app,scope,Preferences(app));runBlocking{store.retryStorage()}
        assertNotNull(store.storageIssue.value)
        val c=AppConfig(animations=false)
        val recovered=java.util.concurrent.atomic.AtomicBoolean(false)
        ui.setContent{SpotlightTheme(c.theme,c){androidx.compose.foundation.layout.Column{ContextStorageStatus(store){recovered.set(true)}}}}
        ui.onNodeWithText(store.storageIssue.value!!).assertIsDisplayed()
        assertEquals(original,file.readText());assertFalse(recovered.get())
        val repaired="""{"version":1,"contexts":[{"id":"old","title":"Guardado recuperado","kind":"text","text":"Conservado","created":1}]}"""
        file.writeText(repaired)
        ui.onNodeWithText("Reintentar lectura").performClick()
        ui.waitUntil(10000){recovered.get()}
        ui.onNodeWithText("Reintentar lectura").assertDoesNotExist()
        assertEquals("old",store.pinned.value.single().id)
        assertEquals(repaired,file.readText());assertNull(store.active.value)
    }
}
