-- MariaDB. Seleccionar la base de destino y detener el importador antes de ejecutar.
-- Los importes anteriores se convierten a DECIMAL desde su valor almacenado en DOUBLE.
-- Solo el ATOM original permite recuperar su precisión y moneda de origen.
-- Las columnas de moneda de los registros anteriores se dejan en NULL.
ALTER TABLE procurement_project
  MODIFY COLUMN estimated_overall_contract_amount DECIMAL(65, 30) NULL,
  MODIFY COLUMN total_amount DECIMAL(65, 30) NULL,
  MODIFY COLUMN tax_exclusive_amount DECIMAL(65, 30) NULL,
  ADD COLUMN IF NOT EXISTS estimated_overall_contract_amount_currency VARCHAR(3) NULL,
  ADD COLUMN IF NOT EXISTS total_amount_currency VARCHAR(3) NULL,
  ADD COLUMN IF NOT EXISTS tax_exclusive_amount_currency VARCHAR(3) NULL;

ALTER TABLE procurement_project_lot
  MODIFY COLUMN total_amount DECIMAL(65, 30) NULL,
  MODIFY COLUMN tax_exclusive_amount DECIMAL(65, 30) NULL,
  ADD COLUMN IF NOT EXISTS total_amount_currency VARCHAR(3) NULL,
  ADD COLUMN IF NOT EXISTS tax_exclusive_amount_currency VARCHAR(3) NULL;
