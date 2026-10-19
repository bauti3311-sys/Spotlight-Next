package dev.bautista.spotlight.control

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/** Reports platform prerequisites, never equates a service/permission with executable tools. */
object AppFunctionsSupport {
    fun inspect(context:Context):String {
        val service=Build.VERSION.SDK_INT>=36 && runCatching{context.getSystemService("app_function")!=null}.getOrDefault(false)
        val authorized=listOf("android.permission.EXECUTE_APP_FUNCTIONS","android.permission.EXECUTE_APP_FUNCTIONS_SYSTEM")
            .any{ContextCompat.checkSelfPermission(context,it)==PackageManager.PERMISSION_GRANTED}
        return describe(Build.VERSION.SDK_INT,service,authorized)
    }
    internal fun describe(sdk:Int,service:Boolean,authorized:Boolean):String = when {
        sdk<36 -> "AppFunctions no compatible: requiere Android 16 o posterior. Se conservan Intents públicos y acciones locales."
        !service -> "AppFunctions no disponible: el sistema no expone el servicio. Se conservan Intents públicos y acciones locales."
        !authorized -> "AppFunctions requiere autorización del sistema para descubrir y ejecutar funciones ajenas. No es un permiso común que Spotlight pueda solicitar. Usá las acciones públicas disponibles."
        else -> "Servicio AppFunctions y autorización detectados. Integración limitada: Spotlight todavía no descubre ni ejecuta funciones ajenas; no se anuncian como herramientas disponibles."
    }
}
