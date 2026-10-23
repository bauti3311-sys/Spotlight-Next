package dev.bautista.spotlight.ui

import android.content.*
import android.content.pm.*
import android.media.AudioManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.ToolCall
import dev.bautista.spotlight.automation.*
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicReference

/** Production persistence/search/review/execution; PackageManager/AudioManager are Android test shadows. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
class NativeCommandFlowTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 private fun command(vararg calls:ToolCall)=Workflow("fixture-command","Mi comando verificado","miverificado",calls.map{WorkflowStep("tool_action",Workflows.encodeTool(it))},kind=NativeCommands.KIND)
 private val volume=ToolCall("set_volume",mapOf("percent" to "40"))
 private fun ready(){ui.waitUntil(15000){ui.onAllNodes(hasText("Confirmar pasos seleccionados") and isEnabled()).fetchSemanticsNodes().isNotEmpty()}}
 @Test fun savingFromEditorCreatesSearchableNativeCommandWithoutExecuting() {
  val audio=app.getSystemService(AudioManager::class.java);val initial=audio.getStreamVolume(AudioManager.STREAM_MUSIC)
  val old=runBlocking{app.preferences.config.first()}
  val clean=old.copy(routines="[]")
  runBlocking{app.preferences.update(clean)}
  val saved=AtomicReference<AppConfig?>(null)
  val observer=app.scope.launch{app.preferences.config.first{NativeCommands.results(it.routines).any{row->row.title=="Mi plan local"}}.let{saved.set(it)}}
  try {
   ui.setContent{SpotlightTheme{EditablePlanPanel(app,clean,"new-command",LocalPlan(listOf(PlanStep.Volume(40),PlanStep.Volume(50))),{})}}
   ready();ui.onNodeWithText("Guardar como comando").performScrollTo().performClick()
   ui.onNodeWithText("Nombre").performScrollTo().performTextInput("Mi plan local")
   ui.onNodeWithText("Guardar comando").performScrollTo().performClick()
   ui.waitUntil(10000){saved.get()!=null}
   assertEquals(initial,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
   val rows=runBlocking{app.search.search("Mi plan local",saved.get()!!).toList().last()}
   assertTrue(rows.any{it.title=="Mi plan local" && ProductVisibility.allowed(it) && (it.action as? Action.SystemPanel)?.page=="saved-command"})
  }finally{observer.cancel();runBlocking{app.preferences.update(app.preferences.config.first().copy(routines=old.routines))}}
 }
 @Test fun searchOpensStoredReviewAndExplicitConfirmationChangesVolume() {
  val audio=app.getSystemService(AudioManager::class.java);val initial=audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
  audio.setStreamVolume(AudioManager.STREAM_MUSIC,initial,0)
  val c=AppConfig(files=false,animations=false,routines=Workflows.encode(listOf(command(volume))))
  ui.setContent{SpotlightPanel(app,c,{},initialQuery="Mi comando verificado")}
  val result=hasText("Mi comando verificado") and !hasSetTextAction()
  ui.waitUntil(15000){ui.onAllNodes(result).fetchSemanticsNodes().isNotEmpty()}
  ui.onNode(result).performClick()
  // First touch only selects under D23; opening the review needs confirmation.
  ui.onNodeWithText("Confirmar pasos seleccionados").assertDoesNotExist()
  assertEquals(initial,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
  ui.onNode(result).performClick();ready()
  assertEquals(initial,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
  ui.onNodeWithText("Confirmar pasos seleccionados").performScrollTo().performClick()
  ui.waitUntil(15000){ui.onAllNodesWithText("Verificado · Volumen multimedia observado:",substring=true).fetchSemanticsNodes().isNotEmpty()}
  assertEquals(initial*40/100,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
 }
 @Test fun savedPanelContainsNativeCommandsAndKeepsLegacyRoutinesHidden() {
  val legacy=Workflow("legacy","Rutina antigua",steps=listOf(WorkflowStep("volume","40")))
  var action:Action?=null
  ui.setContent{SpotlightTheme{SavedPanel(app,AppConfig(routines=Workflows.encode(listOf(legacy,command(volume))))){action=it}}}
  ui.onNodeWithText("Rutina antigua").assertDoesNotExist()
  ui.onNodeWithText("Mi comando verificado").performScrollTo().performClick()
  assertEquals(Action.SystemPanel("saved-command","fixture-command"),action)
 }
 private fun install(pkg:String) {
  val application=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel="Duplicada"}
  val activity=ActivityInfo().apply{packageName=pkg;name="$pkg.Main";applicationInfo=application;nonLocalizedLabel="Duplicada";exported=true}
  val pm=Shadows.shadowOf(app.packageManager)
  pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=application;activities=arrayOf(activity)})
  val info=ResolveInfo().apply{activityInfo=activity}
  for(category in listOf(Intent.CATEGORY_INFO,Intent.CATEGORY_LAUNCHER))pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(category).setPackage(pkg),info)
 }
 @Test fun reorderingIdenticallyNamedAppsPreservesTheirExactPackages() {
  install("fixture.first");install("fixture.second")
  assertEquals("fixture.first",app.packageManager.getLaunchIntentForPackage("fixture.first")!!.component!!.packageName)
  assertEquals("fixture.second",app.packageManager.getLaunchIntentForPackage("fixture.second")!!.component!!.packageName)
  val w=command(ToolCall("open_app",mapOf("package" to "fixture.first")),ToolCall("open_app",mapOf("package" to "fixture.second")))
  val review=NativeCommands.review(w){"Duplicada"}
  ui.setContent{SpotlightTheme{EditablePlanPanel(app,AppConfig(),"bound-command",review.plan,review.calls,{})}}
  ready();ui.onAllNodesWithText("Subir")[1].performScrollTo().performClick()
  ui.waitUntil(15000){ui.onAllNodes(hasText("fixture.second",substring=true) and hasAnyAncestor(hasTestTag("plan-step-0"))).fetchSemanticsNodes().isNotEmpty()}
  ready()
  ui.onNodeWithText("Confirmar pasos seleccionados").performScrollTo().performClick()
  ui.waitUntil(15000){ui.onAllNodesWithText("Solicitud enviada · Android recibió la apertura",substring=true).fetchSemanticsNodes().size==2}
  // Robolectric 4.14.1 removes the last started Activity first (List.remove(size-1)).
  val chronological=List(2){Shadows.shadowOf(app).nextStartedActivity.component!!.packageName}.reversed()
  assertEquals(listOf("fixture.second","fixture.first"),chronological)
 }
 @Test fun deletingSavedCommandRequiresConfirmationAndDoesNotExecuteIt() {
  val old=runBlocking{app.preferences.config.first()};val c=old.copy(routines=Workflows.encode(listOf(command(volume))))
  runBlocking{app.preferences.update(c)}
  val action=AtomicReference<Action?>(null)
  val audio=app.getSystemService(AudioManager::class.java);val before=audio.getStreamVolume(AudioManager.STREAM_MUSIC)
  try {
   ui.setContent{SpotlightTheme{SavedCommandPanel(app,c,"fixture-command"){action.set(it)}}};ready()
   ui.onNodeWithText("Eliminar comando").performClick();ui.onNodeWithText("Cancelar").performClick()
   assertNull(action.get());assertEquals(1,NativeCommands.results(runBlocking{app.preferences.config.first()}.routines).size)
   ui.onNodeWithText("Eliminar comando").performClick();ui.onNodeWithText("Confirmar").performClick()
   ui.waitUntil(10000){action.get()==Action.RestoreQuery("")}
   assertTrue(NativeCommands.results(runBlocking{app.preferences.config.first()}.routines).isEmpty())
   assertEquals(before,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
  }finally{runBlocking{app.preferences.update(app.preferences.config.first().copy(routines=old.routines))}}
 }
}
