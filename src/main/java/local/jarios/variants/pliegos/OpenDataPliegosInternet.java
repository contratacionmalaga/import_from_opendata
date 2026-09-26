package local.jarios.variants.pliegos;

import java.util.Map;
import local.jarios.common.util.Mensajes;
import local.jarios.core.bootstrap.OpenDataBootstrapHelper;
import local.jarios.core.enums.LugarImportacion;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.Entry;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiServiceException;
import local.jarios.helpers.FeedHelper;
import local.jarios.helpers.LocalDateTimeHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.helpers.TipoSindicacionHelper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenDataPliegosInternet extends AbstractOpenDataPliegos {

  public static void main(String[] args) {
    StringHelper.generarTitulo(log, Mensajes.INICIO_INTERNET);
    System.exit(
        new OpenDataPliegosInternet().procesar(OpenDataBootstrapHelper.resolveConfigDir(args)));
  }

  @Override
  protected void initVariantContext(OpenDataExecutionContext context) throws MiServiceException {
    imprimirTitulo("LUGAR DE IMPORTACIÓN Y TIPO DE SINDICACIÓN");

    context.setAplicarFiltros(false);
    context.setCompararConExistentes(true);

    TipoSindicacion tipo = new TipoSindicacionHelper().getTipoSindicacionDesdeProperties();

    context.setLugarImportacion(LugarImportacion.INTERNET);
    context.setTipoSindicacion(tipo);
    adquirirExclusionImportacion(context);

    log.info("Lugar de Importación: {}.", LugarImportacion.INTERNET);
    log.info("Tipo de Sindicación: {}.", tipo);

    context.getMapEntrySnapshotsFromBaseDatos().clear();
    context
        .getMapEntrySnapshotsFromBaseDatos()
        .putAll(getServicePrincipal().getEntrySnapshots(context.getTipoSindicacion()));

    context.setNewestEntry(
        obtenerUltimaEntradaSnapshot(context.getMapEntrySnapshotsFromBaseDatos()));

    log.info(
        "Entries existentes en BD: {}",
        StringHelper.getNumeroConFormato(context.getMapEntrySnapshotsFromBaseDatos().size()));

    Entry newest = context.getNewestEntry();
    if (newest == null) {
      log.info("NewestEntry: null");
    } else {
      log.info("NewestEntry: entryId={}, updated={}", newest.getEntryId(), newest.getUpdated());
    }
  }

  @Override
  protected String parsearAtomsFeeds(OpenDataExecutionContext context)
      throws MiParseException, MiServiceException {

    imprimirTitulo("INICIO DEL PARSEO DE LOS FICHEROS ATOMS");

    long inicio = System.nanoTime();
    FeedHelper.parsearFeedsDesdeInternet(context);

    String duracion = LocalDateTimeHelper.getDiferenciaNanos(inicio, System.nanoTime());
    log.info("Final del parseo. Duración: {}", duracion);

    log.info(
        "[parsearAtomsFeeds] - Entries válidos obtenidos desde Atoms de INTERNET: {}.",
        context.getMapEntriesFromAtoms().size());

    log.info(
        "[parsearAtomsFeeds] - Total entries en base de datos: {}.",
        StringHelper.getNumeroConFormato(context.getMapEntrySnapshotsFromBaseDatos().size()));

    return duracion;
  }

  @Override
  public Map<String, Entry> resolveEntriesToPersist(OpenDataExecutionContext context) {
    return context.getMapEntriesFromAtoms();
  }
}
