# Spotlight Next — Tres skills para Codex

**Este ZIP no instala OpenUI ni RAGFlow.** Incluye tres skills propias, escritas para que Codex evalúe y desarrolle sus capacidades en el proyecto Spotlight Next con criterios de aceptación. No se copió ningún archivo de las tres fuentes de terceros; se enlazan las referencias originales.

## Estructura

```
.agents/skills/spotlight-api-contracts/SKILL.md
.agents/skills/spotlight-openui-forms/SKILL.md
.agents/skills/spotlight-ragflow-documents/SKILL.md
PROMPT-PARA-CODEX.md
```

## Instalar en un clon de Spotlight Next (Ubuntu/Termux)

1. **Comprobar** que estás en la copia y rama correctas, y que no hay modificaciones ajenas. `python3 scripts/autonomy_lock.py status` es consulta de solo lectura.
2. **Extraer** este ZIP en la raíz del repositorio: `unzip -n /ruta/Spotlight-Codex-3-Skills.zip -d /root/projects/spotlight-next-antigravity`
3. **Revisar** `git status --short` y el contenido de `.agents/skills/`. No ejecutar scripts del paquete (no contiene scripts ejecutables).
4. Reabrir o reanudar Codex y pedirle que lea `PROMPT-PARA-CODEX.md`. Codex puede activar skills automáticamente según descripción o invocarse por `$spotlight-api-contracts`, `$spotlight-openui-forms` y `$spotlight-ragflow-documents` según soporte de la versión.
5. **La instalación de los SKILL.md no ejecuta los cambios de la APK**. Se debe implementar y probar cada fase por separado. La colaboración y las reservas siguen el protocolo oficial.

## Fuentes originales
- API & Interface Design: https://github.com/addyosmani/agent-skills/tree/main/skills/api-and-interface-design
- OpenUI: https://github.com/thesysdev/openui
- Skill oficial OpenUI: https://github.com/thesysdev/skills/tree/main/skills/openui
- OpenUI Vue: https://www.openui.com/docs/api-reference/vue-lang
- RAGFlow: https://github.com/infiniflow/ragflow
- RAGFlow API: https://ragflow.io/docs/http_api_reference

## Orden
1. **API & Interface Design**: contrato y pruebas Kotlin–Nuxt.
2. **OpenUI**: formularios contextuales nativos visualmente Raycast (primero sin IA generativa).
3. **RAGFlow**: proveedor documental opcional por REST (primero mock, luego servidor real solo con autorización adicional).

## Lo que NO se debe hacer
- No intentar correr RAGFlow Docker ni modelos de servidor en la tablet.
- No instalar ni ejecutar scripts remotos sin revisar licencias/dependencias y obtener autorización si añaden cambios importantes.
- No añadir API keys al código, frontend o ZIP.
- No cambiar firma histórica, package ID, `main`, reservas ajenas, CI ni el diseño aprobado.
- No afirmar APK lista, test exitoso o captura física que no se haya verificado.
