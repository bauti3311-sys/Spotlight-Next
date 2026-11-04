# C2 — archivos locales, compartir y compatibilidad

Base f17d9b2 del PR21. Continuación autorizada F07/F14 y matriz técnica, sin
esperar la tablet, sin APK/Actions/main/C3–C9. Preview62 permanece referencia
física; no se promueven C0/C1/C2 físicos ni el checkpoint integral.

## Auditoría previa

FileIndex/SearchQuery/QueryInterpreter ya filtraban tipo y fechas locales y
recuperaban contenido indexado/semántica local. ContextStore conservaba contexto
explícito, FileBuffer selección y fábrica de envío, NativeLaunchSession la barrera
de solicitud y revalidación. No se crea otro catálogo, extractor o ejecutor.

Dos fallos reproducidos: el corte de80 coincidencias por fecha podía excluir un
nombre antiguo más relevante; una app llamada «PDFs de ayer» se interpretaba como
consulta de archivos antes de buscar la app exacta. «compartir archivo X» tampoco
entraba en búsqueda local. La ruta actual compartía sin revisión previa, siempre
MULTIPLE incluso con un archivo, y el guard de URI no inspeccionaba los arrays
ni el Intent anidado del chooser. Entrada múltiple extraía documentos como texto;
se revisará para conservar archivos originales y permisos sin lectura automática.

## Subhito F07

- «PDFs de ayer» conserva fecha de modificación local con intervalo semiabierto,
  sin suponer una fecha desconocida. «buscar documento de vestibular» recupera
  documentos; «compartir archivo X» busca archivos compatibles de cualquier tipo,
  conserva comillas y marca preparación, sin abrir destinos ni IA remota.
- App literal exacta tiene prioridad incluso si parece una consulta documental.
- Nombre normalizado Unicode dentro del mismo FileIndex, primero candidatos por
  nombre, después contenido/fecha;80 resultados máximo y URI únicas, orden estable.
  Cancelación por fila y antes de cada consulta. No escaneo de proveedores nuevos.
- Índice cache v3→v4 aditivo: columna name_key e índice árbol/nombre, completados
  conservando filas, URI, textos, autor, fechas y tamaños. No borra ni migra datos
  de usuario, preferencias o ContextStore. El primer acceso actualiza esta caché;
  el coste físico de ese primer acceso todavía debe medirse.
-25 casos dirigidos correctos en primera ronda;9 adicionales dirigidos para
  migración, comodines literales, consulta compartida con comillas/alcance y fechas.
  Son rondas con casos comunes; el cierre deduplicará por identidad de prueba.

Evidencia: verification/c2-files-20261104/search-red, search-first y search-upgrade.
No p95/RAM/jank físicos ni efectos externos verificados.

## F14 — contratos base comprobados, recorrido UI siguiente

FileBuffer produce SEND para1 archivo y SEND_MULTIPLE para2–12, MIME concreto o
familia común y ClipData consistente, sólo READ temporal. Conserva content URI;
file URI de fuentes locales anteriores usa el FileProvider existente, sin copiar
bytes ni resolver content URI como ruta privada.

IntegrationRegistry inspecciona el payload anidado del chooser, todos los streams
y ClipData, permisos vigentes y receptor público. Rechaza arrays sin ClipData,
URI diferentes, extras mal tipados, grants WRITE/PERSISTABLE/PREFIX y un chooser
no perteneciente al sistema. Mantiene NativeLaunchSession como único ejecutor y
barrera de solicitud. Se excluye nuestra entrada de Share del catálogo de destinos
externos y del chooser para no presentar autoimportación como receptor externo.

Dos queries públicas SEND/MULTIPLE por MIME permiten descubrir receptores en
Android11+, sin añadir uses-permission ni acceso al almacenamiento.

51 casos distintos con última evidencia correcta en rondas de contratos:18 de
formato/grants API26/35,19 NativeLaunchSession,6 FileBuffer y8 de preparación real
sobre ContentProvider controlado/descriptor local. No prueban Android Sharesheet
o receptores reales. Sólo metadatos/descriptor, sin extracción/copia. Se conserva
el rojo, el error de runtime API26 inicialmente ausente (jar oficial con SHA512
comprobado en caché), el fixture de flag SYSTEM corregido y la detección de nuestra
propia Activity como falso destino externo. Ver contract-*/prepare-*.
