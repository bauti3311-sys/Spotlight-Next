package dev.bautista.spotlight.search

/** Bounded, typed local plans. Parsing never resolves permissions or executes a step. */
sealed interface PlanStep {
    data class Brightness(val percent:Int):PlanStep
    data class Volume(val percent:Int):PlanStep
    data class Open(val name:String):PlanStep
}
data class LocalPlan(val steps:List<PlanStep>) {
    companion object {
        fun parse(raw:String):LocalPlan? {
            if(raw.length>4000)return null
            val parts=normalize(raw.trim()).split(Regex("\\s+(?:y|luego|despues)\\s+(?=(?:abrir|iniciar|bajar|subir|ajustar|poner|cambiar|brillo|volumen)\\b)"))
            if(parts.size !in 2..6)return null
            val steps=parts.map { part ->
                Regex("^(?:abrir|iniciar) (.+)$").matchEntire(part)?.let { return@map PlanStep.Open(it.groupValues[1]) }
                val m=Regex("^(?:(?:bajar|subir|ajustar|poner|cambiar) )?(?:el )?(brillo|volumen)(?: al| a)? (\\d{1,3})\\s*%?$").matchEntire(part) ?: return null
                val percent=m.groupValues[2].toInt()
                if(percent !in 0..100)return null
                if(m.groupValues[1]=="brillo")PlanStep.Brightness(percent) else PlanStep.Volume(percent)
            }
            return LocalPlan(steps)
        }
    }
}
