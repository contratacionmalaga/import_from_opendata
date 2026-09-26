package local.jarios.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Prueba manual contra opendata_prueba; nunca se ejecuta en la suite ordinaria. */
@EnabledIfSystemProperty(named = "opendata.mariadb.lock.integration", matches = "true")
class MariaDbImportLockIntegrationTest {

  private static final String TEST_DATABASE = "opendata_prueba";
  private static final String LOCK_NAME = "opendata:LOCK_INTEGRATION_TEST";

  @Test
  void prevents_a_second_import_and_releases_the_lock_after_closing_the_first_connection()
      throws Exception {
    Properties properties = loadConnectionProperties();
    String url = databaseUrl(properties.getProperty("jakarta.persistence.jdbc.url"));
    String user =
        System.getProperty(
            "opendata.mariadb.lock.user", properties.getProperty("jakarta.persistence.jdbc.user"));
    String password =
        System.getProperty(
            "opendata.mariadb.lock.password",
            properties.getProperty("jakarta.persistence.jdbc.password"));

    try (Connection first = DriverManager.getConnection(url, user, password);
        Connection second = DriverManager.getConnection(url, user, password)) {
      assertThat(getLock(first)).isEqualTo(1);
      assertThat(getLock(second)).isZero();
      assertThat(releaseLock(first)).isEqualTo(1);
      assertThat(getLock(second)).isEqualTo(1);
      assertThat(releaseLock(second)).isEqualTo(1);
    }
  }

  private static Properties loadConnectionProperties() throws IOException {
    Properties properties = new Properties();
    try (InputStream input = Files.newInputStream(Path.of("properties", "bd.properties"))) {
      properties.load(input);
    }
    return properties;
  }

  private static String databaseUrl(String configuredUrl) {
    if (configuredUrl == null) {
      throw new IllegalStateException("No existe jakarta.persistence.jdbc.url");
    }
    return configuredUrl.replaceFirst("(?<=//[^/]+/)[^?]+", TEST_DATABASE);
  }

  private static int getLock(Connection connection) throws SQLException {
    try (var statement = connection.prepareStatement("SELECT GET_LOCK(?, 0)")) {
      statement.setString(1, LOCK_NAME);
      try (var result = statement.executeQuery()) {
        result.next();
        return result.getInt(1);
      }
    }
  }

  private static int releaseLock(Connection connection) throws SQLException {
    try (var statement = connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
      statement.setString(1, LOCK_NAME);
      try (var result = statement.executeQuery()) {
        result.next();
        return result.getInt(1);
      }
    }
  }
}
