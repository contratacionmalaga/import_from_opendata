package local.jarios.helpers;

import local.jarios.common.util.ConstantesExcel;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.imports.ImportResult;
import local.jarios.managers.ManagerSpreeadSheet;

/**
 * Clase helper para la gestión de la importación y procesamiento de entidades
 * {@link OrganoContratacion} desde un fichero Excel.
 * <p>
 * Proporciona métodos estáticos para obtener listas de órganos de contratación desde hojas de
 * cálculo.
 * </p>
 *
 * <p><b>Author:</b> Juan Antonio</p>
 */
public final class OcHelper {

  /**
   * Constructor privado para evitar instanciación de esta clase estática.
   */
  private OcHelper() {
  }

  /**
   * Obtiene una lista de objetos {@link OrganoContratacion} importados desde un fichero Excel
   * especificado en las constantes de configuración.
   *
   * @return Resultado de la importación que incluye la lista de objetos {@link OrganoContratacion}
   * y la fecha de generación
   */
  public static ImportResult<OrganoContratacion> getListaOcFromExcelInternet() {

    // Obtengo la lista con todas las filas que contiene el excel
    return ManagerSpreeadSheet.importarDesdeExcel(
        ConstantesExcel.EXCEL_OC_URL,
        ManagerSpreeadSheet::getListaOcsFromSheet
    );
  }
}
