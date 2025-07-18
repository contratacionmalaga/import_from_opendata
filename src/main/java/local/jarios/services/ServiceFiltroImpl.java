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
import org.hibernate.SessionFactory;

import java.util.List;

/**
 * Implementación del servicio encargado de gestionar consultas
 * relacionadas con filtros para órganos de contratación.
 * <p>
 * Utiliza un repositorio para acceder a la base de datos y
 * ejecutar consultas SQL específicas para obtener los datos filtrados.
 * </p>
 *
 * <p><b>Autor:</b> juan</p>
 * <p><b>Fecha:</b> 28/12/2024</p>
 * <p><b>Equipo:</b> (vacío)</p>
 */
@Slf4j
public class ServiceFiltroImpl implements ServiceFiltro {

    /**
     * Repositorio para acceso y gestión de datos.
     * Se usa para realizar operaciones CRUD y consultas personalizadas.
     */
    private final Repository repository;

    /**
     * Constructor que inicializa el repositorio con la {@link SessionFactory}
     * adecuada para la conexión FILTRO_SQL.
     *
     * @throws MiSessionFactoryProvider si ocurre un error al obtener la {@link SessionFactory}.
     */
    public ServiceFiltroImpl() throws MiSessionFactoryProvider {
        SessionFactory sessionFactory = SessionFactoryRegistry.getSessionFactory(TipoConexion.FILTRO_SQL);
        log.debug("[ServiceFiltroImpl] Obtenido el objeto SessionFactory correctamente.");
        this.repository = new RepositoryImpl(sessionFactory);
        log.debug("[ServiceFiltroImpl] Obtenido el objeto Repository correctamente.");
    }

    /**
     * Ejecuta una consulta SQL para obtener una lista de objetos {@link FiltroOrganoContratacion}
     * que coinciden con el filtro especificado.
     *
     * @param filtroSQL Consulta SQL en forma de cadena que define el filtro a aplicar.
     * @return Lista de objetos {@link FiltroOrganoContratacion} que cumplen el filtro.
     * @throws MiServiceException si ocurre un error al ejecutar la consulta o acceder a datos.
     */
    @Override
    public List<FiltroOrganoContratacion> getListFiltroOcsFromFiltroSql(String filtroSQL) throws MiServiceException {
        String msg;

        try {
            return repository.getListFiltroOcsFromFiltroSql(filtroSQL);
        } catch (MiRepositoryException ex) {
            msg = String.format("[getListFiltroOcsFromFiltroSql] - Error en la consulta: %s. Error: %s",
                    filtroSQL, ex.getMessage());
            log.error(msg, ex);
            throw new MiServiceException(msg, ex);
        } catch (RuntimeException ex) {
            msg = String.format("[getListFiltroOcsFromFiltroSql] - Error de ejecución en la consulta: %s. Error: %s",
                    filtroSQL, ex.getMessage());
            log.error(msg, ex);
            throw new MiServiceException(msg, ex);
        }
    }
}
