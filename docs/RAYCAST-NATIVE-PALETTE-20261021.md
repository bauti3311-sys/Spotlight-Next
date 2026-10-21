# Raycast Native Palette — Preview32 verificada

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

## Recuperación real21/10
HEAD08d74b5 local/remoto y reserva6135117d del propietario de esta continuidad confirmados. Árbol limpio; no hay trabajo posterior local. Terminal/Java17/Git funcionales; SDK/Gradle de /tmp desaparecidos y restaurados con las mismas versiones. git fetch origin falló por remoto inexistente y alternates desaparecido; se conservó copia de metadata y se rehidrataron891blobs actuales desde archivos con hash igual al índice, sin cambiar historia remota ni archivos. Publicación por conector con CAS sin force. Firma885838aa restaurada desde respaldo privado existente, nunca nueva clave.
Sobrevivieron XML de35pruebas (29correctas/6fallidas), ahora publicados como evidencia recuperada, no nueva ejecución: cierres40dp frente48, Enter tras flecha usa selección capturada en frame anterior, selector ambiguo ante dos campos, panel/submenú demasiado corto. Se inicia de nuevo la ronda dirigida sobre08d74b5 antes de modificar código. Roborazzi1.29.0/versiones conservadas; C1 solo JVM Test como workaround histórico, sin desactivar tests.

## Corrección guardada antes de verificar
Se resuelve la acción desde path/filtro/ID vigentes en cada evento de teclado, evitando ejecutar una selección capturada antes de Flecha/Tab. Cierre y retorno tienen48dp explícitos; el retorno de submenú comparte fila con el filtro, preservando espacio para acciones. En paletas bajas el menú puede solaparse con el footer, sin cambiar el tamaño de la ventana. El test de búsqueda distingue el editor principal del filtro independiente y conserva todas sus aserciones.
La ronda inicial de35pruebas sigue ejecutándose sobre una copia aislada de08d74b5 (caché restaurada). Este checkpoint de corrección es parcial, todavía no verificado; no hay APK nueva.

Ronda inicial nueva finalizada realmente sobre08d74b5:35pruebas,29correctas/6fallidas, coinciden exactamente con la evidencia recuperada. Compilación Kotlin/Java y tests correcta; duración32m12s por restauración completa de caché/compilación JVM. XML/log preservados en docs/verification/native-palette-20261021/resume-baseline. Sobre a2f6537 se inicia el gate de23suites/123casos esperados, lintRaycastPreview y assembleRaycastPreview. Gradle además usa C1 en esta invocación fuera del repositorio, sin cambiar versiones ni checks.

## Ronda corregida real
Sobre a2f6537:123casos/23suites,122correctos/1fallido;20m24s. Las6aserciones que fallaron originalmente ya se superan: teclado de acciones, bloqueo de acción no disponible, selector del editor y3cierres48dp. El único fallo llega después, al exigir foco en Compartir B inmediatamente después de performTextClearance del filtro. La implementación Compose1.7.6 de TextActionsKt.getNodeAndFocus solicita explícitamente RequestFocus al editor antes de reemplazar texto; desplazarlo entonces a la acción rompería la edición. Se corrige esa expectativa conservando control de foco en el editor y agregando aserciones de selección por ID, estado deshabilitado, Enter sin ejecución y navegación+Enter posterior a la acción disponible. No se elimina ninguna prueba ni control de ejecución.
Se preservan los23XML y log. Se repetirá únicamente unavailableActionCannotExecuteAndFilteringPreservesIdentity; las122correctas no se repetirán. Lint/APK no llegaron a ejecutarse porque Gradle detuvo el grafo al fallar ese caso. Código de producción sin cambios respecto a a2f6537; próximo checkpoint solo test/evidencia.

El intento dirigido sobre96b11dd se detuvo compilando el test (9m25s,19tareas de producción reutilizadas): performTextClearance devuelve Unit en Compose1.7.6, por lo que no admite encadenar assertIsFocused. Se separan operación y aserciones, conservando íntegros los controles. Ninguna prueba/lint/APK llegó a ejecutarse en ese intento. Log preservado; no es fallo de producción ni de dependencias.

Reintento sobreaa4b790:compilación correcta,1caso ejecutado, foco del filtro correcto; falla la nueva aserción assertTextEquals vacío porque el nodo fusiona Text=[Buscar acciones…] del placeholder junto con EditableText vacío. El XML prueba Focused=true y EditableText=''. Se precisa la aserción sobre SemanticsProperties.EditableText, sin retirar el control de vacío ni los de selección/seguridad.15m54s,26tareas reutilizadas; lint/APK detenidos por el test, no ejecutados todavía. Evidencia targeted-focus preservada.

## Cierre dirigido de pruebas21/10
Fuente61a63399aeef4e57bc4e285899c7b2fd2085cb26:el único caso pendiente pasó realmente,1/1sin fallos/errores/omisiones (134,395s), XML final-targeted preservado. Se consolidan123casos únicos:122correctos de la ronda sobrea2f6537 y este caso dirigido, sin repetir los122 ni afirmar una ronda completa nueva en verde. Producción idéntica a a2f6537; solo se precisaron semánticas del test. Las6regresiones originales quedan cubiertas. Continúa la misma invocación con lintRaycastPreview y assembleRaycastPreview; todavía pendientes al guardar este checkpoint de pruebas.

LintRaycastPreview completó realmente sobre la misma fuente61a6339:0errores/12advertencias/1informativa, XML preservado. Continúa el ensamblado, actualmente fusionando DEX; no se reinicia Gradle ni se modifica producción. Las advertencias se conservan visibles, sin cambios de dependencias ni supresiones. No hay APK entregada todavía al guardar.

## Entrega verificada — Preview32
APK y ZIP fuente exactos del commit 61a63399aeef4e57bc4e285899c7b2fd2085cb26, rama feat/raycast-sidebar-permissions-20261020. Compilación local assembleRaycastPreview correcta (misma invocación,1h2m58s), sin reiniciar ni recompilar producción después de a2f6537.123casos únicos verificados en rondas (122+1), lint0errores/12advertencias/1informativa. No se presenta como una nueva batería completa en verde.

- Paquete dev.bautista.spotlight.next.raycastpreview; versión 2.0.1-raycast-preview/32; soloARM64. RaycastPreview conserva su firma Android Debug independiente histórica885838aa; no usa ni sustituye la principal/Review.
- apksigner correcto; certificado completo 885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba; zipalign16KB correcto; entrada launcher declarada.
-375entradas compilables contrastadas byte a byte con el commit y la copia compilada; ZIP Git íntegro del mismo SHA, con todas esas entradas y sin claves/credenciales.
-APK Spotlight-Next-Raycast-Preview-61a6339.apk, 86506916bytes, SHA256 c21f93cf9499a45b7463a28be6338b115e24aa07d017310a50a29ba43647f27a.
-ZIP Spotlight-Next-source-61a6339.zip, 7931546bytes, SHA256 7ff53115e8917b299afe158b61c27027a4217552d5a81dbc6de193503a832ff3.

El primer intento de archivo fuente detectó un árbol Git antiguo faltante2a1b3a; se reconstruyeron936entradas en índice temporal separado, con blobs igual al índice y árbol igual a HEAD5be3ff8d. Recorrido del commit fuente y archivo ZIP comprobados después; ningún archivo/commit se sustituyó y no se recompiló. Gradle conservó sin eliminar símbolos ocho bibliotecas nativas precompiladas; empaquetado correcto y alineación verificada. Se conservan todas las advertencias en XML.

Evidencia: verification/native-palette-20261021/delivery, corrected-gate y final-targeted. Capturas nativas adjuntas en ZIP de evidencias: una columna, Actions, filtro, submenú, detalle local, presupuesto IME, teléfono y Light. Las apps de los fixtures usan icono Android genérico; producción conserva PackageManager real. No son capturas OneUI ni prueba de servicios reales.

Pendiente: instalación/actualización física OneUI, teclado/IME reales, PDF real, servicios autorizados y rendimiento medido. No hay ejecución Gradle pendiente. Próximo paso: probar esta Preview en la tablet antes de más diseño o funcionalidades. Sin CI, merge a main, force-push ni nueva clave.
