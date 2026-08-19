package local.jarios.core.abstracts;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import local.jarios.common.util.Mensajes;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.database.SessionFactoryRegistry;
import local.jarios.email.api.EmailSenderImpl;
import local.jarios.email.api.EmailService;
import local.jarios.email.api.EmailServiceImpl;
import local.jarios.email.exception.EmailException;
import local.jarios.email.helper.EmailHelper;
import local.jarios.email.model.EmailData;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Log;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiServiceException;
import local.jarios.exceptions.MiUnknownHostException;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import local.jarios.repositories.EntrySnapshot;
import local.jarios.services.ServicePrincipal;
import local.jarios.services.ServicePrincipalImpl;
import local.jarios.version.api.Version;
import local.jarios.version.api.VersionImpl;
import local.jarios.version.exception.VersionException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractOpenDataBase {

  private final Version versionService;
  private final PropertiesManagerService propertiesManager;
  private final EmailService emailService;

  private ServicePrincipal servicePrincipal;
  private Properties cachedMailProperties;

  private String appName;
  private String appVersion;

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

    imprimirTitulo("ENVÍO DE EMAIL DE CONFIRMACIÓN");

    Properties props = getEmailProperties();
    EmailData emailData = buildSuccessEmailData(estadistica);

    emailService.sendEmail(props, emailData);

    log.info("Email de confirmación enviado correctamente.");
  }

  protected EmailData buildSuccessEmailData(Estadistica estadistica)
      throws PropertiesManagerException, MiUnknownHostException {

    String[][] datos = toStringMatrix(estadistica);

    String subject = EmailHelper.getAsunto(appName, appVersion, getEquipo(), true);

    String body = EmailHelper.getCuerpoEstadistica(datos);

    return new EmailData(getEmailFrom(), getEmailTo(), subject, body);
  }

  protected EmailData buildErrorEmailData(Throwable ex, String tipoError)
      throws PropertiesManagerException, MiUnknownHostException {

    String subject = "❌ ERROR " + EmailHelper.getAsunto(appName, appVersion, getEquipo(), false);

    String[] stack = obtenerStackTraceComoArray(ex);
    String body = EmailHelper.getCuerpoExcepcion(stack);

    return new EmailData(getEmailFrom(), getEmailTo(), subject, body);
  }

  protected void handleFailure(Throwable ex, String tipoError) {
    log.error("{} - {}", tipoError, ex.getMessage(), ex);

    try {
      Properties props = getEmailProperties();
      EmailData emailData = buildErrorEmailData(ex, tipoError);

      emailService.sendEmail(props, emailData);

    } catch (Exception emailEx) {
      log.error("No se pudo enviar el email de error: {}", emailEx.getMessage(), emailEx);
    }
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

  protected ExitStatus runWithPipeline(String configDir) {
    try {
      local.jarios.core.pipeline.OpenDataPipeline<
              local.jarios.core.pipeline.context.OpenDataExecutionContext>
          pipeline = new local.jarios.core.pipeline.OpenDataPipeline<>();

      local.jarios.core.pipeline.context.OpenDataExecutionContext context =
          new local.jarios.core.pipeline.context.OpenDataExecutionContext(configDir);

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
    } catch (Exception ex) {
      handleFailure(ex, "[Exception]");
    } finally {
      SessionFactoryRegistry.closeAll();
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
