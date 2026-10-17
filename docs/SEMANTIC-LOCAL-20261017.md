# Búsqueda documental por significado · 17/10/2026

Continuación de be3d002, rama feat/work-integral-settings-20261016. Evaluación previa publicada en c1f7248 y SEMANTIC-MODEL-EVAL-20261017.md. Reserva propia comprobada por Git Data/CAS; CLI ls-remote sigue sin credencial. No se reclama otra reserva.

## Recorrido de producción

SearchEngine publica la búsqueda habitual primero y después enriquece la misma lista con FileIndex.semanticSearch. Scope documental y búsqueda general comparten este recorrido; fotos y consultas vacías conservan su comportamiento. Fusión por identidad, coincidencia literal del mismo archivo preferida, aplicaciones exactas/prefijo/parcial por delante de los fragmentos aproximados. No cambia SpotlightPanel, geometría, Quick Look, teclado ni el motor remoto.

Modelo real DistilUSE multilingüe cased v2, revisión y tres SHA256 fijados. ONNX Runtime Android1.23.2, WordPiece cased comprobado, media768→Dense aprendido512/Tanh→normalización. Sin tokenizador nativo adicional ni pesos en Git/APK. Licencias MIT/Apache incluidas. Ajustes añade controles funcionales al área documental existente: descarga explícita138MB, verificación de tamaño/hash, habilitación separada desactivada por defecto, preparación/cancelación, eliminación confirmada solo del modelo/vectores. La descarga solicita archivos públicos al alojamiento; no envía documentos, consultas ni embeddings. Instalación incompleta/cancelada no se activa. No se aceptan modelos arbitrarios.

SQLite v2→v3 aditivo conserva texto/extracciones/permiso fuente. Vectores por URI/modelo/SHA/texto/fragmento; offsets y procedencia de texto/OCR conservados. Se vuelven a comprobar permisos, preferencias, modo privado, huella y cancelación antes de guardar/devolver. Cambiar archivo o revocar fuente descarta el resultado tardío. Borrar vectores conserva el índice de texto y archivos.

Límites: 200documentos,1000vectores,8fragmentos/archivo,ventanas96tokens con solapamiento16, máximo128tokens incluido CLS/SEP; preparación60s cooperativos, inferencia1500ms/cancelación, un dueño nativo y una solicitud en espera. Estado parcial y reanudación manual sin repetir fragmentos válidos. Memoria libre mínima768MiB y doble del umbral Android, batería20% sin carga, ahorro, calor y estado desconocido difieren inferencia; no son medidas A9+. Motor se libera tras30s o invalidación; JNI se cierra únicamente al salir su propietario, sin liberar recursos durante run. No indexación automática silenciosa. Cobertura limitada al texto ya autorizado/indexado; no lee documentos completos por suposición. No hay garantía de preempción dura/RSS.

Comillas, operadores booleanos, paréntesis y filtros desconocidos impiden expansión semántica. Filtros admitidos de tipo, autor y fechas permanecen obligatorios. Similitud mínima0,28 y margen0,08 respecto al mejor: heurística de recuperación aproximada, no probabilidad ni garantía general.

## Evidencia nueva y límites

36 pruebas dirigidas/7suites, cero fallos/errores/omisiones, más compilación de instrumentación; inputs pendientes exactos en verification/semantic-evaluation-20261017/directed.json. Casos: política/Unicode/vectores, propiedad nativa y cancelación/cola, SQLite/migración, revocación SAF/cambios de fuente, preferencias/conservación, búsqueda real léxica→semántica. Fixtures de embeddings explícitos para probar autorización y flujo: no acreditan calidad ML.

Separadamente se ejecutaron las clases Kotlin de producción con ONNX nativo en Linux x86_64/Java17 y los pesos reales:16embeddings, tokens iguales a la referencia oficial, error máximo por componente5,16e-8 frente a Python (tolerancia1e-5),8/8paráfrasis españolas sintéticas. verification/semantic-evaluation-20261017/production-verification.json y scripts/probe_semantic_production.py reproducen el recorrido. Smoke test pequeño, no benchmark ni rendimiento ARM64/A9+. Los modelos y jars de laboratorio quedan fuera de Git/APK. La primera ronda compiló con un import de test incorrecto; se corrigió sin quitar casos. Una ronda de31precedió a la definitiva36; no sumar rondas.

**Pendiente:** gate completo Android/JVM, tres lint, Release/R8 y21comparaciones estrictas sobre checkpoint exacto. Instrumentación nativa/latencia/RAM/batería y calidad corpus real requieren entorno Android; compilar no equivale a ejecutar. Firma histórica/final sigue protegida; no nueva clave, APK parcial ni Actions. Las40decisiones siguen parciales.

## Siguiente bloque

Proteger consultas documentales avanzadas: SearchQuery.natural puede normalizar NOT/AND/OR tras prefijos PDF y alterar su sentido. Reproducir con prueba contra este código, corregir mínimamente y mantener consultas literales/filtros sin expansión semántica. Después gates completos del conjunto y continuidad de herramientas documentales/contexto autorizado, sin otro motor Gemini ni envío al escribir.
