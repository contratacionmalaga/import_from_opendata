# Validación UUID v7 — import-from-opendata 8.2.0

Fecha: 13/10/2026. Rama: `jarp/uuid-v7-8.2.0`.
Destino MariaDB autorizado: `localhost:3306/opendata_malaga_testing`.

## Resultado

| Comprobación | Resultado |
|---|---|
| Inventario de generadores | 33: Atom 3, auxiliares 7, CODICE 23 |
| IDs persistidos nuevos | 33 UUID v7, variante RFC 2, sin duplicados |
| FK del modelo | 42 UUID nativos, iguales a la PK de su padre, v7 para padres nuevos |
| Restricciones físicas | PK `id` y cada FK al `id` de la tabla padre verificados por JDBC |
| Tipos físicos | JDBC devuelve `UUID` en las 33 PK y las 42 FK |
| Campos del proveedor | Todos los String mapeados releídos sin transformación |
| Reimportación/actualización CODICE | Dos ejecuciones, UUID de las entidades existentes conservados |
| Colecciones | Reordenación y cambios de descripción conservan identidad; hijos nuevos v7; retirados eliminados |
| Registros v4 previos | Entry y ContractFolderStatus conservan sus UUID v4; hijos nuevos v7 y FK correctas |
| Batería completa | 85 pruebas, 0 fallos, 0 errores, 2 omitidas |
| Pruebas UUID en MariaDB | 3 pruebas, 0 fallos, 0 errores, 0 omitidas |
| Formato del cambio | Spotless: 36 clases verificadas |
| Ejecutables | 4 perfiles Maven compilados, 4 `BUILD SUCCESS` |
| Inspección de cada JAR | 33 `VERSION_7`, `App-Version=8.2.0`, `Main-Class` de su variante |

Las pruebas no importan feeds de Internet ni envían correo. Las filas del ensayo de 33 entidades
se revierten; los logs y sus grafos de las pruebas de importación se limpian por sus UUID al finalizar.
No se remapean claves previas ni se cambian las vistas CPV.

## Esquema de pruebas

La primera validación se detuvo por columnas de versiones anteriores ausentes en
`tendering_process`. Se guardó el esquema original en `target/uuid7-validation/schema-before.sql`.
La base contenía además columnas legacy de auditoría obligatorias ya no mapeadas, sin defaults.

Se aplicó exclusivamente a `opendata_malaga_testing` el script
[`uuid-v7-testing-schema.sql`](uuid-v7-testing-schema.sql): cinco columnas nullable pendientes en
`tendering_process` y defaults de auditoría legacy, conservando las columnas y los datos existentes.
Los defaults reproducen la compatibilidad ya usada en la auditoría de 27/09/2026. No se alteró
ninguna columna UUID ni se recrearon tablas. Este ajuste es un prerrequisito del esquema antiguo
de pruebas, no una migración de UUID requerida por 8.2.0.

## Evidencias locales

- `target/uuid7-mariadb-tests.log`: pruebas reales y resumen de los 33 IDs/42 FK.
- `target/uuid7-verify.log`: batería completa y empaquetado; el objetivo adicional de formato global falla por los siete archivos previos indicados abajo.
- `target/uuid7-format-check.log`: comprobación satisfactoria del formato de las 36 clases del cambio.
- `target/uuid7-build-all.log`: compilación de los cuatro perfiles.
- `target/uuid7-jar-verification.log`: inspección de los cuatro JAR y sus SHA-256.
- `target/uuid7-schema-inspection.log` y `target/uuid7-schema-preparation.log`: inspección y preparación del esquema de pruebas.

Los siete archivos con formato global previo pendiente son `AbstractOpenDataBase`,
`ImportErrorCategory`, `ImportFailureClassifier`, `FeedSource`, `AbstractOpenDataPliegos`,
`AbstractOpenDataBaseEmailConfigurationTest` e `ImportFailureClassifierTest`. No pertenecen
al cambio UUID v7; no se modificaron para corregir formato.

## Alcance de la garantía

Al importar desde cero con 8.2.0 sobre una base limpia, las 33 PK se generan en v7 y sus FK
contienen esos mismos UUID v7. Una FK a un padre v4 existente permanece v4 para conservar
su identidad. Los identificadores externos conservan la representación y valor del proveedor.
La etiqueta de incidente mantiene su llamada aleatoria de ocho caracteres.

## Despliegue verificado

Activado el 13/10/2026 a las 22:46 (Europe/Madrid) en
`C:\java\ejecutables\import-from-opendata-ejecutables`.
`active-release.properties` indica `version=8.2.0` y los properties activos usan
`hibernate.hbm2ddl.auto=validate`. La plantilla también cambia su valor predeterminado a `validate`.

Las cuatro copias operativas y las cuatro archivadas en `versions/8.2.0` coinciden por SHA-256
con los JAR inspeccionados. Se validaron los 22 ficheros del manifiesto del archivo de versión,
incluidos README, notas, scripts y properties. La copia anterior de properties está en
`backups/properties/20261013_224609`. No se ejecutó ninguna importación operativa ni se modificaron
los esquemas de Málaga o Pliegos durante el despliegue.

El manifiesto del primer despliegue registró la rama y `gitWorkingTreeDirty=true`: en ese momento
los cambios todavía estaban sin commit y no se había creado una release remota.
La comprobación final de la base de pruebas encontró cero Entries temporales; su esquema final
se guardó en `target/uuid7-validation/schema-after.sql`.

## Publicación en GitHub

La publicación posterior usa la rama `jarp/uuid-v7-8.2.0` y la etiqueta `v8.2.0`, con los cuatro
JAR ya probados, el README y `SHA256SUMS.txt`. Antes del commit se contrastaron los 179 archivos
Java del JAR de fuentes con el directorio de trabajo: coinciden tras normalizar los saltos de línea.
La versión incluye la corrección incremental 8.1.1 que ya estaba pendiente y formaba parte de
los ejecutables probados. Los properties reales con credenciales no se incorporan a Git ni a
los archivos de la release.

[Release 8.2.0](https://github.com/contratacionmalaga/import_from_opendata/releases/tag/v8.2.0).
