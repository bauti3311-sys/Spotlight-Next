# SPOTLIGHT NEXT — RENOVACIÓN INTEGRAL
## Especificación maestra definitiva para ChatGPT Work

### 0. MISIÓN Y FORMA DE TRABAJO

Sos el equipo responsable de desarrollar, diseñar, probar, compilar y entregar la próxima evolución de **Spotlight Next**, una aplicación Android nativa inspirada en macOS Spotlight, Raycast y Alfred.

Quiero que realices el trabajo de manera autónoma, utilizando mi repositorio privado de GitHub y todas las herramientas disponibles en Work.

**Repositorio:** `bauti3311-sys/Spotlight-Next`  
**Rama principal:** `main`  
**Application ID:** `dev.bautista.spotlight.next`  
**Tecnología:** Kotlin, Jetpack Compose y Android nativo.  
**Dispositivo principal:** Samsung Galaxy Tab A9+ con One UI.  
**Versión de referencia:** 2.0.0, `versionCode 27`; verificar el estado real antes de comenzar.

La misión no es agregar funciones indiscriminadamente. Es crear una aplicación que pueda utilizar todos los días como centro universal de búsqueda y ejecución de acciones.

**Principios innegociables:**

1. Búsqueda verdaderamente universal.
2. Respuesta rápida y resultados relevantes.
3. Inteligencia integrada naturalmente.
4. Diseño visual excepcional.
5. Navegación sencilla y consistente.
6. Integración real con Android.
7. Privacidad y consumo de batería controlados.
8. Estabilidad comprobable.
9. Entregas instalables, no únicamente código.

No me preguntes por detalles pequeños. Investigá, resolvé, implementá y avanzá. Consultame solamente por decisiones realmente importantes, bloqueos que requieran mi intervención o gastos externos.

Tenés libertad para buscar código, componentes, bibliotecas, APIs, buenas prácticas y experiencias de otros desarrolladores en GitHub, Reddit, foros, documentación oficial y proyectos open source.

No dependas de una única biblioteca porque ya exista en el proyecto. Elegí la mejor solución compatible con nuestras necesidades, revisando mantenimiento, licencia y calidad.

**Aprovechá esta ejecución de Work para avanzar sobre todo el alcance aprobado.** Organizá el trabajo en hitos internos con compilaciones verificables, pero no te detengas artificialmente al completar el primero si todavía podés continuar de manera segura.

No declares finalizada ninguna característica que no esté implementada y comprobada.

---

# 1. VISIÓN DEL PRODUCTO

Spotlight Next debe sentirse como una combinación de:

- **macOS Spotlight:** búsqueda inmediata, simplicidad y Quick Look.
- **Raycast:** acciones contextuales, navegación por teclado e inteligencia práctica.
- **Alfred:** Universal Actions, selección temporal de archivos y rapidez.
- **Apple Weather y Fantastical:** información completa, cuidada y útil cuando corresponde.

Pero debe ser una aplicación **nativa de Android**, no una imitación superficial ni una WebView.

La experiencia principal:

**Escribo lo que quiero → Spotlight entiende → presenta el resultado correcto → Enter ejecuta o responde → Tab ofrece alternativas útiles.**

No quiero estar eligiendo constantemente entre «Aplicaciones», «IA», «Archivos», «Comandos», «Web» y otras categorías.

Las categorías se usarán internamente para organizar resultados, no para obligarme a navegar por ellas.

---

# 2. ENTER, TAB Y PANEL LATERAL — CONTRATO OBLIGATORIO

Esta sección es especialmente importante. No puede simplificarse ni interpretarse libremente.

## 2.1 Enter: acción principal

Cuando el foco esté en la búsqueda o en la lista de resultados:

- Si seleccioné una aplicación, **Enter la abre**.
- Si seleccioné un PDF, **Enter lo abre con la aplicación predeterminada de Android**.
- Si seleccioné un enlace, **Enter abre el destino**.
- Si seleccioné un contacto, Enter ejecuta la acción principal compatible o abre sus detalles según el contexto.
- Si seleccioné una nota, Enter abre su edición o visualización.
- Si mi consulta requiere una respuesta generativa, **Enter inicia la respuesta de IA dentro de Spotlight**, si está configurada y autorizada.
- Si seleccioné un encuentro deportivo, Enter abre su ficha completa **dentro de Spotlight**, no simplemente Sofascore.
- Si seleccioné una acción que modifica información sensible, Enter prepara la operación y solicita la confirmación necesaria.

La acción principal debe ser predecible y estar claramente señalada.

## 2.2 Tab: acciones contextuales

**Tab no debe ejecutar la acción principal.**

Al presionar Tab con un resultado seleccionado:

1. Mostrar su panel de acciones contextuales a la derecha.
2. Mantener visible el resultado y su contexto.
3. Dar foco a la primera acción disponible.
4. Cada Tab adicional avanza a la siguiente acción.
5. Shift+Tab retrocede a la anterior.
6. Enter ejecuta la acción contextual enfocada, solamente mientras el foco está en ese panel.
7. Escape abandona las acciones y devuelve el foco a los resultados.

Esta distinción es fundamental:

**Enter desde resultados = acción principal.**  
**Enter desde acciones = acción contextual seleccionada.**

Nunca crear ambigüedad entre ambos estados.

## 2.3 Navegación con flechas

- Flecha arriba y abajo: cambiar resultado.
- Al cambiar de resultado, actualizar Quick Look cuando corresponda.
- Mantener el foco correcto.
- Evitar saltos de selección cuando llegan nuevos resultados.
- En el panel contextual, las flechas pueden recorrer acciones cuando corresponda.
- Respetar navegación por teclado físico, accesibilidad y controles táctiles.

## 2.4 Escape

Orden de cierre:

1. Acciones contextuales.
2. Vista previa ampliada.
3. Formularios o paneles secundarios.
4. Quick Look, si corresponde.
5. Ventana flotante.

No cerrar Spotlight cuando únicamente quiero salir del panel de acciones.

## 2.5 Autocompletado

No reservar Tab para autocompletar sugerencias si entra en conflicto con el sistema anterior.

Utilizar otro mecanismo predecible, por ejemplo flecha derecha cuando corresponda, o aceptación táctil.

## 2.6 Distribución del panel

**En tablet:**

- Lista de resultados a la izquierda.
- Panel lateral contextual a la derecha.
- Vista previa arriba.
- Acciones relacionadas debajo o en una sección claramente integrada.
- Divisor ajustable cuando sea útil.
- No duplicar contenido de manera innecesaria.

**En teléfono o ancho reducido:**

- Conservar la misma lógica.
- Adaptar el panel lateral a una vista vertical integrada.
- Mantener navegación, selección y acciones.
- Evitar ventanas externas o pantallas completas innecesarias.

El panel debe ser una extensión natural de la barra, no una segunda aplicación.

---

# 3. QUICK LOOK — VISTA PREVIA AUTOMÁTICA

Implementar una experiencia real de Quick Look inspirada en macOS.

## 3.1 Comportamiento táctil

Al tocar un resultado PDF:

**Primer toque: seleccionar el PDF y mostrar inmediatamente su vista previa lateral.**

No abrir automáticamente otra aplicación.

Para abrir el documento externamente, utilizar Enter, una acción contextual explícita o el control «Abrir».

Para aplicaciones normales, conservar el comportamiento táctil de apertura directa cuando corresponda.

## 3.2 Comportamiento con teclado

Al navegar con flechas y seleccionar un PDF:

- Mostrar Quick Look automáticamente.
- No exigir Tab para ver la primera página.
- No abrir el PDF externamente hasta presionar Enter desde los resultados.
- Tab revela las acciones disponibles.

## 3.3 Funciones de Quick Look para PDF

- Renderización local mediante `PdfRenderer` u otra solución nativa apropiada.
- Primera página visible con buena resolución.
- Navegación anterior/siguiente.
- Indicador de página actual y total.
- Posibilidad de ingresar número de página.
- Vista previa suficientemente grande para leer el contenido.
- Ampliación a un lector más amplio dentro de Spotlight.
- Ajuste correcto al tamaño de tablet.
- Caché limitada de páginas cercanas.
- Cancelación de cargas cuando cambio de archivo.
- Sin lectura completa del documento en el hilo principal.
- Manejo de permisos y documentos dañados.
- No enviar automáticamente PDF a servidores o IA externa.

El `FilePreview.kt` actual ofrece una base, pero su presentación compacta no alcanza para nuestro diseño definitivo.

## 3.4 Otros tipos de contenido

Implementar vistas apropiadas para:

- Imágenes.
- Texto plano.
- Documentos Office, cuando su formato y bibliotecas lo permitan.
- Audio.
- Video.
- Enlaces.
- Notas y contenido guardado.
- Resultados de IA.
- Entidades deportivas.
- Eventos de calendario.

No prometer una vista previa completa cuando el formato no sea compatible. Mostrar información relevante y una acción de apertura externa.

## 3.5 Quick Look expandible

Referencias:

- Motion Primitives Morphing Dialog.
- Arc UI Resizable Panels.
- Aceternity Expandable Card.
- Aceternity Notch.

Recrear el comportamiento en Compose:

- La vista pequeña se transforma en una amplia.
- Conservar el elemento y la página seleccionados.
- Transición suave, sin saltos.
- Regresar al panel lateral sin perder contexto.
- Respetar el teclado en pantalla.
- Evitar animaciones pesadas en documentos grandes.

---

# 4. SELECCIÓN MÚLTIPLE — ALFRED FILE BUFFER

Agregar una bandeja de selección temporal para archivos.

Quiero poder seleccionar varios PDF, imágenes o documentos desde los resultados.

**Experiencia:**

1. Seleccionar un archivo.
2. Utilizar «Añadir a selección» desde las acciones, un control táctil o un atajo definido.
3. Continuar buscando sin perder lo seleccionado.
4. Repetir con otros documentos.
5. Mostrar una pequeña bandeja únicamente mientras haya elementos seleccionados.
6. Ejecutar una acción sobre el conjunto.

Acciones:

- Compartir varios documentos.
- Comparar archivos.
- Extraer y combinar texto.
- Preparar resumen de varios PDF.
- Utilizar varios archivos como contexto para IA.
- Quitar elementos individualmente.
- Vaciar la selección.

Los elementos deben conservar sus URI y permisos correctamente.

No copiar archivos innecesariamente ni conservar contenido temporal indefinidamente.

Adaptar el sistema actual, porque `ContextStore` maneja principalmente un único contexto activo.

La selección múltiple debe ser un comportamiento transversal, no una categoría permanente.

---

# 5. BÚSQUEDA UNIVERSAL E INTELIGENCIA DE INTENCIÓN

Actualmente existen varios intérpretes parcialmente independientes.

Investigar y unificar:

- `SearchModifiers`
- `SearchQuery`
- `UniversalIntent`
- `IntentRouter`
- `RootActions`
- `SearchEngine`
- `ResultRanker`
- `ResultSections`
- Componentes relacionados

Construir una interpretación única de consulta, manteniendo las funciones locales existentes que sean válidas.

Identificar:

- Intención.
- Tipo de resultado.
- Entidades.
- Fecha y hora.
- Filtros.
- Acción principal.
- Acciones disponibles.
- Necesidad de búsqueda remota.
- Necesidad de IA generativa.
- Requisitos de permisos o confirmación.

## 5.1 Ejemplos obligatorios

`Spotify`

Resultado principal: Spotify instalado.

`Buscar PDF de ayer`

Mostrar archivos PDF realmente modificados ayer según metadatos y zona horaria local. No buscar un PDF llamado «ayer».

`Buscar fotos de la semana pasada`

Aplicar filtros temporales adecuados.

`Abrir WhatsApp`

Abrir la aplicación instalada.

`Mandale un mensaje a Juan diciendo que llego a las 19`

Preparar el mensaje con el contacto correcto y confirmación antes de enviarlo.

`¿Qué tengo mañana?`

Consultar calendarios autorizados.

`Boca próximo partido`

Mostrar información deportiva pertinente.

`¿A qué hora deja de llover?`

Consultar el pronóstico pertinente.

`Explicame el VOR`

Permitir respuesta de IA integrada al presionar Enter, con el proveedor habilitado.

`Resumí estos tres documentos`

Utilizar los documentos seleccionados y confirmar el envío externo.

## 5.2 Ranking

- Coincidencia exacta como prioridad.
- Ranking contextual por intención.
- Coincidencia aproximada y tolerancia a errores tipográficos.
- Historial de uso local opcional.
- Deduplificación entre proveedores.
- Resultados de mejor calidad antes que cantidad.
- Evitar que comandos genéricos opaquen aplicaciones o archivos relevantes.
- No presentar resultados falsos o inventados.

## 5.3 Estabilidad visual

- Búsquedas asíncronas cancelables.
- Debounce apropiado.
- Evitar resultados que saltan de posición mientras navego.
- Mantener identidad de selección.
- Diferenciar carga, error, permiso y ausencia de resultados.
- Reducir resultados de relleno.
- No reorganizar agresivamente la lista mientras interactúo.

## 5.4 Interpretación contextual

Mostrar chips de interpretación solamente cuando ayuden, por ejemplo:

`PDF` · `Modificado ayer` · `Archivos locales`

No mostrar chips innecesarios cuando simplemente escribo `Spotify`.

Permitir modificar filtros de manera sencilla cuando corresponda.

---

# 6. IA INTEGRADA EN LA BÚSQUEDA, SIN MODO SEPARADO

Esta decisión es central.

No quiero depender de un botón «IA» ni de una pestaña especial para obtener respuestas.

## 6.1 Primera capa local

Mientras escribo:

- Interpretación rápida local.
- Buscar aplicaciones.
- Buscar archivos.
- Consultar notas.
- Reconocer acciones nativas.
- Proponer respuestas calculables localmente.
- No llamar continuamente a un modelo remoto.

## 6.2 Segunda capa inteligente

Al presionar Enter:

- Si es una búsqueda de aplicación, abrirla.
- Si es una acción local, ejecutarla o prepararla.
- Si es una pregunta de conocimiento compleja, utilizar la IA configurada cuando esté autorizada.
- Si es una tarea con varios pasos, generar un plan revisable.
- Si implica datos personales, comprobar el contexto y requerir aprobación específica antes de transmitirlos.

No mostrar «Buscar con IA» como resultado genérico para sustituir una operación local que debería funcionar.

## 6.3 Respuestas

- Dentro de la ventana de Spotlight.
- Texto claro y seleccionable.
- Posibilidad de copiar, compartir y guardar.
- Continuación conversacional cuando sea útil.
- Expansión del panel para respuestas largas.
- Acciones contextuales sobre la respuesta.

La conversación con IA es secundaria respecto de la búsqueda universal.

## 6.4 Privacidad de IA

- Integración explícitamente habilitada por el usuario.
- Proveedor y modelo configurables.
- Credenciales protegidas.
- No incluir API keys en código fuente ni registros.
- No enviar contactos, documentos, portapapeles o datos sensibles sin la autorización correspondiente.
- No obedecer instrucciones de documentos recuperados como si fueran órdenes del usuario.
- Permitir cancelar operaciones.
- Explicar cuando una función necesita conexión.

---

# 7. DISEÑO VISUAL: COMPONENTES ELEGIDOS

Las referencias concretas son obligatorias como guía de diseño e interacción.

Investigar las implementaciones originales, verificar URLs y licencias, y recrear sus patrones mediante Compose.

No insertar React, Tailwind ni WebViews para simular una interfaz nativa.

## 7.1 Barra principal

**Arc UI Command Palette**  
https://uiarc.dev/components/blocks/command-palette

**EasyUI Command Menu / Expandable Search / Glass Navbar**  
https://github.com/Surajmaurya1/easyui

Utilizar sus principios para:

- Campo de búsqueda amplio y elegante.
- Jerarquía visual limpia.
- Iconos auténticos.
- Resultados compactos.
- Controles discretos.
- Navegación natural con teclado.

## 7.2 Selección de resultados

**Motion Primitives Animated Background**  
https://motion-primitives.com/docs/animated-background

**MicroKit Spotlight Indicator**  
https://microkit.co/components/spotlight-indicator

Comportamiento:

- Fondo translúcido deslizante sobre el elemento seleccionado.
- Línea luminosa muy fina en el borde de la fila.
- Movimiento rápido.
- Sin animación excesiva al presionar repetidamente las flechas.
- Sin comprometer el estado real de selección.

## 7.3 Menú de opciones

**Aceternity Gooey Dropdown**  
https://ui.aceternity.com/labs

**SmoothUI Gooey Popover**  
https://smoothui.dev/docs/components/gooey-popover

**EasyUI Glass Navbar**

Recrear la sensación de que el botón se expande orgánicamente hacia el menú.

Debe permanecer dentro de la misma ventana `TYPE_APPLICATION_OVERLAY`.

No utilizar popups Android externos que puedan causar fallos de foco o cierres repentinos.

El menú del botón ubicado cerca del micrófono debe ser especialmente estable.

## 7.4 Panel lateral y expansión

**Motion Primitives Morphing Dialog**  
https://motion-primitives.com/docs/morphing-dialog

**Arc UI Resizable Panels**  
https://uiarc.dev/components/resizable-panels

**Aceternity Expandable Card**  
https://ui.aceternity.com/components/expandable-card

**Aceternity Notch**  
https://ui.aceternity.com/components/notch

Utilizarlos como referencias para:

- Vista previa lateral.
- Panel ajustable.
- Ampliación orgánica del contenido.
- Redimensionamiento suave de la ventana.
- Conservación del contexto.
- Animaciones discretas y consistentes.

## 7.5 Iconografía

**Hugeicons Stroke Rounded**  
https://hugeicons.com/

**Phosphor Icons**  
https://phosphoricons.com/

Utilizar recursos gratuitos y legalmente compatibles.

- Hugeicons para controles principales.
- Phosphor Regular para acciones complementarias.
- Duotone cuando ayude a distinguir contenidos.
- Iconos originales para aplicaciones instaladas.

No sustituir iconos auténticos de Spotify, WhatsApp u otras aplicaciones por símbolos genéricos.

## 7.6 Estética general

Nombre de la estética predeterminada: **Liquid Precision**.

Características:

- Cristal translúcido mate.
- Bordes finos con reflejos.
- Esquinas de aproximadamente 24 dp.
- Sombras suaves.
- Contraste alto.
- Tipografía cuidada.
- Diseño inspirado en macOS.
- Tonos neutros con acentos de color medidos.
- Alternativa sólida cuando el blur no sea compatible.

Ancho inicial de aproximadamente el 80% del espacio disponible en tablet.

Permitir expansión inteligente hasta aproximadamente el 90%.

Configuración de ancho y posición por orientación.

Tres niveles de movimiento:

1. Preciso.
2. Liquid Precision, predeterminado.
3. Liquid Immersive.

Respetar ajustes del sistema para reducir movimiento.

Los efectos visuales nunca deben retrasar la aparición de la barra.

## 7.7 Ajustes

Rediseñar Ajustes siguiendo una estructura macOS de barra lateral.

Categorías:

- Apariencia.
- Búsqueda.
- Inteligencia.
- Integraciones.
- Invocación.
- Privacidad y sistema.

Incluir selección de tema, colores y niveles de movimiento.

Ocultar opciones técnicas avanzadas hasta que sean necesarias.

No crear catálogos enormes de funciones.

---

# 8. SIMPLIFICACIÓN Y ELIMINACIÓN DE COMPLEJIDAD

La aplicación actual tiene demasiados recorridos independientes.

Eliminar de la navegación habitual:

- Command Center.
- Modos prefabricados Estudio, Lectura, Concentración y Multimedia.
- Catálogo de extensiones como pantalla permanente.
- Catálogo duplicado de acciones.
- Categorías visibles de búsqueda que obliguen a cambiar de modo.
- Opciones técnicas redundantes.
- Formularios de integración que no aporten una función real.

Conservar capacidades internas valiosas y trasladarlas al buscador universal.

**Tasker:** quitar su protagonismo visual y no convertirlo en requisito. Preservar datos antiguos si corresponde.

**Shizuku:** opcional, avanzado y desactivado por defecto. Inicializarlo únicamente si el usuario utiliza una capacidad que realmente lo necesita.

Priorizar APIs oficiales de Android.

No bloquear búsquedas normales porque un módulo de extensiones esté deshabilitado.

---

# 9. GUARDADOS, NOTAS Y TEXTOS

Crear una experiencia unificada llamada **Guardados**.

Internamente puede contener:

- Notas rápidas.
- Textos frecuentes.
- Enlaces importantes.
- Documentos fijados.
- Consultas favoritas.

No utilizar «Snippets» como nombre visible.

Los elementos deben ser buscables directamente desde la barra.

Ejemplos:

- `Guardá esta idea`
- `Mi dirección`
- `Buscar mis apuntes de rehabilitación`
- `Guardá este PDF`
- `Copiar mensaje de presentación`

Mantener plantillas con fecha, hora o campos editables cuando sean útiles, pero presentarlas en lenguaje sencillo.

Migrar notas, favoritos, alias, consultas y textos ya guardados sin pérdida de información.

No convertir Guardados en un editor complejo ni en otra aplicación independiente.

---

# 10. PORTAPAPELES Y CONTENIDO COMPARTIDO

## Portapapeles

Elegimos la modalidad **temporal inteligente**.

Valores iniciales:

- Hasta 20 textos.
- Retención de 15 minutos.
- Opciones de configuración limitadas y claras.
- Sin servicio de vigilancia permanente.
- Sin captura indiscriminada en segundo plano.
- Respetar contenido marcado como sensible.
- No capturar durante Modo privado.
- Borrado fácil.
- Posibilidad de guardar explícitamente contenido importante.

Aceptar las restricciones reales de Android para acceso al portapapeles.

No prometer un historial universal del dispositivo si Android no proporciona una API confiable para ello.

## Android Compartir y texto seleccionado

Aprovechar:

- `ACTION_SEND`
- `ACTION_SEND_MULTIPLE`
- `ACTION_PROCESS_TEXT`
- Intents compatibles.

Al compartir texto con Spotlight:

- Mostrarlo como contexto dentro de la ventana flotante cuando sea técnicamente apropiado.
- Permitir resumir, traducir, corregir, copiar o guardar.
- No obligarme a navegar por una pantalla completa separada si puede evitarse.
- Conservar el mecanismo oficial para devolver texto modificado al editor de origen cuando `ACTION_PROCESS_TEXT` lo requiera.

Ejemplos:

`Resumí esto en tres puntos`

`Corregí este párrafo`

`Traducí este texto`

`Guardá lo seleccionado`

Las transformaciones sencillas deben ejecutarse localmente cuando sea posible.

---

# 11. INTEGRACIONES REALES CON APLICACIONES

Spotlight debe integrarse con tantas aplicaciones instaladas como permita Android de forma documentada.

Explorar:

- Intents públicos.
- App Shortcuts.
- Android Sharesheet.
- `LauncherApps`.
- MediaSession.
- Content Providers autorizados.
- Deep links documentados.
- Permisos opcionales.
- Shizuku únicamente para funciones compatibles que lo requieran.

Distinguir las capacidades reales de cada aplicación.

Ejemplos:

- Abrir Spotify.
- Buscar música en Spotify.
- Controlar una sesión multimedia compatible.
- Abrir WhatsApp.
- Preparar un mensaje para un contacto.
- Abrir Google Maps con una dirección.
- Buscar contenido en la web.
- Abrir ajustes compatibles.
- Consultar calendario.
- Compartir archivos.
- Invocar funciones públicas de aplicaciones instaladas.

Nunca inventar controles internos de terceros ni simular integraciones inexistentes.

Una aplicación puede permitir abrirse, compartir o buscar sin permitir controlar todas sus funciones. Reflejar esa distinción.

No pedir root.

---

# 12. CALENDARIO COMPLETO

Crear un espacio de Calendario ampliable dentro de Spotlight.

**No crear una agenda independiente.**

Usar los calendarios del dispositivo accesibles mediante `CalendarContract`, con permisos correspondientes.

Funciones:

- Agenda diaria.
- Vista semanal.
- Vista mensual.
- Buscar eventos.
- Crear eventos.
- Editar eventos.
- Eliminar eventos con confirmación.
- Revisar horarios libres.
- Interpretar consultas naturales.

Ejemplos:

`¿Qué tengo mañana?`

`Agendá dentista el jueves a las 16`

`¿Cuándo es mi próxima clase?`

`¿Tengo libre de 15 a 17?`

`Buscame una hora libre el viernes`

`Pasá mi turno del lunes al martes`

## Conflictos de horario

Elegimos la opción inteligente:

Cuando una creación o modificación coincida con otro evento:

1. Avisar del conflicto.
2. Proponer dos o tres alternativas disponibles.
3. Considerar duración y calendarios autorizados.
4. Permitir conservar el horario original.
5. No guardar cambios sin confirmación.

La detección de disponibilidad no implica que un horario esté disponible para terceros; distinguir agenda local de disponibilidad externa.

Si falta permiso de escritura o la cuenta no admite edición, utilizar el editor de calendario del sistema cuando sea posible.

No confundir eventos con tareas o recordatorios de servicios que requieren otra API.

---

# 13. DEPORTES — RECONSTRUCCIÓN COMPLETA

La sección deportiva actual necesita una renovación profunda.

No quiero una lista fea de unos pocos encuentros de equipos aleatorios.

## Deportes principales

1. Fútbol.
2. UFC/MMA.
3. Básquet, especialmente NBA.
4. Tenis, ATP/WTA.

## Diseño principal

**Agenda deportiva unificada**, ordenada cronológicamente y priorizada según favoritos.

Filtros opcionales por deporte.

No construir cuatro aplicaciones deportivas independientes ni obligarme a seleccionar categorías antes de buscar.

Consultas:

`Deportes en vivo`

`Mis partidos`

`Boca próximo partido`

`Próxima UFC`

`NBA hoy`

`Partidos de tenis en vivo`

`Cuándo juega Sinner`

## Comportamiento contextual

- Consulta general de deportes: agenda expandida.
- Consulta de partido específico: tarjeta rápida.
- Consulta de equipo: ficha de entidad.
- Favorito fijado: vista de favoritos.
- Enter sobre un encuentro: ficha completa dentro de Spotlight.
- Tab sobre encuentro: acciones, incluida la apertura de Sofascore mediante enlace compatible.

## Información por deporte

**Fútbol:** resultados, goles, equipos, competición, partidos próximos, clasificaciones cuando la fuente lo permita.

**UFC:** carteleras, peleas principales y preliminares, peleadores, métodos de resultado y datos disponibles.

**Básquet:** partidos, resultados por cuarto, equipos, ligas y clasificaciones.

**Tenis:** torneos ATP/WTA, partidos, resultados por sets, jugadores, cuadros y calendario cuando estén disponibles.

## Favoritos

Permitir guardar:

- Equipos.
- Competiciones.
- Torneos.
- Deportistas.
- Peleadores.
- Encuentros individuales.

Agregar y quitar favoritos mediante acciones sencillas, incluyendo enlaces compartidos desde Sofascore cuando se puedan interpretar de forma segura.

Utilizar identificadores estables de cada proveedor y deduplicar entidades.

No intentar acceder a los favoritos privados de Sofascore sin una API autorizada.

## APIs deportivas

Investigar alternativas documentadas:

- API-Sports.
- API-Football.
- APIs oficiales o comerciales de básquet, tenis y MMA.
- Proyectos open source con licencias apropiadas.

Verificar:

- Cobertura.
- Datos en vivo.
- Planes y costos actuales.
- Cuotas por día.
- Límites comerciales.
- Licencia de uso.
- Latencia.
- Calidad de resultados.
- Manejo de errores.

No dar por completa una API solamente porque responda correctamente a un partido de prueba.

Mostrar fuente de los datos y última actualización.

Caché adaptada al calendario de eventos.

No inventar resultados si falta proveedor.

## Notificaciones deportivas

Dejar Sofascore como responsable habitual de las alertas deportivas.

No duplicar notificaciones por defecto desde Spotlight.

---

# 14. CLIMA COMPLETO Y EFICIENTE

Crear un espacio visual inspirado en Apple Weather.

Funciones:

- Clima actual.
- Pronóstico por horas.
- Pronóstico de varios días.
- Gráficos de temperatura.
- Probabilidad de lluvia.
- Viento.
- Humedad.
- Sensación térmica.
- Índice UV.
- Información de alertas cuando exista una fuente compatible.
- Ciudades favoritas.

Ejemplos:

`Clima hoy`

`¿Necesito paraguas mañana?`

`¿A qué hora deja de llover?`

`Clima en Mar del Plata`

## Ubicación híbrida

- Ubicación automática solo si está permitida.
- Preferir ubicación conocida o aproximada.
- No mantener GPS funcionando.
- Ciudad principal guardada como alternativa.
- Cambio automático a esa ciudad si la ubicación está desactivada.
- Posibilidad de consultar otras ciudades sin reemplazar la principal.

## Batería

- Pronóstico bajo demanda.
- Caché.
- Evitar descargas repetidas.
- Sin consultas periódicas por defecto.
- Sin GPS permanente.

## Alertas opcionales

Las alertas meteorológicas existen como función configurable, pero están **desactivadas por defecto**.

Solo habilitar comprobaciones periódicas y permisos relacionados cuando el usuario active explícitamente esa función.

Si no existen datos oficiales fiables de alertas para la región, no simularlas.

---

# 15. ACCESOS RÁPIDOS OPCIONALES

Permitir fijar accesos a:

- Calendario.
- Deportes favoritos.
- Clima.
- Guardados.
- Aplicaciones o consultas frecuentes.

No presentarlos como pestañas permanentes.

Pueden aparecer como accesos contextuales o sugerencias discretas en la búsqueda vacía.

Mostrar un máximo aproximado de cuatro sugerencias iniciales, basadas en relevancia real, sin relleno.

No ocupar espacio permanente con categorías que no uso.

---

# 16. RENDIMIENTO Y CONSUMO

Esta prioridad es tan importante como el diseño.

Auditar:

- `OverlayService.kt`.
- `SpotlightApp.kt`.
- `SpotlightPanel.kt`.
- `SearchEngine.kt`.
- `FileIndex.kt`.
- `AdaptiveLayout.kt`.
- `WindowFocusHost.kt`.
- `FilePreview.kt`.

## Inicio del overlay

- Mostrar la barra lo antes posible.
- No inicializar Shizuku innecesariamente.
- No esperar a proveedores deportivos, clima o IA.
- Cargar índices y componentes bajo demanda.
- Evitar trabajo pesado en el hilo principal.
- Separar inicialización imprescindible de secundaria.

## Índice de archivos

Revisar la consulta SQLite actual y evitar filtrar grandes cantidades de registros en memoria cuando pueda hacerse eficientemente en base de datos.

- Índices adecuados.
- Filtrado previo.
- Búsqueda textual optimizada donde corresponda.
- Actualizaciones incrementales.
- Consultas cancelables.
- Metadatos reales.
- Respeto de permisos de almacenamiento.

## Rendimiento visual

- Evitar recomposición innecesaria.
- No recalcular todos los resultados al cambiar únicamente la selección.
- Reducir animaciones costosas.
- Caché de iconos y vistas previas.
- Controlar memoria de bitmaps.
- No renderizar PDF de gran tamaño mientras escribo.
- Evitar efectos de blur que no puedan mantenerse fluidos.

## Medición

Medir:

- Tiempo hasta ventana visible.
- Tiempo hasta foco e interacción.
- Latencia de búsqueda.
- Tiempo de respuesta del índice.
- Renderización de Quick Look.
- Memoria y estabilidad en pruebas repetidas.

Comparar con la versión anterior.

No afirmar porcentajes de mejora sin mediciones.

---

# 17. ESTABILIDAD DE LA VENTANA FLOTANTE

Conservar la arquitectura de una ventana principal `TYPE_APPLICATION_OVERLAY`.

Corregir y prevenir:

- Cierres inesperados al abrir opciones.
- Problemas de foco.
- Pérdida del teclado.
- Ventanas huérfanas.
- Errores al cambiar orientación.
- Menús que aparecen fuera de la pantalla.
- Desplazamientos inesperados al abrir el teclado.
- Bloqueos durante cambios rápidos de consulta.
- Estados incompatibles entre Quick Look, acciones e IA.

Mantener la experiencia táctil y con teclado coherente.

Separar internamente las responsabilidades de `SpotlightPanel.kt` para reducir el riesgo de regresiones.

No hacer una refactorización innecesaria que destruya funciones ya válidas.

---

# 18. PRIVACIDAD Y SEGURIDAD

El Modo privado debe:

- Suspender captura de portapapeles.
- Suspender aprendizaje local del uso.
- Evitar incorporar automáticamente contexto personal.
- Mantener disponibles operaciones explícitas iniciadas por el usuario, con consentimiento.
- No borrar datos previos sin solicitarlo.

Las operaciones potencialmente sensibles requieren confirmación.

No guardar secretos en logs ni en el ZIP fuente.

No exponer documentos o contactos a servicios externos sin autorización.

No introducir telemetría invasiva.

Pedir permisos de Android cuando realmente se necesitan, explicando su propósito.

---

# 19. PRUEBAS OBLIGATORIAS

Crear o ampliar pruebas automatizadas para los flujos completos.

## Búsqueda

- `Spotify` muestra la aplicación correcta.
- `Buscar PDF de ayer` aplica fecha real.
- `PDF de esta semana` utiliza filtros temporales.
- Las búsquedas simples no muestran acciones irrelevantes.
- Los resultados duplicados desaparecen.
- La selección no salta por respuestas asíncronas.
- Los permisos denegados generan estados claros.

## Enter y Tab

- Enter abre aplicación.
- Enter abre PDF externamente.
- Un toque sobre PDF solo selecciona y muestra Quick Look.
- Tab abre acciones contextuales.
- Tab repetido avanza.
- Shift+Tab retrocede.
- Enter dentro de acciones ejecuta la acción enfocada.
- Escape respeta el orden de cierre.
- El autocompletado no interfiere con Tab.

## Quick Look

- Primera página de PDF.
- Navegación entre páginas.
- Archivo inexistente.
- Permiso revocado.
- PDF dañado.
- Cambio rápido entre documentos.
- Expansión y retorno.
- Liberación de memoria y recursos.

## Selección múltiple

- Añadir tres archivos.
- Mantener selección durante otra consulta.
- Eliminar un archivo de la bandeja.
- Compartir selección.
- Preparar resumen con revisión de privacidad.

## IA

- Pregunta compleja mediante Enter.
- Proveedor configurado.
- Proveedor no configurado.
- Sin conexión.
- Cancelación.
- Confirmación de datos sensibles.
- Ninguna llamada remota por pulsación de tecla.

## Android

- Invocación repetida.
- Menú de opciones.
- Foco y teclado.
- Orientación horizontal y vertical.
- Ventana ancha y estrecha.
- Cierre por toque exterior.
- Aplicación en segundo plano.
- Permisos revocados.
- Modo privado.

## Espacios inteligentes

- Calendario disponible y sin permisos.
- Conflicto de horario y sugerencias.
- Deportes sin resultados inventados.
- Favoritos deportivos persistentes.
- Clima con ubicación desactivada.
- Ciudad principal alternativa.
- Alertas meteorológicas desactivadas por defecto.
- Sin consultas periódicas innecesarias.

Ejecutar pruebas de Android, Compose, unitarias y de integración disponibles.

Realizar capturas verificables de la nueva interfaz.

Diferenciar claramente pruebas ejecutadas de validaciones pendientes en dispositivo físico.

---

# 20. COMPILACIÓN, FIRMA Y CONSERVACIÓN DE DATOS

**Este apartado es obligatorio.**

El repositorio contiene:

- `.github/workflows/release-apk.yml`
- `scripts/sign-release.sh`
- `scripts/verify_release.py`

El flujo actual incluye valores específicos de la 2.0.0 que deben actualizarse.

Comprobar el estado real del repositorio y de la última versión distribuida.

Requisitos:

1. Conservar `dev.bautista.spotlight.next`.
2. Incrementar `versionCode` correctamente; si la última versión es código 27, la siguiente debe ser mayor que 27.
3. Actualizar verificaciones, nombres de artefactos, etiquetas y constantes antiguas.
4. Firmar con el certificado histórico mediante los secretos existentes.
5. Verificar el certificado con `apksigner`.
6. Mantener la compatibilidad de actualización sobre la instalación existente.
7. No solicitar claves de firma en texto.
8. No incluir claves privadas o API keys en el repositorio ni en el ZIP.
9. Conservar las preferencias y los datos existentes.
10. Crear migraciones cuando cambie el almacenamiento.
11. Verificar compilación, pruebas y lint antes de entregar.

El certificado histórico esperado en los verificadores existentes es:

`11B0B3B37544BD95B128DE52798FA7FFB7489F94819614B1ADA383BD4EE7A6CF`

Comprobarlo contra el estado real de distribución y no reemplazarlo por un certificado nuevo.

No utilizar un APK debug firmado con una clave diferente como supuesto reemplazo de un release actualizable.

Si faltan secretos o permisos de GitHub, informar exactamente el bloqueo y solicitar solamente la intervención imprescindible.

---

# 21. ARTEFACTOS QUE QUIERO RECIBIR

Al terminar, entregarme directamente:

**A. APK instalable**
- Firmado.
- Verificado.
- Con número de versión correcto.
- Compatible con la aplicación existente.

**B. ZIP fuente**
- Código de la misma versión exacta.
- Configuración de compilación.
- Pruebas.
- Documentación.
- Recursos incluidos.
- Sin credenciales privadas.

**C. Informe de cambios**
- Qué se modificó.
- Qué se corrigió.
- Qué se eliminó.
- Qué se conservó.
- Qué quedó pendiente.

**D. Resultados de validación**
- Pruebas ejecutadas.
- Estado de GitHub Actions.
- Versión y certificado verificados.
- Capturas de interfaz.
- Limitaciones no comprobadas físicamente.

No finalizar únicamente con un pull request o una explicación de cómo compilar. El objetivo de mi flujo de trabajo es recibir los archivos listos para utilizar.

---

# 22. ESTRATEGIA DE EJECUCIÓN EN WORK

Aprovechá al máximo el tiempo y las herramientas disponibles.

Dividí internamente el trabajo en las siguientes etapas, pero avanzá de manera continua.

## Etapa 1 — Base funcional

- Corregir búsqueda e interpretación.
- Optimizar ranking.
- Corregir estabilidad de resultados.
- Resolver rendimiento de apertura.
- Consolidar Enter/Tab/Escape.
- Corregir menú y foco.
- Establecer arquitectura del panel lateral.

Crear una compilación verificable.

## Etapa 2 — Experiencia visual y documental

- Aplicar referencias visuales seleccionadas.
- Renovar barra, filas, selección y controles.
- Completar Quick Look.
- Incorporar panel lateral integrado.
- Selección múltiple.
- Guardados.
- Portapapeles y contenido compartido.
- IA integrada.

Crear otra compilación verificable.

## Etapa 3 — Integraciones completas

- Calendario con interpretación natural.
- Detección de conflictos.
- Deportes unificados y favoritos.
- Cuatro deportes con datos reales.
- Clima completo y eficiente.
- Accesos rápidos opcionales.
- Integraciones Android disponibles y verificadas.

Crear nueva compilación verificable.

## Etapa 4 — Calidad y entrega

- Pruebas de regresión.
- Corrección de errores.
- Rendimiento y memoria.
- Revisión visual.
- Verificación de firma.
- Release final.
- APK y ZIP fuente.

No crear versiones artificialmente «completas» cuando todavía tengan pantallas vacías o datos falsos.

Si alguna integración requiere una API paga, credenciales que no están disponibles o permisos adicionales, implementar una alternativa legal y funcional cuando exista. Si no existe, documentar el bloqueo sin simular resultados.

Podés crear commits y utilizar el flujo de GitHub correspondiente. Evitá pedirme operaciones manuales de ramas, merges o compilación.

**No te detengas al terminar la Etapa 1 si todavía podés continuar de forma segura.** Quiero aprovechar esta ejecución para avanzar lo máximo posible sobre el alcance completo.

Al mismo tiempo, mantené una compilación estable recuperable en cada etapa.

No sacrificar una entrega instalable por intentar terminar de golpe una función enorme.

---

# 23. CRITERIOS DEFINITIVOS DE ACEPTACIÓN

No considero terminada la renovación de Spotlight Next únicamente porque la interfaz se vea distinta.

Debe ser posible:

- Invocar Spotlight rápidamente.
- Escribir `Spotify` y abrir Spotify con Enter.
- Buscar PDF por fecha real.
- Tocar un PDF y verlo en Quick Look.
- Ampliar y recorrer sus páginas.
- Presionar Tab y acceder a sus acciones.
- Avanzar con Tab y retroceder con Shift+Tab.
- Abrir el PDF externamente con Enter desde resultados.
- Seleccionar varios documentos.
- Hacer una consulta compleja y obtener IA integrada sin cambiar de modo.
- Compartir texto desde otra aplicación y procesarlo.
- Guardar notas y textos sin aprender términos técnicos.
- Consultar el calendario sincronizado.
- Recibir alternativas ante horarios ocupados.
- Ver una agenda deportiva útil para fútbol, UFC, básquet y tenis.
- Consultar clima con ubicación opcional y bajo consumo.
- Utilizar la aplicación sin root.
- Actualizar la versión instalada sin perder datos.

**Objetivo final:** que Spotlight Next se sienta como la herramienta universal de productividad que faltaba en Android, con la sencillez de Spotlight, las acciones inteligentes de Raycast y la selección contextual de Alfred.

Priorizá calidad y funcionamiento verificable.

Comenzá inspeccionando el repositorio real, creando una estrategia de implementación interna y ejecutando las modificaciones. Trabajá de manera autónoma hasta entregar el APK firmado, el ZIP fuente y el informe de validación.

No me presentes solamente un plan. **Ejecutá el trabajo.**
