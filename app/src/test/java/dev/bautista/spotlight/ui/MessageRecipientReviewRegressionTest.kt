package dev.bautista.spotlight.ui

import android.Manifest
import android.content.*
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.search.Action
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*
import org.robolectric.shadows.ShadowContentResolver

class ReviewContactsFixture:ContentProvider() {
 override fun onCreate()=true
 override fun query(uri:Uri,projection:Array<out String>?,selection:String?,args:Array<out String>?,order:String?):Cursor =
  MatrixCursor(projection).apply{addRow(arrayOf(1L,"Alex","+541100000001"));addRow(arrayOf(2L,"Alex","+541100000002"))}
 override fun getType(uri:Uri):String?=null
 override fun insert(uri:Uri,values:ContentValues?):Uri?=null
 override fun delete(uri:Uri,selection:String?,args:Array<out String>?)=0
 override fun update(uri:Uri,values:ContentValues?,selection:String?,args:Array<out String>?)=0
}
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MessageRecipientReviewRegressionTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 @Test fun selectingAnotherHomonymInvalidatesTheOpenReviewBeforeAnyRequest() {
  val app=RuntimeEnvironment.getApplication() as SpotlightApp
  Shadows.shadowOf(app).grantPermissions(Manifest.permission.READ_CONTACTS)
  val provider=ReviewContactsFixture().apply{attachInfo(app,ProviderInfo().apply{authority=ContactsContract.AUTHORITY})}
  ShadowContentResolver.registerProviderInternal(ContactsContract.AUTHORITY,provider)
  val requested=mutableListOf<Intent>()
  ui.setContent {SpotlightTheme("dark") {Surface(Modifier.width(600.dp)) {
   MessageEditor(Action.ComposeMessage("whatsapp","Alex","Nos vemos"),{requested+=it},{},{})
  }}}
  ui.waitUntil(15000){ui.onAllNodesWithText("Alex · +541100000002").fetchSemanticsNodes().isNotEmpty()}
  ui.onNode(hasSetTextAction() and hasText("Número de destino")).performTextReplacement("+541100000001")
  ui.onNodeWithText("Revisar mensaje").performClick()
  ui.onNodeWithText("Confirmar · WhatsApp personal").assertExists()
  ui.onNodeWithText("Alex · +541100000002").performClick()
  ui.onNodeWithText("Confirmar · WhatsApp personal").assertDoesNotExist()
  assertTrue(requested.isEmpty())
  ui.onNodeWithText("Revisar mensaje").performClick()
  ui.onNodeWithText("Preparar en la app").performClick()
  assertEquals(1,requested.size)
  assertEquals("com.whatsapp",requested.single().`package`)
  assertEquals("/541100000002",requested.single().data?.path)
  assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
 private fun directMessage(onLaunch:(Intent)->Unit) {
  ui.setContent { SpotlightTheme("dark") { Surface(Modifier.width(600.dp)) {
   MessageEditor(Action.ComposeMessage("whatsapp_business", "+541100000001", "Texto revisado"), onLaunch, {}, {})
  } } }
  ui.waitUntil(15000) { ui.onAllNodesWithText("Revisar mensaje").fetchSemanticsNodes().firstOrNull()?.config?.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled)==false }
 }
 @Test fun oneReviewCannotLaunchTwiceBeforeRecomposition() {
  val requested=mutableListOf<Intent>()
  directMessage { requested+=it }
  ui.onNodeWithText("Revisar mensaje").performClick()
  val confirm=ui.onNodeWithText("Preparar en la app").fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsActions.OnClick].action!!
  ui.runOnIdle { confirm(); confirm() }
  assertEquals(1,requested.size)
  assertEquals("com.whatsapp.w4b",requested.single().`package`)
 }
 @Test fun editedBodyInvalidatesEvenAPreviouslyCapturedConfirmCallback() {
  val requested=mutableListOf<Intent>()
  directMessage { requested+=it }
  ui.onNodeWithText("Revisar mensaje").performClick()
  val confirm=ui.onNodeWithText("Preparar en la app").fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsActions.OnClick].action!!
  ui.onNode(hasSetTextAction() and hasText("Mensaje")).performTextReplacement("Texto cambiado")
  ui.runOnIdle { confirm() }
  assertTrue(requested.isEmpty())
  ui.onNodeWithText("Revisar mensaje").performClick()
  ui.onNodeWithText("Preparar en la app").performClick()
  assertEquals(1,requested.size)
  assertTrue(requested.single().data.toString().contains("Texto%20cambiado"))
 }

 @Test fun restoredDraftRequiresAnotherReviewAndNeverRestoresApproval() {
  val requested=mutableListOf<Intent>()
  val restoration=androidx.compose.ui.test.junit4.StateRestorationTester(ui)
  restoration.setContent { SpotlightTheme("dark") { Surface(Modifier.width(600.dp)) {
   MessageEditor(Action.ComposeMessage("whatsapp", "+541100000001", "Texto revisado"), {requested+=it}, {}, {})
  } } }
  ui.waitUntil(15000) { ui.onAllNodesWithText("Revisar mensaje").fetchSemanticsNodes().firstOrNull()?.config?.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled)==false }
  ui.onNodeWithText("Revisar mensaje").performClick()
  ui.onNodeWithText("Preparar en la app").assertExists()
  restoration.emulateSavedInstanceStateRestore()
  ui.onNodeWithText("Preparar en la app").assertDoesNotExist()
  ui.onNode(hasSetTextAction() and hasText("Mensaje")).assertTextContains("Texto revisado")
  assertTrue(requested.isEmpty())
 }
 @Test fun editingAndLeavingTheReviewNeverRequestsAnIntent() {
  val requested=mutableListOf<Intent>()
  var cancelled=0
  ui.setContent { SpotlightTheme("dark") { Surface(Modifier.width(600.dp)) {
   MessageEditor(Action.ComposeMessage("whatsapp", "+541100000001", "Texto revisado"), {requested+=it}, {}, {cancelled++})
  } } }
  ui.waitUntil(15000) { ui.onAllNodesWithText("Revisar mensaje").fetchSemanticsNodes().firstOrNull()?.config?.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled)==false }
  ui.onNodeWithText("Revisar mensaje").performClick()
  ui.onNodeWithText("Editar").performClick()
  ui.onNodeWithText("Preparar en la app").assertDoesNotExist()
  ui.onNodeWithText("Revisar mensaje").performClick()
  val confirm=ui.onNodeWithText("Preparar en la app").fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsActions.OnClick].action!!
  ui.onNodeWithText("Volver").performClick()
  ui.runOnIdle { confirm() }
  assertEquals(1,cancelled)
  assertTrue(requested.isEmpty())
 }

 @Test fun whatsappReviewShowsTheExactInternationalNumberBeforePreparingIt() {
  val requested=mutableListOf<Intent>()
  ui.setContent { SpotlightTheme("dark") { Surface(Modifier.width(600.dp)) {
   MessageEditor(Action.ComposeMessage("whatsapp", "+54 (11) 0000-0001", "Texto revisado"), {requested+=it}, {}, {})
  } } }
  ui.waitUntil(15000) { ui.onAllNodesWithText("Revisar mensaje").fetchSemanticsNodes().firstOrNull()?.config?.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled)==false }
  ui.onNodeWithText("Revisar mensaje").performClick()
  ui.onNodeWithText("Para: +541100000001").assertExists()
  assertTrue(requested.isEmpty())
  ui.onNodeWithText("Preparar en la app").performClick()
  assertEquals("/541100000001",requested.single().data!!.path)
 }

}
