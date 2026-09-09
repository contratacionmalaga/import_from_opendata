-- Cambios de esquema v7.2.0 para nueva carga completa.
-- Recomendado: recrear el esquema y arrancar con hibernate.hbm2ddl.auto=create o update controlado,
-- validar despues con hibernate.hbm2ddl.auto=validate.

-- Semantica de fechas:
--   updated    = fecha funcional del ATOM/PLACSP.
--   created_at = primera aparicion conservada por clave funcional.
--   updated_at = ultima escritura tecnica de la fila.

-- La entidad HistoricoEntry pasa de historico a historico_entry.
-- Si se migra un esquema existente en lugar de recrearlo:
-- RENAME TABLE `historico` TO `historico_entry`;

-- deleted_entry pasa a estado unico por ref.
ALTER TABLE `deleted_entry`
  ADD UNIQUE KEY `uk_deleted_entry_ref` (`ref`);

-- Historico de decisiones sobre tombstones.
CREATE TABLE IF NOT EXISTS `historico_deleted_entry` (
  `id` uuid NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `feed_linkself` varchar(500) DEFAULT NULL,
  `ref` varchar(500) DEFAULT NULL,
  `ref_corto` varchar(50) DEFAULT NULL,
  `previous_updated` datetime(6) DEFAULT NULL,
  `incoming_updated` datetime(6) DEFAULT NULL,
  `opcion` varchar(50) DEFAULT NULL,
  `motivo` text DEFAULT NULL,
  `deleted_entry_id` uuid DEFAULT NULL,
  `log_id` uuid NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_hde_log` (`log_id`),
  KEY `idx_hde_ref` (`ref`),
  KEY `idx_hde_opcion` (`opcion`),
  CONSTRAINT `fk_historicodeletedentry_deletedentry`
    FOREIGN KEY (`deleted_entry_id`) REFERENCES `deleted_entry` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_historicodeletedentry_milog`
    FOREIGN KEY (`log_id`) REFERENCES `log` (`id`) ON DELETE CASCADE
);

-- Validaciones posteriores a la carga.
SELECT COUNT(*) AS total, COUNT(DISTINCT `entry_id`) AS distintos
FROM `entry`;

SELECT `entry_id`, COUNT(*) AS total
FROM `entry`
GROUP BY `entry_id`
HAVING COUNT(*) > 1;

SELECT COUNT(*) AS total, COUNT(DISTINCT `ref`) AS distintos
FROM `deleted_entry`;

SELECT `ref`, COUNT(*) AS total
FROM `deleted_entry`
GROUP BY `ref`
HAVING COUNT(*) > 1;

SELECT `opcion`, COUNT(*) AS total
FROM `historico_deleted_entry`
GROUP BY `opcion`;
