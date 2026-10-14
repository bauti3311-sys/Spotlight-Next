# Actualización compatible sin perder la instalación

La entrega debug38562918546 del código58ac0a2 usa una clave debug del runner. El certificado histórico esperado es `11b0b3b37544bd95b128de52798fa7ffb7489f94819614b1ada383bd4ee7a6cf`. No basta con que coincidan paquete y versión: Android exige firma compatible para actualizar y preservar UID/datos.

Evidencia disponible: release histórica2.0.0/código27 firmada y verificada en CI38264201437 (12/10), artefacto11753988125 todavía disponible. El flujo main consumió correctamente los cuatro Secrets históricos; eso prueba disponibilidad entonces, no acceso actual hasta ejecutar firma. Clave/credenciales no existen en este executor; no se leen ni trasladan aquí.

Flujo privado autorizado para este caso: `.github/workflows/release-apk.yml` **solo en rama fix/historical-update-58ac0a2**, guardas de repositorio/usuario/rama/evento; nunca PR/fork/ref de entrada arbitraria. Main y su workflow original permanecen intactos. Sin modificar Secrets, permisos de administración, branch protection, pagos o ambientes nuevos.

Antes de usar Secrets: consulta CI58 aprobada y entrega619 correcta; descarga dos ZIPs inmutables por ID y exige SHA256 conocidos. Comprueba firmas/manifiestos de histórica y debug reales: mismo `dev.bautista.spotlight.next`, histórica27 y candidata28/2.0.1, min26/ARM64/debug y SOURCE_COMMIT58. Valida checksum del APK original. No ejecuta código del APK ni compila.

Firma con los cuatro Secrets existentes en un directorio temporal077, sin tracing/logs de alias o passwords, borrado con trap; nada privado se sube o cachea. Apksigner verifica resultado; validador original del certificado exige11b0, sin rebajar sus assertions ni permitir clave alternativa. Compara cada archivo de payload de ZIP antes/después, exceptuando únicamente metadatos de firma v1; código y recursos deben ser idénticos. SHA256 real de APK firmada, tamaño y comparación públicos se imprimen y acompañan al artefacto privado7días. Sigue siendo APK de prueba debug, no release oficial.

La comparación es contra APK histórica publicada, no lectura física del certificado instalado en la tablet. Si coincide con esa histórica, las condiciones estáticas de actualización se cumplen: mismo paquete/certificado y código28>27. Android debe ofrecer actualización conservando almacenamiento de la app; OneUI físico/F1.3 foco frío siguen pendientes. No desinstalar, borrar datos, cambiar packageId ni forzar downgrade.

Si la firma falla por inputs ausentes/incorrectos o certificado distinto, no se entrega APK y no se genera clave. Mantener la app anterior con sus datos; recuperar acceso a la clave original en GitHub Secrets sin pegar claves al chat. Si el certificado realmente instalado fuera otro, conservar la instalación y aportar únicamente la APK antigua pública para identificar su firma y la clave correcta. Una copia del certificado público por sí sola no permite firmar.

Resultado final/run/enlace/digests y propietario/cierre en notas de continuidad. No PR nuevo ni commits posteriores que reinicien el job. Fuente funcional58 y checkpoint integral e38 conservados; no nuevas features.

## Resultado 14/10/2026

Firma histórica actual disponible: CI38575553197 correcta. Debug anteriorb609… y publicada histórica11b0… eran diferentes; nueva privada11b0… coincide con histórica. Paqueteigual,código27→28, payloadinmutable verificado.

Descarga: https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/38575553197/artifacts/11900151748 . Iniciar sesiónGitHub, extraerZIP y abrir `Spotlight-Next-update-58ac0a2.apk` en Misarchivos; Android debe ofrecer Actualizar si instalada corresponde a histórica comparada. No desinstalar ni borrar datos.

APK177819655bytes, SHA256 `45a21f56f607e8c9efcd38d8d27ed6e7e7f67883867584fc090a71fef2f12e6e`. Certificado `11b0b3b37544bd95b128de52798fa7ffb7489f94819614b1ada383bd4ee7a6cf`. ZIPenlace SHA256 `3f2c808c0f78618f48f49e3595700ae866e06e6dade9181c9a25a8d718a5f3c5`; diferencias de nivel (APKextraída/ZIPdescarga). Disponiblehasta21octubre.

No instalacióntablet realizada ni certificadode suinstalaciónleído. Si siguiera conflicto, preservarapp y aportarAPKantigua pública para comparación de firma/código; nunca claves.
