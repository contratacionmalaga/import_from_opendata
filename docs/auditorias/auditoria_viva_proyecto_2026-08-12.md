# Auditoria viva del proyecto

Fecha de creacion: 2026-08-12  
Proyecto: `import-from-opendata`  
Version declarada: `7.0.0`  
Estado del documento: vivo, actualizar en cada hito cerrado o en cada revision de dependencias.

## Como mantener viva esta auditoria

Actualizar este documento cada vez que se cierre un hito:

- Cambiar el estado del hito: `Pendiente`, `En curso`, `Bloqueado`, `Hecho`, `Aceptado`.
- Anadir fecha de cierre, PR/commit o evidencia tecnica.
- Mover los riesgos mitigados a "Historial de avances".
- Reejecutar revision de versiones antes de releases o mensualmente.

Formato recomendado para evidencias:

```text
Evidencia: commit <sha>, PR <url>, comando ejecutado, captura de CI o resultado de prueba.
```

## Resumen ejecutivo

El proyecto es una aplicacion Java de importacion batch para datos abiertos PLACSP, con Hibernate nativo, entidades JPA, repositorio propio sobre `SessionFactory`, perfiles Maven para variantes Malaga/Pliegos y ejecucion local/internet. Hay una refactorizacion importante en curso en el arbol de trabajo: nuevas clases `core/pipeline`, variantes movidas y bastantes entidades/mappers reubicados. Esta auditoria se ha hecho sobre el estado actual del workspace, sin revertir ni modificar esos cambios.

Los puntos que mas flaquean ahora son:

- Reproducibilidad: hay JDK 21.0.11 y Maven 3.9.16 instalados en `C:\java\software`, pero el `PATH` por defecto apunta a Java 8 y no expone `mvn`.
- Runtime local: el proyecto compila correctamente si se fija `JAVA_HOME=C:\java\software\jdk-21.0.11` y se antepone `C:\java\software\apache-maven-3.9.16\bin` al `PATH`.
- Calidad automatizada: Checkstyle esta comentado, OWASP Dependency Check esta configurado con `skip=true`, no se ve Surefire configurado explicitamente y Spotless/SpotBugs no estan ligados al ciclo por defecto.
- Persistencia: `getMapEntries` carga historico completo en memoria y acepta HQL como string externo.
- Seguridad: hay una API key NVD hardcodeada en `pom.xml`; los secretos de properties externas quedan fuera del repositorio, pero el codigo solo marca `password` como sensible.
- Modelo JPA: muchas relaciones con cascadas amplias, ausencia de `@Version`, pocos indices declarados y varios `@OneToOne` sin `fetch = LAZY`.
- Documentacion: `Readme.md` esta desactualizado respecto al `pom.xml` actual.

## Alcance revisado

- `pom.xml`: dependencias, plugins, perfiles, version Java y parametros de calidad.
- `Readme.md`: coherencia de versiones/documentacion.
- `qodana.yaml`: configuracion estatica de Qodana.
- `src/main/java/local/jarios/core`, `variants`, `repositories`, `services`, `database`, `entity`, `helpers`, `filtro`, `mappers`.
- `src/test/java/local/jarios/entity`: tests de convenciones JPA y mappers.
- `src/main/resources/logback.xml`, filtros SpotBugs.
- `docs/auditorias/auditoria_java_hibernate_mariadb_2026-05-09.md`: auditoria previa.

Se ha ejecutado `scripts/use-java21-maven3916.ps1 test` usando `JAVA_HOME=C:\java\software\jdk-21.0.11` y Maven `C:\java\software\apache-maven-3.9.16\bin`; resultado: 13 tests, 0 failures, 0 errors, build success. La JVM disponible en el `PATH` por defecto sigue siendo Java 8 (`1.8.0_501`), incompatible con `maven.compiler.source=21`.

## Riesgos principales

| ID | Estado | Prioridad | Area | Hallazgo | Evidencia | Recomendacion |
|---|---|---:|---|---|---|---|
| R01 | Hecho | Critica | Build | El proyecto declara Java 21; existe JDK 21.0.11, pero el `PATH` por defecto ejecuta Java 8. | `scripts/use-java21-maven3916.ps1 test`: 13 tests, 0 failures, 0 errors | Mantener el script como entrada reproducible; preferir JDK 21 LTS para este proyecto salvo migracion planificada a JDK 25 LTS. |
| R02 | Hecho | Critica | Build | Existe Maven 3.9.16 y hay script reproducible de proyecto. El `PATH` global puede seguir sin exponer `mvn`. | `scripts/use-java21-maven3916.ps1`; `mvn test` pasa al anteponer su `bin` al `PATH` | Mantener script; Maven Wrapper queda como mejora opcional si se quiere independencia de `C:\java\software`. |
| R03 | En curso | Alta | Seguridad | La API key NVD hardcodeada se ha retirado del `pom.xml`; queda pendiente rotar la clave expuesta y configurar `NVD_API_KEY` en CI/entorno. | `pom.xml`: `nvdApiKey=${env.NVD_API_KEY}` | Rotar la clave expuesta y guardar `NVD_API_KEY` como secret. |
| R04 | En curso | Alta | Seguridad | OWASP Dependency Check queda configurado igual que en `import-from-gc`: ejecucion explicita del goal Maven, `NVD_API_KEY` por entorno, umbral CVSS 8.0 y OSS Index desactivado. | `pom.xml`: `failBuildOnCVSS=8.0`, `nvdApiKey=${env.NVD_API_KEY}`, `ossIndexAnalyzerEnabled=false` | Ejecutar auditoria real con `NVD_API_KEY` y revisar vulnerabilidades. |
| R05 | Hecho | Alta | Persistencia | HQL libre eliminado de `getMapEntries`; la query vive en repositorio y usa parametro `:tipoSindicacion`. | `Repository.java`, `RepositoryImpl.java`, `ServicePrincipalImpl.java` | Siguiente mejora: reducir carga masiva de entidades completas. |
| R06 | En curso | Alta | Rendimiento | La comparacion historica ya usa snapshots `entryId/updated` en lugar de entidades `Entry` completas. | `RepositoryImpl.getEntrySnapshots`; `EntrySnapshot`; `EntryProcessorSnapshotTest` | Medir contra BD real y valorar paginacion si el mapa de snapshots tambien crece demasiado. |
| R07 | Hecho | Alta | Logica | Filtro de fechas corregido a rango inclusivo, alineado con el comentario del metodo. | `FiltroFechasEvaluator.java`; `FiltroFechasEvaluatorTest` | Mantener tests de rango dentro/bordes/fuera/invertido/nulo. |
| R08 | Hecho | Media | Transacciones | La persistencia de una importacion se agrupa en una unica operacion transaccional y queda cubierta por test de integracion con rollback. | `ImportPersistencePlan`; `ServicePrincipal.persistirImportacion`; `RepositoryImpl.persistirImportacion`; `RepositoryImplPersistenceIntegrationTest` | Si se requiere trazabilidad de fallos antes de commit, valorar estado `INICIADA/COMPLETADA/ERROR`. |
| R09 | Pendiente | Media | JPA | No se detecta `@Version` en entidades actualizables. | busqueda `@Version` sin resultados | Anadir versionado optimista a entidades con updates reales o garantizar lock externo single-instance. |
| R10 | En curso | Media | JPA | Indices de importacion incremental aplicados en Malaga/Pliegos; pendiente ejecutar scripts completos de vistas si procede. | `idx_mlg_log_tipo_id`, `idx_mlg_feed_log_id`, `idx_mlg_entry_feed_updated`, `idx_plg_log_tipo_id`, `idx_plg_feed_log_id`, `idx_plg_entry_feed_updated` | Medir vistas principales y aplicar indices de vistas en ventana de mantenimiento si los EXPLAIN lo justifican. |
| R11 | Hecho | Media | Logging | SQL Hibernate DEBUG y tipos TRACE desactivados por defecto. | `logback.xml`: `org.hibernate.SQL=WARN`, `org.hibernate.type.descriptor.sql=WARN` | Habilitar DEBUG/TRACE solo en diagnostico puntual. |
| R12 | Hecho | Media | Arquitectura | `System.exit` se ha movido a las clases `main`; la clase base devuelve codigo de salida. | `AbstractOpenDataBase.procesar`; mains Malaga/Pliegos local/internet | Mantener nuevos tests sobre core sin interceptar salida de proceso cuando se amplie cobertura. |
| R13 | En curso | Media | Memoria | El contexto retiene varias colecciones grandes simultaneamente; ya se redujo ruido de logging en parseo/preview/persistencia, se anadio `flush/clear` por lote en feeds e historicos, las variantes locales cortan por snapshots existentes, los historicos `RECHAZAR` se omiten por defecto y Pliegos ya no persiste historicos. | 2026-08-17 Pliegos INTERNET `run-6`: parseo 0h46m32s, persistencia aprox. 12m23s, sin `Duplicate entry`; 2026-08-17 rama `jarp/pliegos-persistencia-sin-historicos`: Pliegos transforma historicos `ACTUALIZAR` en `replacementEntryIds` y envia `historicoList` vacio. | Mantener medicion de heap con volumen real y comprobar impacto en carga LOCAL/INTERNET de Pliegos. |
| R14 | En curso | Baja | Documentacion | README actualizado con MariaDB, CI, Java/Maven y Dependency Check; los ficheros `.properties` incluyen comentarios de valores admitidos y formato esperado. | `Readme.md`; `.github/workflows/maven-ci.yml`; `properties/*.properties`; `docs/auditorias/plantilla_validacion_indices.md` | Completar cuando se ejecute Dependency Check real y se documenten metricas finales de indices/vistas. |
| R15 | Hecho | Alta | Rendimiento | La auditoria por entrada rechazada queda resumida por defecto: se conservan contadores y estadisticas, pero no se persiste una fila `historico` por cada `RECHAZAR` salvo modo diagnostico. | Ejecucion real Malaga MAYORES 2026-08-12 20:40: ultimo `log` `d619eb72-3cf3-45e6-96b7-b5b471657f14`; `historico=0`, `feeds=117`, `deleted_entries=155`, `total_historicos=57.864`, `rechazar=57.864` | Mantener `app.persistir_historicos_rechazados=false` por defecto; activar `true` solo para diagnostico puntual. |

## Hitos vivos

| Hito | Estado | Prioridad | Objetivo | Criterio de cierre | Evidencia |
|---|---|---:|---|---|---|
| H01 | Hecho | Critica | Build reproducible | Existe `mvnw` o script equivalente, CI usa JDK 21+, `mvn -version` y `mvn test` funcionan con entorno documentado | 2026-08-12: `scripts/use-java21-maven3916.ps1 test` pasa 13/13 |
| H02 | En curso | Critica | Seguridad de secretos | NVD API key y credenciales fuera del repo; clave expuesta rotada | 2026-08-12: `pom.xml` usa `${env.NVD_API_KEY}`; pendiente rotacion/configuracion externa |
| H03 | En curso | Alta | Dependencias auditables | Dependency Check configurado como en el resto de proyectos y ejecutable bajo demanda | 2026-08-12: plugin alineado con `import-from-gc`; pendiente ejecucion real con `NVD_API_KEY` |
| H04 | Hecho | Alta | Query historica segura | `getMapEntries` parametrizado y sin HQL libre en la interfaz publica | 2026-08-12: `mvn test`: 13 tests, 0 failures, 0 errors |
| H05 | En curso | Alta | Menor consumo de memoria | Carga historica por proyeccion/paginacion/lotes; prueba con volumen representativo | 2026-08-17: Pliegos INTERNET `run-6` finaliza correctamente con 519 feeds y 104.867 entries; `flush/clear` por lote evita el bloqueo observado en `PersistentBag.equalsSnapshot`; Pliegos ya no envia historicos a persistencia y conserva reemplazos mediante `replacementEntryIds`. |
| H06 | Hecho | Alta | Filtros fiables | Tests del filtro de fechas y correccion validada | 2026-08-12: corregido rango inclusivo; `mvn test`: 13 tests, 0 failures, 0 errors |
| H07 | En curso | Media | Modelo JPA medible | Indices y constraints principales versionados en SQL; `EXPLAIN` documentado | Indices de snapshots aplicados y `EXPLAIN` validado en Malaga/Pliegos; migracion Malaga documentada en `docs/auditorias/migracion_opendata_malaga_schema_2026-08-12.sql`; pendientes vistas completas |
| H08 | Hecho | Media | Transaccion de importacion | Importacion no queda inconsistente ante fallo a mitad | 2026-08-17: H2 modo MariaDB cubre reemplazo `ACTUALIZAR` y rollback completo; Pliegos INTERNET `run-6` elimina 39.749 entries existentes y persiste todo en una unica transaccion; `mvn test`: 27 tests, 0 fallos |
| H09 | Hecho | Media | Calidad en ciclo Maven | Spotless y SpotBugs ejecutables con comandos documentados y en verde | 2026-08-12: `spotless:apply` aplicado; `spotless:check` limpio; `spotbugs:check` limpio |
| H11 | Hecho | Alta | Pliegos sin historicos persistidos | Pliegos LOCAL/INTERNET puede reemplazar entries existentes sin insertar filas en `historico` | 2026-08-17: `ImportPersistencePlan.replacementEntryIds`; `AbstractOpenDataPliegos` envia `historicoList` vacio; validacion aislada LOCAL+INTERNET en `opendata_pliegos_validation`: `historico=0`, `log=2`, `estadistica=2`; limpieza real `opendata-pliegos`: `historico 104867 -> 0`; validacion real INTERNET posterior: `historico=0`; `mvn`: 28 tests, 0 fallos; SpotBugs: 0 bugs |
| H10 | En curso | Baja | Documentacion operativa | README actualizado con versiones, CI, configuracion y ejecucion; properties autocomentados con valores admitidos | README y `properties/*.properties` actualizados; pendiente evidencias reales de Dependency Check y metricas finales de indices/vistas |

## Revision de versiones

Fuente principal: `maven-metadata.xml` de Maven Central (`https://repo.maven.apache.org/maven2/.../maven-metadata.xml`). Consulta realizada el 2026-08-12. Los artefactos `local.jarios:*`, `place.codice:*` y `org.w3._2005.atom:atom` no se pudieron verificar en Maven Central; probablemente dependen de repositorio privado/local.

### Dependencias con actualizacion estable o accion recomendada

| Artefacto | Actual | Ultima en Maven Central | Accion |
|---|---:|---:|---|
| `com.fasterxml.jackson.core:jackson-databind` | 2.21.2 | 2.22.1 | Actualizable tras tests de serializacion/deserializacion. |
| `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` | 2.21.2 | 2.22.1 | Actualizar junto con Jackson core. |
| `org.jsoup:jsoup` | 1.22.1 | 1.23.1 | Actualizable con tests de parseo HTML/XML usados. |
| `com.zaxxer:HikariCP` | 7.0.2 | 7.1.0 | Actualizable; revisar compatibilidad con Java minimo requerido. |
| `org.mariadb.jdbc:mariadb-java-client` | 3.5.8 | 3.5.10 | Actualizable; probar conexion y batch insert. |
| `org.postgresql:postgresql` | 42.7.10 | 42.7.13 | Actualizable si PostgreSQL sigue siendo soporte real. Si no se usa, evaluar eliminar. |
| `org.junit.jupiter:junit-jupiter` | 6.0.3 | 6.1.3 | Actualizable; ademas eliminar propiedad obsoleta `junit-jupiter.version=5.12.2` o usarla. |
| `org.apache.commons:commons-collections4` | 4.5.0 | 4.6.0 | Actualizable con tests. |
| `commons-codec:commons-codec` | 1.21.0 | 1.22.1 | Actualizable con tests. |
| `org.projectlombok:lombok` | 1.18.44 | 1.18.46 | Actualizable; validar compilacion con JDK 21. |
| `com.github.spotbugs:spotbugs-maven-plugin` | 4.9.8.3 | 4.10.3.0 | Actualizable; ejecutar `spotbugs:check`. |
| `org.owasp:dependency-check-maven` | 12.2.0 | 13.0.0 | Actualizable; revisar cambios de configuracion y cache NVD. |
| `com.diffplug.spotless:spotless-maven-plugin` | 3.4.0 | 3.9.0 | Actualizable; ejecutar `spotless:check`. |
| `org.apache.maven.plugins:maven-dependency-plugin` | 3.10.0 | 3.11.0 | Actualizable. |
| `org.apache.maven.plugins:maven-enforcer-plugin` | 3.6.2 | 3.6.3 | Actualizable. |

### Dependencias actualmente al dia segun Maven Central

| Artefacto | Version |
|---|---:|
| `org.apache.poi:poi` | 5.5.1 |
| `org.apache.poi:poi-ooxml` | 5.5.1 |
| `jakarta.annotation:jakarta.annotation-api` | 3.0.0 |
| `com.sun.mail:jakarta.mail` | 2.0.2 |
| `org.apache.commons:commons-text` | 1.15.0 |
| `org.reflections:reflections` | 0.10.2 |
| `org.apache.maven.plugins:maven-javadoc-plugin` | 3.12.0 |
| `org.apache.maven.plugins:maven-release-plugin` | 3.3.1 |
| `org.apache.maven.plugins:maven-shade-plugin` | 3.6.2 |
| `org.codehaus.mojo:versions-maven-plugin` | 2.21.0 |
| `org.codehaus.mojo:build-helper-maven-plugin` | 3.6.1 |

### Versiones con cautela

| Artefacto | Actual | Central | Motivo de cautela |
|---|---:|---:|---|
| `org.hibernate.orm:hibernate-core` | 7.3.0.Final | 8.0.0.Beta1 | La ultima reportada es beta. No migrar a 8 beta en produccion; mantenerse en 7.3.x salvo necesidad concreta. |
| `org.hibernate.orm:hibernate-hikaricp` | 7.3.0.Final | 8.0.0.Beta1 | Igual que Hibernate core. |
| `ch.qos.logback:logback-classic` | 1.5.32 | 1.6.2 | Verificar compatibilidad con Java/Jakarta antes de subir major/minor. |
| `org.assertj:assertj-core` | 3.27.7 | 4.0.0-M1 | Ultima es milestone; no actualizar salvo pruebas. |
| `org.apache.logging.log4j:log4j-to-slf4j` | 2.25.4 | 3.0.0-beta2 | Ultima es beta; no usar en estable. |
| `org.slf4j:slf4j-api` | 2.0.17 | 2.1.0-alpha1 | Ultima es alpha; mantener 2.0.x estable. |
| `jakarta.persistence:jakarta.persistence-api` | 3.2.0 | 4.0.0-M6 | Ultima es milestone; no actualizar sin migracion Jakarta/Hibernate planificada. |
| `jakarta.validation:jakarta.validation-api` | 3.1.1 | 4.0.0-M1 | Ultima es milestone; mantener estable. |
| `com.sun.xml.bind:jaxb-core` | 2.3.0.1 | 4.0.9 | Migracion mayor de `javax`/JAXB; requiere revisar clases generadas CODICE. |
| `com.sun.xml.bind:jaxb-impl` | 2.3.1 | 4.0.9 | Igual que JAXB core. |
| `javax.xml.bind:jaxb-api` | 2.4.0-b180830.0359 | 2.4.0-b180830.0359 | Artefacto antiguo `javax`; evaluar migracion o documentar dependencia legacy. |
| `javax.activation:javax.activation-api` | 1.2.0 | 1.2.0 | Legacy `javax`; evaluar si sigue siendo necesaria. |

### Artefactos no verificables en Maven Central

| Artefacto | Version actual | Accion |
|---|---:|---|
| `org.w3._2005.atom:atom` | 1.0 | Documentar origen/repositorio local. |
| `place.codice:codice` | 2.8.0 | Documentar origen; comparar con jars en `lib`. |
| `place.codice:codice-ext` | 1.5.0 | El repo contiene jars `1.3/1.4`; falta verificar disponibilidad de `1.5.0`. |
| `local.jarios:encrypt-helper` | 5.2.0 | Verificar repositorio privado/local. |
| `local.jarios:version-helper` | 5.3.0 | Verificar repositorio privado/local. |
| `local.jarios:email-helper` | 5.2.0 | Verificar repositorio privado/local. |
| `local.jarios:properties-helper` | 5.2.0 | Verificar repositorio privado/local. |

## Java y Maven

| Herramienta | Estado actual | Version reciente comprobada | Recomendacion |
|---|---|---|---|
| Java local | Por defecto `1.8.0_501`; disponible `C:\java\software\jdk-21.0.11` | Oracle lista JDK 26 como ultima feature release y JDK 25 como LTS actual. | Usar JDK 21.0.11 como entorno inmediato del proyecto. Planificar salto a JDK 25 LTS cuando dependencias/CI esten estabilizadas. |
| Maven local | Disponible `C:\java\software\apache-maven-3.9.16`; no expuesto en `PATH` por defecto | Apache Maven 3.9.16 aparece como release 3.9.x reciente; Maven 3.10.0-rc-1 es release candidate. | Anadir Maven Wrapper fijado a Maven 3.9.16 o script de entorno; actualizar `maven.minimum.version` desde 3.6.3. |

Fuentes consultadas:

- Maven Central metadata: `https://repo.maven.apache.org/maven2/`
- Apache Maven Releases History: `https://maven.apache.org/docs/history.html`
- Oracle Java Downloads: `https://www.oracle.com/java/technologies/downloads/`
- Oracle JDK 26 release note: `https://docs.oracle.com/iaas/releasenotes/java-management/jdk-26-release-note.htm`

## Backlog tecnico priorizado

### Prioridad critica

1. Fijar JDK 21.0.11 y Maven 3.9.16 como entorno real de desarrollo/CI.
2. Anadir Maven Wrapper o script de entorno y usarlo como entrada reproducible de build.
3. Rotar y externalizar `nvdApiKey`.
4. Parametrizar la consulta de historico y eliminar HQL libre del repositorio.

### Prioridad alta

1. Cambiar la estrategia de comparacion historica para no cargar toda la tabla `Entry`.
2. Activar Dependency Check en CI/perfil controlado.
3. Validar y corregir `FiltroFechasEvaluator`.
4. Actualizar dependencias estables de bajo riesgo: Jackson, jsoup, drivers JDBC, Lombok, Commons Codec, Commons Collections.
5. Alinear JUnit: usar propiedad `junit-jupiter.version` o eliminarla.

### Prioridad media

1. Reintroducir Checkstyle o decidir que Spotless es la fuente de formato y hacerlo cumplir.
2. Integrar SpotBugs/FindSecBugs en perfil de calidad.
3. Crear scripts SQL versionados para indices principales.
4. Revisar transacciones parciales de importacion.
5. Ampliar tests sobre el core ahora que `System.exit` queda limitado a los `main`.
6. Actualizar README con versiones reales y perfiles Maven.

### Prioridad baja

1. Revisar artefactos `.iml` duplicados y `dependency-reduced-pom.xml` versionado.
2. Limpiar `_old` o documentar por que se mantiene.
3. Reducir logs por fila en importaciones grandes.

## Comandos de seguimiento

Cuando el entorno este preparado:

```powershell
.\mvnw.cmd -version
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
.\mvnw.cmd versions:display-dependency-updates
.\mvnw.cmd versions:display-plugin-updates
.\mvnw.cmd org.owasp:dependency-check-maven:check
.\mvnw.cmd spotless:check
.\mvnw.cmd spotbugs:check
```
Alternativa mientras no exista Maven Wrapper:

```powershell
$env:JAVA_HOME='C:\java\software\jdk-21.0.11'
$env:Path="$env:JAVA_HOME\bin;C:\java\software\apache-maven-3.9.16\bin;$env:Path"
mvn test
```


## Ejecuciones informativas 2026-08-12

| Comando | Resultado | Accion derivada |
|---|---|---|
| `scripts/use-java21-maven3916.ps1 spotless:check` | Falla: 173 de 175 ficheros Java necesitan formato; 2 ya estaban limpios. No se aplico `spotless:apply`. | Decidir si se acepta un cambio masivo de formato separado. |
| `scripts/use-java21-maven3916.ps1 spotbugs:check` | Inicialmente fallaba con 6 bugs medios. Tras validar rutas/URLs y acotar exclusiones justificadas, queda limpio: 0 bugs, 0 errores. | SpotBugs ya puede considerarse candidato a gate, pendiente decidir fase Maven/CI. |
| `scripts/use-java21-maven3916.ps1 versions:display-dependency-updates versions:display-plugin-updates` | Correcto. Lista actualizaciones de dependencias/plugins; varias ultimas son beta/alpha/milestone y no deben aplicarse automaticamente. | Mantener actualizaciones por tandas pequenas con tests. |
| `scripts/use-java21-maven3916.ps1 org.owasp:dependency-check-maven:check` | No ejecutado en esta sesion: requiere `NVD_API_KEY` en entorno y consulta externa a NVD. | Ejecutar con la misma operativa que `import-from-gc`: clave en entorno/CI, no versionada. |

## Patron aplicado para Dependency Check/NVD

Alineado con `import-from-gc`:

- No commitear nunca la clave NVD; usar `NVD_API_KEY` como variable de entorno local o secret de CI.
- Configurar `dependency-check-maven` en el `pom.xml`, sin enlazarlo al ciclo local rapido.
- Ejecutar bajo demanda con `org.owasp:dependency-check-maven:check`.
- Usar `failBuildOnCVSS=8.0` como umbral inicial.
- Desactivar OSS Index con `ossIndexAnalyzerEnabled=false`.
- Rotar cualquier clave que haya estado visible en el repositorio o en historicos compartidos.

## Historial de avances

| Fecha | Cambio | Evidencia |
|---|---|---|
| 2026-08-17 | Cubierta y validada la restriccion de Malaga LOCAL sobre BD no vacia. | Nuevo test `OpenDataMalagaLocalTest.local_import_rejects_non_empty_database_before_parsing`; `scripts/use-java21-maven3916.ps1 spotless:apply test`: 30 tests, 0 fallos; validacion real Malaga LOCAL contra `opendata-malaga` con `hibernate.hbm2ddl.auto=validate`: aborta antes de parsear con 19.040 entries existentes y recomienda usar INTERNET o vaciar BD antes de carga local. |
| 2026-08-17 | Cubierta y validada la restriccion de Pliegos LOCAL sobre BD no vacia. | Nuevo test `local_import_rejects_non_empty_database_before_parsing`; `scripts/use-java21-maven3916.ps1 test`: 29 tests, 0 fallos; validacion real Pliegos LOCAL contra `opendata-pliegos` con `hibernate.hbm2ddl.auto=validate`: aborta antes de parsear con 857.201 entries existentes y recomienda usar INTERNET o vaciar BD antes de carga local. |
| 2026-08-17 | Revisada coherencia de `estadistica` en Pliegos tras la limpieza de `historico`. | `opendata-pliegos.estadistica` ya mantiene `total_historicos` y `n_registros_historicos_insertar/actualizar/eliminar/rechazar` a `NULL` en sus 3 registros; no se modifica `n_entries` porque representa entries procesadas/persistidas, no filas de `historico`. |
| 2026-08-17 | Eliminados historicos antiguos de Pliegos y validada una carga INTERNET real posterior sin reinsertarlos. | `opendata-pliegos.historico` pasa de 104.867 a 0 filas mediante `TRUNCATE TABLE historico`; ejecucion Pliegos INTERNET real con `hibernate.hbm2ddl.auto=validate`: 857.201 entries existentes, 1 feed remoto, 0 entries nuevas, 2 deleted entries, `historicosGenerados=0`, `historicosPersistidos=0`; BD final: `historico=0`, `log=3`, `feed=6.344`, `entry=857.201`, `deleted_entry=67.014`, `estadistica=3`. |
| 2026-08-17 | Validado Pliegos LOCAL e INTERNET de expedientes `MAYORES` tras eliminar historicos persistidos en Pliegos. | Esquema aislado `opendata_pliegos_validation` porque `opendata-pliegos` contiene datos reales; LOCAL con 2 feeds de muestra desde `D:\placsp\may`: 970 entries, 3 deleted entries, `historico=0`; INTERNET con `hibernate.hbm2ddl.auto=validate` y `-Djavax.net.ssl.trustStoreType=Windows-ROOT`: 1 feed remoto, 0 entries nuevas por `newestEntry`, 2 deleted entries, `historico=0`; BD final: `entry=970`, `feed=3`, `deleted_entry=5`, `historico=0`, `estadistica=2`, `log=2`; `scripts/use-java21-maven3916.ps1 test`: 28 tests, 0 fallos. |
| 2026-08-17 | Rama `jarp/pliegos-persistencia-sin-historicos`: Pliegos deja de persistir historicos y conserva reemplazo de entries mediante `replacementEntryIds`. | `ImportPersistencePlan` separa `replacementEntryIds` de `historicoList`; `RepositoryImpl.persistirImportacion` borra entries existentes por ese conjunto explicito; `AbstractOpenDataPliegos` vacia historicos antes del plan. Validacion: `spotless:apply test spotbugs:check`, 28 tests, 0 fallos, SpotBugs 0. |
| 2026-08-17 | Validada carga Pliegos INTERNET de expedientes `MAYORES` contra `opendata-pliegos`. | `run-6` con Java 21.0.11, Maven 3.9.16 y `-Djavax.net.ssl.trustStoreType=Windows-ROOT`: 519 feeds parseados, 258.828 entries leidas, 104.867 entries validas, 661 deleted entries; 39.749 `ACTUALIZAR` eliminados antes de insertar; final correcto sin `Duplicate entry`, `Request Rejected` ni PKIX. BD final: log=2, feed=6.343, entry=857.201, deleted_entry=67.012, historico=104.867, estadistica=2; nuevo log `2cba07f1-5e37-4b25-be76-6a55771ed51d`. |
| 2026-08-17 | Validada carga Málaga INTERNET de expedientes `MAYORES` contra `opendata-malaga`. | Log `36b5718e-516d-4ec6-b62e-c56195064761`: final correcto; 538 feeds, 688 deleted entries, 267.689 históricos `RECHAZAR` omitidos de persistencia; BD final: 65.366 entries, 11.911 feeds, 5.520.920 históricos. |
| 2026-08-17 | Añadida cobertura de integracion para la persistencia incremental y el rollback transaccional. | `RepositoryImplPersistenceIntegrationTest`: verifica que `ACTUALIZAR` elimina el `entry` previo antes de insertar la version nueva sin violar el indice unico y que un duplicado en persistencia revierte `log`, `feed`, `entry` y `estadistica`; `spotless:apply test spotbugs:check`: 26 tests, 0 fallos, SpotBugs 0 bugs. |
| 2026-08-17 | Validada carga Pliegos LOCAL en BD temporal vacía con muestra local de 2 ATOM. | `opendata_pliegos_local_test`: 2 feeds, 816 entries, 1 deleted entry; sin duplicados en memoria y sin persistencia parcial fuera de transacción. |
| 2026-08-17 | Detectado y corregido fallo de Pliegos INTERNET al crear históricos de entries existentes: `LazyInitializationException` por acceder a `entry.feed.miLog` fuera de sesión. | `Historico` ya no copia `miLog` desde el `Feed`; `RepositoryImpl.persistirImportacion` sigue asignando el log transaccional. `mvn`: 23 tests, 0 fallos; SpotBugs: 0 bugs. |
| 2026-08-17 | Pliegos INTERNET avanza tras la corrección hasta el feed 229, pero queda bloqueado por protección/rate limit del servidor remoto: respuesta `200 text/html` con `Request Rejected` para URLs `.atom`. | BD `opendata-pliegos` sin cambios parciales: 1 log, 792.083 entries, 5.824 feeds, 0 históricos. Añadidos `app.http.max_retries`, `app.http.retry_delay_ms` y `app.http.request_delay_ms` para reintentos y throttling; configuración de auditoría de Pliegos INTERNET: 5 reintentos, 120.000 ms de pausa base, 2.500 ms entre peticiones. |
| 2026-08-12 | Ajustada semantica LOCAL: no compara contra BD, exige BD vacia para el tipo de sindicacion y mantiene control de duplicados en memoria por `entry_id`. | `mvn test`: 23 tests, 0 failures, 0 errors; `spotbugs:check`: 0 bugs, 0 errors; Pliegos LOCAL contra BD no vacia aborta antes de parsear con 792.083 entries existentes. |
| 2026-08-12 | Cerrado R15: `app.persistir_historicos_rechazados=false` evita persistir historicos `RECHAZAR` masivos por defecto y conserva los totales en estadistica; los `.properties` documentan valores admitidos. | `mvn test`: 20 tests, 0 failures, 0 errors; `spotbugs:check`: 0 bugs, 0 errors; Malaga local real 20:40: `historico=0`, `feeds=117`, `deleted_entries=155`, `total_historicos=57.864`. |
| 2026-08-12 | Avance H05/R13: reducidos logs por fila de persistencia a `DEBUG` y anadido `flush` por lote en `RepositoryImpl.persistFeedsInCurrentTransaction` usando `hibernate.jdbc.batch_size`. | `mvn test`: 17 tests, 0 failures, 0 errors; `spotless:check`: success; `spotbugs:check`: 0 bugs, 0 errors. |
| 2026-08-12 | Avance H07/R10: aplicados indices de importacion incremental en `opendata-malaga` y `opendata-pliegos`; alineado esquema Malaga con JPA renombrando columnas antiguas. | MariaDB 12.2.2; `ANALYZE TABLE`: OK; `EXPLAIN` snapshots usa `idx_*_log_tipo_id`; schema `validate` supera la fase Hibernate. |
| 2026-08-12 | Avance H05/R13: `FeedHelper` deja de emitir `INFO` por feed parseado y conserva resumen agregado; las variantes locales cargan snapshots y cortan por `newestEntry` igual que internet. | `mvn test`: 17 tests, 0 failures, 0 errors; `spotless:check`: success; `spotbugs:check`: 0 bugs, 0 errors; Malaga local real: 1m28s. |
| 2026-08-12 | Avance H05/R13: el preview de Malaga/Pliegos deja de emitir un `INFO` por feed, entry y deleted entry; el detalle queda disponible en `DEBUG` y `INFO` conserva el resumen. | `mvn test`: 17 tests, 0 failures, 0 errors; `spotless:check`: success; `spotbugs:check`: 0 bugs, 0 errors. |
| 2026-08-12 | Creacion de auditoria viva y primera revision de versiones. | Este documento. |
| 2026-08-12 | Verificado entorno local alternativo con JDK 21.0.11 y Maven 3.9.16. | `mvn test`: 8 tests, 0 failures, 0 errors, build success. |
| 2026-08-12 | Cerrado H01 con script reproducible de entorno. | `scripts/use-java21-maven3916.ps1 test`: 8 tests, 0 failures, 0 errors, build success. |
| 2026-08-12 | Avance H02/H03: NVD API key externalizada y Dependency Check configurado al estilo de `import-from-gc`. | `pom.xml` sin clave hardcodeada; `mvn test` sigue en verde. |
| 2026-08-12 | Ejecutados checks informativos no destructivos. | Spotless: 173 ficheros a formatear; SpotBugs: 6 medios; Versions: correcto; Dependency Check: bloqueado por autorizacion de credencial. |
| 2026-08-12 | Añadidos tests de caracterizacion para `FiltroFechasEvaluator`. | `mvn test`: 10 tests, 0 failures, 0 errors. |
| 2026-08-12 | Cerrado H06 corrigiendo `FiltroFechasEvaluator` a rango inclusivo. | `mvn test`: 13 tests, 0 failures, 0 errors. |
| 2026-08-12 | Cerrado H04 parametrizando `getMapEntries`. | `mvn test`: 13 tests, 0 failures, 0 errors. |
| 2026-08-12 | Resueltos avisos SpotBugs de rutas/URLs. | `spotbugs:check`: 0 bugs, 0 errors; `mvn test`: 13 tests, 0 failures, 0 errors. |
| 2026-08-12 | Alineado JUnit a property unica `junit-jupiter.version=6.1.3`. | `mvn test`: 13 tests, 0 failures, 0 errors. |
| 2026-08-12 | Aplicado formateo Spotless al codigo Java y cerrado H09 para Spotless/SpotBugs. | `spotless:check`: build success; `spotbugs:check`: 0 bugs, 0 errors; `mvn test`: 13 tests, 0 failures, 0 errors. |
| 2026-08-12 | Ajustado Dependency Check al patron real de otros proyectos: goal Maven explicito, `NVD_API_KEY` por entorno, CVSS 8.0 y OSS Index desactivado. | `mvn test`: 13 tests, 0 failures, 0 errors; `spotbugs:check`: 0 bugs, 0 errors; pendiente ejecucion real con clave NVD. |
| 2026-08-12 | Avance H05/R06: sustituida la carga historica de entidades `Entry` completas por snapshots `entryId/updated` para comparacion incremental. | `mvn test`: 16 tests, 0 failures, 0 errors; `spotless:check`: success; `spotbugs:check`: 0 bugs, 0 errors. |
| 2026-08-12 | Revisado H07/R10: ya existen scripts SQL de indices para Malaga y Pliegos con `ANALYZE TABLE` y plan EXPLAIN. | `opendata-malaga-indices.sql`, `opendata-pliegos-indices.sql`, `docs/plan_adecuacion_indices.md`; pendiente ejecucion en entorno real. |
| 2026-08-12 | Creado CI ejecutable en `.github/workflows/maven-ci.yml` con tests, Spotless y SpotBugs; configurada accion local para GitHub Packages privados. | `mvn test`: 16 tests, 0 failures, 0 errors; `spotless:check`: success; `spotbugs:check`: 0 bugs, 0 errors. |
| 2026-08-12 | Cerrados R11/R12: SQL DEBUG/TRACE desactivado por defecto y `System.exit` limitado a los `main`. | `logback.xml`; `AbstractOpenDataBase.procesar`; mains Malaga/Pliegos. |
| 2026-08-12 | Añadida plantilla de validacion de indices para cuando las BD esten cargadas. | `docs/auditorias/plantilla_validacion_indices.md`. |
| 2026-08-12 | Cerrado H08/R08 a nivel de codigo: persistencia de importacion completa mediante `ImportPersistencePlan` y una unica transaccion de repositorio. | `mvn test`: 17 tests, 0 failures, 0 errors; `spotless:check`: success; `spotbugs:check`: 0 bugs, 0 errors. |

