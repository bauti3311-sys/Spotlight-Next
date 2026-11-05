# C2 — aceptación física pendiente

Este checklist no registra pruebas realizadas. Galaxy Tab A9+ y moto g86 no
están conectados a esta sesión. El cierre técnico local no cierra C0/C1/C2 físicos
ni autoriza C3. No generar APK ni ejecutar Actions sin autorización específica.

## Referencia Preview62

Preview62 (fuente135a9af, certificado histórico885838aa) sigue pendiente de
validación física. Antes de evaluar una futura APK C2, registrar en Tab A9+:

- Apertura desde Ajustes y otra app, cierre/reapertura y orientación.
- Gboard cerrado→abierto→cerrado, teclado físico, foco, altura y footer.
- Scroll de resultados/PDF largo e inspector, sin abrir externos por desplazar.

Repetir los puntos aplicables en moto g86. No atribuir a Preview62 los cambios
posteriores: todavía no se entregaron en otra APK.

## Futura APK C2, cuando sea autorizada

Registrar dispositivo, Android/One UI, versiones de apps y proveedor SAF.
Usar documentos propios de prueba, sin datos privados en evidencias.

| Recorrido | Resultado que debe comprobarse en ambos dispositivos |
|---|---|
| Búsqueda | App exacta primero; «PDFs de ayer» según modificación conocida; «buscar documento de vestibular»; «compartir archivo Ayala»; hasta3 sugerencias iniciales, selección estable y ninguna apertura por escribir |
| Selección y recuperación | Recibir2 archivos, añadir un tercero, rotar/recrear y conservar3; retirar contexto intacto retira su selección; selección editada se conserva; URI restaurada no equivale a permiso |
| Revisión | Nombre, origen autorizado, MIME y destino legibles; confirmar sólo tras revisión nueva; cancelar conserva consulta/selección y recupera foco |
| Envío simple/múltiple | PDF, imagen y mezcla autorizados; adjuntos originales y MIME correctos; selector Android sólo después de confirmar; una sola solicitud, sin destino alternativo automático |
| Revalidación | Revocar lectura, borrar/cambiar documento o retirar receptor entre revisión y confirmación: error recuperable, sin apertura ni reintento automático |
| Retorno/restauración | Volver del selector, rotar, cerrar overlay y recrear proceso no restaura confirmación ni repite solicitudes; permiso vencido exige selección explícita |
| Accesibilidad | TalkBack, fuente200%, nombres largos, ventana compacta/amplia, última acción alcanzable, teclado/táctil y footer dentro de límites |
| Rendimiento | Medir primer acceso/migración del índice y búsquedas sobre un árbol representativo; latencia p50/p95, RAM y jank observados, sin inventar métricas desde JVM |

Aplicar la [matriz técnica](C2-APPLICATION-COMPATIBILITY-20261104.md) a WhatsApp
personal, WhatsApp Business, Gmail, Spotify, Vivaldi y lectores/editores disponibles.
Anotar capacidades ausentes como ausentes. No inferir SEND de VIEW ni MULTIPLE
de SEND. Usar proveedor local y uno remoto autorizado si está disponible; comprobar
la vida del grant temporal al terminar Spotlight y al volver del receptor.

En cada receptor compatible comprobar manualmente adjunto, cuenta/conversación,
borrador y resultado final pertinente. El recibo «Solicitud enviada a Android»
no acredita entrega, búsqueda efectiva ni reproducción. No automatizar envíos
reales para completar el checklist.

## Criterio antes de C3

Cerrar con evidencia los recorridos aplicables, documentar capacidades ausentes
y corregir cualquier bloqueo reproducible dentro de C2. Los casos no ejecutados
siguen pendientes; no se convierten en aprobados por fixtures o por este documento.
Presentar el resultado al propietario antes de solicitar avanzar a C3.
