# Plan de adecuacion de indices y codigo Java

Fecha: 2026-05-09

## Alcance ejecutado

Se trabajo contra MariaDB local usando el cliente `C:\Program Files\MariaDB 12.2\bin\mysql.exe` con SSL desactivado. Se inspeccionaron los esquemas:

- `opendata-malaga`: 33 tablas base, 24 vistas.
- `opendata-pliegos`: 33 tablas base, 14 vistas.

Se revisaron indices existentes mediante `information_schema.STATISTICS` y se ejecuto:

```sql
EXPLAIN SELECT * FROM `schema`.`vista`;
```

para cada vista de ambos esquemas.

Artefactos generados:

- `opendata-malaga-indices.sql`
- `opendata-pliegos-indices.sql`
- `docs/plan_adecuacion_indices.md`

## Hallazgos principales de EXPLAIN

1. Muchas vistas devuelven el conjunto completo y por tanto es normal que alguna tabla conductora aparezca con `type=ALL`. Un indice no elimina un full scan si la vista no tiene `WHERE` selectivo.
2. Las vistas de documentos son el mayor problema estructural:
   - `malaga_rel_expedientes_documentos`
   - `pliegos_rel_expedientes_documentos`
   Ambas hacen join con `OR` sobre `document_reference.additional_document_reference_id`, `general_document_reference_id`, `legal_document_reference_id` y `technical_document_reference_id`. MariaDB reporta `Range checked for each record`; los indices ayudan, pero la solucion fuerte es reescribir esas vistas con `UNION ALL`.
3. En `opendata-pliegos` hay tablas muy grandes:
   - `document_reference`: ~5,5M filas.
   - `additional_publication_document_reference`: ~3,6M.
   - `notice_info`: ~2,5M.
   - `commodity_classification`: ~2,0M.
   - `awarding_criteria`: ~1,8M.
   - `entry`: ~743k.
4. En `opendata-malaga`, la tabla mas grande es `historico` con ~4,7M filas, aunque las vistas revisadas no la usan directamente.
5. Se detecto una anomalia a revisar: durante `EXPLAIN` de `opendata-malaga.malaga_rel_expedientes_cpvs_v2` y `malaga_rel_expedientes_cpvs_v3` aparecieron referencias a tablas de `opendata-pliegos`. Conviene confirmar la definicion de esas vistas; podria ser una copia cruzada no intencionada.

## Indices definidos

Los scripts generados usan `CREATE INDEX IF NOT EXISTS` y separan los indices por objetivo:

- carga incremental Java;
- vistas de expedientes/adjudicaciones;
- vistas de criterios y condiciones especiales;
- vistas de anuncios/documentos;
- consultas preliminares de mercado;
- modificaciones.

Tambien incluyen `ANALYZE TABLE` al final para refrescar estadisticas del optimizador tras crear indices.

## Indices que ya existian

### opendata-malaga

Ya existian indices relevantes en:

- `entry(entry_id)`, `entry(entry_id_corto)`, `entry(feed_id)`, `entry(updated)`.
- `contract_folder_status(entry_id)`, `id_plataforma`, `nif`, `contract_folder_id`, `contract_folder_status_code`.
- `tender_result(contract_folder_status_id)`, `(contract_folder_status_id, procurement_project_lot_id, result_code)`, `(result_code, nif, contract_folder_status_id)`, `(nif, award_date)`.
- `procurement_project(contract_folder_status_id)`, `preliminary_market_consultation_status_id`, `type_code`, `total_amount`.
- `tendering_process(contract_folder_status_id)`, `(procedure_code, contract_folder_status_id)`.
- `commodity_classification(procurement_project_id, item_classification_code)` y `(procurement_project_lot_id, item_classification_code)`.
- `document_reference(general_document_reference_id, document_type_code)` y `(additional_document_reference_id, document_type_code)`.

### opendata-pliegos

Ya existian indices relevantes en:

- `entry(entry_id)`, `entry(entry_id_corto)`, `entry(feed_id)`.
- `contract_folder_status(entry_id)`, `id_plataforma`, `party_name`.
- `tender_result(contract_folder_status_id)`, `(contract_folder_status_id, result_code, procurement_project_lot_id)`, `(result_code, nif, contract_folder_status_id)`, `(nif, award_date)`.
- `procurement_project(contract_folder_status_id)`.
- `tendering_terms(contract_folder_status_id)`, `procurement_project_lot_id`.
- `tendering_process(contract_folder_status_id)`, `(procedure_code, contract_folder_status_id)`.
- `commodity_classification(procurement_project_id, item_classification_code)` y `(procurement_project_lot_id, item_classification_code)`.
- `awarding_criteria(tendering_terms_id, awarding_criteria_type_code, awarding_criteria_subtype_code)`.
- `document_reference(general_document_reference_id, document_type_code)` y `(additional_document_reference_id, document_type_code)`.

## Plan de trabajo para adaptar Java

### 1. Parametrizar y aligerar la carga de entradas existentes

Objetivo: que el codigo use los indices `log(tipo_sindicacion, id)`, `feed(log_id, id)`, `entry(feed_id, updated)` y los unique de `entry_id`.

Trabajo:

1. Cambiar `Repository.getMapEntries(String sql)` para que no acepte HQL libre.
2. Crear un metodo especifico, por ejemplo:

```java
Map<String, EntrySnapshot> getEntrySnapshots(TipoSindicacion tipoSindicacion);
```

3. Usar query parametrizada:

```sql
SELECT e.entryId, e.updated, e.id
FROM Entry e
JOIN e.feed f
JOIN f.miLog l
WHERE l.tipoSindicacion = :tipo
```

4. Evitar cargar `Entry` completa para comparar versiones.

Impacto esperado:

- menos memoria;
- menos lazy loading accidental;
- mejor uso de indices;
- menor tiempo de arranque de importaciones sucesivas.

### 2. Incorporar `deleted_entry` al flujo funcional

Objetivo: usar `deleted_entry(ref)` y `deleted_entry(feed_id, ref)` para procesar cancelaciones.

Trabajo:

1. Confirmar que `DeletedEntry.ref` coincide con `Entry.entryId`.
2. Anadir un paso de pipeline despues del parseo y antes de persistir.
3. Resolver cada `DeletedEntry.ref` contra:
   - entries parseadas en memoria;
   - snapshots cargados desde BD.
4. Si existe en memoria, marcar el `ContractFolderStatus` o `PreliminaryMarketConsultationStatus` antes del persist.
5. Si existe solo en BD, ejecutar update por `entry_id`.
6. Registrar historico de cancelacion.

Pendiente de negocio:

- valor exacto de estado cancelado en `contract_folder_status_code`;
- valor exacto de estado cancelado en `preliminary_market_consultation_status_code`;
- si una entrada cancelada puede reactivarse por una version posterior.

### 3. Evitar que Hibernate recree indices manuales

Objetivo: que los indices SQL creados no se pierdan.

Trabajo:

1. En produccion usar `hibernate.hbm2ddl.auto=validate`.
2. Mantener scripts SQL de indices fuera del DDL automatico de Hibernate.
3. Si se decide reflejar algunos indices en entidades con `@Index`, hacerlo solo para los estructurales:
   - `Entry(feed_id, updated)`;
   - `DeletedEntry(ref)`;
   - `ContractFolderStatus(entry_id, contract_folder_status_code)`;
   - `ProcurementProjectLot(contract_folder_status_id, lote)`;
   - `DocumentReference(..., document_type_code)`.

No reflejar todos los indices analiticos en JPA si son exclusivos de vistas/reporting.

### 4. Reescribir vistas de documentos

Objetivo: eliminar `Range checked for each record`.

Trabajo recomendado para las vistas:

- `malaga_rel_expedientes_documentos`
- `pliegos_rel_expedientes_documentos`

Reescribir de:

```sql
JOIN contract_folder_status cfs
  ON cfs.id = dr.additional_document_reference_id
  OR cfs.id = dr.general_document_reference_id
  OR cfs.id = dr.legal_document_reference_id
  OR cfs.id = dr.technical_document_reference_id
```

a cuatro ramas `UNION ALL`, una por columna FK. Asi MariaDB puede usar directamente:

- `idx_*_docref_additional_type`
- `idx_*_docref_general_type`
- `idx_*_docref_legal_type`
- `idx_*_docref_technical_type`

### 5. Validacion tras ejecutar scripts

Ejecutar en cada servidor:

```sql
SOURCE opendata-malaga-indices.sql;
SOURCE opendata-pliegos-indices.sql;
```

Despues repetir:

```sql
EXPLAIN SELECT * FROM `opendata-malaga`.`malaga_adjudicaciones`;
EXPLAIN SELECT * FROM `opendata-malaga`.`malaga_rel_expedientes_documentos`;
EXPLAIN SELECT * FROM `opendata-pliegos`.`pliegos_adjudicaciones`;
EXPLAIN SELECT * FROM `opendata-pliegos`.`pliegos_rel_expedientes_documentos`;
```

Comparar:

- reduccion de `ALL` sobre tablas grandes;
- desaparicion o reduccion de `Range checked for each record`;
- menor uso de `Using temporary; Using filesort` cuando se filtre desde consumidor;
- `rows` estimadas mas bajas.

### 6. Orden recomendado

1. Ejecutar scripts de indices en preproduccion.
2. Repetir EXPLAIN y medir tiempos reales de vistas.
3. Reescribir vistas de documentos con `UNION ALL`.
4. Cambiar Java a snapshots ligeros.
5. Procesar `deleted_entry`.
6. Cambiar `hbm2ddl.auto` a `validate` en produccion.
7. Mantener los indices analiticos en scripts SQL versionados, no dependientes de Hibernate.

