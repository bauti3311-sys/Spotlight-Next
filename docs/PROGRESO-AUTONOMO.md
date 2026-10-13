# Progreso autónomo de Spotlight Next

## 2026-10-13 — inicio manual F1.1

- Fecha/zona: 2026-10-13, America/Argentina/Buenos_Aires.
- Rama: `feat/width-policy-f1`; base `372faa781ad2e1bf32e520bba9ed53567ebc4a34` (punta PR #7).
- Hito/subhito: F1.1, ancho general automático/manual y excepción horizontal explícita.
- Estado: EN CURSO.
- Reserva compartida: `refs/heads/locks/spotlight-autonomy`; su commit contiene JSON de propietario/estado/rama. No editar si la reserva sigue activa o no se puede demostrar que su propietario terminó. Nunca borrar una reserva ajena por antigüedad.
- Diagnóstico: Apariencia muestra automático general sin avisar el override horizontal; el control horizontal es un slider sin activación explícita; restablecer posición también borra ancho. Cambiar a manual siempre vuelve a 640 dp.
- Criterios: excepción visible en ambos ajustes; ancho y posición restablecen por separado; conservar preferencias antiguas y último manual general al alternar automático; tests de persistencia, orientación/IME, interacción y capturas teléfono/tablet. No tocar cálculo de métricas/insets todavía.
- Código base comprobado: CI `83e887a`, run https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/38337171465 correcto; los commits hasta 372faa7 solo añaden documentación.
- Checks de este subhito: aún no ejecutados; no marcado como validado.
- Impacto APK: ninguna dependencia nueva prevista; Compose nativo, misma identidad/firma.
- Main/merge/firma/release: NO.
- Siguiente acción exacta: implementar control compartido de anchos y memoria manual compatible, probar y actualizar este registro. La automatización debe consultar esta rama antes de comenzar otro F1.
