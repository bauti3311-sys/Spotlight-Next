# Puente sin medias tintas: ocho acabados completos

El propietario pidió cambios mucho mayores de paleta, tipografía, espaciado, relleno y transparencia. La tanda anterior se conserva. Esta exploración combina extremos deliberados sin modificar el producto.

Base remota verificada `705ff256aa73ee30f0a5f21cd8e5db4425ab41f2`; PR21 abierto/en borrador; reserva nueva propia. [Galería offline](verification/puente-bold-20261108/Galeria-Puente-Extremos.html) · [PDF](verification/puente-bold-20261108/Puente-Extremos.pdf) · [Ocho juntas](verification/puente-bold-20261108/Comparacion-tablet.jpg).

| Código | Personalidad | Tipografía | Relleno / fila / barra / separación dp | Radio / opacidad |
|---|---|---|---|---|
| X01 Terminal ácido | Negro y lima; selección sólida | Mono ×0,94 | 8 /48 /56 /12 | 0 /100% |
| X02 Papel editorial | Crema y rojo óxido; pausa y amplitud | Serif ×1,22 | 28 /72 /88 /32 | 2 /100% |
| X03 Cobalto eléctrico | Azul saturado y amarillo | Inter ×1,12 | 20 /62 /78 /24 | 8 /100% |
| X04 Chicle | Rosa y ciruela; redondeado | Geist ×1,18 | 24 /68 /84 /24 | 30 /100% |
| X05 Señal naranja | Naranja y negro; compacto y frontal | Inter ×1,08 | 12 /52 /68 /14 | 0 /100% |
| X06 Hielo translúcido | Cian; apertura al fondo | Sistema ×1 | 16 /56 /70 /22 | 24 /76% |
| X07 Berenjena teatral | Violeta y dorado; editorial nocturno | Serif ×1,18 | 22 /66 /82 /26 | 20 /94% |
| X08 Blanco brutal | Blanco/negro; contraste gráfico | Geist ×1,25 | 10 /60 /80 /12 | 0 /100% |

Son combinaciones completas renderizadas. Se conserva la composición Puente, no todos sus acabados. La fuente de sistema corresponde a API33 del laboratorio; puede variar en el dispositivo. Sólo fuentes ya disponibles, sin descarga.

## Transparencia aislada

O01–O04 mantienen Hielo, datos, fuente, escala, rellenos y fondo: sólo cambia la opacidad100/90/76/60%. Valores de opacidad de la superficie, no porcentaje de transparencia. Encabezados añaden otra capa con esa opacidad y por ello son efectivamente más opacos. El fondo es Compose real con texto común; los efectos no se aplican en HTML ni se retocan capturas. Sin blur. Menor opacidad deja interferir el texto detrás;60% se presenta como extremo desaconsejado para lectura, no como propuesta accesible validada.

Las X cambian varios ejes para que se perciban estilos completos; NO es una evaluación causal de cada token. Las O sí aíslan un eje. Elegir una X selecciona esa combinación; elegir una O registra una preferencia aparte y no modifica automáticamente la X ni crea una mezcla falsa.

## Evidencia y pruebas

40 PNG nuevos:8acabados×2perfiles×2escenas (acciones/contexto)=32, más4opacidades×2perfiles=8. Se reutilizan2referencias anteriores exactas de Puente con hashes. Tablet1200×800dp y teléfono412×860dp, API33/Robolectric/Compose; mismo fixture de4resultados, IDs, consulta `proyecto`, selección de app o PDF ficticio. El nombre técnico de archivo `dark` se conserva del harness: NO significa que las paletas claras sean un tema oscuro productivo.

Cuatro pruebas focalizadas aprobadas (0fallos/errores/omitidas): capturas tablet, teléfono, contraste opaco de texto/selección y mínimos táctiles, teclado/selección/recibo local sin Activity externa. Se comprueba que las envolventes quedan dentro del lienzo. El contexto móvil ajusta altura al espacio sobrante y conserva scroll. BUILD SUCCESSFUL54s. Primera compilación del laboratorio falló por un token mal separado, corregido; ambos logs y XML publicados. No hubo fallo productivo ni APK. No se repitieron suites históricas ni lint global: producción sin delta.

Contraste >=4,5 en pares principales opacos y selección; NO certifica todos los textos ni superficies translúcidas. La interferencia visible al60% es parte de la evidencia. Sin pruebas físicas de IME, ampliación200%, scroll táctil real, ventanas Android independientes, consumo o fluidez. La captura representa dos superficies dentro de una única composición; no afirma dos overlays reales.

Chromium:42imágenes decodificadas sin red,8/4opciones, comparador, Escape, zoom de referencia, exportación JSON, escena desactivada para opacidad y sin desborde horizontal390px. Se cargó el HTML autosuficiente exacto mediante set_content; navegaciónfile:// no probada por política del navegador administrado.

Recomendación: X02 por personalidad cálida distinta, X03 por identidad inmediata, X08 por contundencia gráfica. X01/X05 son extremos más técnicos/intensos; X04 más táctil; X06 sacrifica lectura por transparencia. Ninguna aprobada para producto.

## Preservación y reproducción

Sólo se agrega un banco app/src/test y documentos/evidencia. Producción, FocusStudio/Preview69/C2/C5/36funciones, compilación/manifiestos/firmas/versiones/paquetes, bancos y galerías anteriores intactos. Sin APK/testAPK, instalaciones, Actions, dependencias, permisos ni main. Preview69 autorizada ya consumida.

```sh
bash gradlew :app:testDebugUnitTest --tests '*PuenteBoldGalleryTest' -I scripts/directed-tests.init.gradle -I docs/verification/c2-app-open-20261107/no-apk.init.gradle -PspotlightDirectedClasses=dev.bautista.spotlight.ui.PuenteBoldGalleryTest -Pkotlin.compiler.execution.strategy=in-process --offline --no-daemon --max-workers=1 --console=plain
python3 docs/verification/puente-bold-20261108/build_html.py
python3 docs/verification/puente-bold-20261108/browser.py
python3 docs/verification/puente-bold-20261108/contact_sheets.py
python3 docs/verification/puente-bold-20261108/pdf.py
```

Requiere entorno Java17/SDK y caché local existente. Los generadores de entrega leen los PNG originales publicados; no retocan la UI ni requieren Gradle. Reserva propia se libera atómicamente con nota de cierre tras publicar.
