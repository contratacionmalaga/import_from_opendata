package local.jarios.repositories;

import jakarta.persistence.TypedQuery;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.Log;
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.entity.auxiliares.Provincia;
import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.entity.placsp.PreliminaryMarketConsultationStatus;
import local.jarios.exceptions.MiRepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Implementación del repositorio que gestiona la persistencia y recuperación de datos mediante
 * Hibernate {@link SessionFactory}.
 * <p>
 * Proporciona métodos para persistir logs, obtener entradas y feeds, y ejecutar consultas
 * específicas para filtros.
 * </p>
 * <p>
 * Utiliza transacciones para garantizar la integridad y gestión adecuada de las operaciones sobre
 * la base de datos.
 * </p>
 * <p>
 * Implementa {@link AutoCloseable} para liberar recursos cerrando la {@link SessionFactory} cuando
 * sea necesario.
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
  public void persistirEnBaseDatos(Log miLog) throws MiRepositoryException {
    ejecutarDentroDeTransaccion(session -> {
      log.debug("[persistirEnBaseDatos] - Persistiendo Log.");
      session.merge(miLog);
      flushAndClear(session);
      log.debug("[persistirEnBaseDatos] - Persistencia de Log completada.");
      return null;
    }, "persistirEnBaseDatos");
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
   * Ejecuta una función dentro de una transacción Hibernate, gestionando commit, rollback y manejo
   * de excepciones.
   *
   * @param function función que recibe la sesión y retorna un resultado.
   * @param metodo   nombre del método para logs y excepciones.
   * @param <R>      tipo del resultado esperado.
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
   * Maneja el error ocurrido en una transacción, realiza rollback si es posible, y registra el
   * error en logs.
   *
   * @param metodo      nombre del método donde ocurrió el error.
   * @param transaction transacción actual.
   * @param ex          excepción ocurrida.
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
   * Obtiene un mapa de entradas ({@link Entry}) a partir de una consulta HQL. La clave del mapa es
   * el ID de la entrada.
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
   * Ejecuta una consulta nativa SQL para obtener una lista de {@link OrganoContratacion} de acuerdo
   * al filtro indicado.
   *
   * @param sql consulta SQL nativa que define el filtro.
   * @return lista de objetos {@link OrganoContratacion}.
   * @throws MiRepositoryException si ocurre error en la consulta.
   */
  @Override
  public List<OrganoContratacion> getListOrganosContratacionFromSql(String sql)
      throws MiRepositoryException {
    // Obtengo la list dentro de un transacción

    return ejecutarDentroDeTransaccion(
session ->
        session.createNativeQuery(sql, OrganoContratacion.class).getResultList(),
"getListFiltroOcsFromSql");
  }

  /**
   * Ejecuta una consulta nativa SQL para obtener una lista de {@link OrganoContratacion} de acuerdo
   * al filtro indicado.
   *
   * @param sql consulta SQL nativa que define el filtro.
   * @return lista de objetos {@link Provincia}.
   * @throws MiRepositoryException si ocurre error en la consulta.
   */
  @Override
  public List<Provincia> getListProvinciasFromSql(String sql)
      throws MiRepositoryException {
    // Obtengo la list dentro de un transacción

    return ejecutarDentroDeTransaccion(
        session ->
            session.createNativeQuery(sql, Provincia.class).getResultList(),
        "getListProvinciasFromSql");
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

  /**
   * Función encargada de persistir en la base de datos la importación utilizando bloques.
   *
   * @param miLog Instancia de {@link Log} a persistir.
   */
  public void persistirLogEnBloques(Log miLog) {

    ejecutarDentroDeTransaccion(session -> {

      // Apunto el inicio
      long start = System.currentTimeMillis();

      // Obtengo las propiedades del objeto SessionFactory
      var map = sessionFactory.getProperties();

      for (Map.Entry<String, Object> entry : map.entrySet()) {
        log.debug("[persistirLogEnBloques] - {}", entry.getKey() + " = " + entry.getValue());
      }

      String batchSizeStr = (String) map.get("hibernate.jdbc.batch_size");
      int batchSize = Integer.parseInt(batchSizeStr);
      log.debug(String.valueOf(batchSize));

      int contador = 0;

      session.persist(miLog);
      session.flush();
      log.info("Persistido el regsitro Log");

      if (miLog.getConfiguracion() != null) {
        miLog.getConfiguracion().setMiLog(miLog);
        session.persist(miLog.getConfiguracion());
        log.info("Persistido el registro Configuracion");
      }

      if (miLog.getEstadistica() != null) {
        miLog.getEstadistica().setMiLog(miLog);
        session.persist(miLog.getEstadistica());
        log.info("Persistido el registro Estadistica");
      }

      for (OrganoContratacion oc : miLog.getListOrganoContratacion()) {
        oc.setMiLog(miLog);
        session.persist(oc);
      }
      session.flush();
      session.clear();
      log.info("Persistidos {} registros del tipo OrganoContratacion", miLog.getListOrganoContratacion().size());

      for (Historico historico : miLog.getListHistorio()) {
        historico.setMiLog(miLog);
        session.persist(historico);
      }
      session.flush();
      session.clear();
      log.info("Persistidos {} registros del tipo Historico", miLog.getListOrganoContratacion().size());

      // Obtengo el número de registros desde la base de datos en memoria
      int n_registros = VariablesGlobales.getMapEntriesFromAtoms().size();

      // Recorro cada feed dentro de la lista de Feeds asociada al Log.
      for (Feed feed : miLog.getListFeed()) {
        feed.setMiLog(miLog);
        session.persist(feed);
        log.info("Persistido Feed - {}", feed.getLinkSelf());

        for (DeletedEntry deletedEntry : feed.getListDeletedEntry()) {
          deletedEntry.setFeed(feed);
          session.persist(deletedEntry);
        }
        session.flush();
        session.clear();
        log.info("Persistidos {} registros del tipo DeletedEntry", feed.getListDeletedEntry().size());

        // Recorro cada entry dentro de la lista de Entrys asociada al Feed.
        for (Entry entry : feed.getListEntry()) {
          entry.setFeed(feed);
          session.persist(entry);

          // Recorro los ContractFolderStatus
          for (ContractFolderStatus contractFolderStatus : entry.getListContractFolderStatus()) {
            contractFolderStatus.setEntry(entry);
            session.persist(contractFolderStatus);
          }

          // Recorro los PreliminaryMarketConsultationStatus
          for (PreliminaryMarketConsultationStatus pmcs : entry.getListPreliminaryMarketConsultationStatus()) {
            pmcs.setEntry(entry);
            session.persist(pmcs);
          }

          if (++contador % batchSize == 0) {
            session.flush();
            session.clear();
          }

          log.info("Persistido Entry - {} - {}", contador + "/" + n_registros, entry.getIdEntry());

        }
      }

      // Flush final
      flushAndClear(session);
      session.merge(miLog);
      flushAndClear(session);

      long end = System.currentTimeMillis();
      log.debug("[persistirLogEnBloques] - Persistencia de Log completada correctamente.");
      log.info("Tiempo de ejecución en base de datos: {}", (end - start) + "ms");

      return null; // el método Function<Session, R> espera un retorno
    }, "persistirLogEnBloques");
  }
}
