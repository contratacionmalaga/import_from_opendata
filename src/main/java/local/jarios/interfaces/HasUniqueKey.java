package local.jarios.interfaces;

/**
 * Interfaz para entidades que poseen un identificador específico de plataforma.
 *
 * <p>Esta interfaz garantiza que las clases que la implementen tengan un método
 * para obtener el identificador de plataforma, que generalmente es un String.</p>
 *
 * @author juan
 * @since 01/03/2025
 */
public interface HasUniqueKey {

  /**
   * Obtiene una clave única representativa del objeto, que será utilizada para identificarlo
   * lógicamente en estructuras de comparación o unificación.
   *
   * @return Una cadena que representa la clave única del objeto.
   */
  String getUniqueKey();
}
