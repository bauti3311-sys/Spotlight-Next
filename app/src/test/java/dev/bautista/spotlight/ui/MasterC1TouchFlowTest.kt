package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MasterC1TouchFlowTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 private var closed=0
 private fun search()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
 private fun row()=ui.onNodeWithTag("result:com.spotify.music/Main")
 private fun show(speech:Boolean=false){
  if(speech)Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH),ResolveInfo().apply{activityInfo=ActivityInfo().apply{packageName="fixture.voice";name="Voice";exported=true;applicationInfo=ApplicationInfo().apply{packageName="fixture.voice"}}})
  val info=ActivityInfo().apply{packageName="com.spotify.music";name="Main";nonLocalizedLabel="Spotify";exported=true;applicationInfo=ApplicationInfo().apply{packageName="com.spotify.music";nonLocalizedLabel="Spotify"}}
  Shadows.shadowOf(app.packageManager).installPackage(PackageInfo().apply{packageName="com.spotify.music";applicationInfo=info.applicationInfo;activities=arrayOf(info)})
  Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=info})
  app.search.invalidateApps()
  ui.setContent{Box(Modifier.width(740.dp)){SpotlightPanel(app,AppConfig(theme="dark",files=false,animations=false),{closed++},"spot",availableHeightDp=640f)}}
  ui.waitUntil(15000){ui.onAllNodesWithTag("result:com.spotify.music/Main").fetchSemanticsNodes().isNotEmpty()}
 }
 @Test fun initialHighlightRequiresFirstTapThenSecondTapExecutes(){
  show();row().assertIsSelected().assertHeightIsAtLeast(48.dp)
  row().performTouchInput{click()}
  ui.runOnIdle{assertNull(Shadows.shadowOf(app).peekNextStartedActivity());assertEquals(0,closed)}
  row().performTouchInput{click()}
  ui.runOnIdle{assertEquals("com.spotify.music",Shadows.shadowOf(app).nextStartedActivity?.component?.packageName);assertEquals(1,closed)}
 }
 @Test fun cancelledSecondTapDoesNotExecuteAndLongPressShowsActions(){
  show();row().performTouchInput{click()}
  row().performTouchInput{down(center);cancel()}
  ui.runOnIdle{assertNull(Shadows.shadowOf(app).peekNextStartedActivity());assertEquals(0,closed)}
  row().performTouchInput{longClick()}
  ui.onNodeWithTag("action-surface").assertIsDisplayed()
  ui.onNodeWithTag("action-filter").performKeyInput{pressKey(Key.Escape)}
  search().assertTextEquals("spot").assertIsFocused();row().assertIsSelected()
  ui.runOnIdle{assertNull(Shadows.shadowOf(app).peekNextStartedActivity());assertEquals(0,closed)}
 }
 @Test fun enterStillExecutesWithoutTouchConfirmation(){
  show();search().performKeyInput{pressKey(Key.Enter)}
  ui.runOnIdle{assertEquals("com.spotify.music",Shadows.shadowOf(app).nextStartedActivity?.component?.packageName);assertEquals(1,closed)}
 }
 @Test fun unavailableDictationIsHiddenAndOptionsDoNotChangeQuery(){
  show();ui.onNodeWithContentDescription("Dictar consulta").assertDoesNotExist()
  ui.onNodeWithContentDescription("Opciones de Spotlight").performClick()
  listOf("Ajustes","Apariencia","Permisos","Atajos").forEach{ui.onNodeWithText(it).assertIsDisplayed()}
  search().assertTextEquals("spot");row().assertDoesNotExist()
  ui.onNodeWithContentDescription("Opciones de Spotlight").performClick()
  search().assertTextEquals("spot");row().assertIsSelected()
 }
 @Test fun installedSpeechActivityMakesDictationAvailable(){
  show(true)
  ui.waitUntil(15000){ui.onAllNodesWithContentDescription("Dictar consulta").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithContentDescription("Dictar consulta").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
 }

}
