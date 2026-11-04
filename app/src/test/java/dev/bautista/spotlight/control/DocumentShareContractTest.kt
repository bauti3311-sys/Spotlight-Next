package dev.bautista.spotlight.control

import android.app.Application
import android.content.*
import android.content.pm.*
import android.net.Uri
import dev.bautista.spotlight.data.FileBuffer
import dev.bautista.spotlight.search.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[26,35],application=Application::class)
class DocumentShareContractTest {
    private val app get()=RuntimeEnvironment.getApplication()
    private val first=Uri.parse("content://fixture.documents/1")
    private val second=Uri.parse("content://fixture.documents/2")
    private val grants=mutableSetOf(first,second)
    private var calls=0
    private val context get()=object:ContextWrapper(app) {
        override fun checkUriPermission(uri:Uri,pid:Int,uid:Int,flags:Int)=if(uri in grants)PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
        override fun startActivity(intent:Intent){calls++}
    }
    private fun row(uri:Uri=first,mime:String="application/pdf")=Result("file:$uri","Documento.pdf","","Archivos",1,
        Action.Launch(Intent(Intent.ACTION_VIEW).setDataAndType(uri,mime)),mime=mime)
    private fun expose(intent:Intent,pkg:String,system:Boolean=intent.action==Intent.ACTION_CHOOSER):ComponentName {
        val info=ApplicationInfo().apply{packageName=pkg;enabled=true;if(system)flags=flags or ApplicationInfo.FLAG_SYSTEM}
        val a=ActivityInfo().apply{packageName=pkg;name="$pkg.Handler";applicationInfo=info;enabled=true;exported=true}
        val pm=Shadows.shadowOf(app.packageManager)
        pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=info;activities=arrayOf(a)})
        pm.addResolveInfoForIntent(intent,ResolveInfo().apply{activityInfo=a;isDefault=true})
        return ComponentName(pkg,a.name)
    }
    private fun send()=Intent(Intent.ACTION_SEND_MULTIPLE).setType("application/pdf")
        .putParcelableArrayListExtra(Intent.EXTRA_STREAM,arrayListOf(first,second))
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        .also{it.clipData=ClipData.newRawUri("files",first).apply{addItem(ClipData.Item(second))}}
    @Test fun oneFileUsesSingleSendWithAnActualContentUri() {
        val buffer=FileBuffer();assertTrue(buffer.add(row()));val intent=buffer.share()
        assertEquals(Intent.ACTION_SEND,intent.action)
        @Suppress("DEPRECATION") val uri=intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        assertEquals(first,uri);assertEquals(1,intent.clipData!!.itemCount)
        assertEquals(Intent.FLAG_GRANT_READ_URI_PERMISSION,intent.flags)
    }
    @Test fun compatibleMixedImagesUseTheirCommonMimeFamily() {
        val buffer=FileBuffer();buffer.add(row(mime="image/png"));buffer.add(row(second,"image/jpeg"))
        assertEquals("image/*",buffer.share().type)
    }
    @Test fun chooserMustInspectInnerReadGrantEvenWithoutOuterClipOrFlags() {
        val inner=send();expose(inner,"fixture.receiver")
        val chooser=Intent.createChooser(inner,"Compartir").setFlags(0).also{it.clipData=null}
        expose(chooser,"fixture.chooser");grants.remove(second)
        assertFalse(NativeLaunchSession(context).launch(chooser,null,"review",false))
        assertEquals(0,calls)
    }
    @Test fun streamArraysCannotBypassGrantChecksWhenClipDataIsMissing() {
        val inner=send().also{it.clipData=null};expose(inner,"fixture.receiver");grants.remove(second)
        assertFalse(NativeLaunchSession(context).launch(inner,null,"review",false));assertEquals(0,calls)
    }
    @Test fun mismatchedStreamAndClipIsRejectedEvenWhenBothUrisAreReadable() {
        val intent=Intent(Intent.ACTION_SEND).setType("application/pdf").putExtra(Intent.EXTRA_STREAM,first)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).also{it.clipData=ClipData.newRawUri("file",second)}
        expose(intent,"fixture.receiver")
        assertFalse(NativeLaunchSession(context).launch(intent,null,"review",false));assertEquals(0,calls)
    }
    @Test fun fileSharingNeverEscalatesToWriteOrPersistableGrants() {
        val intent=send().addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        expose(intent,"fixture.receiver")
        assertFalse(NativeLaunchSession(context).launch(intent,null,"review",false));assertEquals(0,calls)
    }
    @Test fun malformedStreamExtraIsNotAnAuthorizedDocument() {
        val intent=Intent(Intent.ACTION_SEND).setType("application/pdf").putExtra(Intent.EXTRA_STREAM,"content://fixture.documents/1")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        expose(intent,"fixture.receiver")
        assertFalse(NativeLaunchSession(context).launch(intent,null,"review",false));assertEquals(0,calls)
    }
    @Test fun systemChooserRequestsReadableFilesOnceAndNeverClaimsDelivery() {
        val inner=send();expose(inner,"fixture.receiver")
        val chooser=Intent.createChooser(inner,"Compartir");expose(chooser,"fixture.chooser")
        val session=NativeLaunchSession(context)
        assertTrue(session.launch(chooser,null,"review",false))
        assertEquals(1,calls);assertNotNull(session.requestedDestination)
        assertFalse(session.launch(chooser,null,"review",false));assertEquals(1,calls)
    }
    @Test fun anotherAppCannotImpersonateTheAndroidSharesheet() {
        val inner=send();expose(inner,"fixture.receiver")
        val chooser=Intent.createChooser(inner,"Compartir");expose(chooser,"fixture.impostor",system=false)
        assertFalse(NativeLaunchSession(context).launch(chooser,null,"review",false));assertEquals(0,calls)
    }

}
