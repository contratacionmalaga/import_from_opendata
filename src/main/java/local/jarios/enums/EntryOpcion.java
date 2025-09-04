package local.jarios.enums;

/**
 * Description: Determina si el contrato es MAYOR o MENOR Author: juan Date: 03/03/2024 Team: Juan
 * Antonio Ríos Peláez
 */
public enum EntryOpcion {

  // Cuando el Entry en Base de Datos es mayor o igual que el Entry en Memoria
  REGISTRAR,

  // Aquellos Entry que NO EXISTEN en el MAP de la ejecución y por tanto se añaden a este
  PROPUESTO_INSERTAR,

  // Aquellos Entry que NO EXISTEN en el MAP de la ejecución y por tanto se añaden a este
  INSERTAR,

  // Cuando el Entry
  ACTUALIZAR,

  ELIMINAR,

  // Aquellos Entry que NO CUMPLEN con los filtros
  RECHAZAR;

  /**
   * Mensaje insertar
   */
  public static final String MSG_ENTRYOPCION_REGISTRAR =
      "El Entry no figura en Memoria.";

  /**
   * Mensaje insertar
   */
  public static final String MSG_ENTRYOPCION_INSERTAR =
      "El Entry no figura en Memoria.";

  /**
   * Mensaje actualizar
   */
  public static final String MSG_ENTRYOPCION_ACTUALIZAR =
      "El Entry ya figura en Memoria pero con un valor de UPDATED anterior.";

  /**
   * Mensaje actualizar
   */
  public static final String MSG_ENTRYOPCION_RECHAZAR =
      "El Entry NO CUMPLE con los filtros establecidos.";

  EntryOpcion() {
    // CONSTURCTOR DE LA CLASE
  }
}
