package local.jarios.helpers;

import local.jarios.common.util.Mensajes;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.placsp.*;
import local.jarios.enums.EntryOpcion;
import local.jarios.exceptions.MiInvalidDateFormatException;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.VariablesGlobales;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * @author Juan Antonio
 */
@Slf4j
public final class EntryHelper {

    private EntryHelper() {/* CONSTRUCTOR VACÍO */}

    /**
     * Función que dado un Entry nos devuelve el valor del campo objeto asociado al expediente
     *
     * @param entry Entry del que devolvemos el valor ProcurementProjectName
     * @return Cadena con el Objeto del Entry. NO devuelvo Optional puesto que el objeto es obligatorio.
     */
    public static String getObjetoFromEntry(Entry entry) {

        return entry.getListContractFolderStatus().stream()
                .findFirst()
                .map(ContractFolderStatus::getProcurementProject)
                .map(ProcurementProject::getName)
                .orElse(Constantes.CADENA_VACIA);
    }

    /**
     * Función que dado un Entry nos devuelve el valor del campo NUTS de realización del expediente
     *
     * @param entry Entry del que devolvemos el valor RealizedLocationCountrySubCode
     * @return Optional con el código NUTS (puede que no esté incluído en el modelo, motivo por el que uso Optional)
     */
    public static Optional<String> getNutsFromEntry(Entry entry) {

        return entry.getListContractFolderStatus().stream()
                .findFirst()
                .map(ContractFolderStatus::getProcurementProject)
                .map(ProcurementProject::getRealizedLocation)
                .map(Location::getCountrySubentityCode);
    }

    /**
     * Función que dado un Entry nos devuelve el valor del campo IdPlataforma del expediente
     *
     * @param entry Entry del que devolvemos el valor PartyIdentificationIdPlataforma
     * @return Optional con el código IdPlataforma (puede que no esté incluído en el modelo, motivo del Optional)
     */
    public static Optional<String> getIdPlataformaFromEntry(Entry entry) {

        return entry.getListContractFolderStatus().stream()
                .findFirst()
                .map(ContractFolderStatus::getLocatedContractingParty)
                .map(LocatedContractingParty::getParty)
                .map(Party::getPartyIdentification)
                .map(PartyIdentification::getIdPlataforma);
    }

    /**
     * Función que actualiza en Entry en la base de datos (MAP de VariablesGlobales)
     *
     * @param newEntry Entry que estamos analizando y que vamos a actualizar en el MAP
     * @param entryMapBaseDatos Entry existente en el MAP y que vamos a remover para poner el anterior
     */
    private static void actualizarEntryEnMAP(
            Entry newEntry,
            Entry entryMapBaseDatos,
            Estadistica estadistica) {

        // Borro el entryMapBaseDatos del MAP
        VariablesGlobales.getMapBaseDatos().remove(entryMapBaseDatos.getIdEntry());
        log.info("[actualizarEntryEnMAP] - Remove Entry: {}", entryMapBaseDatos.getIdEntry());

        // Añado el newEntry al MAP
        VariablesGlobales.getMapBaseDatos().put(newEntry.getIdEntry(), newEntry);
        log.info("[actualizarEntryEnMAP] - Put Entry: {}", newEntry.getIdEntry());

        // Actualizo las estadísticas
        estadistica.aumentarNEntryActualizados();
    }

    /**
     * Función encarga del procesamiento de los objetos Entry
     *      Proceso si el Entry cumple con los filtros que estuvieran definidos al inicio de la ejecución
     * @param entry Objeto Entry que se está procesando
     * @param estadistica Objeto Estadistica
     * @throws MiInvalidDateFormatException Excepción en caso de error
     */
    public static void procesarEntry(Entry entry, Estadistica estadistica) {

        log.info("[procesarEntry] - {}", entry.getIdEntry());
        String evaluacionFiltrosEntry = FiltroHelper.entryCumpleFiltros(entry);
        log.debug("[procesarEntry] - Evaluación de los filtros del entry: {}", evaluacionFiltrosEntry);

        if (evaluacionFiltrosEntry.equals(Mensajes.ENTRY_CUMPLE_FILTROS)) {

            // Entry cumple con los filtros --> PROCESADO
            estadistica.aumentarNEntryProcesados();

            // Inicio el procesamiento del entry
            procesarEntrySegunExistencia(entry, estadistica);

        } else {

            // Entry NO cumple con los filtros --> RECHAZADO
            estadistica.aumentarNEntryRechazados();

            // Creo un histórico asociado al Entry
            Historico historico = new Historico(entry, EntryOpcion.RECHAZADO, evaluacionFiltrosEntry);

            // Añado el histórico a la lista de históricos de esta ejecución
            VariablesGlobales.getListHistoricos().add(historico);
        }
    }

    /**
     * Procesa un Entry que ya se que cumple con los filtros
     *      Analizo si existe en MapBaseDatos y dependiendo de si existe o no actúo de una forma u otra
     *
     * @param entry Objeto entry que será procesado
     * @param estadistica Objeto para la gestión de las estadísticas
     */
    private static void procesarEntrySegunExistencia(Entry entry, Estadistica estadistica) {

        //
        boolean existeEntryEnMapBd = VariablesGlobales.getMapBaseDatos().containsKey(entry.getIdEntry());
        log.debug("[procesarEntrySegunExistencia] - Entry pertenece al MapBd: {}", existeEntryEnMapBd);

        if (existeEntryEnMapBd) {
            // Si existe en el MAP, lo procesamos

            log.info("[procesarEntrySegunExistencia] - Existente en el Map Entry.");
            Entry entryEnMap = VariablesGlobales.getMapBaseDatos().get(entry.getIdEntry());
            procesarEntryExistenteEnMAP(entry, entryEnMap, estadistica);

        } else {
            // No existe en el MAP, lo agregamos

            log.info("[procesarEntrySegunExistencia] - NO existe en el Map Entry:}");

            // Lo agrego al MAP
            VariablesGlobales.getMapBaseDatos().put(entry.getIdEntry(), entry);

            //
            Historico historico =  new Historico(entry, EntryOpcion.INSERTAR, Mensajes.ENTRY_NUEVO);

            //
            VariablesGlobales.getListHistoricos().add(historico);

            // Aumento las estadísticas
            estadistica.aumentarNEntryGrabados();
        }
    }

    /**
     *
     * @param entryEnMemoria Objeto Entry que se está procesando
     * @param entryEnMap Objeto Entry que figura en el Map
     * @param estadistica Objeto Estadistica
     */
    private static void procesarEntryExistenteEnMAP(
            Entry entryEnMemoria,
            Entry entryEnMap,
            Estadistica estadistica) {

        //
        if (entryEnMemoria.getUpdated().isBefore(entryEnMap.getUpdated())) {
            // La fecha del entry en el MAP es más nueva, descartamos el Entry

            log.info("[procesarEntryExistenteEnMAP] - Rechazado. Fecha(EntryEnMemoria) - '{}' isBefore Fecha(EntryEnMap) - '{}'.",
                    entryEnMemoria.getUpdated(), entryEnMap.getUpdated());

            estadistica.aumentarNEntryRechazados();

        } else {
            // La fecha del Entry en el MAP es más antigua, actualizamos el Entry en el MAP

            //
            log.info("[procesarEntryExistenteEnMAP] - Modificar en el MAP. Fecha(EntryEnMemoria) - '{}' isAfter Fecha(EntryEnMap) - '{}'.",
                    entryEnMemoria.getUpdated(), entryEnMap.getUpdated());

            actualizarEntryEnMAP(entryEnMemoria, entryEnMap, estadistica);
        }
    }
}
