-- Indices para opendata-malaga
-- Generado desde EXPLAIN de las vistas de opendata-malaga el 2026-05-09.
-- Objetivo: mejorar joins/filtros de vistas y preparar consultas Java de carga incremental.
-- Ejecutar en ventana de mantenimiento. CREATE INDEX hace commits implicitos.
-- Si el servidor no soporta IF NOT EXISTS, revisar INFORMATION_SCHEMA.STATISTICS antes de lanzar.

USE `opendata-malaga`;

-- ============================================================
-- Indices existentes relevantes detectados
-- ============================================================
-- entry: PRIMARY(id), entry_entry_id_unique(entry_id), entry_entry_id_corto_unique(entry_id_corto),
--        feed_entry(feed_id), entry_updated_index(updated)
-- contract_folder_status: PRIMARY(id), cfs_entry(entry_id), cfs_id_plataforma(id_plataforma),
--        cfs_nif(nif), cfs_contract_folder_id(contract_folder_id), cfs_contract_folder_status_code(contract_folder_status_code)
-- tender_result: fk_tenderresult_contractfolderstatus(contract_folder_status_id),
--        idx_mlg_tr_cfs_lot_result(contract_folder_status_id, procurement_project_lot_id, result_code),
--        idx_mlg_tr_result_nif_cfs(result_code, nif, contract_folder_status_id),
--        idx_mlg_tr_nif_award(nif, award_date)
-- procurement_project: pp_contract_folder_status_id(contract_folder_status_id),
--        pp_preliminary_market_consultation_status_id(preliminary_market_consultation_status_id),
--        type_code(type_code), total_amount(total_amount)
-- tendering_terms: UK5tdskranwt92bsd1bd9pr08oj(contract_folder_status_id),
--        UK23gchov7dvye4vluxayj5qknm(procurement_project_lot_id)
-- tendering_process: idx_mlg_tp_cfs(contract_folder_status_id),
--        idx_mlg_tp_procedure_cfs(procedure_code, contract_folder_status_id)
-- commodity_classification: idx_mlg_cc_project_code(procurement_project_id, item_classification_code),
--        idx_mlg_cc_lot_code(procurement_project_lot_id, item_classification_code)
-- notice_info: idx_mlg_ni_cfs_type(contract_folder_status_id, notice_type_code)
-- additional_publication_document_reference: idx_mlg_apdr_status_issue(additional_publication_status_id, issue_date)
-- document_reference: idx_mlg_docref_general_type(general_document_reference_id, document_type_code),
--        idx_mlg_docref_additional_type(additional_document_reference_id, document_type_code)

-- ============================================================
-- Importacion Java / comparacion incremental
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_mlg_log_tipo_id
  ON `log` (`tipo_sindicacion`, `id`);

CREATE INDEX IF NOT EXISTS idx_mlg_feed_log_id
  ON `feed` (`log_id`, `id`);

CREATE INDEX IF NOT EXISTS idx_mlg_entry_feed_updated
  ON `entry` (`feed_id`, `updated`);

CREATE INDEX IF NOT EXISTS idx_mlg_deleted_ref
  ON `deleted_entry` (`ref`);

CREATE INDEX IF NOT EXISTS idx_mlg_deleted_feed_ref
  ON `deleted_entry` (`feed_id`, `ref`);

-- ============================================================
-- Vistas principales de expedientes/adjudicaciones
-- EXPLAIN afectado: malaga_adjudicaciones, malaga_adjudicatarios,
-- malaga_adjudicatarios_v2, malaga_cmayores, malaga_cmenores,
-- malaga_emps, malaga_rel_expedientes_adjudicaciones.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_mlg_cfs_entry_status
  ON `contract_folder_status` (`entry_id`, `contract_folder_status_code`);

CREATE INDEX IF NOT EXISTS idx_mlg_cfs_idplat_status
  ON `contract_folder_status` (`id_plataforma`, `contract_folder_status_code`);

CREATE INDEX IF NOT EXISTS idx_mlg_pp_type_cfs
  ON `procurement_project` (`type_code`, `contract_folder_status_id`);

CREATE INDEX IF NOT EXISTS idx_mlg_pp_total_cfs
  ON `procurement_project` (`total_amount`, `contract_folder_status_id`);

CREATE INDEX IF NOT EXISTS idx_mlg_ppl_cfs_lote
  ON `procurement_project_lot` (`contract_folder_status_id`, `lote`);

CREATE INDEX IF NOT EXISTS idx_mlg_ppl_cfs_total
  ON `procurement_project_lot` (`contract_folder_status_id`, `total_amount`);

-- ============================================================
-- Vistas de criterios, condiciones especiales y garantias
-- EXPLAIN afectado: malaga_rel_expedientes_criterios_adjs,
-- malaga_rel_expedientes_criterios_adjs_v2,
-- malaga_rel_expedientes_cespadj.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_mlg_ac_terms_type_subtype
  ON `awarding_criteria` (`tendering_terms_id`, `awarding_criteria_type_code`, `awarding_criteria_subtype_code`);

CREATE INDEX IF NOT EXISTS idx_mlg_ac_type_subtype_weight
  ON `awarding_criteria` (`awarding_criteria_type_code`, `awarding_criteria_subtype_code`, `weight_numeric`);

CREATE INDEX IF NOT EXISTS idx_mlg_cer_terms_code
  ON `contract_execution_requirement` (`tendering_terms_id`, `execution_requirement_code`);

CREATE INDEX IF NOT EXISTS idx_mlg_fg_terms
  ON `financial_guarantee` (`tendering_terms_id`);

-- ============================================================
-- Vistas de anuncios/documentos
-- EXPLAIN afectado: malaga_rel_expedientes_anuncios,
-- malaga_rel_consultas_anuncios, malaga_rel_expedientes_documentos.
-- Nota: malaga_rel_expedientes_documentos usa JOIN con OR sobre 4 columnas;
-- estos indices ayudan, pero la mejora fuerte requiere reescribir la vista con UNION ALL.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_mlg_apdr_status_issue_type
  ON `additional_publication_document_reference` (`additional_publication_status_id`, `issue_date`, `document_type_code`);

CREATE INDEX IF NOT EXISTS idx_mlg_docref_technical_type
  ON `document_reference` (`technical_document_reference_id`, `document_type_code`);

CREATE INDEX IF NOT EXISTS idx_mlg_docref_legal_type
  ON `document_reference` (`legal_document_reference_id`, `document_type_code`);

CREATE INDEX IF NOT EXISTS idx_mlg_docref_pmcs_type
  ON `document_reference` (`pmcs_general_document_reference_id`, `document_type_code`);

-- ============================================================
-- Consultas preliminares de mercado
-- EXPLAIN afectado: malaga_cpms, malaga_rel_consultas_documentos.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_mlg_pmcs_idplat
  ON `preliminary_market_consultation_status` (`id_plataforma`);

CREATE INDEX IF NOT EXISTS idx_mlg_pmcs_nif
  ON `preliminary_market_consultation_status` (`nif`);

CREATE INDEX IF NOT EXISTS idx_mlg_pmcs_status_entry
  ON `preliminary_market_consultation_status` (`preliminary_market_consultation_status_code`, `entry_id`);

CREATE INDEX IF NOT EXISTS idx_mlg_tp_pmcs
  ON `tendering_process` (`preliminary_market_consultation_status_id`);

-- ============================================================
-- Modificaciones
-- EXPLAIN afectado: malaga_rel_expedientes_modificaciones.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_mlg_cm_cfs_issue
  ON `contract_modification` (`contract_folder_status_id`, `issue_date`);

-- ============================================================
-- Estadisticas del optimizador
-- ============================================================
ANALYZE TABLE
  `log`,
  `feed`,
  `entry`,
  `deleted_entry`,
  `contract_folder_status`,
  `procurement_project`,
  `procurement_project_lot`,
  `tender_result`,
  `tendering_terms`,
  `tendering_process`,
  `awarding_criteria`,
  `contract_execution_requirement`,
  `financial_guarantee`,
  `additional_publication_document_reference`,
  `document_reference`,
  `preliminary_market_consultation_status`,
  `contract_modification`;

