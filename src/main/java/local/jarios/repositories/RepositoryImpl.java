package local.jarios.repositories;

import jakarta.persistence.TypedQuery;
import local.jarios.database.SessionFactoryProvider;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoConexion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.exceptions.MiServiceException;
import local.jarios.exceptions.MiTransactionManagerException;
import local.jarios.models.FiltroOrganoContratacion;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class RepositoryImpl implements Repository {

    /**
     * Fabrica de sesiones Hibernate usada para obtener sesiones.
     */
    private final SessionFactory sessionFactory;

    /**
     * Constructor que inicializa la {@link SessionFactory}.
     *
     * @throws MiServiceException si falla la creación de la SessionFactory.
     */
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

    /**
     * Persiste el log y las listas relacionadas (Oc, OcHistorico, Cnae, Dir3) dentro
     * de una transacción, borrando previamente los datos antiguos de Cnae y Dir3.
     *
     * @param miLog Objeto Log que contiene la información a persistir.
     * @throws MiRepositoryException si ocurre un error durante la operación.
     */
    @Override
    public void persistirLog(Log miLog, Map<String, Entry> mapBaseDatos, LugarImportacion lugarImportacion) throws MiRepositoryException {

        ejecutarDentroDeTransaccion(

            session -> {

                session.persist(miLog);
                log.debug("[persistirLog] - Persistidas las Entidades Log, Estadística y Configuracion. {}", miLog.toString());

                session.flush();
                log.debug("[persistirLog] - Flush de la sesión en base de datos");

                grabarMap(session, mapBaseDatos, lugarImportacion);
                log.debug("[persistirLog] - Map de Entrys persistido correctamente. Registros: {}", mapBaseDatos.size());

            }, "persistirLog");
    }

    private void grabarMap(Session session, Map<String, Entry> mapBaseDatos, LugarImportacion lugarImportacion) {

        boolean esLocal = lugarImportacion.isLocalImport();

        for (Entry newEntry : mapBaseDatos.values()) {

            if (esLocal) {
                // En importación local grabamos todo directamente sin comprobar
                session.persist(newEntry);

            } else {
                // Importación Internet: chequeamos existencia y fecha updated

                // Buscar por idEntry (asumo que clave idEntry es el identificador único de negocio)
                Entry existingEntry = session.createQuery(
                                "FROM Entry e WHERE e.idEntry = :idEntry", Entry.class)
                        .setParameter("idEntry", newEntry.getIdEntry())
                        .uniqueResult();

                if (existingEntry == null) {
                    // No existe, persisto nuevo
                    session.persist(newEntry);

                } else {

                    // Existe, comparar updated
                    LocalDateTime existingUpdated = existingEntry.getUpdated();
                    LocalDateTime newUpdated = newEntry.getUpdated();

                    if (newUpdated != null && (existingUpdated == null || newUpdated.isAfter(existingUpdated))) {
                        // Nuevo es más reciente

                        // Primero eliminar el antiguo
                        session.remove(existingEntry);

                        // Persistir el nuevo
                        session.persist(newEntry);

                    }
                }
            }
        }

        session.flush();
        session.clear();

    }

    /**
     * Ejecuta una función consumidora dentro de una transacción Hibernate.
     * <p>
     * Maneja apertura de sesión, inicio y fin de transacción, con manejo de rollback
     * en caso de errores y logging adecuado.
     * </p>
     *
     * @param function Función que acepta la sesión para realizar operaciones.
     * @param metodo Nombre del método o contexto para logging.
     * @throws MiRepositoryException en caso de error durante la transacción.
     */
    private <R> R ejecutarDentroDeTransaccion(Function<Session, R> function, String metodo) throws MiRepositoryException {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = TransactionManager.beginTransaction(session);
            R result = function.apply(session);
            TransactionManager.commitTransaction(transaction);
            return result;
        } catch (Exception ex) {
            log.error("[ejecutarDentroDeTransaccion] - Método: {}, Error en transacción: {}", metodo, ex.getMessage());

            if ((transaction != null) && transaction.getStatus().canRollback()) {
                try {
                    TransactionManager.rollbackTransaction(transaction);
                } catch (MiTransactionManagerException rollbackEx) {
                    log.error("[ejecutarDentroDeTransaccion] - Error durante rollback de la transacción.");
                }
            }
            throw new MiRepositoryException("[ejecutarDentroDeTransaccion] - Error al realizar commit de la transacción.", ex);
        }
    }

    private void ejecutarDentroDeTransaccion(SessionConsumer consumer, String metodo) throws MiRepositoryException {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = TransactionManager.beginTransaction(session);
            consumer.accept(session);
            TransactionManager.commitTransaction(transaction);

        } catch (Exception ex) {
            log.error("[ejecutarDentroDeTransaccion] - Método: {}, Error en transacción: {}", metodo, ex.getMessage());

            if ((transaction != null) && transaction.getStatus().canRollback()) {
                try {
                    TransactionManager.rollbackTransaction(transaction);
                } catch (MiTransactionManagerException rollbackEx) {
                    log.error("[ejecutarDentroDeTransaccion] - Error durante rollback de la transacción.");
                }
            }

            throw new MiRepositoryException("[ejecutarDentroDeTransaccion] - Error al realizar commit de la transacción.", ex);
        }
    }

    /**
     * Interfaz funcional para consumir una sesión Hibernate.
     */
    @FunctionalInterface
    private interface SessionConsumer {
        /**
         * Operación a realizar sobre la sesión Hibernate.
         *
         * @param session Sesión Hibernate que será usada.
         * @throws HibernateException si ocurre un error en la operación.
         */
        void accept(Session session) throws HibernateException;
    }

    private void persistir(
        Session session,
        Entry entry,
        LugarImportacion lugarImportacion
    ) throws HibernateException {

        //
        if (lugarImportacion == LugarImportacion.LOCAL) {
            session.persist(entry);
        } else {
            persistirInternet(session, entry);
        }
    }

    private static void persistirInternet(
        Session session,
        Entry entry
    ) throws HibernateException {

        //
        String hql = "FROM Entry e WHERE e.idEntry = :idEntry";
        Entry existingEntry = session.createQuery(hql, Entry.class)
                .setParameter("idEntry", entry.getIdEntry())
                .uniqueResult();

        //
        if (existingEntry == null) {
            session.persist(entry);
            return;
        }

        //
        if (existingEntry.getUpdated().before(entry.getUpdated())) {
            session.remove(existingEntry);
            session.flush();
            session.persist(entry);
        }
    }

    @Override
    public Feed getNewestFeed(String sql) throws MiRepositoryException {

        return ejecutarDentroDeTransaccion(
                session -> {
                    TypedQuery<Feed> query = session.createQuery(sql, Feed.class);
                    return query.getResultList().stream().findFirst().orElse(null);
                },
                "getNewestFeed"
        );
    }

    @Override
    public List<FiltroOrganoContratacion> getListFiltroOcsFromFiltroSql(String filtroSQL) throws MiRepositoryException {

        return ejecutarDentroDeTransaccion(
                session -> session.createNativeQuery(filtroSQL, FiltroOrganoContratacion.class).list(),
                "getListFiltroOcsFromFiltroSql"
        );
    }
}

