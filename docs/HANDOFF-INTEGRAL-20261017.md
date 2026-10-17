# Continuidad integral · 17/10/2026

## Checkpoint semántico en progreso

Base be3d002→evaluación c1f7248→semántica8c307e4; misma rama y reserva propia. Implementación real conectada a SearchEngine, modelo optativo, permisos/huellas/cancelación y SQLite v3.36dirigidas correctas/7suites e instrumentación compilada; paridad Kotlin/ONNX nativo Linux16textos y8paráfrasis, NO Android ARM64. Ver SEMANTIC-LOCAL-20261017.md y verification/semantic-evaluation-20261017. Gate integral/R8/lint/visuales aún pendiente. No confundir572históricas con verificaciones actuales. Sin clave nueva, APK parcial, Actions ni cambio de estética.

Operadores documentales reproducidos:3fallos/4casos contra8c307e4; corregidos sin rebajar aserciones. Prefijos conservan NOT/AND/OR/comillas/filtros y agrupan OR bajo el tipo. Nueva ronda dirigida correcta registrada en verification/semantic-evaluation-20261017/operators-regression.json. Se añade JNI smoke instrumentado con Identity explícito (no semántica ni calidad), aún sin ejecutar. Siguiente paso exacto: ejecutar gate completo sobre SHA publicado, preservar evidencia y continuar bloques viables de matriz40. No ejecutar en paralelo otro Gradle ni asumir trabajo en segundo plano al terminar sesión.

---


## Estado integral verificado: conservación ante lectura fallida

Código Android: `f987a0ebe38a244688f168c199b9e799f7b85b1b`, sobre `2de8b52` / `4f429c0`. ContextStore ya no trata JSON ilegible, versión desconocida, listas/identidades/registros no válidos o lectura cancelada como datos vacíos aptos para sobrescribir. Carga ambas colecciones y metadatos antes de publicar, sin descarte/recorte silencioso; bloquea save hasta una lectura válida. AtomicFile puede recuperar su .bak existente. Error y Reintentar lectura están conectados a ContextPanel sin mostrar contenido privado; el reintento lee localmente, no fija/guarda/envía. No reconstruye JSON corrupto ni crea otro respaldo. Ver CONTEXTO-RECUPERACION-20261017.md.

**Verificación nueva de f987a0e:** 572 pruebas completas / 121 suites, cero fallos/errores/omisiones; tres lint sin errores (recuentos exactos en verification/context-recovery-20261017/lint-summary.json). Release/R8 y compileDebugAndroidTestKotlin correctos (BUILD SUCCESSFUL in 11m 19s); 21 comparaciones estrictas intactas, cero grabadas/añadidas/modificadas (BUILD SUCCESSFUL in 22s). 328 entradas coinciden con Git, sin fuentes adicionales. Evidencia: verification/context-recovery-20261017/summary.json. 40 pruebas dirigidas / 7 suites precedieron los gates; sus cinco archivos modificados coinciden con el código publicado. No se suman rondas. Python 30+5 histórico preservado, no repetido porque no cambió.

Antes de corregir fallaron 4 casos esperados sobre producción `4f429c0` / documentación `2de8b52`, identificados por hashes. La primera ronda dirigida se interrumpió por fixture de dispatcher pausado que asumía carga sin cambio de hilo: ahora bombea continuaciones con límite de 5000 ms y conserva todas las aserciones. El componente Compose usa ContextStore/archivos reales del sandbox; no es prueba de arranque físico ni permisos Android reales. C1 sigue siendo ajuste exclusivo del JVM de tests por el crash previo, sin cambiar verificaciones/código de la app/Gradle-R8 ni usarlo para medir rendimiento. El aviso de caché JIT llena no omitió casos; todos los 572 terminaron.

No dispositivo/KVM: instrumentación compilada, NO ejecutada; no One UI/ML nativo ni medidas RAM/batería/latencia. Release interno principal con código 29 / ARM64 / minSdk 26 / targetSdk 35/no depurable, zipalign 16 KB correcto, SIN FIRMA y no instalable/entregado. No se creó clave de firma ni se entregó APK parcial. Gitleaks árbol / 31 commits locales sin hallazgos antes del cierre; no historial remoto completo. Consulta de ejecuciones de esta rama: 0 (registro exacto en evidencia), sin consultar facturación/minutos de la cuenta. No Actions, cambios de presupuesto/retención/workflows/main/PR16, merge ni force. La firma única final sigue condicionada al procedimiento histórico/checks del SHA exacto. La matriz de las 40 decisiones permanece parcial.

### Siguiente bloque funcional exacto

Decisiones 6/13/35: recuperación semántica local optativa sobre el índice documental existente. Antes de introducir un modelo, releer FileIndex, DocumentRetrieval y límites/huellas de DocumentText; identificar modelo real, licencia redistribuible, tamaño y motor Android compatibles con min26/ARM64, y acotar memoria/cancelación/versionado del índice. Conservar FTS/ranking léxico como respuesta inmediata/offline y no llamarlo embeddings. Evaluar corpus sintético reproducible sin subir documentos ni descargar modelos silenciosamente dentro de la app. Las medidas A9+/proveedor y elección definitiva siguen pendientes: no afirmar semántica implementada hasta integrar y verificar el recorrido real. Conservar los bloques cerrados OCR adaptativo, selección Gemini y contextos, Obsidian/Quick Look/Ajustes/conversaciones/acciones. Antes de editar, consultar HEAD y reserva propia; el checkpoint no implica ejecución en segundo plano.

---

## Historial: conservación de contextos

Código4f429c0120aacf78c6568f209c747c8d4c10068b, sobre47f3384 y el cierre documental a34098e. ContextStore espera carga antes de modificar, conserva fijados/conversaciones/campos desconocidos, rechaza el31sin expulsar y publica cambios solo tras AtomicFile exitoso. Fijar queda ligado al contexto solicitado antes de esperar carga; cambiar selección retira importaciones temporales sustituidas sin borrar fuentes fijadas. ContextPanel cancela/desestima lectura tardía al quitar/cambiar/cancelar antes de preparar PRIVATE_CONTEXT; no envío automático ni nuevo motor/interfaz.

**Gates nuevos4f429c0:**562pruebas completas/119suites, cero fallos/errores/omisiones; lint Debug/Release/FunctionalPreview sin errores, recuentos exactos en verification/context-preservation-20261017/lint-summary.json. Release/R8 y compileDebugAndroidTestKotlin correctos (BUILD SUCCESSFUL in 12m 37s). Verificación visual estricta posterior:21sin cambios, cero grabadas/añadidas/modificadas (BUILD SUCCESSFUL in 21s).326entradas compiladas coinciden con Git y no hay fuentes adicionales. Evidencia: verification/context-preservation-20261017/summary.json. Los36dirigidos recuperados no se suman a562; no sobrevivió su manifiesto de entradas. El pipeline de47f3384 se interrumpió después de560/lintDebug para corregir dos seguimientos; no se declara completo.

El primer intento nuevo falló en6min48s por SIGSEGV interno de C2/OpenJDK17.0.16 al compilar SQLiteDatabase instrumentado por Robolectric; no fue una suite aprobada. Se conserva extracto/hash del crash. El reintento usa el init local runtime-test-c1.gradle: únicamente los JVM de Test se limitan a C1 (-XX:TieredStopAtLevel=1), sin omitir pruebas/aserciones ni cambiar código/workflows; Gradle/R8 no cambian. No es una medición de rendimiento.

Siete fallos esperados en8casos contra642b00c y dos regresiones individuales posteriores constan con hashes/casos. Fijados/conversaciones usan archivos reales del sandbox JVM; AtomicFile falla mediante ruta .new bloqueada, no fallo físico de disco. Compose recorre capacidad y lecturas de proveedor fixture por DocumentText; no emite Ask tardío. PRIVATE_CONTEXT vigente sigue exigiendo revisión exacta. Ver CONTEXTO-CONSERVACION-20261017.md.

No hay dispositivo ni KVM (comprobados de nuevo): instrumentación compilada, NO ejecutada; sin reconocimiento ML nativo, One UI o medidas físicas. Release interno sin firma principal29/ARM64/min26/target35/no depurable y zipalign16KB correcto; no instalable ni entregado. Sin clave nueva, APK parcial, Actions, cambios main/PR16/presupuesto o force. Gitleaks árbol/29commits locales sin hallazgos antes del cierre, no historial remoto completo. Python30+5 histórico, no repetido. La matriz40 permanece parcial y la firma final exige el procedimiento histórico/checks del SHA exacto.

### Siguiente bloque funcional exacto

Decisiones7/18: comprobar que JSON ilegible, versión desconocida o registros no válidos no se confundan con almacenamiento vacío y se sobrescriban al fijar/guardar/borrar. Hay cuatro escenarios preparados fuera del árbol; aún no compilados ni corregidos. Reproducir primero contra4f429c0 y proteger lectura/escritura conservando bytes, errores visibles y reintento explícito. No reparar eliminando datos, no añadir memoria automática o envío remoto. Después continuar pendientes viables de matriz40; embeddings/grounding/proveedor/dispositivo no quedan acreditados por estas pruebas. Consultar HEAD y reserva propia antes de editar; el checkpoint no es ejecución en segundo plano.

---

## Historial: selección documental y Gemini

Código Android642b00cadcf0768f1969ad05d289ae9fdea01783, posterior al cierre OCR b1140df, misma rama y propietario. Se consolidó la selección existente: identificadores/fuente/página/método/cobertura y recorte explícito por documento, máximo16000caracteres,6OCRnuevos/60scooperativos; sin take final que oculte el último archivo. Cancelación y propiedad por consulta/conjunto seleccionado reutilizan DocumentExtractionRequest. DirectAi y revisión exacta PRIVATE_CONTEXT existentes, sin envío al escribir ni seleccionar.

**Gates nuevos642b00c:**549 pruebas completas/116suites, cero fallos/errores/omisiones; lint3 sin errores (11advertencias+1informativa cada uno, con avisos GradleDependency sobre versiones sin cambiar); Release/R8 y compileDebugAndroidTestKotlin correctos en10min47s. Verificación estricta21s,21sin cambios y cero referencias grabadas/añadidas/modificadas.323inputs coinciden con Git y no hay fuentes adicionales. Evidencia: verification/document-selection-20261017/summary.json.32dirigidos precedieron el gate, no se suman a549.

Dos TXT reales pasan por extracción → revisión Compose → DirectAi HTTP fixture y payload idéntico a lo revisado, cero solicitudes antes de Enviar; cancelar o quitar chip descarta texto tardío/no HTTP. No proveedor Gemini real ni recorrido de descubrimiento SAF: selección inicial inyectada en el componente existente. Nuevas funciones/pendientes: SELECCION-CONTEXTO-20261017.md. OCR/DeviceResources y la protección de151dfbe permanecen dentro de549.

No dispositivo/KVM; instrumentación solo compilada. No memoria/latencia/OneUI/MLnativo medidos. Release interno sin firma principal29/ARM64/min26/target35/no depurable y alineación16KB correcta, no instalable ni entrega. No clave nueva ni APK parcial. Gitleaks árbol/26commits locales sin hallazgos, no auditoría remota completa. Consulta Actions de esta rama devuelve0ejecuciones; no se inició workflow, modificó presupuesto/main/PR16 ni hizo force. Firma histórica protegida/checks exactos siguen pendientes para la única entrega final APK+ZIP. La matriz40 NO está completa.

### Seguimiento inmediato de memoria/continuidad

ContextStore: reproducir que pinActive/removeContext no esperen carga persistida, el .take(30) silencioso, metadatos desconocidos de contextos y errores de AtomicFile. No perder fijados/conversaciones ni liberar importaciones antes de confirmar borrado. Después comprobar ContextPanel al quitar/cancelar un contexto mientras su lectura sigue en IO: reutilizar DocumentExtractionRequest y descartar revisión tardía, sin nuevo motor o interfaz. Hay escenarios en preparación; no atribuirlos como corregidos/verificados antes de ejecutarlos. Continuar desde el HEAD real y la reserva propia, sin cambiar claves o entregar parciales.

---

## Historial: presupuesto adaptativo del OCR

Código Android f05e82b0c7a53263b2e25d381bb6ce057a75a321, descendiente de151dfbe, en la rama propia feat/work-integral-settings-20261016. Reserva comprobada y renovada por el mismo propietario work-gemini-native-20261014-f914c617 mediante CAS Git Data, sin force. No hay otro escritor autorizado por esta sesión. Consultar HEAD y el registro remoto antes de continuar; el checkpoint no implica tarea en segundo plano.

DeviceResources reutiliza lecturas reales de batería/memoria y añade ahorro/estado térmico. FileIndex/PdfTextPipeline aplazan o reducen el OCR PDF, vuelven a comprobar antes de admitir cada trabajo y conservan texto/páginas pendientes con motivo explícito. Recuperación manual con la acción existente, persistida en SQLite y caché OCR validada por SHA/versión/página/recorte. No worker automático nuevo. Se retiraron transacciones largas que esperaban OCR; consultas y metadatos no bloquean la búsqueda. Una extracción manual más nueva no puede ser sobrescrita por el snapshot antiguo del refresh. El índice privado/deshabilitado no agenda un bucle de refresco.

**Gates nuevos f05e82b:**539 pruebas completas/114suites, cero fallos/errores/omisiones; lint Debug/Release/FunctionalPreview sin errores; Release/R8 y compileDebugAndroidTestKotlin correctos en11min21s. Comparación estricta posterior21s:21sin cambios, cero grabadas/añadidas/modificadas.320entradas compiladas coinciden con Git y no hay fuentes adicionales en app/src. Evidencia: verification/adaptive-ocr-20261017/summary.json. Debug y FunctionalPreview tienen11advertencias+1informativa, Release10+1: las adicionales son avisos GradleDependency sobre dependencias sin cambiar, conservados con mensaje exacto. No se actualizaron versiones para ocultar avisos.

Regresiones reproducidas antes de corregir: escritura manual perdida contra0595052 y notificación repetida del índice frío privado contra c8a02fc. Rondas dirigidas32 y24correctas; no se suman a539. La ronda c8a pasó538 y tres lint, pero su proceso se perdió antes del cierre R8/instrumentación; no se declara pipeline verde de aquella ronda. La anterior059 pasó537 y se interrumpió deliberadamente para reproducir la carrera. Python30+5 es histórico, no repetido.

**Límites:**instrumentación compilada, NO ejecutada. adb sin dispositivo y sin KVM; no prueba ML nativa/One UI ni medidas RSS/latencia/batería. Política adaptativa solo para índice PDF; límites manuales y multimedia anteriores conservados. Plazos cooperativos, no preempción nativa. Reanudación explícita, no worker persistente. Descripción y umbrales: ADAPTIVE-OCR-20261017.md. Matrix40 aún parcial, sin embeddings ni capacidades inventadas.

Release interno: principal dev.bautista.spotlight.next,2.0.1/código29,ARM64,min26/target35,no depurable,zipalign16KB correcto. Apksigner confirma que está SIN FIRMA; no es instalable ni entrega. No se creó ninguna clave. Histórica protegida/checks exactos siguen condicionando la única APK final; P1 no recuperó su clave. Sin Actions, presupuesto, main, PR16, merges, force-push ni APK parcial. Gitleaks sin hallazgos en árbol y24commits locales disponibles antes del cierre; no historial remoto completo.

### Bloque siguiente en preparación

Decisión25, revisión conjunta Gemini de archivos seleccionados. Reutilizar FileBuffer, DocumentText.extractDetailed, DocumentExtractionRequest y DirectAi/AiRequestPolicy: no motor nuevo. Corregir recorte silencioso de varios archivos, conservar identificadores D1…D12/página/método/SHA y avisos de alcance, presupuesto acotado, cancelación explícita y guard de selección/consulta para descartar contenido tardío. El prompt exacto debe seguir revisándose antes de enviar. Los cambios de este bloque deben diferenciar pruebas dirigidas de gates completos y dejar la misma geometría de Obsidian. No entregar APK parcial ni llamar semántica a búsqueda léxica.

---

## Historial: extracción manual y cobertura documental cerradas

Último código Android: `70e6f69b8a7660ae5eac987cc54450f88a3df4d3`, sobre 5bade7a y c107654, en `feat/work-integral-settings-20261016`. El checkpoint posterior agrega únicamente evidencia y continuidad. Comprobar HEAD y reserva remotos antes de editar. Propietario de la reserva de esta continuación: `work-gemini-native-20261014-f914c617`; el registro de checkpoint no significa que haya una ejecución de fondo activa. No modificar reservas ajenas.

Se conservó todo el OCR local y se cerraron sus dos seguimientos: propietario de extracción manual ligado a consulta/selección/cierre, con cancelación explícita y descarte de texto/errores tardíos; y protección del índice para que un extracto corto no elimine cobertura más rica del mismo PDF. Esta última exige SHA256 y fragmentos por página/método idénticos o contenidos; no usa solo longitud. Una fuente distinta o una corrección OCR sí se actualiza. Comprobación y actualización son transaccionales y siguen respetando permisos/modo privado. No es fusión semántica ni una nueva arquitectura.

**Verificación nueva sobre 70e6f69:** 521 pruebas completas, 110 suites, cero fallos/errores/omisiones. Lint Debug/Release/FunctionalPreview: cero errores, cada uno con las 10 advertencias y 1 informativa heredadas. Release/R8 y compileDebugAndroidTestKotlin correctos; pipeline completo en 11 min 45 s. Comparación estricta posterior en 21 s: 21 sin cambios, cero añadidas/grabadas/modificadas. Los 313 archivos compilados de código/configuración/tests/referencias coinciden con Git. Evidencia versionada: `verification/ocr-coverage-20261017/summary.json`.

Se reprodujo antes de corregir la pérdida del índice: 5 casos, un fallo esperado en la palabra de la última página. Tras corregir pasaron 35 dirigidos; ese primer pipeline falló después al descargar dependencias AndroidTest con HTTP403. El mismo URL oficial respondió200 al sondear y el reintento completo posterior pasó, sin cambiar versiones/repositorios/controles. No atribuir verde al primer intento. Los 516 resultados de 5bade7a sobrevivieron al corte; el final de aquel pipeline no sobrevivió. Los 521 actuales son una ejecución nueva, no una suma de rondas históricas. Python30+5 se conserva como resultado anterior, no se repitió porque su código no cambió.

El runtime temporal volvió a perderse; el árbol Git estaba limpio y coincidía con el remoto. La reserva propia se comprobó por Git Data porque `autonomy_lock.py status` no pudo autenticar ls-remote. Se conserva CAS por SHA y avance normal, sin force. Los nuevos logs se guardan desde el inicio fuera de /tmp, y un init script de observación copia JUnit/lint/visuales al finalizar cada tarea, sin cambiar acciones o aserciones del build. Esto permite recuperar los resultados aunque vuelva a perderse el SDK temporal.

**Límites reales:** PdfOcrDeviceTest compiló, NO se ejecutó. adb no muestra dispositivos y no hay KVM. Robolectric/fixtures no acreditan reconocimiento ML nativo, One UI, foco físico ni medidas de memoria/batería/latencia. Los tiempos OCR son cooperativos, sin preempción dura de código nativo. Se mantienen límites por defecto de 3 páginas OCR/40 nativas/8MB, procedencia y avisos parciales. No hay embeddings ni OCR universal.

Release se verificó únicamente como archivo sin firma: principal `dev.bautista.spotlight.next`, 2.0.1/código29, ARM64, min26/target35, no depurable, zipalign16KB correcto; apksigner confirma ausencia de firma. **No es una entrega instalable.** No se creó clave nueva; P1 sigue sin su clave y no se debe reemplazar por una incompatible. La entrega única final requiere firma histórica autorizada, checks requeridos sobre el SHA exacto y APK/ZIP coincidentes. No se entregaron APKs parciales ni se ejecutó Actions, cambió presupuesto, main, PR16 o workflows.

### Siguiente bloque funcional exacto

Decisión35: presupuesto adaptativo del OCR del índice ante ahorro de energía, batería baja, presión de memoria o estado térmico crítico, conservando búsqueda inmediata y texto ya indexado. Primero buscar y reutilizar los adaptadores de estado del dispositivo existentes; no duplicarlos. Separar política comprobable de lectura Android; comprobar antes de admitir cada nueva página/tarea, comunicar por qué se difiere el OCR y no marcar como analizado lo omitido. Mantener extracción manual explícita y sus límites, cancelación y propiedad nativa, sin crear workers en paralelo ni cambiar Obsidian/Quick Look/Ajustes. Añadir pruebas de presupuesto/recuperación y conservación del índice; probar APIs reales en entorno autorizado cuando exista. Después continuar los pendientes independientes de la matriz40, que todavía NO está completa.

---


## Historial: primer cierre OCR

Último código Android publicado: `dae3a8952a45e2ff835a14e7a32fe0fb1f564cca`, descendiente de c107654 sin perder Ajustes, conversaciones ni comandos. El checkpoint posterior contiene cierre y evidencia. Continuar en `feat/work-integral-settings-20261016`; comprobar HEAD y reserva remotos antes de editar. La reserva propia es de `work-gemini-native-20261014-f914c617`; no asumir que este documento acredita una sesión ejecutándose.

OCR PDF local conectado a DocumentText y FileIndex: texto nativo primero; hasta 3 páginas OCR/40 nativas/8 MB, imágenes acotadas y una extracción admitida; SHA y fragmentos por página/método, límites y errores parciales visibles. El índice migra de v1 a v2 aditivamente, conserva permisos y no convierte un permiso SAF revocado en importación. MIME genérico PDF/TXT/Markdown se normaliza sin sobreescribir tipos explícitos. Quick Look y Obsidian intactos.

Bloques posteriores del mismo conjunto: propiedad de bitmaps en OCR/etiquetado/vídeo hasta que las Tasks nativas terminan, y conservación de cobertura parcial al preparar capítulos PDF para IA. El prompt exacto revisado incluye la cobertura; no hay envío al escribir. Los plazos son cooperativos: no hay preempción dura de bibliotecas nativas ni afirmación de RSS medido.

**Verificación nueva sobre dae3a895:** 510 pruebas completas en 108 suites, cero fallos/errores/omisiones; lint Debug/Release/FunctionalPreview sin errores (10 advertencias y 1 informativa heredadas cada uno); Release/R8 y compileDebugAndroidTestKotlin correctos en 11 min. Verificación estricta posterior en 20 s: 21 referencias sin cambios, ninguna añadida/regrabada/modificada. Los 310 archivos de código/configuración/tests/referencias compilados coinciden con Git. Evidencia completa en `verification/ocr-20261017/summary.json` y descripción en `OCR-LOCAL-20261017.md`.

PdfOcrDeviceTest compila, pero NO se ejecutó: adb sin dispositivos y sin KVM. Debe probar PdfRenderer y ML Kit reales en un emulador limpio o instalación de prueba autorizada, sin desinstalar/reemplazar la principal. Las pruebas JVM usan fixtures; no prueban reconocimiento ML nativo, memoria ni One UI. No se repitieron los 30+5 tests Python porque su código no cambió. Rondas dirigidas y pipelines interrumpidos se distinguen del gate final en el informe.

No hay APK parcial. Release sin firma se usó solo para comprobar R8, paquete principal 2.0.1/código29/ARM64/min26/target35, zipalign16KB; apksigner confirma ausencia de firma. **No generar ninguna clave incompatible ni cambiar automáticamente de paquete para resolver la pérdida de P1.** La clave P1 está ausente; la entrega final requiere el mecanismo de firma autorizado y sus condiciones. La instrucción más reciente del usuario prevalece sobre alternativas antiguas de este documento. No hubo Actions, cambio de presupuesto, main, force-push ni modificación del PR16.

### Próximo paso funcional exacto

Vincular la extracción manual de texto de SpotlightPanel a un Job identificado por consulta/selección: cancelar al cambiar de resultado o consulta y al cerrar la vista, y descartar resultados tardíos que reabran el menú/preview anterior. El scope de composición ya cancela al destruir el overlay; falta el guard de vigencia de cada extracción manual. Añadir prueba de cancelación y finalización fuera de orden conservando el contrato de teclado y la geometría aprobada. No reconstruir cancelación del índice: el botón actual ya cancela indexJob y FileIndex.cancelRefresh. Después continuar los pendientes independientes de la matriz, sin presentar OCR nativo/semántica/las40decisiones como completos.

---

## Historial: estado al iniciar OCR

Último commit con cambios Android: `0c2f74fbefc374ae2842ab0d14f14c40c5d3fa0a`, publicado en la misma rama aislada. El checkpoint posterior contiene únicamente cierre documental/evidencia; obtener su HEAD y la reserva remotos antes de editar, sin volver a d4 ni descartar comandos/Ajustes/conversaciones. Gitleaks sin hallazgos en el árbol y 12 commits disponibles localmente.

Este bloque pasó 493 pruebas Android/JVM, tres lint sin errores, Release/R8 sin firma y 21 comparaciones visuales sin cambios. Evidencia: `verification/conversations-20261017/summary.json`; descripción: `CONVERSACIONES-20261017.md`. La clave P1 sigue ausente; no se creó una sustituta. No ejecutar assembleFunctionalPreview para ofrecerlo como actualización P1 con otra firma. No se entrega una APK parcial y no se declara completada la matriz40.

Siguiente cambio funcional: documentos autorizados, decisión35. Punto concreto: `index/DocumentText.kt` actualmente extrae texto PDF/Office/texto e imágenes, pero un PDF escaneado devuelve un error explícito. Incorporar OCR PDF local acotado reutilizando MediaAnalysis/MLKit y las utilidades PDF existentes, con cancelación, límites de páginas/píxeles/memoria y procedencia; no enviar contenido al proveedor ni sustituir el error por texto inventado. Conservar Quick Look. Añadir pruebas dirigidas y un escenario instrumentado del OCR real; si no hay dispositivo/emulador capaz de ejecutar sus bibliotecas nativas, registrar ese límite y continuar tareas independientes sin declararlo verificado. No confundir esta mejora con embeddings o búsqueda semántica. Mantener las prioridades y pendientes restantes de la matriz.

## Recuperación comprobada

Se recuperó el árbol Git limpio de `feat/work-integral-settings-20261016` en `f49096313baa3df3b815d89b0babf80634cdd058`; coincide con el HEAD remoto. La reserva `787eaa6dea380665d68332d97e963332c0b39db3` sigue siendo de `work-gemini-native-20261014-f914c617`, con ese mismo checkpoint. No se reclamó ni liberó una reserva ajena. Se conserva el trabajo completo de Ajustes, capacidades, ejecución, documentos y comandos nativos guardados.

Sobrevivieron los informes de 461 y 470 pruebas completas y las 38 dirigidas del bloque de comandos. La última sesión informó 483 correctas, pero sus XML y el final del pipeline no sobrevivieron en `/tmp`: ese resultado no se usa como evidencia recuperada de un gate integral. Los 30 tests de scripts y 5 de backend y sus informes sí se conservaron.

## Entorno y firma

El entorno temporal, SDK, caché Gradle y clave local de P1 se perdieron. Se restauraron Gradle 8.10.2 y Microsoft JDK 17.0.16 con comprobación SHA256, SDK/Build Tools 35 y dependencias. La ejecución usa una copia de código fuera de la raíz sincronizada, como en la sesión anterior.

**No existe la clave local que firmó P1.** No generar otra clave para presentar una actualización del paquete `dev.bautista.spotlight.next.preview.p1`. Su APK anterior y su certificado público permanecen disponibles; no permiten reconstruir la clave privada. Una futura entrega necesita recuperar esa clave mediante un mecanismo autorizado o completar el firmado histórico protegido; no generar otra clave incompatible. No se tocaron credenciales históricas ni la aplicación principal. No se entrega una APK parcial.

## Verificación de esta reanudación

- Gitleaks 8.30.1: sin hallazgos en el árbol actual y en los 10 commits disponibles localmente. No es auditoría del historial remoto completo.
- Comparación de archivos de referencia contra d4e3694:16 PNG anteriores idénticos; solo cambió `tablet-preferences.png`, con revisión documentada, y se añadieron 4 capturas de Ajustes. No se regrabaron barra/resultados/Quick Look para ocultar discrepancias.
- Pipeline completo correcto en 17 min 47 s: `:app:testDebugUnitTest :app:lintDebug :app:lintRelease :app:lintFunctionalPreview :app:assembleRelease`. Nuevas 483 pruebas, cero fallos/errores/omisiones; 103 suites. Tres lint sin errores, cada uno con 10 advertencias y 1 informativa heredadas. Release/R8 correcta, sin firma histórica. No se empaquetó P1 con un certificado nuevo.
- Verificación estricta posterior correcta en 22 s: `:app:verifyRoborazziDebug --tests '*VisualSnapshotTest'`. 21 referencias sin cambios; cero grabadas/añadidas/modificadas. XML completos preservados antes de filtrar los tests visuales.
- 301 entradas de código, configuración, tests y referencias de la copia compilada coinciden con el contenido Git de f490963. Release de comprobación: paquete principal conservado, 2.0.1/código 29, ARM64, min 26/target 35, no depurable,zipalign 16 KB correcto. Apksigner confirma que está sin firmar: **no es un archivo instalable ni una entrega al usuario**.

Evidencia versionada en [verification/integral-20261017/summary.json](verification/integral-20261017/summary.json): comandos, logs, manifiesto de 301 entradas, 483 casos JUnit, diagnósticos lint y 21 comparaciones visuales. El JUnit consolidado conserva suites/casos/estados; omite properties y salidas de consola duplicadas. Se registran SHA256 de los XML originales conservados fuera del árbol fuente. Los logs y resultados de la sesión anterior se distinguen de esta revalidación.

Estos resultados cierran los gates locales del código f490963; **no completan las 40 decisiones**. No declarar CI verde, instalación física ni verificación de proveedor. No hubo Actions, cambios de presupuesto, main, PR #16, force-push ni modificación de la estética aprobada de la barra.

## Bloque siguiente al primer cierre (ya realizado)

Tras publicar e768a6a se implementó la continuación estructurada de conversaciones guardadas (decisiones 15/24), reutilizando ContextStore y DirectAi. Se añadieron persistencia compatible y recorridos búsqueda→conversación guardada→revisión→HTTP fixture, comprobando cero envíos al escribir y la igualdad del historial autorizado. Código y resultados actuales están al inicio de este documento. La matriz conserva los pendientes reales; no entregar APKs parciales ni declarar completa la app por cerrar estos gates.
