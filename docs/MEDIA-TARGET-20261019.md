# Selección de reproductor multimedia

Base: 8ae090873be22a4d9eaae7493f96488be00aad25. Checkpoint de seis pruebas SIN VERIFICAR; MediaControl todavía sin cambios.

Hipótesis: firstOrNull elige una sesión compatible antes de comprobar si otra reproduce o si el destino es ambiguo. Casos: preferencia por única sesión reproduciendo, múltiples reproduciendo/pausadas, reanudación única, comandos no soportados y cambio de sesiones después de consultar disponibilidad. Se observan las solicitudes de TransportControls mediante shadows Robolectric, no reproducción física.

Contrato propuesto: único reproductor compatible activo; si ninguno reproduce, única sesión compatible; ante ambigüedad no enviar. Reutilizar MediaControl para herramientas y planes, sin permisos, servicios o UI adicionales. No afirmar ejecución confirmada por el reproductor.

Referencias consultadas: Android MediaSessionManager y fuentes Robolectric 4.14.1 de MediaSessionManager/MediaController/TransportControls. Skill android/skills security/android-permissions-security aplicada a consulta dinámica de autorización y límites SecurityException. Decisiones 16, 20, 27 y 38, todas parciales.

Reproducción sobre a527a7c: 6 pruebas ejecutadas, 4 fallos de aserción, 0 errores/omisiones. Se confirma selección incorrecta y ambigüedad, incluidas sesiones que cambian entre consulta y ejecución. XML RED preservado. Corrección implementada: única sesión compatible reproduciendo, o única compatible si ninguna reproduce. Sin destino único, no enviar. Texto de capacidades actualizado. Corrección todavía SIN VERIFICAR.
