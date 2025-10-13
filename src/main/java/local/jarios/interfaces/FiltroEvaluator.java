package local.jarios.interfaces;

import local.jarios.entity.atom.Entry;
import local.jarios.enums.FiltroTipo;

import java.util.Optional;

/**
 * Description: Author: juan Date: 13/10/2025 Team:
 */
public interface FiltroEvaluator {
  Optional<String> evaluar(Entry entry);
  FiltroTipo getTipo();
}
