package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.next.*
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*
import org.json.JSONObject

data class SportsAgendaEntry(val json:JSONObject,val fetchedAt:Long,val stale:Boolean)
object SportsAgendaOrder {
    fun merge(items:List<SportsAgendaEntry>)=items.distinctBy{it.json.optString("idEvent").ifBlank{it.json.optString("strEvent")+it.json.optString("dateEvent")}}.sortedBy{it.json.optString("dateEvent").ifBlank{"9999"}+it.json.optString("strTime")}
    fun live(item:JSONObject)=normalize(item.optString("strStatus")) in setOf("in progress","live","1h","2h","ht","q1","q2","q3","q4")
}
@Composable fun SportsAgendaPanel(app:SpotlightApp,initial:String,onAction:(Action)->Unit) {
    val request=remember(initial){SportsRequest.parse(initial) ?: if(normalize(initial) in setOf("","deportes","resultados deportivos","agenda deportiva"))SportsRequest() else SportsRequest(entity=initial)}
    val store=remember{SportsFavorites(app)};var favorites by remember{mutableStateOf(store.list())}
    var manage by remember{mutableStateOf(favorites.isEmpty() || request?.entity?.isNotBlank()==true && favorites.none{normalize(it.name).contains(normalize(request.entity))})}
    val repo=remember{NativeProviders(app)};var rows by remember{mutableStateOf<List<SportsAgendaEntry>>(emptyList())}
    var error by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)};var progress by remember{mutableStateOf("")};var revision by remember{mutableIntStateOf(0)};var detail by remember{mutableStateOf<SportsAgendaEntry?>(null)}
    if(request?.unsupported?.isNotBlank()==true){
        Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text("UFC / MMA",style=MaterialTheme.typography.titleLarge)
            Text("La fuente configurada no ofrece una cartelera de MMA verificada. No se muestran peleas ni asaltos inventados. API-Sports ofrece MMA, pero requiere una cuenta y clave propia; todavía no está integrado en esta entrega.")
            TextButton(onClick={onAction(Action.ComposeSearch("sofascore","com.sofascore.results","UFC"))}){Text("Consultar UFC en Sofascore")}
        };return
    }
    LaunchedEffect(manage,revision){if(manage)return@LaunchedEffect;busy=true;error="";rows=emptyList();try{
        val relevant=favorites.filter{request?.entity.isNullOrBlank() || normalize(it.name).contains(normalize(request!!.entity))}
        for((index,entity) in relevant.withIndex()){
            ensureActive();progress="Consultando favorito ${index+1} de ${relevant.size}"
            try{val data=repo.sportsEntity(entity,true,revision>0);rows=SportsAgendaOrder.merge(rows+SportsFavorites.events(entity,data.json).map{SportsAgendaEntry(it,data.fetchedAt,data.stale)})}catch(e:CancellationException){throw e}catch(e:Exception){error="Una fuente no respondió. Se conservan los encuentros disponibles."}
        }
    }finally{busy=false;progress=""}}
    if(manage){Column{SportsFavoritesPanel(app,request.entity.ifBlank{initial},onAction);TextButton(onClick={favorites=store.list();if(favorites.isNotEmpty()){manage=false;revision++;error=""}else error="Elegí al menos un favorito para consultar su agenda."}){Text("Ver agenda de favoritos")};if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)};return}
    val relevant=if(request.today)rows.filter{it.json.optString("dateEvent")==java.time.LocalDate.now().toString()}else rows
    val visible=if(request.live)relevant.filter{SportsAgendaOrder.live(it.json)}else relevant
    Column(Modifier.fillMaxWidth().heightIn(max=420.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text(if(request?.live==true)"Deportes en vivo · favoritos" else "Agenda deportiva",style=MaterialTheme.typography.titleLarge)
        Text("Encuentros específicos de tus favoritos, ordenados por fecha. TheSportsDB gratuito puede omitir partidos y no garantiza marcadores en vivo.",style=MaterialTheme.typography.bodySmall)
        Row{TextButton(onClick={manage=true}){Text("Gestionar favoritos")};TextButton(enabled=!busy,onClick={revision++}){Text("Actualizar")}}
        detail?.let{item->val event=item.json
            TextButton(onClick={detail=null}){Text("Volver a la agenda")}
            Text(event.optString("strEvent").ifBlank{"Encuentro"},style=MaterialTheme.typography.titleLarge)
            Text(ProviderValues.score(event),style=MaterialTheme.typography.headlineMedium)
            listOf("strLeague" to "Competición","strSport" to "Deporte","strStatus" to "Estado","dateEvent" to "Fecha","strTime" to "Hora del proveedor","strVenue" to "Lugar","intRound" to "Ronda").forEach{(key,label)->event.optString(key).takeUnless{it.isBlank()||it=="null"}?.let{Text("$label: $it")}}
            Text("Solo se muestran datos publicados por la fuente. Desglose de goles, cuartos o sets no disponible aquí.",style=MaterialTheme.typography.bodySmall)
            TextButton(onClick={onAction(Action.ComposeSearch("sofascore","com.sofascore.results",event.optString("strEvent")))}){Text("Más detalles en Sofascore")}
        } ?: visible.forEach{item->TextButton(onClick={detail=item},modifier=Modifier.fillMaxWidth()){Column(Modifier.fillMaxWidth()){
            Text(item.json.optString("strEvent").ifBlank{"Encuentro"})
            Text(ProviderValues.score(item.json),style=MaterialTheme.typography.bodySmall)
            Text(item.json.optString("strStatus").takeUnless{it.isBlank() || it=="null"} ?: "Estado no informado",style=MaterialTheme.typography.bodySmall)
            item.json.optString("dateEvent").takeUnless{it.isBlank() || it=="null"}?.let{Text(it,style=MaterialTheme.typography.bodySmall)}
        }}}
        if(visible.isEmpty() && !busy)Text(if(request?.live==true)"No hay encuentros en curso informados para tus favoritos. La cobertura puede ser incompleta." else "No hay encuentros disponibles para esta consulta. Probá otro favorito o revisá la cobertura del proveedor.")
        if(busy){LinearProgressIndicator(Modifier.fillMaxWidth());Text(progress,style=MaterialTheme.typography.labelSmall)}
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
        rows.maxOfOrNull{it.fetchedAt}?.let{Text("TheSportsDB · Consulta ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(it))}",style=MaterialTheme.typography.labelSmall)}
        if(rows.any{it.stale})Text("Algunos datos provienen de la caché sin poder actualizarse.",style=MaterialTheme.typography.bodySmall)
    }
}
