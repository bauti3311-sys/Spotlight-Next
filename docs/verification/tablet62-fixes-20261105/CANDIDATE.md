# Candidata63 posterior a correcciones — pendiente de autorización

Fuente/base funcional ef4e60fcd94c33279581dc247461f227274e66b1, versionCode63.
El commit de tooling que fija preview_delivery.json y este documento es el SHA
workflow nuevo, publicado exacto en PR21 y notas. Par inicial71d6980/a82af60
conservado y PAUSADO; no usar para build. No hay APK63 ni firma comprobada aún.

Source y functional_base iguales, allowed_delta_paths vacío: se entrega exactamente
la fuente validada, sin cambios de código posteriores al gate. Workflow/guardas/
firmador reutilizados sin modificaciones. Requisitos de paquete/certificado,
versiones previa62/nueva63, R8/SDK/ABI y presupuestoUS$0 permanecen intactos.

13 contratos offline de configuración de entrega correctos (candidate-contracts.log).
No se ejecuta signer/preflight ni Gradle adicional para preparar el manifiesto.
candidate-review.json comprueba hashes de producción/evidencia, compare idéntico
y ausencia de delta en app desde la fuente. Esto no comprueba APK/firma/instalación.

Antes de ejecutar: autorización expresa del nuevo par fuente/workflow, HEAD remoto
igual al autorizado, reserva nueva propia checkpoint=workflow y primer intento
comprobado por las guardas. confirm_preview=AUTHORIZE_PREVIEW_63_ONCE. Un cambio de
HEAD o par obliga a revisar antes de ejecutar; un fallo no autoriza otro intento.
PresupuestoUS$0/bloqueo conservados sin reconfirmaciones manuales recurrentes.

Checklist físico priorizado en docs/TABLET62-REGRESSIONS-20261105.md. C1/C2
conservados, C3 sólo después de revisión física. No main/merge/APK/Actions ahora.
