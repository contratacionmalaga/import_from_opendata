package local.jarios.core.pipeline.steps;

import local.jarios.core.abstracts.AbstractOpenDataBase;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;

/** Inicialización común: - versión - properties - contexto de variante */
public class InitOpenDataStep extends AbstractOpenDataStep {

  public InitOpenDataStep(AbstractOpenDataBase openData) {
    super("init-open-data", openData);
  }

  @Override
  protected void doExecute(OpenDataExecutionContext context) throws Exception {
    getOpenData().pipelineInitAppVersion();
    getOpenData().pipelineInitProperties(context.getConfigDir());
    getOpenData().pipelineInitVariantContext(context);
  }
}
