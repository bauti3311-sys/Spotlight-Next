# Puente ligero — integración nativa del H02 elegido

El propietario eligió H02 y autorizó su implementación, con evaluación del renderizador si fuera necesario. Se mantiene Compose: las superficies independientes en apariencia, materiales, jerarquía y adaptación se implementan con los componentes existentes, sin migrar motores ni agregar WebView. La valoración estética final y la validación física siguen abiertas; las capturas permiten juzgar el resultado.

Checkpoint productivo publicado: `0b1441e85cc736026f5bee81e1c23eef869e70c1`. Evidencia y continuidad se publican en un commit posterior.

Base remota verificada: `928705de91cadd4629d737d724e4ac5d3dda4f8c`. PR21 abierto/en borrador y reserva nueva propia antes de editar. La credencial GitHub volvió a ser válida; se conserva H02 del laboratorio web que no se había podido publicar.

[Antes/después Compose y referencia web](verification/puente-native-20261108/Galeria-Puente-Nativo.html) · [PDF](verification/puente-native-20261108/Puente-Nativo.pdf).

## Implementación

- Nuevo PuenteFrame con slots para barra, resultados, footer y contexto. No crea estado de búsqueda, ejecutores ni ventanas Android adicionales. La búsqueda sin contexto es compacta (máximo nominal560dp×escala efectiva), centrada dentro del ancho configurado; se expande al revisar un destino. Dos superficies legibles con alto natural, separación18dp y material casi opaco sin blur. Fondo claro Hielo y equivalente nocturno; se conserva Geist y la elección de fuente/acento existente (el acento azul predeterminado toma el teal de H02).
- Con ancho útil suficiente se reutiliza ContextSplitPanel: conserva divisor ajustable, límites mínimos y acciones accesibles de ampliación. En el umbral la separación baja a12dp antes de quitar espacio útil; se redondea a píxeles enteros en vez de truncar y perder1px de mínimo. Variante separada con proporción inicial44/56, ajustada por la política existente; no se impone un ancho global ni se cambia el slider del overlay.
- En estrecho, superficies apiladas. Barra/footer permanecen accesibles, resultados y contexto se acotan al espacio real. En ventanas muy bajas puede no quedar espacio para filas mientras se revisa un inspector: se conservan la consulta y selección, sin hacer controles imposibles de tocar. No es un modo nuevo.
- La identidad de aplicación precede al botón Abrir aplicación. El botón llama al mismo `execute(application)` y NativeLaunchSession; se omite únicamente su duplicado visual del footer mientras el contexto de app está visible. Enter sigue usando el mismo dispatcher. Las demás acciones, recibos, administración plegable, pin y capacidades continúan accesibles.
- Se conserva el controlador de consulta/selección y se mueve la barra/inspector con movableContentOf. Los formularios y tareas conservan su recorrido existente; no se reimplementan Spotify, WhatsApp, Square o C5. ActionPanel conserva su ejecución y navegación existentes; esta fase no reproduce toda su composición web.
- La paleta se aplica al overlay Focus, no al cuerpo de Ajustes: ancho tablet, tres temas y preferencias intactos. Focus=false sigue siendo el valor por defecto; desactivarlo vuelve al recorrido anterior. No se añade otra preferencia o migración.

## Defectos encontrados y corregidos por las regresiones

1. Primer contenedor móvil tenía scroll externo e interno: la versión y los controles de administración podían quedar fuera de vista incluso tras desplazar el contexto. Las pruebas originales FocusB y una nueva prueba con acción explícita fallaron. Se eliminó el scroll externo y se asignó al contexto el espacio restante; sus controles se alcanzan con scroll sin ejecución accidental.
2. Cambiar de formulario a buscador podía entregar una actualización tardía del campo desmontado con searchForm=null, provocando NPE. Se ignoran callbacks contextuales cancelados o de otro destino, sin resucitar borradores ni escribirlos en el campo global.
3. Tras recuperar correctamente el texto, el campo podía perder foco al volver o desactivar/reactivar Focus. Se solicita foco después de la colocación mediante el mecanismo de revisión existente. El contenedor del buscador permanece estable al variar su ancho, evitando otro desmontaje en el estado compacto. Los seis casos originales FocusB pasan sin rebajar aserciones ni modificar sus fuentes.
4. Una prueba nueva de frontera696/700dp reprodujo que18dp decorativos y truncamiento de píxeles reducían los mínimos de las columnas. El gap cede antes que contenido y el redondeo conserva los mínimos; la misma prueba pasa.

Las primeras fallas y las rondas correctas se conservan separadamente. También se registran un import faltante en la prueba nueva y un orden incorrecto de argumentos Gradle, corregidos antes de la ejecución final. El error de CLI no ejecutó lint; no se suprime lint ni se crea baseline.

## Verificación dirigida

20 casos únicos finales: NativeIntegration1, PuenteFrame3, ContextSplitHeight2, C2AppOpenJourney6, StudioActionKeyboard2 y FocusBIntegration6. La ronda final ejecuta los20 casos sobre la fuente final. Las rondas anteriores quedan como evidencia de reproducción y corrección; no se suman sus repeticiones al número de casos únicos.

Cubre solicitud única, destino desaparecido, permiso denegado, regreso sin fallback, consulta/selección/inspector, administración, borrador/selección de texto/restauración, desactivación de Focus, Enter/Tab/Escape y acciones. Son pruebas JVM/Compose con PM y receptores ficticios; un Intent solicitado no equivale a efecto externo verificado.

12 pares nativos equivalentes: tablet1200×800 y teléfono412×860, claro/oscuro; compacto330×760/texto200% en ambos temas. Dos escenas: resultados y app seleccionada. Antes y después usan SpotlightPanel real, tres apps ficticias de PackageManager, la misma consulta `Proyecto` y configuración sin archivos/contactos/online/aprendizaje. No interviene C5. Animaciones variables desactivadas únicamente para capturar; producción140ms preservada.

La referencia H02 web usa4fixtures distintos y dos tamaños equivalentes; se identifica como Chromium, no Compose ni APK. La comparación con la web no afirma igualdad pixel a pixel o de datos. Las dimensiones de ventana nativa respetan las políticas/anchos existentes, no el ancho fijo de890px del prototipo.

La galería offline valida28imágenes,6perfiles/2escenas,4comparaciones de referencia, zoom/Escape y390px sin desborde. Carga mediante set_content del HTML autosuficiente; navegaciónfile:// no verificada por política del navegador administrado.

LintDebug global: 0 errores, 23 advertencias y 1 información. Resultado exacto en final-checks/lint-summary.json y log; se completan dos rondas: primera integración y ajuste compacto/de frontera final, cada una tras un delta productivo distinto, sin supresiones.

## Preservación y límites

Sin cambios de SearchEngine/ResultRanker/CompactResults/HomeSuggestions, IDs, títulos2:1, permisos, datos, firmas, paquetes, versiones, dependencias, WindowManager, apertura140ms, integración de C5 o guardas de entrega. NativeLaunchSession e IntegrationRegistry intactos. Todos los catálogos y checkpoints anteriores preservados. Fuentes69/68 y main comprobadas sin avance.

Es separación visual en una única ventana: no se declara click-through entre superficies. Falta observar OneUI/Gboard, teclado físico/IME, rotación/ventana flotante, TalkBack Android, lifecycle real, fluidez y RAM. Las pruebas JVM no cierran esa aceptación. No se generó APK/testAPK, no se firmó, instaló ni lanzó Actions. No se prepara una Preview nueva ni se reutiliza autorización69.

Reproducción: Java17/SDK/caché local existentes, scripts/directed-tests.init.gradle y guardia no-apk de docs/verification/c2-app-open-20261107. Los comandos y los XML de cada ronda están en la evidencia. gallery.py y pdf.py leen originales; no reconstruyen la UI mediante CSS. La reserva propia se actualiza tras cada checkpoint y se libera atómicamente con nota de cierre.
