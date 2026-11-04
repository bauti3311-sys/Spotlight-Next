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


## F14 — recorrido de archivos integrado

Entrada Share conserva hasta12 URI originales sin extracción/copia automática;
la extracción local anterior sigue disponible como acción manual. ContextStore
proyecta metadatos autorizados al mismo SearchEngine/FileIndex, sin un segundo
índice ni ejecutor. El trampolín transporta ClipData/READ al servicio existente;
Share no toma permisos persistentes automáticamente. Los contextos múltiples son
temporales y fijar un documento exige permiso persistente/propio disponible.

La bandeja existente y «compartir archivo X» preparan un inspector nativo con
nombre, tipo, origen y destino. Revisar comprueba metadatos/descriptor; confirmar
revalida antes del único NativeLaunchSession. Selección o metadatos cambiados,
lectura perdida, receptor ausente, cancelación y restauración no autorizan un
pedido anterior. La confirmación no se guarda; sólo descriptores de selección.
No hay fallback ni afirmación de entrega, y el recibo Android conserva su advertencia.

La forma usa los tokens y scroll del inspector existente, objetivos48dp y foco
inicial en volver sin compartir. Oculta la bandeja repetida durante revisión para
no gastar altura; al cancelar conserva selección/consulta. Footer contextual:
«Preparar envío» / «Revisar archivos». Sin rediseño de C1 ni cambios en OverlayService,
OverlayViewport, WindowManager o cálculos IME. Las capturas/fixtures no validan OneUI.

Primera ronda de entrada/contexto:25 casos correctos (ver XML incoming-first),
sin repetir la suite C1. Los8 recorridos de revisión UI (7 iniciales y1 cambio ancho/compacto) y5 de contexto/grants tienen última evidencia correcta. El gate final/Lint y las comparativas se registran al cierre. La primera
compilación de los fixtures UI detectó un import ambiguo de StateRestorationTester;
se conserva el log y se usa import explícito. No es un efecto externo comprobado.

[Matriz de aplicaciones y protocolo físico](C2-APPLICATION-COMPATIBILITY-20261104.md).


## Fallos encontrados por la integración completa

El motor emitía la propuesta «Revisar archivos seleccionados», pero CompactResults
no reconocía su identidad contextual y la quitaba del listado inicial. La prueba
panel→consulta falló antes de abrir la revisión. Se incorpora a la misma regla de
acciones explícitas existentes; una prueba separada conserva app literal exacta
por encima de esta propuesta. No se introduce un catálogo de sugerencias paralelo.

Dos límites de fixtures se documentan sin convertirlos en garantías Android:
addResolveInfoForIntent no representaba el filtrado MIME esperado; la matriz usa
IntentFilter real de PackageManager. El shadow de permisos de Activity admite por
defecto una URI no concedida: la prueba negativa del trampolín pasa a un ClipData
con Intent anidado inválido; revocación real se mantiene en los tests con Context
controlado y se exige en dispositivo. No se elimina la validación de grants.

Las capturas iniciales de búsqueda se dispararon antes de recibir las filas. Se
conservan en flow-first con su XML pero no sirven como comparación visual final.
El fixture comparable espera una fila de archivo real antes de capturar, idéntico
sobre f17d9b2 y la producción actual. No se retocan imágenes.


La nueva prueba de app literal detectó además que CompactResults reinterpretaba
una etiqueta exacta con forma de consulta («compartir archivos seleccionados»).
El filtro visual ahora utiliza la consulta literal cuando ya existe coincidencia
exacta; no sólo SearchEngine evita reinterpretarla.14 casos CompactResults verdes,
incluida la propuesta de archivos y app exacta por encima. El layout de revisión
conserva estado/scroll al pasar ancho→compacto→ancho; footer dentro de límites.
La matriz usa componentes registrados con IntentFilter real (no arrays sueltos en
PackageInfo);10 casos API26/35 correctos, sin afirmar compatibilidad de apps reales.
