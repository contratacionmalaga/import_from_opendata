package local.jarios.common.util;

/**
 * Constantes que definen tamaños estándar para campos en la base de datos o en validaciones, para
 * evitar la repetición de valores mágicos.
 * <p>
 * Esta clase es final y no debe ser instanciada.
 * </p>
 *
 * @author Juan Antonio
 * @since 04/06/2024
 */
public final class TamanoCampos {

  /**
   * Tamaño estándar para campos de 15 caracteres.
   */
  public static final int TAMANO_15 = 15;

  /**
   * Tamaño estándar para campos de 100 caracteres.
   */
  public static final int TAMANO_100 = 100;

  /**
   * Tamaño estándar para campos de 2500 caracteres.
   */
  public static final int TAMANO_2500 = 2500;

  /**
   * Constructor privado para evitar instanciación.
   */
  private TamanoCampos() {
  }
}
