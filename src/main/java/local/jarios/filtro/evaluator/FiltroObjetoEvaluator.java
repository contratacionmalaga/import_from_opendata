package local.jarios.filtro.evaluator;

import local.jarios.common.util.Mensajes;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.FiltroTipo;
import local.jarios.helpers.EntryHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.interfaces.FiltroEvaluator;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Description: Author: juan Date: 13/10/2025 Team:
 */
@Slf4j
public class FiltroObjetoEvaluator implements FiltroEvaluator {

  private final FiltroTipo tipo = FiltroTipo.OBJETO;

  @Override
  public Optional<String> evaluar(Entry entry) {
    String filtroObjeto = VariablesGlobales.getFiltroObjeto();

    if (filtroObjeto == null || filtroObjeto.isBlank()) {
      log.debug("[FiltroObjetoEvaluator] - Filtro vacío. Entry aceptado.");
      return Optional.empty();
    }

    String objetoEntry = EntryHelper.getObjetoFromEntry(entry);

    if (objetoEntry == null || objetoEntry.isBlank()) {
      log.debug("[FiltroObjetoEvaluator] - Objeto del entry vacío. Rechazado.");
      return Optional.of(tipo.getMensajeError());
    }

    boolean encontrado = StringHelper.contieneCadena(objetoEntry, filtroObjeto);
    return encontrado ? Optional.empty() : Optional.of(Mensajes.ENTRY_NO_FILTRO_OBJETO);
  }

  @Override
  public FiltroTipo getTipo() {
    return tipo;
  }
}

