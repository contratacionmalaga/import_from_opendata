# Revisión de los 12 documentos existentes en /docs

Fecha de revisión: 2026-09-26. Hecho en código no significa desplegado ni validado hoy. Se conservan las evidencias antiguas con su fecha, sin convertirlas en comprobaciones nuevas.

| Documento original | Estado actual | Evidencia / trabajo restante | Acción |
|---|---|---|---|
| `plan_adecuacion_indices.md` | Parcial, abierto | Snapshots parametrizados implementados; tombstones almacenados pero sin cancelación de estados; vistas UNION ALL y medidas pendientes; DDL operativo no verificado | Mantener con aviso de estado |
| `auditorias/auditoria_java_hibernate_mariadb_2026-05-09.md` | Informe histórico, recomendaciones parcialmente resueltas | HQL libre y carga de entidades completas corregidos; memoria, concurrencia, DDL y medición siguen abiertos | Mantener, marcar sustituido como diagnóstico actual, sin afirmar cierre completo |
| `auditorias/auditoria_viva_proyecto_2026-08-12.md` | Parcial, necesita conciliación | R03/H02 rotación no demostrada; R06/H05 memoria; R09 concurrencia; R10/H07 vistas; R13 retención; documentación mezclada con estado antiguo | Mantener, vincular revisión actual |
| `auditorias/plantilla_validacion_indices.md` | Plantilla reutilizable vigente | Campos sin cumplimentar: no constituye validación ejecutada | Mantener como plantilla |
| `auditorias/migracion_opendata_malaga_schema_2026-08-12.sql` | Hecha históricamente según registro, no revalidada hoy | Encabezado de script e historial 12/08 confirman aplicación; JPA usa columnas destino | Marcar y mover a `done/migraciones/` |
| `auditorias/migracion_historicos_entry_deleted_entry_v7_2_0.sql` | Artefacto parcial, despliegue no acreditado | Entidades nuevas existen; rename solo comentado, unique falla con duplicados, no migra todas las fechas técnicas del modelo | Mantener; completar migración y validar por entorno |
| `auditorias/migracion_deleted_entry_ref_corto_2026-08-19.sql` | Implementada como script; aplicación por entorno sin verificar | Modelo y mapper refCorto existen; no hay evidencia actual de todos los esquemas | Mantener, no marcar ejecutada |
| `releases/v7.2.1.md` | Cambios implementados en código | PropertiesFiles.BD, provider y lanzadores usan bd.properties; despliegue externo no verificado | Marcar completado en código; conservar ruta operativa |
| `releases/v7.2.2.md` | Cambios implementados en código | numEntries y HistoricoTotales corregidos; eliminación de columna en cada BD no verificada | Marcar completado en código; conservar ruta operativa |
| `releases/v7.3.0.md` | Funciones entregadas, objetivo de memoria parcial | Reporte email, runtime y entriesByFeed implementados; mapa global sigue reteniendo grafos (A01), tiempo de persistencia incorrecto (A05) | Marcar parcial; mantener |
| `releases/v7.3.1.md` | Código implementado; cierre de validación pendiente | Workflow construye cuatro perfiles; propio documento deja GitHub Actions pendiente, no comprobado remotamente | Mantener abierto para cierre de evidencia |
| `releases/v7.3.2.md` | Código implementado; cierre de validación pendiente | Plantillas y README presentes; Actions pendiente en documento, no comprobado remotamente | Mantener abierto para cierre de evidencia |

Inventario: cinco releases, un plan, dos auditorías, una plantilla y tres SQL.

## Por qué no se trasladan indiscriminadamente las releases

`.github/workflows/release-package.yml` usa `docs/releases/${TAG}.md` como entrada obligatoria al crear/editar una release. Moverla rompería esa operación o publicaría un mero enlace si se reemplazara por un redirect. Se mantiene su ruta y se marca dentro. `docs/done/README.md` indexa los cambios completados con esta excepción operativa. Así no se modifica código ni procedimientos de publicación como efecto lateral de archivar documentación.

## Conciliación de afirmaciones antiguas

- El estado actual es 7.3.2, no 7.0.0. Hay Maven Wrapper y CI/Quality/Dependency Check separados; el antiguo `maven-ci.yml` ya no existe.
- R05/H04: parametrización resuelta. R06/H05: proyección resuelta; memoria proporcional al histórico todavía no.
- R07/H06: filtro inclusivo implementado y con tests; no reabrir el fallo antiguo como si siguiera igual.
- R08/H08: transacción única implementada; el test de rollback citado ya no aparece en la clase actual. No certificar cobertura actual basándose en un resultado viejo.
- R11: SQL no se registra por defecto; sí vuelve a haber INFO por feed y descarga. R12: System.exit permanece en mains.
- R13: LOCAL ya no carga snapshots ni corta por newest; exige tipo vacío. La frase antigua que dice lo contrario está superada.
- R15/H11: rechazados omitidos y Pliegos sin históricos Entry persistidos, pero la creación/retención transitoria sigue siendo mejorable.
- H02: retirar una clave del POM no prueba que fuera rotada. No se ha consultado el proveedor de secretos.
- Tablas de versiones/CVE de agosto son fotografías históricas; no acreditan ausencia actual de vulnerabilidades ni últimas versiones. No repetir actualizaciones propuestas allí sin resolver dependencias actuales.
- `v7.2.2`: quitar un campo JPA no demuestra que `validate` exija borrar una columna sobrante; la operación DROP debe justificarse por evolución de esquema, no por esa afirmación aislada.
- `v7.3.0`: liberar una lista reduce referencias de esa lista, pero no demuestra liberación del grafo completo. Esta auditoría documenta las referencias restantes.

## Regla futura de archivo

Cerrar cada tarea con fecha, alcance, commit/prueba y entorno cuando aplique. Archivar solo documentos de tareas completamente cerradas; no archivar plantillas, contratos operativos o planes mixtos. Para migraciones, distinguir script redactado, aplicado en un entorno y aplicado en todos los entornos objetivo. Nunca ejecutar de nuevo SQL de `done` por el hecho de estar archivado.
