# Solid Graphite · escena maestra ·23/10/2026

Solicitud aprobada: instrucciones adjuntas `Texto pegado(20261023-144832).txt` y referencia `Paleta de comandos Spotify futurista(1).png`. Este encargo sustituye el material predeterminado anterior; el maestro funcional de40decisiones permanece vigente. Base remota3fc5a402, PR18 borrador, misma rama y propietario de reserva. Checkpoint inicial PARCIAL: implementación escrita, todavía sin pruebas ni fidelidad visual certificadas.

## Medidas y decisiones
Referencia1586×992px: superficie principal aproximadamente(234,135)–(1352,851), ActionPanel(876,195)–(1414,778). Con ancho canónico740dp, escala tentativa1.51px/dp: barra≈59dp, filas≈48dp, título≈14sp, panel≈356dp, esquinas≈14–16dp. Son medidas del render, no del dispositivo. El contenido alto del render no justifica inventar resultados para mantener altura: la paleta real conserva altura adaptativa.

Muestras RGB del render: superficie(20,24,44), selección(45,51,71), ActionPanel(18,21,26), selección acción(45,46,56), filtro(38,40,51). El tinte azul de la primera superficie está influido por el fondo: la solicitud prioriza grafito neutro, no incorporar ese wallpaper.

Cinco defectos concretos detectados en fuente: degradado azulado fijo incluso enpreset Graphite; default84%permite leer ajustes detrás; filas con metadatos a la derecha en lugar de subtítulo; ActionPanel390dp con borde9%y sombra12dp poco diferenciado; barra y comandos comunes mezclanHugeicons/Lucide conIcons8delinspector. Corrección incremental enGlassTokens,AppearancePresets,Preferences,GlassAppearance,SpotlightWidgets,SpotlightBar,ContextActionSurface,ActionPanelView eIosCommandIcon.

Default nuevoSolidGraphite:98%/100%/99%, fondo1B1B20→17171C, panel19191D, Inter existente, selección gris. DarkGlass sigue disponible con84/92/94. Preferencias explícitas guardadas NO se sobrescriben: instalaciones actuales cambian al elegirSolidGraphite enApariencia; no migración destructiva ni heurística que confunda una elección de84%con un default. La opción material aplica valores coherentes; sliders existentes conservan efecto inmediato y persistencia. No nuevo esquema de respaldo.

Barra56dp, filas mínimas48dp con título/subtítulo y estado multimedia solo cuando existe; panel técnico hasta360dp conpadding8, radio16dp/sombra20dp/borde17%, filtro interno48dp, grupos discretos. La lista principal y su posición permanecen intactas. Si las acciones exceden el viewport, solo el panel permite scroll. No nueva ventana overlay independiente ni cambios a flags/foco/IME.

Icons8iOS50px autorizados con atribución vigente enAjustes yNOTICE; ampliados a comandos comunes del buscador/panel. PNG oficiales no SF Symbols; PackageManager conserva logos reales. La migración de iconos especializados fuera de esta escena sigue parcial: no se afirma sustitución total. No librerías nuevas.

## Android y transparencia
Documentación oficial consultada23/10/2026: https://developer.android.com/reference/android/view/WindowManager y https://source.android.com/docs/core/display/window-blurs. El desenfoque entre ventanas puede no estar soportado y puede desactivarse dinámicamente. OverlayService ya evitaFLAG_BLUR_BEHIND por su huella rectangular enOneUI; se conserva. NoRenderEffect para fingir blur externo: la superficie grafito aporta legibilidad sin depender del compositor.

## Verificación pendiente en este checkpoint
Runtime anterior eliminado por mantenimiento: Java17 disponible, Gradle8.10.2/SDK35/BuildTools35 restaurados con hashes oficiales. NoJava/Gradle previo activo. Baseline nativa desde fuentePreview41 en ejecución antes de copiar cambios albuildaislado. Pruebas nuevasSolidGraphiteTest escritas para material,contraste,DarkGlass yconservación depreferencias. No se modificaronaserciones para verde. No capturas actuales deAPK adjuntas en este mensaje: comparación antes/después se obtendrá deRobolectric33/NATIVE; no dispositivo físico.

NoAPK nueva niCI/merge/force. Próximo: finalizarbaseline, publicarcheckpointparcial, capturascomparables ygate dirigido; compararregiones/escalas, overlay50%ymapadediferencias conlimitación por contenido real. No porcentajedefidelidadinventado.

## Iteraciones verificadas y segundo checkpoint
Primera ronda20/20correctos:SolidGraphite4,SettingsPersistence4,NativePalette12. Captura confirma que ya no se transparentan resultados a través delActionPanel, pero la última fila estaba parcialmente recortada por cinco encabezados. Corrección dirigida: agrupación visual de apertura/comunicación bajoPrincipal ycopiado bajoOrganizar encontextoApp; no cambia identidad/orden/ejecutor. Segundo gate18/18correctos:ActionPanelKeyboard6,ResultTouchScroll7 y5NativePaletteafectados. Nueva aserción verifica queInformaciónApp quedaenteramentevisible sin scroll en tablet. Se conservaron todas las asercionesprevias; los encabezados esperados pasan amayúsculas por elestiloaprobado.

Ajuste medido: campo52dp+8dpverticales=barra60dp; el valor56dpdelprimercheckpoint daba64dp. Se preparó elencabezadoRESULTADOS con28dpdepresupuesto adicional, sinforzaraltura nidatos. PreparadaPreview42 independiente ycaptura exactaSpotify: noDescubrimientoSemanal si no hayfuente. Última rondaNativePalette13 aún pendiente alpublicar estecheckpoint; R8/lint/APKnuevos aún pendientes. Evidencia de20y18 casos, baseline eiteraciones se conserva en docs/verification/solid-graphite-20261023.
