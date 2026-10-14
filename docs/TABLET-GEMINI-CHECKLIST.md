# Aceptación física — Samsung Galaxy Tab A9+ / One UI

La entrega debe identificar SOURCE_COMMIT y contener APK verificada con certificado histórico11b0b3b37544bd95b128de52798fa7ffb7489f94819614b1ada383bd4ee7a6cf, paquete dev.bautista.spotlight.next y código29. No instalar una APK unsigned/debug local como actualización compatible. Ningún dispositivo apareció en adb devices durante Work; la instalación física no se presume validada.

Antes de actualizar, anotar versión actual, tema/ancho y un elemento Guardado. En Mis archivos abrir la APK firmada y elegir **Actualizar**, sin desinstalar ni borrar datos. Si aparece conflicto o downgrade, conservar la instalación y registrar el mensaje; comprobar la identidad de la APK realmente instalada antes de otro intento. Después verificar que Guardados y preferencias anteriores sigan presentes.

| Caso | Recorrido y resultado esperado |
|---|---|
| Apertura fría/F1.3 | Reiniciar la tablet o detener Spotlight; invocar desde el acceso habitual. La barra debe aparecer y aceptar foco desde el primer toque. Incidencia abierta: registrar si no aparece o pierde foco; no darla por corregida |
| Invocación/teclado | Probar atajo físico y acceso táctil con la preferencia de teclado elegida. Teclado virtual según preferencia, sin forzar IME con teclado físico |
| Editor | Escribir, mover cursor al medio, insertar texto, seleccionar con Shift, usar Home/End. Sin autocompletar con flecha derecha ni perder selección/caracteres |
| Completar | Buscar app parcialmente, Ctrl+Space y seguir escribiendo. Compleción explícita y cursor al final |
| Aplicaciones | Buscar app instalada, seleccionar otra con flechas y tocar una tercera. Iconos del dispositivo y apertura exacta de la fila tocada |
| Calculadora | Escribir2+2: un único4 dentro de la lista; sin inspector duplicado |
| Acciones | Tab abre/recorre; Shift+Tab retrocede; Enter ejecuta la acción enfocada. Tab nunca abre la app |
| Escape | Desde acciones, Escape vuelve a la lista conservando consulta/foco; otro Escape cierra ventana |
| PDF real | Autorizar PDF local, buscar y seleccionar: página real en Quick Look. Abrir externo únicamente por acción explícita. Ampliar y colapsar conservando documento; Escape respeta capas. Un mensaje de error del renderer no cuenta como página correcta |
| Apariencia | Obsidian Glass, Graphite y macOS Light: selección gris elegante, gradientes discretos, secundarios legibles, sin recorte en barra o títulos |
| Personalización | Densidades Compacta/Equilibrada/Espaciosa y tamaño de texto; un solo slider de ancho. Al estrechar la ventana, subtítulo de dos líneas legible; preferencias persistidas |
| Rotación/IME | Horizontal y vertical con resultados, acciones y teclado abierto: barra/lista/pie dentro del espacio útil. Sin inspector lateral permanente |
| IA sin proveedor | Estado honesto, ninguna respuesta falsa ni envío remoto automático al escribir. No evaluar streaming sin proveedor autorizado |
| Fondo/blur | Abrir sobre fondos claros/oscuros de otras apps; texto nítido y sin huella rectangular. Blur entre ventanas aún pendiente de validación, no se presume implementado |

Si falla: registrar SOURCE_COMMIT, Android/OneUI, orientación, tipo de invocación, preferencia de teclado y pasos mínimos; captura/video sin contenido privado. Separar instalación, foco, geometría y estilo.
