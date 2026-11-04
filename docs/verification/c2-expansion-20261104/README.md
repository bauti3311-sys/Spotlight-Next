# C2 — capacidades, consulta y contenido compartido

[Informe funcional y límites](../../C2-CONTINUATION-20261104.md).
Base f49281a/PR21, posterior a la entrega Preview62. Estas funciones necesitan
una próxima APK autorizada; no se cambió la APK62 ni se ejecutaron Actions.

## Comparativas de componentes reales

Compose/Robolectric API33 NATIVE; misma escena por par, ancho360dp,
qualifiers w390dp-h844dp, escala/densidad de fixture predeterminadas (1),
texto y selección iguales. Componentes de producción compilados para pruebas
unitarias RaycastPreview; no dispositivo ni APK instalada. Sin retoque de PNG.
El formulario se captura con su altura natural dentro del mismo ancho/viewport:
la altura del recorte cambia al añadir controles, no demuestra altura del overlay.

| Cambio acotado | Antes | Después |
|---|---|---|
| Compartir texto: incorporación explícita y fuente, oscuro | ![](before/message-dark.png) | ![](after/message-dark.png) |
| Mismo formulario, claro | ![](before/message-light.png) | ![](after/message-light.png) |
| Gmail conserva destino; sin cliente de correo implícito | ![](before/gmail-capability.png) | ![](after/gmail-capability.png) |
| Solicitud Android diferenciada de efecto externo, Obsidian Glass | ![](receipt-red/request-status.png) | ![](after/request-status.png) |

Las dos primeras bases se capturaron sobre producción f49281a; la base Gmail
sobre8645d4c; la base del aviso sobrec071455. Los fixtures nuevos no alteraban
producción. Los PNG finales corresponden a los archivos de producción registrados
en production-hashes.json. Las pruebas/capturas no promueven aceptación física.
Wikipedia, Gmail y Receptor son actividades sintéticas de PackageManager, sin
cuentas conectadas ni transmisión real a esos proveedores.

## Confirmación alcanzable con poco espacio

![](after/shared-confirmation-short.png)

Viewport320dp simula presupuesto corto, no Gboard real. La prueba desplaza hasta
Destino/Preparar en la app, mantiene footer dentro del contenedor, confirma una
vez y revalida el componente exacto. También alcanza el aviso posterior por
scroll, recupera700dp y comprueba que repetir no duplica la solicitud.
La escena usa el tema de sistema claro del fixture; no es una comparación del
hardware de Samsung con el teléfono. La física IME/OneUI sigue pendiente.

## Evidencia y fallos conservados

- web-red/web-green: descubrimiento WEB_SEARCH y contratos del ejecutor.
- query-red/query-green: consulta explícita, exactitud, homónimos y3 sugerencias.
- before/share-red: ausencia original de contexto/confirmación/destino Gmail.
- share-first:21/22 correctos y reproducción de pérdida de foco al cancelar.
- focus-first: primer parche en back no alcanzaba dismissInspector; se retiró.
- focus-green:4 correctos, foco y restauración sin autorización recuperada.
- receipt-red: solicitud aceptada sin aviso visible antes de la mejora.
- gate-first: revisión dirigida final; se conservan todos los XML, incluso fallos
  del fixture que comparaba frases parciales como texto completo.
- final: repetir únicamente los casos afectados del fixture y ejecutar Lint.
  coverage.json identifica la última evidencia por caso, sin sumar repeticiones.

Los errores de compilación de fixtures (opt-in/import de StateRestorationTester)
y el primer No tests found también se conservan. Ninguno se cuenta como una
reproducción funcional o un caso aprobado. No se deshabilitan pruebas ni se
relajan permisos/guardas, aserciones de efectos, timeouts o contratos existentes.

Comando de verificación: :app:testRaycastPreviewUnitTest con
scripts/directed-tests.init.gradle y spotlightDirectedClasses/filtros de métodos;
:app:lintRaycastPreview. JDK17.0.21/SDK35/Gradle8.10.2, offline, un worker,
Gradle2GiB/SerialGC/Kotlin in-process, test1.5GiB. Sin assemble, R8, APK, firma,
Actions, red por pulsación o proveedor externo. No suite global ni medición p95,
jank, RAM o teclado físico. Capturas/semántica no demuestran efectos externos.
