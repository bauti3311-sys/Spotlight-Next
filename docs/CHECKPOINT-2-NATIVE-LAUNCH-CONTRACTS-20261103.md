# Checkpoint 2 — contratos nativos de lanzamiento Android

Estado: **implementación autorizada y ejecutada**; evidencia y límites en
[el informe](verification/native-launch-20261103/README.md). El texto siguiente
conserva el plan de entrada y sus criterios. Preparado originalmente como
seguimiento al checkpoint de recuperación selectiva del PR19, en la rama
`feat/preview57-native-return-20261103` / PR21. Antes de empezar, consultar remoto,
HEAD, notas y reserva otra vez; partir del último checkpoint publicado de esta
rama. No reabrir la línea WebView ni modificar main o PRs anteriores.

## Problema concreto y límite

`SpotlightPanel.launch` intenta el destino de `CommandCenter.preferred` y, si falla,
el Intent original. La rama `Action.InApp` ejecuta `launch(action.fallback)` ante
cualquier fallo del intento elegido. Esto puede cambiar el destino después de que
el usuario revisó la acción. Actualmente varios fallos terminan en el mismo texto
genérico. `IntegrationRegistry.resolves` y `AppCapabilities.prepare` ya comprueban
capacidades: reutilizar sus reglas, sin crear un segundo registro.

Sólo se abordará el lanzamiento nativo de apps/enlaces/acciones públicas y su
presentación de fallo/alternativa en el contexto existente. No cambiar motores de
Spotify/WhatsApp, búsqueda/ranking, permisos, esquema persistente, formularios
OpenUI, RAGFlow, temas ni dependencias como efecto secundario.

## Contrato deseado

| Situación | Resultado exigido |
| --- | --- |
| Destino seleccionado habilitado/exportado y permitido | Solicitar exactamente ese destino una vez; mantener el payload revisado. “Solicitud enviada” no prueba el efecto externo |
| App desinstalada/deshabilitada o actividad inaccesible | Estado no disponible recuperable; sin abrir app o navegador alternativo |
| Permiso requerido o revocado entre preparar y ejecutar | Categoría de permiso/requisito con recuperación explícita; no otorgar ni pedir permisos por un fallback oculto |
| SecurityException / ActivityNotFoundException después de resolver | Clasificar sin filtrar excepciones privadas; conservar contexto y permitir revisar otra alternativa |
| Existe alternativa web u otra app | Mostrar destino/mecanismo y qué cambiaría; ejecutar sólo tras selección/confirmación explícita del usuario |
| Cambia destino, payload o contexto durante la revisión | Invalidar la aprobación y revisar de nuevo; no ejecutar con selección obsoleta |
| Cancelar/cerrar/rotar/restaurar una revisión | Ningún lanzamiento automático; restaurar datos no restaura consentimiento |
| Resultado incierto después de posible efecto | Explicarlo y no reintentar ni cambiar de destino automáticamente |

## Propuesta técnica acotada

1. Trazar los dos callers de `SpotlightPanel`, `Action.Launch/InApp`,
   `CommandCenter.preferred`, `ActionCatalog.search`, `IntegrationRegistry` y
   `AppCapabilities`. Inventariar usos antes de cambiar el contrato existente.
2. Reproducir primero el cambio automático con un destino fixture que falla y
   una alternativa resoluble. Conservar el caso rojo y la ausencia/presencia de
   Intents recibidos por Android de prueba.
3. Extraer sólo si permite probar/reutilizar la política un punto nativo de
   validación/dispatch con resultado tipado y causa recuperable. Preferir tipos
   existentes (`Action`, `CapabilityStatus`, `ExecutionState`) y una extensión
   pequeña, sin imponer un bus ni migrar todos los ToolCall.
4. Revalidar paquete/componente habilitado/exportado, permiso y grants vigentes
   inmediatamente antes del efecto; construir/copiar el Intent revisado de forma
   controlada. Un Intent resoluble no demuestra entrega, reproducción o lectura.
5. Convertir el fallback en propuesta de datos ligada al contexto revisado. La
   alternativa nunca se ejecuta desde un catch. Un control explícito de revisión
   y confirmación usa la superficie contextual existente y las reglas de riesgo.
6. Separar errores de entrada, indisponibilidad y permiso; dar recuperación
   legible. Mantener el overlay/inspector en errores/cancelación. Cerrar sólo
   después del resultado de solicitud correspondiente y según política existente.
7. Respetar cancelación y evitar doble confirmación/reintento de un efecto
   incierto. No usar la clave del resultado visible como autorización universal.

## Pruebas dirigidas propuestas

Los resultados ejecutados se registran en el informe enlazado. Plan original:
elegir las clases mínimas después de definir
la pequeña frontera real; no añadir mocks que sólo repitan la implementación.

- App instalada/ausente/deshabilitada, actividad privada, destino distinto y
  permiso concedido/denegado/revocado: Intent exacto o ningún Intent según caso.
- `InApp` falla: cero Intents alternativos hasta confirmar la propuesta; cancelar
  o cambiar de contexto no abre nada; confirmación válida abre una vez.
- `preferred` falla: mismo comportamiento explícito, sin delegación silenciosa.
- SecurityException tras validación: error de permiso, contexto conservado,
  ningún detalle sensible ni segundo intento.
- Respuesta/revisión obsoleta y confirmación repetida: no ejecutar otro destino
  ni duplicar el ya solicitado. Timeout/restauración no reintentan.
- Wiring Compose mínimo: recuperar/revisar/confirmar/cancelar con teclado y toque;
  Enter/Tab/Escape mantienen su contrato y se conservan selección/consulta.

Gate: pruebas nativas dirigidas afectadas + Lint pertinente, capturas comparables
si aparece una revisión/error nuevo; sin APK ni Actions. Fixtures sólo en tests,
sin enviar mensajes, abrir cuentas, instalar apps ni llamar proveedores reales.
El resultado no certifica One UI físico ni que una app externa complete su efecto.
