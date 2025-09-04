package local.jarios.database;

import jakarta.persistence.Entity;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.Configuration;
import org.reflections.Reflections;

import java.util.Collection;

/**
 * Clase encargada de escanear paquetes y registrar automáticamente clases anotadas como
 * {@link jakarta.persistence.Entity} en la configuración de Hibernate.
 *
 * <p>Utiliza la librería <a href="https://github.com/ronmamo/reflections">Reflections</a>
 * para detectar entidades JPA en tiempo de ejecución.</p>
 *
 * <p>Requiere que las clases estén accesibles en el classpath en tiempo de ejecución.</p>
 */
@Slf4j
public class EntityScanner {

  /**
   * Constructor sin argumentos.
   */
  public EntityScanner() {
    // Constructor vacío
  }

  public void scanAndAddEntities(Configuration configuration, String packageName) {

    // Usamos Reflections para escanear el paquete indicado
    var reflections = new Reflections(packageName);
    log.debug("[scanAndAddEntities] - Objeto Relections creado correctamente para el paquete: {}",
              packageName);

    // Obtenemos todas las clases anotadas con @Entity
    Collection<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    log.debug("[scanAndAddEntities] - Colección con todas las clases anotadas con @entity: {}",
              entities.size());

    // Añadimos cada entidad a la configuración de Hibernate
    for (Class<?> entityClass : entities) {
      configuration.addAnnotatedClass(entityClass);
      log.debug("[scanAndAddEntities] - {}", entityClass.getName());
    }
  }
}
