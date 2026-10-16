# SPOTLIGHT NEXT — PLAN MAESTRO FUNCIONAL
**Versión:** 1.0 — 16/10/2026  
**Dispositivo de referencia:** Samsung Galaxy Tab A9+, Android 16 / One UI, teclado físico  
**Producto:** aplicación privada, local, sin root, Kotlin + Jetpack Compose  
**Aspecto aprobado e intocable salvo defectos:** Obsidian Glass  
**Repositorio privado:** `bauti3311-sys/Spotlight-Next`

## 1. Visión y contrato de producto
Una barra flotante universal, invocable globalmente por mecanismos compatibles con Android, que comprende intenciones, encuentra resultados y ejecuta acciones reales desde el mismo overlay. Primero interpreta localmente; invoca Gemini solo ante consultas complejas o acciones explícitas de IA. Su experiencia predeterminada es mínima y rápida; se expande a inspector o espacio de trabajo solo si agrega valor. El usuario controla contexto privado, permisos y acciones sensibles.

**Invariantes:** No rediseñar Obsidian Glass; no reinstaurar pestañas/categorías obligatorias, Tasker, snippets ni panel lateral vacío; no inventar integraciones ni datos en vivo; no enviar archivos, portapapeles o mensajes privados a proveedores externos sin consentimiento contextual; no efectuar envíos sensibles sin confirmación; no atribuir a la app acceso a privados de otras apps fuera de mecanismos Android autorizados; no interpretar modificación de archivo como fecha de apertura; no asociar tests sintéticos con validación física.

## 2. Las 40 decisiones de producto (aprobadas, NO equivalentes a implementadas)
| # | Decisión seleccionada | Requisito consolidado |
|---|---|---|
| 01 | B — Inteligencia universal híbrida | Motor local de intenciones rápido; Gemini para interpretación compleja. |
| 02 | B — Gemini contextual | IA invocada desde la búsqueda única, sin pestaña obligatoria. |
| 03 | C — Sugerencias predictivas | Aprendizaje local adaptativo, cero relleno alfabético, máximo una app inicial relevante. |
| 04 | C — Automatización adaptable | Acciones compuestas con riesgo, permisos y confirmación proporcional. |
| 05 | C — Integraciones Android | Intents reales, adaptadores verificados, Shizuku y Accesibilidad consentida cuando corresponda. |
| 06 | C — Búsqueda semántica | Coincidencia exacta, metadatos y recuperación local por significado de documentos autorizados. |
| 07 | C — Memoria contextual | Preferencias y continuaciones locales con caducidad, corrección y exclusiones. |
| 08 | C — Información personalizada | Clima/deportes/noticias/música con datos verificables y relevancia. |
| 09 | C — Inspector dinámico | Panel contextual solo donde aporte valor, con expansión gradual. |
| 10 | C — Descubrimiento de acciones | Acción principal clara, `···`, Alt+Enter; Tab autocompleta y Esc retrocede. |
| 11 | C — Espacio Gemini adaptable | Markdown, streaming, seguimiento, archivos autorizados y acciones compatibles. |
| 12 | C — Ranking único | Exactitud determinista, orden estable, sin duplicados ni reordenamiento disruptivo. |
| 13 | C — Rendimiento adaptativo | Overlay instantáneo y trabajo pesado diferido, con presupuesto de recursos. |
| 14 | C — Multimodalidad autorizada | Voz, imágenes y documentos con adquisición y envío explícitos. |
| 15 | C — Guardados inteligentes | Guardados, recientes y continuidad, sin interfaces paralelas. |
| 16 | C — Permisos y confirmación | Registro de capacidades y autorización por riesgo; consentimiento específico al enviar datos. |
| 17 | C — Tareas en segundo plano | Progreso, cancelación y recuperación con restricciones Android. |
| 18 | C — Recuperación de errores | Informar paso fallido, alternativas reales, sin reenvíos silenciosos. |
| 19 | C — Personalización progresiva | Preferencias mínimas; aprendizaje corregible por contexto. |
| 20 | C — Registro universal de capacidades | Fuente única de herramientas disponibles, requisitos, proveedor y resultados. |
| 21 | C — Resultados interactivos | Acciones inline de música, PDFs, deportes, mensajes; compacto → inspector → espacio de trabajo. |
| 22 | C — Lenguaje y alias personales | Lenguaje natural, alias y planes reutilizables con parámetros, sin flujos tipo Tasker. |
| 23 | C — Portapapeles contextual | Share/ProcessText y contexto voluntario; deduplicación de avisos, sin vigilancia permanente. |
| 24 | C — Conversaciones con continuidad | Hilos identificables, reanudación explícita y resúmenes privados opcionales. |
| 25 | C — Multiselección | Selección y operaciones en lote sobre archivos/resultados autorizados. |
| 26 | C — Autocompletado predictivo | Sugerencias estables; Tab acepta; no alterar el texto espontáneamente ni llamar IA por tecla. |
| 27 | C — Resolución de ambigüedad | Umbrales locales, alternativas y preguntas explícitas antes de acciones ambiguas/sensibles. |
| 28 | C — Resiliencia offline | Capacidades locales y tareas pendientes reanudables seguras, sin encolar envíos privados no autorizados. |
| 29 | C — Navegación contextual | Restaurar consulta, selección, scroll, documento, diálogo y tareas según relevancia. |
| 30 | C — Invocación universal | Atajo global factible en Android, Share/ProcessText, recuperación de foco y overlay sin pantalla completa. |
| 31 | C — Editor inteligente de acciones | Editar planes con lenguaje natural o controles; vista previa y confirmación. |
| 32 | C — Comunicaciones contextuales | Notificaciones autorizadas, RemoteInput compatible, borradores Gemini y confirmación de envíos. |
| 33 | C — Web inteligente verificable | Búsqueda/servicios, resultados con fuente, datos frescos, Gemini sobre evidencia y cruce con archivos. |
| 34 | C — Controles Android inteligentes | Brillo, audio, batería, paneles y Shizuku según capacidades efectivas; planes editables. |
| 35 | C — Archivos multimodales | Indexación incremental, Quick Look real, búsqueda semántica y OCR limitado/adaptativo. |
| 36 | C — Organización contextual | Contactos, calendario y recordatorios enlazables a documentos y planes, sin nueva app de tareas. |
| 37 | C — Respuestas instantáneas | Cálculos/conversiones offline, divisas con cotización fechada, continuación y copia. |
| 38 | C — Música inteligente | Sesiones MediaSession, proveedor preferido, búsquedas y acciones compuestas sin prometer reproducción no soportada. |
| 39 | C — Deportes inteligentes | Favoritos, entidades, múltiples fuentes si disponibles, actualización visible y alertas opcionales. |
| 40 | C — Clima inteligente | Pronósticos por horas, ubicación configurada/autorizada, respuestas prácticas, caché etiquetada y alertas opcionales. |

## 3. Estado comprobado del repositorio y límites de la auditoría
**Consulta de GitHub en esta conversación (16/10/2026).** `main` apunta a `47259ce2d7259f0ddfa5d85b7c73f07983d8cd6d`; la rama `feat/work-universal-intelligence-20261016` apunta a `fd3e418e2cd45dd9d5c01bf7ef398bb22b06a57c`, 108 commits por delante de `main` en la comparación consultada. No mezclar ambos puntos de referencia ni comenzar desde `main` por defecto. No se modificaron ramas ni archivos durante esta auditoría.

**Lo que registra el handoff de la rama Universal** (evidencia histórica de Work, no pruebas repetidas aquí): 432 pruebas Android locales correctas, 30 scripts Python y 5 pruebas backend correctas, lint de tres variantes sin errores, compilaciones Debug/Release/Universal Preview, 17 comparaciones visuales sin diferencias frente al checkpoint aprobado `3c3c68e`. El flujo de intenciones nuevo está conectado a `SearchEngine`; consulta explícita a Gemini y Markdown inline con pruebas de HTTP simulado; PDF local con extracción hasta 40 páginas o 24.000 caracteres y consentimiento de fragmento revisado; búsqueda y volumen de Spotify con herramientas nativas; sugerencias iniciales sin relleno alfabético.

**No validado en tablet ni en proveedor real:** instalación/permiso/foco e invocación fría en Galaxy Tab A9+ Android 16, tecla física, interacción de Quick Look con PDFs reales, transición entre Spotify y overlay, llamadas reales a Gemini usando clave y modelo del usuario, alcance real de clima y deportes y compatibilidad completa de los proveedores. Se documentó que **no hay** historial fidedigno de “archivo abierto ayer”, OCR universal, índice semántico real ni detección infalible de capítulos largos. Las decisiones aprobadas son requisitos futuros, no componentes instalados.

**Entrega de prueba:** `dev.bautista.spotlight.next.preview.universal`, versión de prueba independiente; **no** actualiza la app principal firmada históricamente ni comparte automáticamente sus permisos/credenciales. No mezclar esquema de firmas, borrar instalaciones o fusionar ramas para probarla.

Fuentes de auditoría en GitHub:
- `docs/UNIVERSAL-MATRIZ-20261016.md` de `feat/work-universal-intelligence-20261016`
- `docs/UNIVERSAL-INTEGRACION-20261016.md` de esa rama
- `docs/HANDOFF-UNIVERSAL-WORK-20261016.md` de esa rama
- `docs/TABLET-GEMINI-CHECKLIST.md` de esa rama
- `docs/DIAGNOSTICO-APERTURA-FRIA.md` de esa rama

## 4. Arquitectura destino: ocho módulos, no cuarenta sistemas
1. **OverlayHost & Navigation:** OverlayService, foco, IME, atajos, capas Esc, inspector, restauración y tamaño adaptativo. Decisiones 9, 10, 11, 13, 15, 26, 29, 30.
2. **UniversalQueryEngine:** normalización, intenciones locales, búsqueda/ranking unificados, autocompletado, ambigüedad, alias y cálculos. 1, 2, 3, 6, 12, 22, 26, 27, 37.
3. **CapabilityRegistry + ExecutionPlanner:** capacidades detectadas, permisos, plan tipado, edición, confirmación, ejecución, errores y rollback cuando exista. 4, 5, 16, 18, 20, 21, 31, 34.
4. **PrivateContext & AI Gateway:** memoria local, sesiones IA, procedencia de adjuntos, consentimiento, envío explícito, streaming y resumen contextual. 2, 7, 11, 14, 23, 24, 25, 33.
5. **DocumentEngine:** SAF/MediaStore y otros accesos legítimos, indexación incremental, Quick Look, texto, fragmentos, OCR y semántica opcional eficiente. 6, 25, 35.
6. **Android Integration Adapters:** apps, Share/ProcessText, notificaciones/RemoteInput, contactos, calendario, MediaSession, Shizuku y Ajustes. 5, 23, 30, 32, 34, 36, 38.
7. **Verified Data Providers:** clima, deportes, web y tasas; fuentes/fechas, normalización, caché, fallback sin fabricar datos. 8, 33, 37, 39, 40.
8. **Jobs & Reliability:** caché offline, trabajos diferidos, límites de batería, notificaciones de progreso, cancelación, errores y reanudación segura. 13, 17, 18, 28, 39, 40.

Las interfaces de estos módulos deberían mantenerse independientes de los componentes visuales; las tarjetas y el inspector consultan un mismo registro de resultados/acciones. **El motor de búsqueda nunca debe esperar redes, OCR o análisis semántico pesado para pintar la barra.**

## 5. Prioridades y secuencia de entrega

### P0 — Congelar y verificar la base (bloqueante)
**Objetivo:** que la versión Universal Preview sea confiable en dispositivo real antes de añadir superficie funcional.
- Auditar SHA, estado de reserva/bloqueo de autonomía, rama, PRs y archivos modificados; no pisar trabajo de otro agente.
- Confirmar APK/ZIP correspondientes al mismo SHA y variante; instalar Universal Preview sin desinstalar las anteriores ni tocar firma histórica.
- Ejecutar checklist en Samsung Galaxy Tab A9+ Android 16: apertura fría/caliente, foco, atajo de teclado real, Tab, Esc, flechas, Shift, Home/End, IME y rotación.
- Validar PDF Quick Look de páginas reales, flujo permiso→selección→extracción→consentimiento→Gemini real con clave propia y proveedor seleccionado.
- Probar `Abrí Spotify y bajá el volumen al 40 %` con aplicación instalada y permisos correctos. Verificar estado final observado, no solo intents enviados.
- Medir apertura visible y tiempo hasta foco/primera interacción (p50/p95 con método documentado), sin fijar metas inventadas antes de la medición base.
- No afirmar que F1.3 está corregido sin prueba de foco frío; no arreglar aumentando silenciosamente el timeout.

**Gate de salida P0:** cero crashes repetibles en flujos críticos; los casos fallidos quedan reproducidos con evidencia y severidad; ruta Gemini real y ruta PDF probadas en tablet o marcadas bloqueadas; Obsidian Glass intacto.

### P1 — Núcleo universal que se usa todos los días (primera inversión)
- Unificar pipeline de resultados y capacidades; todos los caminos (buscar, inspector, Gemini, plan) usan el mismo catálogo de acciones verificadas.
- Estabilidad del teclado y búsqueda exacta: completar con Tab, Enter acción principal, Alt+Enter inspección, Esc capas, flecha derecha edición; no reordenar resultados durante selección.
- Resolver acciones locales sin IA: brillo/volumen, apps, calculadora/conversiones, abrir ajustes, Share/ProcessText, búsqueda directa de música/web.
- Terminar inspector condicional y editor de planes de 2–4 pasos, sin campos/configuración intrusivos; permisos por paso y resultado observado.
- Mejorar flujos de Gemini con consentimiento por fragmento y continuidad explícita; distinguir consulta nueva de continuación.
- Índice de archivos autorizado robusto por metadatos y texto, Quick Look con fuente real, límites y errores visibles.
- Sugerencias: máximo una app inicial pertinente, evitar relleno y chips repetidos, ranking que aprenda correcciones locales.

**Gate de salida P1:** flujos end-to-end instrumentados y físicamente comprobados: `volumen 40`, `2+2`, `Abrí Spotify`, `Buscá PDF de anatomía`, `explicame PDF`, `traducí texto seleccionado`; sin envíos al teclear y sin resultados duplicados.

### P2 — Diferenciadores inteligentes y útiles (segundo bloque)
- Sistema de continuidad contextual y Guardados; actividad relevante restaurable, búsquedas simples efímeras.
- Consulta de notificaciones/WhatsApp con acceso consentido y RemoteInput cuando exista; respuestas editables y confirmadas.
- MediaSession dinámica, preferencia de reproductor, consulta musical, compatibilidad real de comandos.
- Calendario/contactos/reminders con adapters Android autorizados, verificación de fechas y conflictos.
- Clima detallado y deportes personalizados con fuente, caducidad, preferencias explícitas y fallback identificable.
- Web con búsqueda de proveedores reales y cita de fuentes; Gemini interpreta datos que el proveedor haya devuelto efectivamente.
- Multiselección y comparación de documentos con revisión del material antes de enviarlo.

**Gate P2:** cada proveedor tiene tests de errores, datos caducados y ausencia de cobertura; cada acción externa informa si fue preparada, entregada al sistema o verificada; permisos y privacidad cubiertos.

### P3 — Investigación y optimizaciones exigentes (posterior, controlado)
- Índice semántico local medido en RAM/latencia/almacenamiento; si no supera a texto normal en casos reales, no incluirlo por moda.
- OCR incremental para PDFs escaneados y fotos; límites configurables por carga/batería, con errores de reconocimiento visibles.
- Memoria contextual personalizada de alto nivel con caducidad, control de borrado y reglas reproducibles.
- Predicción avanzada, alias reutilizables, tareas en segundo plano y reanudación offline con idempotencia verificable.
- Alertas deportivas/meteorológicas opcionales según fuentes, permisos y restricciones de batería.
- Expandir fuentes, multideporte, proveedores web y capacidades de Gemini solo después de comprobar fiabilidad y costos.

**Gate P3:** benchmark A9+ sin degradar apertura/cold start; límite de recursos; recuperación de errores y pruebas de privacidad/datos antiguos.

## 6. Riesgos técnicos de máxima prioridad
| Riesgo | Mitigación comprobable |
|---|---|
| Foco frío del overlay en One UI/Android 16 | Instrumentar attach→primer dibujo→commit de frame→foco→IME; reproducción física repetible; no aumentar watchdog como único arreglo. |
| Claves/modelos reales Gemini | Configurar en Preview separada, probar error de modelo/cuota, cancelar, no loguear secretos y no enviar adjuntos sin revisar. |
| Integraciones que parecen funcionar | Contratos `available / requires permission / fallback / unsupported`, ejecución observable y estados precisos. |
| Compatibilidad APK/firma | Firma histórica exclusiva de release protegido; variante Preview separada; no actualizar ni borrar originales para pruebas. |
| Datos privados de otras aplicaciones | Solo APIs, notificaciones publicadas y acciones autorizadas; no prometer historial completo de WhatsApp. |
| Índice de archivos y OCR costosos | Indexar por demanda/incremental, límites, excluir carpetas, detener ante presión de memoria, sin bloqueos del overlay. |
| Deportes/web/clima incompletos | Registrar proveedor, timestamp y nivel de confianza; fallback o «datos no disponibles», nunca resultados inventados. |
| Concurrencia Work/Codex | Comprobar rama, locks y PR; una tarea de escritura por vez; sin merge a main hasta aceptación explícita. |
| Cambios cosméticos inadvertidos | Comparación automática de capturas contra Obsidian aprobado; no rediseño ni nuevos controles visibles innecesarios. |

## 7. Matriz mínima de aceptación física
| Escenario | Condición de éxito |
|---|---|
| Invocación fría 10 veces | Barra visible y campo enfocado; sin cierre watchdog injustificado; documentar p50/p95. |
| Invocación desde otra app | Solo overlay; retorno de foco compatible; no abrir actividad principal. |
| Editor físico | Flechas editan, Shift selecciona, Home/End funcionan; Tab y Esc coherentes. |
| Coincidencia exacta | App instalada y comando idéntico prevalecen sobre sugerencias aprendidas. |
| `2+2` | Resultado local único 4, sin Gemini ni duplicados. |
| `Abrí Spotify y volumen 40` | Plan visible, permisos claros, acciones reales, estado/fallo informado. |
| PDF real autorizado | Render de página real, búsqueda de texto dentro de límites, sin error fingido como preview correcta. |
| Gemini y PDF | Revisión exacta del fragmento → confirmación → respuesta real; nada remoto por escribir. |
| Offline | Apps, cálculos y archivos indexados funcionan; datos remotos etiquetados como caché y pendientes seguros. |
| WhatsApp | Solo notificaciones accesibles; RemoteInput si disponible, confirmación antes del envío. |
| Deportes y clima | Proveedor y actualización identificables; priorización de favoritos; sin marcadores inventados. |
| Resistencia | Rotación, doble invocación, cancelar, volver, permisos denegados y pérdida de internet sin crashes. |

## 8. Cómo dividir el trabajo y proteger la cuota
**Codex + GitHub (preferido para P1 y siguientes):** cambios Kotlin acotados, PRs aislados, tests dirigidos antes de suites completas, revisión de diffs, optimización, código de adaptadores. No rehacer simultáneamente toda la aplicación.

**Work (cuando realmente aporta):** preflight integral con múltiples herramientas, revisión de entrega del mismo SHA, compilación y APK/ZIP verificables, investigación de incidencias complicadas con artefactos, validación física si Work tiene acceso real al dispositivo; no presumirlo. Reservar sus créditos para puertas de calidad y entregables de alto valor.

**Chat (diseño):** definición de casos, priorización, prompts de implementación por etapa y revisión de resultados. No usar Work para seguir debatiendo requisitos.

Antes de editar: leer `AGENTS.md`, `PLANS.md`, el handoff Universal y la reserva actual; verificar HEAD y PR abierto; trabajar aislado y no hacer merge, release o CI costosa sin decisión explícita. Entregar por fase: SHA, commits, pruebas realmente realizadas, fallos, capturas, firma/paquete y lista precisa de pendientes.

## 9. Primera orden de ejecución sugerida
> Retomar Spotlight Next en `feat/work-universal-intelligence-20261016` o rama aislada derivada del SHA exacto `fd3e418e2cd45dd9d5c01bf7ef398bb22b06a57c`, después de comprobar la coordinación/locks actuales. La estética Obsidian Glass está congelada. No implementar nuevas decisiones de producto todavía. Ejecutar P0: auditar estado del HEAD, variantes, permiso/firma, logs de foco frío, pruebas de teclado y Quick Look, consulta real a Gemini y Spotify/volumen en Galaxy Tab A9+ solo si existe acceso al dispositivo y credenciales consentidas. Separar verificaciones automatizadas de físicas y reales. Reproducir los fallos antes de corregir, conservando tests, firma histórica y datos. Entregar informe de aceptación con pruebas, SHA, bloqueos y recomendación precisa para arrancar P1. No mergear main ni sustituir la APK existente.

## 10. Definición de «terminado»
Una capacidad está **diseñada** al aprobar su contrato; **implementada** cuando existe código conectado al flujo de producción; **verificada en pruebas** cuando supera tests dirigidos y de regresión; **validada físicamente** cuando se comprueba en Galaxy Tab A9+; y **lista para integrar** cuando están cubiertos permisos, regresiones, rendimiento, privacidad y compatibilidad de firma/entrega. No colapsar estos cinco estados en un solo «listo».
