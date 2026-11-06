# Visual First — auditoría de las nueve V y decisiones pendientes

Base remota y PR21 verificados: f27c8673ab8aaa557adf4da20b7a28283c449e78.
La prioridad del propietario ahora es visual. C3/C4/C5/C6/C7/C8 quedan congelados;
WhatsApp/Spotify/Square/documentos, sus checkpoints y Preview65 se conservan.
No se modifica producción: sólo fixtures/capturas/propuestas y continuidad.

## Resultado ejecutivo

La base visual está avanzada. No hacen falta otra fuente, tema o sistema de iconos.
Las diferencias importantes se concentran en legibilidad por presupuesto de ventana,
controles periféricos y una política de movimiento uniforme. Focus B de aplicación
más revisión contextual queda protegido, sin rediseño ni adopción global de Focus.

Se usan estados fundamentados, no porcentajes: no existe una aceptación homogénea
que permita afirmar «83%» sin inventar pesos. Implementación, aprobación visual,
prueba de componente y aceptación física son estados diferentes.

## Auditoría V01–V10 — sólo las nueve seleccionadas

| V | Estado actual fundamentado | Evidencia de código y captura | Cierre que falta / recomendación |
|---|---|---|---|
| V01 Obsidian Glass2.0 | Avanzada; identidad conservada | FocusDesign y GlassTokens: superficies grafito/marfil, radio22dp, borde1dp y sombra; glassSurface no introduce blur. Temas claros/oscuros en toda la matriz. | Unificar controles periféricos sin nueva paleta. B prueba radios10dp; conservar color de selección A. Opacidad física/fondo entre ventanas pendiente. |
| V02 Inspector contextual | Base B aprobada y protegida; cobertura especializada parcial | ApplicationInspector con búsqueda prioritaria, administración plegable; FocusSearchTask con app/sitio separados. Ocho vistas del inspector vigente, sin B nuevo. | Mantener este diseño. Armonizar sólo soportes de documentos/multimedia de V08 y verificar dispositivos; no reabrir la jerarquía aprobada. |
| V03 Acentos por aplicación | Avanzada | InspectorAccent fija destino, extrae color de copia32px y ajusta contraste; InspectorTheme y barra contextual consumen el destino. Marca WhatsApp/Spotify en fixtures, no fondo completo. | Conservar adaptación actual; no neutralizar selección de fuente/tema indiscriminadamente. Falta contraste físico con iconos instalados/fondos reales. |
| V04 Jerarquía de resultados | Avanzada; conservar estructura | SpotlightResultList/ResultRow: títuloFocus15sp, secundario12.5sp, metadatos12sp, mínimo48dp; selección y foco diferentes. Búsqueda real del engine con paquetes fixture, sin cambiar ranking. | Mantener jerarquía. En330dp/texto200% se reduce la consulta visible y crece el scroll: no afirmar que eso sea fallo de truncamiento; el editor tiene scroll horizontal. Falta corpus largo/10resultados y TalkBack físico. |
| V05 Footer contextual | Implementado y protegido; aceptación estrecha parcial | GlassContextFooter distingue anchos>=700dp; acciones48dp y atajos según contexto. En formulario con foco se usa el hint existente, no otro botón de envío. Límites dentro del viewport comprobados. | Mantener Enter/Tab/Esc y el diseño vigente. Confirmar etiquetas largas/IME real; no rellenar el footer de controles repetidos. |
| V06 Ventana adaptable | Parcial en aceptación, base técnica existente | WindowSizePolicy/WindowFocusHost y límites de SpotlightPanel; regresión de recuperación a330dp+texto200%. Perfil IME reserva300dp y reduce el viewport. | OneUI/Gboard/rotación/ventana física pendientes. B limita lectura720/860dp sólo en fixture: en Ajustes tablet empeora la densidad, por eso se recomienda A allí. No cambiar WindowManager por estas capturas. |
| V07 Iconografía iOS coherente | Avanzada, normalización por rol pendiente | IosCommandIcon adapta vectores existentes a inspector_ios; CommandIcons/HugeIcons y licencias conservadas. Lámina A usa tamaños de contextos reales16/17/18/20; B ensaya20dp, siempre hit48dp. | Recomiendo conservar distinción por rol, unificar sólo comandos equivalentes tras decidir. No iconos nuevos ni sustitución de apps por símbolos. Monogramas PM de la galería son fixtures, no logos reales. |
| V08 Tarjetas contextuales | Parcial en consistencia transversal | MediaInspector real con portada64dp, pausa/progreso/capacidades; FilePreview real de TXT autorizado; MessageEditor real con destinatario/texto. Capturas equivalentes en8perfiles. | En texto200% hay más scroll, rótulos largos y ayuda técnica. Radios B son un acabado puntual, no resuelven toda la densidad. PDF/otras tarjetas y entrega externa física siguen pendientes; no inventar nuevas capacidades ni nuevas tarjetas. |
| V10 Microinteracciones | Parcial en aceptación/rendimiento | revealMotion actual160ms,6dp y escala. ResultRow usa selección55/90/125ms según movimiento. LocalMotion observa reducción del sistema. Fotogramas A/B reales con reloj Compose y estado reducido. | B ensaya opacidad120ms sin traslación, sólo test. Recomiendo probar esa dirección antes de adoptarla. No hay medición de FPS/jank/RAM en dispositivo; no declarar mejoras de rendimiento por capturas. |

Fuentes: [FocusDesign](../app/src/main/java/dev/bautista/spotlight/ui/FocusDesign.kt),
[GlassTokens](../app/src/main/java/dev/bautista/spotlight/ui/GlassTokens.kt),
[InspectorTheme](../app/src/main/java/dev/bautista/spotlight/ui/InspectorTheme.kt),
[SpotlightResults](../app/src/main/java/dev/bautista/spotlight/ui/SpotlightResults.kt),
[SpotlightWidgets](../app/src/main/java/dev/bautista/spotlight/ui/SpotlightWidgets.kt),
[GlassContextFooter](../app/src/main/java/dev/bautista/spotlight/ui/GlassContextFooter.kt),
[SettingsNavigation](../app/src/main/java/dev/bautista/spotlight/ui/SettingsNavigation.kt),
[Motion](../app/src/main/java/dev/bautista/spotlight/ui/Motion.kt).

## Hallazgos visibles y prioridad

1. **Densidad y legibilidad**: formulario330dp/font200 mantiene el campo enfocado
   a costa de scroll; ayuda y rótulos largos compiten por espacio. No reducir texto
   de17a16sp como solución general. Mantener contraste y controles accesibles.
2. **Ajustes tablet**: B limita ancho860dp y convierte los tres temas en2+1 filas;
   el control de opacidad deja de verse en la primera pantalla. A conserva3temas
   y opacidad. Recomiendo conservar A y evitar una regla de ancho global.
3. **Selección de Ajustes**: la neutralización de primaryContainer en B hace más
   discreta la opción Geist activa. No la recomiendo como regla general: el estado
   activo debe seguir siendo inequívoco. No quitar el acento para «limpiar» la UI.
4. **Controles periféricos**: en formulario, esquinas cuadradas de campos frente a
   radios de otros controles; B ofrece10dp, conservando datos/foco/tamaño táctil.
   Es la diferencia estática que recomiendo tomar, sin tocar Focus B.

La leyenda de Ajustes todavía dice Solid Graphite/Dark Glass mientras Focus describe
«grafito y marfil». Es una deuda de nombres/copy, no una nueva temática a diseñar.
Se conserva por ahora; una corrección de nombres coherente es pequeña después de la
aprobación de alcance, sin cambiar claves/preferencias.

## Decisiones visuales propuestas

| Decisión | A | B observado | Recomendación |
|---|---|---|---|
| D1 Campos periféricos | Radio actual | Radio10dp existente en la propuesta de test | Tomar B en controles equivalentes; mantener objetivos48dp. |
| D2 Ancho de lectura | Ancho vigente | Tope720dp,860dp en Ajustes | Conservar A en Ajustes tablet; evaluar cap sólo por tipo de contenido, no global. |
| D3 Títulos/selección | Título17sp y selección con acento | Título16sp y contenedor neutral | Conservar A para legibilidad y descubribilidad. |
| D4 Iconos de comando | Tamaños por rol | Todos20dp | Conservar A por rol; normalizar sólo acciones equivalentes, no iconos de apps/media. |
| D5 Entrada de superficie |160ms,6dp,escala|Opacidad120ms|Preferir B como siguiente ensayo; requiere decisión antes de adoptar movimiento perceptible. |

B está deliberadamente presentado con sus costes. No es una variante aprobada y
no se adopta un paquete global por preferir un detalle. La recomendación coherente
es: base A/Focus B + radios D1 + ensayo D5; sin reducir tipografía, contraste ni
ancho útil de Ajustes. Nada de esto se activó en producción.

## Galería y trazabilidad

136 PNG reales de Compose/Robolectric, sin retoque/generación de imágenes:
120 de escenas (72 pares, con24pares protegidos que reutilizan el A idéntico),
12fotogramas de movimiento y4láminas de iconos. No se vende una copia idéntica como
un cambio. Las vistas incluyen inicio, búsqueda/resultados, inspector, acciones,
ajustes, formulario, documentoTXT, multimedia y un error de laboratorio.

Ocho perfiles: tablet oscuro/claro1000x720; teléfono oscuro/claro390x760;
330x760 con texto200% oscuro/claro;330x760 con texto130% y300dp reservados al IME,
oscuro/claro. Son tamaños lógicos representativos para revisión A9+/teléfono,
**API33 y densidad1**, no capturas físicas Galaxy/moto ni Gboard real.

A en inicio/resultados/inspector llama SpotlightPanel productivo. Las otras vistas
montan componentes productivos en una escena de laboratorio con chrome compartido;
A NO se presenta como screenshot íntegro de la Activity/APK. B modifica únicamente
MaterialTheme/anchos en tests. Ajustes usa su grupo/contexto real y altura acotada;
formulario espera el número resuelto y el mismo foco en ambas variantes. Los
pilotos detectaron errores de montaje (altura/contexto de Ajustes y espera al
resolver número): se corrigió el fixture, no el producto, y se renderizó la matriz
final. Datos, consulta, tema, dimensiones, escala y capacidades son equivalentes.

Vivaldi/Spotify/WhatsApp del PM son fixtures instalados en Robolectric con monogramas
explícitos; documento local es TXT de prueba y sesión musical es sintética, pausada,
sin audio. El error también es fixture. Sin consultas a cuentas, contactos reales,
archivos del usuario, envíos o reproducción. Focus=true sólo en fixture; sigue
reversible y desactivado por defecto en producción.

[Manifest y hashes de imágenes](verification/visual-first-20261106/manifest.json).
El HTML autocontenido y PDF de revisión se generan con el script local del mismo
directorio, sin servicios ni dependencias nuevas. La matriz completa está enHTML;
el PDF selecciona12comparativas, iconos y movimiento para revisar sin recorrer72.

## Validación y límites

12 casos dirigidos correctos: Gallery8, Motion1, IconReview2 y regresión aprobada
FocusBIntegration.phoneDarkText2001. Las120 capturas de escenas comprobaron footer dentro
del presupuesto y cero solicitudes externas. Iconos mantienen hits48x48dp. Se
comprobó contraste de tokens centrales onSurface/onSurfaceVariant>=4.5 sobre
surface claro/oscuro; no es una auditoríaWCAG completa de cada estado/transparencia.
La regresión conserva CtrlK/Escape, foco, selección, recuperación y reversibilidad.
No se afirma prueba nueva de Enter/Tab ni Gboard físico. Sin cambios productivos,
no se repite lint/APK/CI: las propuestas sí compilan y ejecutan estos tests.

Primera compilación del fixture omitió el parámetro icon de InspectorTheme; corregido
sólo en test, conservando log. No se borraron/desactivaron pruebas para obtener verde.
El último lint productivo conservado es el deC3:0E26W1Info, no una ejecución nueva.

## Secuencia de implementación después de decidir

1. Aprobar o descartar D1–D5 por separado. Preservar A donde B empeora el resultado.
2. Primer checkpoint: radios/copy/medidas equivalentes en controles periféricos,
   sin tocar inspector B, navegación, ranking o permisos. Capturas antes/después
   de la escena exacta y prueba dirigida de foco/táctil.
3. Segundo: uniformidad de iconos por rol con assets existentes y prueba48dp.
4. Tercero: ensayo D5 con reducción de movimiento/interruptibilidad; medición física
   antes de prometer fluidez/consumo. No blur nuevo ni motores gráficos.
5. Cierre visual de nueve V por criterios observables, y validación física de ventana,
   Gboard, scroll y accesibilidad. APK sólo con autorización específica posterior.

C3–C8 no se reanudan durante esta fase. No se declara V09 dentro del alcance, no se
reinicia el proyecto ni se promueve el checkpoint integral por entregar una galería.

El HTML se comprobó con Playwright ya instalado:72tarjetas, filtro de perfil/escena
y ampliación correctos, cero recursos remotos de página. El navegador administrado
bloquea file://; se cargó el mismo HTML mediante set_content, sin servidor nuevo.
El PDF usa ReportLab existente y embebe los PNG originales sin recortes/retocar;
16páginas comprobadas, incluidas portada, Ajustes tablet y formulario200%.

Checkpoint visual de código/capturas publicado:
807becebb178ad163b290850428aa5d5fff6f265. El commit de auditoría posterior
sólo publica planificación/generador, sin delta de producción ni nuevas capturas.
