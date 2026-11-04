package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.key.Key
import dev.bautista.spotlight.control.ActionCatalog
import dev.bautista.spotlight.data.Config as AppConfig
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.search.Action
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w390dp-h844dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class C2SharedMessageFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun install(pkg:String="fixture.receiver",title:String="Receptor",share:Boolean=true) {
        val info=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=title;enabled=true}
        val main=ActivityInfo().apply{packageName=pkg;name="Main";applicationInfo=info;exported=true;enabled=true;nonLocalizedLabel=title}
        val send=ActivityInfo().apply{packageName=pkg;name="$pkg.Share";applicationInfo=info;exported=true;enabled=true}
        val pm=Shadows.shadowOf(app.packageManager)
        pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=info;activities=arrayOf(main,send)})
        pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=main})
        if(share)pm.addResolveInfoForIntent(Intent(Intent.ACTION_SEND).setType("text/plain").setPackage(pkg),ResolveInfo().apply{activityInfo=send})
        app.search.invalidateApps()
    }
    private fun editable()=ui.onNode(hasSetTextAction()).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.EditableText].text
    private val draft=Action.ComposeMessage("share:fixture.receiver","Receptor")
    private fun form(theme:String,onLaunch:(Intent)->Unit={}) {
        app.contexts.setText("https://example.org/nota?tema=café","Texto seleccionado · Process Text")
        ui.setContent{SpotlightTheme(theme){Box(Modifier.width(360.dp).testTag("shared-message-scene")){
            MessageEditor(draft,onLaunch,{}, {})
        }}}
    }
    private fun capture(theme:String) {
        form(theme)
        val phase=System.getenv("SPOTLIGHT_C2_NEXT_PHASE") ?: "after"
        ui.onNodeWithTag("shared-message-scene").captureRoboImage("build/c2-next/$phase/message-$theme.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun captureDark()=capture("dark")
    @Test fun captureLight()=capture("light")
    @Test fun contextRequiresSelectionReviewAndConfirmationToRequestSelectedApp() {
        val requests=mutableListOf<Intent>();form("dark"){requests+=it}
        assertEquals("",editable())
        ui.onNodeWithTag("message-use-context").performClick()
        assertEquals("https://example.org/nota?tema=café",editable())
        assertTrue(requests.isEmpty())
        ui.onNodeWithText("Revisar mensaje").performClick()
        assertTrue(requests.isEmpty())
        ui.onNodeWithText("Destino: Receptor").assertExists()
        ui.onNodeWithText("Preparar en la app").performClick()
        assertEquals(Intent.ACTION_SEND,requests.single().action)
        assertEquals("fixture.receiver",requests.single().`package`)
        assertEquals("https://example.org/nota?tema=café",requests.single().getStringExtra(Intent.EXTRA_TEXT))
    }
    @Test fun newContextDoesNotReplaceAnEditedDraftOrAuthorizeIt() {
        val requests=mutableListOf<Intent>();form("light"){requests+=it}
        ui.onNodeWithTag("message-use-context").performClick()
        ui.onNode(hasSetTextAction()).performTextReplacement("Borrador editado")
        ui.runOnIdle{app.contexts.setText("otro contenido","Share nuevo")}
        assertEquals("Borrador editado",editable())
        ui.onNodeWithText("Revisar mensaje").performClick()
        ui.onNodeWithText("Editar").performClick()
        assertTrue(requests.isEmpty())
        ui.onNodeWithText("Volver").performClick()
        assertTrue(requests.isEmpty())
    }
    @Test fun contextLimitIsVisibleAndNoDocumentIsRead() {
        app.contexts.setText("x".repeat(9000),"Texto largo")
        ui.setContent{SpotlightTheme{MessageEditor(draft,{}, {}, {})}}
        ui.onNodeWithTag("message-use-context").performClick()
        assertEquals(8000,ui.onNode(hasSetTextAction()).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.EditableText].text.length)
        ui.onNodeWithText("Se usarán los primeros 8000 caracteres; podés editarlos antes de compartir.").assertExists()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
        kotlinx.coroutines.runBlocking{app.contexts.setDocument(android.net.Uri.parse("content://fixture.documents/item1"))}
        ui.onNodeWithTag("message-use-context").assertDoesNotExist()
        assertEquals(8000,editable().length)
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun captureGmailCapability() {
        install("com.google.android.gm","Gmail")
        ui.setContent{SpotlightTheme("dark"){Box(Modifier.width(360.dp).testTag("gmail-capability-scene")){
            AppCapabilityActions("com.google.android.gm"){}
        }}}
        ui.waitForIdle()
        val phase=System.getenv("SPOTLIGHT_C2_NEXT_PHASE") ?: "after"
        ui.onNodeWithTag("gmail-capability-scene").captureRoboImage("build/c2-next/$phase/gmail-capability.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
    @Test fun gmailInspectorCannotUseADifferentDefaultMailClient() {
        install("com.google.android.gm","Gmail")
        var selected:Action?=null
        ui.setContent{SpotlightTheme{AppCapabilityActions("com.google.android.gm"){selected=it}}}
        ui.waitUntil(10000){ui.onAllNodesWithTag("app-action-catalog:gmail:compose").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithTag("app-action-catalog:gmail:compose").performClick()
        ui.waitUntil(10000){selected!=null}
        val action=selected as Action.ComposeMessage
        assertEquals("share:com.google.android.gm",action.channel)
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun unavailableGmailShareIsDisabledWithReason() {
        install("com.google.android.gm","Gmail",share=false)
        ui.setContent{SpotlightTheme{AppCapabilityActions("com.google.android.gm") {}}}
        ui.waitUntil(10000){ui.onAllNodesWithText("No disponible: revisá la app y sus permisos para compartir texto.").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithTag("app-action-catalog:gmail:compose").assertIsNotEnabled()
        assertEquals("share:com.google.android.gm",(ActionCatalog.applicationActions("com.google.android.gm").first{it.action is Action.ComposeMessage}.action as Action.ComposeMessage).channel)
    }
    @Test fun sharedQueryReviewScrollAndConfirmationRequestExactAppOnce() {
        install();app.contexts.setText("https://example.org/nota","Enlace compartido")
        var viewport by mutableStateOf(700f)
        ui.setContent{Box(Modifier.width(360.dp)){SpotlightPanel(app,
            AppConfig(files=false,online=false,contacts=false,animations=false,learning=false,preset="glass"),{},"compartir en Receptor",viewport)}}
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:context:app-share:fixture.receiver/Main").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Preparar contenido").assertIsDisplayed()
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).performKeyInput{pressKey(Key.Enter)}
        ui.onNodeWithTag("message-use-context").performScrollTo().performClick()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
        ui.runOnIdle{viewport=320f}
        ui.onNodeWithText("Revisar mensaje").performScrollTo().assertIsDisplayed().performClick()
        ui.onNodeWithText("Destino: Receptor").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Preparar en la app").performScrollTo().assertIsDisplayed()
        val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
        assertTrue(ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot().bottom<=root.bottom)
        ui.onNodeWithTag("spotlight-root").captureRoboImage("build/c2-next/after/shared-confirmation-short.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
        ui.onNodeWithText("Preparar en la app").performClick()
        val requested=Shadows.shadowOf(app).nextStartedActivity
        assertEquals(Intent.ACTION_SEND,requested.action)
        assertEquals("fixture.receiver",requested.component!!.packageName)
        assertEquals("https://example.org/nota",requested.getStringExtra(Intent.EXTRA_TEXT))
        ui.onNodeWithTag("native-launch-request").performScrollTo().assertTextContains("Resultado externo sin comprobar",substring=true)
        assertTrue(ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot().bottom<=root.bottom)
        ui.runOnIdle{viewport=700f}
        ui.onNodeWithTag("context-footer").assertIsDisplayed()
        ui.onNodeWithText("Revisar mensaje").performScrollTo().performClick()
        ui.onNodeWithText("Preparar en la app").performScrollTo().performClick()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }

    @Test fun revokedShareAtConfirmationDoesNotOpenAnAlternative() {
        install();app.contexts.setText("texto revisable","Process Text")
        ui.setContent{Box(Modifier.width(360.dp)){SpotlightPanel(app,
            AppConfig(files=false,online=false,contacts=false,animations=false,learning=false),{},"compartir en Receptor",700f)}}
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:context:app-share:fixture.receiver/Main").fetchSemanticsNodes().isNotEmpty()}
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).performKeyInput{pressKey(Key.Enter)}
        ui.onNodeWithTag("message-use-context").performScrollTo().performClick()
        ui.onNodeWithText("Revisar mensaje").performScrollTo().performClick()
        ui.runOnIdle{Shadows.shadowOf(app.packageManager).removePackage("fixture.receiver")}
        ui.onNodeWithText("Preparar en la app").performScrollTo().performClick()
        ui.onNodeWithText("El destino ya no está disponible. Revisá la app elegida o sus ajustes.").assertExists()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun escapeFromPreparedShareReturnsQueryAndFocusWithoutRequest() {
        install()
        ui.setContent{Box(Modifier.width(360.dp)){SpotlightPanel(app,
            AppConfig(files=false,online=false,contacts=false,animations=false,learning=false),{},"compartir en Receptor",700f)}}
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:context:app-share:fixture.receiver/Main").fetchSemanticsNodes().isNotEmpty()}
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).performKeyInput{pressKey(Key.Enter)}
        ui.onNodeWithText("Revisar mensaje").assertExists()
        ui.onNodeWithTag("spotlight-root").performKeyInput{pressKey(Key.Escape)}
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).assertTextEquals("compartir en Receptor").assertIsFocused()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }

    @Test fun restoringSharedDraftDoesNotRestoreItsConfirmation() {
        val requests=mutableListOf<Intent>()
        app.contexts.setText("texto autorizado","Share")
        val restore=StateRestorationTester(ui)
        restore.setContent{SpotlightTheme{MessageEditor(draft,{requests+=it},{},{})}}
        ui.onNodeWithTag("message-use-context").performClick()
        ui.onNode(hasSetTextAction()).performTextReplacement("Borrador conservado")
        ui.onNodeWithText("Revisar mensaje").performClick()
        ui.onNodeWithText("Preparar en la app").assertExists()
        restore.emulateSavedInstanceStateRestore()
        assertEquals("Borrador conservado",editable())
        ui.onNodeWithText("Preparar en la app").assertDoesNotExist()
        assertTrue(requests.isEmpty())
    }

}
