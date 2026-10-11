package dev.bautista.spotlight.advanced

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Process
import android.provider.Settings
import androidx.annotation.Keep
import java.util.concurrent.TimeUnit

/** Official Shizuku UserService. No command string or general executor crosses Binder. */
@Keep
class AdvancedUserService @Keep constructor(private val context:Context):IAdvancedService.Stub() {
    private fun caller(){check(Process.myUid()==2000){"Solo se admite identidad ADB."};check(Binder.getCallingUid()==context.applicationInfo.uid){"Origen no autorizado."}}
    override fun effectiveUid():Int{caller();return Process.myUid()}
    private fun settings(vararg args:String):String {
        val process=ProcessBuilder(listOf("/system/bin/settings")+args).redirectErrorStream(true).start()
        try {
            check(process.waitFor(5,TimeUnit.SECONDS)){"La operación superó el tiempo disponible."}
            val text=process.inputStream.bufferedReader().use{it.readText().take(4096)}
            check(process.exitValue()==0){"Android rechazó la operación."}
            check(!text.contains("Exception",true) && !text.contains("Permission denial",true)){"Android no permite modificar este ajuste."}
            return text.trim()
        }finally{process.destroy()}
    }
    override fun readSetting(key:String):String {caller();require(AdvancedPolicy.readableSetting(key));return settings("--user",(context.applicationInfo.uid/100000).toString(),"get","system",key)}
    override fun canWriteSettings():Boolean {caller();return context.packageManager.checkPermission("android.permission.WRITE_SECURE_SETTINGS","com.android.shell")==PackageManager.PERMISSION_GRANTED}
    override fun writeSetting(key:String,value:String,confirmed:Boolean){caller();check(confirmed){"Requiere confirmación."};require(AdvancedPolicy.validSetting(key,value));check(canWriteSettings()){ "Permiso no disponible." };settings("--user",(context.applicationInfo.uid/100000).toString(),"put","system",key,value);check(readSetting(key)==value){"El sistema no conservó el valor solicitado."}}
    override fun stopApplication(packageName:String,confirmed:Boolean){
        caller();check(confirmed){"Requiere confirmación."}
        val pm=context.packageManager;val info=pm.getApplicationInfo(packageName,0)
        val protected=mutableSetOf(context.packageName,ShizukuManager.PACKAGE)
        pm.resolveActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName?.let{protected+=it}
        // Public input-method metadata is available through installed service components.
        pm.queryIntentServices(Intent("android.view.InputMethod"),0).forEach{protected+=it.serviceInfo.packageName}
        listOf(Intent.ACTION_DIAL,Intent.ACTION_SENDTO).forEach{action->val intent=Intent(action);if(action==Intent.ACTION_SENDTO)intent.data=android.net.Uri.parse("smsto:");pm.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName?.let{protected+=it}}
        check(AdvancedPolicy.canStop(packageName,info.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)!=0,protected)){"La aplicación está protegida."}
        val manager=Class.forName("android.app.ActivityManager").getDeclaredMethod("getService").invoke(null)
        manager.javaClass.getMethod("forceStopPackage",String::class.java,Integer.TYPE).apply{isAccessible=true}.invoke(manager,packageName,context.applicationInfo.uid/100000)
    }
    override fun destroy(){ kotlin.system.exitProcess(0) }
}
