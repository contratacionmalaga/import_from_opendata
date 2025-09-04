package local.jarios;

import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiServiceException;
import local.jarios.helpers.FeedHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.helpers.TipoSindicacionHelper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase encargada de procesar y comparar entradas (Entry) obtenidas desde fuentes remotas
 * (INTERNET) con las almacenadas en base de datos, determinando cuáles deben insertarse o
 * actualizarse. Extiende {@link AbstractOpenData} para integrar el flujo común de procesamiento de
 * sindicación.
 *
 * @author Juan
 * @since 04/07/2025
 */
@Slf4j
public class OpenDataInternet extends AbstractOpenData {


  /**
   * Mapa que contiene los {@link Entry} que deben ser insertados o actualizados en la base de
   * datos. La clave es el campo idEntry (no UUID).
   */
  @Getter
  private final Map<String, Entry> mapEntriesResultantesCompararMapsFromAtosConMapFromBaseDatos = new HashMap<>();

  /**
   * Método main para ejecutar el procesamiento directamente desde consola o entorno standalone.
   */
  public static void main(String[] args) {
    new OpenDataInternet().procesar();
  }

  /**
   * Obtiene el tipo de sindicación remota (INTERNET).
   *
   * @return tipo de sindicación
   */
  @Override
  protected TipoSindicacion getTipoSindicacion() {
    return new TipoSindicacionHelper().getTipoSindicacionRemota();
  }

  /**
   * Devuelve el lugar de importación como {@link LugarImportacion#INTERNET}.
   */
  @Override
  protected LugarImportacion getLugarImportacion() {
    return LugarImportacion.INTERNET;
  }

  /**
   * Procesa los feeds obtenidos desde internet. 1. Almacena la información en
   * VariablesGlobales.mapEntriesFromAtom (Map#String,Entry#) 2.
   *
   * @throws MiServiceException si hay errores al consultar datos de base de datos
   * @throws MiParseException   si hay errores al parsear feeds remotos
   */
  @Override
  protected void parsearAtomsFeeds() throws MiServiceException, MiParseException {

    // Parsear feeds remotos y cargar resultado en VariablesGlobales
    FeedHelper.parsearFeedsDesdeInternet();

    // Recupero el Map con los Entries (que cumplen los filtros) obtenido desde ATOMS de INTERNET
    Map<String, Entry> mapEntriesFromAtoms = VariablesGlobales.getMapEntriesFromAtoms();
    log.info(
        "[parsearAtomsFeeds] - Entries que cumplen filtros obtenidos desde Atoms de  INTERNET: {}.",
        mapEntriesFromAtoms.size());
    // mapEntriesFromAtoms.values().forEach(e -> log.info("[INTERNET] {}", e.toStringResumido()));

    // Obtengo el Map con los Entries actuales en Base de Datos
    Map<String, Entry> mapEntriesFromBaseDatos = VariablesGlobales.getMapEntriesFromBaseDatos();
    String nEntriesEnBaseDatos = StringHelper.getNumeroConFormato(mapEntriesFromBaseDatos.size());
    log.info("[parsearAtomsFeeds] - Total entries en base de datos: {}.", nEntriesEnBaseDatos);
    // mapEntriesFromBaseDatos.values().forEach(e -> log.info("[parsearAtomsFeeds] {}", e.toStringResumido()));

    // Comparo ambos Mapas (MapEntriesFromAtoms y MapEntriesFromBaseDatos) generando un nuevo Mapa (MapResultado)
    //      que contine  aquellos que son mayores que los existentes en Base de datos o lo que no existan en esta
    mapEntriesFromAtoms.forEach((idEntry, entryFromAtom) -> {

      // Consulto si el Entry en Memoria figura en la Base de Datos
      Entry entryEnBaseDatos = mapEntriesFromBaseDatos.get(idEntry);

      if (entryEnBaseDatos == null) {
        // No existe en BD → insertar

        // Inserto el Entry en el Map Resultante
        mapEntriesResultantesCompararMapsFromAtosConMapFromBaseDatos.put(idEntry, entryFromAtom);
        log.info("[parsearAtomsFeeds] - {} -> INSERTAR.", entryFromAtom.toStringResumido());

      } else {

        // Existe → comparar fechas
        LocalDateTime updatedEntryEnMemoria = entryFromAtom.getUpdated();
        LocalDateTime updatedEntryEnBaseDatos = entryEnBaseDatos.getUpdated();

        if (updatedEntryEnBaseDatos == null ||
            (updatedEntryEnMemoria != null && updatedEntryEnMemoria.isAfter(
                updatedEntryEnBaseDatos))) {
          // Actualización necesaria

          // Conservo el UUID puesto que tengo que realizar un MERGE
          entryFromAtom.setId(entryEnBaseDatos.getId());

          // Inserto el Entry en el Map Resultante
          mapEntriesResultantesCompararMapsFromAtosConMapFromBaseDatos.put(idEntry, entryFromAtom);
          log.info("[parsearAtomsFeeds] - {} -> ACTUALIZAR.", entryFromAtom.toStringResumido());

          /**
           * TRABAJAR CON EL HISTÓRICO PARA ESTABLECER EL VALOR SOBRE LA PRIMERA APARICIÓN DEL OBJETO
           */

        } else {

          log.info("[parsearAtomsFeeds] - {} -> REGISTRAR", entryFromAtom.toStringResumido());

          /**
           * TRABAJAR CON EL HISTÓRICO PARA ESTABLECER EL VALOR SOBRE LA PRIMERA APARICIÓN DEL OBJETO
           */

        }
      }
    });

    //
    String valor = StringHelper
        .getNumeroConFormato(mapEntriesResultantesCompararMapsFromAtosConMapFromBaseDatos.size());
    log.info("[parsearAtomsFeeds] Nº total de registros a enviar a la Base de Datos: {}", valor);
  }
}
