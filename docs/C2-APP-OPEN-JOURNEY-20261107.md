# C2/F01 — recorrido local de apertura de aplicaciones

Base verificada: `bfa07e7d9718467c158bf719dc1adfef9a0f9ebe`, PR #21 abierto y borrador.
Acción evaluada: abrir el launcher público de una aplicación instalada ficticia,
Atlas Reader. No se amplían integraciones ni se inventan capacidades.

## Auditoría y defecto reproducido

El resultado de aplicación ya usa `Action.Launch`. `AppCapabilities` descubre
búsqueda/compartir públicos; no hace falta agregar otro tipo OPEN ni otro ejecutor.
`IntegrationRegistry.launchAccess` verifica componente, disponibilidad, exportación
y permisos. `NativeLaunchSession` valida origen, vuelve a comprobar destino,
fija el componente exacto y consume una sola solicitud. La selección muestra el
inspector; la apertura principal permanece en footer/ActionPanel, sin duplicarla.

Con el inspector fijado, la prueba Compose reproducía un defecto de información:
se registraba una solicitud al launcher, pero no aparecía el recibo ya utilizado
por otros recorridos. Baseline corregida: **6 casos, 1 fallo y 5 correctos**.
Único cambio productivo: una línea en SpotlightPanel reutiliza
`NativeLaunchRequestStatus` en el inspector de aplicación Focus.

Antes: solicitud registrada y ninguna confirmación visible en el inspector fijado.
Después: «Solicitud enviada a Android · Atlas Reader · fixture.atlas.reader» y
«Resultado externo sin comprobar». No se declara ejecución externa completada.
El recibo conserva el contrato efímero de sesión existente; no agrega persistencia.
La apertura sin inspector fijado sigue cerrando el overlay como antes.

## Validación focalizada

**23 casos distintos correctos**, sin errores ni omitidos: seis nuevos recorridos
Compose/Robolectric y 17 regresiones seleccionadas existentes. XML y logs originales
en [evidencia](verification/c2-app-open-20261107/README.md).

- Launcher público disponible y componente exacto; ninguna capacidad inventada.
- Paquete eliminado, componentes ausentes/desactivados/privados/restringidos:
  rechazo antes del despacho. Permiso cambiado durante el envío: error saneado.
- Toque, Enter y doble activación: una solicitud; Enter en «Volver sin abrir»
  cancela, nunca confirma el segundo envío. Sin reintento ni fallback automático.
- Escape/cancelación conservan consulta, selección e inspector.
- Stop/resume con composición retenida conserva estado y no reproduce solicitudes.
- Sesión descartada, origen cambiado o solicitud fallida no conservan consentimiento
  ni publican un recibo falso; restaurar una revisión no restaura autorización.

No se repiten las 28 pruebas de relevancia: fuentes y evidencia anterior intactas.
El único delta productivo no modifica ranking, ejecutores, contratos de selección,
Focus Studio/2:1/140 ms, C5, integraciones, preferencias ni entrega Preview 68.

Lint global `:app:lintDebug`: BUILD SUCCESSFUL; 0 errores, 26 advertencias y 1 información preexistentes. Análisis main, androidTest y unitTest completados;
el reporte no cambió (lintReportDebug UP-TO-DATE). Sin supresiones ni baseline.

Fuente del cambio: `b2b91df784a516e7b767fceba7c5c4e39bdae0ab`.

## Límites y siguiente comprobación real

Robolectric registra `startActivity` mediante ContextWrapper y PackageManager
ficticios; no inicia una app externa ni simula su resultado. El estado probado es
SOLICITADO, nunca VERIFICADO. No hay instrumentación ni hardware Android en esta
ejecución. Retorno real desde otra aplicación, rotación, muerte del proceso,
One UI/Gboard y cambios reales de permisos quedan pendientes. C2 no está completo.
No se solicitan más capturas ni se prepara Preview; instalada 68 permanece intacta.
No APK, instalación, firma ni GitHub Actions.
