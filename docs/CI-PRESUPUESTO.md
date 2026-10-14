# CI con presupuesto limitado

El usuario informó aproximadamente 198 minutos gratuitos restantes el 13 de octubre de 2026. No se consulta ni se modifica facturación, servicios pagos o medios de pago. No se afirma un saldo actualizado ni un ahorro medido.

## Ejecuciones frecuentes

`Codex development tools` ejecuta un job rápido con Python offline, Actionlint y Gitleaks. Android conserva las unitarias, lint, debug, compilación del APK de instrumentación y las 17 comparaciones nativas cuando cambian archivos Android/Gradle. El laboratorio web conserva build y Playwright cuando cambia `visual-lab/`. No arranca emuladores.

Los PR nuevos comparan contra su base. En un push posterior solo se usa el delta del push si la última ejecución del SHA anterior terminó correctamente. Si falló, sigue activa, se canceló o no se puede comprobar, se vuelve a comprobar el diff completo del PR; en push sin evidencia previa se comprueba el árbol completo. Esto evita que un cambio documental cancele pruebas pendientes y deje un verde sin evidencia. El SHA se valida y los nombres de archivos se leen con delimitadores NUL.

Los cambios exclusivamente documentales no requieren SDK o Chromium; si el PR ya contiene código sin verificar, se mantienen sus checks. Agrupar commits relacionados en un push. Publicar la evidencia final en notas Git y descripción del PR evita otro build por un commit documental.

## Validación costosa manual

| Workflow | Predeterminado | Otras opciones |
| --- | --- | --- |
| Spotlight device validation | Una tablet Android 16/API36 | `legacy-complete`: teléfono/tablet API29 y30; `complete`: esos cuatro más tablet36 |
| Native PDF Android check | Tablet36, todas las instrumentadas | API29/30, tablet/teléfono |
| Build Debug APK | Artefacto debug con unitarias/lint/Python | Solo activación manual; no entrega oficial |

La tablet36 usa 1200×1920 y densidad240 como geometría representativa; no equivale a One UI físico. Los emuladores mantienen Maestro, geometría, PDF, capturas, assertions y límites anteriores. Las etapas continúan aunque una falle y el resultado sigue rojo. La matriz manual tiene `max-parallel: 1` y `fail-fast: false`.

Para un PR que contiene esta configuración se puede activar manualmente por etiqueta, sin integrar main: `ci/android16` (una tablet), `ci/android16-trace` (esa tablet con diagnóstico), `ci/legacy` (cuatro legacy) o `ci/complete` (cinco). Solo el evento de agregar una de esas etiquetas activa dispositivos; los siguientes commits no repiten emuladores por conservar la etiqueta. Otras etiquetas no cancelan una validación en curso. No se agregaron etiquetas ni se dispararon esas pruebas en esta implementación.

`capture_trace` activa Perfetto y snapshots de WMS/InputDispatcher; por defecto está apagado. Logcat de arranque y diagnóstico del fallo nativo se conservan. Los workflows de dispositivos y PDF comparten cancelación por ref, evitando dos validaciones costosas simultáneas. Los workflows rápidos también cancelan ejecuciones obsoletas de su ref.

Gradle usa build-cache y permite escribir caché en estas ramas; Node conserva la caché npm. Se cachean CLI con versiones y SHA-256 fijados. No se cachean AVD, aplicaciones instaladas ni datos de tests: se conserva el arranque frío y el aislamiento. Actionlint1.7.12 es una herramienta MIT de desarrollo/CI, fuera del APK, verificada por SHA-256.

## Evidencia y límites

Una CI rápida verde no equivale a validación de emulador, One UI o release. F1.3 continúa abierto: el CI38488258740 falló apertura fría en API30 y36. Android16 ejecutó correctamente geometría y los tres PDF, pero Maestro frío falló. La tablet30 mostró una discrepancia entre visibilidad IME en accesibilidad y WindowMetrics. El checkpoint de animaciones43c8723 está compilado, todavía no ejecutado en dispositivo.

El PR de configuración se apila sobre `test/native-pdf-api36-f13` (43c8723): el diff muestra solo infraestructura y no incorpora F1.3 a otra rama. El ancestro de infraestructura se conserva. Así la apertura del PR no recompila código Android heredado sin cambios.

La optimización se prueba con Actionlint, tests Python de selección/cancelación y revisión de eventos. No se lanza una matriz para probarla. Para la siguiente corrección F1.3, usar primero validación local y luego una ejecución Android16 dirigida cuando corresponda; reservar legacy/completa para cierre o cambios de compatibilidad. No declarar ese cierre con checks manuales ausentes o rojos.

La configuración se prepara en un PR borrador sin merges. Afecta a la CI del head que la contiene; las otras ramas conservan sus definiciones hasta incorporar explícitamente estos commits. Para continuar, partir de esta rama de infraestructura económica, no empujar a PR12 con los workflows antiguos. Los nuevos workflows con Run workflow pueden requerir incorporación autorizada a la rama predeterminada para aparecer en esa interfaz; las etiquetas proporcionan activación manual sobre este PR mientras tanto. No integrar main automáticamente para habilitarlos.

La firma y publicación protegidas no se modifican ni se ejecutan. `checkpoint/autonomous-next` sigue en e38eae8: la rama de trabajo contiene F1.3 pendiente y no debe marcarse como checkpoint completo validado.
