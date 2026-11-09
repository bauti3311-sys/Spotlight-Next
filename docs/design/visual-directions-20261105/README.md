# Tres direcciones — exploración, sin adopción

El propietario evaluó V1–V4 aproximadamente 5,5/10 y eligió ajustar composición
antes de adoptar. Se conserva el checkpoint V4
5df7f7a1907c7c2fe31e6106504d6e30c413ab07 y el trabajo local previo, sin revertirlo.
Ninguna de estas propuestas está implementada. No activar LocalPremiumReview.
No compilar, generar APK ni ejecutar Actions durante esta exploración. C1/C2,
contratos de teclado/touch, ranking exacto, permisos, identidad y main se conservan.
La reserva propia se mantiene por pedido explícito, sin tomar otra reserva.

## Láminas conceptuales

Las imágenes son conceptos generados, NO capturas Compose ni evidencia de APK.
Cada lámina usa cuatro columnas: inicio, Vivaldi, Spotify, PDF; cuatro filas:
tablet oscuro/claro y teléfono oscuro/claro. Son 16 vistas por dirección.
Los textos pequeños, iconos, medidas y controles dibujados no son especificación
funcional. No acreditan contraste, foco, rendimiento ni equivalencia pixel a pixel.
Las capturas reales de referencia están en
[la evidencia anterior](../../verification/premium-review-20261105/before/).
Proceden del laboratorio Compose, no de una nueva captura física del propietario.
El inicio anterior contiene menos sugerencias que el escenario conceptual; por eso
la comparación es de dirección/composición y no una prueba funcional equivalente.

- [01 — Obsidian Liquid](obsidian-liquid.png): grafito/vidrio humo, tres planos,
  inspector como escenario contenido. Claro plateado, no inversión del oscuro.
- [02 — Atelier Studio](atelier-studio.png): composición editorial, índice de
  resultados, papel marfil/tinta y carbón cálido, jerarquía de pesos/espacio.
- [03 — Raycast Neo](raycast-neo.png): consulta protagonista, resultados precisos,
  inspector integrado, acento coral y footer reducido.

## Contrato de escena para una futura prueba Compose

Mismos inputs en las tres direcciones: inicio con hasta tres sugerencias
Vivaldi/Spotify/Archivos; consulta Vivaldi con aplicación exacta primero; Spotify
con Horizonte / Sesión de prueba local en pausa; Vestibular.pdf, origen autorizado
Documentos, PDF de cuatro páginas. El arte de sesión y las páginas son ilustrativos.
No agregar acciones simuladas. Buscar en Spotify debe reflejar capacidad real y
su estado indisponible; no prometer búsqueda por mostrarla en un dibujo.
El panel abstracto rojo de Vivaldi en Neo es decoración conceptual, no un dato ni
una capacidad nueva: recomiendo retirarlo de una implementación.
El PDF nativo sigue sin validarse con estas imágenes; el runtime de la galería
anterior sólo pudo capturar su error real. No confundir ambos tipos de evidencia.

## Tipografía

Inter ya está integrada localmente en res/font/inter.ttf y Theme.kt y tiene
licenses/Inter-OFL.txt. Cambiar sólo el nombre de fuente no resuelve el problema.
Propuesta: escala de búsqueda/títulos renovada, pesos 400/500/600, metadatos
contenidos y tamaños sujetos a densidad/fontScale reales. Geist es alternativa
viable como FontFamily local sin SDK ni red en ejecución. La licencia oficial
consultada el 05/11/2026 es SIL OFL 1.1:
https://github.com/vercel/geist-font/blob/main/OFL.txt
https://github.com/vercel/geist-font/blob/main/LICENSE.txt
Inter: https://github.com/rsms/inter/blob/master/LICENSE.txt
No se añadió ningún archivo de fuente. Antes de integrarlo, fijar revisión y hash
del binario, adjuntar OFL/copyright y comprobar pesos, español y fallback.
La imagen generada sugiere estilo tipográfico; no prueba que renderice Geist real.

## Viabilidad Kotlin/Compose y coste previsto, no medido

| Dirección | Implementación posible tras elección | Coste y riesgo |
|---|---|---|
| Liquid | Box/Surface superpuestas, rellenos limitados, borde fino, sombra estática, inspector contextual | Mayor overdraw que las otras. Blur real opcional, nunca requisito; depende de API/ventana y exige medición GPU/jank en One UI. Sin blur, hay alternativa visual completa. |
| Atelier | Layout adaptable, tipografía local, separadores finos, márgenes editoriales y hoja contextual en teléfono | Menor carga de efectos prevista; cuidar altura/fontScale y no reservar columnas vacías. |
| Neo | Barra de búsqueda dominante, lista y escenario continuo; selección por pintura y footer contextual | Carga de pintura previsiblemente baja; evitar re-medición animada y pérdida de foco al reemplazar lista/detalle. |

En todas: altura contextual acotada al área real, scroll por región, footer visible,
48dp táctiles aun con iconos discretos; hasta tres sugerencias iniciales, aplicación
exacta primero; Tab/Enter/Escape/flechas y retorno táctil existentes. El inspector
muestra sólo datos disponibles. No estirar home para llenar una ventana grande.
Transiciones candidatas 100–160ms sólo de opacidad/color, sin duplicar formularios,
y reducir movimiento. Duraciones tentativas, no resultados de rendimiento.
PDF usa el renderer existente, nunca render por cada recomposición/tecla.
No hay mediciones nuevas de RAM, FPS/p95, GPU ni batería; no afirmar coste cero.

## Recomendación para elegir, sin consolidar

Atelier ofrece el salto más claro respecto de V63 y el modo claro con identidad
más propia. Liquid merece elegirse si profundidad oscura pesa más; exige contener
la cantidad de cápsulas y lograr calidad con blur desactivado. Neo tiene la afinidad
más directa con consola Raycast, pero corre más riesgo de parecer otra evolución
incremental. No mezclar las tres antes de elegir: se perdería la diferencia buscada.

Siguiente paso sólo tras decisión: desarrollar una escena de la dirección elegida
en Compose, comparar ambas densidades/temas con datos idénticos y revisar antes
de extenderla. La elección visual no autoriza APK/Actions ni cierra las incidencias
físicas Preview62/63 en One UI.
