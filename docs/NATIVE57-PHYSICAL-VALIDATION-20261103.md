# Validación física — Preview57 nativa con checkpoints1/2

Actualización de auditoría de firma: [vía Termux prioritaria y alternativa Actions
preparada sin ejecutar](NATIVE60-HISTORICAL-SIGNING-20261103.md). El propietario
identificó una ruta histórica en su Android; su disponibilidad actual no está
verificada. La referencia a un secreto en el workflow no permite confirmar su
configuración desde esta integración (API403).

**Estado: preparación bloqueada por falta de acceso al firmador histórico. No hay
APK nueva compilada, firmada ni descargable.** Base funcional aprobada:
`f6644841bf3e1725a055c6fda19e29256485ffdf`, rama de PR21. Kotlin, recursos,
manifest, permisos, dependencias y funcionalidades de ese commit se conservan.

## Identidad preparada para la actualización manual

| Campo | Candidato previsto, aún sin APK |
| --- | --- |
| Paquete | `dev.bautista.spotlight.next.raycastpreview` |
| versionCode | **60**, superior al57 documentado; distingue esta entrega de la numeración experimental58/59 |
| versionName optimizada | `2.0.1-raycast-preview-native57-v60` |
| versionName si se justifica perfil experimental | `2.0.1-raycast-preview-native57-v60-experimental` |
| Firma histórica Preview SHA256 | `885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba` |
| Perfil/ABI | RaycastPreview, no debuggable, ARM64, min26/target35 |

Sólo cambian metadatos de la variante Raycast. La app principal mantiene su
paquete/código/nombre/certificado: `release.properties` no cambia. La referencia57
original y las ramas/PR existentes se conservan. La experimental WebView58 usa
`.webviewprototype`, otro paquete; no se incorpora ni se modifica esa variante.

Se verificó directamente una APK nativa histórica56 descargada de la Release
privada: checksum del servidor coincidente, firma v2 válida y certificado885838aa.
Coincide con el certificado documentado en la nota de entrega57. No se dispone aquí
del APK57 original ni del dispositivo instalado: la identidad de la instalación
física sigue pendiente. El certificado de la app principal que cita la skill de
release (`11b0...`) es distinto y **no sustituye la identidad Preview**. La firma
histórica Preview tiene DN Android Debug; eso no autoriza generar otra clave debug.

## Bloqueo real de firma

No existen los archivos previstos `signing/credentials.properties` y
`signing/spotlight-debug.keystore` en los worktrees. Tampoco hay variables de firma
configuradas, secret bindings en el entorno cloud ni Codespaces visibles desde
la conexión actual. Se revisaron sólo presencia/configuración y metadatos, sin
imprimir secretos. Ninguna clave nueva fue generada y no se usa otra firma.

Hace falta una **referencia al firmador histórico autorizado** (ruta/entorno),
no credenciales en texto. Cuando sea accesible, comprobar primero su certificado
público contra885838aa y recién entonces ejecutar la preparación restante.

## R8: evidencia y criterio de perfil

Se recuperaron los dos logs originales del paquete de evidencia54 de la Release
privada. Ambos llegan a `minifyRaycastPreviewWithR8` y pierden el daemon; el log no
prueba OutOfMemoryError, fallo del código R8 ni una causa raíz específica. El
reintento usó heap3GiB, un worker y Kotlin in-process. La nota57 acredita una
entrega experimental sin R8/shrink; no acredita R8 optimizado aprobado.

El episodio reciente de presión de memoria durante pruebas confirma que hay que
controlar procesos/recursos en **este** entorno, pero no demuestra la causa de
los fallos históricos de R8. Por eso no se elige automáticamente el perfil
experimental. No se ejecutó R8 ni assemble en esta preparación.

Tras resolver firma, plan para obtener **una sola APK final**:

1. Congelar el commit candidato publicado con estos metadatos. Revisar reserva,
   árbol limpio y que `app/src` sigue idéntico a f664484; no añadir funciones.
2. Reutilizar las52 pruebas/Lint correctos del código f664484 y las19 de
   accesibilidad del checkpoint anterior, sin sumarlas como una misma ejecución.
   No repetir suites sólo por metadatos. La configuración Gradle y dos tests de
   identidad de la app principal pasaron en esta preparación.
3. Comprobar primero disponibilidad/certificado del firmador. No producir una
   APK sin firma ni sustituirlo por debug. Registrar memoria/cgroup/espacio y
   detener únicamente daemons propios confirmados IDLE si hace falta.
4. Intentar `:app:minifyRaycastPreviewWithR8 --offline --max-workers=1`, sin APK
   todavía. Usar JDK17/SDK35 cacheados, Kotlin in-process y heap controlado con
   margen real respecto al límite8GiB; no mantener otros heaps inactivos ocupando
   la memoria. Registrar comando y `memory.events` antes/después.
5. Si R8 pasa, `:app:assembleRaycastPreview` incremental con el mismo perfil y
   commit, una sola APK final. Si falla, conservar log/dump/eventos y diagnosticar
   ese fallo actual antes de cambiar nada. Sólo si la causa/bloqueo lo justifica,
   usar el perfil **ya existente** `-Pspotlight.preview.optimize=false`, declarar
   sin R8/resource shrinking y no presentar ese build como optimizado aprobado.
6. La APK final debe verificar: firma v2/certificado885838aa, paquete y code60,
   no debuggable, min/target/ABI, zipalign16KiB y segmentos ELF16KiB; sin assets
   Nuxt/puente visual WebView ni archivos de firma/credenciales.
7. Crear ZIP fuente con `git archive` del mismo commit compilado y hashes de
   ambos. Registrar inputs, perfil, comandos y SOURCE_COMMIT. El código candidato
   descenderá de f664484 con sólo metadatos/documentación; no atribuir el binario
   al SHA padre si sus metadatos difieren. Verificar artefactos antes de entregar
   APK/ZIP juntos por un mecanismo privado descargable, sin Actions ni merge.

## Protocolo breve para Galaxy Tab A9+ — cuando exista APK verificada

Instalación **manual y opcional**. Anotar versión instalada, ajustes y permisos;
conservar los datos. Este mismo paquete es una actualización de Raycast Preview,
no una instalación paralela. No desinstalar57 ni borrar datos. Si Android indica
conflicto de paquete/firma, cancelar y registrar el mensaje; no intentar resolverlo
eliminando la instalación. La herramienta no instala ni actualiza nada sola.

| Prueba | Resultado esperado |
| --- | --- |
| Abrir overlay en vertical y horizontal, con/sin IME | Barra compacta y lectura correctas; sin fullscreen ni salto de foco |
| Buscar una app realmente instalada, luego un archivo/contacto autorizado | Resultados reales; coincidencia exacta relevante, consulta y selección estables |
| Flechas/Enter, Tab/ShiftTab, CtrlK y Escape | Enter abre la selección; Tab autocompleta sólo con sugerencia inequívoca y foco de búsqueda, si no abre acciones; dentro del menú recorre; Escape cierra primero el contexto |
| Menú con título largo y texto ampliado | Texto completo/foco legible, filtro conserva nombre/consulta; scroll alcanza última acción sin ejecutarla |
| Lanzar una app válida y regresar | App exacta solicitada una vez; ajustes/consulta/contexto conservados según el flujo |
| Destino no instalado: p.ej. `vivaldi prueba` si falta Vivaldi | Error visible en Spotlight. La alternativa web no se abre sola; Volver/Escape/X cancelan. Tab y confirmación explícita abren sólo el destino mostrado |
| Cambiar consulta o cancelar una alternativa pendiente | No se ejecuta la revisión anterior; reapertura no restaura consentimiento |
| Arrastrar resultados/PDF y tocar/abrir explícitamente | Scroll no lanza nada; PDF seleccionado usa Quick Look local; abrir externo sólo con acción explícita |
| Cerrar/reabrir, aumentar fuente, teclado físico/táctil | Estado recuperable, controles alcanzables y datos previos conservados |

Registrar por caso: correcto/falló, orientación, teclado/escala y mensaje observado.
Una captura o video corto del fallo es suficiente; usar consultas de prueba sin
contenido privado. No hace falta enviar mensajes, conectar cuentas ni probar
operaciones sensibles para validar este bloque. La aceptación física y efectos
externos no se certifican por los tests Robolectric.

[Evidencia de preparación](verification/native-validation-apk-20261103/preparation.json).
