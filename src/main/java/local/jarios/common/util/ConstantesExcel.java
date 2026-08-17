package local.jarios.common.util;

/**
 * Clase con constantes relacionadas con la importación de datos desde archivos Excel.
 *
 * <p>Contiene índices de columnas, URLs y otras constantes asociadas a las importaciones de:
 *
 * <ul>
 *   <li>Códigos CNAE (INE)
 *   <li>Listado DIR3 (Centro de Transferencia Tecnológica)
 *   <li>Órganos de Contratación (Ministerio de Hacienda - Datos Abiertos)
 * </ul>
 *
 * @author Juan Antonio
 */
public final class ConstantesExcel {

  /*
   * Constantes EXCEL asociadas a la importación de los Órganos de Contratación
   * Información obtenida desde el Ministerio de Hacienda - Datos Abiertos
   */

  /** Índice de columna para ID plataforma en archivo Órganos de Contratación. */
  public static final int EXCEL_OC_COLUMNA_IDPLATAFORMA = 0;

  /** Índice de columna para nombre del órgano de contratación. */
  public static final int EXCEL_OC_COLUMNA_NOMBREOC = 1;

  /** Índice de columna para ubicación del órgano de contratación. */
  public static final int EXCEL_OC_COLUMNA_UBICACION = 2;

  /** Índice de columna para dependencia 1 del órgano. */
  public static final int EXCEL_OC_COLUMNA_DEPENDENCIA1 = 3;

  /** Índice de columna para dependencia 2 del órgano. */
  public static final int EXCEL_OC_COLUMNA_DEPENDENCIA2 = 4;

  /** Índice de columna para NIF del órgano. */
  public static final int EXCEL_OC_COLUMNA_NIF = 5;

  /** Índice de columna para código DIR3 asociado al órgano. */
  public static final int EXCEL_OC_COLUMNA_DIR3 = 6;

  /** Índice de columna para código postal del órgano. */
  public static final int EXCEL_OC_COLUMNA_CODIGOPOSTAL = 7;

  /** Índice de columna para indicador si es medio propio. */
  public static final int EXCEL_OC_COLUMNA_ESMEDIOPROPIO = 8;

  /** Índice de columna para indicador si el órgano está activo. */
  public static final int EXCEL_OC_COLUMNA_ACTIVO = 9;

  /** URL desde donde se descarga el archivo Excel de Órganos de Contratación. */
  public static final String EXCEL_OC_URL =
      "https://contrataciondelsectorpublico.gob.es/datosabiertos/OrganosContratacion.xlsx";

  /** Número de fila donde comienzan los datos en archivo Órganos de Contratación (0-based). */
  public static final int EXCEL_OC_FILA_INICIODATOS = 6;

  /** Número total de columnas esperadas en archivo Órganos de Contratación. */
  public static final int EXCEL_OC_NCOLUMNAS = 10;

  /** Constructor privado para evitar instanciación. */
  private ConstantesExcel() {}
}
