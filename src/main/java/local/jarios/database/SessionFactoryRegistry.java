package local.jarios.database;

import local.jarios.enums.TipoConexion;
import local.jarios.exceptions.MiSessionFactoryProvider;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;

import java.util.EnumMap;
import java.util.Map;

/**
 * Clase singleton que mantiene una única instancia de {@link SessionFactory} por {@link TipoConexion}.
 * Asegura que las SessionFactory no se creen más de una vez.
 */
@Slf4j
public final class SessionFactoryRegistry {

    private static final Map<TipoConexion, SessionFactory> registry = new EnumMap<>(TipoConexion.class);
    private static final Object lock = new Object();

    private SessionFactoryRegistry() {
        // Previene instanciación
    }

    /**
     * Obtiene (o crea si no existe) la SessionFactory para el tipo de conexión.
     *
     * @param tipoConexion tipo de conexión
     * @return instancia única de SessionFactory
     * @throws MiSessionFactoryProvider si hay error de configuración
     */
    public static SessionFactory getSessionFactory(TipoConexion tipoConexion) throws MiSessionFactoryProvider {
        synchronized (lock) {
            if (!registry.containsKey(tipoConexion)) {
                log.debug("[SessionFactoryRegistry] - Creando SessionFactory para tipo de conexión: {}", tipoConexion);
                SessionFactory factory = new SessionFactoryProvider().getSessionFactory(tipoConexion);
                registry.put(tipoConexion, factory);
            }
            return registry.get(tipoConexion);
        }
    }

    /**
     * Cierra todas las SessionFactory abiertas.
     */
    public static void closeAll() {
        synchronized (lock) {
            registry.forEach((tipo, factory) -> {
                if (factory != null && !factory.isClosed()) {
                    factory.close();
                    log.info("[SessionFactoryRegistry] - Cerrada SessionFactory para tipo: {}", tipo);
                }
            });
            registry.clear();
        }
    }
}
