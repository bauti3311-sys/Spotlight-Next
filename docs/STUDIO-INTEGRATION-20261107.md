# Studio integrado — B con cuerpo A en Ajustes

Base remota f36d8306c1bb081e380d6c72d08a600d85a98854 verificada, PR21 borrador.
El propietario aprobó la combinación. No es un nuevo modo ni un rediseño de motores.

## Cambios productivos

- Barra: superficie enmarcada y diferenciada, radioD1/10dp; preserva campo, foco,
  callbacks y objetivos táctiles. Con espacio añade8dp de margen; con ancho útil
  reducido o texto ampliado omite el margen, conservando el contenido.
- Resultados: tinte suave del seleccionado y marca lateral3dp. No nuevo contorno
  permanente: el borde de foco de teclado sigue siendo independiente. Textos,
  metadatos, orden, tamaño de iconos, selección y gesto nativos intactos.
- Inspector: tarjeta14dp sobre superficie alta, acento contextual y búsqueda
  prioritaria existentes. Administración plegable conservada. No decoración extra
  ni padding cuando el ancho disponible no alcanza420dp×escala efectiva de texto.
- ActionPanel: misma superficie de tarjeta, mismos filtros, grupos y ejecución.
- Footer: separador superior sin alterar altura o controles adaptables.
- Ajustes: cuerpo plano, tipografía/selección/colores y distribución tablet intactos.
  Sólo recibe la barra Studio común. No maxWidth720/860dp, tarjeta decorativa deB
  ni cambio de preferencias. Opt-out de Focus conserva el aspecto anterior.

StudioSurface mide el ancho asignado con BoxWithConstraints, no pantalla física.
Considera fontScaleAndroid×preferencia de texto; umbral360dp para barra y420dp para
paneles. No nuevo setting ni servicio. En estrechos conserva presupuesto interior,
48dp táctiles y capacidades; los marcos sólo dibujan, no interceptan input. No blur,
animación nueva, dependencia o bitmap adicional. No se afirma mejora medida de RAM/FPS.
El inspector conserva el ajuste de contraste de PremiumMaterial sobre sus superficies.

La adaptación simplifica lo ornamental respecto del prototipo: sin contorno extra
permanente por fila ni nueva franja decorativa de acento que compita con Buscar.
No cambia jerarquía aprobada ni se añaden acciones. D5 permanece aislado, ventana
productiva140/100ms intacta. Focusfalse por defecto y reversible.

## Verificación y comparativas

30pares originales equivalentes,6perfiles: tablet1000×720 y teléfono390×760,
claro/oscuro; teléfono330×760/texto200% en ambos temas. Cinco escenas cada uno:
inicio, resultados, inspector, ActionPanel y Ajustes. Antes: producciónf36d830;
después: producción integrada, sin Theme overrides de las propuestas.
Inicio/resultados/inspector usan SpotlightPanel real. Acciones/Ajustes son sus
componentes productivos aislados. Datos PM/iconos/media de laboratorio explícitos,
no aplicaciones ni cuentas reales. No inferir tres sugerencias de una escena con
una sola señal relevante: se conserva el motor de HomeSuggestions sin filler.

18casos dirigidos correctos: galería6, FocusB360/200%restauración1, footer7,
tokens/reversibilidad3, presupuestoStudio1 (4escenarios+legacy). Lint0E26W1Info.
La prueba de presupuesto confirma ancho640→624 barra/628panel cuando hay espacio,
ancho330/200%→330 sin margen y legacy→640 sin adorno; callback único/targets48dp.
No se elimina ninguna aserción. Un error inicial de importOffset se corrigió antes
del pase verde y su log se conserva. No confundir con defecto funcional publicado.

Total: **20casos dirigidos correctos**. Dos contratos de ActionPanel se ejecutaron adicionalmente bajo Focus para cubrir
Tab/ShiftTab/Enter y CtrlF/filtro/CtrlK con los nuevos wrappers, sin repetir lint.
XML/logs/manifiestoSHA256/generador en verification/studio-integration-20261107.
GaleríaHTML con filtros/ampliación y PDF8páginas, ZIPoriginales sin retoque.

## Continuidad

Sin delta en app/control, integraciones, data, search, firma/paquete, Gradle,
manifest66/workflow, Motion ni OverlayWindowMotion. C3–C8/Spotify/Square/WhatsApp y
documentos funcionalmente pausados; Preview66 y checkpoints preservados. NoActions/APK.

| V | Estado tras integrar |
|---|---|
| V01 | Materiales Studio y D1 integrados; lectura/fondo físico pendiente |
| V02 | Tarjeta Studio, jerarquía FocusB y plegable conservados |
| V03 | Acentos por aplicación conservados; no color global nuevo |
| V04 | Selección perceptible integrada; ranking/títulos/roles intactos |
| V05 | Continuidad de materiales y footer adaptable integrados |
| V06 | Decoración cede al ancho/texto; tablet/teléfono y200% cubiertos enCompose |
| V07 | Geist/iconografía por rol sin sustitución |
| V08 | Tarjeta contextual integrada; PDF/notificaciones66 conservados |
| V10 | Animación140ms intacta; D5noaprobado; mediciones físicas pendientes |

Para cerrar VisualFirst falta probar esta integración en una entrega futura
específicamente autorizada: OneUI/Gboard, ventana flotante/IME/rotación, restauración,
scroll/táctil/teclado, contraste con fondos/iconos reales y fluidez/RAM. El feedback
positivo de66 avala su base, no esta integración todavía sinAPK. No generar otra
candidata/versión ni reanudar funciones por esta integración sin instrucción.

Checkpoint de implementación/capturas/pruebas: `3662cf79f0982dea08e48689f67ec7932f1ca4a9`.
Continuidad documental posterior sin delta productivo adicional.
