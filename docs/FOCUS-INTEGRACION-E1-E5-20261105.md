# Spotlight Focus · Integración productiva E1–E5 · 2026-11-05

Base: `bb7096c2a453559a68f9af350bf0454a6160136e`. Rama de desarrollo y PR #21. Ningún APK ni ejecución de Actions. No se inicia C3–C9.

## Decisiones recuperadas y precedencia

Se contrastaron AGENTS, PLANS, DESIGN, STATUS_40_DECISIONES, el maestro v1.0 de 2026-11-03, la especificación de 40 decisiones de 2026-10-21 y la secuencia visual V1–V4 → Liquid → dee1efd → 771a2fa → 6d9289c → bb7096c. Los experimentos y su evidencia permanecen.

Vigente: Focus, una entrada editable contextual, Geist principal, Obsidian Liquid controlado y marfil; reutilización del motor y actores nativos. Se recuperan selección satinada neutra, aplicaciones originales por Android, utilidades iOS, hasta tres sugerencias/máximo una app, exactas primero, tablet lateral y teléfono reversible, footer contextual, accesibilidad táctil y teclado, opacidades independientes, reducción de movimiento y permisos explícitos. Los tokens antiguos Inter14/metadatos11/radio16–18 son referencias históricas, no instrucciones para revertir el acabado actual. Las opacidades configuradas no se sobrescriben; la superficie de búsqueda aplica el resguardo existente de legibilidad. Blur no es sinónimo de transparencia.

STATUS_40_DECISIONES es una auditoría histórica: no convierte funciones parciales, sesiones multimedia sintéticas o contratos Android en validación física actual. Las incidencias de altura/Gboard de Preview62/63 permanecen abiertas.

## E1 · Iconografía

`IosCommandIcon` normaliza utilidades de barra, filas, navegación, menú, ActionPanel, Ajustes, formularios, archivos, IA y controles. Se conservaron recursos inspector_ios existentes y se incorporaron 31 de la misma familia (dos recursos inicialmente descargados se eliminaron por no tener uso productivo) con procedencia y hashes individuales. Arte de 50px, métricas16/20/24dp; hit48dp; color semántico heredado; sin inventar marcas ni alterar el trazo PNG. Los painters de inspector/multimedia ya iOS se mantienen. Un vector desconocido de una extensión conserva su símbolo en vez de recibir un icono falso.

La atribución visible en Ajustes ahora abarca toda la interfaz. El aviso histórico y el registro de procedencia se conservan; la página oficial de términos devolvió403 al consultar esta sesión, por lo que no se afirma haber verificado de nuevo sus términos actuales. No se redistribuye un paquete independiente de iconos.

## E2 · Tipografía y espacio

`FocusDesign` define familia, roles, pesos, interlineado y materiales en producción. Geist local con pesos regular/semibold; alternativas Inter/sistema/mono en Apariencia. Consulta22sp global y20sp contextual; resultado15/21; textos15/22,14/20,13/19; etiquetas≥12. Respeta fontScale Android, escala de texto y densidad guardada. Objetivos táctiles de48dp. Sin petición de fuentes en ejecución ni dependencia nueva.

## E3 · Búsqueda integrada y recuperación

`SpotlightPanel`, `SpotlightBar` y `FocusSearchTask` son componentes productivos. El mismo BasicTextField cambia nombre/icono/placeholder al abrir una acción ComposeSearch existente. PackageManager aporta identidad; los monogramas de la galería son fixtures sintéticos declarados, no marcas distribuidas.

En teléfono, el inspector Focus utiliza una vista contextual reversible sin repetir la lista encima; cerrar inspector devuelve resultados y foco. Cabecera global64dp/contextual76dp, sin altura mínima decorativa para el cuerpo. La consulta contextual tiene su borrador independiente; la global, selección de texto y cursor se conservan al volver. Se reutilizan selección de resultados, LazyListState, foco y navegación existentes. El host contextual usa identidad estable por tema/paquete, no por cada carácter. El guardado acotado conserva texto/cursor, nunca Intents, grants ni confirmaciones; no guarda editores privados. Revisión y ejecución se separan. Enter revisa y después confirma por el mismo actor de lanzamiento; la confirmación se consume antes del despacho y se invalida al editar.

Sin Spotify autorizado se ofrece únicamente enlace público. Con autorización, el panel de búsqueda ya existente utiliza el mismo campo externo y su actor anterior; escribir no consulta la API. El registro/confirmación/revalidación Android, los posibles fallos y la revisión de alternativas siguen en NativeLaunchSession. Android puede aceptar una solicitud sin demostrar un efecto externo.

La preferencia persistente Spotlight Focus en Apariencia permite probar la integración y volver a la anterior. **No se cambia todavía su valor por defecto**: la comparación antecede a consolidar la geometría/navegación como opción global, tal como pidió el propietario. No hay un segundo motor de búsqueda.

## E4 · Ajustes

La Activity real hereda Geist y los materiales Focus; secciones planas, jerarquía refinada e iconos iOS, lateral tablet y selector móvil existentes. Se conservan las ocho áreas, búsqueda, controles, permisos y persistencia. Las nuevas preferencias visuales participan del backup portable y de los temas; importar un respaldo antiguo no borra la elección actual. No se otorgan permisos al recorrer secciones. No se elimina ninguna capacidad ni se traslada la autorización a otro lugar.

## E5 · Componentes

Identidad de aplicación integrada sin una tarjeta gigante; acciones secundarias plegables también en tablet; acentos discretos en inspector con contraste calculado. Panel de acciones, menús, Quick Look, formularios, estados IA/planes, archivos y multimedia comparten los materiales y la tipografía productiva. El footer usa el dock discreto sin cápsulas de atajos pesadas. Los controles multimedia siguen dependiendo de acciones MediaSession comprobadas: no aparecen controles de sesión ausentes. Los formularios y revisiones conservan sus ejecutores y datos originales.

Transición de inspector de110ms, selección según la preferencia existente y apertura WindowManager previa; no se añade blur ni bucles de dibujo. LocalMotion conserva animaciones desactivadas/reducción del sistema. No se introduce un temporizador de búsqueda ni trabajo de red por tecla.

## Evidencia y alcance de la validación

[Galería completa](verification/focus-integrated-20261105/GALERIA.md). Capturas NATIVE de Roborazzi ejecutando SpotlightPanel y MainActivity, no composables de prototipos aislados. Cuatro perfiles con datos controlados. Referencias Preview63 y bb7096c archivadas se identifican separadamente; el antes/después actual de recuperación utiliza la misma revisión y fixtures para aislar la presentación. El fondo del canvas no es wallpaper ni Gboard real.

Las pruebas cubren entrada única, revisión/retorno, cursor/foco/borrador, menú, altura reducida, ocho áreas de Ajustes, persistencia/recreación, multimedia con fixture MediaSession, documentos locales autorizados, ActionPanel, estado IA interceptado sin HTTP, contrato de compartir/restauración y geometría/IME del servicio superpuesto. Los XML y logs distinguen intentos iniciales de la evidencia verde. El fallo inicial de medida de cabecera (weight de TextField dentro de Column) fue reproducido y corregido; dos selectores de test ambiguos se corrigieron sin cambiar navegación.

No se equipara el marco JVM con dispositivo real. Quick Look PDF conserva el mensaje honesto de renderizador nativo no disponible en este runtime; no se genera una página falsa para la captura. La sesión/portada multimedia y la respuesta IA son fixtures explícitos; no prueban una cuenta Spotify ni un proveedor IA real. Las capturas no certifican latencia, fps, GPU ni memoria en tablet.

## Cumplimiento visual

|Mejora|Implementación integrada|Evidencia|Pendiente|
|---|---|---|---|
|V01 superficies|Grafito/marfil, capas sobrias, bordes finos, sin blur obligatorio|Cuatro perfiles del panel/Activity|Wallpaper claro/oscuro y contraste físico|
|V02 inspector|Identidad plana, acciones reales, tarea contextual en la superficie|App, documentos, Spotify, revisión|Aprobación de composición y confort físico|
|V03 acentos|Identidad PM y acento localizado con ajuste de contraste|Contexto Spotify/Vivaldi e inspectores|Iconos reales y casos de contraste en dispositivo|
|V04 resultados|Geist15/21, metadatos12, selección neutra y hit48|Inicio/búsqueda e iconografía cubierta|TalkBack/fontScale extremos en One UI|
|V05 footer|Dock discreto, acción contextual, revisar/abrir separados|Menú, acciones, contexto, archivos|Teclado físico/lector y sin recorte físico|
|V06 adaptación|Presupuesto independiente, chrome medido, retorno y borradores|Servicio IME + viewport330 + cuatro perfiles|Gboard real, abrir desde Ajustes, rotación y multiventana|
|V07 iOS|Cobertura de utilidades, PM para marcas, atribución|Test de glifos y registro de procedencia y31 assets utilizados|Consulta actual de términos bloqueada403; base histórica documentada|
|V08 contenido útil|PDF/archivos, MediaSession y estados IA existentes|Escenas productivas con fixtures explícitos|Render PDF y cuentas/apps reales|
|V10 microinteracciones|Reveal110ms, selección y movimiento reducido existentes|Código productivo y política de movimiento|Perf/foco con teclado real, no hay fps medidos|

## Antes de avanzar a C3

1. Revisar las comparaciones y aprobar la composición para su activación general. La anterior sigue como recuperación; no se borra ningún checkpoint.
2. Autorizar por separado una candidata de compilación con SHA fuente y workflow exactos. La autorización histórica de Preview63 no sirve para esta fuente. No hay APK generado por esta fase.
3. Instalar la candidata autorizada sobre la versión existente: comprobar identidad/firma/actualización y preferencias, y permitir volver a la interfaz anterior.
4. Galaxy Tab A9+: abrir desde Ajustes, Gboard abierto/cerrado varias veces, resultado/inspector/ActionPanel/footer visibles, recuperar altura, rotación y ventana múltiple, volver por toque/Esc/Back sin perder consulta/cursor/scroll.
5. Teléfono moto g86: mismo recorrido y navegación reversible, fuente Android ampliada, TalkBack, teclado físico cuando esté disponible, movimiento reducido y ambas paletas.
6. PDF autorizado real, seleccionar/cancelar/restaurar archivos, elegir destino y revisar/confirmar una sola solicitud. Permiso revocado debe fallar sin cambiar destino. Comprobar en la app receptora el efecto real separadamente.
7. Spotify/Vivaldi instalados: identidad real, búsqueda pública y regreso; multimedia sólo con sesión autorizada. No validar integraciones profundas futuras por estas escenas.

La integración técnica de presentación y su adopción visual/física son cierres distintos. C3–C9 siguen pausados hasta la aceptación estética y física solicitada.

## Cierre técnico de esta candidata

- 44 casos distintos con XML verde conservado, deduplicados por clase+método en test-coverage.json. Son rondas dirigidas, no una suite integral ni una certificación de dispositivo. Se reutilizan además los dos casos de persistencia de Ajustes aprobados en la ronda anterior, sin repetirlos para inflar el total.
- Lint final: **0 errores, 26 advertencias y 1 información**, sin baseline ni supresiones nuevas. Dos recursos nuevos sin uso se retiraron. Se corrigió el bloque de placeholder señalado por SuspiciousIndentation. Un error de compilación del helper de medida se corrigió antes de obtener la evidencia verde. Los logs iniciales y las regresiones introducidas durante la integración móvil se conservan, no se borran.
- 61 archivos de límites funcionales comparados byte a byte con bb7096c: manifiesto, Gradle/identidad, política de entrega, motores search/index/control/sharing y ventana overlay, sin cambios. Preferencias añaden sólo dos campos visuales; el adaptador Spotify comparte su actor anterior.
- 124 PNG originales de componentes productivos, 20 hojas comparativas y PDF autocontenido. Hashes por captura. Revisiones históricas identificadas, originales sin sustituir por imágenes generadas.

|Métrica geométrica de fixtures emparejados|Recuperación actual|Focus productivo|
|---|---:|---:|
|Inicio tablet|168dp|176dp|
|Inicio teléfono|180dp|188dp|
|Inspector Vivaldi tablet|483dp|426dp|
|Inspector Vivaldi teléfono|548dp|426dp|
|Contexto preparado Spotify/Vivaldi|Otra composición histórica|178dp|
|Presupuesto reducido probado para contexto|330dp|178dp de superficie +24dp de margen|

Alturas de superficie medidas por semántica Compose; no incluyen12dp de margen exterior por cada lado. Cabecera global64dp/contextual76dp. El inicio aumenta8dp por la jerarquía de consulta; no se lo presenta como una reducción. En teléfono, se elimina la repetición de resultados sobre el inspector cuando no está fijado. Al editar se vuelve a resultados y la sesión multimedia no los sustituye antes de seleccionar una aplicación. La fijación explícita conserva su modo anterior.

Los31 PNG utilitarios complementarios suman27.901bytes de archivos fuente, no un tamaño de APK estimado. Fuentes ya disponibles en el proyecto. Sin blur, librería, motor, permiso ni servicio nuevo. No hay cifras de fps, memoria o latencia de dispositivo: deben medirse en la candidata física. Las duraciones de tests/render JVM no se presentan como rendimiento Android.

### Correspondencia histórica compatible

|Decisiones históricas|Tratamiento actual|
|---|---|
|D01/D02/D13 materiales y D14 legibilidad|Recuperados con grafito/marfil, opacidades independientes y contraste≥4,5:1 de acentos; Geist y escala actual prevalecen sobre Inter14/11 y radios antiguos.|
|D03/D04/D11 adaptación|Presupuesto WM intacto, chrome medido y contexto reversible; validación One UI pendiente.|
|D05/D19/D20 Ajustes y navegación|Ocho áreas, lateral/selector, búsqueda y centro de permisos conservados; nunca se concede acceso al visitar una sección.|
|D06/D07/D09/D10 resultados/identidad/controles|Una línea y metadatos, iconos PM/monocromos, máximo3/máximo1app y controles disponibles, sin botón IA fijo ni micrófono inventado.|
|D08/D12 footer y movimiento|Dock discreto, revisión explícita, teclado existente, transición corta y reducción del sistema.|
|D15/D16/D17/D18/D21 inspectores y recuperación|Datos autorizados, controles MediaSession existentes, IA/planes existentes y borradores; no se restauran confirmaciones ni se confunden fixtures con efectos externos.|
|D22/D23/D24/D25/D30 seguridad/gestos/orden/atajos|Contratos C1/C2 y bindings conservados; exactas primero y sin remoto por tecla. El regreso tras restauración conserva selección/consulta en Focus.|
|D31/D32/D33/D34/D35 fuentes/contexto/IA/contenido|Se moderniza su presentación existente; no se elevan estados funcionales históricos ni se inicia integración profunda futura.|
|D40 checkpoints|Evidencia local reproducible, reserva propia, PR borrador y main intacta; sin APK ni Actions.|

**Estado:** E1–E5 integrados técnicamente como candidata reversible en producción. Falta aprobación de adopción visual y validación física. El manifiesto ejecutable de entrega de Preview63 queda histórico, intacto y no reutilizable para esta fuente. La siguiente candidata APK requiere numeración/manifest revisados y autorización nueva para el par exacto; esta fase sólo entrega una candidata de fuente para revisión estética.

Fuente productiva congelada de esta candidata: `8d560828369e2fbd35957808cad66ae4a4a2895d`. Preparación de aprobación visual y límites de la próxima entrega en [review-candidate.json](verification/focus-integrated-20261105/review-candidate.json). No es un par autorizado de build; versión64 es una propuesta no aplicada. El siguiente paso de release se prepara tras aceptar la composición para evitar fijar una fuente que todavía deba cambiar de valor por defecto.
