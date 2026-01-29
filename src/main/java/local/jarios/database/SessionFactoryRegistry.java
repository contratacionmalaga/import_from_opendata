package local.jarios.database;

import local.jarios.enums.TipoConexion;
import local.jarios.exceptions.MiSessionFactoryProvider;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro de SessionFactory: una instancia por {@link TipoConexion}. Diseño: - Thread-safe sin
 * locks explícitos (computeIfAbsent). - Provider configurable (DI suave). - closeAll() seguro.
 */
@Slf4j
public final class SessionFactoryRegistry {

  private final ConcurrentHashMap<TipoConexion, SessionFactory> registry = new ConcurrentHashMap<>();
  private volatile SessionFactoryProvider provider;

  private SessionFactoryRegistry(SessionFactoryProvider provider) {
    this.provider = Objects.requireNonNull(provider, "provider");
  }

  private static SessionFactoryProvider defaultProvider() {
    PropertiesManagerService pm = PropertiesManagerServiceImpl.getInstance();
    return new SessionFactoryProvider(pm);
  }

  /**
   * API estática compatible con tu uso actual.
   */
  public static SessionFactory getSessionFactory(TipoConexion tipoConexion) throws MiSessionFactoryProvider {
    return Holder.INSTANCE.getOrCreate(tipoConexion);
  }

  /**
   * Permite inyectar un provider distinto (tests / configuración especial). Llama a esto al inicio
   * del programa si quieres controlar el provider.
   */
  public static void setProvider(SessionFactoryProvider provider) {
    Objects.requireNonNull(provider, "provider");
    Holder.INSTANCE.provider = provider;
  }

  /**
   * Cierra todas las SessionFactory abiertas y limpia el registro.
   */
  public static void closeAll() {
    Holder.INSTANCE.closeAndClear();
  }

  private SessionFactory getOrCreate(TipoConexion tipoConexion) throws MiSessionFactoryProvider {
    Objects.requireNonNull(tipoConexion, "tipoConexion");

    try {
      return registry.computeIfAbsent(tipoConexion, tc -> {
        try {
          log.debug("Creando SessionFactory para {}", tc);
          return provider.getSessionFactory(tc);
        } catch (MiSessionFactoryProvider e) {
          // computeIfAbsent no permite checked, y necesitamos propagarlo:
          throw new SessionFactoryCreationRuntimeException(e);
        }
      });

    } catch (SessionFactoryCreationRuntimeException wrapper) {
      // No dejamos un valor inválido en caché
      registry.remove(tipoConexion);
      throw wrapper.getCauseAsMiSessionFactoryProvider();
    }
  }

  // ==========================
  // Implementación interna
  // ==========================

  private void closeAndClear() {
    // Hacemos snapshot para evitar concurrent modification
    Map<TipoConexion, SessionFactory> snapshot = new EnumMap<>(TipoConexion.class);
    snapshot.putAll(registry);

    snapshot.forEach((tipo, factory) -> {
      try {
        if (factory != null && !factory.isClosed()) {
          factory.close();
          log.debug("Cerrada SessionFactory para tipo: {}", tipo);
        }
      } catch (Exception e) {
        log.warn("No se pudo cerrar SessionFactory para tipo {}: {}", tipo, e.getMessage(), e);
      } finally {
        registry.remove(tipo);
      }
    });
  }

  /**
   * Singleton Holder (lazy, thread-safe).
   */
  private static final class Holder {
    private static final SessionFactoryRegistry INSTANCE =
        new SessionFactoryRegistry(defaultProvider());
  }

  /**
   * Wrapper runtime para poder re-lanzar MiSessionFactoryProvider fuera de computeIfAbsent.
   */
  private static final class SessionFactoryCreationRuntimeException extends RuntimeException {
    private final MiSessionFactoryProvider cause;

    SessionFactoryCreationRuntimeException(MiSessionFactoryProvider cause) {
      super(cause);
      this.cause = cause;
    }

    MiSessionFactoryProvider getCauseAsMiSessionFactoryProvider() {
      return cause;
    }
  }
}
