package dev.bautista.spotlight.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import dev.bautista.spotlight.data.Config

/** Only controls visible in Appearance may change; hidden settings/content stay intact. */
object AppearanceEdits {
    fun merge(current:Config,draft:Config)=current.copy(theme=draft.theme,preset=draft.preset,
        opacity=GlassTokens.opacity(draft),actionOpacity=GlassTokens.actionOpacity(draft),settingsOpacity=GlassTokens.settingsOpacity(draft),windowScale=draft.windowScale,accent=draft.accent,density=draft.density,animations=draft.animations,compactSearchResults=draft.compactSearchResults)
}

@Composable fun ConfirmedSettingsButton(label:String,explanation:String,enabled:Boolean=true,onConfirm:()->Unit) {
    var show by remember{mutableStateOf(false)}
    TextButton(enabled=enabled,onClick={show=true}){Text(label)}
    if(show)AlertDialog(onDismissRequest={show=false},title={Text(label)},text={Text(explanation)},
        confirmButton={TextButton(onClick={show=false;onConfirm()}){Text("Confirmar")}},
        dismissButton={TextButton(onClick={show=false}){Text("Cancelar")}})
}
