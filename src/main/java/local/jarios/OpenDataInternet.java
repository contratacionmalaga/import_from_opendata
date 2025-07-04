package local.jarios;

import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiServiceException;
import local.jarios.helpers.FeedHelper;
import local.jarios.helpers.TipoSindicacionHelper;
import local.jarios.services.ServicePrincipal;
import local.jarios.services.ServicePrincipalImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class OpenDataInternet extends AbstractOpenData {

    @Getter
    private Set<Entry> entriesAEliminar = Collections.emptySet();

    @Override
    protected TipoSindicacion getTipoSindicacion() {

        return TipoSindicacionHelper.getTipoSindicacion(false);
    }

    @Override
    protected LugarImportacion getLugarImportacion() {

        return LugarImportacion.INTERNET;
    }

    @Override
    protected void parsearFeeds(Log miLog, Estadistica estadistica) throws MiServiceException, MiParseException {

        // 1. Hacer el parseo de los feeds de internet
        Feed newestFeed = FeedHelper.getNewestFeed(getTipoSindicacion());
        FeedHelper.parsearFeedsDesdeInternet(miLog, newestFeed, estadistica);

        // 2. Obtener el mapa con los Entry de la base de datos
        Map<String, Entry> entriesEnBaseDatos;
        ServicePrincipal servicePrincipal = new ServicePrincipalImpl();
        entriesEnBaseDatos = servicePrincipal.getMapEntries(getTipoSindicacion());

        // 3. Obtener el mapa de entries parseados desde internet (guardados en VariablesGlobales)
        Map<String, Entry> entriesDesdeInternet = VariablesGlobales.getMapBaseDatos();

        // 4. Calcular intersección por clave
        Set<String> idsComunes = new HashSet<>(entriesEnBaseDatos.keySet());
        idsComunes.retainAll(entriesDesdeInternet.keySet());

        // 5. Eliminar los ids comunes del map de BD => quedan solo los obsoletos
        Map<String, Entry> entriesObsoletos = new HashMap<>(entriesEnBaseDatos);
        idsComunes.forEach(entriesObsoletos::remove);

        // 6. Obtener Set<Entry> con los obsoletos que deben eliminarse
        this.entriesAEliminar = new HashSet<>(entriesObsoletos.values());

        log.info("[OpenDataInternet] - Entries a eliminar de la base de datos: {}", this.entriesAEliminar.size());
    }

    public static void main(String[] args) {

        new OpenDataInternet().procesar();
    }
}
