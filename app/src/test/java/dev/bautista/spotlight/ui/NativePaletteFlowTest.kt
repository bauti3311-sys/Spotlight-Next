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
    private fun show(query:String="WhatsApp",width:Int=740,height:Float=640f,theme:String="dark",expectedPackage:String="com.whatsapp") {
        val intent=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        listOf("com.whatsapp" to "WhatsApp","com.android.chrome" to "Chrome","com.spotify.music" to "Spotify").forEach { (pkg,title)->
            val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=title}
            val activity=ActivityInfo().apply{packageName=pkg;name="Main";nonLocalizedLabel=title;applicationInfo=a;exported=true}
            Shadows.shadowOf(app.packageManager).installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(activity)})
            Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(intent,ResolveInfo().apply{activityInfo=activity})
        }
        app.search.invalidateApps()
        ui.setContent{Box(Modifier.width(width.dp)) {SpotlightPanel(app,AppConfig(theme=theme,preset=if(theme=="light")"macos" else AppConfig().preset,
            files=false,animations=false,learning=false,favorites=setOf("com.whatsapp/Main")),{},query,height)}}
        ui.waitUntil(15000){ui.onAllNodesWithTag(if(query.isBlank())"result:suggest:app:com.whatsapp" else "result:$expectedPackage/Main").fetchSemanticsNodes().isNotEmpty()}
    }
    private fun open(){search().performKeyInput{keyDown(Key.CtrlLeft);pressKey(Key.K);keyUp(Key.CtrlLeft)};ui.onNodeWithTag("action-surface").assertIsDisplayed()}
    private fun capture(name:String){
        val options=RoborazziOptions(taskType=RoborazziTaskType.Record)
        if(ui.onAllNodesWithTag("action-surface").fetchSemanticsNodes().isNotEmpty()) {
            // Popup is a separate Android root; palette-only captures omit it.
            captureScreenRoboImage("build/native-palette/$name.png",roborazziOptions=options)
            ui.onNodeWithTag("action-surface").captureRoboImage("build/native-palette/$name-panel.png",roborazziOptions=options)
        } else ui.onNodeWithTag("spotlight-root").captureRoboImage("build/native-palette/$name.png",roborazziOptions=options)
    }
    @Test fun spotifyPrefixShowsActualInstalledResultAndFloatingActions() {
        show("spot",expectedPackage="com.spotify.music")
        ui.onNodeWithTag("result:com.spotify.music/Main").assertIsSelected()
        search().assertTextEquals("spot");capture("11-spotify-prefix")
        val before=ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot
        open();capture("12-spotify-actions")
        val last=ui.onNodeWithText("Información de la app").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val surface=ui.onNodeWithTag("action-surface").fetchSemanticsNode().boundsInRoot
        assertTrue("All usual app actions fit without scrolling on tablet",last.bottom<=surface.bottom-8*app.resources.displayMetrics.density)
        assertEquals(before,ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot)
        search().assertTextEquals("spot");assertNull(Shadows.shadowOf(app).peekNextStartedActivity())
    }
    @Test fun spotifyExactMasterSceneUsesOnlyAvailableResultsAndRestoresSearch() {
        show("Spotify",expectedPackage="com.spotify.music")
        ui.onNodeWithText("RESULTADOS").assertIsDisplayed()
        ui.onNodeWithTag("result:com.spotify.music/Main").assertIsSelected()
        val original=ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot
        open();capture("13-spotify-master")
        assertEquals(original,ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot)
        ui.onNodeWithText("Descubrimiento Semanal").assertDoesNotExist()
        ui.onNodeWithContentDescription("Cerrar acciones").performClick()
        search().assertIsFocused().assertTextEquals("Spotify")
    }
    @Test fun selectedApplicationColorsTheSearchGlyphAndHomeKeepsActualSuggestions() {
        show("Spotify",expectedPackage="com.spotify.music")
        val path="build/native-palette/14-search-context-accent.png"
        ui.onNodeWithTag("search-context-accent").captureRoboImage(path,roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        val bitmap=android.graphics.BitmapFactory.decodeFile(path)
        assertNotNull(bitmap)
        var green=0
        for(y in 0 until bitmap.height)for(x in 0 until bitmap.width) {
            val pixel=bitmap.getPixel(x,y)
            if(android.graphics.Color.green(pixel)>120 && android.graphics.Color.green(pixel)>android.graphics.Color.red(pixel)*1.3f)green++
        }
        bitmap.recycle();assertTrue("The actual search glyph is Spotify green",green>2)
        search().performTextClearance()
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:suggest:app:com.whatsapp").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithTag("result:suggest:app:com.whatsapp").assertIsDisplayed()
        ui.onNodeWithTag("home-suggestion-hint").assertDoesNotExist()
        capture("15-home-real-suggestion")
    }
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
        val choice=ui.onNode(hasText("Copiar paquete") and hasClickAction()).performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        val choiceBounds=choice.fetchSemanticsNode().boundsInRoot
        val panelBounds=ui.onNodeWithTag("action-surface").fetchSemanticsNode().boundsInRoot
        val filterBounds=ui.onNodeWithTag("action-filter").fetchSemanticsNode().boundsInRoot
        assertTrue("Approved filter is above the submenu rows",choiceBounds.top>=filterBounds.bottom)
        assertTrue("Submenu row stays inside the panel",choiceBounds.bottom<=panelBounds.bottom && choiceBounds.left>=panelBounds.left && choiceBounds.right<=panelBounds.right)
        search().assertTextEquals("WhatsApp");capture("05-submenu")
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
        show(height=260f)
        val original=ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot
        open();ui.onNodeWithTag("action-filter").assertIsDisplayed()
        ui.onNodeWithContentDescription("Cerrar acciones").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        val first=ui.onNode(hasText("Abrir") and hasClickAction()).assertIsDisplayed().assertHeightIsAtLeast(48.dp).fetchSemanticsNode().boundsInRoot
        val filter=ui.onNodeWithTag("action-filter").fetchSemanticsNode().boundsInRoot
        assertTrue("Approved filter is above the primary action",first.top>=filter.bottom)
        val window=ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot
        val panel=ui.onNodeWithTag("action-surface").fetchSemanticsNode().boundsInRoot
        assertEquals(original,window)
        // A child surface can extend beyond the compact parent; the IME budget still bounds it.
        assertTrue(panel.height<=260*app.resources.displayMetrics.density)
        captureScreenRoboImage("build/native-palette/07-ime-budget-simulation.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
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
        // Compose text replacement explicitly requests editor focus; selection must still keep its ID.
        val editor=ui.onNodeWithTag("action-filter")
        editor.performTextClearance()
        editor.assertIsFocused()
        editor.assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.EditableText, androidx.compose.ui.text.AnnotatedString("")))
        ui.onNode(hasText("Compartir B") and isSelected()).assertExists().assertIsNotEnabled()
        editor.performKeyInput{pressKey(Key.Enter)}
        assertEquals(0,called)
        editor.performKeyInput{pressKey(Key.DirectionUp);pressKey(Key.Enter)}
        assertEquals(1,called)
        ui.onNode(hasText("Compartir A") and isFocused()).assertExists()
    }
}
