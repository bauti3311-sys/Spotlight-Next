# Búsqueda minimalista aprobada · 18/10/2026

Base real226c8b1 (código Android751b2a1), misma rama y propietario de reserva recuperados. El helper Git de status no obtiene autenticación CLI; HEAD y propietario se verificaron mediante GitHub Git Data y la reserva propia se renovó con comparación del SHA esperado, sin force. No se reclama ninguna reserva ajena.

## Implementación en curso

CompactResults es una política sobre los resultados del SearchEngine existente, no un proveedor ni ejecutor. Calcula elegibilidad léxica e intención local por candidato; aplica un máximo de tres solamente después de descartar ruido. Aplicación exacta/prefijo precede a acciones reconocidas y datos indexados. No rellena posiciones vacías. No oculta OCR/semántica por ausencia de términos en el nombre del archivo. No transmite contenido ni concede autorizaciones.

La vista ampliada conserva todas las acciones distintas de la búsqueda original, incluidas las secundarias; sin volver a consultar proveedores ni cambiar el texto. Ver más/menos y Ctrl+M mantienen foco e identidad. Si se contrae con un resultado secundario seleccionado, este sustituye la última plaza de la vista compacta. Una nueva consulta restablece el estado compacto. Escape contrae tras cerrar capas superiores.

Tab autocompleta únicamente si el editor posee foco, sugerencia y cursor al final, sin selección ni composición IME; los demás casos conservan acciones contextuales. Ctrl+Space sigue disponible. Flechas horizontales/Home/End pertenecen al editor nativo; la cancelación del salto lateral de foco se conserva. El desplazamiento de lista con teclado es inmediato. La autorización de cambios de Tab proviene de la instrucción18/10 y queda documentada en AGENTS.

Filas sin subtítulo «Abrir aplicación» ni etiquetas laterales repetidas. Se mantienen títulos, subtítulos útiles, iconos reales, reproducción observada, favorito, gris de selección y acciones accesibles. El material de resultados eleva su alfa mínimo a.96; Ajustes/inspectores conservan su material anterior. La altura usa la densidad de filas real en lugar de estimar70dp por resultado.

## Verificación

72casos únicos dirigidos aprobados en rondas; no se suman reejecuciones. Última ronda72tuvo un fallo exclusivamente por selector de geometría fusionado; reintento del caso con Text real conserva aserciones y pasa. Comparación anterior capturada por harness externo recuperando los cuatro renderizadores de751b2a1; cinco imágenes Compose nuevas se guardan en visual/minimalist-search-20261018 sin sobrescribir referencias.183entradas de producción coinciden con el snapshot. Gitleaks del árbol sin hallazgos; sin dispositivoADB niKVM. Gates completos/lint/Review aún pendientes en este checkpoint dirigido. Ver verification/minimalist-search-20261018. No se reutilizan como nuevos los67dirigidos/420/572/597históricos. SDK/JDK/Gradle temporales restaurados; ninguna llamada real a Gemini/proveedores, ninguna ejecución Actions ni clave nueva. Comparaciones nuevas se guardarán separadas de las referencias aprobadas.

Las40decisiones permanecen parciales. Siguiente paso después de verificar este bloque: continuar capacidades reales de memoria/contexto e integraciones con pruebas dirigidas, preservando proveedor explícito, permisos, conversaciones, OCR, semántica y Ajustes aprobados. No se publica una APK en cada microcambio.
