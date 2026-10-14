# Reserva compartida y recuperación

Usar desde la raíz, después de leer AGENTS, PLANS, roadmap, progreso, notas y PRs. La herramienta no inicia automatizaciones, no consulta cuota, no modifica main ni el checkpoint integral. Usa solo Python/Git, sin servicios nuevos.

```sh
python3 scripts/autonomy_lock.py status
python3 scripts/autonomy_lock.py acquire --branch feat/mi-subhito --checkpoint SHA_BASE
python3 scripts/autonomy_lock.py update --owner PROPIETARIO_PROPIO --lease-sha SHA_RESERVA_PROPIA --checkpoint SHA_CHECKPOINT --state checkpoint-publicado
python3 scripts/autonomy_lock.py release --owner PROPIETARIO_PROPIO --lease-sha SHA_RESERVA_PROPIA --publish-notes
```

El acquire genera propietario único si no se proporciona uno. Guardar su JSON y SHA tras cada operación; nunca copiar el propietario de otra sesión para liberarla. Rama/commit deben corresponder al trabajo real. Actualizar la reserva después de publicar cada checkpoint seguro; update exige avance del commit fuente, sin retroceder. El registro remoto permite recuperar la rama tras interrupción.

Si hay reserva existente o metadata desconocida, no editar ni reclamar por antigüedad. status muestra propietario/checkpoint y punteros remoto integral/notas. Leer las notas sobre el SHA y descripciones PR antes de elegir trabajo; un checkpoint parcial no es una validación integral.

Adquisición y actualizaciones normales, sin force: pre-push comprueba el SHA viejo anunciado por el servidor, después Git verifica ese mismo objeto en su transacción atómica. Esto cubre la carrera entre consultar ls-remote y enviar el push. Un hook pre-push existente/configurado sigue ejecutándose con su stdin y argumentos originales.

Para release --publish-notes, preparar primero notas de cierre/evidencia y hacer fetch de refs/notes/spotlight-autonomy por avance normal. Notas y borrado de la reserva propia se publican juntos mediante --atomic. Si notas divergen, NO se libera la reserva; resolver sin force y repetir con la misma propiedad/SHA comprobados. Verificar resultado remoto. Sin --publish-notes solo se borra la reserva propia; el cierre documentado sigue siendo obligatorio.

Pruebas offline con repositorios Git reales: adquisición concurrente entre dos clones, propietario incorrecto, negociación cambiada aun si podría ser fast-forward, SHA viejo, rollback, metadata desconocida, reserva antigua, hooks predeterminados/configurados y notas divergentes. No usa claves, APIs ni repositorios externos.
