# Evaluación semántica local · 17/10/2026

Base be3d002, rama propia feat/work-integral-settings-20261016. Reserva remota propia comprobada por Git Data/CAS; la CLI autonomy_lock status continúa sin autenticar ls-remote. No se reclama otra reserva ni se modifica main.

| Candidato real | Licencia / almacenamiento | Decisión inicial |
|---|---|---|
| DistilUSE multilingual cased v2, ONNX qint8 ARM64 | Apache 2.0; 135.336.307 bytes ONNX +995.526 vocabulario +1.575.104 proyección; 137.906.937 bytes en total | Elegido para integrar: español, seis capas, WordPiece verificable sin otra biblioteca nativa de tokenización. 128 tokens; media +Dense/Tanh; 512 dimensiones. |
| paraphrase-multilingual-MiniLM-L12-v2, qint8 ARM64 | Apache 2.0; 118.412.398 bytes ONNX, tokenización XLM-R/SentencePiece adicional | Alternativa válida; requiere un tokenizador distinto o más componentes nativos. |
| multilingual-e5-small | MIT; 118 MB cuantizado publicado para AVX512/VNNI, tokenizer JSON17,1MB/SentencePiece5,07MB | No reutilizar como optimización ARM sin verificar exportación/operadores y tokenización. |
| MediaPipe USE / EmbeddingGemma300m | API Android real; USE recomendado pequeño, EmbeddingGemma ofrece multilingüe con términos Gemma | No asumir calidad española de USE ni aceptar silenciosamente los términos Gemma. No se integran por ahora. |

Modelo elegido: sentence-transformers/distiluse-base-multilingual-cased-v2, revisión inmutable bfe45d0732ca50787611c0fe107ba278c7f3f889. Hashes completos en verification/semantic-evaluation-20261017/artifacts.json. No se agregan pesos al repositorio ni se inflará cada APK con el modelo. Instalación optativa y explícita, local/noBackup, con tamaño/hash verificados; cero envío de documentos o consultas. La descarga del modelo es distinta de enviar datos a IA.

Motor candidato: ONNX Runtime Android1.23.2, MIT, versión fijada. Se descargó y examinó AAR: minSdk24, compatible estáticamente con min26; incluye ARM64. Bibliotecas ARM64 medidas con readelf: segmentos LOAD alineados a0x4000 (16KiB). libonnxruntime.so19.347.616 bytes y JNI100.648; AAR multiarquitectura30.241.654 bytes. Pendientes compilación/R8 y carga nativa instrumentada real. No se cambia el motor Gemini ni se añaden Extensions/NNAPI/GPU por suposición.

Se ejecutó el modelo ARM64-quantized (grafo portable) con ONNX Runtime1.23.2 CPU en Linux x86_64, dos hilos. Pooling y Dense usan pesos reales safetensors, no embeddings inventados. Ocho consultas españolas de paráfrasis contra ocho documentos sintéticos: recall@1=8/8. Es un smoke test pequeño, no benchmark general. Métricas exactas, ranking completo y tokens de referencia en verification/semantic-evaluation-20261017. RSS incluye Python, tokenizador y runtime: no atribuirlo solo al modelo ni trasladar tiempos a A9+. Los datos no son médicos reales ni consultas a un proveedor.

Reproducir: instalar onnxruntime1.23.2, tokenizers0.22.1 y safetensors0.6.2 en un entorno de laboratorio y ejecutar scripts/evaluate_semantic_model.py --model-dir DIRECTORIO_VERIFICADO --output-dir SALIDA. Dependencias de laboratorio fuera del APK.

Contrato de integración: búsqueda léxica inmediata primero; enriquecimiento semántico local cancelable después. Índice derivado acotado y versionado por modelo/texto/fuente; permisos/modo privado y huellas se vuelven a comprobar antes de guardar y devolver. Límites de memoria/batería/temperatura, cobertura parcial explícita y reanudación manual. Nunca relajar comillas, NOT u operadores estructurados para inventar coincidencias. Quick Look y Obsidian Glass intactos. Esta evaluación todavía NO significa integración Android terminada, rendimiento físico medido ni matriz40 completa.

Fuentes primarias consultadas:
- https://huggingface.co/sentence-transformers/distiluse-base-multilingual-cased-v2
- https://huggingface.co/sentence-transformers/distiluse-base-multilingual-cased-v2/tree/bfe45d0732ca50787611c0fe107ba278c7f3f889/onnx
- https://huggingface.co/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2
- https://huggingface.co/intfloat/multilingual-e5-small
- https://developers.google.com/edge/mediapipe/solutions/text/text_embedder
- https://onnxruntime.ai/docs/build/android.html
- https://github.com/microsoft/onnxruntime/releases/tag/v1.23.2
- https://repo.maven.apache.org/maven2/com/microsoft/onnxruntime/onnxruntime-android/1.23.2/
