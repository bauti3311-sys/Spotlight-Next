# Límites reales

La validación automática utiliza Android/Robolectric y GitHub Actions. No hay acceso físico a la Samsung Galaxy Tab A9+; fluidez con One UI, teclado Samsung, rotaciones rápidas y gesto lateral requieren comprobación física. Los tiempos del diagnóstico son observaciones locales, no un benchmark de esa tablet.

Glass no incluye desenfoque del fondo ni refracción. Se conserva la preferencia histórica de blur por compatibilidad, con superficie legible como alternativa. One Hand Operation+ es una app externa de Samsung: se configura el gesto y su acción compatible allí. Spotlight no instala un interceptor lateral ni inspecciona contenido global con accesibilidad; la posibilidad de seleccionar la actividad «Spotlight · Buscar» debe verificarse en la versión instalada de One Hand Operation+.

Android no ofrece permiso universal. Contactos, notificaciones, calendario, SAF/archivos y Shizuku son opcionales e independientes. No se puede buscar almacenamiento privado de WhatsApp u otras apps. Mensajes requieren destinatario, revisión y mecanismos realmente disponibles; abrir/buscar/preparar no equivale a enviar automáticamente.

Controles multimedia necesitan acceso a notificaciones y una sesión publicada que soporte la operación. Buscar música no reproduce automáticamente. Los ajustes de pantalla reversibles necesitan Shizuku conectado y autorización; valores demasiado bajos de brillo no se ejecutan.

TheSportsDB gratuito tiene restricciones de búsquedas/listas y un encuentro por consulta de equipo, con limitaciones fuera de casa. La clave propia puede ampliar cobertura según el plan. No se implementan cronologías ni garantía de marcadores en vivo; ausencia de datos nunca se interpreta como ausencia de encuentros. Fuentes: https://www.thesportsdb.com/documentation y https://www.thesportsdb.com/api.php .

IA remota exige proveedor configurado y conexión, solamente después de una acción explícita. Extracción de imagen/documento depende de formato, modelos y permisos existentes. Los contextos compartidos se limitan a 20 MB por archivo; el texto enviado se revisa y está acotado. Copias locales y contextos fijados consumen almacenamiento hasta que se quitan.

Los planes locales son tipos registrados; peticiones complejas no autorizan ejecución libre de comandos. La IA solo puede proponer herramientas registradas y disponibles; operaciones sensibles requieren confirmación.
