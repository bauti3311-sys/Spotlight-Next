package dev.bautista.spotlight.sharing

import android.app.Application
import android.content.*
import android.content.pm.*
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver

class ShareDocumentProvider:ContentProvider() {
    var descriptors=0;var queries=0;var missing=false
    lateinit var original:java.io.File
    override fun onCreate():Boolean {original=java.io.File(context!!.cacheDir,"original-share-document");original.writeText("Contenido original del fixture");return true}
    override fun getType(uri:Uri)="application/pdf"
    override fun query(u:Uri,p:Array<out String>?,s:String?,a:Array<out String>?,o:String?)=MatrixCursor(p ?: arrayOf(OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE)).apply{
        queries++;if(!missing)addRow(columnNames.map{when(it){OpenableColumns.DISPLAY_NAME->"Vestibular.pdf";OpenableColumns.SIZE->original.length();else->null}})
    }
    override fun openFile(uri:Uri,mode:String):ParcelFileDescriptor {check(mode=="r");descriptors++;if(missing)throw java.io.FileNotFoundException("private-provider-path");return ParcelFileDescriptor.open(original,ParcelFileDescriptor.MODE_READ_ONLY)}
    override fun insert(u:Uri,v:ContentValues?):Uri?=null
    override fun delete(u:Uri,s:String?,a:Array<out String>?)=0
    override fun update(u:Uri,v:ContentValues?,s:String?,a:Array<out String>?)=0
}
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[26,35],application=Application::class)
class FileSharePreparationTest {
    private val app get()=RuntimeEnvironment.getApplication()
    private val uri=Uri.parse("content://share-files-fixture/document/1")
    private var granted=true
    private val context get()=object:ContextWrapper(app){override fun checkUriPermission(u:Uri,pid:Int,uid:Int,flags:Int)=if(granted)PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED}
    private fun provider():ShareDocumentProvider=ShareDocumentProvider().apply{
        attachInfo(app,ProviderInfo().apply{authority="share-files-fixture"})
        ShadowContentResolver.registerProviderInternal("share-files-fixture",this)
    }
    private fun receiver(exported:Boolean=true) {
        val info=ApplicationInfo().apply{packageName="fixture.reader";enabled=true}
        val a=ActivityInfo().apply{packageName=info.packageName;name="fixture.reader.Send";applicationInfo=info;enabled=true;this.exported=exported}
        val pm=Shadows.shadowOf(app.packageManager);pm.installPackage(PackageInfo().apply{packageName=info.packageName;applicationInfo=info;activities=arrayOf(a)})
        pm.addResolveInfoForIntent(Intent(Intent.ACTION_SEND).setType("application/pdf"),ResolveInfo().apply{activityInfo=a;isDefault=true})
    }
    private fun row()=Result("file:$uri","Índice antiguo.docx","","Archivos",1,Action.Launch(Intent(Intent.ACTION_VIEW,uri)),mime="text/plain")
    @Test fun reviewUsesActualProviderMetadataAndOriginalUriWithoutCopying()=runBlocking {
        val provider=provider();receiver();val before=app.cacheDir.listFiles()!!.map{it.name}.toSet()
        val prepared=FileShareContract.prepare(context,listOf(row()))
        assertEquals("Vestibular.pdf",prepared.documents.single().name)
        assertEquals("application/pdf",prepared.send.type);assertEquals(Intent.ACTION_SEND,prepared.send.action)
        assertEquals(uri,FileShareContract.streams(prepared.send).single())
        assertEquals(1,provider.descriptors);assertEquals(before,app.cacheDir.listFiles()!!.map{it.name}.toSet())
        assertTrue(provider.original.readText().contains("Contenido original"))
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun revokedGrantStopsBeforeMetadataOrOpeningAFile()=runBlocking {
        val provider=provider();receiver();granted=false
        val error=runCatching{FileShareContract.prepare(context,listOf(row()))}.exceptionOrNull() as FileShareContract.Failure
        assertEquals(FileShareContract.Problem.ACCESS,error.problem);assertEquals(0,provider.queries);assertEquals(0,provider.descriptors)
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun privateReceiverIsNotInventedAsAnAvailableDestination()=runBlocking {
        provider();receiver(exported=false)
        val error=runCatching{FileShareContract.prepare(context,listOf(row()))}.exceptionOrNull() as FileShareContract.Failure
        assertEquals(FileShareContract.Problem.NO_TARGET,error.problem);assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun disappearedFileHasNoRequestAndNoProviderPathInTheError()=runBlocking {
        val provider=provider();receiver();provider.missing=true
        val error=runCatching{FileShareContract.prepare(context,listOf(row()))}.exceptionOrNull() as FileShareContract.Failure
        assertFalse(error.message!!.contains("private-provider-path"));assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
}
