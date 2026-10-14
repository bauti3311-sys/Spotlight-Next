# Entrega debug mediante GitHub Actions

Los enlaces locales `/workspace/shared/` no son descargas de GitHub. La APK y el ZIP locales de `8dd2524` existen y se verificaron el 14 de octubre de 2026; no se pudieron adjuntar a una ejecución finalizada desde esta sesión.

Alternativa preparada sin modificar Android: abrir **Actions → Build Debug APK → Run workflow**, seleccionar **fix/debug-artifact-delivery** y ejecutar una vez. El workflow existe en main, pero hay que elegir esta rama para usar la entrega acotada. Esta sesión no dispone de dispatch en el conector y la CLI recibe `Forbidden`; no se afirma que la ejecución haya comenzado.

El único job compila debug del SHA fijo `8dd25249268fc072d1871e037fe6d7a4112795d1`, verifica certificado/integridad y genera `git archive` del mismo SHA. No corre tests, emuladores, firma histórica ni releases. Tiene caché, cancelación y límite de 15 minutos. Los dos artefactos quedan siete días y requieren sesión de GitHub con acceso al repositorio privado. Sus enlaces aparecen en el resumen de la ejecución. GitHub entrega cada artefacto en ZIP: extraerlo para obtener APK o ZIP fuente y `SHA256SUMS`.

La recompilación usa el certificado debug del runner y puede cambiar tamaño/hash respecto de la APK local. El resumen publica los valores reales de esa entrega; no usar el hash local para verificarla.

APK local verificada: **177754718 bytes**, SHA-256 `9808aad66e76014d2bc43396f503cde0997e46c6ec2130c048c19dd12d4073eb`. ZIP **3456769 bytes**, SHA-256 `cdc24384400d7cfc007bd1d636683ee2f914af4eddf40548ce63f9623917b913`. APK 2.0.1, código 28, paquete `dev.bautista.spotlight.next`, Android mínimo 8.0/API26, ARM64 incluido. Firma Android Debug; actualización sobre versión instalada y funcionamiento físico One UI no verificados. F1.3 sigue abierto.

Main, PR anteriores y checkpoints intactos. Continuidad funcional: `feat/contextual-expansion-f14` / `876f7cb`; esta rama solo prepara entrega. Siguiente paso: la única ejecución manual indicada arriba y verificar sus artefactos, tamaño y SHA-256, sin otra matriz.
