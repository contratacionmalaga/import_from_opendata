# Auditoría de importación ATOM y persistencia — 26 de septiembre de 2026

Estado: auditoría estática y documental finalizada; propuestas pendientes de selección e implementación. La validación de rendimiento en MariaDB con 875.000 expedientes queda pendiente del experimento descrito aquí. No se ha ejecutado una importación ni modificado bases de datos.

Base: versión 7.3.2, commit `2b9da0436da225ac1e58b32516462eec3ebc11c5`, incluyendo los cambios locales preexistentes en `FeedHelper.java` y `AbstractOpenDataPliegos.java`, que se han conservado.

## Documentos

- [Plan inicial detallado, con los cuatro campos prioritarios de tendering_process](../../plan_inicial_implementacion_2026-09-26.md): secuencia hasta iniciar la campaña de pruebas.
- [Diagnóstico técnico y hallazgos](01-diagnostico.md).
- [Propuestas priorizadas y decisiones de arquitectura](02-propuestas.md).
- [Protocolo de medición y aceptación](03-validacion.md).
- [Estado de todos los documentos anteriores](04-estado-docs.md).
- [Resultado real de compilación y tests](evidencias/maven-test.log).

## Conclusión principal

875.000 expedientes no equivalen a 875.000 INSERT: cada expediente incorpora un grafo CODICE con documentos, anuncios, lotes, criterios y otras filas. El diseño actual retiene todos los grafos seleccionados antes de escribirlos, mantiene una única transacción y no mide correctamente su duración. Son causas estructurales plausibles de horas de ejecución; falta medir qué parte corresponde a GC, Hibernate, JDBC, disco o servidor para atribuir porcentajes.

Hay mejoras previas útiles: snapshots ligeros, consultas parametrizadas, UUID en lugar de identificadores IDENTITY, ordenación de inserts en la plantilla, `flush/clear`, deduplicación y eliminación de históricos persistidos en Pliegos. No procede presentar su incorporación como trabajo nuevo.

El intento actual de `scripts/use-java21-maven3916.ps1 test` termina en **BUILD FAILURE**, antes de ejecutar tests: `FeedHelper.java:120` y `:121` pasan `long` a `StringHelper.getNumeroConFormato(int)`. Es un cambio local anterior a esta auditoría. No se ha corregido porque esta entrega prepara decisiones, no implementa cambios de aplicación.

## Alcance y límites

Revisión del flujo de las cuatro variantes, repositorio/servicio, contexto y pipeline, parsers, mappers ATOM y estructura de mappers CODICE, relaciones JPA, filtros, configuración, lanzadores, CI, pruebas y los 12 documentos existentes en `docs`. Revisión focal de métodos críticos y búsquedas transversales; no es una certificación funcional campo a campo de todo CODICE.

Se han contrastado logs locales históricos y documentación oficial. No se ha hecho un benchmark nuevo, un heap dump, una captura SQL de producción, un escaneo CVE actualizado ni una comprobación remota de GitHub Actions. Las properties de ejemplo no demuestran la configuración del servidor donde se observa la lentitud. Las evidencias antiguas se identifican como históricas, no como pruebas ejecutadas hoy.
