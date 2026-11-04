# C1 — Refinado + Adaptive, implementación acotada

Base faf84f02c1dad23673d46d06e089432242fd2a92, PR21. Autorización expresa del
propietario para C1 únicamente. C0 estático preparado; C0 físico pendiente.
El plan maestro original se conserva sin alterar en el archivo adjunto versionado.

## Tres límites reversibles

1. Estructural: extraer presentación de lista y etiquetas primarias del panel raíz.
   Estado/scope/generaciones/selección y efectos permanecen en su propietario.
   Conservar keys, LazyListState, saveable drafts y NativeLaunchSession.
2. Base visual: jerarquía y selección satinada/foco sutil, superficies/bordes/sombra
   sobrios. Mismas alturas de barra/filas/densidad, hasta3 sugerencias, teclado
   intacto; sin acento de app en la barra ni dock móvil pesado.
3. Inspectores: acento local, agrupación y controles discretos en superficies que
   YA existen. Menos duplicación móvil, sin ocultar funciones irreversiblemente.
   Etiquetas WhatsApp/WhatsApp Business completas, sin selector de cambio de canal.

## Exclusiones

No ranking contextual nuevo, integración vertical, selector de destinatario/canal
nuevo, cambios de SDK Spotify, Cast, AppFunctions, citas estructuradas, IA nueva,
RAGFlow/OpenUI/WebView, permisos o motores nuevos. Las láminas C3/C4/C5 representan
dirección visual futura, no funciones a ejecutar en C1. No APK/Actions/firmas,
cuentas reales o envíos. Package/versionCode/certificado permanecen intactos.

## Evidencia y aceptación

Capturas de producción Compose antes/refactor/después, mismos fixtures/API33,
360/960dp, oscuro/claro, consulta/selección y escala equivalentes. Los paquetes
son ficticios, sin cuentas. No son capturas físicas ni verificación de IME real.
Antes/refactor debe conservar píxeles y comportamiento; después debe conservar
altura de filas y límites de ventana/scroll. Pruebas dirigidas: presentación,
búsqueda, dock/inspector, teclado, menús ampliados/RTL, acciones y cancelación.
No debilitar pruebas o actualizar baselines para ocultar regresiones.

Riesgos: callbacks que capturen selección obsoleta, restaurar confirmaciones,
recreación del inspector al cambiar ancho, footer fuera del viewport, texto
ampliado y controles que pierdan acceso. Conservar identidad y lifecycle; validar
por regresiones existentes y fixtures de UI. TalkBack/OneUI/rotación/200% reales,
apertura/p95/memoria y efecto de apps quedan pendientes de dispositivo.

Estado inicial: baseline/capturas en preparación. Resultados concretos y SHAs al
cerrar la implementación; no se afirma aprobado por tener sólo un render.
