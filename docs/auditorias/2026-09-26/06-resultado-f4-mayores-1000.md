# F4 — Resultado de humo LOCAL MAYORES

Fecha de ejecución: 2026-09-26. Estado: correcto; baseline inicial, no extrapolable a 875.000 expedientes.

## Corpus y entorno

| Dato | Valor |
|---|---:|
| Origen | Cadena local `may` copiada, sin modificar los ATOM originales |
| Páginas | 3 |
| Entries | 1.026 |
| Bajas | 0 |
| Corte | Tras la tercera página; se eliminó su `next` en la copia |
| Destino | `opendata_prueba` |
| Tipo | `MAYORES`, carga LOCAL sin filtros |
| Esquema Hibernate | `validate` |
| Emails | Desactivados mediante `app.email.enabled=false` |
| JVM | Java 21, `-Xms512m -Xmx2g`, log GC dedicado |

La base se comprobó vacía para MAYORES antes de iniciar, como exige la variante LOCAL. El manifiesto y logs quedan bajo el directorio local `benchmark/`, ignorado por Git; no contienen ni se versionan credenciales.

## Resultado funcional

| Comprobación | Resultado |
|---|---:|
| Código de salida | 0 |
| Entries MAYORES en MariaDB | 1.026 |
| Feeds MAYORES en MariaDB | 3 |
| Filas `tendering_process` | 1.026 |

El manifiesto, el parser y la base coinciden. No se enviaron correos.

## Tiempos observados

| Fase | Tiempo |
|---|---:|
| Proceso Java completo | 12,766 s |
| JAXB y mapeo | 1,810 s |
| Filtrado | 0,008 s |
| Persistencia: preparación | 0,023 s |
| Persistencia: grafo y flush | 6,909 s |
| Transacción: commit | 0,009 s |
| Transacción completa | 6,943 s |

La inicialización de JVM, properties y Hibernate está incluida solo en el tiempo de proceso completo. Este resultado no permite extrapolar linealmente al corpus de 875.000; su utilidad es confirmar integridad, aislamiento y disponibilidad de métricas antes de aumentar a 10.000 entradas.

## Incidencia corregida antes de la corrida válida

El primer intento no escribió datos: una ruta Windows con barras invertidas simples en `app.properties` fue interpretada por Java Properties como secuencias de escape. El preparador F4 escribe ahora rutas con `/`, formato válido también en Windows. El segundo intento es el resultado registrado arriba.
