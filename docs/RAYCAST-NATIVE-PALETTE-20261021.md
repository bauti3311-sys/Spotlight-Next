# Raycast Native Palette — continuidad de Preview31

Base b0218d0b84c1628755b153b2aff6976a4aa380f0, rama feat/raycast-sidebar-permissions-20261020. HEAD local/remoto coinciden, árbol limpio antes de empezar. Reserva4eedbced78621a71814b9f73db751e9259ab0d3c: work-recovery-20261018-cd7b8fe1285c, mismo checkpoint. status por Git terminal sigue sin autenticación; se verifican referencias y propietario por conector GitHub y se publica con CAS sin force.

## Auditoría incremental
Desde a2ef090 ya hay tokens, material legible99,5%, Inter, selección por gesto correcto, IDs/grupos explícitos, inspector jerárquico y centro único de permisos. Preview31 verificó112pruebas y lint sin errores. No repetir esos arreglos. Las capturas físicas A son anteriores; no describen necesariamente Preview31.

El defecto pendiente es estructural: liveAppInspector activa dos columnas para cualquier app y fuerza184/340dp; menu reutiliza details con cabecera/metadatos y puede expandir la ventana. ActionPanel ya filtra pero reinicia selección por índice; carece de pila de submenús y búsqueda siempre accesible. El footer conserva una cápsula pesada. ResultRow contiene BoxWithConstraints por fila.

Nueva instrucción explícita del usuario sustituye el inspector automático aprobado previamente: Search de una columna; Actions flotante en el mismo Box/WindowManager; Detail solo para contenido útil. Se conservan resultado, consulta, motores, preferencias y QuickLook. Táctil: todo resultado primero selecciona; PDF además activa QuickLook; pulsación prolongada o control Acciones abre menú, Enter/pie ejecuta. Arrastre/cancelación nunca ejecutan. Tab conserva condiciones de editor aprobadas.

## Referencias observadas
- C: Screenshot_20261020_235105_Vivaldi(1).jpg, ya abierta: menú sobre lista, secciones, buscador inferior; no columna permanente.
- B: Spotlight Next en modo oscuro(4).png, ya abierta: material/jerarquía; no copiar sus acciones ficticias.
- https://developers.raycast.com/api-reference/user-interface/action-panel (consultada21/10): acciones contextuales, secciones semánticas y submenú que reemplaza elecciones dentro del mismo panel.
- https://developers.raycast.com/api-reference/user-interface/list (consultada21/10): lista/selección/filtro/indicador de carga; conservar SearchEngine propio.

## Bloques
1. Política visual y Search sin inspector automático; footer único y acciones fuera del flujo de medición.
2. Reutilizar ContextAction con hijos/estado/riesgo de presentación, sin otro registry; filtro independiente, identidad estable, pila y Escape. Copiar con formatos reales y comunicación mediante formularios existentes.
3. Capturas y pruebas de foco/retorno/scroll/IME presupuestado/teléfono/Detail y regresiones existentes. Actualizar expectativas del inspector automático porque cambió expresamente el contrato, sin eliminar pruebas ni controles de ejecución.
4. Lint y Preview32 independiente, firma885838aa conservada, APK/ZIP exactos y evidencia. Sin CI/main/permisos/dependencias nuevos.

Estado: plan de implementación; ninguna prueba nueva ni APK ejecutada de este bloque todavía. IME real/OneUI/servicios externos y rendimiento físico requieren dispositivo. No proponer blur adicional ni copiar recursos externos.

Checkpoint26b356b guardó implementación sin verificar. Primera compilación detectó3referencias a maxWidth ocultas por receptores anidados; se capturan constraints antes de Row/LazyColumn. Se retira la cuadrícula de la presentación explícita Apps para conservar el mismo contrato de lista/flechas; preferencias/datos/CommandCenter no se eliminan. Se adaptan pruebas del inspector automático a selección de una columna, manteniendo altura/gestos/no ejecución/retorno y pruebas del divisor en Detail. El hint antiguo del menú se sustituye por Enter·Ejecutar del footer nuevo.10casos nuevos preparados; aún no ejecutados.

Compilación de producción posterior correcta; el primer intento de suites se detuvo compilando un test nuevo por assertHeightIsAtMost no disponible en esta versión de Compose. Se sustituye por aserción numérica equivalente con densidad real; ninguna suite llegó a ejecutarse en esa invocación. Se agrega un11ºcaso de detalle de nota creada mediante LocalStore real y vista previa explícita, sin sustituirlo por validación físicaPDF.
