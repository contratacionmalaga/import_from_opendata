package local.jarios;

import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.enums.EntryOpcion;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiServiceException;
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

    /** Mensaje insertar */
    private final String MSG_INSERTAR = "No figura en la base de datos.";

    /** Mensaje actualizar */
    private final String MSG_ACTUALIZAR = "Figura en la base de datos con fecha anterior.";

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
     * @throws MiServiceException si hay errores al consultar datos de base de datos
     * @throws MiParseException   si hay errores al parsear feeds remotos
     */
    @Override
    protected void parsearAtomsFeeds(Log miLog, TipoSindicacion tipoSindicacion, Entry newestEntry)
            throws MiServiceException, MiParseException {

        // Parsear feeds remotos y cargar resultado en VariablesGlobales
        FeedHelper.parsearFeedsDesdeInternet(miLog, tipoSindicacion, newestEntry);

        // Recupero el Map con los Entries importados desde Atoms
        Map<String, Entry> mapEntryFromAtoms = VariablesGlobales.getMapEntriesFromAtoms();
        log.info("[parsearAtomsFeeds] - Total entries obtenidos desde INTERNET: {}", mapEntryFromAtoms.size());
        mapEntryFromAtoms.values().forEach(e -> log.info("[INTERNET] {}", e.toStringResumido()));

        // Obtengo el Map con los Entries actuales en Base de Datos
        ServicePrincipal servicePrincipal = new ServicePrincipalImpl();
        Map<String, Entry> mapEntryFromBD = servicePrincipal.getMapEntries(tipoSindicacion);
        log.info("[parsearAtomsFeeds] - Total entries en base de datos: {}", mapEntryFromBD.size());
        mapEntryFromBD.values().forEach(e -> log.info("[BASE_DATOS] {}", e.toStringResumido()));

        // Comparo ambos Mapas y me quedo con otro Map que tenga aquellos que son mayores que los existentes en
        //      Base de datos o lo que no existan en Base de Datsos
        mapEntryFromAtoms.forEach((idEntry, entryInternet) -> {
            Entry entryBD = mapEntryFromBD.get(idEntry);
            log.info("[COMPARACIÓN] Entry desde internet: {}", entryInternet.toStringResumido());

            if (entryBD == null) {
                // No existe en BD → insertar
                resultado.put(idEntry, entryInternet);
                log.info("[NUEVO] No figura en la BD → insertar.");
                VariablesGlobales.getListHistoricos()
                        .add(new Historico(entryInternet, EntryOpcion.INSERTAR, MSG_INSERTAR));
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
                            .add(new Historico(entryInternet, EntryOpcion.ACTUALIZAR, MSG_ACTUALIZAR));
                } else {
                    log.info("[SIN CAMBIOS] La versión en BD es más reciente o igual.");
                }
            }
        });

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
