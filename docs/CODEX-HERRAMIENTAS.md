# Herramientas y verificaciones por perfil

## Android / Codex

Activar JDK 17, SDK y Build Tools 35 conservando proxy/CA del entorno. En la instancia actual existe `/workspace/toolchains/env.sh`; fuera de ella usar las rutas configuradas del entorno. `bash .codex/scripts/doctor.sh android` comprueba herramientas sin leer secretos.

Roborazzi 1.29.0 (Apache-2.0) se eligió como experimento compatible: construido con Kotlin 1.9.22/JDK 17; 10 capturas registradas y verificadas con el Kotlin 2.0.21 y Robolectric 4.14.1 existentes. No se bajó la versión de Robolectric ni se añadieron bibliotecas visuales de producción. Las baselines se revisan y versionan; CI solo verifica.

MockWebServer 4.12.0 (Apache-2.0), coincidente con OkHttp existente, es exclusivamente testImplementation. Seis contratos HTTP reales de loopback cubren SSE, 401/429, respuesta vacía, timeout, pronóstico/caché y deportes sin marcador/vacíos. No usan DNS de proveedores ni claves reales. Los tres tests AiTransport previos también se ejecutaron. El timeout de OkHttp marcaba call.isCanceled: ahora se distingue InterruptedIOException de cancelación del usuario, conservando la aserción fallida.

Maestro 2.11.0 (Apache-2.0) requiere Java 17 mínimo. Se instala solo en el perfil Android/CI: `bash .codex/scripts/install-tool.sh maestro`. Descarga y SHA-256 fijados; no ejecuta instaladores remotos mediante pipe. `maestro.sh` deshabilita analytics y la notificación de análisis remoto; no usa Maestro Cloud ni IA para los tests.

`bash .codex/scripts/maestro.sh check-syntax <flujo>` verifica sintaxis. `bash .codex/scripts/device-checks.sh` requiere APK instalado en un dispositivo de prueba y permiso overlay vía appops; opera datos sintéticos. No ejecutar sobre datos personales sin revisar el flujo. Incluye teclado, Escape, menú, Guardados y orientación; Shift+Tab conserva su contrato Compose existente. Las capturas PDF renderizadas/carga se completan en dispositivo con fixtures, no con un shadow inventado.

MCP está implementado por Maestro (`mcp --no-viewer --working-dir ...`); plantilla en `.codex/mcp.example.toml`. Resolver rutas e incorporar el servidor al cliente Codex correspondiente; un archivo ejemplo no registra herramientas en esta sesión ni crea un emulador. Inicialización/listado de herramientas se prueba por stdio por separado de operación real de la app.

## Laboratorio visual

Directorio `visual-lab/`, Node 24 LTS, paquetes exactos/lockfile. `npm ci`, `npm run build`, `npm test`. Chromium existente puede seleccionarse con `PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH`; en CI se instala exclusivamente en este job. Seis pruebas de tablet/teléfono pasaron localmente; generan PNG/traces. Prototipo propio y referencias enlazadas, sin copiar código premium ni insertar herramientas web en Android.

## Calidad y rendimiento

- Android Lint ya obligatorio. Gitleaks 8.30.1 (MIT), binario con checksum fijo, scans de git con reporte redactado. No se añaden allowlists globales para ocultar hallazgos.
- Renovate: configuración con dashboard/approval y automerge desactivado; toolchain Android y laboratorio separados. La configuración no instala/activa la GitHub App por sí sola.
- ktlint: adoptar progresivamente sobre archivos nuevos, sin reformat masivo del legado durante recuperación. Detekt requiere demostrar checks útiles adicionales a Lint antes de agregarlo.
- LeakCanary: evaluar estable 2.14 antes de adoptar alpha 3; únicamente debug, nunca release. No se añadió aún, para no mezclar diagnósticos con snapshots/fixtures.
- Perfetto: `.codex/perfetto/spotlight.cfg` y `trace-overlay.sh`, 15 segundos de apertura/foco/animación/CPU/documentos. Requiere dispositivo; no hay trazas ni mejoras porcentuales afirmadas todavía.
- Macrobenchmark/Baseline Profiles: próximo experimento en módulo separado para invocación, foco, consultas y PDF. El overlay exige definir cuándo realmente está listo; medir percentiles y memoria antes de optimizar.

## CI y límites

`Codex development tools` separa calidad Android/Roborazzi, web, secretos y Maestro en emuladores Pixel 6/Pixel C. Ninguno recibe secretos de firma. Firma histórica permanece en flujo autorizado separado con SHA fijo y checks exigidos.

Rama de infraestructura independiente de la entrega 2.0.1; no cambia el APK/ZIP de `9bb9d0c`. No hay dispositivo local/KVM en esta instancia. Informar resultados de CI/emuladores y validación física por separado; Galaxy Tab A9+ sigue requiriendo pruebas de One UI y actualización real sin desinstalar.
