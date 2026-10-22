package dev.bautista.spotlight.ui

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.SaveableStateHolder

/** At most eight contextual drafts, owned by this overlay, never a global conversation cache. */
@Composable internal fun ContextualDraftHost(taskKey:String,holder:SaveableStateHolder=rememberSaveableStateHolder(),recent:MutableList<String> = remember{mutableListOf()},content:@Composable ()->Unit) {
    SideEffect {
        recent.remove(taskKey);recent.add(taskKey)
        while(recent.size>8)holder.removeState(recent.removeAt(0))
    }
    holder.SaveableStateProvider(taskKey,content)
}
