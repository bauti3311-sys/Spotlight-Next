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
}
