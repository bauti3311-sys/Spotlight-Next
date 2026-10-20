package dev.bautista.spotlight.ui

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.Replies
import dev.bautista.spotlight.data.Config

/** A single source of truth for settings access: status is READ, never a fake grant toggle. */
internal data class PermissionRow(
    val id:String,
    val label:String,
    val explanation:String,
    val allowed:Boolean,
    val status:String,
    val icon:ImageVector
)

internal fun accessRows(app:SpotlightApp,c:Config):List<PermissionRow> {
    fun granted(permission:String)=ContextCompat.checkSelfPermission(app,permission)==PackageManager.PERMISSION_GRANTED
    val enabledAccessibility=Settings.Secure.getString(
        app.contentResolver,Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty().split(':')
        .mapNotNull(ComponentName::unflattenFromString)
    val listeners=Settings.Secure.getString(
        app.contentResolver,"enabled_notification_listeners").orEmpty().split(':')
        .mapNotNull(ComponentName::unflattenFromString)
    val notificationListener=ComponentName(app,dev.bautista.spotlight.control.ReplyService::class.java) in listeners
    val keyboard=ComponentName(app,dev.bautista.spotlight.access.ShortcutService::class.java) in enabledAccessibility
    val phoneUse=ComponentName(app,dev.bautista.spotlight.agent.PhoneUseService::class.java) in enabledAccessibility
    val folders=app.contentResolver.persistedUriPermissions.count{
        it.isReadPermission && it.uri.toString() in c.trees
    }
    val broad=app.index.broadAllowed()
    val fileAllowed=folders>0 || broad
    val shizukuStatus=app.shizuku.state.value
    val shizuku=shizukuStatus.available && shizukuStatus.authorized
    val usage=runCatching {
        val manager=app.getSystemService(android.app.AppOpsManager::class.java)
        manager?.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),app.packageName
        )==android.app.AppOpsManager.MODE_ALLOWED
    }.getOrDefault(false)
    return listOf(
        PermissionRow("overlay","Ventana flotante","Mostrar Spotlight sobre otras aplicaciones.",
            Settings.canDrawOverlays(app),if(Settings.canDrawOverlays(app))"Concedido" else "Sin conceder",CommandIcons.Display),
        PermissionRow("contacts","Contactos","Encontrar contactos autorizados sin enviarlos a un proveedor.",
            granted(Manifest.permission.READ_CONTACTS),if(granted(Manifest.permission.READ_CONTACTS))"Concedido" else "Sin conceder",CommandIcons.Person),
        PermissionRow("notifications","Notificaciones y mensajes","Leer y responder únicamente las notificaciones compatibles.",
            notificationListener && Replies.connected.value,if(notificationListener)if(Replies.connected.value)"Servicio activo" else "Concedido · sin conexión" else "Sin conceder",CommandIcons.ChatBubbleOutline),
        PermissionRow("files","Archivos y documentos","Acceso a carpetas elegidas; Android protege archivos privados de otras apps.",
            fileAllowed,if(broad)"Almacenamiento autorizado" else if(folders>0)"$folders carpeta(s) autorizada(s)" else "Sin carpetas autorizadas",CommandIcons.Folder),
        PermissionRow("calendar","Calendario","Consultar eventos; modificaciones requieren autorización adicional.",
            granted(Manifest.permission.READ_CALENDAR),if(granted(Manifest.permission.READ_CALENDAR))"Concedido" else "Sin conceder",CommandIcons.Event),
        PermissionRow("location","Ubicación","Clima y sugerencias basadas en ubicación aproximada.",
            granted(Manifest.permission.ACCESS_COARSE_LOCATION),if(granted(Manifest.permission.ACCESS_COARSE_LOCATION))"Concedido" else "Sin conceder",CommandIcons.Cloud),
        PermissionRow("keyboard","Atajo de teclado","Servicio independiente para invocar la barra con teclado físico.",
            keyboard,if(keyboard)"Habilitado" else "Opcional",CommandIcons.Keyboard),
        PermissionRow("phone_use","Control de aplicaciones","Servicio optativo para acciones sobre otras apps con confirmación.",
            phoneUse,if(!phoneUse)"Sin conceder" else if(dev.bautista.spotlight.agent.PhoneUseService.connected())"Habilitado y conectado" else "Habilitado · esperando conexión",CommandIcons.Apps),
        PermissionRow("shizuku","Shizuku","Funciones avanzadas de Android sin root, cuando el servicio está disponible.",
            shizuku,if(shizuku)"Conectado y autorizado" else if(shizukuStatus.available)"Disponible · falta autorización" else "Opcional · sin conexión",CommandIcons.Settings),
        PermissionRow("storage_broad","Almacenamiento ampliado","Acceso especial a archivos compartidos; opcional si usás carpetas elegidas.",
            broad,if(broad)"Concedido" else "Opcional",CommandIcons.Folder),
        PermissionRow("usage","Actividad de aplicaciones","Ordenar por uso real; sin acceso se conservan los recientes de Spotlight.",
            usage,if(usage)"Concedido" else "Opcional",CommandIcons.Apps),
        *(if(Build.VERSION.SDK_INT>=33)arrayOf(
            PermissionRow("post_notifications","Avisos de Spotlight","Notificaciones del propio servicio de Spotlight.",
                granted(Manifest.permission.POST_NOTIFICATIONS),if(granted(Manifest.permission.POST_NOTIFICATIONS))"Concedido" else "Sin conceder",CommandIcons.ChatBubbleOutline)
        )else emptyArray())
    )
}

@Composable fun SettingsAccessSummary(app:SpotlightApp,config:Config,revision:Int,onConfigure:(String)->Unit) {
    val shizuku by app.shizuku.state.collectAsState()
    val listener by Replies.connected.collectAsState()
    val entries=remember(app,config,revision,shizuku,listener){accessRows(app,config)}
    val active=entries.count{it.allowed}
    val colors=MaterialTheme.colorScheme
    Text("Todos los accesos en un solo lugar. Los permisos especiales se conceden o revocan desde Android; ningún interruptor simula autorizarlos.",style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)
    Text("$active de ${entries.size} accesos disponibles",style=MaterialTheme.typography.labelMedium,color=colors.onSurfaceVariant)
    Surface(shape=RoundedCornerShape(16.dp),color=colors.onSurface.copy(alpha=.035f)) {
        Column(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=4.dp)) {
            entries.forEachIndexed{index,row->
                Row(Modifier.fillMaxWidth().heightIn(min=62.dp).padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
                    Surface(shape=RoundedCornerShape(10.dp),color=colors.onSurface.copy(alpha=.055f)){
                        Box(Modifier.size(36.dp),contentAlignment=Alignment.Center){
                            Icon(row.icon,null,Modifier.size(18.dp),tint=colors.onSurfaceVariant)
                        }
                    }
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){
                        Text(row.label,style=MaterialTheme.typography.bodyMedium)
                        Text(row.explanation,style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)
                        Text(row.status,style=MaterialTheme.typography.labelSmall,color=if(row.allowed)colors.primary else colors.onSurfaceVariant)
                    }
                    TextButton(onClick={onConfigure(row.id)}){
                        Text(if(row.allowed)"Gestionar" else "Configurar",style=MaterialTheme.typography.labelSmall)
                    }
                }
                if(index<entries.lastIndex)HorizontalDivider(color=colors.onSurface.copy(alpha=.055f))
            }
        }
    }
}
