package dev.bautista.spotlight.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp

@Composable fun NativeBrightnessReview(app:SpotlightApp,value:Int,onAdvanced:()->Unit) {
    var message by remember(value){mutableStateOf("")}
    var previous by remember{mutableStateOf<Pair<Int,Int>?>(null)}
    Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text("Brillo al ${value*100/255}%",style=MaterialTheme.typography.titleMedium)
        Text("Android puede ajustar el brillo con el permiso de modificar ajustes. Al aplicar, se usa brillo manual; podés deshacerlo aquí. Shizuku es una alternativa opcional.")
        TextButton(onClick={runCatching{app.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,Uri.parse("package:"+app.packageName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}.onFailure{message="Abrí el permiso de modificar ajustes desde Android."}}){Text("Permitir ajuste de brillo")}
        Button(onClick={
            if(!Settings.System.canWrite(app)){message="Falta el permiso de modificar ajustes. Concedelo y volvé a aplicar.";return@Button}
            val old=Settings.System.getInt(app.contentResolver,Settings.System.SCREEN_BRIGHTNESS,128)
            val mode=Settings.System.getInt(app.contentResolver,Settings.System.SCREEN_BRIGHTNESS_MODE,0)
            runCatching {
                check(Settings.System.putInt(app.contentResolver,Settings.System.SCREEN_BRIGHTNESS_MODE,0))
                if(!Settings.System.putInt(app.contentResolver,Settings.System.SCREEN_BRIGHTNESS,value.coerceIn(20,255))){Settings.System.putInt(app.contentResolver,Settings.System.SCREEN_BRIGHTNESS_MODE,mode);error("No se pudo aplicar el brillo")}
                previous=old to mode;message="Brillo aplicado mediante Android"
            }.onFailure{message="Android no permitió el cambio. Podés usar Shizuku o sus ajustes."}
        }){Text("Confirmar y aplicar")}
        previous?.let{(brightness,mode)->TextButton(onClick={runCatching{check(Settings.System.canWrite(app));check(Settings.System.putInt(app.contentResolver,Settings.System.SCREEN_BRIGHTNESS,brightness));check(Settings.System.putInt(app.contentResolver,Settings.System.SCREEN_BRIGHTNESS_MODE,mode));previous=null;message="Brillo anterior restaurado"}.onFailure{message="No se pudo restaurar. Revisá los permisos."}}){Text("Deshacer")}}
        TextButton(onClick=onAdvanced){Text("Usar Shizuku · avanzado")}
        if(message.isNotBlank())Text(message)
    }
}
