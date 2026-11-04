package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Identical fixture on f17d9b2 and current production; native Compose only. */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class C2FileSearchComparisonTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun capture(theme:String)=runBlocking {
        app.index.storeTranscript("content://fixture/old","rehabilitación vestibular.docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document","Documento de ejemplo autorizado")
        repeat(90){app.index.storeTranscript("content://fixture/new$it","Sesión $it.docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document","rehabilitación vestibular")}
        app.openOrCreateDatabase("search-index.db",0,null).use{it.execSQL("UPDATE files SET modified=CASE WHEN uri='content://fixture/old' THEN 1762171200000 ELSE 1793707200000 END")}
        ui.setContent{Box(Modifier.width(960.dp)){SpotlightPanel(app,AppConfig(theme=theme,preset="glass",files=true,online=false,contacts=false,animations=false,learning=false,contentIndex=true),{},"buscar documento de vestibular",700f)}}
        ui.waitUntil(15000){ui.onAllNodes(SemanticsMatcher("visible file row"){it.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.TestTag)?.startsWith("result:file:")==true}).fetchSemanticsNodes().isNotEmpty()}
        ui.waitForIdle()
        val phase=System.getenv("SPOTLIGHT_C2_FILES_PHASE") ?: "after"
        ui.onNodeWithTag("spotlight-root").captureRoboImage("build/c2-files/$phase/search-$theme.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun searchDark()=capture("dark")
    @Test fun searchLight()=capture("light")
}
