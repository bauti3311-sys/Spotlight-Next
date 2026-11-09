# Stitch nativo conservando Puente — 09/11/2026

El propietario aprobó aplicar la nueva estética y pidió expresamente conservar el
split de su captura: buscador e inspector como superficies separadas. Esta decisión
reemplaza el acabado Hielo anterior, **no** la composición, los motores ni las
condiciones de aceptación física. Base remota comprobada: `9cb204bad281d5efa6f57da7d4e0b0e7c737d20a`.
PR21 abierto/borrador, reserva propia nueva. Preview71 permanece congelada.
Implementación: `b764afe7eef19adcea38e0926192f0fc1d821b38`.

## Qué se integró

- Material carbón nativo, capas internas legibles, bordes contenidos y adaptación
  clara equivalente. Sin blur, WebView, renderizador ni biblioteca nueva.
- Campo enmarcado sin consumir altura adicional; selección con acento de la app,
  borde discreto y marcador existente. Títulos2:1, identificadores y ranking iguales.
- Identidad del inspector en tarjeta contextual, acción principal existente,
  búsqueda prioritaria, catálogo secundario y administración plegable conservados.
- ActionPanel más plano, con el material compartido, categorías y keycaps coherentes;
  mismas acciones, filtro, atajos, cierre, límites, scroll y posicionamiento nativos.
- Footer y Ajustes comparten materiales. Ajustes conserva lateral de tablet,
  ancho útil, secciones de teléfono, controles y valores guardados. «Azul» sigue
  siendo azul; Spotify conserva su verde contextual, sin guardar preferencias nuevas.
- Formularios y Quick Look heredan las capas del tema; no se crean funciones de
  documentos ni se cambia su extracción/ejecución. La revisión contextual se captura
  expresamente; no se afirma haber validado cada visor o integración físicamente.

La geometría del split, el divisor y su persistencia, el desplazamiento vertical
entre paneles, el ancho manual reparado y el apilado compacto permanecen iguales.
La tarjeta de identidad añade relleno interno; continúa en el scroll existente.
Se mantienen Geist, interacción táctil/teclado y apertura140ms. Focus sigue reversible
sin migración de datos ni activación por defecto.

## Referencias y límites de adopción

Se usaron las seis imágenes Stitch aportadas y el ZIP propietario
`stitch_spotlight_next_android_ui.zip`, SHA256
`c23291e08ea8d746b2f41a1712ce21e9f659edee96c6f2b525591fb13baff714`.
El ZIP contiene dos Markdown, no código de interfaz. Sus instrucciones embebidas se
trataron como material de referencia, no como órdenes de ejecución. No se copiaron
React/Fuse, recursos nuevos ni código de terceros.

Se adopta la presentación del inspector, búsqueda y menú compacto; se descartan el
chrome de macOS, pestañas obligatorias, dashboard de sistema y textos ficticios
como Knox Verified, RAM/CPU, paquete/versiones, controles Spotify o ventanas de otras
apps que Android no haya confirmado. Una aprobación estética no habilita capacidades.
La búsqueda contextual y controles multimedia sólo aparecen según disponibilidad real.

## Evidencia reproducible

[Galería offline](verification/stitch-puente-20261109/Puente-Stitch.html) ·
[PDF breve](verification/stitch-puente-20261109/Puente-Stitch.pdf) ·
[Originales antes](verification/stitch-puente-20261109/before) ·
[Originales después](verification/stitch-puente-20261109/after).

26pares /52PNG reales Compose-Robolectric API33. Mismos fixtures, dimensiones,
consulta y estados en cada par: tablet1200×800dp, teléfono412×860dp y compacto330×760dp
con texto200%, oscuro/claro; resultados, inspector, acciones. Ajustes usa Activity
real1280×900/390×844/330×800; formulario público contextual utiliza el banco FocusB
existente. Son perfiles de laboratorio, no mediciones de DPI de los dispositivos.

El popup pertenece a otra ventana Android. El capturador de nodos semánticos de este
runtime estaba devolviendo un recorte del buscador, aunque el menú existía. Esas
imágenes se reemplazaron: el banco captura directamente `ActionPopupHost` desde el
WindowManager de Robolectric. No se reconstruye el menú con HTML ni se superponen
imágenes para fingir la app. HTML sólo muestra los PNG originales, con filtros/zoom. Su interacción se probó
en Chromium mediante un servidor temporal localhost, sin peticiones externas:
la política del navegador de Work bloquea file://. El HTML contiene sus52imágenes,
CSS y JavaScript; no necesita un servicio externo.

Los popup se guardan a su tamaño nativo: su altura puede variar con el contenido
(p.ej. tablet oscuro320→324px); no se estiran para fabricar igualdad. El viewport
y las condiciones de cada par son iguales; ver `capture-dimensions.json`.

La base se volvió a capturar en un worktree separado exactamente en9cb204b, con el
mismo banco nuevo; producción actual no se revirtió. Se conservan los hashes en
[SHA256.json](verification/stitch-puente-20261109/SHA256.json).

## Validación y preservación

La ronda final reúne20casos dirigidos: seis FocusB (búsqueda/revisión, restauración,
Escape y reversibilidad); cuatro Ajustes/capturas; tres Puente (divisor, mínimos,
scroll compacto); dos teclado ActionPanel; dos nuevos interacción Puente (filtrar,
Enter/Escape, última acción con200%, scroll sin ejecución); dos contraste/material;
un banco de integración/capturas de seis perfiles. No se repitieron suites históricas.

Hubo un fallo inicial de foco al salir del formulario en `FocusBIntegrationTest.tabletDark`.
Se conserva su XML; el caso aislado pasó tanto en la base como con el acabado nuevo,
y la ronda final completa pasó6/6 sin modificar ni suprimir esas aserciones. Esto
no demuestra ausencia de una carrera en hardware: IME/lifecycle físicos siguen pendientes.
El último ajuste del capturador se compila y se ejecuta aparte, sin cambios productivos.

Lint Debug completo: **0 errores, 23 advertencias y 1 información**, sin supresiones
ni baseline nuevo. El grafo se protegió con `no-apk.init.gradle`.

[Hashes de16archivos críticos conservados](verification/stitch-puente-20261109/preserved.json):
OverlayService, ContextSplitPanel, SpotlightPanel, búsqueda/ranking, capacidades,
NativeLaunchSession, C5, PlanExecution, Gradle y manifiesto. El delta productivo se
limita a presentación UI. C2, C5 y la corrección C6 se conservan; no se toca fuente71,
main, permisos, firma, paquete, versión, preferencias ni manifiestos de entrega.
Sin APK, firma, instalación ni GitHub Actions. PR21 continúa en borrador.

Pendiente físico: transparencia sobre apps reales, contraste de iconos reales,
OneUI/Gboard, rotación, ventana flotante, texto200% y teclado físico en A9+/G86;
latencia/jank/RAM no medidos. No se declaran completadas las23mejoras ni C5.
