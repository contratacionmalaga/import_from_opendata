package local.jarios.variants.malaga;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import local.jarios.common.util.ConstantesExcel;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.core.abstracts.AbstractOpenDataBase;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.HistoricoEntry;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.enums.EntryOpcion;
import local.jarios.exceptions.MiServiceException;
import local.jarios.exceptions.MiUnknownHostException;
import local.jarios.filtro.FiltroManager;
import local.jarios.helpers.HistoricoTotales;
import local.jarios.helpers.LocalDateTimeHelper;
import local.jarios.helpers.OcHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.imports.ImportResult;
import local.jarios.properties.exception.PropertiesManagerException;
import local.jarios.services.ImportPersistencePlan;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractOpenDataMalaga extends AbstractOpenDataBase {

  protected AbstractOpenDataMalaga() {
    super();
  }

  @Override
  protected String resolveAppName() throws PropertiesManagerException {
    return getPropertiesManager().getProperty(PropertiesFiles.APP, PropertiesKeys.APP_NAME);
  }

  @Override
  protected String getDefaultAppName() {
    return "import-from-opendata";
  }

  @Override
  protected String getReportProcessType() {
    return "con_filtros";
  }

  @Override
  protected void beforeParse(OpenDataExecutionContext context) throws PropertiesManagerException {
    ImportResult<OrganoContratacion> excelResult = loadOrganosContratacion(context);
    context.setExcelResult(excelResult);
    loadFilters(context);
  }

  @Override
  public void previewPersistData(OpenDataExecutionContext context) {
    HistoricoTotales historicoTotales = logToPersistPreview(context);
    context.setHistoricoTotales(historicoTotales);
  }

  protected ImportResult<OrganoContratacion> loadOrganosContratacion(
      OpenDataExecutionContext context) {
    imprimirTitulo("CARGA DE EXCEL DESDE INTERNET");
    log.info(ConstantesExcel.EXCEL_OC_URL);

    ImportResult<OrganoContratacion> result = OcHelper.getListaOcFromExcelInternet();

    context.getListOrganoContratacionEnExcel().clear();
    context.getListOrganoContratacionEnExcel().addAll(result.lista());
    context.setFechaGeneracionExcel(result.fechaGeneracion());

    log.info("Importada lista OC desde internet.");
    log.info(
        "  Registros leídos: {}",
        StringHelper.getNumeroConFormato(context.getListOrganoContratacionEnExcel().size()));
    log.info("  Fecha de generación del fichero: {}", context.getFechaGeneracionExcel());

    return result;
  }

  protected void loadFilters(OpenDataExecutionContext context) throws PropertiesManagerException {
    FiltroManager filtroManager = new FiltroManager();
    filtroManager.cargarFiltros(context);
    validateRequiredTargetFilter(context);

    StringHelper.generarTitulo(log, "FILTROS CARGADOS CORRECTAMENTE");
    FiltroManager.imprimirFiltros(context);

    context.setFiltrosCargados(true);
  }

  private void validateRequiredTargetFilter(OpenDataExecutionContext context)
      throws PropertiesManagerException {
    boolean hasNifs = context.getFiltroNifs() != null && !context.getFiltroNifs().isBlank();
    boolean hasPostalCodes =
        context.getFiltroCodigosPostales() != null && !context.getFiltroCodigosPostales().isBlank();
    boolean hasEffectiveTargets =
        context.getConjuntoNifsEnFiltro() != null && !context.getConjuntoNifsEnFiltro().isEmpty();

    if (!hasNifs && !hasPostalCodes) {
      throw new PropertiesManagerException(
          "La importacion con filtros requiere informar filter.nifs o filter.codigosPostales en filter.properties.");
    }

    if (!hasEffectiveTargets) {
      throw new PropertiesManagerException(
          "La importacion con filtros no ha encontrado ningun organo/NIF efectivo. Revise filter.nifs o filter.codigosPostales en filter.properties.");
    }
  }

  protected HistoricoTotales logToPersistPreview(OpenDataExecutionContext context) {
    imprimirTitulo("LISTADO DE LOS OBJETOS OBTENIDOS EN EL PARSEO PARA SU PERSISTENCIA");

    int nEntry = 0;
    int nDeletedEntry = 0;

    for (Feed feed : context.getConjuntoFeedsFromAtoms()) {
      log.debug("{}", feed.getLinkSelf());

      List<Entry> entryList = context.getMapFeedsToBaseDatos().get(feed);
      if (entryList != null && !entryList.isEmpty()) {
        feed.setEntryList(entryList);
        for (Entry entry : entryList) {
          nEntry++;
          log.debug("  {}", entry);
        }
      } else {
        log.debug("  (Sin entries activas)");
      }

      List<DeletedEntry> deletedList = feed.getDeletedEntryList();
      if (deletedList != null && !deletedList.isEmpty()) {
        feed.setDeletedEntryList(deletedList);
        for (DeletedEntry de : deletedList) {
          nDeletedEntry++;
          log.debug("  {}", de);
        }
      } else {
        log.debug("  (Sin entries eliminadas)");
      }
    }

    imprimirTitulo("RESUMEN DEL PARSEO");
    log.info(
        "Feeds obtenidos del parseo: {}",
        StringHelper.getNumeroConFormato(context.getConjuntoFeedsFromAtoms().size()));
    log.info("Entry obtenidos del parseo: {}", StringHelper.getNumeroConFormato(nEntry));
    log.info(
        "DeletedEntry obtenidos del parseo: {}", StringHelper.getNumeroConFormato(nDeletedEntry));
    log.info(
        "Históricos parseados: {}",
        StringHelper.getNumeroConFormato(Math.toIntExact(context.getTotalHistoricos())));

    Map<EntryOpcion, Long> historicoCount =
        context.getListHistoricos().stream()
            .collect(
                Collectors.groupingBy(
                    HistoricoEntry::getEntryOpcion,
                    () -> new EnumMap<>(EntryOpcion.class),
                    Collectors.counting()));

    long insertar = historicoCount.getOrDefault(EntryOpcion.INSERTAR, 0L);
    long actualizar = historicoCount.getOrDefault(EntryOpcion.ACTUALIZAR, 0L);
    long rechazar =
        historicoCount.getOrDefault(EntryOpcion.RECHAZAR, 0L)
            + context.getHistoricosRechazadosOmitidos();

    log.info(
        "   Entry INSERTADOS: {}", StringHelper.getNumeroConFormato(Math.toIntExact(insertar)));
    log.info(
        "   Entry ACTUALIZADOS: {}", StringHelper.getNumeroConFormato(Math.toIntExact(actualizar)));
    log.info(
        "   Entry RECHAZADOS: {}", StringHelper.getNumeroConFormato(Math.toIntExact(rechazar)));
    log.info(
        "   Historicos RECHAZAR omitidos de persistencia: {}",
        StringHelper.getNumeroConFormato(
            Math.toIntExact(context.getHistoricosRechazadosOmitidos())));

    return new HistoricoTotales(insertar, actualizar, rechazar, nDeletedEntry);
  }

  @Override
  public Estadistica persistAll(OpenDataExecutionContext context)
      throws MiServiceException, MiUnknownHostException {

    imprimirTitulo("PERSISTENCIA EN LA BASE DE DATOS");
    LocalDateTime inicio = LocalDateTimeHelper.getLocalDateTimeNow();

    ImportResult<OrganoContratacion> excelResult = context.getExcelResult();
    HistoricoTotales historicoTotales = context.getHistoricoTotales();

    Log miLog =
        new Log(
            context.getLugarImportacion(),
            context.getTipoSindicacion(),
            excelResult == null ? 0 : excelResult.lista().size(),
            excelResult == null ? null : excelResult.fechaGeneracion());
    Configuracion configuracion = new Configuracion(miLog, context);
    Estadistica estadistica = buildEstadistica(context, miLog, historicoTotales, inicio);

    ImportPersistencePlan plan =
        new ImportPersistencePlan(
            miLog,
            configuracion,
            context.getListNifs(),
            context.getListOrganoContratacionFiltro(),
            context.getConjuntoFeedsFromAtoms(),
            context.getListHistoricos(),
            estadistica);

    getServicePrincipal().persistirImportacion(plan);
    log.info("Persistida la importacion completa en una unica transaccion.");
    imprimirTitulo("PRESISTENCIA FINALIZADA CORRECTAMENTE");
    return estadistica;
  }

  protected Estadistica buildEstadistica(
      OpenDataExecutionContext context,
      Log miLog,
      HistoricoTotales totales,
      LocalDateTime inicioPersistencia)
      throws MiUnknownHostException {

    Estadistica estadistica = new Estadistica(miLog);

    if (totales != null) {
      estadistica.setNumRegistrosHistoricosInsertar(totales.insertar());
      estadistica.setNumRegistrosHistoricosActualizar(totales.actualizar());
      estadistica.setNumRegistrosHistoricosRechazar(totales.rechazar());
      estadistica.setNumDeletedEntries(totales.deletedEntrys());
      estadistica.setTotalHistoricos(totales.total());
    } else {
      estadistica.setNumRegistrosHistoricosInsertar(0L);
      estadistica.setNumRegistrosHistoricosActualizar(0L);
      estadistica.setNumRegistrosHistoricosRechazar(0L);
      estadistica.setNumDeletedEntries((long) context.getMapDeletedEntriesFromAtoms().size());
      estadistica.setTotalHistoricos(0L);
    }

    String duracionPersistencia =
        LocalDateTimeHelper.getDiferenciaLocalDateTime(
            inicioPersistencia, LocalDateTimeHelper.getLocalDateTimeNow());

    estadistica.setDuracionParseo(context.getDuracionParseo());
    estadistica.setDuracionPersistencia(duracionPersistencia);
    estadistica.setNumFicherosAtoms((long) context.getConjuntoFeedsFromAtoms().size());
    estadistica.setNumEntries((long) context.getMapEntriesToBaseDatos().size());
    estadistica.setNumOrganosContratacionFiltro(
        (long) context.getListOrganoContratacionFiltro().size());
    estadistica.setNumNifsFiltro((long) context.getListNifs().size());

    return estadistica;
  }
}
