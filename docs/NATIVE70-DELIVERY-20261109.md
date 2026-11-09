# Preview70 Puente ligero — preparación y único intento autorizado

Autorización vigente del propietario en esta sesión: «dale manda el apk», tras elegir e integrar H02 Puente ligero. Se autoriza la entrega solicitada con su numeración siguiente y una sola ejecución nativa; no se reutiliza la autorización69. Se fija el par antes del despacho y se mantiene el protocolo/firmador/guardas existentes, sin pedir de nuevo permiso para la acción ya solicitada.

## Fuente e identidad congeladas

- Fuente:143104ce8d83f6ad6b5b4acd80897db579816fcd.
- Rama fuente:feat/preview70-puente-isolated-20261109.
- Base exacta:Preview69 cbc956ab56756d9ebdf7de05300fb0b5f5b14a19.
- Implementación Puente de referencia:0b1441e85cc736026f5bee81e1c23eef869e70c1.
- VersionCode70; versionName2.0.1-raycast-preview-native57-v70.
- Paquete dev.bautista.spotlight.next.raycastpreview; certificado histórico885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba.
- ARM64/min26/target35/noDebuggable/R8/shrink, configuración AGP comprobada sin APK.
- Workflow/manifiesto: el SHA del commit que publica este documento y preview_delivery.json; se registra completo en la nota de preparación y en el despacho. Ejecutar desde ramaPR21 feat/preview57-native-return-20261103, no desde ramaaislada.
- PresupuestoUS$0, stop_on_limit=true. APIbilling403: no demuestra pérdida de cuota ni permite afirmar saldo. Se conserva el límite propietario vigente y el bloqueo de GitHub; no se modifica facturación.

## Aislamiento

Sólo9rutas respecto69: seisUI de Puente byteidénticas a0b1441e, dos pruebas de integración/layout y app/build.gradle.kts (numeración69→70, incluyendo sufijo experimental sólo como identidad, sin compilar esa variante). Ningún otro archivo heredado cambia.

C5 no existe en la fuenteaislada: DocumentCitations/DocumentCitationState ausentes y FileIndex idéntico69. No se fusiona ni revierte C5 enPR21. Ranking/C2, preferencias/datos, manifest/permisos, firmas/dependencias, OverlayService, executores, NativeLaunchSession, AjustesA y apertura140ms conservados. Puente usa una única ventana y Focus sigue reversible/defaultfalse.

Se conservan los commits locales de preparación, incluso un checkpoint previo a completar la numeración; sólo143104c es fuente de entrega. No se recrean fuentes anteriores.

## Evidencia reutilizada y comprobaciones nuevas

-20casos y lint0E/23W/1I de la implementación0b1441e, archivosUI idénticos; evidencia nativa publicada en docs/PUENTE-NATIVE-20261108.md.
-5casos en la fuente aislada nueva: PuenteFrame3, captura/integración nativa1, FocusBphoneLight1. Todoscorrectos; compila producción y pruebas sin C5. NoAPK/testAPK/sign/install.
-17tests offline de guardas/artefactos y3 de comparación/isolation; fixtures identificados, sin dispatch real.
-AGP inspectPreview70Identity correcto, sólo configuración; sin firma ni APK.
-Historial consultado:0intentos para la fuente70 antes de preparar. Se vuelve a comprobar source/workflow exactos e historial completo antes del único despacho.

Se añaden sólo el manifiesto y documentación de preparación; workflow/guardas/firmador intactos. Cierre de reserva de preparación con notas atómicas y adquisición de una reserva NUEVA checkpoint=workflow antes de ejecutar.

## Ejecución y verificaciones pendientes

Una sola llamada build-apk.yml mode=native-delivery, confirm_preview=AUTHORIZE_PREVIEW_70_ONCE. Sin reintentos, reruns, builds alternativos ni fallback. Preflight histórico en runner aislado, unsigned optimizado sin secretos, firma separada y verificadores existentes. Si falla un paso obligatorio se detiene y se conserva evidencia.

Descargar APK/ZIP; comprobar independientemente identidad70, un firmante/v2/certificado885838aa, hashes, zipalign/ELF16KiB, payload/procedencia y ZIPigualgitarchive143104c. Comparar certificado con APK69 si está disponible. No reconstruir independientemente ni instalar; compatibilidad estática no prueba actualización física o conservación real en OneUI.

Al publicar este manifiesto todavía no hay APK70. Resultado/run/artefactos y limitaciones se completan en un cierre posterior. PR21 permanece en borrador/main intacta. No declarar C2/C5/VisualFirst físicamente completos. No pedir más capturas manuales ahora.
