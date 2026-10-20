# Raycast Preview — compilación local 20/10/2026

Base recuperada: e77a1c75f4d82d922fa6f7fe0c5f080b450913fc, rama feat/raycast-sidebar-permissions-20261020 (PR18 sobre PR17). Los cuatro archivos pendientes de la copia anterior coinciden exactamente con esta base; la copia anterior se conserva intacta. Reserva propia actualizada por CAS, sin force ni cambios de ramas anteriores.

Entorno restaurado: Temurin17.0.20.1+1, Gradle8.10.2, SDK35/build-tools35.0.0. Dependencias y Roborazzi1.29.0 conservados. Compilación en copia temporal fuera de sincronización. Sin Actions.

Primera ejecución real: `:app:compileRaycastPreviewKotlin`, fallo Kotlin en SpotlightPanel.kt:889 (maxWidth, receptor implícito oculto). Corrección mínima: capturar disponibilidad de ancho en BoxWithConstraints y usarla en selección de apps. Sin cambios de umbral, Quick Look ni diseño. **Checkpoint parcial: corrección aún no verificada.**

Pendientes: recompilar, pruebas dirigidas de UI/Phone Use/herramientas/multimedia/PDF, lint, assembleRaycastPreview, verificar APK y ZIP del mismo SHA. ADB sin dispositivos; /dev/kvm ausente. Validación física One UI pendiente.

## Primera ronda dirigida
Kotlin/Java y código de tests compilados. 65 casos/14 suites: 44 correctos, 21 fallidos. Veinte fallos por actividad ComponentActivity no declarada para raycastPreview; un fallo por JSONArray Android ejecutado como JVM sin Robolectric. Corrección de configuración: ui-test-manifest en la variante debug independiente y runner Robolectric SDK33 para PhoneUseSequencePolicyTest. Versiones y aserciones intactas. Validador ampliado a las 14 suites solicitadas. Checkpoint parcial, reejecución pendiente.
