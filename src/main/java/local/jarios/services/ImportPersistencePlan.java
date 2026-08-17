package local.jarios.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.enums.EntryOpcion;

public record ImportPersistencePlan(
    Log miLog,
    Configuracion configuracion,
    List<String> nifList,
    List<OrganoContratacion> organoContratacionList,
    Set<Feed> feedSet,
    Set<String> replacementEntryIds,
    List<Historico> historicoList,
    Estadistica estadistica) {

  public ImportPersistencePlan(
      Log miLog,
      Configuracion configuracion,
      List<String> nifList,
      List<OrganoContratacion> organoContratacionList,
      Set<Feed> feedSet,
      List<Historico> historicoList,
      Estadistica estadistica) {
    this(
        miLog,
        configuracion,
        nifList,
        organoContratacionList,
        feedSet,
        replacementEntryIdsFromHistoricos(historicoList),
        historicoList,
        estadistica);
  }

  public static Set<String> replacementEntryIdsFromHistoricos(List<Historico> historicos) {
    Set<String> entryIds = new HashSet<>();
    if (historicos == null) {
      return entryIds;
    }

    for (Historico historico : historicos) {
      if (historico == null || historico.getEntryOpcion() != EntryOpcion.ACTUALIZAR) {
        continue;
      }

      String entryId = historico.getEntryId();
      if (entryId != null && !entryId.isBlank()) {
        entryIds.add(entryId);
      }
    }

    return entryIds;
  }
}
