# Entrega nativa Preview — procedimiento autónomo restringido

## Vigente05/11 — Preview63 preparada, ejecución NO autorizada

Fuente71d6980e50e6206776014946a2a606521fad9fd8, base funcional C2 cerrado
261eef4669f775b3257b707953c6987d374a1d10, code63>62, paquete/certificado
Raycast históricos intactos. [Preparación y checklist breve](NATIVE63-PREPARATION-20261105.md).
El commit que actualiza preview_delivery.json es el SHA tooling/workflow que debe
presentarse junto al fuente para autorización expresa de un único intento63.
Hasta entonces no build, firma, preflight real ni Actions. Adquirir reserva propia
nueva al ejecutar, con checkpoint=workflow autorizado. Autorizaciones anteriores
no habilitan63. No cambiar guardas/scripts/workflow ni presupuestoUS$0 para esta
preparación; reutilizar el procedimiento inferior, sustituyendo sólo identidad
de entrega desde el manifiesto revisado. Datos/efectos/instalación físicos pendientes.

Las secciones62/61 siguientes son históricas; no describen autorización vigente.

Actualización Preview62: sólo preparación autorizada desde76a1c18. Fuente exacta
135a9af74be08fc4368c8b9fc803d12fdcd273d3 (numeración62, función intacta).
Manifiesto revisado con previo61/base76a1c18. Presentar SHA workflow del commit
que fija este manifiesto y esperar autorización específica para un intento62.
La autorización61 inferior es histórica; no habilita ejecución62 ni repetición61.
Ver [preparación62](NATIVE62-PREPARATION-20261104.md).

Decisión del propietario 2026-11-04: GitHub Free con cuota incluida disponible,
presupuesto Actions US$0, bloqueo al alcanzar presupuesto y secretos históricos
configurados. Preview60 es precedente de compilación/firma correcta. No pedir
capturas de facturación en cada checkpoint. No modificar facturación, métodos de
pago o límite0. Consultar uso automáticamente cuando sea accesible;403 indica
falta de permisos, no pérdida de presupuesto. Si GitHub bloquea cuota/presupuesto,
detenerse sin eludirlo. No prometer saldo contable que la API no permite observar.

Cada entrega requiere autorización de su alcance/ejecución; no renovar la
confirmación rutinaria de facturación. El propietario autorizó preparar y ejecutar
un único intento Preview61 después de verificar restricciones y SHA exactos.
Esta decisión prevalece sobre pedidos de reconfirmación de presupuesto anteriores.

## Ruta reutilizable

build-apk.yml añade mode=native-delivery. native60-once, todos los jobs históricos
y scripts native60 permanecen sin cambios. No se modifica main. Se usa el workflow
ya registrado, desde la rama autorizada de PR21. Concurrencia1, sin cancelación
automática; Ubuntu estándar, timeouts5/30/10min, caché de dependencias sólo lectura,
retención unsigned1día/final3días. Sin emuladores, suites repetidas ni reintentos.

scripts/preview_delivery.json fija fuente, base funcional, versión, versión previa,
identidad y lista explícita de archivos permitidos en el delta. Para otra entrega
se actualiza/revisa este manifiesto en un commit autorizado, no las guardas60.
La fuente puede ser un commit anterior al tooling: APK/ZIP siempre representan
el SHA fuente, separado del SHA del workflow que ejecuta la entrega.

Antes de cada job se valida repositorio/actor/triggering_actor/ref/evento,
SHA workflow=SHA evento=SHA revisado, fuente exacta, confirmación por versión,
reserva owner/lease/checkpoint exactos, run_attempt1 e historial autoritativo.
Se rechaza otro intento del mismo SHA workflow y otro dispatch de la misma fuente
identificado por run-name, incluso con workflow distinto. Un fallo no habilita
retry; necesita diagnóstico y una nueva decisión expresa, sin cambiar guardas
para sortearlo. Historial inaccesible o comparación de fuente inválida: detenerse.

1. Preflight del firmador en runner aislado: certificado885838aa y PrivateKey
   accesible; sin Gradle ni exportación de clave.
2. Build unsigned en runner separado, sin secretos, fuente exacta y una sola
   ejecución assembleRaycastPreview optimizada. Verificar identidad, sourceZIP
   exacto, ARM64/min26/target35/noDebuggable, zipalign y ELF16KiB, contenido nativo.
3. Firma en runner nuevo con tooling revisado, sin ejecutar app/sourceZIP/Gradle.
   Credenciales sólo en paso de firma, temporales privados y limpieza al salir y
   always. Validar digests/procedencia antes de firmar; firma v2/un firmante885838aa;
   payload idéntico unsigned/firmado; subir APK+ZIP+hashes+procedencia públicos.
4. Descargar y verificar independientemente; comparar ZIP con git archive del
   SHA fuente y comprobar firma/identidad/compatibilidad estática. Entrega privada;
   instalación manual sin desinstalar/borrar datos. Liberar sólo reserva propia
   con notas atómicas. No promover checkpoint integral por una entrega Preview.

## Preview61 autorizada

Fuente:11b647c2ec930d4f7cc7f6d38d5c7de8d233de80; base funcional C1:8cd43a7.
Único delta de app: numeración61. Certificado histórico completo:
885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba.
Paquete dev.bautista.spotlight.next.raycastpreview, code61>60. Configuración
principal, permisos, app/src y dependencias intactos. C2 no autorizado.
La autorización de este turno permite fijar el SHA tooling tras validarlo y
ejecutar sin otra consulta. Resultado final/source/workflow/run en nota de cierre.

Consulta automática Usage realizada en esta sesión:403 Resource not accessible
by integration. Se aplica la verificación propietaria vigente; ningún cambio de
presupuesto/secretos. El bloqueo de gasto de GitHub es la barrera externa efectiva;
el workflow no lo cambia ni usa runners pagos alternativos.

Se reutilizan78 casos nativos C1 y Lint0 errores/16 advertencias/1 información.
Se verifican sólo las guardas/artifactos/workflow nuevos localmente, sin claves
reales ni Android build previo. Prueba física y métricas siguen pendientes.
Ver [comparativas y protocolo A/B](NATIVE61-PREPARATION-20261104.md).
