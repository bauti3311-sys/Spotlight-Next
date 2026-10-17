package dev.bautista.spotlight.ui

import android.content.*
import android.content.pm.ProviderInfo
import android.database.*
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import java.io.*
import java.util.concurrent.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
class ContextExtractionFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun show(action:(Action)->Unit){val c=AppConfig(animations=false);ui.setContent{SpotlightTheme(c.theme,c){ContextPanel(app,c,"context","",action)}}}
    private fun delayed(cancel:Boolean) {
        val uri=Uri.parse("content://context-fixture/doc/1")
        val provider=object:ContentProvider() {
            override fun onCreate()=true
            override fun getType(uri:Uri)="text/plain"
            override fun query(uri:Uri,projection:Array<out String>?,selection:String?,selectionArgs:Array<out String>?,sortOrder:String?):Cursor=MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME)).apply{addRow(arrayOf("fuente.txt"))}
            override fun insert(uri:Uri,values:ContentValues?):Uri?=null
            override fun update(uri:Uri,values:ContentValues?,selection:String?,selectionArgs:Array<out String>?)=0
            override fun delete(uri:Uri,selection:String?,selectionArgs:Array<out String>?)=0
        }
        provider.attachInfo(app,ProviderInfo().apply{authority="context-fixture"})
        ShadowContentResolver.registerProviderInternal("context-fixture",provider)
        val started=CountDownLatch(1);val release=CountDownLatch(1);val finished=CountDownLatch(1)
        val input=object:InputStream() {
            val bytes=ByteArrayInputStream("Contenido privado que llega tarde".toByteArray());var waiting=true
            override fun read()=bytes.read()
            override fun read(b:ByteArray,off:Int,len:Int):Int {if(waiting){waiting=false;started.countDown();check(release.await(20,TimeUnit.SECONDS))};return bytes.read(b,off,len)}
            override fun close(){finished.countDown();bytes.close()}
        }
        Shadows.shadowOf(app.contentResolver).registerInputStream(uri,input)
        runBlocking{app.contexts.setDocument(uri)}
        val actions=CopyOnWriteArrayList<Action>();show{actions+=it}
        try {
            ui.onNodeWithText("Revisar envío a IA").performClick();ui.waitUntil(10000){started.count==0L}
            if(cancel)ui.onNodeWithText("Cancelar preparación").performClick() else ui.onNodeWithText("Quitar").performClick()
            release.countDown()
            ui.waitUntil(10000){finished.count==0L && ui.onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).fetchSemanticsNodes().isEmpty()}
            assertTrue("Stale private text must not open an AI review",actions.isEmpty())
        } finally{release.countDown()}
    }
    @Test fun clearingContextDiscardsLateDocumentTextWithoutPreparingRemoteReview(){delayed(false)}
    @Test fun explicitCancellationDiscardsLateDocumentText(){delayed(true)}
    @Test fun currentTextContextPreparesReviewWithoutGrantingConsent() {
        app.contexts.setText("Texto elegido explícitamente")
        val actions=CopyOnWriteArrayList<Action>();show{actions+=it}
        ui.onNodeWithText("Revisar envío a IA").performClick();ui.waitUntil(10000){actions.isNotEmpty()}
        val ask=actions.single() as Action.Ask
        assertTrue(ask.prompt.endsWith("Texto elegido explícitamente"));assertTrue(AiRequestPolicy.requiresReview(ask))
    }
}
