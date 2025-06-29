package local.jarios.helpers;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.TipoConexion;
import local.jarios.exceptions.MiServiceException;
import local.jarios.models.FiltroOrganoContratacion;

import local.jarios.properties.exception.PropertiesManagerException;
import local.jarios.services.Service;
import local.jarios.services.ServiceImpl;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.VariablesGlobales;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Juan Antonio
 */
@Slf4j
public final class FiltroHelper {

    /**
     * CONSTRUCTOR DE LA CLASE
     */
    private FiltroHelper() {/* CONSTRUCTOR VACÍO */}

    /**
     *
     * @return Devuelve un Map del tipo Map<IdPlataforma, NombreOrganoContratacion>
     * @throws MiServiceException Excepción a la hora de generar el objeto Session de Hibernate
     */
    public static Map<String, String> getMapFromFiltroSql(String sql) throws MiServiceException {

        //
        Map<String, String> mapFiltro = new HashMap<>();
        log.info("[getMapFiltroSql] - Creado el HashMap que almacenará el filtro.");

        // Creo el objeto Servicio
        Service filterService = new ServiceImpl(TipoConexion.FILTRO_SQL);
        log.info("[getMapFiltroSql] - Creado el objeto Service asociado a: {}", TipoConexion.FILTRO_SQL);

        // Llamar al método para la obtención de la lista con el filtro
        List<FiltroOrganoContratacion> listFiltroOCs = filterService.getListFiltroOcsFromFiltroSql(sql);
        log.info("[getMapFiltroSql] - Lista de Órganos de Contratación: {}", listFiltroOCs);

        // Analizo si la lista con el filtro es vacía
        //     (lo que implicaría que ningún ENTRY podría pertenecer al filtro)
        if (!listFiltroOCs.isEmpty()) {
            // Filtro no vacío

            // Paso de una Lista a un Map (para mejorar la eficiencia a la hora de realizar la búsqueda)
            mapFiltro = MapHelper.getMapFromList(listFiltroOCs);
            log.info("[getMapFiltroSql] - Pasada la lista a un Map para acelerar las búsquedas.");

        }

        //
        return mapFiltro;
    }

    /**
     * Función que devuelve si un Entry pertenece al filtro que se encuentra en VariablesGlobales.getMapFiltro()
     *
     * @param entry Entry que vamos a comprobar si pertenece al filtro, para lo cual es necesario que disponga de
     *                    idPlataforma. En caso de no tener idPlataforma
     * @return Valor devuelto:
     *                      TRUE --> No aplica filtro || Tiene idPlataforma y pertenece al filtro
     *                      FALSE -> En cualquier otra situación
     */
    public static boolean hasEntryInFiltroSql(Entry entry) {

        // Discrimino si existe o no filtro de carga
        if (VariablesGlobales.getMapFiltro().isEmpty()) {
            // En caso de NO APLIAR FILTRO DE CARGA --> Devuelvo TRUE

            log.debug("[hasEntryInFiltroSql] - VariablesGlobales.getMapFiltro() EMPTY.");
            System.exit(0);
            return true;
        }

        // Compruebo si el ENTRY tiene IdPlataforma
        Optional<String> idPlataformaOpt = EntryHelper.getIdPlataformaFromEntry(entry);

        // Si el Entry tiene IdPlataforma, comprobar si está en el mapa de filtros
        if (idPlataformaOpt.isPresent()) {

            //
            var idPlataforma = idPlataformaOpt.get();
            log.debug("[hasEntryInFiltroSql] - IdPlataforma en Entry: {}", idPlataforma);

            // Comprobamos si el idPlataforma está en el mapa de filtros
            boolean encontrado = VariablesGlobales.getMapFiltro().containsKey(idPlataforma);
            log.debug("[hasEntryInFiltroSql] - No figura en VariablesGlobales.getMapFiltro().");

            //
            return encontrado;

        } else {

            // Si no tiene IdPlataforma, logueamos el mensaje correspondiente
            log.debug("[getIfFiltroSqlContainsEntry] - No tiene IdPlataforma el entry. Devuelvo false.");
            return false;
        }
    }

    /**
     * Función que devuelve si un Entry pertenece al filtro que se encuentra en VariablesGlobales.getMapFiltro()
     *
     * @param entry Entry que vamos a comprobar si pertenece al filtro, para lo cual es necesario que disponga de
     *                    idPlataforma. En caso de no tener idPlataforma
     * @return Valor devuelto:
     *                      TRUE --> No aplica filtro || Tiene idPlataforma y pertenece al filtro
     *                      FALSE -> En cualquier otra situación
     */
    public static boolean hasEntryContainsNuts(Entry entry) {

        String filtroNuts = VariablesGlobales.getFiltroNuts();

        if (filtroNuts == null || filtroNuts.isBlank()) {
            log.debug("[hasEntryContainsNuts] - El filtro Nuts es NULL o BLANK. Se acepta el Entry.");
            return true;
        }

        Optional<String> nutsEntryOptional = EntryHelper.getNutsFromEntry(entry);

        if (nutsEntryOptional.isEmpty() || nutsEntryOptional.get().isBlank()) {
            log.debug("[hasEntryContainsNuts] - Nuts del Entry es NULL o BLANK. No cumple.");
            return false;
        }

        String nutsEntry = nutsEntryOptional.get();
        boolean encontrado = nutsEntry.contains(filtroNuts);

        log.debug("[hasEntryContainsNuts] - ¿Filtro figura en Nuts?: {}.", encontrado);

        return encontrado;

    }

    public static boolean hasEntryInFechas(Entry entry) throws PropertiesManagerException {

        LocalDateTime fechaEntry = entry.getUpdated();
        LocalDateTime fechaInicio = VariablesGlobales.getFiltroFechaInicial();
        LocalDateTime fechaFin = VariablesGlobales.getFiltroFechaFinal();

        if (fechaEntry == null) {
            log.debug("[hasEntryFechaEnRango] - La fecha del Entry es NULL. No cumple.");
            return false;
        }

        return ( fechaEntry.isBefore(fechaInicio) ) && ( fechaEntry.isAfter(fechaFin) );

    }

    public static boolean hasEntryContaninsObject(Entry entry) {

        String filtroObjeto = VariablesGlobales.getFiltroObjeto();

        if ((filtroObjeto == null) || (filtroObjeto.isBlank())) {
            log.debug("[hasEntryContaninsObject] - El filtro Objeto es NULL | BLANK. Cumple.");
            return true;
        }

        // Compruebo si el ENTRY tiene IdPlataforma
        var objetoEntry = EntryHelper.getObjetoFromEntry(entry);
        log.debug("[getIfFiltroObjetoContainsEntry] - Objeto del Entry: {}.", objetoEntry);

        if (objetoEntry == null || objetoEntry.isBlank()) {
            log.debug("[hasEntryContainsObject] - Objeto del Entry es NULL o BLANK. No cumple.");
            return false;
        }

        // Comprobamos si filtroObjeto se encuentra dentro de objetoEntry
        boolean encontrado = StringHelper.contieneCadena(objetoEntry, filtroObjeto);
        log.debug("[getIfFiltroObjetoContainsEntry] - ¿Filtro figura en objeto?: {}.", encontrado);

        //
        return encontrado;
    }

    private static void loadFilterFechas() throws PropertiesManagerException {

        // Leo la fecha inicial de lectura
        String filtroFechaInicialStr = PropertiesHelper.getProperty(
                PropertiesFiles.FILTER,
                PropertiesKeys.FILTER_FECHAINICIALLECTURA);

        // Leo la fecha final de lectura
        String filtroFechaFinalStr = PropertiesHelper.getProperty(
                PropertiesFiles.FILTER,
                PropertiesKeys.FILTER_FECHAFINALLECTURA);

        // Si ambas fechas vienen informadas y no están en blanco
        if (!filtroFechaInicialStr.isBlank() && !filtroFechaFinalStr.isBlank()) {

            LocalDate fechaInicial = LocalDate.parse(filtroFechaInicialStr, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate fechaFinal = LocalDate.parse(filtroFechaFinalStr, DateTimeFormatter.ISO_LOCAL_DATE);

            VariablesGlobales.setFiltroFechaInicial(fechaInicial.atStartOfDay());
            VariablesGlobales.setFiltroFechaFinal(fechaFinal.atTime(23, 59, 59));

            log.debug("[loadFilterFechas] - Filtro de fechas aplicado. FechaInicial: {}, FechaFinal: {}",
                    VariablesGlobales.getFiltroFechaInicial(), VariablesGlobales.getFiltroFechaFinal());

        } else {
            // Si alguna está vacía, asigno los valores por defecto
            log.debug("[loadFilterFechas] - Alguna de las fechas es Blank. Se aplican valores por defecto.");

            VariablesGlobales.setFiltroFechaFinal(LocalDate.parse(Constantes.FECHA_FINAL_LECTURA, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay());
            VariablesGlobales.setFiltroFechaInicial(LocalDate.now().atTime(23, 59, 59));

            log.debug("[loadFilterFechas] - Fechas por defecto aplicadas. FechaInicial: {}, FechaFinal: {}",
                    VariablesGlobales.getFiltroFechaInicial(), VariablesGlobales.getFiltroFechaFinal());
        }
    }

    private static void loadFilterNuts() throws PropertiesManagerException {

        String filter = PropertiesHelper
                            .getProperty(
                                    PropertiesFiles.FILTER,
                                    PropertiesKeys.FILTER_NUTS);

        if ((filter != null) && (!filter.isBlank())) {
            VariablesGlobales.setFiltroNuts(filter);
        }
    }

    private static void loadFilterObjeto() throws PropertiesManagerException {

        String filter = PropertiesHelper
                            .getProperty(
                                    PropertiesFiles.FILTER,
                                    PropertiesKeys.FILTER_OBJETO);

        if ((filter != null) && (!filter.isBlank())) {
            VariablesGlobales.setFiltroObjeto(filter);
        }
    }

    private static void loadFilterSql() throws PropertiesManagerException {

        String filter = PropertiesHelper
                            .getProperty(
                                    PropertiesFiles.FILTER,
                                    PropertiesKeys.FILTER_SQL);

        if (!StringHelper.isInvalidString(filter)) {
            VariablesGlobales.setFiltroSql(filter);
            log.info("[loadFilterSql] - VariablesGlobales.setFiltroSql('{}')", filter);

            VariablesGlobales.setMapFiltro(getMapFromFiltroSql(filter));
            log.info("[loadFilterSql] - Asisgnado el Map a VariablesGlobales.setMapFiltro.");
            MapHelper.printMap(getMapFromFiltroSql(filter));
        }
    }

    public static void loadFilters() throws PropertiesManagerException {

        loadFilterFechas();
        log.info("[loadFilters] - FilterFechas cargado correctamente.");

        loadFilterNuts();
        log.info("[loadFilters] - FilterNuts cargado correctamente.");

        loadFilterObjeto();
        log.info("[loadFilters] - FilterObjeto cargado correctamente.");

        loadFilterSql();
        log.info("[loadFilters] - FilterSql cargado correctamente.");

    }

    public static void printFilters() {

        log.info(
                "[printFilters] - Filtro fechas. Inicial: '{}', Final: '{}'.",
                VariablesGlobales.getFiltroFechaInicial(),
                VariablesGlobales.getFiltroFechaFinal());

        log.info("[printFilters] - Filtro Nuts: '{}'.", VariablesGlobales.getFiltroNuts());

        log.info("[printFilters] - Filtro Objeto: '{}'.", VariablesGlobales.getFiltroObjeto());

        log.info("[printFilters] - Filtro Sql: '{}'.", VariablesGlobales.getFiltroSql());

        VariablesGlobales.getMapFiltro().forEach((key, value) ->
                log.info("[printFilters] - IdPlataforma: '{}' - ÓrganoContratacion: {}", key, value));
    }

    public static boolean entryCumpleFiltros(Entry entry) {

        // Si existe filtro SQL y no lo cumple, descarto el Entry
        if (!VariablesGlobales.getMapFiltro().isEmpty() && !hasEntryInFiltroSql(entry)) {
            log.debug("[entryCumpleFiltros] - NO CUMPLE filtro SQL. {}", entry.getIdEntry());
            return false;
        }

        // Si existe filtro NUTS y no lo cumple, descarto el Entry
        String filtroNuts = VariablesGlobales.getFiltroNuts();
        if (filtroNuts != null && !filtroNuts.isBlank() && !hasEntryContainsNuts(entry)) {
            log.debug("[entryCumpleFiltros] - NO CUMPLE filtro NUTS. {}", entry.getIdEntry());
            return false;
        }

        // Si existe filtro Objeto y no lo cumple, descarto el Entry
        String filtroObjeto = VariablesGlobales.getFiltroObjeto();
        if (filtroObjeto != null && !filtroObjeto.isBlank() && !hasEntryContaninsObject(entry)) {
            log.debug("[entryCumpleFiltros] - NO CUMPLE filtro Objeto. {}", entry.getIdEntry());
            return false;
        }

        // Si existe filtro de fechas y no lo cumple, descarto el Entry
        LocalDateTime fechaInicio = VariablesGlobales.getFiltroFechaInicial();
        LocalDateTime fechaFin = VariablesGlobales.getFiltroFechaFinal();
        if (fechaInicio != null && fechaFin != null && !hasEntryInFechas(entry)) {
            log.debug("[entryCumpleFiltros] - NO CUMPLE filtro Fechas. {}", entry.getIdEntry());
            return false;
        }

        log.debug("[entryCumpleFiltros] - Entry cumple los filtros: {}", entry.getIdEntry());
        return true;
    }

}
