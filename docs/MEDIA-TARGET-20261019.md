# Selección de reproductor multimedia

Base: 8ae090873be22a4d9eaae7493f96488be00aad25. Checkpoint de seis pruebas SIN VERIFICAR; MediaControl todavía sin cambios.

Hipótesis: firstOrNull elige una sesión compatible antes de comprobar si otra reproduce o si el destino es ambiguo. Casos: preferencia por única sesión reproduciendo, múltiples reproduciendo/pausadas, reanudación única, comandos no soportados y cambio de sesiones después de consultar disponibilidad. Se observan las solicitudes de TransportControls mediante shadows Robolectric, no reproducción física.

Contrato propuesto: único reproductor compatible activo; si ninguno reproduce, única sesión compatible; ante ambigüedad no enviar. Reutilizar MediaControl para herramientas y planes, sin permisos, servicios o UI adicionales. No afirmar ejecución confirmada por el reproductor.

Referencias consultadas: Android MediaSessionManager y fuentes Robolectric 4.14.1 de MediaSessionManager/MediaController/TransportControls. Skill android/skills security/android-permissions-security aplicada a consulta dinámica de autorización y límites SecurityException. Decisiones 16, 20, 27 y 38, todas parciales.
