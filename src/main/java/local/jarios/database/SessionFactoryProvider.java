package local.jarios.database;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.enums.TipoConexion;
import local.jarios.exceptions.MiSessionFactoryProvider;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.JdbcSettings;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * Proveedor de SessionFactory para distintos tipos de conexión. Principios: - SRP: orquesta la
 * construcción; las estrategias definen "qué" cargar. - Strategy: una estrategia por TipoConexion.
 * - DI: dependencias por constructor (testable). - Fail-fast: valida claves requeridas antes de
 * construir Hibernate. - Logging seguro: no expone credenciales.
 */
@Slf4j
public final class SessionFactoryProvider {

  private static final String CONFIG_PACKAGE_NAME = "local.jarios.entity";

  // Claves mínimas necesarias para conectar
  private static final Set<String> REQUIRED_JAKARTA_KEYS = Set.of(
      PropertiesKeys.JAKARTA_PERSISTENCE_JDBC_URL,
      PropertiesKeys.JAKARTA_PERSISTENCE_JDBC_DRIVER,
      PropertiesKeys.JAKARTA_PERSISTENCE_JDBC_USER,
      PropertiesKeys.JAKARTA_PERSISTENCE_JDBC_PASSWORD
  );

  private final PropertiesManagerService propertiesManager;
  private final HibernateConfigurer hibernateConfigurer;
  private final EntityScanner entityScanner;
  private final Map<TipoConexion, ConnectionStrategy> strategies;

  /**
   * Constructor recomendado (inyectable).
   */
  public SessionFactoryProvider(
      PropertiesManagerService propertiesManager,
      HibernateConfigurer hibernateConfigurer,
      EntityScanner entityScanner
  ) {
    this.propertiesManager = Objects.requireNonNull(propertiesManager, "propertiesManager");
    this.hibernateConfigurer = Objects.requireNonNull(hibernateConfigurer, "hibernateConfigurer");
    this.entityScanner = Objects.requireNonNull(entityScanner, "entityScanner");

    var map = new EnumMap<TipoConexion, ConnectionStrategy>(TipoConexion.class);
    map.put(TipoConexion.PRINCIPAL, new PrincipalStrategy());
    map.put(TipoConexion.FILTRO_SQL, new FiltroSqlStrategy());
    this.strategies = Map.copyOf(map);
  }

  /**
   * Constructor de conveniencia si no usas DI.
   */
  public SessionFactoryProvider(PropertiesManagerService propertiesManager) {
    this(propertiesManager, new HibernateConfigurer(), new EntityScanner());
  }

  /**
   * Construye y devuelve una instancia de {@link SessionFactory}.
   *
   * @param tipoConexion tipo de conexión
   * @return SessionFactory configurada
   * @throws MiSessionFactoryProvider si hay errores al leer properties o construir Hibernate
   */
  public SessionFactory getSessionFactory(TipoConexion tipoConexion) throws MiSessionFactoryProvider {
    Objects.requireNonNull(tipoConexion, "tipoConexion no puede ser null");

    ConnectionStrategy strategy = strategies.get(tipoConexion);
    if (strategy == null) {
      throw new MiSessionFactoryProvider("TipoConexion no soportado: " + tipoConexion);
    }

    try {
      // 1) Resolver propiedades Hibernate + Jakarta JDBC
      ResolvedConfig resolved = strategy.resolve(propertiesManager);

      // 2) Construir Configuration
      Configuration configuration = hibernateConfigurer.buildConfiguration(
          resolved.hibernateProperties());

      // 3) Opcional: escaneo de entidades
      if (resolved.shouldScanEntities()) {
        entityScanner.scanAndAddEntities(configuration, CONFIG_PACKAGE_NAME);
        log.debug("Entidades escaneadas desde '{}'", CONFIG_PACKAGE_NAME);
      }

      // 4) Construir SessionFactory
      SessionFactory sf = configuration.buildSessionFactory();
      log.debug("SessionFactory creada correctamente para {}", tipoConexion);
      return sf;

    } catch (PropertiesManagerException ex) {
      // CLAVE: ahora capturamos la excepción real del servicio de properties
      String msg = "Error leyendo configuración de properties para " + tipoConexion + ": " + ex.getMessage();
      log.error(msg, ex);
      throw new MiSessionFactoryProvider(msg, ex);

    } catch (HibernateException | IllegalArgumentException ex) {
      String msg = "Error creando SessionFactory para " + tipoConexion + ": " + ex.getMessage();
      log.error(msg, ex);
      throw new MiSessionFactoryProvider(msg, ex);
    }
  }

  // =========================================================
  // Strategy + modelos internos
  // =========================================================

  /**
   * Aplica propiedades JDBC comunes (URL, driver, user, password) leyendo de un fichero lógico.
   * Valida claves requeridas antes de asignar.
   */
  private void applyJdbcProperties(Properties target, PropertiesManagerService pm, String file)
      throws PropertiesManagerException {

    boolean ok = pm.validateRequiredKeys(file, REQUIRED_JAKARTA_KEYS);
    if (!ok) {
      throw new PropertiesManagerException(
          "Faltan claves requeridas en '" + file + "'. Requeridas: " + REQUIRED_JAKARTA_KEYS
      );
    }

    target.setProperty(JdbcSettings.JAKARTA_JDBC_URL,
                       pm.getProperty(file, PropertiesKeys.JAKARTA_PERSISTENCE_JDBC_URL));
    target.setProperty(JdbcSettings.JAKARTA_JDBC_DRIVER,
                       pm.getProperty(file, PropertiesKeys.JAKARTA_PERSISTENCE_JDBC_DRIVER));
    target.setProperty(JdbcSettings.JAKARTA_JDBC_USER,
                       pm.getProperty(file, PropertiesKeys.JAKARTA_PERSISTENCE_JDBC_USER));
    target.setProperty(JdbcSettings.JAKARTA_JDBC_PASSWORD,
                       pm.getProperty(file, PropertiesKeys.JAKARTA_PERSISTENCE_JDBC_PASSWORD));
  }

  /**
   * Log seguro: imprime properties pero enmascara password.
   */
  private void safeLogProperties(String tag, Properties props) {
    if (!log.isDebugEnabled()) return;

    log.debug("[{}] Hibernate/JDBC properties:", tag);

    for (String key : props.stringPropertyNames()) {
      String value = props.getProperty(key);

      // No mostrar password
      if (JdbcSettings.JAKARTA_JDBC_PASSWORD.equals(key)
          || PropertiesKeys.JAKARTA_PERSISTENCE_JDBC_PASSWORD.equalsIgnoreCase(key)) {
        log.debug("  {} = {}", key, "********");
      } else {
        log.debug("  {} = {}", key, value);
      }
    }
  }

  /**
   * Estrategia para resolver configuración por tipo de conexión.
   */
  private interface ConnectionStrategy {
    ResolvedConfig resolve(PropertiesManagerService pm) throws PropertiesManagerException;
  }

  /**
   * Resultado inmutable de la resolución de configuración.
   */
  private record ResolvedConfig(Properties hibernateProperties, boolean shouldScanEntities) {
  }

  // =========================================================
  // Helpers
  // =========================================================

  /**
   * Conexión principal: - parte de HIBERNATE (base) - añade Jakarta JDBC desde JAKARTA_PRINCIPAL -
   * escanea entidades
   */
  private final class PrincipalStrategy implements ConnectionStrategy {
    @Override
    public ResolvedConfig resolve(PropertiesManagerService pm) throws PropertiesManagerException {
      Properties props = new Properties();

      // Base Hibernate
      props.putAll(pm.getProperties(PropertiesFiles.HIBERNATE));

      // Jakarta JDBC (principal)
      applyJdbcProperties(props, pm, PropertiesFiles.JAKARTA_PRINCIPAL);

      safeLogProperties("PRINCIPAL", props);
      return new ResolvedConfig(props, true);
    }
  }

  /**
   * Conexión filtro SQL: - crea Properties vacías o mínimas - añade Jakarta JDBC desde
   * JAKARTA_FILTRO - NO escanea entidades (según tu diseño original)
   */
  private final class FiltroSqlStrategy implements ConnectionStrategy {
    @Override
    public ResolvedConfig resolve(PropertiesManagerService pm) throws PropertiesManagerException {
      Properties props = new Properties();
      applyJdbcProperties(props, pm, PropertiesFiles.JAKARTA_FILTRO);

      safeLogProperties("FILTRO_SQL", props);
      return new ResolvedConfig(props, false);
    }
  }
}
