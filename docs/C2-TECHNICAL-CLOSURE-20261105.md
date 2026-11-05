# C2 — cierre de la validación técnica local

Fuente comprobada:84ab6d7aac3a27db30b718f2921dd49a4f3a1b82, PR21 borrador,
rama feat/preview57-native-return-20261103. Esta sesión no implementa otra vez
F07/F14 ni altera la app. La autorización se limita al gate pendiente y su cierre.

## Recuperación y alcance

Se comprobó remoto84ab6d7 y reserva libre. La nota administrativa8354515 sobre
la reserva anterior documenta su liberación autorizada y deja pendiente el verde
de restauración/Lint. Se adquirió propietario nuevo
codex-8fd7ed91-9245-40e1-9142-8ac8b916087e, lease inicial
34f30d8a69ff140a786eb6d1ae0e2387a3fb98c2. Nunca se reutilizó el propietario ajeno.

Se conservan el rojo previo selection-restore-red y todos los informes anteriores.
El cambio84ab6d7 conserva la identidad del contexto importado durante restauración:
no reimporta2 archivos sobre una selección editada de3. La identidad no concede
permisos ni restaura confirmación. El gate posterior pasó sin correcciones nuevas.

## Pruebas dirigidas

8 casos distintos correctos,0 fallos/errores/omisiones, en dos rondas; no una suite
global ni8 funcionalidades nuevas. No se suman a los112 históricos.

| Ronda | Casos | Comprobación |
|---|---:|---|
| Restauración pendiente |2| Selección entrante editada conserva3 archivos tras restaurar; retirar contexto intacto limpia selección sin solicitar Android |
| Regresión búsqueda |2| Consulta de compartir produce archivos locales sin IA/apertura; app exacta prevalece sobre consulta documental |
| Regresión resultados |1| Propuesta explícita de selección visible sin adelantar app exacta |
| Regresión recorrido UI |3| Consulta→revisión→confirmación produce un único chooser/SEND PDF con URI original y READ; restauración exige revisar otra vez; cambiar selección invalida propuesta |

Se ejecutaron únicamente esos métodos de C2FileShareFlowTest, C2FileContextTest
y CompactResultsTest. No se repitieron las suites completas de C1/C2, la matriz
de10 casos ya aprobada ni los contratos de51 casos. Los fixtures de proveedor/PM
son controlados: no prueban receptores reales ni vida OEM de grants.

## Entorno y evidencia

Lint RaycastPreview correcto:0 errores,27 advertencias y1 información. Frente al
reporte de f17d9b2 hay una advertencia adicional ComposableNaming en el helper
de pruebas `C2FileShareFlowTest.panel` (minúscula); no cambia ejecución y no se
oculta/suprime. Las otras26 advertencias y la información conservan identidad,
mensaje y archivo. No se amplía este cierre a limpieza de estilo/dependencias.

Gradle8.10.2 con checksum del wrapper, JDK17 completo, SDK35/build-tools35.0.0;
Robolectric API33 NATIVE para Compose. No se añadieron dependencias de aplicación.
El entorno nuevo carecía de SDK/caché y tenía sólo runtime Java21: el primer
intento terminó antes de pruebas por JAVA_COMPILER ausente. Se conserva su log;
preparar JDK17 resolvió ese bloqueo. No se presenta como fallo de producción.

[Logs, XML, comandos y hashes](verification/c2-final-20261104/README.md).
La carpeta conserva la fecha de inicio04/11; las pruebas correctas cerraron05/11.

## Estado de C2 y siguiente paso

Este cierre cubre la validación técnica local solicitada de la fuente84ab6d7.
**C2 técnicamente cerrado en este alcance local:** restauración verde, regresiones
dirigidas verdes y Lint sin errores; no se necesitaron cambios de código.
La aceptación física y los efectos externos continúan abiertos. No se promueve
checkpoint/autonomous-next ni se declara C0/C1/C2 físico aprobado.

El [checklist Galaxy Tab A9+ y moto g86](C2-PHYSICAL-ACCEPTANCE-20261105.md)
separa la ventana/IME de Preview62 y los recorridos posteriores de C2 en una
futura APK expresamente autorizada. Incluye capacidades por versión instalada,
adjuntos simples/múltiples, permisos vencidos, restauración, cancelación/foco,
OneUI/Gboard, accesibilidad y rendimiento. No hay APK posterior a62 entregada.

Sin cambios en main, C3–C9, APK/Actions, permisos sensibles, motores, SDK de la
app, dependencias, servicios externos, claves, firmas o presupuestoUS$0. No se
autoriza avanzar a C3 por cerrar pruebas JVM/Lint. Únicamente la reserva propia
se libera con nota de cierre atómica después de publicar el checkpoint.
