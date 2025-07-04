package local.jarios.common.util;

public final class Mensajes {

    /** Mensaje indicando un error general en la aplicación. */
    public static final String FINAL_ERRONEO = "Error";

    /** Mensaje que indica el final del log. */
    public static final String FINAL =
            "**** Final del log";
    public static final String PERSISTIDAS_ENTIDADES_BASE_DATOS =
            "Se han persistido todas las entidades correctamente en la base de datos.";

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
    public static final String FEED_INFO =
            "Feed: {}. {}";

    public static final String INICIO_PERSISTENCIA_FICHEROS_ATOM =
            "***** INICIO DE LA PERSISTENCIA DE LOS FICHEROS ATOMS EN BASE DE DATOS *****";

    public static final String FIN_PERSISTENCIA_FICHEROS_ATOM =
            "***** FIN DE LA PERSISTENCIA DE LOS FICHEROS ATOMS EN BASE DE DATOS *****";

    public static final String NUMERO_ATOMS_PARSEADOS =
            "Se han parseado un total de {} ficheros atom.";

    public static final String ENTRY_PERSISTIDO_EN_BASE_DATO =
            "Se persiste en la base de datos.";

    public static final String FECHAS_FEED_LOCAL =
            "{}UpdatedFeed: {} | Fecha Final Lectura: {}";

    public static final String FEED_FECHAS_OK =
            "Pertence al rango de fechas";

    public static final String FEED_FECHAS_NO_OK =
            "Las fecha del Feed es anterior a la fecha del NewestFeed en Base de Datos. Final del procesamiento.";

    public static final String ENTIDADES =
            "{}Se han encontrado {} entidades dentro del paquete {}.";

    public static final String EXCEPTION_ERROR =
            "Excepción ocurrida en el la clase: {}";

    public static final String EXCEPTION =
            "***** Excepción ocurrida *****";

    public static final String EXCEPTION_MENSAJE =
            "{}Mensaje: {}";

    public static final String EXCEPTION_STACK_TRACE =
            "{}Pila con del error:";

    public static final String MAIL_CREACION =
            "Creación del objeto Mail a partir del objeto Estadistica correctamente.";

    public static final String MAIL_ENVIADO =
            "Enviado Mail con los datos estadísticos de la ejecución.";

    public static final String LOG_CREACION =
            "Creación del objeto Log correctamente.";

    public static final String ESTADISTICA_CREACION =
            "Creación del objeto Estadística correctamente.";

    public static final String SERVICE_CREACION_INICIO =
            "Inicio de la creación del Servicio de conexión con la base de datos.";

    public static final String SERVICE_CREACION_CREADO =
            "Servicio de conexión con la base de datos creado correctamente. {}";

    public static final String CONFIGURACION_CREACION =
            "Creación del Objeto Configuración correctamente.";

    public static final String ASIGN_CONFIGURACION_TO_LOG =
            "Asignado el objeto Configuración a Log correctamente.";

    public static final String ASIGN_FECHA_HORA_INICIAL_PARSEO_TO_ESTADISTICA =
            "Asignada la fecha y hora de inicio del parseo al objeto Estadísitica.";

    public static final String ASIGN_FECHA_HORA_FINAL_PARSEO_TO_ESTADISTICA =
            "Asignada la fecha y hora final del parseo al objeto Estadísitica. {}";

    public static final String ASIGN_FECHA_HORA_INICIAL_BASE_DATOS_TO_ESTADISTICA =
            "Asignada la fecha y hora de inicio de la persistencia en base de datos al objeto Estadísitica.";

    public static final String ASIGN_FECHA_HORA_FINAL_BASE_DATOS_TO_ESTADISTICA =
            "Asignada la fecha y hora final de la persistencia en base de datos al objeto Estadísitica.";

    public static final String ASIGN_LIST_FEED_TO_LOG =
            "Asignado la Lista de Feeds parseados al objeto Log correctamente.";

    public static final String ASIGN_LIST_ORGANOS_CONTRATACION_TO_LOG =
            "Asignado la Lista de Órganos de Contratación al Log.";

    public static final String ASIGN_DURACION_PARSEO =
            "Asignada la duración del parseo en memoria al objeto Estadística correctamente.";

    public static final String ASIGN_DURACION_BASE_DATOS =
            "Asignada la duración de la persistencia en base de datos al objeto Estadística correctamente.";

    public static final String ENTRY_3 =
            "{}Entry: {}. {}";

    public static final String ENTRY_NUEVO = "El Entry no figura en la base de datos.";
    public static final String ENTRY_NO_FILTRO_SQL = "El Entry no cumple el filtro SQL.";
    public static final String ENTRY_NO_FILTRO_FECHAS = "El Entry no cumple el filtro FECHAS.";
    public static final String ENTRY_NO_FILTRO_OBJETO = "El Entry no cumple el filtro OBJETO.";
    public static final String ENTRY_NO_FILTRO_NUTS = "El Entry no cumple el filtro NUTS.";
    public static final String ENTRY_CUMPLE_FILTROS = "El Entru cumple los filtros.";

    private Mensajes() { }

}


