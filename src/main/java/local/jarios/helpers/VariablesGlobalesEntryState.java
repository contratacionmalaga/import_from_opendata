package local.jarios.helpers;

import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.Historico;

import java.util.Optional;

public final class VariablesGlobalesEntryState implements EntryProcessor.EntryState {

  @Override
  public Entry getNewestEntry() {
    return VariablesGlobales.getNewestEntry();
  }

  @Override
  public Optional<Entry> getEntry(String entryId) {
    return Optional.ofNullable(VariablesGlobales.getMapEntriesFromAtoms().get(entryId));
  }

  @Override
  public void putEntry(String entryId, Entry entry) {
    VariablesGlobales.getMapEntriesFromAtoms().put(entryId, entry);
  }

  @Override
  public void addHistorico(Historico historico) {
    VariablesGlobales.getListHistoricos().add(historico);
  }
}
