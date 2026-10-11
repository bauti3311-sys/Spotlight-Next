# Desarrollo y APK Debug de Spotlight Next 1.1.0

## Compilación

El único módulo Android es `:app`, con Kotlin/Jetpack Compose y AIDL. Usa
AGP 8.7.3, Kotlin y plugin Compose 2.0.21, Gradle Wrapper 8.10.2, JDK 17,
Android SDK 35 y Build Tools 35.0.0. La app declara versión 1.1.0 (código 23),
Android mínimo 26 y objetivo 35. No necesita NDK para esta compilación Debug.

Las dependencias se obtienen de Google Maven, Maven Central y Gradle Plugin
Portal. Incluyen AndroidX/Compose, Shizuku, coroutines, OkHttp, ML Kit y
PDFBox. Las pruebas usan JUnit y Robolectric; este último descarga su runtime
Android desde Maven Central. No se necesitan API keys para compilar ni para
las pruebas locales del backend opcional.

Con JDK 17 completo y el SDK instalado:

```bash
bash gradlew :app:assembleDebug --no-daemon --max-workers=2 --stacktrace --console=plain
bash gradlew :app:testDebugUnitTest :app:lintDebug --no-daemon --max-workers=2 --stacktrace --console=plain
python3 -m unittest discover -s backend -p 'test_*.py'
```

Se usa `bash gradlew` porque el wrapper está rastreado sin permiso ejecutable.
El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

En la instancia de Codex preparada en esta tarea, activar las herramientas con
`source /workspace/toolchains/env.sh` desde `/workspace/Spotlight-Next`.
El instalador reutilizable se guarda también en la configuración del entorno;
su guardado no equivale a publicar una nueva versión del entorno. Cada tarea
cloud ya está aislada: usar el checkout existente, sin crear worktrees.
No se necesita iniciar un servidor para desarrollar o compilar Android.

## GitHub Actions y descarga desde Android

`Build Debug APK` se ejecuta al enviar cambios a `main`, en PR hacia `main`
y mediante `workflow_dispatch`. Configura Java 17 y SDK 35, valida el wrapper
y mantiene caché de Gradle/dependencias. Cancela ejecuciones anteriores de la
misma rama y limita cada ejecución a 30 minutos. Las pruebas y lint son pasos
obligatorios, con stacktraces y una explicación si fallan.

Solo sube el APK Debug, comprimido por GitHub en un ZIP descargable y retenido
siete días. El APK se publica después de una compilación exitosa; una prueba
o lint posterior puede dejar la ejecución en rojo aunque exista ese APK.
No se compila Release ni se publica una distribución.

1. Desde Chrome en Android, iniciar sesión en GitHub y abrir el repositorio.
2. Entrar en **Actions → Build Debug APK** y abrir la ejecución deseada.
   Si hace falta, activar «Sitio de escritorio» en Chrome.
3. Para compilar manualmente, usar **Run workflow**, seleccionar la rama y
   confirmar **Run workflow**. El workflow debe existir primero en la rama
   predeterminada para que GitHub habilite normalmente el botón manual.
4. Abrir **Artifacts**, descargar `spotlight-next-1.1.0-debug-<número>` y
   extraer el ZIP desde la aplicación de archivos.
5. Abrir `app-debug.apk` y permitir la instalación desde esa aplicación
   cuando Android lo solicite.

Estos APK usan la firma Debug estándar del runner. No pueden actualizar una
instalación con la firma privada histórica, y la firma puede cambiar entre
ejecuciones. Para proteger la instalación estable y sus datos, probar en otro
dispositivo o perfil. Desinstalar la app existente puede borrar sus datos.
No se incluyen ni se solicitan credenciales privadas de firma.

## Auditoría de esta tarea

- El checkout inicial estaba limpio. `git fsck --no-reflogs` no informó errores.
- Los siete XML del proyecto se pudieron analizar sin errores de sintaxis.
- El wrapper tiene checksum SHA-256 configurado para la distribución Gradle.
- No se encontraron claves privadas, almacenes de firma ni tokens con los
  patrones revisados en los 169 archivos rastreados iniciales. Esto es una
  revisión acotada, no una garantía de ausencia de secretos de cualquier tipo.
  `backend/ENV.example` contiene placeholders, no credenciales reales.
- La firma privada del módulo es opcional y no está presente en el checkout.
  Las pruebas y la compilación Debug usan la configuración estándar.
- El README dice 1.0 y enlaza documentos y un bundle de historial que no están
  rastreados. La versión efectiva del módulo es 1.1.0. No se inventaron ni
  restauraron esos documentos durante esta tarea.
- No se modificaron arquitectura, interfaz, búsqueda universal ni IA.

## Resultado de verificación

Verificado en Codex con Temurin JDK 17.0.20.1 y SDK 35:

- `:app:assembleDebug`: APK generado correctamente (aproximadamente 170 MiB).
- `apksigner verify`: firma del APK válida. `aapt dump badging` confirmó
  aplicación `dev.bautista.spotlight.next`, versión 1.1.0/código 23 y SDK 26–35.
- `:app:testDebugUnitTest`: 209 pruebas, 50 suites, cero fallos, errores u omitidas.
- `:app:lintDebug`: cero errores y diez advertencias existentes. Tres avisos
  `TrustAllX509TrustManager` corresponden a clases de la dependencia Bouncy
  Castle 1.72; esto por sí solo no demuestra que la app use esas clases para
  conexiones inseguras. No se modificaron ni se ocultaron las advertencias.
- Backend: cinco pruebas aprobadas.
- El instalador reutilizable recompiló usando las tareas en caché.

Se corrigieron exclusivamente condiciones locales del entorno: runtime Java
sin compilador, cachés en el directorio personal de solo lectura y conexión
Java a través del proxy con su certificado de confianza. Las herramientas y
cachés se mantienen fuera del checkout. No se desactivó la verificación TLS
ni de checksums y no hicieron falta correcciones al código de la aplicación.

La ejecución en GitHub Actions y la instalación real en Android aún no se
verificaron. No se ejecutó un emulador ni una compilación Release.
El acceso Git permitió publicar la rama, pero la API de GitHub respondió
`Forbidden`; la creación automática del PR quedó bloqueada. Abrir el comparador
`main...ci/build-debug-apk` en GitHub para crear el PR sin integrar los cambios.
