package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import dev.bautista.spotlight.search.Result

/** Test-source-only visual experiments. No production switch, state owner or dispatcher replaced. */
internal enum class StructuralOption { Unified, Focus }
@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun StructuralVivaldiPrototype(option:StructuralOption,results:List<Result>,version:String,
    onExecute:(Result)->Unit,onInfo:()->Unit,onFavorite:()->Unit,onCapability:(dev.bautista.spotlight.search.Action)->Unit) {
    val c=MaterialTheme.colorScheme
    var context by remember(option){mutableStateOf(true)}
    var details by remember(option){mutableStateOf(false)}
    var selected by remember(option){mutableIntStateOf(0)}
    val focus=remember{FocusRequester()}
    val app=results.first()
    val line=c.outlineVariant.copy(alpha=.55f)
    fun back(){details=false;context=false;focus.requestFocus()}
    BoxWithConstraints(Modifier.fillMaxWidth().testTag("prototype-footprint").padding(horizontal=16.dp,vertical=12.dp)) {
        val phone=maxWidth<600.dp
        Column(Modifier.fillMaxWidth().testTag("prototype-window")
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(c.surface,c.surfaceContainerLow)))
            .onPreviewKeyEvent { e ->
                if(e.type!=KeyEventType.KeyDown)false else when(e.key) {
                    Key.Escape,Key.Back->{if(details)details=false else back();true}
                    Key.Tab->{details=!details;true}
                    Key.DirectionDown->{selected=(selected+1).coerceAtMost(results.lastIndex);true}
                    Key.DirectionUp->{selected=(selected-1).coerceAtLeast(0);true}
                    Key.Enter->{onExecute(results[selected]);true}
                    else->false
                }
            }) {
            Row(Modifier.fillMaxWidth().testTag("prototype-search").heightIn(min=64.dp).padding(horizontal=20.dp),verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                IosCommandIcon(CommandIcons.Search,null,Modifier.size(18.dp),tint=c.onSurfaceVariant)
                // Frozen query is explicit: visual experiment, not a new search implementation.
                BasicTextField("Vivaldi",{},readOnly=true,singleLine=true,
                    textStyle=MaterialTheme.typography.titleLarge.copy(color=c.onSurface,fontFamily=Geist),
                    modifier=Modifier.weight(1f).focusRequester(focus).testTag("prototype-query"))
                if(option==StructuralOption.Focus && context)TextButton(onClick={back()},modifier=Modifier.testTag("prototype-back")) {
                    Text("Resultados",color=c.onSurfaceVariant,style=MaterialTheme.typography.labelMedium)
                }
            }
            HorizontalDivider(color=line)
            val resultItem:@Composable (Int,Boolean)->Unit = { index,slim ->
                val result=results[index]
                Row(Modifier.fillMaxWidth().heightIn(min=if(slim)48.dp else 56.dp)
                    .clickable(role=Role.Button){selected=index;details=false;if(index==0)context=true else onExecute(result)}
                    .testTag("prototype-result-$index")
                    .background(if(selected==index)c.onSurface.copy(alpha=.045f) else Color.Transparent)
                    .padding(horizontal=16.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically,
                    horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    ResultIcon(result,Modifier.size(if(index==0)28.dp else 18.dp))
                    Column(Modifier.weight(1f)) {
                        Text(result.title,style=MaterialTheme.typography.bodyMedium,fontWeight=if(index==selected)FontWeight.SemiBold else FontWeight.Normal)
                        if(!slim)Text(if(index==0)"Aplicación instalada" else result.subtitle,
                            style=MaterialTheme.typography.bodySmall,color=c.onSurfaceVariant,maxLines=1,
                            overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                    if(index==selected)Box(Modifier.size(4.dp).background(c.onSurfaceVariant,RoundedCornerShape(2.dp)))
                }
            }
            val identity:@Composable (Boolean)->Unit = { large ->
                Column(Modifier.testTag("prototype-context").padding(horizontal=24.dp,vertical=18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    Text(if(option==StructuralOption.Unified)"NAVEGADOR / APLICACIÓN" else "APLICACIÓN · CONTEXTO",
                        style=MaterialTheme.typography.labelSmall,letterSpacing=1.5.sp,color=PremiumMaterial.accent(c,Color(0xFFDE6D72)).primary)
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                        ResultIcon(app,Modifier.size(if(large)44.dp else 32.dp))
                        Column(Modifier.weight(1f)) {
                            Text(app.title,Modifier.semantics{heading()},style=if(large)MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
                                fontWeight=FontWeight.SemiBold)
                            Text("Aplicación instalada",style=MaterialTheme.typography.bodySmall,color=c.onSurfaceVariant)
                        }
                    }
                    Row(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically) {
                        TextButton(onClick={onExecute(app)},modifier=Modifier.heightIn(min=48.dp).testTag("prototype-open"),
                            colors=ButtonDefaults.textButtonColors(contentColor=c.onSurface)) {
                            Text("Abrir aplicación  ↗",fontWeight=FontWeight.SemiBold)
                        }
                        TextButton(onClick={details=!details},modifier=Modifier.heightIn(min=48.dp).testTag("prototype-details")) {
                            Text(if(details)"Menos" else "Detalles",color=c.onSurfaceVariant)
                        }
                    }
                }
            }
            if(option==StructuralOption.Unified) {
                // A: results run across the console; context follows as a horizontal editorial band.
                if(phone)Column {results.indices.forEach{resultItem(it,true)}}
                else Row(Modifier.fillMaxWidth()) {results.indices.forEach { i ->Box(Modifier.weight(1f)){resultItem(i,false)}}}
                if(context) {
                    HorizontalDivider(color=line)
                    identity(false)
                }
            } else if(!context) {
                results.indices.forEach{resultItem(it,false)}
            } else if(phone) {
                // B: contextual replacement rather than a second list plus an inspector.
                identity(true)
            } else Row(Modifier.fillMaxWidth()) {
                Column(Modifier.width(270.dp).padding(vertical=8.dp)) {
                    Text("RESULTADOS",Modifier.padding(start=16.dp,bottom=6.dp),style=MaterialTheme.typography.labelSmall,color=c.onSurfaceVariant)
                    results.indices.forEach{resultItem(it,false)}
                }
                Column(Modifier.weight(1f)) {identity(true)}
            }
            if(details && context)Column(Modifier.fillMaxWidth().heightIn(max=240.dp).verticalScroll(rememberScrollState()).padding(horizontal=24.dp)) {
                HorizontalDivider(color=line)
                Text("Versión  $version",Modifier.padding(top=12.dp),style=MaterialTheme.typography.bodySmall)
                Text("com.vivaldi.browser",style=MaterialTheme.typography.bodySmall,color=c.onSurfaceVariant)
                FlowRow {
                    TextButton(onClick=onInfo){Text("Información de la app")}
                    TextButton(onClick=onFavorite){Text("Añadir favorita")}
                }
                AppCapabilityActions("com.vivaldi.browser",appInfoAlreadyVisible=true,compact=false,onAction=onCapability)
            }
            HorizontalDivider(color=line)
            Row(Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("prototype-footer").padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically) {
                Text(if(option==StructuralOption.Unified)"Spotlight" else if(context)"Vivaldi" else "2 resultados",
                    Modifier.weight(1f),style=MaterialTheme.typography.labelSmall,color=c.onSurfaceVariant)
                if(option==StructuralOption.Unified && context)TextButton(onClick={back()},modifier=Modifier.testTag("prototype-back")) {
                    Text("Resultados",style=MaterialTheme.typography.labelMedium,color=c.onSurfaceVariant)
                }
                TextButton(onClick={context=true;details=!details},modifier=Modifier.testTag("prototype-actions")) {
                    Text("Acciones",color=c.onSurfaceVariant,style=MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
    LaunchedEffect(option){focus.requestFocus()}
}
