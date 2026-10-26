# Gate obligatorio de compilación — pendiente de autorización de CI

Base solicitada085142ea35bd7d19085d45118ee1776b1c80d721; misma rama y PR18borrador. Sin cambios productivos ni nuevas dependencias.

Comprobación local única: JDK17.0.20 disponible; Gradle8.10.2 ejecutable/SDK35 no disponibles. `bash scripts/test-d28-directed.sh ui` terminó2 antes de iniciar Gradle: «Falta SDK Android35 configurado. No se iniciaron pruebas.» No se intentó D28 ni los grupos posteriores, ni descargas/reintentos.

Alternativa preparada en `.github/workflows/build-apk.yml`, manual únicamente:
- `mode=directed-tests`; `source_sha` completo, inicial085142ea35bd7d19085d45118ee1776b1c80d721. Verifica checkout exacto y ancestralidad en la rama autorizada. Registra SHA fuente y SHA/ref del workflow por separado.
- Un job Ubuntu24.04, Temurin17, Gradle8.10.2, SDK35/build-tools35.0.0. Reutiliza SDK/cachés; descarga paquetes faltantes en CI. Compila producción y fuentes unitarias una vez (online para resolver dependencias), luego runner offline en orden ui,d28,capabilities,spotify,cast,imports,metrics,sources. Robolectric puede descargar su runtime propio. Se detiene en el primer fallo.
- Sin firma, secretos de firma, assemble, APK, emulador, instrumentación ni lint amplio. Preview existente separado tras `mode=preview`; no forma parte de esta autorización.
- Conserva primer fallo, logs y XML por grupo antes de la siguiente invocación. Artefacto logs/XML, retención3días; no cachés/binaries/APK adjuntos.

Recursos estimados, NO medidos: un runner Linux, aproximadamente10–25min si completa, límite30min; podría abortar antes en compilación. Artefactos normalmente1–10MB, caché Gradle potencialmente cientos de MB–2GB. No se conoce la cuota actual de Actions; no inferir saldo de datos históricos. No se ha lanzado ninguna ejecución.

Validación local de configuración:5/5 pruebas Python (YAML, permisos, SHA, separación de modos, filtros, preservación de fallos y sintaxis bash). No equivale a actionlint, ejecución GitHub ni compilación Android. Actionlint/ShellCheck no disponibles; no se instalaron para esta preparación.

Tras autorización, ejecutar una sola corrida sobre el SHA aprobado. Si compilar falla, recuperar compile.log y corregir solo lo señalado; conservar el primer fallo. Si ui falla, priorizar Layout/altura/footer/hint Guardados. No promover decisiones ni preparar Preview hasta resultados suficientes.

Se comprobó por GET la existencia de los tagsv4 de checkout/setup-java/setup-android/setup-gradle/upload-artifact en sus repositorios originales. Eso no equivale a probar su ejecución ni a verificar disponibilidad de cuota. El archivo build-apk.yml existe en main con workflow_dispatch; esta preparación modifica solamente la rama autorizada.
