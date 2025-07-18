package local.jarios;

import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.Log;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiParseException;
import local.jarios.helpers.FeedHelper;
import local.jarios.helpers.TipoSindicacionHelper;

/**
 * @author Juan Antonio
 */

public class OpenDataLocal extends AbstractOpenData {

    @Override
    protected TipoSindicacion getTipoSindicacion() {

        return TipoSindicacionHelper.getTipoSindicacionLocal();
    }

    @Override
    protected LugarImportacion getLugarImportacion() {

        return LugarImportacion.LOCAL;
    }

    @Override
    protected void parsearAtomsFeeds(Log log, Estadistica estadistica) throws MiParseException {

        FeedHelper.parsearFeedsDesdeLocal(log, estadistica);
        estadistica.setNEntryGrabados(VariablesGlobales.getMapEntriesFromAtoms().size());
    }

    public static void main(String[] args) {

        new OpenDataLocal().procesar();
    }
}