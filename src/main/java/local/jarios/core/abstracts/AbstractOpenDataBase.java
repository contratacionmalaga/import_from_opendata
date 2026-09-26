package local.jarios.core.abstracts;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import local.jarios.common.util.Mensajes;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.database.MariaDbImportLock;
import local.jarios.database.SessionFactoryRegistry;
import local.jarios.email.ExecutionEmailReportBuilder;
import local.jarios.email.api.EmailSenderImpl;
import local.jarios.email.api.EmailService;
import local.jarios.email.api.EmailServiceImpl;
import local.jarios.email.exception.EmailException;
import local.jarios.email.model.EmailData;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Log;
import local.jarios.enums.TipoConexion;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiServiceException;
import local.jarios.exceptions.MiUnknownHostException;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.LocalDateTimeHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import local.jarios.repositories.EntrySnapshot;
import local.jarios.services.ImportPersistencePlan;
import local.jarios.services.ServicePrincipal;
import local.jarios.services.ServicePrincipalImpl;
import local.jarios.version.api.Version;
import local.jarios.version.api.VersionImpl;
import local.jarios.version.exception.VersionException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public abstract class AbstractOpenDataBase {

  private static final DateTimeFormatter INCIDENT_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

  private final Version versionService;
  private final PropertiesManagerService propertiesManager;
  private final EmailService emailService;

  private ServicePrincipal servicePrincipal;
  private Properties cachedMailProperties;

  private String appName;
  private String appVersion;
  private MariaDbImportLock importLock;

  protected Properties getEmailProperties() throws PropertiesManagerException {
    if (cachedMailProperties != null) {
      return cachedMailProperties;
    }

    Properties props = new Properties();

    props.setProperty(
        PropertiesKeys.MAIL_SMTP_HOST, getRequiredProperty(PropertiesKeys.MAIL_SMTP_HOST));

    props.setProperty(
        PropertiesKeys.MAIL_SMTP_PORT, getRequiredProperty(PropertiesKeys.MAIL_SMTP_PORT));

    props.setProperty(
        PropertiesKeys.MAIL_SMTP_AUTH, getRequiredProperty(PropertiesKeys.MAIL_SMTP_AUTH));

    props.setProperty(
        PropertiesKeys.MAIL_SMTP_STARTTLS, getRequiredProperty(PropertiesKeys.MAIL_SMTP_STARTTLS));

    props.setProperty(
        PropertiesKeys.MAIL_SMTP_USER, getRequiredProperty(PropertiesKeys.MAIL_SMTP_USER));

    props.setProperty(
        PropertiesKeys.MAIL_SMTP_PASSWORD, getRequiredProperty(PropertiesKeys.MAIL_SMTP_PASSWORD));

    // Opcionales
    setIfPresent(props, PropertiesKeys.MAIL_SMTP_SOCKETFACTORY_PORT);
    setIfPresent(props, PropertiesKeys.MAIL_SMTP_SSL_CHECK_SERVER_INTEGRITY);
    setIfPresent(props, PropertiesKeys.MAIL_SMTP_SSL_PROTOCLS);
    setIfPresent(props, PropertiesKeys.MAIL_SMTP_SSL_TRUST);

    cachedMailProperties = props;

    return props;
  }

  private void setIfPresent(Properties props, String key) throws PropertiesManagerException {
    String value = propertiesManager.getProperty(PropertiesFiles.MAIL, key);
    if (value != null && !value.isBlank()) {
      props.setProperty(key, value);
    }
  }

  protected String getEmailFrom() throws PropertiesManagerException {
    return getRequiredProperty(PropertiesKeys.MAIL_FROM);
  }

  protected String getEmailTo() throws PropertiesManagerException {
    return getRequiredProperty(PropertiesKeys.MAIL_TO);
  }

  protected String getEquipo() throws MiUnknownHostException {
    return ComunHelper.getHostName();
  }

  protected String getRequiredProperty(String key) throws PropertiesManagerException {

    String value = propertiesManager.getProperty(PropertiesFiles.MAIL, key);

    if (value == null || value.isBlank()) {
      throw new PropertiesManagerException(
          "Falta la property obligatoria: file=" + PropertiesFiles.MAIL + ", key=" + key);
    }

    return value;
  }

  protected AbstractOpenDataBase() {
    this(
        new VersionImpl(),
        PropertiesManagerServiceImpl.getInstance(),
        null,
        new EmailServiceImpl(new EmailSenderImpl()));
  }

  protected AbstractOpenDataBase(
      Version versionService,
      PropertiesManagerService propertiesManager,
      ServicePrincipal servicePrincipal,
      EmailService emailService) {
    this.versionService = Objects.requireNonNull(versionService, "versionService");
    this.propertiesManager = Objects.requireNonNull(propertiesManager, "propertiesManager");
    this.servicePrincipal = servicePrincipal;
    this.emailService = Objects.requireNonNull(emailService, "emailService");
  }

  public static String[] obtenerStackTraceComoArray(Throwable ex) {
    StackTraceElement[] elementos = ex.getStackTrace();
    String[] resultado = new String[elementos.length];
    for (int i = 0; i < elementos.length; i++) {
      resultado[i] = elementos[i].toString();
    }
    return resultado;
  }

  protected static String[][] toStringMatrix(Estadistica estadistica) {
    List<String[]> datos = new ArrayList<>();
    Field[] fields = Estadistica.class.getDeclaredFields();

    for (Field field : fields) {
      field.setAccessible(true);
      try {
        Object value = field.get(estadistica);
        String nombreCampo = field.getName();

        String valorCampo =
            (value instanceof Log miLog && miLog.getId() != null)
                ? miLog.getId().toString()
                : String.valueOf(value);

        datos.add(new String[] {nombreCampo, valorCampo});
      } catch (IllegalAccessException e) {
        datos.add(new String[] {field.getName(), "Error al acceder"});
      }
    }

    return datos.toArray(new String[0][0]);
  }

  protected int procesar(String configDir) {
    ExitStatus status = runWithPipeline(configDir);
    log.info(status == ExitStatus.OK ? Mensajes.FINAL_CORRECTO : Mensajes.FINAL_ERRONEO);
    return status.code;
  }

  public final void pipelineInitAppVersion() throws VersionException {
    imprimirTitulo("VERSIÓN DE LA APLICACIÓN");
    this.appVersion = versionService.getVersion(getClass());
    log.info("AppVersion: {}.", appVersion);
  }

  public final void pipelineInitProperties(String configDir) throws PropertiesManagerException {
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

    this.appName = Optional.ofNullable(resolveAppName()).orElse(getDefaultAppName());
  }

  public final void pipelineInitVariantContext(OpenDataExecutionContext context)
      throws MiServiceException {
    initVariantContext(context);
    configurePersistirHistoricosRechazados(context);
  }

  protected void adquirirExclusionImportacion(OpenDataExecutionContext context)
      throws MiServiceException {
    if (importLock != null) {
      throw new MiServiceException("La exclusión de importación ya está adquirida");
    }
    String lockName = "opendata:" + context.getTipoSindicacion();
    try {
      importLock =
          MariaDbImportLock.acquire(
              SessionFactoryRegistry.getSessionFactory(TipoConexion.PRINCIPAL), lockName);
      log.info("Exclusión distribuida adquirida: {}", lockName);
    } catch (RuntimeException ex) {
      throw new MiServiceException("No se pudo adquirir la exclusión distribuida: " + lockName, ex);
    }
  }

  private void liberarExclusionImportacion() {
    if (importLock == null) return;
    try {
      importLock.close();
      log.info("Exclusión distribuida liberada.");
    } catch (RuntimeException ex) {
      log.warn("No se pudo liberar la exclusión distribuida: {}", ex.getMessage());
    } finally {
      importLock = null;
    }
  }

  private void configurePersistirHistoricosRechazados(OpenDataExecutionContext context) {
    String value =
        propertiesManager.getProperty(
            PropertiesFiles.APP, PropertiesKeys.APP_PERSISTIR_HISTORICOS_RECHAZADOS);
    boolean persistir = Boolean.parseBoolean(value);
    context.setPersistirHistoricosRechazados(persistir);
    log.info("Persistir históricos RECHAZAR por entrada: {}", persistir);
  }

  public final void pipelineBeforeParse(OpenDataExecutionContext context)
      throws MiServiceException, PropertiesManagerException {
    beforeParse(context);
  }

  public final String pipelineParseFeeds(OpenDataExecutionContext context)
      throws MiParseException, MiServiceException {
    return parsearAtomsFeeds(context);
  }

  public final Map<String, Entry> pipelineResolveEntriesToPersist(
      OpenDataExecutionContext context) {
    return resolveEntriesToPersist(context);
  }

  public final void pipelinePreviewPersistData(OpenDataExecutionContext context)
      throws MiServiceException {
    previewPersistData(context);
  }

  public final Estadistica pipelinePersistAll(OpenDataExecutionContext context)
      throws MiServiceException, MiUnknownHostException {
    return persistAll(context);
  }

  public Map<Feed, List<Entry>> groupEntriesByFeed(Map<String, Entry> mapEntriesToBaseDatos) {
    return mapEntriesToBaseDatos.values().stream().collect(Collectors.groupingBy(Entry::getFeed));
  }

  protected abstract void initVariantContext(OpenDataExecutionContext context)
      throws MiServiceException;

  protected void beforeParse(OpenDataExecutionContext context)
      throws MiServiceException, PropertiesManagerException {
    // No-op por defecto.
  }

  protected abstract String parsearAtomsFeeds(OpenDataExecutionContext context)
      throws MiParseException, MiServiceException;

  /** Persiste la importación y registra su duración una vez confirmado el commit principal. */
  protected String persistirImportacionYRegistrarDuracion(
      ImportPersistencePlan plan, Estadistica estadistica) throws MiServiceException {
    long inicioPersistencia = System.nanoTime();
    getServicePrincipal().persistirImportacion(plan);

    String duracion = LocalDateTimeHelper.getDiferenciaNanos(inicioPersistencia, System.nanoTime());
    estadistica.setDuracionPersistencia(duracion);

    try {
      getServicePrincipal().persistirEstadistica(estadistica);
    } catch (MiServiceException ex) {
      log.warn(
          "La importacion ya fue confirmada, pero no se pudo actualizar su duracion en Estadistica: {}",
          ex.getMessage());
    }
    return duracion;
  }

  public abstract Map<String, Entry> resolveEntriesToPersist(OpenDataExecutionContext context);

  public abstract void previewPersistData(OpenDataExecutionContext context)
      throws MiServiceException;

  public abstract Estadistica persistAll(OpenDataExecutionContext context)
      throws MiServiceException, MiUnknownHostException;

  protected String resolveAppName() throws PropertiesManagerException {
    return null;
  }

  protected abstract String getDefaultAppName();

  protected Entry obtenerUltimaEntrada(Map<String, Entry> mapEntries) {
    return mapEntries == null
        ? null
        : mapEntries.values().stream()
            .filter(Objects::nonNull)
            .max(Comparator.comparing(Entry::getUpdated))
            .orElse(null);
  }

  protected Entry obtenerUltimaEntradaSnapshot(Map<String, EntrySnapshot> snapshots) {
    return snapshots == null
        ? null
        : snapshots.values().stream()
            .filter(Objects::nonNull)
            .max(Comparator.comparing(EntrySnapshot::updated))
            .map(AbstractOpenDataBase::toEntryReference)
            .orElse(null);
  }

  private static Entry toEntryReference(EntrySnapshot snapshot) {
    Entry entry = new Entry();
    entry.setEntryId(snapshot.entryId());
    entry.setUpdated(snapshot.updated());
    return entry;
  }

  public void sendSuccessEmail(Estadistica estadistica)
      throws EmailException, MiUnknownHostException, PropertiesManagerException {
    if (!isEmailNotificationEnabled()) {
      log.info("Envío de email desactivado por configuración.");
      return;
    }
    sendSuccessEmail(null, estadistica);
  }

  public void sendSuccessEmail(OpenDataExecutionContext context)
      throws EmailException, MiUnknownHostException, PropertiesManagerException {
    if (!isEmailNotificationEnabled()) {
      log.info("Envío de email desactivado por configuración.");
      return;
    }
    sendSuccessEmail(context, context == null ? null : context.getEstadistica());
  }

  private void sendSuccessEmail(OpenDataExecutionContext context, Estadistica estadistica)
      throws EmailException, MiUnknownHostException, PropertiesManagerException {

    imprimirTitulo("ENVÍO DE EMAIL DE CONFIRMACIÓN");

    Properties props = getEmailProperties();
    EmailData emailData = buildSuccessEmailData(context, estadistica);

    emailService.sendEmail(props, emailData);

    log.info("Email de confirmación enviado correctamente.");
  }

  protected EmailData buildSuccessEmailData(Estadistica estadistica)
      throws PropertiesManagerException, MiUnknownHostException {
    return buildSuccessEmailData(null, estadistica);
  }

  protected EmailData buildSuccessEmailData(
      OpenDataExecutionContext context, Estadistica estadistica)
      throws PropertiesManagerException, MiUnknownHostException {

    String subject =
        ExecutionEmailReportBuilder.buildSuccessSubject(
            getReportProcessType(),
            estadistica != null && estadistica.getMiLog() != null
                ? estadistica.getMiLog().getLugarImportacion()
                : context == null ? null : context.getLugarImportacion(),
            estadistica);

    String body =
        ExecutionEmailReportBuilder.buildSuccessBody(
            appName, appVersion, getReportProcessType(), context, estadistica);

    return new EmailData(getEmailFrom(), getEmailTo(), subject, body);
  }

  protected EmailData buildErrorEmailData(
      OpenDataExecutionContext context, Throwable ex, String tipoError)
      throws PropertiesManagerException, MiUnknownHostException {

    String subject =
        ExecutionEmailReportBuilder.buildErrorSubject(
            getReportProcessType(), getReportImportOrigin(), ex, tipoError);

    String body =
        ExecutionEmailReportBuilder.buildErrorBody(
            appName,
            appVersion,
            getReportProcessType(),
            getReportImportOrigin(),
            context,
            ex,
            tipoError,
            getIncidentId(context),
            isErrorStackTraceIncluded(),
            getErrorStackTraceMaxChars());

    return new EmailData(getEmailFrom(), getEmailTo(), subject, body);
  }

  private void handleFailure(OpenDataExecutionContext context, Throwable ex, String tipoError) {
    String incidentId = getIncidentId(context);
    log.error(
        "ERROR [{}] {}. Consulte el log técnico asociado a esta ejecución.",
        incidentId,
        getOperatorMessage(ex, tipoError),
        ex);

    try {
      if (!isEmailNotificationEnabled()) {
        log.info("Envío de email de error desactivado por configuración.");
        return;
      }
    } catch (PropertiesManagerException propertiesEx) {
      log.error(
          "ERROR [{}] No se pudo consultar la configuración de email de soporte. Consulte el log técnico asociado a esta ejecución.",
          incidentId,
          propertiesEx);
      return;
    }

    try {
      Properties props = getEmailProperties();
      EmailData emailData = buildErrorEmailData(context, ex, tipoError);

      emailService.sendEmail(props, emailData);

    } catch (Exception emailEx) {
      log.error(
          "ERROR [{}] No se pudo enviar el correo de soporte. Consulte el log técnico asociado a esta ejecución.",
          incidentId,
          emailEx);
    }
  }

  /** Registra un fallo de notificación sin cambiar una importación ya confirmada. */
  public void handleSuccessNotificationFailure(OpenDataExecutionContext context, Throwable ex) {
    String incidentId = getIncidentId(context);
    log.error(
        "ERROR [{}] La importación ya fue confirmada, pero no se pudo enviar el correo de confirmación. Consulte el log técnico asociado a esta ejecución.",
        incidentId,
        ex);
  }

  private boolean isErrorStackTraceIncluded() throws PropertiesManagerException {
    String value =
        propertiesManager.getProperty(
            PropertiesFiles.APP, PropertiesKeys.APP_EMAIL_ERROR_INCLUDE_STACKTRACE);
    return value == null || value.isBlank() || !"false".equalsIgnoreCase(value.trim());
  }

  private int getErrorStackTraceMaxChars() throws PropertiesManagerException {
    String value =
        propertiesManager.getProperty(
            PropertiesFiles.APP, PropertiesKeys.APP_EMAIL_ERROR_MAX_STACKTRACE_CHARS);
    if (value == null || value.isBlank()) {
      return 50_000;
    }
    try {
      int parsed = Integer.parseInt(value.trim());
      return parsed > 0 ? parsed : 50_000;
    } catch (NumberFormatException ex) {
      return 50_000;
    }
  }

  private static String getOperatorMessage(Throwable ex, String tipoError) {
    String type = tipoError == null || tipoError.isBlank() ? "Error de importación" : tipoError;
    String message =
        ex == null || ex.getMessage() == null || ex.getMessage().isBlank()
            ? "sin detalle"
            : ex.getMessage();
    return type + ": " + message;
  }

  private static String getIncidentId(OpenDataExecutionContext context) {
    if (context != null && context.getIncidentId() != null && !context.getIncidentId().isBlank()) {
      return context.getIncidentId();
    }
    String incidentId =
        "IMP-"
            + INCIDENT_TIME_FORMATTER.format(LocalDateTime.now())
            + "-"
            + UUID.randomUUID().toString().substring(0, 8);
    if (context != null) {
      context.setIncidentId(incidentId);
    }
    return incidentId;
  }

  static boolean isEmailNotificationEnabled(String value) {
    return value == null || value.isBlank() || !"false".equalsIgnoreCase(value.trim());
  }

  private boolean isEmailNotificationEnabled() throws PropertiesManagerException {
    return isEmailNotificationEnabled(
        propertiesManager.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_EMAIL_ENABLED));
  }

  protected void imprimirTitulo(String titulo) {
    StringHelper.generarTitulo(log, titulo);
  }

  protected ServicePrincipal getServicePrincipal() {
    return servicePrincipal;
  }

  protected PropertiesManagerService getPropertiesManager() {
    return propertiesManager;
  }

  protected Version getVersionService() {
    return versionService;
  }

  protected EmailService getEmailService() {
    return emailService;
  }

  protected String getAppName() {
    return appName;
  }

  protected String getAppVersion() {
    return appVersion;
  }

  protected String getReportProcessType() {
    return "opendata";
  }

  protected String getReportImportOrigin() {
    String className = getClass().getSimpleName().toLowerCase();
    if (className.contains("internet")) {
      return "INTERNET";
    }
    if (className.contains("local")) {
      return "LOCAL";
    }
    return "desconocido";
  }

  protected ExitStatus runWithPipeline(String configDir) {
    local.jarios.core.pipeline.context.OpenDataExecutionContext context =
        new local.jarios.core.pipeline.context.OpenDataExecutionContext(configDir);
    String incidentId = getIncidentId(context);
    MDC.put("incidentId", incidentId);
    try {
      local.jarios.core.pipeline.OpenDataPipeline<
              local.jarios.core.pipeline.context.OpenDataExecutionContext>
          pipeline = new local.jarios.core.pipeline.OpenDataPipeline<>();

      pipeline
          .addStep(new local.jarios.core.pipeline.steps.InitOpenDataStep(this))
          .addStep(new local.jarios.core.pipeline.steps.PreParseOpenDataStep(this))
          .addStep(new local.jarios.core.pipeline.steps.ParseOpenDataStep(this))
          .addStep(new local.jarios.core.pipeline.steps.ResolveEntriesOpenDataStep(this))
          .addStep(new local.jarios.core.pipeline.steps.ResolveDeletedEntriesOpenDataStep(this))
          .addStep(new local.jarios.core.pipeline.steps.PreviewOpenDataStep(this))
          .addStep(new local.jarios.core.pipeline.steps.PersistOpenDataStep(this))
          .addStep(new local.jarios.core.pipeline.steps.NotifySuccessOpenDataStep(this));

      pipeline.execute(context);

      return ExitStatus.OK;

    } catch (MiUnknownHostException ex) {
      handleFailure(context, ex, "[MiUnknownHostException]");
    } catch (MiServiceException ex) {
      handleFailure(context, ex, "[MiServiceException]");
    } catch (EmailException ex) {
      handleFailure(context, ex, "[EmailException]");
    } catch (PropertiesManagerException ex) {
      handleFailure(context, ex, "[PropertiesManagerException]");
    } catch (VersionException ex) {
      handleFailure(context, ex, "[VersionException]");
    } catch (RuntimeException ex) {
      handleFailure(context, ex, "[RuntimeException]");
    } catch (Exception ex) {
      handleFailure(context, ex, "[Exception]");
    } finally {
      liberarExclusionImportacion();
      SessionFactoryRegistry.closeAll();
      MDC.remove("incidentId");
    }

    return ExitStatus.ERROR;
  }

  @Getter
  protected enum ExitStatus {
    OK(0),
    ERROR(1);

    private final int code;

    ExitStatus(int code) {
      this.code = code;
    }
  }
}
