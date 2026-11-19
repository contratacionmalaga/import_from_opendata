package local.jarios.filtro;

import java.util.List;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.HistoricoEntry;
import local.jarios.enums.EntryOpcion;
import local.jarios.filtro.evaluator.FiltroFechasEvaluator;
import local.jarios.filtro.interfaces.FiltroLoader;
import local.jarios.filtro.loader.FiltroCodigosPostalesLoader;
import local.jarios.filtro.loader.FiltroFechasLoader;
import local.jarios.filtro.loader.FiltroNifsLoader;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

/** Description: Clase encargada de orquestar la carga y evaluación de filtros. */
@Slf4j
public class FiltroManager {

  private final List<FiltroLoader> loaders;
  private final FiltroFechasEvaluator filtroFechas = new FiltroFechasEvaluator();

  /** Constructor de la clase. */
  public FiltroManager() {
    // Lista de los filtros a cargar
    this.loaders =
        List.of(
            new FiltroFechasLoader(), new FiltroCodigosPostalesLoader(), new FiltroNifsLoader());
  }

  /** Imprime la información de los filtros. */
  public static void imprimirFiltros(OpenDataExecutionContext context) {

    // Filtro de fechas
    log.info("Filtro de fechas:");
    log.info("  Fecha inicial: {}", context.getFiltroFechaInicial());
    log.info("  Fecha final: {}", context.getFiltroFechaFinal());

    // Filtro de órganos de contratación por código postal
    log.info("Filtro por códigos postales: ({})", context.getFiltroCodigosPostales());

    // Filtro por lista de nifs
    log.info("Filtro nifs: ({})", context.getFiltroNifs());
    log.info(
        "Registros en la lista de NIFs según el filtro: {} registros",
        context.getListNifs() != null ? context.getListNifs().size() : 0);
    log.info(
        "Registros en la lista de Organos de Contratación según el filtro: {} registros",
        context.getListOrganoContratacionFiltro() != null
            ? context.getListOrganoContratacionFiltro().size()
            : 0);
  }

  /**
   * Carga los filtros desde los loaders.
   *
   * @throws PropertiesManagerException excepción lanzada
   */
  public void cargarFiltros(OpenDataExecutionContext context) throws PropertiesManagerException {
    for (FiltroLoader loader : loaders) {
      loader.cargar(context);
    }
    log.debug("        • Filtros cargados correctamente");
  }

  /**
   * Evaluar los filtros para un Entry.
   *
   * @param entry Entry sobre el que se evaluan los filtros
   * @return Valor devuelto
   */
  public boolean evaluarFiltros(OpenDataExecutionContext context, Entry entry) {
    String motivo = null;
    if (!filtroFechas.evaluar(context, entry)) {
      motivo = "Filtro no cumplido: FiltroFechasEvaluator";
    } else if (tieneAmbito(context) && !perteneceAlAmbito(context, entry)) {
      motivo = "No cumple ningún filtro alternativo (CP/NIF)";
    }
    if (motivo == null) {
      return true;
    }
    context.addHistorico(
        new HistoricoEntry(entry, EntryOpcion.RECHAZAR, motivo, context.getTipoSindicacion()));
    return false;
  }

  /** El ámbito geográfico usa los NIF cargados por código postal o informados directamente. */
  public boolean perteneceAlAmbito(OpenDataExecutionContext context, Entry entry) {
    return tieneAmbito(context)
        && entry.findNifFromEntry(context.getTipoSindicacion())
            .filter(context.getConjuntoNifsEnFiltro()::contains)
            .isPresent();
  }

  private static boolean tieneAmbito(OpenDataExecutionContext context) {
    return context.getConjuntoNifsEnFiltro() != null && !context.getConjuntoNifsEnFiltro().isEmpty();
  }
}
