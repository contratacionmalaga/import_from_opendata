package local.jarios.interfaces;

import java.time.LocalDateTime;

/**
 * Interfaz para entidades que poseen un identificador único tipo UUID.
 *
 * <p>Garantiza que las clases que implementen esta interfaz tengan los métodos
 * para obtener y establecer su ID.</p>
 *
 * @author juan
 * @since 01/03/2025
 */
public interface HasDeletedAt {

  /**
   * Interfaz para obtener el valor de deleted_at de una entidad que implementa AuditableDeletedAt.
   *
   * @return LocalDateTime con el valor de la columna deletedAt
   */
  LocalDateTime getDeletedAt();

  /**
   * Interfaz para borrar el valor de deleted_at en una entidad que implementa AuditableDeletedAt.
   */
  default void cleanMarkAsDeleted() {
    throw new UnsupportedOperationException("cleanMarkAsDeleted() no implementado");
  }

  /**
   * Interfaz para establecer la fecha y hora actual al campo deleted_at en una entidad. que
   * implementa AuditableDeletedAt.
   */
  default void markAsDeleted() {
    throw new UnsupportedOperationException("markAsDeleted() no implementado");
  }

  /**
   * Interfaz para establecer la fecha y hora actual al campo deleted_at en una entidad. que
   * implementa AuditableDeletedAt.
   */
  default void markAsUpdated() {
    throw new UnsupportedOperationException("markAsUpdated() no implementado");
  }
}
