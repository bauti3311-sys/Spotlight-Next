package dev.bautista.spotlight.ui

import androidx.compose.runtime.staticCompositionLocalOf

/** Flush the existing saved-state registry before a confirmed external change. */
internal val LocalTaskCheckpoint=staticCompositionLocalOf<suspend ()->Unit> { {} }
