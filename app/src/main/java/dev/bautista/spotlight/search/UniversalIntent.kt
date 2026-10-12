package dev.bautista.spotlight.search

/** Local, side-effect-free interpretation. Resolution reuses the existing providers. */
sealed interface UniversalIntent {
    data class OpenApp(val name: String) : UniversalIntent
    data class Photos(val query: String) : UniversalIntent
    data class Information(val query: String) : UniversalIntent
    data class Volume(val percent: Int) : UniversalIntent
    data class InvalidLevel(val device: String) : UniversalIntent

    companion object {
        fun recognize(raw: String): UniversalIntent? {
            if (raw.length > 4000) return null
            val text = raw.trim().replace(Regex("\\s+"), " ")
            val q = LocalLanguage.canonical(text)
            Regex("^(?:poner |ajustar |cambiar )?(?:el )?(volumen|brillo)(?: al| a)? (-?\\d{1,9})\\s*%?$").matchEntire(q)?.let {
                val level = it.groupValues[2].toIntOrNull()
                if (level == null || level !in 0..100) return InvalidLevel(it.groupValues[1])
                if (it.groupValues[1] == "volumen") return Volume(level)
                // Brightness uses the existing reversible, confirmed Shizuku action.
                return null
            }
            Regex("^(?:abrir|iniciar|abr[ií]|abre) (.+)$", RegexOption.IGNORE_CASE).matchEntire(text)?.let { return OpenApp(it.groupValues[1]) }
            Regex("^(?:buscar|busca|mostrame) (?:fotos|imagenes) de (.+)$").matchEntire(q)?.let {
                return Photos(SearchQuery.natural("fotos de " + it.groupValues[1]))
            }
            Regex("^(?:buscar informaci[oó]n sobre|buscar informaci[oó]n|buscar sobre|investigar sobre) (.+)$", RegexOption.IGNORE_CASE).matchEntire(text)?.let {
                val term = it.groupValues[1].replace(Regex("^(?:informaci[oó]n sobre|informaci[oó]n|sobre) +", RegexOption.IGNORE_CASE), "")
                if (term.isNotBlank()) return Information(term)
            }
            return null
        }
    }
}
