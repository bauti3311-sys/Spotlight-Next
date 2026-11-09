# Focus Refinado / Focus Studio — decisión visual pendiente

Base remota cfb8fa863de381203d1395e464fab4a78d441c8a/PR21 borrador.
El propietario reporta haber probado y disfrutado Focus en Preview66: conservarlo
como base estética. Esto no equivale a cerrar todos los casos físicos OneUI/Gboard.
La entrega66 tuvo un intentoCI exitoso; su anterior bloqueo de descarga local es
histórico, no motivo para repetir Actions. No se modifica identidad66.

## Auditoría acotada

1. Materiales muy próximos entre barra, lista, inspector y acciones: cuesta distinguir
   planos. Mejorar separación y continuidad de materiales antes que agregar efectos.
2. Selección de resultados discreta: un indicador lateral mejora localización sin
   tocar títulos15sp, metadatos, iconos por rol ni ranking.
3. Inspector: conservar búsqueda prioritaria y administración plegable. Un marco/acento
   delimita mejor el contexto; no se propone reordenar acciones ni rellenar espacios.
4. Ajustes: más tarjetas no siempre mejora la lectura. En B, el marco y padding hacen
   más perceptible la agrupación, pero desplazan la opacidad hacia abajo. Mantener
   ancho completo y tres temas en fila; no volver a límites720/860dp.

## Dos propuestas, sólo en tests

| Región | A — Focus Refinado | B — Focus Studio | Coste de B |
|---|---|---|---|
| Barra | Plano ligeramente diferenciado, proporciones nativas | Campo enmarcado, radioD1/10dp, márgenes10dp | Menos ancho de texto;18dp extra de cromo vertical |
| Resultados | Indicador lateral2dp y separación discreta | Indicador3dp, contorno y tinte suave del seleccionado | Mayor presencia del acento; revisar competencia con inspector |
| Inspector | Plano continuo más reconocible | Tarjeta14dp y línea de acento contextual | Menor ancho interior; más scroll con texto200% |
| ActionPanel/footer | Materiales y divisores coherentes | Panel definido sobre plano más bajo; footer unido visualmente | Más marcos, riesgo de exceso de contenedores |
| Ajustes | Cuerpo plano y distribución actual | Grupo visual con borde y superficie | Desplaza controles hacia abajo; no recomendado como regla general |

**Recomendación:** B para barra/inspector/acciones, manteniendo el cuerpo de Ajustes
más plano de A. Elegir antes de implementar; A es alternativa más conservadora.
No nueva fuente, blur, dependencia, iconos, pantalla, función o animación. B comparte
componentes y motores; no es otra arquitectura. D5 sigue sin aprobarse,140ms intactos.

## Qué muestran las imágenes

78PNG originales Compose/API33/nativegraphics,26comparaciones A/B con referenciaC.
Cuatro perfiles principales: tablet1000×720 y teléfono390×760, claro/oscuro,5escenas
(inicio/resultados/inspector/ActionPanel/Ajustes). Dos perfiles adicionales330×760,
texto200%, ambos temas,3escenas críticas. Mismos datos, consulta, selección y tamaño
entreC/A/B. HTML con filtros/zoom/referenciaC y PDF8páginas. Sin imágenes generadas.

**Son composiciones de componentes productivos en un fixture, no capturas de APK ni
SpotlightPanel completo.** C usa SpotlightResultList actual; A/B decoran ResultRow
real. Inspector usa ApplicationInspector embedded=true y AppCapabilityActions reales,
con búsqueda prioritaria y administración plegable. SettingsLayout/GlassAppearance,
SpotlightBar/GlassContextFooter/ActionPanelView son componentes productivos.
Motores SearchEngine/CompactResults proveen el mismo orden a todas las direcciones.
Home usa contexto textual, acción reciente y app favorita sintéticos: máximo3
sugerencias y máximo1app se conservan, no se inventa relleno de tres aplicaciones.
Iconos con letras son fixtures PM, no logos instalados reales.

Las alturas por escena son encuadres de comparación; no una propuesta de cambiar
WindowManager o la política de ventana. La estabilidad de la barra puede orientar
una futura implementación, pero estas capturas no verifican transiciones reales,
NativeLaunchSession, recuperación de estado, teclado, Gboard ni FPS/RAM.

## Validación

6casos nuevos correctos, uno por perfil. Comprueban tres filas reales iniciales,
ranking exacto Vivaldi primero, footer dentro del viewport, tres temas tablet en
la misma fila, cero callbacks externos y ninguna Activity iniciada. No suite histórica
ni lint repetidos: no hay delta productivo. XML/log/hash y pilotos conservados.
Pilotos corrigieron sólo fixtures: favoritos no producen3apps por contrato; faltaban
señales contextuales. El presupuesto de Settings se alineó con padding8dp del fixture
anterior tras detectar salto de línea por redondeo; no se eliminó la aserción de fila.
El primer chequeo de coordenadas de texto se sustituyó por no-solapamiento horizontal
porque texto de distinta altura no determina la fila de tarjetas. Todo historial rojo
se conserva, no contado como pruebas verdes. No se ha probado teclado nuevo A/B.

## Nueve V

| V | Estado |
|---|---|
| V01 | Base Obsidian/D1 conservada; acabados A/B pendientes de elección |
| V02 | FocusB aprobado conservado; nuevo encuadre es propuesta |
| V03 | Acentos contextuales existentes conservados, sin marca global nueva |
| V04 | Jerarquía/ranking intactos; indicador/contorno propuestos |
| V05 | Footer adaptable conservado; unión de materiales propuesta |
| V06 | Correcciones66 conservadas; compactos200% renderizados, físico exhaustivo pendiente |
| V07 | Geist/iconos por rol conservados; PM sintéticos en galería |
| V08 | Tarjetas/notificaciones/PDF66 conservados; no cambios funcionales |
| V10 |140ms actual intacto; D5aislado; sin métricas físicas nuevas |

No declarar las nueve cerradas por el feedback general del propietario. C3–C8 siguen
pausados. No adoptar variante, APK, Actions, main, firma, paquete o preferencias.
