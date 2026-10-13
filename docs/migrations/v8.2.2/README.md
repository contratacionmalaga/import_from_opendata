# Ajuste de vistas de Málaga

`malaga-views.sql` elimina únicamente estas columnas de salida y sus referencias:

| Vista | Columnas eliminadas | Referencias eliminadas |
|---|---|---|
| `expedientes` | `feed_updated_at`, `log_updated_at` | `feed.updated_at`, `log.updated_at` |
| `vista_expedientes_importacion` | `created_at`, `updated_at` | `historico_entry.created_at`, `historico_entry.updated_at` |

Conserva el resto de la definición, las uniones, el definer y SQL SECURITY.
No añade columnas al modelo Java ni modifica tablas.

Se aplicó y verificó en `opendata_malaga:3306` el 13/10/2026: las 26 vistas admiten consultas.
Los otros esquemas y el puerto 3307 no se han modificado.
`malaga-views-rollback.sql` guarda las definiciones anteriores. Reponerlas requiere volver
a disponer de las columnas de auditoría que referencian.

Para otra instalación compatible, revisar el SQL antes de aplicarlo con su conexión MariaDB:

```powershell
mariadb --host=localhost --port=3306 --user=root --password --execute="SOURCE C:/ruta/malaga-views.sql"
```
