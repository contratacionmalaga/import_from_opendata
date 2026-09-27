# Auditoría de mensajes de error y registro técnico

Fecha: 2026-09-27  
Alcance: salida de consola y ficheros de log de las importaciones ATOM.

## Bloque aplicado: mensajes para el operador

Se ha centralizado en `AbstractOpenDataBase` la traducción de los errores que detienen una importación. La consola conserva un único mensaje comprensible y remite al log técnico de la misma ejecución. El detalle, incluida la traza, se mantiene en `logs/import-from-opendata_error_<identificador>.log`.

| Situación detectada | Mensaje de consola resultante |
|---|---|
| Credenciales de MariaDB rechazadas | Indica que se revise usuario y contraseña de `bd.properties`. |
| Servidor de base de datos no resoluble o no disponible | Indica que se revise el servidor, puerto y conectividad configurados. |
| Otra ejecución del mismo tipo en curso | Indica que ya existe una importación en curso. |
| Carga local con expedientes existentes | Indica que la carga local no puede comenzar porque ya existen expedientes del tipo. |
| Enlace `next` inválido o ciclo de feeds | Indica el problema de navegación del origen sin mostrar rutas ni trazas. |
| XML ATOM no válido | Indica que un feed no puede procesarse. |
| Propiedades o feed remoto | Indica el fichero o la conectividad que debe revisarse. |
| Notificación de éxito no enviada | Se registra como aviso; la importación ya confirmada no pasa a considerarse fallida. |

La clasificación no modifica la excepción original: sigue siendo la causa del fallo y se conserva completa para soporte. Hay pruebas unitarias para las credenciales, bloqueo, navegación de feeds y XML.

## Grupo resuelto: trazas técnicas duplicadas

### Hallazgo

El mismo fallo puede registrar una traza completa en varias capas antes de llegar a `AbstractOpenDataBase`, que vuelve a registrarla. El resultado es un log de error más grande, con información repetida y más difícil de seguir.

Los puntos que actualmente registran una excepción y después la relanzan son:

| Componente | Casos | Efecto |
|---|---|---|
| `SessionFactoryProvider` | Lectura de propiedades y creación de Hibernate | La traza se repite al propagar `MiSessionFactoryProvider`. |
| `ServicePrincipalImpl` | Servicios y acceso al repositorio | La misma causa puede aparecer en servicio, repositorio y frontera de importación. |
| `RepositoryImpl` y `TransactionManager` | Transacción y rollback | Duplica el error principal; el fallo adicional de rollback sí necesita quedar diferenciado. |
| `FeedHelper`, `UnmarshallerHelper` e `InternetFeedSource` | Parseo, JAXB y descarga | El fallo de feed se registra antes de llegar al manejador central. |
| `ManagerSpreeadSheet` | Importación de Excel auxiliar | Registra y relanza la misma excepción. |

El fichero de error está configurado deliberadamente con `%ex{full}`. Por tanto, cada `log.error(..., ex)` de esa lista escribe la traza completa; no es una duplicación visual de consola, sino una duplicación real del diagnóstico técnico.

### Cambio recomendado

Mantener una única traza completa por fallo de importación, en `AbstractOpenDataBase.handleFailure`. Las capas que añaden contexto y relanzan la excepción deben dejar de registrar la excepción completa. Deben conservar su contexto en el mensaje de la excepción encadenada.

Hay dos excepciones a esa regla:

1. Un fallo durante el rollback debe registrarse por separado, porque añade un segundo incidente técnico que no está contenido en la causa inicial.
2. Los componentes que puedan ejecutarse fuera del flujo de importación deben conservar una frontera propia que registre una única traza al no existir `handleFailure`.

La implementación ha distinguido ambos recorridos: se han eliminado los
registros con traza de las capas que relanzan y se conserva la traza en el
manejador final. El rollback fallido sigue registrándose como un segundo
incidente técnico.

### Resultado esperado

Para un fallo de importación habrá:

- una línea clara en consola;
- una única traza completa en el fichero `*_error_<identificador>.log`;
- mensajes de contexto encadenados que permitan identificar la capa afectada;
- una traza adicional solo si también falla el rollback u otro proceso independiente.

## Grupo posterior sugerido

La consola ya identifica los ficheros locales por su nombre. El siguiente
avance consiste en conservar de forma estructurada en el diagnóstico técnico
la ruta local o URL completa, junto con la fase y el feed afectados, sin
llevar esos detalles a consola.

## Mejoras pendientes priorizadas

### P1 — Clasificación de errores por tipo y no por texto

`getOperatorMessage` identifica actualmente los casos con fragmentos de texto
de la cadena de causas. Esto cubre los escenarios conocidos, pero un cambio en
el mensaje de MariaDB, Hibernate o Java puede hacer que un error conocido pase
a mostrarse como genérico.

La mejora consiste en crear una categoría interna de incidente y resolverla
por tipo de excepción, código SQL/SQLState y contexto de la fase de
importación. El texto seguirá siendo un último recurso. Cada categoría tendrá
una prueba unitaria y un mensaje de operador estable.

### P1 — Retención efectiva de los logs por ejecución

Los nombres únicos evitan que dos ejecuciones escriban en el mismo fichero,
pero cada ejecución crea un fichero base nuevo. La política de rotación de
Logback controla principalmente los ficheros archivados; debe verificarse que
los ficheros base de ejecuciones antiguas también se limpian. Si no ocurre,
el directorio `logs` crecerá sin límite.

La solución recomendada es una tarea de limpieza al inicio o final de cada
ejecución que trate como una unidad los tres logs con el mismo identificador,
aplique antigüedad y tamaño máximos configurables y nunca borre archivos de la
ejecución en curso.

### P2 — Contexto técnico estructurado por fase y feed

El correo y el log técnico conservan la causa, pero no siempre indican de
forma uniforme la fase y el feed que estaba en curso. Conviene incorporar al
contexto de ejecución: variante, modo, tipo de sindicación, fase, número de
feed y nombre del feed. Debe aparecer en el correo de soporte y en el mensaje
técnico, sin llevar rutas absolutas ni URLs completas a consola.

### P2 — Validación operativa de extremo a extremo

La suite cubre el código, pero falta comprobar con una instalación operativa
una importación local y otra por Internet que fallen de forma controlada. Debe
revisarse consola, log general, log de error y correo de soporte para confirmar
que la traza es única, que no se muestran secretos y que los detalles del feed
son suficientes para soporte.

## Validación del bloque aplicado

Actualización 2026-09-27: `ImportFailureClassifier` e `ImportErrorCategory`
clasifican SQLState de autenticación y conectividad, tipos Java de red,
excepciones propias y la fase del pipeline. El texto heredado solo se utiliza
como compatibilidad final. El correo de soporte incluye categoría, fase,
número y nombre del feed, sin rutas locales ni URLs completas.

Ejecutado el 2026-09-27:

```text
.\mvnw.cmd test -Dtest=AbstractOpenDataBaseEmailConfigurationTest
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

La suite completa debe ejecutarse al cerrar el conjunto de cambios antes de crear la siguiente versión.
