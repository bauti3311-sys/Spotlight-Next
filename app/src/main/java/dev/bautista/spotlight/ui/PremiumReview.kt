package dev.bautista.spotlight.ui

import androidx.compose.runtime.staticCompositionLocalOf

/** Review-only opt-in. No saved preference, no launcher action, no change to shipped defaults.
 * The owner reviews real Compose comparisons before adopting material composition changes. */
internal val LocalPremiumReview = staticCompositionLocalOf { false }
