package local.jarios.helpers;

import local.jarios.common.util.PropertiesKeys;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.placsp.*;
import local.jarios.exceptions.MiInvalidDateFormatException;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.Mensajes;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
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
     * Función que devuelve TRUE | FALSE para indicar SI |NO cumple, el Entry, con todos los filtros definidos
     *
     * @param entry Entry que analizamos
     * @return Valor TRUE | FALSE
     */
    private static String entryCumpleConLosFiltros(Entry entry)
            throws MiInvalidDateFormatException {

        PropertiesManagerService propertiesManagerService = PropertiesManagerServiceImpl.getInstance();

        String msg;

        // FILTRO SQL
        var filtroSql = propertiesManagerService.getProperty(Constantes.FILTER_PROPERTIES, PropertiesKeys.FILTRO_SQL);
        if (!filtroSql.isBlank() && !FiltroHelper.getIfFiltroSqlContainsEntry(entry)) {
            return Mensajes.FILTROS_NO_SQL;
        }

        // FILTRO NUTS
        var filtroNuts = propertiesManagerService.getProperty(Constantes.FILTER_PROPERTIES, PropertiesKeys.FILTRO_NUTS);
        if (!filtroNuts.isBlank() && !FiltroHelper.getIfFiltroNutsContainsEntry(entry)) {
            return Mensajes.FILTROS_NO_NUTS;
        }

        // FILTRO FECHAS
        var filtroFechaInicial = propertiesManagerService.getProperty(Constantes.FILTER_PROPERTIES, PropertiesKeys.FILTRO_FECHAINICIALLECTURA);
        if (DateTimeHelper.esFechaInvalida(filtroFechaInicial)) {
            msg = String.format("[entryCumpleConLosFiltros] - Fecha inicial inválida: %s", filtroFechaInicial);
            log.error(msg);
            throw new MiInvalidDateFormatException (msg);
        }



        var filtroFechaFinal = propertiesManagerService.getProperty(Constantes.FILTER_PROPERTIES, PropertiesKeys.FILTRO_FECHAFINALLECTURA);
        if (filtroFechaFinal.isBlank()) {
            filtroFechaFinal = Constantes.FECHA_FINAL_LECTURA;
        }


        // FILTRO OBJETO
        var filtroObjeto = propertiesManagerService.getProperty(Constantes.FILTER_PROPERTIES, PropertyConstantes.FILTRO_OBJETO);
        if (!filtroObjeto.isBlank() && !FiltroHelper.getIfFiltroObjetoContainsEntry(entry)) {
            return Mensajes.FILTROS_NO_OBJETO;
        }

        // Todos los filtros pasan
        return Mensajes.FILTROS_OK;
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

        // Añado el newEntry al MAP
        VariablesGlobales.getMapBaseDatos().put(newEntry.getIdEntry(), newEntry);

        // Actualizo las estadísticas
        estadistica.aumentarNEntryActualizados();
    }

    /**
     *
     * @param entry Objeto Entry que se está procesando
     * @param estadistica Objeto Estadistica
     * @throws MiInvalidDateFormatException Excepción en caso de error
     */
    public static void procesarEntry(
            Entry entry,
            Estadistica estadistica) throws MiInvalidDateFormatException {

        if (VariablesGlobales.isExistenFiltros()) {
            // Existen filtros activos en la ejecución

            String respuesta = entryCumpleConLosFiltros(entry);

            if (!respuesta.equals(Mensajes.FILTROS_OK)) {
                // Entry no cumple con los filtros

                estadistica.aumentarNEntryRechazados();

            } else {
                // Entry cumple con los filtros

                estadistica.aumentarNEntryProcesados();
                procesarEntrySegunExistencia(entry, estadistica);
            }

        } else {
            // No existen filtros activos en la ejecución

            estadistica.aumentarNEntryProcesados();
            procesarEntrySegunExistencia(entry, estadistica);
        }
    }

    // Método para procesar el entry según su existencia en el MAP
    private static void procesarEntrySegunExistencia(Entry entry, Estadistica estadistica) {

        //
        if (VariablesGlobales.getMapBaseDatos().containsKey(entry.getIdEntry())) {
            // Si existe en el MAP, lo procesamos

            Entry entryEnMap = VariablesGlobales.getMapBaseDatos().get(entry.getIdEntry());
            procesarEntryExistenteEnMAP(entry, entryEnMap, estadistica);

        } else {
            // No existe en el MAP, lo agregamos

            // Lo agrego al MAP
            VariablesGlobales.getMapBaseDatos().put(entry.getIdEntry(), entry);

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
        if (entryEnMemoria.getUpdated().before(entryEnMap.getUpdated())) {
            // La fecha del entry en el MAP es más nueva, descartamos el Entry

            estadistica.aumentarNEntryRechazados();

        } else {
            // La fecha del Entry en el MAP es más antigua, actualizamos el Entry en el MAP

            //
            actualizarEntryEnMAP(entryEnMemoria, entryEnMap, estadistica);
        }
    }
}
