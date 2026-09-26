# Propuestas para decidir el plan de implementación

Actualización de prioridad: el [plan inicial detallado](../../plan_inicial_implementacion_2026-09-26.md) incorpora los cuatro campos OriginalContractingSystem solicitados como primera entrega funcional tras recuperar compilación. Concreta las fases hasta comenzar pruebas; las alternativas estructurales de este documento quedan para decidir con resultados.

No ejecutadas. El orden prioriza observabilidad e integridad antes de cambios transaccionales. Esfuerzo relativo: bajo (cambio localizado), medio (varias capas/pruebas), alto (arquitectura o migración).

| Orden | Paquete | Hallazgos | Esfuerzo | Criterio de cierre |
|---|---|---|---|---|
| 0 | Recuperar compilación sin truncar contadores | A00 | Bajo | Compilación y suite existente verdes |
| 1 | Medición correcta y diagnóstico seguro | A05, A12 | Medio | Tiempos de parseo, mapeo, selección, flush, commit y total; ninguna credencial en DEBUG |
| 2 | Integridad de navegación/incremental | A06, A07, A09 | Medio/alto | Casos de empate, backfill, cadena rota, ciclo y doble ejecución cubiertos |
| 3 | Reducir retención y objetos descartados | A01, A02, A10 | Medio | Heap retenido medido; históricos innecesarios no construidos; invariantes conservadas |
| 4 | Ventanas ORM y batching verificados | A04 | Medio | Límite efectivo entre feeds y tombstones; batches observados, grafos completos y rollback correctos |
| 5 | Carga LOCAL por staging/reanudación | A01, A03 | Alto | 875.000+ entries, memoria acotada, recuperación y publicación controlada |
| 6 | Incremental por candidatos y política HTTP | A08, A11 | Medio | Trabajo proporcional a cambios, reintentos acotados y ninguna omisión |
| 7 | Procedimientos y limpieza estructural | A12, A13 | Medio | Configuración validada, ejecución aislada, CI reproducible y menos caminos duplicados |

## Alternativas de escritura

| Alternativa | Ventaja | Coste/riesgo | Recomendación |
|---|---|---|---|
| Hibernate actual con ventanas corregidas | Conserva mapeo y atomicidad actuales | Sigue reteniendo datos si no cambia contexto; transacción larga | Primera comparación controlada |
| Hibernate por segmentos | Limita sesión y transacción | Visibilidad parcial y recuperación pasan a ser requisitos | Incremental con cursor/estado idempotente |
| Staging relacional + publicación | Deduplicación global en disco, reinicio y aislamiento de carga | Espacio temporal, migraciones y diseño de publicación con FKs/vistas | Candidato principal para LOCAL masivo |
| JDBC batch en escritor especializado | Control de SQL y menos coste ORM | Mantener orden FK, UUID, auditoría y callbacks manualmente | Prototipo si el perfil demuestra coste ORM dominante |
| StatelessSession | Evita contexto persistente tradicional | Semántica de cascadas/eventos dependiente de versión; adaptación del grafo | No es reemplazo directo del repositorio |
| Carga nativa masiva | Potencial para ingestión tabular | Mayor dependencia de MariaDB y validación fuera del ORM | Solo con datos normalizados y comparación medida |

## Diseño candidato para LOCAL

1. Crear identidad de importación, manifiesto y hash de configuración/filtros. Validar cadena completa, tipos y reglas de ámbito.
2. Leer página/entry en ventana acotada. Extraer clave/versión y filtrar antes de construir el modelo pesado.
3. Guardar candidatos en staging con fuente, posición y checksum; elegir ganador por entryId/updated y desempate determinista. Guardar tombstones con su propia regla. No asumir que un ganador es definitivo antes de terminar de leer todas sus posibles versiones.
4. Construir/persistir grafos ganadores por segmentos y confirmar checkpoints. No exponerlos como importación completa hasta reconciliar contadores, FKs y errores.
5. Publicar la carga validada mediante mecanismo definido para el esquema real. No prometer un simple rename atómico de todas las tablas: revisar FKs, vistas y consumidores. Guardar estado COMPLETADA y plan de reversión.

La restricción actual de LOCAL sobre tipo vacío es incompatible con reanudar segmentos directamente en destino: debe cambiar expresamente o utilizar staging. Vaciar el mapa actual después de cada feed sin soporte para duplicados posteriores puede alterar resultados.

## Mejoras acotadas antes de staging

- Separar resolución de acciones de creación de HistoricoEntry. Pliegos necesita replacement ids y contadores, no una lista completa que descartará.
- Preview sin mutaciones del grafo. Contadores calculados durante selección, sin recorrer cada entry para logging desactivado.
- Unificar el contrato de persistencia Málaga/Pliegos. Evitar fallback ambiguo basado en mapa vacío; declarar modo explícito y validar coherencia de feeds/entries.
- Ventana de flush global, independiente del tamaño JDBC; liberar agregados de todos los índices de aplicación al consumirse, pero conservar la información necesaria para deduplicación/reintento fuera del grafo pesado.
- Reducir snapshots por ids candidatos y no solo por paginación de toda la tabla. Cursor independiente de la última Entry conservada, incluyendo política de filtros y tombstones.
- Configuración inmutable por ejecución; evitar properties compartidas modificadas por varios procesos. Exclusión por destino/tipo y estados de ejecución separados de SMTP.

## Base de datos y procedimientos

Comprobar DDL real, EXPLAIN de snapshots/borrados y relación filas hijas/Entry. Diferenciar índices imprescindibles para unicidad/FKs de índices analíticos. Crear índices secundarios analíticos después de una carga aislada puede ahorrar escritura, pero medir su construcción y no retirar los de integridad. Auditar índices equivalentes con nombres distintos: `IF NOT EXISTS` por nombre no demuestra ausencia de redundancia.

El modelo declara UUID; comprobar almacenamiento físico y localidad del índice en el servidor real antes de proponer UUID temporal o BIGINT. Es una migración transversal de PK/FK, no un ajuste de configuración. Medir buffer pool, latencia de disco, presión de redo, locks, binlog y espacio temporal con el DBA. No reducir durabilidad ni desactivar FKs/unique checks como primera optimización.

La reescritura de vistas con OR a UNION ALL pertenece principalmente al rendimiento de consulta posterior. No explica por sí sola una inserción lenta salvo carga concurrente de esos consumidores. Revisar semántica de duplicados antes de aplicar UNION ALL.

## Limpieza mantenible

Reducir clases vacías y contratos antiguos sin uso, unificar variantes mediante políticas pequeñas y separar lectura, selección, mapeo, persistencia y notificación. Evitar una reescritura total o introducir un framework solo para eliminar boilerplate. Corregir comentarios que prometen comportamientos inexistentes. Mantener fixtures ATOM pequeños representativos como contratos funcionales, más una suite MariaDB separada para persistencia real.

No hay una mejora porcentual garantizada. Cada paquete se acepta con medición comparable y equivalencia funcional, no por aumentar batch, heap, pool o hilos.
