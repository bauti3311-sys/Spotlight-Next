# Continuidad integral C2–C5 · bloqueo real · 22/10/2026

Rama feat/raycast-sidebar-permissions-20261020; PR18 conservado borrador. Propietario work-master-recovery-20261022-cc97a1bd. Último checkpoint publicado 38613534d4f0d4947f5c13cff734c402afcf744b. Política vigente: continuar C2→C5 sin Previews intermedias, entregar una candidata integral solo al verificarla. No main/force/Actions/nuevas claves.

## Implementado y publicado
Registro Compose durable del overlay mediante ContextStore/AtomicFile existente y codec tipado compatible con MutableState; fence antes de efectos externos, sin replay al restaurar. Corrupción preservada y descarte explícito; privacidad excluye recuperación. Identidad y revalidación de documentos/apps/notas/selección, no recursos vivos/grants/Intent/bitmaps. IA parcial por contexto y destino, protección de respuestas tardías y revisión al cambiar destino. Alias{args} y ranking exacto protegido también en CompactResults. Herramienta control_app_media tipada dirigida exclusivamente al paquete autorizado, sin fallback; search_apps entrega paquete real. Prioridades de ranking exportables/validadas y consentimiento IA invalidado por cambio de proveedor.

## Pruebas reales
No sumar rondas como casos únicos:
- /tmp/spotlight-integral-functional.log:23casos,22correctos,1fallido PortableSafFlowTest. Log/XML publicados en docs/verification/integral-c2-c5-20261022/functional-first.
- /tmp/spotlight-integral-saf-consent.log, sesión42337 finalizó exit1:7casos,6correctos,1fallido. AiDestinationConsentTest2 ySettingsPersistenceTest4 correctos; PortableSafFlowTest timeout esperando bytes de exportación. Registrar suppliers de ShadowContentResolver no resolvió por sí solo el fallo. No atribuir definitivamente la causa ni rebajar aserciones.
- ÚLTIMA /tmp/spotlight-integral-saf-second.log, sesión73496:solo PortableSafFlowTest; SIN RESULTADO FINAL COMPROBADO. No reiniciar antes de recuperar proceso/log/XML.
- NO lint/assemble/R8/APK/capturas nuevas completados. No física ni servicios externos reales comprobados.

## Bloqueo exacto
exec_command no pudo crear proceso:
Rejected("Failed to create unified exec process: exec-server transport disconnected; failed to resume exec-server session: recovery timed out after 25s").
Se detuvieron los intentos de terminal. No se conoce si Gradle sobrevivió; no prometer trabajo en segundo plano. GitHub API seguía operativa; este handoff y STATUS se publican mediante comprobación exacta de HEAD/reserva y actualización CAS fast-forward.

## Cambios locales posteriores a3861353: PRESERVAR
/workspace/scratch/cd7b8fe1285c/Spotlight-Next:
1. PortableSafFlowTest.kt usa activityResultRegistry.dispatchResult(requestCode,...) con assert de despacho y dump semántico ante timeout. Ronda aún no comprobada.
2. Preferences.kt refuerza update(Config): consentimiento heredado se invalida también al cambiar proveedor/endpoint. updateAi/import ya estaban publicados.
3. OverlaySnapshotCodec.kt: validación estricta de entero/rango también para tag si; aún no verificada.
4. Nuevo IntegralCandidateCaptureTest.kt:3casos API33/NATIVE de home, spot/acciones/filtro tablet y acciones teléfono, Compose real/apps fixture. NO ejecutados.
5. docs/verification/integral-c2-c5-20261022/saf-consent-first con log/XML de7casos, todavía sin publicar.
Este nuevo commit documental remoto NO está sincronizado localmente; no reset/clean. Sincronizar preservando estos cambios, luego publicarlos con evidencia parcial antes de verificaciones largas.

## Runtime y firma
JDK17.0.20.1 completo, Gradle8.10.2, SDK35/buildtools35 recuperados sin actualizar versiones. Runner /tmp/spotlight-tools/run.py; JAVA_HOME /tmp/spotlight-tools/amazon-corretto-17.0.20.12.1-linux-x64; cache /tmp/spotlight-gradle-cache. No subir herramientas/cache/secretos.
Histórica Raycast Preview recuperada byte a byte en /root/.android/debug.keystore desde private-signing-recovered/historical-debug.keystore, keytool verificó SHA256885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba. No nueva clave/APK firmada.

## Reanudación segura
1. Terminal una vez; si falla, detener intentos. Verificar HEAD/reserva/propietario y ausencia de otra ejecución. Recuperar sesión73496/logs y preservar cambios antes de sincronizar.
2. Diagnosticar SAF usando resultado/dump sin repetir suites correctas por comodidad. Publicar trabajo local parcial. Revisar consentimiento por updateAi/import/update y borrado privado.
3. Consolidar C4 capacidades/alias/ranking/planes, luego C5 afectadas y pasada integral al congelar código.
4. Candidata Raycast Preview40: versionCode39→40, no debuggable, minify/shrinkResources y reglas R8 existentes. AÚN NO IMPLEMENTADO. Mantener certificado885..., principal/Review intactos.
5. Lint/assemble/R8 reales; capturas tablet/teléfono con procedencia; comparar4renders maestros; actualizar D01–D40 según evidencia real.
6. Solo gates correctos: APK+git archive mismo SHA, package/version/certificado/zipalign/hashes verificados, entrega conjunta. C2–C5 siguen parciales; OneUI/Moto, Spotify/Gemini reales, IME/InputDispatcher, muerte abrupta y rendimiento físico pendientes. Nunca declarar C5 completo sin física.
