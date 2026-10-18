# Spotlight Next Review · entrega verificada

Código y ZIP: `751b2a14a77156b00d9a39d56f1f077a32b5defb`. Paquete `dev.bautista.spotlight.next.review`, versión 2.0.1-review/código29, ARM64, Android8/API26 o posterior, target35. Instalación independiente de principal y P1; comienza con datos/permisos propios. No desinstala ni migra automáticamente las anteriores.

## Qué incluye

Inicio de Ajustes compacto, resumen, accesos, centro de9funciones con estados locales y autorización individual guiada; Avanzado oculto tras Más opciones. Conserva barra Obsidian Glass, Quick Look e inspector y todo el código funcional anterior: búsqueda universal, Gemini inline/herramientas, documentos/OCR, semántica optativa, conversaciones/contextos, acciones Android e integraciones. Ninguna nueva función añadida durante esta recuperación.

## Verificación ejecutada

- Recuperación Git exacta;182entradas de producción coinciden con la copia compilada.
- `:app:assembleReview`: R8 y empaquetado correctos en7m49s. No es APK Debug.
- 67 casos únicos dirigidos aprobados entre las rondas registradas: Ajustes, navegación, permisos, persistencia, ancho, OCR, semántica y herramienta documental. Primera ronda67:66correctos y1fallo del selector de captura por dos ventanas; el reintento conserva las comprobaciones funcionales y selecciona el diálogo. No se suman repeticiones ni pruebas históricas.
- `:app:lintReview`: sin errores; recuentos por severidad `{"Warning": 10, "Information": 1}`. No se desactivaron verificaciones.
- Firma v2/v3 y zipalign16KB correctos; certificado Review recuperado: `a5ca9c2479da8149a1f6c43936bce2a4639f724e988d20a9ae224291c55c5f14`. No se usaron firma histórica ni P1. El respaldo privado original sigue recuperable.
- Gitleaks: sin hallazgos en el árbol actual; no auditoría completa de historial. Cero ejecuciones de Actions para esta rama al consultar; no se inició CI ni se cambió presupuesto/main/PR16.
- Capturas reales de MainActivity/Compose con Robolectric native graphics: inicio, permisos, diálogo de guía y Gemini. Son renderizado JVM/Android simulado, no fotografías ni capturas de One UI físico. Las llamadas extra de captura se documentan en la evidencia y no cambian el APK.

## Límites para probar

Gemini necesita seleccionar proveedor/modelo y guardar tu clave; no hice llamadas reales al proveedor. Los permisos son voluntarios y se vuelven a comprobar. OCR es local y acotado; semántica necesita descarga voluntaria138MB, habilitación y preparación del índice con recursos suficientes. Shizuku requiere su propia conexión/autorización; notificaciones/multimedia dependen de servicios y sesiones compatibles. La guía deriva Gemini/Shizuku a sus áreas para completar configuración. Las40decisiones siguen parciales según la matriz; esta es la Review excepcional autorizada.

No hay tablet/emulador conectado ni KVM: instalación, arranque frío, foco, blur, rendimiento/RAM y reconocimiento nativo requieren comprobación física. No se repitió la suite completa ni Python; no se afirma CI verde.

## Archivos y huellas

- `Spotlight-Next-Review-751b2a1.apk` · 73,201,820bytes · SHA256 `c38e034cf5275bed19f5c3f68456392dd642e581603a0a02647c066c993a19e3`
- `Spotlight-Next-source-751b2a1.zip` · 6,796,611bytes · SHA256 `6d3907b7e04a59353c11b90523809bf896e115eb7a2c0e29aa8df4f7396283b0`

La APK y ZIP fuente se guardaron privadamente con sus nombres exactos; recuperarlos por esos nombres mediante la skill de archivos. Respaldo de clave Review: Spotlight-Review-signing-private-backup.zip (privado, nunca incluir en fuente/artefactos públicos). Certificado publicado arriba. No regenerar una clave para ese paquete si no se recupera el respaldo.
