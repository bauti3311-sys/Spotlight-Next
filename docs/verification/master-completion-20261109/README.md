# Fuentes públicas consultadas para C7/C8 — 09/11/2026

Se consultaron en vivo Google Maven, notas oficiales de Cast, documentación Android
AppFunctions y el manifiesto de permisos AOSP Android16. Sólo GET de fuentes
públicas: no se instaló biblioteca/modelo/skill, no se ejecutó código remoto ni se
subió código/documentos del propietario. research.json/research-extra.json guardan
URLs/HTTP/hashes y metadatos; POM y metadata XML originales conservados.
Las páginas HTML y el manifiesto AOSP completos se usaron para lectura local,
no se copian masivamente al repositorio. Las conclusiones están en la matriz.

## Cast

- Google Maven y notas oficiales listan23.0.0 (19/10/2026) como versión reciente.
- Su POM declara MediaRouter1.9.0-beta01 y otras dependencias Google/AndroidX.
- La alternativa22.3.1 también declara MediaRouter1.8.0-beta01 y kotlin-stdlib2.1.0.
- Por tanto, «versión de Cast sin sufijo beta» no prueba un grafo completamente
  estable. No recomendar22.3.1 como solución automática por ser más antigua.
- Falta resolver grafo con versiones del proyecto, revisar licencia/manifest/SDK,
  host nativo de selección de rutas, tamaño/recursos y disponibilidad de receptor.
  No se afirman compatibilidad binaria, tamaño APK ni rendimiento medidos.
- Mantener Intents/MediaSession actuales. No integrar SDK a ciegas ni anunciar Cast
  disponible por tener LocalMediaRequestPolicy. No controla sesiones Spotify/YouTube ajenas.

## AppFunctions

- Guía oficial: disponible desdeAndroid16; invocar funciones ajenas requiere
  EXECUTE_APP_FUNCTIONS. Exponer funciones propias no equivale a obtener permiso
  para ejecutar otras apps. La preview privada de integración Gemini es otro alcance.
- En AOSP android16-release el permiso EXECUTE_APP_FUNCTIONS tiene nivel
  internal|privileged, y BIND_APP_FUNCTION_SERVICE signature; se conservan los
  atributos exactos consultados en research-extra.json.
- La APK Preview firmada por el propietario es una app ordinaria, no una app de
  sistema firmada por la plataforma. No puede resolver ese acceso con un permiso
  común, un botón o una actualización de SDK. No se aprueba elevación/bypass.
- Dictamen acotado: NO-GO para anunciar invocación productiva en la variante actual.
  Estado Android/servicios/privilegios OEM de los equipos no comprobado por ADB.
  Si aparece un entorno autorizado compatible, repetir matriz/prueba aislada.
- No se sustituyen Intents ni se añade un framework/servicio. El diagnóstico actual
  informa honestamente que todavía no descubre/ejecuta funciones de terceros.
