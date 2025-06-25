package local.jarios.helpers;

import local.jarios.entity.Log;
import local.jarios.entity.auxiliares.OrganoContratacion;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Juan Antonio
 */
@Slf4j
public final class OrganoContratacionHelper {

    private OrganoContratacionHelper() { }

    /**
     * Analiza si un String que se pasa es un File válido (EXISTE, SE PUEDA LEER, .entity..)
     *
     * @param mapOrganosContratacion Fichero con la ruta absoluta
     * @return Devuelve un valor indicando si el fichero es valido y en caso contrario indica el motivo
     */
    public static List<OrganoContratacion> getListOrganoContratacion(
            Log miLog, Map<String, String> mapOrganosContratacion) {

        //
        List<OrganoContratacion> listOrganosContratacion = new ArrayList<>();

        //
        for (Map.Entry<String, String> entry : mapOrganosContratacion.entrySet()) {

            //
            OrganoContratacion organoContratacion = new OrganoContratacion();

            //
            organoContratacion.setMiLog(miLog);
            organoContratacion.setIdPlataforma(entry.getKey());
            organoContratacion.setNombre(entry.getValue());

            //
            listOrganosContratacion.add(organoContratacion);
        }

        //
        return listOrganosContratacion;
    }
}
