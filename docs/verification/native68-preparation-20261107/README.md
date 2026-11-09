# Preview 68 — evidencia de preparación, sin APK

Fuente c51677b79446ce1dc8213553891d45897b14d726, rama feat/preview68-visual-isolated-20261107.
Base instalada67=6918204238706d3c9bac826196ad3f1f272a51e7; PR conserva214e00a y C5.

- isolation.json/diffs: auditoría completa de los cambios productivos y aislamiento de la fuente.
- verify_isolation.py + isolation-checks.log: freeze exacto y tres pruebas nuevas de la policy existente con API/dispatch ficticios. No llamadas remotas desde las pruebas ni ejecución real.
- offline-delivery-tests.log:17 casos existentes de entrega, ejecutados contra el manifiesto68 porque cambió su fuente/base/allowlist.
- identity.log/json y script:init: AGP offline, única tarea propia de sólo configuración; code/name68 y perfil, sin compilar ni empaquetar. initial-identity-label.log conserva una primera etiqueta heredada que decía certificado comprobado contra67; esa etiqueta no prueba firma. La salida final fue corregida para indicar que NO se verifican claves/certificados en preparación.
- reuse.json:19 pruebas visuales reutilizadas, seis archivos productivos y seis clases de tests idénticos; hashes anteriores comprobados. No se repiten suites.
- prior-pr-lint-results.xml:lint real de214e00a,0E/26W/1Info. No es una corrida de lint en la rama aislada; fuente no visual conservada de67.
- apk-inventory.json:0 APK en ambos worktrees;0 Actions/instrumentación/firma.

Informe principal:../../NATIVE68-PREPARATION-20261107.md. Manifiesto:../../../scripts/preview_delivery.json.
El SHA workflow exacto se publica en notas/PR/respuesta después de crear el commit de este manifiesto. Los resultados de comparación GitHub posteriores al push atómico se registran en notas y copia descargable, sin cambiar el par autorizado por preparar.
