package dev.bautista.spotlight.ui

import android.content.*
import android.content.pm.*
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.FileBuffer
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.sharing.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowContentResolver

/** Real panel -> existing NativeLaunchSession; controlled providers/PM, no external effects. */
@OptIn(ExperimentalTestApi::class,ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w390dp-h844dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class C2FileShareFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private lateinit var provider:ShareDocumentProvider
    private val buffer=FileBuffer()
    private var viewport by mutableStateOf(700f)
    private var width by mutableStateOf(360f)
    private val phrase="compartir archivos seleccionados"
    @Before fun fixtures() {
        val info=ProviderInfo().apply{authority="share-files-fixture";name=ShareDocumentProvider::class.java.name;packageName=app.packageName;applicationInfo=app.applicationInfo;enabled=true;grantUriPermissions=true}
        provider=ShareDocumentProvider().apply{attachInfo(app,info);names["2"]="Evolución vestibular.pdf";names["3"]="Notas de consulta.pdf"}
        ShadowContentResolver.registerProviderInternal(info.authority,provider)
        val pm=Shadows.shadowOf(app.packageManager);pm.addOrUpdateProvider(info)
        fun receiver(pkg:String,action:String,system:Boolean=false) {
            val a=ApplicationInfo().apply{packageName=pkg;enabled=true;if(system)flags=ApplicationInfo.FLAG_SYSTEM}
            val activity=ActivityInfo().apply{packageName=pkg;name="$pkg.Receive";applicationInfo=a;exported=true;enabled=true}
            pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(activity)})
            val intent=Intent(action);if(!system)intent.type="application/pdf"
            pm.addResolveInfoForIntent(intent,ResolveInfo().apply{activityInfo=activity;isDefault=true})
        }
        receiver("fixture.reader",Intent.ACTION_SEND)
        receiver("fixture.reader.multiple",Intent.ACTION_SEND_MULTIPLE)
        receiver("fixture.system",Intent.ACTION_CHOOSER,true)
        add(1)
    }
    private fun add(n:Int) {
        val uri=Uri.parse("content://share-files-fixture/document/$n")
        buffer.add(FileShareContract.result(FileShareContract.Document(uri,"Vestibular $n.pdf","application/pdf",100,"Origen elegido")))
    }
    @Composable private fun panel(theme:String="dark") {
        Box(Modifier.width(width.dp)){SpotlightPanel(app,AppConfig(theme=theme,files=true,online=false,contacts=false,animations=false,learning=false,preset="glass"),{},phrase,viewport,fileSelection=buffer)}
    }
    private fun open(theme:String="dark") {
        ui.setContent{panel(theme)};enter()
    }
    private fun enter() {
        val tag="result:context:share-files:${FileShareContract.selectionId(buffer.items)}"
        ui.waitUntil(15000){ui.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()}
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).performKeyInput{pressKey(Key.Enter)}
        ui.onNodeWithTag("file-share-review").assertExists()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    private fun review() {
        ui.onNodeWithTag("file-share-review-button").performScrollTo().performClick()
        ui.waitUntil(10000){ui.onAllNodesWithTag("file-share-confirm-button").fetchSemanticsNodes().isNotEmpty()}
    }
    private fun capture(name:String) {ui.onNodeWithTag("spotlight-root").captureRoboImage("build/c2-files/after/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))}
    @Test fun queryReviewsOriginalFileAndRequestsOnlyOneSystemChooser() {
        open();assertEquals(0,provider.descriptors);review()
        assertEquals(1,provider.descriptors)
        ui.onNodeWithText("Vestibular.pdf").assertExists()
        ui.onNodeWithText("Destino: selector de aplicaciones de Android").assertExists()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
        capture("review-dark")
        ui.onNodeWithTag("file-share-confirm-button").performScrollTo().performClick()
        ui.waitUntil(10000){ui.onAllNodesWithTag("native-launch-request").fetchSemanticsNodes().isNotEmpty()}
        val request=Shadows.shadowOf(app).nextStartedActivity
        assertEquals(Intent.ACTION_CHOOSER,request.action)
        val send=FileShareContract.chooserPayload(request)!!
        assertEquals(Intent.ACTION_SEND,send.action);assertEquals("application/pdf",send.type)
        assertEquals(listOf(Uri.parse("content://share-files-fixture/document/1")),FileShareContract.streams(send))
        assertEquals(Intent.FLAG_GRANT_READ_URI_PERMISSION,send.flags)
        assertEquals(2,provider.descriptors)
        ui.onNodeWithTag("file-share-confirm-button").assertIsNotEnabled().performClick()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
        ui.onNodeWithTag("native-launch-request").assertTextContains("Resultado externo sin comprobar",substring=true)
    }
    @Test fun changedMetadataRequiresAnotherReviewInsteadOfOpeningAndroid() {
        open();review();ui.runOnIdle{provider.displayName="Documento cambiado.pdf"}
        ui.onNodeWithTag("file-share-confirm-button").performScrollTo().performClick()
        ui.waitUntil(10000){ui.onAllNodesWithTag("file-share-error").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithTag("file-share-confirm-button").assertDoesNotExist()
        ui.onNodeWithTag("file-share-review-button").assertExists()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun lostReadAccessAtConfirmationNeverRequestsAnAlternative() {
        open();review();ui.runOnIdle{provider.denied=true}
        ui.onNodeWithTag("file-share-confirm-button").performScrollTo().performClick()
        ui.waitUntil(10000){ui.onAllNodesWithTag("file-share-error").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithTag("file-share-error").assertTextContains("permiso de lectura venció",substring=true)
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun restoringMetadataDoesNotRestoreConfirmationOrAuthorization() {
        val restore=StateRestorationTester(ui);restore.setContent{panel()};enter();review()
        restore.emulateSavedInstanceStateRestore()
        ui.onNodeWithTag("file-share-review").assertExists()
        ui.onNodeWithTag("file-share-confirm-button").assertDoesNotExist()
        ui.onNodeWithTag("file-share-review-button").assertExists()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun changedLiveSelectionInvalidatesThePreparedProposal() {
        open();review();ui.runOnIdle{buffer.clear()}
        ui.onNodeWithTag("file-share-confirm-button").assertDoesNotExist()
        ui.onNodeWithTag("file-share-error").assertExists()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun multipleLongNamesScrollInShortViewportAndCancelRestoresQueryFocus() {
        add(2);add(3)
        provider.names["2"]="Documento vestibular con nombre muy largo para revisar sin recortes ni perder las acciones de confirmación.pdf"
        open("light");review();ui.runOnIdle{viewport=320f}
        ui.onNodeWithText("Destino: selector de aplicaciones de Android").performScrollTo().assertIsDisplayed()
        ui.onNodeWithTag("file-share-confirm-button").performScrollTo().assertIsDisplayed()
        val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
        assertTrue(ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot().bottom<=root.bottom)
        capture("review-light-short")
        ui.runOnIdle{viewport=700f};capture("review-light-restored")
        ui.onNodeWithTag("file-share-cancel").performScrollTo().performClick()
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).assertTextEquals(phrase).assertIsFocused()
        assertEquals(3,buffer.items.size);assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun escapeCancelsWithoutLosingSelectionOrSending() {
        open();review()
        ui.onNodeWithTag("spotlight-root").performKeyInput{pressKey(Key.Escape)}
        ui.onNodeWithTag("file-share-review").assertDoesNotExist()
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).assertTextEquals(phrase).assertIsFocused()
        assertEquals(1,buffer.items.size);assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test @Config(qualifiers="w1280dp-h900dp")
    fun reviewedFilesRemainScrollableAcrossWideAndCompactLayouts() {
        width=960f;add(2);open();review();capture("review-tablet")
        ui.runOnIdle{width=360f;viewport=340f}
        ui.onNodeWithTag("file-share-confirm-button").performScrollTo().assertIsDisplayed()
        ui.runOnIdle{width=960f;viewport=700f}
        ui.onNodeWithTag("file-share-confirm-button").performScrollTo().assertIsDisplayed()
        val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
        assertTrue(ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot().bottom<=root.bottom)
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
        ui.onNodeWithTag("file-share-cancel").performScrollTo().performClick()
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).assertIsFocused()
    }
}
