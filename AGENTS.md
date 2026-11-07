## 2026-11-07 — Focus Studio: correcciones por evidencia física de Preview 67

Prioridad vigente del propietario: legibilidad interna, ActionPanel contenido y
layout compacto del formulario Spotify; sólo presentación, sin reparar integración.
Fuente2731897c80b2d87440a58d9f9b6d08ef1710723e publicada/PR21 borrador.
19/19 dirigidas verdes; lint global bloqueado por StateFlow.value en C5AndroidTest
preexistente e intacto (C5FocusHostDeviceTest:65). Análisis main final completado. Ver docs/STUDIO-PHYSICAL-POLISH-20261107.md y galería
Compose antes/después. Conservar Studio/AjustesA/Geist/D1/140ms/acciones/ranking.
C5 congelado e intacto: NO-GO físico C5.4 y resultados C5.3 siguen vigentes.
No APK/Actions/nuevaPreview/main/permisos/paquete/firma/deps. No declarar validación
One UI/Gboard, rendimiento físico ni nueve V completas por capturas JVM.
Publicar checkpoints pequeños y liberar sólo reserva propia con notas atómicas.

## 2026-11-07 — C5.4: ruta segura, NO-GO de instalación actual

Desde b744ecf/PR21. ADB0, AVD0, sinemulador/imagen/KVM; parDebug/testAPK ausente
y materialfirma local ausente. Perfiles/registroglobal Galaxy/Motorola DESCONOCIDOS:
no aprobar instalación personal por suposición ni por elegir otro perfil.
Ver docs/C5-4-SAFE-EXECUTION-DECISION-20261107.md y evidencia de entorno.
Recomendación física: equipoARM64 de pruebas ya limpio/autorizado; alternativamente
auditar si .next/.next.test están libres globalmente y sólo existe Preview67
.raycastpreview. Coexistencia posible por IDs distintos, NO verificada todavía.
AVD nuevo descartable en host apto sólo ensayo preliminar, no validación física.
No aprovisionar/descargar/compilar/firmar/instalar ni adaptar native-delivery ahora.
C5.3 7/12+5omisiones/0FP y bancoC5.4c2f94e8 intactos; no repetir pruebas.
Sin cambiosproductivos/paquetes/firmas/main/Preview67/Actions. NoC5.5 ni C5completo.
Publicar decisión y liberar sólo reserva propia con notas atómicas.

## 2026-11-07 — C5.4: banco físico preparado, no ejecutado

Desde d712a14/PR21. Cinco métodos AndroidTest preparados: PDF/citas/renderer,
SAF DocumentsUI asistido+provider sólofixtures+pipebloqueante/cancel/revocar,
OCRreal optativo, hostStudio aislado/scroll/copiaenmemoria/lifecycle y corpus
semántico real condicionado por ready/preferencias/recursos. Sin mockdeembeddings.
Compilación Kotlin+Java SINAPK correcta; controles estáticos/fixtures/grafo/APKs
correctos. adb0dispositivos: CERO instrumentación/métricas físicas. C5.3 7/12,
5omisiones/0FP inalterado; ninguna evaluación semántica nueva ni modelo descargado.
Ver docs/C5-4-PHYSICAL-PROTOCOL-20261107.md, fichaaceptación y evidencia asociada.
Target compilado DebugAndroidTest=.next/.next.test; Preview67 NO contieneC5.
Ejecución futura requiere artefactos/target/firma compatibles y autorización
separada. No instalar sobreapp personal, no asumir perfilAndroid protege código.
Guardas pkg/cert/app+test/source declarado; source declarado NOprueba APK.
BDprivada sin acceso amplio, provider sóloficticios; prefs/modelo no se escriben.
App/src/main/Studio/QuickLook/140ms/67/identidad/main intactos. C3/C4/C6–C8 pausados.
Sin APK/testAPK/Actions/permisos productivos/deps nuevas/merge/nuevaPreview.
C5 NOcompleto; físico y unión productiva pendientes. Liberar sólo reserva propia.

## 2026-11-07 — C5.3: evaluación léxica y límite de modelo ausente

Desde16ef8bf/PR21, sóloC5.3. Cuatro casos nuevos correctos, corpus6docs/12consultas:
7aciertos documento-consulta/5omisiones/0FP; recall7/12, MRR0.50 en10positivas.
Modelo DistilUSE verificado NO disponible enentorno; calidad semántica actual NO
evaluada, no cero ni vectores simulados. No descarga de modelos/dependencias.
NOintegrar semántica a citas hasta demostrar mejora auténtica y coste/seguridad.
Ver docs/C5-3-SEMANTIC-EVALUATION-20261107.md y evidencia asociada. Históricos8/8
separados/no reejecutados. Offsets semánticos searchableText !=body de citas;
semanticSearch no relee SHAarchivo; puente futuro debe revalidar C5.2 y procedencia.
DocumentCitationSession/State/PDFextractor/barra/ranking/Studio/140ms intactos.
C5 sigue abierto; sin UI productiva/síntesis/backend/RAGFlow. SAF/OCR/lifecycle
físicos pendientes. C3/C4/C6–C8/Spotify/Square pausados; Preview67 preservada.
Sólo tests/corpus/docs; reutilizar lintC5.2(0E26W1Info), no suites históricas,
Actions/APK/main/merge/version/firma/paquete/prefs/deps/permisos. Próximo: evaluación
con modelo local ya verificado disponible y el mismo corpus, antes de un puente.
Publicar checkpoints y liberar sólo reserva propia con notas atómicas.

## 2026-11-07 — C5.2: PDF auténtico y contrato reactivo aislado

Prioridad expresa: sólo C5.2 desde b80d976/PR21, no reconstruir C5.1.
DocumentText/PdfTextSource/PDFBox reales: PDF3páginas, Unicode, offsets y páginas
comprobados; Poppler independiente coincide. PDF raster real sin capa textual,
OCR explícitamente deshabilitado: no afirmar reconocimiento real. SAF probado con
DocumentsProvider en Robolectric, NO instrumentación: adb sin dispositivos y sin
emulador/KVM. DocumentCitationState efímero sin restauración/persistencia; invalida
por configuración/revisión/eventos del host, revalida antes de mostrar/copiar/abrir.
SAF no tiene aviso universal de revocación: conectar lifecycle/eventos al host antes
integrar barra. Corrección mínima: clear() emite revisión tras borrar índice; rojo
previo conservado. Ver docs/C5-2-PROVENANCE-STATE-20261107.md y evidencia asociada.
14casos nuevos +1regresión correctos; lint0E26W1Info. Fuente84c0e1a.
No UI productiva ni cambios a Studio/QuickLook/ranking/140ms/Preview67/identidad/
prefs/manifiesto/deps/permisos; sin Actions/APK/main/merge. C3/C4/C6–C8 pausados.
C5 NO completo. C5.3 semántica pendiente; primero validar SAF/OCR/host físico y
medir I/O. Preservar11casosC5.1, repetir sólo regresión relacionada. Liberar sólo
reserva propia con notas atómicas; no mover checkpoint integral.

## 2026-11-07 — C5 reabierto: citas locales, primer checkpoint técnico

Decisión posterior del propietario: avanzar únicamente C5 desde293290a/PR21.
Preview67 ya entregada por único intento autorizado; no repetir entrega. Studio
aprobado se conserva; validación física exhaustiva sigue pendiente. C3/C4/C6–C8
siguen pausados. Este bloque prevalece sobre congelamientos históricos de C5.
DocumentCitationSession reutiliza FileIndex/extracción/permisos; citas literales
con SHA actual, offsets y página sólo respaldada por metadatos PDF válidos.
Revocación/borrado/cancelación invalidan referencias; no es todavía un motor de
respuestas ni integración productiva de UI. Recuperación léxica natural existente;
semántica inventariada, pendiente. Propuesta Studio sólo test, apertura explícita.
Ver docs/C5-CITATIONS-CHECKPOINT-20261107.md y evidencia asociada: 11 casos dirigidos
correctos, lint0E26W1Info. Fixtures OCR/PDF, no prueba física ni OCR real. C5NOcerrado.
Sin Actions/APK/main/merge/version/manifiesto/firma/paquete/prefs/deps/permisos.
Próximo: páginas/fuentes reales, evaluación semántica y estado reactivo por revocación.
Preservar checkpoints; publicar y liberar únicamente reserva propia con notas.

## Preview67 — candidata congelada, pendiente de autorización de entrega

Base26377fa/PR21 verificada. Fuente6918204238706d3c9bac826196ad3f1f272a51e7:
code67/name2.0.1-raycast-preview-native57-v67; paquete/certificado histórico intactos.
App/src/main idéntico aStudio3662cf7, conserva commits posteriores. Sólo dos líneas
Gradle de numeración. AGPsinAPK correcto;19checks offline67 correctos. Reutilizar
20casosStudio/lint0E26W1Info, sin suites/galerías históricas. Datos/preferencias/
savers/migraciones sin delta; actualización física sobre66 aún pendiente.
Ver docs/NATIVE67-PREPARATION-20261107.md y checklistA9+/G86. D5aislado140ms intacto;
C3–C8/Spotify/Square/WhatsApp/docfunciones congelados. NoActions/firma/APK/main/merge.
Manifest67 source=base/allowed_delta_paths=[]; workflowescommit posterior de entrega,
par exacto ennotas de cierre. Detener tras publicar par y liberar sólo reserva propia.
Nueva entrega requiereautorización explícita delpar67, reserva NUEVA y único intento
native-delivery AUTHORIZE_PREVIEW_67_ONCE. No reusar autorización/reserva66 o actual.
No cerrar nueveV sin evidencia física ni reabrir composición Studio aprobada.

## 2026-11-07 — Studio aprobado e integrado, Ajustes A conservado

El propietario aprueba BStudio con cuerpoA enAjustes. Fuente 3662cf79f0982dea08e48689f67ec7932f1ca4a9.
Barraenmarcada/seleccióntinte+rail sinotrocontorno/inspectorcard/ActionPanel/footer
integrados en producción. StudioSurface cede márgenes según ancho útil×texto,
no tamaño físico. CuerpoAjustes/temas/Geist/D1/FocusB/acciones/ranking intactos.
30pares reales antes/después,6perfiles/5escenas.20casos dirigidos correctos,
lint0E26W1Info. Ver docs/STUDIO-INTEGRATION-20261107.md. No nueva validación
OneUI/Gboard/WM/fluidez/RAM; requiere entrega futura autorizada, no incluidoenAPK66.
Focus=false/reversible; D5aislado140ms intacto. C3–C8/Spotify/Square/WhatsApp/docs
funcionales congelados. NoActions/APK/main/merge/firma/paquete/version/deps/permisos.
Preservar checkpoints; liberar sólo reserva propia con notas. No repetir rediseño
ni interpretar la integración como autorización para compilar otra Preview.

## 2026-11-07 — Focus como base aprobada; direcciones A/B pendientes

Propietario reporta prueba física66 satisfactoria y conservaFocus como base estética,
NO cierre exhaustivo deOneUI/Gboard ni autorización de adoptarA/B. Basecfb8fa8/PR21.
Propuestas sólo test: ARefinado/BStudio,78PNG/26comparativas,6casos nuevos correctos.
Motores y componentes productivos enfixture; NO APK/SpotlightPanelcompleto. Ver
 docs/FOCUS-DIRECTIONS-20261107.md y verification/focus-directions-20261106.
RecomendaciónB con cuerpoA enAjustes (Bconsumeespaciovertical). Esperar elección;
no otrorediseño, producción/Geist/D1/FocusB/ranking/140ms/identidad66 intactos.
D5noaprobado; C3–C8/Spotify/Square/WhatsApp/documentos funcionalmente pausados.
SinActions/APK/main/deps/permisos/firma. Liberar únicamente reserva propia con notas.

## 2026-11-06 — Preview66 preparada, pendiente de autorización específica

Base7bc8a26/PR21 verificada. Fuente 66d4ea0b28192f84ba26b9a451a8299d90f84cfb:
code66/name2.0.1-raycast-preview-native57-v66; paqueteRaycastPreview/cert885838aa
conservados. App/src/main idéntico a7bc8a26: FocusB/D1/footer/PDF/notificaciones
íntegros; auditoría crítica sin otro defecto reproducible, sin rediseño ni C3–C8.
2casos nuevos266dpinterior/330dp/texto200%claroscuro correctos, lint0E26W1Info;
identidadAGPsinAPK correcta; firma/hashAPK65recomprobados;19guardasofflineverdes.
No repetir35/13/18históricos; imágenes originales conservadas. Ver
docs/NATIVE66-PREPARATION-20261106.md y verification/native66-preparation-20261106.
Manifiesto66 congela source=base y allowed_delta_paths=[]; workflowescommit de
manifiesto posterior, SHAexacto ennota de cierre. NoActions/APK/preflightreal/firma,
main/merge/deps/permisos/servicios. D5sólotest; ventana140/100ms intacta.
C3–C8/Spotify/Square/ranking/acciones/datos congelados. FísicaOneUI/Gboard/A9+/g86
y nueveV aún pendientes. DETENERtraspublicarpar y liberarúnicamente reserva propia.
Una futura entrega requiereautorización nueva delpar66 y reserva nueva; jamás
reutilizar autorización65 ni estareserva. Únicaruta native-delivery, un intento
con AUTHORIZE_PREVIEW_66_ONCE, guardas/aislamientofirma y verificaciónAPK+ZIP.

## 2026-11-06 — Visual First: acciones de notificación legibles

Continuación desde73af4d6/PR21. Defecto reproducido: fila fija ocultabaVolver
(ancho0) en390dp/100% y Sugerirrespuesta en330dp/200%. Ajuste mínimo aFlowRow,
4dp entre líneas; mismos botones, orden, capacidades y callbacks. Tablet1000/100
idéntica byteabyte. Sólo presentación NotificationPreview, no funcionalidadC3.
Ver docs/VISUAL-NOTIFICATION-CARD-20261106.md y verification/visual-notification-20261106.
V06/V08 avanzan; D1/FocusB/D2–D4/Preview65 preservados, C3–C8 pausados. D5aislado,
ventana140/100ms intacta. FísicaOneUI/Gboard/WhatsApp/RemoteInput/FPS/RAM pendientes.
Fuente/pruebas/capturas d342c9ec2e4bf19f2f669a045a2df3184bde2fef.
18casos dirigidos correctos, lint0E26W1Info;4pares originales, teclado en orden.
SinActions/APK/main/merge/firma/paquete/deps/permisos ni funciones nuevas.

## 2026-11-06 — Visual First: footer accesible con texto ampliado

Continuación desde d570a86/PR21. Defecto reproducido: footer700dp/texto200% dejaba
Acciones con ancho0 y crecía308dp. Corrección mínima del breakpoint de footer según
escala Android×preferencia existente: reutiliza distribución compacta, altura52dp.
Sin modificar ancho de Ajustes/overlay, tipografía, colores, iconos o atajos.
V05/V06 avanzan; FocusB/D1/D2–D4 y Preview65 conservados, C3–C8 pausados. D5aislado,
ventana140/100ms intacta. Física OneUI/Gboard/ventanas/FPS/RAM sigue pendiente.
Ver docs/VISUAL-FOOTER-BUDGET-20261106.md y evidencia visual-footer-20261106.
Fuente/pruebas/capturas publicadas d9bc0b53f25b3fb9c21a65cd90b22767dfaffe7d.
13casos dirigidos correctos; lint0E26W1Info;5pares y2validaciones extra.
SinActions/APK/main/merge/firma/paquete/deps/permisos ni funciones nuevas.

## 2026-11-06 — Visual First D1 integrado; D5 aislado

Decisiones posteriores del propietario: D1B aprobado; D2/D3/D4A conservados.
Radios10dp en controles Focus, shapes compartido, opt-out4/8dp; copy descriptivo
coherente sin preferencias nuevas. PDF330dp/texto200%: FlowRow, botones visibles48dp,
callbacks/renderer intactos. 35casos vigentes verdes/lint0E26W1Info;75pares originales,
25fotogramas. Ver docs/VISUAL-POLISH-D1-V06-V08-V10-20261106.md y evidencia asociada.
D5sólo test120ms cancelable/reducción; corrección: helper160ms SIN llamadas
productivas, ventana real140msabre/100mscierra intacta. No afirmar mejoraFPS/RAM.
Sin dispositivo: OneUI/Gboard/PDFreal/fondo/fluidez pendientes;9Vno cerradas físicamente.
FocusB/Geist/Ajustestablet/selección/iconos por rol, Preview65 y C3–C8 preservados.
C3–C8 siguen pausados. SinActions/APK/deps/permisos/main/firma/paquete.

## Visual First — prioridad vigente 2026-11-06

Propietario cambia el orden: completar visualmente V01,V02,V03,V04,V05,V06,V07,V08,V10;
NO V09. C3/C4/C5/C6/C7/C8 congelados, conservar todo su código/evidencia y Preview65.
Basef27c867/PR21 comprobada; FocusB,Geist,temas/macOS/Raycast/Obsidian intactos.
Primera entrega auditoría+comparativas REALES Compose sólo en tests, sin integrar
cambios importantes antes de decisión visual. Ver docs/VISUAL-FIRST-AUDIT-20261106.md
y verification/visual-first-20261106.136PNG,8perfiles,72pares de escenas,12casos verdes.
Inicio/resultados/inspector son SpotlightPanel vigente; periféricos son componentes
en fixture. B sólo MaterialTheme/anchos de test. IME simulado, nunca OneUI/Gboard real.
Recomendación: conservarA/FocusB; radiosB en campos; ensayaropacidad120msB; NO adoptar
ancho/títulos/neutralizaciónB global (empeoraAjustes tablet). Aprobación aún pendiente.
NO producto nuevo, deps/permisos/servicios/motores, Actions/APK/main/firma/paquete.
Tras decisión: integrar incremental, comparar antes/después y validarinput/restore.
No reanudar funciones ni rediseñar inspector B por esta auditoría. Reserva propia
se libera con notas; checkpoint integral no avanza por una galería.

## Continuación C3 y Square aislado — decisión posterior 2026-11-05

Preview65 YA entregada: run42130468596 attempt1 success, fuenteffc74d2/workflow48ddfff;
ver notasf7d0209. Textos de bloqueo de publicación inferiores son históricos.
Propietario probó ambas rutas Spotify en65 y reporta reproducción fallida: PAUSAR
reparación App Remote/Web API; conservar código/evidencia. No afirmar audio real.
Autoriza estudio Square aislado sin GPL/librespot/deps productivas/permisos, y C3a
contratos/pruebas/correcciones seguras sobre mecanismos existentes. Ver
C3-PROGRESS-20261105.md y experiments/SQUARE-MEDIA3-FEASIBILITY-20261105.md.
Square sólo auditoría+fixture de protocolo: NO Media3 compilado/Binder/audio reales,
no producto ni UI nuevos. C3 revisión efímera consumida antes del efecto, E164
revisado y RemoteInput vigente. No nuevos envíos sensibles ni reales autorizados.
No cerrar aceptación física C3 ni C0/C1/C2. Focus B,65/checkpoints, main/firma/paquete
íntegros; no Actions/APK. Seguir C3; C4 pausado, C5 siguiente vertical independiente.

## Preview65 — preparación local, publicación bloqueada 2026-11-05

Fuente local ffc74d291a8a13ac1d62c5ddfb989bb90e3b6fe7: numeración65 y
controles Spotify existentes extraídos para3 pruebasCompose sin diseño nuevo.
15 casos dirigidos correctos, lint0E26W1Info,19 checks offline entrega/identidad.
Ver docs/NATIVE65-PREPARATION-20261105.md. Fuente y tooling aún NO publicados:
dos pushes rechazados commit_refs; alternativa API Git HTTP503 antes del primer
blob. Remoto sigue87c75aa/PR21 borrador. No retry infinito/force/Actions/APK.
Recuperar transporte y verificar HEAD/reserva antes de publicar fast-forward y
presentar par remoto fuente/workflow para autorización nueva de65. Focus B,
homónimos, Web/AppRemote, firma y paquete íntegros. C3–C9 después de física65.

## Spotify Web API — corrección acotada autorizada 2026-11-05

Desde4546c55 (Focus B completado, conservarlo), ruta explícita de reproducción en
el dispositivo activo mediante SpotifyPlaybackClient.playSelection y comprobación
Web posterior manual. Mantener App Remote separado y local por defecto; no fallback,
transferencias, reintentos ni polling. Scopes guardados/renovados obligatorios antes
del control; dispositivo vivo activo/no restringido. Confirmación única sin cola.
Ver docs/SPOTIFY-WEB-ACTIVE-20261105.md y evidencia dirigida. Timeout App Remote
físico sigue sin causa confirmada; no modificar SDK/firma/redirect por hipótesis.
No incluido en APK64, no Actions/APK nueva, Square/Outify ni permisos/dependencias.
No rehacer Focus; C3–C9/plan23 conservados sin cierre físico ficticio.

## Focus B integrada — aprobación expresa 2026-11-05

AmbasB autorizadas e integradas en8f6a5b4, desde677d9d8, PR21. Ver informe
FOCUS-B-INTEGRATION-20261105.md. B ya no está pendiente de decisión ni es sólo
prototipo: búsqueda visible/disponible/preparada y deduplicada, administración
plegable, app/sitio separados. Focus=false y recuperación conservados.15casos
JVMdirigidos/lint0E26W1Info;330dp,texto200%,dos temas,teclado/retorno/restauración.
CarreraUIde restauración corregida sin tocar motor/ranking/NativeLaunchSession/WM.
Homónimodf5bd77 y su regresión intactos. NO incluido enAPK64; no nuevaAPK/Actions.
C3/C4 son lo siguiente según contratos/aceptación, sin volver a rediseñar estas
piezas. No activarFocusglobal, main, merge, firma/paquete ni capacidadesnuevas
por esta aprobación. Validación físicaOneUI/Gboard/TabA9+/moto g86 pendiente.

## Focus desde64 — revisión y contratos, decisión pendiente 2026-11-05

Ver docs/FOCUS-CONSOLIDATION-REVIEW-20261105.md y contratosC3/C4 asociados.
B inspector y revisión contextual son prototipos en tests, NO adoptados ni activos.
Preservar Focus=false, recuperabilidad y estilos; no otraAPK/Actions. Defecto menor
reproducido: cambiar contacto no invalidaba revisión; corregido en df5bd77 con
regresión roja/verde. No está en laAPK64.11casos dirigidos/lint0E26W1Info.
C0/C1/C2 físicos pendientes. C3/C4 sólo diseño, sin capacidades productivas nuevas.
Próximo paso: decisión del propietario sobreB; luego integraciónacotada opt-in,
disponibilidad/deduplicación y altura330dp/textoampliado/roles/teclado/retorno.
No activarFocusglobal ni avanzarC3 sin sus condiciones deaceptación y autorización.

## Preview64 entregada — 2026-11-05, física pendiente

Par808bda2/5b346bc expresamente autorizado, único run42082398659 attempt1 success.
APK/ZIP/firma/procedencia comprobados independientemente, certPreview885838aa,
code64. Ver docs/NATIVE64-DELIVERY-20261105.md. Focus=false intacto, sin adopción
global ni validación OneUI. Autorización consumida: no otro dispatch/re-run/fallback.
Reserva propia cerrada con notas; al reanudar comprobar remoto. Main/checkpoints/C3–C9
intactos. Siguiente: instalación sobrePreview existente y aceptación física.

## Preview64 — preparación autorizada 2026-11-05

El propietario autorizó preparar64 desde la integración nativa E1–E5, conservando
Focus desactivado por defecto. Esta autorización posterior permite numeración y
manifiesto sin adoptar globalmente el diseño ni cerrar su aceptación física.
Ver docs/NATIVE64-PREPARATION-20261105.md. No APK, firma, preflight real ni Actions
hasta autorización expresa del par exacto fuente/workflow publicado. No Nuxt/WebView,
main ni cambios estéticos. PresupuestoUS$0 y certificado Preview885838aa intactos.

## Renovación integrada · 2026-11-05

E1–E5 integrados como candidata productiva reversible desde bb7096c; Focus desactivado por defecto hasta aprobación de las comparativas. Pruebas dirigidas y Lint0E/26W/1Info. Sin APK/Actions/C3–C9. Ver docs/FOCUS-INTEGRACION-E1-E5-20261105.md; defectos físicos One UI abiertos.

# Spotlight Next — instrucciones para Codex

## Focus — acabado de producto05/11, arquitectura conceptual aprobada

El propietario aprobó el campo contextual y su recuperación, NO el diseño final
ni la adopción global. Pulido aislado sobre6d9289c: consulta/tarea protagonistas,
explicación secundaria plegable, identidad PM y monogramas de fixture explícitos,
Geist, grafito/marfil y footer integrado. Cuatro estados para Spotify/Vivaldi,
comparaciones reales antes/después en4 perfiles. Sólo app/src/test, sin APK/Actions
ni C3–C9. Ver docs/verification/focus-product-finish-20261105/README.md.
Conservar C1/C2/main/checkpoints, verificar restauración, liberar reserva propia
y detenerse para aprobación visual.


## Focus búsqueda — revisión aislada05/11

B771 es dirección preferida, SIN adopción global. Nueva revisión del recorrido
de búsqueda: Geist/escala completa, inicio, Spotify/Vivaldi global y contextual
en un único campo editable, retorno con consulta/foco/selección/scroll. Sólo
app/src/test, capacidades públicas existentes, sin resultados privados inventados
ni C3–C9. Ver docs/verification/focus-search-20261105/README.md. No APK/Actions
ni otras pantallas; detenerse para revisión y liberar sólo reserva propia.


## A/B estructural Vivaldi — revisión pendiente05/11

El propietario NO aprobó dee1efd (6,3/10); conservarlo. Sólo prototipos aislados
Compose de Unified Command Surface y Spotlight Focus en app/src/test, cuatro
perfiles con los mismos datos/dimensiones y comparación63. Sin adoptar, extender
a otras pantallas, cambiar código productivo, APK/Actions ni C3–C9.
Ver docs/verification/structural-vivaldi-20261105/README.md. Detenerse tras ocho
vistas nuevas para aprobación visual y liberar sólo la reserva propia.


## Liquid — vertical Vivaldi autorizada05/11

El propietario eligió Obsidian Liquid con jerarquía editorial y footer discreto.
Sólo primera escena Compose real Vivaldi/inspector en tablet y teléfono, claro/oscuro,
comparación Inter/Geist frente a presentación63. LocalLiquidScene=null por defecto.
No extender a otras pantallas ni adoptar globalmente, APK/Actions o C3–C9.
Ver docs/verification/liquid-vivaldi-20261105/README.md. Reserva propia se libera
al cerrar este subhito; no tocar reservas ajenas. V1–V4 conservada.


## Preview63 entregada —05/11, física pendiente

Autorización expresa de UN intento ef4e60f/b4bd17e ejecutada correctamente en
run41947253299, attempt1. APK63/ZIP verificados independientemente, certificado
histórico885838aa, paquete Raycast intacto. Ver docs/NATIVE63-DELIVERY-20261105.md.
No otro intento/re-run/fallback ni usar71d6980/a82af60. Sin código nuevo/main/C3–C9.
Siguiente: propietario instala sobre62 sin borrar datos y valida Tab A9+; no dar
OneUI por aprobado con28 pruebas/Lint. Reserva propia liberada con notas al cerrar.

## Corrección física62 y nueva candidata63 —05/11

El propietario probó62 en Tab A9+ y reportó distribución/altura con Gboard y
ausencia de regreso táctil desde opciones. Corregir sólo estos problemas con
pruebas dirigidas/Lint/capturas Compose comparables; C1/C2 íntegros. El par
71d6980/a82af60 queda histórico PAUSADO, no autorizado para compilar. Preparar
par nuevo tras verificar; no APK/firma/Actions/main/C3–C9. La compilación y
ejecución de pruebas locales sí están autorizadas para estas correcciones.
Ver docs/TABLET62-REGRESSIONS-20261105.md. La evidencia Compose no acepta OneUI.
Candidata renovada:fuente/base ef4e60fcd94c33279581dc247461f227274e66b1,
28 casos distintos de última evidencia correcta y Lint0E/27W/1Info. El manifiesto
posterior fija ese SHA sin delta; presentar nuevo workflow exacto y esperar
autorización. El par anterior no está autorizado ni debe usarse para63.

## Preview63 — sólo preparación autorizada05/11

C2 técnicamente cerrado en261eef4; física pendiente. Preparar code63 con todas
las funciones acumuladas, sin nuevas features ni C3–C9. Fuente candidata71d6980,
manifiesto preview_delivery.json revisado y checklist en docs/NATIVE63-PREPARATION-20261105.md.
No compilar, firmar, ejecutar preflight real ni Actions hasta autorización expresa
del par exacto fuente/workflow. PresupuestoUS$0 y bloqueo conservados, sin pedir
otra revisión manual de facturación. No main/merge ni firma alternativa. Reserva
propia nueva para el futuro intento; liberar sólo la propia de preparación con notas.
Reutilizar evidencia Android ya publicada y comprobar sólo delta/contratos offline.

## C2 — completar parte comprobable sin dispositivos

Autorización posterior a f17d9b2: completar F07 de archivos autorizados, F14 con
URI content/MIME/grants temporales y matriz técnica, sin C3–C9/main/APK/Actions.
Reutilizar FileIndex, ContextStore, FileBuffer y NativeLaunchSession. Revisión y
confirmación nuevas tras restaurar, sin copias innecesarias ni extracción automática.
Preview62 y su física siguen pendientes; no confundir PM/fixtures con efectos reales.
La ampliación del índice cache es aditiva y debe conservar metadatos/transcripciones.
No nuevos permisos sensibles, acceso general, servicios ni dependencias.

## Continuación C2 autorizada después de Preview62 — 2026-11-04

Preview62 entregada. El propietario autoriza continuar F01/F07/F14/V02/V05/V06
sin esperar la tablet: integraciones públicas reales, búsqueda contextual y
contenido compartido, pruebas dirigidas/Lint/capturas y commits independientes.
Esta autorización sustituye la pausa de ampliación C2 indicada debajo.
Validación física de ventana/IME62 sigue pendiente; no Actions/APK, main, C3–C9,
permisos sensibles, servicios externos ni cambios visuales mayores sin aprobación.
Conservar ejecutores, contratos de ventana y C1. Reserva propia obligatoria.
Cuatro avances publicados hasta567cc84; ver docs/C2-CONTINUATION-20261104.md
y docs/verification/c2-expansion-20261104.112 casos distintos/Lint0E26W1Info;
C2 sigue abierto y no se generó una APK posterior a62.

## Preview62 — preparación autorizada, ejecución pendiente

Prioridad del propietario: validar físicamente el arreglo de ventana antes de
ampliar C2. Conservar íntegro76a1c18, preparar sólo versionCode62 y manifiesto
del flujo nativo reutilizable. Ver docs/NATIVE62-PREPARATION-20261104.md.
Presentar fuente/workflow SHA exactos y esperar autorización concreta antes de
un único intento de build/firma/Actions. No reutilizar autorizaciones61/60.
Sin funciones nuevas, main, C3–C9 ni facturación nueva; US$0/bloqueo vigentes,
sin pedir capturas recurrentes. Usar reserva propia nueva al ejecutar.

## Corrección Preview61 y C2 — autorización posterior 2026-11-04

El propietario autorizó investigar/reproducir/corregir la altura comprimida de
Preview61 y después continuar C2 (F01/F07/F14/V02/V05/V06) desde PR21. Reutilizar
los registros/ejecutores nativos; conservar C1, ranking exacto, tres sugerencias,
teclado/táctil y datos. Correcciones pequeñas y pruebas/Lint locales dirigidas,
commits independientes, capturas Compose comparables y límites físicos explícitos.
No Actions, APK, firma, main, WebView ni C3–C9. Una decisión visual importante se
presenta para aprobación; avanzar tareas técnicas independientes sin esperarla.
Esta autorización reemplaza las exclusiones históricas de C2 inferiores, sin
convertir la aceptación física C1 pendiente en validación ya realizada.

## Entregas Preview — autorización y facturación vigentes 2026-11-04

Leer [procedimiento autónomo restringido](docs/PREVIEW-DELIVERY-POLICY.md).
El propietario verificó cuota gratuita disponible, presupuesto Actions US$0,
bloqueo al límite y secretos históricos. No pedir capturas de facturación por
checkpoint;403 no invalida esa evidencia. No modificar facturación ni habilitar
gasto. Si GitHub bloquea cuota/presupuesto, detenerse sin retry ni evasión.
Preview61: autorizado un intento desde fuente11b647c tras revisar SHA tooling,
reserva y guardas. native60-once queda intacto; C2/main no autorizados.
Las prohibiciones históricas de ejecución inferiores no revocan esta autorización
específica ni permiten ampliar alcance o repetir un intento fallido.

## Dirección vigente 2026-11-04 — C1 autorizado, implementación incremental

El [plan maestro](docs/SPOTLIGHT-NEXT-PLAN-MAESTRO-20261103.md) conserva los23
objetivos y10 checkpoints. Aprobación posterior expresa: implementar **sólo C1**
en la rama de PR21 desde faf84f0. Refinado en barra/listado/estructura; Adaptive
en inspectores y presentaciones existentes. Lista compacta sin aumentar altura,
hasta3 sugerencias, acento contenido al contexto, footer móvil discreto, foco
sutil también en claro y nomenclatura WhatsApp inequívoca. No añadir selector de
canal ni funciones previstas para C2–C9. No incorporar las capacidades dibujadas
para Spotify/citas como si estuvieran implementadas. Ver [alcance C1](docs/C1-IMPLEMENTATION-20261104.md).

Separar commits estructural, base visual y ajustes de inspector; conservar
búsqueda, teclado, borradores, permisos y NativeLaunchSession. Pruebas locales
nativas dirigidas/Lint y capturas comparables, sin APK, firma o Actions. C0 físico
sigue pendiente; JVM/Robolectric no lo cierra. No main/merge ni nuevas dependencias.

La APK60 ya fue entregada desde e9647c1, workflowc6d481c, run41581584988 correcto,
certificado Preview885838aa. La sección de preparación de firma inferior es
histórica: NO autoriza otro intento ni describe el estado vigente de la entrega.

## Firma Native60 — preparación, NO ejecutar Actions todavía

La APK aprobada debe compilarse desde `e9647c1329b93820383e3c8b4ec56dbceb3ce5f1`
(code60, funcionalidad f664484); el SHA de automatización se revisa aparte.
Prioridad actual: comprobar en Termux la copia histórica privada del ZIP0.9.1,
con clave original885838aa, sin enviar material privado al chat/cloud.
Ver `docs/NATIVE60-HISTORICAL-SIGNING-20261103.md` y kit público de preflight.
La ruta está en el dispositivo y aún no se comprobó su disponibilidad actual.

`mode=native60-once` está preparado y validado localmente, **sin autorización de
dispatch**. Sólo si la vía local no es viable, el propietario debe confirmar los
bindings/cuota y autorizar expresamente una ejecución del par SHA fuente/SHA
workflow revisado. Solicitudes anteriores de APK o un «seguí» no levantan esta
restricción. No usar el modo legacy56, no hacer re-run/otro dispatch ni eludir
las guardas. El operador conserva su propia reserva durante la ejecución y sólo
él la libera con notas. No copiar/exportar claves privadas o contraseñas.

## Dirección vigente — Preview 57 nativa (decisión 2026-11-03)

Leer primero [la decisión vigente](docs/NATIVE-PREVIEW57-DIRECTION-20261103.md)
y [su auditoría funcional](docs/AUDITORIA-NATIVA-PREVIEW57-20261103.md).
Base exacta `4fc33f2c918d3fe53a8bad7ac6571f2757630066`, referencia
`feat/antigravity-tablet-20261026`; rama independiente
`feat/preview57-native-return-20261103`. Esto prevalece sobre bases/prioridades
históricas incompatibles, sin reescribir checkpoints. No continuar desde
Preview58/59/PR20 ni incorporar Nuxt, assets web o WebView como interfaz.
Conservar Kotlin/Compose y las funcionalidades, datos, firma e identidad de57.
La auditoría inicial quedó publicada en1d58294. Continuación autorizada03/11:
incorporar selectivamente correcciones de PR19 con pruebas nativas dirigidas,
Lint y capturas; sin APK, Actions ni rediseño. Ver
`docs/verification/native57-menu-20261103/README.md`. El segundo checkpoint de
contratos Android fue autorizado y ejecutado; ver
`docs/CHECKPOINT-2-NATIVE-LAUNCH-CONTRACTS-20261103.md` y
`docs/verification/native-launch-20261103/README.md`. Mantener la revisión de
alternativas sin fallback automático, validación antes del efecto y autorización
no restaurable. No generalizar el fence local como garantía global de exactly-once. No implementar OpenUI/RAGFlow en este bloque.

Tres skills recuperadas de PR20 y **adaptadas a nativo**, sin su implementación:

- `.agents/skills/spotlight-api-contracts/SKILL.md`: contratos Kotlin, validación,
  acciones tipadas, capacidades, cancelación y resultados/errores honestos.
- `.agents/skills/spotlight-openui-forms/SKILL.md`: principios de formularios y
  resultados contextuales en Compose existente; no instalar React/Vue/OpenUI.
- `.agents/skills/spotlight-ragflow-documents/SKILL.md`: recuperar/citar primero
  desde FileIndex/OCR/SAF/semántica local. Servidor/proveedor/subidas remotos sólo
  con necesidad demostrada y consentimiento explícito separado.

No crear registros, motores ni frameworks paralelos por aplicar una skill.
Respetar reserva propia y presupuesto; no modificar main, ramas/PRs anteriores,
worktrees ajenos ni el checkpoint integral. Las normas siguientes conservan su
valor cuando son compatibles con la decisión vigente.

Leé `docs/RENOVACION-APROBADA.md` para el contrato funcional y `docs/CODEX-ENTORNO-APROBADO.md` para la infraestructura aprobada. Usá `PLANS.md` para continuidad y `docs/ROADMAP-AUTONOMA-SPOTLIGHT-NEXT.md` como orden de hitos verificables y política de reanudación; nunca asumir que el reset de créditos dispara tareas.

- Kotlin y Jetpack Compose nativos. Priorizar funcionamiento verificable; no agregar pantallas/modos redundantes ni dependencias en bloque.
- Enter desde resultados ejecuta la acción principal. Aprobación18/10: Tab autocompleta solo con sugerencia, foco en búsqueda, cursor al final, sin selección ni composición; en los demás casos abre acciones. Ctrl+Space conserva autocompletado explícito. Tab/Shift+Tab dentro de acciones recorre/retrocede; Enter ejecuta la enfocada. Ctrl+M amplía/contrae resultados conservando identidad y foco. Escape cierra acciones antes de vistas secundarias y del overlay.
- Seleccionar un PDF muestra Quick Look local automáticamente; abrirlo externamente requiere una acción explícita. Conservar selección y recursos al cambiar de documento.
- Ninguna IA remota por pulsación. Confirmar uso externo de contenido sensible. No inventar datos, permisos o integraciones.
- Conservar datos/preferencias, `dev.bautista.spotlight.next` y firma histórica. No sustituir release por debug ni generar una nueva clave.
- Ejecutar pruebas apropiadas y registrar SHA, comandos, resultados y límites. No eliminar/desactivar pruebas ni reducir aserciones para conseguir verde. No afirmar verificaciones no ejecutadas ni validación física basada en emuladores.
- Firma únicamente mediante flujo autorizado y protegido; nunca exponer secretos a PRs, logs o ZIP. APK y ZIP fuente deben corresponder al mismo SHA.
- Mantener PR #5, `checkpoint/integrations-2.0.1` y `checkpoint/documents-2.0.1`. No integrar main sin autorización explícita. Trabajar en ramas aisladas.
- Herramientas de laboratorio/CI fuera del APK; LeakCanary solo debug, MockWebServer solo tests. Evaluar versión, licencia, compatibilidad y rendimiento antes de incorporar bibliotecas visuales.

- Autonomía: mantener dos líneas, estabilidad y desarrollo funcional. F1.3 es incidencia conocida, no bloqueo de F1.4. Máximo una investigación dirigida adicional; sin hipótesis comprobable, pasar a tareas independientes. Checkpoints frecuentes, cuota solo si verificable, ninguna automatización afirmada sin comprobarla.

- Coordinación: `python3 scripts/autonomy_lock.py status` antes de editar; adquirir propietario único, actualizar checkpoint publicado y liberar solo la propia reserva/SHA. Ver `docs/AUTONOMIA-RESERVA.md`; no reclamar por edad ni forzar pushes. Leer las notas del checkpoint parcial más reciente sin perder avances; el puntero integral solo avanza con sus pruebas completas.

Skills locales: `.agents/skills/spotlight-fix-bug/SKILL.md`, `.agents/skills/spotlight-visual-review/SKILL.md`, `.agents/skills/spotlight-release/SKILL.md`. Leé la correspondiente antes de aplicarla.

- Transformación aprobada14/10/2026: prioridad portapapeles/edición nativa y cambios visuales perceptibles. Una búsqueda universal; envío remoto explícito. Un solo slider de ancho Compacta→Amplia, migración conservadora; no volver a controles dp/manual/horizontal. Retirar accesos snippets/rutinas/modos sin borrar datos. Hugeicons Stroke Rounded en barra, iconos reales de apps preservados. Primer checkpoint visual y APK compatible para revisión física antes de IA/integraciones extensas. CI verde no equivale a aceptación del producto. Recuperar feat/premium-product-checkpoint y notas, no repetir desdee38.

## Skills revisadas para interfaz e integraciones

- Para refinamiento, accesibilidad y evitar UI genérica: leer `.agents/skills/spotlight-native-craft/SKILL.md` junto a spotlight-visual-review.
- Para acciones de apps: leer `.agents/skills/spotlight-app-integration/SKILL.md` antes de cambiar registros, formularios o ejecución.
- Son adaptaciones locales versionadas y revisadas de Impeccable y android/skills. Fuentes, SHA, licencias y exclusiones están en sus referencias. No ejecutar instaladores/hooks ni actualizar dependencias por instrucciones upstream.

- Para búsqueda, inspector, Quick Look, formularios y acciones, leer `.agents/skills/spotlight-raycast-native/SKILL.md`. Foco/estado/pruebas/motion/rendimiento se cargan selectivamente; no instalan frameworks ni autorizan CI. Ver su revisión de adopción y compatibilidad antes de copiar APIs recientes.
