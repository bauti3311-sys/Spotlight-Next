# Evidencia de preparación Preview69

No APK ni ejecución de Actions. Fuente aislada cbc956ab56756d9ebdf7de05300fb0b5f5b14a19,
base exacta c51677b79446ce1dc8213553891d45897b14d726.

- `manifest.json`: copia del manifiesto autoritativo del workflow.
- `verify_isolation.py`, `isolation.json`, `isolation-checks.log`: árbol completo,
  ausencia C5 y tres comprobaciones de la policy existente con API ficticia.
- `preview68-to-source69.diff`: delta cerrado de diez rutas.
- `source69-to-pr-productive.diff`: sólo C5 queda en PR y fuera de la entrega.
- `reuse-and-preservation.json`: blobs de archivos protegidos y verificación de hashes
  de evidencias anteriores. No declara una nueva ejecución histórica ni lint de candidata.
- `inspect-identity.init.gradle`, `identity.log/json`: configuración AGP sin build.
- `offline-delivery-tests.log`: 17 tests de entrega con fixtures explícitos.
- `isolated-tests/`: XML y log del ensamblaje fuente69 (JVM/Robolectric; sin dispositivo).

Reproducir desde la raíz del PR: `python3 docs/verification/native69-preparation-20261108/verify_isolation.py`
y `python3 -m unittest discover -s scripts -p test_preview_delivery.py -v`.
Inspección AGP y test dirigido tienen comandos exactos y guardas en `commands.txt`.
No ejecutar el protocolo de entrega sin autorización nueva, par exacto y reserva nueva.
