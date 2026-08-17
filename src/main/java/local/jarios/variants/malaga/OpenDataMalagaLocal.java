package local.jarios.variants.malaga;

import java.time.LocalDateTime;
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
public class OpenDataMalagaLocal extends AbstractOpenDataMalaga {

  public static void main(String[] args) {
    StringHelper.generarTitulo(log, Mensajes.INICIO_LOCAL);
    System.exit(new OpenDataMalagaLocal().procesar(OpenDataBootstrapHelper.resolveConfigDir(args)));
  }

  @Override
  protected void initVariantContext(OpenDataExecutionContext context) throws MiServiceException {
    imprimirTitulo("LUGAR DE IMPORTACIÓN Y TIPO DE SINDICACIÓN");

    context.setAplicarFiltros(true);
    context.setCompararConExistentes(false);

    TipoSindicacion tipo = new TipoSindicacionHelper().getTipoSindicacionDesdeProperties();

    context.setLugarImportacion(LugarImportacion.LOCAL);
    context.setTipoSindicacion(tipo);

    log.info("Lugar de Importación: {}.", LugarImportacion.LOCAL);
    log.info("Tipo de Sindicación: {}.", tipo);

    validarBaseDatosVaciaParaCargaLocal(tipo);
    context.getMapEntrySnapshotsFromBaseDatos().clear();
    context.setNewestEntry(null);
  }

  private void validarBaseDatosVaciaParaCargaLocal(TipoSindicacion tipo) throws MiServiceException {
    long entriesExistentes = getServicePrincipal().countEntries(tipo);
    log.info(
        "Entries existentes en BD para carga LOCAL {}: {}",
        tipo,
        StringHelper.getNumeroConFormato(Math.toIntExact(entriesExistentes)));

    if (entriesExistentes > 0) {
      throw new MiServiceException(
          "La importacion LOCAL requiere una base de datos vacia para "
              + tipo
              + ". Se han encontrado "
              + entriesExistentes
              + " entries existentes. Use INTERNET para importacion incremental o vacie la BD antes de la carga local.");
    }
  }

  @Override
  protected String parsearAtomsFeeds(OpenDataExecutionContext context) throws MiParseException {
    imprimirTitulo("INICIO DEL PARSEO DE LOS FICHEROS ATOMS");

    LocalDateTime inicio = LocalDateTimeHelper.getLocalDateTimeNow();
    FeedHelper.parsearFeedsDesdeLocal(context);
    LocalDateTime fin = LocalDateTimeHelper.getLocalDateTimeNow();

    String duracion = LocalDateTimeHelper.getDiferenciaLocalDateTime(inicio, fin);
    log.info("Final del parseo. Duración: {}", duracion);

    log.info(
        "[parsearAtomsFeeds] - Entries válidos obtenidos desde Atoms de LOCAL: {}.",
        context.getMapEntriesFromAtoms().size());

    return duracion;
  }

  @Override
  public Map<String, Entry> resolveEntriesToPersist(OpenDataExecutionContext context) {
    return context.getMapEntriesFromAtoms();
  }
}
