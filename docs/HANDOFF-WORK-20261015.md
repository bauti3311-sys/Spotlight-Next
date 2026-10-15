# Continuidad Work —15/10/2026

## Punto de recuperación

Continuar desde el HEAD remoto de `fix/work-gemini-native-20261014`. Último commit que cambia Android: `8ad8ca299eeec6ac037c10940a604a80700423c1`; anterior17289fb; base0a35569. Este cierre posterior cambia documentación y extracción de herramientas. No volver a main/a47e3bf ni reemplazar el motor por el ZIP de Gemini. PR16 y checkpoints históricos permanecen intactos.

Reserva propia: `work-gemini-native-20261014-f914c617`, referencia `refs/heads/locks/spotlight-autonomy`; consultar SHA y propietario actuales antes de editar. Estado pausado porque el entorno de ejecución quedó desconectado. No reclamar por antigüedad, copiar un propietario ajeno ni afirmar que se liberó. Git CLI carecía de autenticación en este entorno; se utilizaron Git Data API y expected_sha con force=false, verificando árboles/objetos. La reserva se actualizó tras publicar cada checkpoint.

## Cambios implementados

- Ranking por clases antes de bonos personales: app exacta, prefijo y parcial relevante; regresión webOS Dev Manager frente a Wi-Fi/WhatsApp. Consultas de varias palabras requieren todos sus tokens.
- Inicio real sin historial: hasta3 apps instaladas/favoritas/recientes/frecuentes;2 documentos autorizados del índice, por modificación, tras comprobar apertura;2 apps adicionales sin duplicar. IO, consulta de documentos limitada a32 entradas, sin lanzar nueva indexación. Se respetan módulos, aprendizaje/privacidad, ocultar/fijar y favoritos canónicos.
- Tres grupos Gemini; encabezados11sp/15sp y primer margen7dp; filas compactas, secundarios en línea, PDF azul y selección gris conservada. Cápsula visible32dp con botones48dp y margen vertical1dp. Prueba geométrica estricta mantiene más de8px hasta el pie. Acciones por cápsula/pulsación larga/accesibilidad; chevron se conserva en pantalla pequeña.
- Indicador de reproducción ligado a STATE_PLAYING de sesiones autorizadas y callbacks mientras la ventana está compuesta. Nunca por adivinar la app instalada.
- Se conserva DirectAi/CredentialVault/streaming. Preguntas locales ampliadas, Markdown nativo sin HTML/imágenes remotas y cancelación conservando texto visible. Lista de modelos Gemini paginada mediante petición manual con clave en cabecera; ninguna consulta remota por escribir, ningún cambio automático de proveedor/modelo guardado. No se hizo una petición real a IA: pruebas de transporte usan fixtures.
- Preview release optimizada/R8 con paquete independiente `dev.bautista.spotlight.next.preview`, autorizada expresamente por el usuario; no sustituye la app `.next`, no comparte sus datos/permisos ni tiene su firma histórica. Código29. Tipo de servicio specialUse explicitado solo en Preview, idéntico al heredado de main, para resolver lint de esa variante. OverlayService, invocación y TextFieldValue no se reemplazan.

## Evidencia y límites

420 pruebas Android actuales correctas y30 Python correctas. Actionlint/ShellCheck correctos. Gitleaks sobre0a35569/17289fb sin hallazgos. En el último proceso aislado se observaron completados lintPreview, testDebugUnitTest, assembleDebug y assembleRelease/R8. La conexión se perdió durante los restantes gates: no afirmar éxito de todo el pipeline. Lint Debug/Release de una iteración anterior:0 errores,11 advertencias,1 información.

Las17 comparaciones históricas se generaron/revisaron antes del ajuste final; las baselines finales siguen pendientes de aceptación/reverificación. Capturas nativas Robolectric4.14.1/API33, PM/files fixtures explícitos; no Samsung/emulador conectado. No hay PdfRenderer utilizable en ese host: el error PDF no acredita una página real. ADB no mostró dispositivos. Imagen de referencia: ventana central713×560px del JPG1536×960; no equiparar píxeles a dp sin conocer zoom/DPR.

**No se entregó una APK final descargable.** Existía una Preview intermedia firmada/verificada estáticamente, pero no se presenta como el binario final del SHA. El proceso final quedó sin resultado verificable; el intento de conservar una captura también falló por409 environment_offline. La firma histórica necesita CI verde sobre el SHA exacto. Cero Actions solicitados y ninguna modificación de presupuestos, retención o artefactos conservados. Minutos actuales no confirmados; Billing Usage leído mostró US$0 facturado.

## Reanudar sin perder trabajo

1. Revisar archivos locales y git status antes de cualquier restauración; recuperar `verification/isolated-final/workspace.txt`, logs y temporales si siguen disponibles. No borrar derivados/evidencias mientras se diagnostica el cierre. Comprobar reserva/remoto y ausencia de otra sesión antes de editar.
2. Si hace falta repetir gates, usar una copia del SHA publicado fuera de la raíz sincronizada para evitar `.rsync-tmp` dentro de fuentes/dex. Reutilizar SDK35/JDK17/Gradle8.10.2 existentes si están sanos. Comandos locales, desde esa copia:

```sh
bash gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug :app:lintRelease :app:assemblePreview :app:lintPreview -Pkotlin.compiler.execution.strategy=in-process -Pkotlin.incremental=false --no-daemon --max-workers=1 --console=plain
bash gradlew :app:compareRoborazziDebug --tests '*VisualSnapshotTest' --no-daemon --max-workers=1 --console=plain
```

3. Revisar los17 pares visuales, no aceptar automáticamente; conservar aserciones/tolerancias. Registrar baselines solo después de revisión y ejecutar verifyRoborazziDebug con el mismo filtro. Recuperar capturas de GeminiHomeCaptureTest y RaycastCompositionCaptureTest; no sustituir errores de PDF por imágenes inventadas.
4. Congelar SHA, compilar Preview desde ese árbol, comprobar package/version/minSdk/ABI/non-debuggable, apksigner y zipalign; ZIP con git archive del mismo SHA, sin credenciales/cachés. Preservar paquete/firma/datos principales. Guardar entrega privada y evidencia antes de cerrar; sin releases públicas ni Actions repetitivos.
5. Solo considerar firmado histórico después de comprobar cuota disponible y CI verde del SHA exacto, mediante el procedimiento protegido existente. No obtener la clave histórica localmente ni desactivar gates. Mantener presupuesto0 y bloqueo; no aceptar gastos.

La comprobación física debe incluir frío/foco/IME, búsqueda exacta real, iconos, selección/acciones, PDF/Quick Look,3 temas y orientaciones, blur/One UI, reproducción real y, si el usuario configura una clave, conversación IA explícita/streaming/Markdown/copiar/cancelar. La Preview se abre por su acceso propio sin reasignar la invocación principal ya validada.
