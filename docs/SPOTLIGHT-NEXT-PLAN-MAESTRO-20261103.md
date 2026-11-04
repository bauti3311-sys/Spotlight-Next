# SPOTLIGHT NEXT
## Plan maestro de evolución funcional y visual — Preview 60 nativa

**Documento de dirección para Codex · 3 de noviembre de 2026 · Versión del plan 1.0**  
**Estado:** PROPUESTA APROBADA EN ALCANCE; IMPLEMENTACIÓN NO AUTORIZADA TODAVÍA.  
**Prioridad:** funcionalidad y estética en paralelo, mediante checkpoints independientes.

> **Instrucción principal para Codex:** este documento sirve para planificar, auditar y presentar propuestas visuales. **No autoriza editar archivos, generar commits, iniciar builds, usar GitHub Actions, cambiar la firma, instalar APK ni fusionar ramas.** Antes de implementar cambios significativos, presentar la propuesta técnica y visual para aprobación expresa del propietario.

## 1. Resumen ejecutivo

El objetivo es perfeccionar la última línea **Kotlin + Jetpack Compose nativa** de Spotlight Next, evitando repetir el experimento Nuxt/WebView de Preview 58/59. Se propone convertir la barra universal en una consola de acciones Android: buscar contenido, mostrar capacidades verificadas, preparar operaciones, solicitar confirmaciones y observar el resultado real sin fingir éxito.

La evolución reúne **23 mejoras elegidas**: **10 funcionales**, **9 estéticas** y **4 de estabilidad/arquitectura**, repartidas en **10 checkpoints (C0–C9)**. La estética se trabaja junto con la función que presenta, pero se aprueba visualmente **antes** de codificar los cambios grandes. El trabajo debe ser incremental, reversible y trazable; cada checkpoint cerrado debe contener evidencia y riesgos conocidos.

**Criterios no negociables:** conservar paquete, firma histórica y datos instalados; mantener Compose nativo y los motores existentes; no sumar capacidades simuladas; ninguna acción sensible ni alternativa se ejecuta sin la autorización que corresponda; conservar teclado, búsqueda exacta y scroll táctil; proteger privacidad; evitar gasto de CI y recompilaciones inútiles.

### 1.1 Base de referencia

| Campo | Referencia de trabajo |
|---|---|
| Repositorio | `bauti3311-sys/Spotlight-Next` (privado) |
| Línea de producto | Preview 57 nativa, mejorada en Preview 60 |
| Rama de partida documentada | `feat/preview57-native-return-20261103` · PR #21 |
| Fuente exacta de APK Preview 60 | `e9647c1329b93820383e3c8b4ec56dbceb3ce5f1` |
| Comportamiento funcional incorporado | `f6644841bf3e1725a055c6fda19e29256485ffdf` |
| Variante instalada objetivo | `dev.bautista.spotlight.next.raycastpreview` |
| Certificado histórico Raycast Preview | SHA-256 `885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba` |
| Dispositivo principal de validación | Samsung Galaxy Tab A9+ (One UI; teclado físico opcional) |
| Dispositivo complementario | Motorola moto g86 5G |

**Precisión de alcance:** estos datos son la base congelada de referencia, no una instrucción de retroceder HEAD ni sobrescribir trabajo posterior. Al comenzar cualquier sesión, inspeccionar la situación remota, reservas y cambios nuevos y documentar diferencias antes de actuar. El `versionCode` de futuras entregas será calculado y verificado, nunca supuesto.

### 1.2 Qué existe y qué falta demostrar

La auditoría anterior localizó motores de búsqueda de apps/archivos/contactos/acciones, integración de Intents, inspector, Quick Look, FileIndex/OCR/semántica, formularios, acciones y planes, Spotify OAuth/App Remote, mecanismos de WhatsApp con borradores y RemoteInput, y preferencias/permisos. **Existencia en código no equivale a éxito comprobado en dispositivo.** Las capacidades multimedia, comunicación, permisos One UI y efectos de aplicaciones externas requieren pruebas físicas separadas.

`SpotlightPanel.kt` agrupa una parte importante del estado de búsqueda, inspector y UI (unas 1.500 líneas en la base auditada). Se propone extraer responsabilidades por pasos, nunca una reescritura total. `SearchEngine`, `IntegrationRegistry`, `NativeLaunchSession`, FileIndex y ejecutores existentes siguen siendo las fuentes de verdad.

## 2. Alcance elegido: catálogo de 23 mejoras

Cada identificador es estable para el seguimiento; una función «completa» requiere sus propios criterios verificables, no solamente su apariencia.

### 2.1 Funcionalidad (10)

| ID | Mejora aprobada para planificar | Entrega concreta | Dependencia / límite |
|---|---|---|---|
| **F01** | Integraciones profundas | Panel de acciones disponibles por app, con estado y destino verificados; primera integración vertical completa | C2; solo APIs/intents públicos y autorizados |
| **F02** | WhatsApp | Elegir destinatario exacto y personal/Business, revisar texto y usar el mecanismo de envío/respuesta disponible | C3; RemoteInput solo si hay notificación compatible; no lectura integral de chats |
| **F03** | Spotify | Búsqueda y ficha musical con playback controlado, reconexión y `PlayerState` observado | C4; SDK oficial, sesión y cuenta; solicitud no equivale a audio |
| **F04** | Chromecast / multimedia | Descubrimiento, selección, conexión, control y desconexión de Cast en sesiones compatibles | C7; no control arbitrario de apps receptoras ajenas |
| **F05** | Documentos con citas | Consulta sobre archivos autorizados con fragmento, nombre, referencia y Quick Look; página solo si se conoce | C5; reusar FileIndex/OCR; no fabricar citas |
| **F06** | IA operativa | Convertir intención en plan editable de herramientas reales; distinguir preparado, solicitado y verificado | C6; consentimiento por efecto y datos salientes |
| **F07** | Búsqueda contextual | Relevancia por contenido autorizado y situación actual, conservando exacto > prefijo y resultados estables | C2; privacidad, latencia y determinismo |
| **F12** | Controles del sistema | Volumen/brillo/accesos compatibles y controles de medios reales, con explicación de restricciones | C7; permisos Android/Shizuku opcionales; no suponer volumen más fino del permitido |
| **F14** | Contenido compartido | Acciones al recibir texto/archivo con Android Sharesheet y Process Text; fuente revisable | C2/C6; solo contenido explícitamente compartido |
| **F15** | AppFunctions | Estudio de viabilidad y prueba aislada de capacidades autorizadas en versiones compatibles | C8; permisos y privilegios; podría terminar en «no viable» |

### 2.2 Estética (9)

| ID | Mejora aprobada para planificar | Diseño deseado | Condición esencial |
|---|---|---|---|
| **V01** | Obsidian Glass 2.0 | Grafito profundo, capas sobrias, bordes finos, selección satinada y contrastes consistentes | Sin blur costoso ni sobreescritura de preferencias |
| **V02** | Inspector contextual | Composición adaptada a app, contacto, canción, documento o plan | No añadir pantallas completas innecesarias |
| **V03** | Acentos por aplicación | Acento discreto derivado de icono/marca en inspector y acciones | Nunca convertir todo el fondo ni comprometer contraste |
| **V04** | Jerarquía de resultados | Título, descripción, tipo y acción de forma más legible y compacta | Objetivos táctiles adecuados; relevancia intacta |
| **V05** | Footer contextual | Acciones primarias y atajos según selección con estados disponibles | Sin cambio sorpresivo de Enter/Tab/Esc |
| **V06** | Ventana adaptable | Inicio compacto y expansión fluida por tarea/IME/inspector | Sin pérdida de foco, scroll ni saltos en WindowManager |
| **V07** | Iconografía coherente iOS | Un vocabulario de iconos consistente con identidad de apps reconocible | Verificar licencia de iconos; no sustituir iconos de apps arbitrariamente |
| **V08** | Tarjetas de contenido útiles | Música, documentos, conversión o acciones con controles contextuales | No presentar tarjetas grandes o falsas por defecto |
| **V10** | Microinteracciones y fluidez | Transiciones cortas y coherentes con estados reales | Respetar reducción de movimiento y presupuestos de rendimiento |

**No seleccionados para esta etapa:** F08, F09, F10, F11, F13 ni V09. Conservar las implementaciones existentes relacionadas cuando correspondan, pero **no ampliar su alcance** por iniciativa propia.

### 2.3 Estabilidad y arquitectura (4)

| ID | Mejora | Resultado esperado |
|---|---|---|
| **S01** | Validación One UI real | Pruebas en A9+ y, cuando corresponda, moto g86 de ventana flotante, foco, IME, teclado físico, rotación, accesibilidad y acciones |
| **S02** | Modularización Compose | Separar estados/responsabilidades sin alterar búsqueda, contratos ni estilos aprobados |
| **S03** | Diagnósticos tipados | Estados de capacidad y ejecución: disponible, requiere autorización, preparado, solicitado, verificado, error y cancelado; sin éxito falso |
| **S04** | Rendimiento | Línea base y controles de apertura, búsqueda p95, memoria, OCR, sesiones y tamaño de APK |

## 3. Principios de experiencia y restricciones

1. **Búsqueda universal sin pestañas.** No convertir la barra en un launcher tradicional ni en un chat independiente.
2. **Exactitud primero.** App exacta > prefijo > coincidencia parcial relevante > archivos/contactos > acciones contextuales; no introducir sugerencias de relleno. Inicio con **hasta tres sugerencias útiles**.
3. **Capacidades comprobables.** Cada botón se respalda con una ruta existente, permiso disponible y resultado honesto. No resultados simulados en producción.
4. **Confirmación segura.** Cambiar destinatario, payload, destino o contexto invalida una aprobación previa. Nada de fallback automático; `REQUESTED` no es `VERIFIED`.
5. **Teclado conservado.** Tab autocompleta donde corresponde, Shift+Tab retrocede, Enter solicita la acción seleccionada, Escape cierra el contexto y flechas navegan. Documentar variantes concretas de menús y planes.
6. **Táctil correcto.** Scroll no abre resultados; una fila visualmente compacta mantiene objetivo táctil adecuado; long-press ofrece acciones, no efectos inmediatos.
7. **Privacidad por diseño.** Datos de contactos, chats, archivos, portapapeles y contenido compartido solo con permisos/contexto; sin subida remota silenciosa ni logs sensibles.
8. **Firma e instalaciones conservadas.** Mismo certificado Preview histórico y paquete de la variante, sin generar claves nuevas ni desinstalar el APK previo para «resolver» un conflicto.
9. **No introducir WebView/Nuxt.** Toda UI de producción continúa en Compose; OpenUI sirve únicamente como principio de interfaces estructuradas. RAGFlow remoto no está aprobado ni forma parte de estas 23 mejoras.
10. **Sin gastos sorpresa.** Ningún Actions, Codespace o build remoto se inicia por el mero hecho de recibir este documento. Comprobar cuota, presupuesto, secretos y permiso explícito según el protocolo vigente.

## 4. Arquitectura propuesta: evolucionar sin reemplazar

**Dominio de búsqueda:** mantener `SearchEngine` y sus fuentes; añadir contexto autorizado mediante señales explícitas y razones de ranking instrumentables. Evitar consultas IA por tecla.

**Dominio de acciones:** extender `IntegrationRegistry`, `AppCapabilities`, `NativeLaunchSession` y `ToolExecutor/Registry` sin crear un segundo bus de ejecución. Fijar destino, revalidar permisos antes de efecto y separar fallo conocido de resultado incierto.

**Dominio UI:** extraer gradualmente del panel raíz los estados de búsqueda, selección/inspector, borradores, revisiones y presentaciones. Las nuevas piezas Compose deben usar los tokens actuales. Priorizar contratos entre ViewModel/estado/efecto, no dividir archivos solo por cantidad de líneas.

**Dominio de datos:** conservar `FileIndex`, SAF, OCR, extractores y vectores locales; almacenar citas con procedencia verificable (ID del documento, huella, fragmento/offset, página si realmente está disponible). Una revocación debe invalidar acceso y resultados asociados.

**Integraciones:** adaptadores verticales aislados por app con capacidades declaradas, requisitos, observaciones de estado y errores recuperables; no asumir equivalencia entre app instalada y permiso para controlar sus funciones.

**Sesiones:** `MediaSession`/Spotify App Remote y Cast se administran por ciclo de vida, con cancelación, liberación y observación de conexión. Un control «en reproducción» debe reflejar una sesión real.

## 5. Plan por checkpoints y puertas de aprobación

### C0 — Congelar referencia, prueba física inicial y línea base

**Códigos:** S01, S03, S04. **Predecesor:** ninguno. **Tipo:** auditoría y medidas, sin cambios de producto.

**Trabajo:** comprobar SHA/identidad de la base, protocolo de reserva, estado remoto e instalación; documentar en tablet los recorridos abrir overlay → buscar app → Enter → regresar; PDF/Quick Look y scroll; teclado físico/IME; menú contextual; permiso revocado/alternativa; inicio en frío y reapertura. Medir tiempos y memoria en condiciones reproducibles. Documentar límites de Spotify/WhatsApp sin enviar mensajes ni realizar operaciones sensibles.

**Cierre:** matriz esperado/observado con capturas o video corto por fallo; métricas base y lista clasificada de fallos reproducidos. Si no hay acceso al dispositivo, cerrar **solo** la parte estática y marcar C0 físico «pendiente», nunca «aprobado».

**Riesgo:** atribuir a la nueva versión problemas de configuración, permisos o datos previos. **Puerta:** autorización para utilizar dispositivos/cuentas reales; no recopilar datos sensibles.

### C1 — Arquitectura modular y diseño base

**Códigos:** S02, S03, V01, V04, V07. **Depende de:** inventario C0. **Tipo:** desarrollo técnico + visual en commits separados.

**Trabajo:** proponer divisiones de `SpotlightPanel` con pruebas de comportamiento, evitar refactor masivo; preparar dos propuestas visuales («Obsidian Refinado» y «Obsidian Adaptive») en tablet/teléfono, oscuro y claro existente. Definir tokens para superficie/borde/foco, tipografía, jerarquía y catálogo de iconos con licencias. Ajustar resultado, estado vacío y foco, sin alterar ranking ni acciones.

**Cierre:** selección de una variante **aprobada antes de codificar estética**; antes/después de las mismas escenas; pruebas de teclado, resultados, permisos, menús, texto 200%, RTL y scroll. No regresión funcional y fila compacta con objetivo táctil apropiado.

**Riesgo:** cambiar demasiado la interfaz y perder la geometría conocida. **Mitigación:** preferir «Obsidian Refinado» para barra/resultados y reservar lo adaptativo al inspector.

### C2 — Búsqueda contextual e integraciones universales

**Códigos:** F01, F07, F14, V02, V05, V06. **Depende de:** contratos/visual C1.

**Trabajo:** añadir un catálogo de capacidades por app basadas en disponibilidad real; implementar **un solo recorrido vertical** primero (buscar una app real → abrir inspector de acciones → ejecutar o mostrar requisito → volver). Conservar ranking exacto y consultas locales sin IA remota por carácter. Incorporar `Share`/`Process Text` explícitos. Crear footer contextual y expansión de ventana sin perder foco ni estado.

**Cierre:** app exacta sigue primero; acción válida una sola vez; ausente/permiso revocado muestra recuperación sin fallback; Share recibe contenido autorizado sin copiarlo a otro destino; pruebas físicas con IME/rotación. Dos variantes visuales del inspector/footer **antes del cambio UI**.

**Riesgo:** falsos positivos de capacidades o retardo de búsqueda. **Mitigación:** feature flags, telemetría local sin datos y regresiones del ranking.

### C3 — WhatsApp con destinatarios inequívocos

**Códigos:** F02, V02, V08. **Depende de:** revisión de acciones C2.

**Trabajo:** reutilizar formularios actuales; resolver contactos homónimos y WhatsApp personal/Business; revisar y fijar destinatario, contenido y mecanismo. Permitir `RemoteInput` solamente cuando una notificación vigente expone respuesta; si no, continuar al borrador/app. Crear tarjeta Compose discreta de revisión y respuesta.

**Cierre:** cambiar destinatario invalida consentimiento; Escape/Back/X cancelan; dos confirmaciones no duplican solicitud; rechazo de permiso/no notificación se explica; no declarar «entregado» sin evidencia. Pruebas de casos ficticios primero y validación física con autorización específica para cualquier envío.

**Riesgo:** privacidad y APIs internas de WhatsApp. **Regla:** no usar ingeniería de interfaces privadas para leer todos los chats ni enviar silenciosamente.

### C4 — Spotify y observación multimedia

**Códigos:** F03, V03, V08. **Depende de:** C2 y pruebas de cuenta/dispositivo.

**Trabajo:** auditar SDK OAuth/App Remote actual antes de modificarlo; reproducir reportes de corte/crash; revisar login, sesión/reconexión y `PlayerState`; mejorar resultado musical con portada, título, artista, controles disponibles y estados. Acentos de app contenidos solo en inspector y control, con contraste.

**Cierre:** búsqueda Spotify no crashea; se diferencia sin sesión, autorización, offline y app no instalada; play/pause/skip solicitados una sola vez; estado en UI coincide con estado observado, no con click; sesión/callbacks liberados al cerrar. Capturas de conectado/desconectado/cargando/error.

**Riesgo:** cuenta, restricciones de API, conectividad y ciclo de vida App Remote. **No** modificar SDK o fabricar reproducciones sin caso reproducido.

### C5 — Documentos locales con respuestas y citas

**Códigos:** F05, V02, V08. **Depende de:** C2 y contratos de fuente existentes.

**Trabajo:** aprovechar SAF/FileIndex/OCR/semántica. Crear objetos de cita estructurados, evaluar relevancia con pequeño corpus autorizado, mostrar fragmento literal y título/fuente/huella; incluir página solamente cuando la extracción permita probarla. Quick Look abre el archivo autorizado desde acción explícita. Diseñar una tarjeta de cita legible.

**Cierre:** tres consultas reproducibles devuelven fuentes correctas; cita copiada conserva procedencia; OCR incierto se marca; revocar/borrar/cancelar invalida hit; sin red la búsqueda local sigue útil. Probar PDFs escaneados, largos y selección/scroll sin apertura accidental.

**Riesgo:** alucinaciones de página/cita y costo de memoria/OCR. **No** conectar RAGFlow automáticamente.

### C6 — IA operativa y contenido compartido

**Códigos:** F06, F14, V02, V05. **Depende de:** C2, C3 y C5 (al menos sus contratos probados).

**Trabajo:** conectar intención en lenguaje natural con `ToolRegistry`/planes actuales, no introducir otro motor. Mostrar plan editable con fuentes y destino. Preparar → revisar → confirmar → solicitar → observar/verificar. Encadenar solo capacidades disponibles; una acción posterior no se ejecuta automáticamente si falla o queda incierta la anterior. Adaptar inspector y footer al estado del plan.

**Cierre:** comando compuesto «buscar documento → resumir con cita → preparar mensaje» conserva identidad, permisos y edición; cancelar en cualquier etapa no provoca efectos futuros; salida de IA no equivale a acción Android; costes/red/datos expuestos claros y consentidos.

**Riesgo:** inferencias incorrectas, exfiltración y duplicación. **Mitigación:** política de herramientas permitidas, límites, confirmaciones y logs minimizados.

### C7 — Chromecast, controles multimedia y sistema

**Códigos:** F04, F12, V08. **Depende de:** C1/C2; independiente de C3–C6.

**Trabajo:** investigar Google Cast Sender SDK compatible; descubrir dispositivos en red y sesiones propias/autorizadas; conectar, controlar reproducción/volumen/seek cuando receptor lo admite y desconectar. Construir panel de disponibilidad de volumen/brillo/medios/Bluetooth; usar Android oficial y Shizuku solo si está autorizado. No imponer overlays de volumen extra.

**Cierre:** dispositivo ausente/no compatible se distingue de error de red; conexión/estado controlados se observan físicamente; limpieza al salir; permisos explicados; comandos bloqueados no simulan éxito. Probar teléfono y tablet, Wi-Fi distinta, desconexión y reconexión.

**Riesgo:** Cast no controla arbitrariamente YouTube/Spotify ni sesiones de terceros. Controles del sistema sujetos a API y permisos.

### C8 — AppFunctions: estudio de viabilidad aislado

**Códigos:** F15. **Depende de:** C2 y comprobación de entorno Android.

**Trabajo:** revisar permisos y SDK/target actuales, reglas de invocación de aplicaciones compatibles y casos verificables. Crear informe técnico y, solo con autorización expresa posterior, prototipo aislado que no sustituya Intents ni se integre automáticamente al producto.

**Cierre:** matriz soportado/no soportado, requisitos de permisos, ejemplo compatible reproducible o decisión «no viable» con evidencia. Ninguna elevación de privilegios ni promesa de control universal.

**Riesgo:** privilegios de sistema/firmware y APIs cambiantes. **Regla:** puede cancelarse sin afectar el resto de la hoja de ruta.

### C9 — Rendimiento, accesibilidad y consolidación

**Códigos:** V10, V06, S01, S02, S04 (y regresión transversal). **Depende de:** integraciones que efectivamente hayan sido aprobadas.

**Trabajo:** revisar animaciones/IME, contrastes, densidad, TalkBack, teclado, estado de selección, consumo estable y artefactos; limpiar deuda derivada sin rediseños inesperados. Comparar métricas C0 y C9. Preparar candidate release con APK/ZIP solo con autorización explícita y firma histórica validada.

**Cierre:** no regresiones críticas en tablet y teléfono, casos manuales documentados, tiempos/memoria comparables, accesibilidad operativa y notas honestas de limitaciones. Los casos de AppFunctions no viables no bloquean la estabilidad.

**Riesgo:** acumulación de cambios en una sola APK. **Mitigación:** checkpoints independientes y pruebas físicas progresivas.

## 6. Dependencias críticas y trabajo paralelo

```text
C0: referencia / evidencia real
  └─ C1: arquitectura / tokens / propuesta visual
      └─ C2: búsqueda + integraciones + inspector/footer
          ├─ C3: WhatsApp ───┐
          ├─ C4: Spotify     │
          ├─ C5: documentos ─┼─> C6: IA operativa (C2+C3+C5)
          ├─ C7: Cast/sistema│   (C4 útil, no obligatorio)
          └─ C8: AppFunctions (viabilidad aislada)
                    └───────┴─> C9: consolidación y pruebas finales
```

La **línea técnica** puede preparar contratos, regresiones y datos de prueba mientras la **línea visual** produce comparativas, siempre sin modificar la interfaz grande antes del visto bueno del usuario. No ejecutar dos cambios concurrentes sobre los mismos archivos ni permitir reservas simultáneas incompatibles. Un checkpoint cerrado debe permanecer reversible y conservar un SHA claro.

## 7. Protocolo de aprobación visual obligatorio

**Antes de C1 y de toda nueva vista mayor (C2/C3/C4/C5/C7):** entregar dos variantes de composición sobre **las mismas escenas y estados**, con referencia de Preview 60. Recomendación inicial: A «Obsidian Refinado» para barra/resultados, B «Obsidian Adaptive» solo como opción para inspector/tarjetas. La selección definitiva pertenece al propietario.

**Escenas mínimas:** tablet oscuro: inicio con hasta tres sugerencias, resultados, inspector, ActionPanel; tablet tema claro existente; teléfono 320–360 dp; menú con títulos largos y texto 200%; teclado táctil y físico; scroll hasta última acción; acciones cargando/error/sin permiso/revisión/cancelado; tarjetas de WhatsApp, Spotify y PDF. La propuesta se valida también con ventana real superpuesta y teclado visible; una captura Robolectric no reemplaza One UI.

**Elementos a aprobar:** medidas de barra (56 dp nominal como referencia a confirmar), espaciado, jerarquía, color, foco, iconos, footer, altura máxima, transiciones, ancho del inspector y comportamiento al IME. No cambiar globalmente el tema por el color de una app; acentos contenidos a selección/controles/inspector.

**Secuencia:** concepto comparativo → decisión del usuario → implementación Compose nativa → capturas reales antes/después → pruebas dirigidas → verificación física → aceptación/rechazo. Si el usuario rechaza una variante, volver a propuesta sin reescribir la arquitectura.

## 8. Matriz de pruebas y umbrales

| Dimensión | Control / criterio |
|---|---|
| Relevancia | App exacta siempre sobre acciones secundarias; no sugerencias iniciales irrelevantes |
| Acciones | Un intento por confirmación; sin fallback oculto; errores/permisos observables |
| Teclado | Tab/Shift+Tab/Enter/Escape/flechas, sin doble ejecución ni pérdida de foco |
| Táctil | Scroll no abre; última acción alcanzable; long-press sin efecto irreversible |
| Variantes visuales | Tablet/teléfono, oscuro/claro existente, texto 200%, teclado/rotación, estados de error |
| Accesibilidad | Semántica, orden de foco, lectura larga, contrastes medidos y comprobación TalkBack física pendiente donde aplique |
| Búsqueda | p95 y exactitud frente a base C0; objetivo sugerido: no empeorar >10% sin justificación aprobada |
| Apertura overlay | Medir cold/warm en mismo dispositivo; objetivo sugerido: no empeorar >10% |
| Memoria | Sin crecimiento sostenido al abrir/cerrar; liberar sesiones y tareas canceladas |
| Documentos | Corpus verificable; OCR y citas con confianza/procedencia; revocación y modo offline |
| Mensajería | Nunca afirmar entrega a partir de intento; no enviar sin autorización específica |
| Música/Cast | Mostrar estado observado; limpiar sesión y distinguir ausencia de permisos/conexión |
| Firma/entrega | Package, `versionCode`, SHA fuente, APK/ZIP fuente, firma histórica, compatibilidad de actualización y comprobaciones ELF/16 KiB cuando correspondan |

Los objetivos de **10%** son umbrales **propuestos**, no mediciones disponibles. Las mediciones de C0 decidirán objetivos definitivos. Ante una mejora con costo de memoria o latencia inevitable, documentar el intercambio y solicitar aceptación.

## 9. Riesgos y mitigaciones transversales

| Riesgo | Nivel | Mitigación obligatoria |
|---|---|---|
| Repetir Preview 58/59 con rediseño que rompe funciones | Crítico | Compose nativo, prototipos visuales previos, no WebView, aprobación por escena |
| Pérdida de firma/datos instalados | Crítico | Keystore histórica Preview, verificar certificado, no generar debug nueva, instalación siempre manual |
| Ejecutar mensaje/acción equivocada | Crítico | Destino tipado, snapshot inmutable, revisión, consumo único y estado honesto |
| Acceso indebido a chats/archivos | Alto | Consentimiento granular, permisos reales, no backend oculto ni logs privados |
| Controles multimedia que muestran éxito falso | Alto | Requerir observación de sesión y distinguir `REQUESTED` de `VERIFIED` |
| AppFunctions sin privilegios válidos | Alto | Investigación aislada y salida «no viable» permitida |
| Búsqueda más lenta o menos pertinente | Alto | Baseline, presupuesto p95, regresiones de ranking exacto y cancelación |
| R8/CI consume tiempo o falla | Medio-alto | Builds deliberados, perfil registrado, cero reintentos automáticos, diagnóstico actual antes de `optimize=false` |
| Cita inventada o página incorrecta | Alto | Fragments y fuentes exactas; no inferir página desde un offset |
| Deriva visual entre móvil y tablet | Medio-alto | Matriz de escenas, tokens compartidos y revisión de IME/window overlay |

## 10. Normas para Codex: autonomía y cambios

**Instrucciones de trabajo obligatorias, cuando se autorice comenzar:**

1. Leer `AGENTS.md`, `PLANS.md`, `docs/AUTONOMIA-RESERVA.md`, la auditoría nativa y las notas del PR #21; verificar remoto y worktrees. Antes de escribir, adquirir **solo** una reserva propia y liberarla exclusivamente según el protocolo.
2. No modificar `main`, no fusionar PR #19/#20, no sobrescribir cambios locales ajenos, no borrar Codespaces/branches/checkpoints, no incorporar los experimentos WebView.
3. Por checkpoint, documentar objetivo, prerequisitos, archivos que realmente se tocarían, nueva API, amenazas, casos rojos previos, criterios de cierre y comparación de comportamiento.
4. Cada implementación en commits pequeños; pruebas dirigidas después de cada límite; evitar suites completas, CI, capturas repetidas y APK innecesarias. Las matrices históricas no cuentan como pruebas nuevas.
5. Para estética, **presentar primero** variantes de Preview 60 y esperar aprobación. No usar recursos web externos no autorizados ni sustituir el arte/identidad sin verificar licencias.
6. Para integraciones, comprobar el comportamiento de las aplicaciones reales, permisos y autorización. No simular efectos externos ni hacer envíos/transferencias en pruebas reales sin permiso.
7. Para entrega, mantener firma histórica Raycast `885838aa…`, paquete e identidad apropiada. Firma y compilación solo con autorización de entrega vigente y verificación de presupuesto/cuota.
8. Actualizar documentación y estado del checkpoint con SHA, clases probadas, Lint, diferencias visuales, limitaciones físicas y reserva liberada. No afirmar «terminado» si falta validación de dispositivo.
9. No iniciar servicios remotos de RAGFlow, no introducir OpenUI React/Vue ni solicitar secretos en el chat. Integraciones de IA existentes se usan solo con la autorización ya configurada.
10. Si un requisito no es viable con las APIs/privilegios reales, reportar el impedimento y una alternativa limitada; **no** esquivar restricciones con permisos o automatizaciones invasivas.

## 11. Entregables esperados por etapa

| Momento | Entregable | Requiere aprobación expresa |
|---|---|---|
| Inicio de fase | Auditoría de alcance, dependencias, pruebas, estimación de impacto y riesgos | No para redactar; **sí** antes de editar |
| Antes de vista nueva | Dos variantes comparadas, escenas y diseño adaptativo | **Sí** |
| Después de implementación | Diff, pruebas, Lint, capturas reales, SHA, estado de reserva | Revisión de aceptación |
| Antes de datos privados o servicio externo | Declaración de datos salientes, permisos, retención y costes | **Sí** |
| Antes de build remoto/APK | Identidad, fuente, firma, presupuesto y plan de entrega | **Sí**, salvo autorización específica vigente y verificable |
| Antes de fusionar `main` | Auditoría del conjunto y checkpoint estable | **Sí** |

## 12. Próxima acción solicitada a Codex (SOLO LECTURA Y DISEÑO)

**Primera entrega de Codex al recibir este documento:** verificar el estado real del repo de forma **read-only**, señalar diferencias entre el plan y el código actual, producir una propuesta de C0 y las **dos alternativas visuales de C1** para barra/resultados/inspector (tablet y móvil, oscuro y claro existente), y presentar un diseño de separación modular sin tocar la app. Adjuntar matriz de riesgos y qué evidencia física necesita del propietario. **Detenerse antes de implementar** y solicitar aprobación sobre una variante concreta.

**No autorizado ahora:** editar fuentes, commits, PR nuevos, ramas nuevas, generar/modificar APK, ejecutar tests/compilaciones/Actions, instalar, conectar cuentas, transferir documentos o alterar presupuesto/secretos.

---

**Decisión de dirección:** las 23 mejoras quedan **aprobadas como alcance de planificación**, no como autorización de ejecución. Prioridad permanente: **funcionalidad y estética en paralelo**, con rendimiento, seguridad y aprobación visual como restricciones. La fase C0 y las propuestas visuales C1 son lo siguiente a presentar al propietario.
