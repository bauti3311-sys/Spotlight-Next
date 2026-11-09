# Obsidian Liquid — vertical Vivaldi, Compose real

El propietario eligió Liquid con tipografía editorial, menos cápsulas, claro neutro
 y footer discreto. Autoriza sólo esta escena y componentes compartidos; no adopción
general, APK/Actions ni C3–C9. LocalLiquidScene=null por defecto: no hay preferencia
persistida ni entrada de producción que active la revisión. V1–V4 se conserva.

## Método

LiquidVivaldiSceneTest usa el panel de producción, motor de búsqueda, selección,
inspector, toolbar y footer reales, con PackageManager de Robolectric/API33 nativo.
Consulta exacta Vivaldi, selección abierta, versión7.2-fixture y mismo paquete/icono
para las tres variantes, sin nueva búsqueda entre capturas. Vivaldi es el único
paquete de fixture y no publica ACTION_SEARCH; se muestra la indisponibilidad real.
Icono genérico Android del fixture: no es captura de la app comercial ni una foto
física. Contratos de motores, C1/C2 y ejecutores no se modifican.

Se renderiza Preview63 con flags de revisión desactivados, en el árbol actual;
no se ejecuta un binario histórico63 ni se atribuyen las imágenes a One UI.
Luego se activa la escena Liquid con Inter y Geist sobre el mismo estado vivo.
Resoluciones del overlay: tablet960dp y teléfono360dp; presupuesto700dp.
El alto medido se adapta al contenido en cada variante: no se estira la imagen
para aparentar equivalencia geométrica. Mismo ancho/presupuesto/densidad/tema.
No se añade Gboard ni una página PDF conceptual a esta escena.

## Composición implementada

Resultados43% del ancho útil en tablet, sujetos al limitador existente de layout;
inspector con un único plano inset, sombra8dp y gradiente de dos rellenos opacos.
Sin blur, shaders, imágenes decorativas ni nuevas capacidades. Identidad64dp en tablet y48dp en fila en teléfono,
título32/38sp en tablet y26/31sp en teléfono con peso600 y espaciado−0,8sp; etiqueta editorial y subtítulo real
«Navegador» restringidos a Vivaldi. Las utilidades existentes comparten fila;
metadatos y acciones conservan callback y estado reales. Superficies claras marfil,
piedra y tinta, oscuras grafito, con acento coral sólo en inspector. Footer elimina
los hints visuales Enter/Ctrl+K en la escena, conserva acciones y blancos48dp;
los atajos siguen funcionando. En teléfono sigue el layout contextual vertical,
con scroll acotado y footer fuera del contenido desplazable.

Inter ya estaba integrada. Geist Regular/SemiBold se incluyen locales con OFL,
proveniencia fijada y hashes en licenses/Geist-provenance.json. No SDK/red de fuentes.
Comparación real de búsqueda, identidad y estilos de texto; no se afirma que los
iconos sean de una fuente. Los tokens de apariencia/fontScale se conservan.

## Rendimiento y límites

Se añadieron253.920 bytes de fuentes sin comprimir más su licencia; no equivale a
un incremento medido de APK. No hay blur ni trabajo de red/IO por tecla nuevo.
El plano y la sombra agregan pintura/overdraw frente a una superficie plana.
No se introdujo animación de altura ni ejecutores. No hay mediciones físicas
p50/p95/FPS/RAM/energía: Robolectric/capturas no las sustituyen. La duración del
build/test tampoco mide fluidez de búsqueda. Falta One UI/Gboard/teclado físico,
fontScale grande y TalkBack físico; los problemas62/63 siguen abiertos.

## Comparativas sin retoque

| Perfil | Referencia63 | Liquid / Inter | Liquid / Geist |
|---|---|---|---|
| tablet-dark | [preview63](tablet-dark-preview63.png) | [inter](tablet-dark-inter.png) | [geist](tablet-dark-geist.png) |
| tablet-light | [preview63](tablet-light-preview63.png) | [inter](tablet-light-inter.png) | [geist](tablet-light-geist.png) |
| phone-dark | [preview63](phone-dark-preview63.png) | [inter](phone-dark-inter.png) | [geist](phone-dark-geist.png) |
| phone-light | [preview63](phone-light-preview63.png) | [inter](phone-light-inter.png) | [geist](phone-light-geist.png) |

Los PNG son originales de captura nativa Roborazzi, sin edición, recolor, escalado
ni generación de imágenes. Las láminas conceptuales anteriores no se usan como
evidencia de esta implementación.

## Revisión de calidad y tipografía

Geist e Inter tienen capturas independientes y reales, no reemplazos de texto
sobre una imagen. La recomendación es Geist para esta dirección, manteniendo
Inter como comparación; el salto mayor procede de jerarquía/espacio, no del nombre.
La fuente del campo de búsqueda y los estilos de identidad/metadatos cambian
juntos, sin tocar el contenido escrito ni su InputConnection. No se decide aún
la preferencia tipográfica global del producto.

La primera pasada visual dejó metadatos bajo el scroll del teléfono. Se conservan
sus dos capturas Geist en first-pass como diagnóstico. El ajuste cambia a identidad
en fila sólo cuando el ancho interior es menor360dp y exige que el paquete esté
visible en las capturas nuevas. No se elimina contenido ni se relaja el presupuesto
para hacer pasar la comprobación. En tablet el inspector editorial usa más altura
que63; queda mayor vacío bajo los dos resultados. Esto es una contrapartida visible,
no una mejora de densidad universal: debe revisarse antes de extender el lenguaje.

El helper compartido de acentos corrige contraste sobre todas las superficies
empleadas (normal/baja/elevada); se conserva su última evidencia previa de3 casos,
sin repetirlos ni contarlos como nuevas pruebas de esta escena.

## Validación dirigida

6 identidades distintas nuevas: cuatro recorridos reales (tablet/teléfono,
claro/oscuro) y dos contratos de contraste del material. Cada recorrido captura
63/Inter/Geist sobre la misma selección, verifica consulta/versión conservadas,
paquete visible en Liquid, footer dentro del panel y panel dentro de700dp,
ningún lanzamiento/cierre involuntario, Tab abre acciones y Escape devuelve foco
con la consulta intacta. No se suman rondas repetidas ni3 casos de contraste previos
reutilizados a los6 nuevos. No se repite la suite funcional C2 ya aprobada.

compile-red.log conserva el error inicial de compilación del test (propiedad
height inexistente en DpRect), corregido por bottom−top. first-pass conserva la
primera ronda correcta antes del ajuste responsive, no es la candidata final.
Los XML de raíz y los12 PNG de raíz corresponden a la última ronda.
La evaluación visual no es un test aprobado por el propietario.

Reproducción local (sin tareas APK/Actions):

```sh
JAVA_HOME=/workspace/tooling/jdk17 ANDROID_HOME=/workspace/tooling/sdk \
bash gradlew :app:testRaycastPreviewUnitTest \
 --tests dev.bautista.spotlight.ui.LiquidVivaldiSceneTest \
 --tests dev.bautista.spotlight.ui.LiquidMaterialTest \
 :app:lintRaycastPreview --continue --no-daemon --max-workers=2 \
 -I scripts/directed-tests.init.gradle \
 -PspotlightDirectedClasses=dev.bautista.spotlight.ui.LiquidVivaldiSceneTest,dev.bautista.spotlight.ui.LiquidMaterialTest
```

Sólo el entorno de captura usa fixtures y desactiva animaciones; no hay datos ni
credenciales remotas. Configurar el proxy/truststore del entorno si fuera necesario.
Los tiempos de directed-tests.json son del laboratorio, no latencia de la app.

Lint final: 0 errores, 27 advertencias, 1 información. Resultado crudo en lint.xml.
Gradle final correcto en4m36s; análisis realizado y reporte UP-TO-DATE porque
los hallazgos no variaron. No se interpreta ese tiempo como rendimiento de UI.
