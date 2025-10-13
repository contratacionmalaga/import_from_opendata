package local.jarios.filtro.evaluator;

import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.FiltroTipo;
import local.jarios.helpers.EntryHelper;
import local.jarios.interfaces.FiltroEvaluator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

/**
 * Description: Author: juan Date: 13/10/2025 Team:
 */
@Slf4j
public class FiltroOrganoContratacionEvaluator implements FiltroEvaluator {

  private final FiltroTipo tipo = FiltroTipo.ORGANOS_CONTRATACION;

  @Override
  public Optional<String> evaluar(Entry entry) {
    Map<String, String> filtroSql = VariablesGlobales.getMapFiltroSql();

    if (filtroSql == null || filtroSql.isEmpty()) {
      log.debug("[FiltroSqlEvaluator] - Filtro SQL vacío. Entry aceptado.");
      return Optional.empty();
    }

    Optional<String> idPlataformaOpt = EntryHelper.getIdPlataformaFromEntry(entry);

    if (idPlataformaOpt.isEmpty()) {
      log.debug("[FiltroSqlEvaluator] - Entry sin ID plataforma. Rechazado.");
      return Optional.of(tipo.getMensajeError());
    }

    String idPlataforma = idPlataformaOpt.get();
    boolean encontrado = filtroSql.containsKey(idPlataforma);

    if (encontrado) {
      String nuts = filtroSql.get(idPlataforma);
      entry.setNuts(nuts);
      return Optional.empty();
    } else {
      return Optional.of(tipo.getMensajeError());
    }
  }

  @Override
  public FiltroTipo getTipo() {
    return tipo;
  }
}
