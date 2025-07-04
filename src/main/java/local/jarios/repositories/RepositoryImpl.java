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

@Slf4j
public class RepositoryImpl implements Repository, AutoCloseable {

    private final SessionFactory sessionFactory;

    public RepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void persistirMiLogLocal(Log miLog)
            throws MiRepositoryException {

        ejecutarDentroDeTransaccion(session -> {
            log.debug("[persistirMiLogLocal] - Persistiendo Log.");
            session.persist(miLog);
            flushAndClear(session);
            log.debug("[persistirMiLogLocal] - Persistencia de Log completada.");
            return null;
        }, "persistirMiLogLocal");
    }

    public void persistirMiLogInternet(Log miLog, Set<Entry> setEntriesToDelete)
            throws MiRepositoryException {

        ejecutarDentroDeTransaccion(session -> {
            setEntriesToDelete.forEach(session::remove);
            log.debug("[persistirMiLogInternet] - Borrados '{}' Entries de la base de datos.", setEntriesToDelete.size());
            session.persist(miLog);
            log.debug("[persistirMiLogInternet] - Persistencia de Log completada.");
            flushAndClear(session);
            log.debug("[persistirMiLogInternet] - Flush # Clear de la session.");
            return null;
        }, "persistirMiLogInternet");
    }

    private void procesarEntryRemoto(Session session, Entry entry) {
        Entry existing = session.createQuery(
                        "FROM Entry e WHERE e.idEntry = :idEntry", Entry.class)
                .setParameter("idEntry", entry.getIdEntry())
                .uniqueResult();

        if (existing == null) {
            session.persist(entry);
        } else if (entry.getUpdated() != null &&
                (existing.getUpdated() == null || entry.getUpdated().isAfter(existing.getUpdated()))) {
            session.remove(existing);
            session.persist(entry);
        }
    }

    private void flushAndClear(Session session) {
        // Se puede optimizar si se planea ejecutar en muchas transacciones.
        session.flush();
        session.clear();
    }

    private <R> R ejecutarDentroDeTransaccion(Function<Session, R> function, String metodo)
            throws MiRepositoryException {

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction(); // Usamos beginTransaction directamente.

            try {
                session.setFlushMode(FlushMode.AUTO.toJpaFlushMode());
                R result = function.apply(session);
                transaction.commit();
                return result;
            } catch (Exception ex) {
                handleTransactionError(metodo, transaction, ex);
                throw new MiRepositoryException("[" + metodo + "] - Error en transacción", ex);
            }
        }
    }

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
     * Obtiene el feed más reciente correspondiente a un tipo específico de sindicación.
     *
     * @param sql Consulta a ejecutar sobre la base de datos
     * @return el feed más reciente disponible para el tipo indicado.
     */
    @Override
    public Map<String, Entry> getMapEntries(String sql) throws MiRepositoryException {

        Map<String, Entry> mapEntriesEnBdBorrar = new HashMap<>();

        return ejecutarDentroDeTransaccion(session -> {
            TypedQuery<Entry> query = session.createQuery(sql, Entry.class);
            List<Entry> listEntries = query.getResultList();
            if (listEntries == null) {
                return mapEntriesEnBdBorrar;
            }
            for (Entry entry : listEntries) {
                mapEntriesEnBdBorrar.put(entry.getIdEntry(), entry);
            }
            return mapEntriesEnBdBorrar;
        }, "getListEntries");
    }

    @Override
    public Feed getNewestFeed(String sql) throws MiRepositoryException {
        return ejecutarDentroDeTransaccion(session -> {
            TypedQuery<Feed> query = session.createQuery(sql, Feed.class);
            return query.getResultList().stream().findFirst().orElse(null);
        }, "getNewestFeed");
    }

    @Override
    public List<FiltroOrganoContratacion> getListFiltroOcsFromFiltroSql(String filtroSQL)
            throws MiRepositoryException {

        return ejecutarDentroDeTransaccion(session ->
                        session.createNativeQuery(filtroSQL, FiltroOrganoContratacion.class).getResultList(),
                "getListFiltroOcsFromFiltroSql");
    }

    /**
     * Cierra el `SessionFactory` si está abierto. Llamar al final de la aplicación.
     */
    @Override
    public void close() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            log.info("[RepositoryImpl] - SessionFactory cerrado correctamente.");
        }
    }

    private Set<String> obtenerIdsExistentes(Session session, Set<String> ids) {
        List<String> existentes = session.createQuery(
                        "SELECT e.idEntry FROM Entry e WHERE e.idEntry IN :ids", String.class)
                .setParameter("ids", ids)
                .getResultList();
        return new HashSet<>(existentes);
    }
}
