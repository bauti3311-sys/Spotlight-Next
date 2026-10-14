# Entrega debug actual: F2.1 / 58ac0a2

Fuente exacta `58ac0a2d4cb4f96c9fbef46da4f4104475c251ae`, CI38528963148 completed/success. Incluye cierre táctil48dp y acciones adaptables F2.1. Android de la rama documental6de57a9 es idéntico, pero el ZIP y el workflow fijan58ac0a2 para corresponder al commit con CI aprobada.

## Descargar o iniciar en la tablet

La publicación de esta rama activa una sola entrega automáticamente por push, limitada a esta rama; comprobar primero si ya hay una ejecución en curso o correcta. Usar Run workflow únicamente si no existe una entrega válida. Los siguientes pasos describen la alternativa manual.

1. Abrir https://github.com/bauti3311-sys/Spotlight-Next/actions/workflows/build-apk.yml en Chrome o Samsung Internet, con sesión GitHub y acceso al repositorio privado.
2. Si no aparece el botón, activar **Sitio de escritorio** en el navegador. Pulsar **Run workflow**, elegir **fix/debug-delivery-f21** y ejecutar una sola vez. La selección de rama es imprescindible para usar esta configuración.
3. Esperar el job verde. En el resumen están **Descargar APK debug** y **Descargar ZIP fuente**; también aparecen los artefactos `Spotlight-Next-debug-58ac0a2` y `Spotlight-Next-source-58ac0a2`. Duran7días y requieren sesiónGitHub. No hay enlaces de artefacto hasta que termine la ejecución.
4. GitHub entrega cada artefacto en ZIP. En Mis archivos→Descargas, extraer el ZIP de APK y abrir `Spotlight-Next-debug-58ac0a2.apk`; el otro contiene el ZIP completo de fuente, `SOURCE_COMMIT` y `SHA256SUMS`.
5. Si Android solicita permitir instalación desde Mis archivos/navegador, revisar y conceder ese permiso para instalar. Abrir Spotlight y conceder permiso de aparecer sobre otras aplicaciones cuando lo solicite. Ante conflicto de certificados, detener instalación; no desinstalar la versión existente ni borrar datos.

ZIP fuente inmediato, equivalente de `git archive` GitHub: https://github.com/bauti3311-sys/Spotlight-Next/archive/58ac0a2d4cb4f96c9fbef46da4f4104475c251ae.zip . Puede tener otro prefijo/hash de archivo que el ZIP del artefacto; el contenido fuente corresponde al mismo SHA.

## Identidad e integridad verificadas localmente

APK2.0.1/código28, paquete `dev.bautista.spotlight.next`; mínimoAndroid8.0/API26, target35, sin maxSdk; ARM64 incluido. Android16/API36 cumple requisitos estáticos. No instalación física en OneUI; F1.3 foco frío continúa abierto.

APK local actual **178657864bytes**, SHA256 `34a1fd6f46079b29a8e621b98f45f4e725d737a91ed4b84800c21ced0dba6b38`. ZIP `git archive` local **4008664bytes**, SHA256 `32a60f6f0491771f51957e82b16f497062745ad8b39cd228f673cbb8c8c27360`. APK verificada con apksigner y zipalign, ZIP con unzip-t. AssembleDebug cached SUCCESS29s;37tareas up-to-date; no tests repetidos.

Certificado local Android Debug SHA256 `92e631296a7ed975b5926adb876cb97af090dcd28bc3c0bbec5c2dff138c8182`. Histórico de referencia (.agents/skills/spotlight-release): `11B0B3B37544BD95B128DE52798FA7FFB7489F94819614B1ADA383BD4EE7A6CF`. **Son distintos: esta debug no puede actualizar una instalación firmada con la histórica.** No se leyó el certificado de la instalación de la tablet, por lo que no se afirma qué firma tiene esa instalación. La firma histórica no se usa ni cambia.

El runner recompila solo debug del SHA fijado, con su certificado debug; no puede consumir los archivos privados de este workspace. Su hash/tamaño/certificado pueden diferir de los locales: verificar los valores publicados en su resumen, `SHA256SUMS` y `certificate.txt`, no usar el hash local para validar la APK recompilada. Certificado distinto del instalado también impide actualización aunque ambas APK sean debug.

## Alcance y continuidad

Un único job de entrega en esta rama por push o dispatch manual, caché, cancelación y timeout15min: assembleDebug, verificación identidad/certificado/alineación, ZIP fuente y dos uploads. Cero emuladores, tests adicionales, firma histórica o release; sin cambios Android/dependencias/main/PRs/checkpoints. No activar una matriz ni pagar servicios.

El conector no ofrece dispatch y la CLI previamente devolvióForbidden. Se usa el evento push normal de la rama específica para iniciar la entrega autorizada sin tocar main; si GitHub la bloquea, usar la alternativa manual indicada. No se afirma publicación de artefactos hasta verificar el resultado. Continuidad funcional sigue6de57a9/PR15; esta rama solo prepara entrega y no iniciaF2.2. Resultado del intento de despacho y SHA de orquestación en PROGRESO/notas.
