# Preview63 — preparación, sin autorización de ejecución

Actualización posterior: [Preview63 entregada y verificada](NATIVE63-DELIVERY-20261105.md)
desde ef4e60f/b4bd17e, único intento expresamente autorizado41947253299. No
repetir ni usar el par histórico71d6980/a82af60. Las esperas inferiores son historia.

## Actualización posterior — correcciones físicas reportadas en62

El par fuente71d6980e50e6206776014946a2a606521fad9fd8 / workflow
a82af60a8e212364a8f40ed3a9af49323bfa7983 queda **histórico y pausado** por
instrucción del propietario. No compilar ese par. La candidata nueva incorpora
las [correcciones verificadas de distribución y regreso táctil](TABLET62-REGRESSIONS-20261105.md),
conservando code63 y todas las funciones C1/C2. El nuevo SHA fuente es el commit
de corrección; un commit posterior fija fuente y base funcional al mismo SHA
verificado, con delta permitido vacío. Esto evita agregar código después del gate.
El par exacto nuevo se publica en PR21/notas y se presenta al propietario.
No build APK, firma ni Actions hasta autorización expresa de ese nuevo par.

En el checklist físico, repetir prioritariamente los dos casos de las capturas:
abrir desde Ajustes/Apariencia→Gboard abierto/cerrado→inspector de archivo y
footer; tres puntos→Volver a resultados táctil/Esc/Volver→misma consulta y selección.
La prueba real62 reportada tiene defectos: no está aceptada físicamente. La
candidata63 requiere nueva prueba en Tab A9+ y moto g86, incluido teclado físico.

El resto describe la preparación inicial y el procedimiento reutilizable; las
referencias al delta de2 archivos son históricas para71d6980, no para la nueva fuente.

Base consolidada261eef4669f775b3257b707953c6987d374a1d10, PR21 borrador,
rama feat/preview57-native-return-20261103. C1, corrección de altura/IME y todos
los cambios C2 ya están en su historia: no se fusionan ramas ni se repiten features.
El commit fuente candidato contiene sólo numeración Raycast63 y este documento.
El manifiesto posterior fija ese SHA fuente; su commit es el SHA de tooling/workflow.
Presentar ambos exactos al propietario antes de cualquier build/firma/dispatch.

## Identidad preparada

- Paquete:dev.bautista.spotlight.next.raycastpreview, intacto.
- versionCode63>62; versionName optimizado2.0.1-raycast-preview-native57-v63.
- Certificado requerido885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba.
- R8/shrinking activos; ARM64, min26/target35 y no debuggable conservados.
- Sin cambios frente261eef4 en app/src, release.properties, permisos, dependencias,
  datos, motores o contratos de ventana. Sólo cambia la numeración de Raycast.

Se prepara actualización de Raycast Preview62, no del paquete principal. No se
genera clave ni se sustituye la firma histórica. Compatibilidad estática prevista
por paquete/certificado requerido/código creciente: verificar en el APK real
después de una ejecución autorizada y al instalar sin desinstalar/borrar datos.
Si Android informa conflicto de firma/paquete, cancelar; no resolver desinstalando.

Respecto de la APK62 sí hay avances C2 heredados: consultas públicas por app,
texto/enlaces, revisión de archivos originales, grants temporales y búsqueda local.
Incluyen queries SEND/MULTIPLE y recepción MIME amplia en el manifiesto, sin nuevas
uses-permission; caché FileIndex v3→v4 aditiva preservando contenido/metadatos.
No afirmar manifiesto/esquemas idénticos a62 ni conservación física ya validada.

## Evidencia reutilizada

El [cierre técnico C2](C2-TECHNICAL-CLOSURE-20261105.md) publica2 casos de
restauración y6 regresiones mínimas correctas sobre84ab6d7;261eef4 sólo añade
evidencia/documentación. Lint0E/27W/1Info; aviso adicional de nombre en helper
Compose de pruebas, sin supresiones. Reutilizar XML/hashes y evidencia anterior
C1/ventana/C2 sin sumar rondas como una suite global ni repetir Android por versión.
La preparación sólo revisa delta, manifiesto y contratos Python offline pertinentes.
No compila, firma, ejecuta preflight del firmador ni Actions; tampoco instala.

## Procedimiento reutilizable para un único intento posterior

1. Recibir autorización expresa para el par SHA fuente/SHA workflow revisados.
   Verificar HEAD remoto y adquirir reserva nueva propia con checkpoint=SHA
   workflow autorizado. La reserva de preparación se libera y no sirve al dispatch.
2. Reutilizar build-apk.yml, mode=native-delivery, desde la rama de PR21. Indicar
   source_sha exacto, workflow_sha exacto, reservation_owner y lease propio vivo,
   confirm_preview=AUTHORIZE_PREVIEW_63_ONCE. No ejecutar este paso ahora.
3. Las guardas existentes verifican actor/triggering_actor/repositorio/ref/evento,
   SHA evento=workflow=revisado, historial autoritativo, primer intento y reserva;
   comparan fuente con base funcional261eef4 y delta explícito de2 archivos.
   Si cambió el SHA, permiso, reserva o historial no accesible: detenerse.
4. Tras autorización: preflight aislado; un build unsigned optimizado sin secretos;
   firma en runner separado con tooling revisado sin ejecutar fuente/Gradle.
   Mantener timeouts5/30/10min, Ubuntu estándar, concurrencia1 sin cancelación
   automática, caché sólo lectura y retención unsigned1día/final3días.
5. Verificar paquete/code63/nombre/SDK/ABI/noDebuggable, v2/un firmante histórico,
   zipalign16KiB/ELF, payload firmado idéntico al unsigned y ZIP del SHA fuente.
   Descargar y comprobar independientemente con herramientas oficiales y cmp de
   ZIP frente git archive; registrar fuente/workflow/run y hashes APK/ZIP.
6. Entregar APK y ZIP reales mediante enlaces individuales comprobados, más
   respaldo privado del artefacto GitHub con caducidad explícita. No inventar
   enlaces ni usar un blob temporal como único respaldo. Liberar sólo reserva
   propia con notas atómicas; no promover el checkpoint integral.

US$0/bloqueo al límite y secretos históricos conservados. No pedir otra revisión
manual de facturación por checkpoint, no tocar presupuesto/medios de pago ni
prometer saldo no observado. Si GitHub bloquea cuota/presupuesto, detenerse sin
retry, Re-run, fuente alternativa o runner pago. Un fallo requiere diagnóstico y
decisión expresa nueva; no cambiar guardas para sortearlo. No modificar native60.

## Checklist físico breve — Tab A9+ y moto g86

Usar archivos propios sin datos privados. Registrar dispositivo/Android/OneUI,
Gboard, versiones de receptores y resultado OK/fallo/no disponible por punto.
La física de Preview62 sigue pendiente:63 podrá validar el arreglo heredado,
sin convertir retrospectivamente62 en probada. Guardar ajustes y versión anterior.

| Paso | Prueba práctica y resultado esperado |
|---|---|
|1. Actualización| Instalar encima de Raycast Preview62 sin borrar datos; conservar ajustes/permisos/contextos autorizados. Registrar cualquier conflicto y cancelar |
|2. Ventana/Gboard| Abrir desde Ajustes y otra app; Gboard cerrado→abierto→cerrado3 veces; cerrar/reabrir y rotar. Altura útil, barra, inspector y footer alcanzables; probar teclado físico si hay |
|3. Búsqueda| Probar app exacta, «PDFs de ayer», «buscar documento de vestibular», «compartir archivo Ayala» sobre árbol elegido. Tres sugerencias iniciales como máximo, selección estable; scroll/PDF no abre externos |
|4. Archivos/destino| Seleccionar1 PDF y luego varios; revisar nombre/origen/MIME, confirmar una vez y elegir receptor compatible en Android. Antes de confirmar no se abre ninguna app. Comprobar adjuntos en el receptor, sin asumir envío real por el recibo |
|5. Cancelación| Cancelar revisión y selector, volver/escape: recuperar consulta/foco/selección sin duplicados ni cambio automático de destino. Probar Gmail y WhatsApp personal/Business disponibles como paquetes distintos |
|6. Restauración| Recibir2 archivos, añadir un tercero, rotar/recrear: conservar3. Restaurar revisión exige revisar nuevamente, nunca confirmar/ejecutar solo. Volver del receptor y reabrir no repite solicitud |
|7. Permiso/cambio| Tras revisar, retirar permiso o borrar/cambiar documento: confirmar debe dar error recuperable sin alternativa automática. Probar proveedor SAF local y remoto autorizado si está disponible |
|8. Legibilidad| Nombres largos, fuente200%, TalkBack y compacto/amplio: acciones accesibles y footer visible. Anotar lentitud del primer acceso al índice y de búsquedas para medirla |

Para un fallo: anotar paso, esperado/observado y versión; captura/video corto sin
contenido privado. No repetir envíos reales para depurar. Matriz por app/proveedor
y métricas detalladas: [aceptación C2](C2-PHYSICAL-ACCEPTANCE-20261105.md).

C3 (WhatsApp profundo) queda después de revisar la validación física63. No se
inicia en esta preparación ni por ejecutar automáticamente la futura entrega.
