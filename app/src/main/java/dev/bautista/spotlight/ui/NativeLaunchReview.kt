package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.control.LaunchIssue
import dev.bautista.spotlight.control.LaunchProblem

/** Existing inspector content, not a dialog/extra overlay window. Never auto-confirms. */
@Composable internal fun NativeLaunchReview(issue:LaunchIssue,onConfirm:(String)->Unit,onCancel:()->Unit) {
    val cancelFocus=remember{FocusRequester()}
    LaunchedEffect(issue){withFrameNanos{};cancelFocus.requestFocus()}
    Column(Modifier.fillMaxWidth().heightIn(max=GlassTokens.actionPanelMaxHeight).verticalScroll(rememberScrollState()).padding(16.dp).testTag("native-launch-review"),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text(when(issue.problem){LaunchProblem.UNKNOWN_OUTCOME->"Resultado sin comprobar";LaunchProblem.ALREADY_REQUESTED->"Acción ya revisada";else->"No se abrió el destino"},style=MaterialTheme.typography.titleMedium,modifier=Modifier.semantics{heading()})
        Text(issue.problem.message,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall,
            modifier=Modifier.semantics{liveRegion=LiveRegionMode.Polite})
        issue.alternative?.let { alternative ->
            Text("Alternativa disponible",style=MaterialTheme.typography.labelMedium)
            Text(alternative.label,style=MaterialTheme.typography.bodyMedium)
            Text(alternative.destination,style=MaterialTheme.typography.bodySmall)
            Text("Sólo se abrirá si la confirmás. El resultado final depende de la app receptora.",style=MaterialTheme.typography.bodySmall)
        }
        // Default focus is the harmless action; Enter must not silently approve a fallback.
        TextButton(onClick=onCancel,modifier=Modifier.heightIn(min=48.dp).focusProperties{canFocus=true}.focusRequester(cancelFocus)) { Text("Volver sin abrir") }
        issue.alternative?.let { alternative ->
            TextButton(onClick={onConfirm(alternative.id)},modifier=Modifier.heightIn(min=48.dp).focusProperties{canFocus=true}.testTag("confirm-native-alternative")) {
                Text("Confirmar y abrir alternativa")
            }
        }
    }
}

/** Local Android receipt only; no query/URI/message content and no restored authorization. */
@Composable internal fun NativeLaunchRequestStatus(destination:String?) {
    if(destination!=null)Text("Solicitud enviada a Android · $destination\nResultado externo sin comprobar.",
        style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,
        modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=8.dp)
            .testTag("native-launch-request").semantics{liveRegion=LiveRegionMode.Polite})
}
