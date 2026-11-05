# Spotlight Focus — acabado de producto sobre la arquitectura aprobada

El propietario aprobó **conceptualmente** el único campo contextual y su recuperación,
no el diseño visual definitivo ni su adopción global. Este subhito pule la candidata
6d9289c sin reiniciar la composición ni tocar C1/C2. Todo el código sigue en
app/src/test; fuentes productivas, main, permisos, identidad y dependencias intactos.
No APK/Actions/C3–C9. Checkpoints anteriores y WIP local conservados.

## Galerías reales antes/después

- [Tablet oscuro](tablet-dark.md)
- [Tablet claro](tablet-light.md)
- [Teléfono oscuro](phone-dark.md)
- [Teléfono claro](phone-light.md)

Cuatro estados: inicio, global, contextual vacío y contextual preparado. Inicio
compartido +3 estados de Spotify +3 de Vivaldi =7 escenas por perfil:28 nuevas
vistas y28 referencias,56 PNG nativos de Compose/Roborazzi/API33. Mismo lienzo
960×700 o360×700px, mdpi, margen16×12dp, contenido, cursor, estado y destino.
Sin imágenes generadas, retoques o composición de capturas por fuera de Compose.
El fondo libre y el pie de evidencia quedan fuera de la ventana, sin aumentar su
altura para llenar la imagen.

6d9289c se reproduce con su presentación apagada/encendida sobre **el mismo estado
vivo**, no volviendo a ejecutar búsquedas para cada comparación. LocalFocusProductFinish
selecciona sólo presentación. No se afirma que la referencia sea foto o APK físico.
La tipografía antigua y su explicación/estado de footer se mantienen al desactivar
el acabado; los checkpoints históricos no se reescriben.

## Cambios concretos

1. Campo contextual20/28sp, antes16/24; icono/nombre instalado en línea breve
   encima, sin icono lateral que quite ancho a la consulta. Global22/30sp.
   Header mínimo76dp conservado: cambia la jerarquía sin un título gigante.
2. Se retira «Tu búsqueda continúa…» del centro. Vacío: una guía breve de tarea.
   Escrito: se propone revisión; preparado: acción y host destino reales.
   La consulta se muestra **una sola vez**, editable, sin repetirla como tarjeta.
3. Explicación del mecanismo público bajo demanda, desde Enlace público.
   No se muestran resultados de Spotify ni mensajes de éxito externo.
4. Footer integrado como acciones: información secundaria a la izquierda,
   Revisar/Abrir en la app a la derecha. Se retiran contadores/estados tipo
   «Sin solicitud enviada», «Acción preparada» y la etiqueta técnica «Android».
   El callback y la revalidación siguen siendo los mismos; no se fabrica envío.
5. Geist refinada, pesos400/600, tracking moderado, etiquetas en título en lugar
   de bloques en mayúsculas; filas58dp mínimos, menos relleno vertical y sin
   cápsulas/bordes nuevos. Táctil mínimo48dp en acciones y regreso.
6. Grafito con planos opacos y marfil/piedra diferenciados. Una sola superficie,
   sombra8dp de baja opacidad, sin tarjeta interior ni blur. No nuevos shaders.
   Texto normal/secundario >=4,5:1 sobre superficies normal/baja/alta en pruebas.

## Tipografía de esta candidata

| Rol | Tamaño/interlineado sp | Peso/ajuste |
|---|---|---|
| Consulta global |22/30|400, tracking−0,25|
| Consulta contextual |20/28|400, tracking−0,2|
| Acción preparada |16/22|600|
| Resultado |16/22|600, tracking−0,1|
| Guía y acción footer |13/19|400|
| Contexto/metadato |12/17|400; nombre de app600|
| Grupo |12/17|600, tracking0,15|
| Atajo |11/15|400, definido sin añadir keycaps decorativos|

Geist/OFL existente, sin nuevas fuentes ni descargas en ejecución. Se mantiene
la escala accesible del sistema; los valores no fuerzan una densidad física.

## Identidad instalada y fixtures explícitos

La UI conserva nombre e icono del Result real producido por el motor desde
PackageManager. No se dibuja un logo fijo en el componente ni se sustituye una
identidad instalada por una marca inventada. El acento se deriva del mecanismo
existente InspectorAccent (marca conocida o color del icono) y se corrige para
contraste sobre las superficies usadas.

En este laboratorio se instalan **fixtures** Spotify/Vivaldi7.2-fixture y se
registran iconos sintéticos en PackageManager: monogramas S y V dibujados con
Canvas Android, no logos comerciales. Se comprueba que el contexto usa el mismo
nombre y Drawable que ofrece PackageManager. Ambos lados de la comparación
usan idénticos iconos sintéticos, para no atribuir a la composición un cambio de
identidad. El pie externo lo declara en todas las capturas.

La paleta y extracción de acento están memorizadas por tema/aplicación/icono:
no se analiza un bitmap por cada tecla. No se declara ahorro de FPS/RAM medido.
La extracción existente usa una copia privada acotada; no altera el icono compartido.

## Arquitectura y contratos conservados

FocusSearchState, consultas/borradores TextFieldValue, LazyListState, callbacks,
SearchEngine/CompactResults/AppCapabilities/ActionCatalog y sus gates se comparten.
No hay segundo campo ni chat, nueva ventana o motor. En las capturas se comprueba
incluso que el ID semántico del campo sobrevive al cambio antes/después.

Spotify usa exclusivamente el enlace público resoluble de búsqueda; Vivaldi su
búsqueda web con paquete explícito. Se conserva preparación/revisión, cancelación,
revalidación del receptor, bloqueo si desaparece la capacidad y confirmación única.
No OAuth/API de catálogo por tecla, ni integración profunda nueva ni permisos.
La explicación plegable dice que no se leen cuenta/resultados internos de Spotify,
ni pestañas/historial de Vivaldi. No se quita ningún motor/integración de producción.

El callback final se registra en el harness. «Abrir en Spotify/Vivaldi» expresa
la acción solicitada, **no** un efecto verificado. No se llama startActivity desde
estas pruebas. NativeLaunchSession, WindowManager y keymap completo de producto
siguen pendientes de integración tras aprobación; esto no es una APK utilizable.

## Verificación

- 4 casos visuales nuevos: antes/después en los4 perfiles, estado idéntico, icono/
 nombre PM, un campo/nodo conservado, consulta no duplicada, destino del contrato,
 explicación reversible y límites del footer/ventana. Contraste4,5:1 en cada tema.
- 8 regresiones existentes ejecutadas **con el acabado activado**: recorridos,
 cursor/foco/consulta/selección, scroll no nulo/borrador, capacidad/receptor revocados,
 confirmación única e IME Search, texto al160%. Son8 reutilizadas, no8 nuevas.
- No sumar rondas repetidas a las12 identidades distintas. XML/log finales en raíz.
 No se repite toda la suite C2 al no cambiar sus fuentes/ejecutores.

Ronda visual inicial y regresiones previas a memorizar la extracción de acento se
conservan por trazabilidad. Tras esa corrección se verifica la fuente final con
ambas clases y Lint. No se usan tiempos de Gradle como latencia de búsqueda.

## Límites y siguiente decisión

No mediciones físicas de jank/p50/p95/RAM/batería, Gboard/One UI, TalkBack real o
teclado físico. La prueba al160% es de laboratorio. Las incidencias62/63 de ventana/
IME siguen abiertas hasta dispositivo. No atribuir los fixtures a apps comerciales.

Se entrega esta revisión para aprobación visual y se detiene el trabajo. No adoptar,
preparar APK o extender a otras pantallas por aprobación conceptual de arquitectura.
Sólo se libera la reserva propia conforme al protocolo, con notas del checkpoint.

## Medidas finales

| Estado | Tablet antes/después dp | Teléfono antes/después dp |
|---|---:|---:|
| Inicio |236/234|236/234|
| Global Spotify/Vivaldi |298/292|298/292|
| Contextual vacío |226/181|244/181|
| Texto y acción preparada |283/212|301/212|

Igual en oscuro/claro. Footprint incluye12dp arriba/abajo; superficie pintada24dp
menos y ancho928/328dp. Son medidas de layout, no área de tinta ni métricas GPU;
la sombra no modifica el presupuesto de layout. CSV por perfil y geometry.json.
Ningún estado de esta muestra crece para aparentar sofisticación. La guía plegada
es el estado mostrado: expandir información añade contenido real bajo demanda.

Se corrigió sólo metadata de los fixtures para declarar también el recurso de
icono: el inicio ya no cae al icono Android por defecto mientras el contexto usa
un monograma. Se verifica igualdad de bitmap de la sugerencia con el icono PM.
La ronda final de4 visuales y Lint valida ese ajuste; las8 regresiones se conservan
de la verificación de la misma fuente de componente final, sin repetirlas tras
un cambio exclusivo al instalador de fixture de la otra clase. No son12 pruebas
nuevas:4 nuevas +8 regresiones reutilizadas, todas correctas.

Lint final: 0 errores / 27 advertencias / 1 información.
Ronda final correcta en1m59s. Reporte UP-TO-DATE tras análisis sin cambios de
hallazgos; los tiempos de build no miden rendimiento de producto.

Reproducción local del conjunto completo (sin tareas APK):

```sh
SPOTLIGHT_FOCUS_FINISH=true JAVA_HOME=/workspace/tooling/jdk17 \
ANDROID_HOME=/workspace/tooling/sdk bash gradlew :app:testRaycastPreviewUnitTest \
 --tests dev.bautista.spotlight.ui.FocusProductFinishTest \
 --tests dev.bautista.spotlight.ui.FocusSearchReviewTest \
 :app:lintRaycastPreview --continue --no-daemon --max-workers=2 \
 -I scripts/directed-tests.init.gradle \
 -PspotlightDirectedClasses=dev.bautista.spotlight.ui.FocusProductFinishTest,dev.bautista.spotlight.ui.FocusSearchReviewTest
```

Configurar proxy/truststore del laboratorio si corresponde. La variable sólo
activa presentación en un harness de prueba: no hay preferencia de app ni entrada
productiva nueva. XML de raíz contiene las últimas12 identidades correctas.
