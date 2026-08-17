package local.jarios.common.util;

/**
 * Clase final que contiene las claves (keys) de propiedades utilizadas en los diferentes ficheros
 * de configuración de la aplicación.
 *
 * <p>Estas constantes representan los nombres de las propiedades definidas en ficheros como <code>
 * app.properties</code>, <code>filter.properties</code>, <code>hibernate.properties</code>, <code>
 * validation.properties</code> y <code>mail.properties</code>.
 *
 * <p>Esta clase no debe ser instanciada.
 *
 * @author Home
 */
public final class PropertiesKeys {

  /* Claves de propiedades para el fichero app.properties */
  public static final String APP_NAME = "app.name";
  public static final String APP_DESCRIPTION = "app.description";

  public static final String APP_LOCAL_PATH = "app.local.path";

  public static final String APP_LOCAL_MAYORES = "app.local.mayores";
  public static final String APP_LOCAL_MENORES = "app.local.menores";
  public static final String APP_LOCAL_ENCARGOS = "app.local.emps";
  public static final String APP_LOCAL_CONSULTAS = "app.local.cpms";
  public static final String APP_LOCAL_AGREGADAS = "app.local.agregadas";

  public static final String APP_INTERNET_MAYORES = "app.internet.mayores";
  public static final String APP_INTERNET_MENORES = "app.internet.menores";
  public static final String APP_INTERNET_ENCARGOS = "app.internet.emps";
  public static final String APP_INTERNET_CONSULTAS = "app.internet.cpms";
  public static final String APP_INTERNET_AGREGADAS = "app.internet.agregadas";

  public static final String APP_TIPO_SINDICACION = "app.tipo_sindicacion";
  public static final String APP_PERSISTIR_HISTORICOS_RECHAZADOS =
      "app.persistir_historicos_rechazados";
  public static final String APP_HTTP_MAX_RETRIES = "app.http.max_retries";
  public static final String APP_HTTP_RETRY_DELAY_MS = "app.http.retry_delay_ms";
  public static final String APP_HTTP_REQUEST_DELAY_MS = "app.http.request_delay_ms";

  /* Claves de propiedades para el fichero filter.properties */

  /** Clave para la fecha inicial de lectura del filtro. */
  public static final String FILTER_FECHAINICIALLECTURA = "filter.fechaInicialLectura";

  /** Clave para la fecha final de lectura del filtro. */
  public static final String FILTER_FECHAFINALLECTURA = "filter.fechaFinalLectura";

  /** Clave para la sentencia SQL del filtro. */
  public static final String FILTER_CODIGOS_POSTALES = "filter.codigosPostales";

  /** Variable que contiene la lista con los nifs por los que filtrar. */
  public static final String FILTER_NIFS = "filter.nifs";

  /* Claves de propiedades para Jakarta Persistence */

  /** Clave para la URL JDBC en Jakarta Persistence. */
  public static final String JAKARTA_PERSISTENCE_JDBC_URL = "jakarta.persistence.jdbc.url";

  /** Clave para el driver JDBC en Jakarta Persistence. */
  public static final String JAKARTA_PERSISTENCE_JDBC_DRIVER = "jakarta.persistence.jdbc.driver";

  /** Clave para el usuario JDBC en Jakarta Persistence. */
  public static final String JAKARTA_PERSISTENCE_JDBC_USER = "jakarta.persistence.jdbc.user";

  /** Clave para la contraseña JDBC en Jakarta Persistence. */
  public static final String JAKARTA_PERSISTENCE_JDBC_PASSWORD =
      "jakarta.persistence.jdbc.password";

  /* Claves de propiedades para el fichero mail.properties */
  public static final String MAIL_SMTP_HOST = "mail.smtp.host";
  public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
  public static final String MAIL_SMTP_PORT = "mail.smtp.port";
  public static final String MAIL_SMTP_SOCKETFACTORY_PORT = "mail.smtp.socketFactory.port";
  public static final String MAIL_SMTP_SSL_CHECK_SERVER_INTEGRITY =
      "mail.smtp.ssl.checkserveridentity";
  public static final String MAIL_SMTP_SSL_PROTOCLS = "mail.smtp.ssl.protocols";
  public static final String MAIL_SMTP_SSL_TRUST = "mail.smtp.ssl.trust";
  public static final String MAIL_SMTP_STARTTLS = "mail.smtp.starttls.enable";
  public static final String MAIL_SMTP_USER = "mail.smtp.user";
  public static final String MAIL_SMTP_PASSWORD = "mail.smtp.password";
  public static final String MAIL_FROM = "mail.from";
  public static final String MAIL_TO = "mail.to";

  /** Constructor privado para evitar instanciación. */
  private PropertiesKeys() {
    /* CONSTRUCTOR PRIVADO PARA EVITAR LA INSTANCIACIÓN */
  }
}
