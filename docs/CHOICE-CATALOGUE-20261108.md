# Spotlight para elegir — 12 estéticas y 36 mejoras funcionales

Pedido del propietario: ampliar de forma visible el abanico de opciones, sin otra
ronda de implementación antes de elegir. Base PR21 `dd5e595a58593dd0226ed2ac5edf9d14914fd252`.
Preview69 ya entregada, autorización consumida; no nuevos builds/Actions/instalaciones.

## Entrega de revisión

`/workspace/shared/downloads/spotlight-elegir/Elegir-Spotlight.html`: galería
**autocontenida, offline**, con 348 capturas Compose incrustadas, filtros, ampliación,
comparación de dos estilos y selección independiente de estéticas/funciones.
`Catalogo-Spotlight.pdf`: 20 páginas. `Muestrario-esteticas.jpg`: resumen para elegir.
Los códigos E01–E12 y F01–F36 permiten combinar componentes y prioridades.
El resumen/JSON se copia o descarga localmente; no hay backend ni transmisión.
El HTML es un documento de revisión externo: no se incorpora al APK ni a WebView.

## Qué son las imágenes

**Capturas reales de prototipos Compose de laboratorio**, no renders de IA, no
screenshots físicos ni una APK nueva. Se reutilizan SpotlightBar, ResultRow,
SpotlightResultList, ApplicationInspector, AppCapabilityActions, ActionPanelView,
GlassContextFooter y los componentes de Ajustes existentes. Datos, apps e iconos
son fixtures explícitos, sin datos personales. E00 es referencia de componentes
actuales dentro del mismo banco, no una reproducción exacta de WindowManager.
No confundir prototipo de presentación funcional con función productiva terminada.

312 vistas estéticas: 13 direcciones incluyendo referencia × cuatro perfiles
normales × cinco escenas + dos perfiles compactos200% × dos escenas. Perfiles:
tablet1000×720dp, teléfono390×760dp, compacto330×760dp/fontScale2; claro/oscuro.
Escenas: inicio, resultados, inspector, acciones y Ajustes; compacto200% sólo
inspector/acciones, explícitamente limitado en el selector. Consulta/lista/selección
iguales; dimensiones interiores y proporciones varían intencionalmente por propuesta.
36 capturas adicionales muestran ejemplos visuales de funciones con el texto
«PROPUESTA · DATOS FICTICIOS». No ejecutan sus acciones ni simulan resultado externo.

## Direcciones propuestas, ninguna adoptada

- E01 Carbon: compacto, inspector secundario y bordes contenidos.
- E02 Pearl: capas suaves, superficies claras, acabados macOS.
- E03 Ink: negro profundo, mínimo borde y selección violeta.
- E04 Editorial: serif, calidez y ritmo de lectura; cambia la familia tipográfica.
- E05 Nordic: resultados arriba, inspector debajo en tablet; cambia composición.
- E06 Terminal: monoespaciada, rectas y acento verde.
- E07 Tonal: tarjetas de resultados en dos columnas en tablet.
- E08 Navigator: máxima prioridad de ancho para lista/títulos.
- E09 Copper: materiales cálidos, cobre y densidad equilibrada.
- E10 Pop: inspector a la izquierda y lista a la derecha en tablet.
- E11 Titanium: estructura compacta y técnica, gris frío.
- E12 Paper: superficies planas marfil y mínimos adornos.

Cada propuesta tiene su contrapartida visible en la galería. En el muestrario se
mezclan temas para mostrar variedad y se rotulan; dentro de la galería/PDF se
comparan condiciones equivalentes. Ninguna modifica la app instalada ni se declara
aprobada. Tampoco se afirman mejoras de RAM/FPS/fluidez sin mediciones físicas.

## Funciones viables, no promesas de capacidades universales

36 opciones en seis familias: cálculo, texto/escritura, archivos/documentos,
agenda/personas, acciones entre apps y organización. Cada ficha declara ejemplo,
valor, estado, esfuerzo relativo, límite y archivo real inspeccionado del proyecto.
Los ejemplos numéricos y nombres/documentos son ficticios. Esfuerzo no es plazo.

«Ampliación» distingue una base existente del recorrido propuesto aún no terminado;
«Nueva acotada» es una extensión local sin motor paralelo; «Condicional» señala
motor, permisos o aceptación física pendientes. Citas C5, OCR, RemoteInput y
MediaSession no se presentan como disponibles universalmente. La recuperación de
citas C5 sigue fuera de la APK69 y mantiene su bloqueo físico. No se implementan
C3/C4/C5 ni se repara Spotify/Square por crear estas fichas.

Fundamentos auditados: Calculator/LocalTools/java.time; TextTools y LocalStore;
SAF/FileIndex/QuickLook/DocumentExtraction; CalendarContract/Agenda; People y
ACTION_DIAL; ACTION_INSERT, ACTION_SENDTO/mailto, ACTION_SEND; AppCapabilities,
IntegrationRegistry/NativeLaunchSession; NotificationListener/RemoteInput,
MediaSession y AlarmClock.ACTION_SET_TIMER. SET_ALARM ya figura en el manifest.
No se descargan modelos ni se leen documentos para esta tarea.

## Comprobaciones y conservación

- Siete escenarios iniciales de generación correctos: seis perfiles estéticos y
  un escenario de36 funciones. Afirmaciones: footer dentro del lienzo, inicio con
  tres elementos, temas existentes accesibles, callbacks0 y ninguna Activity lanzada.
- Rerender focalizado E05/E07/E10 tras ampliar composición: seis perfiles correctos;
  no se repiten suites históricas de aplicación ni funciones. Todos los PNG finales
  quedan versionados con inventario y hashes.
- Chromium/Playwright: 13 estilos,36 fichas, imágenes offline, comparación,
  selección, filtros y límites200%; sin errores JS ni overflow horizontal a390px.
  Navegación file:// del Chromium administrado bloqueada; documento exacto probado
  mediante set_content, sin servicios externos. Esto no limita el PDF descargable.
- **Delta productivo cero**, comprobado contra dd5e595: app/src/main, Gradle,
  dependencias, permisos, versiones, firma, manifiesto de entrega, workflow,
  ranking/C2, C5, Focus Studio/2:1/140ms y fuentes68/69 intactos.
- No lint global repetido: sólo clases/resources de prueba y evidencia/documentación.
  Sin APK, testAPK, firma, instalación, Actions, nuevas dependencias ni servicios.

Código del banco en app/src/test y JSON en app/src/test/resources. Evidencia,
generadores offline, pruebas y originales en verification/choice-catalogue-20261108.
Próximo paso: decisión del propietario por códigos; después se acota implementación.
No adoptar ninguna estética, nueva navegación o capacidad sensible por silencio.
