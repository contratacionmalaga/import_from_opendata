# Protocolo de validación y rendimiento

Estado: en curso. Se ejecutó un baseline LOCAL MAYORES de 1.026 entries contra `opendata_prueba`; consultar [resultado F4](06-resultado-f4-mayores-1000.md). Las pruebas de volumen y de Internet siguen pendientes.

## Baseline reproducible

Registrar commit, checksum del JAR, versiones efectivas de Java/Hibernate/driver/MariaDB, heap/GC, CPU/RAM, discos, ubicación de BD y ATOM, latencia JDBC, índices, configuración no sensible, mezcla de tipos y número de filas por tabla. El parent local declara Hibernate 7.4.5.Final y MariaDB Connector/J 3.5.10; obtener dependency:tree del build concreto antes del benchmark, porque parent local y publicado pueden diferir.

Guardar hash del corpus y de los filtros. Usar esquema aislado con copia representativa, mismo estado inicial y sin envío real de emails. Ejecutar LOCAL sin filtros, LOCAL con filtros y escenarios INTERNET reproducibles mediante respuestas HTTP grabadas. Medir Internet real aparte para no confundir variación del proveedor con coste del código.

| Escenario | Volumen/condición | Qué verifica |
|---|---|---|
| Humo | 1.000 entries, varias páginas | Integridad y relaciones |
| Escala | 10.000, 100.000, 875.000+ | Pendiente de tiempo/heap; no extrapolar solo muestra pequeña |
| Grafo extremo | Muchos lotes/documentos por entry | Ventana por agregado y picos |
| Incremental vacío | BD grande, cero cambios | Coste fijo de snapshots y HTTP |
| Incremental mixto | Nuevas, repetidas, sustituidas, tombstones | Escrituras útiles, idempotencia y createdAt |
| Recuperación | Fallo tras varios flush y antes/durante commit | Atomicidad vigente y reinicio futuro |
| Red anómala | Timeout, 429/503, 404, HTML 200, truncado | Reintentos y clasificación de errores |

## Mediciones

- Tiempo monotónico por descarga/lectura, JAXB, mapping, selección/filtros, agrupación, carga snapshots, borrado, persist/flush y commit. Cronómetro externo del proceso como control.
- Entries leídas, aceptadas, únicas, reemplazadas, rechazadas y filas SQL por tabla. Tombstones recibidos, únicos y con cambio útil; no llamar borrados físicos a todos los tombstones.
- Entries/s y filas/s, p50/p95 de flush, número de executeBatch y sentencias por batch/tabla. Tener configurado batch no demuestra uso efectivo.
- Heap usado y retenido, asignaciones, pausa/tiempo GC, RSS y paginación. JFR y logs GC del JDK seleccionado, con ficheros dedicados por corrida. Comparar dominadores: mapas de contexto, Entry/CODICE y colecciones Hibernate.
- Servidor: CPU, I/O/latencia, filas escritas, redo/log waits, buffer pool, locks, transacciones abiertas y tamaño de undo. Muestreo a intervalo fijo, sin queries de diagnóstico masivas durante la prueba.

Consultas de lectura a adaptar al esquema aislado (no ejecutadas):

```sql
SELECT VERSION();
SELECT table_name, table_rows, data_length, index_length
FROM information_schema.tables
WHERE table_schema = '<schema>';
SHOW CREATE TABLE entry;
SHOW CREATE TABLE contract_folder_status;
SHOW INDEX FROM entry;
EXPLAIN SELECT e.entry_id, e.updated
FROM entry e JOIN feed f ON f.id=e.feed_id JOIN log l ON l.id=f.log_id
WHERE l.tipo_sindicacion='<tipo>';
SELECT COUNT(*) AS total, COUNT(DISTINCT entry_id) AS claves FROM entry;
SELECT COUNT(*) AS total, COUNT(DISTINCT ref) AS claves FROM deleted_entry;
```

table_rows es una estimación; usar recuentos exactos fuera del tramo cronometrado. Añadir comprobaciones de huérfanos y checksums normalizados por clave funcional, excluyendo UUID/fechas técnicas variables. Verificar valores de hijos, no solo contar Entry.

## Experimentos de una variable

1. Baseline sin modificaciones funcionales tras recuperar compilación.
2. Evitar históricos innecesarios y mapeo de descartados.
3. Corregir ventanas de sesión; comparar batch JDBC 50/150/300 manteniendo ventana y transacción constantes.
4. Comparar ventanas ORM acotadas manteniendo batch fijo, incluyendo feeds pequeños y grafos grandes.
5. Comparar staging/segmentos frente al baseline con idéntico resultado final y misma carga.
6. Prototipo JDBC solo si el perfil atribuye parte dominante a ORM.

Repetir al menos tres veces los casos representativos, separar arranque/caliente y publicar mediana y dispersión. No ensayar toda la matriz grande si muestras previas ya descartan una alternativa.

## Criterios de aceptación propuestos

- Mismas claves y versiones ganadoras; ninguna entrada perdida en empates y ningún tombstone omitido por corte indebido. Se asume como garantía del origen que las páginas están ordenadas de forma descendente.
- Conservación de createdAt de Entry en reemplazo; regla explícita de identidad y fechas de hijos; idempotencia al repetir lote.
- Error inequívoco ante cadena rota/ciclo y recuperación sin mezcla de importaciones. Lock o detección de concurrencia comprobados.
- Para el diseño acotado, heap retenido estable al crecer el corpus por encima de la ventana; sin paginación y con margen de RAM para BD/SO.
- Sin regresión relevante del incremental pequeño. Objetivo numérico de horas/entries por segundo acordado después del baseline; no hay SLA validado actualmente.
- Registrar final de escritura y commit reales; fallo de email no cambia éxito confirmado de datos.

## Pruebas que faltan

Fixtures para todos los tipos de sindicación, namespaces y campos opcionales; offsets temporales; NIF ausente; longitudes límite; duplicados entre páginas; updated iguales; entrada atrasada; feed vacío; tombstone sin Entry en página actual; fichero siguiente ausente; ciclo; XML DTD/entidad externa rechazado; reintentos de IOException e interrupción; batch cero/inválido; rollback tras múltiples clear; deep cascades en MariaDB; fallo de commit y reinicio con plan consumido.

Los tests H2 actuales no certifican protocolo bulk, costes de índices, comportamiento completo del dialecto ni rendimiento. La documentación anterior afirma cobertura de rollback que no se encontró en la suite actual; recuperarla antes de alterar transacciones.
