-- MariaDB. Seleccionar la base de destino antes de ejecutar.
-- Aplicar después de la migración del histórico Atom, con el importador detenido.
-- Los ID y las fechas ya almacenados se conservan. Los offsets históricos quedan sin informar.
-- Para recuperar un ID recortado o un offset histórico exacto se necesita el ATOM original.
ALTER TABLE entry
  MODIFY entry_id VARCHAR(2500) CHARACTER SET utf8mb4 COLLATE utf8mb4_nopad_bin NOT NULL,
  ADD COLUMN IF NOT EXISTS entry_atom_key VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
  ADD COLUMN IF NOT EXISTS updated_offset_seconds INT NULL;

UPDATE entry SET entry_atom_key = SHA2(CONVERT(entry_id USING utf8mb4), 256)
WHERE entry_atom_key IS NULL;

ALTER TABLE entry
  MODIFY entry_atom_key VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  ADD UNIQUE INDEX IF NOT EXISTS uk_entry_atom_key (entry_atom_key),
  ADD INDEX IF NOT EXISTS idx_entry_id_corto (entry_id_corto);

-- Las instalaciones antiguas pueden usar nombres de índice generados por Hibernate.
SELECT GROUP_CONCAT(CONCAT('DROP INDEX `', REPLACE(index_name, '`', '``'), '`') SEPARATOR ', ')
  INTO @atom_old_entry_indexes
FROM (
  SELECT index_name
  FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'entry'
    AND non_unique = 0 AND index_name <> 'PRIMARY'
  GROUP BY index_name
  HAVING COUNT(*) = 1 AND MAX(column_name) IN ('entry_id', 'entry_id_corto')
) AS old_indexes;

SET @atom_sql = IF(@atom_old_entry_indexes IS NULL, 'DO 0',
                   CONCAT('ALTER TABLE entry ', @atom_old_entry_indexes));
EXECUTE IMMEDIATE @atom_sql;

ALTER TABLE historico_entry
  MODIFY entry_id VARCHAR(2500) CHARACTER SET utf8mb4 COLLATE utf8mb4_nopad_bin NULL,
  ADD COLUMN IF NOT EXISTS entry_updated_offset_seconds INT NULL;

ALTER TABLE deleted_entry
  MODIFY ref VARCHAR(2500) CHARACTER SET utf8mb4 COLLATE utf8mb4_nopad_bin NOT NULL,
  ADD COLUMN IF NOT EXISTS ref_atom_key VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
  ADD COLUMN IF NOT EXISTS updated_offset_seconds INT NULL;

UPDATE deleted_entry SET ref_atom_key = SHA2(CONVERT(ref USING utf8mb4), 256)
WHERE ref_atom_key IS NULL;

ALTER TABLE deleted_entry
  MODIFY ref_atom_key VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  ADD UNIQUE INDEX IF NOT EXISTS uk_deleted_entry_atom_key (ref_atom_key);

SELECT GROUP_CONCAT(CONCAT('DROP INDEX `', REPLACE(index_name, '`', '``'), '`') SEPARATOR ', ')
  INTO @atom_old_ref_indexes
FROM (
  SELECT index_name
  FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'deleted_entry'
    AND non_unique = 0 AND index_name <> 'PRIMARY'
  GROUP BY index_name
  HAVING COUNT(*) = 1 AND MAX(column_name) = 'ref'
) AS old_indexes;

SET @atom_sql = IF(@atom_old_ref_indexes IS NULL, 'DO 0',
                   CONCAT('ALTER TABLE deleted_entry ', @atom_old_ref_indexes));
EXECUTE IMMEDIATE @atom_sql;

ALTER TABLE historico_deleted_entry
  MODIFY ref VARCHAR(2500) CHARACTER SET utf8mb4 COLLATE utf8mb4_nopad_bin NULL,
  ADD COLUMN IF NOT EXISTS previous_updated_offset_seconds INT NULL,
  ADD COLUMN IF NOT EXISTS incoming_updated_offset_seconds INT NULL;
