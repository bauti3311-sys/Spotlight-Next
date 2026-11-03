# Firma histórica Native57 / code60 — decisión y ejecución pendiente

**Estado: auditoría y herramientas preparadas. Cero ejecuciones de Actions,
compilaciones de APK, firmas o claves nuevas.** El código APK autorizado es
`e9647c1329b93820383e3c8b4ec56dbceb3ce5f1`, de PR21, con funcionalidad f664484.
La automatización revisada tendrá su propio SHA: **no es el SHA del APK**.
Paquete `.raycastpreview`, code60, certificado completo
`885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba`.

## Hallazgos y decisión

1. El workflow histórico referencia `RAYCAST_PREVIEW_KEYSTORE_BASE64`, restaura
   el keystore y coteja885838aa. Eso demuestra el mecanismo, no que el secreto
   siga configurado. La API de secretos responde403 a esta integración; no se
   interpretó como ausencia ni se intentó obtener valores.
2. Sus modos antiguos se restringen a otras ramas; la ruta Preview de Antigravity
   exige code56/nombre experimental y restaura la clave **antes de Gradle**. No
   se debe usar esa ruta para60 ni relajar sus verificaciones.
3. No se encontró `Spotlight-AndroidStudio-0.9.1.zip` en el workspace, sus refs
   Git o Releases consultadas. El propietario aportó después su ubicación
   **histórica en Android**, aún no comprobada actualmente:
   `/storage/emulated/0/Download/Spotlight-AndroidStudio-0.9.1.zip`, equivalente
   a `~/storage/downloads/Spotlight-AndroidStudio-0.9.1.zip` en Termux. La entrada
   indicada es `SpotlightNext/signing/spotlight-debug.keystore`.
4. **Prioridad: firmador local Termux**, si el archivo sigue disponible, la clave
   se abre y el certificado coincide. El entorno cloud no puede leer esa ruta.
   No hace falta enviar el ZIP privado, la clave ni sus contraseñas al chat/cloud.
5. **Reserva: Actions**, si la vía local no resulta disponible. Requiere que el
   propietario confirme su configuración y autorice expresamente una ejecución
   del par exacto SHA fuente/SHA workflow. No se ha iniciado ni autorizado aquí.

La identidad se cotejó previamente con APK56 nativa y la nota57; el certificado
principal11b0 no es alternativa. No se intenta recuperar una clave privada desde
un APK/certificado público ni usar una clave diferente.

## Primera comprobación en Termux — sin firmar ni compilar Spotlight

Primero comprobar lectura, sin instalar herramientas ni abrir el archivo:

```sh
test -r "$HOME/storage/downloads/Spotlight-AndroidStudio-0.9.1.zip" && echo ZIP_DISPONIBLE || echo NO_DISPONIBLE_O_SIN_PERMISO
```

Si el enlace de storage aún no está configurado, `termux-setup-storage` solicita
el acceso local correspondiente; el usuario decide el permiso. Un error de lectura
no prueba que el ZIP haya sido borrado. No modificar ni borrar el ZIP original.

El kit público de preflight contiene sólo `native60_termux_preflight.sh`,
`Native60SignerPreflight.java` e instrucciones/checksums. Revisar su procedencia
antes de utilizarlo con material privado. Se necesitan `python` y un JDK17+;
**sólo si faltan**, los paquetes oficiales Termux `python openjdk-21` sirven. Se
comprobó en termux/termux-packages que existen OpenJDK17/21 y que `apksigner` es
independiente de plataforma y depende actualmente de OpenJDK21. No se instala
ningún paquete automáticamente. `apksigner` no hace falta para este preflight.

Desde la carpeta del kit extraído:

```sh
bash native60_termux_preflight.sh "$HOME/storage/downloads/Spotlight-AndroidStudio-0.9.1.zip"
```

El script lee **únicamente la entrada exacta indicada**, rechaza symlinks/ambigüedad
y tamaño inválido; extrae dentro del HOME privado de Termux con permisos700/600.
Rechaza HOME fuera del sandbox Termux y no usa TMPDIR compartido.
La utilidad Java pide las contraseñas **en la consola local sin eco**, selecciona
la única entrada cuyo certificado es885838aa y comprueba acceso a su PrivateKey.
No imprime alias/contraseñas, no obtiene bytes codificados de la clave ni firma
nada. La extracción temporal se elimina al salir/error/INT/TERM; no se copia a
Descargas ni se modifica el original. Ante interrupción abrupta que impida ejecutar
el trap, la copia permanece sólo dentro del HOME privado y debe retirarse localmente.

Sólo hace falta comunicar «ZIP accesible / certificado coincide / clave accesible»
o el fallo genérico. **No enviar archivos privados ni valores de credenciales.**
Esta comprobación aún no fue ejecutada en el dispositivo; el helper fue validado
sin material privado y su Java compiló con JDK17.

### Entrega por vía local, si pasa el preflight

1. Comprobar reserva/remoto. Congelar `e9647c1` como fuente exacta. Reutilizar52
   pruebas/Lint del código f664484, no repetirlas por este procedimiento.
2. Compilar **una APK unsigned** en cloud desde e9647c1, sin restaurar ningún
   firmador allí. Intento optimizado único, worker1 y memoria controlada; si R8
   falla, conservar diagnóstico y detenerse. No fallback/reintento automático;
   el perfil experimental anterior sólo tras causa analizada y justificación.
3. Verificar paquete/code60/noDebuggable/min26/target35/ARM64/16KiB/contenido
   nativo y ausencia de credenciales. Crear `git archive` del mismo SHA y manifest
   de hashes/entradas de APK. Entregar esos **datos públicos** por canal privado.
4. En Termux, cotejar los hashes aprobados antes de abrir el firmador. Recuperar
   otra vez sólo la entrada privada, verificar885838aa y firmar con `apksigner`
   oficial usando la entrada validada y prompts locales; v2 habilitado, min26,
   sin rotación ni clave nueva. Si hay más de una entrada, no elegir otra por
   defecto: identificar localmente la entrada que pasó el preflight.
5. Verificar el certificado del APK firmado localmente. Sólo el **APK final**
   puede volver a cloud para cotejar cert/package/16KiB y payload contra el
   unsigned aprobado; la clave y contraseñas permanecen en el dispositivo.
   La firma no equivale a instalar. El ZIP fuente debe seguir siendo el de e9647c1.
6. Entregar APK firmado+ZIP fuente+SOURCE_COMMIT+perfil+SHA256SUMS y protocolo
   físico. No instalar, desinstalar ni actualizar automáticamente la tablet.

Esta vía necesita una intervención local del propietario: no hay conexión Termux
remota en esta sesión. No se ha compilado unsigned anticipadamente sin confirmar
que el firmador pueda usarse.

## Alternativa Actions ya preparada — no ejecutar sin autorización

Se añadió **`mode=native60-once`** al archivo existente `build-apk.yml`, que ya
está registrado en la rama principal. No se modifica main ni se necesita mezclar
PR21 para preparar el modo. Los modos antiguos conservan sus restricciones.

- Disparador sólo `workflow_dispatch`; repositorio, actor y triggering_actor
  exactos `bauti3311-sys`; ref exacta de la rama de PR21.
- Fuente hardcodeada e9647c1. El input de fuente distinto se rechaza antes de
  usar la clave. El SHA workflow introducido debe coincidir con los SHA reales
  de definición y evento; revisarlo y autorizarlo aparte del SHA fuente.
- Guard de historial: `run_attempt=1`; otro dispatch de **ese mismo workflow SHA**
  se rechaza aunque el anterior haya fallado o sido cancelado. El grupo de
  concurrencia ya existente serializa la rama. No equivale a impedir para siempre
  una revisión futura: otro SHA requiere nueva revisión/autorización explícita.
- La reserva pertenece al operador que inicia la ejecución. El workflow comprueba
  propietario/lease/checkpoint exactos antes de cada job; no toma/libera reservas
  ni escribe refs. El operador debe conservarla hasta revisar el resultado y
  liberar sólo la propia con notas de cierre.
- Tokens de los tres jobs: sólo `contents:read` y `actions:read`. No escribe main,
  tags, Releases o notas desde CI; no checkout con credenciales persistidas.

### Tres jobs aislados, una sola ejecución

1. **Preflight, máximo5min:** valida dueño/SHA/reserva/no replay. Sólo tooling
   revisado y JDK; comprueba el keystore/certificado/PrivateKey en un temporal
   privado. Sin Gradle/SDK de la app. Falla antes de compilar si no existe o no
   sirve el firmador. Las credenciales se limpian al salir.
2. **Build unsigned, máximo30min:** checkout e9647c1, JDK17/SDK35 y Gradle wrapper
   del proyecto. Sin secretos de firma, sin archivos privados, caché read-only.
   `assembleRaycastPreview` optimizado una vez, sin pruebas repetidas ni retry o
   `optimize=false` automático. Verifica APK/code60/ELF/zipalign/archivo fuente
   exacto y transfiere sólo unsigned+ZIP+provenance a este mismo run (retención1día).
3. **Firma, máximo10min:** runner nuevo, sólo tooling del workflow revisado,
   sin ejecutar sourceZIP ni Gradle. Revalida política/artefactos/digests, carga
   la clave sólo en el paso de firma, comprueba885838aa y firma. Limpia claves
   **antes** de verificar y subir el artefacto final; `always()` limpia residuos.
   Valida cert v2, code60 y payload idéntico al unsigned. Sube sólo APK+fuente+
   evidencia pública verificada, retención3días. Acciones fijadas a commits completos.

Configuración previa que debe comprobar el propietario en Settings/Actions secrets,
sin copiar valores al chat: `RAYCAST_PREVIEW_KEYSTORE_BASE64`,
`RAYCAST_PREVIEW_STORE_PASSWORD`, `RAYCAST_PREVIEW_KEY_ALIAS`,
`RAYCAST_PREVIEW_KEY_PASSWORD`. Los tres valores auxiliares deben corresponder
al mismo respaldo histórico; el nuevo modo evita repetir valores de credenciales
literales en YAML/informes. No se modificó ningún secreto desde esta sesión.

Cupo actual **no verificado**. Máximos configurados:45min de runner Ubuntu sumados;
estimación sin medir de almacenamiento transitorio ~200MB entre unsigned/final
(con APK histórica~83MB y fuente~11MB), sin nuevos writes de caché. Si falta cuota
o configuración, no iniciar. No se promete que la primera corrida R8 pase.

### Autorización y comando — sólo para después de descartar la vía local

El operador debe fijar el **SHA completo de este workflow revisado**, cotejar que
siga siendo el HEAD de PR21 y registrar autorización expresa para ese SHA y e9647c1.
No basta con autorizar «el último HEAD». La preparación publicada y sus notas
indican el SHA de workflow para esa revisión; un cambio posterior invalida el visto
bueno. Confirmar los cuatro bindings y cuota desde la cuenta propietaria.

Después de esa autorización, adquirir una reserva propia nueva con el protocolo
normal, checkpoint=SHA workflow. Usar únicamente su owner/lease recién obtenidos:

```sh
gh workflow run build-apk.yml --repo bauti3311-sys/Spotlight-Next \
 --ref feat/preview57-native-return-20261103 \
 -f mode=native60-once \
 -f source_sha=e9647c1329b93820383e3c8b4ec56dbceb3ce5f1 \
 -f workflow_sha=<SHA_WORKFLOW_REVISADO_Y_AUTORIZADO> \
 -f reservation_owner=<OWNER_PROPIO_NUEVO> \
 -f reservation_lease_sha=<LEASE_SHA_PROPIO_NUEVO> \
 -f confirm_native60=AUTHORIZE_NATIVE60_ONCE
```

**No ejecutado.** Si falla cualquier etapa, no pulsar Re-run, no lanzar otro
dispatch, no alterar historial/reserva ni cambiar flags para eludir el guard.
Analizar el fallo y obtener una nueva autorización antes de otra revisión/intento.
Si la plataforma rechaza la configuración, detenerse: no usar el modo legacy ni
cambiar main como atajo.

Tras éxito: cotejar source/workflow/run y hashes descargados; sólo entonces
ofrecer el artefacto privado final, o una Release **nueva, privada y borrador**
para descargar APK/ZIP directamente (sin sobrescribir Releases previas). El
workflow no publica por sí solo. Registrar evidencia y cerrar sólo la reserva
propia mediante notas atómicas. Si se interrumpe al operador, no liberar por edad
ni usar el owner de otra sesión.

## Validación de esta preparación

21 pruebas de guardas/artefactos/aislamiento y6 del workflow anterior correctas;
Actionlint1.7.7 con checksum publicado verificado, sintaxis Bash de todos los
steps/helpers y javac17 correctos. No hay workflow dispatch, Android build,
keystore real, firma ni prueba en Termux ejecutados. Los fixtures ZIP de tests
son texto sin material criptográfico; no se generó ninguna clave.

[Evidencia](verification/native60-signing-plan-20261103/). El código de la APK,
versiones/dependencias, main, PR19/20 e instalaciones siguen intactos.
