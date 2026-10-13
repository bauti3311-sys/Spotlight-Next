# Spotlight Next — hoja de ruta maestra y continuidad autónoma
Actualizado: 2026-10-13 · Propietario: usuario del proyecto · Objetivo: Samsung Galaxy Tab A9+ (One UI), Android nativo sin root.

> Este documento es el **orden de trabajo y mecanismo de continuidad**, no una declaración de que las funciones estén listas. El contrato detallado está en `docs/RENOVACION-APROBADA.md`; las reglas en `AGENTS.md`; el entorno y herramientas en `docs/CODEX-ENTORNO-APROBADO.md`; el estado técnico en `PLANS.md`. Ante una contradicción, preservar seguridad/contrato y explicitarla antes de modificar comportamiento.

## Norte del producto
Una **única barra flotante** tipo macOS Spotlight/Raycast/Alfred, invocable por gesto/atajo, rápida y elegante. El usuario escribe una intención; recibe resultados y acciones útiles sin saltar entre modos. IA integrada como segunda capa bajo demanda, búsqueda local primero; Quick Look nativo; integraciones Android reales dentro de los permisos y límites de Android. App nativa Kotlin/Jetpack Compose, sin webview React para su interfaz principal.

## Punto de partida probado / límites conocidos
- APK histórico Spotlight Next 2.0.1 / versionCode 28: fuente exacta `9bb9d0ca7a97e9798ae5197c3ee32958e643dd6d`, rama `fix/recover-2.0.1-ci`, PR #6 borrador. La firma y el ZIP exacto se verificaron en GitHub Actions [38329360568](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/38329360568).
- Infraestructura avanzada en `chore/codex-infrastructure-2.0.1`, PR #7 borrador, a la fecha referencia `83e887a`, CI [38337171465](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/38337171465) correcto. Comprobar **la punta actual** de esta rama y continuar desde `checkpoint/autonomous-next` validado, manteniendo infraestructura como ancestro; leer `PLANS.md`, progreso, PR y `refs/notes/spotlight-autonomy`. No volver al árbol antiguo perdiendo avances sin integrar.
- `main` y PR #5 permanecen intactos. Conservar `checkpoint/documents-2.0.1` y `checkpoint/integrations-2.0.1`.
- La Galaxy Tab A9+ pudo actualizar físicamente; el usuario corrigió una ventana demasiado angosta restableciendo **Ancho horizontal**. Esto demuestra un conflicto de UX entre ancho general, ancho horizontal y densidad "Espaciosa"; NO considerar corregido el diseño de configuración.
- Falta evaluación física sistemática de One UI, batería, accesibilidad y comportamiento con teclado/permisos. Los emuladores NO reemplazan estas pruebas.

## Reglas permanentes de ejecución
1. **Una misión acotada por turno**, de preferencia un subhito de 1–3 archivos o un cambio claramente comprobable, no una renovación de todo el APK. Consultar esta hoja al empezar y marcar avance verificable.
2. **Aislar trabajo** en ramas `feat/...`, `fix/...` o `test/...` basadas en la rama de infraestructura del PR #7 ya validada. PRs como borrador dirigidos a esa base, no a `main`; no ejecutar merges automáticos ni alterar PR #5 / backups.
3. Al finalizar un turno: commit y push de todos los cambios seguros, pruebas relevantes, informe con SHA/PR, capturas reales cuando afecte UI, pendientes, y actualizar `PLANS.md` y el registro `docs/PROGRESO-AUTONOMO.md` (crearlo si falta).
4. Si CI falla, investigar y arreglar **sin desactivar pruebas, quitar aserciones o aceptar baselines nuevos para ocultar defectos**. Informar fallos no resolubles.
5. No bloquear toda la hoja por una validación que requiere la tablet física. Mantenerla como tarea humana separada y seguir con tareas aisladas que sí pueden verificarse.
6. No introducir más de una nueva biblioteca visual por cambio. Revisar licencia, mantenimiento, versión/SDK, impacto en APK, tiempos y accesibilidad; preferir implementación Compose propia si ofrece mejor control.
7. No instalar React/Vite/Playwright en el APK. Web = laboratorio de referencia; Android = aplicación nativa.
8. No usar APIs con costo real en tests; fixtures y MockWebServer. No inventar resultados deportivos, clima, permisos ni integraciones.
9. Conservar package ID, datos, versión incremental y certificado histórico. Firmar **solo** en workflow protegido; nada de claves en código, ZIP ni logs. Publicación protegida, sin gastar dinero ni habilitar servicios externos sin permiso.
10. Ante límite de uso/créditos: **dejar checkpoint**, resumir próxima tarea exacta y detenerse. No suponer que el reset de créditos reinicia automáticamente una ejecución. Retomar solo en ejecución/automatización efectivamente programada.

## Hoja de ruta priorizada
Una casilla `[x]` solo corresponde cuando hay evidencia reproducible del criterio de aceptación.

### F0 · Recuperación y herramientas — BASE DISPONIBLE, mantenimiento continuo
- [x] Recuperar código 2.0.1, corregir import/foco y compilar.
- [x] APK histórico verificable + ZIP de fuente del mismo SHA, mediante workflow autorizado.
- [x] AGENTS, PLANS, 3 skills, fixtures offline, laboratorio web, Roborazzi, Gitleaks, Maestro y CI inicial en PR #7.
- [ ] Confirmar después de cada cambio que CI y baselines siguen verdes; registrar SHA en progreso.
- [ ] Probar en tablet real: orientación, invocación, Enter/Tab, PDF, superposición sobre otras apps, restauración de preferencias y consumo. Depende del usuario.

### F1 · Ventana adaptable y ajustes coherentes — PRIMER HITO DE DESARROLLO
- [x] Unificar semántica de ancho: **automático / manual**, orientación horizontal con override explícito y botón claro de restablecimiento; impedir que un override oculto contradiga visualmente la opción "Automático". F1.1 implementado en PR #8, con persistencia/importación y capturas nativas verificadas localmente; revisar CI y progreso antes de continuar. Validación física One UI sigue separada.
- [x] Separar claramente densidad compacta/equilibrada/espaciosa del **ancho físico**. F1.2 implementado en PR #9 y cierre en PR #10: etiquetas, automático antiguo y preview nativo; corrección5ba1e5d observa filas/selección/Quick Look directamente en el panel, tras reproducir lista anterior pese a recibir filas. Dos suites locales341/341 después, manteniendo25 ciclos/15s y reforzando assertions; captura después/diff real. Producto c6b357c verificado: CI38396241075 cinco jobs correctos y PDF38396240951 tres pruebas Android reales correctas. Leer nota/punta PR #10 para cierre documental; siguiente F1.3. One UI físico e insets separados.
- [ ] Cálculo confiable de métricas actuales/insets/IME en One UI; limitar a pantalla usable y centrar horizontalmente. No fijar un ancho de teléfono a tablet.
- [ ] Ancho adaptable al contenido: barra inicial compacta si se elige, expansión gradual al tener resultados/Quick Look/acciones; sin saltos bruscos ni cambio involuntario de preferencias.
- **Aceptar solo si:** pruebas de valores guardados/rotación/overrides/IME/tablet+teléfono, captura real en los dos tamaños, sin pérdida del ancho que el usuario configuró.

### F2 · Sistema visual nativo premium
- [ ] Tokens Compose centrales: tipografía (Inter si licencia/distribución permitida), espaciado, radio, colores, estados, foco, densidad y transiciones. Iconos reales coherentes (Phosphor/Hugeicons solo subsets con licencia apta).
- [ ] Base estilo Raycast/macOS: alto contraste limpio, sombras discretas, bordes calibrados, modo claro/oscuro, diseño tipo floating command palette.
- [ ] Evaluar **Compose Unstyled** solo donde aporta comportamientos reutilizables; **Haze** para vidrio sobre contenido propio; **AndroidLiquidGlass/Backdrop** en superficies pequeñas si rendimiento/capacidades lo justifican. Fallback matte; NO asumir blur del fondo de otras apps.
- [ ] Prototipos web en laboratorio React/Vite/Playwright: Arc UI, EasyUI, Motion Primitives, MicroKit, SmoothUI, Aceternity; recrear interacción en Compose, nunca incrustar el sitio.
- **Aceptar solo si:** capturas antes/después en tablet y teléfono; contraste/legibilidad, accesibilidad básica, teclado y rendimiento similares o mejores.

### F3 · Barra, ranking visible y panel contextual
- [ ] Un panel de búsqueda unificado, sugerencias no invasivas, mejores coincidencias y categorías útiles sin encabezados redundantes.
- [ ] Enter en resultados **ejecuta acción principal**; Tab entra/recorre acciones, Shift+Tab vuelve, Enter ejecuta acción seleccionada, Esc cierra en capas. Toque directo consistente.
- [ ] Panel derecho contextual, no pantalla fija; tamaño proporcional al contenido y al dispositivo. No mostrar "Abrir en otra app" ni "Sin vista previa" para resultados que no lo admiten (p. ej., calculadora `2+2=4`: mostrar resultado y copiar).
- [ ] Indicador de selección suave tipo Raycast, sin saltos de scroll/resultado bajo el dedo; accesos visibles en pantalla táctil donde tengan sentido.
- **Aceptar solo si:** Maestro/Compose foco-teclado táctil, capturas Roborazzi de estados vacío/cálculo/acciones, pruebas de no regresión.

### F4 · Quick Look y trabajo con documentos
- [ ] Vista previa PDF local `PdfRenderer` al seleccionar; navegación de páginas, caché cancelable, carga/error/vacío, expansión dentro de overlay; no abrir externo hasta acción expresa.
- [ ] Imágenes, metadatos y texto/documentos compatibles, sin prometer vista previa de formatos no soportados.
- [ ] Alfred File Buffer: selección múltiple persistente durante la búsqueda, bandeja solo cuando tiene elementos, compartir con `ACTION_SEND_MULTIPLE`/URIs concedidos correctamente, sin permisos excesivos.
- **Aceptar solo si:** pruebas PDF nativas en emulador, estrés de cierre/cambio de documento, ausencia de fugas, capturas PDF/cargando/error y regresión de navegación.

### F5 · Motor universal y velocidad de búsqueda
- [ ] Indexación incremental y consultas SQL acotadas para archivos compartidos, nombres/contenido cuando hay permiso, apps, contactos, notas, resultados y acciones.
- [ ] Interpretar localmente lenguaje natural y filtros: `PDF de ayer`, `fotos de esta semana`, `abrir WhatsApp`, cálculos/conversiones; ranking exacto primero, sin resultados mezclados ni falsos.
- [ ] Actualizaciones asíncronas cancelables, estabilidad de filas y foco; índices y búsquedas con límites, sin escanear cientos de miles de filas en el hilo principal.
- **Aceptar solo si:** tests de relevancia, filtros temporales, concurrencia e indexación, mediciones de latencia reproducibles y permiso denegado.

### F6 · IA dentro de la búsqueda, no modo obligatorio
- [ ] Primera capa de comandos/intent local rápida; consulta remota solo para intención compleja o acción explícita, nunca una petición por tecla.
- [ ] Respuestas en línea, Markdown/fragmentos estructurados, contexto opcional, alternativas locales sin clave, errores/red/cuota claros.
- [ ] Conjunto de herramientas con previsualización/confirmación de acciones sensibles; no enviar portapapeles, archivos o contactos automáticamente a proveedores.
- [ ] API key en almacenamiento seguro, controles de proveedor, costo y privacidad; mocks offline exhaustivos.
- **Aceptar solo si:** pruebas de intención/cancelación, tráfico saliente cero al teclear, fixtures errores y UI integrada sin pestañas forzadas.

### F7 · Guardados, portapapeles y productividad
- [ ] Guardados como espacio único: notas, textos frecuentes/snippets, alias y búsquedas guardadas, editar/borrar/exportar.
- [ ] Portapapeles temporal opt-in, límites de cantidad/tiempo, nunca registrar contenido sensible ni activar watcher global silencioso.
- [ ] Integración con compartir/selección de texto Android, acciones de copiar, traducir, resumir, mensajes con revisión.
- **Aceptar solo si:** pruebas de persistencia, privacidad, expiración, respaldos y uso sin red.

### F8 · Integraciones Android auténticas
- [ ] Lanzamiento de aplicaciones/shortcuts/actividades autorizadas; contactos, WhatsApp mediante intents y flujos soportados, llamadas/mensajería con confirmación apropiada.
- [ ] Controles de brillo/sonido/sistema: APIs públicas o Shizuku **solamente al pedirlo** y con permisos; estados "no compatible" claros.
- [ ] Notificaciones, medios y búsqueda contextual según privilegios que el usuario haya otorgado. No prometer acceso a carpetas privadas de otras apps, envío silencioso o automatizaciones imposibles sin root.
- [ ] Retirar modos, catálogo de extensiones o vínculos manuales que solo agreguen complejidad; conservar IA/deportes/clima configurables.
- **Aceptar solo si:** pruebas permisos permitidos/denegados, intents reales y regresión de datos.

### F9 · Calendario nativo y agenda personal
- [ ] CalendarContract leído/escrito con permiso, día/semana/mes en panel, creación/edición con confirmación de cambios.
- [ ] Ver conflictos reales y proponer alternativas sin inventar disponibilidad; zonas horarias, eventos completos y repetición si la API lo permite.
- **Aceptar solo si:** tests permisos, conflicto, zona horaria y flujos de guardar/cancelar.

### F10 · Deportes y clima útiles y verificables
- [ ] Fútbol, NBA/básquet, UFC y tenis ATP/WTA con datos de proveedor real, filtros por deportes/equipos favoritos y agenda de partidos relevantes; cero partidos genéricos inventados.
- [ ] Fuentes de API configurables y fallbacks honestos; estados retrasado, suspendido, error, cuota, desfase temporal.
- [ ] Clima Apple Weather como referencia estética (sin implicar acceso gratuito a su API), pronóstico por ciudad y ubicación bajo demanda; gráficos Vico si suma valor.
- [ ] Notificaciones de resultados/alertas severas solo opt-in y con batería razonable.
- **Aceptar solo si:** fixtures de APIs con casos límite, origen y actualización visibles; no atribuir datos ficticios a partidos en vivo.

### F11 · Personalización y accesibilidad
- [ ] Tema limpio, oscuro/claro, acento, blur/matte, espaciados, tipografía, tamaños, animación reducida y selección animada. Menos opciones redundantes; organización clara.
- [ ] Invocación por teclado/atajos y gestos compatibles con One UI, sin capas permanentes innecesarias. TalkBack, tamaños táctiles, contraste, orientación y teclado virtual.
- **Aceptar solo si:** tests de persistencia/reversión de temas, capturas comparables y ausencia de obstáculos para uso sin mouse.

### F12 · Rendimiento, calidad, batería y seguridad
- [ ] LeakCanary **solo debug** para OverlayService/PdfRenderer/Compose; pruebas de abrir/cerrar repetidas.
- [ ] Perfetto y mediciones de apertura, tiempo al primer carácter, FPS/jank, memoria e índice; Macrobenchmark/Baseline Profiles cuando el módulo y emulador estén listos.
- [ ] Lint, revisión de seguridad Gitleaks, dependencias y licencias; ktlint/Detekt graduales, Renovate sin auto-merge de stack.
- [ ] Pruebas de regresión de Android 26–35 según soporte real, One UI y tablets; manejo offline, errores y cambios de permisos.
- **Aceptar solo si:** evidencia comparativa con baseline, sin regresiones graves de memoria/batería/latencia.

### F13 · Entrega autónoma controlada
- [ ] CI checks obligatorios en PR: compilación/lint, test unitarios, visuales, Maestro emulador, Python, seguridad, PDF instrumentado según cambio.
- [ ] Firma histórica en flujo protegido solamente; comprobar `applicationId`, SHA del certificado, versión mayor que instalada y prueba de actualización.
- [ ] APK y ZIP exacto de un mismo commit, `sha256`, reporte y capturas; publicación privada/permanente en GitHub Releases **tras aprobación y verificaciones**.
- [ ] Simplificar a un enlace de descarga sin que el usuario tenga que entrar en Actions; automatizar lo seguro, NO auto-merge arbitrario ni lanzar release con códigos sin validar.
- **Aceptar solo si:** simulación end-to-end sobre rama protegida, artefactos accesibles y que el APK actualice sin desinstalar.

## Política por ciclo de Codex (incluidos turnos nocturnos)
**Antes:** leer `checkpoint/autonomous-next` y su nota final; comprobar/adquirir atómicamente `locks/spotlight-autonomy` con propietario único antes de editar. Si una reserva ajena sigue activa o no se demuestra cierre, omitir edición y registrar el solapamiento en el hilo, sin borrarla por antigüedad. Confirmar rama y SHAs, verificar que el PR #7/infraestructura actual es la base; leer AGENTS, PLANS, roadmap, avance, último CI. No rehacer hitos ya completados.

**Elegir:** el PRIMER subhito pendiente que no dependa de autorización humana, compra, acceso a tablet física o clave. Concentrarse en ese subhito. Registrar hipótesis de riesgo y criterios de aceptación antes de tocar código.

**Ejecutar:** cambio incremental, tests pertinentes, capturas/diff si hay UI, arreglar errores, commit/push. No integrar `main`, no firmar fuera de workflow autorizado, no tocar PRs/checkpoints históricos.

**Finalizar:** actualizar casilla solo si evidencia, `docs/PROGRESO-AUTONOMO.md` y `PLANS.md`; exponer SHA, PR, tests exactos, artefactos y siguiente subhito. Cuando quede bloqueado registrar por qué y pasar a otro subhito **solo si es independiente**.

**Uso/créditos:** no solicitar compras, nuevas suscripciones, credenciales ni resets de pago. Preferir un cambio probado a cinco implementaciones incompletas. Si un turno se aproxima al límite, cerrar con checkpoint y tarea exacta pendiente. No hay detección garantizada del instante en que se renueva la cuota; depende de la automatización y de los controles reales de Codex.

**Cadencia solicitada y autorizada por el usuario:** programar **una única automatización de Codex cada 5 horas exactas**, con primer inicio a las **03:05**, zona `America/Argentina/Buenos_Aires`. Primeras ejecuciones: 03:05, 08:05, 13:05, 18:05, 23:05, luego 04:05, 09:05, etc.; **no** cinco horarios fijos diarios, porque eso produciría intervalos de 4 h/20 h entre días. El usuario suele estar disponible de 13:00 a 15:00 y después de 20:00, pero las ejecuciones deben ser autónomas. **La automatización debe crearse y confirmarse dentro de Codex**, no mediante recordatorios de ChatGPT. Si la interfaz solo ofrece horarios diarios y no intervalos de 5 h, informar la limitación y proponer configuración que respete intervalos sin duplicar tareas. No suponer que el reset de créditos reactiva una sesión; detenerse si se agota la cuota, sin compras. Evitar ejecución simultánea sobre la misma rama, revisar el último SHA antes de trabajar, checkpoint + pruebas + push + progreso. Informar cómo pausar/cancelar la recurrencia.

## Formato obligatorio de actualización
```
Fecha/hora y rama:
Hito/subhito:
Estado: PENDIENTE / EN CURSO / VALIDADO / BLOQUEADO
Base SHA -> commit SHA:
Archivos principales:
Checks ejecutados de verdad (comando, éxito/fallo, enlace):
Capturas comparables:
Impacto APK / nueva dependencia y licencia:
¿Se tocó main/firmó/publicó?: NO / detalle aprobado
Bloqueo y siguiente acción exacta:
```

## Definición de terminado
La hoja es completa solo cuando la UX de una sola barra, búsqueda, IA bajo demanda, Quick Look, productividad, integración nativa, calendario, deportes/clima, personalización y distribución funcionan con evidencias de calidad y las excepciones físicas están documentadas. **No declarar "Spotlight terminada" por CI verde de un único hito.**
