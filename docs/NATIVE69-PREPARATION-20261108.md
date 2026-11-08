# Preview 69 — candidata C2 aislada, sin autorización de ejecución

Base del PR #21 comprobada: `d11a91e1649eca7cedbdd3a756c5b53a7f46d5ec`, abierto/borrador.
Base ejecutable: fuente exacta de Preview 68 `c51677b79446ce1dc8213553891d45897b14d726`.

## Par e identidad

- **source_sha:** `cbc956ab56756d9ebdf7de05300fb0b5f5b14a19`.
- **Rama fuente:** `feat/preview69-c2-isolated-20261108`.
- **workflow_sha:** commit del PR que contiene este informe y el manifiesto
  `scripts/preview_delivery.json`; SHA exacto publicado en nota de cierre/entrega
  al propietario. El workflow se despacha desde `feat/preview57-native-return-20261103`,
  nunca desde la rama fuente aislada.
- **versionCode:** 69; anterior 68.
- **versionName:** `2.0.1-raycast-preview-native57-v69`.
- **Paquete:** `dev.bautista.spotlight.next.raycastpreview`.
- **Certificado requerido:** `885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba`.
- ARM64, minSdk26/target35, no debuggable, R8/shrink habilitados.
- Presupuesto US$0, stop_on_limit=true. **No autorizada, no despachada, sin APK.**
  Autorización de la entrega 68 consumida; no sirve para 69.

## Contenido exacto y aislamiento

Diez rutas diferentes frente a la fuente68: seis archivos productivos, tres pruebas
locales y `app/build.gradle.kts`. Ninguna otra ruta cambia; 3.512 archivos heredados
conservan sus blobs exactos. La lista cerrada está en el manifiesto autoritativo del
workflow y en `verification/native69-preparation-20261108/isolation.json`.

1. **Título Focus 2:1**, de `71500e399a2cf56c5a557c8ba73b223c4e895375`:
   SpotlightWidgets y FocusTitlePriorityTest. Prioridad del título sobre descripción
   cuando el inspector reduce el espacio; filas, selección y callbacks intactos.
2. **Relevancia/estabilidad C2**, delta completo de
   `f9fcd5d8745ed45127f44c7616da2ec9a82ef2c3`: HomeSuggestions, Models,
   ResultRanker, SearchEngine y C2RelevanceCorpusTest. Espacios/alias no ocultan
   coincidencia exacta; puntajes normalizados; recencia respeta learning/modo
   privado; desempate estable de homónimos favoritos. Sin segundo ranker.
3. **Recibo de apertura**, delta completo de
   `b2b91df784a516e7b767fceba7c5c4e39bdae0ab`: una línea SpotlightPanel y
   C2AppOpenJourneyTest. El inspector fijado presenta SOLICITADO, nunca prueba de
   ejecución externa. Ejecutores/cierre/acciones permanecen intactos.
4. Sólo dos líneas de numeración 68→69. Sin migración de preferencias ni datos.

Se trasladaron parches seleccionados, sin fusionar el PR completo. Los nueve
archivos de fuente/pruebas trasladados son byteidénticos a los ya verificados en
el PR. Los commits aislados previos a la numeración son b980d24, 82425d0 y a1cf53f;
se conserva su procedencia en los mensajes de commit. No se rehace ningún arreglo.

**C5 queda completamente fuera de la candidata:** DocumentCitations y
DocumentCitationState no existen en esta fuente; FileIndex es byteidéntico a 68,
sin el cambio de revisión ni métodos de citas posteriores. Tampoco se trasladan
sus fixtures, instrumentación ni documentación posterior. El único delta productivo
entre candidata y PR son esas dos clases C5 y FileIndex. C5 sigue íntegro en el PR,
sin revertirlo, borrarlo ni modificar su evidencia C5.3/C5.4.

No se incorpora código experimental adicional: todo archivo ajeno a las diez rutas
es exactamente el heredado de 68. Las capacidades/experimentos que ya estaban en
68 no se eliminan ni se activan. No se reparan Spotify/Square/WhatsApp/documentos.

## Conservación comprobada por archivos

OverlayService, materiales Studio, FocusDesign, ActionPanel/footer, Ajustes A
(MainActivity), Preferences, manifest Android, permisos, ejecutores, AppCapabilities,
IntegrationRegistry, NativeLaunchSession, release.properties, dependencias y firma
configurada conservan la base68. SpotlightPanel sólo añade el recibo; Widgets sólo
cambia la proporción aprobada. La apertura sigue `setDuration(140L)` y Focus mantiene
`focusInterface=false` por defecto y selector reversible. No hay cambios de claves,
paquete, esquemas ni savers. Prueba estática de actualización esperada 68→69; no se
ha instalado una APK69 ni verificado su certificado, porque aún no existe.

El manifiesto operativo se lee desde el **workflow autorizado**. La copia histórica
de scripts/manifiesto que hereda el árbol fuente no se usa para gobernar entrega;
se mantiene la separación fuente/tooling existente, sin cambiar YAML ni guardas.

## Comprobaciones nuevas y evidencia reutilizada

- Tres comprobaciones offline de aislamiento/guardas: fuente aislada admitida;
  incorporar FileIndex se rechaza; historial divergente se rechaza. Mock de dispatch,
  ninguna llamada de ejecución real.
- 17 pruebas offline del procedimiento de entrega con manifiesto69: actor/rama,
  par exacto, reserva, un intento/reruns, identidad, firma y ZIP. Artefactos ficticios
  identificados como tales; ninguna clave ni APK productiva.
- AGP sólo configuración, tarea única inspectPreview69Identity: identidad69,
  ABI/SDK/noDebuggable/R8/shrink correctos; sin tareas de ensamblado/firma/instalación.
- Comprobación focalizada del nuevo ensamblaje aislado: C2RelevanceCorpusTest y
  C2AppOpenJourneyTest, sin C5: **19/19 correctos** (13 corpus +6 recorridos). Resultado y XML originales en el directorio de evidencia.
- Integridad SHA-256 de evidencia previa: título cinco casos, ranking28 y recorrido
  de apertura23. No se repiten las suites históricas ni las galerías.
- Lint global del PR d11a91e reutilizado: 0 errores/26 advertencias/1 información;
  no se presenta como lint nuevamente ejecutado sobre esta candidata aislada.

Scripts, diff completo, comparación de blobs, logs, XML y hashes en
[verification/native69-preparation-20261108](verification/native69-preparation-20261108/README.md).

## Único procedimiento futuro recomendado

Tras **nueva autorización expresa** del par source/workflow final:

1. Revalidar HEAD/PR/manifiesto/rama fuente, lista exacta de diez rutas, presupuesto,
   historial sin intentos por source/workflow y guardas. Adquirir reserva **nueva** en
   la rama del PR con checkpoint=workflow autorizado. No reutilizar esta reserva.
2. Un único `build-apk.yml`, modo `native-delivery`, desde
   `feat/preview57-native-return-20261103`, source exacto indicado arriba,
   workflow exacto indicado en cierre y `AUTHORIZE_PREVIEW_69_ONCE`.
   Sin reruns, reintentos, fallback ni compilaciones alternativas.
3. Preflight histórico → build optimizado unsigned sin secretos desde fuente69
   aislada → firma/verificación aisladas. Comprobar APK ARM64/noDebuggable, identidad,
   v2/un firmante/certificado histórico, zipalign/payload/16KiB, hashes APK/ZIP,
   ZIP del SHA autorizado y procedencia. No sustituir fuente por HEAD del PR.
4. Validar actualización sobre 68 sin desinstalar ni borrar datos. Paquete y firma
   esperados no acreditan por sí solos actualización instalada ni conservación física.

Esta preparación no habilita el despacho ni solicita pruebas manuales ahora.
C2, las nueve V y C5 no se declaran completos; PR21 permanece borrador/main intacta.

## Checklist físico posterior — Tab A9+ y Motorola G86

En ambos, registrar versión/dispositivo, resultado y cualquier fallo; oscuro/claro,
texto normal/ampliado, vertical/horizontal y ventana flotante cuando esté disponible.

- Actualizar sobre68 sin desinstalar; preferencias, temas, favoritos y datos conservados.
- Abrir inspector con títulos largos: prioridad visible del título; selección y acciones
  accesibles, footer/ActionPanel sin regresión respecto68.
- Buscar `webOS Dev Manager`, `Spotify`, `spot`; probar espacios y alias configurados.
  App exacta/prefijo relevantes sin ocultar acciones explícitas; `queen` no impone intención.
- Inicio vacío: hasta tres sugerencias, máximo una app, sin relleno alfabético.
  Desactivar aprendizaje/modo privado y comprobar que no influye recencia aprendida.
- App local instalada: seleccionar sin ejecutar; abrir por toque/Enter. Inspector fijado
  muestra solicitud, sin afirmar resultado comprobado. Volver mantiene estado coherente.
  Doble activación no duplica; Escape/cancelación no abre nada; sin destino alternativo.
- Gboard y teclado físico: Enter/Tab/ShiftTab/Escape, scroll sin ejecutar, retorno de foco;
  rotación/ventana flotante y regreso de otra app sin repetir solicitud.
- Focus reversible, materiales legibles y apertura140ms conservados. Registrar fluidez
  observada; no inventar FPS/RAM ni inferir rendimiento físico de JVM.

No hace falta probar reproducción Spotify, envíos ni citas C5 para esta candidata.
