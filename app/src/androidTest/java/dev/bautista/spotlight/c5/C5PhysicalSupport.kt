package dev.bautista.spotlight.c5

import android.content.*
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.*
import androidx.test.platform.app.InstrumentationRegistry
import dev.bautista.spotlight.index.*
import dev.bautista.spotlight.data.Config
import org.json.JSONObject
import org.junit.Assume.assumeTrue
import java.io.File
import java.security.MessageDigest

/** Private fixture DB. Never clears the installed app's index, model or preferences. */
internal class C5PhysicalLab {
    val instrumentation=InstrumentationRegistry.getInstrumentation()
    val target=instrumentation.targetContext
    val args=InstrumentationRegistry.getArguments()
    val runId=args.getString("c5_run_id").orEmpty()
    init {
        require(runId.matches(Regex("[A-Za-z0-9_-]{1,64}"))){"Explicit c5_run_id required"}
        require(args.getString("c5_authorize")=="FIXTURES_ONLY"){"Explicit future execution authorization required"}
        require(args.getString("c5_source_sha").orEmpty().matches(Regex("[0-9a-f]{40}"))){"Operator-attested authorized source SHA required"}
        require(args.getString("c5_target_package")==target.packageName){"Unexpected target package"}
        val expected=args.getString("c5_expected_cert_sha256").orEmpty()
        require(expected.matches(Regex("[0-9a-f]{64}"))){"Approved historical certificate digest required"}
        for(pkg in listOf(target.packageName,instrumentation.context.packageName)) {
            @Suppress("DEPRECATION")
            val signatures=if(Build.VERSION.SDK_INT>=28)target.packageManager.getPackageInfo(pkg,android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES).signingInfo!!.apkContentsSigners
                else target.packageManager.getPackageInfo(pkg,android.content.pm.PackageManager.GET_SIGNATURES).signatures!!
            require(signatures.size==1 && sha(signatures.single().toByteArray())==expected){"App/test APK certificate does not match approved identity"}
        }
    }
    val directory=File(target.getExternalFilesDir(null) ?: target.filesDir,"c5-physical/$runId").apply{mkdirs()}
    val context=object:ContextWrapper(target) {
        private val isolated=File(target.cacheDir,"c5-isolated-"+java.util.UUID.randomUUID()).apply{mkdirs()}
        override fun getCacheDir():File=File(isolated,"cache").apply{mkdirs()}
        override fun getDatabasePath(name:String):File=File(isolated,"db/$name").apply{parentFile!!.mkdirs()}
        override fun openOrCreateDatabase(name:String,mode:Int,factory:SQLiteDatabase.CursorFactory?):SQLiteDatabase=SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name),factory)
        override fun openOrCreateDatabase(name:String,mode:Int,factory:SQLiteDatabase.CursorFactory?,errorHandler:DatabaseErrorHandler?):SQLiteDatabase=SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name).path,factory,errorHandler)
    }
    fun sha(bytes:ByteArray)=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
    fun asset(name:String)=instrumentation.context.assets.open("c5/$name").use{it.readBytes()}
    fun file(name:String,bytes:ByteArray)=File(context.cacheDir,name).apply{writeBytes(bytes)}
    private val written=mutableSetOf<String>()
    @Synchronized fun report(name:String,value:JSONObject) {
        value.put("run_id",runId).put("operator_attested_source_sha",args.getString("c5_source_sha")).put("device",Build.MODEL)
            .put("sdk",Build.VERSION.SDK_INT).put("target_package",target.packageName)
            .put("source_attestation_is_not_cryptographic_apk_proof",true)
        val info=target.packageManager.getPackageInfo(target.packageName,0)
        @Suppress("DEPRECATION")
        value.put("version_name",info.versionName).put("version_code",if(Build.VERSION.SDK_INT>=28)info.longVersionCode else info.versionCode.toLong())
        value.put("verified_cert_sha256",args.getString("c5_expected_cert_sha256"))
        val report=File(directory,"$name.json")
        if(name !in written){check(report.createNewFile()){"Existing evidence: use a fresh run ID"};written.add(name)}
        val atomic=android.util.AtomicFile(report);val stream=atomic.startWrite()
        try{stream.write(value.toString(2).toByteArray(Charsets.UTF_8));atomic.finishWrite(stream)}
        catch(e:Throwable){atomic.failWrite(stream);throw e}
        instrumentation.sendStatus(0,Bundle().apply{putString("c5_report",report.absolutePath);putString("c5_status",value.optString("status"));putString("c5_json",value.toString())})
    }
    fun gate(name:String,allowed:Boolean,reason:String) {
        if(!allowed)report(name,JSONObject().put("status","BLOCKED_NOT_EXECUTED").put("reason",reason))
        assumeTrue(reason,allowed)
    }
    fun pssKiB():Int=Debug.MemoryInfo().also{Debug.getMemoryInfo(it)}.totalPss
    fun index(config:()->Config)=FileIndex(context,sharedAccess={false},storageRoots={emptyList()},liveConfig=config)
    suspend fun runCase(name:String,body:suspend ()->JSONObject) {
        try{report(name,body().put("status","PASS"))}
        catch(e:org.junit.AssumptionViolatedException){throw e}
        catch(e:Throwable){report(name,JSONObject().put("status","FAIL").put("error",e.toString()));throw e}
    }
    suspend fun seed(index:FileIndex,file:File,mime:String,c:Config,doc:ExtractedDocument?=null):ExtractedDocument {
        val extracted=doc ?: DocumentText.extractDetailed(context,Uri.fromFile(file),mime,limits=PdfTextLimits(ocrPages=0))
        index.storeTranscript(Uri.fromFile(file).toString(),file.name,mime,extracted.text)
        check(index.cacheExtraction(Uri.fromFile(file).toString(),extracted,c));return extracted
    }
}
