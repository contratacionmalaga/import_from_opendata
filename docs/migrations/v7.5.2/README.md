# Migración 7.5.2: auditoría temporal solo en Entry

Esta migración elimina `created_at` y `updated_at` de las entidades distintas de `entry`.
La tabla `entry` conserva ambas columnas: `created_at` representa la primera importación y
`updated_at` el último reemplazo detectado desde Internet.

## Ejecución

1. Realizar copia de seguridad y ejecutar `01_precheck.sql` en la base de datos objetivo.
2. Confirmar que `entry` conserva sus dos columnas y que las 32 tablas indicadas contienen ambas.
3. Ejecutar `02_drop_child_audit_columns.sql` durante una ventana de mantenimiento.
4. Arrancar la aplicación con `hibernate.hbm2ddl.auto=validate` y ejecutar una importación de prueba.

La aplicación ya no mapea esas columnas en las entidades afectadas. No ejecute la migración con
`hibernate.hbm2ddl.auto=create` sobre datos que deban conservarse.
