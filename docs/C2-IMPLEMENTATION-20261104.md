# Recuperación Preview61 + primer recorrido C2

Estado: implementado y comprobado localmente; aceptación física pendiente.
Rama feat/preview57-native-return-20261103, PR21. C1 se conserva; no se repite su
implementación ni se promueven los checkpoints físicos/integrales históricos.

## Checkpoints independientes

1. be14f7e: corrección de altura e insets/visibilidad,39 casos distintos correctos,
   reproducción roja y capturas antes/después. Sin cambios arbitrarios de diseño.
2. 5f385c2: búsqueda pública por app y prioridad exacta,65 casos distintos con
   última evidencia correcta; capacidades reales, preparación, solicitud y retorno.
3. Contenido compartido/footer/scroll compacto y filtro de módulos: publicado con
   este informe. El SHA exacto de cierre está en HEAD/notas de autonomía y PR21.

### Ventana: causa y corrección

El cálculo confundía un marco visible pequeño con altura del IME, incluso con
IME oculto. Reproducción: se esperaban0px de oclusión y calculaba488px. Se conserva
el XML rojo y se corrige la prioridad de métricas/insets y visibilidad: el marco
del propio contenido no reduce el viewport. Ocultar IME descarta importes antiguos;
refrescos sin evento conservan el último inset. Recuperar foco solicita insets.

[39 regresiones y comparativas de ventana](verification/window61-20261104/README.md):
apertura, foco transitorio, cierre exterior, IME, reapertura/presupuesto,
orientación, scroll sin abrir y nombres largos. El gesto real llega al final del
inspector; no se desactivan pruebas ni se amplían plazos para ocultar fallos.
Es una causa reproducible compatible con la captura aportada, no un log tomado
de OneUI ni una afirmación de qué eventos emitió Gboard en la tablet.

### C2 funcional y presentación existente

| Objetivo | Avance comprobado |
|---|---|
| F01 | App instalada con búsqueda pública accesible: consulta→inspector/formulario→revisión→solicitud al componente exacto. Ausencia/revocación y cancelación sin alternativa automática. |
| F07 | «App + términos» prepara búsqueda local por nombre inequívoco y conserva acentos; homónimos requieren elección. App exacta primero en ranking y lista compacta. La ruta respeta Apps y ExtensionStore, sin saltar módulos desactivados. |
| F14 | ACTION_SEND y Process Text existentes alimentan ContextStore. Botón explícito copia texto al formulario, editable antes de Buscar; no hay copia/envío automático. Fuente visible y límite4000 informado. Documentos no se extraen automáticamente. |
| V02 | Inspector de C1 reutilizado, capacidad viva y botón deshabilitado con motivo cuando falta búsqueda. Una página principal de Sofascore no demuestra búsqueda. |
| V05 | Footer distingue «Preparar búsqueda»/«Preparar mensaje» de ejecución; hints de teclado conservados y pie dentro del viewport. No cambia Enter/Tab/Escape. |
| V06 | Área ajustada por IME real y recuperación de altura; cuerpos compactos de formularios y app quedan limitados y desplazables sin empujar el footer fuera. Orden y estado conservados; no nueva hoja, paleta o vista mayor. |

El recorrido UI usa actividades sintéticas con Android PackageManager y el
NativeLaunchSession real. Se inspeccionan ACTION_SEARCH, componente y QUERY y se
bloquea doble solicitud. También se verifica app retirada antes de abrir formulario
y Escape devolviendo consulta/foco. REQUESTED no se presenta como efecto externo
verificado; no hay app real/cuenta conectada en el executor.

El inspector compacto conserva resultados encima del contexto. En viewport320dp
puede requerir scroll para llegar al inspector; la prueba alcanza el metadato final
con dos swipes y mantiene el footer visible. No se ocultan resultados ni se
introduce un cambio de jerarquía sin aprobación visual.

## Evidencia y verificación final

- [Comparativas de C2 y XML](verification/c2-native-20261104/README.md).
- [Cobertura119 sin duplicar repeticiones](verification/c2-native-20261104/coverage-combined.json):
 39 de ventana y83 en rondas C2, con3 casos comunes; última evidencia correcta.
 No es una suite global ejecutada de una sola vez. Sin fallos/errores/omisiones en
 la última evidencia de esos casos. Se conservaron los XML de fallos anteriores.
- LintRaycastPreview:0 errores/26 advertencias/1 información. Las10 advertencias
 adicionales frente al C1 offline son GradleDependency (versiones disponibles,
 consultadas online). [Delta](verification/c2-native-20261104/lint-delta.json).
 Las16 advertencias anteriores siguen presentes; sin supresión ni actualización
 de dependencias para cambiar el resultado.
- Unitarias dirigidas y Lint locales: JDK17.0.21, SDK35, Gradle8.10.2, worker1,
 Gradle2GiB/SerialGC/Kotlin in-process y test1.5GiB. Se preparó el entorno vacío y
 su caché, preservando proxy/CA y versiones del proyecto; sin Actions/assemble/APK.
- Últimos gates:12 casos UI/C1 móvil + Lint correctos; después6 casos de búsqueda
 y módulos + Lint correctos al corregir el filtro. Se reutilizan las rondas
 anteriores válidas; no se repiten suites globales, firma, R8 o backend ajeno.
- [Hashes de producción comprobada](verification/c2-native-20261104/production-hashes.json).
 Gradle, manifest, permisos, identidad/versión, dependencias, workflows y firma
 no cambian frente a65a9572. Source/artefactos de APK61 siguen siendo11b647c.

## Límites y siguiente paso

No hay APK nueva, instalación, Actions, cuentas, mensajes reales, C3–C9, WebView,
main/merge o promoción del puntero integral. No se midieron p95, apertura, memoria
o jank físicos; no se afirma mejora cuantificada. La implementación instalada61
no incorpora estos commits hasta una futura entrega específicamente autorizada.

La aceptación C0/C1/C2 física sigue pendiente en Galaxy Tab A9+ y moto g86. En una
futura entrega autorizada: abrir desde Apariencia y desde otra app; teclado
cerrado/abierto/cerrado repetido, físico/táctil, vertical/horizontal, PDF largo,
última acción/scroll, fuente200%/TalkBack y medidas comparables. Comprobar primero
la recuperación de altura y foco; después el recorrido de app y texto compartido.
No afirmar entregado/reproducido ni ampliar integraciones por un Intent aceptado.

El siguiente paso de aceptación es revisar capturas y comprobarlo físicamente
cuando exista una entrega autorizada. C2 es incremental: no se declara cerrada
la matriz física ni una integración profunda universal con todas las apps.
