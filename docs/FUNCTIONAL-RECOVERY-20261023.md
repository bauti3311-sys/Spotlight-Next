# Recuperación funcional · 23/10/2026

Checkpoint PARCIAL, prueba pendiente de finalizar. Base d740a112470f6ccea5d7c6fe834a0cf162ece8eb. No entrega APK43 ni firma verificada.

El directorio canónico recovery/source y runtime previo ya no existen. El árbol productivo y tests de /workspace/spotlight-native-build-20261023 coincide con todos los blobs remotos de app; 1414 blobs del repositorio coinciden, 46 documentos/evidencias ausentes y 3 documentos antiguos. Se preservaron APK43, XML de 17 pruebas correctas previas, lint y mapping en functional-recovery/prior43. No se repiten esas pruebas estéticas.

Terminal real, JDK17, Gradle8.10.2 y SDK35 base r02 recuperados desde distribuciones oficiales con hashes verificados. No CI, dependencias nuevas ni cambio de firma. CLI git sin autenticación; coordinación y publicación mediante Git Data API autenticada con comparación exacta de SHA, avance descendiente y force:false. Reserva propia work-master-recovery-20261022-cc97a1bd, 6d098f5 →159b429; sin otra JVM/Gradle al comenzar.

Brecha D27: resolvePlan calcula cada ajuste relativo contra el dispositivo inicial, aunque haya pasos previos; EditablePlanPanel carece de añadir pasos. Se añade RelativePlanChainTest que espera volumen40 +subir10 =50, sin ejecutar. Comando real activo: :app:testDebugUnitTest --tests dev.bautista.spotlight.ui.RelativePlanChainTest, Gradle aislado/forkEvery1. Aún no hay resultado al publicar este checkpoint. No se declara prueba verde ni fallo reproducido hasta terminar.

Siguiente: conservar resultado rojo, corregir proyección por pasos seleccionados/orden y añadir paso validado al editor existente, pruebas focalizadas. Rehacer clasificación D01–D40 por evidencia funcional/automatizada/física; no sustituir maestro por numeración histórica ni confundir parcial con ausente. Preservar Solid Graphite y preferencias aprobadas posteriores al maestro.
