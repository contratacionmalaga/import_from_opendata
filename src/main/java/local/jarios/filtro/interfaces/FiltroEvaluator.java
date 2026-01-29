package local.jarios.filtro.interfaces;

import local.jarios.entity.atom.Entry;
import local.jarios.enums.FiltroTipo;

/**
 * Description: Author: juan Date: 13/10/2025 Team:
 */
public interface FiltroEvaluator {
  boolean evaluar(Entry entry);

  FiltroTipo getTipo();
}
