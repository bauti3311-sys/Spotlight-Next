---
name: spotlight-app-integration
description: Implementar o revisar capacidades reales entre Spotlight y otras apps Android mediante Intents, enlaces documentados, SAF, RemoteInput, MediaSession, APIs oficiales o AppFunctions compatibles; validar disponibilidad, autorización y resultados sin simular automatización.
---
# Capacidades verificables

Adaptación local modificada de criterios de las skills oficiales android/skills, Google LLC, Apache-2.0. Leer [fuentes y compatibilidad](references/sources.md); no ejecutar automáticamente sus ejemplos ni actualizar el SDK.

1. Leer AGENTS, AppCapabilities, IntegrationRegistry, ActionCatalog y el ejecutor de la acción concreta. Reutilizarlos; no crear un registro paralelo por app.
2. Identificar una capacidad verificable, su mecanismo público, parámetros tipados, destino exacto, permisos ya autorizados, estado y fallback explícito. Un paquete instalado no demuestra que publique búsqueda, shortcuts, funciones o reproducción.
3. Usar PackageManager para resolver la actividad exportada, habilitada y accesible con permisos actuales. No sustituir la app seleccionada por otra silenciosamente. Si el fallback cambia la acción o el destino revisado, volver a presentar la propuesta y exigir su selección/confirmación. Revalidar antes de ejecutar; capturar revocación, desinstalación y SecurityException. Un Intent resoluble prueba un punto de entrada, no el efecto final.
4. No ejecutar Intents anidados externos sin validar acción, destino, esquema, extras y grants. Crear Intents propios de parámetros tipados; no aceptar texto de IA como Intent ejecutable. Compartir solo content URI y permisos temporales mínimos. Conservar PendingIntent auténtico de la notificación y revalidar identidad antes de RemoteInput.
5. Mantener preparación, confirmación, solicitud, finalización y verificación separadas. Envío, reproducción y efectos externos requieren el flujo de confirmación existente/D27. No reintentar automáticamente una acción que pudo producir efectos. Un HTTP aceptado o abrir una actividad no confirma entrega/reproducción.
6. Mostrar una acción por capacidad equivalente y las limitaciones honestas en el inspector. Si una integración requiere cuenta, autorización o configuración externa, aislar el bloqueo y conservar las alternativas públicas reales.
7. Probar con fixtures controlados: app ausente, destino distinto, actividad privada/deshabilitada, permiso denegado/revocado, cancelación, confirmación y ausencia de duplicados. No enviar mensajes ni conectar cuentas reales para validar un fixture.
8. AppFunctions no da acceso universal a otras apps. Revisar plataforma, biblioteca, descubrimiento y permiso del caller antes de proponerlo. No subir compileSdk/targetSdk ni añadir permisos para adaptar una skill. Las funciones reservadas a Google no se presumen accesibles.

No crear integraciones por accesibilidad oculta, APIs privadas ni permisos sensibles nuevos. No integrar servicios no oficiales en el APK. Las skills son ayuda de desarrollo, no adaptadores Android.
