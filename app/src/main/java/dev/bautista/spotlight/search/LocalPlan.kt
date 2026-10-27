package dev.bautista.spotlight.search

sealed interface PlanStep {
 data class Brightness(val percent:Int):PlanStep
 data class Volume(val percent:Int):PlanStep
 data class Open(val name:String):PlanStep
 data class RelativeBrightness(val delta:Int):PlanStep
 data class RelativeVolume(val delta:Int):PlanStep
 data class Media(val command:String):PlanStep
}
data class LocalPlan(val steps:List<PlanStep>) {
 companion object {
  private val compoundSplit=Regex("(?:\\s*,\\s*|\\s+(?:y|luego|despu[eé]s)\\s+)(?=(?:abrir|abr[ií]|abre|iniciar|bajar|baj[aá]|subir|sub[ií]|ajustar|poner|pon[eé]|cambiar|brillo|volumen|pausar|paus[aá]|reanudar|reproducir|siguiente)\\b)",RegexOption.IGNORE_CASE)
  fun step(raw:String):PlanStep? {
   val trimmed=raw.trim()
   if(compoundSplit.containsMatchIn(trimmed) || Regex("\\s+(?:y|luego|despu[eé]s)\\s+(?:borrar|eliminar|formatear|enviar|mandar|explic|resum)\\b",RegexOption.IGNORE_CASE).containsMatchIn(trimmed))return null
   Regex("^(?:abrir|iniciar|abr[ií]|abre) (.+)$",RegexOption.IGNORE_CASE).matchEntire(trimmed)?.let{return PlanStep.Open(normalize(it.groupValues[1]))}
   val q=LocalLanguage.canonical(trimmed)
   if(q in setOf("pausar musica","pausar la musica","pausar"))return PlanStep.Media("pause")
   if(q in setOf("reanudar musica","reproducir musica","reanudar"))return PlanStep.Media("play")
   if(q in setOf("siguiente cancion","siguiente tema"))return PlanStep.Media("next")
   if(q in setOf("cancion anterior","tema anterior"))return PlanStep.Media("previous")
   Regex("^(?:abrir|iniciar) (.+)$").matchEntire(q)?.let{return PlanStep.Open(it.groupValues[1])}
   val m=Regex("^(?:(bajar|subir|ajustar|poner|cambiar) )?(?:el )?(brillo|volumen)(?: (al|a|en|un))? (\\d{1,3})\\s*%?$").matchEntire(q) ?: return null
   val value=m.groupValues[4].toInt();if(value !in 0..100)return null
   val relative=m.groupValues[1] in setOf("bajar","subir") && m.groupValues[3] !in setOf("al","a")
   val delta=if(m.groupValues[1]=="bajar")-value else value
   return if(m.groupValues[2]=="brillo")if(relative)PlanStep.RelativeBrightness(delta) else PlanStep.Brightness(value) else if(relative)PlanStep.RelativeVolume(delta) else PlanStep.Volume(value)
  }
  fun parse(raw:String):LocalPlan? {
   if(raw.length>4000)return null
   val parts=raw.trim().split(compoundSplit)
   if(parts.size !in 2..6)return null
   return LocalPlan(parts.map{step(it) ?: return null})
  }
 }
}
