# C5.4 — banco físico preparado, sin ejecución ni nueva Preview

Fuente de instrumentación preparada: c2f94e8a5f63230c48470015b76ac9047071c29a.

Base remota d712a14cb8e924f958fdf2526b1defd7e925e61d, PR21 en borrador. **C5 no está completo.** Los datos aceptados de C5.3 siguen siendo 7/12 pares relevantes léxicos, 5 omisiones, 0 falsos positivos; la evaluación semántica auténtica actual sigue sin realizarse.

## Estado real de esta entrega

Se preparan cinco métodos Android instrumentados, un DocumentsProvider de fixtures, activos reproducibles y un formato de evidencia. Se compilan sólo las fuentes Kotlin y Java. No hay dispositivo conectado; **cero tests Android ejecutados, cero resultados físicos, cero APK de aplicación/test, cero Actions, cero descargas de modelos**. Compilar no prueba Binder, DocumentsUI, OCR, JNI, gestos ni lifecycle.

Las fuentes app/src/main, Gradle, manifiesto productivo, preferencias, identidad67 y workflow de entrega no cambian. El provider y sus activos existen sólo en androidTest. No hay dependencias nuevas. MANAGE_DOCUMENTS es el permiso que el provider EXIGE a DocumentsUI, no un permiso solicitado por Spotlight. El manifiesto de test fusionado conserva REORDER_TASKS del monitor AndroidX existente; no se añade a producción.

La primera compilación detectó un uso incorrecto de use con PdfPreviewSession, que tiene close pero no implementa Closeable. Se corrigió con try/finally exclusivamente en el test. Se conserva el log inicial; no se modificó el renderer para acomodar la prueba.

## Target seguro: condición previa que no se puede omitir

**Preview67 instalada no contiene C5. No ejecutar estos tests contra ella.** La compilación preparada corresponde a DebugAndroidTest: test package dev.bautista.spotlight.next.test y target dev.bautista.spotlight.next, según manifiesto fusionado. No es un test APK dirigido a raycastpreview.

Una ejecución futura requiere autorización separada para disponer de APK de aplicación y test APK del mismo checkpoint, target compatible, con firma histórica aprobada coincidente. Este checkpoint no genera ni propone una nueva Preview y no incorpora otro package/flavor.

Usar sólo una instalación de pruebas autorizada sin carpetas personales ni acceso amplio al almacenamiento. No retirar accesos/datos personales existentes para convertir una instalación personal en banco.

No instalar un Debug APK encima de la aplicación personal dev.bautista.spotlight.next. Si ese paquete está ocupado por una instalación protegida, detenerse: el plan de instalación debe resolverse al autorizar los artefactos futuros. **Un segundo usuario/perfil Android separa datos, pero el APK de un paquete se comparte entre usuarios; no protege frente a cambiar su código.** No desinstalar, borrar datos, reemplazar certificados ni generar claves por conveniencia.

Los tests exigen argumentos de autorización, paquete esperado, source_sha declarado y digest del certificado aprobado. Comprueban que aplicación y test APK tengan un solo firmante con ese digest. La declaración de source_sha es una atestación del operador: **no prueba por sí sola el origen del APK**. Antes de instalar/ejecutar, cotejar APK+ZIP/manifiesto de la entrega autorizada, hashes y certificado. No usar el certificado que imprima cualquier APK como aprobación de su identidad. Si el test APK fue firmado con una clave debug distinta, detenerse; no hay autorización para sustituir la firma.

## Activos y aislamiento

- Corpus C5.3 idéntico, seis documentos/doce consultas. Los dos PDF se copian de la evidencia anterior y se verifican por hash.
- PDF nativo auténtico de tres páginas con Unicode y frases repetidas.
- PDF raster auténtico con glifos 1O mg. Para comparar el corpus C5.3 se conserva su transcripción OCR CONTROLADA y etiquetada; el test OCR separado sí invoca al motor real. No mezclar ambas fuentes de evidencia.
- Cada caso crea una base SQLite privada bajo cache/c5-isolated-UUID. FileIndex utiliza el ContentResolver real, sin acceso amplio: sharedAccess=false y sin raíces compartidas. Nunca se borra ni reconstruye el índice personal.
- El modelo se lee del noBackup del TARGET real. Un modelo instalado en raycastpreview no está disponible automáticamente para el paquete Debug. No se copia entre aplicaciones, instala o elimina desde el banco.
- El provider del test APK sólo lista tres archivos ficticios de su propia carpeta. Sus controles requieren permiso al árbol fijo. No recibe rutas arbitrarias, no expone documentos personales ni abre servicios de red.
- La lectura bloqueante usa una pipe real con escritor transitorio de máximo3s, sin servicio permanente. El estado debe vaciarse antes de que vuelva read(). Puede quedar un descriptor en curso hasta que termine/cierre; cancelar la corrutina no garantiza interrumpir JNI/Binder/read instantáneamente.

## Qué comprueba cada caso preparado

| Método | Evidencia futura esperada | Límite |
|---|---|---|
| C5DocumentDeviceTest#authenticPdfCitationsAndExplicitNativePreview | PDF real, páginas1–3, Unicode, offsets/literal/SHA, repetición por página; renderer nativo abre página2 desde acción explícita | No demuestra la barra ni visibilidad de todos los controles |
| C5DocumentDeviceTest#documentsUiPersistedGrantMutationBlockingCancelAndRevocation | Grant del árbol elegido en DocumentsUI, listado SAF real, modificación/borrado/lectura inaccesible controlada, observer, read bloqueante, cancelación y retirada del grant persistido | Selección del picker asistida por operador. No proveedor de nube ni muerte de proceso |
| C5DocumentDeviceTest#realOcrOnlyWhenExplicitlyEnabledAndResourcesAllow | PdfRenderer+OCR real existente, texto efectivamente reconocido, cita incierta/página1; tiempo/PSS | Si el operador no habilita o recursos no permiten, BLOCKED. Si se intenta y falla, FAIL; no convertirlo en éxito |
| C5FocusHostDeviceTest#touchScrollNeverOpensAndHostStopClearsTheIsolatedStudioPreview | Diálogo aislado con componentes existentes, gestos Android reales, cero aperturas por scroll, copia en adaptador de memoria, apertura explícita y limpieza al parar/reanudar Activity | No clipboard del sistema; no integración C5 en barra/overlay. Contador de apertura es solicitud, no postcondición de pintura |
| C5SemanticDeviceTest#optionalAuthenticModelComparisonOnTheUnchangedC53Corpus | Modelo auténtico, recursos/preferencias actuales, preparación explícita aislada; orden, TP/FP/FN, rango recíproco y tiempos por consulta léxico/semántico/combinado | Resultados semánticos NO se convierten a citas. El API no distingue vacío por no-match de todos los errores internos |

PASS del método semántico significa que la medición auténtica se ejecutó y registró; no significa mejora, ausencia general de falsos positivos ni adopción aprobada. Revisar cobertura parcial y métricas antes de cualquier decisión.

Los tiempos son observados en dispositivo cuando se ejecute. PSS antes/después corresponde al proceso completo, no pico ni RAM exclusiva del modelo. El límite1.2s de algunos asserts sirve para demostrar retirada ANTES del retorno de la pipe controlada2s; no es una nueva garantía de latencia productiva.

## Modelo y recursos: reutilizar Ajustes existentes

Ajustes → búsqueda de archivos/contenido → sección Búsqueda por significado. Inspeccionar el estado del modelo. La ruta existente Instalar modelo local pide confirmación, necesita aproximadamente170MB libres y verifica los tres tamaños/SHA fijados (137906937 bytes). **No se pulsa ni automatiza instalación en C5.4.** Si no está instalado, registrar BLOCKED_MODEL_MISSING; no repetir inferencias imposibles.

Cuando el propietario ya tenga un modelo verificado en el target autorizado: archivos/contenido/semántica deben estar habilitados fuera del modo privado efectivo, incluido el temporal. El test sólo LEE preferencias. Actualizar significado es la preparación manual existente; el caso instrumentado usa esa misma API sobre una base de fixtures, no sobre carpetas personales.

SemanticPolicy aplaza por memoria disponible insuficiente (<max768MiB/2×umbral), lowMemory, temperatura≥2 si soportada, ahorro, batería≤20% sin carga o datos desconocidos. No falsear recursos para lograr un PASS. Registrar estado de Ajustes, batería/carga/ahorro/temperatura, disponibilidad del modelo y mensajes de preparación parcial/error.

El caso opcional exige c5_semantic=true, ready() verdadero y políticas aptas. No llama install() ni codifica documentos por tecla. Usa DocumentEmbedder/ONNX/SQLite existentes, mismos umbrales .28 y margen .08, mismo límite de ocho resultados. Consulta y registra inmediatamente cada caso para conservar evidencia parcial si otro falla. Si la preparación produce cero vectores con modelo verificado/recursos aptos, FAIL con mensaje del motor. Si cambian privacidad/recursos, detener y conservar IN_PROGRESS/BLOCKED sin inventar el resto.

El SearchEngine general existente puede codificar consultas con semántica activa; este banco no modifica ese comportamiento ni conecta C5 a él. El índice documental se prepara explícitamente. No usar el smoke ONNX identity como evaluación de relevancia.

## Procedimiento futuro, sólo después de autorización de artefactos/ejecución

1. Verificar SHA remoto/reserva nueva, dispositivo y artefactos aprobados. Este protocolo no autoriza compilar, firmar, instalar o ejecutar ahora. No usar connectedDebugAndroidTest: empaqueta e instala APK automáticamente.
2. Comprobar target existente y runner. Deben corresponder al checkpoint autorizado y al certificado histórico; conservar Preview67. Si hace falta otra instalación del target, detenerse hasta contar con su autorización concreta.
3. Verificar firmas y hashes de los dos APK autorizados mediante apksigner verify --print-certs y sha256sum; cotejar procedencia con la entrega aprobada. No firmar ni generar claves.
4. Si el test APK aprobado todavía no está instalado, instalar sólo ese artefacto (sin -r ni desinstalación automática). Si existe otro test APK, detenerse y conciliarlo.
5. Una vez disponible el modelo autorizado, ejecutar sin WiFi/datos cuando sea posible y registrar la conectividad; no automatizar cambios de red. Elegir únicamente la raíz SPOTLIGHT C5 · SOLO FIXTURES desde el selector DocumentsUI invocado por el recorrido existente de carpetas. Conservar captura del picker/raíz. Esto es una acción explícita del operador sobre fixtures; no dar acceso a documentos personales ni cambiar otras preferencias.
6. Ejecutar primero PDF y host. OCR/SAF/semántica se seleccionan explícitamente por separado y sólo una vez por condición apta. Antes de SAF asegurar grant persistido exacto; el último paso lo libera. No intentar reotorgarlo programáticamente: si se repite un escenario por una causa documentada, requiere selección explícita nueva.
7. Recuperar JSON y salida JUnit/instrumentation; completar la ficha por dispositivo. Un SKIP no es PASS. No ajustar umbrales ni reintentar sin diagnosticar una falla.

Ejemplo de comandos **NO ejecutados en esta preparación**. Los valores de artefactos/identidad se obtienen de una autorización posterior, no de este ejemplo:

~~~sh
C5_SERIAL='SERIAL_AUTORIZADO'
C5_TARGET='dev.bautista.spotlight.next'
C5_TESTPKG='dev.bautista.spotlight.next.test'
C5_RUN='A9_C54_01'
C5_SOURCE='SHA40_DE_LA_FUTURA_ENTREGA_AUTORIZADA'
C5_CERT='SHA256_64_DEL_CERTIFICADO_HISTORICO_APROBADO'

adb -s "$C5_SERIAL" shell pm path "$C5_TARGET"
adb -s "$C5_SERIAL" shell pm list instrumentation
# Sólo con test APK ya autorizado/verificado y sin otro test APK en conflicto:
adb -s "$C5_SERIAL" install -t "$C5_TEST_APK"

adb -s "$C5_SERIAL" shell am instrument -w -r \
  -e class 'dev.bautista.spotlight.c5.C5DocumentDeviceTest#authenticPdfCitationsAndExplicitNativePreview' \
  -e c5_authorize FIXTURES_ONLY -e c5_run_id "$C5_RUN" \
  -e c5_source_sha "$C5_SOURCE" -e c5_target_package "$C5_TARGET" \
  -e c5_expected_cert_sha256 "$C5_CERT" \
  "$C5_TESTPKG/androidx.test.runner.AndroidJUnitRunner"
~~~

Para los otros casos, conservar las guardas y sustituir el valor de class por el método exacto de la tabla. Agregar solamente el selector correspondiente: -e c5_saf true, -e c5_ocr true o -e c5_semantic true. El host no necesita selector adicional. Cada uno genera un JSON de resultado propio; se puede usar un RUN_ID común por campaña y dispositivo. El banco rechaza sobrescribir una ejecución previa y guarda progreso de la misma ejecución con AtomicFile. Elegir un RUN_ID nuevo al documentar un reintento autorizado.

Los reportes quedan en getExternalFilesDir/c5-physical/RUN_ID y se anuncian en instrumentation status. Recuperación típica, sujeta a acceso real del dispositivo:

~~~sh
adb -s "$C5_SERIAL" pull "/sdcard/Android/data/$C5_TARGET/files/c5-physical/$C5_RUN" "./evidence-$C5_RUN"
~~~

Cada status también incluye c5_json con datos exclusivamente de fixtures; conservar la salida USB de instrumentation. Si pull queda bloqueado, registrar el bloqueo y conservar esa salida y el archivo/dispositivo. run-as sólo es alternativa si el target autorizado es debuggable; no asumir que funciona en Raycast Preview. No pedir permisos amplios nuevos ni extraer datos personales para resolverlo.

## Auditoría de unión futura con Studio — contrato, no integración

DocumentCitationState es el único estado de texto visible, sin rememberSaveable/Bundle/preferencias ni restauración de una cita autorizada anteriormente. Una consulta nueva limpia primero y cambia generación. No mantener una copia paralela de quote/opened/result.preview que reviva al recomponer o navegar.

Conectar invalidación síncrona a cancelación, cambios de configuración efectiva/privacidad, revisión del índice, avisos del proveedor y salida del contexto. Activity ON_PAUSE/STOP no basta para el overlay: su LifecycleService puede seguir STARTED. Reutilizar la pauta existente de AuthorizedFilePreview (foco de ventana, lifecycle y reapertura autorizada); cerrar/invalidate al ocultar/desmontar WindowManager y al volver de Ajustes de permisos. No añadir servicio o polling permanente.

SAF carece de aviso universal de revocación. Descartar estado al perder foco y revalidar al retornar; siempre withValidated antes de mostrar/copiar/abrir. Revocar persistencia no retira necesariamente un descriptor ya abierto o un grant transitorio. No presentar una URI guardada como autorización.

Copiar debe ser explícito y conservar fragmento literal, documento, URI/fuente, SHA y página sólo conocida. La prueba usa adaptador de memoria para no tocar el clipboard personal. El texto ya copiado no puede borrarse retroactivamente. Abrir fuente debe entrar únicamente por acción validada al Quick Look existente; scroll/selección/recomposición no abren documentos. Al invalidar, quitar también el renderer y cancelar su trabajo.

El diálogo instrumentado ensaya este contrato y usa componentes Studio/Quick Look existentes, pero no demuestra que el overlay productivo cumpla hooks todavía no integrados. La referencia semántica sigue pendiente de mapear offsets searchableText/body y de pasar las puertas C5.2. No hay adopción de un puente, nueva pantalla productiva ni cambio de Focus.

## Aceptación

Usar C5-4-PHYSICAL-ACCEPTANCE-TEMPLATE.md. Cerrar C5 requiere evidencia física real y resolver defectos pendientes; este checkpoint sólo prepara el banco. C3/C4/C6–C8, Spotify y Square siguen pausados.
