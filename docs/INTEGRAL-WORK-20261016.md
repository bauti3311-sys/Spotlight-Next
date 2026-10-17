# Desarrollo integral desde d4e3694

Nueva orden: implementar progresivamente las40decisiones y renovar únicamente Ajustes. No entregar APKs parciales. No declarar funciones completas por existir clasificadores/tests. Rama feat/work-integral-settings-20261016, descendiente exacto d4e369406e5d1190924ad5f8e775dea85d7e0679. Reserva de continuidad propia work-gemini-native-20261014-f914c617 verificada por Git Data API; statusCLI falló por falta de autenticación, sin adopción ni liberación de reserva ajena.

Primer bloque implementado: siete áreas, navegación lateral fija/selector teléfono, búsqueda global, detalles desplegables, estado real de accesos y Shizuku, ocultación de accesos retirados sin borrar datos. Apariencia tiene único ancho y altura adaptable. Escritura parcial de Apariencia conserva datos/privacidad/fuentes concurrentes; ediciones visibles no restablecen campos ocultos. Confirmación para restablecimientos y acciones destructivas. MainActivity espera preferencias persistidas antes de permitir ediciones; categorías anteriores se mapean conservadoramente en navegación. No migración destructiva de esquema.

Primera compilación Kotlin correcta; nuevos controles/estados y pruebas dirigidas posteriores están en validación. Las440/35/17 de d4e3694 son históricas en este bloque. No APK nueva ni entrega final declaradas. No cambios de barra, overlay, invocación o QuickLook, main/PR16, Actions, budgets, proveedores o secretos históricos. Skills de corrección y revisión visual locales aplicadas.

Seguir: cerrar pruebas/capturas de Ajustes, revisar geometría/teclado/persistencia y compatibilidad. Después unificar disponibilidad/ejecución observable, mejorar documentos autorizados y los flujos Gemini existentes. Construir matriz40 con estados separados implementado/verificado/pendiente físico/proveedor. La salida final exige gates íntegros y APK+fuente del mismoSHA; si la sesión se interrumpe, publicar checkpoint/handoff, nunca otro APK parcial.

## Segundo bloque: ejecución y documentos

37 pruebas dirigidas iniciales de Ajustes/persistencia/geometría correctas. Dos rondas posteriores de ejecución/flujo de producción y documentos también terminaron BUILD SUCCESSFUL. Se corrigió una incompatibilidad de firma de GlassAppearance detectada al compilar tests anteriores; se conservó su overload público y no se eliminaron pruebas. Se revisaron capturas Compose nativas: eliminados encabezados repetidos, temas duplicados y recorte de la vista previa en teléfono (solo preview de Ajustes). La barra real y sus tokens no cambiaron.

ToolExecutor.capabilities comprueba requisitos actuales (permisos Android, SAF vigente, MediaSession, Shizuku, extensiones y volumen fijo); available deriva del mismo catálogo. Ajustes muestra esos estados sin solicitar permisos automáticamente. PlanExecution usa el ejecutor anterior, conserva el límite existente de seis pasos, diferencia preparado/solicitado/verificado, bloquea pasos posteriores al fallo y propaga cancelación sin repetir. Antes de cada paso del panel vuelve a leer preferencias y requisitos. Volumen se lee después del cambio; una solicitud de apertura/reproducción no se declara estado final verificado.

El índice deja de devolver caché de carpetas SAF cuyo permiso fue revocado, aunque la preferencia siga guardada. Se conserva la base local; no se elimina información por simplificar la interfaz. Las consultas documentales naturales admiten «que hablan sobre»; el fragmento se centra en coincidencia textual profunda con límite estricto y acentos. Esto es búsqueda léxica, **no semántica ni embeddings**. DocumentRetrievalTest recorre interpretación→índice real SQLite→fragmento; FileIndexTest comprueba concesión/revocación en Android simulado.

Suite Android completa en marcha sobre el siguiente conjunto de cambios, incluidos tests nuevos de restauración de actividad, Escape, rail fijo y tema oscuro. Aún no se declara gate integral ni APK final. La matriz completa de alcance y pendientes está en MATRIZ-40-INTEGRAL-20261016.md. No llamadas reales a Gemini/proveedores, no dispositivo físico, no Actions ni secretos históricos.

## Tercer bloque: recorridos y persistencia

Checkpoint publicado32be09b. Pase anterior:461Android/JVM correctas, lintDebug/Release/functionalPreview sin errores (10advertencias/1info por variante), Debug/ReleaseR8/functionalPreviewR8 correctos en13m16. Es un pase previo a los últimos handlers de entrada/controles y a las nuevas referencias visuales, **no** la aprobación de todo el estado posterior. Python30scripts+5backend correctos; Gitleaks8.30.1 sin hallazgos en árbol actual e historial local disponible (7commits), no auditoría de todo el historial remoto.

El engranaje conservado abre ahora el nuevo Ajustes. La actividad recibe solicitudes de categoría, con compatibilidad para nombres anteriores. Controles directos de volumen porcentual y MediaSession reutilizan ToolExecutor, con preferencias/requisitos actuales; las solicitudes multimedia dejan de declararse como reproducción verificada. El filtro de extensiones se aplica también a resultados nativos con módulo conocido, no solo a filas cuyo ID comience por extension. Guardado de IA parcial por campos realmente editados: conserva privacidad, fuentes, apariencia y endpoint concurrente no editado. Borrado de credencial confirmado y con manejo de error/cancelación.

Revisión visual nativa: los controles de Apariencia pasan a segunda línea en teléfono, evitando etiquetas cortadas. La modificación es exclusiva de Ajustes y su preview simple; renderer de resultados/QuickLook y geometría del overlay permanecen intactos. Se añaden cuatro referencias de MainActivity para Obsidian/Graphite/Light en tablet y Obsidian en teléfono; una referencia anterior de Apariencia requiere actualización revisada, las16restantes deben conservarse.

Una ejecución dirigida de registro se interrumpió al bloquearse el test nuevo newIntentTargetsExistingSettingsActivity. Se aisló la prueba y se cambió la obtención de Activity fuera del callback de UI para no anidar ActivityScenario. No se declara esa corrida aprobada ni se elimina la aserción. Su verificación y los gates del estado actual siguen pendientes en este checkpoint de continuidad.

Actualización: la traza dirigida confirmó que las aserciones terminaban; el bloqueo ocurría en el cierre de ActivityScenario, que identifica eventos por el filtro del Intent original. El fixture restaura ese Intent en finally, después de comprobar la categoría real. La dirigida aislada pasó en36s. Registro posterior de5referencias de Ajustes correcto en19s, con revisión manual de todas las capturas y copia exclusivamente de las5autorizadas. Gates completos del código103be38 más estas referencias en marcha; no se cuenta una corrida interrumpida como aprobada.

## Cuarto bloque: comandos nativos guardados conectados

El código43bce6a pasó470Android/JVM sin fallos/errores/omisiones. Se guardó esa evidencia antes de continuar. Se interrumpió el resto de ese pipeline al encontrar un defecto funcional comprobable: Guardar como comando escribía rutinas heredadas que ProductVisibility ocultaba después. No se declara aquel pipeline completo como verde.

NativeCommands identifica explícitamente planes reviewed-native-v1 en el almacenamiento/codec anterior, sin promover rutinas existentes. Solo admite abrir una app, volumen, MediaSession y brillo compatible;1–6pasos, sin continuar tras fallo, sin IA/texto remoto/terminal. El plan aparece en búsqueda por nombre/alias y en Guardados; abre el editor/revisor y utiliza PlanExecution+ToolExecutor existentes. No se reintroduce WorkflowRunner ni su editor. Guardar no ejecuta, recordar tampoco; ejecutar requiere confirmación nueva y requisitos vigentes.

Se conservan paquetes exactos al reordenar pasos aunque las apps tengan igual nombre, y el editor invalida la revisión al cambiar datos. Un refresco de capacidades conserva omisiones explícitas. El panel es desplazable para poder acceder a todos los controles. Persistencia atómica solo del campo correspondiente: conserva registros legados y campos desconocidos; límite30sin expulsar datos. Borrado confirmado afecta únicamente al comando nativo seleccionado, no a un registro legado con el mismoID.

38pruebas dirigidas actuales correctas (NativeCommandsTest, NativeCommandFlowTest, NextToolsTest, HybridIntelligenceTest, UniversalProductionFlowTest),44s. Se mantuvieron las aserciones de ejecución real sobre Android simulado. Correcciones de fixtures: estado de preferencias explícito/restaurado y normalización de orden de la cola de Activity de Robolectric4.14.1, cuyo bytecode usa remove(size-1). Las verificaciones de paquete, revisión, orden cronológico y volumen observado permanecen. Suite integral, lint/R8 y21comparaciones del nuevo bloque aún pendientes en este checkpoint.

## Reanudación17/10: gates actuales cerrados

Recuperados HEAD limpio y reserva propia; no había cambios Android sin publicar. El entorno temporal y la clave P1 se perdieron; no se generó una clave sustituta. La revalidación nueva de f490963 pasó483Android/JVM (103suites, cero fallos/errores/omisiones), tres lint sin errores (10advertencias/1info cada uno), Release/R8 sin firma en17m47s. Verificación estricta de21referencias: todas sin cambios, cero regrabadas,22s.301entradas compiladas coinciden con Git. Gitleaks sin hallazgos en árbol y10commits locales disponibles. Python30+5 conserva evidencia previa, no se repitió sin cambios de código.

La Release de comprobación es2.0.1/código29,paquete principal intacto,ARM64,min26/target35,no depurable,zipalign16KB correcto; está deliberadamente sin firmar y no se entrega. No hubo Actions/proveedores reales/dispositivo físico. Evidencia versionada en `verification/integral-20261017/summary.json`; recuperación y siguiente paso exacto en `HANDOFF-INTEGRAL-20261017.md`. Las40decisiones siguen parciales según matriz; no hay APK parcial ni declaración de entrega final.


### Continuación estructurada de conversaciones — 17/10/2026

Después de e768a6a, se conectó guardado de intercambios completos y reanudación desde búsqueda con revisión exacta del historial y pregunta antes de enviar. Conserva registros legados, no añade contexto activo ni relee adjuntos, no expulsa conversaciones al llegar al límite y confirma borrados. 48 pruebas dirigidas correctas y Gitleaks sin hallazgos. Ver docs/CONVERSACIONES-20261017.md. Suite completa, lint/R8 y visuales del bloque nuevo pendientes; no aplicar los gates de e768a6a a estos cambios todavía. No APK parcial ni Actions.


### Conversaciones: gates cerrados — 17/10/2026

Código publicado 0c2f74f: 493 Android/JVM correctas (104 suites, cero fallos/errores/omisiones), tres lint sin errores (10 advertencias/1 informativa por variante), Release/R8 sin firma correcta en 10 min 33 s y 21 comparaciones visuales estrictas correctas en 21 s, sin regrabar referencias. Las 303 entradas compiladas coinciden con Git. Gitleaks sin hallazgos en árbol y 12 commits locales. Las 48 dirigidas forman parte de las 493, no se suman. Evidencia en docs/verification/conversations-20261017/summary.json; docs/CONVERSACIONES-20261017.md describe límites. El handoff actual identifica OCR PDF local acotado como próximo bloque. Sin API real, validación física, Actions, firma histórica, clave P1 nueva ni APK parcial. La matriz40 sigue parcial.


### OCR PDF local — 17/10/2026

Implementada extracción PDF unificada con OCR local acotado, trazabilidad por página y fuente, resultados parciales, cancelación segura de recursos ML e índice v2 aditivo. La búsqueda excluye las etiquetas generadas para no crear relevancia falsa. 29 pruebas dirigidas y compileDebugAndroidTestKotlin correctos; OCR real instrumentado compilado, no ejecutado por falta de dispositivo/KVM. Gates completos pendientes. Ver docs/OCR-LOCAL-20261017.md para comandos/límites. No interfaz rediseñada, Actions, clave nueva ni APK parcial.


### Recursos multimedia y siguiente validación integral

La revisión 5af8dff pasó 507 pruebas completas, cero fallos/errores/omisiones. Se preservaron los XML y se interrumpió el resto de ese pipeline antes de completarlo para añadir gestión de memoria multimedia: no se declara lint/R8 de aquel pipeline como verde.

MediaAnalysis comparte ahora el propietario de bitmap entre OCR y etiquetado y recicla al terminar realmente todas sus Tasks. Imágenes rotadas y fotogramas escalados liberan intermedios; cancelar no recicla una entrada que ML Kit aún usa. La decodificación inicializa inSampleSize=1 y rechaza dimensiones inválidas. Las salidas existentes de OCR/etiquetas y vídeo se conservan; no se añaden modelos ni proveedores. Ronda final: 25 pruebas dirigidas y compileDebugAndroidTestKotlin correctos en 49 s. El primer intento detectó inferencia genérica en el test; el segundo usaba BitmapFactory simulado que aceptaba bytes inválidos. El fixture pasó a GraphicsMode NATIVE sin debilitar su aserción. No es ejecución del reconocedor ML en un dispositivo.

El flujo PDF no añade código HTTP para enviar documentos; usa el modelo latino ya incluido. No se midió tráfico ni RSS del SDK en un dispositivo real. Se inicia la suite completa del conjunto OCR/MIME/memoria antes del cierre.


## Cierre verificado OCR · 17/10/2026

Código Android: `dae3a8952a45e2ff835a14e7a32fe0fb1f564cca`. Pipeline final completo: 510 pruebas en108suites, cero fallos/errores/omisiones, tres lint sin errores (10 advertencias+1 informativa heredadas cada uno), Release/R8 sin firma y compilación instrumentada correctas en11min. Comparación estricta posterior:21sin cambios, cero grabadas/añadidas/modificadas, en20s. Los310archivos compilados coinciden conGit. Evidencia: `docs/verification/ocr-20261017/summary.json` (desde docs: `verification/ocr-20261017/summary.json`).

El OCR real instrumentado no se ejecutó por falta de dispositivo/KVM. Los límites de tiempo son cooperativos y la memoria nativa no se midió. Gitleaks: sin hallazgos en árbol y17commits locales disponibles antes del cierre; no es auditoría del historial remoto completo. Ninguna clave nueva, APK parcial, Actions o modificación de main/PR16. Próximo bloque: vigencia/cancelación de extracción manual al cambiar consulta o selección, con pruebas de finalización tardía; detalles en HANDOFF-INTEGRAL-20261017.md.


## Cierre actual · cobertura y cancelación documental

Código Android 70e6f69b8a7660ae5eac987cc54450f88a3df4d3: 521 pruebas nuevas completas en110suites, cero fallos/errores/omisiones, tres lint sin errores (10advertencias+1informativa heredadas cada uno), Release/R8 y compilación instrumentada correctos en11min45s. Verificación estricta posterior:21referencias sin cambios en21s; ninguna añadida/grabada/modificada. Se cotejaron313archivos de entrada contraGit. Evidencia: docs/verification/ocr-coverage-20261017/summary.json.

La pérdida de cobertura por extracto corto se reprodujo antes (5casos/1fallo) y se corrigió con conservación por huella/página/método/texto, sin impedir cambios de fuente o correcciones OCR. Los35dirigidos pasaron; la descarga HTTP403 de su compilación instrumentada se resolvió en el reintento completo, sin cambiar dependencias. El primer pipeline fallido no se declara verde.

OCR nativo instrumentado y mediciones físicas siguen pendientes: no hay dispositivo/KVM. Los516XML recuperados son históricos, no se suman a521; Python30+5 tampoco se repitió. Sin APK parcial, clave nueva, Actions, main, PR16 o cambios de presupuesto. Próximo bloque exacto: presupuesto OCR adaptativo del índice; ver HANDOFF-INTEGRAL-20261017.md. Las40decisiones permanecen parciales.


## Presupuesto adaptativo OCR · cierre f05e82b · 17/10/2026

539 Android/JVM nuevos en114suites, cero fallos/errores/omisiones; lint3 sin errores, Release/R8 y Kotlin instrumentado correctos en11min21s. Verificación estricta21referencias sin cambios en21s;320inputs Git idénticos/sin fuentes adicionales. Evidencia: docs/verification/adaptive-ocr-20261017/summary.json (desde docs, verification/adaptive-ocr-20261017/summary.json). Debug/FunctionalPreview11advertencias+1informativa; Release10+1, incluidos avisos de versiones sobre dependencias no modificadas. Reutilización de estado real del dispositivo, OCR PDF reducido/aplazado con causa, pendientes persistidos y recuperación SHA; cancelación/concurrencia/permisos conservados. Regresiones de escritura manual perdida y bucle privado reproducidas/corregidas. Detalles/umbrales en ADAPTIVE-OCR-20261017.md y handoff.

No instrumentación ejecutada ni dispositivo/KVM; no medidas físicas. Resume mediante acción existente, no worker nuevo. Release interno sin firma, alineado/principal29, no instalable ni entregado. Gitleaks árbol/24commits sin hallazgos; no auditoría completa remota. Sin clave nueva/Actions/main/PR16/force/APK parcial. Matriz40 parcial. Continúa revisión conjunta documental/Gemini, reutilizando motores existentes, con prueba del contenido exacto y cancelación; gates de ese siguiente bloque se registran por separado.


## Selección documental · cierre642b00c ·17/10/2026

549Android/JVM/116suites, cero fallos/errores/omisiones; lint3sin errores (11advertencias+1informativa cada uno), Release/R8 y compilación instrumentada correctos en10min47s.21visuales estrictas sin cambios en21s,323inputs Git/sin fuentes adicionales. Evidencia verification/document-selection-20261017/summary.json desde docs; SELECCION-CONTEXTO-20261017.md describe límites y10casos nuevos.32dirigidos anteriores no se suman. Contexto de hasta12archivos con SHA/página/método/cobertura/recorte explícito, revisión exacta y cancelación o selección cambiada sin HTTP tardío. Fixtures de proveedor, no llamada real Gemini ni MLnativo. Gitleaks árbol/26commits sin hallazgos. Release sin firma/clave nueva/APK parcial; Actions rama0. Mantiene OCR/Ajustes/Obsidian/conversaciones. Siguiente seguimiento: conservación de fijados al cargar/guardar/borrar y cancelación de contexto leído tarde; casos en reproducción, no completados aún.


## Conservación de contextos · cierre4f429c0 ·17/10/2026

Código4f429c0120aacf78c6568f209c747c8d4c10068b, sobre47f3384 y el cierre documental a34098e. ContextStore espera carga antes de modificar, conserva fijados/conversaciones/campos desconocidos, rechaza el31sin expulsar y publica cambios solo tras AtomicFile exitoso. Fijar queda ligado al contexto solicitado antes de esperar carga; cambiar selección retira importaciones temporales sustituidas sin borrar fuentes fijadas. ContextPanel cancela/desestima lectura tardía al quitar/cambiar/cancelar antes de preparar PRIVATE_CONTEXT; no envío automático ni nuevo motor/interfaz.

**Gates nuevos4f429c0:**562pruebas completas/119suites, cero fallos/errores/omisiones; lint Debug/Release/FunctionalPreview sin errores, recuentos exactos en verification/context-preservation-20261017/lint-summary.json. Release/R8 y compileDebugAndroidTestKotlin correctos (BUILD SUCCESSFUL in 12m 37s). Verificación visual estricta posterior:21sin cambios, cero grabadas/añadidas/modificadas (BUILD SUCCESSFUL in 21s).326entradas compiladas coinciden con Git y no hay fuentes adicionales. Evidencia: verification/context-preservation-20261017/summary.json. Los36dirigidos recuperados no se suman a562; no sobrevivió su manifiesto de entradas. El pipeline de47f3384 se interrumpió después de560/lintDebug para corregir dos seguimientos; no se declara completo.

El primer intento nuevo falló en6min48s por SIGSEGV interno de C2/OpenJDK17.0.16 al compilar SQLiteDatabase instrumentado por Robolectric; no fue una suite aprobada. Se conserva extracto/hash del crash. El reintento usa el init local runtime-test-c1.gradle: únicamente los JVM de Test se limitan a C1 (-XX:TieredStopAtLevel=1), sin omitir pruebas/aserciones ni cambiar código/workflows; Gradle/R8 no cambian. No es una medición de rendimiento.

Siete fallos esperados en8casos contra642b00c y dos regresiones individuales posteriores constan con hashes/casos. Fijados/conversaciones usan archivos reales del sandbox JVM; AtomicFile falla mediante ruta .new bloqueada, no fallo físico de disco. Compose recorre capacidad y lecturas de proveedor fixture por DocumentText; no emite Ask tardío. PRIVATE_CONTEXT vigente sigue exigiendo revisión exacta. Ver CONTEXTO-CONSERVACION-20261017.md.

No hay dispositivo ni KVM (comprobados de nuevo): instrumentación compilada, NO ejecutada; sin reconocimiento ML nativo, One UI o medidas físicas. Release interno sin firma principal29/ARM64/min26/target35/no depurable y zipalign16KB correcto; no instalable ni entregado. Sin clave nueva, APK parcial, Actions, cambios main/PR16/presupuesto o force. Gitleaks árbol/29commits locales sin hallazgos antes del cierre, no historial remoto completo. Python30+5 histórico, no repetido. La matriz40 permanece parcial y la firma final exige el procedimiento histórico/checks del SHA exacto.

### Siguiente bloque funcional exacto

Decisiones7/18: comprobar que JSON ilegible, versión desconocida o registros no válidos no se confundan con almacenamiento vacío y se sobrescriban al fijar/guardar/borrar. Hay cuatro escenarios preparados fuera del árbol; aún no compilados ni corregidos. Reproducir primero contra4f429c0 y proteger lectura/escritura conservando bytes, errores visibles y reintento explícito. No reparar eliminando datos, no añadir memoria automática o envío remoto. Después continuar pendientes viables de matriz40; embeddings/grounding/proveedor/dispositivo no quedan acreditados por estas pruebas. Consultar HEAD y reserva propia antes de editar; el checkpoint no es ejecución en segundo plano.
