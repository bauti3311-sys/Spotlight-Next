package dev.bautista.spotlight.advanced

import android.content.*
import android.os.IBinder
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rikka.shizuku.Shizuku
import org.json.JSONObject

interface AdvancedBackend {suspend fun read(key:String):String;suspend fun canWrite():Boolean;suspend fun write(key:String,value:String,confirmed:Boolean);suspend fun stop(packageName:String,confirmed:Boolean)}
class ShizukuBackend(private val context:Context,private val manager:ShizukuManager):AdvancedBackend {
    private val mutex=Mutex()
    private suspend fun <T> call(block:(IAdvancedService)->T):T=mutex.withLock {
        manager.refresh();check(manager.state.value.available){"Iniciá Shizuku y autorizá Spotlight en modo ADB."}
        val args=Shizuku.UserServiceArgs(ComponentName(context,AdvancedUserService::class.java)).tag("spotlight.advanced.1").version(1).daemon(false).processNameSuffix("advanced")
        val connected=CompletableDeferred<IAdvancedService>()
        val connection=object:ServiceConnection{
            override fun onServiceConnected(name:ComponentName,binder:IBinder){connected.complete(IAdvancedService.Stub.asInterface(binder))}
            override fun onServiceDisconnected(name:ComponentName){connected.completeExceptionally(IllegalStateException("Shizuku se desconectó."));manager.refresh()}
        }
        try {
            withContext(Dispatchers.Main){Shizuku.bindUserService(args,connection)}
            val service=withTimeout(8000){connected.await()}
            currentCoroutineContext().ensureActive();manager.refresh();check(manager.state.value.available){"La autorización o conexión cambió."}
            withContext(Dispatchers.IO){check(service.effectiveUid()==2000);block(service)}
        }catch(e:CancellationException){throw e}catch(e:Exception){manager.refresh();throw IllegalStateException("No se pudo realizar la operación. Revisá Shizuku y la compatibilidad del dispositivo.",e)}
        finally{withContext(NonCancellable+Dispatchers.Main){runCatching{Shizuku.unbindUserService(args,connection,true)}}}
    }
    override suspend fun read(key:String)=call{it.readSetting(key)}
    override suspend fun canWrite()=call{it.canWriteSettings()}.also{manager.reportWriteCapability(it)}
    override suspend fun write(key:String,value:String,confirmed:Boolean){call{it.writeSetting(key,value,confirmed)}}
    override suspend fun stop(packageName:String,confirmed:Boolean){call{it.stopApplication(packageName,confirmed)}}
}
interface RecoveryJournal {fun original(key:String):String?;fun remember(key:String,value:String);fun clear(key:String);fun entries():Map<String,String>}
class SettingsJournal(context:Context):RecoveryJournal {
    private val prefs=context.getSharedPreferences("advanced_recovery",Context.MODE_PRIVATE)
    override fun original(key:String)=prefs.getString(key,null)
    override fun remember(key:String,value:String){if(original(key)==null)check(prefs.edit().putString(key,value).commit()){"No se pudo guardar el estado anterior."}}
    override fun clear(key:String){check(prefs.edit().remove(key).commit())}
    override fun entries()=prefs.all.filterKeys{it in AdvancedPolicy.settingKeys}.mapValues{it.value.toString()}
}
class ReversibleSettings(private val backend:AdvancedBackend,private val journal:RecoveryJournal) {
    private val mutex=Mutex()
    suspend fun apply(key:String,value:String,confirmed:Boolean)=mutex.withLock{
        check(confirmed){"Confirmá el cambio antes de aplicar."};require(AdvancedPolicy.validSetting(key,value)){"Valor fuera de los límites permitidos."}
        check(backend.canWrite()){ "Este dispositivo no permite el cambio con Shizuku." }
        val before=backend.read(key);check(AdvancedPolicy.validSetting(key,before)){"No se pudo guardar un valor anterior restaurable."}
        journal.remember(key,before);backend.write(key,value,true)
        check(backend.read(key)==value){"No se pudo verificar el cambio. El estado anterior permanece guardado."}
    }
    suspend fun restore(key:String,confirmed:Boolean)=mutex.withLock{
        check(confirmed){"Confirmá la restauración."};val value=journal.original(key) ?: error("No hay un valor anterior guardado.")
        require(AdvancedPolicy.validSetting(key,value));backend.write(key,value,true);check(backend.read(key)==value);journal.clear(key)
    }
}
