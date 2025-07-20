package local.jarios.repositories;

import jakarta.persistence.TypedQuery;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.models.FiltroOrganoContratacion;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.*;

import java.util.*;
import java.util.function.Function;

/**
 * Implementación del repositorio que gestiona la persistencia y
 * recuperación de datos mediante Hibernate {@link SessionFactory}.
 * <p>
 * Proporciona métodos para persistir logs, obtener entradas y feeds,
 * y ejecutar consultas específicas para filtros.
 * </p>
 * <p>
 * Utiliza transacciones para garantizar la integridad y gestión adecuada
 * de las operaciones sobre la base de datos.
 * </p>
 * <p>
 * Implementa {@link AutoCloseable} para liberar recursos cerrando
 * la {@link SessionFactory} cuando sea necesario.
 * </p>
 *
 * <p><b>Autor:</b> juan</p>
 * <p><b>Fecha:</b> (sin especificar en código)</p>
 * <p><b>Equipo:</b> (vacío)</p>
 */
@Slf4j
public class RepositoryImpl implements Repository, AutoCloseable {

    /**
     * Factoría de sesiones para obtener sesiones Hibernate y ejecutar operaciones.
     */
    private final SessionFactory sessionFactory;

    /**
     * Constructor que recibe la {@link SessionFactory} para trabajar.
     *
     * @param sessionFactory factoría para crear sesiones Hibernate.
     */
    public RepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Persiste un objeto {@link Log} en la base de datos dentro de una transacción.
     *
     * @param miLog objeto {@link Log} a persistir.
     * @throws MiRepositoryException si ocurre algún error en la transacción.
     */
    @Override
    public void persistirMiLogLocal(Log miLog) throws MiRepositoryException {
        ejecutarDentroDeTransaccion(session -> {
            log.debug("[persistirMiLogLocal] - Persistiendo Log.");
            session.persist(miLog);
            flushAndClear(session);
            log.debug("[persistirMiLogLocal] - Persistencia de Log completada.");
            return null;
        }, "persistirMiLogLocal");
    }

    /**
     * Persiste un objeto {@link Log} con entradas a borrar y la persistencia del log,
     * todo dentro de una transacción.
     *
     * @param miLog objeto {@link Log} a persistir.
     * @param setEntriesToDelete conjunto de {@link Entry} a eliminar previamente.
     * @throws MiRepositoryException si ocurre algún error en la transacción.
     */
    public void persistirMiLogInternet(Log miLog, Set<Entry> setEntriesToDelete) throws MiRepositoryException {

        //
        ejecutarDentroDeTransaccion(session -> {
            setEntriesToDelete.forEach(session::remove);
            log.info("[persistirMiLogInternet] - Borrados Entry de la base de datos. {}", setEntriesToDelete.size());
            session.merge(miLog);
            log.info("[persistirMiLogInternet] - Persistencia de Log completada.");
            return null;
        }, "persistirMiLogInternet");
    }

    /**
     * Realiza un flush y clear de la sesión para sincronizar y liberar memoria.
     *
     * @param session sesión de Hibernate.
     */
    private void flushAndClear(Session session) {
        session.flush();
        session.clear();
    }

    /**
     * Ejecuta una función dentro de una transacción Hibernate, gestionando
     * commit, rollback y manejo de excepciones.
     *
     * @param function función que recibe la sesión y retorna un resultado.
     * @param metodo nombre del método para logs y excepciones.
     * @param <R> tipo del resultado esperado.
     * @return resultado de la función.
     * @throws MiRepositoryException si ocurre error durante la transacción.
     */
    private <R> R ejecutarDentroDeTransaccion(Function<Session, R> function, String metodo)
            throws MiRepositoryException {

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            try {
                R result = function.apply(session);
                transaction.commit();
                return result;
            } catch (Exception ex) {
                handleTransactionError(metodo, transaction, ex);
                throw new MiRepositoryException("[" + metodo + "] - Error en transacción", ex);
            }
        }
    }

    /**
     * Maneja el error ocurrido en una transacción, realiza rollback si es posible,
     * y registra el error en logs.
     *
     * @param metodo nombre del método donde ocurrió el error.
     * @param transaction transacción actual.
     * @param ex excepción ocurrida.
     */
    private void handleTransactionError(String metodo, Transaction transaction, Exception ex) {
        log.error("[{}] - Error en transacción: {}", metodo, ex.getMessage(), ex);
        if (transaction != null && transaction.getStatus().canRollback()) {
            try {
                transaction.rollback();
                log.warn("[{}] - Transacción revertida debido a error", metodo);
            } catch (HibernateException rollbackEx) {
                log.error("[{}] - Error durante rollback: {}", metodo, rollbackEx.getMessage(), rollbackEx);
            }
        }
    }

    /**
     * Obtiene un mapa de entradas ({@link Entry}) a partir de una consulta HQL.
     * La clave del mapa es el ID de la entrada.
     *
     * @param sql consulta HQL a ejecutar.
     * @return mapa con claves de ID y valores {@link Entry}.
     * @throws MiRepositoryException si ocurre error en la consulta.
     */
    @Override
    public Map<String, Entry> getMapEntries(String sql) throws MiRepositoryException {
        Map<String, Entry> mapEntries = new HashMap<>();

        return ejecutarDentroDeTransaccion(session -> {
            TypedQuery<Entry> query = session.createQuery(sql, Entry.class);
            List<Entry> listEntries = query.getResultList();
            if (listEntries == null) {
                return mapEntries;
            }
            for (Entry entry : listEntries) {
                mapEntries.put(entry.getIdEntry(), entry);
            }
            return mapEntries;
        }, "getListEntries");
    }

    /**
     * Obtiene el feed más reciente de acuerdo con la consulta proporcionada.
     *
     * @param sql consulta HQL para obtener el feed más reciente.
     * @return objeto {@link Feed} o null si no existe.
     * @throws MiRepositoryException si ocurre error en la consulta.
     */
    @Override
    public Feed getNewestFeed(String sql) throws MiRepositoryException {
        return ejecutarDentroDeTransaccion(session -> {
            TypedQuery<Feed> query = session.createQuery(sql, Feed.class);
            return query.getResultList().stream().findFirst().orElse(null);
        }, "getNewestFeed");
    }

    /**
     * Obtiene la entrada más reciente de acuerdo con la consulta proporcionada.
     *
     * @param sql consulta HQL para obtener la entrada más reciente.
     * @return objeto {@link Entry} o null si no existe.
     * @throws MiRepositoryException si ocurre error en la consulta.
     */
    @Override
    public Entry getNewestEntry(String sql) throws MiRepositoryException {
        return ejecutarDentroDeTransaccion(session -> {
            TypedQuery<Entry> query = session.createQuery(sql, Entry.class);
            return query.getResultList().stream().findFirst().orElse(null);
        }, "getNewestEntry");
    }

    /**
     * Ejecuta una consulta nativa SQL para obtener una lista de
     * {@link FiltroOrganoContratacion} de acuerdo al filtro indicado.
     *
     * @param filtroSQL consulta SQL nativa que define el filtro.
     * @return lista de objetos {@link FiltroOrganoContratacion}.
     * @throws MiRepositoryException si ocurre error en la consulta.
     */
    @Override
    public List<FiltroOrganoContratacion> getListFiltroOcsFromFiltroSql(String filtroSQL) throws MiRepositoryException {
        return ejecutarDentroDeTransaccion(session ->
                        session.createNativeQuery(filtroSQL, FiltroOrganoContratacion.class).getResultList(),
                "getListFiltroOcsFromFiltroSql");
    }

    /**
     * Cierra el {@link SessionFactory} si está abierto, liberando recursos.
     * <p>
     * Se recomienda llamar al finalizar el ciclo de vida de la aplicación.
     * </p>
     */
    @Override
    public void close() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            log.info("[RepositoryImpl] - SessionFactory cerrado correctamente.");
        }
    }

}
