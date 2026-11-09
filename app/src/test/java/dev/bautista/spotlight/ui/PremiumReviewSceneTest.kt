package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import android.graphics.*
import android.media.MediaMetadata
import android.media.session.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
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

/** Same production components/data on both revisions. Synthetic PM/session, real local PDF. */
@OptIn(ExperimentalTestApi::class,ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PremiumReviewSceneTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private var viewport by mutableStateOf(700f)
    private var closes=0
    private fun field()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
    private fun install(pkg:String,label:String) {
        val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=label;enabled=true}
        val activity=ActivityInfo().apply{packageName=pkg;name="Main";applicationInfo=a;nonLocalizedLabel=label;enabled=true;exported=true}
        val pm=Shadows.shadowOf(app.packageManager)
        pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(activity);versionName="7.2-fixture"})
        pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=activity})
    }
    private fun fixtures() {
        install("com.vivaldi.browser","Vivaldi");install("com.spotify.music","Spotify");install("com.whatsapp","WhatsApp")
        app.search.invalidateApps()
        val controller=MediaSession(app,"premium-review").controller
        Shadows.shadowOf(controller).setPackageName("com.spotify.music")
        val art=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888)
        Canvas(art).apply{drawColor(Color.rgb(29,62,56));drawCircle(64f,64f,42f,Paint().apply{color=Color.rgb(157,203,164)});drawCircle(64f,64f,22f,Paint().apply{color=Color.rgb(29,62,56)})}
        Shadows.shadowOf(controller).setMetadata(MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE,"Horizonte")
            .putString(MediaMetadata.METADATA_KEY_ARTIST,"Sesión de prueba local").putString(MediaMetadata.METADATA_KEY_ALBUM,"Atlas · fixture sin audio")
            .putLong(MediaMetadata.METADATA_KEY_DURATION,180000).putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART,art).build())
        Shadows.shadowOf(controller).setPlaybackState(PlaybackState.Builder().setState(PlaybackState.STATE_PAUSED,42000,0f,android.os.SystemClock.elapsedRealtime())
            .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_SKIP_TO_NEXT).build())
        Shadows.shadowOf(app.getSystemService(MediaSessionManager::class.java)).addController(controller)
        val file=java.io.File(app.cacheDir,"review/vestibular.pdf").apply{parentFile!!.mkdirs()}
        com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(app)
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { pdf ->
            val page=com.tom_roush.pdfbox.pdmodel.PDPage();pdf.addPage(page)
            com.tom_roush.pdfbox.pdmodel.PDPageContentStream(pdf,page).use { content ->
                content.beginText();content.setFont(com.tom_roush.pdfbox.pdmodel.font.PDType1Font.HELVETICA,22f)
                content.newLineAtOffset(40f,720f);content.showText("VESTIBULAR - Notas de consulta")
                content.setFont(com.tom_roush.pdfbox.pdmodel.font.PDType1Font.HELVETICA,12f)
                repeat(8){content.newLineAtOffset(0f,-30f);content.showText("Contenido local autorizado de prueba ${it+1}")}
                content.endText()
            };pdf.save(file)
        }
        val uri=FileProvider.getUriForFile(app,app.packageName+".files",file)
        runBlocking{app.index.storeTranscript(uri.toString(),"Vestibular - notas.pdf","application/pdf","Notas de consulta autorizadas de prueba")}
    }
    private fun capture(profile:String,scene:String) {
        val phase=System.getenv("SPOTLIGHT_PREMIUM_PHASE") ?: "before"
        ui.waitForIdle()
        ui.onNodeWithTag("spotlight-root").captureRoboImage("build/premium-review/$phase/$profile-$scene.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
    private fun query(text:String) {field().performClick().performTextReplacement(text)}
    private fun select(pkg:String) {
        val tag="result:$pkg/Main"
        ui.waitUntil(15000){ui.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithTag(tag).performClick()
        ui.waitUntil(15000){ui.onAllNodesWithTag("inspector-title").fetchSemanticsNodes().isNotEmpty()}
    }
    private fun fits() {
        val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
        val footer=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
        assertTrue(footer.bottom<=root.bottom);assertTrue(root.bottom-root.top<=viewport.dp)
        assertEquals(0,closes);assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    private fun scenes(width:Int,theme:String) {
        fixtures();val profile="${if(width>600)"tablet" else "phone"}-$theme"
        val premium=System.getenv("SPOTLIGHT_PREMIUM_PHASE")=="after"
        ui.setContent{CompositionLocalProvider(LocalPremiumReview provides premium){SpotlightTheme(theme){Surface{Box(Modifier.width(width.dp)){
            SpotlightPanel(app,AppConfig(theme=theme,preset="glass",files=true,online=false,contacts=false,animations=false,learning=false,
                favorites=setOf("com.spotify.music/Main","com.vivaldi.browser/Main","com.whatsapp/Main")),{closes++},"",viewport)
        }}}}}
        // The real engine deliberately suggests at most one app (Spotify here),
        // not one row for every favorite configured by the fixture.
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:suggest:app:com.spotify.music").fetchSemanticsNodes().isNotEmpty()}
        capture(profile,"home");fits()
        query("Vi");ui.waitUntil(15000){ui.onAllNodesWithTag("result:com.vivaldi.browser/Main").fetchSemanticsNodes().isNotEmpty()}
        capture(profile,"search");query("Vivaldi");select("com.vivaldi.browser")
        ui.waitUntil(15000){ui.onAllNodesWithText("7.2-fixture").fetchSemanticsNodes().isNotEmpty()}
        capture(profile,"app");fits()
        ui.runOnIdle{viewport=330f};ui.waitForIdle();capture(profile,"app-ime");fits()
        ui.runOnIdle{viewport=700f};ui.waitForIdle()
        query("Spotify");select("com.spotify.music")
        ui.waitUntil(15000){ui.onAllNodesWithTag("media-inspector").fetchSemanticsNodes().isNotEmpty()}
        capture(profile,"spotify");fits()
        ui.onNodeWithContentDescription("Pista anterior").assertDoesNotExist()
        query("WhatsApp");select("com.whatsapp");capture(profile,"whatsapp");fits()
        query("vestibular");ui.waitUntil(15000){ui.onAllNodesWithText("Vestibular - notas.pdf").fetchSemanticsNodes().isNotEmpty()}
        capture(profile,"files");ui.onNodeWithText("Vestibular - notas.pdf").performClick()
        ui.waitUntil(15000){ui.onAllNodesWithContentDescription("Página 1 del PDF").fetchSemanticsNodes().isNotEmpty() ||
            ui.onAllNodesWithText("No se pudo abrir el PDF",substring=true).fetchSemanticsNodes().isNotEmpty() ||
            ui.onAllNodesWithText("El PDF no contiene páginas",substring=true).fetchSemanticsNodes().isNotEmpty()}
        val rendered=ui.onAllNodesWithContentDescription("Página 1 del PDF").fetchSemanticsNodes().isNotEmpty()
        capture(profile,if(rendered)"quicklook" else "quicklook-runtime-unavailable");fits()
        query("2+2");ui.waitUntil(15000){ui.onAllNodesWithText("4").fetchSemanticsNodes().isNotEmpty()}
        field().performKeyInput{pressKey(Key.Tab)}
        ui.waitUntil(15000){ui.onAllNodesWithTag("context-action-panel").fetchSemanticsNodes().isNotEmpty()}
        capture(profile,"actions")
        field().performKeyInput{pressKey(Key.Escape)}
        field().assertTextEquals("2+2").assertIsFocused();assertEquals(0,closes)
    }
    @Test fun tabletDark()=scenes(960,"dark")
    @Test fun tabletLight()=scenes(960,"light")
    @Test @Config(qualifiers="w390dp-h844dp") fun phoneDark()=scenes(360,"dark")
    @Test @Config(qualifiers="w390dp-h844dp") fun phoneLight()=scenes(360,"light")
}
