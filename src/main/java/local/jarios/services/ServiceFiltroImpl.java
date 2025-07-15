package local.jarios.services;

import local.jarios.database.SessionFactoryRegistry;
import local.jarios.enums.TipoConexion;

import local.jarios.exceptions.MiRepositoryException;
import local.jarios.exceptions.MiServiceException;
import local.jarios.exceptions.MiSessionFactoryProvider;
import local.jarios.models.FiltroOrganoContratacion;
import local.jarios.repositories.Repository;
import local.jarios.repositories.RepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import java.util.List;

/**
 * Description:
 * Author: juan
 * Date: 28/12/2024
 * Team:
 */
@Slf4j
public class ServiceFiltroImpl implements ServiceFiltro {

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
    public ServiceFiltroImpl() throws MiSessionFactoryProvider {

        SessionFactory sessionFactory = SessionFactoryRegistry.getSessionFactory(TipoConexion.FILTRO_SQL);
        log.debug("[ServiceFiltroImpl] Obtenido el objeto SessionFactory correctamente.");
        this.repository = new RepositoryImpl(sessionFactory);
        log.debug("[ServiceFiltroImpl] Obtenido el objeto Repository correctamente.");
    }

    @Override
    public List<FiltroOrganoContratacion> getListFiltroOcsFromFiltroSql(String filtroSQL) throws MiServiceException {

        String msg;

        try {

            return repository.getListFiltroOcsFromFiltroSql(filtroSQL);

        } catch (MiRepositoryException ex) {

            msg = String.format("[getListFiltroOcsFromFiltroSql] - Error en la consunta: %s. Error: %s",
                    filtroSQL, ex.getMessage());
            log.error(msg, ex);
            throw new MiServiceException(msg, ex);

        } catch (RuntimeException  ex) {

            msg = String.format("[getListFiltroOcsFromFiltroSql] - Error de ejecución en la consulta: %s. Error: %s",
                    filtroSQL, ex.getMessage());
            log.error(msg, ex);
            throw new MiServiceException(msg, ex);

        }
    }
}
