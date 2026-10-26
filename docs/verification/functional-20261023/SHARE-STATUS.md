# D32 · checkpoint PARCIAL en verificación

ShareContextActivity reutiliza un lector único y acotado de ACTION_SEND/ACTION_SEND_MULTIPLE. Se incluyen URIs ClipData, deduplicación estable, rechazo de file/http/malformados, límite12 y texto16k sin coerceToText ni dereferencia durante inspección. La ruta anterior leía EXTRA_STREAM como Uri antes del ArrayList y omitía ClipData. Se conservan extracción/copia privada20MB, cuota60MB y contexto existentes.

8casos nuevos SharedInputTest y un único caso de regresión táctil del bloqueD27 en ejecución. No se declaran verdes aún. Parser probado no equivale a proveedor/SAF/grants reales. Sin permisos nuevos, vigilancia de portapapeles, red/IA automática, motor o formato persistente nuevo. D32 continúa parcial hasta resultados y revalidación física.
