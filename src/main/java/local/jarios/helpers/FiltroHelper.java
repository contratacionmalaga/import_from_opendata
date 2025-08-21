package local.jarios.helpers;

import local.jarios.common.util.Constantes;
import local.jarios.common.util.Mensajes;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.enums.TipoConexion;
import local.jarios.exceptions.MiServiceException;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import local.jarios.services.ServiceFiltro;
import local.jarios.services.ServiceFiltroImpl;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interfaz para acciones sobre los posibles filtros que se pueden realizar durante el proceso de importación
 */
@Slf4j
public final class FiltroHelper {

  /**
   * Variable asociada al servicio de consulta de los ficheros properties
   */
  private final PropertiesManagerService propertyManager;

  /**
   * CONSTRUCTOR DE LA CLASE
   */
  private FiltroHelper() {
    this.propertyManager = PropertiesManagerServiceImpl.getInstance();
  }

  /**
   *
   * @return Devuelve un Map del tipo Map
   * @throws MiServiceException Excepción a la hora de generar el objeto Session de Hibernate
   */
  public static List<OrganoContratacion> getListFromFiltroSql(String sql) throws MiServiceException {

    // Creo el objeto Servicio
    ServiceFiltro serviceFiltro = new ServiceFiltroImpl();
    log.debug("[getMapFiltroSql] - Creado el objeto Service asociado a: {}", TipoConexion.FILTRO_SQL);

    // Llamar al método para la obtención de la lista con el filtro
    List<OrganoContratacion> listFiltroOCs = serviceFiltro.getListFiltroOcsFromFiltroSql(sql);
    log.debug("[getMapFiltroSql] - Lista de Órganos de Contratación: {}", listFiltroOCs);

    //
    return listFiltroOCs;
  }

  /**
   * Función que devuelve si un Entry pertenece al filtro que se encuentra en VariablesGlobales.getMapFiltro()
   *
   * @param entry Entry que vamos a comprobar si pertenece al filtro, para lo cual es necesario que disponga de
   *              idPlataforma. En caso de no tener idPlataforma
   * @return Valor devuelto:
   * TRUE --> No aplica filtro || Tiene idPlataforma y pertenece al filtro
   * FALSE -> En cualquier otra situación
   */
  public static boolean hasEntryInFiltroSql(Entry entry) {

    // Discrimino si existe o no filtro de carga
    if (VariablesGlobales.getMapFiltroSql().isEmpty()) {
      // En caso de NO APLIAR FILTRO DE CARGA --> Devuelvo TRUE

      log.debug("[hasEntryInFiltroSql] - VariablesGlobales.getMapFiltro() EMPTY.");
      return true;
    }

    // Compruebo si el ENTRY tiene IdPlataforma
    Optional<String> idPlataformaOpt = EntryHelper.getIdPlataformaFromEntry(entry);
    log.debug("[hasEntryInFiltroSql] - {}", entry.toStringResumido());

    // Si el Entry tiene IdPlataforma, comprobar si está en el mapa de filtros
    if (idPlataformaOpt.isPresent()) {

      //
      var idPlataforma = idPlataformaOpt.get();
      log.debug("[hasEntryInFiltroSql] - IdPlataforma en Entry: {}", idPlataforma);

      // Comprobamos si el idPlataforma está en el mapa de filtros
      boolean encontrado = VariablesGlobales.getMapFiltroSql().containsKey(idPlataforma);
      log.debug("[hasEntryInFiltroSql] - No figura en VariablesGlobales.getMapFiltro().");

      //
      return encontrado;

    } else {

      // Si no tiene IdPlataforma, logueamos el mensaje correspondiente
      log.debug("[hasEntryInFiltroSql] - No tiene IdPlataforma el entry. Devuelvo false.");
      return false;
    }
  }

  /**
   * Función que devuelve si un Entry pertenece al filtro que se encuentra en VariablesGlobales.getMapFiltro()
   *
   * @param entry Entry que vamos a comprobar si pertenece al filtro, para lo cual es necesario que disponga de
   *              idPlataforma. En caso de no tener idPlataforma
   * @return Valor devuelto:
   * TRUE --> No aplica filtro || Tiene idPlataforma y pertenece al filtro
   * FALSE -> En cualquier otra situación
   */
  public static boolean hasEntryContainsNuts(Entry entry) {

    HashSet<String> filtroNuts = VariablesGlobales.getFiltroNuts();

    if (filtroNuts == null || filtroNuts.isEmpty()) {
      log.debug("[hasEntryContainsNuts] - El filtro Nuts es NULL o EMPTY. Se acepta el Entry.");
      return true;
    }

    Optional<String> nutsEntryOptional = EntryHelper.getNutsFromEntry(entry);

    if (nutsEntryOptional.isEmpty() || nutsEntryOptional.get().isBlank()) {
      log.debug("[hasEntryContainsNuts] - Nuts del Entry es NULL o BLANK. No cumple.");
      return false;
    }

    String nutsEntry = nutsEntryOptional.get();
    boolean encontrado = filtroNuts.contains(nutsEntry);

    log.debug("[hasEntryContainsNuts] - ¿Filtro figura len Nuts?: {}.", encontrado);

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

    return (fechaEntry.isBefore(fechaInicio)) && (fechaEntry.isAfter(fechaFin));

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

  public static void loadFilters() throws PropertiesManagerException {

    // Creación de un nuevo objeto
    FiltroHelper filtroHelper = new FiltroHelper();

    // Las fecha se cargan siempre
    filtroHelper.loadFilterFechas();
    String msg = String.format(
        "[loadFilters] - Filtro Fechas cargado correctamente (Fecha Inicial: '%s', Final Final: '%s')",
        VariablesGlobales.getFiltroFechaInicial(),
        VariablesGlobales.getFiltroFechaFinal());
    log.debug(msg);

    filtroHelper.loadFilterNuts();
    if (VariablesGlobales.getFiltroNuts().isEmpty()) {
      msg = "[loadFilters] - Filtro Nuts se encuentra vacío.";
    } else {
      msg = String.format(
          "[loadFilters] - Filtro Nuts cargado correctamente. %s",
          VariablesGlobales.getFiltroNuts());
    }
    log.debug(msg);

    filtroHelper.loadFilterObjeto();
    if (VariablesGlobales.getFiltroObjeto().isBlank()) {
      msg = "[loadFilters] - Filtro Objeto se encuentra vacío.";
    } else {
      msg = String.format(
          "[loadFilters] - Filtro Objeto cargado correctamente. %s",
          VariablesGlobales.getFiltroObjeto());
    }
    log.debug(msg);

    filtroHelper.loadFilterSql();
    if (VariablesGlobales.getMapFiltroSql().isEmpty()) {
      msg = "[loadFilters] - Filtro Sql se encuentra vacío.";
    } else {
      msg = String.format(
          "[loadFilters] - Filtro Sql cargado correctamente (%s registros)",
          VariablesGlobales.getMapFiltroSql().size());
    }
    log.debug(msg);
  }

  public static String entryCumpleFiltros(Entry entry) {

    // Si existe filtro SQL y no lo cumple, descarto el Entry
    if (!VariablesGlobales.getMapFiltroSql().isEmpty() && !hasEntryInFiltroSql(entry)) {
      log.debug("[entryCumpleFiltros] - {}", Mensajes.ENTRY_NO_FILTRO_SQL);
      return Mensajes.ENTRY_NO_FILTRO_SQL;
    }

    // Si existe filtro NUTS y no lo cumple, descarto el Entry
    HashSet<String> filtroNuts = VariablesGlobales.getFiltroNuts();
    if (filtroNuts != null && !filtroNuts.isEmpty() && !hasEntryContainsNuts(entry)) {
      log.debug("[entryCumpleFiltros] - {}", Mensajes.ENTRY_NO_FILTRO_NUTS);
      return Mensajes.ENTRY_NO_FILTRO_NUTS;
    }

    // Si existe filtro Objeto y no lo cumple, descarto el Entry
    String filtroObjeto = VariablesGlobales.getFiltroObjeto();
    if (filtroObjeto != null && !filtroObjeto.isBlank() && !hasEntryContaninsObject(entry)) {
      log.debug("[entryCumpleFiltros] - {}", Mensajes.ENTRY_NO_FILTRO_OBJETO);
      return Mensajes.ENTRY_NO_FILTRO_OBJETO;
    }

    // Si existe filtro de fechas y no lo cumple, descarto el Entry
    LocalDateTime fechaInicio = VariablesGlobales.getFiltroFechaInicial();
    LocalDateTime fechaFin = VariablesGlobales.getFiltroFechaFinal();
    if (fechaInicio != null && fechaFin != null && !hasEntryInFechas(entry)) {
      log.debug("[entryCumpleFiltros] - {}", Mensajes.ENTRY_NO_FILTRO_FECHAS);
      return Mensajes.ENTRY_NO_FILTRO_FECHAS;
    }

    //
    log.debug("[entryCumpleFiltros] - {}}", Mensajes.ENTRY_CUMPLE_FILTROS);
    return Mensajes.ENTRY_CUMPLE_FILTROS;
  }

  private void loadFilterFechas() throws PropertiesManagerException {

    // Leo la fecha inicial de lectura
    String filtroFechaInicialStr = propertyManager.getProperty(
        PropertiesFiles.FILTER,
        PropertiesKeys.FILTER_FECHAINICIALLECTURA);

    // Leo la fecha final de lectura
    String filtroFechaFinalStr = propertyManager.getProperty(
        PropertiesFiles.FILTER,
        PropertiesKeys.FILTER_FECHAFINALLECTURA);

    // Si ambas fechas no están en blanco
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

  private void loadFilterNuts() throws PropertiesManagerException {

    String filter = propertyManager.getProperty(PropertiesFiles.FILTER, PropertiesKeys.FILTER_NUTS);
    log.debug("[loadFilterNuts] Valor del filtro en el fichero properties: {}", filter);

    HashSet<String> nutsSet = new HashSet<>();

    if (filter != null && !filter.trim().isEmpty()) {
      String[] nutsArray = filter.split(",");
      log.debug("[loadFilterNuts] Array de String tras realizar split de filter (,): {}", (Object) nutsArray);

      // Validar cada código NUTS
      for (String nutsCode : nutsArray) {

        // Procesando
        log.debug("[loadFilterNuts] Procesando: {}", nutsCode);

        // Eliminar espacios alrededor del código NUTS
        nutsCode = nutsCode.trim();
        log.debug("[loadFilterNuts] Eliminados los espacios alrededor: {}", nutsCode);

        // Agregar el código NUTS al conjunto
        nutsSet.add(nutsCode);
        log.debug("[loadFilterNuts] Agregada al conjunto correctamente: {}", nutsCode);
      }
    }

    log.debug("[loadFilterNuts] - Conjunto de Nuts: {}", nutsSet);

    // Almacenamos el conjunto de códigos NUTS en VariablesGlobales
    VariablesGlobales.setFiltroNuts(nutsSet);
    log.debug("[loadFilterNuts] - Establecido el filtro - VariablesGlobales.setFiltroNuts({})", nutsSet);

  }

  private void loadFilterObjeto() throws PropertiesManagerException {

    String filter = propertyManager
        .getProperty(
            PropertiesFiles.FILTER,
            PropertiesKeys.FILTER_OBJETO);

    // Almaceno el filtro Objeto
    VariablesGlobales.setFiltroObjeto(filter);
    log.debug("[loadFilterObjeto] - VariablesGlobales.setFiltroObjeto('{}')", filter);
  }

  private void loadFilterSql() throws PropertiesManagerException {

    // Definicion de variables
    Map<String, String> mapFilter = new HashMap<>();
    List<OrganoContratacion> listOrganosContratacion;
    String filter = propertyManager
        .getProperty(
            PropertiesFiles.FILTER,
            PropertiesKeys.FILTER_SQL);


    //
    if (StringHelper.isValidString(filter)) {

      // Almaceno el filtro SQL
      VariablesGlobales.setFiltroSql(filter);
      log.debug("[loadFilterSql] - VariablesGlobales.setFiltroSql('{}')", filter);

      //
      listOrganosContratacion = getListFromFiltroSql(filter);
      log.debug("[loadFilterSql] - Tamaño del filtro ('{}')", listOrganosContratacion.size());

      // Paso de una Lista a un Map (para mejorar la eficiencia a la hora de realizar la búsqueda)
      mapFilter = MapHelper.getMapFromList(listOrganosContratacion);
      log.debug("[getMapFiltroSql] - Pasada la lista a un Map para acelerar las búsquedas.");

    }

    String valor = StringHelper.getNumeroConFormato(mapFilter.size());
    VariablesGlobales.setMapFiltroSql(mapFilter);
    log.debug("[loadFilterSql] - Asisgnado el Map a VariablesGlobales.setMapFiltro. ({})", valor);

  }

}
