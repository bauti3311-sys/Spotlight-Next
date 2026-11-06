# Preview66 — candidata Visual First; sin autorización de ejecución

Base remota verificada `7bc8a26a1a18d461099e2e0fb816afb8fa53465d` en
`feat/preview57-native-return-20261103`, PR21 abierto/borrador. Reserva propia nueva.
Preparar Preview66 está autorizado; **no** compilar APK, firmar, hacer preflight
real o despachar Actions. Las autorizaciones de65 no se reutilizan para66.

## Identidad

- versionCode66 > Preview65; versionName `2.0.1-raycast-preview-native57-v66`.
- Paquete `dev.bautista.spotlight.next.raycastpreview`, ARM64, minSDK26/target35.
- Optimizada con R8/shrink; noDebuggable; configuración de firma original intacta.
- Certificado histórico esperado: `885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba`.
- release.properties principal29/2.0.1 y demás variantes intactas. Sólo cambia la
  numeración de Raycast Preview (incluido sufijo experimental existente, no usado).

Se volvió a comprobar el APK65 conservado con apksigner/aapt2: paquete/code65,
un firmante, v2 y certificado requerido; SHA256
`a5857160720de93cbc5d3a71cb301d095a5e19c9872fdbd7ef63ef5d8e4cdb2e`.
No se abre un keystore, consulta secretos o genera certificado. **No hay APK66**:
su firma/metadata final deberán comprobarse tras una autorización de entrega nueva.

## Consolidación visual y auditoría acotada

Se conservan FocusB inspector/revisión contextual, Geist, radiosD1/10dp, anchoA
de Ajustes tablet, contraste/selecciónA, iconografía por rol, footer adaptable al
texto, paginaciónPDF envolvente y tarjeta de notificación con acciones visibles.
D5 sigue sólo test; ventana productiva abre140ms/cierra100ms, ambos archivos de
movimiento sin delta. Focus=false por defecto y opt-out conserva formas antiguas.

Revisión crítica V05/V06/V08 sin rediseño: footer con acciones ya cubierto; transporte
multimedia son3controles48dp; títulos largos limitados con elipsis; PDF separa navegación
y salto a página (campo con peso, Ir a continuación); notificaciones usan FlowRow.
No se encontró otro defecto crítico reproducible que justificara cambiar producción.

Cobertura nueva: dos escenas del componente real con presupuesto **330dp exterior,
266dp interior, texto200%, oscuro/claro**, metadatos largos, sesión multimedia sintética
en pausa, paginación3páginas y notificación sintética. Los botones de transporte no se
solapan; anteriores/siguiente y las cuatro acciones secundarias están visibles y se
alcanzan con scroll. Responder/Abrir deshabilitados cuando falta capacidad. No hay
envío, cuenta, IA, portapapeles o reproducción real. Navegación PDF es fixture sin
PdfRenderer. El scroll pertenece al contenedor de test, no un nuevo modo productivo.

No se repiten galerías ni regresiones históricas. Se reutiliza evidencia publicada:
35casosD1/PDF/movimiento,13footer y18notificaciones, sin sumarlos como pruebas nuevas
ni prometer una suite global. Informes y PNG originales anteriores permanecen intactos:
- [D1/PDF y nueve V](VISUAL-POLISH-D1-V06-V08-V10-20261106.md).
- [Footer adaptable](VISUAL-FOOTER-BUDGET-20261106.md).
- [Acciones de notificación](VISUAL-NOTIFICATION-CARD-20261106.md).

## Estado de las nueve V

| V | Estado de la candidata | Validación pendiente |
|---|---|---|
| V01 | Identidad y radiosD1 consolidados | Fondo real, opacidad y lectura |
| V02 | FocusB aprobado, preservado | Contextos especializados físicos |
| V03 | Acentos existentes, preservados | Contraste con iconos/fondos instalados |
| V04 | Jerarquía/selecciónA y ranking preservados | Corpus largo, TalkBack real |
| V05 | Footer con texto ampliado corregido | IME/atajos/etiquetas en equipo real |
| V06 | Paginación/footer/acciones adaptables | OneUI/Gboard/rotación/ventanas/scroll |
| V07 | Auditoría por rol completada; sin sustituciones | Legibilidad física |
| V08 | Campos/tarjetas/acciones coherentes; controles sin ancho cero | PDF real, multimedia y notificaciones reales |
| V10 | Producción140/100ms intacta; D5aislado | Fluidez/RAM y decisión tras comparar físicamente |

Las nueve V no se declaran cerradas físicamente. C3–C8, Spotify, Square, datos,
permisos, motor/ranking, acciones, NativeLaunchSession y confirmaciones quedan congelados.
Preview65 y checkpoints conservados; no main, merge, paquetes, firmas o dependencias.

## Validación física propuesta — después de entrega autorizada

1. Instalar66 sobre65 sin desinstalar ni borrar datos, primero Galaxy Tab A9+ y
   luego moto g86. Verificar versión, preferencias, Focus apagado por defecto o
   preferencia existente conservada, y retorno completo al aspecto anterior.
2. Claro/oscuro, texto100/200%, retrato/paisaje y ventana flotante: inicio, consulta,
   resultados, inspectorB, Ajustes sin límite de ancho, footer y más resultados.
3. Abrir/cerrarGboard, cambiar de campo, scroll sin ejecutar; teclado físico con
   Tab/ShiftTab/Enter/Escape, recuperación de consulta, cursor y selección.
4. PDF local autorizado de varias páginas: anterior/siguiente/Ir y scroll; no
   abrir otra app al deslizar. Notificación válida: acciones accesibles y Volver,
   sin probar envíos ni IA real sin autorización separada. Multimedia: metadatos
   y disponibilidad reales; no declarar Spotify resuelto ni reabrir sus rutas.
5. Registrar video breve de invocación con140ms actual, rotación y cambios de foco;
   medir tiempos/jank/RAM sólo con hardware. D5 no está integrado en la candidata:
   decidir120ms requiere un experimento físico futuro separado, no una comparación
   ficticia de66 con una animación que no contiene.

Fallo crítico de foco, control oculto, pérdida de datos o toque que ejecute al hacer
scroll bloquea aceptación. Registrar dispositivo/OneUI/Gboard/escala/tema y evidencia;
no aceptar sólo por pruebas JVM. Seguir con funciones sólo después de la decisión
del propietario, sin rehacer FocusB.

## Único procedimiento recomendado de entrega

Autorizar posteriormente el par exacto publicado y **un solo intento**. Adquirir
reserva NUEVA con checkpoint=workflow autorizado, verificar HEAD/PR21/owner+lease,
manifest y ausencia de intento anterior por fuente/workflow. Despachar una sola vez
`build-apk.yml` desde la rama del PR21, `mode=native-delivery`, los SHA exactos,
`confirm_preview=AUTHORIZE_PREVIEW_66_ONCE` y owner/lease de esa reserva nueva.
Sin retry, re-run, fallback u otra variante. Guardas existentes, presupuestoUS$0.

Flujo aislado existente: preflight de certificado histórico; un build optimizado
unsigned de fuente exacta sin secretos; firma aislada; verificación independiente
APK+ZIP. La fuente aprobada y el workflow se revisan separados. Confirmar paquete,
code/name66, noDebuggable, ARM64/SDK/16KiB, payload, un firmante histórico/v2, SHA256,
y ZIP idéntico a git archive del source_sha. Publicar sólo Preview privada, nunca
estable; liberar únicamente la reserva de entrega con notas atómicas.

Esta preparación no autoriza ni ejecuta ese procedimiento. No se exige reutilizar
la reserva actual, que se libera al cerrar la preparación.

## Fuente congelada y validación final

source_sha: `66d4ea0b28192f84ba26b9a451a8299d90f84cfb`.
El manifiesto66 fija esa misma fuente como functional_base_sha y
allowed_delta_paths=[]: no se admite un delta productivo adicional. El SHA de
workflow es el commit posterior que contiene este manifiesto y este informe;
se publica completo en las notas de cierre y en la entrega al propietario.
El YAML build-apk.yml no cambia: se reutiliza native-delivery existente con
confirmación derivada del manifiesto66, no se crea otra ruta de entrega.

Resultados nuevos: **2pruebas Compose dirigidas correctas**, lintDebug
**0errores/26advertencias/1informativo**, inspección AGP66 correcta y
**19pruebas offline de guardas/identidad correctas**. No se ejecuta policy.main
contra un intento real ni preflight con secretos; sólo fixtures en tests offline.
No se repiten35/13/18 pruebas históricas, builds o galerías equivalentes.
La interfaz/funciones `app/src/main` es idéntica a7bc8a26; los únicos cambios
productivos de preparación son code/name66 en app/build.gradle.kts.

Galería consolidada: HTML/PDF8páginas/ZIP con3pares históricos aprobados y4PNG
nuevos de auditoría, diferenciados explícitamente. Evidencia, manifiesto de imágenes,
identidad AGP, firma previa65 y logs en verification/native66-preparation-20261106.
No confundir candidata preparada con APK construida ni nueveV cerradas físicamente.
