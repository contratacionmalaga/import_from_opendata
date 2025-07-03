package local.jarios.services;

import local.jarios.database.SessionFactoryRegistry;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.enums.LugarImportacion;
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
import java.util.Set;

/**
 * Description:
 * Author: juan
 * Date: 28/12/2024
 * Team:
 */
@Slf4j
public class ServicePrincipalImpl implements ServicePrincipal {

    /**
     * Instancia del repositorio para acceso y gestión de datos.
     * <p>
     * Se utiliza para realizar operaciones CRUD sobre las entidades persistentes.
     * </p>
     */
    private final Repository repository;

    /**
     * Constructor que inicializa los componentes necesarios para la persistencia.
     *
     * @throws HibernateException Si ocurre un error al crear la {@link SessionFactory}.
     */
    public ServicePrincipalImpl() throws MiServiceException {

        SessionFactory sessionFactory = SessionFactoryRegistry.getSessionFactory(TipoConexion.MARIADB);
        this.repository = new RepositoryImpl(sessionFactory);
    }

    /**
     * Persiste un objeto {@link Log} en la base de datos.
     *
     * @param miLog Objeto {@link Log} a persistir.
     */
    @Override
    public void persistirMiLogLocal(Log miLog, Map<String, Entry> mapBaseDatos, LugarImportacion lugarImportacion) throws MiServiceException {

        try {

            // El repositorio se encarga de la persistencia y manejo de la las transacciones
            repository.persistirLogYDatos(miLog, mapBaseDatos, lugarImportacion);

        } catch (MiRepositoryException ex) {

            String msg = String.format("[persistirLog] - Error persistiendo Log con ID %s: %s", miLog.getId(), ex.getMessage());
            log.error(msg, ex.getMessage(), ex);
            throw new MiServiceException (msg, ex);

        } catch (RuntimeException ex) {

            String msg = String.format("[persistirLog] - Error desconocido al persisitir el Log con ID %s: %s", miLog.getId(), ex.getMessage());
            log.error(msg, ex.getMessage(), ex);
            throw new MiServiceException(msg, ex);

        }
    }

    /**
     * Persiste un objeto {@link Log} en la base de datos.
     *
     * @param miLog Objeto {@link Log} a persistir.
     */
    @Override
    public void persistirMiLogInternet(Log miLog, Set<String> setEntrysToDelete) throws MiServiceException {

        try {

            // El repositorio se encarga de la persistencia y manejo de la las transacciones
            // repository.persistirLogYDatos(miLog, mapBaseDatos, lugarImportacion);

        } catch (MiRepositoryException ex) {

            String msg = String
                            .format(
                                    "[persistirLog] - Error persistiendo Log con ID %s: %s",
                                    miLog.getId(),
                                    ex.getMessage());
            log.error(msg, ex.getMessage(), ex);
            throw new MiServiceException (msg, ex);

        } catch (RuntimeException ex) {

            String msg = String
                            .format(
                                    "[persistirLog] - Error desconocido al persisitir el Log con ID %s: %s",
                                    miLog.getId(),
                                    ex.getMessage());
            log.error(msg, ex.getMessage(), ex);
            throw new MiServiceException(msg, ex);

        }
    }

    @Override
    public Feed getNewestFeed(TipoSindicacion tipoSindicacion) throws MiServiceException {

        String msg;

        //
        String sql = "SELECT f FROM Feed f " +
                "JOIN f.miLog l " +
                "WHERE l.tipoSindicacion = :tipoSindicacion " +
                "ORDER BY f.updated DESC";

        try {

            return repository.getNewestFeed(sql);

        } catch (MiRepositoryException ex) {

            msg = String.format("[getListFiltroOcsFromFiltroSql] - Error en la consunta: %s. Error: %s", sql, ex.getMessage());
            log.error(msg, ex);
            throw new MiServiceException(msg, ex);

        } catch (RuntimeException  ex) {

            msg = String.format("[getListFiltroOcsFromFiltroSql] - Error de ejecución en la consulta: %s. Error: %s", sql, ex.getMessage());
            log.error(msg, ex);
            throw new MiServiceException(msg, ex);

        }
    }
}
