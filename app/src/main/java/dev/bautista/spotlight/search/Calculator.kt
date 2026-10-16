package dev.bautista.spotlight.search

import kotlin.math.*

/** Bounded recursive descent parser: never evaluates code or uses a JavaScript engine. */
object Calculator {
    fun evaluate(input: String): String? =
        runCatching {
                if (input.length > 240) return null
                // The search bar is Spanish-first: accept short natural requests and
                // Argentine grouped thousands without affecting ordinary calculations.
                var s =
                    input
                        .trim()
                        .lowercase()
                        .trim('¿', '?', ' ', '\t', '\n')
                        .replace(Regex("^(?:cu[aá]nto es|calcul[aá]|calcular)\\s+"), "")
                        .replace("×", "*")
                        .replace("÷", "/")
                        // A leading zero denotes a fraction (0.125), never a thousands group.
                        .replace(Regex("(?<![\\w.])[1-9]\\d{0,2}(?:\\.\\d{3})+(?:,\\d+)?(?![\\w.])")) { match ->
                            match.value.replace(".", "")
                        }
                        .replace(",", ".")
                        .replace("√", "sqrt")
                        .replace("π", "pi")
                val percent = Regex("^([0-9.]+)\\s*%\\s+de\\s+([0-9.]+)$").matchEntire(s)
                if (percent != null) s = "${percent.groupValues[1]}/100*${percent.groupValues[2]}"
                if (
                    (!s.any { it.isDigit() } && !s.contains("pi") && !s.contains("e")) ||
                        (!s.any { it in "+-*/^%()" } &&
                            !listOf(
                                    "sqrt",
                                    "sin",
                                    "cos",
                                    "tan",
                                    "asin",
                                    "acos",
                                    "atan",
                                    "ln",
                                    "log",
                                    "abs",
                                    "exp",
                                )
                                .any { s.startsWith(it) } &&
                            s != "pi" &&
                            s != "e")
                )
                    return null
                val p = Parser(s.replace(" ", ""))
                val result = p.expression()
                require(p.done() && result.isFinite())
                java.math.BigDecimal.valueOf(result).stripTrailingZeros().toPlainString()
            }
            .getOrNull()

    private class Parser(val s: String) {
        var i = 0

        fun done() = i == s.length

        fun eat(c: Char): Boolean =
            if (i < s.length && s[i] == c) {
                i++
                true
            } else false

        fun expression(): Double {
            var v = term()
            while (true) v =
                when {
                    eat('+') -> v + term()
                    eat('-') -> v - term()
                    else -> return v
                }
        }

        fun term(): Double {
            var v = unary()
            while (true) v =
                when {
                    eat('*') -> v * unary()
                    eat('/') -> {
                        val d = unary()
                        require(d != 0.0)
                        v / d
                    }
                    else -> return v
                }
        }

        fun unary(): Double =
            when {
                eat('+') -> unary()
                eat('-') -> -unary()
                else -> power()
            }

        fun power(): Double {
            var v = atom()
            if (eat('^')) v = v.pow(unary())
            while (eat('%')) v /= 100
            return v
        }

        fun atom(): Double {
            val functions =
                listOf(
                    "asin",
                    "acos",
                    "atan",
                    "sqrt",
                    "sind",
                    "cosd",
                    "tand",
                    "sin",
                    "cos",
                    "tan",
                    "log",
                    "abs",
                    "exp",
                    "ln",
                )
            for (name in functions) if (s.startsWith(name, i)) {
                i += name.length
                val value = atom()
                return when (name) {
                    "sind" -> sin(Math.toRadians(value))
                    "cosd" -> cos(Math.toRadians(value))
                    "tand" -> tan(Math.toRadians(value))
                    "sin" -> sin(value)
                    "cos" -> cos(value)
                    "tan" -> tan(value)
                    "asin" -> asin(value)
                    "acos" -> acos(value)
                    "atan" -> atan(value)
                    "sqrt" -> sqrt(value)
                    "ln" -> ln(value)
                    "log" -> log10(value)
                    "abs" -> abs(value)
                    else -> exp(value)
                }
            }
            if (s.startsWith("pi", i)) {
                i += 2
                return PI
            }
            if (s.startsWith("e", i)) {
                i++
                return E
            }
            if (eat('(')) {
                val v = expression()
                require(eat(')'))
                return v
            }
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
            require(i > start)
            return s.substring(start, i).toDouble()
        }
    }
}
