package dev.bautista.spotlight.ui

internal enum class RelativePlanSetting { VOLUME, BRIGHTNESS }
internal data class RelativePlanValue(val setting: RelativePlanSetting, val delta: Int, val baseline: Int)

/** Pure preview: omitted/unavailable steps never affect a later relative target.
 * Keep the original baseline so checkbox changes do not accumulate deltas.
 * The existing executor receives only these explicitly reviewed absolute calls. */
internal fun projectPlanRows(rows: List<ReviewedStep>, selected: Set<Int>): List<ReviewedStep> {
    var volume: Int? = null
    var brightness: Int? = null
    return rows.mapIndexed { index, row ->
        val relative = row.relative
        val projected = if (relative != null && row.call != null) {
            val base = when (relative.setting) {
                RelativePlanSetting.VOLUME -> volume
                RelativePlanSetting.BRIGHTNESS -> brightness
            } ?: relative.baseline
            val value = (base + relative.delta).coerceIn(
                if (relative.setting == RelativePlanSetting.BRIGHTNESS) 8 else 0, 100)
            val isVolume = relative.setting == RelativePlanSetting.VOLUME
            row.copy(
                title = "${if (isVolume) "Volumen" else "Brillo"} ${if (relative.delta > 0) "+" else ""}${relative.delta}% → $value%",
                call = row.call.copy(arguments = if (isVolume) mapOf("percent" to value.toString())
                    else mapOf("key" to "screen_brightness", "value" to (value * 255 / 100).toString())))
        } else row
        if (index in selected) projected.call?.let { call ->
            when (call.name) {
                "set_volume" -> volume = call.arguments["percent"]?.toIntOrNull()
                "write_display_setting" -> if (call.arguments["key"] == "screen_brightness") {
                    // Inverse of the rounded-down raw brightness representation.
                    brightness = call.arguments["value"]?.toIntOrNull()?.let { (it * 100 + 254) / 255 }
                }
            }
        }
        projected
    }
}
