# Focus — experiencia de búsqueda, revisión aislada

Dirección B preferida, **sin adopción global**. Esta revisión implementa en Compose
real el recorrido completo de búsqueda acordado, en app/src/test. No modifica
fuentes de producción, motores, permisos, paquete, firma, dependencias o main.
No APK/Actions ni C3–C9. Se conservan771a2fa y dee1efd y el WIP anterior.

## Galerías accesibles

Cada perfil contiene las6 escenas nuevas frente a las6 de estilo63 y la referencia
B anterior donde existe. PNG originales de captura nativa Roborazzi/API33, sin
retoque ni generación, mismos fixtures/ancho/densidad/lienzo700dp por perfil:

- [Tablet oscuro](tablet-dark.md)
- [Tablet claro](tablet-light.md)
- [Teléfono oscuro](phone-dark.md)
- [Teléfono claro](phone-light.md)

24 vistas nuevas +24 referencias63 +4 referencias B compatibles =52 PNG.
El pie de evidencia FUERA de la consola distingue explícitamente contrato público
resuelto por fixture de integración global y validación en dispositivo pendientes. Ninguna página
muestra canciones, contactos, historial o resultados privados inventados.

## Qué funciona en este prototipo

- Campo único **editable**, ya no la consulta readOnly de B771. Misma llamada
  BasicTextField antes/durante/después del contexto, sin barra adicional ni chat.
- Motor local SearchEngine, ProductVisibility y CompactResults existentes para
  resultados reales del fixture; hasta3 sugerencias de inicio, aplicación exacta
  primero. El laboratorio produce1 sugerencia útil, no se fabrican otras para
  llenar3 filas. Escritura/cursor no disparan una API privada ni IA remota.
- AppCapabilities.available/prepare y IntegrationRegistry existentes gobiernan
  el ingreso al contexto. Nombre/icono/placeholder cambian al elegir la acción.
- Consulta global y borrador contextual separados con TextFieldValue. Al volver
  se restauran consulta/cursor/selección de texto, selección de resultado, foco y
  la misma LazyListState, incluido índice/offset no nulos en prueba dirigida.
- Edición contextual prepara ActionCatalog.searchAction existente, revalidando
  capacidad, tema/destino y URI explícita. Cambio de mecanismo obliga a volver.
  Revisión, cancelación y confirmación, sin resucitar una confirmación vieja ni
  entregar dos veces la misma acción. Revalidación del receptor al confirmar.
- Enter y acción Search de IME preparan revisión; flechas cambian selección global,
  Escape cancela revisión/contexto y hay regreso táctil visible. Tab/Shift+Tab
  usan foco Compose: no se declara integrado todo el keymap de producción.
- App/acción primaria global conservan su Result/Action original en callback.

## Lo que sigue siendo integración pendiente

Todo es código Compose ejecutado, pero su adopción, conexión a NativeLaunchSession,
WindowManager, Guardados/acciones generales y keymap completo **no** está integrada
en producción. El callback final se registra en el harness; no se llama
startActivity ni se interpreta como solicitud aceptada por Android o efecto real.
No hay actividad ni nueva ventana superpuesta por entrar en contexto.
Los contratos C1/C2 permanecen intactos porque sus fuentes/ejecutores no se editan.
No se equiparan estas pruebas a una regresión completa del producto.

## Capacidades mostradas y límites

| Contexto | Mecanismo real reutilizado | Evidencia de esta ronda | No se promete |
|---|---|---|---|
| Spotify | DOCUMENTED_LINK, ACTION_VIEW a open.spotify.com/search con paquete explícito | Resolver exportado de fixture + ActionCatalog + revisión/cancelación/confirmación | Consultar cuenta, catálogo en Spotlight, audio o búsqueda nativa universal. La integración autorizada ya existente no se elimina ni amplía. |
| Vivaldi | DOCUMENTED_LINK, búsqueda web pública con paquete explícito | Resolver de fixture + URI pública revisada | Leer pestañas/historial ni buscar contenido privado. |
| Archivos | FileIndex/ContextStore/SearchScope existentes para orígenes autorizados | Sin cambios; no escena contextual nueva en esta revisión | Escanear fuentes sin permiso ni UI nueva ya implementada. |
| WhatsApp | Contactos Android sólo con consentimiento y People.allowed; contratos públicos que realmente exponga la instalación | Sin escena nueva ni búsqueda interna supuesta | Buscar chats privados o integrar C3/C4. No nuevos permisos. |

No credenciales, OAuth, consultas externas o API de Spotify por tecla. PackageManager
sintético contiene Spotify y Vivaldi7.2-fixture con icono Android genérico. Se
registran lanzadores y enlaces públicos resolubles: no es prueba de versiones
comerciales. La rama sigue conservando las integraciones existentes.

## Comparación honesta con63 y B771

63 se renderiza usando SpotlightPanel/SearchEditor reales, flags de revisión
apagados, mismos fixtures/configuración y motor. No es una foto física ni la
instalación del APK63. Sus6 escenas incluyen el formulario existente y su campo
secundario. No se retoca ni elimina una barra de la referencia.

B771 sólo implementaba Vivaldi con contexto abierto y campo readOnly fijo. Se
re-renderiza **sin cambiar su código** con los datos de Vivaldi de esta ronda.
No tenía inicio, Spotify ni transformación contextual: esos casilleros se marcan
sin equivalente; no se inventa una versión anterior. Su contexto seleccionado
no se presenta como estado idéntico a la nueva búsqueda global sin inspector.
Las diferencias de datos visibles al abrir contexto son deliberadas: se sustituye
la lista en el teléfono y se mantienen los datos globales para volver.

## Sistema tipográfico y espacio

Geist local/OFL ya fijada en dee1efd; sin recursos nuevos. Ocho roles explícitos en
FocusSearchType, usados en toda la experiencia revisada, no sólo en el inspector:

| Rol | Tamaño/interlineado sp | Peso |
|---|---|---|
| Consulta global |22/30|400|
| Consulta contextual |16/24|400|
| Título de tarea |18/24|600|
| Resultado |16/22|600|
| Subtítulo/acción |13/18|400|
| Metadato/contexto |12/16|400|
| Atajo |11/14|400|
| Grupo |11/16, espaciado0,8|600|

El rol de atajo queda definido pero no se añaden keycaps decorativos a las
capturas. No afirmar una actualización de Ajustes u otras pantallas.
Campo con etiqueta contextual e icono alineados, header mínimo76dp; filas con
icono32dp o22dp según tipo, paddings20×10dp y separación de texto3dp. Títulos
máximos18sp: no display gigante. Footer mínimo48dp, una acción primaria y estado
breve. En claro, marfil/piedra; grafito en oscuro. Acentos sólo por contexto.
Revisar vacío aparece deshabilitado visual y funcionalmente.

La ventana se mide por contenido, con cuerpo desplazable acotado y footer externo
al scroll. Mismo ancho de overlay960/360dp, margen16×12dp y lienzo700dp. El fondo
libre exterior no cuenta como vacío interior. Los CSV/JSON separan área de superficie
y footprint con márgenes; no se computan píxeles de tinta como «eficiencia».

## Pruebas y límites

8 identidades distintas:4 perfiles×6 escenas + restauración de scroll/borrador y
receptor revocado + capacidad ausente + confirmación única del contrato original
+ teléfono con fontScale1,6. No sumar rondas repetidas ni PNG como casos nuevos.
La primera ronda falló por usar assertTextEquals vacío sobre semántica combinada
con placeholder; se preserva el rojo. Se corrige comprobando EditableText vacío
junto con una aserción independiente del placeholder, sin borrar ninguna prueba.
No se repite la suite C2: sus fuentes no cambian. Primer verde conservado por
trazabilidad, raíz corresponde a capturas/ronda finales.

Sin blur, imágenes decorativas, cargas de fuentes por red ni animación de altura.
El cambio de contexto sustituye contenido y mantiene el campo, no crea ventanas.
No se acredita rendimiento por el tiempo de Gradle: p50/p95, jank, memoria,
Gboard/OneUI/teclado físico y TalkBack real siguen pendientes. FontScale1,6 en
JVM no sustituye accesibilidad física. Las incidencias62/63 permanecen abiertas.

Tras esta entrega se detiene el trabajo para aprobación visual del propietario.

## Métricas finales y contrapartidas

| Perfil | Inicio63/Focus dp | Global63/Focus dp | Contexto63/Focus dp |
|---|---:|---:|---:|
| Tablet oscuro/claro |192/236|261/298|367/226|
| Teléfono oscuro/claro |204/236|285/298|367/244|

Footprint incluye12dp arriba/abajo; superficie pintada24dp menos. Ancho pintado
928/328dp constante. Área de superficie en CSV/geometry.json es ancho×alto, no
una métrica de píxeles de texto ni de eficiencia. El pie externo de evidencia no
se incluye en la altura de consola. B771 seleccionado mide316dp en ambos tamaños,
pero es una tarea/estado distinto, no un formulario web equivalente.

El inicio/global crecen frente a63 por la etiqueta del campo, legibilidad y filas
con jerarquía; se declara como contrapartida visual pendiente de aprobación. El
contexto reduce barras/controles y altura. No se sostiene que todo sea más compacto.
La densidad/tamaño se revisan con estos datos antes de extender a otras pantallas.

Lint final: 0 errores / 27 advertencias / 1 información.
Gradle final correcto en2m17s; reporte UP-TO-DATE después del análisis al no cambiar
los hallazgos. No se interpreta como latencia/rendimiento de interfaz.

Reproducir localmente (sin tareas APK):

```sh
JAVA_HOME=/workspace/tooling/jdk17 ANDROID_HOME=/workspace/tooling/sdk \
bash gradlew :app:testRaycastPreviewUnitTest \
 --tests dev.bautista.spotlight.ui.FocusSearchReviewTest \
 :app:lintRaycastPreview --continue --no-daemon --max-workers=2 \
 -I scripts/directed-tests.init.gradle \
 -PspotlightDirectedClasses=dev.bautista.spotlight.ui.FocusSearchReviewTest
```

Configurar proxy/truststore del entorno si corresponde. XML/log final en raíz;
los verdes anteriores no son casos adicionales. La última ronda revalida las8
identidades después de sacar el rótulo técnico del interior del producto.
