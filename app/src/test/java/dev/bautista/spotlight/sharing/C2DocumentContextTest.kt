package dev.bautista.spotlight.sharing

import android.content.*
import android.content.pm.ProviderInfo
import android.net.Uri
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.last
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.shadows.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class C2DocumentContextTest {
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private val uri=Uri.parse("content://share-files-fixture/document/1")
    private lateinit var provider:ShareDocumentProvider
    @Before fun expose() {
        val info=ProviderInfo().apply{authority=uri.authority;name=ShareDocumentProvider::class.java.name;packageName=app.packageName;applicationInfo=app.applicationInfo;enabled=true;grantUriPermissions=true}
        provider=ShareDocumentProvider().apply{attachInfo(app,info)}
        ShadowContentResolver.registerProviderInternal(uri.authority,provider)
        Shadows.shadowOf(app.packageManager).addOrUpdateProvider(info)
    }
    @Test fun metadataContextSearchUsesOriginalUrisAndDoesNotExtractOrGuessDates()=runBlocking {
        app.contexts.setDocuments(listOf(uri))
        assertEquals(0,provider.descriptors)
        assertEquals(uri.toString(),app.contexts.active.value!!.uri)
        assertEquals("",app.contexts.active.value!!.text)
        val rows=app.search.search("buscar documento de vestibular",AppConfig(files=true,online=false,contacts=false,learning=false)).last()
        assertTrue(rows.any{(it.action as? Action.Launch)?.intent?.data==uri})
        assertTrue(app.contexts.sharedFileResults("type:pdf after:2026-11-03").isEmpty())
        assertEquals(0,provider.descriptors)
        app.contexts.clearActive();assertTrue(app.contexts.sharedFiles.items.isEmpty())
        assertTrue(app.contexts.sharedFileResults("vestibular").isEmpty())
    }
    @Test fun explicitEmptyUiSelectionDoesNotResurrectIncomingSelection()=runBlocking {
        app.contexts.setDocuments(listOf(uri))
        val cfg=AppConfig(files=true,online=false,contacts=false,learning=false)
        val valid=app.search.search("compartir archivos seleccionados",cfg).last().single()
        assertEquals("file-share-selection",(valid.action as Action.SystemPanel).page)
        val cleared=app.search.search("compartir archivos seleccionados",cfg,selectedFiles=emptyList()).last().single()
        assertEquals("files:selection-unavailable",cleared.id)
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun cancellingMetadataPreparationStopsBeforeOpeningAStream()=runBlocking {
        val started=CountDownLatch(1);val release=CountDownLatch(1)
        provider.onMetadata={started.countDown();check(release.await(5,TimeUnit.SECONDS))}
        var completed=false
        val job=launch(Dispatchers.IO){FileShareContract.prepare(app,listOf(FileShareContract.result(FileShareContract.Document(uri,"file.pdf","application/pdf",null,"Fixture"))));completed=true}
        assertTrue(started.await(5,TimeUnit.SECONDS));job.cancel();release.countDown();job.join()
        assertFalse(completed);assertEquals(0,provider.descriptors)
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun invocationForwardsOnlyOriginalReadGrantsToExistingService() {
        ShadowSettings.setCanDrawOverlays(true)
        val second=uri.buildUpon().appendPath("2").build()
        val entry=Intent(app,dev.bautista.spotlight.InvokeActivity::class.java).putExtra("query","compartir archivos seleccionados").apply{
            clipData=FileShareContract.canonicalClip(listOf(uri,second));addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        val controller=Robolectric.buildActivity(dev.bautista.spotlight.InvokeActivity::class.java,entry).create()
        try {
            val service=Shadows.shadowOf(controller.get()).nextStartedService
            assertEquals(dev.bautista.spotlight.overlay.OverlayService::class.java.name,service.component!!.className)
            assertEquals(listOf(uri,second),(0 until service.clipData!!.itemCount).map{service.clipData!!.getItemAt(it).uri})
            assertEquals(Intent.FLAG_GRANT_READ_URI_PERMISSION,service.flags)
            assertNull(Shadows.shadowOf(controller.get()).nextStartedActivity)
        }finally{controller.destroy()}
    }
    @Test fun invocationRejectsAnEmbeddedIntentWithoutStartingService() {
        ShadowSettings.setCanDrawOverlays(true)
        val entry=Intent(app,dev.bautista.spotlight.InvokeActivity::class.java).apply{clipData=ClipData("invalid",arrayOf("application/pdf"),ClipData.Item("text",null,Intent(Intent.ACTION_VIEW),uri))}
        val controller=Robolectric.buildActivity(dev.bautista.spotlight.InvokeActivity::class.java,entry).create()
        try{assertTrue(controller.get().isFinishing);assertNull(Shadows.shadowOf(controller.get()).nextStartedService)}finally{controller.destroy()}
    }
}
