# Gemini: propuestas completas antes de ofrecer herramientas

Base: `1d84fc11e292ccefa739cbf52490aeb1b944f3d8`.
Estado: fallo reproducido sobre 043ff24 (10 pruebas, 3 fallos, 0 errores/omisiones); corrección implementada, todavía SIN VERIFICAR. XML RED preservado en verification/gemini-completion-20261018.

Fallos observados: LANGUAGE aceptado como propuesta completa; prompt SAFETY aceptado pese a bloqueo; llamada marcada thought ofrecida como acción. Corrección: exigir STOP en generateContent no streaming, rechazar bloqueos de prompt y excluir partes thought completas. Se conserva el streaming independiente de DirectAi.

Se comprueba que el parser de propuestas no acepte estados de finalización incompletos/desconocidos, bloqueos de prompt aunque incluyan candidato y llamadas marcadas como pensamiento. Una respuesta STOP normal debe conservar las herramientas disponibles. Son fixtures locales, sin solicitudes ni gastos de proveedor.

Referencias oficiales consultadas: https://ai.google.dev/api/generate-content y https://developers.raycast.com/api-reference/tool . Se mantiene ToolRegistry y la autorización de acciones existente; no se crea un ejecutor paralelo.

Siguiente paso: ejecutar ToolProtocolTest contra producción actual, conservar evidencia del fallo real, corregir lo mínimo y volver a verificar junto con planes/capacidades. Las decisiones 4, 16, 18 y 20 siguen parciales. Lint del checkpoint PDF sigue en curso y no se declara aprobado aquí.
