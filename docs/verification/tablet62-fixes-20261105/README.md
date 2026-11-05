# Distribución y regreso táctil — evidencia nativa

Antes: fuentea82af60a8e212364a8f40ed3a9af49323bfa7983; sus componentes de
ventana/posición/menú conservan el código relevante de Preview62/135a9af.
Después: commit de corrección que publica estos artefactos (par exacto en PR21 y
notas). Fixture Robolectric API33 NATIVE,1280×800dp, densidad1, ancho overlay1050dp,
tema oscuro/claro, viewport independiente600→300→600dp. **No es captura OneUI**.
No se recrean Ajustes/Gboard ni se afirma que métricas OEM sean esas dimensiones.

## Comparativas sin retoque

Misma escena, datos, tema, ancho y presupuesto para cada par. Los PNG son copias
byte a byte del renderer; no se redimensionan, recortan ni retocan. Cambiar las
aserciones del fixture no cambia los datos/escena anteriores a capturar.

| Escena | Antes | Después |
|---|---|---|
| Oscuro, viewport300 | [268dp usados](comparisons/before/dark-ime.png) | [300dp usados, una fila más visible](comparisons/after/dark-ime.png) |
| Claro, viewport300 | [Antes](comparisons/before/light-ime.png) | [Después](comparisons/after/light-ime.png) |
| Menú abierto | [Sin regreso visible](comparisons/before/menu-open.png) | [Volver a resultados](comparisons/after/menu-open.png) |

También: [restauración oscura](comparisons/after/dark-restored.png),
[restauración clara](comparisons/after/light-restored.png),
[regreso del menú](comparisons/after/menu-return.png),
[control alcanzable con220dp](comparisons/after/menu-short.png).
Los viewport cerrados no se estiran artificialmente si su contenido es corto.

[Inspector APK con300dp](comparisons/after/apk-ime.png) y
[tras recuperar600dp](comparisons/after/apk-restored.png) comprueban nombre y
selección. Es un URI sintético de caché sin proveedor/lectura, por lo que aparece
el error recuperable esperado; no prueba lectura ni autorización de ese APK.
Los10 casos C2FileShareFlowTest sí ejercitan proveedor controlado y revisión/grants.

## Resultados

28 identidades distintas con última evidencia correcta, sin fallos/errores/skips
pendientes, en rondas dirigidas; **no suite global ni28 casos nuevos**. No sumar
los112 ni8 históricos. Lint RaycastPreview0 errores/27 advertencias/1 información,
mismos avisos que el gate C2 anterior. XML/TXT íntegros y logs conservados.

- green-first:5 recorridos de tablet/menú +2 de posición. Correctos.
- regression:20 casos C1/C2/barra correctos,2 fallos de fixture nuevos conservados.
- retry-fixtures:regreso corto correcto; APK encontró dos nombres (header/metadatos).
- file-final:sólo caso APK repetido, correcto; Lint y BUILD SUCCESSFUL.
- red:reproducción inicial con2 pérdidas de altura y3 ausencias del control visible.
  No demuestra que Esc/Back estuvieran rotos: esos casos exigían antes el regreso
  táctil que faltaba. height-first conserva2 fallos de expectativa de165/170dp.

summary.json deduplica por clase+método y enlaza cada última evidencia. Correcciones
de fixtures explicadas en el [informe](../../TABLET62-REGRESSIONS-20261105.md).
Se repitieron sólo casos afectados por cambios/fallos; sin suites completas ajenas.

## Comandos y entorno

JDK17 completo, SDK35/build-tools35.0.0, Gradle8.10.2. Cada ronda usa
`:app:testRaycastPreviewUnitTest`, filtros `--tests` explícitos,
`-I scripts/directed-tests.init.gradle -PspotlightDirectedClasses=<clases de esa ronda>`
y `--no-daemon --max-workers=2 --console=plain`. Las dos rondas que incluían Lint
con fallos de pruebas no produjeron cierre correcto; el comando final añadió
`:app:lintRaycastPreview --continue` para registrar ambos resultados.
Proxy/trustStore del entorno heredados por los JVM, sin modificar Gradle de la app.

Para reconstruir los filtros exactos del conjunto final desde la raíz (no se
ejecutó como una suite adicional en este cierre):

```sh
mapfile -t cases < <(python3 -c 'import json; print("\n".join(json.load(open("docs/verification/tablet62-fixes-20261105/summary.json"))["cases"]))')
filters=(); for item in "${cases[@]}"; do filters+=(--tests "$item"); done
classes=$(python3 -c 'import json; print(",".join(sorted({c.rsplit(".",1)[0] for c in json.load(open("docs/verification/tablet62-fixes-20261105/summary.json"))["cases"]})))')
bash gradlew :app:testRaycastPreviewUnitTest "${filters[@]}" \
  -I scripts/directed-tests.init.gradle -PspotlightDirectedClasses="$classes" \
  --no-daemon --max-workers=2 --console=plain
```

Las rondas funcionales rojas/verdes usan TabletPhysicalRegressionTest;
green-first incluye KeyboardPositionTest. La tabla anterior y summary.json
identifican los métodos efectivamente ejecutados. Hashes de producción y de
artefactos guardados junto a este README. Las fotos originales se identifican por
hash, sin volver a publicar el contenido de otras ventanas del dispositivo.

Sin assemble/package APK, firma/preflight real, Actions o dispositivo conectado.
Insets/global frame, focos OEM, gesto Android Back, teclado físico y resultado
real en Tab A9+/moto g86 siguen pendientes de validar en la futura APK autorizada.
