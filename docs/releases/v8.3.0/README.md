# Importador PLACSP 8.3.0

Esta versión incorpora el histórico XML de Málaga, mejoras en la comparación incremental y un mapeo más completo de contratos, lotes y consultas. [Descargas en GitHub](https://github.com/contratacionmalaga/import_from_opendata/releases/tag/v8.3.0)

## Cambios de la versión

- `entry.xml` guarda la versión vigente. En Málaga, `historico_entry` guarda el XML de cada observación dentro del ámbito postal/NIF, aunque el `updated` sea anterior, igual o esté fuera del rango configurado. El resumen se desglosa en expediente, órgano, importe y estado. Se conservan los puntos y coma internos, como el de `C.28;G489/2026`.
- El incremental compara por ID Atom completo y por el instante de `updated` con su desplazamiento horario. Sigue los enlaces `next` sin interrumpir la lectura por la fecha de otro expediente. Conserva las identidades existentes y genera UUID v7 en las altas.
- Se conservan títulos de hasta 2.000 caracteres. `OTROS` se guarda por separado y el filtro postal usa el NIF explícito del órgano. Los anuncios de las consultas se guardan aunque el ATOM carezca de documentos generales. Cuando se repite un tipo de anuncio, su medio y fecha de publicación permiten emparejarlo aunque cambie el orden.
- Los proyectos y lotes guardan importes como `DECIMAL(65,30)` y conservan el `currencyID` de cada importe. Si el ATOM omite la moneda, queda en `NULL`.
- Las consultas guardan la jerarquía del órgano desde el padre inmediato hasta la raíz, las líneas de dirección, localidad, código postal, país, los idiomas en el orden del ATOM y el enlace de `Attachment/ExternalReference/URI`.

## Preparación del esquema

Detenga las importaciones activas y aplique las migraciones en cada esquema existente de Málaga y Pliegos. El orden es histórico XML, identidad Atom, importes y consultas. Después inicie cada variante con `hibernate.hbm2ddl.auto=validate`. [SQL, comandos y recuperación](https://github.com/contratacionmalaga/import_from_opendata/blob/v8.3.0/docs/migrations/v8.3.0/README.md)

## Ejecutables y configuración

Requiere Java 21 y acceso a MariaDB. Las importaciones con filtros también necesitan acceso al fichero de órganos de contratación de la PLACSP para resolver los prefijos postales.

| JAR | Destino del lanzador Windows | Tipos |
|---|---|---|
| `opendata_con_filtros-8.3.0-local.jar` | `opendata_malaga` | CONSULTAS, ENCARGOS, MAYORES, MENORES |
| `opendata_con_filtros-8.3.0-internet.jar` | `opendata_malaga` | CONSULTAS, ENCARGOS, MAYORES, MENORES |
| `opendata_sin_filtros-8.3.0-local.jar` | `opendata_pliegos` | MAYORES |
| `opendata_sin_filtros-8.3.0-internet.jar` | `opendata_pliegos` | MAYORES |

Mantenga los properties propios de la instalación. Para una instalación nueva, copie las seis plantillas `app`, `bd`, `filter`, `hibernate`, `mail` y `runtime` desde el código fuente de la etiqueta. El lanzador Windows toma el servidor, puerto y credenciales de `bd.properties` y selecciona la base por grupo, incluso con `-BaseDir`. La ejecución directa de un JAR y el lanzador Bash usan la URL JDBC configurada.

Para Málaga por código postal, configure `filter.properties`.

```properties
filter.codigosPostales=29
filter.nifs=
```

Los límites `filter.fechaInicialLectura` y `filter.fechaFinalLectura` usan `yyyy-MM-dd`. El primero fija el extremo moderno a las 00:00 y el segundo el extremo antiguo al final del día indicado. Si se omiten, se usan el inicio de hoy y el final del 01/01/2018, respectivamente. Con `app.persistir_historicos_rechazados=false` los rechazos del ámbito CP/NIF siguen guardándose con su XML. Use `app.email.enabled=false` en pruebas. La memoria del lanzador se controla mediante `java.opts` de `runtime.properties`.

Para usar `D:\placsp` como origen, configure `app.properties`.

```properties
app.local.path=D:\\placsp\\
app.local.cpms=CPM_SectorPublico.atom
app.local.emps=EMP_SectorPublico.atom
app.local.mayores=licitacionesPerfilesContratanteCompleto3.atom
app.local.menores=contratosMenoresPerfilesContratantes.atom
```

El importador busca esos ficheros en los subdirectorios `cpm`, `emp`, `may` y `men`, respectivamente. Los orígenes remotos se configuran con `app.internet.*` y los reintentos con `app.http.*`.

## Comandos de importación en Windows

Desde la instalación operativa, que por defecto está en `C:\java\ejecutables\import-from-opendata-ejecutables`, muestre primero el plan con `-DryRun`. Cuando se omite `-TipoSindicacion`, se ejecutan todos los tipos admitidos por el grupo.

```powershell
# Málaga desde disco. Ejecuta CONSULTAS, ENCARGOS, MAYORES y MENORES.
.\importar_atoms.ps1 -Grupo con_filtros -Mode local -DryRun
.\importar_atoms.ps1 -Grupo con_filtros -Mode local

# Málaga desde disco, tipos seleccionados.
.\importar_atoms.ps1 -Grupo con_filtros -Mode local -TipoSindicacion MAYORES,MENORES

# Málaga incremental desde Internet.
.\importar_atoms.ps1 -Grupo con_filtros -Mode internet
.\importar_atoms.ps1 -Grupo con_filtros -Mode internet -TipoSindicacion CONSULTAS

# Pliegos, MAYORES sin filtros.
.\importar_atoms.ps1 -Grupo sin_filtros -Mode local -TipoSindicacion MAYORES
.\importar_atoms.ps1 -Grupo sin_filtros -Mode internet -TipoSindicacion MAYORES

# Ambos grupos. Comprobar destinos antes de ejecutar.
.\importar_atoms.ps1 -Grupo all -Mode internet -DryRun
.\importar_atoms.ps1 -Grupo all -Mode internet
```

El lanzador pide escribir `I` para confirmar. En tareas programadas ya configuradas, añada `-SkipConfirmation`. `-ContinueOnError` permite que continúen otros bloques y conserva el código de error global. `-Mode all` ejecuta LOCAL antes de INTERNET. LOCAL requiere que el tipo esté vacío, por lo que al repetirlo sobre datos existentes la aplicación se detiene antes de leer los ATOM. `-CreateSchemaFirstRun` recrea las tablas gestionadas en el primer bloque de cada base. Para aplicar las migraciones a datos existentes mantenga `validate`.

## Comandos en Linux o macOS

Configure `bd.properties` para el destino de cada ejecución. Use `--base-dir` si Málaga y Pliegos tienen instalaciones separadas.

```bash
./importar_atom.sh --base-dir /srv/opendata/malaga --grupo con_filtros --mode local --dry-run
./importar_atom.sh --base-dir /srv/opendata/malaga --grupo con_filtros --mode local
./importar_atom.sh --base-dir /srv/opendata/malaga --grupo con_filtros --mode internet
./importar_atom.sh --base-dir /srv/opendata/pliegos --grupo sin_filtros --mode internet --tipo MAYORES
```

## Ejecutar un JAR contra `prueba-opendata`

Prepare un directorio de properties independiente con `jdbc:mariadb://localhost:3306/prueba-opendata`, `hibernate.hbm2ddl.auto=validate`, el filtro `29`, los orígenes locales y el correo desactivado. Establezca `app.tipo_sindicacion` al tipo que quiere cargar antes de cada ejecución. El JAR usa el esquema de esos properties.

```powershell
# Con app.tipo_sindicacion=MAYORES en los properties de pruebas.
java -Xms512m -Xmx6g -jar .\opendata_con_filtros-8.3.0-local.jar `
  '--configDir=C:\opendata-pruebas\properties'
```

## Compilar y archivar los cuatro perfiles

Desde la raíz de desarrollo de Windows, compile y compruebe los ejecutables antes de activar una instalación. La publicación local archiva los JAR, la guía y las migraciones en `versions/8.3.0`.

```powershell
.\generar_ejecutables.ps1 -NoDeploy
.\publicar_ejecutables.ps1 -Version 8.3.0 -WhatIf
.\publicar_ejecutables.ps1 -Version 8.3.0
Get-FileHash .\target\opendata_*8.3.0*.jar -Algorithm SHA256
```

## Validación

En `prueba-opendata` se leyeron 62 páginas ATOM de los cuatro tipos con filtro postal `29`. De 29.608 entradas leídas, quedaron 729 expedientes vigentes y 842 observaciones en el histórico. La repetición LOCAL se detuvo en los cuatro bloques. La repetición con la lógica incremental y los mismos ficheros locales conservó los UUID y los XML vigentes, y añadió otras 842 observaciones con estado `RECHAZAR`. Se comprobaron los UUID v7 y las referencias del modelo, los importes y las monedas, los campos de consulta y la conservación de dos `DOC_CN` al cambiar el orden de una revisión de prueba.
