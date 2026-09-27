# Migración 8.0.2: `log.created_at`

La tabla `log` vuelve a incorporar `created_at`. Este campo registra la fecha y hora técnica en que
la aplicación persiste el registro de una importación. No se incorpora `updated_at`: un log es un
registro histórico de una ejecución y no debe cambiar tras su creación.

`fecha_generacion` se mantiene sin cambios porque representa la fecha funcional comunicada por el
origen, que puede ser distinta de la fecha de persistencia.

## Aplicación

1. Realizar una copia de seguridad de la base de datos.
2. Ejecutar `01_add_log_created_at.sql` una única vez.
3. Desplegar la versión que incorpora el nuevo mapeo.
4. Arrancar con `hibernate.hbm2ddl.auto=validate` y ejecutar una importación de prueba.

Los logs existentes reciben `fecha_generacion` cuando esté disponible; los restantes reciben el
instante de ejecución de la migración.
