# Importador PLACSP 8.2.0

Versión del 13/10/2026 para Málaga y Pliegos: UUID v7 en los 33 generadores de entidades,
conservación de UUID existentes al actualizar y mantenimiento de los identificadores externos.

Release y descargas: [8.2.0 en GitHub](https://github.com/contratacionmalaga/import_from_opendata/releases/tag/v8.2.0).
Los cuatro ejecutables se distribuyen junto a este README y `SHA256SUMS.txt`.

## Ejecutables

| JAR | Variante |
|---|---|
| `opendata_con_filtros-8.2.0-local.jar` | Málaga, local |
| `opendata_con_filtros-8.2.0-internet.jar` | Málaga, Internet |
| `opendata_sin_filtros-8.2.0-local.jar` | Pliegos, local |
| `opendata_sin_filtros-8.2.0-internet.jar` | Pliegos, Internet |

Requiere Java 21 y los properties operativos. El esquema existente debe validarse con
`hibernate.hbm2ddl.auto=validate`. La versión no requiere convertir UUID v4 anteriores.
Para construir una base limpia, usar `create` expresamente en la primera carga y volver a
`validate` en las siguientes. No ejecutar `create` sobre una base cuyos registros se quieran conservar.

## Pruebas reproducibles

Desde el directorio de desarrollo, con JDK 21 y Maven 3.9.16:

```powershell
# Batería completa con base H2 efímera.
.\scripts\use-java21-maven3916.ps1 verify -Pcon-filtros-local

# Pruebas reales en localhost:3306/opendata_malaga_testing.
# El fichero aporta usuario/contraseña; su URL operativa se sustituye por el destino fijo de pruebas.
.\scripts\use-java21-maven3916.ps1 test -Pcon-filtros-local `
  '-Dtest=UuidV7PersistenceIntegrationTest' '-Dopendata.uuid.mariadb=true' `
  '-Dopendata.uuid.properties=properties/bd.properties'
```

El test valida los 33 IDs y todas las FK del modelo: valor UUID v7, columna UUID nativa y
coincidencia exacta con el padre. Comprueba además la unicidad y los campos externos,
la repetición/actualización del grafo CODICE y la conservación de registros v4 existentes.
Los datos de prueba se revierten o limpian; MariaDB no utiliza `create-drop`.

Resultado del 13/10/2026: batería de 85 pruebas, cero fallos/errores y dos pruebas manuales
omitidas; tres pruebas MariaDB, cero fallos/errores. Confirmados 33 IDs y 42 FK UUID v7 nativos,
con sus PK/FK reales. Los cuatro JAR contienen los 33 generadores `VERSION_7` y los manifiestos
de sus variantes. Spotless pasó en las 36 clases modificadas; el control global detecta
formato previo pendiente en siete archivos ajenos a UUID v7.

## Compilación y despliegue

```powershell
# Genera las cuatro variantes en target/ sin activar una versión.
.\generar_ejecutables.ps1 -NoDeploy

# Archiva y activa las cuatro variantes ya generadas.
.\publicar_ejecutables.ps1 -Version 8.2.0

# Restauración de una versión previamente archivada.
.\publicar_ejecutables.ps1 -RollbackVersion <versión-anterior>
```

La instalación predeterminada es `C:\java\ejecutables\import-from-opendata-ejecutables`.
El publicador archiva los JAR, scripts, properties, estas instrucciones y las notas de versión
en `versions/8.2.0`, genera un manifiesto con SHA-256 y actualiza `active-release.properties`.
Los lanzadores utilizan la versión activa. Una restauración del ejecutable no cambia los UUID
ya guardados en la base de datos.

Las altas futuras son v7. Las FK hacia esas altas son el mismo v7. Las referencias hacia padres
v4 existentes conservan v4; convertirlas requiere una reconstrucción o remapeo posterior.
