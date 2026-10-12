# Verificación de 2.0

Comandos de CI: testDebugUnitTest, lintRelease, assembleRelease con R8/resource shrink, cinco pruebas del backend y pruebas del verificador de firma. No se declara un resultado hasta que el workflow concluye.

Glass20CoreTest ejecuta contratos en API 26 y 35: cantidades/verbos, plan inválido, contenido literal, inicio vacío, exactitud sobre hábitos y archivos, selección estable, transferencia exclusivamente visual, privacidad sin destrucción, TTL/fijados y restricción de favoritos deportivos.

Glass20VisualTest utiliza gráficos nativos de Robolectric: calculadora real en las tres familias, opciones integradas en teléfono con Escape conservando consulta, onboarding deportivo sin datos ficticios. Las capturas se publican con los reportes de CI; no representan capturas físicas de One UI.

Las pruebas históricas se conservan. Se actualizan las expectativas de relleno de inicio eliminado, nombres de ajustes, resultados adicionales de intención y proveedor deportivo dirigido; se mantienen comprobaciones de efectos secundarios, revisión antes de red, navegabilidad y datos guardados.

## Matriz física pendiente

1. Instalar release sobre 1.3.1: datos/preferencias/SAF/favoritos/clave IA conservados.
2. Abrir fría/caliente mediante toque, tile y teclado; consultar diagnóstico. No debe aparecer fondo rectangular ni cierres al abrir opciones.
3. Rotar con IME abierto/cerrado; editar consulta, abrir panel y volver; teclado físico con Tab/Enter/Escape y atajos existentes.
4. Buscar WhatsApp con miles de archivos asociados: app/acciones primero, archivos limitados; contactos duplicados se eligen por dirección.
5. Privacidad activada: recientes, captura y hábitos no aumentan; locales siguen funcionando sin conexión.
6. Apps ausentes y permisos denegados: requisito útil, sin resultados ficticios ni envío accidental.
7. Compartir documento, quitar temporal, fijar/reabrir, eliminar; enviar a IA solo tras revisar.
8. Favorito deportivo real, resultados de otro equipo excluidos, sin datos/errores/caché antigua claramente identificados.

Estado: verificaciones automáticas en curso; pendientes revisión de capturas y release firmado.


## Cierre de entrega, 12 de octubre de 2026

La ejecución final previa 38263129913 y el Release 38264201437 completaron 271 pruebas Android con cero fallos/errores. Lint Debug y Lint Release pasaron; backend 5 y verificador 10 pasaron. El Release optimizado fue firmado y verificado con apksigner y zipalign, conservando certificado y applicationId. Detalles y hash del APK en ENTREGA.md.

La regresión de navegación se comprobó manteniendo resultados al mover solo el cursor, eligiendo tipos de archivo sin permisos opcionales, abriendo PDF y regresando al menú. El toque bloquea el orden sin activar el desplazamiento destinado al teclado. No hubo acceso físico a la Tab A9+.
