# Entorno avanzado aprobado de Spotlight Next

Definición proporcionada por el usuario el 2026-10-13. Complementa `RENOVACION-APROBADA.md`; no sustituye su contrato funcional. Incorporación gradual, con commits y pruebas, sin mezclar dependencias masivas con la recuperación 2.0.1.

## Tres áreas

| Área | Herramientas y alcance |
| --- | --- |
| Codex Android | JDK 17, SDK/Build Tools 35, Gradle wrapper, Kotlin/Compose; Lint, calidad, instrucciones y skills. Maestro, capturas y perfiles según capacidad del dispositivo. |
| Laboratorio visual secundario | Node.js LTS, React, Vite, TypeScript, Playwright y Chromium. Referencias/animaciones ejecutables; sin dependencias web dentro del APK. |
| GitHub Actions | Checks por PR, emuladores teléfono/tablet, informes/capturas/diffs, secretos, firma histórica y publicación conjunta desde el mismo SHA autorizado. |

Base actual: AGP 8.7.3, Kotlin/Compose compiler 2.0.21, Compose BOM 2024.12.01, minSdk 26, compile/target 35. Compatibilidad nueva debe comprobarse contra esta base; no forzar actualización conjunta de Kotlin, Compose o Gradle.

## Evaluación visual nativa

Evaluar Compose Unstyled (controles tipo Raycast), Haze (cristal/blur), AndroidLiquidGlass (distorsión sutil), MaterialKolor (paletas), Lumo UI (referencias/controles), Vico (clima/estadísticas), Kizitonwose Calendar (calendario) y Markdown Compose nativo con listas, tablas, enlaces y código.

Para cada candidato: repositorio exacto, última versión observada y fecha, licencia/SPDX, mantenimiento, SDK/Kotlin/Compose requeridos, dependencias transitivas, alternativa/fallback y experimento de rendimiento. Un nombre de biblioteca no basta para resolver una licencia o aprobar su uso. No instalar todos los candidatos ni introducirlos automáticamente en release.

## Referencias del laboratorio

- Barra: Arc UI Command Palette; EasyUI Command Menu, Expandable Search y Glass Navbar.
- Selección: Motion Primitives Animated Background; MicroKit Spotlight Indicator.
- Menús: Aceternity Gooey Dropdown; SmoothUI Gooey Popover.
- Panel lateral: Arc UI Resizable Panels; Motion Primitives Morphing Dialog; Aceternity Expandable Card y Notch.
- Iconos: Hugeicons Stroke Rounded exclusivamente con licencia compatible; Phosphor Regular/Duotone.

Ejecutar y observar referencias cuando sea legal y accesible; recrear sus patrones en Compose. No copiar recursos premium/sin licencia ni añadir React/Vite al APK.

## Pruebas y diagnóstico

- Maestro: invocación/cierre, consultas, selección, Enter/Tab, Quick Look, panel lateral, menús y orientación; evaluar MCP solo si hay servidor/capacidades reales. ADB/overlay y teclado físico pueden requerir complemento instrumentado.
- Roborazzi: capturas de barra, resultados/selección, lateral, PDF, menú, Guardados, Ajustes y carga/error; baselines versionadas y revisión de diffs. No aceptar cambios de baseline para ocultar errores.
- LeakCanary: solo debug para OverlayService, apertura/cierre, PDF/bitmaps y Compose. Perfetto para apertura, foco, hilo principal, CPU, animación/documentos. Investigar Macrobenchmark/Baseline Profiles antes de incorporar módulos.
- MockWebServer: solo test; IA, HTTP/timeout/cuota/vacío, deportes y clima con fixtures reproducibles, sin claves reales ni costos.
- Calidad: Lint ya obligatorio; ktlint progresivo; Detekt solo si añade checks útiles; Gitleaks; Renovate con versiones críticas agrupadas/manuales y automerge desactivado.

Emuladores de tablet/teléfono cuando sea posible. Galaxy Tab A9+ requiere validación física de One UI, orientación, teclado físico/virtual, overlay/foco, gestos, transparencia/blur, memoria, PDF y escalado. No inferir estas verificaciones de un emulador.

## Entrega automatizada

APK firmado únicamente en flujo autorizado/protegido y sin secretos en PRs. Checks completos antes de firmar, identidad/certificado históricos, ZIP del mismo commit, hashes e informe. Publicación conjunta en GitHub Releases u otro mecanismo privado confiable después de aprobar la versión. No auto-merge de fallos ni quitar controles.

Orden: recuperación/entrega 2.0.1; AGENTS/PLANS/skills; pruebas visuales/funcionales; laboratorio/evaluación Compose; automatización general. Las herramientas costosas no se instalan innecesariamente en todos los entornos.

### Work15/10: sincronización y extracción de herramientas
Se observó packageDebug fallando por `.rsync-tmp/classes.dex` transitorio dentro de build durante la sincronización de Work. Para los gates se copió el mismo árbol fuente, sin build/.gradle/.rsync-tmp, a un temporal fuera de la raíz sincronizada. Se reutilizaron JDK17/SDK35/Gradle8.10.2 y dependencias verificadas; no se rebajaron controles. Guardar informes y comprobar hashes de entradas de producción antes de asociar el APK a un SHA.

Tar verificó los SHA256 de actionlint/Gitleaks pero falló restaurando propietarios Unix inexistentes en Work. `--no-same-owner` evita ese error conservando archivos/modos; no cambia versiones, URLs ni hashes. ShellCheck del script corregido pasó. La desconexión posterior impidió confirmar los últimos reintentos de instalación y la entrega. Si el entorno informa409 environment_offline, no asumir que una tarea en curso terminó correctamente: comprobar logs/artefactos al reconectar.
