package dev.bautista.spotlight.automation

import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.personal.PersonalCommands
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.*

data class WorkflowStep(val type:String,val value:String="",val onError:String="stop")
data class Workflow(val id:String,val name:String,val alias:String="",val steps:List<WorkflowStep>,val icon:String="layers",val kind:String="")
data class WorkflowProgress(val step:Int,val total:Int,val label:String,val output:String,val complete:Boolean=false,val failed:Boolean=false,val warning:String="")
interface WorkflowHost {suspend fun copy(text:String);suspend fun note(text:String);suspend fun volume(level:Int);suspend fun timer(minutes:Int);suspend fun tool(call:ToolCall):String=error("Herramienta no disponible en este entorno.");suspend fun ai(text:String):String=error("IA no disponible.");suspend fun extract(uri:String):String=error("Archivo no disponible.")}
object Workflows {
    val types=listOf("text","upper","lower","copy","note","volume","wait","timer","clean","dedupe","require_text","setvar","if_empty","if_contains","else","end","tool","tool_action","ai_text","extract")
    val labels=mapOf("text" to "Preparar texto","upper" to "Mayúsculas","lower" to "Minúsculas","copy" to "Copiar","note" to "Guardar nota","volume" to "Volumen","wait" to "Esperar","timer" to "Temporizador","clean" to "Limpiar espacios","dedupe" to "Quitar líneas duplicadas","require_text" to "Continuar si hay texto","setvar" to "Guardar variable","if_empty" to "Si entrada vacía","if_contains" to "Si entrada contiene","else" to "Si no","end" to "Fin de condición","tool" to "Consultar herramienta","tool_action" to "Acción revisable","ai_text" to "Procesar con IA","extract" to "Extraer archivo")
    fun validate(w:Workflow):String? {
        if(w.kind.isNotEmpty() && w.kind!=NativeCommands.KIND)return "Formato de comando no compatible."
        if(w.kind==NativeCommands.KIND)NativeCommands.validateSteps(w)?.let{return it}
        if(w.id.isBlank() || w.id.length>100)return "Identificador de rutina inválido."
        if(w.icon !in setOf("layers","bolt","sun","music"))return "Icono de comando no compatible."
        if(w.name.isBlank() || w.name.length>60)return "Usá un nombre de hasta 60 caracteres."
        if(w.alias.isNotBlank() && !PersonalCommands.validAlias(w.alias))return "Alias inválido."
        if(w.steps.size !in 1..12)return "Elegí entre 1 y 12 pasos."
        val branches=mutableListOf<Boolean>()
        w.steps.forEachIndexed{i,s->
            if(s.type !in types)return "Paso no compatible."
            if(s.onError !in setOf("stop","continue") || s.onError=="continue" && s.type !in setOf("tool","extract","ai_text","clean","dedupe","upper","lower","require_text"))return "Solo las consultas y transformaciones pueden continuar tras un error."
            when(s.type){
                "if_empty","if_contains"->{if(branches.size>=3)return "Máximo 3 condiciones anidadas.";branches+=false}
                "else"->{if(branches.isEmpty() || branches.last())return "Si no requiere una condición sin bifurcación previa.";branches[branches.lastIndex]=true}
                "end"->{if(branches.isEmpty())return "Fin sin condición.";branches.removeAt(branches.lastIndex)}
                "setvar"->if(!s.value.substringBefore('=').matches(Regex("[A-Za-z_][A-Za-z0-9_]{0,23}")) || !s.value.contains('='))return "Variable: nombre=texto."
                "tool","tool_action"->{val call=runCatching{toolCall(s.value)}.getOrNull() ?: return "Elegí una herramienta y sus argumentos.";val d=ToolRegistry.definition(call.name) ?: return "Herramienta no registrada.";if(call.name=="run_workflow" || (s.type=="tool")!=(d.risk==ToolRisk.READ))return "Tipo de herramienta incompatible.";if(call.arguments.keys!=d.fields.toSet())return "Faltan argumentos de la herramienta."}
            }
            val number=s.value.toIntOrNull()
            if(s.type=="volume" && (number==null || number !in 0..100))return "Volumen: 0 a 100."
            if(s.type=="wait" && (number==null || number !in 1..30))return "Espera: 1 a 30 segundos."
            if(s.type=="timer" && (number==null || number !in 1..1440 || i!=w.steps.lastIndex))return "El temporizador debe ser el último paso (1 a 1440 minutos)."
            if(s.value.length>8000)return "El texto supera 8000 caracteres."
        };if(branches.isNotEmpty())return "Cerrá las condiciones con Fin.";return null
    }
    fun parse(raw:String):List<Workflow> = runCatching {val a=JSONArray(raw);(0 until minOf(a.length(),30)).map{i->val j=a.getJSONObject(i);val steps=j.getJSONArray("steps");Workflow(j.getString("id"),j.getString("name"),j.optString("alias"),(0 until steps.length()).map{k->val s=steps.getJSONObject(k);WorkflowStep(s.getString("type"),s.optString("value"),s.optString("onError","stop"))},j.optString("icon","layers").takeIf{it in setOf("layers","bolt","sun","music")} ?: "layers",j.optString("kind",""))}.filter{validate(it)==null}}.getOrDefault(emptyList())
    fun encode(items:List<Workflow>)=JSONArray().apply {items.take(30).forEach{w->put(JSONObject().put("id",w.id).put("name",w.name).put("alias",w.alias).put("icon",w.icon).apply{if(w.kind.isNotEmpty())put("kind",w.kind)}.put("steps",JSONArray().apply{w.steps.forEach{s->put(JSONObject().put("type",s.type).put("value",s.value).put("onError",s.onError))}}))}}.toString()
    fun results(query:String,raw:String,favorites:Set<String>):List<Result> {
        val q=normalize(query.trim());return parse(raw).filter{q in setOf("rutinas","workflows") || q.isBlank() && (if(NativeCommands.isNative(it))"command:${it.id}" else "routine:${it.id}") in favorites || q.isNotBlank() && (relevance(q,it.name)>0 || it.alias.isNotBlank() && q==normalize(it.alias))}.map{if(NativeCommands.isNative(it))NativeCommands.result(it) else Result("routine:${it.id}",it.name,"${it.steps.size} pasos · revisar y ejecutar","Acciones",1800,Action.WorkflowRun(it.id))}
    }
    fun toolCall(raw:String):ToolCall {val j=JSONObject(raw);require(j.keys().asSequence().toSet()==setOf("name","arguments"));return ToolCall(j.getString("name"),ToolRegistry.parseArguments(j.getJSONObject("arguments")))}
    fun encodeTool(call:ToolCall)=JSONObject().put("name",call.name).put("arguments",JSONObject(call.arguments)).toString()
    fun run(w:Workflow,host:WorkflowHost,initialInput:String=""):Flow<WorkflowProgress> = flow {
        require(validate(w)==null){validate(w).orEmpty()};require(initialInput.length<=8000);var output=initialInput;var current=0
        val variables=mutableMapOf<String,String>();val conditions=mutableListOf<Pair<Boolean,Boolean>>()
        fun active()=conditions.all{it.first && it.second}
        fun expand(raw:String):String {var text=PersonalCommands.render(raw).replace("{entrada}",output);Regex("\\{variable:([A-Za-z_][A-Za-z0-9_]{0,23})\\}").findAll(text).map{it.groupValues[1]}.distinct().toList().forEach{name->text=text.replace("{variable:$name}",variables[name] ?: error("Variable sin definir: $name"))};require(text.length<=8000){"El resultado supera 8000 caracteres."};return text}
        try{w.steps.forEachIndexed{i,s->current=i+1;currentCoroutineContext().ensureActive()
            when(s.type){
                "if_empty","if_contains"->{val parent=active();val condition=parent && if(s.type=="if_empty")output.isBlank() else output.contains(expand(s.value),true);conditions+=parent to condition;return@forEachIndexed}
                "else"->{val last=conditions.last();conditions[conditions.lastIndex]=last.first to !last.second;return@forEachIndexed}
                "end"->{conditions.removeAt(conditions.lastIndex);return@forEachIndexed}
            }
            if(!active())return@forEachIndexed
            emit(WorkflowProgress(current,w.steps.size,labels[s.type] ?: s.type,output))
            try {
                if(s.type in setOf("tool","tool_action")){val call=toolCall(s.value);val resolved=call.copy(arguments=call.arguments.mapValues{expand(it.value)});ToolRegistry.decode(resolved);output=host.tool(resolved).take(8000)}
                else {val value=expand(s.value);when(s.type){
                    "setvar"->{val name=value.substringBefore('=');check(variables.size<16 || name in variables);variables[name]=value.substringAfter('=')}
                    "ai_text"->output=host.ai(value.ifBlank{output}).take(8000)
                    "extract"->output=host.extract(value.ifBlank{output}).take(8000)
                    "clean"->output=TextTools.transform("limpiar espacios",output);"dedupe"->output=TextTools.transform("quitar duplicados",output);"require_text"->require(output.isNotBlank()){ "El texto de entrada está vacío." };"text"->output=value;"upper"->output=output.uppercase();"lower"->output=output.lowercase();"copy"->host.copy(value.ifBlank{output});"note"->host.note(value.ifBlank{output});"volume"->host.volume(value.toInt());"wait"->delay(value.toLong()*1000);"timer"->host.timer(value.toInt())
                }};require(output.length<=8000)
            }catch(e:CancellationException){throw e}catch(e:Exception){if(s.onError!="continue")throw e;emit(WorkflowProgress(current,w.steps.size,labels[s.type].orEmpty(),output,warning="Paso $current omitido: ${e.message.orEmpty().take(200)}"))}
        };emit(WorkflowProgress(current,w.steps.size,"Completada",output,true))}catch(e:CancellationException){throw e}catch(e:Exception){emit(WorkflowProgress(current,w.steps.size,"No se pudo completar el paso $current: ${e.message.orEmpty().take(200)}",output,complete=true,failed=true))}
    }
}
