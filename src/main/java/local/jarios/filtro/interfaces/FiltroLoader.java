package local.jarios.filtro.interfaces;

import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.properties.exception.PropertiesManagerException;

/** Description: Author: juan Date: 13/10/2025 Team: */
public interface FiltroLoader {
  void cargar(OpenDataExecutionContext context) throws PropertiesManagerException;
}
