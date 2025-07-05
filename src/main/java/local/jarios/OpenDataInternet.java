package local.jarios;

import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.Estadistica;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class OpenDataInternet extends AbstractOpenData {

    @Getter
    private Set<Entry> entriesAEliminar = Collections.emptySet();

    @Override
    protected TipoSindicacion getTipoSindicacion() {

        return TipoSindicacionHelper.getTipoSindicacion(false);
    }

    @Override
    protected LugarImportacion getLugarImportacion() {

        return LugarImportacion.INTERNET;
    }

    @Override
    protected void parsearAtomsFeeds(Log miLog, Estadistica estadistica) throws MiServiceException, MiParseException {

        // 0. Tipo de Sindicacion
        TipoSindicacion tipoSindicacion = getTipoSindicacion();

        // 1. Obtengo el newestFeed según el tipo de sindicación
        Entry newestEntry = EntryHelper.getNewestEntry(tipoSindicacion);
        if (newestEntry != null) {
            log.info("[parsearAtomsFeeds] - NewestEntry - {}", newestEntry.toStringResumido());
        } else {
            log.info("[parsearAtomsFeeds] - NewestEntry es null. Se procesarán todos los entries.");
        }

        // 2. Obtener el map de entries parseados desde internet (guardados en VariablesGlobales)
        log.info("[parsearAtomsFeeds] - Inicio del procesado de los AtomsFeeds.");
        FeedHelper.parsearFeedsDesdeInternet(miLog, newestEntry, estadistica);

        Map<String, Entry> entriesDesdeInternet = VariablesGlobales.getMapBaseDatos();
        log.info(
                "[parsearAtomsFeeds] - Listado de Entries almacenados en el map ('{}')",
                VariablesGlobales.getMapBaseDatos().size());
        entriesDesdeInternet.values().forEach(e->log.info(e.toStringResumido()));

        // 3. Obtener el mapa con los Entry de la base de datos
        Map<String, Entry> entriesEnBaseDatos;
        ServicePrincipal servicePrincipal = new ServicePrincipalImpl();
        entriesEnBaseDatos = servicePrincipal.getMapEntries(getTipoSindicacion());
        log.info(
                "[parsearAtomsFeeds] - Listado de Entries almacenados en la base de datos ('{}')",
                entriesEnBaseDatos.size());
        entriesEnBaseDatos.values().forEach(e->log.info("[parsearAtomsFeeds] - {}", e.toStringResumido()));

        // 4. Calcular intersección -- Aquellos Entry que están en los dos y en el Map entriesDesdeInternet
        //      tiene una fecha de Updated más reciente
        this.entriesAEliminar = entriesDesdeInternet.entrySet().stream()
                .filter(e -> {
                    Entry baseEntry = entriesEnBaseDatos.get(e.getKey());
                    return baseEntry != null && e.getValue().getUpdated().isAfter(baseEntry.getUpdated());
                })
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        log.info(
                "[parsearAtomsFeeds] - Listado de los Entries a actualizar en la base de datos ('{}')",
                this.entriesAEliminar.size());
        this.entriesAEliminar.forEach(e->log.info("[parsearAtomsFeeds] - {}", e.toStringResumido()));
    }

    public static void main(String[] args) {

        new OpenDataInternet().procesar();
    }
}
