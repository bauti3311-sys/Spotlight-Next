package dev.bautista.spotlight.ui

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.Replies

@Composable fun SettingsAccessSummary(app:SpotlightApp,revision:Int) {
    val shizuku by app.shizuku.state.collectAsState()
    val notificationConnection by Replies.connected.collectAsState()
    val accesses=remember(revision) {
        listOf("Ventana flotante" to Settings.canDrawOverlays(app),
            "Contactos" to (ContextCompat.checkSelfPermission(app,Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED),
            "Calendario" to (ContextCompat.checkSelfPermission(app,Manifest.permission.READ_CALENDAR)==PackageManager.PERMISSION_GRANTED),
            "Almacenamiento compartido" to app.index.broadAllowed())
    }
    Text("Cada acceso habilita una capacidad concreta. La búsqueda básica funciona sin permisos adicionales.",style=MaterialTheme.typography.bodySmall)
    accesses.forEach{(name,allowed)->Row(Modifier.fillMaxWidth().padding(vertical=4.dp)){
        Text(name,Modifier.weight(1f));Text(if(allowed)"Permitido" else "Opcional · pendiente",color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodySmall)
    }}
    Text("Notificaciones · ${if(notificationConnection)"servicio conectado" else "servicio no conectado"}",style=MaterialTheme.typography.bodySmall)
    Text("Shizuku · ${shizuku.status.label}",style=MaterialTheme.typography.bodySmall)
    TextButton(onClick={app.startActivity(Intent(app,dev.bautista.spotlight.PermissionActivity::class.java).putExtra("access","calendar").putExtra("reopen",false).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}){Text("Configurar calendario")}
    TextButton(onClick={app.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:${app.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}){Text("Abrir permisos de Android")}
}
