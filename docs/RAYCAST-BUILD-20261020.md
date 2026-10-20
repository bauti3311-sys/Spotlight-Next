# Raycast Preview — compilación local 20/10/2026

Base recuperada: e77a1c75f4d82d922fa6f7fe0c5f080b450913fc, rama feat/raycast-sidebar-permissions-20261020 (PR18 sobre PR17). Los cuatro archivos pendientes de la copia anterior coinciden exactamente con esta base; la copia anterior se conserva intacta. Reserva propia actualizada por CAS, sin force ni cambios de ramas anteriores.

Entorno restaurado: Temurin17.0.20.1+1, Gradle8.10.2, SDK35/build-tools35.0.0. Dependencias y Roborazzi1.29.0 conservados. Compilación en copia temporal fuera de sincronización. Sin Actions.

Primera ejecución real: `:app:compileRaycastPreviewKotlin`, fallo Kotlin en SpotlightPanel.kt:889 (maxWidth, receptor implícito oculto). Corrección mínima: capturar disponibilidad de ancho en BoxWithConstraints y usarla en selección de apps. Sin cambios de umbral, Quick Look ni diseño. **Checkpoint parcial: corrección aún no verificada.**

Pendientes: recompilar, pruebas dirigidas de UI/Phone Use/herramientas/multimedia/PDF, lint, assembleRaycastPreview, verificar APK y ZIP del mismo SHA. ADB sin dispositivos; /dev/kvm ausente. Validación física One UI pendiente.
