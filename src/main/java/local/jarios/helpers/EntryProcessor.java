package local.jarios.helpers;

import java.time.LocalDateTime;
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

  /**
   * Procesa una lista de Entry (posiblemente inmutable), ordenada por updated DESC. Devuelve true
   * si se “supera” newestEntry (es decir, se encuentra un entry MENOR O IGUAL newestEntry).
   */
  public boolean processEntries(List<Entry> entries) {

    Entry newestEntry = state.shouldCompareWithExisting() ? state.getNewestEntry() : null;

    int readCount = 0;

    List<Entry> ordered =
        entries.stream()
            .filter(Objects::nonNull)
            .sorted(
                (a, b) -> {
                  LocalDateTime ua = a.getUpdated();
                  LocalDateTime ub = b.getUpdated();

                  if (ua == null && ub == null) {
                    return 0;
                  }
                  if (ua == null) {
                    return 1;
                  }
                  if (ub == null) {
                    return -1;
                  }

                  return ub.compareTo(ua);
                })
            .toList();

    for (Entry entry : ordered) {
      readCount++;

      if (isNotNewerThanNewest(entry, newestEntry)) {
        log.info(
            "SUPERADO newestEntry. Fin del Parseo. Entries leídos de este Feed: {}.",
            readCount - 1);
        return true;
      }

      processEntry(entry);
    }

    return false;
  }

  private boolean isNotNewerThanNewest(Entry entry, Entry newestEntry) {
    if (!state.shouldCompareWithExisting()) {
      return false;
    }

    if (newestEntry == null) {
      return false;
    }

    LocalDateTime updated = entry.getUpdated();
    LocalDateTime newestUpdated = newestEntry.getUpdated();

    if (updated == null || newestUpdated == null) {
      return false;
    }

    return !updated.isAfter(newestUpdated);
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

    Optional<LocalDateTime> existingUpdatedOpt = state.getEntryUpdated(entryId);

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

  private void resolveConflict(Entry incoming, LocalDateTime existingUpdated) {
    LocalDateTime incomingUpdated = incoming.getUpdated();

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
    Entry getNewestEntry();

    boolean shouldApplyFilters();

    boolean shouldCompareWithExisting();

    Optional<LocalDateTime> getEntryUpdated(String entryId);

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
