# Corrección visual contra imágenes A/B

Base: d831136ec079a0eb49093ca119e4f8996d47f83e, rama feat/raycast-sidebar-permissions-20261020. Las tres imágenes del usuario se abrieron realmente: Screenshot_20261020_234500(2).jpg (A, tablet con WhatsApp), Spotlight Next en modo oscuro(4).png (B, objetivo prioritario) y Screenshot_20261020_235105_Vivaldi(1).jpg (referencia adicional Raycast). No copiar recursos de terceros ni interfaces web.

## Auditoría antes de implementar

| Área | A: defecto observado | B: dirección que se implementa |
|---|---|---|
| Geometría | Franja de navegación entre búsqueda y lista; pie de expansión separado; inspector recortado | Una cabecera integrada, cuerpo 62/38 y pie único; más espacio útil para contenido |
| Material | Fondo subyacente de Ajustes legible entre filas, superficies inconsistentes | Grafito azulado con gradiente suave, translucidez acotada, borde continuo y reflejo tenue; sin blur caro |
| Espaciado | Identidad del inspector pegada a la lista y controles; filas con título y descripción compitiendo horizontalmente | Insets coherentes, identidad separada, título sobre subtítulo y tipo alineado a la derecha |
| Jerarquía | Botón principal reconocible pero favorito aparece como otro botón azul centrado | Identidad con icono grande, principal azul con pista Enter, secundarias neutras alineadas, metadatos al final |
| Densidad | Bandas de navegación consumen la altura; información lateral apenas visible | Controles de 48dp con contenido visual compacto, regiones continuas y scroll accesible |
| Tipografía | Nombre y explicación en una misma línea crean ruido; etiquetas sin jerarquía | Inter existente, pesos moderados, subtítulos fríos, encabezados discretos |
| Panel derecho | Pocas acciones visibles; ancho y altura efectiva pequeños | Primaria de ancho completo, secundarias en filas, propiedades reales en pares; nada destructivo arriba |
| Comportamiento percibido | Rótulo ACCIONES y Volver hacen parecer otra pantalla | Búsqueda siempre presente, retorno accesible integrado; selección actualiza inspector sin ejecutar |
| Calidad general | Mezcla de ajustes y overlay, banda inferior pesada | Material estable y unificado, separación fina y sombra exterior; Ajustes con rail azul discreto y menos cajas |

La B es una composición visual: sus datos/atajos no acreditan capacidades Android. No inventar desarrollador, tamaño, nueva ventana ni atajos no registrados. Mantener solo acciones existentes y propiedades consultadas localmente. Conservar máximo tres resultados iniciales; no añadir resultados para imitar las cuatro filas de la B.

## Plan y coordinación

HEAD local/remoto coinciden, árbol limpio. Reserva remota 3427ec377eb99ee00a598edb7f03125255a991e9 del propietario de esta continuidad work-recovery-20261018-cd7b8fe1285c, checkpoint d831136. El script status no tiene autenticación Git por terminal (ls-remote exit128); lectura mediante conector GitHub confirma propietario y SHA. Publicar mediante avances normales y CAS, nunca force. No hay build anterior activo declarado.

Implementación y pruebas todavía pendientes. Capturas Compose de estados equivalentes antes/después, revisión directa contra B, suites de gestos/teclado/MediaSession/PhoneUse/Gemini/permisos y Ajustes, lint y APK Preview independiente con mismo certificado. La translucidez real entre ventanas, instalación y rendimiento One UI requieren tablet; no sustituirlos con capturas Robolectric.
