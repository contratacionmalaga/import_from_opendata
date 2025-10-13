package local.jarios.common.util;

public final class Mensajes {

  /**
   * Mensaje indicando un error general en la aplicación.
   */
  public static final String FINAL_ERRONEO = "Error";
  /**
   * Mensaje que indica el final del log.
   */
  public static final String FINAL =
      "**** Final del log";
  public static final String FILE_NOT_EXIST =
      "Fichero no existe. {}";
  public static final String NOT_FILE =
      "No es un fichero. {}";
  public static final String FILE_NOT_READ =
      "No se puede leer. {}";
  public static final String MENSAJE_VALOR_SWITCH_INCORRECTO =
      "EL VALOR DE {}: ({}) NO ES VÁLIDO";
  public static final String INICIO =
      "**** Inicio de la ejecución del programa ****";
  public static final String FINAL_CORRECTO =
      "**** La ejecución ha finalizado CORRECTAMENTE ****";
  public static final String ENTIDADES =
      "{}Se han encontrado {} entidades dentro del paquete {}.";
  public static final String ENTRY_NUEVO = "El Entry no figura en la base de datos.";
  public static final String ENTRY_NO_FILTRO_ORGANOS_CONTRATACION = "El Entry no cumple el filtro ORGANOS CONTRATACIÓN.";
  public static final String ENTRY_NO_FILTRO_FECHAS = "El Entry no cumple el filtro FECHAS.";
  public static final String ENTRY_NO_FILTRO_OBJETO = "El Entry no cumple el filtro OBJETO.";
  public static final String ENTRY_NO_FILTRO_NUTS = "El Entry no cumple el filtro NUTS.";
  public static final String ENTRY_CUMPLE_FILTROS = "El Entry cumple los filtros.";
  /**
   * Mensaje insertar
   */
  public final String MSG_ENTRYOPCION_INSERTAR =
      "El Entry no figura en Memoria.";
  /**
   * Mensaje actualizar
   */
  public final String MSG_ENTRYOPCION_REGISTRAR =
      "El Entry figura en al Base de Datos con un valor de UPDATED mayor o igual que el existen en Memoria.";
  /**
   * Mensaje actualizar
   */
  public final String MSG_ENTRYOPCION_ACTUALIZAR =
      "El Entry ya figura en Memoria pero con un valor de UPDATED anterior.";

  private Mensajes() {
    // CONSTRUCTOR VACÍO
  }

}


