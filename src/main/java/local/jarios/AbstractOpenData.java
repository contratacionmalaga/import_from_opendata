package local.jarios;

import local.jarios.common.util.*;
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
import local.jarios.helpers.*;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Clase abstracta que gestiona el procesamiento general para la importación y análisis
 * de datos OpenData basados en feeds ATOM, incluyendo carga de configuración,
 * parseo, persistencia y notificaciones.
 * <p>
 * Las subclases deben implementar métodos específicos para definir el tipo de sindicacion,
 * lugar de importación y la lógica de parseo de los feeds.
 * </p>
 */
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

    /**
     * Obtiene el tipo de sindicacion que será usado en el proceso.
     * <p>
     * Método abstracto que debe implementar la subclase para indicar
     * el tipo de sindicacion específico.
     * </p>
     *
     * @return TipoSindicacion a utilizar en el proceso.
     */
    protected abstract TipoSindicacion getTipoSindicacion();

    /**
     * Parsea los feeds ATOM para extraer las entradas (entries) y otros datos
     * relevantes, actualizando el log y estadísticas asociados.
     * <p>
     * Método abstracto que debe implementar la subclase para definir
     * la lógica específica de parseo según la fuente o formato.
     * </p>
     *
     * @param tipoSindicacion objeto
     * @param newestEntry objeto {@link Entry} que recopila datos estadísticos.
     * @throws MiParseException si ocurre un error durante el parseo.
     */
    protected abstract void parsearAtomsFeeds(Log miLog, TipoSindicacion tipoSindicacion, Entry newestEntry)
            throws MiParseException;

    /**
     * Obtiene el lugar de importación de los datos, que puede ser local o desde internet.
     * <p>
     * Método abstracto que debe implementar la subclase para definir
     * el lugar de importación correspondiente.
     * </p>
     *
     * @return LugarImportacion que indica el origen de los datos.
     */
    protected abstract LugarImportacion getLugarImportacion();

    /**
     * Método principal que ejecuta el flujo completo de procesamiento:
     * carga de configuración, inicialización, parseo, persistencia,
     * envío de notificaciones y gestión de errores.
     * <p>
     * Controla y registra logs detallados, maneja excepciones específicas,
     * y finaliza el programa según el resultado.
     * </p>
     */
    protected void procesar() {

        // Inicio del log
        log.info("[procesar] - {}", Mensajes.INICIO);

        try {

            // Obtener la instancia singleton
            Version versionService = new VersionImpl();
            log.info("[procesar] - El servicio de consulta de la versión del JAR se ha creado correctamente.");

            propertiesManager = PropertiesManagerServiceImpl.getInstance();
            log.info("[procesar] - El servicio de consulta de los ficheros .properties se ha creado correctamente.");

            propertiesManager.setConfigDir(Constantes.CONFIG_DIR);
            log.info("[procesar] - Directorio donde se encuentran los ficheros .properties: {}", Constantes.CONFIG_DIR);

            // === Configuración inicial ===
            Set<String> clavesSensibles = Set.of("password");
            propertiesManager.setSensitiveKeys(clavesSensibles);  // Ahora se aplica sobre la instancia
            log.info("[procesar] - Conjunto de claves sensibles: {}", clavesSensibles);

            // Cargar todas las propiedades desde el directorio de configuración
            propertiesManager.loadAllProperties();
            log.info("[procesar] - Leídas todas las propiedades de todos los ficheros.");

            List<String> listFicheros = propertiesManager.getListFiles();
            log.info("[procesar] - Ficheros cargados correctamente: {}", listFicheros);

            // Obtengo y Muestro el valor de la key dentro del properties que tiene el nombre del aplicativo
            appName = propertiesManager.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_NAME);
            log.info("[procesar] - AppName: {}", appName);

            // Obtengo y muestro el valor de la versión de la aplicación
            appVersion = versionService.getVersion(AbstractOpenData.class);
            log.info("[procesar] - AppVersion: {}", appVersion);

            // Creo el objeto Log para esta ejecución
            LugarImportacion lugarImportacion = getLugarImportacion();
            log.info("[procesar] - LugarImportación: {}", lugarImportacion);

            TipoSindicacion tipoSindicacion = getTipoSindicacion();
            log.info("[procesar] - TipoSindicacion: {}", tipoSindicacion);

            Log miLog = new Log(lugarImportacion, tipoSindicacion);
            log.info("[procesar] - Creación correcta del objeto {}", miLog);

            // Creo el objeto Estadistica que se inicializa con el Log anteriormente creado y con el
            //         valor LocalDateTime.now() para el campo fechaHoraInicial
            Estadistica estadistica = new Estadistica(miLog);
            log.info("[procesar] - Creación correcta del objeto {}", estadistica);

            //
            LocalDateTime fechaHoraInicial =  LocalDateTimeHelper.getLocalDateTimeNow();

            // Cargo los filtros que se amplican y los imprimo
            log.info("[procesar] - Cargamos los filtros asociados a esta ejecución.");
            FiltroHelper.loadFilters();

            // Asigno la lista de órganos de contratación a miLog
            miLog.setListOrganoContratacion(
                    VariablesGlobales
                            .getMapFiltroSql()
                            .entrySet()
                            .stream()
                            .map(e -> new OrganoContratacion(miLog, e.getKey(), e.getValue()))
                            .collect(Collectors.toList()));
            log.info("[procesar] - Asignados los órganos de contratación del filtro Sql al Log.");

            // Creo el objeto Configuracion
            Configuracion configuracion = new Configuracion(miLog);
            log.info("[procesar] - Creación correcta del objeto {}", configuracion);

            // Cargo la configuración
            miLog.setConfiguracion(configuracion);
            log.info("[procesar] - Asignado el objeto configuración al Log correctamente.");

            // Obtengo el newestEntry en caso de que la importación sea desde INTERNET (para LOCAL es null)
            Entry newestEntry = null;
            if (lugarImportacion.equals(LugarImportacion.INTERNET)) {
                newestEntry = EntryHelper.getNewestEntry(tipoSindicacion);
                if (newestEntry != null) {
                    log.info("[procesar] - NewestEntry: {}", newestEntry.toStringResumido());
                }
            }

            // Parseo de los Feeds
            log.info("[procesar] - Inicio del parseo de los ficheros ATOM.");
            parsearAtomsFeeds(miLog, tipoSindicacion, newestEntry);

            Map<String, Entry> mapEntriesFromAtoms;

            if (lugarImportacion.equals(LugarImportacion.INTERNET)) {
                mapEntriesFromAtoms = ((OpenDataInternet) this).getResultado();

            } else {
                // Obtengo el Map con los datos del Entry de la Base de Datos
                mapEntriesFromAtoms = VariablesGlobales.getMapEntriesFromAtoms();
            }

            String valor = StringHelper.getNumeroConFormato(mapEntriesFromAtoms.size());
            log.info("[procesar] - Nº Entries from Atoms que cumplan los filtros: {}", valor);

            // Mapa auxiliar para agrupar Feeds por su lista de Entry
            Map<Feed, List<Entry>> feedEntryMap = new HashMap<>();

            // Recorro el mapEntriesFromAtoms
            for (Entry entry : mapEntriesFromAtoms.values()) {

                // Obtengo el feed del Entry
                Feed feed = entry.getFeed();
                feed.setMiLog(miLog);

                // Si no es null lo añado a la lista
                feedEntryMap.computeIfAbsent(feed, k -> new ArrayList<>()).add(entry);

                log.info("[procesar] - Asociado el {} al {}", entry.toStringResumido(), feed.toStringResumido());
            }

            // Asociar feeds y entries a miLog
            valor = StringHelper.getNumeroConFormato(feedEntryMap.size());
            log.info("[procesar] - Nº Feeds con Entries que cumplan los filtros: {}", valor);

            // Recorro el map con los Feeds resultantes
            for (Map.Entry<Feed, List<Entry>> map : feedEntryMap.entrySet()) {

                // Obtengo el Feed a partir del Map
                Feed feed = map.getKey();

                // Obteng la lista de Entry a partir del Map
                List<Entry> entries = map.getValue();

                // Asignar miLog al feed
                feed.setMiLog(miLog);

                // Asignar las entries al feed
                feed.setListEntry(entries);

                // Asigno a cada Entry el Feed
                for (Entry e : entries) {
                    e.setFeed(feed);
                }

                // Añado el feed a la lista de feeds del Log
                miLog.getListFeed().add(feed);
                log.info("[procesar] - Asociado {} a {}", feed.toStringResumido(), miLog);
            }

            // ASIGNO LOS HISTÓRICOS DE ACCIONES QUE HAN OCURRIDO SOBRE CADA ENTRY AL LOG
            List<Historico> listHistoricos = VariablesGlobales.getListHistoricos();
            miLog.setListHistorio(listHistoricos);
            String nHistoricos = StringHelper.getNumeroConFormato(VariablesGlobales.getListHistoricos().size());
            log.info("[procesar] - Asigno los históricos al Log. Nº Históricos: {}", nHistoricos);

            // La lista de Historico la paso a un mapa para poder realizar filtrado por el tipo de acción realizada
            Map<EntryOpcion, Long> mapHistorico = listHistoricos
                                                        .stream()
                                                        .collect(
                                                                Collectors
                                                                        .groupingBy(
                                                                                Historico::getEntryOpcion,
                                                                                Collectors.counting()));
            log.info ("[procesar] - Generado MapHistorico a partir de ListHistorico");

            // Asigno valores según el tipo de acción almacenada en el map de historico de OC
            estadistica.setNRegistrosHistoricosInsertar(mapHistorico.getOrDefault(EntryOpcion.INSERTAR, 0L));
            estadistica.setNRegistrosHistoricosEliminar(mapHistorico.getOrDefault(EntryOpcion.ELIMINAR, 0L));
            estadistica.setNRegistrosHistoricosActualizar(mapHistorico.getOrDefault(EntryOpcion.ACTUALIZAR, 0L));
            estadistica.setNRegistrosHistoricosRechazar(mapHistorico.getOrDefault(EntryOpcion.RECHAZAR, 0L));

            // Obtengo la duración, la almaceno en Estadistica y asigno esta a miLog
            LocalDateTime fechaHoraFinal =  LocalDateTimeHelper.getLocalDateTimeNow();
            String duracion = LocalDateTimeHelper.getDiferenciaLocalDateTime(fechaHoraInicial, fechaHoraFinal);
            estadistica.setDuracion(duracion);
            miLog.setEstadistica(estadistica);

            //
            //     PERSISTENCIA EN LA BASE DE DATOS
            //
            log.info("[procesar] - ***** INICIO DE LA PERSISTENCIA EN LA BASE DE DATOS *****");

            // CREO LA INSTANCIA DEL SERVICIO ENCARGADO DE INTERACTUAR CON LA BASE DE DATOS
            ServicePrincipal servicePrincipal = new ServicePrincipalImpl();
            String baseDatos = propertiesManager.getProperty(
                    PropertiesFiles.JAKARTA_PRINCIPAL, PropertiesKeys.JAKARTA_PERSISTENCE_JDBC_URL);

            log.info("[procesar] - Cadena de Conexión: {}", baseDatos);

            // PERSISTO EN LA BASE DE DATOS SEGÚN PROVENGAN LOS DATOS (INTERNET | LOCAL)
            servicePrincipal.persistirEnBaseDatos(miLog);
            log.info("[procesar] - Se han persistido correctamente las entidades en la base de datos.");

            log.info("[procesar] - {}", estadistica.toString());

            // Envío de las estadísticas por correo
            enviarEmail(estadistica, null, true);
            log.info("[procesar] - Email enviado correctamente.");

            // Finalizo el programa correctamente
            finalizar (Mensajes.FINAL_CORRECTO, 0);

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
            Properties emailProps = propertiesManager.getProperties(PropertiesFiles.MAIL);
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
            log.info("[finalizar] - {}", mensaje);
        } else {
            log.error("[finalizar] - {} (Código de salida: {})", mensaje, exitCode);
        }

        log.info("[finalizar] - {}", Mensajes.FINAL); // Se asume que FINAL es una constante tipo String
        System.exit(exitCode);
    }
}
