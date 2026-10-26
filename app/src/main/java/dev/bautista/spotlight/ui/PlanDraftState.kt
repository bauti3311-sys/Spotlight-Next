package dev.bautista.spotlight.ui

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import dev.bautista.spotlight.ai.ToolCall
import dev.bautista.spotlight.automation.Workflows
import org.json.JSONArray
import org.json.JSONObject

internal val PlanBindingsSaver=listSaver<List<ToolCall?>,String>(
    save={calls->calls.map{it?.let(Workflows::encodeTool).orEmpty()}},
    restore={values->values.map{it.takeIf(String::isNotBlank)?.let(Workflows::toolCall)}})
internal val PlanSelectionSaver=listSaver<Set<Int>,Int>(save={it.toList()},restore={it.toSet()})
internal val PlanStatusSaver=Saver<Map<Int,String>,ArrayList<String>>(
    save={map->ArrayList(map.entries.flatMap{listOf(it.key.toString(),it.value)})},
    restore={list->list.chunked(2).associate{it[0].toInt() to it[1].replace("Ejecutando…","Interrumpido · revisá el dispositivo")}})
internal fun planDraftKey(steps:List<String>,bindings:List<ToolCall?>)=JSONObject()
    .put("steps",JSONArray(steps)).put("bindings",JSONArray(bindings.map{it?.let(Workflows::encodeTool)})).toString()
