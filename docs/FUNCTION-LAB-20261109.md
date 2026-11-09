# Elegir funciones — siguiente etapa de Spotlight

Solicitud: «ahora hace lo mismo pero en cuanto a funciones». Alcance exclusivo de
exploración: catálogo interactivo para elegir, sin implementar capacidades nuevas.
Base remota verificada: `dccc9c5fc34a58432e7101b4934a3b15add2ad11`, PR21 OPEN/DRAFT.
Preview70 Puente entregada se conserva y su autorización está consumida.

## Entrega

- [Galería offline](verification/function-lab-20261109/Elegir-funciones.html):
  36 opciones, seis familias, filtros, ocho recomendaciones, ejemplos, privacidad,
  coste relativo, base existente/delta, selección ordenable y exportación JSON.
- [PDF de 11 páginas](verification/function-lab-20261109/Funciones-para-elegir.pdf),
  [seis ejemplos juntos](verification/function-lab-20261109/Seis-ejemplos.jpg),
  [ZIP completo](verification/function-lab-20261109/Spotlight-Funciones-Offline.zip).
- 19 capturas originales Chromium de laboratorio; HTML/CSS/JS y Geist embebidos.
  No capturas Compose ni de la app instalada. No se introduce WebView en Android.
- [Metadatos y evidencia de fuente](verification/function-lab-20261109/funciones.json),
  [211 comprobaciones web](verification/function-lab-20261109/checks.json),
  [reproducción y límites](verification/function-lab-20261109/README.md).

Se preservan byte por byte el catálogo anterior y sus F01–F36, títulos y grupos.
Estos F son IDs del catálogo, no el numerado distinto del plan maestro23. Se corrige
únicamente el vínculo de evidencia F11 hacia LocalStore, donde residen captura,
favoritos y caducidad; no se presenta esa base ya existente como nueva capacidad.
35 archivos base existen en fuente70; esto NO demuestra las 35 propuestas completas.
F17 DocumentCitations sólo existe en PR y sigue excluido de Preview70.

## Qué se puede probar realmente

11 demos editables locales: F01 aritmética/historial de sesión; F02 unidades;
F03 días/fechas; F04 horario de destino con tzdb del navegador y origen UTC−03
explícito; F05 cuenta/propina/redondeo; F06 tasa manual; F07 texto; F08 diff por
líneas; F09 plantillas; F12 limpiar parámetros URL; F21 huecos de agenda fixture.
No usan el motor Kotlin. Parser sin eval, límites de entrada/profundidad, diff
acotado a 150 líneas, fechas inválidas rechazadas. No se leen entradas privadas.

25 recorridos Android/data usan fixtures identificados. Revisar/simular/cancelar,
indisponibilidad, pérdida de acceso y cambio de destino ilustran el contrato futuro.
No se ejecuta ningún Intent, RemoteInput, OCR, SAF, modelo, envío ni control musical.
Un recibo simulado nunca se etiqueta como reproducción, envío o guardado observado.
F17 sólo muestra texto literal de TXT fixture y página no aplicable; no PDF auténtico.
F18 muestra SHA-256 real del texto fixture conocido; no son archivos del dispositivo.

C3/C4 siguen pausados y C5 físico bloqueado. Las demos F22/F29/F30 no reabren esas
etapas. La galería no altera resultados C5.3 (7/12 léxicos, cinco omisiones, semántica
no evaluada auténticamente), sus cinco escenarios físicos ni sus aceptaciones.

## Recomendación sin adopción

Primera elección sugerida: **F08 comparar textos, F12 limpiar enlaces, F34 colecciones,
F33 búsquedas por proyecto**. Utilidad cotidiana, ejecución local, poco acoplamiento
a cuentas/servicios. F08/F12 introducen herramientas acotadas; F33/F34 requieren
validar referencias, restauración y privacidad antes de desarrollo productivo.
Complementos: F01 historial, F02 favoritos de conversión, F07 texto y F36 diagnóstico.
Elegir no implementa automáticamente ni autoriza capacidades sensibles.

## Verificación y preservación

211 comprobaciones del HTML exacto en Chromium (`set_content`), cero errores JS y
cero solicitudes de red; interacciones de las 36, cálculo real, entradas inválidas,
comparación literal, Unicode, escape HTML, URL funcional conservada, homónimos,
doble activación, cancelación, pérdida de acceso fixture sin restaurar resultados,
cambio de destino, filtros, exportación real y prioridades. Viewports1440/390 y
330px sin desborde; comprobación de desborde con cuerpo a200%, no prueba de escalado
Android. Claro/oscuro; revisión visual de capturas y PDF. No navegación file://
verificada en navegador administrado, ni aceptación física A9+/G86.

Sin Gradle/lint: delta sólo documentación y laboratorio web. No repetir suites Android
históricas ni confundir estas211 comprobaciones con pruebas C2/C5/Compose. Sin APK,
firmas, Actions, permisos, dependencias de producto, instalaciones ni cambios main.
Código app, fuentes68/69/70, manifiestos, scripts de entrega y preferencias intactos.
PR21 debe permanecer borrador; cierre por notas atómicas y sólo reserva propia.
