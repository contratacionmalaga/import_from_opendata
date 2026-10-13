package local.jarios.database;

import jakarta.persistence.Table;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;

/** Prepara una reconstrucción explícita eliminando las FK antiguas entre tablas del modelo. */
@Slf4j
final class MariaDbSchemaReset {

  private MariaDbSchemaReset() {}

  static void prepare(Properties properties, Collection<Class<?>> entities) throws SQLException {
    String action = properties.getProperty("hibernate.hbm2ddl.auto", "none").trim();
    String url = properties.getProperty("jakarta.persistence.jdbc.url", "");
    if (!(action.equals("create") || action.equals("create-drop"))
        || !url.startsWith("jdbc:mariadb:")) {
      return;
    }
    // JPA database.action tiene precedencia sobre hbm2ddl.auto. No alterar nada si lo define.
    if (properties.containsKey("jakarta.persistence.schema-generation.database.action")) {
      throw new SQLException(
          "No combine hbm2ddl.auto=create/create-drop con schema-generation.database.action.");
    }
    Set<String> tables = new TreeSet<>();
    for (Class<?> entity : entities) {
      Table table = entity.getAnnotation(Table.class);
      if (table == null || table.name().isBlank()) {
        throw new SQLException("La reconstrucción requiere nombres de tabla explícitos.");
      }
      tables.add(table.name());
    }
    try (Connection connection =
        DriverManager.getConnection(
            url,
            properties.getProperty("jakarta.persistence.jdbc.user"),
            properties.getProperty("jakarta.persistence.jdbc.password"))) {
      removeMappedForeignKeys(connection, tables);
    }
  }

  static void removeMappedForeignKeys(Connection connection, Set<String> tables)
      throws SQLException {
    String catalog = connection.getCatalog();
    if (catalog == null || catalog.isBlank() || tables.isEmpty()) {
      throw new SQLException("No se ha identificado el esquema o las tablas a reconstruir.");
    }
    Set<ForeignKey> constraints = new LinkedHashSet<>();
    // Primero se inspecciona todo. Una referencia desde una tabla externa impide cualquier DDL.
    for (String table : new TreeSet<>(tables)) {
      try (var keys = connection.getMetaData().getExportedKeys(catalog, null, table)) {
        while (keys.next()) {
          String childCatalog = keys.getString("FKTABLE_CAT");
          String child = keys.getString("FKTABLE_NAME");
          if (!catalog.equals(childCatalog) || !tables.contains(child)) {
            throw new SQLException(
                "No se puede reconstruir: " + childCatalog + "." + child + " referencia " + table);
          }
          constraints.add(new ForeignKey(child, keys.getString("FK_NAME")));
        }
      }
    }
    for (ForeignKey constraint : constraints) {
      try (var statement = connection.createStatement()) {
        statement.executeUpdate(
            "ALTER TABLE "
                + quote(catalog)
                + "."
                + quote(constraint.table())
                + " DROP FOREIGN KEY "
                + quote(constraint.name()));
      }
    }
    log.info(
        "Reconstrucción de {}: retiradas {} FK internas con sus nombres reales; {} tablas del modelo.",
        catalog,
        constraints.size(),
        tables.size());
  }

  private static String quote(String name) throws SQLException {
    if (name == null || name.isBlank()) {
      throw new SQLException("Identificador SQL vacío en la reconstrucción del esquema.");
    }
    return "`" + name.replace("`", "``") + "`";
  }

  private record ForeignKey(String table, String name) {}
}
