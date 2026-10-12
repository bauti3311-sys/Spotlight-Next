# Entrega 2.0.0 / 27

Identidad conservada: dev.bautista.spotlight.next. Mínimo API 26, objetivo API 35, release arm64-v8a optimizado. Firma únicamente mediante el workflow histórico en main, con Secrets fuera de PR y forks.

Certificado esperado SHA-256: 11b0b3b37544bd95b128de52798fa7ffb7489f94819614b1ada383bd4ee7a6cf. apksigner y zipalign verifican el APK real; el script comprueba applicationId, versión mayor que 26, ausencia de debuggable y minSdk compatible. No se generan claves de firma.

Rama de implementación: feat/spotlight-glass-2.0. La recuperación usa el checkpoint 2a8e317 y commits posteriores; main permanece estable hasta completar comprobaciones. El entorno perdió trabajo local no publicado; los nuevos checkpoints se guardan en GitHub.

Estado: aún no se entrega un APK firmado 2.0 ni se declara terminado el control de calidad. Este archivo se actualizará con el resultado final y el hash del artefacto cuando exista.
