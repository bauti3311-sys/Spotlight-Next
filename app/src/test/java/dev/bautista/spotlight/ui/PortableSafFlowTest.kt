package dev.bautista.spotlight.ui

import android.app.Activity
import android.content.*
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.MainActivity
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.ThemeTransfer
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implements
import org.robolectric.annotation.Implementation

class BackupDocumentProvider:ContentProvider() {
 lateinit var file:java.io.File
 override fun onCreate():Boolean {file=java.io.File(context!!.cacheDir,"portable-saf.json");return true}
 override fun getType(uri:Uri)="application/json"
 override fun query(u:Uri,p:Array<out String>?,s:String?,a:Array<out String>?,o:String?):Cursor?=null
 override fun insert(u:Uri,v:ContentValues?):Uri?=null
 override fun delete(u:Uri,s:String?,a:Array<out String>?)=0
 override fun update(u:Uri,v:ContentValues?,s:String?,a:Array<out String>?)=0
 override fun openFile(uri:Uri,mode:String)=ParcelFileDescriptor.open(file,if(mode.contains('w'))ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE or ParcelFileDescriptor.MODE_READ_WRITE else ParcelFileDescriptor.MODE_READ_ONLY)
}
/** Explicit document stream adapter: Robolectric descriptors do not implement a real SAF provider. */
@Implements(ContentResolver::class)
class BackupResolverShadow:org.robolectric.shadows.ShadowContentResolver() {
 companion object {lateinit var target:java.io.File}
 @Implementation public override fun openOutputStream(uri:Uri,mode:String):java.io.OutputStream {
  println("Provider output: "+uri+" thread="+Thread.currentThread().name+" file="+target)
  if(uri.authority!="portable-backup")return super.openOutputStream(uri,mode)
  return target.outputStream()
 }
 @Implementation public override fun openInputStream(uri:Uri):java.io.InputStream {
  if(uri.authority!="portable-backup")return super.openInputStream(uri)
  return target.inputStream()
 }
}
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class,qualifiers="w1280dp-h800dp",shadows=[BackupResolverShadow::class])
class PortableSafFlowTest {
 @get:Rule val ui=createAndroidComposeRule<MainActivity>()
 @Test fun actualExportAndImportCallbacksUseSafWithPreviewAndExplicitCommit() {
  val app=ui.activity.application as SpotlightApp
  val provider=BackupDocumentProvider();provider.attachInfo(app,ProviderInfo().apply{authority="portable-backup"})
  org.robolectric.shadows.ShadowContentResolver.registerProviderInternal("portable-backup",provider)
  val uri=Uri.parse("content://portable-backup/document/config")
  BackupResolverShadow.target=provider.file
  // Prove the simulated provider can carry bytes before exercising the production callbacks.
  ui.activity.contentResolver.openOutputStream(uri,"wt")!!.use{it.write(byteArrayOf(7))}
  assertEquals(listOf<Byte>(7),provider.file.readBytes().toList())
  runBlocking{kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO){ui.activity.contentResolver.openOutputStream(uri,"wt")!!.use{it.write(byteArrayOf(8))}}}
  assertEquals(listOf<Byte>(8),provider.file.readBytes().toList());provider.file.writeBytes(byteArrayOf())
  val original=runBlocking{val c=app.preferences.config.first();ThemeTransfer.save(c.copy(opacity=.79f,sourcePriority="""{"Archivos":80}""",aliases=setOf("musica|buscar {args} en spotify")),"Mi Glass").also{app.preferences.update(it)}}
  ui.waitUntil(15000){ui.onAllNodesWithTag("settings-search").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithTag("settings-area:Avanzado").performScrollTo().performClick()
  ui.onNodeWithText("Transferir configuración").performScrollTo().performClick()
  ui.onNodeWithText("Exportar",useUnmergedTree=true).performScrollTo().performClick()
  val shadow=Shadows.shadowOf(ui.activity)
  val export=shadow.nextStartedActivityForResult
  assertEquals(Intent.ACTION_CREATE_DOCUMENT,export.intent.action)
  ui.runOnIdle{assertTrue(ui.activity.activityResultRegistry.dispatchResult(export.requestCode,Activity.RESULT_OK,Intent().setData(uri)))}
  try {ui.waitUntil(10000){provider.file.exists() && provider.file.length()>0}}catch(e:Throwable){println("Lifecycle="+ui.activity.lifecycle.currentState+" exportCode="+export.requestCode+" file="+provider.file);println(ui.onRoot(useUnmergedTree=true).printToString());println("Toast="+org.robolectric.shadows.ShadowToast.getTextOfLatestToast());throw e}
  val settings=JSONObject(provider.file.readText()).getJSONObject("settings")
  assertEquals(1,settings.getJSONArray("customThemes").length());assertFalse(settings.has("trees"));assertFalse(settings.has("endpoint"))
  runBlocking{app.preferences.update(original.copy(opacity=.95f,customThemes="[]",aliases=emptySet()))}
  ui.onNodeWithText("Importar",useUnmergedTree=true).performScrollTo().performClick()
  val imported=shadow.nextStartedActivityForResult;assertEquals(Intent.ACTION_OPEN_DOCUMENT,imported.intent.action)
  ui.runOnIdle{assertTrue(ui.activity.activityResultRegistry.dispatchResult(imported.requestCode,Activity.RESULT_OK,Intent().setData(uri)))}
  ui.waitUntil(10000){ui.onAllNodesWithText("Confirmar importación").fetchSemanticsNodes().isNotEmpty()}
  assertEquals(.95f,runBlocking{app.preferences.config.first()}.opacity)
  ui.onNodeWithText("Confirmar importación").performScrollTo().performClick()
  ui.waitUntil(10000){app.preferences.current?.opacity==.79f}
  val restored=runBlocking{app.preferences.config.first()};assertEquals(original.aliases,restored.aliases);assertEquals(original.customThemes,restored.customThemes);assertEquals(original.trees,restored.trees)
 }
}
