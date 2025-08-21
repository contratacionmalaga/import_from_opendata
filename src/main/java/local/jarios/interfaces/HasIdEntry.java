package local.jarios.interfaces;

/**
 * Interfaz para entidades que poseen un identificador único tipo UUID.
 * <p>
 * Garantiza que las clases que implementen esta interfaz tengan los métodos
 * para obtener y establecer su ID.
 * </p>
 *
 * @param <V> tipo de dato del identificador único de la entidad
 * @author juan
 * @since 01/03/2025
 */
public interface HasIdEntry<V> {

  /**
   * Obtiene el identificador único de la entidad.
   *
   * @return identificador de tipo {@code V}.
   */
  String getIdEntry();

  /**
   * Establece el identificador único de la entidad.
   *
   * @param idEntry valor a asignar de tipo {@code V}.
   */
  void setIdEntry(String idEntry);
}
