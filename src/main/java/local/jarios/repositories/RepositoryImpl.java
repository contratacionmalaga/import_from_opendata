package local.jarios.repositories;

import jakarta.persistence.TypedQuery;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.enums.LugarImportacion;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.models.FiltroOrganoContratacion;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Slf4j
public class RepositoryImpl implements Repository, AutoCloseable {

    private static final int DEFAULT_BATCH_SIZE = 50;

    private final SessionFactory sessionFactory;

    public RepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void persistirLogYDatos(Log miLog, Map<String, Entry> mapBaseDatos, LugarImportacion lugarImportacion)
            throws MiRepositoryException {

        ejecutarDentroDeTransaccion(session -> {
            log.debug("[persistirLog] - Persistiendo Log.");
            session.persist(miLog);
            flushAndClear(session);
            log.debug("[persistirLog] - Persistencia de Log completada.");
            return null;
        }, "persistirLog");
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

    private String formatContador(int actual, int total) {
        int padding = String.valueOf(total).length();
        return String.format("%0" + padding + "d/%0" + padding + "d", actual, total);
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
