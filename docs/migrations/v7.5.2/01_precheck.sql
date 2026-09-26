-- Ejecutar conectado a la base de datos objetivo.
-- Debe devolver dos columnas de auditoría para entry y para las tablas que se van a migrar.
SELECT table_name, column_name, column_type, is_nullable
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND column_name IN ('created_at', 'updated_at')
ORDER BY table_name, column_name;

-- Debe devolver exactamente dos filas: created_at y updated_at.
SELECT table_name, column_name
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'entry'
  AND column_name IN ('created_at', 'updated_at')
ORDER BY column_name;
