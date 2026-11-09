# Preview63 — evidencia de preparación, no de entrega

Fuente71d6980e50e6206776014946a2a606521fad9fd8, base funcional
261eef4669f775b3257b707953c6987d374a1d10. El SHA tooling/workflow es el commit
que publica este directorio y preview_delivery.json. Se comunica exacto en PR21
y notas; ningún SHA aquí constituye autorización de ejecución.

## Contratos offline del manifiesto actualizado

```sh
PYTHONPATH=scripts python3 -m unittest -v \
  test_preview_delivery.Native60PolicyTest \
  test_preview_delivery.Native60ArtifactTest
```

13 casos correctos, sin errores/fallos; contracts.log íntegro. Las clases conservan
su nombre histórico pero importan la configuración63 del manifiesto. Comprueban
revisiones/actor/ref/reserva/historial/reintentos y metadata/certificado/ZIP con
fixtures de texto/archivo, sin APK real, claves ni ejecución del firmador.
No se ejecuta PreviewDeliveryWorkflowTest.test_missing_secrets_create_no_signing_files,
que invocaría el script preflight. Workflow/scripts/guardas permanecen idénticos
y se reutilizan sus verificaciones anteriores; no se repite Actionlint sin delta.

## Revisión estática y evidencia Android reutilizada

static-review.json registra comprobaciones con aserciones sobre el árbol Git:

- Base ancestro de fuente; diff exactamente app/build.gradle.kts y documento63.
- Gradle idéntico tras sustituir sufijos v62→v63 y output.versionCode62→63.
- Manifiesto coincide con fuente/base, versión, paquete/certificado históricos yUS$0.
- app/src, identidad principal, dependencias, workflows, verificadores y firmador
  sin cambios frente261eef4. No cambios visuales ni funcionalidades nuevas.
- uses-permission idénticos a la fuente APK62 (135a9af); no confundir esto con
  manifiesto completo idéntico: C2 ya amplió queries/filtro receptor y caché local.
- Todos los hashes de c2-final-20261104 comprobados;8 pruebas dirigidas/Lint
  0E/27W/1Info reutilizados, sin ejecutar Android otra vez ni sumarlos a históricos.

Para inspeccionar el delta fuente y manifiesto:

```sh
git diff 261eef4669f775b3257b707953c6987d374a1d10 \
  71d6980e50e6206776014946a2a606521fad9fd8 -- app/build.gradle.kts
git diff --name-only 261eef4669f775b3257b707953c6987d374a1d10 \
  71d6980e50e6206776014946a2a606521fad9fd8
cat scripts/preview_delivery.json
```

No Gradle/build/compilación Android, firma, preflight real, dispatch/re-run ni APK
en esta preparación. No acceso a secretos ni revisión manual nueva de facturación.
Paquete/firma/instalación efectivos sólo se validan sobre artefacto futuro autorizado
y dispositivo real. [Checklist práctico](../../NATIVE63-PREPARATION-20261105.md).
