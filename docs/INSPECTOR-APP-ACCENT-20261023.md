# Inspector con acento por aplicación · 23/10/2026

Implementación en la app Android real, con Kotlin y Jetpack Compose. **Código verificado: `821ea952b4925499de49f94a4300a1f58946b150`.** La documentación y las capturas posteriores no cambian las entradas compiladas. No se generó una APK nueva en este bloque.

## Cambios

- Se conserva la base Dark Glass, la selección gris, la proporción adaptable del inspector y el ActionPanel superpuesto. El inspector añade un tinte suave y aplica el acento al botón principal y al progreso multimedia.
- Spotify, WhatsApp, Gmail, Chrome y YouTube usan una paleta por paquete exacto. Otras apps pueden aportar el color de su icono: se dibuja una copia privada de 32 px y se muestrean 256 píxeles. Iconos neutros, ausentes o incompatibles conservan el acento general.
- El contraste del acento como texto y del botón se verifica en temas oscuro y claro. Los colores de error y el tema de búsqueda, ActionPanel y Ajustes no se sustituyen por la marca de la app.
- El destino de un formulario tiene precedencia sobre la selección. Una tarea de IA o plan no toma el color de una app ajena. Fijar el inspector conserva su identidad.
- `ApplicationInspector`, que ya existía, ahora se conecta al inspector unificado: identidad, apertura real, información del sistema, favoritos, acciones y metadatos. El reproductor conserva los callbacks y controles autorizados de MediaSession para el paquete exacto. No hay un segundo inspector ni datos musicales inventados.
- Se incorporan diez PNG originales de Icons8 iOS Style, de 50 px, en la ficha, reproductor y toolbar del inspector. Hay NOTICE y enlace de atribución en Ajustes. Los iconos originales de aplicaciones siguen viniendo de PackageManager. Esto **no** declara una migración de toda la iconografía de otras áreas.

## Coordinación y recuperación

Rama: `feat/raycast-sidebar-permissions-20261020`. Base: `4bb1b7050b63721f25e893ba2bfa385d26c3fec6`. PR #18 continúa como borrador; no se modificó main.

La fuente se recuperó del ZIP verificado de candidata 40 y de los 17 blobs documentales posteriores. Los 1.373 blobs finales y el árbol `f3acfddabfd457eba1cbe6863fb2b657dba87c03` coincidieron con GitHub. No había ejecución Java/Gradle previa observada y las automatizaciones consultadas estaban desactivadas.

Se conservó el propietario `work-master-recovery-20261022-cc97a1bd`. La reanudación de la reserva `194f094…` a `8bdcb33…` y sus actualizaciones usaron commits descendientes y comparación exacta de SHA con `force=false`. El estado anterior permanece en la historia. No se reclamó una reserva por antigüedad.

Checkpoints parciales publicados antes del cierre: `b820450`, `a9f185b` y `821ea95`. Todos conservaron las limitaciones de verificación de ese momento.

## Pruebas ejecutadas

**27 casos únicos correctos en rondas**, no una repetición completa de 27 casos:

| Suite | Casos | Evidencia final |
|---|---:|---|
| InspectorAccentTest | 4 | Primera ronda, correctos; no repetidos |
| MediaInspectorFlowTest | 3 | Primera ronda, correctos; no repetidos |
| UnifiedInspectorFlowTest | 5 | Primera ronda, correctos; no repetidos |
| AdaptiveInspectorFlowTest | 3 | Repetición dirigida, correctos |
| NativePaletteFlowTest | 12 | Repetición dirigida, correctos |

La primera ronda tuvo 23 correctos y cuatro fallos. Tres revelaron una regresión real: abrir Acciones establecía un preview de app y activaba la nueva ficha, agrandando la paleta. Se corrigió separando la inspección explícita del resultado que necesita el ActionPanel. El cuarto fue PixelCopy esperando un redraw no disponible en Robolectric. Se cambió a captura nativa Roborazzi del botón, conservando las tres aserciones RGB; no se aumentó el timeout ni se eliminaron comprobaciones.

Las dos clases afectadas pasaron sus 15 casos en una ejecución aislada de 1 min 59 s. Se añadió además una comprobación de geometría al abrir Acciones desde una ficha ya visible. El contraste, el aislamiento del tema, la fijación, las tareas, el reproductor y el retorno al campo de búsqueda tienen cobertura dirigida.

Comando final de pruebas, desde el árbol aislado:

```sh
python3 /workspace/spotlight-native-runtime-20261023/run-gradle.py \
  :app:testDebugUnitTest \
  --tests dev.bautista.spotlight.ui.AdaptiveInspectorFlowTest \
  --tests dev.bautista.spotlight.ui.NativePaletteFlowTest \
  --init-script /workspace/spotlight-native-runtime-20261023/isolated-tests.gradle
```

La primera ronda incluyó también las tres suites retenidas. El init script separa JVM por clase y conserva todos los tests y aserciones.

## Compilación, lint y entorno

- Kotlin y Java compilaron en Debug y Raycast Preview.
- `:app:lintRaycastPreview` terminó correctamente en 4 min 24 s: **0 errores, 13 advertencias y 1 informativa**. No se agregó un baseline ni se desactivaron controles.
- Se cotejaron por SHA256 las **436 entradas productivas y de pruebas** entre el árbol canónico y el build aislado.
- Se restauraron Gradle 8.10.2, SDK 35 y Build Tools 35 con hashes oficiales. AGP instaló la plataforma base 35 revisión 2; los avisos de ubicación de la extracción unificada ext15 se conservan en los logs. No se cambiaron versiones de dependencias del proyecto.
- El entorno ya tenía los módulos JDK 17, pero faltaban los launchers javac/jlink/jmod. Se restauraron sobre esos módulos. No se cambió el target Java.
- Se preservaron los errores iniciales de inferencia de una lambda Compose, de metadata de un transform de Gradle y de limpieza de archivos compilados. Tras diagnosticar un único transform se lo puso en cuarentena, sin borrar las descargas. La limpieza del árbol sincronizado volvió a fallar; se verificó la misma fuente fuera de esa raíz, como indica `CODEX-ENTORNO-APROBADO.md`.

No se ejecutaron GitHub Actions, Release/R8 ni ensamblado de una APK nueva. No se generó una clave ni se cambiaron paquetes, permisos o preferencias persistidas. Para una próxima APK hay que recuperar y cotejar la keystore histórica antes de firmar; no sirve una clave debug nueva.

## Capturas y límites

Hay tres capturas de la interfaz Android/Compose, obtenidas con **Robolectric API 33 y gráficos NATIVE**: tablet Spotify, tablet Gmail y teléfono Spotify. `captures.json` registra resolución, SHA256 y procedencia.

Los iconos Android genéricos pertenecen a los paquetes de prueba. No representan instalaciones o cuentas reales de Spotify/Gmail. El fondo gris es el host ComponentActivity, no un wallpaper ni One UI. No se editaron las imágenes ni se sustituyeron iconos después de capturarlas.

La comparación con Obsidian/Atelier cubre la distribución, jerarquía y acento del inspector. Transparencia entre ventanas, blur, One UI, Motorola, servicios reales y rendimiento físico siguen pendientes. C5 no se declara completo. Las instalaciones ya entregadas no se actualizan automáticamente con este commit.

## Continuidad

- Fuente canónica recuperada: `/workspace/scratch/cd7b8fe1285c/recovery/source/Spotlight-Next`.
- Build aislado: `/workspace/spotlight-native-build-20261023`.
- Runtime y caché: `/workspace/spotlight-native-runtime-20261023`.
- Evidencias: `docs/verification/adaptive-inspector-20261023`, con rondas originales, tests.json, lint, capturas y manifiesto de entradas.
- La publicación se hizo por GitHub API con CAS y avance normal. El Git local conserva HEAD `4bb1b70` y el árbol modificado, porque no se recuperaron los encabezados raw de los commits posteriores de la API. **No desechar ese árbol ni hacer push de la base**: comprobar el HEAD remoto vigente y cotejar los blobs/entradas. Los checkpoints remotos preservan el trabajo.

Siguiente paso: revisión visual del usuario o una actualización instalable cuando se solicite, usando firma histórica. No repetir los 27 casos si los cambios siguientes no los afectan.
