package local.jarios.filtro;

import java.util.List;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.enums.EntryOpcion;
import local.jarios.filtro.evaluator.FiltroCodigosPostalesEvaluator;
import local.jarios.filtro.evaluator.FiltroFechasEvaluator;
import local.jarios.filtro.evaluator.FiltroNifsEvaluator;
import local.jarios.filtro.interfaces.FiltroEvaluator;
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
  private final List<FiltroEvaluator> evaluators;

  /** Constructor de la clase. */
  public FiltroManager() {
    // Lista de los filtros a cargar
    this.loaders =
        List.of(
            new FiltroFechasLoader(), new FiltroCodigosPostalesLoader(), new FiltroNifsLoader());

    // Lista de los filtros a evaluar
    this.evaluators =
        List.of(
            new FiltroFechasEvaluator(),
            new FiltroCodigosPostalesEvaluator(),
            new FiltroNifsEvaluator());
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

    boolean aplicaFiltro =
        context.getConjuntoNifsEnFiltro() != null && !context.getConjuntoNifsEnFiltro().isEmpty();

    boolean pasaAlternativos = false;

    for (FiltroEvaluator evaluator : evaluators) {

      boolean esAlternativo =
          evaluator instanceof FiltroCodigosPostalesEvaluator
              || evaluator instanceof FiltroNifsEvaluator;

      // ============================
      // 1) Filtros alternativos
      // ============================
      if (esAlternativo) {

        // Si no hay filtros activos → se omite SOLO este tipo
        if (!aplicaFiltro) {

          log.debug("    Filtro alternativo: {}. OMITIDO", evaluator.getClass().getSimpleName());

          continue;
        }

        boolean ok = evaluator.evaluar(context, entry);

        log.debug(
            "    Filtro alternativo: {}. Resultado: {}", evaluator.getClass().getSimpleName(), ok);

        if (ok) {
          pasaAlternativos = true;
        }

        continue;
      }

      // ============================
      // 2) Filtros obligatorios
      // ============================
      boolean ok = evaluator.evaluar(context, entry);

      log.debug(
          "    Filtro obligatorio: {}. Resultado: {}", evaluator.getClass().getSimpleName(), ok);

      if (!ok) {

        String msg = "Filtro no cumplido: " + evaluator.getClass().getSimpleName();

        context.addHistorico(
            new Historico(entry, EntryOpcion.RECHAZAR, msg, context.getTipoSindicacion()));

        return false;
      }
    }

    // ============================
    // 3) Validar alternativos
    // ============================
    if (aplicaFiltro && !pasaAlternativos) {

      String msg = "No cumple ningún filtro alternativo (CP/NIF)";

      context.addHistorico(
          new Historico(entry, EntryOpcion.RECHAZAR, msg, context.getTipoSindicacion()));

      return false;
    }

    return true;
  }
}
