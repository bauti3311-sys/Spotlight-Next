package dev.bautista.spotlight.advanced

import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

/** Process-scoped listeners; no polling and no binder query on each keystroke. */
class ShizukuManager(private val context:Context) {
    private val mutable=MutableStateFlow(AdvancedState())
    val state=mutable.asStateFlow()
    private val writeMutable=MutableStateFlow<Boolean?>(null)
    val writeCapability=writeMutable.asStateFlow()
    fun reportWriteCapability(available:Boolean){writeMutable.value=available}
    private var seen=false
    private val handler=Handler(Looper.getMainLooper())
    private val received=Shizuku.OnBinderReceivedListener { refresh() }
    private val dead=Shizuku.OnBinderDeadListener { refresh() }
    private val permission=Shizuku.OnRequestPermissionResultListener { code,_ -> if(code==REQUEST)refresh() }
    @Volatile private var started=false
    @Synchronized fun start(){
        if(started)return
        started=true
        Shizuku.addBinderReceivedListenerSticky(received,handler)
        Shizuku.addBinderDeadListener(dead,handler)
        Shizuku.addRequestPermissionResultListener(permission,handler)
        refresh()
    }
    fun refresh(){
        val installed=runCatching{context.packageManager.getApplicationInfo(PACKAGE,0)}.isSuccess
        val alive=runCatching{Shizuku.pingBinder()}.getOrDefault(false)
        val granted=alive && runCatching{Shizuku.checkSelfPermission()==PackageManager.PERMISSION_GRANTED}.getOrDefault(false)
        val denied=alive && !granted && runCatching{Shizuku.shouldShowRequestPermissionRationale()}.getOrDefault(false)
        val version=if(alive)runCatching{Shizuku.getVersion()}.getOrNull() else null
        val uid=if(granted)runCatching{Shizuku.getUid()}.getOrNull() else null
        if(!alive || !granted)writeMutable.value=null
        mutable.value=AdvancedPolicy.state(installed,alive,granted,denied,version,uid,seen)
        if(alive && granted)seen=true
    }
    fun requestAuthorization():String? {
        start()
        if(!runCatching{Shizuku.pingBinder()}.getOrDefault(false)){refresh();return "Iniciá Shizuku antes de solicitar autorización."}
        if(runCatching{Shizuku.isPreV11()}.getOrDefault(true))return "Actualizá Shizuku desde su fuente oficial."
        if(runCatching{Shizuku.shouldShowRequestPermissionRationale()}.getOrDefault(false))return "Permití Spotlight desde Aplicaciones autorizadas en Shizuku."
        return runCatching{Shizuku.requestPermission(REQUEST);null}.getOrElse{"No se pudo solicitar autorización. Abrí Shizuku para revisarla."}
    }
    companion object {const val PACKAGE="moe.shizuku.privileged.api";const val REQUEST=1001}
}
