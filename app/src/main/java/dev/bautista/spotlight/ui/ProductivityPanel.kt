package dev.bautista.spotlight.ui
import android.content.Intent
import android.provider.AlarmClock
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.productivity.FocusSession
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.*
@Composable fun ProductivityPanel(onAction:(Action)->Unit){
 val context=LocalContext.current;val session=remember{FocusSession(context)};var state by remember{mutableStateOf(session.snapshot())};var minutes by remember{mutableStateOf("25")};var error by remember{mutableStateOf("")}
 LaunchedEffect(Unit){while(isActive){state=session.snapshot();delay(1000)}}
 Column(Modifier.fillMaxWidth().heightIn(max=340.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
  Text("Concentración",style=MaterialTheme.typography.titleMedium)
  Text(if(state.phase=="break")"Pausa" else "Sesión de trabajo",style=MaterialTheme.typography.labelLarge)
  Text("%02d:%02d".format(state.remaining/60000,(state.remaining/1000)%60),style=MaterialTheme.typography.headlineLarge)
  Text(when(state.status){"running"->"En curso";"paused"->"En pausa";"finished"->"Sesión terminada";else->"Elegí una duración"})
  OutlinedTextField(minutes,{minutes=it.filter(Char::isDigit).take(3)},label={Text("Minutos · 1 a 180")},singleLine=true,modifier=Modifier.fillMaxWidth())
  Row(Modifier.horizontalScroll(rememberScrollState())){Button(onClick={val value=minutes.toIntOrNull();if(value !in 1..180)error="Elegí entre 1 y 180 minutos." else {session.start(value!!);state=session.snapshot();error=""}}){Text("Comenzar")};TextButton(onClick={session.start(5,"break");state=session.snapshot()}){Text("Pausa de 5 min")};TextButton(onClick={if(state.status=="paused")session.resume() else session.pause();state=session.snapshot()},enabled=state.status in setOf("running","paused")){Text(if(state.status=="paused")"Reanudar" else "Pausar")};TextButton(onClick={session.stop();state=session.snapshot()}){Text("Finalizar")}}
  if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
  Text("La cuenta se conserva al cerrar la barra. Para una alarma fuera de Spotlight, prepará el temporizador en Reloj. Tras reiniciar el dispositivo, la sesión se descarta.",style=MaterialTheme.typography.bodySmall)
  TextButton(onClick={val value=minutes.toIntOrNull();if(value !in 1..180){error="Duración inválida.";return@TextButton};val intent=Intent(AlarmClock.ACTION_SET_TIMER).putExtra(AlarmClock.EXTRA_LENGTH,value!!*60).putExtra(AlarmClock.EXTRA_SKIP_UI,false);if(context.packageManager.resolveActivity(intent,0)==null)error="No hay una app Reloj compatible." else onAction(Action.Launch(intent))}){Text("Preparar alarma en Reloj")}
  Row{Checkbox(state.stats,{session.statistics(it);state=session.snapshot()});Text("Estadísticas locales opcionales",style=MaterialTheme.typography.bodySmall)}
  if(state.stats){Text("${state.completed} sesiones completas");TextButton(onClick={session.clearStatistics();state=session.snapshot()}){Text("Borrar estadísticas")}}
  HorizontalDivider();Row{TextButton(onClick={onAction(Action.NewNote())}){Text("Nota rápida")};TextButton(onClick={onAction(Action.RestoreQuery("/files "))}){Text("Documentos")}}
 }
}
