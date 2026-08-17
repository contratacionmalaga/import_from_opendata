# Plantilla de validacion de indices

Fecha:
Entorno:
Servidor MariaDB:
Esquema:
Responsable:

## Preparacion

- Confirmar backup o snapshot disponible.
- Ejecutar en ventana de mantenimiento.
- Confirmar version de MariaDB.
- Confirmar numero aproximado de filas en tablas principales.
- Guardar resultado antes y despues en esta plantilla.

## Comandos base

```sql
SELECT VERSION();

SELECT table_name, table_rows
FROM information_schema.tables
WHERE table_schema = '<schema>'
  AND table_name IN (
    'log',
    'feed',
    'entry',
    'deleted_entry',
    'historico',
    'contract_folder_status',
    'document_reference'
  )
ORDER BY table_name;
```

## Antes de indices

```sql
EXPLAIN SELECT e.entry_id, e.updated
FROM entry e
JOIN feed f ON f.id = e.feed_id
JOIN log l ON l.id = f.log_id
WHERE l.tipo_sindicacion = '<tipo_sindicacion>';
```

Resultado:

| Tabla | type | possible_keys | key | rows | Extra |
|---|---|---|---|---:|---|
| | | | | | |

Tiempo real:

```sql
SELECT COUNT(*)
FROM entry e
JOIN feed f ON f.id = e.feed_id
JOIN log l ON l.id = f.log_id
WHERE l.tipo_sindicacion = '<tipo_sindicacion>';
```

Duracion:

## Ejecucion de indices

```sql
SOURCE opendata-malaga-indices.sql;
SOURCE opendata-pliegos-indices.sql;
```

Ejecutar solo el script del esquema correspondiente.

## Despues de indices

Repetir `EXPLAIN` y `COUNT`.

Resultado:

| Tabla | type | possible_keys | key | rows | Extra |
|---|---|---|---|---:|---|
| | | | | | |

Duracion:

## Criterio de aceptacion

- La query de snapshots usa indices en `log`, `feed` y `entry`.
- No aparece full scan grande evitable en `entry`.
- Las filas estimadas bajan o la duracion real mejora.
- `ANALYZE TABLE` finaliza sin errores.

## Observaciones

- Si `entry` sigue en `ALL`, revisar cardinalidad y `ANALYZE TABLE`.
- Si las vistas de documentos siguen mostrando `Range checked for each record`, planificar reescritura con `UNION ALL`.
