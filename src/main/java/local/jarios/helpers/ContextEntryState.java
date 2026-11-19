package local.jarios.helpers;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.HistoricoEntry;
import local.jarios.repositories.EntrySnapshot;

public final class ContextEntryState implements EntryProcessor.EntryState {

  private final OpenDataExecutionContext context;

  public ContextEntryState(OpenDataExecutionContext context) {
    this.context = Objects.requireNonNull(context, "context");
  }

  @Override
  public Optional<Instant> getEntryUpdated(String entryId) {
    Entry entry = context.getMapEntriesFromAtoms().get(entryId);

    if (entry != null) {
      return Optional.ofNullable(entry.updatedInstant());
    }

    if (!shouldCompareWithExisting()) {
      return Optional.empty();
    }

    return Optional.ofNullable(context.getMapEntrySnapshotsFromBaseDatos().get(entryId))
        .map(EntrySnapshot::updatedInstant);
  }

  @Override
  public void putEntry(String entryId, Entry entry) {
    context.getMapEntriesFromAtoms().put(entryId, entry);
  }

  @Override
  public void addHistorico(HistoricoEntry historicoEntry) {
    context.addHistorico(historicoEntry);
  }

  @Override
  public boolean shouldApplyFilters() {
    return context.isAplicarFiltros();
  }

  private boolean shouldCompareWithExisting() {
    return context.isCompararConExistentes();
  }
}
