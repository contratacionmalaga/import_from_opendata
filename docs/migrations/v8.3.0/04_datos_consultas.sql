-- MariaDB. Seleccionar la base de destino y detener el importador antes de ejecutar.
-- El orden de los arrays JSON corresponde al orden de origen.
-- La jerarquía empieza por el órgano padre inmediato y termina en la raíz.
-- Para las consultas ya cargadas, la información se recupera reprocesando el ATOM de origen.
ALTER TABLE preliminary_market_consultation_status
  ADD COLUMN IF NOT EXISTS parent_located_parties JSON NULL,
  ADD COLUMN IF NOT EXISTS postal_address_lines JSON NULL,
  ADD COLUMN IF NOT EXISTS postal_city_name TEXT NULL,
  ADD COLUMN IF NOT EXISTS postal_zone VARCHAR(50) NULL,
  ADD COLUMN IF NOT EXISTS postal_country_code VARCHAR(50) NULL,
  ADD COLUMN IF NOT EXISTS postal_country_name TEXT NULL,
  ADD COLUMN IF NOT EXISTS languages JSON NULL,
  ADD COLUMN IF NOT EXISTS submission_uri TEXT NULL;
