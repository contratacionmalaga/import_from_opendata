package local.jarios;

import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.enums.EntryOpcion;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiServiceException;
import local.jarios.helpers.EntryHelper;
import local.jarios.helpers.FeedHelper;
import local.jarios.helpers.TipoSindicacionHelper;
import local.jarios.services.ServicePrincipal;
import local.jarios.services.ServicePrincipalImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase encargada de procesar y comparar entradas (Entry) obtenidas desde fuentes remotas (INTERNET)
 * con las almacenadas en base de datos, determinando cuáles deben insertarse o actualizarse.
 * Extiende {@link AbstractOpenData} para integrar el flujo común de procesamiento de sindicación.
 *
 * @author Juan
 * @since 04/07/2025
 */
@Slf4j
public class OpenDataInternet extends AbstractOpenData {

    /**
     * Mapa que contiene los {@link Entry} que deben ser insertados o actualizados en la base de datos.
     * La clave es el campo idEntry (no UUID).
     */
    @Getter
    private final Map<String, Entry> resultado = new HashMap<>();

    /**
     * Obtiene el tipo de sindicación remota (INTERNET).
     *
     * @return tipo de sindicación
     */
    @Override
    protected TipoSindicacion getTipoSindicacion() {
        return new TipoSindicacionHelper().getTipoSindicacionRemota();
    }

    /**
     * Devuelve el lugar de importación como {@link LugarImportacion#INTERNET}.
     */
    @Override
    protected LugarImportacion getLugarImportacion() {
        return LugarImportacion.INTERNET;
    }

    /**
     * Procesa los feeds obtenidos desde internet y los compara con la base de datos para
     * determinar qué entradas se deben insertar o actualizar.
     *
     * @param miLog       objeto de log del proceso
     * @param estadistica objeto de estadísticas del proceso
     * @throws MiServiceException si hay errores al consultar datos de base de datos
     * @throws MiParseException   si hay errores al parsear feeds remotos
     */
    @Override
    protected void parsearAtomsFeeds(Log miLog, Estadistica estadistica) throws MiServiceException, MiParseException {
        log.info("[parsearAtomsFeeds] - Iniciando procesamiento de AtomsFeeds desde INTERNET.");

        // 1. Obtener el tipo de sindicación y el Entry más reciente desde BD
        TipoSindicacion tipoSindicacion = getTipoSindicacion();
        Entry newestEntry = EntryHelper.getNewestEntry(tipoSindicacion);
        if (newestEntry != null) {
            log.info("[parsearAtomsFeeds] - NewestEntry encontrado: {}", newestEntry.toStringResumido());
        } else {
            log.info("[parsearAtomsFeeds] - No se encontró un Entry previo en la BD. Se procesarán todos los entries.");
        }

        // 2. Parsear feeds remotos y cargar resultado en VariablesGlobales
        FeedHelper.parsearFeedsDesdeInternet(miLog, newestEntry, estadistica);
        Map<String, Entry> entriesInternet = VariablesGlobales.getMapEntriesFromAtoms();
        log.info("[parsearAtomsFeeds] - Total entries obtenidos desde INTERNET: {}", entriesInternet.size());
        entriesInternet.values().forEach(e -> log.info("[INTERNET] {}", e.toStringResumido()));

        // 3. Obtener entries actuales desde BD
        ServicePrincipal servicePrincipal = new ServicePrincipalImpl();
        Map<String, Entry> entriesBD = servicePrincipal.getMapEntries(tipoSindicacion);
        log.info("[parsearAtomsFeeds] - Total entries en base de datos: {}", entriesBD.size());
        entriesBD.values().forEach(e -> log.info("[BASE_DATOS] {}", e.toStringResumido()));

        // 4. Comparar y decidir qué insertar o actualizar
        entriesInternet.forEach((idEntry, entryInternet) -> {
            Entry entryBD = entriesBD.get(idEntry);
            log.info("[COMPARACIÓN] Entry desde internet: {}", entryInternet.toStringResumido());

            if (entryBD == null) {
                // No existe en BD → insertar
                resultado.put(idEntry, entryInternet);
                log.info("[NUEVO] No figura en la BD → insertar.");
                VariablesGlobales.getListHistoricos()
                        .add(new Historico(entryInternet, EntryOpcion.INSERTAR, "No figura en la base de datos."));
            } else {
                // Existe → comparar fechas
                LocalDateTime updatedInternet = entryInternet.getUpdated();
                LocalDateTime updatedBD = entryBD.getUpdated();

                if (updatedBD == null || (updatedInternet != null && updatedInternet.isAfter(updatedBD))) {
                    // Actualización necesaria
                    entryInternet.setId(entryBD.getId()); // conservar UUID
                    resultado.put(idEntry, entryInternet);
                    log.info("[ACTUALIZAR] Existe en BD pero es más antiguo → actualizar.");
                    VariablesGlobales.getListHistoricos()
                            .add(new Historico(entryInternet, EntryOpcion.ACTUALIZAR, "Figura en la base de datos con fecha anterior."));
                } else {
                    log.info("[SIN CAMBIOS] La versión en BD es más reciente o igual.");
                }
            }
        });

        // 5. Actualizar estadísticas
        estadistica.aumentarNEntryGrabados(resultado.size());

        // 6. Log final
        log.info("[FINAL] Nº total de registros a enviar a la BD: {}", resultado.size());
        resultado.forEach((id, entry) ->
                log.info("[A_GRABAR] {}", entry.toStringResumido()));
    }

    /**
     * Método main para ejecutar el procesamiento directamente desde consola o entorno standalone.
     */
    public static void main(String[] args) {
        new OpenDataInternet().procesar();
    }
}
