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
import java.util.*;

@Slf4j
public class OpenDataInternet extends AbstractOpenData {

    @Getter
    Map<String, Entry> resultado = new HashMap<>();

    @Override
    protected TipoSindicacion getTipoSindicacion() {

        return TipoSindicacionHelper.getTipoSindicacionRemota();
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

        Map<String, Entry> mapEntriesFromAtoms = VariablesGlobales.getMapEntriesFromAtoms();
        log.info("[parsearAtomsFeeds] - Nª Entries desde INTERNET ('{}')", VariablesGlobales.getMapEntriesFromAtoms().size());
        mapEntriesFromAtoms.values().forEach(e->log.info("[parsearAtomsFeeds] - Entry desde INTERNET: {}", e.toStringResumido()));

        // 3. Obtener el mapa con los Entry de la base de datos
        ServicePrincipal servicePrincipal = new ServicePrincipalImpl();
        Map<String, Entry> mapEntriesFromBaseDatos = servicePrincipal.getMapEntries(getTipoSindicacion());
        log.info("[parsearAtomsFeeds] - Listado de Entries almacenados en DB ('{}')", mapEntriesFromBaseDatos.size());
        mapEntriesFromBaseDatos.values().forEach(e->log.info("[parsearAtomsFeeds] - Entry en DB: {}", e.toStringResumido()));

        // 4. Calcular intersección -- Aquellos Entry que están en los dos y en el Map entriesDesdeInternet
        //      tiene una fecha de Updated más reciente
        mapEntriesFromAtoms.forEach((idEntry, entryFromAtom) -> {

            Entry entryFromBaseDatos = mapEntriesFromBaseDatos.get(idEntry);
            log.info("[parsearAtomsFeeds] - Entry from Atom: {}", entryFromAtom.toStringResumido());

            if (entryFromBaseDatos == null) {

                // No existe en BD → insertar
                resultado.put(idEntry, entryFromAtom);
                log.info("[parsearAtomsFeeds] - No figura en la BD.");
                Historico historico = new Historico(entryFromAtom, EntryOpcion.INSERTAR, "No figura en la base de datos.");
                VariablesGlobales.getListHistoricos().add(historico);

            } else {

                // Existe en BD → comparar updated
                LocalDateTime updatedFromEntry = entryFromAtom.getUpdated();
                LocalDateTime updatedFromBaseDatos = entryFromBaseDatos.getUpdated();

                if (updatedFromBaseDatos == null ||
                        (updatedFromEntry != null && updatedFromEntry.isAfter(updatedFromBaseDatos))) {
                    // Internet tiene una versión más nueva → reemplazar, manteniendo UUID
                    entryFromAtom.setId(entryFromBaseDatos.getId());
                    resultado.put(idEntry, entryFromAtom);
                    log.info("[parsearAtomsFeeds] - Figura en la base de datos con fecha anterior.");
                    Historico historico = new Historico(entryFromAtom, EntryOpcion.ACTUALIZAR, "Figura en la base de datos con fecha anterior.");
                    VariablesGlobales.getListHistoricos().add(historico);
                }
                // Si la BD está más actualizada → no hacer nada
            }
        });

        estadistica.aumentarNEntryGrabados(resultado.size());

        log.info("[parsearAtomsFeeds] - Nº Registros a enviar a la base de datos ('{}')",
                this.resultado.size());
        this.resultado.forEach(
                (idEntry, entry) ->
                        log.info("[parsearAtomsFeeds] - Entry a Base Datos: {}", entry.toStringResumido()
                ));
    }

    public static void main(String[] args) {

        new OpenDataInternet().procesar();
    }
}
