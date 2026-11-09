package dev.bautista.spotlight.ui

import android.app.*
import android.content.Intent
import android.service.notification.StatusBarNotification
import androidx.activity.ComponentActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.control.Replies
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Synthetic notification; all callbacks collected locally, never dispatched. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1200dp-h1000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class,androidx.compose.ui.ExperimentalComposeUiApi::class)
class NotificationCardLayoutTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val message="Fixture local: revisar el documento antes de la reunión. No es un mensaje real."
 @Before fun reset(){Replies.clear()}
 @After fun cleanup(){Replies.clear()}
 private fun fixture():String {
  val context=ui.activity
  val pi=PendingIntent.getBroadcast(context,761,Intent("fixture.notification.layout"),PendingIntent.FLAG_MUTABLE)
  val action=Notification.Action.Builder(null,"Responder",pi).addRemoteInput(RemoteInput.Builder("body").build()).build()
  val n=Notification.Builder(context,"fixture").setSmallIcon(android.R.drawable.ic_dialog_info)
   .setContentTitle("María · revisión de diseño y documentos").setContentText(message).setShortcutId("layout-review").addAction(action).build()
  Replies.put(StatusBarNotification("com.whatsapp","com.whatsapp",761,null,1000,0,0,n,android.os.Process.myUserHandle(),1700000000000))
  return Replies.targets.value.single().key
 }
 private fun scene(width:Int,scale:Float,theme:String,keyboard:Boolean=false) {
  lateinit var input:InputModeManager
  val key=fixture();val actions=mutableListOf<Action>();val copies=mutableListOf<String>();var closed=0
  ui.setContent {SpotlightTheme(theme,AppConfig(focusInterface=true,animations=false)) {
   val d=LocalDensity.current;val manager=LocalInputModeManager.current
   SideEffect{input=manager}
   CompositionLocalProvider(LocalDensity provides Density(d.density,scale)) {
    Surface(Modifier.width(width.dp).height(760.dp).testTag("notification-fixture")) {
     Column(Modifier.verticalScroll(rememberScrollState())) {
      NotificationPreview(key,{actions+=it},{copies+=it},{closed++})
     }
    }
   }
  }}
  ui.waitForIdle()
  val phase=System.getenv("SPOTLIGHT_CARD_PHASE") ?: "before"
  if(!keyboard)ui.onNodeWithTag("notification-fixture").captureRoboImage("build/visual-notification/$phase/$theme-${width}dp-text${(scale*100).toInt()}.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  for(label in listOf("Responder","Copiar mensaje","Resumir con IA","Sugerir respuesta con IA","Volver")) {
   val node=ui.onNodeWithText(label);val b=node.getUnclippedBoundsInRoot()
   assertTrue("$label must have visible width >=48dp: $b",b.right-b.left>=48.dp)
   assertTrue("$label outside viewport: $b",b.left>=0.dp && b.right<=width.dp)
   node.performScrollTo().assertIsDisplayed()
  }
  assertEquals(0,actions.size);assertTrue(copies.isEmpty());assertEquals(0,closed)
  ui.onNodeWithText("Abrir").assertIsNotEnabled()
  if(keyboard){
   ui.runOnIdle{assertTrue(input.requestInputMode(InputMode.Keyboard))}
   ui.onNodeWithText("Responder").performScrollTo().performSemanticsAction(SemanticsActions.RequestFocus){it()}
   ui.onNodeWithText("Responder").assertIsFocused().performKeyInput{pressKey(Key.Enter);pressKey(Key.Tab)}
   ui.onNodeWithText("Copiar mensaje").assertIsFocused()
  } else ui.onNodeWithText("Responder").performScrollTo().performClick()
  val reply=actions.single() as Action.ComposeMessage
  assertEquals(key,reply.replyKey);assertEquals("whatsapp",reply.channel)
  for(label in listOf("Copiar mensaje","Resumir con IA","Sugerir respuesta con IA")) {
   if(keyboard)ui.onNodeWithText(label).assertIsFocused().performKeyInput{pressKey(Key.Enter);pressKey(Key.Tab)}
   else ui.onNodeWithText(label).performScrollTo().performClick()
  }
  assertEquals(listOf(message),copies)
  assertEquals(3,actions.size);assertTrue(actions.drop(1).all{it is Action.Ask})
  if(keyboard){
   ui.onNodeWithText("Volver").assertIsFocused().performKeyInput{keyDown(Key.ShiftLeft);pressKey(Key.Tab);keyUp(Key.ShiftLeft)}
   ui.onNodeWithText("Sugerir respuesta con IA").assertIsFocused().performKeyInput{pressKey(Key.Tab)}
   ui.onNodeWithText("Volver").assertIsFocused().performKeyInput{pressKey(Key.Enter)}
  } else ui.onNodeWithText("Volver").performScrollTo().performClick()
  assertEquals(1,closed)
  assertEquals(key,Replies.targets.value.single().key)
 }
 @Test fun compactDarkText200()=scene(330,2f,"dark")
 @Test fun compactLightText200()=scene(330,2f,"light")
 @Test fun phoneLightStandard()=scene(390,1f,"light")
 @Test fun wrappedActionsKeepTabShiftTabAndEnterOrder()=scene(330,2f,"light",keyboard=true)
 @Test fun tabletDarkStandard()=scene(1000,1f,"dark")
}
