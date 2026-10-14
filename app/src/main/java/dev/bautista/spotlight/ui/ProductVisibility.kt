package dev.bautista.spotlight.ui

import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result

/** Retire product entry points, never delete previously saved user data. */
object ProductVisibility {
    fun allowed(result:Result):Boolean = allowedAction(result.action) && !result.id.startsWith("snippet:") && !result.id.startsWith("routine:") && !result.id.startsWith("workflow:")
    fun allowedAction(action:Action):Boolean = when(action) {
        is Action.ExpandSnippet,is Action.WorkflowRun,is Action.Routine -> false
        is Action.SystemPanel -> action.page !in setOf("profiles","tasker","agent")
        else -> true
    }
}
