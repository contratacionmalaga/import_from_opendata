package local.jarios.helpers;

import local.jarios.common.util.Mensajes;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.enums.EntryOpcion;
import local.jarios.filtro.FiltroManager;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Procesa entries aplicando filtros, deduplicación por entryId y trazabilidad. Diseño: - SRP: solo
 * procesa Entries. - DI: recibe dependencias por constructor (filtros y “estado” externo). - No
 * muta listas de entrada (acepta List.of / inmutables). - Testable: puedes mockear EntryFilter y
 * EntryState.
 */
@Slf4j
public final class EntryProcessor {

  private final EntryFilter filter;
  private final EntryState state;

  public EntryProcessor(EntryFilter filter, EntryState state) {
    this.filter = Objects.requireNonNull(filter, "filter");
    this.state = Objects.requireNonNull(state, "state");
  }

  /**
   * Procesa una lista de Entry (posiblemente inmutable), ordenada por updated DESC. Devuelve true
   * si se “supera” newestEntry (es decir, se encuentra un entry MENOR O IGUAL newestEntry).
   */
  public boolean processEntries(List<Entry> entries) {
    Objects.requireNonNull(entries, "entries");

    var newestEntry = state.getNewestEntry(); // puede ser null
    int readCount = 0;

    // no mutamos la lista de entrada; creamos una vista ordenada
    List<Entry> ordered = entries.stream()
        .sorted(Comparator.comparing(Entry::getUpdated).reversed())
        .toList();

    for (Entry entry : ordered) {
      readCount++;

      if (isNotNewerThanNewest(entry, newestEntry)) {
        log.info("SUPERADO newestEntry. Fin del Parseo. Entries leídos de este Feed: {}.",
                 readCount - 1);
        return true;
      }

      processEntry(entry);
    }

    return false;
  }

  private boolean isNotNewerThanNewest(Entry entry, Entry newestEntry) {
    if (newestEntry == null) return false;
    LocalDateTime updated = entry.getUpdated();
    LocalDateTime newestUpdated = newestEntry.getUpdated();

    // “superado” cuando entry.updated NO es posterior a newestEntry.updated
    return updated == null || newestUpdated == null || !updated.isAfter(newestUpdated);
  }

  /**
   * Procesa un Entry individual: - aplica filtros - si cumple: inserta/actualiza según existencia y
   * fecha
   */
  public void processEntry(Entry entry) {
    Objects.requireNonNull(entry, "entry");

    if (!filter.matches(entry)) {
      log.info("  {} - {} - {} | NO CUMPLE LOS FILTROS.",
               entry.getEntryId(),
               entry.getNifFromEntry(),
               entry.getIdPlataformaFromEntry()
      );
      return;
    }

    upsertByMostRecent(entry);
  }

  private void upsertByMostRecent(Entry incoming) {
    String entryId = incoming.getEntryId();
    if (entryId == null || entryId.isBlank()) {
      // Decide política: ignorar o lanzar excepción
      log.warn("Entry sin entryId. Se ignora. Entry={}", incoming);
      return;
    }

    Optional<Entry> existingOpt = state.getEntry(entryId);

    if (existingOpt.isEmpty()) {
      log.info("  {} - {} - {} | CUMPLE.",
               incoming.getEntryId(),
               incoming.getNifFromEntry(),
               incoming.getIdPlataformaFromEntry()
      );

      state.putEntry(entryId, incoming);
      state.addHistorico(new Historico(incoming, EntryOpcion.INSERTAR, Mensajes.ENTRY_NUEVO));
      return;
    }

    Entry existing = existingOpt.get();
    resolveConflict(incoming, existing);
  }

  private void resolveConflict(Entry incoming, Entry existing) {
    LocalDateTime incomingUpdated = incoming.getUpdated();
    LocalDateTime existingUpdated = existing.getUpdated();

    // Si incoming es anterior o igual => rechazar
    boolean reject = incomingUpdated == null
        || existingUpdated == null
        || !incomingUpdated.isAfter(existingUpdated);

    if (reject) {
      String motivo = String.format(
          "Se RECHAZA el Entry. La Fecha del Entry(%s) es ANTERIOR o IGUAL a la Fecha del Entry en el Map (%s).",
          incomingUpdated,
          existingUpdated
      );

      state.addHistorico(new Historico(incoming, EntryOpcion.RECHAZAR, motivo));
      log.debug("{}", motivo);
      return;
    }

    // incoming es posterior => actualizar
    String motivo = String.format(
        "Se ACTUALIZA el Entry en el MapEntriesFromAtoms. Fecha(EntryEnMemoria) - '%s' isAfter Fecha(EntryEnMap) - '%s'.",
        incomingUpdated,
        existingUpdated
    );

    state.putEntry(incoming.getEntryId(), incoming);
    state.addHistorico(new Historico(existing, EntryOpcion.ACTUALIZAR, motivo));
    log.debug("{}", motivo);
  }

    /* ==========================
       Puertos (interfaces)
       ========================== */

  @FunctionalInterface
  public interface EntryFilter {
    boolean matches(Entry entry);
  }

  public interface EntryState {
    Entry getNewestEntry();

    Optional<Entry> getEntry(String entryId);

    void putEntry(String entryId, Entry entry);

    void addHistorico(Historico historico);
  }

  /* ==========================
     Implementación por defecto del filtro usando tu FiltroManager
     ========================== */
  public static final class FiltroManagerEntryFilter implements EntryFilter {
    private final FiltroManager filtroManager;

    public FiltroManagerEntryFilter(FiltroManager filtroManager) {
      this.filtroManager = Objects.requireNonNull(filtroManager, "filtroManager");
    }

    @Override
    public boolean matches(Entry entry) {
      return filtroManager.evaluarFiltros(entry);
    }
  }
}
