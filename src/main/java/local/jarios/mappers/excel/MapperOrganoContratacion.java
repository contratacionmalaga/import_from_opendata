package local.jarios.mappers.excel;

import local.jarios.common.util.ConstantesExcel;
import local.jarios.entity.auxiliares.OrganoContratacion;
import lombok.extern.slf4j.Slf4j;

/**
 * Clase utilitaria para mapear filas de Excel (representadas como arrays de String) a entidades del
 * dominio{@link OrganoContratacion}.
 *
 * <p>Esta clase está diseñada para facilitar la conversión de datos importados desde
 * hojas de cálculo a objetos manejables en la aplicación.</p>
 *
 * <p>Clase final con constructor privado para evitar instanciación.</p>
 *
 * @author Juan Antonio
 * @since 04/06/2024
 */
@Slf4j
public final class MapperOrganoContratacion {

  /**
   * Constructor privado para evitar instanciación.
   */
  private MapperOrganoContratacion() {
    // SIN IMPLEMENTAR
  }

  /**
   * Mapea una fila de Excel al objeto {@link OrganoContratacion}.
   *
   * @param row Array de String con los valores de la fila
   * @return Instancia de {@link OrganoContratacion} con datos asignados
   */
  public static OrganoContratacion getOc(String[] row) {

    // 1. Creo el objeto
    var oc = new OrganoContratacion();

    // 2. Asigno los valores asociados al objeto desde la fila de Excel
    oc.setIdPlataforma(row[ConstantesExcel.EXCEL_OC_COLUMNA_IDPLATAFORMA]);
    oc.setNombreOc(row[ConstantesExcel.EXCEL_OC_COLUMNA_NOMBREOC]);
    oc.setUbicacion(row[ConstantesExcel.EXCEL_OC_COLUMNA_UBICACION]);
    oc.setDependencia1(row[ConstantesExcel.EXCEL_OC_COLUMNA_DEPENDENCIA1]);
    oc.setDependencia2(row[ConstantesExcel.EXCEL_OC_COLUMNA_DEPENDENCIA2]);
    oc.setNif(row[ConstantesExcel.EXCEL_OC_COLUMNA_NIF]);
    oc.setDir3(row[ConstantesExcel.EXCEL_OC_COLUMNA_DIR3]);
    oc.setCodigoPostal(row[ConstantesExcel.EXCEL_OC_COLUMNA_CODIGOPOSTAL]);
    oc.setMedioPropio(row[ConstantesExcel.EXCEL_OC_COLUMNA_ESMEDIOPROPIO]);
    oc.setActivo(row[ConstantesExcel.EXCEL_OC_COLUMNA_ACTIVO]);

    // 3. Imprimo en DEBUG el objeto mapeado
    log.debug("[getOc] - Órgano de Contratación Mapeado: {}", oc);

    // 4. Devuelvo el objeto
    return oc;
  }
}
