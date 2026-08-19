-- Indices para opendata-pliegos
-- Generado desde EXPLAIN de las vistas de opendata-pliegos el 2026-05-09.
-- Objetivo: mejorar joins/filtros de vistas y preparar consultas Java de carga incremental.
-- Ejecutar en ventana de mantenimiento. CREATE INDEX hace commits implicitos.
-- Si el servidor no soporta IF NOT EXISTS, revisar INFORMATION_SCHEMA.STATISTICS antes de lanzar.

USE `opendata-pliegos`;

-- ============================================================
-- Indices existentes relevantes detectados
-- ============================================================
-- entry: PRIMARY(id), idx_unique_entry(entry_id), UKgufr4707xcb74rk0otnr6x1dh(entry_id_corto),
--        fk_entry_feed(feed_id)
-- contract_folder_status: PRIMARY(id), fk_contractfolderstatus_entry(entry_id),
--        cfs_id_plataforma(id_plataforma), cfs_party_name(party_name)
-- tender_result: tenderresult_cfs(contract_folder_status_id),
--        idx_plg_tr_cfs_result_lot(contract_folder_status_id, result_code, procurement_project_lot_id),
--        idx_plg_tr_result_nif_cfs(result_code, nif, contract_folder_status_id),
--        idx_plg_tr_nif_award(nif, award_date), idx_plg_tr_award_result(award_date, result_code)
-- procurement_project: UKlshrflt7p02u7b0je7rncb13n(contract_folder_status_id)
-- tendering_terms: UK5tdskranwt92bsd1bd9pr08oj(contract_folder_status_id),
--        UK23gchov7dvye4vluxayj5qknm(procurement_project_lot_id)
-- tendering_process: UKmv9v3b8q2mwk6s2wxr7nygaoc(contract_folder_status_id),
--        idx_plg_tp_procedure_cfs(procedure_code, contract_folder_status_id)
-- commodity_classification: idx_plg_cc_project_code(procurement_project_id, item_classification_code),
--        idx_plg_cc_lot_code(procurement_project_lot_id, item_classification_code)
-- awarding_criteria: idx_plg_ac_terms_type_subtype(tendering_terms_id, awarding_criteria_type_code, awarding_criteria_subtype_code),
--        idx_plg_ac_type_subtype_weight(awarding_criteria_type_code, awarding_criteria_subtype_code, weight_numeric)
-- document_reference: idx_plg_docref_general_type(general_document_reference_id, document_type_code),
--        idx_plg_docref_additional_type(additional_document_reference_id, document_type_code)

-- ============================================================
-- Importacion Java / comparacion incremental
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_plg_log_tipo_id
  ON `log` (`tipo_sindicacion`, `id`);

CREATE INDEX IF NOT EXISTS idx_plg_feed_log_id
  ON `feed` (`log_id`, `id`);

CREATE INDEX IF NOT EXISTS idx_plg_entry_updated
  ON `entry` (`updated`);

CREATE INDEX IF NOT EXISTS idx_plg_entry_feed_updated
  ON `entry` (`feed_id`, `updated`);

CREATE UNIQUE INDEX IF NOT EXISTS uk_plg_deleted_ref
  ON `deleted_entry` (`ref`);

CREATE INDEX IF NOT EXISTS idx_plg_deleted_feed_ref
  ON `deleted_entry` (`feed_id`, `ref`);

CREATE INDEX IF NOT EXISTS idx_plg_hde_log
  ON `historico_deleted_entry` (`log_id`);

CREATE INDEX IF NOT EXISTS idx_plg_hde_ref
  ON `historico_deleted_entry` (`ref`);

CREATE INDEX IF NOT EXISTS idx_plg_hde_opcion
  ON `historico_deleted_entry` (`opcion`);

-- ============================================================
-- Vistas principales de expedientes/adjudicaciones
-- EXPLAIN afectado: pliegos_adjudicaciones, pliegos_cmayores,
-- pliegos_rel_expedientes_adjudicaciones.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_plg_cfs_contract_folder_id
  ON `contract_folder_status` (`contract_folder_id`);

CREATE INDEX IF NOT EXISTS idx_plg_cfs_status
  ON `contract_folder_status` (`contract_folder_status_code`);

CREATE INDEX IF NOT EXISTS idx_plg_cfs_nif
  ON `contract_folder_status` (`nif`);

CREATE INDEX IF NOT EXISTS idx_plg_cfs_entry_status
  ON `contract_folder_status` (`entry_id`, `contract_folder_status_code`);

CREATE INDEX IF NOT EXISTS idx_plg_cfs_idplat_status
  ON `contract_folder_status` (`id_plataforma`, `contract_folder_status_code`);

CREATE INDEX IF NOT EXISTS idx_plg_pp_type_cfs
  ON `procurement_project` (`type_code`, `contract_folder_status_id`);

CREATE INDEX IF NOT EXISTS idx_plg_pp_total_cfs
  ON `procurement_project` (`total_amount`, `contract_folder_status_id`);

CREATE INDEX IF NOT EXISTS idx_plg_ppl_cfs_lote
  ON `procurement_project_lot` (`contract_folder_status_id`, `lote`);

CREATE INDEX IF NOT EXISTS idx_plg_ppl_cfs_total
  ON `procurement_project_lot` (`contract_folder_status_id`, `total_amount`);

-- ============================================================
-- Vistas de condiciones especiales, criterios y garantias
-- EXPLAIN afectado: pliegos_rel_expedientes_conds_esp_adj,
-- pliegos_rel_expedientes_criterios_adjs,
-- pliegos_rel_expedientes_criterios_adjs_v2.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_plg_cer_terms_code
  ON `contract_execution_requirement` (`tendering_terms_id`, `execution_requirement_code`);

CREATE INDEX IF NOT EXISTS idx_plg_fg_terms
  ON `financial_guarantee` (`tendering_terms_id`);

-- ============================================================
-- Vistas de anuncios/documentos
-- EXPLAIN afectado: pliegos_rel_expedientes_anuncios,
-- pliegos_rel_expedientes_documentos.
-- Nota: pliegos_rel_expedientes_documentos usa JOIN con OR sobre 4 columnas;
-- estos indices ayudan, pero la mejora fuerte requiere reescribir la vista con UNION ALL.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_plg_docref_technical_type
  ON `document_reference` (`technical_document_reference_id`, `document_type_code`);

CREATE INDEX IF NOT EXISTS idx_plg_docref_legal_type
  ON `document_reference` (`legal_document_reference_id`, `document_type_code`);

CREATE INDEX IF NOT EXISTS idx_plg_docref_pmcs_type
  ON `document_reference` (`pmcs_general_document_reference_id`, `document_type_code`);

-- ============================================================
-- Modificaciones
-- EXPLAIN afectado: pliegos_rel_expedientes_modificaciones.
-- ============================================================
-- Ya existe idx_plg_cm_cfs_issue(contract_folder_status_id, issue_date).

-- ============================================================
-- Estadisticas del optimizador
-- ============================================================
ANALYZE TABLE
  `log`,
  `feed`,
  `entry`,
  `deleted_entry`,
  `historico_entry`,
  `historico_deleted_entry`,
  `contract_folder_status`,
  `procurement_project`,
  `procurement_project_lot`,
  `tender_result`,
  `tendering_terms`,
  `tendering_process`,
  `contract_execution_requirement`,
  `financial_guarantee`,
  `document_reference`;

