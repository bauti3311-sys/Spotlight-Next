package dev.bautista.spotlight.ui

import android.content.*
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.next.*
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.*
import org.json.*
import java.time.LocalDate
import java.text.DateFormat
import java.util.Date

private fun stamp(value:Long)=DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(Date(value))
@Composable private fun NextCard(title:String,detail:String,icon:androidx.compose.ui.graphics.vector.ImageVector=CommandIcons.Info,content:@Composable ColumnScope.()->Unit={}) {
    Surface(shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.surfaceContainerLow,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){IosCommandIcon(icon,null,Modifier.size(22.dp),tint=MaterialTheme.colorScheme.primary);Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.titleSmall);if(detail.isNotBlank())Text(detail,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
            content()
        }
    }
}
@Composable fun NextPanel(app:SpotlightApp,page:String,payload:String,onAction:(Action)->Unit) {
    when(page){
        "profiles"->{val profile=remember(payload){NativeCatalog.profile(payload)};if(profile!=null)WorkflowRunner(profile,app.local,onAction=onAction,onDismiss={onAction(Action.SystemPanel("catalog"))}) else CatalogPanel(app,onAction)}
        "catalog"->CatalogPanel(app,onAction)
        "display-review"->DisplayReview(app,payload)
        "tasker"->TaskerPanel(app)
        "weather-native"->WeatherPanel(app,payload,onAction)
        "sports-native"->SportsAgendaPanel(app,payload,onAction)
    }
}
@Composable private fun CatalogPanel(app:SpotlightApp,onAction:(Action)->Unit) {
    var pack by remember{mutableStateOf("Todos")};var filter by remember{mutableStateOf("")}
    val prefs=remember{app.getSharedPreferences("catalog_v1",Context.MODE_PRIVATE)}
    var disabled by remember{mutableStateOf(prefs.getStringSet("disabled",emptySet()).orEmpty().toSet())}
    val productItems=NativeCatalog.items.filter{ProductVisibility.allowedAction(it.action)}
    val packs=listOf("Todos")+productItems.map{it.pack}.distinct()
    Column(Modifier.fillMaxWidth().heightIn(max=360.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("Centro de comandos",style=MaterialTheme.typography.titleLarge)
        Text("Acciones listas para usar. Cada una indica sus requisitos y abre una revisión cuando corresponde.",style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(filter,{filter=it.take(100)},label={Text("Buscar acciones")},singleLine=true,modifier=Modifier.fillMaxWidth())
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){packs.forEach {FilterChip(selected=pack==it,onClick={pack=it},label={Text(it)})}}
        productItems.filter{(pack=="Todos" || it.pack==pack) && (filter.isBlank() || dev.bautista.spotlight.search.normalize(it.title+" "+it.detail).contains(dev.bautista.spotlight.search.normalize(filter)))}.forEach {item->
            NextCard(item.title,item.detail,when(item.pack){"Clima y deportes"->if(item.id=="weather")CommandIcons.Cloud else CommandIcons.Sports;"Comunicación"->CommandIcons.Forum;"Multimedia"->CommandIcons.MusicNote;"Shizuku"->CommandIcons.Shield;"IA"->CommandIcons.AutoAwesome;"Sistema"->CommandIcons.Settings;else->CommandIcons.Layers}){
                Text(item.requirement,style=MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){TextButton(enabled=item.id !in disabled,onClick={onAction(item.action)}){Text("Abrir")};TextButton(onClick={disabled=if(item.id in disabled)disabled-item.id else disabled+item.id;prefs.edit().putStringSet("disabled",disabled).apply()}){Text(if(item.id in disabled)"Activar" else "Desactivar")}}
            }
        }
    }
}
@Composable private fun DisplayReview(app:SpotlightApp,payload:String) {
    var advanced by remember(payload){mutableStateOf(false)}
    val nativeValue=payload.substringAfter(':').toIntOrNull()
    if(payload.startsWith("screen_brightness:") && nativeValue!=null && nativeValue in 20..255 && !advanced){NativeBrightnessReview(app,nativeValue!!){advanced=true};return}
    LaunchedEffect(Unit){app.shizuku.start()}
    val scope=rememberCoroutineScope();val state by app.shizuku.state.collectAsState();var message by remember(payload){mutableStateOf("")};var busy by remember{mutableStateOf(false)}
    val key=payload.substringBefore(':');val value=payload.substringAfter(':',"");val valid=dev.bautista.spotlight.advanced.AdvancedPolicy.validSetting(key,value)
    Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Revisar cambio de pantalla",style=MaterialTheme.typography.titleMedium)
        val label=when(key){"screen_brightness"->"Brillo: ${value.toIntOrNull()?.let{it*100/255} ?: "—"}% (mínimo seguro 8%)";"accelerometer_rotation"->"Rotación automática: ${if(value=="0")"desactivada" else "activada"}";"screen_off_timeout"->"Suspender pantalla: ${value.toLongOrNull()?.div(60000)} minutos";else->"Valor no compatible"}
        NextCard(label,"Se guarda el valor original antes del cambio para que puedas restaurarlo.",CommandIcons.Display)
        Text(state.status.label,style=MaterialTheme.typography.bodySmall)
        Row{Button(enabled=valid && state.available && !busy,onClick={busy=true;scope.launch{try{app.reversibleSettings.apply(key,value,true);message="Cambio aplicado y verificado."}catch(e:CancellationException){throw e}catch(e:Exception){message=e.message.orEmpty()}finally{busy=false}}}){Text("Confirmar cambio")};TextButton(enabled=valid && state.available && !busy,onClick={busy=true;scope.launch{try{app.reversibleSettings.restore(key,true);message="Valor original restaurado y verificado."}catch(e:CancellationException){throw e}catch(e:Exception){message=e.message.orEmpty()}finally{busy=false}}}){Text("Restaurar")}}
        if(!state.available)TextButton(onClick={runCatching{app.startActivity(Intent(app,dev.bautista.spotlight.MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}}){Text("Abrir opciones de conexión")}
        if(busy)LinearProgressIndicator(Modifier.fillMaxWidth());if(message.isNotBlank())Text(message)
    }
}
@Composable private fun TaskerPanel(app:SpotlightApp) {
    val adapter=remember{TaskerAdapter(app)};val scope=rememberCoroutineScope();var snapshot by remember{mutableStateOf<TaskerSnapshot?>(null)};var selected by remember{mutableStateOf<TaskerTask?>(null)}
    var p1 by remember{mutableStateOf("")};var p2 by remember{mutableStateOf("")};var message by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)};var revision by remember{mutableIntStateOf(0)};var filter by remember{mutableStateOf("")}
    LaunchedEffect(revision){snapshot=adapter.discover()}
    Column(Modifier.fillMaxWidth().heightIn(max=360.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text("Tasker",style=MaterialTheme.typography.titleLarge);Text(snapshot?.explanation ?: "Comprobando Tasker…")
        Row{TextButton(onClick={revision++},enabled=!busy){Text("Comprobar")};snapshot?.packageName?.let {pkg->TextButton(onClick={runCatching{app.startActivity(TaskerContract.preferences(pkg).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}.onFailure{message="Tasker no permite abrir esta pantalla. Abrí sus preferencias manualmente."}}){Text("Abrir acceso externo")}}}
        if(snapshot?.available==true){
            OutlinedTextField(filter,{filter=it.take(100)},label={Text("Buscar tarea")},singleLine=true,modifier=Modifier.fillMaxWidth())
            if(snapshot!!.tasks.isEmpty())Text("No hay tareas con nombre. Importá el proyecto adjunto o creá una en Tasker.")
            snapshot!!.tasks.filter{it.name.contains(filter,true)}.forEach {task->NextCard(task.name,task.project,CommandIcons.Layers){TextButton(enabled=!busy,onClick={selected=task;p1="";p2="";message=""}){Text("Preparar tarea")}}}
        }
        selected?.let {task->NextCard("Revisar: ${task.name}","La tarea puede modificar el dispositivo según sus pasos configurados en Tasker.",CommandIcons.Shield){
            OutlinedTextField(p1,{p1=it.take(8000)},label={Text("Parámetro 1 · opcional")},modifier=Modifier.fillMaxWidth());OutlinedTextField(p2,{p2=it.take(8000)},label={Text("Parámetro 2 · opcional")},modifier=Modifier.fillMaxWidth())
            Button(enabled=!busy,onClick={busy=true;message="Esperando confirmación de Tasker…";scope.launch{try{message=adapter.run(task.name,if(p2.isNotBlank())listOf(p1,p2) else if(p1.isNotBlank())listOf(p1) else emptyList())}catch(e:CancellationException){throw e}catch(e:Exception){message=e.message.orEmpty()}finally{busy=false}}}){Text("Confirmar ejecución")}
        }}
        if(busy)LinearProgressIndicator(Modifier.fillMaxWidth());if(message.isNotBlank())Text(message)
    }
}
@Composable private fun WeatherPanel(app:SpotlightApp,initial:String,onAction:(Action)->Unit) {
    val repo=remember{NativeProviders(app)};val prefs=remember{app.getSharedPreferences("weather_native",Context.MODE_PRIVATE)}
    val scope=rememberCoroutineScope()
    var primary by remember{mutableStateOf(prefs.getString("primary_city",prefs.getString("city","Mar del Plata")).orEmpty())}
    var cities by remember{mutableStateOf(prefs.getStringSet("cities",emptySet()).orEmpty().toSet())}
    var coordinates by remember{mutableStateOf<Pair<Double,Double>?>(null)}
    var locationMessage by remember{mutableStateOf("")}
    var city by remember(initial){mutableStateOf(initial.removePrefix("tomorrow:").ifBlank{primary})};var requested by remember(initial){mutableStateOf(city)}
    var revision by remember{mutableIntStateOf(0)};var data by remember{mutableStateOf<NativeSnapshot?>(null)};var error by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)}
    LaunchedEffect(requested,revision,coordinates){busy=true;error="";try{data=coordinates?.let{repo.weatherAt(it.first,it.second,refresh=revision>0)} ?: repo.weather(requested,revision>0)}catch(e:CancellationException){throw e}catch(e:Exception){error=e.message.orEmpty();data=null}finally{busy=false}}
    Column(Modifier.fillMaxWidth().heightIn(max=360.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(if(initial.startsWith("tomorrow:"))"Clima mañana" else "Clima",style=MaterialTheme.typography.titleLarge)
        OutlinedTextField(city,{city=it.take(100)},label={Text("Localidad")},singleLine=true,modifier=Modifier.fillMaxWidth())
        Row{TextButton(enabled=!busy,onClick={coordinates=null;requested=city.ifBlank{primary};revision++}){Text("Consultar")};TextButton(enabled=!busy,onClick={coordinates=null;city=primary;requested=city;revision++}){Text("Ciudad principal")}}
        Row(Modifier.horizontalScroll(rememberScrollState())){TextButton(onClick={primary=city.ifBlank{"Mar del Plata"};prefs.edit().putString("primary_city",primary).apply()}){Text("Usar como ciudad principal")};TextButton(onClick={if(city.isNotBlank()){cities=(cities+city).take(8).toSet();prefs.edit().putStringSet("cities",cities).apply()}}){Text("Guardar ciudad")}}
        if(cities.isNotEmpty())Row(Modifier.horizontalScroll(rememberScrollState())){cities.forEach{saved->InputChip(saved==requested,{coordinates=null;city=saved;requested=saved;revision++},label={Text(saved)},trailingIcon={IconButton(onClick={cities=cities-saved;prefs.edit().putStringSet("cities",cities).apply()},modifier=Modifier.size(24.dp)){IosCommandIcon(CommandIcons.Close,"Quitar ciudad",Modifier.size(14.dp))}})}}
        TextButton(onClick={
            if(androidx.core.content.ContextCompat.checkSelfPermission(app,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=android.content.pm.PackageManager.PERMISSION_GRANTED){onAction(Action.Access("location","clima"));return@TextButton}
            scope.launch {val latest=withContext(Dispatchers.IO){val lm=app.getSystemService(android.location.LocationManager::class.java);lm.getProviders(true).mapNotNull{provider->runCatching{lm.getLastKnownLocation(provider)}.getOrNull()}.filter{System.currentTimeMillis()-it.time in 0..3600000}.maxByOrNull{it.time}}
                if(latest!=null){coordinates=(kotlin.math.round(latest.latitude*100)/100) to (kotlin.math.round(latest.longitude*100)/100);revision++;locationMessage="Ubicación reciente aproximada · sin iniciar GPS"}
                else{coordinates=null;requested=primary;city=primary;revision++;locationMessage="No hay ubicación reciente. Se usa tu ciudad principal."}
            }
        }){Text("Usar ubicación aproximada reciente")}
        if(locationMessage.isNotBlank())Text(locationMessage,style=MaterialTheme.typography.bodySmall)
        Text("Principal: $primary · consultas bajo demanda, sin GPS permanente ni alertas periódicas activadas.",style=MaterialTheme.typography.bodySmall)
        if(busy)LinearProgressIndicator(Modifier.fillMaxWidth());if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
        data?.let {snapshot->val j=snapshot.json;val current=j.optJSONObject("current") ?: JSONObject()
            NextCard(j.optString("location_name"),ProviderValues.condition(current.optInt("weather_code",-1)),CommandIcons.Cloud){
                Text(ProviderValues.number(current,"temperature_2m"," °C"),style=MaterialTheme.typography.headlineLarge)
                Text("Sensación ${ProviderValues.number(current,"apparent_temperature"," °C")} · Humedad ${ProviderValues.number(current,"relative_humidity_2m","%")}")
                Text("Viento ${ProviderValues.number(current,"wind_speed_10m"," km/h")} · Ráfagas ${ProviderValues.number(current,"wind_gusts_10m"," km/h")}")
                Text("Precipitación ${ProviderValues.number(current,"precipitation"," mm")}")
            }
            val hourly=j.optJSONObject("hourly");val times=hourly?.optJSONArray("time");val temperatures=hourly?.optJSONArray("temperature_2m");val rain=hourly?.optJSONArray("precipitation_probability")
            if(times!=null){Text("Próximas horas",style=MaterialTheme.typography.titleSmall);val currentTime=if(initial.startsWith("tomorrow:"))runCatching{java.time.LocalDate.parse(current.optString("time").take(10)).plusDays(1).toString()+"T00:00"}.getOrDefault("") else current.optString("time");val start=(0 until times.length()).firstOrNull{times.optString(it)>=currentTime.take(13)+":00"} ?: 0
                val indices=(start until minOf(start+24,times.length())).toList()
                WeatherChart("Temperatura por hora",indices.map{temperatures?.optDouble(it)?.takeIf{v->v.isFinite()}?.toFloat()}," °C")
                WeatherChart("Probabilidad de lluvia",indices.map{rain?.optDouble(it)?.takeIf{v->v.isFinite()}?.toFloat()}," %",100f)
                Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){for(i in start until minOf(start+12,times.length())){Surface(shape=RoundedCornerShape(10.dp),color=MaterialTheme.colorScheme.surfaceContainerLow){Column(Modifier.padding(12.dp)){Text(times.optString(i).substringAfter('T'));Text(if(temperatures?.isNull(i)==false)"${temperatures.optString(i)} °C" else "—");Text(if(rain?.isNull(i)==false)"${rain.optString(i)}% lluvia" else "—")}}}}
            }
            j.optJSONObject("daily")?.let {d->val days=d.optJSONArray("time");for(i in 0 until (days?.length() ?: 0)){NextCard(days!!.optString(i),"${d.optJSONArray("temperature_2m_min")?.optString(i)}° / ${d.optJSONArray("temperature_2m_max")?.optString(i)}° · Lluvia ${d.optJSONArray("precipitation_probability_max")?.optString(i)}%",CommandIcons.Cloud){Text("UV ${d.optJSONArray("uv_index_max")?.optString(i)} · Amanecer ${d.optJSONArray("sunrise")?.optString(i)?.substringAfter('T')} · Atardecer ${d.optJSONArray("sunset")?.optString(i)?.substringAfter('T')}",style=MaterialTheme.typography.bodySmall)}}}
            if(snapshot.stale)Text(snapshot.warning,color=MaterialTheme.colorScheme.error)
            SourceFreshnessLabel(SourceFreshness("Open-Meteo",snapshot.fetchedAt,snapshot.fromCache,snapshot.stale))
        }
    }
}
