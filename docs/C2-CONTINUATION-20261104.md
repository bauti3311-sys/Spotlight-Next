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

## Subhito 3 — compartir contenido con destino revisado

«compartir en App» y «compartir enlace/texto/contenido en App» preparan el
formulario existente sólo si esa app publica ACTION_SEND text/plain accesible.
No copian texto ni abren destinos. El botón explícito incorpora contexto local
Share/Process Text; límite8000 anunciado, edición y revisión antes de confirmar.
Un contexto nuevo no sobrescribe el borrador. Restaurar estado no restaura la
confirmación. Documentos sin texto no se extraen por esta ruta.

Gmail del inspector/panel se conserva en com.google.android.gm: ya no deriva al
cliente de correo predeterminado. La acción se deshabilita con recuperación si
no hay receptor accesible. Revalidación antes del formulario y antes de solicitar
Android; error por destino retirado sin fallback. Correo genérico/WhatsApp y sus
contratos anteriores se conservan. Footer distingue contenido/correo/mensaje,
y el acento existente reconoce el destino share elegido.

La ronda inicial fue21/22 correcta. Detectó pérdida de foco con Escape al retirar
un formulario recién abierto. La ruta real era dismissInspector, no back; el
primer parche en back no resolvió el caso y se retiró. Se solicita foco después
de retirar el subárbol del formulario, en el frame siguiente; sin cambiar IME,
WindowManager ni introducir espera temporal fija. Cuatro regresiones finales
correctas: Escape compartir, restauración sin confirmación y dos retornos de
búsqueda.25 casos distintos correctos de última evidencia en ambas rondas;
no una suite única de25. Ver share-first/focus-first/focus-green, rojos conservados.

Capturas reales de Compose/Robolectric API33 NATIVE, no dispositivo: formulario
claro/oscuro y catálogo Gmail antes/después; confirmación con viewport320dp,
scroll y footer dentro de la ventana. Se conservaron tokens/composición; sin
nueva hoja, paleta o jerarquía. Los fixtures usan actividades sintéticas.

## Subhito 4 — solicitud aceptada no equivale a efecto externo

El inspector de búsqueda/compartir muestra un recibo efímero únicamente después
que startActivity retorna: «Solicitud enviada a Android», destino exacto y
«Resultado externo sin comprobar». No persiste texto, URI, contenido ni permiso
de ejecución. Se descarta al cambiar contexto o cerrar la sesión. Error/resultado
incierto no publica recibo ni provoca reintento. Mismo ejecutor NativeLaunchSession.

Gate dirigido95 casos:93 correctos y2 errores del fixture de texto, sin nuevos
fallos de producción. Las frases se estaban comparando con el Text completo;
se comprueban explícitamente como subcadenas, manteniendo las comprobaciones de
paquete, advertencia de efecto no comprobado y ausencia de solicitudes extra.
Sólo esos2 casos se repitieron, ambos correctos, más Lint:0errores/26advertencias/
1información (igual al reporte online anterior).112 casos distintos de última
evidencia correcta en esta sesión, no una suite global ni112 casos nuevos.
Los XML/fallos originales se conservan; no se vuelven a sumar las119 previas.

Las primeras capturas aisladas claro/oscuro carecían de Surface en el fixture;
la versión clara no permite evaluar contraste contra el fondo real. Se conservan
como evidencia inicial; se preparan capturas comparables con Surface sobre fuente
anterior y actual, sin retocar PNG ni cambiar producción para arreglar la imagen.

## Capturas cerradas y trazabilidad

Comparativas Surface corregidas:3 sobre8645d4c y3 sobre567cc84, fixture idéntico
por hash y sin alterar producción para capturar. El worktree anterior era propio,
detached y se retiró al guardar evidencia. Los recortes iniciales se conservan
con su limitación explícita; no sirven para juzgar contraste claro. Lint y gate
funcional corresponden a producción567cc84, sin cambios funcionales posteriores.
[Comparativas, XML, hashes y límites](verification/c2-expansion-20261104/README.md).

| Checkpoint | Resultado |
|---|---|
|6d872d7|F01: ACTION_WEB_SEARCH público, sin duplicar búsqueda interna/documentada|
|8645d4c|F07: consulta explícita por app, revisión editable, exactos y3 sugerencias|
|c071455|F14/V02/V05/V06: compartir texto/enlaces, Gmail fijo, footer y foco al cancelar|
|567cc84|Estado REQUESTED explícito, sin afirmar efecto externo completado|

## Qué probar en la próxima APK autorizada

1. Mantener la prueba física prioritaria de Preview62: abrir desde Ajustes/otra
   app, Gboard cerrado→abierto→cerrado, reapertura, scroll/PDF largo, orientación,
   inspector/footer y teclado físico en Tab A9+; complementar con moto g86.
2. En una futura APK de este código: app exacta primero; «App términos», «buscar
   en App: términos» y «buscar términos en App». Revisar/editar y solicitar a la
   misma app. Si no publica capacidad, comprobar motivo sin abrir otra app.
3. Share/Process Text→«compartir en App»→incorporar contexto explícitamente→editar→
   revisar→confirmar. No apertura antes de confirmar; verificar destinatario en
   la app receptora. Gmail se prepara en Gmail aunque otro correo sea predeterminado.
4. Cancelar con Escape/Volver y comprobar consulta/foco; alternar320dp disponible
   con ventana restaurada, orientación y texto200%. Última acción alcanzable,
   scroll sin ejecutar y footer accesible. Restaurar borrador no debe ejecutar.
5. Diferenciar recibo Android de efecto real: comprobar en el destino la búsqueda,
   borrador o envío final. Registrar fallos sin repetir automáticamente solicitudes.

## Pendientes para cerrar C2

- Matriz física de capacidades por apps realmente instaladas/versiones y efectos
  externos observables; disponibilidad/contratos públicos no prueban acceso a
  cuentas o datos privados. Shortcuts siguen sujetos al rol/permisos de Android.
- Ampliar contexto/relevancia local de archivos autorizados y candidatos útiles,
  preservando exactos,3 sugerencias, cancelación y selección estable; sin IA remota
  por tecla ni inferencia de contenido privado.
- Generalizar envío dirigido de archivos/URI/MIME con grants mínimos y revisión,
  reutilizando el Sharesheet/selección existentes; este bloque incorpora texto y
  enlaces, no extrae/adjunta documentos automáticamente ni convierte URI en texto.
- Matriz de IME/rotación/foco, fuente200%/TalkBack y mediciones físicas de latencia,
  memoria/jank.112 casos JVM no cierran esos criterios ni C0/C1/C2 físicos.

No main, C3–C9, WebView, APK/Actions, nuevas dependencias/SDK/permisos sensibles,
servicios externos ni cambios de firma/facturación. Preview62/135a9af sigue siendo
la entrega instalada de referencia; estos commits sólo están en la rama/PR21.
El puntero integral e38eae8 permanece sin promover. Se libera exclusivamente la
reserva propia con nota de cierre atómica; fuente/artefactos anteriores conservados.
