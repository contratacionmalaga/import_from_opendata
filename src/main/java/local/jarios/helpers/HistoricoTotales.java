package local.jarios.helpers;

/**
 * Description: Author: juan Date: 29/01/2026 Team:
 */
public record HistoricoTotales(
    long insertar,
    long eliminar,
    long actualizar,
    long rechazar,
    long deletedEntrys

) {

  /**
   * Obtiene la suma de los valores
   *
   * @return Suma de los valores
   */
  public long total() {
    return insertar + eliminar + actualizar + rechazar;
  }
}

