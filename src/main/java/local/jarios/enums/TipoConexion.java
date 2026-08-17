package local.jarios.enums;

import lombok.Getter;

/**
 * Description: Determina si el contrato es MAYOR o MENOR Author: juan Date: 03/03/2024 Team: Juan
 * Antonio Ríos Peláez
 */
@Getter
public enum TipoConexion {

  /** Tipo de conexión que tendrá la información de la base de datos principal. */
  PRINCIPAL,

  /**
   * Tipo de conexión que tendrá la inforamción de la base de datos desde la que se aplicará el
   * filtro (en caso de existir).
   */
  FILTRO_SQL;

  TipoConexion() {}
}
