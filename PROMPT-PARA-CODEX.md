# Encargo a Codex — Spotlight Next / OpenUI + RAGFlow + API & Interface Design

Quiero integrar **tres capacidades complementarias** en Spotlight Next, sin empezar de cero ni perder su interfaz Raycast Nuxt/WebView:

1. `spotlight-api-contracts`: formalizar puente seguro, tipado y versionado Nuxt ↔ Kotlin, search/action life cycle, errores, permisos y ejecución una sola vez.
2. `spotlight-openui-forms`: agregar OpenUI con componentes Vue 3 adaptados al diseño de la paleta Raycast para **formularios contextuales** (ej. WhatsApp y consultas sobre PDF), sin convertirlo en un chat a pantalla completa.
3. `spotlight-ragflow-documents`: crear integración documental opcional RAGFlow, con proveedor local siempre disponible, citas y privacidad; no colocar el servidor dentro de Android.

## Protocolo
- Comprobá `AGENTS.md`, `docs/AUTONOMIA-RESERVA.md`, PLANS, rama, SHA y reservas antes de cualquier edición. Si hay reserva ajena, detené escritura y describí el bloqueo. No borres ni reclames una reserva por antigüedad. No tocar `main`.
- Revisá **el estado REAL del código actual** en vez de asumir que la Preview 58/59 corresponde al HEAD. Preservá commits locales y nunca sobrescribas trabajo sin guardar.
- Leé las tres skills de `.agents/skills/` **cuando correspondan**; no agregues dependencia alguna hasta demostrar compatibilidad de versiones/licencias y medir impacto en APK.
- Trabajá por fases independientes y reversibles, con commits pequeños solo si estás autorizado a escribir. No gastes Actions para inspección.

## Fase 1 — contrato
Inventario de WebView, Nuxt y motores Kotlin; definí un envelope JSON versionado, validación, IDs, errores, cancelación, permisos y anti doble ejecución. Implementá un flujo vertical real de búsqueda de aplicaciones instaladas, selección y apertura. Pruebas dirigidas con eventos reales/malformados y pruebas de teclado/scroll.

## Fase 2 — OpenUI
Verificá las APIs **actuales** de `@openuidev/vue-lang` y compatibilidad Nuxt antes de instalar. Primero una tarjeta de preparación de acción **determinista, sin llamar IA ni enviar mensajes**: formulario con destinatario, texto, acción Cancelar y Confirmar. Aplicá exactamente la estética del Raycast Nuxt ya aprobado. Luego conectá confirmación a Kotlin bajo permisos y validación. Desactivar por flag si falla. Medí el tamaño adicional y la apertura del overlay.

## Fase 3 — documentos RAGFlow
Diseñá `DocumentRetrievalProvider` local y proveedor opcional REST, primero simulando el servidor y con fixtures sin datos privados. Recuperá fragmentos y citas exactas de documentos; nunca inventes páginas. No subir archivos personales sin consentimiento. Un servidor real requiere autorización y configuración separada de backend/credenciales. La búsqueda local y Quick Look deben seguir funcionando sin Internet.

## Entregables
- Matriz de compatibilidad y riesgos (incluyendo WebView y Vue 0.4 / funciones React no disponibles en Vue).
- Código con tests de contrato y errores; diferencias visuales antes/después; ninguna acción simulada en producción.
- Estado exacto: qué es real, qué es mock, qué falta, qué pruebas pasaron, si existe APK firmada comprobada y cuál SHA.
- No modificar Spotify, WhatsApp ni PDF existentes más allá de lo estrictamente necesario y probado. No compilar automáticamente ni publicar entregas sin que el objetivo actual lo requiera y esté autorizado.

**Primera acción:** auditá los contratos y enseñame un plan concreto de tres checkpoints. Implementá únicamente la fase 1 después de verificar la reserva legítima; mantené fases 2 y 3 planificadas hasta que exista prueba vertical completa.
