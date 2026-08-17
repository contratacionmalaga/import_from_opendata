package local.jarios.core.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Orquestador simple de pasos de pipeline.
 *
 * @param <C> tipo del contexto compartido
 */
public final class OpenDataPipeline<C> {

  private final List<PipelineStep<C>> steps = new ArrayList<>();

  public OpenDataPipeline() {}

  public OpenDataPipeline<C> addStep(PipelineStep<C> step) {
    steps.add(Objects.requireNonNull(step, "step no puede ser null"));
    return this;
  }

  public List<PipelineStep<C>> getSteps() {
    return List.copyOf(steps);
  }

  /**
   * Ejecuta todos los pasos en orden.
   *
   * @param context contexto compartido
   * @throws Exception error durante cualquier paso
   */
  public void execute(C context) throws Exception {
    Objects.requireNonNull(context, "context no puede ser null");

    for (PipelineStep<C> step : steps) {
      step.execute(context);
    }
  }

  public boolean isEmpty() {
    return steps.isEmpty();
  }

  public int size() {
    return steps.size();
  }
}
