-- Migración 8.0.2: fecha técnica de creación del registro de importación.
-- fecha_generacion conserva la fecha funcional informada por el origen.

ALTER TABLE log ADD COLUMN created_at DATETIME(6) NULL;

UPDATE log
SET created_at = COALESCE(fecha_generacion, CURRENT_TIMESTAMP(6))
WHERE created_at IS NULL;

ALTER TABLE log MODIFY COLUMN created_at DATETIME(6) NOT NULL;
