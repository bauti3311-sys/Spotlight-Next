package dev.bautista.spotlight.ui

import androidx.compose.runtime.saveable.Saver
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.automation.*
import org.json.*

internal val ToolProposalSaver=Saver<ToolPlan?,String>(
    save={p->p?.let{JSONObject().put("explanation",it.explanation).put("calls",JSONArray(it.calls.map(Workflows::encodeTool))).toString()}},
    restore={raw->runCatching{val j=JSONObject(raw);val a=j.getJSONArray("calls");require(a.length()<=12);ToolPlan(j.getString("explanation"),(0 until a.length()).map{Workflows.toolCall(a.getString(it)).also(ToolRegistry::decode)})}.getOrNull()})
internal data class ToolOutcome(val text:String,val state:ExecutionState,val review:Boolean=false)
internal val ToolOutcomesSaver=Saver<Map<Int,ToolOutcome>,String>(
    save={map->JSONArray().apply{map.forEach{(index,value)->put(JSONObject().put("index",index).put("text",value.text).put("state",value.state.name).put("review",value.review))}}.toString()},
    restore={raw->runCatching{val a=JSONArray(raw);(0 until a.length()).associate{val j=a.getJSONObject(it);j.getInt("index") to ToolOutcome(j.getString("text"),ExecutionState.valueOf(j.getString("state")),j.getBoolean("review"))}}.getOrDefault(emptyMap())})
internal val WorkflowProgressSaver=Saver<WorkflowProgress?,String>(save={p->p?.let{JSONObject().put("step",it.step).put("total",it.total).put("label",it.label).put("output",it.output).put("complete",it.complete).put("failed",it.failed).put("warning",it.warning).toString()}},restore={raw->val j=JSONObject(raw);val done=j.getBoolean("complete");WorkflowProgress(j.getInt("step"),j.getInt("total"),if(done)j.getString("label") else "Interrumpida · revisar antes de repetir",j.getString("output"),complete=done,failed=j.getBoolean("failed")||!done,warning=j.getString("warning"))})

internal val SearchTaskSaver=Saver<dev.bautista.spotlight.search.Action.ComposeSearch?,String>(save={it?.let{v->JSONArray(listOf(v.topic,v.packageName,v.query)).toString()}},restore={val a=JSONArray(it);dev.bautista.spotlight.search.Action.ComposeSearch(a.getString(0),a.getString(1),a.getString(2))})
internal val MessageTaskSaver=Saver<dev.bautista.spotlight.search.Action.ComposeMessage?,String>(save={it?.let{v->JSONArray(listOf(v.channel,v.target,v.text,v.replyKey)).toString()}},restore={val a=JSONArray(it);dev.bautista.spotlight.search.Action.ComposeMessage(a.getString(0),a.getString(1),a.getString(2),if(a.isNull(3))null else a.getString(3))})
internal val SystemTaskSaver=Saver<dev.bautista.spotlight.search.Action.SystemPanel?,String>(save={it?.let{v->JSONArray(listOf(v.page,v.packageName)).toString()}},restore={val a=JSONArray(it);dev.bautista.spotlight.search.Action.SystemPanel(a.getString(0),a.getString(1))})
internal val WorkflowTaskSaver=Saver<Workflow?,String>(save={it?.let{v->Workflows.encode(listOf(v))}},restore={Workflows.parse(it).singleOrNull()})
internal val PreparedWorkflowSaver=androidx.compose.runtime.saveable.listSaver<List<Pair<String,String>>,String>(save={it.flatMap{p->listOf(p.first,p.second)}},restore={it.chunked(2).map{p->p[0] to p[1]}})
internal val ConversationTaskSaver=androidx.compose.runtime.saveable.listSaver<MutableList<Pair<String,String>>,String>(save={it.flatMap{p->listOf(p.first,p.second)}},restore={it.chunked(2).map{p->p[0] to p[1]}.toMutableList()})
internal val AskTaskSaver=Saver<dev.bautista.spotlight.search.Action.Ask?,String>(save={it?.let{v->JSONObject().put("prompt",v.prompt).put("sensitive",v.sensitive).put("target",v.messageTarget).put("origin",v.origin.name).put("continuation",v.continuation).put("turns",JSONArray(v.priorMessages.map{listOf(it.first,it.second)})).toString()}},restore={val j=JSONObject(it);val a=j.getJSONArray("turns");dev.bautista.spotlight.search.Action.Ask(j.getString("prompt"),j.getBoolean("sensitive"),j.getString("target"),dev.bautista.spotlight.search.AiInputOrigin.valueOf(j.getString("origin")),reviewed=false,continuation=j.getBoolean("continuation"),priorMessages=(0 until a.length()).map{val p=a.getJSONArray(it);p.getString(0) to p.getString(1)})})
