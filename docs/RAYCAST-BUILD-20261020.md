# Raycast Preview — compilación local 20/10/2026

Base recuperada: e77a1c75f4d82d922fa6f7fe0c5f080b450913fc, rama feat/raycast-sidebar-permissions-20261020 (PR18 sobre PR17). Los cuatro archivos pendientes de la copia anterior coinciden exactamente con esta base; la copia anterior se conserva intacta. Reserva propia actualizada por CAS, sin force ni cambios de ramas anteriores.

Entorno restaurado: Temurin17.0.20.1+1, Gradle8.10.2, SDK35/build-tools35.0.0. Dependencias y Roborazzi1.29.0 conservados. Compilación en copia temporal fuera de sincronización. Sin Actions.

Primera ejecución real: `:app:compileRaycastPreviewKotlin`, fallo Kotlin en SpotlightPanel.kt:889 (maxWidth, receptor implícito oculto). Corrección mínima: capturar disponibilidad de ancho en BoxWithConstraints y usarla en selección de apps. Sin cambios de umbral, Quick Look ni diseño. **Checkpoint parcial: corrección aún no verificada.**

Pendientes: recompilar, pruebas dirigidas de UI/Phone Use/herramientas/multimedia/PDF, lint, assembleRaycastPreview, verificar APK y ZIP del mismo SHA. ADB sin dispositivos; /dev/kvm ausente. Validación física One UI pendiente.

## Primera ronda dirigida
Kotlin/Java y código de tests compilados. 65 casos/14 suites: 44 correctos, 21 fallidos. Veinte fallos por actividad ComponentActivity no declarada para raycastPreview; un fallo por JSONArray Android ejecutado como JVM sin Robolectric. Corrección de configuración: ui-test-manifest en la variante debug independiente y runner Robolectric SDK33 para PhoneUseSequencePolicyTest. Versiones y aserciones intactas. Validador ampliado a las 14 suites solicitadas. Checkpoint parcial, reejecución pendiente.

## Reanudación 20/10 — entorno de Work indisponible

HEAD remoto comprobado: `8d695c4bca804cb24c28b5ba701d6adae9993fb7`. Correcciones publicadas: ámbito de maxWidth, manifest anfitrión de Compose para raycastPreview y runner Robolectric de PhoneUseSequencePolicyTest. La segunda ronda de las mismas 14 suites se inició en la sesión anterior, pero **su resultado no está recuperado ni verificado**.

Al reanudar, el entorno figura unavailable y exec_command responde: "No usable execution environment is available. Wait for an environment to become available before using this tool." No es un fallo nuevo del código ni de Gradle. No se puede consultar el proceso local, logs, XML ni APK. Ninguna ejecución en segundo plano está acreditada. GitHub sigue disponible. No se lanzó GitHub Actions ni se consumió cuota de CI.

Última evidencia confirmada: producción y pruebas compilan; primera ronda 65 casos, 44 correctos y 21 fallidos por configuración documentada arriba. No declarar 65/65 ni lint aprobado. APK y ZIP de entrega siguen pendientes; no hay validación física One UI.

### Recuperación exacta cuando vuelva el entorno
1. Consultar HEAD y reserva antes de editar; propietario propio work-recovery-20261018-cd7b8fe1285c. Conservar cambios concurrentes.
2. Revisar primero el log `/workspace/scratch/cd7b8fe1285c/raycast-tests-final.log` y XML en `/tmp/spotlight-raycast-build/app/build/test-results/testRaycastPreviewUnitTest/`. Comprobar el proceso real antes de lanzar otra ejecución.
3. Si no sobrevivieron resultados, restaurar las mismas versiones y ejecutar las 14 suites de `scripts/validate-raycast-sidebar.sh`; corregir fallos reales sin omitir aserciones.
4. Ejecutar `:app:lintRaycastPreview` y `:app:assembleRaycastPreview`, verificar package `dev.bautista.spotlight.next.raycastpreview`, apksigner, zipalign y hashes, entregar APK + ZIP fuente del mismo commit. No incluir claves, cachés ni credenciales.
5. Conservar firma principal/Review. Sin merge, force-push o CI no autorizado. Continuar Phone Use/integraciones solo después de cerrar esta entrega instalable.
