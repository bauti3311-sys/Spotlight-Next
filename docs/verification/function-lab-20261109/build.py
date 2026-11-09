"""Offline choice catalogue. No Android execution, network or production imports."""
from pathlib import Path
import json,base64,hashlib,subprocess
O=Path(__file__).resolve().parent
R=O.parents[2]
OLD=R/'docs/verification/choice-catalogue-20261108/catalogo.json'
items=json.loads(OLD.read_text())['functions']
BASE='dccc9c5fc34a58432e7101b4934a3b15add2ad11'
APK='143104ce8d83f6ad6b5b4acd80897db579816fcd'
# Present code is a reusable base, not proof of the entire proposed journey.
bases=[
'Calculadora local con parser acotado. El historial de operaciones es la ampliación.',
'Conversión local de unidades y temperaturas. Los favoritos son la ampliación.',
'Diferencia entre fechas ya existente; sumar días desde la barra es la propuesta.',
'Reloj por ciudades existente; comparar horarios de una fecha futura es la ampliación.',
'Calculadora existente; reparto con formulario y redondeo explícito es nuevo.',
'Calculadora existente; guardar una tasa manual etiquetada es nuevo.',
'TextTools ya ofrece transformaciones locales; se propone un recorrido directo y previsualizable.',
'TextTools aporta la base; el comparador de dos versiones es nuevo.',
'ExpandSnippet y preferencias de snippets existen; campos revisables son la ampliación.',
'Notas locales existentes; adjuntar una referencia elegida es la ampliación.',
'Captura en memoria, favoritos y caducidad ya existen; búsqueda directa y explicación de retención son la ampliación.',
'TextTools existente; limpieza revisable de parámetros de seguimiento es nueva.',
'SAF e índice local existentes; acotar la consulta por carpeta es la ampliación.',
'Extracción local e índice existentes; priorizar coincidencias literales revisables es la ampliación.',
'Quick Look y paginación PDF existentes; guardar progreso por documento es la ampliación.',
'OCR local y presupuesto de extracción existen; disponibilidad y exactitud dependen del motor y del dispositivo.',
'C5.1/C5.2 sólo en el PR, fuera de APK70. C5 sigue bloqueado para validación física; no está en la barra.',
'Índice de archivos existente; agrupar duplicados por SHA-256 sería nuevo.',
'Agenda local ya consulta calendarios autorizados; resumen hoy/mañana es la ampliación.',
'Intents de calendario existentes; formulario de borrador explícito es la ampliación.',
'Lectura de agenda existente; cálculo de huecos en calendarios elegidos sería nuevo.',
'People y corrección de homónimos existentes; no se amplía WhatsApp ni se activa C3 aquí.',
'ACTION_DIAL existente; revisión de persona y número es la ampliación.',
'Preparación de correo existente; campos revisables son la ampliación. Nunca envío directo.',
'AppCapabilities, IntegrationRegistry e inspector ya existen. Se propone explicar mejor disponibilidad y límites, no otro ejecutor.',
'Búsqueda contextual y revisión app/sitio ya existen. Se propone elegir destinos guardados sin fallback.',
'Compartir mediante Intents existentes; revisión de archivo, destino y permiso temporal es la ampliación.',
'ReplyService existente; búsqueda en notificaciones actuales autorizadas es la ampliación.',
'RemoteInput existente, condicionado a una notificación vigente; mejora sensible de C3 pausada.',
'MediaControl existente. Sólo sesión y acciones anunciadas; no resuelve la reproducción Spotify desde la barra.',
'HomeSuggestions y favoritos existentes; parámetros y caducidad por acción son la ampliación.',
'Alias y preferencias existentes; parámetros explícitos y previsualizados son la ampliación.',
'Preferencias y consultas fijadas existentes; agrupación por proyecto sería la ampliación.',
'Notas y referencias locales existentes; colecciones de recursos serían nuevas.',
'TimerForm y permiso SET_ALARM ya existen; recorrido al reloj instalado es la ampliación.',
'DeviceResources y Ajustes existentes; explicación conjunta de disponibilidad sería la ampliación.'
]
permissions=[
'No requiere permisos nuevos.','No requiere permisos nuevos.','No requiere permisos nuevos.','Zonas horarias locales; sin ubicación.','No requiere permisos nuevos.','Tasa ingresada a mano; sin cotización remota.',
'Sólo texto escrito o pegado explícitamente.','Sólo dos textos ingresados explícitamente.','Texto local; copiar sólo por acción explícita.','Referencia elegida; nunca leer otra app de fondo.','Sólo captura autorizada con Spotlight en foco; no historial ajeno.','URL ingresada; no se visita ni se verifica su destino.',
'SAF sobre carpeta elegida; revocación invalida resultados.','Sólo archivos autorizados e indexados.','Revalidar permiso y documento al restaurar.','Motor OCR instalado y política de recursos; sin descarga automática.','SAF/lifecycle/OCR físicos pendientes; modelo semántico no evaluado.','Leer sólo archivos autorizados; no borrar ni modificar.',
'READ_CALENDAR con consentimiento existente.','ACTION_INSERT: el calendario revisa y guarda.','READ_CALENDAR; no conoce agendas de terceros.','READ_CONTACTS opcional; elegir número exacto.','ACTION_DIAL: sin permiso CALL_PHONE ni llamada directa.','ACTION_SENDTO/mailto; sin leer bandeja ni enviar.',
'PackageManager y entradas públicas; no acceso a datos privados.','Intent público disponible; la app destino puede usar red.','SAF + URI temporal; diálogo Android no prueba entrega.','Acceso a notificaciones autorizado explícitamente.','RemoteInput vigente y confirmación sensible. C3 pausado.','Sesión accesible y permiso existente; C4 pausado.',
'Identificadores estables; no guardar autorizaciones sensibles.','Sin scripts ni ejecución arbitraria.','Guardar consulta, no textos privados obsoletos.','Referencias revalidadas; nunca apertura masiva automática.','SET_ALARM ya declarado; requiere reloj compatible.','Sólo muestra estado; no concede permisos ni descarga modelos.'
]
# Each demonstration owns different inputs/results; Android effects stay fixtures.
scenes=[
(['Expresión'],['(1250 + 890) / 2'],'1070','Cálculo editable · historial sólo de esta demo'),
(['Cantidad','De','A'],['2,5','km','m'],'2500 m','Conversión local editable'),
(['Fecha (AAAA-MM-DD)','Días'],['2026-11-15','14'],'29/11/2026','Sin crear eventos'),
(['Fecha','Hora en Buenos Aires','Destino IANA'],['2026-11-15','14:00','Europe/Madrid'],'18:00 · Madrid','Zonas horarias del navegador; horario de origen UTC−03'),
(['Cuenta','Propina %','Personas'],['18000','10','3'],'6600 por persona','Sin pagos · redondeo a centavos'),
(['Importe USD','Tasa manual ARS por USD'],['100','1000'],'100000 ARS','No es una cotización actual'),
(['Texto'],['  Hola    equipo  \n  reunión    mañana  '],'Hola equipo\nreunión mañana','Transformación real en el navegador'),
(['Antes','Después'],['Entrega el lunes\nPresupuesto: 900','Entrega el martes\nPresupuesto: 900'],'− Entrega el lunes\n+ Entrega el martes\n  Presupuesto: 900','Comparación de líneas; no interpreta significado'),
(['Plantilla','Nombre','Fecha'],['Hola {nombre}, nos vemos el {fecha}.','Luz','18/11'],'Hola Luz, nos vemos el 18/11.','No escribe en otras aplicaciones'),
(['Nota','Referencia elegida'],['Revisar presupuesto el jueves','Proyecto Aurora · archivo ficticio'],'Nota con referencia revisable','Guardar en Spotlight aún no se implementa aquí'),
(['Buscar texto capturado'],['presupuesto'],'Presupuesto Aurora · capturado en foco\nCaduca en 15 minutos · fixture','No lee el portapapeles del dispositivo'),
(['Enlace'],['https://ejemplo.test/informe?id=42&utm_source=boletin&utm_campaign=noviembre#resumen'],'https://ejemplo.test/informe?id=42#resumen','Se conservan id y fragmento; no se abre la URL'),
(['Consulta','Carpeta elegida'],['presupuesto','Documentos / Aurora'],'Presupuesto Aurora.txt\nCarpeta autorizada · fixture','Sin explorar carpetas del dispositivo'),
(['Frase literal'],['entrega el martes'],'Plan Aurora.txt\n«La entrega el martes requiere revisión.»','Fragmento literal de un TXT ficticio, sin página'),
(['Documento','Página a retomar'],['Guía Aurora.pdf','12'],'Retomar página 12 · referencia ficticia','La demo no contiene ni abre ese PDF'),
(['Imagen de ejemplo'],['Recibo ficticio · baja nitidez'],'Total: 30?0\nCarácter incierto: revisar imagen','Texto predefinido, no es OCR ejecutado'),
(['Consulta literal'],['entrega el martes'],'Plan Aurora.txt\n«La entrega el martes requiere revisión.»','Cita de un TXT fixture; página no aplicable'),
(['Carpeta'],['Carpeta de prueba'],'Dos TXT ficticios con contenido idéntico','Hash calculado sobre texto fixture; nunca sobre tus archivos'),
(['Día'],['Hoy'],'09:00 Revisión Aurora\n14:00 Planificación','Agenda ficticia; no consulta calendarios'),
(['Título','Fecha y hora'],['Revisión Aurora','2026-11-18 10:00'],'Borrador: Revisión Aurora\n18/11/2026 10:00','El calendario tendría la confirmación final'),
(['Ventana (HH:MM-HH:MM)','Duración (min)'],['09:00-17:00','45'],'09:00–09:45','Cálculo sobre agenda ficticia: 10–11 y 14–15 ocupados'),
(['Contacto'],['Alex Rivera'],'Dos coincidencias · elegí persona y número\nAlex Rivera · +54 9 11 5555-0101\nAlex Rivera · +54 9 351 555-0102','Homónimos ficticios; ninguna elección automática'),
(['Persona','Número'],['Luz Benítez','+54 9 11 5555-0103'],'Revisar Luz Benítez\n+54 9 11 5555-0103','Solicitud de marcador; no llamada completada'),
(['Para','Asunto','Mensaje'],['luz@example.test','Revisión Aurora','Te comparto el borrador para revisar.'],'Borrador de correo revisable','No abre correo ni envía nada'),
(['Aplicación'],['Mapas de ejemplo'],'Abrir aplicación\nBuscar: disponible si Android resuelve la entrada pública','La disponibilidad real se volvería a comprobar'),
(['Consulta','Aplicación receptora','Sitio web'],['cafeterías','Mapas de ejemplo','maps.example.test'],'Revisión: cafeterías\nApp y sitio se muestran por separado','Sin cambiar de destino automáticamente'),
(['Contenido','Destino elegido'],['Informe Aurora.txt · fixture','Compartir con…'],'Revisar archivo y destino\nPermiso URI temporal','No comparte ningún archivo real'),
(['Buscar en notificaciones'],['Aurora'],'Luz · Revisión Aurora mañana\nNotificación actual ficticia','No representa conversaciones completas'),
(['Notificación','Respuesta'],['Luz · Aurora · fixture','Recibido, lo reviso.'],'Destinatario y notificación vigentes por revisar','Demostración sensible: no envía respuestas'),
(['Sesión'],['Reproductor de prueba'],'Tema de prueba · sesión ficticia\nPausa disponible · salto no anunciado','Sin Spotify, App Remote ni Web API'),
(['Acción favorita'],['Conversión · km a m'],'Favorito: conversión km → m\nParámetros se revisan antes de usar','No guarda autorizaciones de envío'),
(['Alias','Parámetro','Destino explícito'],['mapa','cafeterías','Mapas de ejemplo'],'mapa cafeterías → revisar búsqueda en Mapas','Sin scripts ni fallback a otra aplicación'),
(['Proyecto','Consulta'],['Aurora','presupuesto'],'Aurora · presupuesto\nCarpeta a revalidar al abrir','No guarda fragmentos sensibles'),
(['Colección'],['Proyecto Aurora'],'Notas de reunión.txt\nCarpeta Aurora\nApp de calendario\nEnlace del proyecto','Abrir un recurso elegido; nunca todos automáticamente'),
(['Minutos','Etiqueta'],['15','Descanso'],'Revisar temporizador de 15 minutos','Solicitud al reloj; no temporizador observado'),
(['Área'],['Documentos'],'Archivos: desactivados · fixture\nModelo: no instalado · fixture\nModo privado: activo · fixture','No lee el estado real de tu dispositivo')
]
recommended={'F08','F12','F34','F33','F01','F02','F07','F36'}
for i,x in enumerate(items):
 x.update(base=bases[i],permission=permissions[i],labels=scenes[i][0],inputs=scenes[i][1],preview=scenes[i][2],demoNote=scenes[i][3],recommended=x['id'] in recommended,kind='editable' if i in list(range(9))+[11,20] else 'fixture',paused=x['id'] in {'F17','F22','F29','F30'})
 if x['id']=='F11':x['source']='app/src/main/java/dev/bautista/spotlight/data/LocalStore.kt'
 text=(R/x['source']).read_text()
 # Evidence is exact code snapshot; no inference that it completes the proposal.
 lines=text.splitlines();x['evidence_line']=next((n for n,l in enumerate(lines,1) if 'object ' in l or 'class ' in l or 'sealed ' in l),1)
 x['source_sha256']=hashlib.sha256(text.encode()).hexdigest()
 try:
  b=subprocess.check_output(['git','show',APK+':'+x['source']],cwd=R,stderr=subprocess.DEVNULL)
  x['present_in_70']=True;x['same_file_in_70']=hashlib.sha256(b).hexdigest()==x['source_sha256']
 except subprocess.CalledProcessError:x['present_in_70']=False;x['same_file_in_70']=False
 # Real SHA-256 for a tiny fixture, not a file scan.
 x['fixture_sha256']=hashlib.sha256('Proyecto Aurora\nEntrega el martes.\n'.encode()).hexdigest() if x['id']=='F18' else None
payload={'version':'funciones-20261109','source_head':BASE,'preview70_source':APK,'old_catalogue_sha256':hashlib.sha256(OLD.read_bytes()).hexdigest(),'functions':items,'production_changes':False,'android_execution':False}
(O/'funciones.json').write_text(json.dumps(payload,ensure_ascii=False,indent=2)+'\n')
html=(O/'template.html').read_text()
for token,val in [('DATA',json.dumps(payload,ensure_ascii=False).replace('<','\\u003c')),('FONT',base64.b64encode((R/'app/src/main/res/font/geist_regular.ttf').read_bytes()).decode()),('SEMI',base64.b64encode((R/'app/src/main/res/font/geist_semibold.ttf').read_bytes()).decode())]:html=html.replace('__'+token+'__',val)
(O/'Elegir-funciones.html').write_text(html)
print('36 funciones; generado HTML offline y evidencia de código.')
