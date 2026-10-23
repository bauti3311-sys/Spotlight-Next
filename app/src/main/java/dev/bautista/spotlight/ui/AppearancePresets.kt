package dev.bautista.spotlight.ui
import dev.bautista.spotlight.data.Config

object AppearancePresets {
    val names=listOf("Solid Graphite","Dark Glass","Minimal Pure")
    fun apply(name:String,c:Config):Config=when(name){
        "Spotlight Hybrid"->c.copy(preset="hybrid",theme="auto",appearance="spotlight",opacity=1f,corner=20,blur=false)
        "Solid Graphite","Raycast Dark","Raycast Graphite"->c.copy(preset="raycast",theme="dark",appearance="raycast",accent="blue",density="balanced",opacity=.98f,actionOpacity=1f,settingsOpacity=.99f,corner=16,maxHeight=520,blur=false)
        "Dark Glass"->c.copy(preset="glass",theme="dark",opacity=.84f,actionOpacity=.92f,settingsOpacity=.94f)
        "Minimal Pure"->c.copy(preset="pure",theme="auto",appearance="spotlight",accent="graphite",density="compact",opacity=1f,corner=14,glassIntensity=0f,shadowSize=2f,blur=false)
        "iOS Light"->c.copy(preset="ios",theme="light",appearance="spotlight",accent="blue",density="balanced",opacity=.98f,corner=24,blur=true)
        "Minimal Graphite"->c.copy(preset="graphite",theme="dark",appearance="raycast",accent="graphite",density="compact",opacity=1f,corner=14,blur=false)
        else->c.copy(preset="glass",theme="auto",appearance="spotlight",accent="blue",density="balanced",opacity=.95f,corner=22,blur=true)
    }
}
