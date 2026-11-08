# C2 — relevancia y estabilidad acotadas

Fuente publicada `f9fcd5d8745ed45127f44c7616da2ec9a82ef2c3`. Base remota verificada71500e399a2cf56c5a557c8ba73b223c4e895375, PR21 abierto/borrador, reserva propia. Sin diseño nuevo, APK ni Actions. Preview68 permanece intacta; C5 no se retoma.

## Auditoría

SearchEngine conserva el enrutado específico de comandos/contenido y espera al proveedor de aplicaciones antes de publicar la primera instantánea universal. ResultRanker ya protege clases léxicas por encima de favoritos/hábitos; CompactResults conserva elegibilidad, expansión y orden bloqueado durante navegación. HomeSuggestions ya limita inicio a3 y1app con señales reales, sin relleno alfabético. Se reutilizan esos mecanismos; no hay otro ranker ni cambio de umbrales.

Los casos base `webOS Dev Manager`, `Spotify` y `spot` ya cumplían: app exacta/prefijo antes de Wi-Fi/WhatsApp genéricos, acciones y web, incluida la primera instantánea asíncrona. Las acciones explícitas siguen su interpretación, no se convierten todas en búsquedas de apps.

## Cuatro defectos reproducidos y corregidos

| Caso local reproducible | Antes | Después |
|---|---|---|
| `WEBOS   DEV\tMANAGER`, app instalada y alias `webos→Spotify` | El alias ocultaba la app: sólo `search:natural:spotify` | Primero `fixture.webos/Main`; alias no reemplaza el nombre exacto |
| Apps `Café Manager`/`Cafe   Manager`, misma base y distinta separación | Puntos3733/1206 y cambio de orden al variar espacios/tabulación | Ambas3733; orden estable por ID en cuatro variantes de escritura |
| `playe`, Player Beta reciente, aprendizaje apagado | Beta desplazaba Alpha por20p de recencia | Alpha vuelve primero; con aprendizaje activo Beta sigue primera; privado ignora recencia y favorito explícito todavía puede elegir Beta |
| Dos apps favoritas llamadas Duplicada | Inicio cambia fixture.z→fixture.a si cambia la enumeración del PackageManager | fixture.a estable con ambas enumeraciones, máximo1app |

Delta productivo:18 inserciones/11 eliminaciones en cuatro archivos existentes. Comparación léxica comparte `normalizedWords` y una regex de espacios precompilada; no modifica argumentos/payloads de acciones ni alias almacenados. ResultRanker reutiliza esa comparación en clase/bonus/desempate. SearchEngine protege nombres exactos con la misma clave y condiciona el bonus reciente por learning/no privado. Home añade ID como último desempate. No se cambian puntajes/pesos generales, categorías, IDs o ejecutores.

## Corpus y resultados

**13 casos nuevos**, fixtures locales reproducibles, aplicaciones ficticias en Robolectric y resultados de archivos/contactos sintéticos identificados. La primera iteración tenía una expectativa inválida de archivo antes de contacto: pertenecen al mismo nivel y su puntuación existente favorecía al contacto. Se corrigió el fixture, no producción. Se sustituyó también un cast que ocultaba el mensaje del fallo por una aserción explícita. La baseline definitiva, todavía sin cambios productivos, tiene **4 fallos reales/9 casos aprobados**. Se conservan ambos registros sin presentarlos como cuatro defectos adicionales.

Después: **28/28 casos dirigidos aprobados**, sin omisiones:13 nuevos +15 regresiones pertinentes seleccionadas (alias literales, errores tipográficos, comandos exactos, scopes, consultas/contexto, homónimos, permisos/módulos, expansión y orden de navegación). No se repiten suites UI/históricas completas.

- `queen` sin appQueen no inventa intención musical: web/diccionario quedan exploratorios. Con appQueen instalada ésta es primera. `música de Queen` prepara la búsqueda musical existente incluso con Queen instalada; no inicia reproducción ni consulta Spotify.
- Archivos/contactos coincidentes superan apps ajenas y web; su orden entre pares conserva puntaje. Se preservan Action/ID y todo secundario permanece en `View.all`/«Ver más». No es una nueva prueba SAF/OCR ni autorización de fuentes reales.
- Alias `mus→Spotify`, nombres largos, acentos, capitalización y tokens de varias palabras; calculadora, volumen y `pdf anatomía` mantienen semántica. No se cambia gramática de consultas avanzadas.
- READ_CONTACTS denegado y archivos deshabilitados no aportan contenido privado; desactivar módulo apps sigue filtrando resultados. No se piden permisos ni se ejecutan destinos.
- Favoritos/hábitos sólo ordenan pares; exacto/prefijo superan una parcial con puntaje muy alto. Privado y aprendizaje apagado ignoran historia aprendida; favoritos explícitos se conservan.
- Inicio vacío sin señales devuelve vacío; fixture con señales reales produce3 sugerencias y1app. Se comprueban hábitos vencidos, modo privado, ocultar/fijar y desactivar sugerencias.
- La selección conserva ID/Action/posición cuando llegan proveedores tarde, elimina resultados desaparecidos y retiene selección al contraer. Autocompletado conserva contrato. No se cambia código Enter/Tab/Escape, scroll ni UI; no se afirma nueva validación física.

Lint global correcto:0 errores/26advertencias/1informativo. No APK/testAPK/instrumentación. La guardia del grafo rechaza empaquetado/firma/instalación/connected.

## Protección y límites

Sólo cambian SearchEngine, ResultRanker, Models (comparación léxica) y HomeSuggestions. CompactResults queda intacto: la sospecha de reordenamiento no se reprodujo como defecto en los casos válidos evaluados, incluidos comandos/scopes explícitos; no se cambia por una hipótesis. UI/FocusStudio/2:1/AjustesA/Geist/140ms, NativeLaunchSession, gestos, preferencias, permisos, firma, paquete, versión/manifiesto68, indexación/C5/semántica/Spotify/Square/WhatsApp y main byteidénticos donde corresponde. No nuevos proveedores, dependencias, servicios, I/O o trabajo de embeddings; mismo esquema de caché y ordenamiento local. No se aportan cifras de RAM/latencia físicas porque no se midieron.

Evidencia XML/logs/órdenes/hashes y comprobación de preservación en [c2-relevance-20261107](verification/c2-relevance-20261107). El código nuevo queda en PR21 para futura entrega autorizada; no está en la APK68. No se declara C2 global ni VisualFirst/C5 completos, no se solicita otra Preview ni capturas.
