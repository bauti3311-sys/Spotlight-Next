# Preview62 física — distribución y regreso del menú

El propietario probó Preview62 en Samsung Galaxy Tab A9+ y aportó dos fotos
1280×800. No es aceptación física: reporta problemas. La etiqueta Preview61 en
la primera foto es el nombre del archivo inspeccionado, no la versión instalada;
la segunda muestra62 en Ajustes. Se conservaron C1 y C2 sobre la rama de PR21.

## Evidencia observada y diagnóstico acotado

En1000071027.jpg, Gboard comienza aproximadamente en y469; Spotlight ocupa
aproximadamente y165–400, con resultados/inspector pequeños y espacio libre
inferior. La captura no contiene WindowMetrics, insets ni densidad runtime:
no permite atribuir todo el síntoma a una API OEM concreta ni convertir px en dp.
En1000071029.jpg, las opciones sustituyen los resultados/footer; no hay regreso
táctil visible. La apertura de tres puntos puede vivir justamente en el footer
que desaparece al mostrar opciones; volver a tocarlo deja de ser posible.

Se comparó la fuente APK62/135a9af con la preparación a82af60: AdaptiveLayout,
OverlayViewport y SpotlightBar no habían cambiado. Las reservas de altura en
SpotlightPanel también conservaban la lógica relevante de62, aunque C2 añadió
formularios/contexto. No se dio por repetido el fallo anterior de frame/insets.

Dos causas reproducibles independientes:

1. El panel restaba reservas fijas112/168dp y48/90dp en capas, además del padding
   real. Un fixture nativo con viewport independiente300dp sólo usaba268dp y
   recortaba el inspector, dejando32dp utilizables fuera de su superficie.
2. La posición vertical preferida se calculaba sobre toda la pantalla aunque el
   teclado ocultara parte. Sólo se movía al quedar menos de124dp mínimos, dejando
   un desplazamiento superior innecesario cuando ya faltaba altura útil.

Son defectos demostrados y compatibles con la captura, no una traza del dispositivo.
OverlayViewport, flags WindowManager y los contratos de insets62 quedan intactos.

## Cambios

- Medir header/footer reales frente al presupuesto independiente de ventana;
  descontar una vez padding/divisor y repartir el resto al inspector/resultados.
  El resultado corto sigue encogiendo naturalmente; no se fuerza fullscreen ni
  se elimina la preferencia de lista compacta o el máximo de3 sugerencias iniciales.
- Posición relativa al área libre sobre IME, restaurando la fracción guardada al
  desaparecer el teclado. Sin editar preferencias ni cambiar ancho según IME.
- Botón48dp «Volver a resultados» fijo antes de la lista desplazable del menú.
  Cierra opciones y devuelve foco sin limpiar consulta/selección/inspector. Se
  conserva el orden existente de Esc/Back: opciones antes de inspector/overlay.
  Se descuenta el header/padding al limitar menú y se oculta la bandeja redundante
  mientras está abierto, sin cambiar sus archivos ni permisos.

Sin paleta/tipografía/rediseño, motores, permisos, dependencias ni integraciones
nuevas. El formulario C2 y NativeLaunchSession siguen siendo los mismos.

## Verificación y límites

28 casos distintos con última evidencia correcta en rondas dirigidas,0 fallos/
errores/omisiones pendientes. Lint0E/27W/1Info, mismos avisos que el cierre C2.
No se suman pruebas históricas ni se afirma una suite global.20 regresiones de
C1/C2/barra correctas;8 casos distintos nuevos de tablet/posición completan28.

[Comparativas, XML, logs y procedencia](verification/tablet62-fixes-20261105/README.md).
Se conservan rojo inicial y fallos de fixtures, sin borrar/suprimir pruebas.

La primera expectativa de170dp de inspector era incorrecta: el footer real mide
52dp. Se reemplazó por igualdad con presupuesto menos padding/header/footer/divisor
medidos, manteniendo la aserción que reproduce los32dp perdidos antes del parche.
El fixture APK requirió expandir la lista compacta antes de buscar el séptimo
archivo; el nombre aparece en encabezado y metadatos, por lo que se verifica el
encabezado visible. El fixture de regreso a220dp esperaba erróneamente el inspector
lateral: Adaptive apila por altura, y recuperar600dp comprueba que sigue seleccionado.
Ninguno de esos ajustes autoriza envíos ni aumenta timeouts para ocultar fallos.

No se generó APK ni se ejecutó Actions/firma. El par inicial63 queda conservado
y pausado; se presentará nueva fuente/workflow después del gate local. No main,
merge, C3–C9 ni promoción del checkpoint integral.

## Validación física prioritaria de la nueva63

1. Tab A9+: abrir desde Ajustes/Apariencia, Gboard abierto→cerrado→abierto,
   inspector del mismo APK/documento y consultas largas. Comparar altura útil,
   filas, scroll, última acción/footer y recuperación al ocultar teclado.
2. Abrir tres puntos desde header y footer; volver táctil, Esc y Volver/gesto Android.
   Conservar consulta/selección y regresar al estado anterior sin cerrar Spotlight.
3. Rotación y teclado físico; repetir con ventana compacta/amplia, fuente200%,
   archivos seleccionados y formulario de compartir. Sin repetición de solicitudes.
4. Repetir lo pertinente en moto g86; registrar Android/OneUI/Gboard y esperado/
   observado. La vida real de grants, transiciones OEM, jank y foco entre ventanas
   continúan pendientes; JVM/Robolectric no los valida físicamente.

Mantener [checklist C2](C2-PHYSICAL-ACCEPTANCE-20261105.md). C3 sólo después de
revisar la nueva validación física; no se inicia por preparar otra candidata.
