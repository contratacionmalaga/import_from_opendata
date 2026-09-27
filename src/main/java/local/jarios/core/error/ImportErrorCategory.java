package local.jarios.core.error;

/** Categorías estables de incidencias durante una importación. */
public enum ImportErrorCategory {
  DATABASE_AUTHENTICATION(
      "No se ha podido conectar a la base de datos: las credenciales configuradas han sido rechazadas. Revise el usuario y la contraseña de bd.properties."),
  DATABASE_HOST_UNRESOLVED(
      "No se ha podido conectar a la base de datos: no se puede resolver el servidor configurado en bd.properties."),
  DATABASE_UNAVAILABLE(
      "No se ha podido conectar a la base de datos: el servidor no acepta conexiones. Revise el servidor, puerto y disponibilidad configurados en bd.properties."),
  CONFIGURATION_INVALID(
      "No se ha podido cargar la configuración de la importación. Revise los ficheros properties."),
  IMPORT_ALREADY_RUNNING(
      "No se puede iniciar la importación porque ya hay otra ejecución en curso para el mismo tipo."),
  LOCAL_DATA_ALREADY_EXISTS(
      "No se puede iniciar la carga local porque la base de datos ya contiene expedientes para ese tipo."),
  FEED_NEXT_INVALID(
      "No se puede continuar el parseo porque el enlace al siguiente feed no es válido."),
  FEED_CYCLE(
      "No se puede continuar el parseo porque el origen ha devuelto un ciclo entre feeds."),
  ATOM_XML_INVALID(
      "No se puede procesar uno de los feeds porque su XML ATOM no es válido."),
  REMOTE_FEED_UNAVAILABLE(
      "No se ha podido descargar un feed remoto. Revise la conectividad y la configuración de la importación por Internet."),
  PERSISTENCE_FAILED(
      "No se ha podido guardar la importación en la base de datos. Consulte el log técnico asociado a esta ejecución."),
  UNKNOWN("La importación se ha interrumpido.");

  private final String operatorMessage;

  ImportErrorCategory(String operatorMessage) {
    this.operatorMessage = operatorMessage;
  }

  public String getOperatorMessage() {
    return operatorMessage;
  }
}

