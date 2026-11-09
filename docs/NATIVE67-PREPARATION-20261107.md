# Preview67 — Studio congelado, sólo preparación

Base26377fabbfc6402cf53d152f84e8a098fb60ff8e, PR21 abierto/borrador verificado.
Studio3662cf79f0982dea08e48689f67ec7932f1ca4a9 es ancestro conservado; todos los
commits posteriores permanecen. app/src/main idéntico al Studio aprobado.
Sin decisiones estéticas nuevas. Sin Actions, firma, preflight real o APK.

## Identidad y congelamiento

source_sha: `6918204238706d3c9bac826196ad3f1f272a51e7`.
El commit posterior de este manifiesto/informe es workflow_sha; SHA exacto en
notas de cierre y respuesta al propietario. YAML build-apk.yml sin modificaciones.
Manifiesto congela source=base con allowed_delta_paths=[], sin deltas adicionales.

- versionCode67, anterior66; versionName `2.0.1-raycast-preview-native57-v67`.
- Paquete `dev.bautista.spotlight.next.raycastpreview`.
- Certificado requerido `885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba`.
- Perfil optimizado R8/shrink, ARM64, noDebuggable, min26/target35.
- Configuración de firma, release.properties principal29/2.0.1, otras variantes,
  permisos, servicios, dependencias y esquema de datos sin cambios.

AGP inspeccionado con tarea de sólo configuración, sin assemble. El campo signing
del log describe configuración/antecedenteCI66; esa tarea NO comprueba un keystore
ni firma67. Certificado histórico comprobado en entregaCI66, intento1 run42219035267;
la descarga local de aquel artefacto quedó bloqueada y no se repite en esta tarea.
Firma, package/code/name y actualización efectiva de APK67 quedan pendientes del
único intento de entrega que el propietario autorice posteriormente.

La compatibilidad esperada con66 deriva del mismo paquete/certificado y code67>66.
No cambios de DataStore, preferencias, savers, migraciones o restauración; Focus
siguefalse por defecto y conserva la elección existente al actualizar. No se
promete conservación física de datos sin instalar/probar: nunca desinstalar66 ni
borrar datos para resolver un fallo de actualización.

## Validación económica

- 19pruebas offline de entrega/identidad correctas tras fijar el manifiesto67:
  actor/rama/evento, source/workflow exactos, reserva, un intento, rechazo de reruns,
  firmas/metadata incorrectas, archivos privados y aislamiento de build/firma.
  Fixtures sin claves ni policy.main contra un intento real.
- Inspección AGP correcta: code/name67, paquete, ABI/SDK, noDebuggable/R8.
- Verificación estática de freeze: sólo cambian dos líneas de numeración en
  app/build.gradle.kts. app/src/main idéntico a3662cf7 y26377fa.
- Reutilizadas20pruebas Studio válidas: galería6, restauraciónFocusB1, footer7,
  tokens/reversibilidad3, presupuestoStudio1, tecladoActionPanel2. XML/hashes en
  verification/native67-preparation-20261107/freeze-verification.json. Son evidencia
  previa válida, NO veinte nuevas pruebas. Lint previo0E26W1Info reutilizado.

Sin compilar app, repetir suites/galerías, lint, firmar o generar APK. No se
modifica composición Studio, Geist, D1, selección/ranking, footer, inspector,
AjustesA o140ms. D5aislado. C3–C8 y Spotify/Square/WhatsApp/documentos intactos.

## Única entrega futura recomendada

Autorización expresa NUEVA del par exacto y un intento native-delivery. Adquirir
reserva NUEVA con checkpoint=workflow autorizado; comprobar HEAD/PR, owner/lease,
manifiesto y ausencia de intento previo por fuente o workflow. Ejecutar una vez
build-apk.yml desde feat/preview57-native-return-20261103, mode=native-delivery,
source_sha/workflow_sha exactos, confirm_preview=AUTHORIZE_PREVIEW_67_ONCE y owner/lease.
No retry/re-run/fallback ni otra variante. Guardas y presupuestoUS$0 intactos.
Preflight histórico aislado→un build unsigned sin secretos→firma aislada→verificar
APK/ZIP y fuente exacta, v2/un firmante, certificado, code/name, noDebuggable,
ABI/SDK/16KiB, payload, SHA256 y git archive. Sólo Preview privada, no estable.
La reserva de preparación se libera ahora; NO reutilizarla para una entrega.

## Checklist físico corto — Galaxy Tab A9+ y Motorola G86

En ambos dispositivos, instalar sobre66 sin desinstalar/borrar datos. Registrar
modelo/Android/OneUI o interfaz/Gboard/tema/escala y video/captura si hay fallo.

1. **Datos y diferencia visible:** conservar preferencias/favoritos; comparar66→67:
   barra enmarcada, selección con acento, inspector definido y ActionPanel coherente.
2. **Recorrido diario:** inicio hasta3sugerencias pertinentes, búsqueda/resultados,
   inspector con búsqueda prioritaria/administración plegable; ActionPanel y AjustesA.
3. **Lectura:** oscuro/claro, texto100/200%, temas tablet en fila cuando hay ancho;
   marcos no deben ocultar texto, resultados, footer o acciones.
4. **Input:** abrir/cerrar Gboard, teclado físico Tab/ShiftTab/Enter/Escape y CtrlK;
   scroll no ejecuta; consulta/cursor/selección se recuperan al volver.
5. **Geometría:** retrato/paisaje, rotación con consulta y ventana flotante, con/sin
   teclado; no solapamiento, salto de foco o controles fuera de pantalla.
6. **Fluidez:** apertura140ms actual, cambios rápidos de vista y desplazamiento;
   registrar tirones/tiempos/RAM con hardware, no afirmar rendimiento por pruebasJVM.
7. **Reversibilidad:** apagarFocus, comprobar interfaz anterior, reactivarlo;
   conservar tema/preferencias/datos y volver al recorrido sin acciones accidentales.

Fallo crítico de datos/foco, control inaccesible o ejecución al hacer scroll bloquea
aceptación. No necesitan envíos reales ni arreglosSpotify para evaluar Studio.
Las nueveV no quedan cerradas hasta contar con esta evidencia física. D5no está en67:
no comparar120ms como si estuviera integrado. Tras preparación: detener ejecución.
