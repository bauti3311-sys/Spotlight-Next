package dev.bautista.spotlight.ui

import android.content.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.*
import org.json.JSONArray

@Composable fun GlassAppearance(c:Config,update:(Config)->Unit) {
    var draft by remember(c){mutableStateOf(c)};var name by remember{mutableStateOf("")};var imported by remember{mutableStateOf("")};var error by remember{mutableStateOf("")}
    val context=LocalContext.current
    Text("Spotlight Glass",style=MaterialTheme.typography.titleLarge)
    Text("Probá el tema en la vista previa y aplicalo cuando quieras. Tus datos e integraciones se conservan.",style=MaterialTheme.typography.bodySmall)
    Row(Modifier.horizontalScroll(rememberScrollState())){AppearancePresets.names.forEach{family->FilterChip(draft.preset==AppearancePresets.apply(family,draft).preset,{draft=AppearancePresets.apply(family,draft)},label={Text(family)})}}
    Row{listOf("auto" to "Automático","light" to "Claro","dark" to "Oscuro").forEach{(key,label)->FilterChip(draft.theme==key,{draft=draft.copy(theme=key)},label={Text(label)})}}
    OverlayAppearancePreview(draft)
    Row(Modifier.horizontalScroll(rememberScrollState())){listOf("blue" to "Océano","purple" to "Lavanda","rose" to "Rosa","green" to "Bosque","orange" to "Ámbar","graphite" to "Grafito").forEach{(key,label)->FilterChip(draft.accent==key,{draft=draft.copy(accent=key)},label={Text(label)})}}
    Row{listOf("neutral","warm","cool").forEach{tone->FilterChip(draft.surfaceTone==tone,{draft=draft.copy(surfaceTone=tone)},label={Text(when(tone){"warm"->"Cálido";"cool"->"Frío";else->"Neutro"})})}}
    Text("Opacidad · ${(draft.opacity*100).toInt()} %");Slider(draft.opacity,{draft=draft.copy(opacity=it)},valueRange=.9f..1f)
    Text("Intensidad del vidrio");Slider(draft.glassIntensity,{draft=draft.copy(glassIntensity=it)},valueRange=0f..1f)
    Text("Borde · ${draft.borderWidth} dp");Slider(draft.borderWidth,{draft=draft.copy(borderWidth=it)},valueRange=0f..2f)
    Text("Sombra");Slider(draft.shadowSize,{draft=draft.copy(shadowSize=it)},valueRange=0f..20f)
    Text("Esquinas · ${draft.corner} dp");Slider(draft.corner.toFloat(),{draft=draft.copy(corner=it.toInt())},valueRange=12f..32f)
    Row(Modifier.horizontalScroll(rememberScrollState())){listOf("inter" to "Inter","system" to "Sistema","mono" to "Monoespaciada").forEach{(key,label)->FilterChip(draft.typeface==key,{draft=draft.copy(typeface=key)},label={Text(label)})}}
    Text("Texto · ${(draft.textScale*100).toInt()} %");Slider(draft.textScale,{draft=draft.copy(textScale=it)},valueRange=.9f..1.3f)
    Text("Iconos · ${draft.iconSize} dp");Slider(draft.iconSize.toFloat(),{draft=draft.copy(iconSize=it.toInt())},valueRange=24f..36f)
    OverlayDensitySettings(draft){draft=it}
    OverlayWidthSettings(draft){draft=it}
    Text("Altura máxima · ${draft.maxHeight} dp");Slider(draft.maxHeight.toFloat(),{draft=draft.copy(maxHeight=it.toInt())},valueRange=320f..700f)
    Row(Modifier.horizontalScroll(rememberScrollState())){listOf("precise" to "Preciso","liquid_precision" to "Liquid Precision","liquid_immersive" to "Liquid Immersive").forEach{(key,label)->FilterChip(draft.movement==key,{draft=draft.copy(movement=key)},label={Text(label)})}}
    Row{Text("Reducir animaciones",Modifier.weight(1f));Switch(!draft.animations,{draft=draft.copy(animations=!it)})}
    Row{FilterChip(draft.selectionStyle=="glass",{draft=draft.copy(selectionStyle="glass")},label={Text("Selección Glass")});FilterChip(draft.selectionStyle=="solid",{draft=draft.copy(selectionStyle="solid")},label={Text("Selección sólida")})}
    Text("El overlay usa un material nativo de alto contraste. El desenfoque de ventanas se reemplaza por translucidez y bordes suaves cuando no puede aislarse de forma segura en One UI.",style=MaterialTheme.typography.bodySmall)
    Row{Button(onClick={update(draft)}){Text("Aplicar tema")};TextButton(onClick={draft=c}){Text("Restaurar vista previa")}}
    OutlinedTextField(name,{name=it.take(50)},label={Text("Nombre del tema")},modifier=Modifier.fillMaxWidth())
    TextButton(enabled=name.isNotBlank(),onClick={runCatching{val saved=ThemeTransfer.save(draft,name);draft=saved;update(saved);name=""}.onFailure{error=it.message.orEmpty()}}){Text("Duplicar y guardar tema")}
    val themes=runCatching{JSONArray(c.customThemes)}.getOrDefault(JSONArray())
    for(i in 0 until themes.length()){val item=themes.optJSONObject(i) ?: continue
        Row{TextButton(onClick={runCatching{draft=ThemeTransfer.decode(item.getJSONObject("theme").toString(),c)}.onFailure{error=it.message.orEmpty()}},modifier=Modifier.weight(1f)){Text(item.optString("name","Tema"))};TextButton(onClick={val kept=JSONArray();for(j in 0 until themes.length())if(j!=i)kept.put(themes.get(j));update(c.copy(customThemes=kept.toString()))}){Text("Eliminar")}}
    }
    TextButton(onClick={context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Tema Spotlight",ThemeTransfer.encode(draft)))}){Text("Exportar tema al portapapeles")}
    OutlinedTextField(imported,{imported=it.take(16000)},label={Text("Importar tema JSON")},modifier=Modifier.fillMaxWidth())
    TextButton(enabled=imported.isNotBlank(),onClick={runCatching{draft=ThemeTransfer.decode(imported,c);imported=""}.onFailure{error=it.message ?: "Tema inválido."}}){Text("Revisar tema importado")}
    if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
}
