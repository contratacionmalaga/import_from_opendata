# Plan de mejora: una única traza técnica por incidente

Fecha: 2026-09-27  
Estado: implementado y validado el 2026-09-27.  
Referencia: [auditoría de mensajes de error](auditoria-mensajes-error-2026-09-27.md).

## Objetivo

Cada importación fallida debe producir una sola comunicación para el operador en consola y una única traza completa en el log técnico de esa ejecución.

La excepción original debe conservarse encadenada de extremo a extremo para que el correo de soporte y el fichero `*_error_<identificador>.log` mantengan el diagnóstico necesario. Los fallos secundarios reales, como un rollback que también falla, se deben conservar como incidentes separados.

## Fase 1: fijar el contrato de registro

1. Definir `AbstractOpenDataBase.handleFailure` como frontera de errores de las importaciones. Será el único punto que escriba la traza completa de un fallo que termine la ejecución.
2. Mantener `ERROR` con excepción únicamente en fronteras que puedan ejecutarse de forma autónoma y no lleguen a `handleFailure`.
3. Sustituir en capas intermedias los `log.error(mensaje, ex)` seguidos de `throw ...` por una de estas alternativas:
   - no registrar nada cuando la excepción se reenvía sin añadir información;
   - registrar `DEBUG` sin la excepción cuando aporta contexto de diagnóstico durante desarrollo;
   - enriquecer el mensaje de la excepción que se relanza cuando añade contexto útil.
4. Normalizar los mensajes internos: sin prefijos de método como `[validateUrl]` o `[rollbackTransaction]`, y sin repetir `Error` cuando el tipo de excepción y el contexto ya lo expresan.

Criterio de aceptación: una incidencia que atraviese varias capas conserva su cadena de causas, pero genera una única traza completa en el fichero de error.

## Fase 2: corregir el arranque y el acceso a base de datos

Componentes: `SessionFactoryProvider`, `SessionFactoryRegistry` y `ServicePrincipalImpl`.

1. Revisar la cadena desde Hibernate/HikariCP hasta `MiSessionFactoryProvider` y `MiServiceException`.
2. Eliminar el registro con traza de `SessionFactoryProvider` cuando la excepción se propaga al pipeline.
3. Eliminar el registro con traza en el constructor y en los adaptadores `execute` / `executeVoid` de `ServicePrincipalImpl` cuando solo encapsulan una excepción.
4. Conservar el mensaje técnico con el tipo de conexión y el contexto de la operación, pero evitar incluir secretos o contraseñas.
5. Verificar que el clasificador de `AbstractOpenDataBase` sigue presentando el mensaje operativo correcto para credenciales rechazadas, servidor no resoluble, conexión rechazada y configuración de HikariCP.

Pruebas:
- credenciales rechazadas;
- host de MariaDB no resoluble;
- puerto de MariaDB inaccesible;
- propiedad obligatoria ausente;
- una sola traza completa en el log técnico por caso.

## Fase 3: corregir parseo local e Internet

Componentes: `FeedHelper`, `UnmarshallerHelper`, `InternetFeedSource` y `FileHelper`.

1. Suprimir las trazas completas registradas en `FeedHelper` y `UnmarshallerHelper` cuando relanzan `MiParseException` o `MiUnmarshallerException`.
2. En `InternetFeedSource`, conservar los reintentos y sus avisos, pero no registrar una traza completa por cada capa cuando se agotan.
3. Mantener en la excepción el URL técnico para soporte, pero evitar que llegue a consola.
4. Cambiar los avisos de fichero inexistente, ilegible o no válido para que la consola muestre solo el nombre del feed. La ruta absoluta seguirá disponible en el log técnico.
5. Mantener como errores distintos los fallos que ocurran durante un reintento y el fallo final solo cuando se hayan agotado los intentos.

Pruebas:
- fichero local inexistente;
- XML ATOM inválido;
- enlace `next` inválido;
- ciclo de feeds;
- URL HTTP inválida;
- error HTTP temporal seguido de reintento correcto;
- error HTTP permanente tras agotar reintentos.

Criterio de aceptación: la consola no muestra rutas completas ni trazas; el log técnico permite identificar feed, ruta o URL y conserva una sola traza final.

## Fase 4: corregir persistencia y transacciones

Componentes: `RepositoryImpl` y `TransactionManager`.

1. Eliminar la traza duplicada del fallo principal de la transacción cuando se vuelve a lanzar como `MiRepositoryException`.
2. Mantener un registro `WARN` simple cuando el rollback se realiza correctamente como consecuencia de un fallo previo.
3. Mantener un `ERROR` con traza completa cuando el propio rollback falla: es una segunda incidencia y puede requerir recuperación manual.
4. Incluir en el mensaje encadenado el nombre de la operación o lote afectado, sin serializar entidades ni datos sensibles.
5. Confirmar que el error de persistencia llega al manejador final sin perder la causa original de Hibernate o MariaDB.

Pruebas:
- error de restricción de base de datos que provoca rollback correcto;
- error simulado durante rollback;
- error de lote Hibernate;
- revisión de que el archivo técnico tiene una traza para el error principal y solo una adicional si falla rollback.

## Fase 5: revisar procesos auxiliares

Componentes: `ManagerSpreeadSheet`, cierre de repositorios y de `SessionFactory`.

1. Determinar qué recorridos usan estos componentes fuera de una importación ATOM.
2. Cuando tengan una frontera propia, registrar allí una sola traza completa y propagar una excepción con contexto.
3. Si siempre se ejecutan dentro del pipeline, aplicar la misma regla de las fases anteriores y delegar el registro final a `handleFailure`.
4. Para errores de cierre que no alteren un resultado ya confirmado, usar `WARN` sin traza en consola y conservar el detalle técnico cuando sea necesario.

Pruebas:
- fallo al leer un Excel auxiliar;
- excepción durante el cierre de repositorio;
- excepción al cerrar una `SessionFactory`;
- proceso completado cuya notificación o cierre secundario falla.

## Fase 6: pruebas de extremo a extremo y revisión del log

1. Añadir pruebas unitarias de la política de registro mediante un appender de Logback de prueba:
   - una excepción propagada por varias capas genera una sola traza completa;
   - un rollback fallido genera una segunda traza diferenciada;
   - la consola no contiene `Exception`, `at ...`, rutas absolutas ni URL completas.
2. Ejecutar la suite Maven completa.
3. Ejecutar una importación local controlada y una importación por Internet controlada.
4. Revisar los tres artefactos de cada ejecución:
   - consola;
   - `import-from-opendata_<identificador>.log`;
   - `import-from-opendata_error_<identificador>.log`.
5. Comprobar que el correo de soporte contiene el incidente y la cadena técnica configurada, sin exponer secretos.
6. Documentar los resultados con fecha, versión, corpus usado y rutas de los logs revisados.

## Orden de ejecución y decisión de entrega

Las fases 1 y 2 deben ir juntas porque el arranque de Hibernate es la ruta más habitual de fallo temprano. Las fases 3 y 4 pueden seguir de forma independiente. La fase 5 se ejecuta después de confirmar los recorridos auxiliares.

Resultado de la implementación:

- Se ha eliminado el registro con traza de las capas que solo encapsulan y
  propagan un error: proveedor de SessionFactory, servicio, parseo, JAXB,
  descarga, Excel y error principal de repositorio.
- Se conserva el `ERROR` con traza en la frontera final de la importación, en
  el fallo de notificación de soporte y cuando el rollback falla.
- Los avisos de ficheros locales muestran únicamente el nombre del feed en
  consola.
- La suite Maven completa ha finalizado con 74 pruebas superadas y 2 pruebas
  de integración MariaDB omitidas por configuración.

Queda pendiente una ejecución manual local y otra por Internet para comprobar
los artefactos de log de una instalación operativa antes de publicar la
siguiente versión.
