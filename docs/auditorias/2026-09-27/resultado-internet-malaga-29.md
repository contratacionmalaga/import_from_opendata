# Importaciones desde Internet — Málaga, filtro 29

Destino: `opendata_malaga`. Ejecutable: `opendata_con_filtros-8.1.1-internet.jar`. Todas las cargas usan INTERNET, filtro postal 29 y comparación con los snapshots locales.

## Datos antes de empezar

| Tipo | Entries locales |
|---|---:|
| MAY | 37.649 |
| MEN | 99.590 |
| EMP | 192 |
| CPM | 154 |

## Incidencias corregidas

La configuración general apuntaba a Pliegos, no tenía filtro y empleaba `hibernate.hbm2ddl.auto=create`. Se usó una configuración aislada en `benchmark/internet-malaga-20260927/properties`, con destino Málaga, filtro 29 y `validate`, sin modificar los properties generales. Memoria ajustada de 48 GB iniciales a `-Xms2g -Xmx12g`. Correos desactivados. Certificados: `-Djavax.net.ssl.trustStoreType=Windows-ROOT`. Los argumentos `-D` deben entrecomillarse en PowerShell.

La validación inicial detectó columnas ausentes en `tendering_process`. Tras respaldo de las 137.585 filas y su estructura en `benchmark/internet-malaga-20260927/tendering_process-before.sql` (hash en `backup-sha256.txt`), se añadieron las cuatro columnas `original_contracting_system_*` de la migración de 26/09/2026 y `participation_request_reception_period` de v7.6.0. El recuento de la tabla se conservó. No se recrearon tablas ni bases.

## Interrupción y reanudación

El primer intento MAY se interrumpió a las 16:46 del 27/09, en el feed 2776. No quedaban Java ni supervisor activos ni sesiones de terminal; no se registró una excepción de aplicación. La causa exacta no está confirmada. La BD conservaba sus 137585 entries y no había log INTERNET nuevo. Se preservó la consola anterior y se reanudó desde el origen con un supervisor de PowerShell oculto independiente del terminal y un mutex contra duplicados.

## Compatibilidad de auditoría legacy

EMP alcanzó correctamente el corte en dos feeds, pero sus dos primeros intentos de escritura hicieron rollback por columnas antiguas obligatorias: log.updated_at y configuracion.created_at. Se respaldaron las cuatro filas de log y el esquema legacy. Se permitió NULL en log.updated_at y se añadieron DEFAULT CURRENT_TIMESTAMP(6) reversibles a 62 columnas de auditoría en 31 tablas antiguas, conservando sus columnas y datos. Se excluyeron entry y log.created_at. Scripts y respaldo: audit-schema-before.sql, audit-compatibility.sql, audit-rollback.sql y log-before-updated-at.sql en benchmark/internet-malaga-20260927; apply y rollback también en docs/auditorias/2026-09-27. EMP se repitió y confirmó la transacción.

## Resultados confirmados en MariaDB

| Tipo | Log | Log creado | Entries del lote | Feeds | Insertar | Actualizar | Rechazar | Bajas del lote | Parseo | Persistencia |
|---|---|---|---:|---:|---:|---:|---:|---:|---|---|
| ENCARGOS | bb2645bb-3777-466b-81ef-7f16afa1912b | 2026-09-27 19:23:14.817090 | 2 | 2 | 2 | 0 | 597 | 0 | 0h 0m 3s 521ms | 0h 0m 0s 178ms |
| MAYORES | 076c77c5-6ff5-452c-9989-08e75835f79e | 2026-09-27 19:31:35.436425 | 1961 | 119 | 706 | 1255 | 56585 | 1 | 0h 6m 52s 46ms | 0h 0m 16s 320ms |
| CONSULTAS | 1c4fe51c-2119-45d1-b5fa-0db063a98bbe | 2026-09-27 19:32:05.102787 | 8 | 1 | 8 | 0 | 43 | 0 | 0h 0m 2s 487ms | 0h 0m 0s 230ms |
| MENORES | 2fd32b45-8931-4e49-a1e4-672a3a6ccc64 | 2026-09-27 19:34:54.589569 | 4516 | 96 | 4500 | 16 | 43142 | 0 | 0h 2m 37s 352ms | 0h 0m 5s 456ms |

## Recuentos actuales de expedientes

| Tipo | Entries en BD | Última actualización del origen |
|---|---:|---|
| CONSULTAS | 162 | 2026-09-18 08:52:10.699000 |
| ENCARGOS | 194 | 2026-09-17 10:48:27.275000 |
| MAYORES | 38355 | 2026-09-22 17:28:03.294000 |
| MENORES | 104090 | 2026-09-22 14:35:37.816000 |

Comprobaciones tras cada carga: código de salida 0; log INTERNET nuevo; filtro 29 almacenado; estadística del lote presente; entries y feeds del lote coinciden exactamente con su estadística; cero entries sin feed; cero duplicados de entry_id y entry_id_corto. Los detalles están en `benchmark/internet-malaga-20260927/*-verification.txt`. Los logs técnicos están en `logs/import-from-opendata*_malaga29-*.log`.

## Duración y opciones ante problemas

En 8.1.0 `FeedHelper` seguía navegando después de `newestEntry` para recoger bajas de feeds posteriores. El usuario pidió detener y corregir ese comportamiento. La nueva versión 8.1.1 termina la navegación al detectar un entry estrictamente anterior al máximo updated local; mantiene fechas iguales y las bajas de feeds ya leídos. No descarga feeds posteriores al corte. Se actualizaron Readme.md y docs/releases/v8.1.1.md; la batería completa pasó con 82 pruebas, cero fallos/errores y dos omitidas.

Reintentos HTTP usados: `app.http.max_retries=5`, `app.http.retry_delay_ms=60000`, `app.http.request_delay_ms=500`. Ante limitación del servidor, valorar pausa de 2500 ms y base de 120000 ms; requiere reiniciar la ejecución pendiente. Ante PKIX, revisar Windows-ROOT. Ante validación del esquema, aplicar únicamente la migración faltante con respaldo, manteniendo validate.

Estado: las cuatro importaciones están confirmadas y verificadas.

## Cierre y comprobación del corte

Todas las cargas confirmaron su transacción y fueron verificadas antes de continuar. Orden real: EMP, MAY, CPM y MEN. Fin global verificado: 27/09/2026 19:35:03 (Europe/Madrid).

| Tipo | Feed del corte | Commit confirmado en log técnico |
|---|---:|---|
| EMP | 2 | 19:23:14.988 |
| MAY | 119 | 19:31:51.746 |
| CPM | 1 | 19:32:05.321 |
| MEN | 96 | 19:35:00.035 |

Los tiempos de la tabla de resultados proceden de log.created_at, que se crea antes del commit; la confirmación se acreditó separadamente con los mensajes técnicos anteriores. Total final: 142801 entries, incremento neto5216 respecto a137585. La baja de MAY es un tombstone registrado, no debe interpretarse automáticamente como un expediente eliminado; el recuento neto MAY aumentó exactamente en706. No se encontraron duplicados ni entries sin feed.

La consola redirigida de MAY dejó de actualizarse al cambiar su supervisor para adelantar CPM a MEN. El proceso Java siguió activo y su log técnico completo (import-from-opendata_malaga29-MAY-20260927-192435.log) confirma el corte en119 y el commit. MAY-exit.txt contiene0 y MAY-verification.txt confirma la escritura en MariaDB.

Se detiene el seguimiento temporal al concluir las cuatro importaciones.
