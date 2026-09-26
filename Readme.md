# Importador de Datos Abiertos de la PLACSP

Aplicación Java para importar los feeds ATOM publicados por la Plataforma de Contratación del Sector Público (PLACSP), transformarlos al modelo CODICE y persistirlos en MariaDB.

El proyecto admite importaciones desde ficheros locales y desde Internet, tanto con filtros como sin ellos. Está preparado para cargas de gran volumen, con métricas de parseo y persistencia, operaciones JDBC por lote y parámetros externos de memoria, Hibernate y conexiones.

## Modalidades de importación

| Grupo | Modo | Finalidad |
|---|---|---|
| `con_filtros` | `local` | Importa un ATOM local y aplica filtros por NIF o código postal. |
| `con_filtros` | `internet` | Lee feeds remotos de forma incremental y aplica filtros. |
| `sin_filtros` | `local` | Importa un ATOM local completo sin filtros. |
| `sin_filtros` | `internet` | Lee feeds remotos de forma incremental sin filtros. |

## Requisitos

Para compilar desde código fuente:

- JDK 21.
- Maven 3.9.16 o superior.
- Acceso a los repositorios Maven configurados, incluidos los paquetes privados de `local.jarios`.

Para ejecutar una importación:

- MariaDB accesible desde el equipo que ejecuta el proceso.
- Ficheros de configuración preparados.
- Un JAR de la variante seleccionada.
- En modo `local`, el fichero ATOM configurado debe existir en disco.
- En modo `internet`, conectividad HTTPS con los orígenes PLACSP.

## Obtención de ejecutables

Las releases publicadas incluyen los cuatro JAR operativos:

<https://github.com/contratacionmalaga/import_from_opendata/releases>

| JAR | Uso |
|---|---|
| `opendata_con_filtros-<versión>-local.jar` | Carga local con filtros. |
| `opendata_con_filtros-<versión>-internet.jar` | Carga incremental desde Internet con filtros. |
| `opendata_sin_filtros-<versión>-local.jar` | Carga local completa sin filtros. |
| `opendata_sin_filtros-<versión>-internet.jar` | Carga incremental desde Internet sin filtros. |

Los lanzadores seleccionan el JAR con mayor versión disponible para cada combinación de grupo y modo.

## Estructura de una instalación operativa

Una instalación puede estar fuera del repositorio. El directorio debe contener los JAR, los scripts y la configuración local.

```text
import-from-opendata-ejecutables/
├── importar_atoms.ps1
├── importar_atom.sh
├── opendata_con_filtros-<versión>-local.jar
├── opendata_con_filtros-<versión>-internet.jar
├── opendata_sin_filtros-<versión>-local.jar
├── opendata_sin_filtros-<versión>-internet.jar
├── logs/
└── properties/
    ├── app.properties
    ├── bd.properties
    ├── filter.properties
    ├── hibernate.properties
    ├── mail.properties
    ├── runtime.properties
    └── jakarta_filtro.properties
```

En Windows, la ruta operativa predeterminada es `C:\java\ejecutables\import-from-opendata-ejecutables`. Si no existe y el script se ejecuta desde un directorio con `properties`, ese directorio se utiliza como directorio operativo.

## Configuración

El repositorio incluye plantillas en `properties/*.properties.example`. Copie las necesarias y sustituya los valores de ejemplo.

```powershell
Copy-Item properties\app.properties.example properties\app.properties
Copy-Item properties\bd.properties.example properties\bd.properties
Copy-Item properties\filter.properties.example properties\filter.properties
Copy-Item properties\hibernate.properties.example properties\hibernate.properties
Copy-Item properties\mail.properties.example properties\mail.properties
Copy-Item properties\runtime.properties.example properties\runtime.properties
```

En Linux o macOS:

```bash
cp properties/app.properties.example properties/app.properties
cp properties/bd.properties.example properties/bd.properties
cp properties/filter.properties.example properties/filter.properties
cp properties/hibernate.properties.example properties/hibernate.properties
cp properties/mail.properties.example properties/mail.properties
cp properties/runtime.properties.example properties/runtime.properties
```

Los ficheros reales de configuración están excluidos de Git porque pueden contener rutas, credenciales de base de datos y datos SMTP.

### `app.properties`

Define los orígenes locales y remotos, el tipo procesado, los reintentos HTTP y las notificaciones.

```properties
app.local.path=d:\\placsp\\
app.local.mayores=licitacionesPerfilesContratanteCompleto3.atom
app.internet.mayores=https://contrataciondelsectorpublico.gob.es/...

app.http.max_retries=3
app.http.retry_delay_ms=60000
app.http.request_delay_ms=0

app.tipo_sindicacion=MAYORES
app.persistir_historicos_rechazados=false
app.email.enabled=false
```

Mantenga `app.persistir_historicos_rechazados=false` durante la operación normal. Activarlo persiste un histórico por cada entrada rechazada y puede aumentar mucho el volumen almacenado.

Para pruebas y benchmarks, desactive las notificaciones mediante `app.email.enabled=false`.

### `bd.properties`

Contiene la conexión principal a MariaDB.

```properties
jakarta.persistence.jdbc.url=jdbc:mariadb://localhost:3306/opendata_prueba
jakarta.persistence.jdbc.driver=org.mariadb.jdbc.Driver
jakarta.persistence.jdbc.user=USUARIO
jakarta.persistence.jdbc.password=CONTRASENA
```

El lanzador muestra la base de datos, servidor, puerto y usuario antes de ejecutar; la contraseña nunca se muestra.

### `filter.properties`

Los filtros se aplican solamente a `con_filtros`.

```properties
filter.fechaInicialLectura=
filter.fechaFinalLectura=
filter.codigosPostales=
filter.nifs=
```

- Las fechas usan formato `yyyy-MM-dd`.
- Los códigos postales son prefijos de dos dígitos separados por comas: `29,11,41`.
- Los NIF se separan por comas.
- Para ejecutar `con_filtros`, debe informarse `filter.nifs` o `filter.codigosPostales`. Un filtro exclusivo por fecha no habilita la ejecución.

### `hibernate.properties`

Controla el esquema, Hibernate y el pool de conexiones.

```properties
hibernate.jdbc.batch_size=150
hibernate.order_inserts=true
hibernate.order_updates=true
hibernate.jdbc.batch_versioned_data=true

hibernate.hikari.maximumPoolSize=30
hibernate.hikari.minimumIdle=15

hibernate.generate_statistics=true
hibernate.hbm2ddl.auto=validate
```

| Valor de `hibernate.hbm2ddl.auto` | Comportamiento |
|---|---|
| `validate` | Comprueba el esquema sin modificarlo. Recomendado con datos reales. |
| `update` | Puede modificar el esquema al detectar diferencias. |
| `create` | Recrea tablas y puede eliminar datos existentes. |
| `create-drop` | Crea tablas y puede eliminarlas al finalizar. No usar con datos reales. |
| `none` | No valida ni modifica el esquema. |

### `runtime.properties`

Define las opciones JVM empleadas por los lanzadores.

```properties
java.opts=-Xms12g -Xmx12g
```

Para cargas de gran volumen, ajuste el valor a los recursos realmente disponibles:

```properties
java.opts=-Xms48g -Xmx48g
```

## Tipos de sindicación

| Valor técnico | Contenido |
|---|---|
| `MAYORES` | Licitaciones y contratos mayores. |
| `MENORES` | Contratos menores. |
| `ENCARGOS` | Encargos a medios propios. |
| `CONSULTAS` | Consultas preliminares de mercado. |
| `AGREGRADAS` | Plataformas agregadas. |

Actualmente, `sin_filtros` solo admite `MAYORES`.

## Ejecución en Windows

El lanzador Windows es `importar_atoms.ps1`.

```powershell
.\importar_atoms.ps1 `
  -Grupo sin_filtros `
  -Mode local `
  -TipoSindicacion MAYORES
```

Antes de ejecutar, muestra el plan completo: bloques seleccionados, origen, JAR, destino de base de datos, esquema Hibernate, memoria, filtros, correo y comportamiento aplicable. La importación solo comienza al escribir `I`, salvo que se indique expresamente `-SkipConfirmation` para una tarea programada o servicio.

| Parámetro | Valores | Descripción |
|---|---|---|
| `-Grupo` | `con_filtros`, `sin_filtros`, `all` | Grupo que se ejecutará. Valor predeterminado: `con_filtros`. |
| `-Mode` | `local`, `internet`, `all` | Origen de los feeds. Valor predeterminado: `local`. |
| `-TipoSindicacion` | Uno o varios tipos | Limita los tipos ejecutados. Si se omite, ejecuta todos los permitidos para el grupo. |
| `-DryRun` | Interruptor | Valida el plan y muestra los comandos Java sin modificar properties ni iniciar Java. |
| `-SkipConfirmation` | Interruptor | Omite la confirmación interactiva. Solo para ejecuciones autónomas previamente configuradas. El plan y la activación quedan registrados en el log. |
| `-ContinueOnError` | Interruptor | Continúa con el siguiente bloque si uno falla. |
| `-CreateSchemaFirstRun` | Interruptor | Usa `create` solo en el primer bloque y `none` en los restantes. Solo para una base de datos nueva de pruebas. |
| `-BaseDir` | Ruta | Directorio que contiene JAR, `properties` y `logs`. |

### Ejemplos de Windows

Carga local completa de contratos mayores:

```powershell
.\importar_atoms.ps1 -Grupo sin_filtros -Mode local -TipoSindicacion MAYORES
```

Carga incremental desde Internet:

```powershell
.\importar_atoms.ps1 -Grupo sin_filtros -Mode internet -TipoSindicacion MAYORES
```

Carga con filtros por NIF o código postal:

```powershell
.\importar_atoms.ps1 -Grupo con_filtros -Mode internet -TipoSindicacion MAYORES
```

Validar una ejecución sin modificar configuración ni datos:

```powershell
.\importar_atoms.ps1 -Grupo sin_filtros -Mode local -TipoSindicacion MAYORES -DryRun
```

Ejecutar todos los grupos, modos y tipos permitidos:

```powershell
.\importar_atoms.ps1 -Grupo all -Mode all
```

Usar una instalación operativa alternativa:

```powershell
.\importar_atoms.ps1 -BaseDir D:\opendata\ejecutables -Grupo sin_filtros -Mode local -TipoSindicacion MAYORES
```

### Ejecución autónoma en Windows

Para el Programador de tareas o un servicio, añada `-SkipConfirmation` al script y ejecute PowerShell con `-NonInteractive`. La confirmación se mantiene para todas las invocaciones que no incluyan ese interruptor.

```powershell
powershell.exe -NoProfile -NonInteractive -File "C:\java\ejecutables\import-from-opendata-ejecutables\importar_atoms.ps1" `
  -Grupo sin_filtros `
  -Mode local `
  -TipoSindicacion MAYORES `
  -SkipConfirmation
```

Configure la tarea con una cuenta que tenga acceso de lectura a los ficheros ATOM y de escritura al directorio de `logs` y a la base de datos configurada. Revise el log de lanzador generado en `logs/importacion_yyyyMMdd_HHmmss.log` al finalizar.

## Ejecución en Linux y macOS

El lanzador es `importar_atom.sh`.

```bash
chmod +x importar_atom.sh

./importar_atom.sh \
  --grupo sin_filtros \
  --mode local \
  --tipo MAYORES
```

| Parámetro | Valores | Descripción |
|---|---|---|
| `--grupo` | `con_filtros`, `sin_filtros`, `all` | Grupo funcional. |
| `--mode` | `local`, `internet`, `all` | Origen de los feeds. |
| `--tipo` | Tipos separados por comas | Tipo o tipos a ejecutar. |
| `--base-dir` | Ruta | Directorio operativo. |
| `--java` | Ruta | Ejecutable Java que se utilizará. |
| `--dry-run` | — | Muestra el plan sin ejecutar Java ni modificar properties. |
| `--continue-on-error` | — | Continúa con los bloques siguientes tras un error. |
| `--help` | — | Muestra la ayuda del script. |

Ejemplos:

```bash
./importar_atom.sh --grupo sin_filtros --mode local --tipo MAYORES

./importar_atom.sh --grupo con_filtros --mode internet --tipo MAYORES,MENORES

./importar_atom.sh \
  --base-dir /srv/opendata \
  --java /usr/lib/jvm/java-21/bin/java \
  --grupo sin_filtros \
  --mode internet \
  --tipo MAYORES
```

## Reglas de importación

### Modo local

- Lee el ATOM configurado en `app.local.path`.
- Requiere que no existan entradas previas del tipo seleccionado.
- Si encuentra datos existentes para ese tipo, detiene la importación.
- Se utiliza para cargas iniciales o reconstrucciones sobre una base de datos vacía.

### Modo Internet

- Obtiene el feed inicial configurado en `app.internet.*`.
- Continúa la navegación por los enlaces `next`.
- Detecta ciclos y enlaces `next` inválidos.
- Compara con los datos existentes para decidir inserciones y actualizaciones.
- Aplica los reintentos y pausas definidos en `app.properties`.

## Logs, incidencias y observabilidad

Cada ejecución crea un log de lanzador en `logs/importacion_yyyyMMdd_HHmmss.log`. La aplicación separa los registros en los siguientes ficheros:

| Fichero | Contenido |
|---|---|
| `logs/import-from-opendata.log` | Progreso de la importación, métricas y avisos operativos. |
| `logs/import-from-opendata_error.log` | Diagnóstico técnico de errores, incluida la pila completa de excepciones. |
| `logs/import-from-opendata_hibernate.log` | Eventos de Hibernate, JDBC y HikariCP. |

La consola no muestra trazas Java. Cuando una importación falla, muestra un identificador con formato `IMP-<fecha>-<hora>-<id>` y pide consultar `logs/import-from-opendata_error.log`. Ese identificador permite relacionar la consola, el correo de soporte y el log técnico.

Los logs se rotan por fecha y tamaño. Los archivos archivados se comprimen automáticamente; los registros operativos se conservan 30 días y los de errores 90 días.

### Correo de soporte

Si `app.email.enabled=true`, el correo de incidencia incluye el identificador, versión, grupo, origen, tipo de sindicación, fase del pipeline, excepción raíz y pila técnica depurada. Nunca incorpora contraseñas JDBC o SMTP, tokens ni valores de autenticación.

```properties
app.email.error.include_stacktrace=true
app.email.error.max_stacktrace_chars=50000
```

Si la importación ya se confirmó y falla solamente el correo de éxito, la carga mantiene el resultado correcto y se registra una incidencia técnica. Si el propio SMTP no está disponible, el diagnóstico queda en el log de errores.

La aplicación registra feeds, entradas y tombstones procesados; tiempos de lectura, JAXB, mapeo, filtrado y persistencia; duración de preparación, borrado, `flush` y `commit`; tamaño de la ventana ORM y métricas Hibernate cuando están activadas.

Para operación habitual, desactive las estadísticas y el SQL detallado si no están siendo utilizados para diagnóstico:

```properties
hibernate.generate_statistics=false
hibernate.show_sql=false
hibernate.format_sql=false
```

## Compilación desde código fuente

En el entorno de desarrollo Windows se incluye un script para seleccionar Java y Maven:

```powershell
.\scripts\use-java21-maven3916.ps1 test
```

Comprobar formato y ejecutar pruebas:

```powershell
.\mvnw.cmd spotless:check test
```

Generar los cuatro artefactos y copiarlos al directorio operativo:

```powershell
.\generar_ejecutables.ps1
```

El script omite las pruebas de forma predeterminada. Para ejecutarlas durante el empaquetado:

```powershell
.\generar_ejecutables.ps1 -SkipTests $false
```

Empaquetado manual por perfil:

```powershell
.\mvnw.cmd clean package -Pcon-filtros-local -DskipTests
.\mvnw.cmd package -Pcon-filtros-internet -DskipTests
.\mvnw.cmd package -Psin-filtros-local -DskipTests
.\mvnw.cmd package -Psin-filtros-internet -DskipTests
```

## Ejecución directa de un JAR

Los lanzadores son la forma recomendada porque validan la configuración, seleccionan el JAR adecuado y establecen temporalmente `app.tipo_sindicacion`.

Para ejecutar directamente un JAR, indique siempre el directorio de configuración:

```powershell
java -Xms12g -Xmx12g `
  -jar opendata_sin_filtros-7.5.0-local.jar `
  --configDir=C:\java\ejecutables\import-from-opendata-ejecutables\properties
```

En una ejecución directa, `app.tipo_sindicacion` debe tener ya el valor correcto en `app.properties`.

## Calidad, seguridad y documentación

El proyecto dispone de pruebas unitarias, pruebas de integración configurables, comprobación de formato con Spotless y análisis de seguridad.

La documentación técnica se organiza en:

- [`docs/auditorias/`](docs/auditorias/)
- [`docs/plan_inicial_implementacion_2026-09-26.md`](docs/plan_inicial_implementacion_2026-09-26.md)
- [`docs/releases/`](docs/releases/)
- [`docs/migraciones/`](docs/migraciones/)

## Licencia

Este proyecto se distribuye bajo licencia MIT. Consulte [`LICENSE`](LICENSE).
