package local.jarios.common.util;

/**
 * Clase final que contiene constantes globales utilizadas en toda la aplicación.
 *
 * <p>Estas constantes incluyen configuraciones para encriptación, formatos de fecha, nombres de
 * archivos, tipos de enlaces, delimitadores, espacios de nombres JAXB, tamaños máximos de campos
 * para validaciones, identificadores específicos para el componente PARTY, y valores comunes usados
 * en diferentes contextos.
 *
 * <p>Esta clase no debe ser instanciada.
 *
 * @author Home
 */
public final class Constantes {

  /** Formato de fecha para logs y visualización. */
  public static final String JSON_VACIO = "{}";

  /**
   * Nombre del directorio donde se almacenan los archivos de configuración de la aplicación, como
   * properties u otros ficheros de ajustes.
   */
  public static final String PROPERTIES_DIR = "properties";

  /**
   * Cadena vacía estándar para evitar valores nulos y simplificar la manipulación de cadenas en la
   * aplicación.
   */
  public static final String CADENA_VACIA = "";

  /**
   * Caracter de retorno de carro estándar (ASCII 13), utilizado en algunas operaciones de texto o
   * formatos específicos.
   */
  public static final String RETORNO_CARRO = "\r";

  /** Caracter de salto de línea estándar (ASCII 10), usado para separar líneas de texto. */
  public static final String SALTO_LINEA = "\n";

  /**
   * Representación HTML del retorno de carro, útil para formateo en textos que se mostrarán en
   * entornos web.
   */
  public static final String RETORNO_CARRO_HTML = "&#xD;";

  public static final String PUNTO = ".";
  public static final String GUION = "-";

  /**
   * Formato estándar para fecha y hora en la aplicación, compatible con la mayoría de parsers y
   * formatos de bases de datos. Ejemplo: 2024-07-18 15:30:00
   */
  public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

  /**
   * Nombre del archivo principal para licitaciones mayores, utilizado para la importación o
   * procesamiento de datos.
   */
  public static final String FILENAME_MAYORES = "licitacionesPerfilesContratanteCompleto3.atom";

  /**
   * Nombre del archivo que contiene contratos menores, utilizado para la importación o
   * procesamiento específico.
   */
  public static final String FILENAME_MENORES = "contratosMenoresPerfilesContratantes.atom";

  /**
   * Archivo que contiene plataformas agregadas excluyendo los contratos menores, para análisis o
   * procesamiento de datos.
   */
  public static final String FILENAME_AGREGADAS = "PlataformasAgregadasSinMenores.atom";

  /**
   * Archivo relacionado con encargos medios propios del sector público, para importar datos
   * específicos de este ámbito.
   */
  public static final String FILENAME_ENCARGOSMEDIOSPROPIOS = "EMP_SectorPublico.atom";

  /**
   * Archivo para consultas preliminares de mercado en el sector público, utilizado para importar o
   * analizar información.
   */
  public static final String FILENAME_CONSULTASPRELIMINARESMERCADO = "CPM_SectorPublico.atom";

  /* Tipos de LINKs usados en paginación o referencias en documentos */

  /** Enlace que apunta a la primera página o recurso. */
  public static final String LINK_FIRST = "first";

  /** Enlace que apunta a la página o recurso siguiente. */
  public static final String LINK_NEXT = "next";

  /** Enlace que apunta al recurso actual (self-reference). */
  public static final String LINK_SELF = "self";

  /** Enlace que apunta a la página o recurso anterior. */
  public static final String LINK_PREV = "prev";

  /* Delimitadores usados en la generación de cadenas o logs */

  /** Delimitador de salto de línea (nueva línea) estándar. */
  public static final String CR = "\n";

  /* Formatos de fecha */

  /**
   * Formato estándar para fechas sin tiempo, útil para validaciones y conversiones. Ejemplo:
   * 2024-07-18
   */
  public static final String FORMATO_FECHA = "yyyy-MM-dd";

  /**
   * Fecha límite para lectura o procesamiento de datos históricos. Datos anteriores a esta fecha
   * pueden ser descartados o tratados de forma especial.
   */
  public static final String FECHA_FINAL_LECTURA = "2018-01-01";

  /* Espacios de nombres JAXB para parsing XML */

  /** Espacio de nombres JAXB para documentos Atom 2005. */
  public static final String JAXB_ATOM = "org.w3._2005.atom";

  /** Espacio de nombres JAXB para librería común CACLIB. */
  public static final String JAXB_ORG_DGPE_CODICE_COMMON_CACLIB = "org.dgpe.codice.common.caclib";

  /** Espacio de nombres JAXB para librería común CBCLIB. */
  public static final String JAXB_ORG_DGPE_CODICE_COMMON_CBCLIB = "org.dgpe.codice.common.cbclib";

  /** Espacio de nombres JAXB para extensión PLACE CACLIB. */
  public static final String JAXB_EXT_PLACE_CODICE_COMMON_CACLIB = "ext.place.codice.common.caclib";

  /** Espacio de nombres JAXB para extensión PLACE CBCLIB. */
  public static final String JAXB_EXT_PLACE_CODICE_COMMON_CBCLIB = "ext.place.codice.common.cbclib";

  /** Espacio de nombres JAXB para manejo de tombstones (recursos eliminados). */
  public static final String JAXB_TOMBSTONES = "org.purl.atompub.tombstones._1";

  /* Identificadores específicos para el componente PARTY */

  /** Identificador DIR3 usado para representar direcciones o entidades. */
  public static final String DIR3 = "DIR3";

  /** Identificador de plataforma, usado para diferenciar plataformas en registros. */
  public static final String IDPLATAFORMA = "ID_PLATAFORMA";

  /** Identificador de orden de compra de plataforma. */
  public static final String IDOCPLAT = "ID_OC_PLAT";

  /** Identificador fiscal NIF (Número de Identificación Fiscal). */
  public static final String NIF = "NIF";

  /** Identificador genérico para otros tipos no clasificados. */
  public static final String OTROS = "OTROS";

  /** Valor utilizado para representar un valor nulo o no definido en cadenas. */
  public static final String NULL = "NULL";

  public static final String DIRECTORIO_MAY = "may\\";
  public static final String DIRECTORIO_MEN = "men\\";
  public static final String DIRECTORIO_EMP = "emp\\";
  public static final String DIRECTORIO_CPM = "cpm\\";
  public static final String DIRECTORIO_AGR = "agr\\";

  /** Constructor privado para evitar instanciación de esta clase utilitaria. */
  private Constantes() {
    // Evita la creación de instancias
  }
}
