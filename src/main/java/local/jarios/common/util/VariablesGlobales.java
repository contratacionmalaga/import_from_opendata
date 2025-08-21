package local.jarios.common.util;

import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Clase final que contiene variables globales utilizadas en todo el proyecto.
 * <p>
 * Esta clase agrupa estructuras de datos estáticas que almacenan información
 * compartida durante la ejecución del programa, como filtros, entradas procesadas,
 * feeds y acciones históricas sobre entradas.
 * </p>
 * <p>
 * No debe ser instanciada.
 * </p>
 *
 * @author juan
 */
public final class VariablesGlobales {

  /**
   * Mapa que almacena los identificadores de plataforma y los órganos de contratación
   * que se aplican mediante el filtro SQL.
   */
  @Getter
  @Setter
  private static Map<String, String> mapFiltroSql = new HashMap<>();

  /**
   * Mapa que contiene los objetos Entry indexados por su identificador durante la ejecución.
   */
  @Getter
  @Setter
  private static Map<String, Entry> mapEntriesFromAtoms = new HashMap<>();

  /**
   * Mapa que contiene los objetos Entry indexados por su identificador durante la ejecución.
   */
  @Getter
  @Setter
  private static Map<String, Entry> mapEntriesFromBaseDatos = new HashMap<>();

  /**
   * Lista que almacena las acciones (historias) realizadas sobre cada Entry analizada.
   */
  @Getter
  @Setter
  private static List<Historico> listHistoricos = new ArrayList<>();

  /**
   * NewestEntry asociado al tipo de sindicación que se está realizando
   */
  @Getter
  @Setter
  private static Entry newestEntry;

  /**
   * Lugar de Importación para la ejecución actual
   */
  @Getter
  @Setter
  private static LugarImportacion lugarImportacion;

  /**
   * Tipo de Sindicación para la ejecución actual
   */
  @Getter
  @Setter
  private static TipoSindicacion tipoSindicacion;


  /**
   * Fecha y hora inicial usada como filtro para procesar entradas.
   */
  @Getter
  @Setter
  private static LocalDateTime filtroFechaInicial;

  /**
   * Fecha y hora final usada como filtro para procesar entradas.
   */
  @Getter
  @Setter
  private static LocalDateTime filtroFechaFinal;

  /**
   * Filtro basado en objeto, utilizado durante el procesamiento.
   */
  @Getter
  @Setter
  private static String filtroObjeto;

  /**
   * Conjunto de filtros NUTS aplicados durante la ejecución.
   */
  @Getter
  @Setter
  private static HashSet<String> filtroNuts;

  /**
   * Cadena SQL utilizada como filtro durante la ejecución.
   */
  @Getter
  @Setter
  private static String filtroSql;

  /**
   * Constructor privado para evitar instanciación.
   */
  private VariablesGlobales() {
  }
}
