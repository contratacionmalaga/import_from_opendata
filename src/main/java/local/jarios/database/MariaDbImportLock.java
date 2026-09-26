package local.jarios.database;

import java.util.Objects;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/** Bloqueo distribuido de MariaDB, asociado a una conexión dedicada durante una importación. */
public final class MariaDbImportLock implements AutoCloseable {

  private final String lockName;
  private final Session session;

  private MariaDbImportLock(String lockName, Session session) {
    this.lockName = lockName;
    this.session = session;
  }

  public static MariaDbImportLock acquire(SessionFactory sessionFactory, String lockName) {
    Objects.requireNonNull(sessionFactory, "sessionFactory no puede ser null");
    Objects.requireNonNull(lockName, "lockName no puede ser null");

    Session session = sessionFactory.openSession();
    try {
      Number result =
          (Number)
              session
                  .createNativeQuery("SELECT GET_LOCK(:lockName, 0)")
                  .setParameter("lockName", lockName)
                  .getSingleResult();
      if (result == null || result.intValue() != 1) {
        throw new IllegalStateException("Ya existe una importacion en curso para " + lockName);
      }
      return new MariaDbImportLock(lockName, session);
    } catch (RuntimeException ex) {
      session.close();
      throw ex;
    }
  }

  @Override
  public void close() {
    try {
      if (session.isOpen()) {
        session
            .createNativeQuery("SELECT RELEASE_LOCK(:lockName)")
            .setParameter("lockName", lockName)
            .getSingleResult();
      }
    } finally {
      if (session.isOpen()) {
        session.close();
      }
    }
  }
}
