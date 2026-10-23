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


## Checkpoint estable · Preview42
Código APK/ZIP **be76e8ed54177a44df14930d627009c953ef7f7d**. Pruebas actuales:34casos únicos correctos en rondas20+18+13 (51ejecuciones), más1baseline anterior; no se reutilizan las27 dePreview41 como resultado de este cambio. Lint actual0errores/14advertencias/1informativa. Aviso adicional:inspector_ios_link sinuso; se conserva informado yR8elimina recursos no utilizados. Gate lint+R8/shrinkResources/assembleRaycastPreview correcto5m54s. No se desactivaron pruebas ni controles.

Paquete dev.bautista.spotlight.next.raycastpreview, versionCode42, min26/target35, ARM64 y no debuggable. Firma final externa con respaldo privado histórico, certificado885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba; V2 yzipalign16KB correctos. ZIP1435blobs Git cotejados y449entradas idénticas entre fuente/build. Principal yReview intactas; PR18borrador; noActions/main/force.

### Cinco diferencias corregidas con evidencia
1. Material:el fondo delActionPanel ya no deja ver los títulos/resultados detrás enSolidGraphite.
2. Capas:fondo19191D,borde17%,radio16dp y elevación20dp distinguen panel ypaleta sinblur externo.
3. Jerarquía:filas con título/subtítulo, encabezadoRESULTADOS discreto eInter existente; no nuevas tarjetas.
4. Densidad:gruposdeacciones compactados sin alterar identidad/ejecución; última acción habitual totalmente visible en tablet sin scroll, comprobado por aserción de límites.
5. Coherencia:Icons8iOS en controles comunes,búsqueda/copiado/favoritos/info/abrir,conatribución; logos de apps siguen PackageManager.

### Comparación real y límites restantes
Hay antes/después con la misma consulta `spot`, selección yhost1280×800; además escenaexactaSpotify yteléfono390×844. Referencia yCompose se comparan por recorte delActionPanel yescala uniforme de ancho; superposición50%ymapadediferencias sininventarporcentajedefidelidad. Roborazzi33/NATIVE, paquetesfixture eiconos genéricos, no un Spotify real. El render tiene playlists/MediaSession no disponibles enfixture: no se inventaron para igualar altura. La etiqueta de búsquedaweb muestra elfallback delresolver delpaquete de prueba, no una capacidadSpotifycertificada.

Se detectó quecaptureRoboImage de nodo de subventana captura la raízActivity en este runtime. Esas imágenes históricas se conservan como diagnóstico, pero la comparación usa el recorte delpopup en la captura real de todaslasventanas. ElROI está registrado en captures.json; no se sustituyeron iconos ni superficies por renders.

Diferencias visibles pendientes:filas deacciones mínimas48dp son más altas que elrender normalizado; paleta se acorta con2resultados reales; agrupación ytexto corresponden acapacidades reales; noacentoSpotify fuera delinspector; hostActivity no valida centrado/sombras/composición entreventanas deOneUI. La pantallaAjustes comparte tokens yperfil, pero no se declara auditada visualmente en todas sus secciones en este bloque. Tampoco migración total de iconos especializados. **La fidelidad visual definitiva yC5 siguen abiertos.**

En instalaciones actuales se preserva elmaterial/las opacidades guardadas. Activar **Apariencia→SolidGraphite** aplica98/100/99; DarkGlass sigueopcional con84/92/94. Nuevas instalaciones usanSolidGraphite. No se sobrescriben ajustes manuales mediante una migración ambigua.

### Continuidad
Runtime restauradoen/workspace/spotlight-solid-runtime-20261023, buildaislado en/workspace/spotlight-native-build-20261023. Fuente canónica conservaGitlocalbase4bb1b70 con cambios ya publicados: no descartar ni publicar esa base. SDK35r02/buildtools35/Gradle8.10.2 restauradosconhashesoficiales,sindependenciasnuevas. Comparador y scripts de laboratorio se preservan en evidencia privada; keystore/credenciales nuncaenGit/ZIP. Siguiente: comprobación física dePreview42, después ajustar diferencias reales yextender revisión de estos tokens aGeneral/Apariencia/inspector, manteniendoelfuncionamiento existente. No hay desarrollo en segundo plano anunciado.

### Hashes entregables
- Spotlight-Next-Raycast-Preview-42-be76e8e.apk: `e65f100c3970615835e65f801714b2892fd375e20a84d204a87aa0bd4b63d4cf`.
- Spotlight-Next-Preview-42-source-be76e8e.zip: `9722941c9590894957460968f26e35385a6612c6c6a4c23ebf1633baf80f1178`.
