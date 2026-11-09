# Integración Studio, evidencia original

Basef36d830,30pares reales antes/después,6perfiles/5escenas, hashes enmanifest.json.
SpotlightPanel real en3escenas; componentes reales enacciones/ajustes. API33/native,
fixtures PM, no hardware. No retoque ni overrides A/B. build-gallery.py SALIDA genera
HTML/PDF8p/ZIP usando herramientas existentes de laboratorio, sin deps de aplicación.

VisualFirstGalleryTest incorpora env opcional SPOTLIGHT_CAPTURE_ROOT=build/studio,
SPOTLIGHT_MAIN_SCENES=1 y SPOTLIGHT_VISUAL_PHASE=before/after. Se limitan los6métodos
tabletDark/tabletLight/phoneDark/phoneLight/enlargedDark/enlargedLight. Before6correctos.
After agrega FocusBIntegrationTest.phoneDarkText200,VisualFooterBudgetTest,
VisualPolishTokensTest y StudioBudgetTest,18casos correctos y lintDebug0E26W1Info.
Comandos/logs enbefore/after; init scripts/directed-tests.init.gradle filtra esasclases,
--no-daemon --max-workers=1 --console=plain, Java17/SDK35 existentes. No assemble.
StudioActionKeyboardTest adapta2contratos existentes al Focus productivo para nuevas
superficies; se ejecuta solo sin volveracorrer lint. No tests históricos globales.
Inicialcompile.log preserva importfaltante, luego corregido. No baselines ocultados.
