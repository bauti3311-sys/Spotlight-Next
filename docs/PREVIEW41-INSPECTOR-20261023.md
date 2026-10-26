# Raycast Preview 41 · entrega verificada ·23/10/2026

Código APK/ZIP: `d30c7adee82b51e94b66fce53b2a2301ab2e9a68`, rama `feat/raycast-sidebar-permissions-20261020`. PR18 sigue abierto y borrador. Reserva propia `work-master-recovery-20261022-cc97a1bd`.

## Cambios incluidos
Inspector nativo Obsidian/Atelier con acento según identidad real de la aplicación/destino, contraste y fallback local acotado desde el icono. Ficha de aplicación conectada al inspector existente; controles MediaSession del paquete autorizado. Icons8 iOS en inspector/reproductor con atribución. Ctrl+K conserva la geometría/consulta/selección. No nuevos permisos, proveedores o motores.

## Verificación real
27 casos únicos aprobados en rondas en el checkpoint anterior:12 no afectados retenidos y15 afectados repetidos; no se ejecutaron de nuevo para cambiar únicamente versionCode40→41. Detalle/fallos originales en `docs/verification/adaptive-inspector-20261023`.

436 entradas productivas/test/build cotejadas entre fuente Git e instalación local aislada. ZIP contiene1412 blobs cotejados por SHA de objeto Git, sin claves/credenciales/cachés. Gradle8.10.2/JDK17/SDK35. `:app:assembleRaycastPreview` correcto en2m19s con R8 y shrinkResources, no debuggable y ARM64. `:app:lintRaycastPreview` ejecutado nuevamente: {"Warning": 13, "Information": 1}.

Firma final externa con la clave histórica recuperada mediante respaldo privado autorizado. La variante deriva de debug pero el APK entregado está optimizado, no debuggable y firmado con el certificado histórico; no se confunde con la aplicación principal. Certificado SHA256 `885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba`. FirmaV2 y zipalign16KB correctos. Paquete `dev.bautista.spotlight.next.raycastpreview`, versionCode41, min26/target35; Activity launchable. Ocho librerías ARM64 idénticas a sus AARs originales.

## Artefactos
- `Spotlight-Next-Raycast-Preview-41-d30c7ad.apk`: SHA256 `f8cc541bd8f0217ac22e8bca656035a2efff7efa8647ebf5993f41c69c0b9736`, 73265717 bytes.
- `Spotlight-Next-Preview-41-source-d30c7ad.zip`: SHA256 `5e117ccc4d8764e688ab351ddc4bb561fb000981d670ec31bf46fc1dfe95467b`, 8937939 bytes.

APK y ZIP fuente persistidos para entrega directa. No Actions, merge, force-push ni cambios de firma principal/Review. No hay dispositivo ADB conectado: instalación física, OneUI/Moto, servicios Spotify/Gemini/WhatsApp reales y actualización con conservación de datos siguen pendientes. C5 continúa parcial. Las tres capturas de la implementación ya publicadas proceden de Compose/Robolectric33/NATIVE, con paquetes fixture, no hardware.

## Continuidad
Runtime y fuente de build aislados en `/workspace/spotlight-native-runtime-20261023` y `/workspace/spotlight-native-build-20261023`; no recrear SDK/cachés ni repetir tests sin motivo. Fuente canónica local conserva Git base4bb1b70 con cambios ya publicados: no descartarlos ni empujar esa base. Próximo bloque: validación física de esta APK y correcciones demostradas; no desarrollo paralelo automático anunciado.
