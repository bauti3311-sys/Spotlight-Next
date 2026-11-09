# Preview63 — entregada para validación física

Único intento autorizado [41947253299](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/41947253299),
correcto, run_attempt1, mode=native-delivery, actor/triggering_actor bauti3311-sys.
Fuente APK/ZIP ef4e60fcd94c33279581dc247461f227274e66b1; workflow revisado y
ejecutado b4bd17ecaae4eea951299674b41986d5bae4fdb1. Este informe es posterior:
su commit no es el código compilado. Par inicial71d6980/a82af60 no utilizado.

## Identidad y contenido

- dev.bautista.spotlight.next.raycastpreview, versionCode63>62.
- versionName2.0.1-raycast-preview-native57-v63; R8/shrinking, no debuggable.
- Firma v2 válida, un firmante; certificado SHA256 histórico exacto
  885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba.
- ARM64, min26/target35; zipalign/ELF16KiB verificados.

Incluye C1, todos los avances técnicos C2 y las correcciones505507a/ef4e60f:
altura con header/footer medidos, posición sobre área libre del IME recuperable
y botón táctil «Volver a resultados» desde opciones, conservando consulta/selección.
No features adicionales. Marcadores compilados de regreso y formularios C2 presentes;
eso no se presenta como prueba de efectos externos ni aceptación de UX física.

| Archivo | Bytes | SHA256 |
|---|---:|---|
| Spotlight-Next-Preview63-ef4e60f.apk | 73446821 | b63203c4687a0a089d1481f74dc9919ff9c1d0888a21b2aa4f33c5a4d7a259f9 |
| Spotlight-Next-source-ef4e60f.zip | 14522962 | 3204cd944cb8ac7630d2e012fa47686e5bbe1fb12be1091c7fc1f403d42aeb5b |

Copias individuales verificadas en shared/downloads/spotlight-preview63 para la
entrega directa en ChatGPT, junto a SHA256SUMS. No existen enlaces inventados.
[Respaldo privado GitHub](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/41947253299/artifacts/13378899009):
APK, ZIP, hashes, procedencia, SOURCE_COMMIT/WORKFLOW_COMMIT, package y signature.
Retención3días; caduca2026-11-08T01:26:07Z. Descargar/conservar copias antes.
ZIP conjunto88051350bytes, SHA256
3aac85bac55bd0c59050344779a82171ee52be5ad2c910e66b2a0e3e993e83ec.
No se usa una URL temporal de blob como único respaldo.

## Verificación independiente

Descarga por conector oficial, hash GitHub cotejado antes de extraer, CRC/rutas
seguros. Verificador local con SHA workflow/run exactos y herramientas oficiales:
firma, identidad, payload firmado idéntico al mapa unsigned, ABI/SDK/noDebuggable,
zipalign y todos los ELF. ZIP fuente idéntico byte a byte a git archive del SHA
aprobado (preparado independientemente durante el build), sin claves/cachés privadas.

Paquete/certificado/SDK/ABI coinciden con el informe62 previamente verificado;
63>62 acredita compatibilidad estática prevista. Se reutiliza esa evidencia, sin
descargar otra vez la APK62. La conservación efectiva de datos/ajustes, instalación
y comportamiento OneUI sólo se comprueban en el dispositivo. La fuente no añade
uses-permission respecto62; C2 heredó queries y filtro receptor amplio, no afirmar
que el manifiesto completo o la versión de caché sean idénticos a62.

[Resultado verificable](verification/native63-delivery-20261105.json) y
[evidencia pública de firma/paquete/build](verification/native63-delivery-20261105/).

## Ejecución y autonomía

01:15:24–01:26:12UTC del05/11/2026. Preflight9s, unsigned9m56s, firma34s;
Gradle BUILD SUCCESSFUL in9m21s. Únicamente3 jobs de entrega correctos; los jobs
históricos/otras variantes quedaron skipped. Una sola compilación optimizada,
sin re-run, fallback experimental o cambio de fuente. Guardas activas en cada job.
Preflight/firma aislados, build sin secretos y limpieza correcta.

Reserva propia codex-f557bdd9-d767-4ca9-b71c-a843cc2339a5, lease durante el run
a55cd03850a464fe4a179db4f87c53e388a2ac4f, checkpoint b4bd17e. Se conserva hasta
publicar cierre y se libera sólo la propia con notas atómicas. PresupuestoUS$0 y
bloqueo mantenidos; sin cambios de facturación ni nuevas capturas manuales. GitHub
permitió este intento; no se afirma saldo contable que no se consultó.

La CLI no pudo descargar el blob de logs (403 de transporte); el conector oficial
sí lo obtuvo. Se guarda extracto de tareas/BUILD SUCCESSFUL, no se repite el run.
No se modificaron código aprobado, main, permisos, firma, dependencias o secretos,
ni se inició C3–C9.28 casos dirigidos/Lint0E27W1Info reutilizados sin repetirlos;
el checkpoint integral permanece sin promover.

## Instalación y siguiente prueba

Instalar63 sobre Raycast Preview62, **sin desinstalar ni borrar datos**. Cancelar
ante conflicto de firma/paquete y registrar el mensaje, sin intentar otra firma.

En Galaxy Tab A9+ priorizar:

1. Abrir desde Ajustes/Apariencia; Gboard abierto→cerrado→abierto y reapertura.
   Inspector del mismo archivo, altura útil, footer y última acción alcanzables.
2. Tres puntos→«Volver a resultados» táctil, Esc y Volver/gesto; consulta y selección
   conservadas, sin cerrar/reabrir Spotlight ni ejecutar al regresar.
3. Rotación, scroll, teclado físico, nombres largos y fuente200%; después archivos
   simples/múltiples, destino compatible, cancelar/restaurar sin duplicar solicitudes.

Registrar versión63/Android/OneUI/Gboard, OK/fallo por paso y captura/video breve
sin datos privados. Complementar en moto g86. [Checklist completo](C2-PHYSICAL-ACCEPTANCE-20261105.md)
y [casos físicos reportados](TABLET62-REGRESSIONS-20261105.md). Preview62 fue
probada con defectos;63 todavía no está validada físicamente. C3 queda pendiente
de revisar ese resultado, sin automatizar su comienzo.
