package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bautista.spotlight.control.*
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.*

/** Presentation identity only; AppCapabilities remains the sole availability/preparation owner. */
internal fun appActionCapabilityId(action:Action):String?=when {
    action is Action.ComposeSearch->"search"
    (action as? Action.ComposeMessage)?.channel?.startsWith("share:")==true->"share-text"
    else->null
}

@Composable internal fun AppCapabilityActions(packageName:String,onAction:(Action)->Unit) =
    AppCapabilityActions(packageName,appInfoAlreadyVisible=false,onAction=onAction)

/** Shared contextual actions; re-resolve immediately at selection, never dispatch from cached availability. */
@Composable internal fun AppCapabilityActions(packageName:String,appInfoAlreadyVisible:Boolean,compact:Boolean=false,
    promoteSearch:Boolean=true,onAction:(Action)->Unit) {
    val context=LocalContext.current;val scope=rememberCoroutineScope()
    val appearance=LocalAppearance.current
    val d=GlassTokens.densityScale(appearance)
    val focusUi=LocalFocusUi.current
    val registry=remember(context){AppCapabilities(context)}
    var capabilities by remember(packageName){mutableStateOf(emptyList<AppCapability>())}
    var error by remember(packageName){mutableStateOf("")}
    var busy by remember(packageName){mutableStateOf(false)}
    var loading by remember(packageName){mutableStateOf(true)}
    val catalog=remember(packageName,appInfoAlreadyVisible){ActionCatalog.applicationActions(packageName).filterNot {
        appInfoAlreadyVisible && (it.action as? Action.Launch)?.intent?.action==android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    }.distinctBy{appActionCapabilityId(it.action) ?: it.id}}
    LaunchedEffect(packageName){capabilities=withContext(Dispatchers.IO){registry.available(packageName)};loading=false}
    fun prepare(id:String) {
        if(busy)return
        busy=true;error=""
        scope.launch {
            try{onAction(withContext(Dispatchers.IO){registry.prepare(packageName,id)})}
            catch(e:CancellationException){throw e}
            catch(_:Exception){
                error="La acción ya no está disponible. Revisá la app y sus permisos."
                capabilities=withContext(Dispatchers.IO){runCatching{registry.available(packageName)}.getOrDefault(emptyList())}
            } finally{busy=false}
        }
    }
    val promoted=capabilities.firstOrNull{focusUi && promoteSearch && it.id=="search"}
    val remaining=catalog.filterNot{promoted!=null && appActionCapabilityId(it.action)==promoted.id}
    val represented=catalog.mapNotNull{appActionCapabilityId(it.action)}.toSet()
    val extra=capabilities.distinctBy{it.id}.filterNot{it.id==promoted?.id || it.id in represented}
    Column(Modifier.fillMaxWidth().testTag("app-capabilities")) {
        if(promoted!=null) {
            val label=remember(packageName){runCatching{context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(packageName,0)).toString()}.getOrDefault(packageName)}
            TextButton(enabled=!busy,onClick={prepare(promoted.id)},
                modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("app-promoted-search"),
                contentPadding=PaddingValues(horizontal=2.dp)) {
                IosCommandIcon(CommandIcons.Search,null,Modifier.size(20.dp));Spacer(Modifier.width(10.dp))
                Text("Buscar en $label",Modifier.weight(1f),style=MaterialTheme.typography.titleSmall)
            }
        }
        InspectorSecondaryActions(compact,packageName) {
            HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant,modifier=Modifier.padding(bottom=(12f*d).dp))
            Text("Acciones de app",style=MaterialTheme.typography.labelMedium,fontSize=GlassTokens.scaledSp(12.sp,appearance),color=MaterialTheme.colorScheme.onSurfaceVariant,
                modifier=Modifier.padding(bottom=(4f*d).dp))
            remaining.forEach { target ->
                val capabilityId=appActionCapabilityId(target.action)
                val search=capabilityId=="search"
                val available=capabilityId==null || capabilities.any{it.id==capabilityId}
                TextButton(enabled=!busy && available,onClick={if(capabilityId==null)onAction(target.action) else prepare(capabilityId)},
                    colors=ButtonDefaults.textButtonColors(contentColor=MaterialTheme.colorScheme.onSurface),
                    contentPadding=PaddingValues(horizontal=(2f*d).dp,vertical=(8f*d).dp),
                    modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("app-action-${target.id}")) {
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy((10f*d).dp)) {
                        ResultIcon(target,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy((3f*d).dp)) {
                            Text(target.title,style=MaterialTheme.typography.bodySmall,fontSize=GlassTokens.scaledSp(12.sp,appearance))
                            Text(if(!available)if(loading)"Comprobando disponibilidad…" else if(search)"No disponible: la app no publica una búsqueda accesible." else "No disponible: revisá la app y sus permisos para compartir texto." else target.subtitle,
                                style=MaterialTheme.typography.labelSmall,fontSize=GlassTokens.scaledSp(11.sp,appearance),color=MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            extra.forEach { capability ->
                TextButton(enabled=!busy,onClick={prepare(capability.id)},colors=ButtonDefaults.textButtonColors(contentColor=MaterialTheme.colorScheme.onSurface),
                    contentPadding=PaddingValues(horizontal=(2f*d).dp,vertical=(8f*d).dp),
                    modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("app-capability-${capability.id}")) {
                    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy((3f*d).dp)) {
                        Text(capability.title,style=MaterialTheme.typography.bodySmall,fontSize=GlassTokens.scaledSp(12.sp,appearance))
                        Text(capability.expectedResult,style=MaterialTheme.typography.labelSmall,fontSize=GlassTokens.scaledSp(if(focusUi)12.sp else 11.sp,appearance),color=MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if(remaining.isEmpty() && extra.isEmpty())Text("La app no publica acciones adicionales compatibles.",style=MaterialTheme.typography.bodySmall,fontSize=GlassTokens.scaledSp(12.sp,appearance))
            if(!focusUi && error.isNotBlank())Text(error,style=MaterialTheme.typography.bodySmall,fontSize=GlassTokens.scaledSp(12.sp,appearance),color=MaterialTheme.colorScheme.error)
        }
        if(focusUi && error.isNotBlank())Text(error,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.error)
    }
}
