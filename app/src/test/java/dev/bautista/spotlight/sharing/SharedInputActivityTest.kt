package dev.bautista.spotlight.sharing

import android.content.*
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Looper
import android.provider.OpenableColumns
import dev.bautista.spotlight.ShareContextActivity
import dev.bautista.spotlight.SpotlightApp
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import java.io.ByteArrayInputStream
import java.util.concurrent.atomic.AtomicInteger

class SharedTextProvider : ContentProvider() {
    override fun onCreate()=true
    override fun getType(uri: Uri)="text/plain"
    override fun query(u:Uri,p:Array<out String>?,s:String?,a:Array<out String>?,o:String?):Cursor {
        val columns=p ?: arrayOf(OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE)
        return MatrixCursor(columns).apply { addRow(columns.map { name ->
            when(name) { OpenableColumns.DISPLAY_NAME -> "${u.lastPathSegment}.txt"; OpenableColumns.SIZE -> 100L; else -> null }
        }) }
    }
    override fun insert(u:Uri,v:ContentValues?):Uri?=null
    override fun delete(u:Uri,s:String?,a:Array<out String>?)=0
    override fun update(u:Uri,v:ContentValues?,s:String?,a:Array<out String>?)=0
}

/** Actual Activity/ContextStore/extraction flow; resolver and grants remain simulated. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33], application=SpotlightApp::class)
class SharedInputActivityTest {
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun runShare(intent: Intent, check:(ShareContextActivity)->Unit) {
        val controller=Robolectric.buildActivity(ShareContextActivity::class.java,intent).create()
        try {
            val deadline=System.nanoTime()+10_000_000_000L
            while(!controller.get().isFinishing && System.nanoTime()<deadline) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                Thread.sleep(10)
            }
            assertTrue("Import must finish within the bounded test wait",controller.get().isFinishing)
            check(controller.get())
        } finally { controller.destroy() }
    }
    @Test fun clipTextCreatesExplicitLocalContextAndOnlyRequestsTheSpotlightEntry() {
        runShare(Intent(Intent.ACTION_SEND).setType("text/plain").apply {
            clipData=ClipData.newPlainText("text","Contexto elegido explícitamente")
        }) { activity ->
            assertEquals("Contexto elegido explícitamente",app.contexts.active.value!!.text)
            val request=Shadows.shadowOf(activity).nextStartedActivity
            assertEquals("dev.bautista.spotlight.InvokeActivity",request.component!!.className)
            assertEquals("contexto",request.getStringExtra("query"))
            assertNull(Shadows.shadowOf(activity).peekNextStartedActivity())
        }
    }
    @Test fun multipleFilesUseExistingExtractionAndDeduplicateBeforeReading() {
        val provider=SharedTextProvider().apply { attachInfo(app,ProviderInfo().apply { authority="shared-fixture" }) }
        ShadowContentResolver.registerProviderInternal("shared-fixture",provider)
        val first=Uri.parse("content://shared-fixture/first")
        val second=Uri.parse("content://shared-fixture/second")
        val reads=AtomicInteger()
        val resolver=Shadows.shadowOf(app.contentResolver)
        resolver.registerInputStreamSupplier(first) { reads.incrementAndGet();ByteArrayInputStream("Contenido uno".toByteArray()) }
        resolver.registerInputStreamSupplier(second) { reads.incrementAndGet();ByteArrayInputStream("Contenido dos".toByteArray()) }
        runShare(Intent(Intent.ACTION_SEND_MULTIPLE).setType("text/plain").apply {
            putParcelableArrayListExtra(Intent.EXTRA_STREAM,arrayListOf(first,second))
            clipData=ClipData("files",arrayOf("text/plain"),ClipData.Item(first))
        }) { activity ->
            assertEquals(2,reads.get())
            assertEquals("2 documentos compartidos",app.contexts.active.value!!.title)
            assertTrue(app.contexts.active.value!!.text.contains("Contenido uno"))
            assertTrue(app.contexts.active.value!!.text.contains("Contenido dos"))
            assertNotNull(Shadows.shadowOf(activity).nextStartedActivity)
        }
    }
    @Test fun invalidIncomingUriPreservesExistingContextAndDoesNotLaunch() {
        val prior=app.contexts.setText("No reemplazar este contexto")
        runShare(Intent(Intent.ACTION_SEND).setType("application/pdf").apply {
            clipData=ClipData("file",arrayOf("application/pdf"),ClipData.Item(Uri.parse("file:///private.pdf")))
        }) { activity ->
            assertEquals(prior.id,app.contexts.active.value!!.id)
            assertNull(Shadows.shadowOf(activity).peekNextStartedActivity())
        }
    }
}
