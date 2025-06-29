package local.jarios;

import local.jarios.entity.Log;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiParseException;
import local.jarios.helpers.FeedHelper;
import local.jarios.helpers.TipoSindicacionHelper;

public class OpenDataInternet extends AbstractOpenData {

    @Override
    protected TipoSindicacion getTipoSindicacion() {

        return TipoSindicacionHelper.getTipoSindicacion(false);
    }

    @Override
    protected LugarImportacion getLugarImportacion() {

        return LugarImportacion.INTERNET;
    }

    @Override
    protected void parsearFeeds(Log log, Feed newestFeed, Estadistica estadistica) throws MiParseException {

        FeedHelper.parsearFeeds(log, newestFeed, estadistica, false);
    }

    public static void main(String[] args) {
        new OpenDataInternet().procesar();
    }
}
