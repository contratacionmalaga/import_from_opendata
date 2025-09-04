package local.jarios.services;

import local.jarios.common.util.VariablesGlobales;
import local.jarios.database.SessionFactoryRegistry;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.TipoConexion;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.exceptions.MiServiceException;
import local.jarios.repositories.Repository;
import local.jarios.repositories.RepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import java.util.Map;

/**
 * Implementación principal del servicio que maneja operaciones sobre feeds, logs y entidades
 * relacionadas a sindicación.
 *
 * <p>Esta clase utiliza un repositorio para gestionar la persistencia y acceso a datos, permitiendo
 * operaciones CRUD y consultas específicas.</p>
 *
 * Autor: juan
 * Fecha: 28/12/2024
 * Equipo: (vacío)
 */
@Slf4j
public class ServicePrincipalImpl implements ServicePrincipal {

  /**
   * Instancia del repositorio para acceso y gestión de datos. Se utiliza para realizar operaciones
   * CRUD sobre las entidades persistentes.
   */
  private final Repository repository;

  /**
   * Constructor que inicializa los componentes necesarios para la persistencia.
   *
   * @throws MiServiceException Si ocurre un error al crear la {@link SessionFactory}.
   */
  public ServicePrincipalImpl() throws MiServiceException {
    try {
      SessionFactory sessionFactory = SessionFactoryRegistry.getSessionFactory(
          TipoConexion.MARIADB);
      this.repository = new RepositoryImpl(sessionFactory);
    } catch (HibernateException ex) {
      String msg = "Error al obtener la SessionFactory para la conexión MARIADB";
      log.error(msg, ex);
      throw new MiServiceException(msg, ex);
    }
  }

  /**
   * Persiste un objeto {@link Log} en la base de datos.
   *
   * @param miLog Objeto {@link Log} a persistir.
   * @throws MiServiceException En caso de error durante la persistencia.
   */
  @Override
  public void persistirEnBaseDatos(Log miLog) throws MiServiceException {
    try {
      // El repositorio se encarga de la persistencia y manejo de las transacciones
      repository.persistirEnBaseDatos(miLog);
    } catch (MiRepositoryException ex) {
      String msg = String.format("[persistirLog] - Error persistiendo Log con ID %s: %s",
                                 miLog.getId(), ex.getMessage());
      log.error(msg, ex);
      throw new MiServiceException(msg, ex);
    } catch (RuntimeException ex) {
      String msg = String.format(
          "[persistirLog] - Error desconocido al persistir el Log con ID %s: %s", miLog.getId(),
          ex.getMessage());
      log.error(msg, ex);
      throw new MiServiceException(msg, ex);
    }
  }

  /**
   * Obtiene un mapa de entradas (Entries) indexadas por un String, correspondientes a un tipo
   * específico de sindicación.
   *
   * @return un mapa con las entradas encontradas.
   * @throws MiServiceException si ocurre un error en la consulta.
   */
  @Override
  public Map<String, Entry> getMapEntriesEnBaseDatos() throws MiServiceException {

    // Defino la consulta
    String sql = String.format(
        "SELECT e FROM Entry e " +
            "JOIN e.feed f " +
            "JOIN f.miLog l " +
            "WHERE l.tipoSindicacion = %s ", VariablesGlobales.getTipoSindicacion());

    try {
      return repository.getMapEntries(sql);
    } catch (MiRepositoryException ex) {
      String msg = String.format("[getListEntries] - Error en la consulta: %s. Error: %s", sql,
                                 ex.getMessage());
      log.error(msg, ex);
      throw new MiServiceException(msg, ex);
    }
  }
}
