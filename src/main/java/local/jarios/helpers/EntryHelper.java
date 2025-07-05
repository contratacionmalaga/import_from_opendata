package local.jarios.helpers;

import local.jarios.common.util.Mensajes;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.placsp.*;
import local.jarios.enums.EntryOpcion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiInvalidDateFormatException;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.exceptions.MiServiceException;
import local.jarios.services.ServicePrincipal;
import local.jarios.services.ServicePrincipalImpl;
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
     * @param entryEnMemoria Entry que estamos analizando y que vamos a actualizar en el MAP
     * @param entryEnMapBd Entry existente en el MAP y que vamos a remover para poner el anterior
     */
    private static void actualizarEntryEnMAP(Entry entryEnMemoria, Entry entryEnMapBd, String motivo, Estadistica estadistica) {

        // Borro el entryMapBaseDatos del MAP
        VariablesGlobales.getMapBaseDatos().remove(entryEnMapBd.getIdEntry());
        log.debug("[actualizarEntryEnMAP] - Remove Entry: {}", entryEnMapBd.getIdEntry());
        Historico historicoEnMapBd = new Historico(entryEnMapBd, EntryOpcion.BORRAR, motivo);
        log.debug("[actualizarEntryEnMAP] - Creación de Histórico: {}", historicoEnMapBd);
        VariablesGlobales.getListHistoricos().add(historicoEnMapBd);

        // Añado el newEntry al MAP
        VariablesGlobales.getMapBaseDatos().put(entryEnMemoria.getIdEntry(), entryEnMemoria);
        log.debug("[actualizarEntryEnMAP] - Put Entry: {}", entryEnMemoria.getIdEntry());
        Historico historicoEnMemoria= new Historico(entryEnMemoria, EntryOpcion.INSERTAR, motivo);
        log.debug("[actualizarEntryEnMAP] - Creación de Histórico: {}", historicoEnMemoria);

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

        log.info("[procesarEntry] - {} - {}", entry.toStringResumido(), evaluacionFiltrosEntry);
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

            Entry entryEnMap = VariablesGlobales.getMapBaseDatos().get(entry.getIdEntry());
            log.debug("[procesarEntrySegunExistencia] - Datos del Entry en el MapBd: {}", entryEnMap);

            procesarEntryExistenteEnMAP(entry, entryEnMap, estadistica);

        } else {
            // No existe en el MAP, lo agregamos

            log.debug("[procesarEntrySegunExistencia] - NO existe en en el Map.");

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
     * Procesado de un Entry que figura en el MapBd
     *      Comparo el valor del campo Updated asociado al Entry en Memoria y al Entry en la Base de Datos y
     *          me quedo con la versión más moderna del Entry
     * @param entryEnMemoria Objeto Entry que se está procesando
     * @param entryEnMap Objeto Entry que figura en el Map
     * @param estadistica Objeto Estadistica
     */
    private static void procesarEntryExistenteEnMAP(Entry entryEnMemoria, Entry entryEnMap, Estadistica estadistica) {

        //
        boolean isEntryEnMemoriaBeforeEntryEnMap = entryEnMemoria.getUpdated().isBefore(entryEnMap.getUpdated());
        log.debug("[procesarEntryExistenteEnMAP - isEntryEnMemoriaBeforeEntryEnMap: {}", isEntryEnMemoriaBeforeEntryEnMap);

        String motivo;

        if (isEntryEnMemoriaBeforeEntryEnMap) {
            // La fecha del EntryEnMemoria es ANTERIOR a la fecha del EntryEnMap

            motivo = String.format(
                            "Rechazado. Fecha(EntryEnMemoria) - '%s' isBefore Fecha(EntryEnMap) - '%s'.",
                            entryEnMemoria.getUpdated(),
                            entryEnMap.getUpdated());

            estadistica.aumentarNEntryRechazados();

            Historico historico = new Historico(entryEnMemoria, EntryOpcion.RECHAZADO, motivo);

            VariablesGlobales.getListHistoricos().add(historico);

        } else {
            // La fecha del Entry en el MAP es más antigua, actualizamos el Entry en el MAP

            motivo = String.format(
                            "Modificar Entry en Map. Fecha(EntryEnMemoria) - '%s' isAfter Fecha(EntryEnMap) - '%s'.",
                            entryEnMemoria.getUpdated(),
                            entryEnMap.getUpdated());

            actualizarEntryEnMAP(entryEnMemoria, entryEnMap, motivo, estadistica);
        }

        log.debug("[procesarEntryExistenteEnMAP] - {}", motivo);
    }

    /**
     * Devuelve el feed más reciente en base al tipo de sindicación.
     */
    public static Entry getNewestEntry(TipoSindicacion tipoSindicacion) throws MiServiceException {
        ServicePrincipal servicePrincipal = new ServicePrincipalImpl();
        Entry entry = servicePrincipal.getNewestEntry(tipoSindicacion);
        log.debug("[getNewestEntry] - TipoSindicacion: {}. NewestEntry: {}", tipoSindicacion, entry.toStringResumido());
        return entry;
    }
}
