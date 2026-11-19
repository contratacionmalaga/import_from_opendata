package local.jarios.helpers;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import local.jarios.common.util.Mensajes;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.HistoricoEntry;
import local.jarios.enums.EntryOpcion;
import local.jarios.filtro.FiltroManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class EntryProcessor {

  private final OpenDataExecutionContext context;
  private final EntryFilter filter;
  private final EntryState state;

  public EntryProcessor(OpenDataExecutionContext context, EntryFilter filter, EntryState state) {
    this.context = Objects.requireNonNull(context, "context");
    this.filter = Objects.requireNonNull(filter, "filter");
    this.state = Objects.requireNonNull(state, "state");
  }

  /** Selecciona la versión más reciente para cada ID Atom. */
  public void processEntries(List<Entry> entries) {
    entries.stream()
        .filter(Objects::nonNull)
        .sorted(
            Comparator.comparing(
                Entry::updatedInstant, Comparator.nullsLast(Comparator.reverseOrder())))
        .forEachOrdered(this::processEntry);
  }

  public void processEntry(Entry entry) {
    Objects.requireNonNull(entry, "entry");

    if (state.shouldApplyFilters() && !filter.matches(entry)) {
      log.debug("entryId={} | NO CUMPLE LOS FILTROS.", entry.getEntryId());
      return;
    }

    upsertByMostRecent(entry);
  }

  private void upsertByMostRecent(Entry entry) {
    String entryId = entry.getEntryId();

    if (entryId == null || entryId.isBlank()) {
      log.debug("Entry sin entryId. Se ignora. Entry={}", entry);
      return;
    }

    Optional<Instant> existingUpdatedOpt = state.getEntryUpdated(entryId);

    if (existingUpdatedOpt.isEmpty()) {
      log.debug("entryId={} | ENTRY NUEVO. Se inserta.", entry.getEntryId());

      state.putEntry(entryId, entry);
      state.addHistorico(
          new HistoricoEntry(
              entry, EntryOpcion.INSERTAR, Mensajes.ENTRY_NUEVO, context.getTipoSindicacion()));
      return;
    }

    resolveConflict(entry, existingUpdatedOpt.get());
  }

  private void resolveConflict(Entry incoming, Instant existingUpdated) {
    Instant incomingUpdated = incoming.updatedInstant();

    boolean reject =
        incomingUpdated == null
            || existingUpdated == null
            || !incomingUpdated.isAfter(existingUpdated);

    if (reject) {
      String motivo =
          String.format(
              "Se RECHAZA el Entry. La Fecha del Entry(%s) es ANTERIOR o IGUAL a la Fecha del Entry en el Map (%s).",
              incomingUpdated, existingUpdated);

      state.addHistorico(
          new HistoricoEntry(incoming, EntryOpcion.RECHAZAR, motivo, context.getTipoSindicacion()));
      log.debug("{}", motivo);
      return;
    }

    String motivo =
        String.format(
            "Se ACTUALIZA el Entry en el MapEntriesFromAtoms. Fecha(EntryEnMemoria) - '%s' isAfter Fecha(EntryEnMap) - '%s'.",
            incomingUpdated, existingUpdated);

    state.putEntry(incoming.getEntryId(), incoming);
    state.addHistorico(
        new HistoricoEntry(incoming, EntryOpcion.ACTUALIZAR, motivo, context.getTipoSindicacion()));
    log.debug("{}", motivo);
  }

  @FunctionalInterface
  public interface EntryFilter {
    boolean matches(Entry entry);
  }

  public interface EntryState {
    boolean shouldApplyFilters();

    Optional<Instant> getEntryUpdated(String entryId);

    void putEntry(String entryId, Entry entry);

    void addHistorico(HistoricoEntry historicoEntry);
  }

  public static final class FiltroManagerEntryFilter implements EntryFilter {
    private final OpenDataExecutionContext context;
    private final FiltroManager filtroManager;

    public FiltroManagerEntryFilter(OpenDataExecutionContext context, FiltroManager filtroManager) {
      this.context = Objects.requireNonNull(context, "context");
      this.filtroManager = Objects.requireNonNull(filtroManager, "filtroManager");
    }

    @Override
    public boolean matches(Entry entry) {
      return filtroManager.evaluarFiltros(context, entry);
    }
  }
}
