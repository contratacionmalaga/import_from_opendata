package local.jarios.core.pipeline.steps;

import local.jarios.core.abstracts.AbstractOpenDataBase;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;

/** Ejecuta el parseo y guarda su duración. */
public class ParseOpenDataStep extends AbstractOpenDataStep {

  public ParseOpenDataStep(AbstractOpenDataBase openData) {
    super("parse-open-data", openData);
  }

  @Override
  protected void doExecute(OpenDataExecutionContext context) throws Exception {
    String duracionParseo = getOpenData().pipelineParseFeeds(context);
    context.setDuracionParseo(duracionParseo);
  }
}
