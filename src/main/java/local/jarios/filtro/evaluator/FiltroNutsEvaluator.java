package local.jarios.filtro.evaluator;

import local.jarios.common.util.Mensajes;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.FiltroTipo;
import local.jarios.helpers.EntryHelper;
import local.jarios.interfaces.FiltroEvaluator;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Optional;

/**
 * Description: Author: juan Date: 13/10/2025 Team:
 */
@Slf4j
public class FiltroNutsEvaluator implements FiltroEvaluator {

  private final FiltroTipo tipo = FiltroTipo.NUTS;

  @Override
  public Optional<String> evaluar(Entry entry) {
    HashSet<String> filtroNuts = VariablesGlobales.getFiltroNuts();

    if (filtroNuts == null || filtroNuts.isEmpty()) {
      log.debug("[FiltroNutsEvaluator] - Filtro vacío. Entry aceptado.");
      return Optional.empty();
    }

    Optional<String> nutsOptional = EntryHelper.getNutsFromEntry(entry);

    if (nutsOptional.isEmpty() || nutsOptional.get().isBlank()) {
      log.debug("[FiltroNutsEvaluator] - NUTS del entry vacío. Rechazado.");
      return Optional.of(tipo.getMensajeError());
    }

    boolean encontrado = filtroNuts.contains(nutsOptional.get());
    return encontrado ? Optional.empty() : Optional.of(Mensajes.ENTRY_NO_FILTRO_NUTS);
  }

  @Override
  public FiltroTipo getTipo() {
    return tipo;
  }
}

