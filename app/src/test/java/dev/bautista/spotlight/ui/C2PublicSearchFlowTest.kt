package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import dev.bautista.spotlight.data.Config as AppConfig
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
class C2PublicSearchFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun install(pkg:String="org.wikipedia",title:String="Wikipedia",web:Boolean=false) {
        val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=title;enabled=true}
        val main=ActivityInfo().apply{packageName=pkg;name="Main";nonLocalizedLabel=title;applicationInfo=a;exported=true;enabled=true}
        val search=ActivityInfo().apply{packageName=pkg;name="$pkg.Search";applicationInfo=a;exported=true;enabled=true}
        val pm=Shadows.shadowOf(app.packageManager)
        pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(main,search)})
        pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=main})
        pm.addResolveInfoForIntent(Intent(if(web)Intent.ACTION_WEB_SEARCH else Intent.ACTION_SEARCH).setPackage(pkg),ResolveInfo().apply{activityInfo=search})
        app.search.invalidateApps()
    }
    private fun capture(name:String) {
        val phase=System.getenv("SPOTLIGHT_C2_CAPTURE_PHASE") ?: "after"
        ui.onNodeWithTag("c2-search-form").captureRoboImage("build/c2/$phase/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
    private fun form(theme:String) {
        install();app.contexts.setText("luna azul de prueba","Texto compartido")
        ui.setContent {SpotlightTheme(theme){Box(Modifier.width(360.dp).testTag("c2-search-form")){
            SearchEditor(Action.ComposeSearch("app","org.wikipedia"),{}, {})
        }}}
        ui.onNodeWithText("Qué querés buscar").assertExists();capture("phone-$theme-shared-search")
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun darkSharedSearchFormUsesExistingComposition()=form("dark")
    @Test fun lightSharedSearchFormUsesExistingComposition()=form("light")
    @Test fun unavailableCatalogSearchIsDisabledInsteadOfAdvertisedAsExecutable() {
        ui.setContent{SpotlightTheme{AppCapabilityActions("com.android.chrome") {}}}
        ui.onNodeWithTag("app-action-catalog:chrome:search").assertIsNotEnabled()
    }    @Test fun contextualQueryOpensReviewAndRequestsTheExactPublicActivityOnce() {
        install();var closed=0
        ui.setContent {Box(Modifier.width(360.dp)){SpotlightPanel(app,
            AppConfig(files=false,online=false,contacts=false,animations=false,learning=false),{closed++},"Wikipedia luna",700f)}}
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:context:app-search:org.wikipedia/Main").fetchSemanticsNodes().isNotEmpty()}
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).performKeyInput{pressKey(Key.Enter)}
        ui.onNode(hasSetTextAction() and hasText("luna")).assertExists()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
        ui.onNodeWithText("Buscar").performClick()
        val request=Shadows.shadowOf(app).nextStartedActivity
        assertNotNull(request);assertEquals(Intent.ACTION_SEARCH,request.action)
        assertEquals("org.wikipedia",request.component!!.packageName)
        assertEquals("org.wikipedia.Search",request.component!!.className)
        assertEquals("luna",request.getStringExtra(android.app.SearchManager.QUERY))
        assertEquals("An active contextual task stays open after requesting Android",0,closed)
        ui.onNode(hasSetTextAction() and hasText("luna")).assertExists()
        ui.onNodeWithText("Buscar").performClick()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun removedAppCannotOpenTheStalePreparedSearchForm() {
        install()
        ui.setContent {Box(Modifier.width(360.dp)){SpotlightPanel(app,
            AppConfig(files=false,online=false,contacts=false,animations=false,learning=false),{},"Wikipedia luna",700f)}}
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:context:app-search:org.wikipedia/Main").fetchSemanticsNodes().isNotEmpty()}
        ui.runOnIdle{Shadows.shadowOf(app.packageManager).removePackage("org.wikipedia")}
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).performKeyInput{pressKey(Key.Enter)}
        ui.onNodeWithText("La búsqueda de esta app ya no está disponible. Revisá la instalación y sus permisos.").assertExists()
        ui.onNodeWithText("Qué querés buscar").assertDoesNotExist()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test @Config(qualifiers="w1280dp-h900dp")
    fun exactAppInspectorPreparesLiveSearchAndEscapeReturnsToItsQuery() {
        install()
        ui.setContent {Box(Modifier.width(960.dp)){SpotlightPanel(app,
            AppConfig(files=false,online=false,contacts=false,animations=false,learning=false,preset="glass"),{},"Wikipedia",700f)}}
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:org.wikipedia/Main").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithTag("result:org.wikipedia/Main").performClick()
        ui.waitUntil(15000){ui.onAllNodesWithTag("app-capability-search").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithTag("app-capability-search").performClick()
        ui.waitUntil(15000){ui.onAllNodesWithText("Qué querés buscar").fetchSemanticsNodes().isNotEmpty()}
        ui.onNode(hasSetTextAction() and hasText("")).performTextInput("órbita")
        ui.onNodeWithText("Buscar").performClick()
        val request=Shadows.shadowOf(app).nextStartedActivity
        assertNotNull(request);assertEquals("org.wikipedia",request.component!!.packageName)
        assertEquals("órbita",request.getStringExtra(android.app.SearchManager.QUERY))
        ui.onNodeWithTag("spotlight-root").performKeyInput{pressKey(Key.Escape)}
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).assertTextEquals("Wikipedia").assertIsFocused()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun sharedTextRequiresSelectionAndASeparateSearchRequest() {
        install();val calls=mutableListOf<Action>()
        app.contexts.setText("luna azul de prueba","Texto compartido")
        ui.setContent{SpotlightTheme{SearchEditor(Action.ComposeSearch("app","org.wikipedia"),{calls+=it},{})}}
        assertEquals("",ui.onNode(hasSetTextAction()).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.EditableText].text)
        assertTrue(calls.isEmpty())
        ui.onNodeWithTag("search-use-context").performClick()
        assertEquals("luna azul de prueba",ui.onNode(hasSetTextAction()).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.EditableText].text)
        assertTrue(calls.isEmpty());assertNull(Shadows.shadowOf(app).nextStartedActivity)
        ui.onNodeWithText("Buscar").performClick()
        val request=calls.single() as Action.InApp
        assertEquals("org.wikipedia",request.intent.`package`)
        assertEquals("luna azul de prueba",request.intent.getStringExtra(android.app.SearchManager.QUERY))
    }
    @Test fun largeSharedTextIsExplicitlyBoundedBeforeSearch() {
        install();val calls=mutableListOf<Action>()
        app.contexts.setText("x".repeat(5000),"Texto compartido largo")
        ui.setContent{SpotlightTheme{SearchEditor(Action.ComposeSearch("app","org.wikipedia"),{calls+=it},{})}}
        ui.onNodeWithTag("search-use-context").performClick()
        val length=ui.onNode(hasSetTextAction()).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.EditableText].text.length
        assertEquals(4000,length);assertTrue(calls.isEmpty())
        ui.onNodeWithText("Buscar").performClick()
        assertEquals(4000,(calls.single() as Action.InApp).intent.getStringExtra(android.app.SearchManager.QUERY)!!.length)
    }
    @Test fun androidSharedTextReviewKeepsFooterAndActionsReachableWithImeBudget() {
        install()
        val controller=Robolectric.buildActivity(dev.bautista.spotlight.ShareContextActivity::class.java,
            Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,"luna compartida explícitamente")).create()
        try {
            ui.waitUntil(10000){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();controller.get().isFinishing}
            val entry=Shadows.shadowOf(app).nextStartedActivity
            assertEquals("dev.bautista.spotlight.InvokeActivity",entry.component!!.className)
        }finally{controller.destroy()}
        assertEquals("luna compartida explícitamente",app.contexts.active.value!!.text)
        var viewport by mutableStateOf(700f)
        ui.setContent{Box(Modifier.width(360.dp)){SpotlightPanel(app,
            AppConfig(files=false,online=false,contacts=false,animations=false,learning=false,preset="glass"),{},"Wikipedia luna",viewport)}}
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:context:app-search:org.wikipedia/Main").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Preparar búsqueda").assertIsDisplayed()
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).performKeyInput{pressKey(Key.Enter)}
        ui.runOnIdle{viewport=320f}
        ui.onNodeWithTag("search-use-context").performScrollTo().assertIsDisplayed().performClick()
        ui.onNode(hasSetTextAction() and hasText("luna compartida explícitamente")).assertExists()
        ui.onNodeWithText("Buscar").performScrollTo().assertIsDisplayed()
        ui.onNodeWithTag("context-footer").assertIsDisplayed()
        val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
        assertTrue(ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot().bottom<=root.bottom)
        ui.onNodeWithTag("spotlight-root").captureRoboImage("build/c2/after/phone-shared-ime.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        ui.runOnIdle{viewport=700f};ui.waitForIdle()
        ui.onNodeWithTag("context-footer").assertIsDisplayed()
        ui.onNodeWithTag("spotlight-root").captureRoboImage("build/c2/after/phone-shared-restored.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
        ui.onNodeWithText("Buscar").performClick()
        val request=Shadows.shadowOf(app).nextStartedActivity
        assertEquals("org.wikipedia",request.component!!.packageName)
        assertEquals("luna compartida explícitamente",request.getStringExtra(android.app.SearchManager.QUERY))
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun compactAppInspectorScrollKeepsFooterInsideShortViewport() {
        install();var viewport by mutableStateOf(700f)
        ui.setContent{Box(Modifier.width(360.dp)){SpotlightPanel(app,
            AppConfig(files=false,online=false,contacts=false,animations=false,learning=false,preset="glass"),{},"Wikipedia",viewport)}}
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:org.wikipedia/Main").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithTag("result:org.wikipedia/Main").performClick()
        ui.onNodeWithTag("inspector-title").assertTextEquals("Wikipedia")
        ui.runOnIdle{viewport=320f};ui.waitForIdle()
        val phase=System.getenv("SPOTLIGHT_C2_CAPTURE_PHASE") ?: "after"
        ui.onNodeWithTag("spotlight-root").captureRoboImage("build/c2/$phase/phone-app-inspector-ime.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
        assertTrue("Footer is outside the short window",ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot().bottom<=root.bottom)
        ui.onNodeWithTag("context-footer").assertIsDisplayed()
        repeat(2){ui.onNodeWithTag("compact-inspector-scroll").performTouchInput{swipeUp()}}
        ui.onNodeWithText("org.wikipedia").assertIsDisplayed()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }

    @Test fun explicitQueryUsesSelectedWebSearchHandlerAndKeepsEditableReview() {
        install("fixture.webatlas","Web Atlas",web=true)
        ui.setContent{Box(Modifier.width(360.dp)){SpotlightPanel(app,
            AppConfig(files=false,online=false,contacts=false,animations=false,learning=false),{},"buscar en Web Atlas: órbita",700f)}}
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:context:app-search:fixture.webatlas/Main").fetchSemanticsNodes().isNotEmpty()}
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).performKeyInput{pressKey(Key.Enter)}
        ui.onNode(hasSetTextAction() and hasText("órbita")).performTextReplacement("órbita lunar")
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
        ui.onNodeWithText("Buscar").performClick()
        val intent=Shadows.shadowOf(app).nextStartedActivity
        assertEquals(Intent.ACTION_WEB_SEARCH,intent.action)
        assertEquals("fixture.webatlas",intent.component!!.packageName)
        assertEquals("órbita lunar",intent.getStringExtra(android.app.SearchManager.QUERY))
        ui.onNodeWithTag("spotlight-root").performKeyInput{pressKey(Key.Escape)}
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).assertIsFocused()
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }

    @Test fun acceptedSearchShowsRequestReceiptWithoutClaimingExternalEffect() {
        install()
        ui.setContent{Box(Modifier.width(360.dp)){SpotlightPanel(app,
            AppConfig(theme="dark",preset="glass",files=false,online=false,contacts=false,animations=false,learning=false),{},"Wikipedia luna",700f)}}
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:context:app-search:org.wikipedia/Main").fetchSemanticsNodes().isNotEmpty()}
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).performKeyInput{pressKey(Key.Enter)}
        ui.onNodeWithText("Buscar").performClick()
        assertNotNull(Shadows.shadowOf(app).nextStartedActivity)
        val phase=System.getenv("SPOTLIGHT_C2_NEXT_PHASE") ?: "after"
        ui.onNodeWithTag("spotlight-root").captureRoboImage("build/c2-next/$phase/request-status.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        ui.onNodeWithTag("native-launch-request").assertTextContains("Solicitud enviada a Android",substring=true)
            .assertTextContains("org.wikipedia",substring=true).assertTextContains("Resultado externo sin comprobar",substring=true)
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }

}
