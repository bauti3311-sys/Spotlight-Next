# Checkpoint continuo — código pendiente de validación Android

Base2ec43592. Se continuó después de cada publicación; PR18 en borrador, reserva propia y actualizaciones condicionales sin force. No SDK/Gradle descargado, Actions ni APK nueva.

|Bloque|Commit|Resultado de código y límite|
|---|---|---|
|D28 dynamic forms|88f2bf0f|Formularios por esquema, opciones y validación conjunta; no ejecutados.|
|D26 contextual public app capabilities|d33f4a5a|Búsqueda/compartir por handler público y paquete exacto; destino real pendiente.|
|Spotify bounded search and PKCE|32fad862|Search HTTPS acotado/cancelable y PKCE S256 de un uso; SIN login de cuenta, almacenamiento de tokens, UI de resultados ni reproducción App Remote.|
|Cast authorized media/range policy|1c17960a|Política de acceso SAF/rangos; SIN servidor, descubrimiento, Sender SDK, conexión ni transmisión.|
|D36 shared import feedback and cancellation|f0d5af41|Progreso/cancelación y publicación protegida; Activity→ContextStore pendiente.|
|D37 honest local opening measurements|e8766a32|Primera apertura no es arranque frío; heap Java no es RAM/PSS; SIN benchmarks físicos.|
|D35 per-result freshness and newest observations|8921e3b0|Fuente/caché/hora de consulta por fila y observación más reciente; no implica datos en vivo.|
|D28 preserve readable commands beside unknown records|89cc9261|No ocultar comandos compatibles por metadata futura; datos sin migración destructiva.|
|Cast range overflow hardening|a81df13b|Overflow de longitud rechazado; caso pendiente.|
|D36 retain configured appearance during import|d70611aa|Conservar preferencias de aspecto; UI pendiente.|

44 casos Android/JVM nuevos escritos en esta ejecución; junto con17 previos de D28 son61 casos pendientes en las clases enumeradas. Ninguno compilado/ejecutado. Se ampliaron además aserciones existentes. No sumar repeticiones ni contar las60 pruebas históricas de D27 como nuevas.

Verificación ejecutada: `bash -n scripts/test-d28-directed.sh` correcto; `python3 -m unittest discover -s scripts -p test_directed_runner.py -v`:5/5, cero fallos, SOLO filtros/guardas del script con stub. Repetición tras añadir init de forks sin contar doble. No prueba Gradle ni SDK. Comparación byte a byte: manifest, app/build.gradle.kts, release.properties y wrapper.properties idénticos a la fuente Preview45/39430eb2 (JSON adjunto). Fixture SharedTextProvider y pruebas ClipData históricas conservados.

Runner local: `bash scripts/test-d28-directed.sh ui` primero; luego `d28`, `capabilities`, `spotify`, `cast`, `imports`, `metrics`, `sources`. Offline, sin descargar distribución, un worker y test JVM por clase. `all` solo si las rondas dirigidas lo justifican. Entorno requerido JDK17/Gradle8.10.2/SDK35 con dependencias cacheadas. No invocar CI sin autorización.

Fuentes técnicas consultadas el23/10/2026: [Spotify Search](https://developer.spotify.com/documentation/web-api/reference/search), [PKCE](https://developer.spotify.com/documentation/web-api/tutorials/code-pkce-flow), [restricciones Development Mode](https://developer.spotify.com/documentation/web-api/tutorials/february-2026-migration-guide), [App Remote oficial](https://developer.spotify.com/documentation/android/tutorials/getting-started), [Cast Sender](https://developers.google.com/cast/docs/android_sender/integrate), [release notes Cast](https://developers.google.com/cast/docs/release-notes). Search limita10 por tipo; la cuenta/aplicación necesita cumplir las restricciones vigentes. Cast23.0.0 incorpora MediaRouter1.9.0-beta01: compatibilidad transitoria no comprobada, no se añadió dependencia. [Remote v2 del mantenedor](https://github.com/tronikos/androidtvremote2) es una implementación Python Apache2, no una API pública garantizada ni un adaptador Kotlin validado. [Fallback oficial Google TV](https://support.google.com/androidtv/answer/6122465?hl=en): selección de dispositivo y código en la app Google TV. No se instaló ni ejecutó software de terceros.

Matriz sin promociones:9 completas/25 insuficientes/6parciales. D28/D26/D35/D36/D37/D40 conservan brechas y validación pendiente. Preview44/45 intactas; no atribuirles estos cambios. UI08f19055 permanece pendiente. El siguiente gate verificable es ejecutar el grupo ui, compilar D28 y corregir errores antes de ampliar la activación de integraciones.

Revisión posterior: 6c55b11a endurece tokens/cabeceras y URLs privadas; dos casos nuevos incluidos en44/61, todavía sin ejecución. El runner5/5 cubre únicamente tooling; ninguna promoción en la matriz. Hay suficientes cambios de producción separados para una primera compilación: no ampliar dependencias, activar login/playback ni abrir un listener LAN antes de esa verificación.
