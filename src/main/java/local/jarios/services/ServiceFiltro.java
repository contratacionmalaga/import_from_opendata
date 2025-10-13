package local.jarios.services;

import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.entity.auxiliares.Provincia;
import local.jarios.exceptions.MiServiceException;

import java.util.List;

/**
 * Interfaz que define los métodos principales para la persistencia y recuperación de información
 * relacionada con la importación de datos abiertos.
 */
public interface ServiceFiltro {

  /**
   * Devuelve una lista de filtros de órganos de contratación a partir de una cláusula SQL.
   *
   * @param sql el SQL que representa los criterios de filtrado.
   * @return una lista de filtros aplicables a órganos de contratación.
   */
  List<OrganoContratacion> getListOrganosContratacionFromSql(String sql) throws MiServiceException;

  /**
   * Devuelve una lista con las provincias a partir de una cláusula SQL. Se utiliza para asignar
   * el código NUTS a cada órgnao de contratación.
   *
   * @param sql el SQL que representa los criterios de filtrado.
   * @return una lista de filtros aplicables a órganos de contratación.
   */
  List<Provincia> getListProvinciasFromSql(String sql) throws MiServiceException;
}

