# Auditoría de la base 1.3.1

Base: e7c50bdf975062708eb59fceb40088bfdf9561b8, applicationId dev.bautista.spotlight.next, versión 1.3.1/26. Inspección de los 178 archivos de texto del árbol, con lectura detallada de los motores, ventana, preferencias, controles, proveedores y workflows. No hubo acceso físico a la Tab A9+.

## Componentes que se conservan

- Índice SQLite de archivos y búsqueda SAF/almacenamiento compartido, operadores y metadatos. La indexación y extracciones usan coroutines; no se migra ni destruye search-index.db.
- Contactos por identificador/dirección, calendario autorizado, intents y borradores con revisión. No se promete envío autónomo ni control interno de apps.
- Calculadora, conversiones, herramientas de texto, notas, fragmentos, rutinas y proveedores OpenAI/Gemini/backend opcional.
- CredentialVault cifra claves mediante AES-GCM/Android Keystore en noBackupFilesDir. ConfigTransfer excluye claves y permisos.
- ToolRegistry autoriza argumentos y clasifica riesgos; ToolExecutor comprueba extensiones, permisos y conexión Shizuku. Los ajustes reversibles conservan un diario.
- OverlayService TYPE_APPLICATION_OVERLAY, dueños Lifecycle/SavedState/ViewModel en el host, opciones dentro del mismo host y cierre por toque exterior. No se vuelve a cerrar por pérdidas transitorias de foco.
- Workflows de GitHub con firma histórica y bloqueo de Secrets en PR/forks. La base anterior pasó pruebas, Lint y firma en CI; eso no demuestra la experiencia física.

## Defectos y límites comprobados

- SearchEngine: 728 líneas; SpotlightPanel: 1082. Las devoluciones tempranas de intención, planes y extensiones evitan mezclar alternativas. Los resultados pueden cambiar de posición durante publicaciones de fuentes.
- HomeSuggestions mezcla favoritos, hábitos y relleno genérico. No limita a una app. El inicio ofrece Crear nota/Explorar comandos aun sin contexto.
- LocalPlan solo reconoce brillo, volumen y abrir con cifras y un conector limitado. InterpretationPanel permite omitir, pero no editar/reordenar/guardar el plan.
- Ajustes: trece grupos, pestañas/desplegable en vertical, sin búsqueda global. El menú rápido sigue centrado en filtros, no en apariencia/privacidad.
- Captura y aprendizaje no tienen un interruptor privado común. ToolAudit también escribe eventos fuera de ese control.
- Contexto seleccionado/compartido solo existe dentro de ProcessTextActivity; no hay contexto temporal explícito entre aperturas ni fijado durable.
- NativeProviders.sports usa eventsday.php; SportsPanel filtra después una muestra global limitada y fija eventos, no equipos/ligas. No es un marcador en vivo personalizado.
- La barra fuerza IME cuando no hay teclado físico, sin distinguir invocación táctil. Falta instrumentación de presentación, foco y duración de búsquedas.
- Material visual con opacidad casi total; blur no desenfoca el fondo. One UI puede extender FLAG_BLUR_BEHIND a toda la huella rectangular. Se conservará un fallback legible sin refracción ni APIs ocultas.
- Atajo de accesibilidad no inspecciona ventanas. No se añadirá lectura global de pantalla para gestos o contexto. Preferir invocación compatible con One Hand Operation+ antes que interceptar Back con una gran ventana lateral.
- FileProvider declara root-path amplio para archivos compartidos actuales. No se ampliarán rutas ni se expondrán archivos privados sin una acción explícita.

## Datos y migración

Conservar DataStore spotlight, notas, favoritas/recientes, usageData, snippets, alias, rutinas, carpetas SAF, índice SQLite, extensiones, caché nativa, CredentialVault y diario Shizuku. Añadir campos con valores por defecto y leer versiones antiguas de exportación. Favoritos deportivos de eventos no se reinterpretan como equipos: usar un registro nuevo para entidades. Contextos/conversaciones son temporales salvo guardado explícito; diagnóstico y exportación de temas excluyen contenido y secretos.

## Permisos

Superposición obligatoria para la barra; contactos/calendario/notificaciones, accesibilidad, almacenamiento y Shizuku opcionales y solicitados al ejecutar la función. SAF permite documentos autorizados sin MANAGE_EXTERNAL_STORAGE; ese permiso se conserva para la función amplia existente. Sin root, no existe un permiso universal para controlar otras apps.

## Dependencias

Conservar Compose, DataStore, coroutines, OkHttp, DocumentFile, PDFBox/ML Kit y Shizuku por funciones existentes. Sin React, WebView, shaders animados o librerías nuevas para decorar. Inter y Lucide tienen licencias ya presentes; no incorporar símbolos propietarios de Apple. Backend continúa opcional.

## Implementación ordenada

1. Tokens Glass, seis grupos de ajustes y menú rápido integrado.
2. Privacidad persistente con puertas comunes; contexto autorizado y guardados explícitos; diagnóstico sin contenido.
3. Normalización lingüística, ranking/deduplicación, inicio sin relleno, selección estable y comandos universales.
4. Planes editables con comprobación de cada herramienta y estados parciales; integración de contexto/IA inline.
5. Deportes por entidades favoritas y consultas específicas, clima con caché y fecha; capacidades reales de apps.
6. Pruebas, capturas reales de entornos disponibles, matriz física pendiente, CI en rama antes de integrar y release 2.0.0/27 con identidad histórica.

## Estado

Auditoría completada; implementación en curso. Este documento no declara terminadas funciones de 2.0.
