# C5.4 — decisión de entorno seguro

Base remota verificada b744ecf609d6ad14f7bc3bd76421cf44c37f567a, PR21 abierto/en borrador. Banco preparado c2f94e8a5f63230c48470015b76ac9047071c29a sin cambios.

**Decisión: NO-GO para instalar o ejecutar hoy. No hay un Android seguro y verificable disponible.** Esto no declara inseguros los dispositivos del propietario; significa que no se pudieron inspeccionar y no pueden aprobarse por suposición.

## Hechos comprobados en este Work

| Requisito | Resultado |
|---|---|
| Dispositivo Android accesible por ADB | Ninguno |
| AVD configurado | Ninguno; avdmanager list avd vacío |
| Binario de emulador / imagen Android | Ausentes |
| /dev/kvm | Ausente; no hay aceleración KVM expuesta |
| APK Debug C5.4 + test APK correspondientes | No existen en app/build; no se generaron |
| Material histórico de firma local | No hay keystore ni credentials.properties en las rutas usadas por Gradle |
| Usuarios/perfiles/paquetes del Galaxy o Motorola | No inspeccionables sin conexión; su estado es DESCONOCIDO |
| Código y pruebas C5.4 / resultados C5.3 | Conservados; sin recompilar ni ejecutar |

Hay APK históricos con nombres Preview64/65 en descargas. No se revalidaron ni se consideran un par Debug/test C5.4. Preview67 tampoco contiene las clases C5 y tiene otro package. Su entrega no autoriza una compilación adicional.

El SDK base tiene archivo de licencia, pero no hay imagen/emulador instalados. No se aceptaron nuevas licencias, descargaron componentes ni configuraron máquinas. No se inspeccionaron secretos CI. La ausencia de claves locales no demuestra que falte la firma histórica en CI; sí impide afirmar que aquí exista un par firmable y autorizado.

La configuración Gradle usa projectPrivate para Debug sólo cuando hay material histórico; en su ausencia puede recurrir a la firma debug predeterminada. **No ejecutar una compilación de APK para “ver qué firma sale”: podría generar una clave no autorizada.** El native-delivery existente construye Raycast Preview, no entrega actualmente un par Debug/test APK C5.4. No reutilizar su autorización anterior ni asumir que firma automáticamente el test APK.

## Paquetes y todos los perfiles

El target compilado es dev.bautista.spotlight.next; el test package es dev.bautista.spotlight.next.test. Preview67 usa dev.bautista.spotlight.next.raycastpreview.

Un Debug y Preview67 pueden coexistir por sus identificadores distintos, **sólo si el target Debug y el test package están libres globalmente o son instalaciones de prueba expresamente autorizadas del mismo checkpoint**. No hace falta cambiar applicationId para esa coexistencia condicional. No se ha verificado tal condición en Galaxy/Motorola.

Android comparte el APK de un package entre usuarios/perfiles. Que no aparezca en el perfil actual, que esté detenido/deshabilitado, o que exista otro usuario de pruebas no prueba que pueda instalarse sin reemplazar una aplicación de otro perfil. También se deben tratar como ocupación los registros retenidos con datos y cualquier instalación cuyo carácter personal sea desconocido. Una coincidencia de certificado no autoriza reemplazarla.

No se instalará nada sobre una instalación personal, aunque conservara datos o fuera compatible. No usar -r, downgrade, desinstalación o borrado de datos como solución. Si no pueden inspeccionarse todos los perfiles, espacios privados/Knox o el registro global, el resultado es NO-GO.

### Auditoría de sólo lectura cuando exista un dispositivo autorizado

Comandos de referencia, NO ejecutados ahora contra un Android:

~~~sh
C5_SERIAL='SERIAL_DEL_EQUIPO_AUTORIZADO'
adb -s "$C5_SERIAL" shell pm list users
adb -s "$C5_SERIAL" shell dumpsys package dev.bautista.spotlight.next
adb -s "$C5_SERIAL" shell dumpsys package dev.bautista.spotlight.next.test

# Repetir para CADA ID devuelto, incluido trabajo/privado/Knox cuando esté expuesto:
C5_USER_ID='ID_VERIFICADO'
adb -s "$C5_SERIAL" shell pm list packages -u --user "$C5_USER_ID" dev.bautista.spotlight
~~~

Interpretar el registro global de dumpsys junto con las consultas por usuario, no sólo pm path del usuario actual. Conservar las salidas referidas a Spotlight; no hace falta inventariar aplicaciones personales ajenas al proyecto. Si hay errores de permisos, perfiles ocultos/no verificables o registros ambiguos, detenerse. La instalación futura necesitaría una comprobación fresca inmediatamente antes, además del veto a reemplazos.

## Ruta recomendada

**Para validación física: equipo ARM64 dedicado a pruebas, ya limpio de Spotlight en todos sus usuarios/perfiles y sin documentos personales, con autorización del titular/administrador.** No se recomienda resetear los equipos personales para lograrlo. Como alternativa, conectar Galaxy/Motorola y comprobar si sólo alojan Preview67 y el slot Debug está realmente libre globalmente. En ese caso se podría preparar una instalación paralela autorizada, manteniendo Preview67 intacta; hoy falta esa comprobación.

**Ensayo preliminar de menor riesgo: AVD nuevo y desechable en un host ya apto para virtualización y SDK.** Debe estar vacío, sin restaurar cuentas/backups ni montar almacenamiento personal. Este Work no dispone de ese AVD listo; no se aprovisionó ni se presupuestó un servicio externo. El AAR ONNX Android ya fijado incluye x86_64 y ARM64 entre sus ABI, lo que permite estudiar una imagen compatible sin cambiar dependencias, pero no prueba que JNI/OCR funcionen en ella. No se descargaron imágenes o modelos.

Un AVD sirve para ensayar instalación, Binder/SAF y el banco; **no satisface la validación física de Tab A9+/G86 ni permite trasladar sus cifras de RAM/latencia a ellos**.

## Qué debe quedar listo antes de pedir autorización de ejecución

1. Inventario real del target: dispositivo, Android/ABI, usuarios/perfiles completos y registro global sin conflicto. Atestación de que es equipo de pruebas autorizado, sin datos personales.
2. Elección del target compatible con DebugAndroidTest, sin modificar paquetes. Si el package está protegido u ocupado, no preparar su reemplazo: elegir otro equipo o mantener el bloqueo.
3. APK Debug y test APK del mismo source_sha aprobado, procedencia comprobable, hashes SHA-256 y certificado histórico autorizado coincidente. Hoy estos artefactos NO existen; no hay un par exacto fuente/workflow de entrega nuevo listo para autorizar.
4. Ruta de construcción/firma del par revisada y autorizada separadamente; sin keystore debug nuevo ni adaptación silenciosa de native-delivery. Este documento no implementa ni ejecuta ese flujo.
5. Instalar sólo con autorización posterior y sin reemplazos; después seguir el protocolo físico ya publicado. Documentos exclusivamente ficticios. Modelo opcional sólo si ya está verificado en ese mismo package y los recursos permiten usarlo. Si falta, registrar bloqueo; no descargarlo automáticamente.
6. Mantener salidas reales separadas de lo no ejecutado. No repetir C5.3 ni los cinco métodos por falta de infraestructura.

**Bloqueos actuales:** ningún dispositivo/AVD disponible; perfiles no auditables; par Debug/test ausente; firma del par no comprobable. No se puede resolver ninguno fingiendo aislamiento con otro perfil ni usando una Preview antigua.

## Continuidad

Se preservan los resultados C5.3: 7/12 pares relevantes léxicos, 5 omisiones, 0FP; semántica auténtica sin evaluar. C5.4 conserva sus cinco métodos y evidencia de compilación anteriores. En esta decisión sólo hubo lecturas de entorno, protocolo, configuración y metadatos de ABI; ninguna compilación, firma, instalación, instrumentación, Actions o cambio de permisos.

Focus Studio, Preview67, paquetes, certificados, código productivo, main y checkpoints intactos. C5 continúa abierto. **No se inicia C5.5.**
