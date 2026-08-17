package local.jarios.core.pipeline.steps;

import local.jarios.core.abstracts.AbstractOpenDataBase;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;

/** Muestra el resumen previo a la persistencia. */
public class PreviewOpenDataStep extends AbstractOpenDataStep {

  public PreviewOpenDataStep(AbstractOpenDataBase openData) {
    super("preview-open-data", openData);
  }

  @Override
  protected void doExecute(OpenDataExecutionContext context) throws Exception {
    getOpenData().previewPersistData(context);
  }
}
