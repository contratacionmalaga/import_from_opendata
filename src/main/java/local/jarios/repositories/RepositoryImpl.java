package local.jarios.repositories;

import jakarta.persistence.TypedQuery;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.enums.LugarImportacion;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.models.FiltroOrganoContratacion;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
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

    /**
     * Lee el batch size desde el fichero de propiedades.
     * Si el valor no es válido, devuelve un valor por defecto seguro.
     */
    private int cargarBatchSizeDesdeProperties() {

        //
        PropertiesManagerService propertiesManager = PropertiesManagerServiceImpl.getInstance();
        String valor = propertiesManager
                .getProperty(
                        PropertiesFiles.HIBERNATE,
                        PropertiesKeys.HIBERNATE_JDBC_BATCH_SIZE);

        try {
            int parsed = Integer.parseInt(valor);
            if (parsed <= 0) throw new NumberFormatException("El valor debe ser mayor a cero.");
            log.debug("[RepositoryImpl] - Batch size configurado: {}", parsed);
            return parsed;
        } catch (Exception e) {
            log.debug("[RepositoryImpl] - Valor inválido para '{}': '{}'. Usando valor por defecto: {}",
                    PropertiesKeys.HIBERNATE_JDBC_BATCH_SIZE, valor, DEFAULT_BATCH_SIZE);
            return DEFAULT_BATCH_SIZE;
        }
    }

    @Override
    public void persistirLogYDatos(Log miLog, Map<String, Entry> mapBaseDatos, LugarImportacion lugarImportacion)
            throws MiRepositoryException {

        ejecutarDentroDeTransaccion(session -> {

            log.debug("[persistirLog] - Inicio persistencia Log.");
            session.persist(miLog);
            log.debug("[persistirLog] - Final persistencia Log.");

            flushAndClear(session);
            log.debug("[persistirLog] - Flush ejecutado.");

            /*
            grabarMap(session, mapBaseDatos, lugarImportacion);
            log.debug("[persistirLog] - Mapa Entry persistido. Total: {}",
                    StringHelper.getNumeroConFormato(mapBaseDatos.size()));
             */
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
        session.flush();
        session.clear();
    }

    private <R> R ejecutarDentroDeTransaccion(Function<Session, R> function, String metodo)
            throws MiRepositoryException {

        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            session.setFlushMode(FlushMode.AUTO.toJpaFlushMode());

            transaction = TransactionManager.beginTransaction(session);
            R result = function.apply(session);
            TransactionManager.commitTransaction(transaction);

            return result;

        } catch (Exception ex) {
            log.error("[{}] - Error en transacción: {}", metodo, ex.getMessage(), ex);

            if (transaction != null && transaction.getStatus().canRollback()) {
                try {
                    TransactionManager.rollbackTransaction(transaction);
                } catch (Exception rollbackEx) {
                    log.error("[{}] - Error durante rollback: {}", metodo, rollbackEx.getMessage(), rollbackEx);
                }
            }

            throw new MiRepositoryException("[" + metodo + "] - Error en transacción", ex);
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
