# Importador PLACSP 8.2.1 — guía de comandos

Versión del 13/10/2026 para Málaga y Pliegos.
[Release y descargas en GitHub](https://github.com/contratacionmalaga/import_from_opendata/releases/tag/v8.2.1).

## Qué incluye la versión

El lanzador PowerShell selecciona automáticamente la base según el grupo, mantiene la
conexión y cambia de esquema antes de cada bloque. `DryRun` muestra el destino sin ejecutar
Java ni modificar properties. La creación inicial de esquema se aplica una vez por base.
Se conservan los UUID v7, relaciones e identidad existentes incorporados en 8.2.0.

| Grupo | Base seleccionada por PowerShell | Tipos permitidos |
|---|---|---|
| `con_filtros` | `opendata_malaga` | CONSULTAS, ENCARGOS, MAYORES, MENORES |
| `sin_filtros` | `opendata_pliegos` | MAYORES |
| `all` | Alterna entre ambas | Los permitidos por cada grupo |

| JAR | Uso |
|---|---|
| `opendata_con_filtros-8.2.1-local.jar` | Málaga, ficheros locales |
| `opendata_con_filtros-8.2.1-internet.jar` | Málaga, Internet |
| `opendata_sin_filtros-8.2.1-local.jar` | Pliegos, ficheros locales |
| `opendata_sin_filtros-8.2.1-internet.jar` | Pliegos, Internet |

## Instalación y configuración

Requiere Java 21, MariaDB accesible y los properties de la instalación.
El directorio operativo Windows predeterminado es
`C:\java\ejecutables\import-from-opendata-ejecutables`. Los comandos PowerShell de importación
se pueden ejecutar desde la raíz de desarrollo o desde ese directorio.

La publicación local archiva los cuatro JAR, scripts, properties, README y notas en
`versions/8.2.1`, y activa la versión en `active-release.properties`. Los lanzadores leen esa
versión; sin el fichero de activación, eligen el JAR de mayor versión en el directorio operativo.
Si se descargan los archivos de GitHub, las plantillas se obtienen del código fuente de la
etiqueta `v8.2.1`. Los properties reales con credenciales no se distribuyen en GitHub.

En una instalación nueva, copiar las plantillas y completar sus valores; conservar los
properties existentes cuando se actualiza una instalación operativa:

```powershell
Copy-Item properties\app.properties.example properties\app.properties
Copy-Item properties\bd.properties.example properties\bd.properties
Copy-Item properties\filter.properties.example properties\filter.properties
Copy-Item properties\hibernate.properties.example properties\hibernate.properties
Copy-Item properties\mail.properties.example properties\mail.properties
Copy-Item properties\runtime.properties.example properties\runtime.properties
```

`bd.properties` aporta servidor, puerto, usuario, contraseña y opciones JDBC. PowerShell
sustituye solamente el nombre de la base, según el grupo. No cambia entre puertos: si se
desea utilizar 3307 en lugar de 3306, debe configurarse ese puerto en la URL de conexión.
Una instalación alternativa se selecciona con `-BaseDir`.

Para operación sobre esquemas existentes, `hibernate.properties` debe contener:

```properties
hibernate.hbm2ddl.auto=validate
```

Para Málaga por prefijo postal, configurar en `filter.properties`:

```properties
filter.codigosPostales=29
filter.nifs=
```

También se puede filtrar mediante una lista de NIF en `filter.nifs`. `con_filtros` exige
al menos un NIF o prefijo postal; un rango de fechas por sí solo no satisface esa comprobación.
El grupo `sin_filtros` no aplica filtros de NIF ni de código postal.

Los límites temporales opcionales usan `yyyy-MM-dd`: `filter.fechaInicialLectura` es el extremo
más moderno y `filter.fechaFinalLectura`, el más antiguo. Vacío significa sin límite en ese extremo.

## Orígenes locales e Internet

El modo `local` requiere que no existan entries previos del tipo elegido. Si ya hay datos de
ese tipo, la aplicación detiene la carga. El modo `internet` compara los datos existentes e
inserta o actualiza; detiene la navegación al alcanzar entries anteriores al cursor local.

En `app.properties`, `app.local.path` es la raíz local y debe terminar en separador. Ejemplo:

```properties
app.local.path=d:\\placsp\\
app.local.cpms=CPM_SectorPublico.atom
app.local.emps=EMP_SectorPublico.atom
app.local.mayores=licitacionesPerfilesContratanteCompleto3.atom
app.local.menores=contratosMenoresPerfilesContratantes.atom
```

| Tipo | Subdirectorio | Fichero local | URL de Internet |
|---|---|---|---|
| CONSULTAS | `cpm` | `app.local.cpms` | `app.internet.cpms` |
| ENCARGOS | `emp` | `app.local.emps` | `app.internet.emps` |
| MAYORES | `may` | `app.local.mayores` | `app.internet.mayores` |
| MENORES | `men` | `app.local.menores` | `app.internet.menores` |

Por ejemplo, CONSULTAS leerá `d:\placsp\cpm\CPM_SectorPublico.atom`. Las URLs iniciales se
configuran en `app.internet.*`; reintentos y pausas, en `app.http.*`.
`app.email.enabled` controla las notificaciones; usar `false` en pruebas.
La memoria se configura mediante `java.opts` en `runtime.properties`; sin esa propiedad el
lanzador usa `-Xms12g -Xmx12g`. Los lanzadores establecen `app.tipo_sindicacion` antes de cada bloque.

## Parámetros de importar_atoms.ps1

| Parámetro | Valores y comportamiento |
|---|---|
| `-Grupo` | `con_filtros` (predeterminado), `sin_filtros`, `all` |
| `-Mode` | `local` (predeterminado), `internet`, `all` |
| `-TipoSindicacion` | Uno o varios tipos, separados por comas en PowerShell; sin especificarlo se ejecutan todos los permitidos del grupo |
| `-BaseDir` | Directorio de ejecutables, properties y logs; predeterminado: `C:\java\ejecutables\import-from-opendata-ejecutables` |
| `-DryRun` | Valida parámetros, JAR y filtros; muestra destinos/comandos; no modifica properties ni inicia Java. Puede escribir el log del lanzador |
| `-SkipConfirmation` | Omite la confirmación interactiva; destinado a tareas programadas ya configuradas |
| `-ContinueOnError` | Continúa con el siguiente bloque si uno devuelve error; el proceso global sigue terminando con error |
| `-CreateSchemaFirstRun` | Usa `create` en el primer bloque de cada base y `none` en los siguientes de esa base; recrea tablas |

La importación normal muestra el plan y comienza únicamente al escribir `I`.
`-TipoSindicacion` debe ser válido para todos los grupos seleccionados; por eso, con
`-Grupo all`, MAYORES es el único tipo que puede especificarse explícitamente para ambos.
Si se omite el tipo, Málaga ejecuta CONSULTAS, ENCARGOS, MAYORES y MENORES, y Pliegos ejecuta MAYORES.

## Comandos Windows de importación

Málaga, todos sus tipos desde ficheros locales, con los filtros configurados:

```powershell
.\importar_atoms.ps1 -Grupo con_filtros -Mode local
```

Málaga, solamente mayores y menores locales:

```powershell
.\importar_atoms.ps1 -Grupo con_filtros -Mode local -TipoSindicacion MAYORES,MENORES
```

Málaga desde Internet, todos los tipos o uno seleccionado:

```powershell
.\importar_atoms.ps1 -Grupo con_filtros -Mode internet
.\importar_atoms.ps1 -Grupo con_filtros -Mode internet -TipoSindicacion MAYORES
```

Pliegos, MAYORES sin filtros, local o Internet:

```powershell
.\importar_atoms.ps1 -Grupo sin_filtros -Mode local
.\importar_atoms.ps1 -Grupo sin_filtros -Mode internet
```

Ambas bases, o ambos grupos y modos:

```powershell
.\importar_atoms.ps1 -Grupo all -Mode local
.\importar_atoms.ps1 -Grupo all -Mode internet
.\importar_atoms.ps1 -Grupo all -Mode all
```

Con `-Mode all`, LOCAL se ejecuta antes que INTERNET para cada grupo. Los fallos detienen la
secuencia, salvo `-ContinueOnError`. La selección de esquema cambia automáticamente al pasar
del grupo con filtros al grupo sin filtros.

Simular Málaga local o ambos grupos y modos, sin iniciar importaciones:

```powershell
.\importar_atoms.ps1 -Grupo con_filtros -Mode local -DryRun
.\importar_atoms.ps1 -Grupo all -Mode all -DryRun
```

Utilizar otra instalación o continuar los bloques después de un error:

```powershell
.\importar_atoms.ps1 -BaseDir D:\opendata\ejecutables -Grupo con_filtros -Mode local
.\importar_atoms.ps1 -Grupo con_filtros -Mode internet -ContinueOnError
```

## Reconstruir esquema y vaciar datos

`-CreateSchemaFirstRun` **borra y recrea las tablas mapeadas**. No significa borrar solamente
sus filas ni garantiza conservar intactas las dependencias externas de las vistas.
En Málaga se han comprobado 35 tablas físicas y 26 vistas, en ambos puertos locales 3306 y 3307.
Hibernate gestiona 33 tablas; `empresas_adjudicatarias` y `entry_anuncios` están fuera del modelo.
Esta opción no representa una limpieza completa de todas las tablas de `opendata_malaga`.

Para borrar solamente datos conservando tablas y vistas, preparar esa limpieza por separado
y ejecutar el importador con `validate`, sin `-CreateSchemaFirstRun`. No existe un interruptor
de vaciado de filas en estos lanzadores. No se ha ejecutado ninguna limpieza operativa al
preparar esta release.

Para una reconstrucción expresamente deseada de las tablas gestionadas, simular primero:

```powershell
.\importar_atoms.ps1 -Grupo con_filtros -Mode local -CreateSchemaFirstRun -DryRun
```

El comando de reconstrucción, al confirmar con `I`, es:

```powershell
.\importar_atoms.ps1 -Grupo con_filtros -Mode local -CreateSchemaFirstRun
```

Con `-Grupo all`, el primer bloque de Málaga y el primero de Pliegos usarán `create`;
los posteriores de cada base usarán `none`. El script deja el último modo en `hibernate.properties`;
establecer de nuevo `validate` antes de las siguientes ejecuciones normales.

## Programador de tareas de Windows

Ejemplo de importación incremental de Málaga sin interacción:

```powershell
powershell.exe -NoProfile -NonInteractive -File "C:\java\ejecutables\import-from-opendata-ejecutables\importar_atoms.ps1" `
  -Grupo con_filtros -Mode internet -TipoSindicacion MAYORES -SkipConfirmation
```

La cuenta de la tarea debe tener acceso a los properties, a los ATOM locales cuando se utilicen,
a los logs y a la base de datos. Para continuar tras fallos, añadir `-ContinueOnError`.

## Linux y macOS: importar_atom.sh

El lanzador Bash conserva el esquema configurado en `bd.properties`: **no selecciona la base
automáticamente por grupo** en 8.2.1. Configurar `opendata_malaga` para `con_filtros` y
`opendata_pliegos` para `sin_filtros` antes de ejecutar; usar instalaciones/configuraciones
separadas si se necesitan ambos destinos. Esto también se aplica a `--grupo all`.

| Opción | Uso |
|---|---|
| `--grupo` | `con_filtros`, `sin_filtros`, `all`; predeterminado: `con_filtros` |
| `--mode` | `local`, `internet`, `all`; predeterminado: `local` |
| `--tipo` | Tipos separados por comas; sin opción, los predeterminados del grupo |
| `--base-dir` | Instalación operativa; predeterminada: `/home/contratacion/java/import-from-opendata-ejecutables` |
| `--java` | Ruta de Java 21; predeterminada: `/home/contratacion/java/jdk-21.0.11/bin/java` |
| `--dry-run` | Simula y no inicia Java ni modifica properties; puede escribir logs |
| `--continue-on-error` | Continúa tras errores, manteniendo el resultado global fallido |
| `-h`, `--help` | Muestra ayuda |

```bash
chmod +x importar_atom.sh
./importar_atom.sh --help
./importar_atom.sh --grupo con_filtros --mode local --dry-run
./importar_atom.sh --grupo con_filtros --mode local
./importar_atom.sh --grupo con_filtros --mode internet --tipo MAYORES,MENORES
./importar_atom.sh --grupo sin_filtros --mode local --tipo MAYORES
./importar_atom.sh --base-dir /srv/opendata --java /usr/lib/jvm/java-21/bin/java --grupo sin_filtros --mode internet
```

La ejecución real pide escribir `I`. Bash no ofrece `--skip-confirmation` ni equivalente
de `-CreateSchemaFirstRun`; el modo de Hibernate procede de sus properties.

## Ejecutar un JAR directamente

La ejecución directa utiliza el esquema que tenga `bd.properties`; no aplica el enrutamiento
del lanzador. Preparar destino, filtros, `app.tipo_sindicacion` y modo Hibernate antes de ejecutar.

```powershell
java -Xms12g -Xmx12g `
  -jar C:\java\ejecutables\import-from-opendata-ejecutables\opendata_con_filtros-8.2.1-local.jar `
  --configDir=C:\java\ejecutables\import-from-opendata-ejecutables\properties
```

Se puede sustituir el JAR por otra de las cuatro variantes de 8.2.1.

## Compilar y publicar en ejecutables

Desde la raíz de desarrollo, con JDK 21 y Maven 3.9.16 configurados:

```powershell
# Generar los cuatro JAR sin activar una versión.
.\generar_ejecutables.ps1 -NoDeploy

# Generar sin limpiar target; útil después de verificar las pruebas.
.\generar_ejecutables.ps1 -NoDeploy -SkipClean

# Generar, archivar y activar automáticamente.
.\generar_ejecutables.ps1

# Ejecutar pruebas durante la generación.
.\generar_ejecutables.ps1 -SkipTests $false

# Cambiar el destino operativo.
.\generar_ejecutables.ps1 -OutputDir D:\opendata\ejecutables
```

`-SkipTests` es booleano y vale `$true` de forma predeterminada. `-SkipClean` omite la limpieza
inicial; `-NoDeploy` evita la publicación. Sin `-NoDeploy`, `-OutputDir` fija dónde se activa la versión.

Para publicar los JAR ya compilados, simular o restaurar 8.2.0:

```powershell
.\publicar_ejecutables.ps1 -Version 8.2.1 -WhatIf
.\publicar_ejecutables.ps1 -Version 8.2.1
.\publicar_ejecutables.ps1 -Version 8.2.1 -OutputDir D:\opendata\ejecutables
.\publicar_ejecutables.ps1 -RollbackVersion 8.2.0
```

La versión predeterminada se toma de `pom.xml`. El publicador guarda un manifiesto SHA-256,
copia los properties del directorio fuente y respalda los anteriores en `backups/properties/`.
La restauración repone los JAR, scripts y properties de la versión archivada; no remapea UUID
ni deshace cambios realizados en las bases de datos.

Desde el directorio operativo, debe indicarse la fuente de desarrollo:

```powershell
.\publicar_ejecutables.ps1 -SourceDir C:\java\desarrollo\import-from-opendata -Version 8.2.1
```

## Pruebas y comprobación de descargas

Desde la raíz de desarrollo:

```powershell
# Pruebas Java y empaquetado de una variante.
.\scripts\use-java21-maven3916.ps1 verify -Pcon-filtros-local

# Siete casos del lanzador, con Java simulado y sin acceder a bases reales.
.\scripts\test-importar-atoms-routing.ps1

# UUID, columnas y relaciones reales en localhost:3306/opendata_malaga_testing.
.\scripts\use-java21-maven3916.ps1 test -Pcon-filtros-local `
  '-Dtest=UuidV7PersistenceIntegrationTest' '-Dopendata.uuid.mariadb=true' `
  '-Dopendata.uuid.properties=properties/bd.properties'
```

El test MariaDB usa el fichero para leer las credenciales, sustituye el destino por la base
fija de pruebas y revierte o limpia sus filas. Comprueba 33 IDs y 42 FK UUID nativos, versión 7,
referencias exactas, campos externos y conservación de identidad en reimportaciones y filas v4.

Comparar los hashes publicados con las descargas, desde el directorio que las contiene:

```powershell
Get-FileHash .\opendata_*8.2.1*.jar -Algorithm SHA256
Get-Content .\SHA256SUMS.txt
```

En Linux/macOS, usar `sha256sum -c SHA256SUMS.txt` con todos los archivos listados presentes.

## Logs y diagnóstico

| Fichero | Información |
|---|---|
| `logs/importacion_<id>.log` | Plan, base seleccionada y resultado del lanzador |
| `logs/import-from-opendata_<id>.log` | Progreso de la importación |
| `logs/import-from-opendata_error_<id>.log` | Diagnóstico técnico de errores |
| `logs/import-from-opendata_hibernate_<id>.log` | Hibernate, JDBC y conexiones |

Cada ejecución tiene su propio identificador. Un fallo sin `-ContinueOnError` detiene la
secuencia; un fallo de un bloque con esa opción deja continuar, pero la salida global sigue
siendo fallida. Si faltan filtros, revisar `filter.properties`; si el esquema no valida,
comprobar las migraciones previas antes de volver a importar. En local, verificar que no
existan datos del tipo solicitado y que el ATOM inicial esté en su subdirectorio.

Las altas nuevas de las 33 entidades son UUID v7. Las FK a esos padres contienen el mismo v7;
las referencias a registros v4 anteriores mantienen v4. Los identificadores externos de Atom
y CODICE conservan sus valores del proveedor.

## Validación de la release

Comprobaciones del 13/10/2026:

| Comprobación | Resultado |
|---|---|
| Batería Java | 85 pruebas, 0 fallos, 0 errores, 2 pruebas manuales omitidas |
| Lanzador Windows | 7 casos correctos con Java simulado, sin bases reales |
| MariaDB `opendata_malaga_testing:3306` | 3 pruebas, 0 fallos, 0 errores |
| UUID y relaciones reales | 33 IDs y 42 FK nativos v7; restricciones PK/FK y referencias exactas |
| Identidad | Reimportaciones y actualizaciones conservan UUID existentes; hijos nuevos v7 |
| Scripts PowerShell | Sintaxis comprobada en importador, generador, publicador y pruebas |
| Ejecutables | Cuatro perfiles compilados; cuatro JAR con versión 8.2.1, clase principal correcta y 33 generadores `VERSION_7` cada uno |

No se han ejecutado importaciones ni limpiezas en las bases operativas para preparar esta release.
