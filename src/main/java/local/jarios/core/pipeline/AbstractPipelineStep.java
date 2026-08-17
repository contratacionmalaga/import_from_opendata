package local.jarios.core.pipeline;

import java.util.Objects;

/**
 * Clase base opcional para pasos del pipeline.
 *
 * @param <C> tipo del contexto compartido
 */
public abstract class AbstractPipelineStep<C> implements PipelineStep<C> {

  private final String name;

  protected AbstractPipelineStep(String name) {
    this.name = Objects.requireNonNull(name, "name no puede ser null");
  }

  public String getName() {
    return name;
  }

  @Override
  public final void execute(C context) throws Exception {
    Objects.requireNonNull(context, "context no puede ser null");
    doExecute(context);
  }

  /**
   * Implementación concreta del paso.
   *
   * @param context contexto compartido
   * @throws Exception error durante la ejecución
   */
  protected abstract void doExecute(C context) throws Exception;
}
