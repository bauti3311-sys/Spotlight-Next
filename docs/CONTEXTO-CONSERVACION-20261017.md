# Conservación de contextos y cancelación ·17/10/2026

Código4f429c0120aacf78c6568f209c747c8d4c10068b, continuación de47f3384 y del cierre documental a34098e. Continúa ContextStore y ContextPanel existentes; no crea memoria remota, otro motor IA ni una interfaz nueva. Se preservan conversaciones, JSON versión1, preferencias y el aspecto normal de los controles.

## Problemas reproducidos

Ocho casos contra producción642b00c sin modificaciones y fixtures de prueba: siete fallos esperados. Fijar antes de start perdía guardados/conversaciones previos; borrar antes de start perdía los registros no elegidos; el fijado31 expulsaba un contexto sin pedirlo; refijar perdía campos desconocidos; errores de escritura dejaban estado optimista incorrecto al fijar o eliminaban el activo antes de confirmar borrado. Una lectura de contexto que terminaba después de Quitar todavía emitía Action.Ask. El caso que impedía borrar archivos fuera de context-imports ya pasaba y se conserva.

La evidencia identifica hashes de las fuentes anteriores y confirma que ContextStore/ContextPanel compilados coincidían con642b00c; no se atribuye un fallo de fixture a producción.

## Cambios conectados

pinActive/removeContext esperan la carga persistida, igual que las operaciones de conversaciones. Fijar31 se rechaza con un mensaje claro; no se expulsa información. Se puede refijar uno de los30 existentes, conservando identidad, registros y metadatos desconocidos. Guardar sigue siendo una acción local explícita; no se persiste contexto temporal automáticamente.

Los contextos fijados se serializan conservando campos desconocidos de registros válidos, junto con las conversaciones y propiedades raíz existentes. No hay migración destructiva. El archivo continúa siendo privado/noBackup y AtomicFile. La memoria visible se publica tras escritura exitosa. Un error de escritura comunicado por AtomicFile mantiene fijados y activo anteriores. Quitar una referencia/importación ocurre después de confirmar el borrado; no antes de un save que puede fallar.

Una importación privada en proceso de fijado queda retenida hasta cerrar la escritura. Una vez iniciado ese pequeño commit local, escritura y publicación terminan juntas en NonCancellable; cancelar el llamador no deshace una operación de fijar/borrar ya confirmada. La IO continúa fuera del hilo UI. La liberación sigue restringida al directorio gestionado context-imports; no borra archivos ajenos. No es una promesa de recuperación frente a todo fallo de disco/proceso ni de latencia medida.

ContextPanel muestra errores de capacidad/escritura mediante el texto de error existente, sin dejar excepciones de la operación sin tratar. Reutiliza DocumentExtractionRequest para lectura manual: propietario por ID de contexto/página/payload, cancelación al quitar/cambiar/cerrar y guard de identidad actual antes de publicar. El botón existente Revisar envío a IA cambia temporalmente a Cancelar preparación. Una lectura tardía no reabre una revisión ni publica errores antiguos. Cancelar no intenta preemptar una lectura bloqueada o una Task nativa; descarta su resultado y conserva el propietario nativo previo.

El texto vigente genera PRIVATE_CONTEXT y requiere la revisión/envío existentes. No se envía al proveedor por leer, seleccionar, cancelar o teclear. Los timeouts internos se distinguen de cancelar la solicitud y permiten informar error. No se añade recogida de datos, memoria por entidades o ejecución automática de herramientas.

## Pruebas y alcance

Se recuperaron los XML y el log de36pruebas dirigidas/7suites correctas y compilación instrumentada en1min30s, anteriores a publicar4f429c0. Incluyen13casos nuevos y regresiones de conversaciones, portapapeles automático, producción Gemini y selección documental. No sobrevivió un manifiesto inmutable de entradas de aquella ronda: su resultado se distingue de los gates completos nuevos. Las rondas anteriores34/35no se suman. Los gates completos del código publicado se registran por separado en handoff/evidencia.

Nueve pruebas de persistencia utilizan JSON/AtomicFile y archivos reales del sandbox JVM. El fallo de escritura se induce con una ruta .new bloqueada; no es fault injection de hardware. La prueba Compose de capacidad muestra el error, conserva los30IDs, el activo sin fijar y el archivo sin cambiar. Las tres de ciclo de vida leen mediante un proveedor Android fixture y DocumentText reales, reteniendo la IO hasta después de Quitar/cancelar; no emiten Action.Ask tardío. Un texto vigente sí prepara revisión, sin conceder consentimiento.

Android está simulado con Robolectric: no acredita permisos/SAF del dispositivo, One UI, reconocimiento ML ni latencia/memoria/batería físicos. No hay clave real, llamada real a Gemini ni tráfico HTTP añadido. La matriz40 permanece parcial: falta memoria de alto nivel, semántica/embeddings medidos, grounding y tareas persistentes, entre otros pendientes. No se implementó recuperación general de JSON corrupto.

Sin clave nueva, APK parcial, Actions, cambio de main/PR16/budget o force-push. La entrega única final requiere la firma histórica autorizada y sus checks del SHA exacto; no se reemplaza por un certificado incompatible.


Seguimiento de recursos: seleccionar otro contexto ahora retira la importación temporal sustituida, mantiene las fuentes fijadas y cancela su temporizador anterior. El caso fallaba contra47f3384 por retención del archivo; se añadió protección de propiedad de Fijar durante carga/espera para no guardar una selección privada posterior. Los gates de47f3384 se interrumpieron después de560pruebas y lintDebug, antes del cierre, para reproducir/corregir estos seguimientos. No se declara verde aquel pipeline. El cierre definitivo usa el código posterior y su evidencia propia.


## Cierre verificable del código publicado

Código4f429c0120aacf78c6568f209c747c8d4c10068b, sobre47f3384 y el cierre documental a34098e. ContextStore espera carga antes de modificar, conserva fijados/conversaciones/campos desconocidos, rechaza el31sin expulsar y publica cambios solo tras AtomicFile exitoso. Fijar queda ligado al contexto solicitado antes de esperar carga; cambiar selección retira importaciones temporales sustituidas sin borrar fuentes fijadas. ContextPanel cancela/desestima lectura tardía al quitar/cambiar/cancelar antes de preparar PRIVATE_CONTEXT; no envío automático ni nuevo motor/interfaz.

**Gates nuevos4f429c0:**562pruebas completas/119suites, cero fallos/errores/omisiones; lint Debug/Release/FunctionalPreview sin errores, recuentos exactos en verification/context-preservation-20261017/lint-summary.json. Release/R8 y compileDebugAndroidTestKotlin correctos (BUILD SUCCESSFUL in 12m 37s). Verificación visual estricta posterior:21sin cambios, cero grabadas/añadidas/modificadas (BUILD SUCCESSFUL in 21s).326entradas compiladas coinciden con Git y no hay fuentes adicionales. Evidencia: verification/context-preservation-20261017/summary.json. Los36dirigidos recuperados no se suman a562; no sobrevivió su manifiesto de entradas. El pipeline de47f3384 se interrumpió después de560/lintDebug para corregir dos seguimientos; no se declara completo.

El primer intento nuevo falló en6min48s por SIGSEGV interno de C2/OpenJDK17.0.16 al compilar SQLiteDatabase instrumentado por Robolectric; no fue una suite aprobada. Se conserva extracto/hash del crash. El reintento usa el init local runtime-test-c1.gradle: únicamente los JVM de Test se limitan a C1 (-XX:TieredStopAtLevel=1), sin omitir pruebas/aserciones ni cambiar código/workflows; Gradle/R8 no cambian. No es una medición de rendimiento.

Siete fallos esperados en8casos contra642b00c y dos regresiones individuales posteriores constan con hashes/casos. Fijados/conversaciones usan archivos reales del sandbox JVM; AtomicFile falla mediante ruta .new bloqueada, no fallo físico de disco. Compose recorre capacidad y lecturas de proveedor fixture por DocumentText; no emite Ask tardío. PRIVATE_CONTEXT vigente sigue exigiendo revisión exacta. Ver CONTEXTO-CONSERVACION-20261017.md.

No hay dispositivo ni KVM (comprobados de nuevo): instrumentación compilada, NO ejecutada; sin reconocimiento ML nativo, One UI o medidas físicas. Release interno sin firma principal29/ARM64/min26/target35/no depurable y zipalign16KB correcto; no instalable ni entregado. Sin clave nueva, APK parcial, Actions, cambios main/PR16/presupuesto o force. Gitleaks árbol/29commits locales sin hallazgos antes del cierre, no historial remoto completo. Python30+5 histórico, no repetido. La matriz40 permanece parcial y la firma final exige el procedimiento histórico/checks del SHA exacto.

### Siguiente bloque funcional exacto

Decisiones7/18: comprobar que JSON ilegible, versión desconocida o registros no válidos no se confundan con almacenamiento vacío y se sobrescriban al fijar/guardar/borrar. Hay cuatro escenarios preparados fuera del árbol; aún no compilados ni corregidos. Reproducir primero contra4f429c0 y proteger lectura/escritura conservando bytes, errores visibles y reintento explícito. No reparar eliminando datos, no añadir memoria automática o envío remoto. Después continuar pendientes viables de matriz40; embeddings/grounding/proveedor/dispositivo no quedan acreditados por estas pruebas. Consultar HEAD y reserva propia antes de editar; el checkpoint no es ejecución en segundo plano.
