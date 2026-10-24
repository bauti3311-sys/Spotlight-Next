package dev.bautista.spotlight.control

import android.content.Context

/** Consent only, not an engine switch. No session, socket, transport factory or AI entry point. */
class ExperimentalMessagingPreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("whatsapp-experimental-consent", Context.MODE_PRIVATE)
    val allowed: Boolean get() = preferences.getInt("consent-version", 0) == 1
    fun confirmFromUi(risksAccepted: Boolean, privacyAccepted: Boolean) {
        require(risksAccepted && privacyAccepted) { "Revisá y aceptá ambos avisos antes de continuar." }
        check(preferences.edit().putInt("consent-version", 1).commit()) { "No se pudo guardar el consentimiento." }
    }
    fun revoke() { check(preferences.edit().clear().commit()) { "No se pudo retirar el consentimiento." } }
}
