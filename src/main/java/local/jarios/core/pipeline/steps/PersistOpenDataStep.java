package local.jarios.core.pipeline.steps;

import local.jarios.core.abstracts.AbstractOpenDataBase;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;

/** Persistencia final. */
public class PersistOpenDataStep extends AbstractOpenDataStep {

  public PersistOpenDataStep(AbstractOpenDataBase openData) {
    super("persist-open-data", openData);
  }

  @Override
  protected void doExecute(OpenDataExecutionContext context) throws Exception {
    context.setEstadistica(getOpenData().persistAll(context));
  }
}
