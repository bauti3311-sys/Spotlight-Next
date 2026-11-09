# Ficha C5.4 — completar sólo con evidencia real

Estado inicial: **NO EJECUTADO**. No trasladar PASS de Robolectric, compilación o PDF fixture a hardware.

| Identificación | Galaxy Tab A9+ | Motorola G86 |
|---|---|---|
| RUN_ID / fecha / operador | Pendiente | Pendiente |
| Android/build/orientación/ventana | Pendiente | Pendiente |
| SHA fuente autorizado y procedencia APK/ZIP cotejada | Pendiente | Pendiente |
| Paquete/versionCode/versionName reales | Pendiente | Pendiente |
| SHA-256 APK aplicación / test APK | Pendiente | Pendiente |
| Certificado aprobado y coincidencia app/test | Pendiente | Pendiente |
| Modelo ready/revisión/hash, en ESTE paquete | Pendiente | Pendiente |
| Recursos batería/carga/ahorro/memoria/térmica | Pendiente | Pendiente |

Estados permitidos: NO_EJECUTADO, BLOCKED (causa concreta), FAIL (defecto/evidencia), PASS (postcondición comprobada).

| Comprobación | Tab A9+ | G86 | Evidencia exigida |
|---|---|---|---|
| PDF real: 3 páginas, literal/offsets/Unicode/SHA | NO_EJECUTADO | NO_EJECUTADO | pdf.json + JUnit |
| Native renderer página2 desde acción explícita | NO_EJECUTADO | NO_EJECUTADO | pdf.json + observación de Quick Look |
| DocumentsUI seleccionó sólo raíz ficticia | NO_EJECUTADO | NO_EJECUTADO | Captura picker + grant del árbol fijo |
| Mutación/borrado/inaccesible controlados retiran cita | NO_EJECUTADO | NO_EJECUTADO | saf.json + sin callback obsoleto |
| Read bloqueante y cancelación: estado vacío antes de retorno | NO_EJECUTADO | NO_EJECUTADO | saf-cancel-observation.json, tiempos reales |
| Retirada grant persistido, ningún texto republicado | NO_EJECUTADO | NO_EJECUTADO | saf.json; distinguir PFD/transitorio |
| OCR real disponible y extracción observada | NO_EJECUTADO | NO_EJECUTADO | ocr.json + ocr-extraction.json |
| OCR conserva incertidumbre; revisar 1O/10 sin inventar corrección | NO_EJECUTADO | NO_EJECUTADO | Comparar imagen y texto reconocido |
| Scroll real no abre; botón solicita apertura explícita | NO_EJECUTADO | NO_EJECUTADO | focus-host.json + observación |
| Copia conserva procedencia (adaptador en memoria) | NO_EJECUTADO | NO_EJECUTADO | focus-host.json; NO clipboard real probado |
| Stop/resume elimina cita, sin restaurar snapshot | NO_EJECUTADO | NO_EJECUTADO | focus-host.json |
| Semántica auténtica opcional: ready/preferencias/recursos aptos | NO_EJECUTADO | NO_EJECUTADO | semantic.json/semantic-build.json |
| Ranking/TP/FP/FN/tiempos por consulta, mismo corpus | NO_EJECUTADO | NO_EJECUTADO | semantic.json + corpus SHA |
| PSS/latencia observados, sin atribuir pico o RAM exclusiva | NO_EJECUTADO | NO_EJECUTADO | JSON crudo; condiciones del dispositivo |
| Hooks del overlay productivo C5 | NO_EJECUTADO | NO_EJECUTADO | Pendiente de integración futura autorizada |

C5.3 aceptado: léxico7/12 pares relevantes, 5 omisiones, 0FP; modelo auténtico actual sin evaluar. Mantener esa referencia separada de cada campaña física. El caso de semántica devuelve resultados de recuperación, no citas semánticas válidas.

Bloqueos (dispositivo/autorización/modelo/recursos/engine/firma/target/descarga de evidencias):
- Pendiente.

Defectos observados, pasos exactos, archivo JSON/captura y gravedad:
- Pendiente.

Decisión de aceptación del propietario:
- Pendiente. No cerrar C5 ni declarar otra Preview autorizada mediante esta ficha.
