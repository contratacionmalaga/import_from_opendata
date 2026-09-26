# Diagnóstico técnico

Fecha: 2026-09-26. Las rutas Java siguientes son relativas a `src/main/java/local/jarios/`. Los nombres de método son anclas de búsqueda reproducibles sobre el commit indicado en el índice. P0 bloquea la comprobación actual; P1 afecta escalabilidad, integridad o operación; P2 afecta eficiencia o mantenimiento.

## Recorrido real

`AbstractOpenDataBase.runWithPipeline`: inicialización → preparación de filtros → parseo → resolución/agrupación → ámbito de tombstones → preview → persistencia → email.

| Variante | Preparación | Parseo y selección | Escritura |
|---|---|---|---|
| Pliegos LOCAL | Cuenta entries del tipo; exige cero | Cadena de ficheros, sin comparar BD; deduplicación en mapa global | Grafo completo, agrupado por feed; sin históricos Entry persistidos |
| Málaga LOCAL | Misma restricción por tipo; carga Excel remoto y filtros | Construye el grafo antes de filtrar | Camino de listas de Feed y mapa adicional de deduplicación; históricos admitidos |
| Pliegos INTERNET | Carga todos los snapshots del tipo y obtiene máximo updated en Java | HTTP secuencial, corte por newestEntry | Borra versiones sustituidas e inserta grafos nuevos |
| Málaga INTERNET | Snapshots y máximo del tipo, además de Excel/filtros | Mismo corte temporal, luego filtrado | Reemplazo y auditoría de históricos |

LOCAL no significa completamente offline en Málaga: `AbstractOpenDataMalaga.beforeParse/loadOrganosContratacion` descarga el Excel. Todos los modos acumulan primero y persisten después. El parser sigue `next`; no enumera y valida la integridad de todos los ATOM del directorio.

## Hallazgos principales

### A00 — P0 — El árbol actual no compila (confirmado)

`helpers/FeedHelper.java:120-121` y `helpers/StringHelper.getNumeroConFormato(int)`. Los contadores totalEntries/totalDeletedEntries son long. La llamada introducida en el diff local no compila. Evidencia: `evidencias/maven-test.log`, compilador 3.15.0, dos errores de conversión. Propuesta: sobrecarga long o formateador numérico con contrato long; no resolver mediante cast que trunque. No afecta a la explicación del rendimiento del JAR anteriormente distribuido.

### A01 — P1 — Retención global de grafos durante toda la importación (confirmado; coste sin cuantificar)

`FeedHelper.parsearFeeds`, `OpenDataExecutionContext`, `ResolveEntriesOpenDataStep` y `RepositoryImpl.persistEntriesByFeed`.

El mapa `mapEntriesFromAtoms` conserva cada Entry ganadora y sus descendientes. `resolveEntriesToPersist` devuelve ese mismo mapa en las variantes: `mapEntriesToBaseDatos` es un alias, no una segunda copia completa de objetos. La agrupación añade listas de referencias y el preview vuelve a colocarlas en Feed. Vaciar listas por feed en Pliegos no libera los grafos mientras el mapa global siga apuntando a ellos. `session.clear()` libera el contexto Hibernate, no esos mapas Java. Málaga además reconstruye `latestEntriesById` y no usa entriesByFeed.

Impacto probable: heap vivo proporcional al total seleccionado, presión de GC y posible paginación del sistema cuando se combina con heaps de 12/32 GB y MariaDB en la misma máquina. No se ha observado GC/paginación en esta sesión. Solución estructural: almacenamiento intermedio acotado y escritura por segmentos; liberar todas las referencias solo cuando ya no sean necesarias para deduplicar, reportar o reintentar.

### A02 — P1 — Construcción completa antes de saber si se necesita (confirmado)

`MapperFeed.getFeed` → `MapperEntry.getListEntryFromFeedType` → mappers CODICE → `EntryProcessor.processEntries`. JAXB materializa un feed entero, se transforma todo su contenido, se ordenan las entries y solo después se comprueba corte, filtros y duplicados. En Internet se conserva además el body String. El pico de página contiene modelo JAXB y modelo JPA; el retenido global es el de entradas aceptadas.

Mejora gradual: seleccionar por id/updated y campos necesarios para filtros sobre JAXB antes de construir descendientes JPA. Después evaluar StAX por entry. No basta cambiar JAXB a StAX si el contexto sigue acumulando todas las entradas. Mantener cobertura de namespaces, extensiones CODICE, tombstones y campos opcionales.

### A03 — P1 — Transacción única de millones de filas (confirmado; impacto del servidor por medir)

`RepositoryImpl.persistirImportacion/ejecutarEnTransaccion`: log, configuración, filtros, borrados, feeds, entries, cascadas, históricos y estadística comparten commit. Conserva atomicidad, pero una carga masiva puede acumular undo/redo, mantener locks y necesitar un rollback largo. Flush no es commit. No reemplazarla por commits arbitrarios sin diseñar recuperación y visibilidad de cargas parciales.

Para LOCAL masivo se recomienda evaluar un esquema de staging y publicación controlada. Para INTERNET, segmentos idempotentes con cursor confirmado y estado de importación, o conservar atomicidad por una ventana incremental pequeña. El coste del cambio es alto porque afecta al contrato de integridad.

### A04 — P1 — Un solo parámetro gobierna conceptos distintos de lote (confirmado)

`RepositoryImpl.getBatchSize/persistEntries/persistEntriesByFeed/flushAndClearIfBatchReached`. Se utiliza `hibernate.jdbc.batch_size` también como límite del contexto. El contador suma Entry y CFS/PMCS explícitos, pero no todos los descendientes en cascade. Por tanto 150 no significa 150 filas SQL. Se reinicia en cada llamada por feed y no hay flush final en `persistEntries`; residuos de feeds pequeños pueden acumularse hasta un feed que alcance el umbral o hasta el flush final general.

Todos los feeds se hacen persist antes de escribir entries. Los tombstones, su mapa de existentes y sus históricos tampoco tienen límite de flush propio. En el primer flush puede haber muchas colecciones de Feed ya pobladas por preview. Debe medirse el tiempo de dirty checking y gestión de colecciones; no afirmar un comportamiento cuadrático sin perfil.

Separar batch JDBC, ventana de contexto ORM y tamaño transaccional. Llevar el contador de ventana a toda la operación, preservar límites de agregado y probar grafos extremos. Tras clear, reconstruir referencias a padres por identificador de forma explícita cuando proceda.

### A05 — P1 — Métrica de persistencia incorrecta (confirmado)

`AbstractOpenDataPliegos.persistAll/buildEstadistica` y equivalentes de Málaga construyen la duración antes de llamar al repositorio. No existe actualización posterior de ese campo en el flujo revisado. El email hereda esta cifra. Medir con reloj monotónico cada fase y el commit; separar escritura de datos, commit y notificación. Registrar el resultado después del commit en un mecanismo cuyo fallo no provoque repetir la importación ya confirmada.

### A06 — P1 — Corte incremental por máximo global puede omitir datos (riesgo lógico demostrable por recorrido)

`EntryProcessor.processEntries/isNotNewerThanNewest` termina toda la cadena al encontrar updated <= máximo en BD, antes de comprobar la existencia de ese id. Ejemplo: en BD existe A a las 12:00; B ausente llega con las 12:00 y se omite. Un backfill a las 11:59 también queda fuera. Ordenar dentro de una página no garantiza orden entre páginas. Cambiar filtros en Málaga puede requerir recuperar entradas anteriores al máximo actual. Los tombstones en páginas posteriores tampoco se visitan tras el corte de entries.

Definir cursor de fuente confirmado por ejecución, ventana de solapamiento, reconciliación y política para empates/backfills/cambios de filtro. No basta sustituir <= por < para resolver todos los casos. Preservar deduplicación por id y comparación de versión.

### A07 — P1 — Fin normal y cadena incompleta se confunden (confirmado)

`FeedHelper.parsearFeeds` acepta enlace inicial inválido con return y errores obteniendo next con break. En LOCAL, un fichero next ausente acaba como fin de parseo; puede persistirse una importación incompleta y notificarse éxito. No hay conjunto de enlaces visitados: un ciclo puede repetir páginas indefinidamente, especialmente LOCAL. `LocalFeedSource.getNextLink` devuelve el directorio base para next vacío.

Modelar explícitamente EOF frente a enlace roto, fichero ausente o ciclo; fallar ante cadena incompleta y registrar manifiesto de ficheros/checksums. Los enlaces se recortan a 500 caracteres en MapperFeed aunque el modelo admite 2500: una ruta recortada puede contribuir a este problema.

### A08 — P2 — Lectura total de snapshots incluso para cambios mínimos (confirmado)

`RepositoryImpl.getEntrySnapshots` realiza getResultList y luego HashMap; las variantes hacen putAll a otro mapa. Son proyecciones ligeras, no entidades completas, pero siguen costando O(total histórico) por ejecución. Consultar snapshots de ids candidatos por lotes, y cursor por separado; para validación de vacío usar una consulta de existencia si el recuento exacto no es necesario. Streaming de snapshots reduce la lista transitoria, no el mapa retenido final.

### A09 — P1 — Reemplazo destructivo y concurrencia sin coordinación (confirmado como diseño)

`getCreatedAtByEntryId` y `deleteExistingEntriesForUpdates` consultan/borran por entryId en grupos de 500, después reinsertan todo. Se conserva createdAt de Entry, pero cambia su UUID y el de sus hijos; los descendientes tienen nuevas fechas técnicas. El borrado HQL depende de las FKs reales ON DELETE CASCADE, no de cascade Java. Las anotaciones no prueban que el esquema desplegado tenga esas FKs.

El snapshot está separado temporalmente de la escritura. Dos importadores pueden competir y uno reinsertar una versión obsoleta. No hay lock de importación en el flujo revisado. Un @Version aislado no protege el bulk DELETE actual. Elegir exclusión por esquema/tipo o revalidación transaccional; revisar referencias externas antes de decidir update diferencial. No sustituirlo automáticamente por merge del grafo: puede aumentar SELECT y dirty checking.

### A10 — P2 — Históricos y tombstones generan trabajo evitable (confirmado)

Pliegos construye históricos INSERTAR/ACTUALIZAR durante el parseo y solo los vacía al persistir. Los rechazados se construyen antes de que `OpenDataExecutionContext.addHistorico` decida omitirlos. Mantener contadores y replacement ids directamente evita objetos y textos innecesarios. Málaga sí requiere su política de auditoría.

`persistDeletedEntries` registra IGNORAR incluso ante tombstone repetido: puede crecer el histórico sin cambio útil. La deduplicación del repositorio elige el más reciente; el mapa del parser usa put, por lo que su valor depende del orden, aunque actualmente se usa fundamentalmente para conteo/ámbito. Unificar regla. Persistir DeletedEntry no cambia el estado de Entry/CFS: el plan antiguo de cancelaciones no está implementado completamente y exige decisión funcional.

### A11 — P2 — Red secuencial, copias y reintentos incompletos (confirmado)

`InternetFeedSource.openBufferedReader/sendRequest/isAtomResponse`: reutiliza correctamente HttpClient, pero descarga String completo, hace strip/lowercase del body completo y luego JAXB. Los reintentos cubren respuestas no válidas, no IOException/timeout lanzados por sendRequest. Reintenta también errores permanentes; no usa Retry-After, límite global de espera ni caché reanudable. Los números de reintento/pausa no se validan por rango; `bytes=body.length()` mide caracteres.

Con defaults, tres reintentos añaden 60+120+180=360 segundos de espera por página problemática, aparte de solicitudes. Un retraso de 2500 ms en 1750 páginas implica 72,9 minutos de espera: ejemplo aritmético, no configuración actual confirmada. Aplicar política por status/excepción, backoff con jitter y presupuesto, descarga a spool/caché acotado y validación XML real. Paralelizar agresivamente puede agravar el bloqueo remoto; nunca compartir Unmarshaller entre hilos.

### A12 — P1 — Configuración y diagnóstico pueden perjudicar la operación (confirmado en código/plantillas)

`hibernate.properties.example`: batch=150, order_inserts/order_updates=true, pool máximo 30/mínimo 15, statistics=true y hbm2ddl=create. No son valores efectivos del servidor auditado. La escritura actual es secuencial; ampliar Hikari no la paraleliza. Dimensionar pool contra concurrencia real. La plantilla create y la modificación persistente de hbm2ddl en el lanzador merecen separar creación de esquema de importación cotidiana y utilizar validate.

`importar_atoms.ps1`: CreateSchemaFirstRun cambia a none a partir de la segunda ejecución global, sin restaurar properties en finally; tras fallo de la primera puede quedar create. Ambos lanzadores modifican properties compartidas y carecen de exclusión interproceso detectada. Fallback Xms=Xmx=12g exige presupuesto de RAM real; 32g no arregla retención ilimitada.

`HibernateConfigurer.buildConfiguration` registra todas las properties a DEBUG, incluidas las JDBC añadidas por SessionFactoryProvider. Su logger seguro anterior no protege ese segundo registro. Corregir antes de activar DEBUG para rendimiento. `UnmarshallerHelper` no configura explícitamente bloqueo de DTD/entidades externas: endurecer y probar, sin dar por demostrada explotación con el proveedor actual.

### A13 — P2 — Mantenibilidad y cobertura (confirmado)

- `TransactionManager` y `OpenDataContext` no tienen usos encontrados fuera de su definición; `ParseFeedsStep`, `GroupFeedsStep` y otros esqueletos conviven con el pipeline real. Retirar tras comprobar consumidores y reflexión.
- Cuatro variantes repiten inicialización, parseo y resolución. Extraer políticas de origen, filtros e históricos conservando entry points operativos.
- `ImportPersistencePlan` es un record de colecciones mutables que el repositorio consume y vacía. Un fallo no deja el plan listo para reintento. Definir contrato de propiedad de memoria y resultado, no simular inmutabilidad con un record.
- `getBatchSize` fuerza cast a String, no admite Number y usa módulo sin proteger cero en históricos. Validar configuración tipada al inicio.
- `UnmarshallerHelper` documenta singleton/reutilización del JAXBContext, pero llama a newInstance. Hoy ocurre una vez por parseo completo, no por fichero; no es el principal cuello.
- Filtros de NIF y CP consultan el mismo conjunto de NIF efectivos; evaluar una única vez tras loaders. Un NIF ausente puede lanzar IllegalStateException y abortar toda la carga; definir rechazo con razón frente a error fatal.
- `MapperEntry` recorta title a 500 aunque JPA permite 2500 y summary a 2500 aunque es TEXT. Las pruebas de longitud detectan exceso, no pérdida por truncado inferior. Revisar contrato de datos, especialmente ids y URLs; no ampliar columnas sin analizar índices y datos.
- `LocalDateTimeHelper.getLocalDateTimeFromXmlGregorianCalendar` descarta el offset sin normalizar a un instante común. Fechas con offsets diferentes o DST necesitan pruebas antes de apoyarse en ellas para deduplicar/cortar.
- Fallo de SMTP después del commit lleva a salida ERROR global; un relanzamiento puede repetir trabajo ya grabado. Separar éxito de datos y estado de notificación.
- Tests actuales de repositorio usan H2 modo MariaDB y grafos pequeños. No se encontró el test de rollback anunciado en la auditoría viva: la clase actual no contiene ese escenario. Falta cobertura de límites de lote, cascadas profundas, rollback tras clear, ciclos, interrupciones HTTP y reinicio.

## Qué puede explicar las horas

El log histórico `logs/import-from-opendata_2026-08-17_16-56-25.log` ofrece evidencia concreta: parseo 46m32,894s; inicio de persistencia 17:43:07.883; inicio de feeds 17:43:48.424 con 104.867 entries y 661 tombstones; fin 17:56:11.437. La fase completa dura 13m03,554s, unos 133,8 expedientes/s. Extrapolar linealmente ese ritmo a 875.000 produce unos 109 minutos de persistencia, **solo como ilustración**: versión, mezcla, reemplazos y entorno no equivalen a LOCAL actual. El tiempo total incluye parseo/red y puede crecer de forma no lineal por heap, índices o I/O.

Ejemplo de tamaño: a 20 filas por Entry serían 17,5 millones de filas; a 50, 43,75 millones. No se ha medido ese factor en la carga del usuario. UUID no es IDENTITY: no hay evidencia de que la estrategia IDENTITY esté desactivando batching aquí. Tampoco se ha demostrado un N+1 de lectura por Entry en el camino de inserción. Hay que medir batches realmente enviados y su distribución por tabla antes de aumentar el tamaño.

## Fuentes técnicas consultadas

Hibernate describe el batching y la necesidad de flush/clear periódico; ordenar inserts requiere medir su balance entre agrupación y coste. No implica que clear libere referencias de la aplicación. [Guía de batching oficial](https://github.com/hibernate/hibernate-orm/blob/main/documentation/src/main/asciidoc/userguide/chapters/batch/Batching.adoc).

MariaDB Connector/J dispone de protocolo bulk para lotes elegibles. La configuración depende de versión, claves generadas y streams; verificar la versión resuelta y el SQL real en lugar de copiar opciones de MySQL. [Opciones oficiales](https://mariadb.com/docs/connectors/mariadb-connector-j/about-mariadb-connector-j) y [cambios de defaults en 3.2.0](https://mariadb.com/docs/release-notes/connectors/java/3.2/mariadb-connector-j-3-2-0-release-notes). Las páginas generales y notas históricas difieren en defaults; esta auditoría no presupone uno efectivo.
