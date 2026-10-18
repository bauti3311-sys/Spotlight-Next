# Spotlight Next Review30 · recuperación y entrega

Código fuente: `82369b7ae4d94809a7f4754322d7b61cde449637`, rama `feat/work-integral-settings-20261016`. Continúa el trabajo posterior a751b2a1 sin retroceder ni descartar avances. APK y ZIP se vinculan a ese SHA; cualquier cierre documental posterior no cambia el código compilado.

## Qué incluye

Inicio compacto de Ajustes, centro de funciones y permisos observados, guía de capacidades elegidas por el usuario, accesos a Gemini/apariencia/documentos/integraciones/privacidad y avanzado oculto. Visitar la pantalla o seleccionar una función no concede permisos; cada configuración exige acción explícita. Preferencias existentes preservadas.

Incluye búsqueda universal, resultados compactos expandibles, Gemini inline sobre infraestructura existente, conversaciones guardadas, contexto local, acciones Android, Quick Look, OCR local con presupuesto de recursos y búsqueda semántica optativa. Se conserva la apariencia aprobada y las integraciones previas. La cancelación descarta acciones devueltas tardíamente antes de despacharlas; no deshace operaciones nativas que ya terminaron.

## Verificaciones recuperadas del mismo código

- 629 pruebas JVM/Robolectric,131 suites,0 fallos/errores/omisiones. Los XML completos y el resumen se conservaron. No se repitieron en esta recuperación para entregar antes la APK.
- lintReview:0 errores,10 advertencias y1 informativa. Incluye advertencias heredadas por acceso amplio a archivos, APIs privadas y TrustAllX509TrustManager; no se ocultaron ni se desactivaron controles. Esta revisión no equivale a una auditoría de seguridad completa.
- Comparaciones y capturas Compose incluidas en esos tests y evidencia del bloque minimalista; Settings de producción coincide con751b2a1. Solo cambió la espera asíncrona del test de selección de capacidades, conservando sus aserciones.
- La ejecución anterior completó assembleReview/R8 y luego falló al resolver dependencias de instrumentación en modo offline (junit1.2.1/runner1.6.2 ausentes). No se declara exitoso ese pipeline completo. El APK temporal se perdió; esta recuperación ejecuta únicamente assembleReview y las verificaciones del archivo final.

## Identidad e instalación

La evidencia definitiva de paquete, versión, certificado, hashes y alineación está en verification.json. Review usa `dev.bautista.spotlight.next.review`, código30, ARM64/min26/target35, sin debuggable. Se reutiliza la misma clave privada de Review751b2a1; no se usan certificados principal/P1 ni secretos históricos. Review se instala junto a la principal y tiene datos/permisos propios; no importa datos automáticamente. Si ya existe Review con el mismo certificado, el código30 permite actualizarla sin desinstalarla.

## Limitaciones reales

No hay tablet conectada ni emulador disponible: instalación física, Android16/OneUI, foco, servicios, consumo RAM/batería y OCR nativo siguen pendientes de prueba en el dispositivo. La compatibilidad ARM64/minSdk se verifica estáticamente y no demuestra comportamiento físico.

Gemini requiere credencial/modelo válidos y acción de envío explícita; no se probó el proveedor con una clave real. OCR necesita acceso autorizado y recursos disponibles. La semántica requiere consentimiento y descarga/preparación del modelo138MB; no se acredita su rendimiento en la A9+. Shizuku requiere servicio/permiso real; contactos, calendario, multimedia, notificaciones y accesibilidad dependen de permisos y capacidades Android. Las40 decisiones permanecen parciales según la matriz; no hay resultados de servicios simulados presentados como verificados.

Las capturas de Ajustes son renderizados reales de Compose/Robolectric recuperados de751b2a1 (código de Ajustes conservado); no son capturas de una instalación física. El ZIP de capturas distingue esa procedencia de las capturas de búsqueda posteriores.

No se ejecutaron GitHub Actions ni se modificaron presupuestos,main,PR16,firmas históricas o entregas conservadas. La reserva propia se comprobó antes de trabajar.

## Resultado de esta recuperación

`:app:assembleReview` correcto en7m35s,47 tareas ejecutadas. Después:183 entradas de producción idénticas al commit; firma v2/v3 verificada; certificado igual a Review anterior; zipalign16KB correcto. APK:73.144.277bytes, SHA256 `8f865b42a27db36d02d0a96659b9b3285e13a0f8a455b0fc19d869adcecd793e`. ZIP fuente:7.077.087bytes, SHA256 `dba2181e49b6afae08e1f77be7642bb922e583471da634ad17f345c4d6855e89`.

No se recompilaron las629 pruebas ni lint en esta recuperación: se verificó y reutilizó su evidencia del mismo SHA.
