package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** In-place disclosure, not a navigation destination or action engine. No effect on expansion. */
@Composable internal fun InspectorSecondaryActions(
    compact:Boolean,identity:Any?,forceExpanded:Boolean=false,content:@Composable ()->Unit,
) {
    var expanded by remember(identity){mutableStateOf(false)}
    // Finishing work must not fold the controls under the user's pointer/focus.
    LaunchedEffect(identity,forceExpanded){if(forceExpanded)expanded=true}
    Column(Modifier.fillMaxWidth()) {
        if(compact || LocalFocusUi.current)TextButton(onClick={expanded=!expanded},enabled=!forceExpanded,
            modifier=Modifier.heightIn(min=48.dp).testTag("inspector-more-actions"),
            contentPadding=PaddingValues(horizontal=2.dp),
            colors=ButtonDefaults.textButtonColors(contentColor=MaterialTheme.colorScheme.onSurfaceVariant)) {
            Text(if(expanded || forceExpanded)"Menos acciones" else "Más acciones",
                fontSize=GlassTokens.scaledSp(12.sp,LocalAppearance.current))
        }
        // A running extraction's cancel action must never disappear behind the disclosure.
        if((!compact && !LocalFocusUi.current) || expanded || forceExpanded)content()
    }
}

@Composable internal fun inspectorButtonColors()=ButtonDefaults.buttonColors(
    containerColor=MaterialTheme.colorScheme.onSurface.copy(alpha=.065f),
    contentColor=MaterialTheme.colorScheme.onSurface,
    disabledContainerColor=MaterialTheme.colorScheme.onSurface.copy(alpha=.025f),
    disabledContentColor=MaterialTheme.colorScheme.onSurface.copy(alpha=.38f),
)
