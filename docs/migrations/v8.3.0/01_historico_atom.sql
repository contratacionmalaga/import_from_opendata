-- Ejecutar con la base de destino seleccionada. En pruebas: prueba-opendata.
ALTER TABLE entry
  ADD COLUMN IF NOT EXISTS xml LONGTEXT NULL;

ALTER TABLE historico_entry
  ADD COLUMN IF NOT EXISTS xml LONGTEXT NULL,
  ADD COLUMN IF NOT EXISTS summary_expediente TEXT NULL,
  ADD COLUMN IF NOT EXISTS summary_organo_contratacion TEXT NULL,
  ADD COLUMN IF NOT EXISTS summary_importe TEXT NULL,
  ADD COLUMN IF NOT EXISTS summary_estado TEXT NULL;
