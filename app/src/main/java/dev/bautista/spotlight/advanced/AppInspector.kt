package dev.bautista.spotlight.advanced

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.*

data class InspectedApp(val packageName:String,val label:String,val version:String,val system:Boolean,val permissions:List<Pair<String,Boolean>>,val components:List<String>)
object AppInspector {
    suspend fun inspect(context:Context,packageName:String)=withContext(Dispatchers.IO){
        require(packageName.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")))
        val pm=context.packageManager;val p=pm.getPackageInfo(packageName,PackageManager.GET_PERMISSIONS or PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES or PackageManager.GET_PROVIDERS or PackageManager.GET_RECEIVERS)
        val app=p.applicationInfo ?: error("La aplicación ya no está instalada.")
        InspectedApp(packageName,pm.getApplicationLabel(app).toString(),p.versionName ?: "Sin versión",app.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)!=0,
            p.requestedPermissions.orEmpty().mapIndexed{i,name->name to ((p.requestedPermissionsFlags?.getOrNull(i) ?: 0) and android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED!=0)},
            (p.activities.orEmpty().filter{it.exported}.map{"Actividad · ${it.name}"}+p.services.orEmpty().filter{it.exported}.map{"Servicio · ${it.name}"}+p.providers.orEmpty().filter{it.exported}.map{"Proveedor · ${it.name}"}+p.receivers.orEmpty().filter{it.exported}.map{"Receptor · ${it.name}"}).take(100))
    }
}
