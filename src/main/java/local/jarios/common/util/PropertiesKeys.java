package local.jarios.common.util;

/**
 * Clase final que contiene las claves (keys) de propiedades utilizadas en los diferentes ficheros
 * de configuración de la aplicación.
 *
 * <p>
 * Estas constantes representan los nombres de las propiedades definidas en ficheros como
 * <code>app.properties</code>, <code>filter.properties</code>, <code>hibernate.properties</code>,
 * <code>validation.properties</code> y <code>mail.properties</code>.
 * </p>
 *
 * <p>
 * Esta clase no debe ser instanciada.
 * </p>
 *
 * @author Home
 */
public final class PropertiesKeys {

  /* Claves de propiedades para el fichero app.properties */

  /**
   * Clave para el nombre de la aplicación.
   */
  public static final String APP_NAME = "app.name";

  /**
   * Clave para la URL de la aplicación.
   */
  public static final String APP_URL = "app.url";

  /**
   * Clave para la ruta (path) de la aplicación.
   */
  public static final String APP_PATH = "app.path";

  /**
   * Clave para el nombre del fichero principal de la aplicación.
   */
  public static final String APP_FILENAME = "app.filename";

  /* Claves de propiedades para el fichero filter.properties */

  /**
   * Clave para la fecha inicial de lectura del filtro.
   */
  public static final String FILTER_FECHAINICIALLECTURA = "filter.fechaInicialLectura";

  /**
   * Clave para la fecha final de lectura del filtro.
   */
  public static final String FILTER_FECHAFINALLECTURA = "filter.fechaFinalLectura";

  /**
   * Clave para la sentencia SQL del filtro.
   */
  public static final String FILTER_SQL = "filter.sql";

  /**
   * Clave para el objeto del filtro.
   */
  public static final String FILTER_OBJETO = "filter.objeto";

  /**
   * Clave para el filtro NUTS.
   */
  public static final String FILTER_NUTS = "filter.nuts";

  /**
   * Clave para el filtro con la ubicación desde la que se realiza la importación.
   */
  public static final String FILTER_UBICACION = "filter.ubicacion";

  /* Claves de propiedades para Jakarta Persistence */

  /**
   * Clave para la URL JDBC en Jakarta Persistence.
   */
  public static final String JAKARTA_PERSISTENCE_JDBC_URL = "jakarta.persistence.jdbc.url";

  /**
   * Clave para el driver JDBC en Jakarta Persistence.
   */
  public static final String JAKARTA_PERSISTENCE_JDBC_DRIVER = "jakarta.persistence.jdbc.driver";

  /**
   * Clave para el usuario JDBC en Jakarta Persistence.
   */
  public static final String JAKARTA_PERSISTENCE_JDBC_USER = "jakarta.persistence.jdbc.user";

  /**
   * Clave para la contraseña JDBC en Jakarta Persistence.
   */
  public static final String JAKARTA_PERSISTENCE_JDBC_PASSWORD = "jakarta.persistence.jdbc.password";

  /* Claves de propiedades para el fichero mail.properties */
  /**
   * Clave para la dirección del remitente del correo.
   */
  public static final String MAIL_FROM = "mail.from";

  /**
   * Clave para la dirección del destinatario del correo.
   */
  public static final String MAIL_TO = "mail.to";

  /**
   * Constructor privado para evitar instanciación.
   */
  private PropertiesKeys() { /* CONSTRUCTOR PRIVADO PARA EVITAR LA INSTANCIACIÓN */ }

}
