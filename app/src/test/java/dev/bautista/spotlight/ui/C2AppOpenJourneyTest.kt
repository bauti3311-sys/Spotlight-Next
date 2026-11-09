package dev.bautista.spotlight.ui

import android.content.*
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.AppCapabilities
import dev.bautista.spotlight.control.IntegrationRegistry
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Real Focus/search/inspector/footer/executor, synthetic Android app; no external effect verified. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1000dp-h1000dp-mdpi",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalTestApi::class)
class C2AppOpenJourneyTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=ui.activity.application as SpotlightApp
 private val pkg="fixture.atlas.reader"
 private val id="$pkg/Main"
 private val calls=mutableListOf<Intent>()
 private var attempts=0
 private var closed=0
 private var failure:Exception?=null
 private fun install(permission:String?=null,exported:Boolean=true) {
  val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel="Atlas Reader";enabled=true}
  val activity=ActivityInfo().apply{packageName=pkg;name="Main";applicationInfo=a;nonLocalizedLabel="Atlas Reader";this.exported=exported;this.permission=permission;enabled=true}
  val pm=Shadows.shadowOf(app.packageManager)
  pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(activity)})
  val info=ResolveInfo().apply{activityInfo=activity}
  pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),info)
  pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(pkg),info)
  app.search.invalidateApps()
 }
 private fun show(pin:Boolean=false) {
  install()
  val wrapper=object:ContextWrapper(ui.activity) {
   override fun startActivity(intent:Intent) {
    attempts++;failure?.let{throw it};calls+=Intent(intent)
    // This deliberately records a request, never an external app's completion.
   }
  }
  ui.setContent{CompositionLocalProvider(LocalContext provides wrapper){Box(Modifier.width(900.dp)){
   SpotlightPanel(app,AppConfig(focusInterface=true,theme="dark",files=false,contacts=false,online=false,learning=false,suggestions=false,animations=false),{closed++},"Atlas Reader",680f)
  }}}
  ui.waitUntil(15000){ui.onAllNodesWithTag("result:$id").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithTag("result:$id").performClick()
  ui.onNodeWithTag("inspector-title").assertTextEquals("Atlas Reader")
  assertEquals(0,attempts)
  if(pin)ui.onNodeWithTag("inspector-pin").performClick()
 }
 private fun editor()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
 private fun open()=ui.onNode(hasText("Abrir aplicación") and hasClickAction())
 private fun coherent() {
  editor().assertTextEquals("Atlas Reader")
  ui.onNodeWithTag("result:$id").assertIsSelected()
  ui.onNodeWithTag("inspector-title").assertTextEquals("Atlas Reader")
 }
 @Test fun availableLauncherRequestedFromInspectorContextPublishesHonestReceipt() {
  show(pin=true)
  val probe=Intent(Intent.ACTION_MAIN).setComponent(ComponentName(pkg,"Main"))
  assertNull(IntegrationRegistry(ui.activity).launchAccess(probe).problem)
  assertTrue("No invented public search/share capability",AppCapabilities(ui.activity).available(pkg).isEmpty())
  open().performClick()
  assertEquals(1,attempts);assertEquals(ComponentName(pkg,"Main"),calls.single().component)
  coherent();assertEquals(0,closed)
  ui.onNodeWithTag("native-launch-request").assertIsDisplayed().assertTextEquals("Solicitud enviada a Android · Atlas Reader · $pkg\nResultado externo sin comprobar.")
 }
 @Test fun rapidTouchAndEnterShareOneRequestFence() {
  show(pin=true);open().performClick();open().performClick()
  assertEquals(1,attempts);assertEquals(1,calls.size)
  ui.onNodeWithTag("native-launch-review").assertExists()
  ui.onNodeWithText("Volver sin abrir").performKeyInput{pressKey(Key.Enter)}
  ui.onNodeWithTag("native-launch-review").assertDoesNotExist()
  coherent();assertEquals(1,attempts)
 }
 @Test fun removedPackageCannotDispatchAndCancelRestoresSearchAndInspector() {
  show();Shadows.shadowOf(app.packageManager).removePackage(pkg)
  open().performClick()
  assertEquals(0,attempts);assertTrue(calls.isEmpty())
  ui.onNodeWithText("El destino ya no está disponible. Revisá la app elegida o sus ajustes.").assertExists()
  ui.onNodeWithTag("confirm-native-alternative").assertDoesNotExist()
  ui.onNodeWithText("Volver sin abrir").performKeyInput{pressKey(Key.Escape)}
  coherent();assertEquals(0,closed)
 }
 @Test fun permissionRaceReportsFailureWithoutFallbackRetryOrPrivateDetails() {
  show();failure=SecurityException("fixture-secret-path")
  open().performClick();assertEquals(1,attempts);assertTrue(calls.isEmpty())
  ui.onNodeWithText("Android no permite abrir este destino. Revisá sus permisos y volvé a seleccionarlo.").assertExists()
  ui.onNodeWithText("fixture-secret-path",substring=true).assertDoesNotExist()
  ui.onNodeWithTag("confirm-native-alternative").assertDoesNotExist()
  ui.onNodeWithTag("native-launch-request").assertDoesNotExist()
  ui.onNodeWithText("Volver sin abrir").performClick();coherent();assertEquals(1,attempts)
 }
 @Test fun escapeBeforeRequestAndExplicitEnterDoNotLoseQueryOrSelection() {
  show();editor().performKeyInput{pressKey(Key.Escape)}
  assertEquals(0,attempts);editor().assertTextEquals("Atlas Reader")
  ui.onNodeWithTag("result:$id").performClick()
  editor().performKeyInput{pressKey(Key.Enter)}
  assertEquals(1,attempts);assertEquals(pkg,calls.single().component?.packageName);assertEquals(1,closed)
 }
 @Test fun stopAndResumeKeepSearchSelectionInspectorWithoutReplayingRequest() {
  show(pin=true);open().performClick();assertEquals(1,attempts)
  ui.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
  ui.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
  ui.waitForIdle();coherent();assertEquals(1,attempts);assertEquals(0,closed)
 }
}
