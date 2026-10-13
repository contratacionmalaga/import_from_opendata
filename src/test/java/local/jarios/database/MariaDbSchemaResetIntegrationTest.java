package local.jarios.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Regresión de FK heredadas en tablas desechables de la base aislada. */
@EnabledIfSystemProperty(named = "opendata.schema.mariadb", matches = "true")
class MariaDbSchemaResetIntegrationTest {

  @Test
  void legacy_names_can_be_removed_and_tables_recreated_without_touching_external_objects()
      throws Exception {
    try (Connection connection = connect()) {
      cleanup(connection);
      try {
        execute(connection, "CREATE TABLE reset_parent (id UUID PRIMARY KEY)");
        execute(
            connection,
            "CREATE TABLE reset_child (id UUID PRIMARY KEY, parent_id UUID,"
                + " CONSTRAINT legacy_fk_reset FOREIGN KEY (parent_id) REFERENCES reset_parent(id))");
        execute(connection, "CREATE TABLE reset_external (id INT PRIMARY KEY)");
        execute(connection, "INSERT INTO reset_external VALUES (17)");
        execute(connection, "CREATE VIEW reset_view AS SELECT id FROM reset_parent");
        execute(connection, "INSERT INTO reset_parent VALUES (UUID())");
        execute(connection, "INSERT INTO reset_child SELECT UUID(), id FROM reset_parent");
        MariaDbSchemaReset.removeMappedForeignKeys(
            connection, Set.of("reset_parent", "reset_child"));
        // Orden de Hibernate: primero el padre. Con la FK heredada fallaba.
        execute(connection, "DROP TABLE reset_parent");
        execute(connection, "DROP TABLE reset_child");
        execute(connection, "CREATE TABLE reset_parent (id UUID PRIMARY KEY)");
        execute(
            connection,
            "CREATE TABLE reset_child (id UUID PRIMARY KEY, parent_id UUID,"
                + " CONSTRAINT new_fk_reset FOREIGN KEY (parent_id) REFERENCES reset_parent(id))");
        assertThat(count(connection, "SELECT COUNT(*) FROM reset_parent")).isZero();
        assertThat(count(connection, "SELECT COUNT(*) FROM reset_child")).isZero();
        assertThat(count(connection, "SELECT COUNT(*) FROM reset_view")).isZero();
        assertThat(count(connection, "SELECT id FROM reset_external")).isEqualTo(17);
        assertThatThrownBy(
                () -> execute(connection, "INSERT INTO reset_child VALUES (UUID(), UUID())"))
            .isInstanceOf(SQLException.class);
        // La segunda reconstrucción también funciona con los nombres de la nueva versión.
        MariaDbSchemaReset.removeMappedForeignKeys(
            connection, Set.of("reset_parent", "reset_child"));
        execute(connection, "DROP TABLE reset_parent");
        execute(connection, "DROP TABLE reset_child");
      } finally {
        cleanup(connection);
      }
    }
  }

  @Test
  void external_references_block_reset_before_any_constraint_is_removed() throws Exception {
    try (Connection connection = connect()) {
      cleanup(connection);
      try {
        execute(connection, "CREATE TABLE reset_parent (id UUID PRIMARY KEY)");
        execute(
            connection,
            "CREATE TABLE reset_child (id UUID PRIMARY KEY, parent_id UUID,"
                + " CONSTRAINT legacy_fk_reset FOREIGN KEY (parent_id) REFERENCES reset_parent(id))");
        execute(
            connection,
            "CREATE TABLE reset_external (id INT PRIMARY KEY, parent_id UUID,"
                + " CONSTRAINT external_fk_reset FOREIGN KEY (parent_id) REFERENCES reset_parent(id))");
        assertThatThrownBy(
                () ->
                    MariaDbSchemaReset.removeMappedForeignKeys(
                        connection, Set.of("reset_parent", "reset_child")))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("reset_external");
        assertThat(
                count(
                    connection,
                    "SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS"
                        + " WHERE CONSTRAINT_SCHEMA=DATABASE()"
                        + " AND CONSTRAINT_NAME IN ('legacy_fk_reset','external_fk_reset')"))
            .isEqualTo(2);
      } finally {
        cleanup(connection);
      }
    }
  }

  private static Connection connect() throws Exception {
    Properties properties = new Properties();
    try (var input = Files.newInputStream(Path.of("properties/bd.properties"))) {
      properties.load(input);
    }
    return DriverManager.getConnection(
        "jdbc:mariadb://localhost:3306/opendata_malaga_testing",
        properties.getProperty("jakarta.persistence.jdbc.user"),
        properties.getProperty("jakarta.persistence.jdbc.password"));
  }

  private static void cleanup(Connection connection) throws SQLException {
    execute(connection, "DROP VIEW IF EXISTS reset_view");
    execute(connection, "DROP TABLE IF EXISTS reset_external");
    execute(connection, "DROP TABLE IF EXISTS reset_child");
    execute(connection, "DROP TABLE IF EXISTS reset_parent");
  }

  private static void execute(Connection connection, String sql) throws SQLException {
    try (var statement = connection.createStatement()) {
      statement.executeUpdate(sql);
    }
  }

  private static long count(Connection connection, String sql) throws SQLException {
    try (var statement = connection.createStatement(); var result = statement.executeQuery(sql)) {
      assertThat(result.next()).isTrue();
      return result.getLong(1);
    }
  }
}
