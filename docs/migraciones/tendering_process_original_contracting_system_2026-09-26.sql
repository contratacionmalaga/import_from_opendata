-- Campos del sistema de contratación original en tendering_process.
-- Ejecutar una sola vez por esquema, tras validar backup, espacio y ventana de mantenimiento.
-- No usar hibernate.hbm2ddl.auto=create para aplicar este cambio.

ALTER TABLE `tendering_process`
  ADD COLUMN IF NOT EXISTS `original_contracting_system_id` VARCHAR(50) NULL,
  ADD COLUMN IF NOT EXISTS `original_contracting_system_description` TEXT NULL,
  ADD COLUMN IF NOT EXISTS `original_contracting_system_lot_id` VARCHAR(50) NULL,
  ADD COLUMN IF NOT EXISTS `original_contracting_system_lot_description` TEXT NULL;

-- Verificación posterior: deben aparecer exactamente las cuatro columnas y sus tipos.
SELECT column_name, column_type, is_nullable
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'tendering_process'
  AND column_name IN (
    'original_contracting_system_id',
    'original_contracting_system_description',
    'original_contracting_system_lot_id',
    'original_contracting_system_lot_description'
  )
ORDER BY column_name;
