# Puente ligero / implementación nativa

Abrir Galeria-Puente-Nativo.html (autosuficiente, sin red) o Puente-Nativo.pdf. No es una APK ni un nuevo prototipo web de la app: las imágenes de implementación proceden de SpotlightPanel real, Compose/Robolectric API33. web-reference contiene el H02 elegido, Chromium propio, rotulado por separado. Sus fixtures son distintos; no afirmar paridad pixel a pixel.

24PNG nativos equivalentes antes/después y4referencias web. test-summary.json y final-checks registran20casos finales, todos correctos sobre la fuente publicada. first-native-checks conserva la primera verificación anterior al ajuste compacto. Se preservan las fallas para reproducir los defectos corregidos. No se cambian ni rebajan los tests históricos. Sin nuevos baselines de lint.

Para captura con caché existente:

```sh
PUENTE_CAPTURE_PHASE=after bash gradlew :app:testDebugUnitTest --tests '*PuenteNativeIntegrationTest' -I scripts/directed-tests.init.gradle -I docs/verification/c2-app-open-20261107/no-apk.init.gradle -PspotlightDirectedClasses=dev.bautista.spotlight.ui.PuenteNativeIntegrationTest -Pkotlin.compiler.execution.strategy=in-process --offline --no-daemon --max-workers=1 --console=plain
```

gallery.py y pdf.py reconstruyen desde originales; browser.py comprueba comparación/zoom. verify_preservation.py coteja rutas protegidas, despacho/atajos y refs remotas sin escribir GitHub. SHA256SUMS verifica evidencia. Requieren librerías/herramientas ya presentes, sin instalación automática.

La evidencia de tests enumera comandos y limitaciones; faltan OneUI/Gboard/IME/rotación/TalkBack/rendimiento físicos. Sin APK/testAPK, firma, instalación ni Actions. El valor Focus=false sigue por defecto, sin nueva preferencia. La autorización69 no se reutiliza. Ver ../../PUENTE-NATIVE-20261108.md.
