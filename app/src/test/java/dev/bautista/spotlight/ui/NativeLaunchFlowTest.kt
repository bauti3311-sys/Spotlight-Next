package dev.bautista.spotlight.ui

import android.content.*
import android.content.pm.*
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1000dp-h900dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeLaunchFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private var closed=0
    private val restoration=StateRestorationTester(ui)
    private fun show(query:String="vivaldi prueba",realApp:Boolean=false,width:Int=720,theme:String=AppConfig().theme) {
        val activity=ActivityInfo().apply {
            packageName="test.browser";name="Main";exported=true;enabled=true
            applicationInfo=ApplicationInfo().apply{packageName="test.browser";enabled=true}
        }
        Shadows.shadowOf(app.packageManager).installPackage(PackageInfo().apply {
            packageName="test.browser";applicationInfo=activity.applicationInfo;activities=arrayOf(activity)
        })
        Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(
            Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/search?q=prueba")),
            ResolveInfo().apply{activityInfo=activity})
        if(realApp) {
            val info=ApplicationInfo().apply{packageName="test.reader";enabled=true;nonLocalizedLabel="Reader"}
            val main=ActivityInfo().apply{packageName="test.reader";name="Main";exported=true;enabled=true;applicationInfo=info}
            Shadows.shadowOf(app.packageManager).installPackage(PackageInfo().apply{packageName="test.reader";applicationInfo=info;activities=arrayOf(main)})
            Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=main})
            app.search.invalidateApps()
        }
        val failing=object:ContextWrapper(ui.activity) {
            override fun startActivity(intent:Intent) {
                if(intent.`package`=="com.vivaldi.browser")throw ActivityNotFoundException("fixture-private-details")
                super.startActivity(intent)
            }
        }
        restoration.setContent { CompositionLocalProvider(LocalContext provides failing) {
            Box(Modifier.width(width.dp)) { SpotlightPanel(app,AppConfig(theme=theme,preset=if(theme=="dark")"glass" else AppConfig().preset,files=false,animations=false,online=false,
                learning=false,suggestions=false),{closed++},query,700f) }
        } }
        ui.waitUntil(15000){ui.onAllNodesWithTag(if(realApp)"result:test.reader/Main" else "result:search:natural:vivaldi").fetchSemanticsNodes().isNotEmpty()}
    }
    private fun activate()=ui.onNodeWithTag("result:search:natural:vivaldi").performClick().performClick()
    @Test fun failedSelectedActionNeverOpensFallbackOrClosesOverlay() {
        show();activate()
        ui.onNodeWithTag("spotlight-root").captureRoboImage("build/native-launch/failed-selected.png",
            roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        assertNull("The web alternative requires explicit confirmation",Shadows.shadowOf(app).nextStartedActivity)
        assertEquals("Failed launch must preserve Spotlight",0,closed)
    }
    @Test fun alternativeOnlyLaunchesAfterExplicitConfirmation() {
        show();activate()
        ui.waitUntil(15000){ui.onAllNodes(isFocused() and hasText("Volver sin abrir")).fetchSemanticsNodes().size==1}
        assertNull(Shadows.shadowOf(app).peekNextStartedActivity())
        ui.onNodeWithText("Volver sin abrir").performKeyInput{pressKey(Key.Tab)}
        ui.onNodeWithTag("confirm-native-alternative").assertIsFocused().performKeyInput{pressKey(Key.Enter)}
        val launched=Shadows.shadowOf(app).nextStartedActivity
        assertEquals("test.browser",launched.component?.packageName)
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
        assertEquals(1,closed)
    }
    @Test fun escapeCancelsReviewAndKeepsQueryWithoutLaunching() {
        show();activate()
        ui.onNodeWithText("Volver sin abrir").performKeyInput{pressKey(Key.Escape)}
        ui.onNodeWithTag("native-launch-review").assertDoesNotExist()
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).assertTextEquals("vivaldi prueba")
        assertNull(Shadows.shadowOf(app).nextStartedActivity);assertEquals(0,closed)
    }
    @Test fun installedApplicationStillLaunchesOnceFromEnter() {
        show("Reader",realApp=true)
        val search=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
        search.performKeyInput{pressKey(Key.Enter)}
        assertEquals("test.reader",Shadows.shadowOf(app).nextStartedActivity.component?.packageName)
        search.performKeyInput{pressKey(Key.Enter)}
        assertNull(Shadows.shadowOf(app).nextStartedActivity);assertEquals(1,closed)
    }
    @Test fun changingQueryDiscardsAlternativeWithoutLaunch() {
        show();activate()
        val search=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
        search.performTextClearance();search.performTextInput("otra consulta")
        ui.onNodeWithTag("native-launch-review").assertDoesNotExist()
        assertNull(Shadows.shadowOf(app).nextStartedActivity);assertEquals(0,closed)
    }
    @Test fun enterOnDefaultReviewActionDoesNotConfirm() {
        show();activate()
        ui.waitUntil(15000){ui.onAllNodes(isFocused() and hasText("Volver sin abrir")).fetchSemanticsNodes().size==1}
        ui.onNodeWithText("Volver sin abrir").performKeyInput{pressKey(Key.Enter)}
        ui.onNodeWithTag("native-launch-review").assertDoesNotExist()
        assertNull(Shadows.shadowOf(app).nextStartedActivity);assertEquals(0,closed)
    }
    @Test @Config(qualifiers="w360dp-h800dp") fun phoneReviewAllowsExplicitTouchConfirmation() {
        show(width=320,theme="dark");activate()
        ui.onNodeWithTag("confirm-native-alternative").performScrollTo().assertIsDisplayed()
        ui.onNodeWithTag("spotlight-root").captureRoboImage("build/native-launch/phone-review.png",
            roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        assertNull(Shadows.shadowOf(app).peekNextStartedActivity())
        ui.onNodeWithTag("confirm-native-alternative").performClick()
        assertEquals("test.browser",Shadows.shadowOf(app).nextStartedActivity.component?.packageName)
        assertEquals(1,closed)
    }
    @Test fun closingInspectorCancelsPendingAlternative() {
        show();activate()
        ui.onNodeWithContentDescription("Cerrar inspector").performClick()
        ui.onNodeWithTag("native-launch-review").assertDoesNotExist()
        assertNull(Shadows.shadowOf(app).nextStartedActivity);assertEquals(0,closed)
    }
    @Test fun restoringReviewDoesNotRestoreConsentOrLaunch() {
        show();activate()
        restoration.emulateSavedInstanceStateRestore()
        ui.onNodeWithTag("native-launch-review").assertDoesNotExist()
        assertNull(Shadows.shadowOf(app).nextStartedActivity);assertEquals(0,closed)
    }

}
