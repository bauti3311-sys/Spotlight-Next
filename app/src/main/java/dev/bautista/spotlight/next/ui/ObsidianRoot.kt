package dev.bautista.spotlight.next.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ObsidianRoot(
    currentMode: SpotlightViewMode,
    onModeChanged: (SpotlightViewMode) -> Unit
) {
    // Fondo obsidiana / grafito profundo idéntico al diseño
    val obsidianBg = Color(0xFF0E131A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(obsidianBg)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera con selector de 3 modos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Spotlight Next - Obsidian", color = Color.White)
                // Aquí se integran los modos: Sugerencias, Split Inspector y Dashboard
            }
        }
    }
}

enum class SpotlightViewMode {
    SUGGESTIONS, SPLIT_INSPECTOR, DASHBOARD
}
