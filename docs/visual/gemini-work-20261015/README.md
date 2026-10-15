# Fidelidad Gemini — consolidación15/10/2026

Referencia analizada: ventana central del JPG adjunto, `(574,140)-(1287,700)`,713×560px dentro de1536×960. El zoom/DPR del navegador es desconocido: no asumir1px=1dp. La prueba692dp incluye16dp exteriores por lado, superficie660dp. Se preservan ancho único y preferencias existentes en el dispositivo.

La barra56dp, los tres temas y la selección gris ya existían en0a35569: se conservaron. Se implementaron las tres secciones reales del inicio, encabezados11sp/15sp, primer margen7dp, filas compactas en línea sin chevron en tablet, metadatos singulares y PDF azul26dp. Cápsula visible32dp dentro de botones48dp y margen vertical1dp; conserva Enter/Tab/Escape en lugar de anunciar otro atajo solo para imitar la imagen. Los objetivos táctiles introducen una diferencia de altura respecto del prototipo de escritorio. No se declara fidelidad física/píxel absoluta.

GeminiHomeCaptureTest genera el inicio de producción pasando por SearchEngine/FileIndex, con5 paquetes fixture de PackageManager y2 PDF locales fixture reales. No hay un catálogo estático de Spotify/WhatsApp/Chrome en producción. Las apps/iconos/documentos de un usuario dependen de lo instalado y autorizado. Indicador de reproducción solo para una sesión autorizada STATE_PLAYING. Si no hay documentos, no se inventa una sección de archivos.

Se revisaron17 pares de baselines históricas: cambios esperados de barra, densidad, temas, selección y cápsula; Guardados conserva acciones; Apariencia conserva un ancho visible y añade3 temas. Las capturas de helpers de ancho heredados no son controles nuevos del producto. Falta aceptar/reverificar las baselines del último margen: el entorno se desconectó antes del cierre. No se modificaron tolerancias ni se ocultaron zonas para obtener verde.

Capturas generadas con Compose de producción, Robolectric4.14.1/API33 y gráficos nativos; no capturas Samsung/emulador ni mockups. La captura del inicio verificada mostraba los7 resultados completos y cápsula sin solaparlos. PdfRenderer no funciona de forma utilizable en el host: el error de Quick Look capturado no es una página validada. Blur, frío/IME y One UI requieren evaluación física. Las capturas nuevas no pudieron publicarse ni respaldarse tras409 environment_offline; no se incluyen PNG falsos o enlaces a archivos ausentes.

El ZIP revisado de Gemini se comparó con el árbol exacto0a35569:17 archivos distintos. Se tomaron ideas léxicas/home, verificadas con regresiones nuevas. Se descartaron sustituciones masivas de pruebas, resolveConfig sin efecto, consulta de uso global sin un contrato de permiso comprobado y bonos que no garantizan precedencia de apps exactas. Se mantuvo el parser de archivos/AND/OR/fechas existente. No se adoptaron consultas al vault al escribir ni cambios implícitos de proveedor; la configuración/consulta de modelos es explícita.

Estado y comandos de recuperación: `docs/HANDOFF-WORK-20261015.md`.
