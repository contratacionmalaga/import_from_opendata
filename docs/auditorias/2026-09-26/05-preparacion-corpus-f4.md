# F4 — Preparación de corpus ATOM local

Fecha: 2026-09-26. Estado: preparado; pendiente de generar y ejecutar los corpus reales.

## Decisión de corte

No se aplicarán filtros por fecha, NIF, código postal ni ningún otro campo del expediente para formar las muestras de rendimiento. El corpus se forma con páginas ATOM completas, siguiendo el `link rel="next"` desde el fichero inicial configurado para cada tipo de sindicación.

El script copia los feeds a un directorio nuevo y, cuando el acumulado alcanza el máximo solicitado, elimina únicamente el `link rel="next"` de la copia de esa última página. La cadena queda terminada de forma válida para el importador. Por ello, una muestra solicitada de 1.000 entradas puede contener un número ligeramente superior: nunca se parte una página ni se modifica una entrada.

Para el volumen completo, usar `MaxEntries=0`: se copia toda la cadena sin alterar enlaces.

## Preparador

El preparador [preparar_corpus_atom.ps1](../../../scripts/preparar_corpus_atom.ps1) no modifica la fuente. Rechaza un destino existente, enlaces fuera del directorio fuente, ficheros ausentes y ciclos. Escribir el corpus fuera del repositorio para que los ATOM y sus resultados no entren en control de versiones.

Ejemplo para MAYORES, con una muestra que alcance al menos 1.000 entradas:

```powershell
.\scripts\preparar_corpus_atom.ps1 `
  -SourceDirectory 'D:\atoms\may' `
  -DestinationDirectory 'D:\opendata-benchmark\corpus\mayores-1000' `
  -InitialFile 'licitacionesPerfilesContratanteCompleto3.atom' `
  -MaxEntries 1000
```

El resultado contiene `manifest.json` con las páginas copiadas, entradas y bajas acumuladas, y si se eliminó el enlace final. Ese manifiesto es la referencia del corpus usado en cada corrida.

## Configuración aislada de ejecución

No se utilizarán ni modificarán los `properties` operativos. El preparador [preparar_configuracion_f4.ps1](../../../scripts/preparar_configuracion_f4.ps1) copia las credenciales locales sin mostrarlas, sustituye la base de datos por `opendata_prueba`, configura el directorio del corpus, fija `hibernate.hbm2ddl.auto=validate` y desactiva ambos emails con `app.email.enabled=false`.

El directorio del corpus debe contener el subdirectorio de tipo (`may` para MAYORES). Ejemplo:

```powershell
.\scripts\preparar_configuracion_f4.ps1 `
  -SourcePropertiesDirectory .\properties `
  -DestinationPropertiesDirectory 'D:\opendata-benchmark\runs\mayores-1000\properties' `
  -CorpusRoot 'D:\opendata-benchmark\corpus' `
  -TipoSindicacion MAYORES `
  -DatabaseName opendata_prueba
```

Si las credenciales de pruebas difieren de las operativas, se pueden proporcionar mediante `-DatabaseUser` y `-DatabasePassword`; no deben incluirse en documentación, commits ni manifiestos.

Para un experimento de batching, indicar `-HibernateBatchSize 300`; el valor se guarda en el manifiesto de configuración de la corrida.

## Secuencia de cargas

1. Preparar corpus de 1.000, 10.000 y 100.000 entradas con este mecanismo. Para 875.000 o más, copiar la cadena completa.
2. Para cada corpus, apuntar una configuración aislada a `opendata_prueba` y usar `hibernate.hbm2ddl.auto=validate`.
3. Restaurar el mismo estado inicial de MariaDB antes de cada repetición: esquema vacío para carga desde cero; copia base para incremental.
4. Ejecutar primero LOCAL. INTERNET se probará después sirviendo una copia controlada del mismo corpus, para que la red externa no altere las medidas.
5. Conservar el manifiesto, log, versión del JAR y métricas de la corrida en una carpeta de resultados separada.
