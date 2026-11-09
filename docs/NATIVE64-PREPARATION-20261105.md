# Preview64 — preparación autorizada; ejecución pendiente

Continuidad de `feat/preview57-native-return-20261103` / PR21 borrador.
Remoto comprobado antes de editar: `db79c84b22a102d62b870ad61ff8bdc7b0ccfac2`;
fuente E1–E5 `8d560828369e2fbd35957808cad66ae4a4a2895d` recuperada por su
historia, sin merge ni cherry-pick de experimentos. Main permanece en
`47259ce2d7259f0ddfa5d85b7c73f07983d8cd6d`.

La autorización posterior del propietario permite preparar64 conservando Focus
opt-in y su recuperación anterior; no activa el diseño globalmente ni acredita
aceptación visual/física. El manifiesto y la autorización63 son históricos.

## Identidad y alcance

- Code64 >63; nombre `2.0.1-raycast-preview-native57-v64` (modo optimizado existente).
- Paquete `dev.bautista.spotlight.next.raycastpreview`.
- Certificado requerido `885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba`.
- release.properties, app/src/main, manifiesto, dependencias, permisos, firma,
  workflow y scripts de guardas/firmado permanecen idénticos a E1–E5.
- Único delta productivo: dos líneas de numeración en app/build.gradle.kts.
  Dos regresiones nuevas, sólo test: selector persistente tras recreación y
  preferencia observada por servicio abierto. Sin cambios estéticos ni funcionales.
- Teclado, geometría, footer, inspectores, navegación, C1/C2 y Focus=false intactos.

## Verificación

Auditoría y logs en [evidencia](verification/native64-preparation-20261105/).
Se inspeccionaron44 casos XML históricos verdes y los cinco hashes del manifiesto
de revisión E1–E5; no se cuentan como pruebas Android nuevas. Lint previo0E/26W/1Info
es histórico, no una ejecución en este equipo. La cadena Apariencia→merge→DataStore→
Flow→estado Compose del OverlayService→SpotlightPanel se revisó en fuentes reales.

El equipo nuevo está running/connected y su terminal funciona. Se completó Gradle
8.10.2 por checksum del wrapper y SDK35. El runtimeJava21 inicial carecía de javac;
JDK17 aislado de Temurin con checksum verificado, sin dependencias nuevas del proyecto.
Resultados: **6 casos JVM distintos correctos** en rondas dirigidas (tres de
FocusFoundationTest; selector/recreación, opacidad/recreación y overlay abierto).
El selector persiste al desactivar/reactivar y recrear la Activity. El servicio
abierto observa true→false→true sin reiniciarse, con permiso/foco simulados y
bombeo explícito del looper Android, separado del reloj Compose. Los tres fallos
iniciales del fixture permanecen en initial/permission-fixture/focus-fixture;
no se modificó producción ni se redujeron aserciones/tiempos. Sólo se repitió el
caso del overlay. El resumen deduplica por clase+método; no son nueve casos.
Los17 checks offline de entrega y2 de identidad principal también pasaron.

## Par de entrega

Fuente: 808bda2c25887d14b42665dea4c254181d72b837. El commit posterior que fija preview_delivery.json
constituye el SHA workflow a revisar, separado de la fuente compilable/ZIP.
El manifiesto final fija base funcional=fuente congelada y delta permitido vacío.
Workflow build-apk.yml y guardas intactos; no se ejecuta preflight real.

No se generó APK, certificado, firma ni ejecución Actions. US$0/bloqueo vigentes.
No retry/fallback ni build antes de autorización del par exacto. Para el único
intento futuro, adquirir reserva propia nueva con checkpoint=workflow autorizado;
comprobar nuevamente remoto, owner/lease e historial autoritativo.

OneUI/Gboard, altura al abrir desde Ajustes, rotación/multiventana, teclado físico,
TalkBack, PDF y efectos de apps reales siguen pendientes en Tab A9+ y moto g86.
Las pruebas JVM no aceptan físicamente63 ni64. C3–C9 siguen pausados.
