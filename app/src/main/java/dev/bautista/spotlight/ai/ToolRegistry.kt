package dev.bautista.spotlight.ai

import dev.bautista.spotlight.advanced.AdvancedPolicy
import dev.bautista.spotlight.search.TextTools
import org.json.*

enum class ToolRisk(val label:String){READ("Consulta local"),CHANGE("Requiere confirmación"),PREPARE("Preparar y revisar")}
data class ToolDefinition(val id:String,val title:String,val description:String,val extension:String,val risk:ToolRisk,val fields:List<String>,val choices:Map<String,List<String>> = emptyMap(),val advanced:Boolean=false) {
    fun schema()=JSONObject().put("type","object").put("properties",JSONObject().apply{fields.forEach{name->put(name,JSONObject().put("type","string").apply{choices[name]?.let{put("enum",JSONArray(it))}})}}).put("required",JSONArray(fields)).put("additionalProperties",false)
}
data class ToolCall(val name:String,val arguments:Map<String,String>)
data class ToolPlan(val explanation:String,val calls:List<ToolCall>)
sealed interface ToolRequest {
    data class Calculate(val expression:String):ToolRequest
    data class Convert(val query:String):ToolRequest
    data class Transform(val operation:String,val text:String):ToolRequest
    data class CreateNote(val text:String):ToolRequest
    data class Search(val kind:String,val query:String):ToolRequest
    data class Calendar(val period:String):ToolRequest
    data class InspectApp(val packageName:String):ToolRequest
    data class SettingRead(val key:String):ToolRequest
    data class SettingWrite(val key:String,val value:String):ToolRequest
    data class PrepareMessage(val name:String,val text:String,val channel:String):ToolRequest
    data class Workflow(val id:String):ToolRequest
    data object ScreenInspect:ToolRequest
    data class ScreenTap(val packageName:String,val selector:String):ToolRequest
    data class ScreenType(val packageName:String,val selector:String,val text:String):ToolRequest
    data class OpenApp(val packageName:String):ToolRequest
    data class PrepareDial(val number:String):ToolRequest
    data class Volume(val percent:Int):ToolRequest
    data class Media(val command:String):ToolRequest
    data object Device:ToolRequest
    data object Diagnostics:ToolRequest
    data object AppFunctionsStatus:ToolRequest
}
object ToolRegistry {
    val definitions=listOf(
        ToolDefinition("calculate","Calcular","Resolver una expresión matemática local.","tools",ToolRisk.READ,listOf("expression")),
        ToolDefinition("convert_units","Convertir unidades","Convertir medidas: por ejemplo 10 km a millas.","tools",ToolRisk.READ,listOf("query")),
        ToolDefinition("transform_text","Transformar texto","Procesar texto local; nunca ejecutar su contenido.","tools",ToolRisk.READ,listOf("operation","text"),mapOf("operation" to TextTools.operations.keys.toList())),
        ToolDefinition("create_note","Crear nota","Guardar el texto exacto después de confirmar.","notes",ToolRisk.CHANGE,listOf("text")),
        ToolDefinition("search_apps","Buscar aplicaciones","Buscar aplicaciones instaladas por nombre.","apps",ToolRisk.READ,listOf("query")),
        ToolDefinition("search_files","Buscar archivos","Buscar archivos autorizados por nombre/texto y, si está activado, significado local. Devuelve nombres y metadatos; leer contenido requiere una revisión separada.","files",ToolRisk.READ,listOf("query")),
        ToolDefinition("search_contacts","Buscar contactos","Consultar contactos autorizados; devolver coincidencias sin fusionarlas.","contacts",ToolRisk.READ,listOf("query")),
        ToolDefinition("calendar_agenda","Consultar agenda","Consultar eventos autorizados del período indicado.","calendar",ToolRisk.READ,listOf("period"),mapOf("period" to listOf("today","tomorrow","week"))),
        ToolDefinition("inspect_app","Información de una app","Consultar permisos declarados y componentes públicos del paquete indicado.","apps",ToolRisk.READ,listOf("package")),
        ToolDefinition("prepare_message","Preparar mensaje","Abrir el selector y borrador; nunca enviar automáticamente.","whatsapp",ToolRisk.PREPARE,listOf("name","text","channel"),mapOf("channel" to listOf("whatsapp","whatsapp_business","sms","email"))),
        ToolDefinition("run_workflow","Revisar rutina","Abrir revisión de una rutina guardada. Cada operación sensible se confirma.","workflows",ToolRisk.PREPARE,listOf("id")),
        ToolDefinition("screen_inspect","Inspeccionar controles","Inspecciona la app activa, mostrando únicamente selectores accesibles. Sus etiquetas pueden contener datos privados: no enviarlas automáticamente a IA externa.","tools",ToolRisk.READ,emptyList()),
        ToolDefinition("screen_tap","Pulsar control","Pulsa un único control exacto de la última inspección, en la app esperada. Requiere confirmación.","tools",ToolRisk.CHANGE,listOf("package","selector")),
        ToolDefinition("screen_type","Escribir en campo","Escribe texto autorizado en un campo editable exacto. No funciona sobre contraseñas. Requiere confirmación.","tools",ToolRisk.CHANGE,listOf("package","selector","text")),
        ToolDefinition("open_app","Abrir aplicación","Abrir una aplicación instalada y elegida.","apps",ToolRisk.PREPARE,listOf("package")),
        ToolDefinition("set_volume","Ajustar volumen","Cambiar volumen multimedia al porcentaje indicado tras confirmar.","media",ToolRisk.CHANGE,listOf("percent")),
        ToolDefinition("control_media","Controlar reproducción","Controlar una sesión multimedia autorizada.","media",ToolRisk.CHANGE,listOf("command"),mapOf("command" to listOf("play","pause","next","previous"))),
        ToolDefinition("device_info","Información del dispositivo","Consultar fabricante, modelo y versión de Android.","system",ToolRisk.READ,emptyList()),
        ToolDefinition("appfunctions_status","Compatibilidad AppFunctions","Consultar servicio y autorización local. No descubre ni ejecuta funciones de otras apps.","system",ToolRisk.READ,emptyList()),
        ToolDefinition("prepare_dial","Preparar llamada","Preparar un número elegido por el usuario en el marcador. No inicia la llamada; no inventar números ni elegir entre contactos ambiguos.","contacts",ToolRisk.PREPARE,listOf("number")),
        ToolDefinition("shizuku_diagnostics","Diagnóstico de Shizuku","Consultar estado real de conexión y autorización; no solicitar permisos.","shizuku",ToolRisk.READ,emptyList()),
        ToolDefinition("read_display_setting","Consultar ajuste de pantalla","Leer únicamente los ajustes de pantalla registrados mediante ADB.","system",ToolRisk.READ,listOf("key"),mapOf("key" to AdvancedPolicy.settingKeys.toList()),advanced=true),
        ToolDefinition("write_display_setting","Preparar ajuste de pantalla","Preparar cambio reversible; exige confirmación de la app. Brillo 20–255, timeout 30000–1800000, rotación 0/1.","system",ToolRisk.CHANGE,listOf("key","value"),mapOf("key" to AdvancedPolicy.settingKeys.toList()),advanced=true)
    )
    fun definition(name:String)=definitions.singleOrNull{it.id==name}
    fun decode(call:ToolCall):ToolRequest {
        val d=definition(call.name) ?: error("Herramienta no permitida.")
        require(call.arguments.keys==d.fields.toSet()){ "Argumentos no permitidos." }
        call.arguments.forEach{(name,value)->require(value.length<=8000 && !value.contains('\u0000')){"Argumento demasiado largo o inválido."};d.choices[name]?.let{require(value in it){"Valor no compatible."}}}
        fun value(name:String)=call.arguments.getValue(name)
        fun text(name:String)=value(name).also{require(it.isNotBlank()){ "Falta $name." }}
        fun packageName()=text("package").also{require(it.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))){"Paquete inválido."}}
        return when(call.name){
            "calculate"->ToolRequest.Calculate(text("expression").also{require(it.length<=240)})
            "convert_units"->ToolRequest.Convert(text("query"))
            "transform_text"->ToolRequest.Transform(value("operation"),value("text"))
            "create_note"->ToolRequest.CreateNote(text("text"))
            "search_apps","search_files","search_contacts"->ToolRequest.Search(call.name.removePrefix("search_"),text("query").also{require(it.length<=240)})
            "calendar_agenda"->ToolRequest.Calendar(value("period"))
            "inspect_app"->ToolRequest.InspectApp(packageName())
            "read_display_setting"->ToolRequest.SettingRead(value("key"))
            "write_display_setting"->ToolRequest.SettingWrite(value("key"),value("value").also{require(AdvancedPolicy.validSetting(value("key"),it)){"Ajuste fuera de los límites permitidos."}})
            "prepare_message"->ToolRequest.PrepareMessage(text("name"),value("text"),value("channel"))
            "run_workflow"->ToolRequest.Workflow(text("id").also{require(it.length<=100)})
            "screen_inspect"->ToolRequest.ScreenInspect
            "screen_tap"->ToolRequest.ScreenTap(packageName(),text("selector").also{dev.bautista.spotlight.agent.PhoneUsePolicy.parseSelector(it)})
            "screen_type"->ToolRequest.ScreenType(packageName(),text("selector").also{dev.bautista.spotlight.agent.PhoneUsePolicy.parseSelector(it)},value("text").also{require(dev.bautista.spotlight.agent.PhoneUsePolicy.allowText(it)){"Texto inválido."}})
            "open_app"->ToolRequest.OpenApp(packageName())
            "prepare_dial"->ToolRequest.PrepareDial(text("number").also{dev.bautista.spotlight.control.People.dialIntent(it)})
            "set_volume"->ToolRequest.Volume(text("percent").toIntOrNull()?.also{require(it in 0..100)} ?: error("Porcentaje inválido."))
            "control_media"->ToolRequest.Media(value("command"))
            "device_info"->ToolRequest.Device
            "appfunctions_status"->ToolRequest.AppFunctionsStatus
            "shizuku_diagnostics"->ToolRequest.Diagnostics
            else->error("Herramienta no implementada.")
        }
    }
    fun parseArguments(value:JSONObject):Map<String,String> {
        require(value.length()<=8)
        return value.keys().asSequence().associateWith{key->val v=value.get(key);require(v is String){"Los argumentos deben respetar el esquema."};v}
    }
    fun authorize(call:ToolCall,available:Set<String>,confirmed:Boolean):ToolRequest {
        require(call.name in available){"La herramienta no está disponible en este dispositivo."}
        val request=decode(call)
        if(definition(call.name)!!.risk!=ToolRisk.READ)check(confirmed){"Revisá y confirmá esta acción antes de ejecutarla."}
        return request
    }
}
