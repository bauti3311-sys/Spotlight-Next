# Evidencia de corrección Focus Studio

`gallery.html`: 24 pares antes/después (8 perfiles × 3 escenas). `before/` procede de 9d65277; `after/` del código de 2731897. PNG originales de componentes Compose, fondo y autorización ficticios. No imágenes de APK ni mediciones One UI.

`final/`: último XML de cada clase (19/19 casos distintos), log final de galería y análisis main, más log del arreglo de footer. Las tandas reutilizan regresiones no afectadas; no sumar reejecuciones. `lint/`: lint global 1 error preexistente de C5, 26 warnings y 1 informativo; hash confirma archivo idéntico al HEAD base. No se suprime.

`before-failures/` y `footer-before-failure/`: cinco aserciones rojas genuinas previas a sus arreglos. `initial/`, `initial-regression/`, `final-fixture-failures/`, `footer-fixture-failure/`: fallos intermedios de banco, conservados. La captura diagnóstica identificó controles renderizados mientras la aserción de visibilidad aún no se estabilizaba; la espera por condición conserva las mismas aserciones. No es evidencia de una falla de autorización real.

`intermediate-footer/`: primera corrección todavía incompleta del respaldo del footer. Los logs intermedios corresponden a ese estado, no a los PNG finales.

Las fotos físicas originales permanecen en la conversación; no se publica la aplicación personal de fondo. `validation.json` describe alcance, identidad y ausencia de APK. `SHA256SUMS` cubre los archivos de esta evidencia excepto a sí mismo.
