# Preview 68 — candidata visual aislada, sin APK ni autorización de ejecución

PR #21 abierto/en borrador, base remota verificada `214e00ac95e517717cc98dc05379bc5e4aa27773`.
La fuente de Preview 67 instalada/aprobada es `6918204238706d3c9bac826196ad3f1f272a51e7`.

## Identidad exacta y alcance

- **source_sha:** `c51677b79446ce1dc8213553891d45897b14d726`.
- **Rama fuente aislada:** `feat/preview68-visual-isolated-20261107`.
- **workflow_sha:** el commit del PR que contiene este informe y `scripts/preview_delivery.json`; su SHA exacto se publica en la nota de cierre y en el resultado al propietario. No es el HEAD de la rama fuente.
- **versionCode:** 68, anterior 67.
- **versionName:** `2.0.1-raycast-preview-native57-v68`.
- **Paquete:** `dev.bautista.spotlight.next.raycastpreview`.
- **Certificado requerido:** `885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba`.
- ARM64, minSdk 26/target 35, R8/shrink, no debuggable. Sin cambios de permisos, dependencias, firma, esquema de datos o `release.properties` principal.

La versión 68 está configurada sólo en la rama fuente aislada. El código del PR permanece en 67 con sus avances C5 íntegros; no se debe compilar el HEAD del PR como si fuese la fuente 68.
El JSON de entrega del workflow es el manifiesto autoritativo: la copia histórica 67 que existe dentro de la rama fuente no se usa como tooling de entrega. Este desacople fuente/workflow ya existe en native-delivery.

## Auditoría C5: por qué no entregar el HEAD completo

Diferencia productiva 67→HEAD: seis archivos visuales, dos clases nuevas de citas y `FileIndex`.

1. `DocumentCitations.kt` y `DocumentCitationState.kt` no están instanciados ni registrados en la barra productiva. Sus consultas/lecturas SHA son explícitas; no se ejecutan por existir las clases. Las referencias de integración están en las pruebas C5. No se habilitó recuperación semántica ni síntesis remota.
2. El método añadido `FileIndex.citationDocument()` es utilizado sólo por la sesión de citas. No se llama desde el motor universal, ajustes, Quick Look o la indexación ordinaria.
3. **Existe un cambio en una ruta antigua activa:** `MainActivity` llama `app.index.clear()` desde «Borrar índice». C5 añadió `changes.update{it+1}` al terminar ese borrado. `SpotlightPanel` ya observa `app.index.revision` y la revisión forma parte del efecto de búsqueda/sugerencias. Esa emisión puede refrescar la experiencia existente aunque no se haya integrado la UI de citas. Por ello no se clasifica el HEAD completo como una entrega exclusivamente visual.

La solución es una **rama de entrega desde la fuente exacta 67**, sin revertir nada en el PR:

- `57b01dd` aplica con `cherry-pick -x` el pulido aprobado `2731897c80b2d87440a58d9f9b6d08ef1710723e`.
- `c51677b79446ce1dc8213553891d45897b14d726` modifica únicamente las dos líneas de numeración Preview 67→68.
- Delta total respecto de 67: seis archivos productivos visuales, tres archivos de pruebas dirigidas y `app/build.gradle.kts`. Los seis archivos visuales son idénticos byte a byte al PR actual. Todo lo demás del árbol permanece como en la fuente 67.
- `FileIndex` tiene el mismo blob Git que en 67. Las clases de citas y los tests C5 posteriores no están en esta rama de entrega. No se borran de la rama de desarrollo, no se reescribe historial y no se elimina ningún checkpoint.
- C5.3 conserva 7/12 aciertos léxicos, cinco omisiones y ninguna evaluación semántica auténtica. Contratos, banco C5.4, corrección de lint y pruebas físicas pendientes continúan en PR #21. Esta entrega no distribuye C5 ni acredita su aceptación.

Evidencia: [isolation.json](verification/native68-preparation-20261107/isolation.json), diff productivo completo 67→HEAD y diff 67→candidata en el mismo directorio.

## Compatibilidad con guardas existentes

No se cambia YAML, policy, firma, presupuesto ni verificador de artefactos.
`native-delivery` ejecuta sus tres jobs desde la rama fija del PR, verifica workflow exacto, reserva nueva en ese workflow y checkout de `source_sha` exacto. En estos jobs no exige que la fuente sea ancestro del workflow; esa comprobación pertenece a otros modos y no se usa como sustituto de la revisión de native-delivery.
La comparación que sí exige la policy es `functional_base_sha...source_sha`: base **67 exacta**, fuente descendiente y sólo diez rutas permitidas explícitamente en el manifiesto. No se usa source=base para ocultar cambios ni una allowlist amplia. `FileIndex` y las clases C5 no están permitidos.

La fuente y el manifiesto se publican en una sola transacción `git push --atomic`, después de revalidar ambos refs y la reserva propia. Así el PR conserva todo C5 y el workflow apunta a una fuente publicada independiente. Luego se verifica la comparación autoritativa de GitHub y se registra en las notas; no se ejecuta policy contra un dispatch real.

## Comprobaciones realizadas y reutilizadas

- **17 pruebas offline de entrega aprobadas** con manifiesto 68: actor/rama/evento, source/workflow, reserva, un intento, rechazo de reruns, identidad/firma/archivo fuente y aislamiento de build/firma. Fixtures; cero despacho o claves reales.
- **3 comprobaciones nuevas de comparación aprobadas:** la fuente aislada pasa la policy existente en un dispatch ficticio; añadir `FileIndex` al delta se rechaza; historia divergente se rechaza. Además se comprueba el árbol exacto y el congelamiento de preferencias/140 ms.
- **Inspección AGP offline de sólo configuración aprobada:** código/nombre/paquete/perfil/ABI/SDK. El grafo permite únicamente `:app:inspectPreview68Identity`; no se compila aplicación, test APK ni APK. Se conservó una primera salida con una etiqueta de firma heredada del banco 67; se corrigió esa etiqueta y se repitió sólo la inspección de configuración. Ninguna de las dos inspecciones verificó claves o certificados.
- **19 casos visuales reutilizados, cero reejecuciones:** los seis archivos productivos y las seis clases de pruebas correspondientes tienen los mismos blobs que en el PR validado. Integridad de PNG/XML comprobada contra SHA256SUMS anterior. Las capturas son Compose/fixtures; no One UI físico.
- **Lint global 0 errores, 26 warnings y 1 informativo reutilizado de `214e00a`**, con XML y hash conservados. Se ejecutó sobre el PR, no sobre esta rama aislada. No se presenta como nueva ejecución de lint de la candidata: la base no visual 67 conserva su evidencia anterior y el pulido visual es idéntico al aprobado.
- Focus continúa desactivado por defecto/reversible. Preferencias, savers, temas/Geist, datos, ranking, permisos y apertura productiva de 140 ms mantienen la fuente 67 salvo las seis modificaciones visuales aprobadas. No hay migraciones o acciones nuevas. La conservación física al actualizar se comprobará después de una entrega autorizada.

Evidencia reproducible en [native68-preparation-20261107](verification/native68-preparation-20261107/): script de aislamiento, logs offline, manifiesto de hashes e inspección de identidad.

## Único procedimiento futuro recomendado

Después de **nueva autorización expresa** del par source/workflow final:

1. Comprobar PR/HEAD/workflow, manifiesto, presupuesto US$0/stop_on_limit, ausencia de intentos anteriores por fuente/workflow y disponibilidad de las referencias. Adquirir **reserva nueva** en `feat/preview57-native-return-20261103`, checkpoint=workflow autorizado. No reutilizar la reserva de preparación.
2. Despachar **una sola vez** `build-apk.yml` desde esa rama fija, `mode=native-delivery`, `source_sha=c51677b79446ce1dc8213553891d45897b14d726`, workflow exacto y `confirm_preview=AUTHORIZE_PREVIEW_68_ONCE`, con owner/lease nuevos. No ejecutar desde la rama fuente aislada ni sustituir su SHA por el HEAD del PR.
3. Guardas/preflight histórico aislado → un build optimizado unsigned sin secretos desde la fuente aislada → firma aislada/verificación APK/ZIP. Sin reruns, reintentos ni otra variante. Verificar v2/un firmante, certificado histórico, paquete/code/name, noDebuggable/ABI/SDK/payload/16KiB, SHA-256 y ZIP del mismo source.
4. Validar físicamente sobre 67 **sin desinstalar ni borrar datos**. No declarar actualización compatible realmente instalada hasta comprobarla. La preparación actual sólo demuestra compatibilidad esperada de configuración; no firma ni inspecciona una APK68.

## Checklist físico de las tres correcciones

Galaxy Tab A9+ y Motorola G86: oscuro/claro, texto 100/200 %, aplicación con texto detrás.

- Resultados/formularios/footer legibles; conservar borde glass y selección Studio.
- ActionPanel alineado al panel, acciones completas por scroll, filtro y Tab/ShiftTab/Enter/Escape; márgenes con Gboard, teclado físico, rotación y ventana flotante.
- Formulario Spotify compacto en tablet, filtros que envuelven en teléfono, Buscar legible; no hace falta reproducción real ni reparar integración para esta validación visual.
- Consulta, foco, scroll, preferencias y reversibilidad de Focus conservados al volver/rotar/actualizar. Apertura 140 ms; registrar fluidez real sin inferir FPS/RAM de las pruebas JVM.

Sin Actions, compilación de APK, firma, instalación, modelos descargados o nueva funcionalidad. PR sigue en borrador, main e integral intactos; las nueve V y C5 no se declaran completos. Detener ejecución al terminar esta preparación y liberar sólo la reserva propia.
