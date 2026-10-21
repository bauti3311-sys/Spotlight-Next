# Obsidian Glass — revisión de producto y geometría

Base: afc682c / APK a2ef090. PR17 y PR18 abiertos, sin merges. Reserva propia de Work comprobada. Esta revisión implementa primero P1 y un incremento de P2; no acredita las40decisiones completas.

## Diagnóstico y decisiones

- El inspector se habilita a600dp interiores, pero62/38 deja223dp para detalles y365dp para resultados. El divisor admite35/65 sin mínimos: puede degradar ambas columnas. Nueva política por espacio medido:400dp resultados +280dp inspector +12dp divisor; con fuentes grandes aumentan los mínimos, con altura útil menor240dp se usa una columna. Son presupuestos iniciales comprobables, no detección de tablet.
- ResultRow usa560dp para pasar a formato de teléfono; una columna de400–500dp termina con filas más altas que la estimación externa. Se alineará el formato horizontal a400dp y una superficie mínima48dp, con una sola fuente de altura para lista e inspector.
- El inspector repite subtítulo, paquete y acciones como enlaces sin jerarquía. Extraer presentación de app con identidad, botón principal, secundarias y metadatos locales; reutilizar callbacks existentes. No inventar desarrollador/tamaño ni sesiones.
- ContextAction infiere grupo desde palabras españolas y usa título como identidad. Migración compatible a grupo e ID explícitos; el ejecutor y sus confirmaciones siguen siendo la autoridad. No añadir un segundo modelo de ejecución ni campos decorativos de disponibilidad sin proveedor real.
- Las preferencias manuales de ancho no deben sobrescribirse para forzar dos columnas. El usuario conserva su slider; una ventana estrecha ofrece detalles bajo demanda. Sin controles adicionales de dp. No animar la geometría por cada cambio de inspector.
- Ajustes ya fue renovado: conservar navegación/centro de permisos. Este primer incremento no reescribe Ajustes ni añade dependencias, capacidades o permisos.

## Evidencia y límites

La imagen mencionada no llegó en este adjunto: solo Texto pegado(9).txt. Se puede comparar antes/después nativo, pero no certificar fidelidad a esa referencia. La clave Raycast recuperada coincide con885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba; conservar paquete y aumentar solo versionCode de la variante. Backup privado separado, nunca Git/ZIPfuente. Persistencia externa falló403 en entrega anterior: no asumir respaldo remoto.

Baseline Compose en preparación desdeafc682c. No hay dispositivo/KVM ni mediciones físicas de jank, memoria o batería. Se conservarán tests y capturas antes/después; IME geométrico simulado no equivale a teclado OneUI real. Políticas/pruebas nuevas todavía SIN EJECUTAR.

Fuentes: Android Window Size Classes (developer.android.com/develop/ui/compose/layouts/adaptive/use-window-size-classes); Raycast ActionPanel y Detail (developers.raycast.com/api-reference/user-interface/action-panel y /detail). Se adaptan presupuesto de viewport, secciones semánticas y metadatos opcionales; sin componentes web.

## Checkpoint implementado, gate pendiente
Baseline afc682c:7/7MinimalistSearchFlowTest, BUILD SUCCESSFUL6m11s, cuatro capturas antes preservadas. Implementados: política de panes y divisor con mínimos; ResultRow horizontal desde400dp y alto48dp compartido; ApplicationInspector extraído con acción principal, favorita, medios compatibles y metadatos; ContextAction con ID/grupo explícitos en todos los productores del panel; colores Obsidian y sombra refinados sin dependencias. Constructor legado conserva compatibilidad; disponibilidad/riesgo/confirmación continúan en ToolRegistry/Executor y no se declaran unificados.

Nueve escenarios/políticas de test nuevos cubren selección, cambio de inspector sin lanzamiento, límites del divisor, teléfono, presupuesto IME simulado, favoritos reales del proveedor fixture, tema claro y centro de permisos. No equivalen a pruebas físicas. Preview versionCode30, mismo paquete y clave885838aa. Cambios preparados antes de gate, todavía no verificados.

Primera ronda f10cf15:101casos,99correctos/2fallidos. En692dp, redondeo Float invierte los límites del divisor: se normaliza el extremo máximo sin reducir mínimos. Altura compacta: se retira «Mejor coincidencia», redundante en una búsqueda con solo tres resultados; se ajusta presupuesto al contenido real manteniendo48dp táctiles y aserción<340dp. Se verifica también la tercera fila. Inspector de inicio tiene mínimo128dp para identidad+botón, sin altura fija grande. Botón principal10dp de radio, coherente con controles del panel. Verificación pendiente.

Tamaño medido APK a2ef090:257.978.614bytes. Entradas lib comprimidas:ARM64 58.899.760,ARMv7 40.006.288,x86 65.553.572,x86_64 65.740.680bytes. Preview se restringe aARM64 para los dos dispositivos objetivo; se conservan todas las bibliotecas/funciones para esa arquitectura. No cambia compatibilidad/firma de principal o Review. La reducción final se medirá después de assemble; no es una medición de RAM/batería.
