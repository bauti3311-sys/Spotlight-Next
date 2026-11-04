package dev.bautista.spotlight.control

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Parcel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.security.MessageDigest
import java.util.UUID

internal enum class LaunchProblem(val message:String) {
    INVALID_INPUT("La acción no es válida. Volvé a seleccionarla."),
    UNAVAILABLE("El destino ya no está disponible. Revisá la app elegida o sus ajustes."),
    PERMISSION_REQUIRED("Android no permite abrir este destino. Revisá sus permisos y volvé a seleccionarlo."),
    STALE_TARGET("La revisión cambió o venció. Volvé a seleccionar la acción."),
    ALREADY_REQUESTED("Esta acción ya fue solicitada o revisada. Volvé a seleccionarla desde otra búsqueda para continuar."),
    UNKNOWN_OUTCOME("No se pudo comprobar el resultado. Revisá la app antes de volver a intentarlo; no se reintentó la acción.")
}
internal data class LaunchAlternative(val id:String,val label:String,val destination:String,
    internal val intent:Intent,internal val signature:String,internal val component:ComponentName,internal val close:Boolean)
internal data class LaunchIssue(val problem:LaunchProblem,val alternative:LaunchAlternative?=null)

/** Overlay-scoped fence/review around the existing Android startActivity executor.
 * No registry, background job, retry, persisted authorization or second action engine. */
internal class NativeLaunchSession(private val context:Context) {
    var issue by mutableStateOf<LaunchIssue?>(null)
        private set
    // Ephemeral receipt of startActivity returning, never proof of an external effect.
    var requestedDestination by mutableStateOf<String?>(null)
        private set
    private var origin:Any?=null
    private val consumed=mutableSetOf<String>()
    private var disposed=false
    fun synchronize(current:Any) {
        if(origin!=current){origin=current;consumed.clear();issue=null;requestedDestination=null}
    }
    fun cancel(){
        if(issue?.problem in setOf(LaunchProblem.INVALID_INPUT,LaunchProblem.UNAVAILABLE,LaunchProblem.PERMISSION_REQUIRED,LaunchProblem.STALE_TARGET))consumed.clear()
        issue=null
    }
    fun signature(intent:Intent):String=runCatching{snapshot(intent).second}.getOrDefault("invalid")
    fun dispose(){disposed=true;issue=null;requestedDestination=null;consumed.clear()}
    private fun snapshot(intent:Intent):Pair<Intent,String> {
        val parcel=Parcel.obtain()
        try {
            intent.writeToParcel(parcel,0)
            val bytes=parcel.marshall()
            require(bytes.size<=262144 && !intent.hasFileDescriptors())
            parcel.setDataPosition(0)
            val copy=Intent.CREATOR.createFromParcel(parcel)
            val hash=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
            return copy to hash
        }finally{parcel.recycle()}
    }
    fun launch(primary:Intent,fallback:Intent?,current:Any,close:Boolean):Boolean {
        if(disposed)return false
        synchronize(current)
        val (intent,key)=try{snapshot(primary)}catch(_:Exception){issue=LaunchIssue(LaunchProblem.INVALID_INPUT);return false}
        if(key in consumed || consumed.size>=64){
            if(issue==null)issue=LaunchIssue(LaunchProblem.ALREADY_REQUESTED)
            return false
        }
        consumed+=key
        issue=null
        requestedDestination=null
        return dispatch(intent,null,close,fallback)
    }
    fun confirm(id:String,current:Any):Boolean {
        if(disposed)return false
        if(origin!=current){synchronize(current);issue=LaunchIssue(LaunchProblem.STALE_TARGET);return false}
        val alternative=issue?.alternative?.takeIf{it.id==id} ?: return false
        // Consume the proposal before validation or Android dispatch, including failure paths.
        issue=null
        val (intent,key)=try{snapshot(alternative.intent)}catch(_:Exception){issue=LaunchIssue(LaunchProblem.INVALID_INPUT);return false}
        if(key!=alternative.signature){issue=LaunchIssue(LaunchProblem.STALE_TARGET);return false}
        if(!consumed.add(key)){issue=LaunchIssue(LaunchProblem.ALREADY_REQUESTED);return false}
        requestedDestination=null
        return dispatch(intent,alternative.component,alternative.close,null)
    }
    private fun dispatch(intent:Intent,reviewed:ComponentName?,close:Boolean,fallback:Intent?):Boolean {
        val access=IntegrationRegistry(context).launchAccess(intent)
        val failure=access.problem ?: if(reviewed!=null && access.component!=reviewed)LaunchProblem.STALE_TARGET else null
        if(failure!=null){fail(failure,fallback,close);return false}
        try {
            // Pin the just-validated handler so Android cannot silently choose a different one.
            context.startActivity(Intent(intent).setComponent(access.component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }catch(_:ActivityNotFoundException){fail(LaunchProblem.UNAVAILABLE,fallback,close);return false}
        catch(_:SecurityException){fail(LaunchProblem.PERMISSION_REQUIRED,fallback,close);return false}
        catch(_:Exception){fail(LaunchProblem.UNKNOWN_OUTCOME,null,close);return false}
        issue=null
        requestedDestination=access.label
        return true // REQUESTED, not proof of the external effect.
    }
    private fun fail(problem:LaunchProblem,fallback:Intent?,close:Boolean) {
        val proposal=if(fallback!=null)runCatching {
            val (copy,key)=snapshot(fallback)
            val access=IntegrationRegistry(context).launchAccess(copy)
            if(access.problem!=null || access.component==null || key in consumed)null else
                LaunchAlternative(UUID.randomUUID().toString(),access.label,
                    copy.data?.toString()?.take(1000) ?: access.component.flattenToShortString(),copy,key,access.component,close)
        }.getOrNull() else null
        issue=LaunchIssue(problem,proposal)
    }
}
