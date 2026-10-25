# Fuentes y límites — 2026-10-25

Repositorio oficial: https://github.com/android/skills/tree/4ebabf2b0fd6c04b7150023c8b612f80592a6226
Licencia: Apache-2.0, autor Google LLC. Copia: [LICENSE](LICENSE).
Revisados: security/android-intent-security/SKILL.md y device-ai/appfunctions/SKILL.md.

Cambios locales: instrucciones condensadas y adaptadas a minSdk26/compileSdk35/target35. No se incorporan ejemplos, dependencias ni comandos ADB upstream. Algunos ejemplos de seguridad asumen minSdk36 e incluyen un Binder local pese a recomendar verificación IPC: no copiarlos a Spotlight. Preservar los contratos y permisos existentes.

La skill AppFunctions revisada exige targetSdk36 y compileSdk37. Es una referencia condicionada, no una implementación compatible con el build actual. No instalarla sin adaptar ni presentar sus capacidades como disponibles. Exponer funciones propias tampoco habilita invocar funciones privadas de WhatsApp/Gemini u otras apps.

MediaSession solo controla sesiones autorizadas y del paquete correcto; RemoteInput solo responde acciones que la notificación publica. Web API requiere sus scopes y cuenta; enlaces públicos no reemplazan esas APIs. SAF no permite acceder a archivos no concedidos.
