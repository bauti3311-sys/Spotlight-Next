# C2 — continuación posterior a Preview62

Autorización del propietario: continuar desarrollo C2 sin esperar disponibilidad
de la Tab A9+. Base f49281a, PR21, rama feat/preview57-native-return-20261103.
La APK62/source135a9af/workflow388fe90 y su evidencia no se modifican.
La validación física del arreglo de ventana sigue pendiente.

## Auditoría y secuencia

Se leyeron SearchEngine, ResultRanker, CompactResults, AppCapabilities,
IntegrationRegistry, ActionCatalog, NativeLaunchSession, CommandForms,
AppCapabilityActions y ContextStore. Ya existían búsqueda ACTION_SEARCH,
enlaces de proveedores conocidos, preparación ACTION_SEND, Share/Process Text,
inspector adaptativo, footer y contenedor compacto desplazable. Se reutilizan.

1. Ampliar descubrimiento a ACTION_WEB_SEARCH público en la app seleccionada.
   ACTION_SEARCH/enlace documentado siguen teniendo prioridad; una sola capacidad.
   Validación viva antes del formulario y al solicitar Android, sin auto fallback.
2. Mejorar consultas contextuales explícitas por nombre de aplicación, sin
   inferir contenido privado, romper exactos ni abrir apps durante la escritura.
3. Texto/enlaces autorizados al formulario de compartir existente mediante botón
   explícito; edición, revisión y confirmación separadas. Corregir destino Gmail
   en inspector y panel de acciones, sin afectar correo genérico ni WhatsApp/C3.

No se cambian composición, colores, dimensiones, WindowManager/IME, permisos,
SDK, dependencias, identidad ni código de firma/workflows. No APK ni Actions.
Las capturas serán Compose/Robolectric equivalentes, no capturas de OneUI.

## Evidencia inicial

Las pruebas nuevas detectaron ausencia de ACTION_WEB_SEARCH y del botón de
contexto en MessageEditor. Capturas base claro/oscuro del formulario real.
Se conserva el fallo inicial de infraestructura «No tests found»: dos procesos
Gradle coincidieron sobre el directorio de clases; se serializaron las siguientes
rondas y la reproducción web posterior sí ejecutó los tres casos. No se cuenta
ese primer intento como prueba del comportamiento. Un assert del fixture de
mensaje comparó Text+EditableText con vacío e incluyó la etiqueta: se corrige a
EditableText exacto, sin quitar la comprobación del borrador inicialmente vacío.
Resultados y límites finales se completan con cada checkpoint comprobado.

## Subhito 1 — búsqueda pública ampliada

28 casos correctos (3 nuevos WEB_SEARCH,9 AppCapabilities,16 NativeLaunchSession),
sin fallos/omisiones. Reproducción previa:2 fallos de3 casos nuevos.
Evidencia web-red/web-green. Ningún Intent se solicita durante descubrimiento;
la actividad debe ser pública, habilitada, del paquete elegido y sin permiso
denegado. PUBLIC_SEARCH/documentado precede a WEB_SEARCH, sin duplicados.

## Subhito 2 — consulta contextual explícita

Se admiten «Wikipedia términos», «buscar en Wikipedia: términos» y «buscar
 términos en Wikipedia» cuando la app expone búsqueda pública. El nombre más
largo inequívoco gana, se conservan acentos/texto original y las apps exactas
siguen primero. No se elige un homónimo ni se ofrecen capacidades ausentes.
WEB_SEARCH usa el formulario existente y QUERY, sin inventar búsqueda interna.

16 casos correctos:10 C2ContextSearch,5 HomeSuggestions y1 recorrido UI nuevo
consulta→edición→solicitud WEB_SEARCH al componente exacto→Escape/foco.
Rojo previo:2 fallos/10; evidencia query-red/query-green. Sin remoto por tecla,
lectura de otras apps ni efectos externos verificados.
