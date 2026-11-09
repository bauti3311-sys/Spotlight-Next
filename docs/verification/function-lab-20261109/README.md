# Funciones para elegir — laboratorio offline

Abrir `Elegir-funciones.html` en un navegador moderno con JavaScript habilitado.
No instalar nada en Android. Se puede abrir sin red: HTML, datos y Geist embebidos.
La selección es en memoria; descargar JSON antes de cerrar. No contiene aprobación
implícita ni activa funciones productivas.

36 propuestas conservan exactamente IDs, títulos y grupos del catálogo del 08/11.
**Los IDs F de este catálogo son su propio espacio de nombres**, no el numerado F
del plan maestro de 23 mejoras. Exportar siempre código + título + versión.

- 11 demos editables: F01–F09, F12, F21. Cálculos, texto y horarios realmente
  calculados en navegador con límites; no importan motores Kotlin ni prueban Android.
- 25 recorridos ficticios para capacidades de plataforma/datos; F17 no contiene
  PDF ni OCR/modelo real. F18 calcula SHA-256 real de un texto fixture conocido
  durante la generación; no escanea archivos. F22 obliga elegir homónimo.
- «Simular solicitud» no abre apps, envía, llama ni verifica efectos externos.
  Doble activación no duplica el recibo ficticio; cambio de datos exige revisión.
- Fuentes y hash del código auditado en `funciones.json`. Presencia de archivo
  en fuente70 no equivale a implementación completa de una propuesta. F17 sólo PR.
- C3/C4 pausados, C5 físico bloqueado; no cambiar sus aceptaciones con esta evidencia.

## Reproducir

Desde la raíz, Python existente + Playwright/Chromium, Pillow y ReportLab:

```sh
python3 docs/verification/function-lab-20261109/build.py
python3 docs/verification/function-lab-20261109/check.py
python3 docs/verification/function-lab-20261109/package.py
```

No Gradle, APK, red, CI, permisos ni dependencias de la app. Las herramientas de
verificación ya estaban disponibles; no se instaló ninguna. `check.py` bloquea y
registra todas las solicitudes de red. Carga con `set_content` el HTML exacto;
**navegación file:// no fue probada** por restricción del navegador administrado.
211 comprobaciones web correctas, sin errores JS ni solicitudes de red. No prueban
IME, One UI, Compose, SAF, lifecycle, OCR o ejecución externa.

19 PNG de Chromium sin retoque en `originales/`; montaje sólo redimensiona y pone
rótulos fuera de las capturas. Teléfono390px/tablet1440px son viewports de laboratorio,
no mediciones físicas A9+/G86. PDF de 11 páginas para lectura, HTML para interactuar.
`SHA256SUMS` verifica originales/entregables; ZIP contiene todo para uso offline.
