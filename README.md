# Spotlight Next 1.3.0 · Hybrid Intelligence

Centro de comandos nativo Kotlin/Jetpack Compose para Android 8 o superior. Conserva las funcionalidades previas y los datos compatibles. Diseño Spotlight/macOS/iOS, organización de acciones inspirada en Raycast y release optimizado ARM64 para Samsung Galaxy Tab A9+ y teléfonos compatibles.

- [Novedades 1.3.0, consultas reconocidas y APK actualizable](docs/hybrid-intelligence-1.3.md)
- [Funciones, límites y recuperación](docs/v1.0/ENTREGA.md)
- [Verificación final](docs/VALIDACION.md)
- [Límites reales](docs/PENDIENTES.md)
- [Evidencia de la fase G](docs/v1.0/faseG/IMPLEMENTACION.md)
- [Backend opcional](backend/README.md)

## Compilar

Abrir SpotlightNext en Android Studio con JDK 17 y SDK 35. Ejecutar `./gradlew testDebugUnitTest assembleRelease lintDebug lintRelease`. Variante universal de prueba: `./gradlew assembleDebug`. Backend: `python3 -m unittest discover -s backend -p 'test*.py'`.

El proyecto fuente no contiene API keys ni claves privadas de firma, SDK o cachés. El APK distribuido usa la firma histórica para permitir actualización privada; recompilar sin esa clave no produce un APK compatible para actualizar la instalación existente. Las credenciales opcionales se guardan localmente mediante Android Keystore.

## Recuperar el historial

El ZIP incluye `docs/Spotlight-1.0-history.bundle`, un historial Git completo y limpio. Recuperación: `git clone docs/Spotlight-1.0-history.bundle Spotlight-history`. El código y la evidencia de esta entrega son la referencia actual. Las guías de versiones anteriores permanecen identificadas como historial.
