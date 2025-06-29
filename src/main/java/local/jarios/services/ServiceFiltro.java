package local.jarios.services;

import local.jarios.exceptions.MiServiceException;
import local.jarios.models.FiltroOrganoContratacion;

import java.util.List;

/**
 * Interfaz que define los métodos principales para la persistencia y recuperación
 * de información relacionada con la importación de datos abiertos.
 */
public interface ServiceFiltro {

    /**
     * Devuelve una lista de filtros de órganos de contratación a partir de una cláusula SQL.
     *
     * @param filtroSQL el SQL que representa los criterios de filtrado.
     * @return una lista de filtros aplicables a órganos de contratación.
     */
    List<FiltroOrganoContratacion> getListFiltroOcsFromFiltroSql(String filtroSQL) throws MiServiceException;
}

