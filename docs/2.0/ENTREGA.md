# Entrega 2.0.0 / 27

APK Release arm64-v8a optimizado: Spotlight-Next-2.0.0-arm64-release.apk.

Identidad conservada: dev.bautista.spotlight.next. Mínimo API 26, objetivo API 35. Certificado histórico SHA-256: 11b0b3b37544bd95b128de52798fa7ffb7489f94819614b1ada383bd4ee7a6cf. El workflow existente firmó con sus Secrets; no se generó ninguna clave ni se publicaron credenciales.

Código entregado: commit 2946a645d2730e43ea577f6c34af7f979cd10f39, integrado en main mediante PR #4. Esta actualización documental no modifica el código del APK.

## Verificaciones completadas

- Workflow previo 38263129913: 271 pruebas Android, cero fallos/errores; Android Lint Debug; backend 5 y verificador de actualización 10.
- Workflow Release 38264201437: 271 pruebas Android, cero fallos/errores; Android Lint Release; backend 5 y verificador de actualización 10; assembleRelease con R8 y reducción de recursos.
- apksigner verificó la firma; zipalign comprobó la alineación. Verificador de actualización comprobó applicationId, código 27 superior a 26, minSdk 26 y ausencia de debuggable; también exigió versión exacta 2.0.0 / 27.
- Manifiesto y certificado v2/v3 comprobados sobre el APK descargado. Solo bibliotecas arm64-v8a. Marcadores reales de funciones nuevas encontrados en DEX: Modo privado, Tus deportes, Guardar comando, context-imports y entities_v2.
- Capturas nativas de tablet/teléfono, tres familias visuales, menú en ventana, planes, deportes sin favoritos, y pruebas de navegación de WhatsApp, selección/foco sin perder resultados y regreso conservando consulta.

APK SHA-256: 563b4f9f2ec6538ffd66ca11132bd73cdff6ac9680516112f76e69ef005c83b9.

## Instalación y límites

Instalar encima de 1.3.0/1.3.1 firmadas con el certificado histórico. No desinstalar: la actualización conserva datos, permisos y preferencias. La compatibilidad de paquete y firma se verificó automáticamente; no se ejecutó una instalación física en la Galaxy Tab A9+.

Revisar LIMITACIONES.md: Glass usa una superficie estática legible, sin desenfoque/refracción real; permisos y capacidades de apps son independientes; IA remota exige selección explícita/proveedor; deportes dependen de cobertura; el gesto One Hand Operation+ se configura en la app externa compatible. No se presenta la validación automática como benchmark de One UI.

## Recuperación

Proyecto completo preservado en GitHub privado bauti3311-sys/Spotlight-Next. ZIP fuente completo sin signing, keystores, credenciales, cachés ni outputs de compilación; contiene wrapper, recursos, licencias, código, pruebas y documentación. Los respaldos de implementación están en el historial remoto de feat/spotlight-glass-2.0 y main.
