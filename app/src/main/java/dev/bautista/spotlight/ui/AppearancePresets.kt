package dev.bautista.spotlight.ui
import dev.bautista.spotlight.data.Config

object AppearancePresets {
    val names=listOf("Spotlight Hybrid","macOS Glass","Raycast Dark","iOS Light","Minimal Graphite")
    fun apply(name:String,c:Config):Config=when(name){
        "Spotlight Hybrid"->c.copy(preset="hybrid",theme="auto",appearance="spotlight",opacity=1f,corner=20,blur=false)
        "Raycast Dark"->c.copy(preset="raycast",theme="dark",appearance="raycast",accent="purple",density="compact",opacity=.98f,corner=16,maxHeight=520,blur=false)
        "iOS Light"->c.copy(preset="ios",theme="light",appearance="spotlight",accent="blue",density="balanced",opacity=.98f,corner=24,blur=true)
        "Minimal Graphite"->c.copy(preset="graphite",theme="dark",appearance="raycast",accent="graphite",density="compact",opacity=1f,corner=14,blur=false)
        else->c.copy(preset="macos",theme="auto",appearance="spotlight",accent="blue",density="balanced",opacity=.95f,corner=22,blur=true)
    }
}
