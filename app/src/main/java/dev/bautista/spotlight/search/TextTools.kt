package dev.bautista.spotlight.search

import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Locale
import org.json.*

/** Bounded local transformations, without scripting or external services. */
object TextTools {
    val operations=linkedMapOf("mayusculas" to "Mayúsculas","minusculas" to "Minúsculas","capitalizar" to "Capitalizar","limpiar espacios" to "Limpiar espacios","ordenar lineas" to "Ordenar líneas","quitar duplicados" to "Quitar duplicados","contar palabras" to "Contar palabras","contar caracteres" to "Contar caracteres","crear lista" to "Crear lista","slug" to "Crear identificador","codificar url" to "Codificar URL","decodificar url" to "Decodificar URL","formatear json" to "Formatear JSON")
    fun transform(operation:String,text:String):String {
        require(text.length<=16000){"Máximo 16.000 caracteres."}
        return when(operation){
            "mayusculas"->text.uppercase(Locale.ROOT)
            "minusculas"->text.lowercase(Locale.ROOT)
            "capitalizar"->Regex("\\p{L}+").replace(text.lowercase(Locale.ROOT)){it.value.replaceFirstChar{c->c.titlecase(Locale.ROOT)}}
            "limpiar espacios"->text.lineSequence().joinToString("\n"){it.trim().replace(Regex("[\\t ]+")," ")}.trim()
            "ordenar lineas"->text.lines().sortedWith(compareBy{normalize(it)}).joinToString("\n")
            "quitar duplicados"->text.lines().distinct().joinToString("\n")
            "contar palabras"->Regex("[\\p{L}\\p{N}]+(?:['’-][\\p{L}\\p{N}]+)*").findAll(text).count().toString()
            "contar caracteres"->text.codePointCount(0,text.length).toString()
            "crear lista"->text.lines().filter{it.isNotBlank()}.joinToString("\n"){"• ${it.trim()}"}
            "slug"->normalize(text).replace(Regex("[^a-z0-9]+"),"-").trim('-')
            "codificar url"->URLEncoder.encode(text,"UTF-8").replace("+","%20")
            "decodificar url"->URLDecoder.decode(text,"UTF-8")
            "formatear json"->{require(text.trim().startsWith('{')||text.trim().startsWith('[')){"Pegá un objeto o una lista JSON."};if(text.trim().startsWith('{'))JSONObject(text).toString(2) else JSONArray(text).toString(2)}
            else->error("Herramienta no compatible.")
        }
    }
    fun results(query:String):List<Result> {
        val n=normalize(query.trim());val out=mutableListOf<Result>()
        if(n in setOf("herramientas de texto","texto","transformar texto"))operations.forEach{(key,label)->out+=Result("text:menu:$key",label,"Local · escribir o pegar texto","Herramientas",2100,Action.RestoreQuery("$label: "))}
        val head=normalize(query.substringBefore(':').trim());val input=query.substringAfter(':',"")
        if(head in operations && input.isNotBlank())out+=Result("text:$head",operations.getValue(head),"Transformación local · revisar resultado","Herramientas",2800,Action.Lookup("text:$head",input.trimStart()))
        if(n in setOf("uuid","generar uuid"))out+=Result("text:uuid","Generar UUID","Identificador aleatorio local","Herramientas",2800,Action.Lookup("uuid",""))
        return out
    }
}

object SymbolTools {
    private val entries=listOf("✓" to "check correcto","✕" to "cruz cerrar","→" to "flecha derecha","←" to "flecha izquierda","↑" to "flecha arriba","↓" to "flecha abajo","±" to "mas menos","°" to "grado temperatura","×" to "multiplicar","÷" to "dividir","∞" to "infinito","π" to "pi","©" to "copyright","®" to "registrado","™" to "marca","€" to "euro","⌘" to "comando","😀" to "sonrisa feliz","❤️" to "corazon amor","👍" to "pulgar bien","🎉" to "celebracion","📌" to "chincheta fijar")
    fun results(query:String):List<Result>{val n=normalize(query.trim());val prefix=listOf("simbolos","simbolo","emoji","emojis").firstOrNull{n==it||n.startsWith("$it ")}?:return emptyList();val term=n.removePrefix(prefix).trim();return entries.filter{term.isBlank()||it.second.contains(term)}.map{(value,label)->Result("symbol:$value",label.replaceFirstChar{it.titlecase()},"Copiar $value","Herramientas",2100,Action.Copy(value),preview=value)}}
}
