package local.jarios.core.pipeline.steps;

import java.util.Objects;
import local.jarios.core.abstracts.AbstractOpenDataBase;
import local.jarios.core.pipeline.AbstractPipelineStep;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;

/** Clase base para los steps del pipeline OpenData. */
public abstract class AbstractOpenDataStep extends AbstractPipelineStep<OpenDataExecutionContext> {

  private final AbstractOpenDataBase openData;

  protected AbstractOpenDataStep(String name, AbstractOpenDataBase openData) {
    super(name);
    this.openData = Objects.requireNonNull(openData, "openData no puede ser null");
  }

  protected AbstractOpenDataBase getOpenData() {
    return openData;
  }
}
