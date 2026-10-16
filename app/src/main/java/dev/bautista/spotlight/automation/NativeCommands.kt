package dev.bautista.spotlight.automation

import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.search.*

/** Explicitly marked native plans only. Legacy routines are never promoted implicitly. */
object NativeCommands {
 const val KIND="reviewed-native-v1"
 fun accepts(call:ToolCall):Boolean=runCatching {
  when(val request=ToolRegistry.decode(call)) {
   is ToolRequest.OpenApp,is ToolRequest.Volume,is ToolRequest.Media->true
   is ToolRequest.SettingWrite->request.key=="screen_brightness" && request.value.toInt() in (8..100).map{it*255/100}
   else->false
  }
 }.getOrDefault(false)
 fun validateSteps(w:Workflow):String? {
  if(w.steps.size !in 1..6)return "Un comando nativo admite entre 1 y 6 pasos."
  if(w.steps.any{it.type!="tool_action" || it.onError!="stop" || !runCatching{accepts(Workflows.toolCall(it.value))}.getOrDefault(false)})return "El comando contiene una acción no compatible."
  return null
 }
 fun isNative(w:Workflow)=w.kind==KIND && Workflows.validate(w)==null
 fun result(w:Workflow)=Result("command:${w.id}",w.name,"${w.steps.size} pasos nativos · revisar antes de ejecutar","Comandos",1800,Action.SystemPanel("saved-command",w.id))
 fun results(raw:String)=Workflows.parse(raw).filter(::isNative).map(::result)
 data class Review(val plan:LocalPlan,val calls:List<ToolCall>)
 fun step(call:ToolCall,label:(String)->String):PlanStep {
  require(accepts(call)){"Acción no compatible."}
  return when(val request=ToolRegistry.decode(call)) {
   is ToolRequest.OpenApp->PlanStep.Open(label(request.packageName))
   is ToolRequest.Volume->PlanStep.Volume(request.percent)
   is ToolRequest.Media->PlanStep.Media(request.command)
   is ToolRequest.SettingWrite->PlanStep.Brightness((request.value.toInt()*100+254)/255)
   else->error("Acción no compatible.")
  }
 }
 fun review(w:Workflow,label:(String)->String):Review {
  require(isNative(w)){"El comando no está disponible o requiere una versión compatible."}
  val calls=w.steps.map{Workflows.toolCall(it.value)}
  val steps=calls.map{step(it,label)}
  return Review(LocalPlan(steps),calls)
 }
}
