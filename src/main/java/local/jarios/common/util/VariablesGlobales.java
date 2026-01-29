package local.jarios.common.util;

import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Clase final que contiene variables globales utilizadas en todo el proyecto. Esta clase agrupa
 * estructuras de datos estáticas que almacenan información compartida durante la ejecución del
 * programa, como filtros, entradas procesadas, feeds y acciones históricas sobre entradas.
 */
public final class VariablesGlobales {

  /**
   * Mapa que contiene los objetos Entry importados desde los atoms.
   */
  @Getter
  @Setter
  private static Map<String, Entry> mapEntriesFromAtoms = new HashMap<>();

  /**
   * Conjunto que contiene los objetos Feeds leídos de los ficheros Atoms.
   */
  @Getter
  @Setter
  private static Set<Feed> setFeedsFromAtoms = new HashSet<>();

  /**
   * Mapa que contiene los objetos DeletedEntry importados desde los atoms.
   */
  @Getter
  @Setter
  private static Map<String, DeletedEntry> mapDeletedEntriesFromAtoms = new HashMap<>();

  /**
   * Mapa que contiene los objetos Entry existentes en la base de datos.
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
   * NewestEntry asociado al tipo de sindicación que se está realizando.
   */
  @Getter
  @Setter
  private static Entry newestEntry;

  /**
   * Lugar de Importación para la ejecución actual.
   */
  @Getter
  @Setter
  private static LugarImportacion lugarImportacion;

  /**
   * Tipo de Sindicación para la ejecución actual.
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
   * Cadena los dos primeros dígitos del código postal que voy a importar.
   */
  @Getter
  @Setter
  private static String filtroCodigosPostales;

  /**
   * Cadena con los Nifs asociados a los órganos de contratación que voy a importar.
   */
  @Getter
  @Setter
  private static String filtroNifs;

  /**
   * Lista de Nifs obtenida desde filtroNifs para realizar la importación
   */
  @Getter
  @Setter
  private static List<String> listNifs = new ArrayList<>();

  /**
   * Lista de órganos de contratación dados de alta en la Plataforma en el momento de realizar la
   * importación
   */
  @Getter
  @Setter
  private static List<OrganoContratacion> listOrganoContratacionFiltro = new ArrayList<>();

  /**
   * Lista de órganos de contratación dados de alta en la Plataforma en el momento de realizar la
   * importación
   */
  @Getter
  @Setter
  private static List<OrganoContratacion> listOrganoContratacionEnExcel = new ArrayList<>();

  /**
   * String que contiene la fecha de generación del fichero Excel.
   */
  @Getter
  @Setter
  private static Date fechaGeneracionExcel;

  /**
   * Conjunto de nifs asociados a los órganos de contratación para realizar el filtrado. En caso de
   * ser EMPTY no aplica el filtro
   */
  @Getter
  @Setter
  private static Set<String> setNifsFiltro = new HashSet<>();

  /**
   * Constructor privado para evitar instanciación.
   */
  private VariablesGlobales() {
    // SIN IMPLEMENTAR
  }
}
