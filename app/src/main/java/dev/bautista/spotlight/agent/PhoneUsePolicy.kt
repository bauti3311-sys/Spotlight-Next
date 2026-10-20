package dev.bautista.spotlight.agent

/** Strict exact-match selectors; never accept coordinates or inferred packages. */
internal object PhoneUsePolicy {
    private val packagePattern = Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)+")
    private val resourcePattern = Regex("[A-Za-z_][A-Za-z0-9_.]*:id/[A-Za-z_][A-Za-z0-9_]*")
    private val blocked = setOf(
        "com.android.systemui", "com.android.permissioncontroller",
        "com.google.android.permissioncontroller", "com.google.android.packageinstaller",
        "com.android.packageinstaller"
    )

    fun allowPackage(actual: String, expected: String, host: String): Boolean =
        expected.matches(packagePattern) &&
            actual == expected && actual != host && actual !in blocked &&
            actual != "android" && !actual.startsWith("dev.bautista.spotlight.")

    fun allowObservation(actual: String, host: String): Boolean =
        allowPackage(actual, actual, host)

    sealed interface Selector {
        data class Id(val value: String) : Selector
        data class Label(val value: String) : Selector
        data class Description(val value: String) : Selector
    }

    fun parseSelector(raw: String): Selector {
        require(raw.length in 5..180 && !raw.contains('\n') && !raw.contains('\u0000')) { "Selector inválido." }
        return when {
            raw.startsWith("id:") -> {
                val id = raw.removePrefix("id:")
                require(id.matches(resourcePattern)) { "El ID de control es inválido." }
                Selector.Id(id)
            }
            raw.startsWith("text:") -> Selector.Label(raw.removePrefix("text:").also { require(it.isNotBlank()) })
            raw.startsWith("desc:") -> Selector.Description(raw.removePrefix("desc:").also { require(it.isNotBlank()) })
            else -> throw IllegalArgumentException("Usá id:, text: o desc:; no se admiten coordenadas o coincidencias difusas.")
        }
    }

    fun allowText(value: String): Boolean = value.isNotBlank() && value.length <= 1600 && '\u0000' !in value

    fun compactLabel(value: CharSequence?): String? = value?.toString()?.trim()?.takeIf { it.isNotBlank() }
        ?.replace(Regex("[\\r\\n\\t]+"), " ")?.take(65)
}
