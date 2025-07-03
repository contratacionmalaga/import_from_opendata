package local.jarios.database;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.enums.TipoConexion;
import local.jarios.exceptions.MiSessionFactoryProvider;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.JdbcSettings;

import java.util.Objects;
import java.util.Properties;

/**
 * Clase encargada de construir una instancia de {@link SessionFactory}
 * utilizando propiedades externas y escaneo automático de entidades JPA.
 */
@Slf4j
public class SessionFactoryProvider {

    private static final String CONFIG_PACKAGE_NAME = "local.jarios.entity";
    private final PropertiesManagerService propertyManager;

    public SessionFactoryProvider() {
        this.propertyManager = PropertiesManagerServiceImpl.getInstance();
    }

    /**
     * Construye y devuelve una instancia de {@link SessionFactory} configurada.
     *
     * @param tipoConexion tipo de conexión a usar
     * @return instancia de {@link SessionFactory}
     * @throws MiSessionFactoryProvider si ocurre un error al crearla
     */
    public SessionFactory getSessionFactory(TipoConexion tipoConexion) throws MiSessionFactoryProvider {
        Objects.requireNonNull(tipoConexion, "TipoConexion no puede ser null");

        try {
            final var hibernateProperties = getHibernateProperties(tipoConexion);
            log.debug("[getSessionFactory] - Propiedades Hibernate obtenidas correctamente para tipo de conexión: {}", tipoConexion);

            final var hibernateConfigurer = new HibernateConfigurer();
            final Configuration configuration = hibernateConfigurer.buildConfiguration(hibernateProperties);

            if (tipoConexion == TipoConexion.MARIADB) {
                final var entityScanner = new EntityScanner();
                entityScanner.scanAndAddEntities(configuration, CONFIG_PACKAGE_NAME);
                log.debug("[getSessionFactory] - Entidades escaneadas desde el paquete '{}'", CONFIG_PACKAGE_NAME);
            }

            final var sessionFactory = configuration.buildSessionFactory();
            log.debug("[getSessionFactory] - SessionFactory creada exitosamente.");
            return sessionFactory;

        } catch (HibernateException | IllegalArgumentException ex) {
            final var msg = String.format("[getSessionFactory] - Error creando SessionFactory: %s", ex.getMessage());
            log.error(msg, ex);
            throw new MiSessionFactoryProvider(msg, ex);
        }
    }

    /**
     * Obtiene las propiedades necesarias para configurar Hibernate.
     *
     * @param tipoConexion tipo de conexión
     * @return propiedades de configuración de Hibernate
     */
    private Properties getHibernateProperties(TipoConexion tipoConexion) {
        return switch (tipoConexion) {
            case MARIADB -> configurePrincipalProperties(
                    propertyManager.getProperties(PropertiesFiles.HIBERNATE));
            case FILTRO_SQL -> configureConnectionProperties(PropertiesFiles.JAKARTA_FILTRO);
        };
    }

    /**
     * Configura propiedades para la conexión principal.
     */
    private Properties configurePrincipalProperties(Properties props) {
        setCommonConnectionProperties(props, PropertiesFiles.JAKARTA_PRINCIPAL);
        return props;
    }

    /**
     * Configura propiedades para cualquier conexión basada en el archivo indicado.
     */
    private Properties configureConnectionProperties(String propertiesFile) {
        final var props = new Properties();
        setCommonConnectionProperties(props, propertiesFile);
        return props;
    }

    /**
     * Asigna propiedades JDBC comunes.
     */
    private void setCommonConnectionProperties(Properties props, String file) {
        props.setProperty(JdbcSettings.JAKARTA_JDBC_URL,
                propertyManager.getProperty(file, PropertiesKeys.JAKARTA_PERSISTENCE_JDBC_URL));
        props.setProperty(JdbcSettings.JAKARTA_JDBC_DRIVER,
                propertyManager.getProperty(file, PropertiesKeys.JAKARTA_PERSISTENCE_JDBC_DRIVER));
        props.setProperty(JdbcSettings.JAKARTA_JDBC_USER,
                propertyManager.getProperty(file, PropertiesKeys.JAKARTA_PERSISTENCE_JDBC_USER));
        props.setProperty(JdbcSettings.JAKARTA_JDBC_PASSWORD,
                propertyManager.getProperty(file, PropertiesKeys.JAKARTA_PERSISTENCE_JDBC_PASSWORD));

        log.debug("Propiedades configuradas desde archivo '{}': {}", file, props);
    }
}
