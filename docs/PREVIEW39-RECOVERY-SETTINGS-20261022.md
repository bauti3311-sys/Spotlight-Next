# Spotlight Next · Preview39 · 22/10/2026

Código APK/ZIP: `6b433ff328df57ed28183bcb7dc977e5e1f97ab9`. Rama `feat/raycast-sidebar-permissions-20261020`; PR18 borrador. Propietario conservado `work-master-recovery-20261022-cc97a1bd`. Publicación por CAS/avance normal; sin Actions, main, force ni clave nueva.

## Implementación
C2: descriptores SAF guardables sin Intent vivo, extras, grants ni texto extraído; página y expansión conservadas, ámbito de búsqueda y revisión sensible guardables. Tarjetas conservadas como información recuperada, sin refresco remoto por restaurar. Respuestas asíncronas se vinculan a consulta/ámbito/generación; se cancela el trabajo al disponer el panel. El documento se reabre para validar acceso antes de presentar contenido, después de volver de pausa y de recuperar foco de ventana; la segunda señal cubre LifecycleService, que no recibe Activity.onResume. Al quedar inactivo se retira el hijo de PDF/audio/vídeo para disponer sus recursos. No se ejecuta una acción externa al restaurar.

C3: colección completa de hasta20temas visuales validada/canónica en respaldo version1; rechazo de identificadores duplicados, tipos incorrectos y campos privados. Importación en la misma transacción DataStore existente; error revierte toda la operación, respaldos antiguos sin colección conservan los temas actuales. Alias y quicklinks acompañan el roundtrip; atajos, planes y los demás campos portables mantienen sus validadores existentes. No se exportan grants SAF, credenciales, endpoint, consentimientos ni historial privado. La confirmación de importación ahora incluye el número de temas.

Pulido: acciones de tarjeta usan FlowRow y objetivos de48dp para permitir varias líneas en teléfono/fuente grande. Sin nuevo inspector, motor, permiso, dependencia, blur o rediseño general.

## Verificación real
29casos únicos aprobados en rondas dirigidas: 11nuevos (7recursos/contexto y4temas) más18regresiones relacionadas. No se relanzó la batería histórica completa de38casos. XML/logs y deduplicación en tests.json. Nueva restauración de documento/página/tarjeta mediante ActivityScenario.recreate; pruebas de revocación por lifecycle y foco de ventana, ausencia de extras/grants/ejecución automática, confirmación sensible invalidada y tarjeta estrecha con fuente130%. El proveedor documental de prueba es text/plain: conservar la posición de página en SavedState no equivale a renderizar un PDF real ni a instrumentar PdfRenderer. Regresiones de formulario/IA, inspector fijable, liberación del callback multimedia, configuración portable y ruta de Ajustes.

Los primeros fallos de fixtures están conservados: alias de quicklink en mayúscula, sustitución sobre JSON con barras escapadas, selector de árbol fusionado y acceso a un helper privado de otro archivo. Una ronda de StateRestorationTester produjo CalledFromWrongThreadException/SlotTable dentro del recompositor de pruebas al combinar restauración y lectura asíncrona; el caso conserva sus aserciones y ahora utiliza una Activity real de prueba y ActivityScenario.recreate. No se presenta ese cambio de fixture como una corrección probada de Android en producción.

Lint: {'Fatal': 0, 'Error': 0, 'Warning': 13, 'Information': 1}. Gate local JDK17/Gradle8.10.2/SDK35, assembleRaycastPreview correcto; comandos completos y duraciones en logs. No se ejecutaron servicios reales de Spotify/Gemini, OneUI, Motorola, SAF externo ni instrumentación física. La prueba de documento usa un ContentProvider local con revocación simulada.

## Identidad y archivos
Paquete `dev.bautista.spotlight.next.raycastpreview`, versionCode39, min26/target35, ARM64; certificado histórico `885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba`, firmaV2 y zipalign16KB correctos. Preview derivada de debug; no Release/R8 ni actualización del paquete principal. 204inputs productivos registrados, 1238blobs del ZIP cotejados con Git, ocho librerías ARM64 coinciden con AARs y DEX válidos.

- Spotlight-Next-Raycast-Preview-39-6b433ff.apk: SHA256 `ff4c8986ababab3d2bbea589a51343b9fa08d26ac002c0a04a4d71633f300a9d`.
- Spotlight-Next-source-6b433ff.zip: SHA256 `e881c41e87c4eb3799bbbfdb48100f5501afa258db099d11e4d2b9d2a2621270`.

## Límites pendientes
C2 y C3 siguen parciales. El overlay aún llama a saved.performRestore(null) y no persiste su registro de servicio: los nuevos savers permiten reconstrucción con SavedState y las pruebas de Activity, pero NO garantizan recuperación del overlay tras una muerte abrupta del proceso o un cierre sin snapshot. Las operaciones externas mantienen las salvaguardas de Preview38; jamás se presume su resultado ni se reanuda su ejecución automáticamente. Aún falta integrar recuperación durable del servicio sobre el almacenamiento existente, invalidar todas las variantes de contenido local, y probar revocaciones/proveedores y actualización real en hardware. No se garantiza restaurar un recurso cuyo acceso Android haya desaparecido. Tampoco se certifican TalkBack/IME/rotación física ni latencia/RAM.

Las capturas de documento/tarjeta proceden de este código de prueba. Las capturas completas de formularios/Ajustes proceden de7c25d6a; las modificaciones posteriores no cambian su geometría. captures.json precisa cada procedencia. La escena Avanzado muestra prioridades, no el diálogo de respaldo. No se declara nueva fidelidad visual global ni validación de transparencia entre ventanas.

Siguiente bloque: recuperación durable y acotada del estado del OverlayService sin restaurar autorizaciones/efectos, seguido de pruebas SAF/importación mediante proveedor y de actualización física. No repetir estas29pruebas si el cambio no las afecta.
