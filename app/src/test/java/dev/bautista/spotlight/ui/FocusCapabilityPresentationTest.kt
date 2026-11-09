package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.ActionCatalog
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FocusCapabilityPresentationTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 private val pkg="com.vivaldi.browser"
 private fun install():Intent {
  val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel="Vivaldi";enabled=true}
  val ai=ActivityInfo().apply{packageName=pkg;name="Main";applicationInfo=a;enabled=true;exported=true}
  val pm=Shadows.shadowOf(app.packageManager)
  pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(ai)})
  val intent=(ActionCatalog.searchAction("vivaldi",pkg,"consulta") as Action.InApp).intent
  pm.addResolveInfoForIntent(intent,ResolveInfo().apply{activityInfo=ai})
  return intent
 }
 @Test fun promotedSearchIsUniqueAndRevokedCapabilityNeverOpensAForm() {
  val probe=install();val prepared=mutableListOf<Action>()
  ui.setContent{SpotlightTheme("dark",AppConfig(focusInterface=true,theme="dark")){Surface(Modifier.width(360.dp)){
   AppCapabilityActions(pkg,appInfoAlreadyVisible=true,compact=true){prepared+=it}
  }}}
  ui.waitUntil(15000){ui.onAllNodesWithTag("app-promoted-search").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithTag("inspector-more-actions").performClick()
  ui.onAllNodesWithText("Buscar en Vivaldi").assertCountEquals(1)
  ui.runOnIdle{Shadows.shadowOf(app.packageManager).removeResolveInfosForIntent(probe,pkg)}
  ui.onNodeWithTag("app-promoted-search").performClick()
  ui.waitUntil(15000){ui.onAllNodesWithText("La acción ya no está disponible. Revisá la app y sus permisos.").fetchSemanticsNodes().isNotEmpty()}
  assertTrue(prepared.isEmpty())
  ui.onNodeWithTag("app-promoted-search").assertDoesNotExist()
  ui.onNodeWithTag("app-action-catalog:vivaldi:search").assertIsNotEnabled()
  assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
 @Test fun disablingFocusKeepsTheLegacySearchAvailableWithoutPromotion() {
  install();val prepared=mutableListOf<Action>()
  ui.setContent{SpotlightTheme("light",AppConfig(theme="light",focusInterface=false)){Surface(Modifier.width(470.dp)){
   AppCapabilityActions(pkg,appInfoAlreadyVisible=true){prepared+=it}
  }}}
  ui.onNodeWithTag("app-promoted-search").assertDoesNotExist()
  ui.waitUntil(15000){ui.onAllNodes(hasTestTag("app-action-catalog:vivaldi:search") and isEnabled()).fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithTag("app-action-catalog:vivaldi:search").performClick()
  ui.waitUntil(15000){prepared.isNotEmpty()}
  assertEquals(listOf(Action.ComposeSearch("vivaldi",pkg)),prepared)
 }
 @Test fun reviewNamesTheActualPreparedReceiverAndDoesNotChangeTheAction() {
  val intent=install();val plan=Action.InApp(intent,Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://www.google.com/")))
  val review=FocusSearchReviewController().apply{prepared=plan}
  ui.setContent{SpotlightTheme("light",AppConfig(theme="light",focusInterface=true)){Surface(Modifier.width(360.dp)){
   FocusSearchTaskBody(app,Action.ComposeSearch("vivaldi","incorrect.fixture.package","consulta"),review,false,0)
  }}}
  ui.onNodeWithText("Abrir con").assertExists();ui.onNodeWithText("Vivaldi").assertExists()
  ui.onNodeWithText("Sitio web").assertExists();ui.onNodeWithText("www.google.com").assertExists()
  ui.onNodeWithText("incorrect.fixture.package").assertDoesNotExist()
  assertSame(plan,review.prepared)
  assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
}
