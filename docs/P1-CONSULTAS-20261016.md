# Consultas verificadas — primer bloque P1

Pruebas de producción y evaluadores locales en Android/JVM/Robolectric. No son validación física ni respuestas reales del proveedor Gemini.

| Consulta | Resultado comprobado |
|---|---|
| `¿Cuánto es 15% de 48.000?` | 7200, resultado local; Enter copia; cero solicitudes al servidor IA de prueba. |
| `15% de 48.000` / `48.000 × 0,15` | 7200, calculadora existente. |
| `0.125 * 2` / `0.001 + 0.001` | 0.25 / 0.002; no se convierten en miles. |
| `12.345,67 + 0` | 12345.67, miles y decimal argentinos. |
| `calculá -48.000 / 2` | -24000, operación local. |
| `¿Cuántos metros son 2 km?` | 2000 metros, resultado principal local sin Action.Ask. |
| `convertí 1,5 mib a kib` | 1536 kib, conversor existente. |
| `¿Cuánto es 32 f en c?` | 0 c, temperatura local. |
| `¿Cuántos dólares son 100 pesos?` | El conversor local rechaza monedas sin proveedor; no inventa cotizaciones. No se verificó una respuesta remota a esta pregunta. |
| `¿Cuántos kg son 10 metros?` | Familias incompatibles: ninguna conversión ficticia. |
| `cuántas veces ganó Boca la Libertadores` | Se conserva Gemini principal, envío explícito con Enter y respuesta inline; transporte HTTP simulado en tests. |
| `Abrí Spotify y bajá el volumen al 40%` | Plan existente, confirmación y herramientas Android; paquete instalado de fixture y AudioManager Robolectric, no reproducción física. |
| `Buscá el PDF de rehabilitación vestibular y explicame el capítulo 3` | PDF de fixture, extracción local, revisión exacta y envío autorizado igual al texto mostrado; transporte HTTP simulado. |

ToolExecutor y búsqueda usan los mismos Calculator/LocalTools; prueba de ejecución de calculate y convert_units con las consultas naturales. No se creó un motor paralelo.

Limitaciones: formato ambiguo `1.234` se trata como miles en esta calculadora española; para expresar el decimal se puede usar `1,234`. El parser conserva Double y límites existentes; no es contabilidad de precisión arbitraria. No se implementaron expresiones verbales ilimitadas, cotizaciones, OCR/semántica universal ni nuevas fuentes de clima/deportes en este bloque. Historial de apertura de archivos sigue explícitamente pendiente. Resto de las40 decisiones: referencia aprobada, no afirmación de terminación.
