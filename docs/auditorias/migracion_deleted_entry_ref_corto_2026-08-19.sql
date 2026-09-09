-- Migracion para alinear deleted_entry con el nuevo campo DeletedEntry.refCorto.
-- Ejecutar en cada esquema existente antes de arrancar con hibernate.hbm2ddl.auto=validate.

ALTER TABLE `deleted_entry`
  ADD COLUMN IF NOT EXISTS `ref_corto` varchar(50) NULL AFTER `ref`;

UPDATE `deleted_entry`
SET `ref_corto` = LEFT(SUBSTRING_INDEX(`ref`, '/', -1), 50)
WHERE `ref_corto` IS NULL
  AND `ref` IS NOT NULL;