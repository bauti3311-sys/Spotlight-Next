package dev.bautista.spotlight.ui
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.index.*
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w600dp-h700dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class)
class C5CitationCardReviewTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 @Test fun existingStudioComponentsCanPresentALiteralCitationWithoutOpeningOnScroll() {
  ui.activity.deleteDatabase("search-index.db")
  val file=File(ui.activity.cacheDir,"acta-c5.txt").apply{writeText("La reunión será el martes a las 15 horas.")}
  val c=AppConfig(files=true,contentIndex=true,online=false,focusInterface=true,animations=false)
  val index=FileIndex(ui.activity);val uri=Uri.fromFile(file)
  runBlocking{index.storeTranscript(uri.toString(),file.name,"text/plain",file.readText());index.cacheExtraction(uri.toString(),DocumentText.extractDetailed(ui.activity,uri,"text/plain"),c)}
  DocumentCitationSession(index,{c}).use { session ->
   val ref=runBlocking{session.retrieve("reunión martes").single()};val citation=runBlocking{session.resolve(ref)!!}
   var theme by mutableStateOf("dark");var opens=0
   ui.setContent{SpotlightTheme(theme,c){val scope=rememberCoroutineScope()
    Surface(Modifier.width(360.dp).height(390.dp).testTag("citation-card")){
     Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
      Text("Propuesta C5 · fixture local",style=MaterialTheme.typography.labelMedium)
      StudioSurface("inspector") {Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
       Text(citation.documentTitle,style=MaterialTheme.typography.titleMedium)
       Text(citation.quote,style=MaterialTheme.typography.bodyMedium)
       Text("Fragmento literal · sin página comprobada",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
       Text("Fuente local verificada",style=MaterialTheme.typography.labelSmall)
       OutlinedButton(onClick={scope.launch{if(session.resolve(ref)!=null)opens++}},modifier=Modifier.heightIn(min=48.dp)){Text("Abrir fuente")}
      }}
     }
    }
   }}
   for(t in listOf("dark","light")){ui.runOnIdle{theme=t};ui.waitForIdle();ui.onNodeWithTag("citation-card").captureRoboImage("build/c5-citations/card-$t.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))}
   ui.onNodeWithTag("citation-card").performTouchInput{swipeUp()};ui.runOnIdle{assertEquals(0,opens)}
   ui.onNodeWithText("Abrir fuente").performScrollTo().performClick();ui.waitUntil(5000){opens==1}
   assertNull(citation.page);assertEquals(file.readText(),citation.quote)
  }
  file.delete()
 }
}
