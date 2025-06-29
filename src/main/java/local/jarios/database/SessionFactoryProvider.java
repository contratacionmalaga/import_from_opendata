package local.jarios.database;

import local.jarios.common.util.Constantes;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.enums.TipoConexion;
import local.jarios.exceptions.MiSessionFactoryProvider;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.JdbcSettings;

import java.util.Properties;

/**
 * Clase encargada de construir una instancia de {@link SessionFactory}
 * utilizando propiedades externas y escaneo automático de entidades JPA.
 *
 * <p>Este proveedor centraliza la creación de una configuración completa de Hibernate,
 * incluyendo la lectura del fichero de propiedades, construcción del objeto
 * {@link org.hibernate.cfg.Configuration}, y el escaneo del paquete que contiene las entidades
 * anotadas con {@code @Entity}.</p>
 *
 * <p>La configuración de Hibernate se obtiene a partir de un fichero de propiedades
 * definido en {@link Constantes#HIBERNATE_PROPERTIES}.</p>
 *
 * <p>Esta clase depende de los siguientes componentes:
 * <ul>
 *     <li>{@link PropertiesManagerService} para obtener las propiedades de configuración</li>
 *     <li>{@link HibernateConfigurer} para construir la configuración de Hibernate</li>
 *     <li>{@link EntityScanner} para registrar las entidades JPA dinámicamente</li>
 * </ul>
 * </p>
 */
@Slf4j
public class SessionFactoryProvider {

    /**
     * Paquete donde se encuentran las entidades JPA para el escaneo automático.
     */
    private static final String CONFIG_PACKAGE_NAME = "local.jarios.entity";

    /**
     * Constructor vacío.
     */
    public SessionFactoryProvider() {
        // Constructor por defecto
    }

    /**
     * Construye y devuelve una instancia de {@link SessionFactory} configurada
     * con las propiedades Hibernate proporcionadas.
     *
     * @return Instancia de {@link SessionFactory} configurada.
     * @throws HibernateException Si ocurre un error durante la creación de la SessionFactory.
     */
    public SessionFactory getSessionFactory(TipoConexion tipoConexion) throws MiSessionFactoryProvider {

        try {

            Properties hibernateProperties = getUpdateHibernateProperties(tipoConexion);
            log.debug("[getSessionFactory] - Obtenidas las Properties correctamente del fichero {}.", Constantes.HIBERNATE_PROPERTIES);

            var hibernateConfigurer = new HibernateConfigurer();
            log.debug("[getSessionFactory] - Objeto HibernateConfigurer creado correctamente.");

            var configuration = hibernateConfigurer.buildConfiguration(hibernateProperties);
            log.debug("[getSessionFactory] - Configuración Hibernate creada correctamente.");

            // Aquí podemos agregar el escaneo de entidades y la configuración del DataSource, si es necesario
            if (tipoConexion == TipoConexion.PRINCIPAL) {

                var entityScanner = new EntityScanner();
                log.debug("[getSessionFactory] - Objeto EntityScanner creado correctamente.");

                entityScanner.scanAndAddEntities(configuration, CONFIG_PACKAGE_NAME);
                log.debug("[getSessionFactory] - Entidades escaneadas y añadidas desde el paquete '{}'.", CONFIG_PACKAGE_NAME);

            }

            var sessionFactory = configuration.buildSessionFactory();
            log.debug("[getSessionFactory] - SessionFactory creada exitosamente.");
            return sessionFactory;

        } catch (HibernateException ex) {

            String msg = String.format("[getSessionFactory] - Error creando SessionFactory: %s", ex.getMessage());
            log.error(msg, ex);
            throw new MiSessionFactoryProvider (msg, ex); // Propagar la excepción para que el llamador la maneje

        } catch (IllegalArgumentException ex) {

            String msg = String.format("[getSessionFactory] - Error en los parámetros: %s", ex.getMessage());
            log.error(msg, ex);
            throw new MiSessionFactoryProvider (msg, ex); // Propagar la excepción para que el llamador la maneje

        }
    }

    /**
     *
     * @param tipoConexion Tipo de Conexión (Enumerado)
     * @return Devuelve un objeto Properties
     */
    private Properties getUpdateHibernateProperties(TipoConexion tipoConexion) throws IllegalArgumentException {

        log.debug("[getUpdateHibernateProperties] - Obteniendo propiedades Hibernate para tipoConexion: {}", tipoConexion);

        switch (tipoConexion) {
            case TipoConexion.PRINCIPAL -> {
                Properties p = PropertiesManagerServiceImpl.getInstance().getProperties(Constantes.HIBERNATE_PROPERTIES);
                log.info("[getUpdateHibernateProperties] - Propiedades obtenidas para PRINCIPAL: {}", p);
                return p;
            }
            case TipoConexion.FILTRO_SQL -> {
                Properties p = getFiltroSqlProperties();
                log.info("[getUpdateHibernateProperties] - Propiedades obtenidas para FILTRO_SQL: {}", p);
                return p;
            }
            default -> throw new IllegalArgumentException("TipoConexion no soportado: " + tipoConexion);
        }
    }

    /**
     *
     * @return Devuelve un objeto Properties
     */
    private Properties getFiltroSqlProperties() {

        log.debug("[getFiltroSqlProperties] - Obteniendo propiedades filtro SQL");

        PropertiesManagerService propertyManager = PropertiesManagerServiceImpl.getInstance();
        Properties filtroSqlProperties = new Properties();

        filtroSqlProperties.setProperty(
                JdbcSettings.JAKARTA_JDBC_URL,
                propertyManager.getProperty(Constantes.HIBERNATE_PROPERTIES, PropertiesKeys.HIBERNATE_FILTER_URL));
        log.debug("[getFiltroSqlProperties] - URL configurada: {}", filtroSqlProperties.getProperty(JdbcSettings.JAKARTA_JDBC_URL));

        filtroSqlProperties.setProperty(
                JdbcSettings.JAKARTA_JDBC_DRIVER,
                propertyManager.getProperty(Constantes.HIBERNATE_PROPERTIES, PropertiesKeys.HIBERNATE_FILTER_DRIVER));
        log.debug("[getFiltroSqlProperties] - Driver configurado: {}", filtroSqlProperties.getProperty(JdbcSettings.JAKARTA_JDBC_DRIVER));

        filtroSqlProperties.setProperty(
                JdbcSettings.JAKARTA_JDBC_USER,
                propertyManager.getProperty(Constantes.HIBERNATE_PROPERTIES, PropertiesKeys.HIBERNATE_FILTER_USER));
        log.debug("[getFiltroSqlProperties] - Usuario configurado: {}", filtroSqlProperties.getProperty(JdbcSettings.JAKARTA_JDBC_USER));

        filtroSqlProperties.setProperty(
                JdbcSettings.JAKARTA_JDBC_PASSWORD,
                propertyManager.getProperty(Constantes.HIBERNATE_PROPERTIES, PropertiesKeys.HIBERNATE_FILTER_PASSWORD));
        log.debug("[getFiltroSqlProperties] - Password configurado (oculto en logs por seguridad)");

        log.debug("[getFiltroSqlProperties] - FilterProperties: {}", filtroSqlProperties);

        return filtroSqlProperties;
    }
}
