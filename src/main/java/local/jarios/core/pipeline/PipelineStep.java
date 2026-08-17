package local.jarios.core.pipeline;

/**
 * Contrato base para un paso del pipeline.
 *
 * @param <C> tipo del contexto compartido durante la ejecución
 */
@FunctionalInterface
public interface PipelineStep<C> {

  /**
   * Ejecuta el paso usando el contexto recibido.
   *
   * @param context contexto compartido
   * @throws Exception si ocurre cualquier error durante la ejecución
   */
  void execute(C context) throws Exception;
}
