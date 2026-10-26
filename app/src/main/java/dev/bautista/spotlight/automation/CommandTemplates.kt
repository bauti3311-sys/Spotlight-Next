package dev.bautista.spotlight.automation

import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.personal.PersonalCommands
import dev.bautista.spotlight.search.*
import org.json.*

enum class CommandParameterType { TEXT, NUMBER, PACKAGE, CHOICE }
data class CommandParameter(val name:String,val type:CommandParameterType,val defaultValue:String)
data class CommandParameterTarget(val step:Int,val argument:String,val parameter:String)
data class CommandTemplate(
    val id:String,val name:String,val alias:String,val plan:ToolPlan,
    val parameters:List<CommandParameter> = emptyList(),
    val targets:List<CommandParameterTarget> = emptyList(),
    val bindings:ToolBindings = emptyMap(),val revision:Int=0,
)

/** Local data only: parameters replace complete argument values, never code or tool names. */
object CommandTemplates {
    const val KIND="reviewed-tool-template-v1"
    private val identifier=Regex("[A-Za-z_][A-Za-z0-9_]{0,39}")
    fun parameterType(call:ToolCall,argument:String):CommandParameterType {
        val definition=ToolRegistry.definition(call.name) ?: error("Herramienta no registrada.")
        require(argument in definition.fields)
        return when {
            argument in definition.choices -> CommandParameterType.CHOICE
            argument=="package" -> CommandParameterType.PACKAGE
            StructuredToolResults.inputType(call,argument)==ToolValueType.NUMBER -> CommandParameterType.NUMBER
            else -> CommandParameterType.TEXT
        }
    }
    private fun checkValue(parameter:CommandParameter,value:String) {
        require(value.length<=8000 && '\u0000' !in value){"Valor demasiado largo o inválido."}
        when(parameter.type) {
            CommandParameterType.NUMBER -> require(StructuredToolResults.number(value)!=null){"${parameter.name}: número inválido."}
            CommandParameterType.PACKAGE -> require(value.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))){"Paquete inválido."}
            else -> Unit // Choice membership is checked against each destination's actual schema.
        }
    }
    fun validate(template:CommandTemplate) {
        require(template.id.isNotBlank() && template.id.length<=100 && template.revision>=0)
        require(template.name.isNotBlank() && template.name.length<=60)
        require(template.alias.isEmpty() || PersonalCommands.validAlias(template.alias)){"Alias inválido."}
        require(template.plan.explanation.length<=3000 && template.plan.calls.size in 1..6)
        require(template.parameters.size<=16 && template.targets.size<=48)
        require(template.parameters.map{it.name}.distinct().size==template.parameters.size){"Parámetro duplicado."}
        template.parameters.forEach{require(identifier.matches(it.name));checkValue(it,it.defaultValue)}
        require(template.targets.map{it.step to it.argument}.distinct().size==template.targets.size){"Argumento parametrizado más de una vez."}
        require(template.parameters.map{it.name}.toSet()==template.targets.map{it.parameter}.toSet()){"Parámetro sin destino o sin definir."}
        template.plan.calls.forEach { require(it.name!="run_workflow"){"Un comando no puede invocar otra rutina."};ToolRegistry.decode(it) }
        template.targets.forEach { target ->
            require(target.step in template.plan.calls.indices){"Paso inexistente."}
            val parameter=template.parameters.single{it.name==target.parameter}
            require(parameter.type==parameterType(template.plan.calls[target.step],target.argument)){"Tipo incompatible con el argumento."}
            require(target.argument !in template.bindings[target.step].orEmpty()){"El argumento ya depende de otro paso."}
        }
        StructuredToolResults.validate(template.plan.calls,template.bindings,emptySet())
        resolve(template,template.parameters.associate{it.name to it.defaultValue})
    }
    /** Dynamic forms derive choices from every destination, not from user-entered labels. */
    fun choices(template:CommandTemplate,parameter:String):List<String> {
        val destinations=template.targets.filter{it.parameter==parameter}
        if(destinations.isEmpty())return emptyList()
        val sets=destinations.map { target ->
            ToolRegistry.definition(template.plan.calls[target.step].name)?.choices?.get(target.argument)?.toSet() ?: emptySet()
        }
        return sets.reduce{a,b->a.intersect(b)}.sorted()
    }
    fun parameterErrors(template:CommandTemplate,values:Map<String,String>):Map<String,String> {
        validate(template)
        val errors=linkedMapOf<String,String>()
        template.parameters.forEach { parameter ->
            val value=values[parameter.name]
            val problem=runCatching {
                require(value!=null){"Falta este parámetro."};checkValue(parameter,value)
                template.targets.filter{it.parameter==parameter.name}.forEach { target ->
                    val call=template.plan.calls[target.step]
                    val supplied=template.targets.filter{it.step==target.step && it.parameter in values}
                        .associate{it.argument to values.getValue(it.parameter)}
                    ToolRegistry.decode(call.copy(arguments=call.arguments+supplied+(target.argument to value)))
                }
            }.exceptionOrNull()?.message
            if(problem!=null)errors[parameter.name]=problem
        }
        (values.keys-template.parameters.map{it.name}.toSet()).forEach{errors[it]="Parámetro desconocido."}
        return errors
    }
    private fun resolve(template:CommandTemplate,values:Map<String,String>):ToolPlan {
        require(values.keys==template.parameters.map{it.name}.toSet()){"Faltan parámetros o hay parámetros desconocidos."}
        template.parameters.forEach{checkValue(it,values.getValue(it.name))}
        val calls=template.plan.calls.mapIndexed { i,call ->
            val replacements=template.targets.filter{it.step==i}.associate{it.argument to values.getValue(it.parameter)}
            call.copy(arguments=call.arguments+replacements).also{ToolRegistry.decode(it)}
        }
        return template.plan.copy(calls=calls)
    }
    fun instantiate(template:CommandTemplate,values:Map<String,String> = template.parameters.associate{it.name to it.defaultValue}):ToolPlan {
        validate(template)
        return resolve(template,values)
    }
    fun encode(template:CommandTemplate):JSONObject {
        validate(template)
        return JSONObject().put("kind",KIND).put("id",template.id).put("name",template.name).put("alias",template.alias)
            .put("revision",template.revision).put("explanation",template.plan.explanation)
            .put("calls",JSONArray().apply{template.plan.calls.forEach{put(JSONObject(Workflows.encodeTool(it)))}})
            .put("bindings",JSONArray(StructuredToolResults.encodeBindings(template.bindings)))
            .put("parameters",JSONArray().apply{template.parameters.forEach{put(JSONObject().put("name",it.name).put("type",it.type.name).put("default",it.defaultValue))}})
            .put("targets",JSONArray().apply{template.targets.forEach{put(JSONObject().put("step",it.step).put("argument",it.argument).put("parameter",it.parameter))}})
    }
    private fun integer(j:JSONObject,key:String):Int {
        val value=j.get(key);require(value is Int){"Se requiere un entero exacto: $key."};return value
    }
    fun decode(j:JSONObject):CommandTemplate {
        require(j.keys().asSequence().toSet()==setOf("kind","id","name","alias","revision","explanation","calls","bindings","parameters","targets")){"Formato de plantilla desconocido."}
        require(j.getString("kind")==KIND)
        val calls=j.getJSONArray("calls");val parameters=j.getJSONArray("parameters");val targets=j.getJSONArray("targets")
        require(calls.length() in 1..6 && parameters.length()<=16 && targets.length()<=48)
        val template=CommandTemplate(j.getString("id"),j.getString("name"),j.getString("alias"),
            ToolPlan(j.getString("explanation"),(0 until calls.length()).map{Workflows.toolCall(calls.getJSONObject(it).toString())}),
            (0 until parameters.length()).map{val p=parameters.getJSONObject(it);require(p.keys().asSequence().toSet()==setOf("name","type","default"));CommandParameter(p.getString("name"),CommandParameterType.valueOf(p.getString("type")),p.getString("default"))},
            (0 until targets.length()).map{val t=targets.getJSONObject(it);require(t.keys().asSequence().toSet()==setOf("step","argument","parameter"));CommandParameterTarget(integer(t,"step"),t.getString("argument"),t.getString("parameter"))},
            StructuredToolResults.decodeBindings(j.getJSONArray("bindings").toString()),integer(j,"revision"))
        validate(template);return template
    }
    fun parse(raw:String):List<CommandTemplate> = runCatching {
        val array=JSONArray(raw);require(array.length()<=30)
        (0 until array.length()).mapNotNull{i->val item=array.optJSONObject(i)
            if(item?.optString("kind")==KIND)runCatching{decode(item)}.getOrNull() else null}
    }.getOrDefault(emptyList())
    /** Called inside DataStore.edit: compare revision and check conflicts against current data. */
    fun upsert(raw:String,template:CommandTemplate,expectedRevision:Int?,aliases:Set<String>):String {
        validate(template)
        val array=JSONArray(raw);require(array.length()<=30)
        val matches=(0 until array.length()).filter{array.optJSONObject(it)?.optString("id")==template.id}
        val index=if(expectedRevision==null) {require(matches.isEmpty()){"El identificador ya existe."};require(array.length()<30){"Límite de 30 comandos."};array.length()}
        else {require(matches.size==1){"El comando cambió o no está disponible."};val i=matches.single();val previous=decode(array.getJSONObject(i));require(previous.revision==expectedRevision && template.revision==expectedRevision){"El comando cambió en otra sesión."};i}
        val keys=setOf(template.name,template.alias).filter{it.isNotBlank()}.map(::normalize).toSet()
        require(aliases.none{normalize(it.substringBefore('|')) in keys}){"El nombre o alias coincide con un alias existente."}
        for(i in 0 until array.length())if(i!=index) {
            val item=array.optJSONObject(i) ?: continue
            require(listOf(item.optString("name"),item.optString("alias")).filter{it.isNotBlank()}.none{normalize(it) in keys}){"El nombre o alias coincide con otro comando."}
        }
        val next=if(expectedRevision==null)template.copy(revision=0) else template.copy(revision=Math.addExact(expectedRevision,1))
        array.put(index,encode(next));return array.toString()
    }
    fun delete(raw:String,id:String,expectedRevision:Int):String {
        val array=JSONArray(raw)
        val matches=(0 until array.length()).filter{array.optJSONObject(it)?.optString("id")==id}
        require(matches.size==1);val index=matches.single()
        require(decode(array.getJSONObject(index)).revision==expectedRevision){"El comando cambió en otra sesión."}
        return JSONArray().apply{for(i in 0 until array.length())if(i!=index)put(array.get(i))}.toString()
    }
    fun result(t:CommandTemplate)=Result("command:${t.id}",t.name,"${t.parameters.size} parámetros · revisar antes de ejecutar","Comandos",1800,Action.SystemPanel("saved-command",t.id))
    fun results(query:String,raw:String,favorites:Set<String>):List<Result> {
        val q=normalize(query.trim())
        return parse(raw).filter{q in setOf("rutinas","workflows") || q.isBlank() && "command:${it.id}" in favorites || q.isNotBlank() && (relevance(q,it.name)>0 || it.alias.isNotBlank() && q==normalize(it.alias))}.map(::result)
    }
}
