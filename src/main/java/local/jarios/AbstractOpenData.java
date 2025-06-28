package local.jarios;

import local.jarios.common.util.PropertiesKeys;
import local.jarios.email.api.EmailSender;
import local.jarios.email.api.EmailSenderImpl;
import local.jarios.email.api.EmailService;
import local.jarios.email.api.EmailServiceImpl;
import local.jarios.email.exception.EmailException;
import local.jarios.email.helper.EmailHelper;
import local.jarios.email.model.EmailData;
import local.jarios.email.validator.EmailRequestValidator;
import local.jarios.encryptor.exception.EncryptorException;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoConexion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiServiceException;
import local.jarios.exceptions.MiUnknownHostException;
import local.jarios.helpers.*;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import local.jarios.services.Service;
import local.jarios.services.ServiceImpl;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.Mensajes;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.version.api.Version;
import local.jarios.version.api.VersionImpl;
import local.jarios.version.exception.VersionException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public abstract class AbstractOpenData {

    /**
     * Servicio de gestión de propiedades de configuración.
     * <p>
     * Se obtiene como instancia singleton mediante {@link PropertiesManagerServiceImpl#getInstance()}.
     * Permite cargar, acceder y gestionar propiedades definidas en ficheros externos.
     * </p>
     */
    public static PropertiesManagerService propertiesManager = null;

    /**
     * Nombre de la aplicación, cargado desde las propiedades externas.
     * <p>
     * Se obtiene desde el fichero de configuración a través de {@code propertiesManager}
     * utilizando la clave {@code Constantes.KEY_APP_NAME}.
     * </p>
     */
    public static String appName = null;

    /**
     * Versión de la aplicación en ejecución.
     * <p>
     * Se determina mediante el componente {@link local.jarios.version.api.Version}
     * que analiza los metadatos del JAR en ejecución.
     * </p>
     */
    public static String appVersion = null;

    //
    protected abstract TipoSindicacion getTipoSindicacion();

    //
    protected abstract List<Feed> parsearFeeds(Log log, Feed newestFeed, Estadistica estadistica)
            throws MiParseException;

    // Método que determina el lugar de importación (Local o Internet)
    protected abstract LugarImportacion getLugarImportacion();

    //
    protected void procesar() {

        // Inicio del log
        log.info(Mensajes.INICIO);

        try {

            // Obtener la instancia singleton
            Version versionService = new VersionImpl();
            log.info("El servicio de consulta de la versión del JAR se ha creado correctamente.");

            propertiesManager = PropertiesManagerServiceImpl.getInstance();
            log.info("El servicio de consulta de los ficheros properties se ha creado correctamente con los valores por defecto.");

            propertiesManager.setConfigDir(Constantes.CONFIG_DIR);
            log.info("Directorio configurado: {}", Constantes.CONFIG_DIR);

            // === Configuración inicial ===
            Set<String> clavesSensibles = Set.of("password");
            propertiesManager.setSensitiveKeys(clavesSensibles);  // Ahora se aplica sobre la instancia
            log.info("Establezco el conjunto de claves Sensibles: {}", clavesSensibles);

            // Configurar clave secreta
            propertiesManager.setSecretKey(Constantes.ENCRYPT_PASSWORD);
            log.info("Clave secreta configurada correctamente");

            // Cargar todas las propiedades desde el directorio de configuración
            propertiesManager.loadAllProperties();
            log.info("Leídas todas las propiedades de todos los ficheros.");

            List<String> listFicheros = propertiesManager.getListFiles();
            log.info("Ficheros cargados correctamente desde el directorio '{}'. Lista ficheros: {}", Constantes.CONFIG_DIR, listFicheros);

            // Muestro el valor de APP_NAME
            appName = propertiesManager.getProperty(Constantes.APP_PROPERTIES, Constantes.KEY_APP_NAME);
            log.info("AppName: {}", appName);

            // Obtengo y muestro el valor de APP_VERSION
            appVersion = versionService.getVersion(AbstractOpenData.class);
            log.info("AppVersion: {}", appVersion);

            // Creo el objeto Log para esta ejecución
            LugarImportacion lugarImportacion = getLugarImportacion();
            log.info("LugarImportación: {}", lugarImportacion);

            TipoSindicacion tipoSindicacion = getTipoSindicacion();
            log.info("TipoSindicacion: {}", tipoSindicacion);

            Log miLog = new Log(lugarImportacion, tipoSindicacion);
            log.info(Mensajes.LOG_CREACION, miLog);

            // Creo el objeto Estadistica que se inicializa con el Log anteriormente creado y con el
            //         valor LocalDateTime.now() para el campo fechaHoraInicial
            Estadistica estadistica = new Estadistica(miLog);
            log.info(Mensajes.ESTADISTICA_CREACION);

            // Cargo los filtros que se amplican
            FiltroHelper.loadFilters();

            //
            FiltroHelper.printFilters();

            // Creo el objeto Configuracion
            Configuracion configuracion = new Configuracion(miLog);
            log.info(Mensajes.CONFIGURACION_CREACION);

            //
            miLog.setConfiguracion(configuracion);
            log.info(Mensajes.ASIGN_CONFIGURACION_TO_LOG);

            Feed newestFeed = null;

            // Obtengo el NewestFeed para el caso en que esté importando desde INTERNET
            if (lugarImportacion == LugarImportacion.INTERNET) {
                newestFeed = FeedHelper.getNewestFeed(tipoSindicacion);
                log.info("Obtenido el Newest feed: {}", newestFeed);
            }

            //
            //     COMIENZO EL PARSEO DE LOS FEEDS
            //

            // Parseo de los Feeds
            List<Feed> listFeedEntities = parsearFeeds(miLog, newestFeed, estadistica);
            log.info(Mensajes.FIN_PARSEO_FICHEROS_ATOM);

            // Asignar lista de feeds al log
            miLog.setListFeed(listFeedEntities);
            log.info("Feeds asignadas al Log: {}", listFeedEntities.size());

            // Asignar la lista de Órganos de Contratación del Filtro al log
            List<OrganoContratacion> listOrganosContratacion =
                    OrganoContratacionHelper.getListOrganoContratacion(miLog, VariablesGlobales.getMapFiltro());
            miLog.setListOrganoContratacion(listOrganosContratacion);
            log.info("Órganos de Contratación asignados al Log: {}", listOrganosContratacion.size());

            //
            //     FINAL DEL PARSEO DE LOS FEEDS
            //

            // Asigno la fecha y hora final del parseo
            LocalDateTime localDateTime = LocalDateTimeHelper.getLocalDateTimeNow();
            estadistica.setFechaHoraFinal(localDateTime);

            // Calculo el tiempo de ejecución del parseo
            String duracion = LocalDateTimeHelper.getDiferenciaLocalDateTime(
                    estadistica.getFechaHoraInicial(),
                    estadistica.getFechaHoraFinal());
            estadistica.setDuracion(duracion);
            log.info("Duración del parseo: {}", duracion);

            //
            //     VERIFICACIÓN DEL MAP
            //
            Map<String, Entry> map = VariablesGlobales.getMapBaseDatos();
            MapHelper.printMap(map);

            //
            //     PERSISTENCIA EN LA BASE DE DATOS
            //

            // Persistir en la base de datos
            log.info(Mensajes.INICIO_PERSISTENCIA_FICHEROS_ATOM);

            //
            //     CREO LA INSTANCIA DEL SERVICIO ENCARGADO DE INTERACTUAR CON LA BASE DE DATOS
            //
            log.info(Mensajes.SERVICE_CREACION_INICIO);
            log.info(propertiesManager.getProperty(Constantes.HIBERNATE_PROPERTIES, PropertiesKeys.JAKARTA_URL));
            Service service = new ServiceImpl(TipoConexion.PRINCIPAL);
            log.info(Mensajes.SERVICE_CREACION_CREADO, TipoConexion.PRINCIPAL);

            // Persisto el objeto Log -> Configuracion + List<OrganoContratacion>
            service.persistirLog(miLog, VariablesGlobales.getMapBaseDatos(), lugarImportacion);
            log.info(Mensajes.PERSISTIDO_LOG_CONFIGURACION_LIST_ORGANOS_CONTRATACION);

            //
            enviarEmail(estadistica, null, true);
            log.info("Email enviado correctamente.");

            // Finalizo el programa correctamente
            finalizar (Mensajes.FINAL_CORRECTO, 0);

        } catch (MiUnknownHostException ex) {
            manejarExcepcion(ex, "[MiUnknownHostException] - ");
        } catch (MiServiceException ex) {
            manejarExcepcion(ex, "[MiServiceException] - ");
        } catch (EncryptorException ex) {
            manejarExcepcion(ex, "[EncryptorException] - ");
        } catch (EmailException ex) {
            manejarExcepcion(ex, "[EmailServiceException] - ");
        } catch (PropertiesManagerException ex) {
            manejarExcepcion(ex, "[PropertiesManaerException] - ");
        } catch (VersionException ex) {
            manejarExcepcion(ex, "[VersionException] - ");
        } catch (RuntimeException ex) {
            manejarExcepcion(ex, "[RuntimeException] - ");
        }
    }

    /**
     * Maneja de forma centralizada las excepciones que ocurren durante la ejecución del programa.
     * <p>
     * Este método registra el mensaje de error proporcionado, imprime el stack trace del error
     * con sangría personalizada, intenta enviar un email con el detalle del error,
     * y finaliza el programa indicando un error en la ejecución.
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

            log.error("[manejarExcepcion] -Error inesperado al intentar enviar email de fallo: {}", e.getMessage());
        }

        finalizar(Mensajes.FINAL_ERRONEO, 1);
    }

    /**
     * Envía un email con el asunto y cuerpo indicados usando la configuración de propiedades.
     * <p>
     * Método común para centralizar el envío de emails evitando duplicidad.
     * Controla excepciones relacionadas con el envío y las registra.
     * </p>
     *
     * @param estadistica            Asunto del email a enviar.
     * @param ex        Cuerpo del email en formato HTML.
     * @param success        Cuerpo del email en formato HTML.
     */
    private static void enviarEmail(Estadistica estadistica, Exception ex, boolean success) {
        try {

            // Configuración del servidor SMTP
            Properties emailProps = propertiesManager.getProperties(Constantes.EMAIL_PROPERTIES);
            log.info("[enviarEmail] - Properties cargadas correctamente.");

            // Construcción de los datos del correo
            EmailData emailData = construirEmailData(estadistica, ex, success);
            log.info("[enviarEmail] - EmailData creado correctamente.");

            EmailRequestValidator.validarEmailRequest(emailProps, emailData);
            log.info("[enviarEmail] - Properties e EmailData validados correctamente.");

            // Creación del servicio de correo con la implementación de envío SMTP
            EmailSender emailSender = new EmailSenderImpl();
            log.info("[enviarEmail] - Creación del objeto EmailSender correctamente.");

            EmailService emailService = new EmailServiceImpl(emailSender);
            log.info("[enviarEmail] - Creado el objeto EmailService correctamente.");

            // Envío del correo
            emailService.sendEmail(emailProps, emailData);
            log.info("[enviarEmail] - Correo enviado correctamente.");

        } catch (EmailException e) {
            log.error("[enviarEmail] - No se pudo enviar el email: Error en el servicio de correo -> {}", e.getMessage());
            throw new EmailException ("No se pudo enviar el email: Error en el servicio de correo", e);
        }
    }

    /**
     * Construye un objeto {@link EmailData} con toda la información necesaria para el envío de un correo,
     * en función del resultado del proceso (éxito o error).
     * <p>
     * Utiliza la configuración cargada desde el sistema de propiedades para establecer remitente y destinatario.
     * El asunto y el cuerpo del mensaje se generan usando las utilidades de {@link EmailHelper}.
     * </p>
     *
     * @param estadistica Objeto {@link Estadistica} que contiene datos del proceso. Puede ser {@code null} en caso de error.
     * @param ex Excepción lanzada durante la ejecución, en caso de fallo. Puede ser {@code null} si el proceso fue exitoso.
     * @param success Indicador booleano que señala si el proceso finalizó correctamente ({@code true}) o con error ({@code false}).
     * @return Objeto {@link EmailData} completamente inicializado y listo para ser enviado.
     * @throws EmailException Si ocurre un error al obtener el nombre del host o las propiedades necesarias.
     */
    private static EmailData construirEmailData(Estadistica estadistica, Exception ex, boolean success) {

        try {

            String equipo = ComunHelper.getHostName();
            log.debug("[construirEmailData] - Equipo desde el que se envía el email: {}", equipo);

            String from = propertiesManager.getProperty(Constantes.EMAIL_PROPERTIES, Constantes.KEY_EMAIL_FROM);
            log.debug("[construirEmailData] - Remitente: {}", from);

            String to = propertiesManager.getProperty(Constantes.EMAIL_PROPERTIES, Constantes.KEY_EMAIL_TO);
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
            log.error("[construirEmailData] - Error al obtener el nombre del host. Error: {}", e.getMessage());
            throw new EmailException("Error al obtener el nombre del host.", e);

        } catch (PropertiesManagerException e) {
            log.error("Error al leer las propiedades. Error: {}", e.getMessage());
            throw new EmailException("Error al leer las propiedades.", e);
        }
    }

    /**
     * Convierte los campos de una instancia de {@link Estadistica} en una matriz de cadenas de texto.
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
     * Cada elemento del arreglo representa una línea del stack trace, tal como se imprimiría
     * en un log o consola. Este método es útil para enviar errores por correo o almacenarlos
     * en sistemas donde no se puede registrar el {@code Throwable} directamente.
     * </p>
     *
     * @param ex la excepción de la cual se extrae el stack trace
     * @return un arreglo de {@code String} que representa línea por línea el stack trace
     */
    public static String[] obtenerStackTraceComoArray(Throwable ex) {
        StackTraceElement[] elementos = ex.getStackTrace();
        log.debug("[obtenerStackTraceComoArray] - Obtenidos los elementos del StactTrace. Nº elementos: {}", elementos.length);
        String[] resultado = new String[elementos.length];
        log.debug("[obtenerStackTraceComoArray] - Defino un String[] con el número de elementos del StackTrace.");
        for (int i = 0; i < elementos.length; i++) {
            resultado[i] = elementos[i].toString();
            log.debug("[obtenerStackTraceComoArray] - Elemento: {} - {}", i, elementos[i].toString());
        }
        return resultado;
    }

    /**
     * Finaliza la ejecución del programa mostrando un mensaje de log
     * y llamando a System.exit con el código proporcionado.
     *
     * @param mensaje  Mensaje que se mostrará en el log.
     * @param exitCode Código de salida del sistema:
     *                 0 para éxito, 1 para error. Otros valores también serán aceptados.
     */
    public static void finalizar(String mensaje, int exitCode) {
        if (exitCode == 0) {
            log.info(mensaje);
        } else {
            log.error("[finalizar] - {} (Código de salida: {})", mensaje, exitCode);
        }

        log.info(Mensajes.FINAL); // Se asume que FINAL es una constante tipo String
        System.exit(exitCode);
    }
}
