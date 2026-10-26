# C5 · candidata integral · validación física pendiente

Esta matriz no certifica dispositivos. ADB no detectó equipos durante la continuación. Robolectric/GraphicsMode.NATIVE compone Android real en JVM con aplicaciones, permisos, MediaSession y proveedores de prueba; no equivale a One UI, Motorola, emulador completo ni servicios externos reales.

|Área|Procedimiento|Resultado requerido|Galaxy Tab A9+ / One UI|Motorola G86|
|---|---|---|---|---|
|Actualización|Instalar encima de la Preview existente, sin desinstalar|Mismo certificado/paquete; preferencias, temas y datos conservados|Pendiente|Pendiente|
|Búsqueda|Vacía; `spot`, `whats`, `webOS Dev Manager`, consulta sin coincidencias|≤3 sugerencias/≤1 app; exacta/prefijo relevantes; sin resultados falsos ni IA por tecla|Pendiente|Pendiente|
|Teclado|↑↓, izquierda/derecha, Tab/Shift+Tab, Enter, Ctrl+K/M, Escape|Editor conserva foco/contexto; selección estable; Enter ejecuta solo seleccionado|Pendiente|Pendiente|
|ActionPanel|Abrir/cerrar, filtrar, submenú, scroll, tocar fuera, IME|Panel sobrepuesto sin mover resultados ni interceptar superficies ajenas|Pendiente|Pendiente|
|PDF|Seleccionar, deslizar lento/rápido/cancelar, largo, recorrer PDF largo|No abre externo al scroll; Quick Look local; inercia y acciones correctas|Pendiente|Pendiente|
|Adaptación|Rotar con teclado abierto, ancho compacto/amplio, fuente130–150%|Sin clipping ni controles inaccesibles; consultas y borradores conservados|Pendiente|Pendiente|
|Recuperación|Escribir argumentos/plan; retirar proceso de laboratorio y volver a invocar|Recupera último snapshot disponible; tareas inciertas sin replay; nueva confirmación sensible|Pendiente|Pendiente|
|Privacidad|Activar privado con overlay cerrado, reabrir; revisar borrado explícito|No reaparece sesión efímera; conversaciones guardadas no se borran por accidente|Pendiente|Pendiente|
|Recursos|Revocar carpeta/contactos/notificaciones mientras inspector está abierto y volver|Revalidación auténtica; error recuperable; sin acceso/grant inventado|Pendiente|Pendiente|
|MediaSession|Spotify autorizado y otro reproductor simultáneo; cambiar sesión/app|Metadatos/portada/progreso/controles de la app seleccionada; sin fallback al otro player|Pendiente|Pendiente|
|Gemini|Clave propia autorizada, streaming/cancelar/seguimiento, cambiar destino|Revisión de contexto; no envía al escribir/restaurar; errores/cuota auténticos|Pendiente|Pendiente|
|Phone Use|Activar servicio opcional; revisar y cancelar pasos autorizados|Respeta alcance/confirmaciones; informa solicitado frente a efecto verificado|Pendiente|Pendiente|
|Apps nativas|WhatsApp normal, Gmail, Chrome y acciones de documentos|Abre borrador/destino correcto, no afirma envío/completitud externa no observada|Pendiente|Pendiente|
|Respaldo SAF|Exportar a proveedor local, modificar ajustes, importar/revisar/confirmar; cancelar y archivo inválido|Roundtrip transaccional de temas/alias/comandos/atajos; sin claves, grants ni historial en archivo; fallo preserva configuración|Pendiente|Pendiente|
|Glass/accesibilidad|Fondo claro/oscuro, blur deshabilitado, contraste/lectura/TalkBack|Roles84/92/94 ajustables, gris de selección, fallback legible y objetivos48dp|Pendiente|Pendiente|
|Rendimiento|Arranque frío/caliente, consultas e índice autorizado, desplazamiento prolongado|Medir con trazas Android; registrar p50/p95, memoria/jank y batería reales; sin porcentajes inventados|Pendiente|Pendiente|

No se recomienda desinstalar la aplicación para resolver una firma. Una operación externa interrumpida permanece incierta hasta verificarla; restaurar nunca es autorización para repetirla. La recuperación periódica puede perder las últimas ediciones no guardadas ante terminación abrupta; los efectos externos tienen una barrera durable previa específica.

AppFunctions permanece condicionado a APIs, permisos y apps compatibles; el reporte de prerrequisitos existente no constituye ejecución universal. El índice/modelo semántico opcional, SAF de terceros, OCR y formatos complejos necesitan muestras autorizadas. No se descargaron modelos ni se concedieron permisos en los dispositivos del usuario.
