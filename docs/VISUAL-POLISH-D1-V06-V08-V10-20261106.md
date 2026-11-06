> **Continuación posterior:** [acciones de notificación legibles](VISUAL-NOTIFICATION-CARD-20261106.md)
> corrige botones con ancho cero en teléfono sin nuevas capacidades.

> **Continuación posterior:** [footer con texto ampliado](VISUAL-FOOTER-BUDGET-20261106.md)
> corrige Acciones con ancho cero a700dp/200%, preservando escenas normales.

# Visual First — integración D1 y pulido V06/V08/V10

Base remota `7332b2effc717ba8a5c59b9ed9a9158848500351`, PR #21 abierto/borrador.
Decisiones expresas del propietario: D1 B aprobado; D2/D3/D4 conservan A;
D5 sólo experimento. C3–C8 siguen congelados. Preview65 y checkpoints conservados.

## Resultado

- **D1 integrado:** radio compartido10dp en shapes small/extraSmall de Focus;
  campos/controles equivalentes heredan el token. No se cambian dimensiones,
  hit targets, colores, tipografía, selección ni radios estructurales22dp.
  Shapes compartido e inmutable; desactivar Focus restaura defaults4/8dp incluso
  dentro de un Theme Focus. Focus sigue desactivado por defecto.
- Copy descriptivo de Apariencia coherente con Obsidian Glass, temas y tipografía.
  Claves, nombres de presets, preferencias y migraciones intactos.
- **V06/V08:** paginación PDF pasa de Row a FlowRow con separación4dp y
  asignación visible mínima48dp por botón. Defecto reproducido a330dp/texto200%:
  «Siguiente» se partía dentro del botón; ahora pasa completo a la fila siguiente.
  No se modifica PdfRenderer, URI, caché, apertura ni callbacks de página.
  El Row normal medía menos48dp de altura visible; esto no demuestra que el
  área táctil efectiva de Material fuera menor48dp.
- WhatsApp/documentos/multimedia usan los mismos componentes, capacidades y datos
  de fixture. Ningún cambio funcional ni nueva tarjeta. Ajustes tablet mantiene
  ancho completo, tres temas por fila y opacidad visible. Geist17sp, contraste,
  selección e iconos por rol permanecen. No se detectó inconsistencia objetiva
  que justifique sustituir glyphs o normalizar todos los tamaños.

## Corrección importante sobre movimiento

El informe anterior confundía el helper `revealMotion`160ms con la animación
productiva de ventana. **El helper no tiene llamadas productivas.** La ventana
real `OverlayWindowMotion` abre140ms desde alfa0,92/traslación2dp; cierra100ms.
Ambos archivos productivos permanecen sin cambios. La galería histórica es
inmutable, pero sus rótulos «actual160ms» se deben interpretar como referencia
al helper, no al comportamiento real de WindowManager.

D5 compara esa referencia160ms con opacidad0→1/120ms **sólo en test** usando el
mismo MediaInspector en pausa. Incluye24fotogramas, cancelación al sustituir entrada,
retirada/reapertura con identidad nueva, reducción global de movimiento durante
la transición y ausencia de replay al reactivar animaciones. No hay nuevo modo,
preferencia, servicio o hook productivo. Una entrada0→1 podría percibirse más lenta
que la ventana real que ya empieza casi visible. No se recomienda adopción global
sin comparar físicamente con140ms y medir jank/tiempos/RAM; el reloj JVM no lo mide.

## Verificación

**35 casos distintos vigentes correctos**, XML y logs conservados en
`verification/visual-polish-20261106`, resumen por caso en `test-summary.json`.
Incluyen galería8perfiles, tokens/reversibilidad3, paginación4 (táctil, Tab,
Shift+Tab, Enter y Escape sin efecto), iconos2, movimiento5, ventana3,
homónimos6, Spotify3 y FocusB/recuperación1. LintDebug: **0errores,26warnings,
1informativo**, sin nuevas incidencias. No Actions, assemble ni APK.

Historia roja conservada: paginación3casos reprodujeron el defecto antes de la
corrección. Primer ensayo D5 falló por reutilizar identidad al representar una
reapertura; se corrigió el fixture para una entrada nueva, sin retirar aserciones,
y los5casos finales pasaron. Dos fallos de compilación del test de teclado por
opt-ins experimentales se corrigieron sólo en test antes de la ejecución final.
No se contabilizan esos intentos como pruebas verdes.

75pares antes/después:72escenas (9×8perfiles) y3paginaciones;25PNG de movimiento.
Antes: originales inmutables de la auditoría; árbol `app/src/main` de f27c867
idéntico al de7332b2e, comprobado en manifiesto. Después: producción candidata
real, sin los overrides B rechazados de ancho/16sp/selección neutra. Manifiesto
fija SHA256 de archivos fuente y PNG. Inicio/resultados/inspector son SpotlightPanel;
periféricos, componentes reales aislados. PDF es paginación con3páginas fixture,
no una prueba del renderizador. Multimedia en pausa, sin audio ni comandos reales.

No hay dispositivos en `adb devices -l`. Sin evidencia física nueva de OneUI,
Gboard, teclado del sistema, WindowManager, rotación, audio o FPS/RAM. IME300dp es
reserva de espacio simulada. No se declara éxito físico ni cierre integral.

## Estado de las nueve V

| Mejora | Estado tras este checkpoint | Cierre pendiente |
|---|---|---|
| V01 Obsidian Glass2.0 | D1 y nomenclatura completados en código; identidad aprobada conservada | Fondo/opacidad y lectura física |
| V02 Inspector contextual | FocusB aprobado conservado; sin otro rediseño | Aceptación física de contextos especializados |
| V03 Acentos dinámicos | Implementación actual conservada | Contraste con iconos/fondos reales |
| V04 Jerarquía resultados | A conservada, búsqueda/ranking intactos | Corpus largo y accesibilidad física |
| V05 Footer contextual | A conservado, matriz de viewport correcta | IME/atajos y etiquetas largas reales |
| V06 Ventana adaptable | Mejora PDF330dp/200% completada; sin límite global de ancho | OneUI/Gboard/rotación/ventanas físicas |
| V07 Iconografía | Auditoría por rol completada; sin cambio injustificado | Lectura física y revisión de glyphs instalados |
| V08 Tarjetas contextuales | Radios coherentes y paginación adaptable completados en componentes | PDF real, multimedia y flujos externos físicos |
| V10 Microinteracciones | Experimento aislado probado; producción intacta | Comparación140ms real vs120ms, mediciones y decisión posterior |

No se asigna porcentaje ficticio ni se declaran las nueve cerradas. Próximo paso
prioritario: revisar esta galería y validar físicamente la candidata mediante una
entrega futura específicamente autorizada. Hasta entonces no rehacer Focus B ni
reanudar C3–C8. La única decisión adicional pendiente es adoptar o descartar D5
tras evidencia física; no bloquea los acabados estáticos ya aprobados.

## Reproducción y revisión

`verification/visual-polish-20261106/README.md` documenta filtros y artefactos.
HTML autocontenido con75pares, filtros por perfil y ampliación; PDF12páginas con
10pares seleccionados y movimiento; ZIP incluye PNG originales y manifiesto.
No son imágenes conceptuales ni capturas retocadas. No requiere servidor externo.

## Checkpoints publicados

- D1: `8da2b457fd2fac090b45f0a834e93509f99ca0bb`.
- V06/V08 PDF (último cambio productivo): `8736b14b077f50dc753a0b549e81982639e3fbe5`.
- D5 aislado y pruebas: `460e15d7d1d76bf3802ae02a8ef7698f05354091`.

La galería/documentación posterior no incorpora cambios productivos. Transporte
Git devolvió503 en el primer push PDF; tras comprobar remoto8da2b45, un segundo
push normal publicó los mismos objetos. Sin force, recreación ni cambios a main.
