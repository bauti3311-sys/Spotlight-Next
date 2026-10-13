# Laboratorio visual separado

Node 24 LTS, React/Vite/TypeScript y Playwright/Chromium. Paquetes fijados en package-lock.json; no forman parte de Gradle ni del APK. Prototipo propio de interacción, no una copia de componentes premium ni una vista PDF real.

`npm ci`, `npm run build`, `npm test`. En un entorno con Chromium existente: `PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH=/usr/bin/chromium npm test`. En el perfil visual/CI: `npx playwright install --with-deps chromium` instala el navegador solo donde se lo usa.

Las pruebas ejecutan tablet/teléfono, teclado, toque, panel/expansión y captura. Generan traces y PNG en test-results; no actualizan baselines automáticamente. Capturas web no acreditan el funcionamiento del overlay Android ni One UI.

Las referencias aprobadas están enlazadas en el inspector y en docs/CODEX-ENTORNO-APROBADO.md. Verificar licencia y acceso de cada implementación original antes de ejecutarla o reutilizarla. La matriz de bibliotecas nativas se conserva por separado; este laboratorio no introduce dependencias Android.
