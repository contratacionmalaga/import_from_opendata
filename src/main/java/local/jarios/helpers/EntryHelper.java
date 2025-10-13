package local.jarios.helpers;

import local.jarios.common.util.Constantes;
import local.jarios.common.util.Mensajes;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.entity.placsp.LocatedContractingParty;
import local.jarios.entity.placsp.Location;
import local.jarios.entity.placsp.Party;
import local.jarios.entity.placsp.PartyIdentification;
import local.jarios.entity.placsp.PreliminaryMarketConsultationStatus;
import local.jarios.entity.placsp.ProcurementProject;
import local.jarios.enums.EntryOpcion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiInvalidDateFormatException;
import local.jarios.filtro.FiltroManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Interfaz para acciones sobre objetos Entry
 */
@Slf4j
public final class EntryHelper {

  private EntryHelper() {/* CONSTRUCTOR VACÍO */}

  /**
   * Función que dado un Entry nos devuelve el valor del campo objeto asociado al expediente
   *
   * @param entry Entry del que devolvemos el valor ProcurementProjectName
   * @return Cadena con el Objeto del Entry. NO devuelvo Optional puesto que el objeto es
   * obligatorio.
   */
  public static String getObjetoFromEntry(Entry entry) {

    return entry.getListContractFolderStatus().stream()
        .findFirst()
        .map(ContractFolderStatus::getProcurementProject)
        .map(ProcurementProject::getName)
        .orElse(Constantes.CADENA_VACIA);
  }

  /**
   * Función que dado un Entry nos devuelve el valor del campo NUTS de realización del expediente
   *
   * @param entry Entry del que devolvemos el valor RealizedLocationCountrySubCode
   * @return Optional con el código NUTS (puede que no esté incluído en el modelo, motivo por el que
   * uso Optional)
   */
  public static Optional<String> getNutsFromEntry(Entry entry) {

    return entry.getListContractFolderStatus().stream()
        .findFirst()
        .map(ContractFolderStatus::getProcurementProject)
        .map(ProcurementProject::getRealizedLocation)
        .map(Location::getCountrySubentityCode);
  }

  /**
   * Función que dado un Entry nos devuelve el valor del campo IdPlataforma del expediente
   *
   * @param entry Entry del que devolvemos el valor PartyIdentificationIdPlataforma
   * @return Optional con el código IdPlataforma (puede que no esté incluído en el modelo, motivo
   * del Optional)
   */
  public static Optional<String> getIdPlataformaFromEntry(Entry entry) {

    Stream<? extends LocatedContractingParty> stream;

    if (VariablesGlobales.getTipoSindicacion() == TipoSindicacion.CPM) {
      stream = entry.getListPreliminaryMarketConsultationStatus().stream()
          .map(PreliminaryMarketConsultationStatus::getLocatedContractingParty);
    } else {
      stream = entry.getListContractFolderStatus().stream()
          .map(ContractFolderStatus::getLocatedContractingParty);
    }

    return stream
        .map(LocatedContractingParty::getParty)
        .map(Party::getPartyIdentification)
        .map(PartyIdentification::getIdPlataforma)
        .findFirst();
  }

  /**
   * Procesa una lista de Entry y devuelve TRUE | FALSE según se haya superado NewestEntry.
   */
  public static boolean procesarListaEntry(List<Entry> listEntry)
      throws MiInvalidDateFormatException {

    // Definición de variables
    Entry newestEntry = VariablesGlobales.getNewestEntry();
    int nEntryLeidos = 0;

    // Ordenar por fecha descendente
    listEntry.sort(Comparator.comparing(Entry::getUpdated).reversed());

    // Proceso todos los entry hasta que se supere el NewestEntry en cuyo caso salgo anticipadamente
    for (Entry entry : listEntry) {

      //
      boolean esMasReciente = newestEntry == null || entry.getUpdated().isAfter(newestEntry.getUpdated());

      //
      if (!esMasReciente) {

        //
        log.info("[procesarListaEntry] SUPERADO newestEntry. Entries leídos del Feed: {}.", nEntryLeidos);
        return true;
      }

      // Aumento el número de entries leídos en este feed
      nEntryLeidos++;

      // Proceso el entry puesto que es más moderno que el NewestEntry
      procesarEntry(entry);
    }

    // Si hemos procesado toda la lista, no se superó newestEntry
    return false;
  }

  /**
   * Función encarga del procesamiento de los objetos Entry Proceso si el Entry cumple con los
   * filtros que estuvieran definidos al inicio de la ejecución
   *
   * @param entry Objeto Entry que se está procesando
   * @throws MiInvalidDateFormatException Excepción en caso de error
   */
  public static void procesarEntry(Entry entry) {

    FiltroManager filtroManager = new FiltroManager();

    // Compruebo que si el entry cumple con los filtros establecidos
    String evaluacionFiltrosEntry = filtroManager.evaluarFiltros(entry);
    log.info("{} -> {}", entry.getIdEntry(), evaluacionFiltrosEntry);

    if (evaluacionFiltrosEntry.equals(Mensajes.ENTRY_CUMPLE_FILTROS)) {
      // Cumple con los filtros

      // Inicio el procesamiento del entry
      procesarEntrySegunExistencia(entry);

    }
  }

  /**
   * Procesa un Entry que ya se que cumple con los filtros Analizo si existe en MapBaseDatos y
   * dependiendo de si existe o no actúo de una forma u otra
   *
   * @param entry Objeto entry que será procesado
   */
  private static void procesarEntrySegunExistencia(Entry entry) {

    // Compruebo si el Entry figura en el MAP
    boolean isEntryInMapFromAtoms = VariablesGlobales.getMapEntriesFromAtoms().containsKey(entry.getIdEntry());

    if (isEntryInMapFromAtoms) {
      // Si existe en el MAP

      // Obtengo
      Entry entryFromAtoms = VariablesGlobales.getMapEntriesFromAtoms().get(entry.getIdEntry());
      log.debug("[procesarEntrySegunExistencia] - Datos del Entry: {}",
                entryFromAtoms.toStringResumido());

      // Como existe en el Map lo tengo que seguir procesando
      procesarUnEntryQueExisteEnMapFromAtoms(entry, entryFromAtoms);

    } else {
      // No existe en el MAP
      log.debug("[procesarEntrySegunExistencia] - NO existe en en el Map.");

      // Lo agrego al MAP de EntriesFromAtoms
      VariablesGlobales.getMapEntriesFromAtoms().put(entry.getIdEntry(), entry);

      // Añado la trazabilidad sobre el Entry en la lista de Históricos
      Historico historico = new Historico(entry, EntryOpcion.INSERTAR, Mensajes.ENTRY_NUEVO);
      VariablesGlobales.getListHistoricos().add(historico);

    }
  }

  /**
   * Procesado de un Entry que figura en el MapBd Comparo el valor del campo Updated asociado al
   * Entry en Memoria y al Entry en la Base de Datos y me quedo con la versión más moderna del
   * Entry
   *
   * @param entry      Objeto Entry que se está procesando
   * @param entryEnMap Objeto Entry que figura en el Map
   */
  private static void procesarUnEntryQueExisteEnMapFromAtoms(Entry entry, Entry entryEnMap) {

    // Defino la variable que expresará el motivo por el que se inserta en el Map de Históricos
    String motivo;

    //
    boolean isEntryAnteriorAlEntryEnMapFromAtoms = entry.getUpdated().isBefore(
        entryEnMap.getUpdated());

    if (isEntryAnteriorAlEntryEnMapFromAtoms) {
      // La fecha del Entry es ANTERIOR o IGUAL a la fecha del Entry que figura en el MapFromAtoms

      motivo = String.format(
          "Se RECHAZA el Entry. " +
              "La Fecha del Entry(%s) es ANTERIOR o IGUAL a la Fecha del Entry en el Map (%s).",
          entry.getUpdated(),
          entryEnMap.getUpdated());

      // Añado la trazabilidad sobre el Entry en la lista de Históricos
      Historico historico = new Historico(entry, EntryOpcion.RECHAZAR, motivo);
      VariablesGlobales.getListHistoricos().add(historico);


    } else {
      // La fecha del Entry es POSTERIOR a la fecha del Entry que figura en el MapFromAtoms

      motivo = String.format(
          "Se ACTUALIZA el Entry en el MapEntriesFromAtoms. " +
              "Fecha(EntryEnMemoria) - '%s' isAfter Fecha(EntryEnMap) - '%s'.",
          entry.getUpdated(),
          entryEnMap.getUpdated());

      // Borro el entryMapBaseDatos del MAP
      VariablesGlobales.getMapEntriesFromAtoms().remove(entryEnMap.getIdEntry());

      // Añado el newEntry al MAP
      VariablesGlobales.getMapEntriesFromAtoms().put(entry.getIdEntry(), entry);

      // Añado la trazabilidad sobre el Entry en la lista de Históricos
      Historico historicoEnMapBd = new Historico(entryEnMap, EntryOpcion.ACTUALIZAR, motivo);
      VariablesGlobales.getListHistoricos().add(historicoEnMapBd);

    }

    log.debug("[procesarEntryExistenteEnMAP] - {}", motivo);
  }
}
