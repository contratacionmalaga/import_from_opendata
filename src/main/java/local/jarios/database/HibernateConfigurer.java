package local.jarios.database;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

/**
 * Clase utilitaria para construir configuraciones de Hibernate a partir de un conjunto de
 * propiedades personalizadas.
 *
 * <p>Se encarga de inicializar un objeto {@link Configuration}
 * de Hibernate y aplicarle las propiedades necesarias para establecer la conexión, el dialecto, y
 * otros parámetros del ORM.</p>
 *
 * <p>Útil para centralizar la inicialización de Hibernate desde código
 * en lugar de usar archivos XML tradicionales.</p>
 */
@Slf4j
public class HibernateConfigurer {

  /**
   * Constructor por defecto.
   */
  public HibernateConfigurer() { /*    */ }

  /**
   * Construye una configuración de Hibernate a partir de las propiedades proporcionadas.
   *
   * @param hibernateProperties Propiedades para configurar Hibernate (conexión, dialecto, etc.)
   * @return Configuration configurada con las propiedades
   */
  public Configuration buildConfiguration(Properties hibernateProperties) {

    log.debug("hibernaterProperties: {}", hibernateProperties);

    Configuration configuration = new Configuration();
    log.debug("Objeto Configuration creado correctamente.");

    // Seteamos las propiedades
    configuration.setProperties(hibernateProperties);
    log.debug("Hibernate Configuration creada con {} propiedades.",
              hibernateProperties.size());

    return configuration;
  }
}
