package dev.bautista.spotlight.ui

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder

/** At most eight contextual drafts, owned by this overlay, never a global conversation cache. */
@Composable internal fun ContextualDraftHost(taskKey:String,content:@Composable ()->Unit) {
    val holder=rememberSaveableStateHolder()
    val recent=remember{mutableListOf<String>()}
    SideEffect {
        recent.remove(taskKey);recent.add(taskKey)
        while(recent.size>8)holder.removeState(recent.removeAt(0))
    }
    holder.SaveableStateProvider(taskKey,content)
}
