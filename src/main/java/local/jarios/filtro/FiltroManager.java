package local.jarios.filtro;

import local.jarios.common.util.VariablesGlobales;
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

import java.util.List;

/**
 * Description: Clase encargada de orquestar la carga y evaluación de filtros.
 */
@Slf4j
public class FiltroManager {

  private final List<FiltroLoader> loaders;
  private final List<FiltroEvaluator> evaluators;

  /**
   * Constructor de la clase.
   */
  public FiltroManager() {
    // Lista de los filtros a cargar
    this.loaders = List.of(
        new FiltroFechasLoader(),
        new FiltroCodigosPostalesLoader(),
        new FiltroNifsLoader()
    );

    // Lista de los filtros a evaluar
    this.evaluators = List.of(
        new FiltroFechasEvaluator(),
        new FiltroCodigosPostalesEvaluator(),
        new FiltroNifsEvaluator()
    );
  }

  /**
   * Imprime la información de los filtros.
   */
  public static void imprimirFiltros() {

    // Filtro de fechas
    log.info("Filtro de fechas:");
    log.info("  Fecha inicial: {}", VariablesGlobales.getFiltroFechaInicial());
    log.info("  Fecha final: {}", VariablesGlobales.getFiltroFechaFinal());

    // Filtro de órganos de contratación por código postal
    log.info(
        "Filtro por códigos postales: ({})",
        VariablesGlobales.getFiltroCodigosPostales()
    );

    // Filtro por lista de nifs
    log.info(
        "Filtro nifs: ({})",
        VariablesGlobales.getFiltroNifs()
    );
    log.info("Registros en la lista de NIFs según el filtro: {} registros",
             VariablesGlobales.getListNifs().size());
    log.info(
        "Registros en la lista de Organos de Contratación según el filtro: {} registros",
        VariablesGlobales.getListOrganoContratacionFiltro().size()
    );
  }

  /**
   * Carga los filtros desde los loaders.
   *
   * @throws PropertiesManagerException excepción lanzada
   */
  public void cargarFiltros() throws PropertiesManagerException {
    for (FiltroLoader loader : loaders) {
      loader.cargar();
    }
    log.debug("        • Filtros cargados correctamente");
  }

  /**
   * Evaluar los filtros para un Entry.
   *
   * @param entry Entry sobre el que se evaluan los filtros
   * @return Valor devuelto
   */
  public boolean evaluarFiltros(Entry entry) {

    boolean aplicaFiltro = VariablesGlobales.getSetNifsFiltro() != null
        && !VariablesGlobales.getSetNifsFiltro().isEmpty();

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

          log.debug("    Filtro alternativo: {}. OMITIDO",
                    evaluator.getClass().getSimpleName());

          continue;
        }

        boolean ok = evaluator.evaluar(entry);

        log.debug("    Filtro alternativo: {}. Resultado: {}",
                  evaluator.getClass().getSimpleName(), ok);

        if (ok) {
          pasaAlternativos = true;
        }

        continue;
      }

      // ============================
      // 2) Filtros obligatorios
      // ============================
      boolean ok = evaluator.evaluar(entry);

      log.debug("    Filtro obligatorio: {}. Resultado: {}",
                evaluator.getClass().getSimpleName(), ok);

      if (!ok) {

        String msg = "Filtro no cumplido: "
            + evaluator.getClass().getSimpleName();

        VariablesGlobales.getListHistoricos()
            .add(new Historico(entry, EntryOpcion.RECHAZAR, msg));

        return false;
      }
    }

    // ============================
    // 3) Validar alternativos
    // ============================
    if (aplicaFiltro && !pasaAlternativos) {

      String msg = "No cumple ningún filtro alternativo (CP/NIF)";

      VariablesGlobales.getListHistoricos()
          .add(new Historico(entry, EntryOpcion.RECHAZAR, msg));

      return false;
    }

    return true;
  }
}
