# D27 · cierre funcional verificado · 23/10/2026

Estado: COMPLETA Y PROBADA para el contrato funcional de software, con60casos únicos. No certifica Android físico; D40 sigue PARCIAL. Lint Preview45:0errores/15avisos/1informativa; R8/recursos reducidos/assemble correctos en4m32s; APK45 verificada.

Base d3c221b9. Núcleo tipado/secuencial publicado en5fc672e4; inspector, recuperación
y brillo relativo en39430eb2b8247c59bb9b4f8a8341d00329ca3d70. La rama yPR18borrador
se conservan. No main, merge, force, dependencia o permiso nuevo.

## Contrato y aceptación

|Criterio del maestro|Comportamiento comprobado|Evidencia|
|---|---|---|
|Editar/añadir/quitar/omitir/reordenar|Máximo6pasos, argumentos registrados, identidad de fuentes remapeada. Eliminar una fuente deja referencia inválida; no la reasigna.|20regresiones previas; ToolPlanGraphProjectionTest|
|Dependencias tipadas|NUMBER/TEXT con esquemas de salida/entrada locales explícitos. Rechaza ausentes, omitidas, ciclos, referencias futuras, campos/tipos incorrectos y resultados no verificados.|StructuredToolResultsTest; ToolPlanGraphProjectionTest|
|Ejecutar en orden y confirmar riesgo|Consultas locales avanzan en orden; cada cambio/preparación sensible consume su propia confirmación. No hay confirmación global que autorice acciones posteriores.|ToolSequenceExecutionTest; AiPlanSequenceFlowTest|
|Estados verificables|Conserva PREPARED/REQUESTED/COMPLETED/VERIFIED. Una dependencia exige VERIFIED y un campo estructurado; nunca extrae instrucciones del texto descriptivo.|StructuredToolResultsTest; StructuredToolAdapterTest|
|Fallo/cancelación|Detiene pasos posteriores, conserva resultados previos y marcador de interrupción. Descarta respuestas tardías aunque el adaptador ignore cancelación.|ToolSequenceExecutionTest; AiPlanSequenceFlowTest|
|Persistencia sin replay|Borradores/referencias/resultados/confirmación pendiente/traza en el registro guardable existente. Flush durable antes de efectos y al guardar resultados/pausas. Snapshot corrupto bloquea ejecución.|DurablePlanSequenceTest; DurableTaskRegistryTest|
|Relativos|Volumen0–100%; brillo8–100% representado20–255. Recalcula al omitir/reordenar/editar, usa observaciones estructuradas posteriores cuando existen, sin acumular proyecciones.|ToolPlanRelativeTest; ToolPlanGraphProjectionTest; AiPlanRelativeFlowTest|
|Permisos y postcondición|Executor revalida capacidades al ejecutar. Brillo usa Shizuku/ReversibleSettings y comparación de lectura posterior; sin base válida/permiso no ejecuta.|ReversibleSettingsTest; ToolCapabilityTest|

## Alcance tipado explícito

Salidas NUMBER: calculate.value, set_volume.percent observado,
read_display_setting.value y write_display_setting.value verificado. Salida TEXT:
transform_text.text. Entradas compatibles: expresión numérica de calculate, percent
de set_volume, value de write_display_setting y campos text/query registrados de
transformación/notas/preparación de mensaje/búsqueda. No se enlazan paquetes,
selectores, comandos, canales o nombres de herramientas desde texto arbitrario.
Los valores numéricos siguen sujetos a límites y validación del destino.

El usuario elige referencias explícitas en el editor después de recibir una
propuesta; no se interpreta sintaxis escondida en el texto del proveedor. La UI
muestra el argumento efectivo antes de confirmar. Los argumentos del proveedor
siguen siendo los del registro existente; no se afirma que Gemini genere grafos
tipados automáticamente. Otros adaptadores sin esquema de salida no ofrecen un
enlace ficticio. Esto delimita capacidades D26/D34, no una ejecución pendiente oculta.

## Estados externos y límites reales

Preparar mensaje/llamada/abrir una app puede devolver PREPARED; controles multimedia
pueden devolver REQUESTED. La secuencia se detiene y conserva esos estados hasta una
revisión explícita; no los eleva a VERIFIED por un temporizador, una pulsación o una
restauración. No hay un verificador genérico de efectos de apps externas ni una
reanudación ciega tras ellos. Su ausencia no se disimula con éxito simulado.

No se repite automáticamente un efecto fallido/interrumpido. Una nueva tentativa
manual exige revisar el estado y una confirmación nueva. Las operaciones ya
registradas se conservan; editar/restaurar no ejecuta. La traza local está acotada a
24eventos, complementada por el registro existente, sin textos/argumentos en la traza.

Se verificó software con Robolectric/API33, DataStore/ContextStore y repositorios
locales existentes. Sin dispositivo, OneUI, Motorola, Shizuku físico ni Gemini real.
No se convierte una medición simulada en certificación física: D40 sigue PARCIAL.

## Pruebas ejecutadas

60casos únicos con último resultado aprobado:20previos deD27,28nuevos y12regresiones
contiguas. Ronda1:22/23; fallo de JSONObject en test JVM sin runner Android. Se
corrigió el runner conservando aserciones; codec repetido y correcto. Ronda2:27/27.
Una ronda durable no llegó a tests por un paréntesis faltante en el fixture; se
corrigió sin modificar producción. Ronda durable final:19/19. No se suman rondas ni
repeticiones para inflar el total. XML íntegros en verification/d27-completion-20261023.

## Avance contiguo D22

Crear nota ahora compara el almacenamiento persistido después de guardar: debe
existir un ID nuevo con el texto exacto. Solo entonces informa VERIFIED. Prueba de
adaptador real exige confirmación y verifica DataStore; cadena nativa de transformación
→nota→cálculo confirma la nota por separado, conserva el resultado y no la duplica al
restaurar. D22 aumenta cobertura, pero su certificación global de efectos externos
permanece IMPLEMENTADA SIN PRUEBAS SUFICIENTES; no se eleva por estas pruebas locales.

## Preview45

Fuente APK/ZIP:39430eb2b8247c59bb9b4f8a8341d00329ca3d70;1514blobs de fuente y237
entradas productivas/configuración cotejados contra GitHub. Preview44 y su evidencia
se conservan intactas; no se le atribuyen los cambios posteriores. Gate final4m32s correcto; R8/recursos reducidos, paquete independiente45, certificado
histórico885838aa, firmaV2, zipalign16KB, ARM64, min26/target35, noDebuggable verificados.
Ocho librerías nativas idénticas aPreview44; mappingR8 preservado en la evidencia.
APK SHA256:6722b2c57d51de89533a2aaa3766f65d27bc20180270b277753652c53b867472.
ZIP SHA256:ca9f76d4f259d1e052e95c5440d8e6c58b722b1dfb02e58c71e308d2bae2108f.
Sin nueva instalación física ni servicios externos reales.
