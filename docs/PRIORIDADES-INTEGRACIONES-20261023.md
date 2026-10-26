# Continuidad aprobada: integraciones, Chromecast y las 40 decisiones

Ampliación del propietario del 23/10/2026. Complementa el plan anterior y la especificación maestra; no reemplaza los errores pendientes ni sus requisitos de validación.

## Orden de ejecución
1. Validar y cerrar ambos errores de 08f19055: altura/scroll/footer/IME del inspector y aviso de inicio en Guardados. Ejecutar primero ContextSplitHeightTest, ProductEntryPointsTest, MediaInspectorFlowTest y SpotlightPanelLayoutTest; separar Robolectric de comprobación física.
2. D28: parámetros tipados generales, plantillas reutilizables, edición, conflictos, persistencia, confirmaciones y recuperación mediante D27.
3. D26: registro modular e inspectores por capacidades reales.
4. Spotify: Search y reproducción integrada.
5. Google Cast y control remoto del Chromecast con Google TV HD.
6. D35, D36, D37 y las 25 decisiones con validación insuficiente.
7. Continuar las 40 decisiones hasta cumplir cada contrato con evidencia.

## Integraciones por capacidades
Reutilizar Intents, Deep Links, Shortcuts, MediaSession, APIs oficiales y AppFunctions solo donde exista soporte comprobado. Declarar disponibilidad, parámetros, permisos, confirmación, ejecución, estado, resultado observable y fallback. El inspector Spotify es el modelo de experiencia, manteniendo Solid Graphite, Icons8 iOS Style e iconos originales.

- Spotify: buscar canciones/artistas/álbumes/playlists dentro de Spotlight y solicitar reproducción controlando el cliente. Investigar Web API, App Remote y OAuth PKCE con requisitos vigentes. Sin secretos en APK ni supuesta reproducción validada.
- WhatsApp: contactos autorizados, mensajes y adjuntos preparados/revisados; distinguir apertura/solicitud de envío confirmado.
- YouTube: búsqueda contextual, apertura y reproducción autorizadas, Cast cuando esté disponible.
- Gmail: búsqueda y redacción autorizadas; API oficial solo configurada y consentida.
- Chrome/Vivaldi: búsqueda, URLs, compartir y APIs/shortcuts publicados.
- Documentos: Quick Look, búsqueda por contenido, acciones contextuales y transmisión compatible.

## Chromecast con Google TV HD
Priorizar Google Cast Android Sender SDK: descubrimiento local cuando la función esté habilitada, selección, conexión/desconexión, video/audio/imágenes/URLs compatibles, reproducción, posición, volumen y estado real en inspector contextual.

Para SAF evaluar servidor HTTP temporal del archivo autorizado: acceso limitado, expiración, cierre, soporte Range y protección de rutas. Sin servicios permanentes innecesarios. Sin DRM ni accesos privados eludidos; Cast no es duplicación completa de pantalla.

Remote Service v2 es experimental hasta verificar compatibilidad y seguridad. Evaluar PIN, D-pad/OK/Atrás/Inicio, teclado, multimedia y volumen soportados; credenciales de vinculación en almacenamiento seguro. No exigir ADB ni root.

## Búsqueda universal y ejecución
Objetivos: «Buscar Cerati en Spotify», «Reproducir Bad Habit», «Mandar WhatsApp a Juan», «Transmitir este video al Chromecast», «Pausar Chromecast», «Abrir control remoto de la TV». Mostrar/resolver cada comando solo con capacidad y autorización reales, sin intenciones ficticias. D27 conserva planes, confirmaciones por acción, cancelación, recuperación sin duplicados y estados PREPARED/REQUESTED/COMPLETED/VERIFIED.

## Continuidad y límites
Una configuración externa pendiente de Spotify o Chromecast no bloquea D28 ni las otras decisiones independientes una vez validado el bloque de errores. No declarar conexión, reproducción ni validación física a partir de fixtures.

Commits pequeños y pruebas dirigidas. Mantener PR18 en borrador, rama actual, reserva legítima, firmas/paquetes/datos y Preview44/45. Sin force/main/merge, nuevos permisos sensibles sin autorización ni Actions sin necesidad/cuota comprobada. Compilar Preview independiente solo en un hito estable, con fuente del mismo commit y firma/hashes comprobados.

Estado al incorporar esta ampliación: los errores de 08f19055 siguen pendientes de ejecución; el runtime Android anterior sigue ausente. No se repitieron instalaciones ni compilaciones. Matriz sin cambios: 9 completas, 25 con pruebas insuficientes, 6 parciales. Esta incorporación de alcance no acredita nueva funcionalidad implementada.
