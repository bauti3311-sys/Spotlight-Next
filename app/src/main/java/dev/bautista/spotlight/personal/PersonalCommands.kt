package dev.bautista.spotlight.personal

import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.*
import dev.bautista.spotlight.control.CommandCenter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PersonalCommands {
    fun validAlias(alias: String) = Regex("[\\p{L}\\p{N}_-]{1,24}").matches(alias)
    fun expand(query: String, aliases: Set<String>): String {
        var text = query.trim()
        val seen = mutableSetOf<String>()
        repeat(5) {
            val alias = normalize(text.substringBefore(' '))
            val entry = aliases.firstOrNull { normalize(it.substringBefore('|')) == alias } ?: return text
            if (!seen.add(alias)) return query.trim()
            val target = entry.substringAfter('|', "").trim()
            if (target.isBlank()) return query.trim()
            val arguments = text.substringAfter(' ', "").trim()
            text = target + if(arguments.isNotEmpty()) " $arguments" else ""
        }
        return query.trim()
    }
    fun render(text: String, now: LocalDateTime = LocalDateTime.now()): String = text
        .replace("{fecha}", now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        .replace("{hora}", now.format(DateTimeFormatter.ofPattern("HH:mm")))
    fun fields(template:String):List<String> = Regex("\\{([\\p{L}\\p{N}_-]{1,24})\\}").findAll(template).map{it.groupValues[1]}.filter{it !in setOf("fecha","hora")}.distinct().toList()
    fun fill(template:String,values:Map<String,String>):String { var text=render(template);fields(template).forEach{field->text=text.replace("{$field}",values[field] ?: "{$field}")};return text }
    fun results(query: String, c: Config): List<Result> {
        val q = normalize(query.trim())
        val out = mutableListOf<Result>()
        c.pins.sorted().filter { q.isEmpty() || q in setOf("fijados","favoritos") || relevance(query, it)>0 }.forEach { target ->
            out += Result("pin:$target", target, "Consulta fijada · cargar sin ejecutar", "Consultas",850,Action.RestoreQuery(target))
        }
        val needle = q.removePrefix("snippet ").removePrefix("snip ")
        c.snippets.sorted().forEach { entry ->
            val alias = entry.substringBefore('|')
            val text = entry.substringAfter('|', "")
            if (text.isNotBlank() && (q in setOf("snippets","textos","snippet") || relevance(needle,alias)>0)) {
                val rendered = render(text)
                out += Result("snippet:$alias", alias, "Texto guardado · copiar", "Textos",if(q.startsWith("snip"))1950 else 450,if(fields(text).isEmpty())Action.Copy(rendered) else Action.ExpandSnippet(text), preview=rendered)
            }
        }
        if(q=="ayuda" || q=="comandos" || q.startsWith("ayuda ")) {
            val filter=if(q=="comandos") "" else q.removePrefix("ayuda").trim()
            CommandCenter.examples.filter { filter.isBlank() || normalize(it).contains(filter) }.forEach { example ->
                out += Result("help:$example",example,"Ejemplo · cargar y completar","Comandos",1300,Action.RestoreQuery(example))
            }
        }
        return out
    }
}
