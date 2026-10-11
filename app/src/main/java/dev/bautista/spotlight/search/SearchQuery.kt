package dev.bautista.spotlight.search

import java.time.*

/** Exact phrases and uppercase boolean operators; implicit AND, NOT > AND > OR. */
class SearchQuery
private constructor(private val root: Node, val problem: String?, val plain: String) {
    data class Document(
        val name: String,
        val kind: String,
        val mime: String = "",
        val text: String = "",
        val author: String = "",
        val modified: Long = 0,
        val created: Long = 0,
    )

    private sealed interface Node {
        fun matches(d: Document): Boolean
    }

    private data class Term(val value: String) : Node {
        override fun matches(d: Document): Boolean {
            val decoded = value.replace("\u0001", "")
            val key = decoded.substringBefore(':', "")
            val v = normalize(decoded.substringAfter(':', decoded))
            return when (key.lowercase()) {
                "kind",
                "tipo" ->
                    when (v) {
                        "app",
                        "apps",
                        "applications",
                        "aplicaciones" -> d.kind == "app"
                        "clipboard",
                        "portapapeles" -> d.kind == "clipboard"
                        "pdf" -> d.mime == "application/pdf" || d.name.endsWith(".pdf", true)
                        "image",
                        "images",
                        "imagen",
                        "imagenes",
                        "foto",
                        "fotos" -> d.kind == "image"
                        "folders",
                        "folder",
                        "carpetas",
                        "carpeta" -> d.kind == "folder"
                        "email",
                        "correo" -> d.kind == "email"
                        "audio" -> d.kind == "audio"
                        "video" -> d.kind == "video"
                        "notes",
                        "note",
                        "notas",
                        "nota" -> d.kind == "note"
                        "message",
                        "mensajes" -> d.kind == "message"
                        "document",
                        "documents",
                        "documento",
                        "documentos" -> d.kind == "document" || d.kind == "email"
                        else -> d.name.lowercase().endsWith(".$v") || normalize(d.mime) == v
                    }
                "author",
                "autor" -> normalize(d.author).contains(v)
                "name",
                "nombre" -> normalize(d.name).contains(v)
                "modified",
                "modificado",
                "date",
                "fecha" -> date(d.modified, v)
                "created",
                "creado" -> date(d.created, v)
                "after",
                "despues" -> compareDate(d.modified, v, true)
                "before",
                "antes" -> compareDate(d.modified, v, false)
                else -> normalize(d.name + "\n" + d.text).contains(normalize(decoded))
            }
        }

        private fun date(timestamp: Long, value: String): Boolean {
            if (timestamp <= 0) return false
            val day = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            return when (value) {
                "hoy",
                "today" -> day == LocalDate.now()
                "ayer",
                "yesterday" -> day == LocalDate.now().minusDays(1)
                else -> day.toString().startsWith(value)
            }
        }

        private fun compareDate(timestamp: Long, value: String, after: Boolean): Boolean {
            if (timestamp <= 0) return false
            val edge =
                runCatching {
                        (if (value.contains('t'))
                                LocalDateTime.parse(value.uppercase())
                                    .atZone(ZoneId.systemDefault())
                            else LocalDate.parse(value).atStartOfDay(ZoneId.systemDefault()))
                            .toInstant()
                            .toEpochMilli()
                    }
                    .getOrNull() ?: return false
            return if (after) timestamp >= edge else timestamp < edge
        }
    }

    private data class And(val a: Node, val b: Node) : Node {
        override fun matches(d: Document) = a.matches(d) && b.matches(d)
    }

    private data class Or(val a: Node, val b: Node) : Node {
        override fun matches(d: Document) = a.matches(d) || b.matches(d)
    }

    private data class Not(val a: Node) : Node {
        override fun matches(d: Document) = !a.matches(d)
    }

    private object All : Node {
        override fun matches(d: Document) = true
    }

    fun matches(d: Document) = problem == null && root.matches(d)

    companion object {
        fun parse(input: String): SearchQuery {
            if (input.length > 4000) return SearchQuery(All, "Consulta demasiado larga", "")
            val tokens = mutableListOf<String>()
            var i = 0
            while (i < input.length) {
                if (input[i].isWhitespace()) {
                    i++
                    continue
                }
                if (input[i] == '(' || input[i] == ')') {
                    tokens += input[i++].toString()
                    continue
                }
                val part = StringBuilder()
                var quoted = false
                while (
                    i < input.length &&
                        (quoted || (!input[i].isWhitespace() && input[i] != '(' && input[i] != ')'))
                ) {
                    if (input[i] == '"') {
                        if (!quoted) part.append('\u0001')
                        quoted = !quoted
                    } else part.append(input[i])
                    i++
                }
                if (quoted) return SearchQuery(All, "Falta cerrar las comillas", "")
                if (part.isNotEmpty()) tokens += part.toString()
                if (tokens.size > 160) return SearchQuery(All, "Demasiados términos", "")
            }
            var pos = 0
            var depth = 0
            fun unary(parseOr: () -> Node): Node {
                require(++depth <= 32)
                val result =
                    when {
                        pos >= tokens.size -> error("Falta un término")
                        tokens[pos] == "NOT" -> {
                            pos++
                            Not(unary(parseOr))
                        }
                        tokens[pos] == "(" -> {
                            pos++
                            val n = parseOr()
                            require(tokens.getOrNull(pos++) == ")")
                            n
                        }
                        tokens[pos] in setOf("AND", "OR", ")") -> error("Operador sin término")
                        else -> Term(tokens[pos++])
                    }
                depth--
                return result
            }
            lateinit var parseOr: () -> Node
            fun parseAnd(): Node {
                var a = unary { parseOr() }
                while (pos < tokens.size && tokens[pos] != "OR" && tokens[pos] != ")") {
                    if (tokens[pos] == "AND") pos++
                    a = And(a, unary { parseOr() })
                }
                return a
            }
            parseOr = {
                var a = parseAnd()
                while (tokens.getOrNull(pos) == "OR") {
                    pos++
                    a = Or(a, parseAnd())
                }
                a
            }
            return try {
                val n = if (tokens.isEmpty()) All else parseOr()
                require(pos == tokens.size)
                SearchQuery(
                    n,
                    null,
                    tokens
                        .filterNot { it in setOf("AND", "OR", "NOT", "(", ")") || it.contains(':') }
                        .joinToString(" ")
                        .replace("\u0001", ""),
                )
            } catch (e: Exception) {
                SearchQuery(All, "Revisá los operadores y los paréntesis", "")
            }
        }

        fun natural(input: String): String {
            var s = normalize(input.trim()).removePrefix("buscar ")
            if(s.startsWith("pdf ")){val rest=s.removePrefix("pdf ").removePrefix("de ");return "kind:pdf "+if(rest in setOf("ayer","hoy"))"modified:$rest" else rest}
            if(s.startsWith("archivos de "))return s.removePrefix("archivos de ")
            if (
                listOf(
                        "fotos de ",
                        "imagenes de ",
                        "foto de ",
                        "imagen de ",
                        "videos de ",
                        "video de ",
                    )
                    .any { s.startsWith(it) }
            ) {
                if(s.substringAfter(" de ") in setOf("ayer","hoy")) return (if(s.startsWith("video")) "kind:video " else "kind:image ")+"created:"+s.substringAfter(" de ")
                val stop = setOf("en", "la", "el", "las", "los", "una", "un", "con", "y", "de")
                return (if (s.startsWith("video")) "kind:video " else "kind:image ") +
                    s.substringAfter(" de ").split(' ').filter { it !in stop }.joinToString(" ")
            }
            if (s.contains("ayer") && (s.startsWith("documento") || s.startsWith("archivo")))
                return if (s.contains("tarde"))
                    "kind:document after:${LocalDate.now().minusDays(1)}T12:00 before:${LocalDate.now().minusDays(1)}T20:00"
                else "kind:document modified:ayer"
            return input
        }
    }
}
