package local.jarios.repositories;

import jakarta.persistence.TypedQuery;
import local.jarios.database.SessionFactoryProvider;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoConexion;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.exceptions.MiTransactionManagerException;
import local.jarios.helpers.StringHelper;
import local.jarios.models.FiltroOrganoContratacion;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class RepositoryImpl implements Repository {

    private static final int BATCH_SIZE =
            Integer.parseInt(System.getProperty("repository.batch.size", "50"));

    private final SessionFactory sessionFactory;

    public RepositoryImpl(TipoConexion tipoConexion) throws MiRepositoryException {
        try {
            this.sessionFactory = new SessionFactoryProvider().getSessionFactory(tipoConexion);
            log.debug("[RepositoryImpl] - SessionFactory inicializada correctamente.");
        } catch (HibernateException ex) {
            String msg = String.format("[RepositoryImpl] - Error creando SessionFactory. Error: %s", ex.getMessage());
            log.error(msg, ex);
            throw new MiRepositoryException(msg, ex);
        }
    }

    @Override
    public void persistirLog(Log miLog, Map<String, Entry> mapBaseDatos, LugarImportacion lugarImportacion)
            throws MiRepositoryException {

        ejecutarDentroDeTransaccion(session -> {
            session.persist(miLog);
            log.debug("[persistirLog] - Persistido Log (Estadística y Configuración).");

            grabarMap(session, mapBaseDatos, lugarImportacion);
            log.debug("[persistirLog] - Map de Entry persistido. Registros: {}", mapBaseDatos.size());

            return null;
        }, "persistirLog");
    }

    private void grabarMap(Session session, Map<String, Entry> mapBaseDatos, LugarImportacion lugarImportacion) {
        boolean esLocal = lugarImportacion.isLocalImport();
        int total = mapBaseDatos.size();

        log.info("[grabarMap] - Iniciando persistencia de {} registros. esLocal: {}", total, esLocal);

        int contador = 0;

        for (Entry entry : mapBaseDatos.values()) {
            contador++;
            String indiceFormateado = formatContador(contador, total);

            if (esLocal) {
                session.persist(entry);
            } else {
                procesarEntryRemoto(session, entry);
            }

            log.info("[grabarMap] - {} Persistido entry ID: {}", indiceFormateado, entry.getIdEntry());

            if (contador % BATCH_SIZE == 0) {
                log.info("[grabarMap] - Contador múltipo de {}. Valor contardor: {} ", BATCH_SIZE, StringHelper.getNumeroConFormato(contador));
                flushAndClear(session);
            }
        }

        flushAndClear(session); // Flush final
        log.debug("[grabarMap] - Persistencia completada. Total: {}", total);
    }

    private void procesarEntryRemoto(Session session, Entry entry) {
        Entry existingEntry = session.createQuery(
                        "FROM Entry e WHERE e.idEntry = :idEntry", Entry.class)
                .setParameter("idEntry", entry.getIdEntry())
                .uniqueResult();

        if (existingEntry == null) {
            session.persist(entry);
        } else {
            LocalDateTime existingUpdated = existingEntry.getUpdated();
            LocalDateTime newUpdated = entry.getUpdated();

            if (newUpdated != null && (existingUpdated == null || newUpdated.isAfter(existingUpdated))) {
                session.remove(existingEntry);
                session.persist(entry);
            }
        }
    }

    private String formatContador(int actual, int total) {
        int padding = String.valueOf(total).length();
        return String
                .format("%0" + padding + "d/%0" + padding + "d",
                        StringHelper.getNumeroConFormato(actual),
                        StringHelper.getNumeroConFormato(total));
    }

    private void flushAndClear(Session session) {
        session.flush();
        session.clear();
    }

    private <R> R ejecutarDentroDeTransaccion(Function<Session, R> function, String metodo)
            throws MiRepositoryException {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            session.setFlushMode(FlushMode.MANUAL.toJpaFlushMode());
            transaction = TransactionManager.beginTransaction(session);
            R result = function.apply(session);
            TransactionManager.commitTransaction(transaction);
            return result;

        } catch (Exception ex) {
            log.error("[{}] - Error en transacción: {}", metodo, ex.getMessage());

            if (transaction != null && transaction.getStatus().canRollback()) {
                try {
                    TransactionManager.rollbackTransaction(transaction);
                } catch (MiTransactionManagerException rollbackEx) {
                    log.error("[{}] - Error durante rollback: {}", metodo, rollbackEx.getMessage());
                }
            }

            throw new MiRepositoryException("[" + metodo + "] - Error en transacción", ex);
        }
    }

    @Override
    public Feed getNewestFeed(String sql) throws MiRepositoryException {

        //
        return ejecutarDentroDeTransaccion(session -> {
            TypedQuery<Feed> query = session.createQuery(sql, Feed.class);
            return query.getResultList().stream().findFirst().orElse(null);
        }, "getNewestFeed");
    }

    @Override
    public List<FiltroOrganoContratacion> getListFiltroOcsFromFiltroSql(String filtroSQL)
            throws MiRepositoryException {

        //
        return ejecutarDentroDeTransaccion(session -> {
            TypedQuery<FiltroOrganoContratacion> query = session.createQuery(filtroSQL, FiltroOrganoContratacion.class);
            return query.getResultList();
        }, "getListFiltroOcsFromFiltroSql");
    }
}
