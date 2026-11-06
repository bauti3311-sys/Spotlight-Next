package dev.bautista.spotlight.ui

import android.app.Notification
import android.media.session.*
import android.service.notification.StatusBarNotification
import androidx.activity.ComponentActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.control.*
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Additional nesting budget, not another historical gallery or a hardware acceptance. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w600dp-h1000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class)
class Preview66CriticalLayoutTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private var session:MediaSession?=null
 @After fun clean(){session?.release();Replies.clear()}
 private fun scene(theme:String) {
  Replies.clear()
  val n=Notification.Builder(ui.activity,"fixture").setSmallIcon(android.R.drawable.ic_dialog_info)
   .setContentTitle("Revisión de diseño · nombre largo de conversación")
   .setContentText("Fixture local, sin mensajes reales ni acciones externas.").build()
  Replies.put(StatusBarNotification("com.whatsapp","com.whatsapp",66,null,1000,0,0,n,android.os.Process.myUserHandle(),1700000000000))
  val key=Replies.targets.value.single().key
  val token=MediaSession(ui.activity,"preview66-layout-no-audio").also{session=it}.sessionToken
  val snapshot=MediaSnapshot(token,"fixture.media","Título de una pista con nombre muy largo para comprobar truncamiento","Artista y colaboración de laboratorio","Álbum de prueba",null,180000,
   PlaybackState.Builder().setState(PlaybackState.STATE_PAUSED,42000,0f,1).setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_SKIP_TO_PREVIOUS or PlaybackState.ACTION_SKIP_TO_NEXT).build(),1)
  val commands=mutableListOf<String>();val pages=mutableListOf<Int>();var external=0
  ui.setContent{SpotlightTheme(theme,AppConfig(focusInterface=true,animations=false)){
   val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,2f)) {
    Surface(Modifier.width(330.dp).height(760.dp).testTag("critical66-scene")) {
     Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal=32.dp)) {
      Text("Fixture · presupuesto interior 266 dp",style=MaterialTheme.typography.bodySmall)
      MediaInspector(snapshot,{commands+=it},{external++})
      PdfPageNavigation(1,3){pages+=it}
      NotificationPreview(key,{external++},{external++},{external++})
     }
    }
   }
  }}
  ui.waitForIdle()
  ui.onNodeWithTag("critical66-scene").captureRoboImage("build/preview66-critical/$theme-initial.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  for(label in listOf("Pista anterior","Reanudar reproducción","Pista siguiente")) {
   val node=ui.onNodeWithContentDescription(label);val b=node.getUnclippedBoundsInRoot()
   assertTrue(b.right-b.left>=48.dp && b.left>=32.dp && b.right<=298.dp)
   node.performScrollTo().performClick()
  }
  for(label in listOf("Anterior","Siguiente"))ui.onNodeWithText(label).performScrollTo().performClick()
  for(label in listOf("Copiar mensaje","Resumir con IA","Sugerir respuesta con IA","Volver")) {
   val node=ui.onNodeWithText(label);val b=node.getUnclippedBoundsInRoot()
   assertTrue("$label hidden or outside budget: $b",b.right-b.left>=48.dp && b.left>=32.dp && b.right<=298.dp)
   node.performScrollTo().assertIsDisplayed()
  }
  ui.onNodeWithText("Responder").assertIsNotEnabled();ui.onNodeWithText("Abrir").assertIsNotEnabled()
  ui.onNodeWithTag("critical66-scene").captureRoboImage("build/preview66-critical/$theme-scrolled.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  assertEquals(listOf("previous","play","next"),commands);assertEquals(listOf(0,2),pages);assertEquals(0,external)
 }
 @Test fun nestedDarkText200()=scene("dark")
 @Test fun nestedLightText200()=scene("light")
}
