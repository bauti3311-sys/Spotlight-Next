# Comparación con los últimos adjuntos Gemini

Se revisaron los tres adjuntos del usuario después de publicar el checkpoint de recuperación62aa4ef. Son referencias, no un proyecto para sustituir Spotlight Next.

| Adjunto | SHA256 |
|---|---|
| raycastspotlighttheme (2)(1).kt | 8c43270d49491a822b09536eccccb568d0081983255a9259b661e2702f5e9c30 |
| spotlight-next-obsidian-glass(1).patch | da94a4122d6856e85195066dd7d1b1728d7a10012590ffc49e051a462ec8c74b |
| Spotlight-Next-ObsidianGlass-Android(1).zip | b39f37b15a23a96c0cae150018762e8ed25684bf7b917caf30fe7a8c67f46c03 |

El Kotlin del ZIP y el adjunto separado solo difieren por el salto de línea final. El ZIP declara paquete dev.bautista.spotlight, código2 y SDK34; el principal conserva dev.bautista.spotlight.next, código29 y SDK35. No se aplica el patch: está destinado a otro archivo/host.

| Referencia | Principal recuperado | Decisión |
|---|---|---|
| Barra56dp/16sp |56dp/16sp | Mantener editor TextFieldValue y atajos nativos |
| Filas36/42/48dp; iconos26/radio7; radiofila11 | Iguales en ancho real>=560dp | Mantener fallback estrecho de dos líneas legibles |
| Gradientes Obsidian E01C1C21→F2101014; Graphite EB26272C→F7191A1E; Light E0FAFAFC→F0EEEEF3 | Mismos valores ARGB a opacidad de referencia | Conservar preferencia de opacidad |
| Texto F4F4F6/94949E, FAFAFC/A0A0AB,18181B/52525B | Mismos colores por tema | No sustituir por paleta Material convencional |
| Selección neutra13–8–11% y borde14% | Gradiente neutro animado con espacio de acción estable | Conservar selección gris aprobada |
| Cabecera con glyphs15 y botón32 | Glyphs Hugeicons15dp; áreas48 accesibles | Tamaño visual de referencia y objetivos accesibles |
| Cápsula inferior/vignette48 | Cápsula/vignette nativas, funciones reales y Tab | Incorporado; sin importar Ctrl+K en lugar del contrato Tab |
| Slider inline manual24 | Sistema nativo de volumen/brillo existente | No importar sin una necesidad real ni perder semántica accesible |
| Quick Look condicional | Quick Look nativo por documento con expansión/Escape | No importar un inspector permanente ni lectores simulados |

Se rechazan los fallbacks de almacenamiento ficticio «74.6GB libres de128GB», catálogos de marcas no instaladas y proveedores meteorológicos/deportivos/IA de demostración. No se añade envío remoto, lectura automática de portapapeles ni permisos del proyecto independiente. No se compila ese proyecto como prueba del nuestro.

## Entrega protegida

Se prepara una plantilla **inactiva** gemini-tablet-delivery.yml.in derivada de la existente, orientada a Release/R8 ARM64 sin debuggable. Conserva autorización de propietario/rama/evento, CI exitosa del SHA exacto y repositorio original, verificación del ZIP histórico fijo y certificado antes de acceder a Secrets, paquete/código/minSdk, identidad/payload inalterados al firmar, limpieza de clave temporal y ZIP fuente del mismo SHA. La antigua plantilla debug queda intacta. No se habilita ni firma con checks pendientes. Retención propuesta3d solo para la nueva entrega privada (sin cambiar ajuste general ni workflows existentes); respaldo permanente necesario tras descarga verificada.
