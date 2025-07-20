package local.jarios.services;

import local.jarios.database.SessionFactoryRegistry;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.TipoConexion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.exceptions.MiServiceException;
import local.jarios.repositories.Repository;
import local.jarios.repositories.RepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import java.util.Map;

/**
 * Implementación principal del servicio que maneja operaciones sobre feeds, logs
 * y entidades relacionadas a sindicación.
 * <p>
 * Esta clase utiliza un repositorio para gestionar la persistencia y acceso a datos,
 * permitiendo operaciones CRUD y consultas específicas.
 * </p>
 *
 * <p><b>Autor:</b> juan</p>
 * <p><b>Fecha:</b> 28/12/2024</p>
 * <p><b>Equipo:</b> (vacío)</p>
 */
@Slf4j
public class ServicePrincipalImpl implements ServicePrincipal {

    /**
     * Instancia del repositorio para acceso y gestión de datos.
     * Se utiliza para realizar operaciones CRUD sobre las entidades persistentes.
     */
    private final Repository repository;

    /**
     * Constructor que inicializa los componentes necesarios para la persistencia.
     *
     * @throws MiServiceException Si ocurre un error al crear la {@link SessionFactory}.
     */
    public ServicePrincipalImpl() throws MiServiceException {
        try {
            SessionFactory sessionFactory = SessionFactoryRegistry.getSessionFactory(TipoConexion.MARIADB);
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
            String msg = String.format("[persistirLog] - Error persistiendo Log con ID %s: %s", miLog.getId(), ex.getMessage());
            log.error(msg, ex);
            throw new MiServiceException(msg, ex);
        } catch (RuntimeException ex) {
            String msg = String.format("[persistirLog] - Error desconocido al persistir el Log con ID %s: %s", miLog.getId(), ex.getMessage());
            log.error(msg, ex);
            throw new MiServiceException(msg, ex);
        }
    }

    /**
     * Obtiene la entrada (Entry) más reciente para un tipo específico de sindicación.
     *
     * @param tipoSindicacion el tipo de sindicación (RSS, Atom, etc.).
     * @return la entrada más reciente disponible para el tipo indicado.
     * @throws MiServiceException si ocurre un error en la consulta.
     */
    public Entry getNewestEntry(TipoSindicacion tipoSindicacion) throws MiServiceException {
        String sql = String.format(
                "SELECT e FROM Entry e " +
                        "JOIN e.feed f " +
                        "JOIN f.miLog l " +
                        "WHERE l.tipoSindicacion = %s " +
                        "ORDER BY f.updated DESC", tipoSindicacion);
        log.info("[getNewestEntry] - Consulta: {}", sql);

        try {
            return repository.getNewestEntry(sql);
        } catch (MiRepositoryException ex) {
            String msg = String.format("[getNewestEntry] - Error en la consulta: %s. Error: %s", sql, ex.getMessage());
            log.error(msg, ex);
            throw new MiServiceException(msg, ex);
        }
    }

    /**
     * Obtiene un mapa de entradas (Entries) indexadas por un String,
     * correspondientes a un tipo específico de sindicación.
     *
     * @param tipoSindicacion el tipo de sindicación (RSS, Atom, etc.).
     * @return un mapa con las entradas encontradas.
     * @throws MiServiceException si ocurre un error en la consulta.
     */
    @Override
    public Map<String, Entry> getMapEntries(TipoSindicacion tipoSindicacion) throws MiServiceException {
        String sql = String.format(
                "SELECT e FROM Entry e " +
                        "JOIN e.feed f " +
                        "JOIN f.miLog l " +
                        "WHERE l.tipoSindicacion = %s ", tipoSindicacion);

        try {
            return repository.getMapEntries(sql);
        } catch (MiRepositoryException ex) {
            String msg = String.format("[getListEntries] - Error en la consulta: %s. Error: %s", sql, ex.getMessage());
            log.error(msg, ex);
            throw new MiServiceException(msg, ex);
        }
    }
}
