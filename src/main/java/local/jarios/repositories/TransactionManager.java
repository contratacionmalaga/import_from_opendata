package local.jarios.repositories;

import local.jarios.exceptions.MiTransactionManagerException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Clase utilitaria para la gestión de transacciones Hibernate.
 * <p>
 * Proporciona métodos estáticos para iniciar, confirmar y revertir transacciones
 * en una sesión de Hibernate, con logging de las acciones realizadas.
 * </p>
 * <p>
 * Esta clase es final y su constructor privado evita la instanciación.
 * </p>
 *
 * @author juan
 * @since 28/12/2024
 */
@Slf4j
public final class TransactionManager {

    /**
     * Constructor privado para evitar instanciación de la clase utilitaria.
     */
    private TransactionManager() {
        // Evita instancias
    }

    /**
     * Inicia una nueva transacción en la sesión Hibernate proporcionada.
     *
     * @param session Sesión Hibernate donde se iniciará la transacción.
     * @return La transacción iniciada.
     */
    public static Transaction beginTransaction(Session session) {
        Transaction transaction = session.beginTransaction();
        log.debug("[beginTransaction] - Inicio de transacción.");
        return transaction;
    }

    /**
     * Realiza commit de la transacción si esta no está marcada para rollback.
     *
     * @param transaction Transacción a confirmar.
     */
    public static void commitTransaction(Transaction transaction) {
        if ((transaction != null) && !transaction.getRollbackOnly()) {
            transaction.commit();
            log.debug("[commitTransaction] - Commit de la transacción.");
        }
    }

    /**
     * Realiza rollback de la transacción indicada.
     *
     * @param transaction Transacción a revertir.
     * @throws MiTransactionManagerException Si ocurre un error durante el rollback.
     */
    public static void rollbackTransaction(Transaction transaction) throws MiTransactionManagerException {
        if (transaction != null) {
            try {

                transaction.rollback();
                log.warn("[rollbackTransaction] - Rollback ejecutado correctamente.");

            } catch (Exception ex) {

                String msg = String.format("[rollbackTransaction] - Error haciendo rollback: %s", ex.getMessage());
                log.error(msg, ex);
                throw new MiTransactionManagerException(msg, ex);

            }
        }
    }
}
