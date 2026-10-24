package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h1100dp",application=SpotlightApp::class)
class AdaptiveResultsGeometryTest {
    @get:Rule val ui=createComposeRule()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private var viewport by mutableStateOf(1000f)
    private fun show(width:Int=800,count:Int=12,compact:Boolean=false) {
        val launcher=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        repeat(count) { index ->
            val pkg="fixture.geometry${index.toString().padStart(2,'0')}"
            val activity=ActivityInfo().apply {
                packageName=pkg;name="$pkg.Main";nonLocalizedLabel="Geometry ${index.toString().padStart(2,'0')}"
                applicationInfo=ApplicationInfo().apply{packageName=pkg}
            }
            Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(launcher,ResolveInfo().apply{activityInfo=activity})
        }
        app.search.invalidateApps()
        ui.setContent { Box(Modifier.width(width.dp)) {
            SpotlightPanel(app,AppConfig(files=false,animations=false,learning=false,compactSearchResults=compact),{},
                initialQuery="Geometry",availableHeightDp=viewport)
        } }
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:fixture.geometry00/fixture.geometry00.Main").fetchSemanticsNodes().isNotEmpty()}
    }
    private fun footerFits() {
        val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
        val footer=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
        assertTrue("Footer clipped: $footer outside $root",footer.bottom<=root.bottom)
        ui.onNodeWithTag("context-footer").assertIsDisplayed()
        assertTrue(root.bottom-root.top<=viewport.dp)
    }
    @Test fun tabletShowsTwelveMatchesWithoutManualExpansionOrScroll() {
        show();ui.onNodeWithText("Geometry 11").assertIsDisplayed();footerFits()
        val last=ui.onNodeWithText("Geometry 11").getUnclippedBoundsInRoot()
        assertTrue(last.bottom<=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot().top)
    }
    @Test @Config(qualifiers="w390dp-h844dp") fun phoneUsesActualRowHeightAndKeepsFooterVisible() {
        viewport=760f;show(width=360,count=8)
        ui.onNodeWithText("Geometry 07").assertIsDisplayed();footerFits()
    }
    @Test fun keyboardBudgetShrinksAndRestoresWithoutLosingMatches() {
        show();ui.onNodeWithText("Geometry 11").assertIsDisplayed()
        ui.runOnIdle{viewport=330f};footerFits()
        ui.onNodeWithText("Geometry 11").performScrollTo().assertIsDisplayed()
        ui.runOnIdle{viewport=1000f};ui.waitForIdle();footerFits()
        ui.onNodeWithText("Geometry 11").assertIsDisplayed()
    }
    @Test fun inspectorSharesTheViewportWithResultsAndFooter() {
        show();ui.onNodeWithText("Geometry 00").performClick()
        ui.onNodeWithTag("context-inspector").assertIsDisplayed();footerFits()
        val inspector=ui.onNodeWithTag("context-inspector").getUnclippedBoundsInRoot()
        assertTrue(inspector.bottom<=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot().top)
        ui.onNodeWithText("Acciones de app").assertExists()
    }
    @Test fun compactPreferenceKeepsSixAndManualExpansion() {
        show(compact=true)
        ui.onNodeWithText("Geometry 06").assertDoesNotExist()
        ui.onNodeWithTag("results-expand").performClick()
        ui.onNodeWithText("Geometry 11").performScrollTo().assertIsDisplayed()
        footerFits()
    }
}
