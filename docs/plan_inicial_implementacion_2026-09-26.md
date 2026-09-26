# Plan inicial de implementación: campos prioritarios y preparación de pruebas

Fecha: 2026-09-26. Estado: F0, F1 y F2 implementadas; F3 y F4 en curso.

Este documento concreta el siguiente bloque de trabajo de la [auditoría](auditorias/2026-09-26/README.md). Termina en la preparación e inicio de la campaña de pruebas funcionales y de rendimiento. No selecciona todavía staging, JDBC, StatelessSession, cambios de PK ni una nueva estrategia de transacciones; esas decisiones se tomarán con resultados.

La incorporación de los cuatro campos de `tendering_process` es la prioridad funcional. Recuperar la compilación es su prerrequisito técnico inmediato. Durante la implementación se ejecutarán verificaciones unitarias y de regresión por cambio; el punto de parada final se refiere a la campaña integrada y de volumen, no a posponer todas las comprobaciones hasta el final.

## 1. Orden, entregables y dependencias

| Fase | Objetivo | Entregable revisable | Depende de |
|---|---|---|---|
| F0 | Recuperar una base verificable | Corrección mínima de compilación y registro de suite existente | Estado actual del repositorio |
| F1 | Incorporar los cuatro campos prioritarios | Modelo, mapper, migración aditiva, fixtures y pruebas de persistencia | F0 |
| F2 | Hacer fiables las mediciones | Instrumentación por fases y diagnóstico sin secretos | F0; integrar sobre F1 para medir el modelo definitivo |
| F3 | Preparar controles de integridad | Navegación inequívoca, casos de corte incremental, exclusión de ejecución y rollback | F0–F2 |
| F4 | Preparar entorno y campaña comparables | Corpus, esquema aislado, ejecutor, métricas y hojas de resultados | F1–F3 |
| Hito T0 | Comenzar las pruebas integradas | Baseline etiquetado y lista de entrada completa | F4 |

No mezclar en estos cambios reformateos masivos, limpieza de clases antiguas, cambio de motor ni optimización especulativa de lotes. Cada entrega debe poder revisarse y revertirse de forma independiente. Preservar los cambios locales previos y documentar qué ajuste se hace sobre ellos.

## 2. F0 — Base compilable y reproducible

### Trabajo

1. Registrar commit base, diff previo, Java/Maven y versiones resueltas de dependencias. El repositorio de partida de la auditoría es `2b9da0436da225ac1e58b32516462eec3ebc11c5`, pero se registrará el estado real al iniciar la implementación.
2. Corregir `FeedHelper`/`StringHelper`: los contadores `long` deben formatearse sin cast a int. Preferir una sobrecarga long compatible con llamadas existentes.
3. Ejecutar la suite existente. Conservar el resultado de partida; distinguir fallos anteriores de los introducidos por esta fase.
4. Inventariar el test de rollback anunciado en documentos antiguos y ausente en la clase actual. Preparar su recuperación con fallo después de escrituras y de al menos un flush/clear, no solo antes de empezar.
5. Resolver la versión efectiva de CODICE y el parent con Maven; registrar checksum del JAR para evitar que otra copia de la dependencia cambie el resultado.

### Archivos y cierre

- `src/main/java/local/jarios/helpers/FeedHelper.java` y `StringHelper.java`.
- Test de formato para valores mayores que Integer.MAX_VALUE y compatibilidad del formato habitual.
- Registro de compilación, tests y dependency tree sin credenciales.
- **Cierre:** compilación correcta y suite existente evaluada; no se declara rendimiento mejorado.

## 3. F1 — Cuatro campos de tendering_process: prioridad funcional

### 3.1 Contrato de datos

| Elemento CODICE | Propiedad Java propuesta | Columna SQL | Tipo JPA / MariaDB |
|---|---|---|---|
| OriginalContractingSystemID | originalContractingSystemId | original_contracting_system_id | String; length = TamanoCampos.TAMANO_50 / VARCHAR(50) |
| OriginalContractingSystemDescription | originalContractingSystemDescription | original_contracting_system_description | String; columnDefinition = "TEXT" / TEXT |
| OriginalContractingSystemLotID | originalContractingSystemLotId | original_contracting_system_lot_id | String; length = TamanoCampos.TAMANO_50 / VARCHAR(50) |
| OriginalContractingSystemLotDescription | originalContractingSystemLotDescription | original_contracting_system_lot_description | String; columnDefinition = "TEXT" / TEXT |

Las cuatro columnas serán opcionales (`NULL` permitido): los ATOM anteriores pueden no traerlas y la migración debe conservar los registros existentes. No añadir UNIQUE, índices ni relaciones FK sin una necesidad de consulta demostrada. Se añaden a TenderingProcess, no a ProcurementProjectLot.

El tipo TEXT coincide con `tenderingProcessDescription`, `economicShortListDescription` y descripciones de ProcurementProject/TenderingTerms. **No copiar el recorte a 500 caracteres que algunos mappers actuales aplican a TEXT**: las nuevas descripciones deben conservar el texto recibido dentro de la capacidad del tipo solicitado. TEXT no es ilimitado; validar valores que excedan su capacidad en el charset del entorno y producir un error explícito, sin truncado silencioso.

Para identificadores mayores de 50 caracteres, propuesta inicial: validar y rechazar con diagnóstico de campo/entry, evitando alterar una referencia mediante recorte silencioso. Este comportamiento se documentará y probará; no ampliar TAMANO_50 ni aplicar la política nueva a todos los identificadores del sistema dentro de esta fase.

### 3.2 Evidencia sobre el origen

Inspección local con `javap` de `lib/codice-2.8.0.jar`, clase `org.dgpe.codice.common.caclib.TenderingProcessType`, realizada al redactar el plan:

- `getOriginalContractingSystemID()` devuelve un valor tipado individual.
- `getOriginalContractingSystemDescription()` devuelve una lista de OriginalContractingSystemDescriptionType.
- `getOriginalContractingSystemLotID()` devuelve un valor tipado individual.
- `getOriginalContractingSystemLotDescription()` devuelve una lista de OriginalContractingSystemLotDescriptionType.
- Existen además getters distintos `getOriginalContractingSystemDPSCategoryID()` y `getOriginalContractingSystemDPSCategoryDescription()`.

**Lote y categoría no se asumirán equivalentes.** Se mapearán exactamente los cuatro elementos solicitados. No se rellenará LotID con DPSCategoryID como fallback. La interpretación funcional de lote/categoría se contrastará con un ATOM representativo y su esquema; si se necesitan categorías DPS, será una ampliación explícita, pues sus identificadores también son listas. La disponibilidad en el JAR local no sustituye comprobar la dependencia que Maven resuelva realmente.

### 3.3 Mapper

Modificar `src/main/java/local/jarios/mappers/codice/MapperTenderingProcess.java`:

1. Extraer el valor de los dos tipos ID de forma segura ante ausencia.
2. Aplicar validación de longitud 50, preservando la referencia original válida.
3. Para cada descripción, recorrer la lista en orden, descartar elementos/valores nulos y conservar todos los textos no vacíos.
4. Representación escalar aplicada: unir varios textos con el carácter de salto de línea `\n`; una lista ausente o sin texto queda NULL. No concatenar sin separador, no quedarse solo con el primero y no convertir contenido a través del toString del objeto JAXB.
5. Conservar caracteres Unicode y saltos internos. Si los elementos llevan idioma u otros atributos que deban conservarse, documentar que estas cuatro columnas textuales no los modelan; no inventar idioma ni mezclar categorías.
6. El mapper compartido debe funcionar para sus dos padres actuales: ContractFolderStatus y PreliminaryMarketConsultationStatus cuando el origen incluya esos datos.

Preferir un helper pequeño y específico para listas de estos tipos o una función genérica tipada. No modificar globalmente `MapperStringFromList.joinValues` como efecto lateral: alteraría otros campos.

### 3.4 Modelo y escritura

Modificar `src/main/java/local/jarios/entity/codice/TenderingProcess.java` con las cuatro propiedades y anotaciones del contrato. Lombok generará accesores. Mantener las relaciones y estrategia de persistencia existentes: no debería requerirse un nuevo bucle de persistencia porque TenderingProcess ya forma parte del grafo.

Verificar ese supuesto mediante una prueba que importe, cierre/limpie la sesión y relea las cuatro columnas; un test que solo invoque setters no acredita la integración.

### 3.5 Migración aditiva y despliegue

Crear un script versionado en `docs/migraciones/` durante la implementación. Definición objetivo, ilustrativa y **no ejecutada**:

```sql
ALTER TABLE tendering_process
  ADD COLUMN original_contracting_system_id VARCHAR(50) NULL,
  ADD COLUMN original_contracting_system_description TEXT NULL,
  ADD COLUMN original_contracting_system_lot_id VARCHAR(50) NULL,
  ADD COLUMN original_contracting_system_lot_description TEXT NULL;
```

El script definitivo debe comprobar si las columnas ya existen y si sus tipos son correctos; no dar por buena una columna incompatible porque IF NOT EXISTS no falle. No fijar un esquema de producción dentro del SQL genérico. Registrar aplicación por destino Málaga/Pliegos y por entorno.

Secuencia futura:

1. Obtener DDL, versión MariaDB, charset y tamaño de tendering_process; comprobar copias y espacio del entorno objetivo.
2. Probar el ALTER en una copia representativa, incluidos tiempo y bloqueos; no prometer DDL instantáneo sin verificar versión y condiciones.
3. Aplicar la migración aditiva antes del JAR que requiere columnas nuevas.
4. Arrancar con `hibernate.hbm2ddl.auto=validate`; no recrear tablas para añadir campos.
5. Verificar conteos previos, nulos de registros antiguos y escritura de registros nuevos.

Reversión propuesta: volver al JAR anterior conservando columnas opcionales; comprobar esta compatibilidad en pruebas. No incluir un DROP automático que borre datos recién capturados.

### 3.6 Datos ya importados: decisión separada del ALTER

Agregar columnas no rellena expedientes existentes. INTERNET puede omitir entradas cuyo updated no cambió por el corte newestEntry y por comparación de snapshots. LOCAL rechaza una BD no vacía para ese tipo. **Ninguno de esos flujos garantiza un backfill automático.**

Preparar una propuesta de backfill a partir de los ATOM conservados: seleccionar versión coincidente por clave funcional y fecha, actualizar solo las cuatro columnas, registrar origen y cantidades y evitar sobrescribir con una versión histórica incorrecta. No activar el backfill masivo en este bloque. Incluir un caso pequeño en la campaña para decidir después si conviene backfill específico o reconstrucción aislada. Nunca forzar updated de origen para provocar reimportación.

### 3.7 Pruebas y aceptación de F1

Preparar fixtures con ninguno, cada campo por separado y los cuatro campos; listas múltiples; valores nulos/vacíos; descripción superior a 500 y 2500 caracteres; Unicode; ID de 50 y de 51 caracteres. Incluir un fixture con categorías DPS y sin LotID para demostrar que no se mezclan.

Añadir prueba ATOM → JAXB → mapper → JPA → relectura SQL en esquema aislado, incluyendo sustitución de Entry y preservación de los valores del nuevo grafo. Verificar ambos caminos de padre soportados y ausencia de cambios en campos anteriores.

Ejecutar pruebas de convenciones JPA y cobertura del mapper. Preparar integración MariaDB real; H2 no acredita los límites físicos de TEXT, charset ni ALTER.

**Cierre:** cuatro campos capturados y persistidos, contrato de multiplicidad/longitud documentado, migración probada en entorno de desarrollo y sin requerir nueva versión CODICE salvo que la resolución real contradiga el JAR inspeccionado. Una entrega funcional de F1 no esperará a una futura reestructuración masiva de rendimiento.

## 4. F2 — Instrumentación fiable y segura

### Trabajo y archivos

- `HibernateConfigurer`: retirar volcado directo de properties; lista permitida de parámetros de rendimiento, sin password, tokens ni URLs con credenciales.
- `FeedHelper`, parsers y mapper: diferenciar lectura/descarga, JAXB, mapping y selección. Utilizar `System.nanoTime()` para duración; timestamps de calendario solo para identificar momentos.
- Variantes `AbstractOpenDataMalaga`/`AbstractOpenDataPliegos`: eliminar la falsa duración calculada antes de persistir.
- Repositorio: acumular tiempo de snapshots, búsqueda de createdAt, borrado, persist, flush y commit. Contadores agregados, sin INFO por fila ni logging SQL masivo por defecto.
- Contexto/resultado/email: separar entries leídas, únicas, elegidas, reemplazadas, tombstones y filas persistidas cuando se disponga de ese dato. No deducir filas SQL a partir de un contador parcial de entidades.

### Contrato de duración y commit

El tiempo de commit solo se conoce después de confirmar. Propuesta: devolver un resultado de persistencia con duraciones definitivas y publicarlo en log/artefacto y email después del commit. Si también debe quedar en `estadistica`, usar actualización breve posterior, con fallo de métricas separado del estado de datos; no afirmar que esa cifra se guardó atómicamente dentro del commit que mide.

Una avería de SMTP o de registro de métricas no debe provocar que el operador interprete que los datos confirmados necesitan reimportación. Representar resultado de datos y notificación por separado, aunque la política exacta de códigos de salida se documente al implementar.

### Entregables y cierre

- Resumen por ejecución con identificador, versión, configuración no sensible y métricas; sin datos sensibles completos por entry.
- Test con reloj/control de tiempos inyectable donde sea necesario; verificar que la persistencia se mide después de la llamada, no exigir duraciones de pared frágiles.
- Perfil opcional de estadísticas Hibernate/JFR/GC y procedimiento de captura acotado.

### Aplicación realizada (2026-09-26)

- Se sustituyeron los cronómetros de parseo y persistencia por `System.nanoTime()`. El parseo informa apertura del origen, JAXB+mapeo y filtrado; la persistencia informa preparación, consulta y borrado de reemplazos, grafo+flush y commit.
- La duración de `Estadistica` se calcula tras retornar de `persistirImportacion`, que ya ha confirmado su transacción. Después se actualiza la estadística en una transacción breve. Si esa actualización falla, se registra aviso y no se presenta una importación ya confirmada como fallida.
- Los logs de configuración Hibernate solo contienen el número o los nombres de las propiedades; no registran valores de JDBC, URL, usuario o contraseña.
- Se cubrieron el formato de duración, el orden importación/estadística y el caso de fallo aislado al actualizar métricas. Queda para F4 activar perfiles opcionales de JFR/GC y usar estas métricas contra un corpus representativo.
- **Cierre:** una ejecución pequeña permite reconciliar total y fases, incluye commit real y no expone secretos.

## 5. F3 — Integridad mínima y caracterización antes del volumen

### F3.1 Navegación de feeds

En `FeedSource`, `LocalFeedSource`, `InternetFeedSource` y `FeedHelper`, representar explícitamente fin normal. Enlace next informado pero inválido, fichero ausente o ciclo será error. Conservar conjunto de referencias normalizadas visitadas y diagnosticar la posición. No guardar una cadena parcial como éxito.

Preparar pruebas de cadena válida, next ausente, fichero perdido, URL inválida, self-loop, ciclo de dos páginas y XML truncado. Rechazar DTD/entidades externas explícitamente en el parser y añadir fixture de comprobación. No compartir Unmarshaller entre hilos.

### F3.2 Semántica incremental

Convertir A06 en casos ejecutables: mismo `updated` con id ausente, versión atrasada y tombstones en páginas posteriores. El contrato operativo asume que el origen entrega las páginas en orden descendente; no se implementará ni probará una recuperación específica para páginas desordenadas. Para recuperar históricos se ejecutará una carga desde cero.

Los cambios de filtros no forman parte del requisito incremental y no se implementará un modo adicional de reconciliación. El baseline de rendimiento debe indicar si es una carga desde cero o incremental; no comparar ambos recorridos como si procesaran el mismo trabajo.

### F3.3 Persistencia y ejecución

- Recuperar rollback real después de múltiples flush/clear, incluyendo log/feed/Entry/hijos/estadística y tombstones. Inyectar fallo tardío determinista.
- Verificar que la FK desplegada elimina hijos del bulk DELETE y preserva integridad; no confiar solo en anotaciones.
- Preparar exclusión por destino y tipo: elegir mecanismo válido para los hosts reales. Un mutex local no evita dos importadores en máquinas distintas. Adquirir la exclusión antes de snapshots y liberar siempre; comprobar dos ejecuciones contendientes.
- Validar tamaños/reintentos/timeouts al inicio. Rechazar cero/negativos o valores fuera de rango cuando no tengan semántica permitida.
- Preparar dos juegos aislados de configuración. No compartir ficheros que el lanzador reescribe entre corridas concurrentes. La campaña usará esquema preparado y validate, no CreateSchemaFirstRun.

**Cierre:** casos de integridad definidos y fallos evidentes corregidos; riesgos que dependan de la fuente o del servidor identificados como preguntas de la campaña. No se amplía todavía la transacción única ni se introducen commits parciales.

### Aplicación parcial realizada (2026-09-26)

- El parseo reconoce como fin normal únicamente un feed sin `next`. Un `next` informado pero inválido, un enlace inicial inválido o un ciclo provocan fallo, sin persistir una cadena parcial como éxito. Se rechazan DTD y entidades externas antes del unmarshalling JAXB.
- El corte incremental conserva y evalúa los entries con el mismo `updated` que el cursor; el control por snapshot decide si se insertan o rechazan. Cuando encuentra una entrada estrictamente anterior deja de procesar las entradas activas de esa página, pero continúa la cadena de feeds para recoger los `deleted-entry` posteriores. La prueba `FeedHelperNavigationTest` cubre ese recorrido. El orden descendente de la fuente es una garantía operativa asumida; si se requiere recuperar histórico, se ejecutará carga desde cero.
- Hay una prueba de rollback tardío que confirma que, tras un `flush/clear` efectivo y un fallo posterior, se revierten log, feed, entries, hijos, tombstone, histórico de tombstone y estadística.
- Se incorporó exclusión distribuida con `GET_LOCK` de MariaDB por tipo de sindicación. Se adquiere antes de snapshots y parseo y se libera en el `finally` del pipeline. La prueba con dos conexiones reales contra `opendata_prueba` confirmó la contención y la liberación.
- En `opendata_prueba` se aplicó la migración aditiva de `tendering_process` y se validó el `bulk DELETE` real sobre un grafo temporal `Entry → ContractFolderStatus → TenderingProcess`: los tres niveles se eliminaron, sin restos del dato de prueba. No se requiere matriz de desorden de páginas ni cambios de filtros: el primero está cubierto por la garantía del origen y el histórico se recuperará mediante carga desde cero.

## 6. F4 — Preparar el laboratorio y el inicio de pruebas

**Progreso (2026-09-26):** se completaron los corpus y corridas LOCAL MAYORES de humo (1.026 entries) y escala (10.016 leídas, 8.755 únicas). El perfil mostró 349.241 inserciones de entidad y 224.032 recreaciones de colección en la muestra de escala. Aumentar el batch JDBC de 150 a 300 redujo `grafoYFlush` de 63,014 s a 57,611 s; batch 300 es la referencia siguiente. Una captura JFR completa confirmó 57,584 s de `grafoYFlush`, 30 eventos de GC y recuentos funcionales correctos. Las persistencias explícitas de `Entry`, `ContractFolderStatus` y consultas preliminares son necesarias porque no existe cascada desde `Entry`; los hijos del expediente se persisten mediante las cascadas del agregado. El siguiente experimento separará la ventana de sesión ORM del batch JDBC, sin quitar entidades ni cascadas. Evidencias: [humo](auditorias/2026-09-26/06-resultado-f4-mayores-1000.md) y [escala](auditorias/2026-09-26/07-resultado-f4-mayores-10000.md).

### F4.1 Corpus y entornos

Preparar conjuntos reproducibles de 1.000, 10.000 y 100.000 entries y un corpus completo de 875.000 o más, incluyendo variantes de grafo grande. La cadena de enlaces de cada muestra debe ser válida; no cortar el directorio al azar. Registrar checksum, mezcla de tipos, número leído/único y distribución de hijos.

Reservar esquema MariaDB aislado, con versión/charset/FKs/índices representativos, restauración del estado inicial y espacio para logs/capturas. No ejecutar importadores contra producción durante preparación. Configurar notificación de prueba sin envío real.

Preparar los escenarios local e Internet, para carga desde cero e incremental. Se medirá con la configuración de filtros establecida; no se requiere una campaña específica para cambios de filtros. Internet reproducible utilizará respuestas grabadas o servidor controlado; red real se medirá por separado. Málaga LOCAL debe resolver también su dependencia del Excel remoto mediante recurso controlado y documentado.

### F4.2 Material que debe existir antes de T0

- Fixtures y pruebas de F1/F3, ejecutor documentado y configuración de ejemplo sin secretos.
- JAR/commit exactos y dependency tree; cuatro campos incluidos en el baseline.
- Migración aplicada al esquema de prueba y verificación validate.
- Plan de restauración entre corridas; no contaminar la siguiente con cambios previos.
- Capturas JFR/GC, contadores JDBC/Hibernate y servidor preparadas con coste de diagnóstico medido o claramente separado.
- Hoja de resultados con columnas: escenario, corpus, commit, opciones, filas/entries, tiempos por fase, commit, heap pico/retenido, GC, batches, CPU/I/O/locks y resultado funcional.
- Consultas de reconciliación por clave, versión y contenido, incluidos los cuatro campos. Excluir UUID/fechas técnicas variables al comparar contenidos cuando corresponda.

Usar como base el [protocolo de validación](auditorias/2026-09-26/03-validacion.md). Añadir a los casos allí definidos migración de los cuatro campos, listas de descripciones, límites y backfill controlado.

### F4.3 Comprobación de entrada

- [ ] F0 compilable; suite de regresión revisada.
- [ ] F1 modelo/mapper/migración y casos de datos listos.
- [x] F2 tiempos correctos y diagnóstico sin secretos.
- [ ] F3 navegación, rollback y concurrencia cubiertos o limitaciones explícitas.
- [ ] Esquema y configuración aislados; ningún email real.
- [ ] Corpus con manifiesto y resultados esperados.
- [ ] Versión y comandos fijados; baseline sin optimizaciones masivas.
- [ ] Capacidad del entorno comprobada y criterio de abortar ante presión de memoria/disco definido.

**T0: aquí termina el bloque inicial de implementación.** El siguiente trabajo es ejecutar la campaña empezando por humo e integración, aumentar volumen gradualmente y registrar evidencias. No pasar directamente al corpus completo si falla la integridad de la muestra pequeña.

## 7. Decisiones que se tomarán después de las pruebas

| Resultado observado | Decisión a evaluar, no autorizada por este documento como implementación automática |
|---|---|
| Heap retenido crece con el total de grafos | Staging y consumo acotado; revisar todas las referencias antes de liberar |
| Mapeo de descartados domina CPU/asignaciones | Selección temprana sobre JAXB; después comparar StAX |
| Flush/colecciones domina frente a SQL | Separar ventana ORM de lote JDBC, corregir contador global y límites por agregado |
| Batches pequeños o no efectivos | Medir agrupación por tabla y configuración efectiva del driver; variar una opción cada vez |
| Servidor domina por redo/I/O/índices | Revisión con DBA y experimento aislado de índices/segmentos |
| Snapshot completo domina el incremental | Consultas por candidatos y cursor independiente |
| Red domina | Reintentos por causa, Retry-After, presupuesto y caché/reanudación |
| Omisiones por cursor o cambio de filtros | Rediseñar semántica incremental antes de optimizar ese recorrido |
| Campos nuevos ausentes en registros antiguos | Elegir backfill específico frente a reconstrucción validada |
| Categorías aparecen en elementos DPS separados | Proponer ampliación de contrato; no reutilizar LotID/LotDescription |

Cada decisión posterior tendrá comparación funcional, medición antes/después y estrategia de reversión. La carga LOCAL por staging sigue siendo una candidata importante, pero no se compromete aquí su implementación sin las evidencias de esta campaña.
