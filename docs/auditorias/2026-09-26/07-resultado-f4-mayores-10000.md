# F4 — Resultado de escala LOCAL MAYORES

Fecha de ejecución: 2026-09-26. Estado: correcto; se detiene la escalada antes de 100.000 para perfilar persistencia.

## Corpus y resultado funcional

| Dato | Valor |
|---|---:|
| Páginas ATOM | 21 |
| Entries leídas | 10.016 |
| Entries únicos seleccionados y persistidos | 8.755 |
| Duplicados resueltos en memoria | 1.261 |
| Bajas recibidas y persistidas | 10 |
| Feeds persistidos | 21 |
| Filas `tendering_process` | 8.755 |
| Código de salida | 0 |

La corrida usó el mismo modo LOCAL MAYORES sin filtros, la base `opendata_prueba` vacía para MAYORES, `hibernate.hbm2ddl.auto=validate`, Java 21 con `-Xms512m -Xmx2g` y correos desactivados. Los recuentos de MariaDB coinciden con el resumen de la aplicación.

## Comparativa de muestras

| Muestra | Entries leídas | Entries persistidos | JAXB/mapeo | Parseo total | Persistencia/flush | Proceso Java completo |
|---|---:|---:|---:|---:|---:|---:|
| Humo | 1.026 | 1.026 | 1,810 s | — | 6,909 s | 12,766 s |
| Escala | 10.016 | 8.755 | 11,180 s | 11,737 s | 68,417 s | 83,661 s |

El parseo de la muestra de escala procesó aproximadamente 853 entradas leídas/s. La fase de persistencia alcanzó aproximadamente 128 entries únicos/s, frente a aproximadamente 148 en la muestra de humo. El `flush` del grafo domina la duración de la corrida.

## Decisión antes de aumentar volumen

No escalar todavía a 100.000 ni a 875.000. Se debe perfilar la fase `grafoYFlush` y medir el tamaño efectivo de la sesión Hibernate, las sentencias/batches JDBC y la memoria retenida.

## Perfil Hibernate repetido

Se repitió el mismo corpus y configuración sin modificar la semántica. El resultado fue 63,014 s de `grafoYFlush`, 63,047 s de trabajo transaccional y 77,583 s de proceso completo. Hibernate informó:

| Métrica | Valor |
|---|---:|
| Inserciones de entidad | 349.241 |
| Actualizaciones / borrados | 0 / 0 |
| Recreaciones de colección | 224.032 |
| Flushes | 112 |
| Sentencias preparadas | 2.524 |
| Heap antes / después de persistir | 219 / 368 MiB |

La profundidad del grafo es el factor dominante: cada expediente genera aproximadamente 40 inserciones de entidad y 26 recreaciones de colección. El primer experimento de una sola variable será aumentar `hibernate.jdbc.batch_size` de 150 a 300 con el mismo corpus. Se mantendrán transacción, heap, origen y resultados funcionales.

La restauración de este lote mediante `DELETE FROM log` y claves foráneas en cascada tardó aproximadamente cuatro minutos para 8.755 expedientes. Esta operación se medirá y revisará por separado; no forma parte del tiempo de importación.

## Comparación de batch JDBC

Se repitió el corpus con `hibernate.jdbc.batch_size=300`, sin modificar origen, heap, transacción ni modelo. El resultado funcional se mantuvo: 8.755 entries únicos y 10 bajas.

| Métrica | Batch 150 | Batch 300 | Variación |
|---|---:|---:|---:|
| `grafoYFlush` | 63,014 s | 57,611 s | −8,6 % |
| Trabajo transaccional | 63,047 s | 57,641 s | −8,6 % |
| Proceso Java completo | 77,583 s | 72,144 s | −7,0 % |
| Flushes Hibernate | 112 | 53 | −52,7 % |
| Sentencias preparadas | 2.524 | 1.180 | −53,2 % |
| Heap antes / después | 219 / 368 MiB | 220 / 316 MiB | menor pico final |

Las inserciones de entidad (349.241) y recreaciones de colección (224.032) no cambian, por lo que la ganancia procede de reducir sincronizaciones y preparación de sentencias, no de omitir datos. Batch 300 queda como referencia de las siguientes pruebas. El siguiente cambio debe dirigirse a reducir el trabajo ORM del grafo profundo; aumentar más el batch sin perfilar de nuevo no es suficiente.

## Perfil JFR y revisión de cascadas

Se ejecutó una tercera corrida completa con el mismo corpus, batch 300, heap máximo de 2 GiB y Java Flight Recorder en perfil. Terminó correctamente en **72,873 s**. Los resultados funcionales se reconciliaron en MariaDB: 8.755 `entry`, 8.755 `tendering_process` y 10 `deleted_entry`.

| Fase o indicador | Resultado |
|---|---:|
| Parseo total | 11,101 s |
| `grafoYFlush` | 57,584 s |
| Flushes | 53 |
| Sentencias preparadas | 1.180 |
| Heap antes / después de persistir | 224 / 398 MiB |
| Eventos de recolección G1 | 30 |

La inspección de las entidades descarta una eliminación trivial de llamadas a `persist`: `Feed → Entry` y `Entry → ContractFolderStatus` no tienen cascada de persistencia, por lo que el repositorio debe persistir de forma explícita `Entry`, `ContractFolderStatus` y `PreliminaryMarketConsultationStatus`. Desde `ContractFolderStatus` hacia sus hijos sí existe `CascadeType.ALL`; allí se materializan los aproximadamente 349.000 registros de entidad de la muestra.

Por tanto, el siguiente experimento no eliminará cascadas ni datos. Separará de forma configurable el número de agregados `Entry` retenidos por la sesión antes de `flush/clear` del tamaño de batch JDBC. Se comparará contra la referencia de 300, con el mismo corpus y reconciliación completa. Solo se conservará si reduce `grafoYFlush` sin incrementar de forma inaceptable heap o GC.

### Observación operativa de restauración

La eliminación de los 8.755 expedientes mediante `DELETE FROM log` y cascadas de base de datos tarda varios minutos, frente a aproximadamente un minuto de importación. La prueba ha confirmado que la lentitud está en el recorrido de borrado de las claves foráneas, no en el parseo. La campaña debe seguir midiendo este mantenimiento por separado y no atribuirlo al tiempo de carga.

El esquema mantiene un índice único global sobre `entry.entry_id`. No se puede reutilizar el mismo corpus bajo otro `TipoSindicacion` dentro de la misma base como mecanismo de aislamiento, porque colisiona aunque el control LOCAL solo compruebe entries del tipo solicitado. Las próximas corridas se restaurarán a vacío entre ejecuciones.
