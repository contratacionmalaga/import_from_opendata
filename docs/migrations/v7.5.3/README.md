# Migración de Fase 1: plazos de licitación

La migración añade `tendering_process.participation_request_reception_period`.

`DocumentAvailabilityPeriod` no requiere una nueva columna: la corrección usa la columna existente
`document_availability_period` y cambia únicamente su origen dentro del ATOM.

## Aplicación

1. Realizar copia de seguridad de la base de datos.
2. Ejecutar `01_add_participation_request_reception_period.sql` una única vez.
3. Desplegar el ejecutable que contiene el mapper corregido.
4. Para corregir los valores históricos de `document_availability_period`, volver a importar los
   ATOM afectados; las importaciones incrementales sustituyen el `Entry` y su grafo completo.
