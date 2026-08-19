package local.jarios.variants.pliegos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.core.abstracts.AbstractOpenDataBase;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Log;
import local.jarios.exceptions.MiServiceException;
import local.jarios.exceptions.MiUnknownHostException;
import local.jarios.helpers.LocalDateTimeHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.properties.exception.PropertiesManagerException;
import local.jarios.services.ImportPersistencePlan;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractOpenDataPliegos extends AbstractOpenDataBase {

  protected AbstractOpenDataPliegos() {
    super();
  }

  @Override
  protected String resolveAppName() throws PropertiesManagerException {
    return getPropertiesManager().getProperty(PropertiesFiles.APP, PropertiesKeys.APP_NAME);
  }

  @Override
  protected String getDefaultAppName() {
    return "import-from-opendata-pliegos";
  }

  @Override
  protected String getReportProcessType() {
    return "sin_filtros";
  }

  @Override
  public void previewPersistData(OpenDataExecutionContext context) {
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
          log.debug("  {}", entry.getEntryId());
        }
      } else {
        log.debug("  (Sin entries activas)");
      }

      List<DeletedEntry> deletedList = feed.getDeletedEntryList();
      if (deletedList != null && !deletedList.isEmpty()) {
        feed.setDeletedEntryList(deletedList);
        for (DeletedEntry de : deletedList) {
          nDeletedEntry++;
          log.debug("  {}", de.getRef());
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
  }

  @Override
  public Estadistica persistAll(OpenDataExecutionContext context)
      throws MiServiceException, MiUnknownHostException {

    imprimirTitulo("PERSISTENCIA EN LA BASE DE DATOS");
    LocalDateTime inicio = LocalDateTimeHelper.getLocalDateTimeNow();

    Log miLog = new Log(context.getLugarImportacion(), context.getTipoSindicacion());
    Configuracion configuracion = new Configuracion(miLog, context);
    Estadistica estadistica = buildEstadistica(context, miLog, inicio);

    Set<String> replacementEntryIds =
        ImportPersistencePlan.replacementEntryIdsFromHistoricos(context.getListHistoricos());
    int historicosGenerados = context.getListHistoricos().size();
    context.getListHistoricos().clear();

    ImportPersistencePlan plan =
        new ImportPersistencePlan(
            miLog,
            configuracion,
            List.of(),
            List.of(),
            context.getConjuntoFeedsFromAtoms(),
            replacementEntryIds,
            List.of(),
            estadistica);

    getServicePrincipal().persistirImportacion(plan);
    log.info(
        "Persistida importacion Pliegos en una unica transaccion: historicosGenerados={}, historicosPersistidos=0, reemplazos={}",
        StringHelper.getNumeroConFormato(historicosGenerados),
        StringHelper.getNumeroConFormato(replacementEntryIds.size()));
    imprimirTitulo("PRESISTENCIA FINALIZADA CORRECTAMENTE");
    return estadistica;
  }

  protected Estadistica buildEstadistica(
      OpenDataExecutionContext context, Log miLog, LocalDateTime inicioPersistencia)
      throws MiUnknownHostException {

    Estadistica estadistica = new Estadistica(miLog);

    String duracionPersistencia =
        LocalDateTimeHelper.getDiferenciaLocalDateTime(
            inicioPersistencia, LocalDateTimeHelper.getLocalDateTimeNow());

    estadistica.setDuracionParseo(context.getDuracionParseo());
    estadistica.setDuracionPersistencia(duracionPersistencia);
    estadistica.setNumFicherosAtoms((long) context.getConjuntoFeedsFromAtoms().size());
    estadistica.setNumEntries((long) context.getMapEntriesToBaseDatos().size());
    estadistica.setNumDeletedEntries((long) context.getMapDeletedEntriesFromAtoms().size());

    return estadistica;
  }
}
