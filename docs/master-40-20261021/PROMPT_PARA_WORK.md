# INSTRUCCIÓN PARA CHATGPT WORK — SPOTLIGHT NEXT (40 DECISIONES CERRADAS)

**Adjunto `Spotlight-Next-Maestro-40-Decisiones-Work.zip`.** Contiene el documento `ESPECIFICACION_MAESTRA_40_DECISIONES.md` con las 40 decisiones aprobadas, matriz CSV de trazabilidad, cinco checkpoints verificables y **cuatro imágenes de referencia visual definitivas**. Leé el maestro completo; no sustituirlo por este resumen.

**Objetivo:** implementar y cerrar la experiencia Raycast Dark Glass de la aplicación Android nativa Spotlight Next, Kotlin + Jetpack Compose, sin nuevo rediseño conceptual ni controles ficticios.

**Primero, antes de modificar código:**

1. Inspeccioná repositorio `bauti3311-sys/Spotlight-Next`, rama y HEAD actuales (local/remoto), PRs/checkpoints, `AGENTS.md`, `PLANS.md` y flujo de reserva autónoma `python3 scripts/autonomy_lock.py status`. Continuá la rama/trabajo más recientes sin sobrescribir avances, sin merge a `main`, sin cambiar applicationId ni firma histórica.
2. Tomá el ZIP de fuente Preview 34 / commit `cc1c28d` únicamente como **baseline histórico** si está adjunto/accesible. El código actual puede estar más adelantado; verificarlo.
3. Hacé inventario verificable de D01–D40: `implementada y probada / parcial / pendiente / bloqueada`, con archivos/tests concretos. Señalá discrepancias y conflictos del repo y preservá migraciones.
4. Leé las cuatro imágenes, **no como pantallas de la app real**. `03-ActionPanel-Glass-APROBADO.png` y `04-Ajustes-Glass-APROBADO.png` definen el Glass; `01/02` establecen composición. Las decisiones prevalecen sobre textos y elementos ficticios de los renders.

**Implementá por checkpoints C1–C5** según el maestro. Prioridad inmediata: C1 buscador y resultados nativos, selección gris, tres sugerencias, 5–6 resultados, opacidad 84/92/94, anchura/altura adaptativas, tocar para seleccionar / confirmar y ranking. Después C2 panel e inspector, C3 Ajustes, C4 integraciones/IA/plan/archivos, C5 certificación. Verificá antes de declarar terminado cualquier submódulo.

**Reglas obligatorias:** una sola interfaz Raycast Dark Glass; ActionPanel superpuesto; inspector contextual único para Quick Look, Spotify multimedia, IA y formularios, con opción de fijar y sin columna vacía. Material real Compose, sin WebView ni imágenes falsas incrustadas. Respeto de límites Android, APIs/MediaSession/Intents, permisos reales y confirmaciones. No mostrar acciones que no se puedan ejecutar. No IA remota por teclear, no monitoreo constante, no scripts arbitrarios. Diseño inicial: Inter 14 sp, filas 44 dp, selección gris, opacidad 84/92/94 %, centro superior adaptativo y microanimaciones.

**Rendimiento y pruebas:** foco inmediato; Android tablet/teléfono; IME, scroll PDF, acciones, permisos, fallos, backups, persistencia y privacidad. Reutilizá tests y skills del proyecto; no debilites aserciones. Usá compilación/pruebas locales y GitHub Actions solo cuando haga falta; preservá la cuota gratuita.

**Checkpoint = evidencia, no solo progreso narrado:** registrar SHA, pruebas reales ejecutadas, diferencias con las imágenes, problemas pendientes, capturas Android reales identificando emulador vs equipo físico, APK Preview actualizable firmada con clave histórica si está disponible y ZIP fuente idéntico SHA + hashes. Si el entorno no dispone de SDK/keystore o no puede construir, informá el bloqueo exacto sin inventar artefactos ni afirmar ejecución.

Trabajá autónomamente dentro de las 40 decisiones; pedí aprobación únicamente si hay un cambio significativo fuera del alcance o nuevo permiso sensible. **Empezá ahora con inventario C0 y la implementación C1, no con nuevas propuestas de mockups.**
