# Preview64 — entregada para validación física

Único intento autorizado [42082398659](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/42082398659),
**success**, run_attempt1, mode=native-delivery, actor/triggering_actor bauti3311-sys.
Rama feat/preview57-native-return-20261103. Fuente APK/ZIP
`808bda2c25887d14b42665dea4c254181d72b837`; workflow/manifiesto
`5b346bc34a59fa4f879532a5c485a3dcddc6b1da`. Este informe es posterior y no
cambia ninguno de los SHA autorizados ni el archivo ejecutable de entrega.

## Descarga y hashes

APK individual verificada en
`/workspace/shared/downloads/spotlight-preview64/Spotlight-Next-Preview64-808bda2.apk`,
con ZIP fuente, SHA256SUMS, procedencia y comprobación independiente en el mismo
directorio compartido, para descarga directa en Work.

[Respaldo privado GitHub](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/42082398659/artifacts/13442486203)
con APK+ZIP+hashes+procedencia. Retención3días, caduca2026-11-08T18:24:02Z;
conservar una copia. No se publica release estable ni se usa una URL temporal
como único respaldo.

| Archivo | Bytes | SHA256 |
|---|---:|---|
| Spotlight-Next-Preview64-808bda2.apk | 73637160 | 40fde01a6a7ec7fa5a263278012aa372f36af0d31d5c0f64d0bdc20bd30540f3 |
| Spotlight-Next-source-808bda2.zip | 36820682 | ec9876004fbcf5acc1f9651d430f9fea734c069296249f0c60dd3502cb965beb |

ZIP conjunto110542352bytes, SHA256
b873e7efc5137a08bebe1ba709005f59d9e354dd72751edf799e6b662c3f8170,
cotejado con el digest de GitHub antes de extraer; rutas seguras y CRC correctos.

## Identidad y comprobación independiente

- `dev.bautista.spotlight.next.raycastpreview`, code64>63,
  `2.0.1-raycast-preview-native57-v64`, optimizada R8, sin debuggable.
- Firma v2 válida, un firmante, certificado histórico exacto
  `885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba`.
  Comparado con signature.txt verificado de Preview63; no se generó certificado.
- ARM64, min26/target35, zipalign y segmentos ELF16KiB correctos.
- APK firmada conserva el mapa de contenido unsigned; fuente/workflow/run/profile
  coinciden con procedencia y archivos SOURCE_COMMIT/WORKFLOW_COMMIT.
- ZIP fuente comparado byte por byte mediante hash con git archive independiente
  de808bda2, calculado antes de descargar la entrega. Sin claves/configuración privada.
- No assets Nuxt/raycast-web ni puente LocalRaycastWebView en la APK.
- Fuente E1–E5 nativa intacta; Focus=false por defecto. Sin cambios estéticos,
  main, permisos, dependencias ni nuevos C3–C9.

## Gradle, pruebas y lint

Un solo assembleRaycastPreview optimizado: **BUILD SUCCESSFUL in7m21s** en Actions.
Preflight13s, unsigned7m55s, firma49s;18:14:59–18:24:07UTC. Todos los pasos
obligatorios de los tres jobs de entrega correctos, incluidos guardas y limpieza.
Jobs históricos y directed-gate skipped por diseño de mode=native-delivery;
no se afirma que Actions haya ejecutado las unitarias/lint en este intento.

Se reutilizan los6casos JVM dirigidos de la preparación exacta, con XML verde
por caso en [resumen deduplicado](verification/native64-preparation-20261105/directed-results.json).
Incluyen selector persistente tras recreación y servicio abierto true→false→true.
No se repiten tests ni se suman los44casos históricos a ese total.

LintDebug local nuevo durante el run: **BUILD SUCCESSFUL in3m45s**,0errores,
26advertencias,1información.18tareas reutilizadas/10ejecutadas; no generó APK.
Sin baseline ni supresiones nuevas. [Resultado](verification/native64-delivery-20261105.json)
y [XML/logs/firma/paquete/procedencia](verification/native64-delivery-20261105/).
Los17checks offline de entrega y2de identidad permanecen correctos según la
preparación; no se los presenta como una nueva ejecución en este intento.

## Autonomía y límites

Reserva propia nueva codex-632cb1c8-d60b-4961-8a99-6c1250741c5a,
lease durante el run b9d159e5ef7b429aedc9e732059d1318b91a02a6,
checkpoint=workflow5b346bc. Se mantiene hasta publicar este cierre y se libera
sólo la propia con notas atómicas; comprobar status remoto al reanudar.
Main47259ce2d7259f0ddfa5d85b7c73f07983d8cd6d y checkpoint integral e38eae8 intactos.
PresupuestoUS$0/bloqueo conservados, sin retries, fallback ni otro dispatch.
Esta autorización quedó consumida; no permite repetir64 ni otro par.

Sigue siendo Preview. La compatibilidad estática con63 no prueba instalación,
conservación efectiva de datos ni aceptación de UX en OneUI. Próximo paso:
instalar sobre la Preview existente sin desinstalar/borrar datos; validar
TabA9+/moto g86, Gboard abierto/cerrado, altura/foco/footer, regreso táctil/Esc,
cursor/selección/scroll, rotación/multiventana, PDF/grants y apps reales.
La adopción global de Focus y C3–C9 siguen pendientes de aceptación expresa.
