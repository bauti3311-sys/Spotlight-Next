# Apertura fría Android 16: diagnóstico pendiente

Fuente: `fix/economical-ci-android16`, base `1f52f67`. No se ha resuelto el fallo ni cerrado F1.3.

## Evidencia disponible y límite de acceso

[Actions 38499316489](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/38499316489) probó6af16ef: Maestro frío falla y las cuatro instrumentadas posteriores pasan. El ZIP privado [11864974587](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/38499316489/artifacts/11864974587) existe; el conector entrega un enlace File Service, pero descargarlo al executor devuelve HTTP403. No se usó download_file con ese identificador porque solo admite Sediment. No se inspeccionaron imágenes/XML/TXT del ZIP; hace falta adjuntarlo directamente al hilo o habilitar su descarga por el mecanismo soportado. No eludir el proxy.

Las doce evidencias exportadas por OverlayGeometryDeviceTest son seis pares PNG/TXT: portrait-auto, portrait-manual, landscape-manual, landscape-auto, landscape-ime-hidden, landscape-ime-restored. Los TXT registran API, root, visible, ime y queryFocused tras comprobar las aserciones. **No son una traza de primer buffer ni dumps de WindowManager/InputDispatcher.** La prueba se ejecutó después de Maestro, con ActivityScenario y configuración propia; no permite concluir que la apertura fría esté corregida.

## Secuencia contrastada

| Apertura fría de Maestro | Prueba nativa posterior |
| --- | --- |
| Driver preparado en launcher antes de invocar | Proceso ya utilizado, ActivityScenario MainActivity |
| InvokeActivity inicia servicio y finaliza inmediatamente | Servicio abierto desde contexto de instrumentación |
| Query2+2 e invocation keyboard enviados por intent | Query2+2 e invocation touch; autoKeyboard always |
| Preferencias persistidas del destino | Anchos automáticos, posición0.35, sin archivos/animaciones/clipboard/contexto automático |
| Resultado4 esperado antes de tap/inputText | Espera editor, IME estable, foco y geometría; seis capturas |

No es una comparación controlada frío/caliente: también cambian lanzador, preferencias e invocación.

En el log accesible, OnDraw02:15:49.354 observa windowFocus=false/editorFocus=false; watchdog02:15:54.668 sigue callbackSeen=false/actualFocus=false y cierra la ventana. Maestro falla después en la visibilidad de4. Esto demuestra ausencia real de foco observada por la app; no basta con atribuirlo a un selector o una espera insuficiente de Maestro. Tampoco prueba por sí solo si la causa última es renderizado frío, una carrera de handoff o interacción de UiAutomation.

## Hallazgos estáticos

- OverlayService adjunta el host, solicita insets y agenda el watchdog existente2500ms. Compose pide foco/teclado después de withFrameNanos; ese límite de frame no garantiza que SurfaceFlinger haya presentado un buffer o que InputDispatcher entregue foco. No se cambiaron plazos ni aserciones.
- El mensaje `First draw` usa OnDrawListener. [AOSP Android16 ViewTreeObserver](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-16.0.0_r1/core/java/android/view/ViewTreeObserver.java) lo define como vista **a punto de dibujarse**. No confirma buffer enviado/presentado. FrameCommitCallback puede observar envío de buffer en renderizado acelerado, pero tampoco prueba presentación en pantalla; debe correlacionarse con SurfaceFlinger/input.
- [AOSP Android16 WindowManagerGlobal](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-16.0.0_r1/core/java/android/view/WindowManagerGlobal.java) añade FLAG_HARDWARE_ACCELERATED a ventanas sin padre cuando ApplicationInfo lo habilita. Su ausencia literal en params() no demuestra renderizado software ni justifica añadirlo como supuesto arreglo.
- En esta corrida SPOTLIGHT_CAPTURE_TRACE=false. El script no recolectó dumps periódicos WindowManager/InputDispatcher ni Perfetto. Los hallazgos de superficie/input y shaders en38488258740 son de otra corrida; no presentarlos como datos nuevos de38499316489.
- Robolectric cubre propietarios/adjunto/foco transitorio/touch/insets; no reproduce el handoff real de primer buffer, WMS, InputDispatcher o GPU del emulador.

## Pruebas y próxima acción

Local: testDebugUnitTest filtrado OverlayStartupTest y OverlayViewportTest, **15/15**, cero errores/fallos/omisiones,31s; Python18scripts+5backend correctos. Sin cambios Android/UI/baselines/dependencias. No nueva instrumentación, matriz legacy ni consumo de Actions en este diagnóstico.

Primero recuperar el ZIP y examinar jerarquía/captura/log frío junto a los seis pares nativos. Si la evidencia sigue sin separar buffer/foco, preparar una hipótesis discriminante: correlación monotónica de attach → OnDraw → commit → foco/IME → watchdog y WMS/InputDispatcher, comparando dos invocaciones con idénticas preferencias. Ejecutar como máximo una validación focalizada tras definir qué resultado distinguirá la causa. No extender watchdog, cambiar aserciones o culpar a Maestro sin demostrarlo. Checkpoint integral e38eae8 intacto; One UI físico separado.
