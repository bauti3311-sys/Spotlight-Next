# Spotlight Next — ESPECIFICACIÓN MAESTRA DEFINITIVA

**Versión:** 1.0 · 40 decisiones aprobadas · 21 de octubre de 2026  
**Proyecto:** `bauti3311-sys/Spotlight-Next` · Android nativo Kotlin/Jetpack Compose  
**Dispositivos objetivo:** Samsung Galaxy Tab A9+ y Motorola G86 5G  
**Uso:** personal, instalación privada por APK, sin Google Play  
**Identidad:** **Raycast Dark Glass**: una búsqueda universal, compacta, veloz y contextual.

> **Estado de este documento:** decisión de producto aprobada por el usuario, NO informe de que el código ya la implemente. La evidencia de aplicación adjunta es la Preview 34 / `cc1c28d` (estado histórico comprobable del archivo provisto, no HEAD remoto actual). Work debe comparar con HEAD actual y ejecutar pruebas reales. Ningún render es una captura de la APK.

## 0. Contrato de ejecución para Work — leer antes de editar

1. **Continuidad primero:** inspeccionar rama, HEAD local/remoto, status, PRs, checkpoints, trabajo en curso, `AGENTS.md`, `PLANS.md`, `docs/RENOVACION-APROBADA.md`, `docs/AUTONOMIA-RESERVA.md` y skills de repo. Ejecutar `python3 scripts/autonomy_lock.py status` y seguir su protocolo; nunca tomar el lock a la fuerza. No sobrescribir commits/PR ni iniciar una rama incompatible si otra sesión trabaja en el mismo árbol.
2. **Preservar la app real:** Kotlin + Jetpack Compose + WindowManager overlay, motor de búsqueda y ejecutores actuales. Ningún WebView, prototipo React o imagen colocada como interfaz. Mantener `main`, firma/keystore histórica, permisos, preferencias, usuario y flujo de actualización. No hacer merge automático. No crear otra key.
3. **Diferencia funcional/visual:** cada componente visible debe conectarse a estado real o marcarse como no disponible; jamás botones ficticios, permisos supuestos, datos climáticos inventados, canciones ficticias como reproducción real, llamadas a IA silenciosas ni apertura accidental de PDFs.
4. **Los cuatro PNG son especificaciones visuales**, no datos reales. Solo se usan para geometría y composición o materiales, según el README de imágenes. Si chocan con las decisiones 01–40, **mandan las decisiones**. No copiar los ejemplos como resultados reales.
5. **Autonomía con límites:** arreglar defectos menores y diferencias visuales coherentes con esta especificación; pedir aprobación para cambios de arquitectura disruptivos, permisos sensibles nuevos, cambios del paquete/firma o eliminar funcionalidad ya existente. Registrar dudas/bloqueos y continuar tareas independientes, sin reescribir todo.
6. **Priorizar compilación local:** para ahorrar GitHub Free, acciones de CI solo cuando aporten evidencia indispensable. No afirmar que se ejecutó un test, se verificó en hardware, o se generó una APK si no ocurrió.
7. **Cada checkpoint:** commit SHA, pruebas ejecutadas y resultados, lista implementada/pendiente, capturas reales con su procedencia (dispositivo/emulador/Robolectric), comparaciones anotadas, APK Preview instalable + ZIP de fuente del *mismo commit* y huellas SHA256. Si la keystore no está disponible, decirlo y no publicar una APK incompatible como actualización.

## 1. Jerarquía de autoridad y conflictos detectados

1. Seguridad, límites reales del sistema operativo, documentación de APIs y protección de datos.
2. Las **40 decisiones** de este documento (producto actual aprobado).
3. Cuatro imágenes adjuntas: `03/04` prevalecen para **materiales y Glass**; `01/02` son referencia de **estructura y composición**.
4. Código real y tests existentes, salvo funcionalidades en contradicción expresa con las nuevas decisiones; migrar manteniendo los datos antiguos.
5. Documentos históricos del repositorio y prototipos previos.

**Conflictos que deben resolverse de forma explícita, sin borrar datos antiguos:**

- `DESIGN.md` del ZIP `cc1c28d` define opacidad mínima de 90 %: **nuevo objetivo inicial es 84/92/94 %**. Si el blur real no está disponible, aplicar fallback legible; no forzar 90 % en la preferencia del buscador como regla general.
- `DESIGN.md` permite hasta **cuatro sugerencias al inicio**: nueva decisión fija **máximo tres**, máximo **una app**; nunca rellenar con falsos positivos.
- La descripción histórica menciona **hasta doce resultados**: en la vista inmediata objetivo es **5–6 en consulta** cuando la geometría lo permita; si hay «Ver más», conservar una navegación explícita sin romper resultados adicionales ni truncar el índice a seis internamente.
- Las familias visuales históricas (macOS Glass, Raycast Graphite, Minimal Pure) no deben mezclarse entre buscador y Ajustes. **Estilo inicial común: Raycast Dark Glass**, con preferencias históricas migradas de forma conservadora y sin eliminación masiva. Se pueden mantener otros presets como opciones si no alteran la identidad aprobada.
- El esquema antiguo de Apariencia con botón **Aplicar** entra en conflicto con la petición de previsualización inmediata de opacidad y tema. Los cambios visuales aprobados deberán ser persistentes y comprobables; preservar rollback si hace falta.
- `AGENTS.md` menciona que Tab abre acciones cuando no hay sugerencia. La decisión 30 exige autocompletar solo con candidato válido y navegar en el ActionPanel con Tab/Shift+Tab. Definir una regla única sin destruir el contrato ya probado ni interferir con entrada de texto/IME.
- Algunos documentos anteriores normalizan «un toque ejecuta». **La decisión 23 prevalece**: toque 1 selecciona, toque 2 o Enter ejecuta; gesto largo acciones; scroll jamás ejecuta.
- Donde los renders muestren filas de dos líneas, **la decisión 6 prevalece**: una sola línea por resultado con metadatos a la derecha. Los detalles van en el inspector.

## 2. Diseño visual vinculante

**Tokens iniciales, ajustables tras capturas físicas:**

| Token | Valor aceptado | Reglas |
|---|---|---|
| Paleta | grafito oscuro y cristal translúcido | lenguaje único, sin apariencia Material separada en Ajustes |
| Opacidad buscador | **84 %** | regulador real, persistente, preview instantánea |
| Opacidad ActionPanel | **92 %** | independiente visualmente, lectura prioritaria |
| Opacidad Ajustes | **94 %** | más sólido para lectura |
| Selección | gris translúcido | no azul permanente en filas |
| Esquinas ventana | 16–18 dp | subcomponentes con radios menores |
| Contorno | ~1 dp fino | sin halos decorativos |
| Texto resultado | Inter 14 sp | respetar fontScale/legibilidad |
| Metadatos | Inter 11 sp | a la derecha, discretos |
| Altura fila | ~44 dp | accesibilidad táctil con hit-area apropiada |
| Entrada/salida | ~140/~100 ms | foco anterior o simultáneo a la animación; reducción de movimiento |
| Ventana | centro-superior adaptativo | anclaje estable y respeto al IME |
| Home | ≤3 sugerencias, ≤1 app | contextual, real y local |
| Con búsqueda | hasta 5–6 visibles cuando hay espacio | crecimiento de alto sin reubicar foco |

**Blur real**: no confundir alpha con blur. Evaluar soporte en **WindowManager/One UI** y fallbacks; si el fabricante desactiva blur o produce artefactos/huellas rectangulares, usar superficie grafito translúcida con contraste seguro. Evitar blur a pantalla completa, elevadas GPU cost y shaders privados. Validar sobre wallpapers claros y oscuros.

**Barra:** iconos reales de apps desde Android; comandos, archivos y acciones con iconos monocromáticos; opciones ⋯ (Ajustes/Apariencia/Permisos/Atajos); microfooter contextual; IA no visible como botón permanente; micrófono solo si puede ejecutar reconocimiento; al escribir aparece limpiar.

**Inspector y superposiciones:** un único espacio contextual sin columna vacía: Quick Look / reproductor multimedia / IA / formularios / datos en vivo / planes. Fijación explícita para mantener el contenido. ActionPanel es menú flotante de acciones **superpuesto** al buscador, nunca empuja resultados. Tablet lateral y móvil adaptado dentro de insets seguros. Resultados de una línea; descripciones solo en inspector.

**Ajustes:** secciones **General, Apariencia, Búsqueda, Integraciones, Permisos, IA, Atajos, Avanzado**. Menú lateral tablet y móvil adaptado. Filas planas, controles a la derecha, encabezados discretos, búsqueda de ajustes opcional si ya existe. Migrar controles operativos previos a sus nuevas ubicaciones; no quitar capacidades ocultándolas sin acceso. Centralizar todos los permisos exclusivamente en **Permisos**, y reservar **Integraciones** para funcionalidad por aplicación.

## 3. Contratos funcionales que no se negocian

### 3.1 Búsqueda/orden
- Exact match de aplicación instalada tiene precedencia sobre prefijos, parciales, archivos secundarios y coincidencias irrelevantes; no hacer que webOS Dev Manager gane a WhatsApp/Spotify cuando se busca esa app.
- Intenciones de acción local verificadas (`brillo al 40 %`, volumen, reproducción) pueden priorizarse frente a aplicaciones textuales irrelevantes. Fuzzy/historial recorta empates, no destruye exact match.
- Una sola paleta universal. Local-first; sin consulta remota ni IA automática en cada pulsación. Conexión solo cuando se solicita y autoriza.
- Resultados estabilizados durante navegación por teclado aunque lleguen fuentes tardías; cancelar trabajo obsoleto por query.
- Inicio vacío muestra hasta 3 sugerencias y no está dominado por Recientes ni por un enlace genérico a IA.

### 3.2 Entrada/gestos/teclado
- Primer toque selecciona; segundo toque del mismo elemento ejecuta, Enter ejecuta seleccionado; pulsación larga abre ActionPanel; scroll no ejecuta ni hace click por error; cancelación/arrastre no dispara.
- Atajos en paleta con foco: flechas, Enter, Tab, Shift+Tab, Ctrl+K, Ctrl+M, Escape jerárquico. Conflictos configurables; no interceptar teclado fuera de Spotlight.
- Al abrir la barra el campo recibe foco inmediatamente. El teclado virtual no oculta el resultado seleccionado, ActionPanel ni formularios. Escape/Back cierra el nivel superior pertinente antes del overlay.

### 3.3 Acciones/estado
- Estado de cada `Capability`: disponible, requiere permiso, requiere configuración, no disponible, fallo de ejecución. Nunca `enabled` inventado.
- Abrir app, volumen/pausa y acciones simples: cerrar tras éxito (excepto si inspector fijado o tarea pendiente). IA, documentos, planes, formularios: mantener. Errores: permanecer y permitir corregir/reintentar.
- Acción sensible/comunicación/destructiva: resumen del destinatario/objetivo/contenido y confirmación; respetar UI de app destino. No hay envío silencioso de WhatsApp no autorizado.
- Estado de tarea real medible o indeterminado si no se puede medir. Reporte de éxito solo tras señal de finalización verificable.

### 3.4 Multimedia e IA
- Controles con MediaSession donde se permita; Spotify App Remote solo con la app y autorización compatibles. **El audio de Spotify no se reproduce como streaming propio en Spotlight**; controla el cliente Spotify. Sin falsas canciones, progreso o portada.
- Proveedores IA modulares y credenciales protegidas; Gemini preferido configurable, pero no depender de cuota incluida en Gemini Pro ni en ChatGPT Plus. Sesiones vinculadas a tarea y estado de streaming/cancelación, sin conversaciones globales mezcladas.
- Acciones/planes editables con pasos locales y remotos, dependencia, autorización, pausa/error y reanudación segura; alias persistentes con parámetros, sin ejecución de scripts arbitrarios por defecto.

### 3.5 Documentos, contexto, privacidad, tiempo real
- Indexación incremental local y limitada a URIs/carpetas autorizadas; OCR opcional. Separar lectura/selección/Quick Look de apertura externa explícita, evitando abrir PDF por scroll.
- Recibir contenido explícito de otros apps (ACTION_SEND, enlaces, imágenes, texto y URIs con grants) con manejo correcto de acceso y revocación; no vigilar portapapeles continuamente.
- Datos externos (clima Mar del Plata, deporte, monedas) solo con fuente comprobable, tiempo de actualización y estado no disponible sin conexión. Calculadora local.
- Historial/sesiones/índice locales, borrables independientemente; IA requiere consentimiento para enviar contenido privado. Backup portable sin claves/secretos; import preview, compatibilidad, recuperación ante datos corruptos.

## 4. Registro trazable de las 40 decisiones

Cada fila es un requisito aprobado y un criterio de aceptación mínimo. **No implica que ya se haya implementado**.

**D01 — Selección: Gris translúcido Raycast**  
Filas seleccionadas y estados de foco en gris translúcido consistente en búsqueda, ActionPanel y Ajustes; azul solo para controles activos o casos informativos.  
*Checkpoint objetivo: C1.*

**D02 — Superficies Glass: Glass equilibrado**  
Opacidad inicial buscador 84 %, ActionPanel 92 %, Ajustes 94 %; valores configurables y persistentes con previsualización inmediata; fallback legible cuando blur no esté disponible.  
*Checkpoint objetivo: C1.*

**D03 — Altura del buscador: Altura adaptativa**  
Inicio de hasta tres sugerencias; con consulta mostrar 5–6 resultados si hay espacio y coincidencias, sin huecos, respetando IME, creciendo hacia abajo y conservando anclaje superior.  
*Checkpoint objetivo: C1.*

**D04 — ActionPanel: Adaptativo inteligente**  
ActionPanel sobrepuesto lateral en tablet; overlay dentro del área segura en teléfono; jamás refluye los resultados, permite filtro de acciones y entrada de teclado/tacto.  
*Checkpoint objetivo: C2.*

**D05 — Ajustes: Preferencias Raycast**  
Filas planas, controles a la derecha, secciones discretas, sidebar tablet y navegación adaptada en teléfono; conservar opciones reales existentes sin controles decorativos; permisos centralizados.  
*Checkpoint objetivo: C3.*

**D06 — Filas de búsqueda: Minimalista Raycast**  
Una línea por resultado: título a la izquierda; metadato breve a la derecha, truncado elegante. Los detalles solo en inspector; evitar subtítulos bajo filas normales.  
*Checkpoint objetivo: C1.*

**D07 — Iconografía: Originales + monocromáticos**  
Cargar iconos originales de apps instaladas mediante Android; acciones/documentos con vectoriales monocromáticos coherentes, sin emojis ni cajas innecesarias.  
*Checkpoint objetivo: C1.*

**D08 — Pie del buscador: Barra contextual minimalista**  
Una línea de acciones disponibles, atajos de teclado en tablet y equivalentes táctiles en teléfono; ocultar lo no disponible.  
*Checkpoint objetivo: C1.*

**D09 — Inicio: Tres sugerencias inteligentes**  
Hasta tres sugerencias útiles, máximo una aplicación, sin relleno ni IA remota automática; usar contexto local autorizado, recientes/frecuentes y multimedia real.  
*Checkpoint objetivo: C1.*

**D10 — Controles de entrada: Controles contextuales**  
Micrófono solo cuando funcione; botón borrar mientras haya texto; menú ⋯ accesible; sin botón fijo de IA, integrada en resultados.  
*Checkpoint objetivo: C1.*

**D11 — Geometría: Centro superior adaptativo**  
Anclaje horizontal centrado y vertical superior contextual; anchura según tablet/teléfono; IME seguro; no saltos al cambiar cantidad de resultados.  
*Checkpoint objetivo: C1.*

**D12 — Animaciones: Microanimaciones Raycast**  
Objetivos ~140 ms apertura y ~100 ms cierre, transiciones ligeras, foco/entrada inmediata, respetar reducción de movimiento, sin coste de frames significativo.  
*Checkpoint objetivo: C1.*

**D13 — Contorno: Dark Glass refinado**  
Radio principal ~16–18 dp, borde ~1 dp y sombra suave; radios jerarquizados en menus/filas; sin glow exagerado.  
*Checkpoint objetivo: C1.*

**D14 — Tipografía: Raycast equilibrada**  
Inter; títulos resultado ~14 sp, metadatos ~11 sp, fila base ~44 dp; ajuste accesible al fontScale y objetivo táctil donde sea necesario.  
*Checkpoint objetivo: C1.*

**D15 — Quick Look: Contextual inteligente**  
Selección de archivo compatible presenta vista lateral en tablet; teléfono a demanda, nunca columna vacía, acceso autorizado y gestión de recursos, arrastre no abre archivo.  
*Checkpoint objetivo: C2.*

**D16 — Música: Reproductor en inspector**  
Reproductor contextual en el mismo panel de Quick Look, portada/título/artista/progreso/controles si hay sesión válida; audio sigue en app de origen; sin panel permanente vacío.  
*Checkpoint objetivo: C2.*

**D17 — IA visual: IA en inspector contextual**  
Respuestas, herramientas, formularios de IA y planes en panel; sin pestaña independiente ni invocación remota ante cada pulsación; streaming/cancelar/copiar/seguimiento.  
*Checkpoint objetivo: C2.*

**D18 — Persistencia del inspector: Contextual con opción de fijar**  
Inspector sigue selección salvo fijación explícita; al fijarlo conserva contenido al navegar; estados de IA y planes no se pierden accidentalmente.  
*Checkpoint objetivo: C2.*

**D19 — Menú ⋯: Esencial Raycast**  
Cuatro accesos: Ajustes, Apariencia, Permisos, Atajos; separado de ActionPanel contextual, compacto y accesible.  
*Checkpoint objetivo: C2.*

**D20 — Permisos: Centro inteligente**  
Centralizar permisos reales, estado comprobado, motivo y ruta para habilitar; pedirlos cuando se necesitan; integrar accesibilidad/Shizuku solo si justificado; Integraciones aparte.  
*Checkpoint objetivo: C3.*

**D21 — Formularios: En el inspector lateral**  
Acciones con argumentos pendientes presentan campos editables/contextuales en inspector; teléfono overlay adaptado; no pedir valores ya presentes; resolver datos reales.  
*Checkpoint objetivo: C2.*

**D22 — Seguridad de acciones: Confirmación inteligente**  
Acciones triviales autorizadas directas; comunicación externa/sensibles/destructivas con revisión/confirmación; respetar confirmación de apps destino; reporte verificado de error/éxito.  
*Checkpoint objetivo: C4.*

**D23 — Gestos: Seleccionar y confirmar**  
Primer toque selecciona; segundo sobre mismo resultado o Enter ejecuta; largo abre ActionPanel; scroll/cancelación jamás ejecuta; ↑↓ seleccionan; Escape vuelve niveles.  
*Checkpoint objetivo: C1.*

**D24 — Post-ejecución: Cierre contextual inteligente**  
Cerrar tras acciones simples exitosas; mantener Quick Look/IA/planes/pendientes/errores; no cerrar inspector fijado por una acción lateral sin decisión explícita.  
*Checkpoint objetivo: C2.*

**D25 — Ranking: Búsqueda híbrida inteligente**  
App exacta > prefijo > parciales relevantes; reconoce intents locales y argumentos; acciones disponibles; historial no desbanca app exacta; IA solo complejas; latencia local baja.  
*Checkpoint objetivo: C1.*

**D26 — Arquitectura integraciones: Profunda por capacidades**  
Capability registry: intents/shortcuts/MediaSession/APIs oficiales/AppFunctions compatibles, Shizuku opcional; comprobación de ejecutabilidad y fallback honesto.  
*Checkpoint objetivo: C4.*

**D27 — Planes: Encadenados editables**  
Plan en inspector con pasos editables/quitar/reordenar, dependencias/verificaciones/cancelación y confirmación de riesgo; detenerse correctamente ante fallos.  
*Checkpoint objetivo: C4.*

**D28 — Alias: Comandos personalizados inteligentes**  
Alias locales para acciones/planes, parámetros/favoritos, edición, conflictos con nombres exactos y permisos; sin scripts arbitrarios por defecto ni marketplace.  
*Checkpoint objetivo: C4.*

**D29 — Invocación: Múltiple configurable**  
Teclado físico cuando OS permita, TileService/Ajustes rápidos, launcher shortcuts; métodos por dispositivo y alternativas honestas; sin burbuja permanente forzada.  
*Checkpoint objetivo: C4.*

**D30 — Teclado: Atajos Raycast configurables**  
↑↓ navegar; Enter ejecutar; Tab autocompletar según sugerencia; Shift+Tab retroceder; Ctrl+K acciones; Ctrl+M expandir; Esc jerárquico; editor y detector conflictos.  
*Checkpoint objetivo: C1.*

**D31 — Documentos: Índice local inteligente**  
Búsqueda por nombre y texto contenido de archivos autorizados; indexación incremental, OCR opcional explícito, límites RAM/CPU, carpeta exclusiones/borrado y Quick Look.  
*Checkpoint objetivo: C4.*

**D32 — Contexto Android: Integración contextual**  
Recibir ACTION_SEND/ACTION_SEND_MULTIPLE y otros mecanismos compatibles para texto/URLs/imágenes/docs, con URIs seguros y permisos; sin vigilancia continua de portapapeles.  
*Checkpoint objetivo: C4.*

**D33 — Sesiones de IA: Contextuales inteligentes**  
Sesión asociada a documento/tarea con follow-ups, historial local optativo, recuperación y eliminación; no mezclar contexto global; envío explícito.  
*Checkpoint objetivo: C4.*

**D34 — Proveedores IA: Motor modular**  
Local-first, Gemini preferido configurable, proveedores adicionales opcionales mediante adaptadores, cuotas/error/cancelación/credenciales seguras; no confundir subscripción con API.  
*Checkpoint objetivo: C4.*

**D35 — Resultados en vivo: Compactos + inspector**  
Cálculos locales, tiempo/deportes/FX externos con fuente+actualización, sin datos fabricados; fila única y detalle/acciones en inspector; offline apropiado.  
*Checkpoint objetivo: C4.*

**D36 — Estados: Microestados contextuales**  
Feedback discreto para acción local y progreso/cancelación en inspector para tareas largas; permiso faltante y errores claros, no falso éxito, no notificaciones decorativas.  
*Checkpoint objetivo: C2.*

**D37 — Recursos: Rendimiento adaptativo inteligente**  
Cold/warm start medido, foco inmediatamente, índices incrementales, cache LRU/límites, no IA por tecla, tareas pesadas diferibles, batería/RAM monitoreados.  
*Checkpoint objetivo: C5.*

**D38 — Privacidad: Inteligente y configurable**  
Historial/índice/sesiones locales y borrables por separado; confirmación por contenido/destino de IA, credenciales seguras, permisos mínimos; no recopilación indiscriminada.  
*Checkpoint objetivo: C4.*

**D39 — Respaldo: Actualización segura + respaldo local**  
No modificar applicationId/firma; migración de preferencias, export/import local con preview/esquema/versionado/rollback; no secretos en respaldo ordinario; índice se reconstruye.  
*Checkpoint objetivo: C3.*

**D40 — Entregas: Calidad por checkpoints**  
Hitos C1–C5, comparaciones con renders aprobados y screenshots Android reales diferenciados de Roborazzi, unit/instrumental/lint/R8, APK+ZIP fuente mismo SHA.  
*Checkpoint objetivo: C5.*


## 5. Plan por checkpoints (decisión 40)

**C0 — Baseline, sin reimplementación:** verificar Git HEAD/rama/PR/lock, diferencias respecto a Preview 34 `cc1c28d` adjunta, componentes existentes, pruebas y evidencia. Listar estado de cada D01–D40 como `ya funciona`, `parcial`, `pendiente`, `incompatible/condicionado`, citando código o prueba. Identificar soluciones ya realizadas para no duplicarlas. No compilar de manera masiva ni generar un APK nuevo solo para descubrir la rama.

**C1 — Buscador, visual, ranking y teclado (D01–D03, D06–D14, D23, D25, D30).** Barra Raycast Dark Glass real, ajuste de opacidad, 3 sugerencias, lista adaptativa, filas de una línea, selección gris, toque doble/largo/scroll, ranking y acciones locales, atajos. Verificar focus/IME/motion/recomposiciones. **APK checkpoint** cuando esté estable + capturas comparadas.

**C2 — ActionPanel e inspector (D04, D15–D19, D21, D24, D36; soporte IA D17).** Panel superpuesto responsive, Quick Look que no se abre por scroll, MediaSession funcional cuando disponible, inspector único y fijable, menús ⋯ vs Ctrl+K, formularios adaptados, microestados. No confundir preview de portada/doc con contenido real. **APK checkpoint** y pruebas de foco/paneles.

**C3 — Ajustes, permisos, backup (D05, D20, D39; preferencia D02).** Navegación real 8 secciones con componentes uniformes, migración de opciones, estados permission-check de Android, integraciones diferenciadas, sliders persistentes y preview; export/import datos no secretos, migración con rollback. **APK checkpoint** y prueba de actualización preservando datos.

**C4 — Capabilities e inteligencia (D22, D26–D29, D31–D35, D38; completar D16/D17).** Registro real de capacidades por app, tests con apps instaladas, planes, alias, invocación configurable, documentos/OCR selectivo, compartidos Android, Gemini/opcional proveedores, consultas externas y consentimiento. Cada integración completa solo cuando ejecuta la capacidad real. Sub-checkpoints pequeños e instalables al consolidar áreas completas; evitar una mega-PR.

**C5 — Certificación (D37, D40 y regresiones D01–D39).** Pruebas unitarias, Robolectric/screenshots sin manipular baseline para ocultar fallas, instrumentación cuando entorno permita, análisis en tablet/teléfono con prueba física diferenciada de emulación, cold/warm startup, memoria, batería, IME, fragmentación, blur fallback, permisos rechazados y rotación. APK Preview + ZIP fuente + SHA256 manifest / idéntico commit. Registrar limitaciones y pendientes, no declarar «100 % completado» si hay bloqueos.

**Al entregar un checkpoint informar en 5 apartados:**

1. Rama/commit SHA y cambios desde el anterior; estado del lock y PR.
2. IDs de decisiones implementadas y pendientes, diferenciando código presente de comportamiento verificado.
3. Comandos de tests realmente ejecutados, nº pruebas, lint/R8, límites y errores; procedencia de capturas.
4. Comparación de 4 imágenes aprobadas con capturas **de Android real/Compose**; lista de discrepancias visuales y técnicas.
5. Enlaces o archivos reales de APK Preview actualizable, ZIP fuente y manifest del mismo SHA; si no se pudo generar, motivo exacto sin prometer enlaces inexistentes.

## 6. Matriz mínima de aceptación y pruebas reproducibles

| Prueba | Escenario | Resultado esperado |
|---|---|---|
| Arranque y foco | Abrir overlay sobre Chrome y usar teclado | Paleta visible sin Activity full-screen, foco instantáneo, sin artefactos |
| Ranking | `spotify`, `WhatsApp`, `brillo 40%`, alias conflictivos | App exacta o acción local intencional primero, no resultados irrelevantes encima |
| Inicio | Barra vacía, sesiones con/sin MediaSession | ≤3 sugerencias útiles, ≤1 app; sin estado multimedia ficticio |
| Gestos | Deslizar sobre PDF varias veces, tap/cancel/long tap/doble tap | Scroll no abre; toque selecciona; doble toque/Enter abre; largo acciones |
| IME | Tablet horizontal, teléfono vertical, rotación | Search, seleccionado, Quick Look/ActionPanel permanecen en viewport seguro |
| Paneles | Seleccionar app/PDF/música/IA, fijar inspector | Un inspector sin columna vacía; fijado conserva estado; acciones no lo desplazan |
| Música | Spotify instalado/no instalado, sesión activa/inactiva | Control solo cuando API disponible, sin reproducir audio propio ni inventar estado |
| Permisos | Denegar/revocar superposición/contactos/etc | Estado real e instrucciones pertinentes; no falso `Habilitado` |
| Seguridad | Preparar WhatsApp / eliminar documento | Datos verificados y confirmación adecuada; no envío ni borrado silencioso |
| IA | Modelos sin key, cuota, timeout, cancelación | Error honesto, consulta explícita, recuperación sesión si configurada |
| Documentos | SAF autorizado, PDF escaneado, índice actualizado | Acceso correcto y OCR opt-in, sin explorar archivos sin permiso |
| Backup | Export/import, invalid schema, update APK | Mantener settings/comandos, no copiar secretos, importar con preview, rollback |
| Visual | Comparar cuatro renders en teléfono/tablet, claro/oscuro | Misma tipografía/material, filas 1 línea, selección gris, 84/92/94 % inicial |
| Rendimiento | búsqueda en ráfaga, inicio frío/caliente, indexing | input no bloqueado, recursos controlados; métricas reales registradas, no supuestas |

### Tests locales / herramientas existentes que Work debe reutilizar

El ZIP de fuente de Preview 34 contiene, entre otros, `app/src/main/java/dev/bautista/spotlight/overlay/OverlayService.kt`, `.../search/{CompactResults,ResultRanker,SearchEngine,HomeSuggestions}.kt`, `.../ui/{SpotlightPanel,GlassSettings,GlassAppearance,ActionPanelView,ContextPanel,EditablePlanPanel}.kt`, así como tests `ActionPanelKeyboardContractTest`, `DarkGlassSettingsTest`, `IntegrationRegistryTest` y `SettingsPersistenceTest`. **Verificar la existencia actual antes de referenciarlos.** Emplear las skills `.agents/skills/spotlight-visual-review`, `spotlight-fix-bug` y `spotlight-release` según corresponda. Mantener los cambios pequeños y evitar acciones repetitivas de CI.

> **Nota sobre pruebas visuales:** capturas Robolectric/Roborazzi, emuladores y pruebas físicas no son equivalentes. Declarar origen, Android SDK, resolución/densidad, valores de opacidad y presencia de fondo real. No afirmar fidelidad a One UI a partir de un fixture sintético.

## 7. Restricciones técnicas y fuentes para contrastar

- **AppFunctions**: Android 16+ y permisos/caller autorizado; no prometer descubrir/ejecutar arbitrariamente funciones de todas las apps en Tab A9+ sin comprobar compatibilidad.
- **Glass/blur**: consulta `WindowManager.isCrossWindowBlurEnabled()` y fallbacks para blur deshabilitado o sin soporte. No basta con un alpha visual si el render aprobado muestra fondo difuminado.
- **Invocación global**: probar atajos reales en One UI/Android y ofrecer TileService/shortcuts; no prometer interceptación arbitraria de hotkeys mientras otras apps tienen foco.
- **Spotify**: SDK/App Remote controla la app Spotify instalada y autorizada; no es un SDK de streaming propio. MediaSession depende de la sesión disponible y acceso requerido.
- **WhatsApp y Gmail**: distinguir Intents/compartir, profundas APIs oficialmente disponibles y lectura por notificaciones autorizadas; no prometer acceso total a mensajes/correos de terceros.
- **Storage/SAF**: rutas autorizadas y URIs con permisos, sin dependencia automática del permiso general de almacenamiento.
- **Seguridad**: nunca poner claves de API o keystore en ZIP, repositorio, logs ni capturas; la API requiere cuentas y cuotas propias.

Documentación de referencia: https://developer.android.com/ai/appfunctions · https://developer.android.com/reference/android/view/WindowManager · https://developer.android.com/develop/ui/views/quicksettings-tiles · https://developer.spotify.com/documentation/android

## 8. Entregable definitivo y criterio de cierre

El producto es una **APK Android nativa instalable** que implementa las 40 decisiones, con variantes de tamaño reales para tablet y teléfono, datos reales o estados de indisponibilidad, y pruebas acreditadas. La entrega de un render, un dashboard HTML o pruebas verdes aisladas **no equivale a completar el producto**. Mantener un `STATUS_40_DECISIONES.md` actualizado por IDs D01–D40 con evidencia y bloqueos, y diferenciar claramente aceptación visual aprobada por usuario, prueba automatizada y validación física.

**El usuario autorizó mejoras pequeñas dentro de estas decisiones; cambios de permisos, arquitectura disruptiva o eliminación de funcionalidades exigen consulta.**
