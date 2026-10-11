package dev.bautista.spotlight.ui
import android.media.AudioManager
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.automation.*
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
@RunWith(RobolectricTestRunner::class) @Config(sdk=[33],qualifiers="w360dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WorkflowConfirmationTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 @Test fun volumeRequiresReviewAndCancelStopsRemainingSteps(){val app=RuntimeEnvironment.getApplication() as SpotlightApp;val audio=app.getSystemService(AudioManager::class.java);val before=audio.getStreamVolume(AudioManager.STREAM_MUSIC);val w=Workflow("confirmation","Revisión",steps=listOf(WorkflowStep("volume","0"),WorkflowStep("note","MUST NOT SAVE")));ui.setContent{SpotlightTheme{Surface{WorkflowRunner(w,app.local){}}}};ui.onNodeWithText("Ejecutar").performClick();ui.waitUntil(10000){ui.onAllNodesWithText("Confirmar paso").fetchSemanticsNodes().isNotEmpty()};Assert.assertEquals(before,audio.getStreamVolume(AudioManager.STREAM_MUSIC));ui.onNodeWithText("Cancelar paso").performClick();ui.waitUntil(10000){ui.onAllNodesWithText("Repetir").fetchSemanticsNodes().isNotEmpty()};Assert.assertEquals(before,audio.getStreamVolume(AudioManager.STREAM_MUSIC));ui.onNodeWithText("La rutina se detuvo; los pasos restantes no se ejecutaron.").assertExists()}
}
