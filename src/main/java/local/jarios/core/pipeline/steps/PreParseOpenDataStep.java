package local.jarios.core.pipeline.steps;

import local.jarios.core.abstracts.AbstractOpenDataBase;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;

/**
 * Paso opcional previo al parseo.
 *
 * <p>En Málaga carga Excel y filtros; en pliegos no hace nada.
 */
public class PreParseOpenDataStep extends AbstractOpenDataStep {

  public PreParseOpenDataStep(AbstractOpenDataBase openData) {
    super("pre-parse-open-data", openData);
  }

  @Override
  protected void doExecute(OpenDataExecutionContext context) throws Exception {
    getOpenData().pipelineBeforeParse(context);
  }
}
