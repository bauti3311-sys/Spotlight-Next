# C5.4 — decisión de instalación y aislamiento

Base b744ecf609d6ad14f7bc3bd76421cf44c37f567a; banco c2f94e8 intacto. Auditoría de sólo lectura: ADB, lista de AVD, archivos SDK/KVM/material de firma (existencia, sin leer claves), ABI del AAR ONNX ya cacheado y configuración Gradle/workflow.

Resultado: NO_GO_NO_VERIFIED_SAFE_ANDROID_TARGET. No hay dispositivo ni emulador listo; no se pueden inspeccionar los perfiles de los equipos personales. No existen APK Debug/test correspondientes ni material histórico de firma local.

- adb-devices.txt: salida real sin dispositivos.
- avd-list.txt: salida real sin AVD.
- environment-decision.json: hechos y límites; perfiles no auditados no equivalen a ausencia de instalaciones personales.
- manifest.json: integridad de evidencia y límites de actividad.

No se compiló ni firmó APK, no se instaló nada, no hubo instrumentación, Actions, descarga/aprovisionamiento ni nueva evaluación. No se revalidaron APK históricos ni se repitió el banco. C5.3 y C5.4 permanecen inalterados.

Ver ../../C5-4-SAFE-EXECUTION-DECISION-20261107.md: bloqueo concreto, alternativa física de menor riesgo, papel limitado de un AVD y preflight global/perfiles antes de una autorización posterior.
