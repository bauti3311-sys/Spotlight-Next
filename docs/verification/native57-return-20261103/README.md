# Evidencia — retorno a Preview57, sin cambios productivos

Fecha: 2026-11-03 UTC. [Decisión vigente](../../NATIVE-PREVIEW57-DIRECTION-20261103.md)
y [auditoría funcional](../../AUDITORIA-NATIVA-PREVIEW57-20261103.md).

## Procedencia y aislamiento

- Nueva rama `feat/preview57-native-return-20261103`, creada con git worktree
  directamente desde `4fc33f2c918d3fe53a8bad7ac6571f2757630066`, no desde otra Preview.
- `remote-heads-before.tsv` conserva las refs inspeccionadas, incluyendo main,
  checkpoints y las ramas de PR19/20. La reserva estaba libre y el puntero
  integral seguía en `e38eae8802c6633cb9e9cb0bcb33bb724cb3db0b`.
- `reservation-acquired.json` registra sólo el propietario/lease de esta tarea;
  no habilita reutilizar esa propiedad en otra sesión. El cierre y liberación
  se registran mediante nota Git atómica sobre el commit publicado.
- PR19/20 leídos en GitHub y guardados en `pr19.json`/`pr20.json`; siguen abiertos
  en borrador. PR19 sólo se revisa; ningún parche de UI se incorpora aquí.
- `skills-origin.json`: siete archivos recuperados por path de las tres skills
  de PR20 (`00fec133...`), con blob y SHA256 originales. Se adaptaron a nativo
  en esta rama; **no son copias byte-idénticas ni paquetes oficiales instalados**.
  El ejemplo bridge-contract.json se sustituye por native-contract.json;
  `scope-check.json` conserva hashes de las adaptaciones. No se importa el resto
  de PR20, su AGENTS, su implementación, assets, tests, prompt ni lockfile web.
- Worktrees anteriores conservados: `/workspace/Spotlight-Next` en `a622df752...`
  y `/workspace/Spotlight-Next-bridge` en `00fec133...`, sin cambios Git locales.

## Comprobaciones actuales

```sh
python3 docs/verification/native57-return-20261103/verify_scope.py
# Guardar su stdout como scope-check.json si se necesita una nueva observación.
git diff --check
```

`verify_scope.py` sólo lee Git/archivos: ancestralidad exacta, ausencia del HEAD
del puente como ancestro, árbol app idéntico, sin cambios en fuentes/tests,
Gradle/configuración de build/manifest/workflows, y procedencia de las tres skills.
No ejecuta código de la aplicación. Las comprobaciones terminaron correctamente;
no deben presentarse como pruebas Kotlin, de accesibilidad o de funcionamiento.

`actions-readonly.json` conserva una lectura de cinco ejecuciones recientes y
consultas de trabajos en curso/en cola (ambas vacías). No se conoce el saldo real
de facturación/minutos: no se afirma una cuota. Los workflows se inspeccionaron:
la rama nueva y el PR contra `feat/antigravity-tablet-20261026` no están en sus
filtros automáticos. No se modifican workflows, se añaden labels ni se hace dispatch.

## Evidencia histórica revisada y sus límites

- `preview57-delivery-note.txt`: nota remota original. Declara67 casos en cohortes,
  ensamblado experimental, identidad/firma y hashes APK/ZIP. No se descargaron ni
  verificaron de nuevo esos binarios/ZIP. No se certifica R8 ni se afirma nueva APK.
- `previous-native-note.txt`: continuidad de PR19, sin trasladar sus modificaciones.
- `pr19-evidence-inspection.json`: atributos de cuatro XML leídos desde el HEAD
  de PR19: reproducción inicial1/1 fallida, bloque posterior4+7 correcto y barra4
  de otra ronda. No son pruebas ejecutadas hoy ni se suman repeticiones.
- Se inspeccionaron las capturas históricas antes/después de `phone-large-text`:
  el recorte pasa a texto completo y la fila aumenta de altura. Son capturas
  Robolectric del componente Compose, no un dispositivo ni un diseño nuevo.
  Las imágenes y logs completos permanecen [en PR19](https://github.com/bauti3311-sys/Spotlight-Next/blob/a622df75208e40826955e51cc84c65d7754f9f53/docs/verification/hallmark-jakub-preview57-20261102/README.md).

## Alcance cerrado

Cero compilaciones Gradle, ejecuciones nuevas Kotlin/UI, APK, firma, servidor,
dependencia, permiso, subida documental, mensaje, llamada de IA o cuenta conectada.
Sólo skills adaptadas, dirección vigente y auditoría/propuesta. Producción y tests
siguen siendo Preview57 exacta. No se modifica main, PR19/20, las otras ramas ni
checkpoints; no se promueve matriz ni puntero integral. La validación física de
One UI/IME/Spotify/WhatsApp y la evaluación nativa OCR/semántica siguen pendientes.
