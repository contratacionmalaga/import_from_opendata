package local.jarios;

import local.jarios.common.util.ConstantesExcel;
import local.jarios.common.util.Mensajes;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.database.SessionFactoryRegistry;
import local.jarios.email.api.EmailSenderImpl;
import local.jarios.email.api.EmailService;
import local.jarios.email.api.EmailServiceImpl;
import local.jarios.email.exception.EmailException;
import local.jarios.email.helper.EmailHelper;
import local.jarios.email.model.EmailData;
import local.jarios.email.validator.EmailRequestValidator;
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.enums.EntryOpcion;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiServiceException;
import local.jarios.exceptions.MiUnknownHostException;
import local.jarios.filtro.FiltroManager;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.HistoricoTotales;
import local.jarios.helpers.LocalDateTimeHelper;
import local.jarios.helpers.OcHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.imports.ImportResult;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Clase abstracta que gestiona el procesamiento general para la importación y análisis de datos
 * OpenData basados en feeds ATOM, incluyendo carga de configuración, parseo, persistencia y
 * notificaciones.
 *
 * <p>Las subclases deben implementar métodos específicos para definir el tipo de sindicación,
 * lugar de importación y la lógica de parseo de los feeds.</p>
 */
@Slf4j
public abstract class AbstractOpenData {

  // =========
  // Dependencias (DI simple)
  // =========
  private final Version versionService;
  private final PropertiesManagerService propertiesManager;
  private final EmailService emailService;
  private ServicePrincipal servicePrincipal;
  // =========
  // Estado de ejecución (por instancia, no estático)
  // =========
  private String appName;
  private String appVersion;
  /**
   * Constructor por defecto (crea dependencias reales). Si algún día quieres testear, añade otro
   * constructor inyectando mocks.
   */
  protected AbstractOpenData() {
    this(new VersionImpl(),
         PropertiesManagerServiceImpl.getInstance(),
         null,
         new EmailServiceImpl(new EmailSenderImpl()));
  }

  /**
   * Constructor inyectable para futuras versiones.
   *
   * @param versionService    Version
   * @param propertiesManager Propidedad
   * @param servicePrincipal  Servicio
   * @param emailService      Servicio de email
   */
  protected AbstractOpenData(
      Version versionService,
      PropertiesManagerService propertiesManager,
      ServicePrincipal servicePrincipal,
      EmailService emailService
  ) {
    this.versionService = Objects.requireNonNull(versionService, "versionService");
    this.propertiesManager = Objects.requireNonNull(propertiesManager, "propertiesManager");
    this.servicePrincipal = servicePrincipal;
    this.emailService = Objects.requireNonNull(emailService, "emailService");
  }

  private static String[][] toStringMatrix(Estadistica estadistica) {
    List<String[]> datos = new ArrayList<>();
    Field[] fields = Estadistica.class.getDeclaredFields();

    for (Field field : fields) {
      field.setAccessible(true);
      try {
        Object value = field.get(estadistica);
        String nombreCampo = field.getName();

        String valorCampo = (value instanceof Log miLog && miLog.getId() != null)
            ? miLog.getId().toString()
            : String.valueOf(value);

        datos.add(new String[]{nombreCampo, valorCampo});
      } catch (IllegalAccessException e) {
        datos.add(new String[]{field.getName(), "Error al acceder"});
      }
    }
    return datos.toArray(new String[0][0]);
  }

  // ==========================
  // Hooks (subclases)
  // ==========================

  public static String[] obtenerStackTraceComoArray(Throwable ex) {
    StackTraceElement[] elementos = ex.getStackTrace();
    String[] resultado = new String[elementos.length];
    for (int i = 0; i < elementos.length; i++) {
      resultado[i] = elementos[i].toString();
    }
    return resultado;
  }

  protected abstract TipoSindicacion getTipoSindicacion();

  protected abstract LugarImportacion getLugarImportacion();

  // ==========================
  // API principal
  // ==========================

  /**
   *
   * @throws MiParseException Excepción asociada al parseo
   */
  protected abstract void parsearAtomsFeeds() throws MiParseException;

  /**
   * Ejecuta el flujo completo y devuelve el estado de salida.
   *
   * @param configDir directorio donde se encuentran los ficheros properties
   * @return estado final de la ejecución
   */
  protected ExitStatus run(String configDir) {
    try {
      initAppVersion();
      initProperties(configDir);
      initContext();

      ImportResult<OrganoContratacion> excelResult = loadOrganosContratacion();
      loadFilters();

      String duracionParseo = parseFeeds();

      Map<String, Entry> mapEntriesToBaseDatos = resolveEntriesToPersist();
      Map<Feed, List<Entry>> mapFeedsToBaseDatos = groupEntriesByFeed(mapEntriesToBaseDatos);

      HistoricoTotales totales = logToPersistPreview(mapFeedsToBaseDatos);

      Estadistica estadistica = persistAll(excelResult, totales, duracionParseo);

      sendSuccessEmail(estadistica);

      return ExitStatus.OK;

    } catch (MiUnknownHostException ex) {
      handleFailure(ex, "[MiUnknownHostException]");
    } catch (MiServiceException ex) {
      handleFailure(ex, "[MiServiceException]");
    } catch (EmailException ex) {
      handleFailure(ex, "[EmailException]");
    } catch (PropertiesManagerException ex) {
      handleFailure(ex, "[PropertiesManagerException]");
    } catch (VersionException ex) {
      handleFailure(ex, "[VersionException]");
    } catch (RuntimeException ex) {
      handleFailure(ex, "[RuntimeException]");
    } finally {
      SessionFactoryRegistry.closeAll();
    }

    return ExitStatus.ERROR;
  }

  // ==========================
  // Inicialización
  // ==========================

  /**
   * Mantengo tu método original (por compatibilidad), pero delega a run(). Si quieres, aquí sí
   * puedes hacer System.exit(run(...).code) desde el main, no desde aquí.
   *
   * @param configDir Directorio de properties
   */
  protected void procesar(String configDir) {
    ExitStatus status = run(configDir);
    finalizar(status == ExitStatus.OK
                  ? Mensajes.FINAL_CORRECTO
                  : Mensajes.FINAL_ERRONEO, status.code);
  }

  private void initAppVersion() throws VersionException {
    imprimirTitulo("VERSIÓN DE LA APLICACIÓN");
    this.appVersion = versionService.getVersion(AbstractOpenData.class);
    log.info("AppVersion: {}.", appVersion);
  }

  /**
   * Inicializador de las properties
   *
   * @param configDir Directorio de properties
   * @throws PropertiesManagerException Excepción de properties
   */
  private void initProperties(String configDir) throws PropertiesManagerException {
    imprimirTitulo("ACCESO A LOS FICHEROS properties");
    propertiesManager.setConfigDir(configDir);
    log.info("configDir (param) = {}", configDir);
    log.info("configDir (resolved) = {}", propertiesManager.getConfigDir());
    log.info("java user.dir = {}", System.getProperty("user.dir"));
    Set<String> clavesSensibles = Set.of("password");
    propertiesManager.setSensitiveKeys(clavesSensibles);
    log.info("Establecidas claves sensibles: {}", clavesSensibles);
    propertiesManager.loadAllProperties();
    log.info("Cargadas todas las propiedades correctamente.");
    log.info("Ficheros leídos: {}", propertiesManager.getListFiles());

    if (this.servicePrincipal == null) {
      this.servicePrincipal = new ServicePrincipalImpl();
      log.info("ServicePrincipalImpl creado correctamente tras cargar properties.");
    }
    this.appName = Optional
        .ofNullable(propertiesManager.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_NAME))
        .orElse("import-from-opendata");
  }

  // ==========================
  // Excel + filtros
  // ==========================

  /**
   * Inicio del contexto de la ejecución
   *
   * @throws MiServiceException Excepción de ejecución
   */
  private void initContext() throws MiServiceException {
    imprimirTitulo("LUGAR DE IMPORTACIÓN Y TIPO DE SINDICACIÓN");
    LugarImportacion lugar = getLugarImportacion();
    TipoSindicacion tipo = getTipoSindicacion();

    VariablesGlobales.setLugarImportacion(lugar);
    VariablesGlobales.setTipoSindicacion(tipo);

    log.info("Lugar de Importación: {}.", lugar);
    log.info("Tipo de Sindicación: {}.", tipo);

    if (lugar == LugarImportacion.INTERNET) {
      VariablesGlobales.setMapEntriesFromBaseDatos(servicePrincipal.getMapEntries());
      VariablesGlobales.setNewestEntry(
          obtenerUltimaEntrada(VariablesGlobales.getMapEntriesFromBaseDatos())
      );
    }
  }

  private ImportResult<OrganoContratacion> loadOrganosContratacion() {
    imprimirTitulo("CARGA DE EXCEL DESDE INTERNET");
    log.info(ConstantesExcel.EXCEL_OC_URL);

    ImportResult<OrganoContratacion> result = OcHelper.getListaOcFromExcelInternet();
    VariablesGlobales.setListOrganoContratacionEnExcel(result.lista());
    VariablesGlobales.setFechaGeneracionExcel(result.fechaGeneracion());

    log.info("Importada lista OC desde internet.");
    log.info(
        "  Registros leídos: {}",
        StringHelper.getNumeroConFormato(
            VariablesGlobales.getListOrganoContratacionEnExcel().size()
        )
    );
    log.info("  Fecha de generación del fichero: {}", VariablesGlobales.getFechaGeneracionExcel());

    return result;
  }

  // ==========================
  // Parseo
  // ==========================

  /**
   * Carga de los filtros
   *
   * @throws PropertiesManagerException Excepción al leer el fichero de filtros
   */
  private void loadFilters() throws PropertiesManagerException {
    StringHelper.generarTitulo(log, "CARGA DE FILTROS");

    FiltroManager filtroManager = new FiltroManager();
    filtroManager.cargarFiltros();

    log.info("Valores de filtros desde properties:");
    log.info(
        "  Filtro fecha inicial: {}",
        propertiesManager.getProperty(PropertiesFiles.FILTER,
                                      PropertiesKeys.FILTER_FECHAFINALLECTURA)
    );
    log.info(
        "  Filtro fecha final: {}",
        propertiesManager.getProperty(PropertiesFiles.FILTER,
                                      PropertiesKeys.FILTER_FECHAFINALLECTURA)
    );
    log.info(
        "  Filtro códigos postales: {}",
        propertiesManager.getProperty(PropertiesFiles.FILTER,
                                      PropertiesKeys.FILTER_CODIGOS_POSTALES)
    );
    log.info(
        "  Filtro nifs: {}",
        propertiesManager.getProperty(PropertiesFiles.FILTER, PropertiesKeys.FILTER_NIFS)
    );

    StringHelper.generarTitulo(log, "FILTROS APLICADOS");
    FiltroManager.imprimirFiltros();
  }

  private String parseFeeds() throws MiParseException {
    imprimirTitulo("INICIO DEL PARSEO DE LOS FICHEROS ATOMS");

    LocalDateTime inicio = LocalDateTimeHelper.getLocalDateTimeNow();
    parsearAtomsFeeds(); // rellena VariablesGlobales
    LocalDateTime fin = LocalDateTimeHelper.getLocalDateTimeNow();

    String duracion = LocalDateTimeHelper.getDiferenciaLocalDateTime(inicio, fin);
    log.info("Final del parseo. Duración: {}", duracion);

    return duracion;
  }

  private Map<String, Entry> resolveEntriesToPersist() {
    return VariablesGlobales.getLugarImportacion() == LugarImportacion.INTERNET
        ? ((OpenDataInternet) this).getMapEntriesToBaseDatos()
        : VariablesGlobales.getMapEntriesFromAtoms();
  }

  private Map<Feed, List<Entry>> groupEntriesByFeed(Map<String, Entry> mapEntriesToBaseDatos) {
    return mapEntriesToBaseDatos.values().stream()
        .collect(Collectors.groupingBy(Entry::getFeed));
  }

  // ==========================
  // Persistencia + estadística
  // ==========================

  private HistoricoTotales logToPersistPreview(Map<Feed, List<Entry>> mapFeedsToBaseDatos) {
    imprimirTitulo("LISTADO DE LOS OBJETOS OBTENIDOS EN EL PARSEO PARA SU PERSISTENCIA");

    int nEntry = 0;
    int nDeletedEntry = 0;

    for (Feed feed : VariablesGlobales.getSetFeedsFromAtoms()) {
      log.info("{}", feed);

      // ENTRADAS ACTIVAS
      List<Entry> entryList = mapFeedsToBaseDatos.get(feed);
      if (entryList != null && !entryList.isEmpty()) {
        feed.setEntryList(entryList);
        for (Entry entry : entryList) {
          nEntry++;
          log.info("  {}", entry);
        }
      } else {
        log.info("  (Sin entries activas)");
      }

      // ENTRADAS ELIMINADAS
      List<DeletedEntry> deleted = feed.getDeletedEntryList();
      if (deleted != null && !deleted.isEmpty()) {
        for (DeletedEntry de : deleted) {
          nDeletedEntry++;
          log.info("  {}", de);
        }
      } else {
        log.info("  (Sin entries eliminadas)");
      }
    }

    imprimirTitulo("RESUMEN DEL PARSEO");
    log.info("Feeds obtenidos del parseo: {}",
             StringHelper.getNumeroConFormato(mapFeedsToBaseDatos.size()));
    log.info("Entry obtenidos del parseo: {}",
             StringHelper.getNumeroConFormato(nEntry));
    log.info("DeletedEntry obtenidos del parseo: {}",
             StringHelper.getNumeroConFormato(nDeletedEntry));
    log.info("Históricos parseados: {}",
             StringHelper.getNumeroConFormato(VariablesGlobales.getListHistoricos().size()));

    Map<EntryOpcion, Long> historicoCount = VariablesGlobales.getListHistoricos().stream()
        .collect(Collectors.groupingBy(
            Historico::getEntryOpcion,
            () -> new EnumMap<>(EntryOpcion.class),
            Collectors.counting()
        ));

    long insertar = historicoCount.getOrDefault(EntryOpcion.INSERTAR, 0L);
    long eliminar = historicoCount.getOrDefault(EntryOpcion.ELIMINAR, 0L);
    long actualizar = historicoCount.getOrDefault(EntryOpcion.ACTUALIZAR, 0L);
    long rechazar = historicoCount.getOrDefault(EntryOpcion.RECHAZAR, 0L);

    log.info(
        "   Entry INSERTADOS: {}",
        StringHelper.getNumeroConFormato(Math.toIntExact(insertar))
    );
    log.info(
        "   Entry ELIMINADOS: {}",
        StringHelper.getNumeroConFormato(Math.toIntExact(eliminar))
    );
    log.info(
        "   Entry ACTUALIZADOS: {}",
        StringHelper.getNumeroConFormato(Math.toIntExact(actualizar))
    );
    log.info(
        "   Entry RECHAZADOS: {}",
        StringHelper.getNumeroConFormato(Math.toIntExact(rechazar))
    );

    return new HistoricoTotales(
        historicoCount.getOrDefault(EntryOpcion.INSERTAR, 0L),
        historicoCount.getOrDefault(EntryOpcion.ELIMINAR, 0L),
        historicoCount.getOrDefault(EntryOpcion.ACTUALIZAR, 0L),
        historicoCount.getOrDefault(EntryOpcion.RECHAZAR, 0L),
        nDeletedEntry
    );
  }

  private Estadistica persistAll(
      ImportResult<OrganoContratacion> excelResult,
      HistoricoTotales totales,
      String duracionParseo
  ) throws MiServiceException, MiUnknownHostException {

    imprimirTitulo("PERSISTENCIA EN LA BASE DE DATOS");
    LocalDateTime inicio = LocalDateTimeHelper.getLocalDateTimeNow();

    Log miLog = new Log(
        VariablesGlobales.getLugarImportacion(),
        VariablesGlobales.getTipoSindicacion(),
        excelResult.lista().size(),
        excelResult.fechaGeneracion()
    );
    servicePrincipal.persistirLog(miLog);

    Configuracion configuracion = new Configuracion(miLog);
    servicePrincipal.persistirConfiguracion(configuracion);

    servicePrincipal.persistirListaNifFiltro(miLog, VariablesGlobales.getListNifs());
    servicePrincipal.persistirListaOcFiltro(miLog,
                                            VariablesGlobales.getListOrganoContratacionFiltro());
    servicePrincipal.persistirListaHistoricos(miLog, VariablesGlobales.getListHistoricos());

    Estadistica estadistica = buildEstadistica(miLog, totales, duracionParseo, inicio);

    servicePrincipal.persistirSetFeeds(miLog, VariablesGlobales.getSetFeedsFromAtoms());
    servicePrincipal.persistirEstadistica(estadistica);

    log.info("Persistencia completada.");
    return estadistica;
  }

  // ==========================
  // Email + errores
  // ==========================

  private Estadistica buildEstadistica(
      Log miLog,
      HistoricoTotales totales,
      String duracionParseo,
      LocalDateTime inicioPersistencia
  ) throws MiUnknownHostException {

    Estadistica estadistica = new Estadistica(miLog);

    estadistica.setNumRegistrosHistoricosInsertar(totales.insertar());
    estadistica.setNumRegistrosHistoricosEliminar(totales.eliminar());
    estadistica.setNumRegistrosHistoricosActualizar(totales.actualizar());
    estadistica.setNumRegistrosHistoricosRechazar(totales.rechazar());

    LocalDateTime fin = LocalDateTimeHelper.getLocalDateTimeNow();
    String duracionPersistencia = LocalDateTimeHelper
        .getDiferenciaLocalDateTime(
            inicioPersistencia,
            fin
        );

    estadistica.setDuracionParseo(duracionParseo);
    estadistica.setDuracionPersistencia(duracionPersistencia);
    estadistica.setNumFicherosAtoms((long) VariablesGlobales.getSetFeedsFromAtoms().size());
    estadistica.setNumOrganosContratacionFiltro(
        (long) VariablesGlobales.getListOrganoContratacionFiltro().size()
    );
    estadistica.setNumNifsFiltro((long) VariablesGlobales.getListNifs().size());
    estadistica.setNumDeletedEntries(totales.deletedEntrys());
    estadistica.setTotalHistoricos(totales.total());

    return estadistica;
  }

  private void sendSuccessEmail(Estadistica estadistica) {
    imprimirTitulo("ENVÍO EMAIL CON LAS ESTADÍSTICAS");
    sendEmail(estadistica, null, true);
    log.info("Email enviado correctamente.");
  }

  private void handleFailure(Exception ex, String tag) {
    log.error("{} - {}", tag, ex.getMessage(), ex);

    try {
      sendEmail(null, ex, false);
      log.info("Correo de error enviado correctamente.");
    } catch (EmailException mailEx) {
      log.error("Error al enviar email de fallo: {}", mailEx.getMessage(), mailEx);
    }

    log.error("{}", Mensajes.FINAL_ERRONEO);
  }

  private void sendEmail(Estadistica estadistica, Exception ex, boolean success) {
    Properties emailProps = propertiesManager.getProperties(PropertiesFiles.MAIL);
    EmailData emailData = buildEmailData(estadistica, ex, success);

    EmailRequestValidator.validarEmailRequest(emailProps, emailData);
    emailService.sendEmail(emailProps, emailData);
  }

  // ==========================
  // Utilidades (mantengo tu lógica)
  // ==========================

  private EmailData buildEmailData(Estadistica estadistica, Exception ex, boolean success) {
    try {
      String equipo = ComunHelper.getHostName();
      String from = propertiesManager.getProperty(PropertiesFiles.MAIL, PropertiesKeys.MAIL_FROM);
      String to = propertiesManager.getProperty(PropertiesFiles.MAIL, PropertiesKeys.MAIL_TO);

      String asunto = EmailHelper.getAsunto(appName, appVersion, equipo, success);

      String cuerpo = success
          ? EmailHelper.getCuerpoEstadistica(toStringMatrix(estadistica))
          : EmailHelper.getCuerpoExcepcion(obtenerStackTraceComoArray(ex));

      return new EmailData(from, to, asunto, cuerpo);

    } catch (MiUnknownHostException e) {
      throw new EmailException("Error al obtener el nombre del host.", e);
    } catch (PropertiesManagerException e) {
      throw new EmailException("Error al leer las propiedades.", e);
    }
  }

  public void finalizar(String mensaje, int exitCode) {
    if (exitCode == 0) {
      imprimirTitulo(mensaje);
    } else {
      imprimirTitulo(mensaje + ". Código de salida: " + exitCode);
    }
    System.exit(exitCode);
  }

  public Entry obtenerUltimaEntrada(Map<String, Entry> entradas) {
    return entradas.values().stream()
        .max(Comparator.comparing(Entry::getUpdated))
        .orElse(null);
  }

  private void imprimirTitulo(String titulo) {
    if (titulo == null || titulo.isBlank()) {
      log.warn("Título nulo o vacío en imprimirTitulo()");
      return;
    }
    String sep = "=".repeat(titulo.length());
    log.info(sep);
    log.info(titulo);
    log.info(sep);
  }

  /**
   * Estado de salida del proceso. Evita System.exit() dentro de la lógica de negocio.
   */
  public enum ExitStatus {
    OK(0), ERROR(1);
    public final int code;

    ExitStatus(int code) {
      this.code = code;
    }
  }
}
