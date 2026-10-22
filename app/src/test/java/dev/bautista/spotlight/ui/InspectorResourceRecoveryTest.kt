package dev.bautista.spotlight.ui

import android.content.*
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.lifecycle.Lifecycle
import dev.bautista.spotlight.search.*
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.shadows.ShadowContentResolver
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.github.takahirom.roborazzi.*

class RecoveryDocumentProvider:ContentProvider() {
 var allowed=true;var opened=0
 override fun onCreate()=true
 override fun getType(uri:Uri)="text/plain"
 override fun query(uri:Uri,p:Array<out String>?,s:String?,a:Array<out String>?,o:String?):Cursor?=null
 override fun insert(uri:Uri,v:ContentValues?):Uri?=null
 override fun delete(uri:Uri,s:String?,a:Array<out String>?)=0
 override fun update(uri:Uri,v:ContentValues?,s:String?,a:Array<out String>?)=0
 override fun openFile(uri:Uri,mode:String):ParcelFileDescriptor {
  if(!allowed)throw SecurityException("Revoked test grant")
  opened++;val f=java.io.File(context!!.cacheDir,"recovery-fixture.txt");f.writeText("Contenido documental autorizado")
  return ParcelFileDescriptor.open(f,ParcelFileDescriptor.MODE_READ_ONLY)
 }
}
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=android.app.Application::class,qualifiers="w800dp-h700dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class InspectorResourceRecoveryTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val descriptor=Result("file:fixture","Documento autorizado","Texto local","Archivos",1,Action.Launch(Intent(Intent.ACTION_VIEW,Uri.parse("content://recovery/document/one")).putExtra("unsafe","not persisted").addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)),mime="text/plain",preview="extracted body must not persist")
 private fun provider():RecoveryDocumentProvider {
  val p=RecoveryDocumentProvider();p.attachInfo(ui.activity,ProviderInfo().apply{authority="recovery"});ShadowContentResolver.registerProviderInternal("recovery",p);return p
 }
 @Test fun descriptorDoesNotRestoreIntentExtrasGrantsOrExtractedBody() {
  val raw=with(DocumentPreviewSaver){SaverScope{true}.save(descriptor)}!!
  val restored=DocumentPreviewSaver.restore(raw)!!
  val intent=(restored.action as Action.Launch).intent
  assertEquals(0,intent.flags);assertNull(intent.extras);assertNull(restored.preview);assertEquals(descriptor.id,restored.id)
  assertNull(DocumentPreviewSaver.restore(org.json.JSONObject(raw).put("uri","file:///forbidden").toString()))
 }
 @Test fun restoredDocumentAndPageKeepIdentityAndRecheckAccess() {
  val provider=provider();val restore=StateRestorationTester(ui)
  restore.setContent {SpotlightTheme("dark",AppConfig(theme="dark")){
   var doc by rememberSaveable(stateSaver=DocumentPreviewSaver){mutableStateOf<Result?>(descriptor)}
   val pages=rememberSaveable(saver=PreviewPagesSaver){mutableStateMapOf(descriptor.id to 2)}
   var card by rememberSaveable(stateSaver=InfoCardSaver){mutableStateOf<InfoCard?>(InfoCard("Tarjeta local","Fuente local","Dato conservado"))}
   Column(Modifier.width(700.dp).padding(20.dp)){
    Text(doc!!.title);Text("Página conservada: ${pages[doc!!.id]}")
    Text(card!!.subtitle);Text(card!!.body)
    AuthorizedFilePreview(ui.activity,doc!!,false,pages[doc!!.id]!!,{pages[doc!!.id]=it})
   }
  }}
  ui.waitUntil(10000){ui.onAllNodesWithText("Contenido documental autorizado").fetchSemanticsNodes().isNotEmpty()}
  val before=provider.opened;restore.emulateSavedInstanceStateRestore()
  ui.onNodeWithText("Página conservada: 2").assertExists();ui.onNodeWithText("Recuperada · Fuente local").assertExists()
  ui.waitUntil(10000){provider.opened>before && ui.onAllNodesWithText("Contenido documental autorizado").fetchSemanticsNodes().isNotEmpty()}
  captureScreenRoboImage("build/preview39/document-restored-tablet.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
 }
 @Test fun revokedDocumentIsInvalidatedOnResumeWithoutOpeningExternalApp() {
  val provider=provider()
  ui.setContent{SpotlightTheme("dark",AppConfig(theme="dark")){AuthorizedFilePreview(ui.activity,descriptor,false,0,{})}}
  ui.waitUntil(10000){ui.onAllNodesWithText("Contenido documental autorizado").fetchSemanticsNodes().isNotEmpty()}
  ui.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
  provider.allowed=false
  ui.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
  ui.waitUntil(10000){ui.onAllNodesWithText("El documento ya no está disponible o perdió su autorización. Volvé a seleccionarlo desde Archivos.").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithText("Contenido documental autorizado").assertDoesNotExist()
  assertNull(Shadows.shadowOf(ui.activity).nextStartedActivity)
 }
 @Test fun savedCardDoesNotRestoreUnsupportedExternalScheme() {
  val card=InfoCard("Título","Fuente","Texto",url="intent://untrusted")
  val raw=with(InfoCardSaver){SaverScope{true}.save(card)}!!
  assertNull(InfoCardSaver.restore(raw)!!.url)
 }
 @Test fun scopeAndSensitiveReviewRestoreWithoutAnAuthorization() {
  val original=dev.bautista.spotlight.control.SearchScope("anatomía","files","pdf")
  val raw=with(SearchScopeSaver){SaverScope{true}.save(original)}!!
  assertEquals(original,SearchScopeSaver.restore(raw))
  val reviewed=Action.Ask("Documento privado",sensitive=true,reviewed=true)
  val saved=with(AskTaskSaver){SaverScope{true}.save(reviewed)}!!
  val restored=AskTaskSaver.restore(saved)!!
  assertFalse(restored.reviewed);assertTrue(restored.sensitive);assertEquals(reviewed.prompt,restored.prompt)
 }
 @Test @Config(qualifiers="w412dp-h915dp") fun recoveredCardActionsFitNarrowPhoneAtLargeFontScale() {
  var clicked=0
  ui.setContent {
   val density=androidx.compose.ui.platform.LocalDensity.current
   CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(density.density,1.3f)) {
    SpotlightTheme("dark",AppConfig(theme="dark")) {
     Box(Modifier.width(280.dp)) { InfoCardView(InfoCard("Resultado recuperado","Recuperada · Fuente local","Contenido preservado",url="https://example.com"),{clicked++},{clicked++},{clicked++}) }
    }
   }
  }
  val link=ui.onNodeWithText("Abrir fuente / mapa");link.performScrollTo().assertIsDisplayed()
  ui.onNode(hasClickAction() and hasAnyDescendant(hasText("Abrir fuente / mapa"))).assertHeightIsAtLeast(48.dp)
  val bounds=link.fetchSemanticsNode().boundsInRoot
  assertTrue(bounds.left>=0 && bounds.right<=ui.onRoot().fetchSemanticsNode().boundsInRoot.right)
  captureScreenRoboImage("build/preview39/card-phone-font130.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  link.performClick();assertEquals(1,clicked)
 }

}
