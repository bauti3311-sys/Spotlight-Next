# Visual First — footer legible con texto ampliado

Continuación desde `d570a8689a63caeb4e29eb4273dba34e2570317b`, PR21 abierto/borrador.
D1 integrado, D2/D3/D4A conservados; D5 sigue exclusivamente en tests. C3–C8 pausados.

## Defecto comprobado y corrección

El footer elegía la distribución de tablet sólo por ancho>=700dp, sin considerar
el texto ampliado. Con700dp/texto200%, sugerencia, más resultados y acción principal
larga, los rótulos auxiliares consumían el espacio: el botón «Acciones» medía
**0dp de ancho** y el footer crecía a308dp de alto. Captura y aserción roja preservadas.
No era un cambio deseable de densidad ni un fallo de una aplicación externa.

Ahora el breakpoint del propio footer considera escala de fuente Android y la
preferencia de tamaño de texto existente. Por debajo del presupuesto legible usa
**la distribución compacta ya existente**, sin nueva pantalla o modo. En el caso
reproducido, el footer ocupa52dp y «Más», acción principal y «Acciones» tienen espacio
visible. No se altera el tamaño tipográfico, colores, ejecutores o atajos. Las
leyendas auxiliares de teclado se omiten como ya ocurría en teléfono; Tab/Enter
siguen funcionando. Esto no limita el ancho de Ajustes ni del overlay.

El cambio es una corrección mínima de `GlassContextFooter`: factor de texto y
condición de layout. Sin modificar callbacks, estados de búsqueda, ranking, motor,
WindowManager, FocusB, iconos, firma, paquete, versión o preferencias guardadas.
No nueva dependencia, blur ni animación; se conserva el comportamiento normal.

## Evidencia y alcance

Comparativas A/B originales Compose/API33, densidad1, mismos datos/temas/ancho:
700dp oscuro200%, tablet1000dp claro/oscuro200%, teléfono330dp claro200% y
tablet1000dp claro100%. El antes usa producción d570a86; el después contiene sólo
la corrección del footer. Fixture aislado del componente real, sin efectos externos.
La captura normal de tablet y la de teléfono conservan exactamente los mismos bytes.

Prueba roja:5casos,4correctos/1fallido (Acciones ancho0 en700dp/texto200%).
Resultado final: **13casos dirigidos distintos correctos** (7footer,5geometría,
1FocusB). LintDebug **0errores,26warnings,1informativo**, sin incidencias nuevas.
Dos casos adicionales confirman teléfono con opciones y escala Android130%×
preferencia interna130%, sin repetir geometría ni lint.

Después: controles visibles>=48dp de ancho dentro del footer, expansión,
ejecución única, apertura/cierre de acciones y deshabilitado mientras el panel está
abierto. Teclado Tab/Enter verificado en el componente; Escape/restauración en la
regresión de FocusB. Geometría del panel con12resultados, scroll, inspector,
reducción/restauración de viewport por presupuesto IME y preferencia compacta.
XML/logs, manifestSHA256 y originales en `verification/visual-footer-20261106`.
No hay nueva aceptación física de OneUI/Gboard/IME/WindowManager/TalkBack o jank/RAM;
la geometría JVM y un callback contado no demuestran efectos en aplicaciones reales.

## Continuidad de las nueve V

V05/V06 avanzan con una corrección objetiva del dock; V01/D1 y V07/auditoría
mantienen el cierre técnico anterior. V02/V03/V04 y V08 aprobadas se preservan.
V10 no adopta el experimento120ms. La ventana real sigue140ms/100ms, sin cambios.
Las nueve V no están cerradas físicamente. Próximo paso: aceptación física del
conjunto visual cuando se autorice otra entrega, con OneUI/Gboard y texto ampliado;
D5 debe compararse con la ventana real140ms antes de decidir. No reanudar C3–C8 ni
rehacer componentes aprobados por falta de un dispositivo.

Checkpoint de fuente/pruebas/capturas publicado: `d9bc0b53f25b3fb9c21a65cd90b22767dfaffe7d`.
La documentación de continuidad posterior conserva esa misma fuente productiva.
