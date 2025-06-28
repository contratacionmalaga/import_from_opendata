package local.jarios.interfaces;

/**
 * Interfaz para entidades que poseen un identificador único tipo UUID.
 * <p>
 * Garantiza que las clases que implementen esta interfaz tengan los métodos
 * para obtener y establecer su ID.
 * </p>
 *
 * @author juan
 * @since 01/03/2025
 */
public interface HasIdEntry<V> {

    /**
     * Obtiene el identificador único de la entidad.
     *
     * @return cadena con el identificador.
     */
    String getIdEntry();

    /**
     * Establece el identificador único de la entidad.
     *
     * @param idEntry valor a asignar
     */
    void setIdEntry(String idEntry);
}
