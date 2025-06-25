package local.jarios;

import local.jarios.entity.Log;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoConexion;
import local.jarios.enums.TipoFinalEjecucion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiMailException;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiUnknownHostException;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.FiltroHelper;
import local.jarios.helpers.OrganoContratacionHelper;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.common.util.FinalDelPrograma;
import local.jarios.services.Service;
import local.jarios.services.ServiceImpl;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.Mensajes;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.version.api.Version;
import local.jarios.version.api.VersionImpl;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
public abstract class AbstractOpenData {

    /**
     * Servicio de gestión de propiedades de configuración.
     * <p>
     * Se obtiene como instancia singleton mediante {@link PropertiesManagerServiceImpl#getInstance()}.
     * Permite cargar, acceder y gestionar propiedades definidas en ficheros externos.
     * </p>
     */
    public static final PropertiesManagerService propertiesManager = PropertiesManagerServiceImpl.getInstance();

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
    protected abstract List<Feed> parsearFeeds(
            Log log, Feed newestFeed, Estadistica estadistica)
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
            List<String> listFicheros = propertiesManager.getListFiles();
            log.info("Ficheros cargados correctamente desde el directorio '{}'. Lista ficheros: {}", Constantes.CONFIG_DIR, listFicheros);

            // Muestro el valor de APP_NAME
            appName = propertiesManager.getProperty(Constantes.APP_PROPERTIES, Constantes.KEY_APP_NAME);
            log.info("AppName: {}", appName);

            // Obtengo y muestro el valor de APP_VERSION
            appVersion = versionService.getVersion(VersionDemo.class);
            log.info("AppVersion: {}", appVersion);

            // Creo el objeto Log para esta ejecución
            LugarImportacion lugarImportacion = getLugarImportacion();
            TipoSindicacion tipoSindicacion = getTipoSindicacion();
            Log miLog = new Log(lugarImportacion, tipoSindicacion);
            log.info(Mensajes.LOG_CREACION, miLog);

            // Creo el objeto Estadistica que se inicializa con el Log anteriormente creado y con la
            //         el valor Timestamp.valueOf(LocalDateTime.now()) para el campo fechaHoraInicial
            Estadistica estadistica = new Estadistica(miLog);
            log.info(Mensajes.ESTADISTICA_CREACION);

            // Imprimo los filtros que se aplican
            imprimirFiltros();

            //
            //     CREO LA INSTANCIA DEL SERVICIO ENCARGADO DE INTERACTUAR CON LA BASE DE DATOS
            //
            log.info(Mensajes.SERVICE_CREACION_INICIO);
            log.info(propertiesManager.getProperty(Constantes.HIBERNATE_PROPERTIES, Constantes.LOCAL_URL));
            Service service = new ServiceImpl(TipoConexion.PRINCIPAL);
            log.info(Mensajes.SERVICE_CREACION_CREADO, TipoConexion.PRINCIPAL);

            //
            //     CREO LA INSTANCIA DEL SERVICIO DEL FILTRO SQL EN CASO DE NO SER VACÍA LA VARIABLE
            //
            if (!propertiesManager.getProperty(Constantes.FILTER_PROPERTIES, Constantes.FILTRO_SQL).isBlank()) {
                VariablesGlobales.setMapFiltro(FiltroHelper.getMapFiltroSql());
                log.info("Asignado el Map a Variables Globales");
            }

            // Creo el objeto Configuracion
            Configuracion configuracion = new Configuracion(miLog);
            log.info(Mensajes.CONFIGURACION_CREACION);

            //
            miLog.setConfiguracion(configuracion);
            log.info(Mensajes.ASIGN_CONFIGURACION_TO_LOG);

            // Obtengo el NewestFeed para el caso en que esté importando desde INTERNET
            Feed newestFeed = service.getNewestFeed(tipoSindicacion);

            //
            //     COMIENZO EL PARSEO DE LOS FEEDS
            //

            // Parseo de los Feeds
            List<Feed> listFeedEntities = parsearFeeds(miLog, newestFeed, estadistica);
            log.info(Mensajes.FIN_PARSEO_FICHEROS_ATOM);

            // Asignar lista de feeds al log
            miLog.setListFeed(listFeedEntities);
            log.info(Mensajes.ASIGN_LIST_FEED_TO_LOG);

            // Asignar la lista de Órganos de Contratación del Filtro al log
            List<OrganoContratacion> listOrganosContratacion =
                    OrganoContratacionHelper.getListOrganoContratacion(miLog, VariablesGlobales.getMapFiltro());
            miLog.setListOrganoContratacion(listOrganosContratacion);
            log.info(Mensajes.ASIGN_LIST_ORGANOS_CONTRATACION_TO_LOG);

            //
            //     FINAL DEL PARSEO DE LOS FEEDS
            //

            // Asigno la fecha y hora final del parseo
            timestamp = Timestamp.valueOf(LocalDateTime.now());
            estadistica.setFechaHoraFinalParseo(timestamp);
            log.info(Mensajes.ASIGN_FECHA_HORA_FINAL_PARSEO_TO_ESTADISTICA);

            // Calculo el tiempo de ejecución del parseo
            String duracion = ComunHelper.calcularTiempoEjecucion(
                    estadistica.getFechaHoraInicialParseo(),
                    estadistica.getFechaHoraFinalParseo());

            // Asigno la duración al objeto Estadistica
            estadistica.setDuracionParseo(duracion);
            log.info(Mensajes.ASIGN_DURACION_PARSEO, duracion);

            //
            //     PERSISTENCIA EN LA BASE DE DATOS
            //

            // Establecer fecha de inicio de la grabación en la base de datos
            timestamp = Timestamp.valueOf(LocalDateTime.now());
            estadistica.setFechaHoraInicialBaseDatos(timestamp);
            log.info(Mensajes.ASIGN_FECHA_HORA_INICIAL_BASE_DATOS_TO_ESTADISTICA);

            // Persistir en la base de datos
            log.info(Mensajes.INICIO_PERSISTENCIA_FICHEROS_ATOM);

            // Persisto el objeto Log -> Configuracion + List<OrganoContratacion>
            service.persistir(miLog);
            log.info(Mensajes.PERSISTIDO_LOG_CONFIGURACION_LIST_ORGANOS_CONTRATACION);

            // Persistir el Map con los Entrys en la base de datos
            service.persistir(VariablesGlobales.getMapBaseDatos(), lugarImportacion);
            log.info(Mensajes.PERSISTIDO_MAP_ENTRIES, VariablesGlobales.getMapBaseDatos().size());

            // Establecer fecha final de grabación en la base de datos
            timestamp = Timestamp.valueOf(LocalDateTime.now());
            estadistica.setFechaHoraFinalBaseDatos(timestamp);
            log.info(Mensajes.ASIGN_FECHA_HORA_FINAL_BASE_DATOS_TO_ESTADISTICA);

            //
            //     OBTENGO LAS ESTADÍSTICAS DE DURACIÓN DE LA GRABACIÓN EN LA BASE DE DATOS
            //

            // Calculo el tiempo de ejecución con el formato deseado
            duracion = ComunHelper.calcularTiempoEjecucion(
                    estadistica.getFechaHoraInicialBaseDatos(),
                    estadistica.getFechaHoraFinalBaseDatos());

            // Asigno la duración al objeto Estadistica
            estadistica.setDuracionBaseDatos(duracion);
            log.info(Mensajes.ASIGN_DURACION_BASE_DATOS, duracion);

            // Persisto las estadisticas en la base de datos
            service.persistir(estadistica);
            log.info(Mensajes.PERSISTIDO_ESTADISTICA);
            log.info(Mensajes.FIN_PERSISTENCIA_FICHEROS_ATOM);

            // Creación del objeto Mail a partir de las Estadísticas para el envío de la información
            MiMail miMail = new MiMail(propertyManager, estadistica, miLog);
            log.info(Mensajes.MAIL_CREACION);

            // Envío un correo con la información de la ejecución del aplicativo
            miMail.enviarEmail();
            log.info(Mensajes.MAIL_ENVIADO);

            // Finalizar el programa correctamente
            FinalDelPrograma.finalizar(TipoFinalEjecucion.CORRECTO);

        } catch (MiMailException | MiSessionFactoryProviderException | MiUnknownHostException ex) {

            // Registro la excepción
            log.error(Mensajes.EXCEPTION);
            log.error(Mensajes.EXCEPTION_MENSAJE, Constantes.TABULADOR_1, ex.getMessage());
            log.error(Mensajes.EXCEPTION_STACK_TRACE, Constantes.TABULADOR_1);

            //
            for (StackTraceElement stackTraceElement : ex.getStackTrace()) {
                log.error("{}{}", Constantes.TABULADOR_2, stackTraceElement.toString());
            }

            // Finalizo la ejecución del programa
            FinalDelPrograma.finalizar(TipoFinalEjecucion.ERROR);

        }
    }

    /**
     *
     */
    public static void imprimirFiltros() {

        //
        String filtroSql = propertiesManager.getProperty(Constantes.FILTER_PROPERTIES, PropertyConstantes.FILTRO_SQL);
        String filtroNuts = propertiesManager.getProperty(Constantes.FILTER_PROPERTIES, PropertyConstantes.FILTRO_NUTS);
        String filtroObjeto = propertiesManager.getProperty(Constantes.FILTER_PROPERTIES, PropertyConstantes.FILTRO_OBJETO);
        String filtroFechaInicialLectura = propertiesManager.getProperty(Constantes.FILTER_PROPERTIES, PropertyConstantes.FILTRO_FECHAINICIALLECTURA);
        String filtroFechaFinalLectura = propertiesManager.getProperty(Constantes.FILTER_PROPERTIES, PropertyConstantes.FILTRO_FECHAFINALLECTURA);

        //
        boolean existenFiltros =
                filtroSql.isBlank() ||
                        filtroNuts.isBlank() ||
                        filtroObjeto.isBlank() ||
                        filtroFechaInicialLectura.isBlank() ||
                        filtroFechaFinalLectura.isBlank();

        //
        VariablesGlobales.setExistenFiltros(existenFiltros);

        //
        if (existenFiltros) {

            log.info(Mensajes.FILTROS);
            log.info("{}Filtro SQL: {}", Constantes.TABULADOR_1, filtroSql);
            log.info("{}Filtro Nuts: {}", Constantes.TABULADOR_1, filtroNuts);
            log.info("{}Filtro Objeto del Contrato: {}", Constantes.TABULADOR_1, filtroObjeto);
            log.info("{}Filtro Fecha Inicio Lectura: {}", Constantes.TABULADOR_1, filtroFechaInicialLectura);
            log.info("{}Filtro Fecha Fin Lectura: {}", Constantes.TABULADOR_1, filtroFechaFinalLectura);

        } else {

            log.info(Mensajes.FILTROS_NO);

        }
    }
}
