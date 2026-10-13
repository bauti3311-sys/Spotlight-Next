# F1.3: fixtures de geometría nativos

OverlayGeometryCaptureTest rasteriza SpotlightPanel real con Compose NATIVE/SDK33. Las áreas de sistema/IME son sintéticas explícitas: no son capturas de un dispositivo ni validación One UI.

Mismos controles/preferencias antes/después. Antes usa algoritmo histórico del servicio (display completo, posición sin IME, altura mínima100dp); después usa viewport seguro y límite superior del teclado. El test exige bounds reales fuera de IME y dentro de ancho seguro.

Tablet horizontal1280x800:76992 píxeles distintos; teléfono vertical390x844:6246. Diff RGB absoluto sin máscaras. Baselines existentes intactos. Archivos before/after/diff versionados; workflow conserva f13-*.png como evidencia.

El fixture antes/después mantiene el centrado del marco ajustado de WindowManager.

ActionsIme: fixture Compose NATIVE tablet con controlador IME simulado, viewport246dp antes de Tab. RED real: encabezado Acciones fuera de vista; GREEN: cerrar IME al entrar a acciones permite recuperar700dp manteniendo foco en Copiar resultado. 308600 píxeles distintos, diffRGB absoluto sin máscaras. No es captura de dispositivo ni prueba OneUI.
