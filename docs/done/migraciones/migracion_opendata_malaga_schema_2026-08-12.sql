-- HECHO (evidencia histórica 2026-08-12). Archivado 2026-09-26; no reejecutado ni revalidado contra BD hoy.
-- Migracion aplicada durante auditoria 2026-08-12 para alinear opendata-malaga con el modelo JPA actual.
-- No borra datos: renombra columnas existentes antiguas a los nombres actuales usados por Hibernate.
-- Ejecutar solo si existen las columnas origen y no existen las columnas destino.

USE `opendata-malaga`;

ALTER TABLE `tender_result`
  RENAME COLUMN `countryName` TO `country_name`;

ALTER TABLE `tender_result`
  RENAME COLUMN `countryIdentificationCode` TO `country_identification_code`;

ALTER TABLE `tender_result`
  RENAME COLUMN `rate` TO `subcontract_terms_rate`;

ALTER TABLE `tendering_terms`
  RENAME COLUMN `rate` TO `subcontract_terms_rate`;