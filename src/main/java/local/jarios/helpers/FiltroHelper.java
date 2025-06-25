package local.jarios.helpers;

import local.jarios.entity.atom.Entry;
import local.jarios.enums.TipoConexion;
import local.jarios.exceptions.MiInvalidDateFormatException;
import local.jarios.models.FiltroOrganoContratacion;
import local.jarios.services.Service;
import local.jarios.services.ServiceImpl;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.Mensajes;
import local.jarios.common.util.VariablesGlobales;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Juan Antonio
 */
@Slf4j
public final class FiltroHelper {

    /**
     * CONSTRUCTOR DE LA CLASE
     */
    private FiltroHelper() {/* CONSTRUCTOR VACÍO */}



    /**
     *
     * @return Devuelve un Map del tipo Map<IdPlataforma, NombreOrganoContratacion>
     * @throws MiSessionFactoryProviderException Excepción a la hora de generar el objeto Session de Hibernate
     */
    public static Map<String, String> getMapFiltroSql()
            throws  MiSessionFactoryProviderException {

        //
        PropertyManager propertyManager = PropertyManager.getInstance();

        //
        Map<String, String> mapFiltro = new HashMap<>();

        // Creo el objeto Servicio
        Service filterService = new ServiceImpl(PropertyManager.getInstance(), TipoConexion.FILTRO_SQL);

        // Obtengo la consulta
        String filtroSQL = propertyManager.getProperty(PropertyConstantes.FILTRO_SQL);

        // Llamar al método para la obtención de la lista con el filtro
        List<FiltroOrganoContratacion> listFiltroOCs = filterService.getListFiltroOcsFromFiltroSql(filtroSQL);

        // Analizo si la lista con el filtro es vacía
        //     (lo que implicaría que ningún ENTRY podría pertenecer al filtro)
        if (!listFiltroOCs.isEmpty()) {
            // Filtro no vacío

            // Muestro en el LOG el contenido del filtro (NO VACÍO)
            log.info(Mensajes.FILTRO_SQL_LISTADO, listFiltroOCs.size());
            listFiltroOCs.forEach(
                    item -> log.info("{}{}", Constantes.TABULADOR_1, item));

            // Paso de una Lista a un Map (para mejorar la eficiencia a la hora de realizar la búsqueda)
            mapFiltro = MapHelper.getMapFromList(listFiltroOCs);

            //
            if (log.isDebugEnabled()) {
                log.info("Convertida la Lista a Map");
            }
        }

        //
        return mapFiltro;
    }

    /**
     * Función que devuelve si un Entry pertenece al filtro que se encuentra en VariablesGlobales.getMapFiltro()
     *
     * @param entry Entry que vamos a comprobar si pertenece al filtro, para lo cual es necesario que disponga de
     *                    idPlataforma. En caso de no tener idPlataforma
     * @return Valor devuelto:
     *                      TRUE --> No aplica filtro || Tiene idPlataforma y pertenece al filtro
     *                      FALSE -> En cualquier otra situación
     */
    public static boolean getIfFiltroSqlContainsEntry(Entry entry) {

        // Discrimino si existe o no filtro de carga
        if (VariablesGlobales.getMapFiltro().isEmpty()) {
            // En caso de NO APLIAR FILTRO DE CARGA --> Devuelvo TRUE

            if (log.isDebugEnabled()) {
                log.info(Mensajes.FILTRO, Constantes.TABULADOR_2, Constantes.NO, "SQL", true);
            }

            //
            return true;
        }

        // Compruebo si el ENTRY tiene IdPlataforma
        Optional<String> idPlataformaOpt = EntryHelper.getIdPlataformaFromEntry(entry);

        // Si el Entry tiene IdPlataforma, comprobar si está en el mapa de filtros
        if (idPlataformaOpt.isPresent()) {

            //
            var idPlataforma = idPlataformaOpt.get();

            // Comprobamos si el idPlataforma está en el mapa de filtros
            boolean encontrado = VariablesGlobales.getMapFiltro().containsKey(idPlataforma);

            if (log.isDebugEnabled()) {
                log.info("{}IdPlataforma del Entry: {}", Constantes.TABULADOR_1, idPlataforma);
            }

            //
            return encontrado;

        } else {

            // Si no tiene IdPlataforma, logueamos el mensaje correspondiente
            if (log.isDebugEnabled()) {
                log.info(Mensajes.ENTRY_NO_TIENE_IDPLATAFORMA, Constantes.TABULADOR_1, entry.getIdEntry());
            }
            return false;
        }
    }

    /**
     * Función que devuelve si un Entry pertenece al filtro que se encuentra en VariablesGlobales.getMapFiltro()
     *
     * @param entry Entry que vamos a comprobar si pertenece al filtro, para lo cual es necesario que disponga de
     *                    idPlataforma. En caso de no tener idPlataforma
     * @return Valor devuelto:
     *                      TRUE --> No aplica filtro || Tiene idPlataforma y pertenece al filtro
     *                      FALSE -> En cualquier otra situación
     */
    public static boolean getIfFiltroNutsContainsEntry(Entry entry) {

        //
        var propertyReader = PropertyManager.getInstance();

        //
        var filtroNuts = propertyReader.getProperty(PropertyConstantes.FILTRO_NUTS);

        // Discrimino si existe o no filtro de NUTS
        if (filtroNuts.isEmpty()) {
            // En caso de NO APLIAR FILTRO DE NUTS --> Devuelvo TRUE

            log.info(Mensajes.FILTRO, Constantes.TABULADOR_2, Constantes.NO, "NUTS", true);
            return true;
        }

        //
        // En caso de APLICAR FILTRO DE CARGA
        //

        // Compruebo si el ENTRY tiene IdPlataforma
        Optional<String> nutsOptional = EntryHelper.getNutsFromEntry(entry);

        // Si el Entry tiene Nuts
        if (nutsOptional.isPresent()) {

            //
            var nuts = nutsOptional.get();

            // Comprobamos si el idPlataforma está en el mapa de filtros
            boolean encontrado = nuts.equalsIgnoreCase(filtroNuts);

            if (log.isDebugEnabled()) {
                log.info(Mensajes.FILTRO, Constantes.TABULADOR_2, Constantes.SI, "NUTS", encontrado);
                log.info("{}Nuts del Entry: {}", Constantes.TABULADOR_3, nuts);
                log.info("{}Nuts del Filtro: {}", Constantes.TABULADOR_3, filtroNuts);
            }

            //
            return encontrado;

        } else {

            if (log.isDebugEnabled()) {
                // Si no tiene Nuts, logueamos el mensaje correspondiente
                log.info(Mensajes.FILTRO, Constantes.TABULADOR_2, Constantes.SI, "NUTS", false);
                log.info(Mensajes.ENTRY_NO_TIENE_NUTS, Constantes.TABULADOR_3, entry.getIdEntry());
            }

            return false;
        }
    }

    public static boolean getIfFiltroFechasContainsEntry(Entry entry)
            throws MiInvalidDateFormatException {

        var propertyReader = PropertyManager.getInstance();

        var filtroFechaInicial = propertyReader.getProperty(PropertyConstantes.FILTRO_FECHAINICIALLECTURA);
        var filtroFechaFinal = propertyReader.getProperty(PropertyConstantes.FILTRO_FECHAFINALLECTURA);

        //
        if ((filtroFechaInicial.isEmpty()) && (filtroFechaFinal.isEmpty())) {
            // Si ambas fechas son VACÍAS --> No aplica filtro de fecha --> Devuelvo TRUE
            if (log.isDebugEnabled()) {
                log.info(Mensajes.FILTRO, Constantes.TABULADOR_1, Constantes.NO, "FECHAS", true);
            }
            return true;
        }

        // Obtener la fecha actual
        var fechaActual = LocalDate.now();

        // Formatear la fecha en el formato aaaa-MM-dd
        var formatter = DateTimeFormatter.ofPattern(Constantes.FORMATO_FECHA);
        var fechaHoyFormateada = fechaActual.format(formatter);

        // Si la fecha inicial es VACÍA --> Establezco la fecha de hoy
        if (filtroFechaInicial.isEmpty()) {
            // En caso de NO APLIAR FILTRO DE OBJETO --> Devuelvo TRUE
            filtroFechaInicial = fechaHoyFormateada;
        }

        // Si la fecha final es VACÍA --> Estblezco la fecha definida como FINAL_LECTURA
        if (filtroFechaFinal.isEmpty()) {
            // En caso de NO APLIAR FILTRO DE OBJETO --> Devuelvo TRUE
            filtroFechaFinal = Constantes.FECHA_FINAL_LECTURA;
        }

        // Comprobamos si filtroObjeto se encuentra dentro de objetoEntry
        boolean encontrado = FechaHelper.esFechaValida(filtroFechaInicial, filtroFechaFinal, entry.getUpdated());

        // Si el Entry tiene IdPlataforma, comprobar si está en el mapa de filtros
        if (log.isDebugEnabled()) {
            log.info(Mensajes.FILTRO, Constantes.TABULADOR_1, Constantes.SI, "FECHA", encontrado);
            log.info("{}Fecha Inicial del filtro: {}", Constantes.TABULADOR_2, filtroFechaInicial);
            log.info("{}Fecha Final del filtro: {}", Constantes.TABULADOR_2, filtroFechaFinal);
            log.info("{}Fecha del Entry: {}", Constantes.TABULADOR_2, entry.getUpdated());
        }

        //
        return encontrado;
    }

    public static boolean getIfFiltroObjetoContainsEntry(Entry entry) {

        //
        var propertyReader = PropertyManager.getInstance();

        //
        var filtroObjeto = propertyReader.getProperty(PropertyConstantes.FILTRO_OBJETO);

        // Discrimino si existe o no filtro de OBJETO
        if (filtroObjeto.isEmpty()) {
            // En caso de NO APLIAR FILTRO DE OBJETO --> Devuelvo TRUE
            log.info(Mensajes.FILTRO, Constantes.TABULADOR_2, Constantes.NO, "OBJETO", true);
            return true;
        }

        //
        // En caso de APLICAR FILTRO OBJETO
        //

        // Compruebo si el ENTRY tiene IdPlataforma
        var objetoEntry = EntryHelper.getObjetoFromEntry(entry);

        // Comprobamos si filtroObjeto se encuentra dentro de objetoEntry
        boolean encontrado = StringHelper.contieneCadena(objetoEntry, filtroObjeto);

        // Si el Entry tiene IdPlataforma, comprobar si está en el mapa de filtros
        log.info(Mensajes.FILTRO, Constantes.TABULADOR_2, Constantes.SI, "OBJETO", encontrado);

        var lineaLog = StringHelper.limitarLineaLog(objetoEntry);

        log.info("{}Objeto del Entry: {}", Constantes.TABULADOR_3, lineaLog);
        log.info("{}Objeto del Filtro: {}", Constantes.TABULADOR_3, filtroObjeto);

        //
        return encontrado;
    }
}
