# Spotlight Next 1.2.0: búsqueda universal y Release

La barra de búsqueda resuelve intenciones localmente; escribir no cambia el
volumen, no envía mensajes y no consulta IA remota. Reutiliza SearchEngine,
IntentRouter, FileIndex, IntegrationRegistry, MessageEditor y la revisión de IA.
Las categorías siguen disponibles, pero estas consultas no requieren elegirlas.

| Consulta | Resultado operativo | Requisitos y límites |
| --- | --- | --- |
| `volumen al 60%` | Cambiar volumen multimedia al seleccionar | AudioManager existente; Android puede redondear al nivel disponible |
| `brillo al 40%` | Revisar, confirmar y restaurar brillo | Shizuku conectado/autorizado; mínimo seguro existente de aproximadamente 8% |
| `abrir Spotify` | Abrir actividad instalada; alternativa web | Si no está instalada, solo aparecen alternativas reales |
| `buscar fotos de vacaciones` | Resultados del índice de imágenes en la misma lista | Búsqueda de archivos habilitada y almacenamiento/carpeta autorizado; nombres y contenido ya indexado, sin inventar comprensión visual |
| `buscar fotos de ayer` | Filtrar imágenes por fecha de creación | Usa el parser y el índice existentes |
| `mandar WhatsApp a Juan` | Preparar borrador y revisar destinatario | WhatsApp compatible y número válido; contactos opcionales; envío final en WhatsApp |
| `buscar información sobre rehabilitación vestibular` | Combinar resultados existentes, web e IA en la misma lista | Web requiere conexión; IA requiere configuración y revisión explícita antes de enviar |

También se reconocen `iniciar <app>`, `ajustar el volumen a 60 %`,
`poner volumen 60`, `brillo 40`, `buscar imágenes de <término>`,
`buscar sobre <tema>` e `investigar sobre <tema>`. Permanecen los comandos,
alias, consultas estructuradas, calculadora, notas, portapapeles, contactos,
calendario, notificaciones, rutinas y demás integraciones existentes.
Los porcentajes fuera de 0–100 muestran un error; ya no se convierten
silenciosamente en un ajuste distinto al solicitado.

Para consultas generales, cada proveedor publica resultados progresivamente.
El ranking prioriza coincidencias exactas y conserva el resultado más relevante
cuando dos proveedores publican la misma identidad. Favoritos y uso aprendido
siguen aportando puntuación. Los grupos se ordenan por su mejor resultado,
no por la cantidad de filas. Las consultas exactas de integraciones conocidas
no activan un escaneo de archivos innecesario; sus acciones y otros proveedores
ligeros se muestran juntos. Se conserva el orden de inicio sin consulta.
Las acciones interpretadas muestran una etiqueta discreta y requisitos en hasta
dos líneas, sin cambiar el tema ni la disposición general.

## Release y datos históricos

El propietario extrajo estos datos de `Spotlight-Next-1.1.0-arm64-release.apk`:

- applicationId: `dev.bautista.spotlight.next`
- versionName: `1.1.0`
- versionCode: `23`
- SHA-256 del certificado:
  `11B0B3B37544BD95B128DE52798FA7FFB7489F94819614B1ADA383BD4EE7A6CF`

La versión nueva declara `1.2.0`, código `24` y el mismo identificador.
Android mínimo 26, objetivo 35 y Release arm64 se mantienen; esto incluye
la arquitectura de la Galaxy Tab A9+. No hubo prueba en una tablet física.

`Build Signed Release APK` solo permite una ejecución manual del repositorio
original sobre `main`. No firma PRs, forks ni otras ramas. La compilación,
las pruebas y lint se ejecutan antes de recibir secretos. Gradle produce un
Release sin firma; el paso aislado posterior usa exclusivamente los cuatro
Secrets ya configurados y `apksigner`. No existe fallback a una clave Debug
para Release. La configuración privada local opcional de Debug se conserva.

El keystore se reconstruye con permisos restrictivos dentro de `RUNNER_TEMP`
y se elimina con un trap, incluso ante fallos. Las contraseñas se pasan mediante
variables de entorno; no se escriben propiedades de firma ni se imprimen
diagnósticos de firma que puedan contener detalles sensibles. Se ignoran
`*.keystore` y `*.jks`. Ninguna clave privada se incorpora al repositorio.

Antes de publicar, el workflow verifica la firma real con `apksigner`, exige
un único firmante con la huella histórica exacta, comprueba alineación y lee
el manifiesto del APK con `aapt`. Rechaza otro identificador, código <= 23,
APK debuggable o un mínimo Android superior al existente. Cualquier fallo
impide subir el artefacto. El APK y un informe público de verificación se
conservan 14 días; no se suben keystore, contraseñas, logs de firma ni carpetas
completas de trabajo.

## Obtener el APK

Después de revisar e integrar el PR manualmente:

1. GitHub → repositorio → Actions → **Build Signed Release APK**.
2. **Run workflow** → elegir **main** → ejecutar.
3. Esperar una ejecución exitosa y revisar la verificación en el resumen.
4. Descargar `spotlight-next-1.2.0-release-<número>` en Artifacts.
5. Extraer el ZIP, conservar `update-verification.txt` y abrir
   `spotlight-next-release.apk` en Android para instalar encima de 1.1.0.

No hace falta introducir datos de firma ni la huella al ejecutar el workflow.
No se debe desinstalar la instalación previa. El workflow verifica las
condiciones de identidad, certificado y versión; la prueba de instalación y
conservación de datos debe hacerse en el dispositivo con el APK firmado.
Los APK Debug siguen disponibles en el workflow previo y no se ofrecen como
actualizaciones de la instalación histórica.

Este PR no se integra automáticamente. Al restringir la firma a `main`, el
primer Release firmado se generará después de la integración manual. Una
compilación Release local sin firma no prueba la actualización histórica.

## Verificación de esta entrega

- `:app:testDebugUnitTest`: 221 pruebas, 51 suites, cero fallos, errores u omitidas. Incluye 12 pruebas nuevas de intenciones, ranking e integración del motor.
- `:app:lintDebug`: cero errores; se conservan las 10 advertencias existentes.
- `:app:assembleDebug`: APK generado y firma Debug válida con `apksigner`. Su certificado real fue rechazado por el verificador de la firma histórica (prueba negativa).
- `:app:assembleRelease`: compilación optimizada R8 y reducción de recursos completadas. APK local sin firma generado; `aapt` y el verificador confirmaron identificador histórico, código 24 > 23 y mínimo Android 26.
- Backend: cinco pruebas aprobadas. Verificador Release: diez pruebas aprobadas, incluyendo certificado incorrecto/múltiple, versión insuficiente, identificador incorrecto y APK debuggable.
- Sintaxis Bash/Python, ambos YAML y `git diff --check`: verificados.
- La prueba visual de expansión, desplazamiento y regreso pasó; se revisó su captura. Los formularios y pruebas visuales existentes pasan.

Pendientes: ejecución del workflow firmado en `main` y prueba de instalación
sobre la versión histórica en el dispositivo. Los Secrets no están disponibles
en este entorno local. No se afirma que la actualización haya sido validada
hasta verificar el APK firmado real.
