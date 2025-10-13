package local.jarios;

import local.jarios.common.util.Mensajes;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.database.SessionFactoryRegistry;
import local.jarios.email.api.EmailSender;
import local.jarios.email.api.EmailSenderImpl;
import local.jarios.email.api.EmailService;
import local.jarios.email.api.EmailServiceImpl;
import local.jarios.email.exception.EmailException;
import local.jarios.email.helper.EmailHelper;
import local.jarios.email.model.EmailData;
import local.jarios.email.validator.EmailRequestValidator;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.enums.EntryOpcion;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiServiceException;
import local.jarios.exceptions.MiUnknownHostException;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.FiltroHelper;
import local.jarios.helpers.LocalDateTimeHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import local.jarios.services.ServicePrincipal;
import local.jarios.services.ServicePrincipalImpl;
import local.jarios.version.api.Version;
import local.jarios.version.api.VersionImpl;
import local.jarios.version.exception.VersionException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Clase abstracta que gestiona el procesamiento general para la importación y análisis de datos
 * OpenData basados en feeds ATOM, incluyendo carga de configuración, parseo, persistencia y
 * notificaciones.
 * <p>
 * Las subclases deben implementar métodos específicos para definir el tipo de sindicacion, lugar de
 * importación y la lógica de parseo de los feeds.
 * </p>
 */
@Slf4j
public abstract class AbstractOpenData {

  /**
   * Servicio de gestión de propiedades de configuración.
   * <p>
   * Se obtiene como instancia singleton mediante
   * {@link PropertiesManagerServiceImpl#getInstance()}. Permite cargar, acceder y gestionar
   * propiedades definidas en ficheros externos.
   * </p>
   */
  public static PropertiesManagerService propertiesManager = null;

  /**
   * Nombre de la aplicación, cargado desde las propiedades externas.
   * <p>
   * Se obtiene desde el fichero de configuración a través de {@code propertiesManager} utilizando
   * la clave {@code Constantes.KEY_APP_NAME}.
   * </p>
   */
  public static String appName = null;

  /**
   * Versión de la aplicación en ejecución.
   * <p>
   * Se determina mediante el componente {@link local.jarios.version.api.Version} que analiza los
   * metadatos del JAR en ejecución.
   * </p>
   */
  public static String appVersion = null;

  /**
   * Maneja de forma centralizada las excepciones que ocurren durante la ejecución del programa.
   * <p>
   * Este método registra el mensaje de error proporcionado, imprime el stack trace del error con
   * sangría personalizada, intenta enviar un email con el detalle del error, y finaliza el programa
   * indicando un error en la ejecución.
   * </p>
   *
   * @param ex           La excepción que fue lanzada.
   * @param mensajeError El mensaje personalizado que describe el contexto del error.
   */
  private static void manejarExcepcion(Exception ex, String mensajeError) {

    log.error("{}. Error: {}", mensajeError, ex.getMessage());

    for (StackTraceElement ste : ex.getStackTrace()) {
      log.error("[manejarExcepcion] - {}", ste);
    }

    try {

      //
      enviarEmail(null, ex, false);
      log.info("[manejarExcepcion] - Correo de error enviado correctamente.");

    } catch (EmailException e) {

      log.error("[manejarExcepcion] -Error inesperado al intentar enviar email de fallo: {}",
                e.getMessage());
    }

    finalizar(Mensajes.FINAL_ERRONEO, 1);
  }

  /**
   * Envía un email con el asunto y cuerpo indicados usando la configuración de propiedades.
   * <p>
   * Método común para centralizar el envío de emails evitando duplicidad. Controla excepciones
   * relacionadas con el envío y las registra.
   * </p>
   *
   * @param estadistica Asunto del email a enviar.
   * @param ex          Cuerpo del email en formato HTML.
   * @param success     Cuerpo del email en formato HTML.
   */
  private static void enviarEmail(Estadistica estadistica, Exception ex, boolean success) {
    try {

      // Configuración del servidor SMTP
      Properties emailProps = propertiesManager.getProperties(PropertiesFiles.MAIL);
      log.debug("[enviarEmail] - Properties cargadas correctamente.");

      // Construcción de los datos del correo
      EmailData emailData = construirEmailData(estadistica, ex, success);
      log.debug("[enviarEmail] - EmailData creado correctamente.");

      EmailRequestValidator.validarEmailRequest(emailProps, emailData);
      log.debug("[enviarEmail] - Properties e EmailData validados correctamente.");

      // Creación del servicio de correo con la implementación de envío SMTP
      EmailSender emailSender = new EmailSenderImpl();
      log.debug("[enviarEmail] - Creación del objeto EmailSender correctamente.");

      EmailService emailService = new EmailServiceImpl(emailSender);
      log.debug("[enviarEmail] - Creado el objeto EmailService correctamente.");

      // Envío del correo
      emailService.sendEmail(emailProps, emailData);
      log.debug("[enviarEmail] - Correo enviado correctamente.");

    } catch (EmailException e) {
      log.error("[enviarEmail] - No se pudo enviar el email: Error en el servicio de correo -> {}",
                e.getMessage());
      throw new EmailException("No se pudo enviar el email: Error en el servicio de correo", e);
    }
  }

  /**
   * Construye un objeto {@link EmailData} con toda la información necesaria para el envío de un
   * correo, en función del resultado del proceso (éxito o error).
   * <p>
   * Utiliza la configuración cargada desde el sistema de propiedades para establecer remitente y
   * destinatario. El asunto y el cuerpo del mensaje se generan usando las utilidades de
   * {@link EmailHelper}.
   * </p>
   *
   * @param estadistica Objeto {@link Estadistica} que contiene datos del proceso. Puede ser
   *                    {@code null} en caso de error.
   * @param ex          Excepción lanzada durante la ejecución, en caso de fallo. Puede ser
   *                    {@code null} si el proceso fue exitoso.
   * @param success     Indicador booleano que señala si el proceso finalizó correctamente
   *                    ({@code true}) o con error ({@code false}).
   * @return Objeto {@link EmailData} completamente inicializado y listo para ser enviado.
   * @throws EmailException Si ocurre un error al obtener el nombre del host o las propiedades
   *                        necesarias.
   */
  private static EmailData construirEmailData(Estadistica estadistica, Exception ex, boolean success) {

    try {

      String equipo = ComunHelper.getHostName();
      log.debug("[construirEmailData] - Equipo desde el que se envía el email: {}", equipo);

      String from = propertiesManager.getProperty(PropertiesFiles.MAIL, PropertiesKeys.MAIL_FROM);
      log.debug("[construirEmailData] - Remitente: {}", from);

      String to = propertiesManager.getProperty(PropertiesFiles.MAIL, PropertiesKeys.MAIL_TO);
      log.debug("[construirEmailData] - Destinatarios: {}", to);

      // Defino el asunto y el cupero del Email
      String asunto = EmailHelper.getAsunto(appName, appVersion, equipo, success);
      log.debug("[construirEmailData] - Asunto del correo: {}.", asunto);

      String cuerpo;
      if (success) {
        cuerpo = EmailHelper.getCuerpoEstadistica(toStringMatrix(estadistica));
      } else {
        cuerpo = EmailHelper.getCuerpoExcepcion(obtenerStackTraceComoArray(ex));
      }
      log.debug("[construirEmailData] - Cuerpo del email creado correctamente");

      return new EmailData(from, to, asunto, cuerpo);

    } catch (MiUnknownHostException e) {
      log.error("[construirEmailData] - Error al obtener el nombre del host. Error: {}",
                e.getMessage());
      throw new EmailException("Error al obtener el nombre del host.", e);

    } catch (PropertiesManagerException e) {
      log.error("Error al leer las propiedades. Error: {}", e.getMessage());
      throw new EmailException("Error al leer las propiedades.", e);
    }
  }

  /**
   * Convierte los campos de una instancia de {@link Estadistica} en una matriz de cadenas de
   * texto.
   * <p>
   * Cada fila de la matriz representa un par clave-valor donde:
   * <ul>
   *     <li>La primera columna es el nombre del campo.</li>
   *     <li>La segunda columna es el valor del campo convertido a {@code String}.</li>
   * </ul>
   * Para campos que son instancias de {@link Log}, se utiliza el valor del identificador ({@code getId()}).
   *
   * @param estadistica la instancia de {@code Estadistica} que se va a procesar
   * @return una matriz de {@code String} con los nombres y valores de los campos de la instancia
   */
  private static String[][] toStringMatrix(Estadistica estadistica) {

    List<String[]> datos = new ArrayList<>();
    log.debug("[toStringMatrix] - Creación de List<String[]>");

    Field[] fields = Estadistica.class.getDeclaredFields(); // también corregido esto: getClass() → .class
    log.debug("[toStringMatrix] - Creación de Field[]");

    for (Field field : fields) {
      log.debug("[toStringMatrix] - Campo: {}", field.getName());
      field.setAccessible(true);

      try {

        Object value = field.get(estadistica); //
        log.debug("[toStringMatrix] - Obtengo el valor: {}", value);

        String nombreCampo = field.getName();
        String valorCampo;

        if (value instanceof Log miLog && miLog.getId() != null) {
          valorCampo = miLog.getId().toString();
        } else {
          valorCampo = String.valueOf(value); // Maneja null de forma segura
        }

        datos.add(new String[]{nombreCampo, valorCampo});
        log.debug("[toStringMatrix] - {} - {}", nombreCampo, valorCampo);

      } catch (IllegalAccessException e) {
        log.debug("[toStringMatrix] - Error de acceso ilegal. Error: {}", e.getMessage());
        datos.add(new String[]{field.getName(), "Error al acceder"});
      }
    }

    return datos.toArray(new String[0][0]);
  }

  /**
   * Convierte el stack trace de una excepción en un arreglo de cadenas de texto.
   * <p>
   * Cada elemento del arreglo representa una línea del stack trace, tal como se imprimiría en un
   * log o consola. Este método es útil para enviar errores por correo o almacenarlos en sistemas
   * donde no se puede registrar el {@code Throwable} directamente.
   * </p>
   *
   * @param ex la excepción de la cual se extrae el stack trace
   * @return un arreglo de {@code String} que representa línea por línea el stack trace
   */
  public static String[] obtenerStackTraceComoArray(Throwable ex) {
    StackTraceElement[] elementos = ex.getStackTrace();
    log.debug(
        "[obtenerStackTraceComoArray] - Obtenidos los elementos del StactTrace. Nº elementos: {}",
        elementos.length);
    String[] resultado = new String[elementos.length];
    log.debug(
        "[obtenerStackTraceComoArray] - Defino un String[] con el número de elementos del StackTrace.");
    for (int i = 0; i < elementos.length; i++) {
      resultado[i] = elementos[i].toString();
      log.debug("[obtenerStackTraceComoArray] - Elemento: {} - {}", i, elementos[i].toString());
    }
    return resultado;
  }

  /**
   * Finaliza la ejecución del programa mostrando un mensaje de log y llamando a System.exit con el
   * código proporcionado.
   *
   * @param mensaje  Mensaje que se mostrará en el log.
   * @param exitCode Código de salida del sistema: 0 para éxito, 1 para error. Otros valores también
   *                 serán aceptados.
   */
  public static void finalizar(String mensaje, int exitCode) {
    if (exitCode == 0) {
      log.info("[finalizar] - {}", mensaje);
    } else {
      log.error("[finalizar] - {} (Código de salida: {})", mensaje, exitCode);
    }

    log.info("[finalizar] - {}", Mensajes.FINAL); // Se asume que FINAL es una constante tipo String
    System.exit(exitCode);
  }

  private static Map<String, Entry> getMapEntryFromBaseDatos() throws MiServiceException {

    ServicePrincipal serviceBaseDatos = new ServicePrincipalImpl();
    return serviceBaseDatos.getMapEntriesEnBaseDatos();
  }

  /**
   * Obtiene el tipo de sindicacion que será usado en el proceso.
   * <p>
   * Método abstracto que debe implementar la subclase para indicar el tipo de sindicacion
   * específico.
   * </p>
   *
   * @return TipoSindicacion a utilizar en el proceso.
   */
  protected abstract TipoSindicacion getTipoSindicacion();

  /**
   * Parsea los feeds ATOM para extraer las entradas (entries) y otros datos relevantes,
   * actualizando el log y estadísticas asociados.
   *
   * <p>
   * Método abstracto que debe implementar la subclase para definir la lógica específica de parseo
   * según la fuente o formato.
   * </p>
   *
   * @throws MiParseException si ocurre un error durante el parseo.
   */
  protected abstract void parsearAtomsFeeds()
      throws MiParseException;

  /**
   * Obtiene el lugar de importación de los datos, que puede ser local o desde internet.
   * <p>
   * Método abstracto que debe implementar la subclase para definir el lugar de importación
   * correspondiente.
   * </p>
   *
   * @return LugarImportacion que indica el origen de los datos.
   */
  protected abstract LugarImportacion getLugarImportacion();

  /**
   * Método principal que ejecuta el flujo completo de procesamiento: carga de configuración,
   * inicialización, parseo, persistencia, envío de notificaciones y gestión de errores.
   * <p>
   * Controla y registra logs detallados, maneja excepciones específicas, y finaliza el programa
   * según el resultado.
   * </p>
   */
  protected void procesar(String configDir) {

    try {

      // Obtener la instancia singleton
      Version versionService = new VersionImpl();
      log.info("[procesar] - versionSevice creado correctamente.");

      propertiesManager = PropertiesManagerServiceImpl.getInstance();
      log.info("[procesar] - propertiesManager creado correctamente.");

      propertiesManager.setConfigDir(configDir);
      log.info("[procesar] - Ruta ficheros properties: /{}.", configDir);

      // === Configuración inicial ===
      Set<String> clavesSensibles = Set.of("password");
      propertiesManager.setSensitiveKeys(clavesSensibles);  // Ahora se aplica sobre la instancia
      log.info("[procesar] - Conjunto de claves sensibles definidas: {}", clavesSensibles);

      // Cargar todas las propiedades desde el directorio de configuración
      propertiesManager.loadAllProperties();
      log.info("[procesar] - Lectura correcta de los fichereos properties.");

      // Obtengo y Muestro el valor de la key dentro del properties que tiene el nombre del aplicativo
      appName = propertiesManager.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_NAME);
      log.info("[procesar] - AppName: {}.", appName);

      // Obtengo y muestro el valor de la versión de la aplicación
      appVersion = versionService.getVersion(AbstractOpenData.class);
      log.info("[procesar] - AppVersion: {}.", appVersion);

      // Creo el objeto Log para esta ejecución
      VariablesGlobales.setLugarImportacion(getLugarImportacion());
      log.info("[procesar] - LugarImportación: {}.", VariablesGlobales.getLugarImportacion());

      VariablesGlobales.setTipoSindicacion(getTipoSindicacion());
      log.info("[procesar] - TipoSindicacion: {}.", VariablesGlobales.getTipoSindicacion());

      // Si el lugar de importación es INTERNET entonces tengo que obtener el Map con los Entry y de paso
      //      el NewestEntry
      if (VariablesGlobales.getLugarImportacion().equals(LugarImportacion.INTERNET)) {

        VariablesGlobales.setMapEntriesFromBaseDatos(getMapEntryFromBaseDatos());
        VariablesGlobales.setNewestEntry(
            obtenerUltimaEntrada(VariablesGlobales.getMapEntriesFromBaseDatos()));
      }

      Log miLog = new Log(VariablesGlobales.getLugarImportacion(),
                          VariablesGlobales.getTipoSindicacion());
      log.info("[procesar] - Creación correcta del objeto {}.", miLog);

      // Creo el objeto Estadistica que se inicializa con el Log anteriormente creado y con el
      //         valor LocalDateTime.now() para el campo fechaHoraInicial
      Estadistica estadistica = new Estadistica(miLog);
      log.info("[procesar] - Creación correcta del objeto Estadistica.");

      //
      LocalDateTime fechaHoraInicial = LocalDateTimeHelper.getLocalDateTimeNow();

      // Cargo los filtros que se amplican y los imprimo
      FiltroHelper.loadFilters();
      log.info("[procesar] - Cargados correctamente los filtros que se aplican en esta ejecución.");

      // Creo el objeto Configuracion
      Configuracion configuracion = new Configuracion(miLog);
      log.info("[procesar] - Creación correcta del objeto {}.", configuracion);

      // Cargo la configuración
      miLog.setConfiguracion(configuracion);
      log.info("[procesar] - Asignado el objeto configuración al Log correctamente.");

      // Parseo de los Feeds según el tipo de sindicación.
      //      La información que se genera se almacena en VariablesGlobales.MapsEntriesFromAtoms
      log.info("[procesar] - **** INICIO DEL PARSEO DE LOS FICHEROS ATOMS.");
      parsearAtomsFeeds();
      log.info("[procesar] - **** FINALIZADO EL PARSEO DE LOS FICHEROS ATOMS.");

      List<OrganoContratacion> listOrganoContratacion = VariablesGlobales.getMapFiltroSql().entrySet().stream()
          .map(entry -> new OrganoContratacion(miLog, entry.getKey(), entry.getValue()))
          .toList();
      miLog.setListOrganoContratacion(listOrganoContratacion);
      log.info("[procesar] - Asignada la lista de órganos de contratación al log.");

      // Obtengo el Map según la importación se realiza desde Internet o desde Local
      Map<String, Entry> mapEntriesToBaseDatos =
          VariablesGlobales.getLugarImportacion().equals(LugarImportacion.INTERNET)
              ? ((OpenDataInternet) this).getMapEntriesResultantesCompararMapsFromAtosConMapFromBaseDatos()
              : VariablesGlobales.getMapEntriesFromAtoms();
      log.info("[procesar] - Obtenido el Map con los Entries para MERGE en la Base de datos.");

      // Recorro el mapEntriesFromAtoms
      Map<Feed, List<Entry>> mapFeedConEntries = mapEntriesToBaseDatos.values().stream()
          .peek(entry -> entry.getFeed().setMiLog(miLog))  // asigno el log a cada feed
          .collect(Collectors.groupingBy(Entry::getFeed));
      log.info("[procesar] - Agrupar Entry por Feed.");

      // Asigno los Feeds al Log
      mapFeedConEntries.forEach((feed, entries) -> {
        feed.setListEntry(entries);
        miLog.getListFeed().add(feed);
      });
      log.info("[procesar] - Asignar entries a cada feed y asignados al log.");

      // Asigno miLog a cada elemento de la lista de Históricos
      VariablesGlobales.getListHistoricos().forEach(historico -> historico.setMiLog(miLog));
      miLog.setListHistorio(VariablesGlobales.getListHistoricos());
      log.info("[procesar] - Asignar históricos al log.");

      // Obtengo un resumen de la importación que se acaba de realizar previa a la grabación en base de datos
      String nFeeds = StringHelper.getNumeroConFormato(mapFeedConEntries.size());
      String nEntries = StringHelper.getNumeroConFormato(mapEntriesToBaseDatos.size());
      String nHistoricos = StringHelper.getNumeroConFormato(
          VariablesGlobales.getListHistoricos().size());
      log.info("[procesar] - **** RESUMEN DE LA IMPORTACIÓN");
      log.info("[procesar] - Nº Feeds que tienen Entry que cumplen los filtros: {}", nFeeds);
      log.info("[procesar] - Nº Entries que cumplen los filtros: {}", nEntries);
      log.info("[procesar] - Nº Históricos procesados: {}", nHistoricos);

      // La lista de Historico la paso a un mapa para poder realizar filtrado por el tipo de acción realizada
      Map<EntryOpcion, Long> mapHistorico = VariablesGlobales.getListHistoricos().stream()
          .collect(Collectors.groupingBy(Historico::getEntryOpcion,
                                         () -> new EnumMap<>(EntryOpcion.class),
                                         Collectors.counting()));
      log.info("[procesar] - Generado un Map de históricos según la opción.");

      // Asigno los valores al objeto estadistica
      estadistica.setNRegistrosHistoricosInsertar(
          mapHistorico.getOrDefault(EntryOpcion.INSERTAR, 0L));
      estadistica.setNRegistrosHistoricosEliminar(
          mapHistorico.getOrDefault(EntryOpcion.ELIMINAR, 0L));
      estadistica.setNRegistrosHistoricosActualizar(
          mapHistorico.getOrDefault(EntryOpcion.ACTUALIZAR, 0L));
      estadistica.setNRegistrosHistoricosRechazar(
          mapHistorico.getOrDefault(EntryOpcion.RECHAZAR, 0L));
      log.info("[procesar] - Asignados valores a estadísticas.");

      // Transformo los valores a string para su correcta impresión en el log
      logHistorico("INSERTAR", estadistica.getNRegistrosHistoricosInsertar());
      logHistorico("ELIMINAR", estadistica.getNRegistrosHistoricosEliminar());
      logHistorico("ACTUALIZAR", estadistica.getNRegistrosHistoricosActualizar());
      logHistorico("RECHAZAR", estadistica.getNRegistrosHistoricosRechazar());

      // Obtengo la duración, la almaceno en Estadistica y asigno esta a miLog
      LocalDateTime fechaHoraFinal = LocalDateTimeHelper.getLocalDateTimeNow();
      String duracion = LocalDateTimeHelper.getDiferenciaLocalDateTime(fechaHoraInicial,
                                                                       fechaHoraFinal);
      estadistica.setDuracion(duracion);
      miLog.setEstadistica(estadistica);
      log.info("[procesar] - Calculo la duración y asigno la estadísticas al log.");

      //
      //     PERSISTENCIA EN LA BASE DE DATOS
      //
      log.info("[procesar] - ***** INICIO DE LA PERSISTENCIA EN LA BASE DE DATOS *****");

      // CREO LA INSTANCIA DEL SERVICIO ENCARGADO DE INTERACTUAR CON LA BASE DE DATOS
      ServicePrincipal servicePrincipal = new ServicePrincipalImpl();
      String baseDatos = propertiesManager.getProperty(
          PropertiesFiles.JAKARTA_PRINCIPAL, PropertiesKeys.JAKARTA_PERSISTENCE_JDBC_URL);

      log.debug("[procesar] - Cadena de Conexión: {}", baseDatos);

      // PERSISTO EN LA BASE DE DATOS SEGÚN PROVENGAN LOS DATOS (INTERNET | LOCAL)
      servicePrincipal.persistirEnBaseDatos(miLog);
      log.info("[procesar] - Se han persistido correctamente las entidades en la base de datos.");

      // Envío de las estadísticas por correo
      enviarEmail(estadistica, null, true);
      log.info("[procesar] - Email enviado correctamente.");

      // Finalizo el programa correctamente
      finalizar(Mensajes.FINAL_CORRECTO, 0);

    } catch (MiUnknownHostException ex) {
      manejarExcepcion(ex, "[MiUnknownHostException] - ");
    } catch (MiServiceException ex) {
      manejarExcepcion(ex, "[MiServiceException] - ");
    } catch (EmailException ex) {
      manejarExcepcion(ex, "[EmailServiceException] - ");
    } catch (PropertiesManagerException ex) {
      manejarExcepcion(ex, "[PropertiesManaerException] - ");
    } catch (VersionException ex) {
      manejarExcepcion(ex, "[VersionException] - ");
    } catch (RuntimeException ex) {
      manejarExcepcion(ex, "[RuntimeException] - ");
    } finally {
      // Cierra todos los SessionFactory
      SessionFactoryRegistry.closeAll();
    }
  }

  public Entry obtenerUltimaEntrada(Map<String, Entry> entradas) {
    // Usamos un stream para obtener el Entry con el campo "updated" más reciente
    Optional<Entry> ultimaEntrada = entradas.values().stream()
        .max(Comparator.comparing(Entry::getUpdated));

    if (ultimaEntrada.isPresent()) {
      log.info("[obtenerUltimaEntrada] - Se encontró una entrada con fecha: {}", ultimaEntrada.get().getUpdated());
    } else {
      log.warn("[obtenerUltimaEntrada] - No se encontró ninguna entrada (devuelve null).");
    }

    // Si existe, devuelve la entrada, si no, devuelve null
    return ultimaEntrada.orElse(null);
  }

  private void logHistorico(String tipo, Long valor) {
    log.info("[logHistorico] - Nº Históricos ({}): {}", tipo,
             StringHelper.getNumeroConFormato(Math.toIntExact(valor)));
  }
}
