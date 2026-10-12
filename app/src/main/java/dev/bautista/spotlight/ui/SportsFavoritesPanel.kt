package dev.bautista.spotlight.ui

import android.content.*
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.next.*
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*
import java.text.DateFormat
import java.util.Date

@Composable fun SportsFavoritesPanel(app:SpotlightApp,initial:String,onAction:(Action)->Unit) {
    val scope=rememberCoroutineScope();val store=remember{SportsFavorites(app)};val repo=remember{NativeProviders(app)};val vault=remember{dev.bautista.spotlight.ai.CredentialVault(app)}
    var favorites by remember{mutableStateOf(store.list())};var selected by remember{mutableStateOf(favorites.firstOrNull {normalize(it.name).contains(normalize(initial))} ?: favorites.firstOrNull())}
    var onboarding by remember{mutableStateOf(favorites.isEmpty() || initial.isNotBlank() && favorites.none{normalize(it.name).contains(normalize(initial))})}
    var sport by remember{mutableStateOf("Soccer")};var kind by remember{mutableStateOf("team")};var country by remember{mutableStateOf("Argentina")};var name by remember(initial){mutableStateOf(initial)}
    var choices by remember{mutableStateOf<List<SportsEntity>>(emptyList())};var snapshot by remember{mutableStateOf<NativeSnapshot?>(null)};var busy by remember{mutableStateOf(false)};var error by remember{mutableStateOf("")};var next by remember{mutableStateOf(true)};var revision by remember{mutableIntStateOf(0)}
    var showProvider by remember{mutableStateOf(false)};var key by remember{mutableStateOf("")};var providerMessage by remember{mutableStateOf("")}
    LaunchedEffect(selected,next,revision){val entity=selected ?: return@LaunchedEffect;if(onboarding)return@LaunchedEffect;busy=true;error="";snapshot=null;try{snapshot=repo.sportsEntity(entity,next,revision>0)}catch(e:CancellationException){throw e}catch(e:Exception){error="No se pudo consultar el proveedor. Comprobá la conexión y reintentá."}finally{busy=false}}
    Column(Modifier.fillMaxWidth().heightIn(max=360.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text("Tus deportes",style=MaterialTheme.typography.titleLarge)
        Text("TheSportsDB · cobertura limitada según el plan. El acceso gratuito devuelve listas cortas y un encuentro por equipo; puede omitir partidos fuera de casa. No garantiza marcadores en vivo ni cronologías.",style=MaterialTheme.typography.bodySmall)
        if(favorites.isEmpty())Text("Elegí tu primer equipo o competición. Se consultarán sus encuentros, sin mostrar partidos arbitrarios.")
        if(!onboarding && selected!=null){
            Row(Modifier.horizontalScroll(rememberScrollState())){favorites.forEach{e->FilterChip(e==selected,{selected=e},label={Text(e.name)})}}
            Row{TextButton(enabled=!busy,onClick={next=true;revision++}){Text("Próximos")};TextButton(enabled=!busy,onClick={next=false;revision++}){Text("Recientes")};TextButton(enabled=!busy,onClick={revision++}){Text("Actualizar")}}
            snapshot?.let{data->val events=SportsFavorites.events(selected!!,data.json)
                if(events.isEmpty())Text("Sin encuentros disponibles para este favorito. La ausencia de datos no significa que no haya partidos.")
                events.forEach{event->HorizontalDivider();Text(event.optString("strEvent").takeUnless{it.isBlank() || it=="null"} ?: "Encuentro",style=MaterialTheme.typography.titleMedium);Text(ProviderValues.score(event));Text(event.optString("strStatus").takeUnless{it.isBlank() || it=="null"} ?: "Estado no informado");Text(event.optString("dateEvent").takeUnless{it.isBlank() || it=="null"} ?: "Fecha no informada");val time=event.optString("strTimestamp").takeUnless{it.isBlank() || it=="null"} ?: event.optString("strTime").takeUnless{it.isBlank() || it=="null"};if(time!=null)Text("Horario del proveedor: $time · zona no confirmada",style=MaterialTheme.typography.bodySmall)}
                if(data.stale)Text(data.warning,color=MaterialTheme.colorScheme.error);Text("TheSportsDB · Actualizado ${DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(Date(data.fetchedAt))}",style=MaterialTheme.typography.labelSmall)
            }
            Row{TextButton(onClick={onboarding=true}){Text("Agregar favorito")};TextButton(onClick={favorites=favorites.filterNot{it==selected};store.save(favorites);selected=favorites.firstOrNull();onboarding=favorites.isEmpty()}){Text("Quitar favorito")}}
        }
        if(onboarding){
            Row(Modifier.horizontalScroll(rememberScrollState())){listOf("Soccer" to "Fútbol","Basketball" to "Básquet","Motorsport" to "Motor","Tennis" to "Tenis","Baseball" to "Béisbol").forEach{(id,label)->FilterChip(sport==id,{sport=id;choices=emptyList()},label={Text(label)})}}
            Row{FilterChip(kind=="team",{kind="team";choices=emptyList()},label={Text("Equipos")});FilterChip(kind=="league",{kind="league";choices=emptyList()},label={Text("Competiciones")})}
            OutlinedTextField(country,{country=it.take(60)},label={Text("País del proveedor · ejemplo Argentina")},singleLine=true,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(name,{name=it.take(100)},label={Text("Nombre")},singleLine=true,modifier=Modifier.fillMaxWidth())
            TextButton(enabled=!busy && country.trim().length>=2,onClick={busy=true;error="";scope.launch{try{choices=repo.sportsEntities(sport,country,kind,name);if(choices.isEmpty())error="Sin coincidencias en la lista disponible. Probá otra competición o un proveedor con mayor cobertura."}catch(e:CancellationException){throw e}catch(e:Exception){error="No se pudo obtener la lista del proveedor."}finally{busy=false}}}){Text("Buscar en el proveedor")}
            choices.forEach{entity->TextButton(enabled=favorites.size<8 || entity in favorites,onClick={favorites=(favorites+entity).distinctBy{it.kind+it.id}.take(8);store.save(favorites);selected=entity;onboarding=false;revision++;choices=emptyList()}){Text("${entity.name} · guardar favorito")}}
            if(favorites.isNotEmpty())TextButton(onClick={onboarding=false;revision++}){Text("Volver a favoritos")}
        }
        if(busy)LinearProgressIndicator(Modifier.fillMaxWidth());if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
        TextButton(onClick={onAction(Action.ComposeSearch("sofascore","com.sofascore.results",selected?.name ?: name))}){Text("Buscar cobertura en Sofascore")}
        TextButton(onClick={showProvider=!showProvider}){Text("Proveedor · opciones avanzadas")}
        if(showProvider){Text("La clave propia de TheSportsDB amplía la cobertura según tu suscripción. Se guarda cifrada en el dispositivo.");OutlinedTextField(key,{key=it.take(4096)},label={Text("Clave propia opcional")},visualTransformation=PasswordVisualTransformation(),singleLine=true,modifier=Modifier.fillMaxWidth());Row{TextButton(enabled=key.matches(Regex("[A-Za-z0-9]{1,4096}")),onClick={val value=key;key="";scope.launch{providerMessage=withContext(Dispatchers.IO){runCatching{vault.save("thesportsdb",value);"Clave guardada."}.getOrDefault("No se pudo guardar la clave.")};revision++}}){Text("Guardar")};TextButton(onClick={scope.launch{withContext(Dispatchers.IO){vault.delete("thesportsdb")};providerMessage="Acceso gratuito activado.";revision++}}){Text("Usar gratuito")}};if(providerMessage.isNotBlank())Text(providerMessage)}
    }
}
