# Migraciones MariaDB de la 8.3.0

Estas migraciones actualizan el esquema existente de la 8.2.2. Aplíquelas en `opendata_malaga` y `opendata_pliegos`, ya que los cuatro JAR comparten el mismo modelo. Use el servidor y puerto de cada instalación y ejecute con el importador detenido y una copia de seguridad. Para un esquema nuevo, Hibernate puede crear el modelo de la 8.3.0; después use `hibernate.hbm2ddl.auto=validate`.

## Orden de aplicación

| Paso | Fichero | Cambio |
|---|---|---|
| 1 | [01_historico_atom.sql](01_historico_atom.sql) | Añade `xml` a `entry` y `historico_entry`, y los cuatro campos del `summary` al histórico. |
| 2 | [02_identidad_atom.sql](02_identidad_atom.sql) | Amplía los ID y referencias a 2.500 caracteres, calcula las claves SHA-256 de los valores existentes, ajusta los índices y añade los desplazamientos horarios de entradas y bajas. |
| 3 | [03_importes_proyecto.sql](03_importes_proyecto.sql) | Convierte los importes de proyectos y lotes de `DOUBLE` a `DECIMAL(65,30)` y añade una columna de moneda por importe. |
| 4 | [04_datos_consultas.sql](04_datos_consultas.sql) | Añade la jerarquía, la dirección postal, los idiomas y el enlace de presentación al estado de consulta. |

Desde la raíz del repositorio, seleccione una base por ejecución. El cliente solicita la contraseña en cada paso.

```powershell
$servidor = 'localhost'
$puerto = 3306
$usuario = 'USUARIO'
$base = 'prueba-opendata' # Para actualizar producción, seleccionar su esquema explícitamente.
$migraciones = @(
  '01_historico_atom.sql',
  '02_identidad_atom.sql',
  '03_importes_proyecto.sql',
  '04_datos_consultas.sql'
)
foreach ($fichero in $migraciones) {
  mariadb --host=$servidor --port=$puerto --user=$usuario --password --database=$base `
    --abort-source-on-error --execute="source docs/migrations/v8.3.0/$fichero"
  if ($LASTEXITCODE -ne 0) { throw "Error aplicando $fichero en $base" }
}
```

En una instalación archivada por `publicar_ejecutables.ps1`, los cuatro SQL están en `versions/8.3.0/migrations`. Al finalizar, arranque la aplicación con `validate` para comprobar el modelo. El paso de vistas de la 8.2.2 sigue siendo independiente. [Vistas de la 8.2.2](https://github.com/contratacionmalaga/import_from_opendata/blob/v8.2.2/docs/migrations/v8.2.2/README.md)

## Datos existentes y vuelta a la versión anterior

- Se conservan las filas, sus UUID y las referencias. La clave SHA-256 se calcula sobre el ID que esté almacenado. El ID Atom completo y el desplazamiento exacto de versiones antiguas se recuperan del ATOM original. En la comparación de fechas históricas sin desplazamiento se usa `Europe/Madrid`.
- El XML, los campos nuevos del histórico, la moneda y los datos adicionales de consulta se completan al procesar los ATOM correspondientes. Repetir una carga con la misma fecha de `updated` conserva la fila vigente. Los valores `DOUBLE` ya almacenados se convierten a decimal con la precisión disponible en la base.
- La DDL de MariaDB confirma cambios por sentencia. Para volver al esquema anterior se restaura la copia de seguridad junto con la versión anterior de la aplicación.
