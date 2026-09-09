# Resumen ejecutivo

Auditoria realizada sobre el estado actual del workspace el 2026-05-09. El proyecto es Java 21 con Hibernate nativo, JPA annotations, MariaDB, HikariCP y un repositorio propio basado en `SessionFactory`; no se detecta Spring Data ni controladores REST en `src/main/java`.

El riesgo principal para produccion no esta en un unico N+1 clasico de controlador, sino en la combinacion de: `hibernate.hbm2ddl.auto=create`, credenciales en properties, carga completa de historico de `Entry` en memoria, HQL construido como texto externo, transacciones manuales largas para importaciones, ausencia casi total de indices en claves foraneas/columnas consultadas y entidades con muchos grafos `@OneToMany` persistidos en cascada o manualmente.

No se han aplicado cambios automaticos de codigo. Hay muchas modificaciones previas en el working tree y los cambios de mas valor afectan configuracion de produccion, indices y contratos de repositorio; se dejan priorizados para revision. Se creo este informe en `docs/auditorias/auditoria_java_hibernate_mariadb_2026-05-09.md`.

# Que se ha solicitado

Auditoria profunda de rendimiento, estabilidad y calidad de un backend Java 21 con Hibernate/JPA y MariaDB, orientada a preparacion para produccion. El alcance solicitado incluye entidades, relaciones, consultas, repositorios, servicios, transacciones, batch, memoria, seguridad SQL, configuracion Hibernate/HikariCP, indices, pruebas y plan priorizado.

# Alcance analizado

- `pom.xml`: dependencias, plugins, perfiles y version Java.
- `properties/hibernate.properties` y `properties/bd.properties`: Hibernate, HikariCP, JDBC MariaDB.
- `src/main/resources/logback.xml`: SQL/logging.
- `src/main/java/local/jarios/database`: `SessionFactoryProvider`, `SessionFactoryRegistry`, `HibernateConfigurer`, `EntityScanner`.
- `src/main/java/local/jarios/repositories`: `Repository`, `RepositoryImpl`, `TransactionManager`.
- `src/main/java/local/jarios/services`: `ServicePrincipal`, `ServicePrincipalImpl`.
- `src/main/java/local/jarios/entity`: 32 entidades JPA.
- `src/main/java/local/jarios/variants`, `core`, `helpers`, `filtro`, `mappers`: flujo de parseo, filtrado, persistencia y memoria.
- `src/test/java/local/jarios/entity`: tests de convenciones JPA/mappers.

No se pudo ejecutar `mvn test` porque `mvn` no esta disponible en PATH. Se revisaron reportes previos en `target/surefire-reports`: ultima ejecucion del 2026-04-11 con 8 tests, 0 failures, 0 errors.

# Riesgos criticos

1. `properties/hibernate.properties:69` usa `hibernate.hbm2ddl.auto=create`. En produccion destruye y recrea esquema/datos al construir la `SessionFactory`.
2. `properties/bd.properties:11-12` contiene usuario `root` y password en claro. Riesgo operativo y de seguridad.
3. `ServicePrincipalImpl.java:112-118` construye HQL por interpolacion de texto y `RepositoryImpl.java:218-223` acepta cualquier HQL externo. Aunque hoy el valor viene de enum, el contrato del repositorio permite inyeccion HQL si se reutiliza.
4. `RepositoryImpl.java:221-229` carga todas las `Entry` de un tipo de sindicacion sin paginacion ni proyeccion. En produccion crecera linealmente con el historico y puede agotar memoria.
5. Solo se detecta un `@Index` en 32 entidades (`Entry.entry_id`). Las FKs usadas en `JOIN`, `WHERE` y cascadas de borrado no estan indexadas de forma explicita.
6. `FiltroFechasEvaluator.java:37-39` parece tener la condicion invertida: exige `fechaEntry.isBefore(fechaInicio)` y `fechaEntry.isAfter(fechaFin)`, normalmente imposible si inicio <= fin. Esto puede rechazar todos los registros validos.

# Problemas Hibernate / JPA

## P1. DDL destructivo activo

- Severidad: critica
- Archivo: `properties/hibernate.properties:69`
- Componente afectado: configuracion Hibernate
- Codigo actual:

```properties
hibernate.hbm2ddl.auto=create
```

- Descripcion: Hibernate recrea el esquema al iniciar.
- Riesgo: perdida total de datos en produccion y locks DDL durante arranque.
- Recomendacion: usar `validate` en produccion; gestionar cambios con migraciones SQL revisadas. Mantener `create` solo en perfil local aislado.
- Impacto esperado: elimina el riesgo de borrado accidental y fuerza compatibilidad esquema-entidades.
- Riesgo del cambio: medio; si el esquema actual no coincide, el arranque fallara, que es deseable antes de produccion.
- Estado: pendiente, requiere revision manual de entorno.

## P2. Carga completa de `Entry` sin paginacion ni proyeccion

- Severidad: alta
- Archivo: `RepositoryImpl.java:218-229`; llamado desde `ServicePrincipalImpl.java:111-121`; variantes `OpenDataMalagaInternet.java:43-58` y `OpenDataPliegosInternet.java:43-58`
- Entidad/repositorio/servicio afectado: `Entry`, `Feed`, `Log`, `RepositoryImpl.getMapEntries`, `ServicePrincipalImpl.getMapEntries`
- Codigo actual:

```java
TypedQuery<Entry> query = session.createQuery(sql, Entry.class);
List<Entry> listEntries = query.getResultList();
for (Entry entry : listEntries) {
  mapEntries.put(entry.getEntryId(), entry);
}
```

- Descripcion: se recuperan todas las entradas historicas del tipo de sindicacion y se retienen en un `HashMap`.
- Riesgo: consumo de heap elevado, GC prolongado, transaccion de lectura larga, bloqueo de conexion Hikari, arranque lento de importaciones.
- Recomendacion: sustituir por consulta paginada/streaming o proyeccion minima `entryId, updated` y datos requeridos por comparacion. Si solo se necesita newest, consultar `max(updated)` o `order by updated desc limit 1`.
- Impacto esperado: reduccion importante de memoria y tiempo de lectura inicial.
- Riesgo del cambio: medio; cambia el contrato interno porque hoy se devuelve `Map<String, Entry>`.
- Estado: pendiente.

## P3. HQL como string externo e interpolado

- Severidad: alta
- Archivo: `ServicePrincipalImpl.java:112-118`, `RepositoryImpl.java:218-223`
- Componente: `ServicePrincipalImpl`, `RepositoryImpl`
- Codigo actual:

```java
String sql = String.format(
    "SELECT e FROM Entry e JOIN e.feed f JOIN f.miLog l WHERE l.tipoSindicacion = '%s'",
    tipoSindicacion
);
return repository.getMapEntries(sql);
```

- Descripcion: el servicio crea HQL con concatenacion/interpolacion y el repositorio acepta un string arbitrario.
- Riesgo: inyeccion HQL por futuras llamadas, cache de query menos efectiva, errores por quoting de enums, dificil de testear.
- Recomendacion: mover la query al repositorio con parametro bind:

```java
session.createQuery("""
    SELECT e FROM Entry e
    JOIN e.feed f
    JOIN f.miLog l
    WHERE l.tipoSindicacion = :tipo
    """, Entry.class).setParameter("tipo", tipoSindicacion)
```

- Impacto esperado: seguridad y estabilidad de query.
- Riesgo del cambio: bajo-medio; requiere cambiar interfaz `Repository.getMapEntries`.
- Estado: pendiente.

## P4. Relaciones `Entry` -> hijos sin cascade, persistencia manual parcial

- Severidad: media
- Archivo: `Entry.java:82-86`, `RepositoryImpl.java:189-205`
- Entidades afectadas: `Entry`, `ContractFolderStatus`, `PreliminaryMarketConsultationStatus`
- Codigo actual:

```java
@OneToMany(mappedBy = "entry", orphanRemoval = true, fetch = FetchType.LAZY)
private List<ContractFolderStatus> contractFolderStatusList = new ArrayList<>();
```

```java
session.persist(entry);
for (ContractFolderStatus cfs : entry.getContractFolderStatusList()) {
  cfs.setEntry(entry);
  session.persist(cfs);
}
```

- Descripcion: al no haber cascade en `Entry`, el repositorio debe persistir manualmente los dos tipos de hijo. Si se anade otro hijo o se olvida setear el padre, se pierden datos.
- Riesgo: inconsistencias parciales y errores `TransientObjectException`.
- Recomendacion: decidir explicitamente una politica. Para importacion agregada, `cascade = CascadeType.ALL` en `Entry` podria ser coherente, pero debe revisarse con borrados. Alternativa: encapsular setters bidireccionales y tests de persistencia.
- Impacto esperado: menor fragilidad del grafo.
- Riesgo del cambio: alto si se toca cascade/orphanRemoval; no aplicar sin revision.
- Estado: pendiente.

## P5. `flushAndClear` con entidades padre usadas despues

- Severidad: media
- Archivo: `RepositoryImpl.java:175-208`
- Componente: persistencia de feeds
- Codigo actual:

```java
session.persist(feed);
...
flushAndClear(session);
for (Entry entry : entryList) {
  entry.setFeed(feed);
  session.persist(entry);
}
```

- Descripcion: se limpia el contexto tras persistir `Feed`; luego se usa el mismo objeto `feed` ya detached como padre de nuevas `Entry`.
- Riesgo: errores con entidad detached o FK no resuelta en escenarios de flush/cascade distintos; dificulta dirty checking y diagnostico.
- Recomendacion: retrasar el `clear` hasta finalizar el feed completo o usar referencia gestionada (`session.getReference(Feed.class, feed.getId())`) antes de asignar.
- Impacto esperado: persistencia mas predecible.
- Riesgo del cambio: bajo-medio; requiere test de integracion.
- Estado: pendiente.

## P6. Ausencia de `@Version`

- Severidad: media
- Archivo: todas las entidades JPA; busqueda sin resultados reales de `@Version`
- Entidades afectadas: especialmente `Entry`, `Log`, `Feed`, `ContractFolderStatus`, `Estadistica`
- Descripcion: no hay versionado optimista para updates concurrentes.
- Riesgo: lost updates si dos procesos importan o actualizan simultaneamente el mismo dato.
- Recomendacion: anadir `@Version` en entidades que se actualicen (`Entry`, `Estadistica`, quizas `Log`) o garantizar ejecucion single-instance con lock externo.
- Impacto esperado: deteccion de concurrencia.
- Riesgo del cambio: medio; cambia esquema.
- Estado: pendiente.

## P7. `equals/hashCode` no definidos en entidades usadas en `HashSet`

- Severidad: media
- Archivo: `OpenDataExecutionContext.java:88-90`, `Feed.java:25-30`, entidades con Lombok `@Getter/@Setter` sin `equals/hashCode`
- Entidad afectada: `Feed` en `Set<Feed>`
- Descripcion: `conjuntoFeedsFromAtoms` es `HashSet<Feed>`, pero `Feed` usa identidad de objeto. Dos feeds con mismo `linkSelf` no se deduplican.
- Riesgo: duplicados en memoria y persistencia; violaciones de integridad si luego se anaden unique constraints.
- Recomendacion: no implementar `equals/hashCode` con relaciones. Usar un `Map<String, Feed>` por clave natural (`linkSelf`) o implementar igualdad solo por clave inmutable natural, si existe.
- Impacto esperado: deduplicacion estable.
- Riesgo del cambio: medio; afecta comportamiento de colecciones.
- Estado: pendiente.

## P8. Relaciones `@OneToOne` inversas sin `fetch = LAZY`

- Severidad: baja/media
- Archivo: `ContractFolderStatus.java:124-127,162-177`, `PreliminaryMarketConsultationStatus.java:153-157`, `TenderingTerms.java:134-135`, `Log.java:56-60`
- Entidades afectadas: `ContractFolderStatus`, `PreliminaryMarketConsultationStatus`, `TenderingTerms`, `Log`
- Descripcion: en JPA, `@OneToOne` es EAGER por defecto cuando no se especifica. Las relaciones inversas pueden cargar mas grafo del necesario o requerir bytecode enhancement para lazy real.
- Riesgo: consultas adicionales y memoria al navegar entidades.
- Recomendacion: especificar `fetch = FetchType.LAZY` donde proceda y validar con SQL real. En one-to-one inverso, comprobar si Hibernate lo respeta sin enhancement.
- Impacto esperado: menos carga incidental.
- Riesgo del cambio: bajo-medio.
- Estado: pendiente.

# N+1 queries detectadas

No se detecta un endpoint REST que serialice entidades ni bucles de repositorio consultando por cada fila. Si aparece N+1, el foco real esta en estos puntos:

- `Entry.getIdPlataformaFromEntry` y `Entry.getNifFromEntry` (`Entry.java:93-134`) recorren colecciones LAZY (`contractFolderStatusList`, `preliminaryMarketConsultationStatusList`). Si se invocan sobre entidades leidas desde BD fuera de la transaccion de `RepositoryImpl.getMapEntries`, producirian `LazyInitializationException`; dentro de sesion, producirian N+1 por cada `Entry`.
- `FiltroNifsEvaluator.java:33-38` y `FiltroCodigosPostalesEvaluator.java:26-31` llaman a `entry.getNifFromEntry(...)`. Hoy se aplican sobre entradas parseadas desde XML, no sobre entidades cargadas por Hibernate, por lo que no hay N+1 observado en ese flujo. El riesgo queda si se reutilizan con entradas de BD.
- `RepositoryImpl.getMapEntries` hace `JOIN e.feed f JOIN f.miLog l` para filtrar, pero no usa `JOIN FETCH`; si despues se navega `entry.feed` o hijos fuera de la transaccion, habra lazy loading roto.

# Relaciones problematicas

- `Log.feedList`, `Log.historicoList`, `Log.organoContratacionList` (`Log.java:63-70`): colecciones grandes LAZY con `orphanRemoval=true` sin cascade. No deben cargarse completas para borrados o reportes.
- `ContractFolderStatus` (`ContractFolderStatus.java:124-193`): grafo con muchas relaciones hijas y `cascade = CascadeType.ALL`, `orphanRemoval = true`. Es correcto para agregado de importacion, pero peligroso para operaciones de merge/delete masivas.
- `AdditionalPublicationStatus.java:75,81` y `ClassificationScheme.java:87`: `@OneToMany` sin `fetch = FetchType.LAZY` explicito. En JPA el default de `OneToMany` ya es LAZY, pero conviene declararlo por legibilidad y convencion.
- `PreliminaryMarketConsultationStatus.java:139-140`: relacion comentada. Riesgo de modelo incompleto si el XML trae attachments y no se persisten.
- `DocumentReference` concentra multiples padres opcionales (`DocumentReference.java:81-126` segun busqueda). Requiere constraints/checks para evitar que una fila apunte a multiples padres incompatibles.

# Consultas SQL / JPQL / Native mejorables

## Q1. Query principal de historico

- Archivo: `ServicePrincipalImpl.java:112-118`
- Query actual:

```sql
SELECT e FROM Entry e
JOIN e.feed f
JOIN f.miLog l
WHERE l.tipoSindicacion = '<enum>'
```

- Problemas: sin bind parameter, sin paginacion, devuelve entidad completa, no ordena, no limita, depende de indices ausentes en `feed.log_id`, `entry.feed_id`, `log.tipo_sindicacion`.
- Recomendacion: parametrizar y plantear dos queries:
  - Para corte incremental: `select e.entryId, e.updated from Entry e join e.feed f join f.miLog l where l.tipoSindicacion = :tipo`.
  - Para newest: `order by e.updated desc fetch first 1 row only` o `setMaxResults(1)`.

## Q2. Falta de consultas de existencia

- Archivo: `EntryProcessor.java:121-140`, `OpenDataMalagaInternet.java:43-58`
- Descripcion: para saber si una entrada existe se carga todo el historico en `mapEntriesFromBaseDatos`.
- Recomendacion: si el volumen crece, reemplazar por carga paginada de claves o por `existsByEntryId`/bulk query por lotes de ids parseados.

## Q3. Conteos en memoria

- Archivo: `AbstractOpenDataMalaga.java:141-161`
- Descripcion: agrupa `listHistoricos` en memoria. Es aceptable mientras la lista se genera en memoria por el parseo, pero si historicos se recuperan de BD deberia ir a SQL.

# Problemas de transacciones

## T1. Transacciones de lectura abiertas para carga completa

- Severidad: alta
- Archivo: `RepositoryImpl.java:221-232`
- Descripcion: `getMapEntries` abre transaccion y mantiene conexion hasta materializar toda la lista.
- Riesgo: conexiones retenidas, heap alto, tiempos de lock/undo innecesarios.
- Recomendacion: marcar modo read-only en Session/Query (`session.setDefaultReadOnly(true)`, `query.setReadOnly(true)`), usar `setFetchSize`, paginar o stream controlado.
- Estado: pendiente.

## T2. Importacion persistida en varias transacciones independientes

- Severidad: media
- Archivo: `AbstractOpenDataMalaga.java:180-212`, `AbstractOpenDataPliegos.java:96-110`
- Descripcion: se persisten `Log`, `Configuracion`, filtros, feeds, historicos y estadistica en llamadas separadas. Cada metodo abre su propia transaccion.
- Riesgo: importacion parcialmente persistida si falla en mitad; estadisticas inconsistentes; rollback global imposible.
- Recomendacion: introducir caso de uso transaccional unico para una importacion o registrar estado de importacion (`INICIADA`, `COMPLETADA`, `ERROR`) y permitir reintento idempotente.
- Estado: pendiente.

## T3. Batch parcial en `persistirSetFeeds`

- Severidad: media
- Archivo: `RepositoryImpl.java:144-215`
- Descripcion: hay `flushAndClear` por feed, pero no por numero de entries ni por tamano real del grafo. Un feed grande puede acumular muchas entidades antes de limpiar.
- Recomendacion: aplicar batch por contador global de entidades persistidas y revisar el clear para no dejar padres detached.
- Estado: pendiente.

## T4. Sin retry controlado ante deadlock

- Severidad: baja/media
- Archivo: `RepositoryImpl.java:252-279`
- Descripcion: todos los errores transaccionales se envuelven igual; no se diferencia deadlock/lock wait timeout de errores de datos.
- Recomendacion: detectar SQLState/codigo MariaDB de deadlock y aplicar retry limitado solo en operaciones idempotentes.
- Estado: pendiente.

# Problemas de MariaDB e indices

Solo se encontro un indice JPA explicito: `Entry.java:36-37` sobre `entry.entry_id`. Para MariaDB/InnoDB, los indices de FKs pueden ser creados automaticamente si no existen, pero no conviene depender del nombre/forma que el motor genere ni cubren columnas de filtro/ordenacion.

Indices recomendados:

| Prioridad | Tabla | Columnas | Tipo | Consulta afectada | Motivo | Impacto esperado | Riesgo | Validacion manual |
|---|---|---|---|---|---|---|---|---|
| Critica | `log` | `tipo_sindicacion`, `id` | BTREE compuesto | `WHERE l.tipo_sindicacion = :tipo` | Filtro raiz de carga historica | Menos scan de `log` y mejor join hacia `feed` | Bajo | Si |
| Critica | `feed` | `log_id`, `id` | BTREE compuesto | join `feed -> log` | Join principal y borrado cascada | Menos coste en join/cascade | Bajo | Si |
| Critica | `entry` | `feed_id`, `entry_id`, `updated` | BTREE compuesto | join `entry -> feed`, mapa por `entry_id`, newest | Cubre carga incremental y busqueda por feed | Alto en tablas grandes | Medio por tamano | Si |
| Alta | `entry` | `updated` | BTREE | `max(updated)` / `order by updated desc limit 1` | Obtener newest sin cargar todo | Alto | Bajo | Si |
| Alta | `contract_folder_status` | `entry_id` | BTREE | acceso a hijos de `Entry`, cascadas | Evita scans al resolver hijos/borrados | Medio | Bajo | Si |
| Alta | `preliminary_market_consultation_status` | `entry_id` | BTREE | acceso a hijos de `Entry`, cascadas | Evita scans al resolver hijos/borrados | Medio | Bajo | Si |
| Alta | `contract_folder_status` | `nif` | BTREE | filtros por NIF si se llevan a SQL | Permite filtrado por organo | Medio/alto | Bajo | Si |
| Alta | `preliminary_market_consultation_status` | `nif` | BTREE | filtros por NIF en consultas | Permite filtrado por organo | Medio/alto | Bajo | Si |
| Media | `contract_folder_status` | `id_plataforma` | BTREE | deduplicacion/filtros por plataforma | Búsqueda por identificador externo | Medio | Bajo | Si |
| Media | `preliminary_market_consultation_status` | `id_plataforma` | BTREE | deduplicacion/filtros por plataforma | Búsqueda por identificador externo | Medio | Bajo | Si |
| Media | `historico` | `log_id`, `opcion` | BTREE compuesto | estadisticas por importacion | Conteos por importacion si se consultan en SQL | Medio | Bajo | Si |
| Media | `nif` | `log_id`, `nif` | BTREE compuesto | filtros persistidos por importacion | Reportes/validacion | Medio | Bajo | Si |
| Media | `organo_contratacion` | `log_id`, `nif` | BTREE compuesto | filtros persistidos por importacion | Reportes por organo | Medio | Bajo | Si |

Constraints recomendadas:

- `feed.link_self`: considerar unique por importacion (`log_id`, `link_self`) si no se deben duplicar feeds.
- `entry.entry_id_corto`: ya tiene `unique=true` en `Entry.java:54`; falta indice explicito nombrado para control de DDL.
- `contract_folder_status.contract_folder_id`: evaluar indice/unique por `entry_id` si identifica expediente.

# Problemas de paginacion y carga masiva

- `RepositoryImpl.getMapEntries` no pagina.
- `OpenDataExecutionContext.java:84-92` mantiene en memoria `mapEntriesToBaseDatos`, `mapEntriesFromAtoms`, `mapDeletedEntriesFromAtoms`, `mapEntriesFromBaseDatos`, `listHistoricos` y `conjuntoFeedsFromAtoms`.
- `FeedHelper.java:57-109` procesa todos los feeds hasta superar `newestEntry`; correcto para corte incremental, pero cada feed se parsea completo y se ordenan sus entries en memoria (`EntryProcessor.java:46-64`).
- `AbstractOpenDataMalaga.java:100-124` y `AbstractOpenDataPliegos.java:47-71` hacen preview logueando feeds/entries; en volumen alto puede generar logs masivos.

Recomendacion: introducir limites configurables por ejecucion, paginacion de historico, procesamiento por lotes y modo de preview resumido.

# Problemas de memoria

- La mayor presion de memoria esta en la fase de comparacion: se cargan entradas de BD y entradas parseadas a la vez.
- `EntryProcessor.processEntries` ordena cada lista de entries con `.stream().sorted(...).toList()` (`EntryProcessor.java:46-64`). Si los feeds ya vienen ordenados por la fuente, evitar esta copia; si no, ordenar solo cuando el feed tenga mas de un elemento y se haya medido necesidad.
- `AbstractOpenDataBase.groupEntriesByFeed` agrupa todo `mapEntriesToBaseDatos` (`AbstractOpenDataBase.java:260-263`); util para persistencia, pero duplica referencias.
- `log.info` por cada `Entry`, `DeletedEntry`, `ContractFolderStatus` y `PMCS` en `RepositoryImpl.java:178-204` puede saturar I/O y retener strings en buffers.

# Configuracion Hibernate / HikariCP

Hallazgos:

- `hibernate.jdbc.batch_size=150` (`hibernate.properties:11`): positivo, pero falta `hibernate.jdbc.fetch_size` para lecturas grandes.
- `hibernate.order_inserts=true` y `hibernate.order_updates=true` (`hibernate.properties:13-15`): positivo.
- `hibernate.generate_statistics=true` (`hibernate.properties:34`): no recomendado siempre activo en produccion por overhead; usar bajo flag diagnostico.
- `hibernate.hikari.maximumPoolSize=30`, `minimumIdle=15` (`hibernate.properties:40-41`): alto para un proceso batch si MariaDB no esta dimensionado. Puede abrir demasiadas conexiones sin necesidad.
- `hibernate.hikari.leakDetectionThreshold=0` (`hibernate.properties:45`): desactivado; para preproduccion activar temporalmente, por ejemplo 60000 ms.
- `hibernate.show_sql=false` (`hibernate.properties:74`): correcto.
- `logback.xml:71-76`: `org.hibernate.SQL` en DEBUG y `org.hibernate.type.descriptor.sql` en TRACE. Aunque va a fichero, en produccion puede generar logs muy voluminosos y datos sensibles.
- No se ve `hibernate.dialect`. Hibernate puede autodetectar, pero en produccion conviene declarar dialecto MariaDB compatible con Hibernate 7.

Recomendaciones:

- Perfil produccion: `hbm2ddl=validate`, statistics off, SQL DEBUG/TRACE off, pool dimensionado al numero real de workers, `leakDetectionThreshold` temporal en preproduccion, timeouts revisados.
- Anadir `hibernate.jdbc.fetch_size` para lecturas paginadas/streaming.
- Externalizar credenciales via variables/secret manager.

# Seguridad SQL

- Riesgo principal: HQL interpolado y repositorio que acepta HQL libre (`ServicePrincipalImpl.java:112-118`, `RepositoryImpl.java:218-223`).
- No se detectan `createNativeQuery` ni SQL nativo directo.
- `bd.properties:11-12` expone usuario/password; ademas se usa `root`.
- `logback.xml:75-76` activa TRACE de tipos Hibernate, que puede exponer parametros/datos si la categoria aplica con Hibernate 7.
- `AbstractOpenDataBase.java:213-216` marca claves sensibles como solo `"password"`, correcto pero incompleto si hay `token`, `secret`, `apiKey`.

# APIs y DTOs

No se han encontrado controladores HTTP ni endpoints REST en el codigo analizado. No aplica exposicion directa de entidades por Jackson en APIs. Aun asi, existe `ManagerJackson` y logs de objetos; si en el futuro se exponen entidades, hay alto riesgo de serializacion circular por relaciones bidireccionales LAZY (`Log -> Feed -> Entry -> ContractFolderStatus...`).

Recomendacion si se anade API: usar DTOs/proyecciones, no entidades; paginar por defecto; no serializar relaciones LAZY; definir limites maximos de page size.

# Errores de estabilidad no estrictamente Hibernate

## S1. Filtro de fechas aparentemente invertido

- Severidad: alta
- Archivo: `FiltroFechasEvaluator.java:37-39`
- Codigo actual:

```java
return fechaEntry != null
    && fechaEntry.isBefore(fechaInicio)
    && fechaEntry.isAfter(fechaFin);
```

- Riesgo: si `fechaInicio` es anterior a `fechaFin`, ningun registro cumple. Esto puede provocar importaciones vacias y falsos rechazos.
- Recomendacion: validar intencion. Normalmente seria `!fechaEntry.isBefore(fechaInicio) && !fechaEntry.isAfter(fechaFin)`.
- Estado: pendiente; no aplicado por posible semantica historica de propiedades.

## S2. Ejecucion termina con `System.exit`

- Severidad: media
- Archivo: `AbstractOpenDataBase.java:373-375`
- Riesgo: dificulta tests, integracion como libreria/job gestionado y cierre controlado si se invoca embebido.
- Recomendacion: devolver codigo al `main` o encapsular salida solo en clases `main`.

## S3. Maven no disponible en PATH

- Severidad: media para CI local
- Evidencia: `mvn test` falla con "The term 'mvn' is not recognized".
- Recomendacion: incluir Maven Wrapper (`mvnw`) o documentar runtime Maven en CI/desarrollo.

# Cambios aplicados automaticamente

- Se creo la carpeta `docs/auditorias`.
- Se creo este informe: `docs/auditorias/auditoria_java_hibernate_mariadb_2026-05-09.md`.

No se aplicaron cambios en codigo fuente ni configuracion productiva. Motivo: los cambios de mayor impacto requieren validacion manual de esquema, contratos internos y entorno, y el working tree contiene muchas modificaciones previas no relacionadas que no deben tocarse sin coordinacion.

# Cambios recomendados pero no aplicados

1. Cambiar `hibernate.hbm2ddl.auto=create` a `validate` en configuracion de produccion.
2. Externalizar credenciales JDBC y dejar de usar `root`.
3. Parametrizar `getMapEntries` y eliminar HQL libre de la interfaz.
4. Sustituir carga completa de `Entry` por proyecciones/paginacion.
5. Anadir indices explicitos para FKs y columnas de filtrado.
6. Revisar transaccion unica o estado transaccional de importacion.
7. Ajustar logs SQL/TRACE para produccion.
8. Corregir/validar filtro de fechas.
9. Anadir tests de integracion con MariaDB/Testcontainers o entorno MariaDB dedicado.

# Pruebas recomendadas

- Test de repositorio `getMapEntries` con MariaDB: verifica query parametrizada, resultado por `TipoSindicacion`, plan con indices y ausencia de HQL injection.
- Test de volumen: 100k `Entry`, medir heap y tiempo de carga antes/despues de proyeccion/paginacion.
- Test de persistencia de grafo: `Feed -> Entry -> ContractFolderStatus -> TenderingTerms/ProcurementProject/...` validando que todos los hijos quedan persistidos.
- Test de rollback: fallo simulado durante `persistirSetFeeds` y durante `persistirListaHistoricos`; confirmar estado parcial o transaccion global.
- Test de filtros: `FiltroFechasEvaluator` con rango normal inicio <= fecha <= fin.
- Test de concurrencia: dos importaciones simultaneas con mismo `entry_id`; validar unique constraint, rollback y mensajes.
- Test de configuracion produccion: arranque con `hibernate.hbm2ddl.auto=validate`.
- Test de indices: `EXPLAIN` de la query principal sobre MariaDB real con volumen representativo.

# Checklist preproduccion

- [ ] `hibernate.hbm2ddl.auto=validate` en produccion.
- [ ] Usuario MariaDB no-root, privilegios minimos.
- [ ] Credenciales fuera del repositorio.
- [ ] SQL DEBUG/TRACE desactivado por defecto.
- [ ] Indices aplicados y validados con `EXPLAIN`.
- [ ] Query principal parametrizada.
- [ ] Carga historica paginada/proyectada.
- [ ] Pool Hikari dimensionado con MariaDB.
- [ ] Leak detection activado en preproduccion.
- [ ] Prueba de importacion completa con volumen productivo.
- [ ] Estrategia de rollback/reintento definida.
- [ ] Backup y migraciones probadas.
- [ ] Tests de filtros y persistencia de grafo en CI.

# Plan de actuacion priorizado

## 1. Acciones criticas antes de produccion

| Prioridad | Descripcion | Archivo/componente | Impacto esperado | Riesgo de no hacerlo | Esfuerzo | Automatizable | Revision manual |
|---|---|---|---|---|---|---|---|
| Critica | Cambiar DDL productivo a `validate` | `properties/hibernate.properties:69` | Evitar destruccion de datos | Perdida total de datos | Bajo | Si | Si |
| Critica | Externalizar credenciales y eliminar `root` | `properties/bd.properties:11-12` | Menor riesgo de compromiso | Acceso total a BD expuesto | Medio | Parcial | Si |
| Critica | Parametrizar query principal y cerrar HQL libre | `ServicePrincipalImpl`, `Repository` | Seguridad y estabilidad | Inyeccion HQL futura | Bajo/medio | Si | Si |
| Critica | Anadir indices para query principal | `log`, `feed`, `entry` | Evitar scans y lentitud | Importaciones lentas/locks | Medio | Script SQL | Si |

## 2. Prioridad alta

| Prioridad | Descripcion | Archivo/componente | Impacto esperado | Riesgo de no hacerlo | Esfuerzo | Automatizable | Revision manual |
|---|---|---|---|---|---|---|---|
| Alta | Sustituir carga completa por proyeccion/paginacion | `RepositoryImpl.getMapEntries` | Menor heap y GC | OOM con historico | Medio/alto | Parcial | Si |
| Alta | Validar/corregir filtro de fechas | `FiltroFechasEvaluator.java:37-39` | Evitar rechazos incorrectos | Importaciones vacias | Bajo | Si | Si |
| Alta | Ajustar logs SQL/TRACE para prod | `logback.xml:71-76` | Menos I/O y datos en logs | Logs enormes/sensibles | Bajo | Si | Si |
| Alta | Definir transaccion global o estado de importacion | `AbstractOpenDataMalaga`, `AbstractOpenDataPliegos`, `RepositoryImpl` | Consistencia | Estados parciales | Medio/alto | Parcial | Si |

## 3. Prioridad media

| Prioridad | Descripcion | Archivo/componente | Impacto esperado | Riesgo de no hacerlo | Esfuerzo | Automatizable | Revision manual |
|---|---|---|---|---|---|---|---|
| Media | Batch por contador global en `persistirSetFeeds` | `RepositoryImpl.java:144-215` | Memoria estable | Picos por feed grande | Medio | Si | Si |
| Media | Revisar `flushAndClear` con padres detached | `RepositoryImpl.java:175-208` | Persistencia mas predecible | Errores intermitentes | Medio | Parcial | Si |
| Media | Declarar `fetch = LAZY` en one-to-one inversos | entidades codice/auxiliares | Menos carga incidental | Queries extra | Medio | Si | Si |
| Media | Anadir `@Version` o lock externo | entidades actualizables | Control de concurrencia | Lost updates | Medio | No | Si |
| Media | Maven Wrapper | raiz del proyecto | CI reproducible | Builds locales no ejecutables | Bajo | Si | No |

## 4. Mejoras posteriores

| Prioridad | Descripcion | Archivo/componente | Impacto esperado | Riesgo de no hacerlo | Esfuerzo | Automatizable | Revision manual |
|---|---|---|---|---|---|---|---|
| Posterior | DTO/proyecciones si se anade API | capa API futura | Evitar serializacion circular | Endpoints pesados | Medio | Parcial | Si |
| Posterior | Reducir preview/log por fila | `AbstractOpenDataMalaga`, `RepositoryImpl` | Menos I/O | Logs masivos | Bajo | Si | No |
| Posterior | Reemplazar `HashSet<Feed>` por mapa por clave natural | `OpenDataExecutionContext` | Deduplicacion estable | Duplicados | Medio | Parcial | Si |
| Posterior | Tests Testcontainers MariaDB | `src/test` | Validacion real dialecto/indices | Falsos positivos H2/no DB | Medio | Si | Si |

## 5. Riesgos aceptados temporalmente

| Prioridad | Descripcion | Archivo/componente | Impacto esperado | Riesgo de no hacerlo | Esfuerzo | Automatizable | Revision manual |
|---|---|---|---|---|---|---|---|
| Aceptado temporal | No activar second-level cache | `hibernate.properties:28-29` | Simplicidad | Mas lecturas repetidas | N/A | N/A | Revisar tras medir |
| Aceptado temporal | Mantener cascades actuales | entidades `codice` | Evita refactor de modelo | Borrados amplios si se usan mal | Alto | No | Si |
| Aceptado temporal | Mantener Hibernate nativo sin Spring | arquitectura actual | Menos cambio | Mas boilerplate transaccional | Alto | No | Si |
