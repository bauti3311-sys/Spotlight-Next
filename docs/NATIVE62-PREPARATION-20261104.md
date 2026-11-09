# Preview62 — candidata para validación física, ejecución pendiente

Actualización posterior: [Preview62 entregada y verificada](NATIVE62-DELIVERY-20261104.md).
La espera de autorización que sigue describe la etapa histórica de preparación.

Base funcional exacta:76a1c185b644c67a61cd9defd909c9075632e856 de PR21.
C1 completo, corrección de ventana y primer recorrido C2 conservados. No más
funciones ni ampliación C2/C3–C9. Sólo numeración Raycast62 y esta documentación.
El SHA fuente exacto será el commit que contiene esos dos archivos; el tooling
posterior fija ese SHA en preview_delivery.json, separado del SHA workflow.

## Identidad y actualización

- Paquete dev.bautista.spotlight.next.raycastpreview, sin cambios.
- versionCode62>61; nombre optimizado previsto2.0.1-raycast-preview-native57-v62.
- Certificado histórico requerido SHA256:
  885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba.
- R8/resource shrinking activos, ARM64, min26/target35, no debuggable.
- release.properties, variante principal, app/src, permisos, dependencias y datos
  sin cambios frente76a1c18; no hay migración ni limpieza de almacenamiento.

La APK61 distribuida se verificó con ese mismo paquete/certificado y code61.
La candidata está preparada como actualización conservadora, sin desinstalar ni
borrar datos. No se afirma compatibilidad binaria/firma62 verificada antes de
construirla, ni instalación física ya realizada: se comprobarán ambos al entregar
y al instalar manualmente. Ante conflicto de firma/paquete, cancelar.

## Un intento, con autorización concreta pendiente

Reutilizar mode=native-delivery de build-apk.yml; no editar native60-once ni sus
guardas. Manifiesto revisado con fuente candidata exacta, base76a1c18, código62,
previo61 e identidad histórica. Repositorio/actor/ref/SHA workflow/evento iguales,
reserva propia, historial sin intento anterior de esa fuente y run_attempt1.
No retry, Re-run, fallback experimental ni otra fuente automática.

El propietario autorizó preparar; solicitó presentar el par exacto fuente/workflow
antes de ejecutar. No compilar, firmar ni hacer dispatch hasta recibir esa
autorización específica para62. Adquirir una reserva propia nueva al ejecutar,
checkpoint=SHA workflow autorizado; no reutilizar el lease de preparación liberado.

Secuencia ya usada en61: preflight aislado del firmador; un build unsigned
optimizado sin secretos; firma en otro runner con tooling revisado, sin Gradle
ni ejecución del ZIP fuente. Limpieza de claves antes de verificación/upload.
Validar v2/un firmante885838aa, paquete/code62/nombre/noDebuggable/SDK/ABI,
zipalign/ELF16KiB, payload firmado idéntico al unsigned, fuenteZIP exacta y hashes.
Descargar y comprobar independientemente con herramientas Android oficiales y
cmp contra git archive del mismo SHA fuente; registrar source/workflow/run.

## Evidencia reutilizada y presupuesto

Se reutilizan119 casos distintos con última evidencia correcta y Lint0errores/
26advertencias/1información de76a1c18; no repetirlos por numeración de versión.
Sólo verificar estáticamente el delta y los contratos Python del manifiesto/
verificador de entrega. No hay build Android, firma ni Actions de preparación.

Política vigente: GitHub Free, presupuestoUS$0 y bloqueo al límite confirmados
por el propietario; no pedir capturas recurrentes, no modificar facturación ni
secretos. Consultar uso automáticamente si hay acceso;403 no invalida esa evidencia.
Ante bloqueo real de cuota/presupuesto, detenerse sin eludirlo. Ubuntu estándar,
un intento, timeouts5/30/10min, caché sólo lectura, sin emuladores ni suites extra.

## Entrega y prueba física

Tras éxito verificado, entregar enlaces individuales APK y ZIP desde ChatGPT,
con archivos realmente descargados y hashes comprobados. Respaldo: artefacto
privado GitHub del mismo run, junto con SHA256SUMS/procedencia/informe; indicar
expiración de retención3días. No anunciar enlaces62 antes de existir ni ofrecer
una URL firmada temporal como único respaldo. La descarga oficial por conector
ya funcionó para61 cuando el blob rechazó la CLI con403; mantener esa vía.

Antes de actualizar, registrar61/ajustes/permisos y escenas de referencia en la
Tab A9+. Instalar62 manualmente sin desinstalar. Revisar: abrir desde Apariencia
y desde otra app; Gboard cerrado→abierto→cerrado repetido; reapertura; vertical/
horizontal; teclado físico; búsqueda/PDF largo y scroll sin abrir; inspector y
footer alcanzables; texto ampliado y conservación de ajustes/datos. Repetir lo
pertinente en moto g86. Capturar esperado/observado y un video corto por fallo.
La aceptación física del arreglo tiene prioridad sobre ampliar C2.
