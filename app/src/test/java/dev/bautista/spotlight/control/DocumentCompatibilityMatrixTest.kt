package dev.bautista.spotlight.control

import android.app.Application
import android.content.*
import android.content.pm.*
import android.net.Uri
import dev.bautista.spotlight.data.FileBuffer
import dev.bautista.spotlight.sharing.FileShareContract
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

/** Fixtures exercise contracts, not installed vendor applications or delivery. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[26,35],application=Application::class)
class DocumentCompatibilityMatrixTest {
    private val app get()=RuntimeEnvironment.getApplication()
    private val context get()=object:ContextWrapper(app){override fun checkUriPermission(u:Uri,pid:Int,uid:Int,flags:Int)=PackageManager.PERMISSION_GRANTED}
    private fun expose(pkg:String,action:String,mime:String,exported:Boolean=true) {
        val info=ApplicationInfo().apply{packageName=pkg;enabled=true}
        val a=ActivityInfo().apply{packageName=pkg;name="$pkg.PublicContract";applicationInfo=info;enabled=true;this.exported=exported}
        val pm=Shadows.shadowOf(app.packageManager)
        pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=info})
        pm.addActivityIfNotPresent(ComponentName(pkg,a.name));pm.addOrUpdateActivity(a)
        pm.addIntentFilterForActivity(ComponentName(pkg,a.name),IntentFilter(action).apply{addCategory(Intent.CATEGORY_DEFAULT);addDataType(mime)})
    }
    private fun send(pkg:String,multiple:Boolean=false,mime:String="application/pdf"):Intent {
        val buffer=FileBuffer()
        repeat(if(multiple)2 else 1){n->buffer.add(FileShareContract.result(FileShareContract.Document(Uri.parse("content://fixture.docs/$n"),"document.pdf",mime,100,"Fixture")))}
        return buffer.share(context,pkg)
    }
    @Test fun personalWhatsAppTextHandlerDoesNotImplyPdfOrBusinessSupport() {
        expose("com.whatsapp",Intent.ACTION_SEND,"text/plain")
        assertFalse(IntegrationRegistry(context).hasCompatibleShareTarget(send("com.whatsapp")))
        assertFalse(IntegrationRegistry(context).hasCompatibleShareTarget(send("com.whatsapp.w4b")))
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun businessReceiverDoesNotSilentlyFallBackToPersonal() {
        expose("com.whatsapp",Intent.ACTION_SEND,"application/pdf")
        assertTrue(IntegrationRegistry(context).hasCompatibleShareTarget(send("com.whatsapp")))
        assertFalse(IntegrationRegistry(context).hasCompatibleShareTarget(send("com.whatsapp.w4b")))
    }
    @Test fun gmailSingleAttachmentDoesNotImplyMultipleAttachmentSupport() {
        expose("com.google.android.gm",Intent.ACTION_SEND,"application/pdf")
        assertTrue(IntegrationRegistry(context).hasCompatibleShareTarget(send("com.google.android.gm")))
        assertFalse(IntegrationRegistry(context).hasCompatibleShareTarget(send("com.google.android.gm",multiple=true)))
    }
    @Test fun spotifyAndVivaldiCannotBeInventedAsDocumentReceivers() {
        expose("com.spotify.music",Intent.ACTION_WEB_SEARCH,"text/plain")
        expose("com.vivaldi.browser",Intent.ACTION_VIEW,"application/pdf")
        assertFalse(IntegrationRegistry(context).hasCompatibleShareTarget(send("com.spotify.music")))
        assertFalse(IntegrationRegistry(context).hasCompatibleShareTarget(send("com.vivaldi.browser")))
    }
    @Test fun publicMultipleDocumentHandlerIsDistinctFromPrivateOrRemovedReceiver() {
        expose("fixture.documents",Intent.ACTION_SEND_MULTIPLE,"application/pdf")
        val registry=IntegrationRegistry(context)
        assertTrue(registry.hasCompatibleShareTarget(send("fixture.documents",multiple=true)))
        Shadows.shadowOf(app.packageManager).removePackage("fixture.documents")
        assertFalse(registry.hasCompatibleShareTarget(send("fixture.documents",multiple=true)))
        expose("fixture.private",Intent.ACTION_SEND,"application/pdf",exported=false)
        assertFalse(registry.hasCompatibleShareTarget(send("fixture.private")))
    }
}
