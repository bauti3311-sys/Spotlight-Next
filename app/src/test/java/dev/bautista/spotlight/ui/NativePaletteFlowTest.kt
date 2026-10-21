package dev.bautista.spotlight.ui

import android.content.*
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class,ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativePaletteFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun search()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
    private fun show(query:String="WhatsApp",width:Int=740,height:Float=640f,theme:String="dark") {
        val intent=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        listOf("com.whatsapp" to "WhatsApp","com.android.chrome" to "Chrome","com.spotify.music" to "Spotify").forEach { (pkg,title)->
            val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=title}
            val activity=ActivityInfo().apply{packageName=pkg;name="Main";nonLocalizedLabel=title;applicationInfo=a;exported=true}
            Shadows.shadowOf(app.packageManager).installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(activity)})
            Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(intent,ResolveInfo().apply{activityInfo=activity})
        }
        app.search.invalidateApps()
        ui.setContent{Box(Modifier.width(width.dp)) {SpotlightPanel(app,AppConfig(theme=theme,preset=if(theme=="light")"macos" else "glass",
            files=false,animations=false,learning=false,favorites=setOf("com.whatsapp/Main")),{},query,height)}}
        ui.waitUntil(15000){ui.onAllNodesWithTag(if(query.isBlank())"result:suggest:app:com.whatsapp" else "result:com.whatsapp/Main").fetchSemanticsNodes().isNotEmpty()}
    }
    private fun open(){search().performKeyInput{keyDown(Key.CtrlLeft);pressKey(Key.K);keyUp(Key.CtrlLeft)};ui.onNodeWithTag("action-surface").assertIsDisplayed()}
    private fun capture(name:String){ui.onNodeWithTag("spotlight-root").captureRoboImage("build/native-palette/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))}
    @Test fun compactHomeHasNoAutomaticInspector(){show("");ui.onNodeWithTag("context-inspector").assertDoesNotExist();assertTrue(ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot.height<=300*app.resources.displayMetrics.density);capture("01-home")}
    @Test fun searchIsSingleColumnAndOpeningActionsDoesNotResizeOrLaunch(){
        show();ui.onNodeWithTag("context-inspector").assertDoesNotExist()
        val before=ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot
        capture("02-whatsapp");open()
        assertEquals(before,ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot)
        search().assertTextEquals("WhatsApp");ui.onNodeWithTag("result:com.whatsapp/Main").assertIsSelected()
        assertNull(Shadows.shadowOf(app).peekNextStartedActivity());capture("03-actions")
    }
    @Test fun filteringActionsNeverChangesQueryOrExecutesAndEscapeRestoresFocus(){
        show();open();ui.onNodeWithTag("action-filter").performTextInput("información")
        search().assertTextEquals("WhatsApp");ui.onNodeWithText("Información de la app").assertExists()
        assertNull(Shadows.shadowOf(app).peekNextStartedActivity());capture("04-filter")
        ui.onNodeWithTag("action-filter").performKeyInput{pressKey(Key.Escape)}
        search().assertIsFocused().assertTextEquals("WhatsApp");ui.onNodeWithTag("action-surface").assertDoesNotExist()
    }
    @Test fun copySubmenuReturnsToSameRootSelectionBeforeClosing(){
        show();open();ui.onNodeWithText("Copiar…").performScrollTo().performClick()
        ui.onNodeWithText("Copiar paquete").assertExists();search().assertTextEquals("WhatsApp");capture("05-submenu")
        ui.onNodeWithTag("action-filter").performKeyInput{pressKey(Key.Escape)}
        ui.onNode(hasText("Copiar…") and isFocused()).assertExists()
        ui.onNode(hasText("Copiar…") and isFocused()).performKeyInput{pressKey(Key.Escape)}
        search().assertIsFocused().assertTextEquals("WhatsApp")
    }
    @Test fun briefTapSelectsWhileLongPressOpensActionsWithoutLaunching(){
        show();ui.onNodeWithTag("result:com.whatsapp/Main").performClick().assertIsSelected()
        ui.onNodeWithTag("action-surface").assertDoesNotExist();assertNull(Shadows.shadowOf(app).peekNextStartedActivity())
        ui.onNodeWithTag("result:com.whatsapp/Main").performTouchInput{longClick()};ui.onNodeWithTag("action-surface").assertIsDisplayed()
    }
    @Test fun enterFromSearchLaunchesTheRealPrimaryIntent(){
        show();search().performKeyInput{pressKey(Key.Enter)}
        val intent=Shadows.shadowOf(app).nextStartedActivity;assertNotNull(intent);assertEquals("com.whatsapp",intent.component?.packageName)
    }
    @Test fun constrainedImeBudgetKeepsActionFilterAndCloseInsideWindow(){
        show(height=260f);open();ui.onNodeWithTag("action-filter").assertIsDisplayed()
        ui.onNodeWithContentDescription("Cerrar acciones").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        val window=ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot
        val panel=ui.onNodeWithTag("action-surface").fetchSemanticsNode().boundsInRoot
        assertTrue(panel.top>=window.top && panel.bottom<=window.bottom);capture("07-ime-budget-simulation")
    }
    @Test @Config(qualifiers="w390dp-h844dp") fun phoneUsesSameFloatingActionsAndCanReachLastEntry(){
        show(width=360);open();ui.onNodeWithTag("action-filter").assertIsDisplayed()
        ui.onNodeWithText("Información de la app").performScrollTo().assertIsDisplayed();capture("08-phone")
        ui.onNodeWithContentDescription("Cerrar acciones").performClick();search().assertIsFocused()
    }
    @Test fun lightSearchAndActionsUseSameStructure(){show(theme="light");capture("10-light-search");open();capture("10-light-actions")}
    @Test fun localNoteDetailAppearsOnlyOnExplicitPreviewAndPreservesQuery() {
        runBlocking { app.local.saveNote("Palettedetail\n"+"Contenido local autorizado para revisar. ".repeat(20)) }
        ui.setContent{Box(Modifier.width(960.dp)){SpotlightPanel(app,AppConfig(files=false,animations=false,theme="dark",preset="glass"),{},"Palettedetail",640f)}}
        ui.waitUntil(15000){ui.onAllNodes(SemanticsMatcher("local note row"){it.config.getOrElse(androidx.compose.ui.semantics.SemanticsProperties.TestTag){""}.startsWith("result:note:")}).fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithTag("context-inspector").assertDoesNotExist()
        search().performKeyInput{pressKey(Key.DirectionDown);pressKey(Key.DirectionUp);pressKey(Key.Spacebar)}
        ui.onNodeWithTag("context-inspector").assertIsDisplayed()
        search().assertTextEquals("Palettedetail");capture("06-local-note-detail")
        search().performKeyInput{pressKey(Key.Escape)}
        ui.onNodeWithTag("context-inspector").assertDoesNotExist();search().assertTextEquals("Palettedetail").assertIsFocused()
    }
    @Test fun unavailableActionCannotExecuteAndFilteringPreservesIdentity(){
        var called=0
        ui.setContent{SpotlightTheme("dark"){Box(Modifier.width(390.dp).height(360.dp)){
            ActionPanelView(listOf(ContextAction("Abrir","",CommandIcons.Apps,{},id="primary"),
                ContextAction("Compartir A","",CommandIcons.Share,{called++},id="share-a"),
                ContextAction("Compartir B","",CommandIcons.Share,{called++},id="share-b",state=ContextActionState.Unavailable)),FocusRequester())
        }}}
        ui.onNodeWithTag("action-filter").performTextInput("Compartir")
        ui.onNodeWithTag("action-filter").performKeyInput{pressKey(Key.DirectionDown);pressKey(Key.Enter)}
        assertEquals(0,called);ui.onNodeWithText("No disponible actualmente").assertExists()
        ui.onNodeWithTag("action-filter").performTextClearance();ui.onNode(hasText("Compartir B") and isFocused()).assertExists()
    }
}
