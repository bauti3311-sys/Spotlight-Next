# C2 — matriz de contratos y validación de aplicaciones

Fecha: 2026-11-04. Alcance: Kotlin/Compose, PR21. **No se inspeccionaron aplicaciones
reales instaladas en esta sesión.** Los nombres de paquetes en fixtures sólo
prueban que la política respeta el destino solicitado. No prueban compatibilidad
con versiones comerciales, cuentas, contenido privado ni efectos externos.

## Qué comprueba el código

IntegrationRegistry/AppCapabilities consultan PackageManager y contratos públicos:
actividad exportada/habilitada, aplicación no suspendida, permiso de componente,
paquete exacto y capacidad específica. Para archivos se consulta SEND o
SEND_MULTIPLE con el MIME real; el soporte de texto, VIEW o SEND individual no
implica soporte de adjuntos ni de múltiples archivos. El Sharesheet pertenece al
sistema y excluye las entradas de importación de Spotlight. Se revalida la lectura
y los metadatos al revisar y confirmar, y la ruta/grants antes de solicitar Android.

La consulta PM es una condición necesaria, no suficiente: perfiles de trabajo,
políticas del dispositivo, versiones, proveedores, cuentas o el propio receptor
pueden impedir la operación. Un descriptor de lectura abierto/cerrado comprueba
lectura en ese instante, sin copiar ni demostrar que el destino leerá después.
El archivo puede cambiar entre revisión y lectura externa; Android no ofrece una
transacción entre ambos sin crear otra copia, que aquí no se realiza.

| Aplicación / paquete | Capacidad pública comprobable en ejecución | Evidencia local de política | Validación física pendiente | Fuera de alcance o restringido |
|---|---|---|---|---|
| WhatsApp personal `com.whatsapp` | Abrir app; borrador por contrato público existente; SEND/MULTIPLE únicamente si PM publica el MIME concreto | Texto no implica PDF; personal nunca satisface un destino Business | Aparece en selector; PDF/imagen/varios; elección de conversación; revisión y envío manual; retorno sin duplicado | Leer historial completo/DB privada, elegir cuenta o enviar silenciosamente no se obtiene con un Intent |
| WhatsApp Business `com.whatsapp.w4b` | Mismas comprobaciones, con paquete Business independiente | Ausente Business no abre personal; grants y exportación se vuelven a comprobar | Cuenta Business y conversación correctas, adjuntos/MIME real, permisos tras cerrar Spotlight | No equivalencia de cuentas ni fallback a personal. RemoteInput exige notificación compatible; su ampliación pertenece a C3 |
| Gmail `com.google.android.gm` | Borrador existente fijado a Gmail; compartir adjuntos si anuncia SEND/MULTIPLE y MIME | SEND PDF individual no anuncia MULTIPLE; cliente predeterminado diferente no reemplaza Gmail | Cuenta, borrador, nombre/tamaño/lectura de adjuntos, cancelación y envío manual | Un Intent no permite leer todo Gmail ni demuestra entrega. No se agrega API remota/OAuth de correo |
| Spotify `com.spotify.music` | Abrir app/enlaces oficiales y búsqueda pública sólo cuando la ruta actual existe; capacidades multimedia existentes dependen de sesión autorizada | Un handler WEB_SEARCH no convierte Spotify en receptor PDF | Búsqueda/link en versión instalada; sesión/cuenta y estado observable si se prueba capacidad existente | Intent enviado no demuestra audio ni playback. No se amplía OAuth/SDK ni C4; datos privados no son accesibles por PM |
| Vivaldi `com.vivaldi.browser` | Abrir enlaces/búsqueda pública admitida por versión; archivos sólo con handler SEND/MULTIPLE específico | VIEW PDF no implica SEND PDF; sin receptor no se inventa destino | Navegación real, retorno/foco, si ofrece compartir documentos y qué MIME acepta | No leer pestañas/historial privados ni controlar páginas internas por inferencia |
| Apps de documentos (lector/editor/nube) | VIEW y SEND son capacidades separadas; adjunto simple/múltiple según MIME, exportación y permiso | Receptor público MULTIPLE PDF aceptado; privado o eliminado rechazado; URI/ClipData originales y sólo READ | SAF local/nube; descriptor abierto; importación efectiva, nombre/MIME, imagen/PDF/Office/mixto; varios adjuntos; fuente eliminada/revocada | Documento virtual no abrible requiere una ruta pública adicional; se muestra error, sin copiar/convertir automáticamente. Escribir necesita autorización distinta que no se concede aquí |

Pruebas técnicas: `DocumentCompatibilityMatrixTest`,10 casos API26/35 correctos
con IntentFilter/PackageManager controlados. El fixture registra componentes,
filtros DEFAULT y MIME, sin forzar resoluciones incompatibles. Se conserva el rojo
inicial y las correcciones de registro del fixture en la evidencia. minSdk26 y
targetSdk35 permanecen iguales; esto no prueba todas las versiones intermedias ni
la política de paquetes de un dispositivo real.

## Límites de Android que no se sortean

- PM descubre componentes públicos visibles para el usuario/perfil actual. No
  autoriza acceder a bases privadas, chats, tokens, contraseñas ni cuentas.
- Shortcuts profundos pueden exigir rol de launcher/permisos; AppFunctions depende
  de versión/privilegios y pertenece a C8. No se usa accesibilidad para automatizar
  interfaces privadas ni se agrega acceso general al almacenamiento.
- Una URI en estado restaurado no restaura su permiso. Share conserva acceso
  temporal ofrecido; el trampolín lo transporta al servicio existente con ClipData
  y READ. La vida real de ese grant en One UI y tras terminar el servicio queda por
  comprobar. Si vence, se requiere selección explícita nuevamente. Sólo el flujo
  SAF existente conserva grants persistentes ofrecidos; Share no los toma solo.
- Sólo lectura temporal para el receptor elegido. Sin WRITE, PREFIX, PERSISTABLE
  salientes; sin conversión content URI→ruta privada ni copia para “resolver” un
  permiso. No hay reintento ni destino alternativo automático.
- El recibo de Spotlight dice **solicitud a Android**, resultado externo sin
  comprobar. Ver un fixture o capturar startActivity nunca cuenta como entrega.

## Protocolo físico para la próxima APK autorizada

Registrar dispositivo, Android/One UI, versiones de apps y proveedor SAF; no
incluir contenido privado en capturas/logs. Usar archivos de prueba propios.

1. Antes de instalar otra APK: mantener Preview62 como referencia y ejecutar en
   Tab A9+ ventana desde Ajustes/otra app, Gboard cerrado→abierto→cerrado,
   reapertura, orientación, scroll/PDF largo, inspector/footer y teclado físico.
   Complementar moto g86. No marcar esas casillas por pruebas Compose.
2. En la futura APK C2: búsqueda exacta de app primero; tres sugerencias máximas;
   «PDFs de ayer», «buscar documento de vestibular», «compartir archivo X» sobre
   árboles autorizados. Un archivo sin fecha no debe aparecer como “de ayer”.
3. Seleccionar 1 y varios archivos; revisar nombre/tipo/origen/destino; cancelar
   antes y después de revisión. Ninguna app externa debe abrirse antes de confirmar.
4. Confirmar una vez: selector Android→app elegida→verificar manualmente adjunto.
   Probar cada fila de la tabla con MIME PDF, imagen y mixto cuando sea compatible;
   si una app no aparece, registrar ausencia sin seleccionar otra automáticamente.
5. Revocar permiso/eliminar archivo/cambiar nombre/retirar receptor entre revisión
   y confirmación. Esperar error recuperable, sin fallback ni solicitud repetida.
6. Rotar/restaurar proceso/volver del selector/cerrar overlay; no restaurar una
   confirmación ni repetir envío. Si el grant temporal venció, seleccionar de nuevo.
7. Comprobar teclado físico/táctil, foco al cancelar, nombres largos, fuente200%,
   TalkBack y última acción alcanzable con Gboard; footer dentro de ventana. Registrar
   latencia de primer acceso tras migración de caché, búsqueda p95, RAM y jank.

Resultado físico de todas estas filas: **pendiente**. No hay una nueva APK en esta
sesión y no se inició GitHub Actions.
