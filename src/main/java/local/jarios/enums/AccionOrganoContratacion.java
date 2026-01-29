package local.jarios.enums;

import lombok.Getter;

/**
 * Enumeración que representa las acciones posibles que se pueden realizar sobre una entidad o
 * registro en el sistema.
 * <p>
 * Las acciones disponibles son:
 * <ul>
 *   <li>CREAR: Indica que se está creando un nuevo registro.</li>
 *   <li>ELIMINAR: Indica que se está eliminando un registro existente.</li>
 *   <li>ACTUALIZAR: Indica que se está modificando un registro existente.</li>
 * </ul>
 *
 * <p><b>Author:</b> Juan</p>
 * <p><b>Date:</b> 03/03/2024</p>
 * <p><b>Team:</b> Juan Antonio Ríos Peláez</p>
 */
@Getter
public enum AccionOrganoContratacion {
  /**
   * Acción para crear un nuevo registro
   */
  CREAR,
  /**
   * Acción para eliminar un registro existente
   */
  ELIMINAR,
  /**
   * Acción para actualizar un registro existente
   */
  ACTUALIZAR,
  /**
   * Acción para actualizar un registro que figura como borrado
   */
  REACTIVAR;

  /**
   * Constructor por defecto
   */
  AccionOrganoContratacion() {
    // Constructor por defecto
  }
}
