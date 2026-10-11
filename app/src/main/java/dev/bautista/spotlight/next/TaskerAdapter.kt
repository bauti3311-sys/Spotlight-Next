package dev.bautista.spotlight.next

import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/** Contract from tasker.joaoapps.com/code/TaskerIntent.java, version 1.3.3. */
object TaskerContract {
    const val BASE="net.dinglisch.android.tasker"
    const val PERMISSION="$BASE.PERMISSION_RUN_TASKS"
    val packages=listOf("${BASE}m",BASE)
    fun taskIntent(pkg:String,name:String,parameters:List<String>,token:String=UUID.randomUUID().toString()):Intent {
        require(pkg in packages && name.isNotBlank() && name.length<=200 && parameters.size<=10 && parameters.all{it.length<=8000})
        return Intent("$BASE.ACTION_TASK").setPackage(pkg).setData(Uri.parse("id:$token"))
            .putExtra("version_number","1.1").putExtra("task_name",name).putExtra("task_priority",5)
            .putStringArrayListExtra("varNames",ArrayList(parameters.indices.map{"%par${it+1}"}))
            .putStringArrayListExtra("varValues",ArrayList(parameters))
    }
    fun preferences(pkg:String)=Intent("$BASE.ACTION_OPEN_PREFS").setPackage(pkg).putExtra("tno",3)
}
data class TaskerTask(val name:String,val project:String)
data class TaskerSnapshot(val packageName:String?=null,val available:Boolean=false,val explanation:String,val tasks:List<TaskerTask> = emptyList())
class TaskerAdapter(private val context:Context) {
    companion object { private val execution=Mutex() }
    suspend fun discover():TaskerSnapshot=withContext(Dispatchers.IO) {
        val pkg=TaskerContract.packages.firstOrNull {runCatching{context.packageManager.getApplicationInfo(it,0);true}.getOrDefault(false)}
            ?: return@withContext TaskerSnapshot(explanation="Tasker no está instalado. Las funciones locales no lo necesitan.")
        if(ContextCompat.checkSelfPermission(context,TaskerContract.PERMISSION)!=PackageManager.PERMISSION_GRANTED)return@withContext TaskerSnapshot(pkg,explanation="Android no concedió el permiso de Tasker. Si lo instalaste después de Spotlight, reinstalá esta actualización conservando datos.")
        try {
            val prefs=context.contentResolver.query(Uri.parse("content://${TaskerContract.BASE}/prefs"),arrayOf("enabled","ext_access"),null,null,null)?.use {if(it.moveToFirst())(it.getInt(0)!=0) to (it.getInt(1)!=0) else null}
                ?: return@withContext TaskerSnapshot(pkg,explanation="Tasker no respondió a la consulta de preferencias.")
            if(!prefs.first)return@withContext TaskerSnapshot(pkg,explanation="Tasker está desactivado. Activá su interruptor principal.")
            if(!prefs.second)return@withContext TaskerSnapshot(pkg,explanation="Activá Preferencias → Varios → Permitir acceso externo en Tasker.")
            val tasks=context.contentResolver.query(Uri.parse("content://${TaskerContract.BASE}/tasks"),arrayOf("name","project_name"),null,null,null)?.use { c ->buildList {while(c.moveToNext() && size<300)add(TaskerTask(c.getString(0),c.getString(1).orEmpty()))}}.orEmpty()
            TaskerSnapshot(pkg,true,"Tareas descubiertas mediante el proveedor oficial de Tasker.",tasks)
        } catch(e:Exception){TaskerSnapshot(pkg,explanation="No se pudo leer Tasker. Revisá acceso externo y versión instalada.")}
    }
    suspend fun run(name:String,parameters:List<String>):String {
        check(execution.tryLock()){ "Ya hay una tarea de Spotlight en ejecución." }
        try {
            val snapshot=discover();check(snapshot.available){snapshot.explanation};check(snapshot.tasks.any{it.name==name}){"La tarea ya no está disponible."}
            val intent=TaskerContract.taskIntent(snapshot.packageName!!,name,parameters)
            val done=CompletableDeferred<Boolean>()
            val receiver=object:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){if(i.action=="${TaskerContract.BASE}.ACTION_TASK_COMPLETE" && i.data?.scheme=="task" && i.data?.schemeSpecificPart==name)done.complete(i.getBooleanExtra("success",false))}}
            val filter=IntentFilter("${TaskerContract.BASE}.ACTION_TASK_COMPLETE").apply{addDataScheme("task")}
            // Completion is emitted by another app; only accept that app's permission.
            ContextCompat.registerReceiver(context,receiver,filter,TaskerContract.PERMISSION,null,ContextCompat.RECEIVER_EXPORTED)
            try {context.sendBroadcast(intent);val completed=withTimeoutOrNull(30000){done.await()};return when(completed){true->"Tasker informó que la tarea terminó correctamente.";false->"Tasker informó un error en la tarea.";null->"Enviada a Tasker, sin confirmación en 30 segundos. Puede seguir ejecutándose; no se volvió a enviar."}}
            finally {context.unregisterReceiver(receiver)}
        } finally {execution.unlock()}
    }
}
