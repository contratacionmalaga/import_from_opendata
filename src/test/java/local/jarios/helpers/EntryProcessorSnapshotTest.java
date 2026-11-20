package local.jarios.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import local.jarios.core.enums.LugarImportacion;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.HistoricoEntry;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.enums.EntryOpcion;
import org.junit.jupiter.api.Test;

class EntryProcessorSnapshotTest {

  @Test
  void inserts_entry_when_snapshot_does_not_exist() {
    FakeState state = new FakeState(Map.of());
    Entry incoming = entry("entry-1", LocalDateTime.parse("2026-01-02T10:00:00"));

    processor(state).processEntry(incoming);

    assertThat(state.entries).containsEntry("entry-1", incoming);
    assertThat(state.historicos)
        .extracting(HistoricoEntry::getEntryOpcion)
        .containsExactly(EntryOpcion.INSERTAR);
    assertThat(state.historicos).extracting(HistoricoEntry::getMiLog).containsExactly((Log) null);
  }

  @Test
  void rejects_entry_when_snapshot_is_same_or_newer() {
    FakeState state = new FakeState(Map.of("entry-1", LocalDateTime.parse("2026-01-02T10:00:00")));
    Entry incoming = entry("entry-1", LocalDateTime.parse("2026-01-02T10:00:00"));

    processor(state).processEntry(incoming);

    assertThat(state.entries).doesNotContainKey("entry-1");
    assertThat(state.historicos)
        .extracting(HistoricoEntry::getEntryOpcion)
        .containsExactly(EntryOpcion.RECHAZAR);
  }

  @Test
  void updates_entry_when_incoming_is_newer_than_snapshot() {
    FakeState state = new FakeState(Map.of("entry-1", LocalDateTime.parse("2026-01-01T10:00:00")));
    Entry incoming = entry("entry-1", LocalDateTime.parse("2026-01-02T10:00:00"));

    processor(state).processEntry(incoming);

    assertThat(state.entries).containsEntry("entry-1", incoming);
    assertThat(state.historicos)
        .extracting(HistoricoEntry::getEntryOpcion)
        .containsExactly(EntryOpcion.ACTUALIZAR);
  }

  @Test
  void inserts_only_the_latest_version_when_a_new_entry_is_repeated() {
    FakeState state = new FakeState(Map.of());
    Entry first = entry("entry-1", LocalDateTime.parse("2026-01-01T10:00:00"));
    Entry second = entry("entry-1", LocalDateTime.parse("2026-01-02T10:00:00"));

    EntryProcessor entryProcessor = processor(state);
    entryProcessor.processEntry(first);
    entryProcessor.processEntry(second);

    assertThat(state.entries).containsEntry("entry-1", second);
    assertThat(state.entries).hasSize(1);
    assertThat(state.historicos)
        .extracting(HistoricoEntry::getEntryOpcion)
        .containsExactly(EntryOpcion.RECHAZAR, EntryOpcion.INSERTAR);
  }

  @Test
  void rejects_entry_when_duplicate_in_memory_is_not_newer() {
    FakeState state = new FakeState(Map.of());
    Entry first = entry("entry-1", LocalDateTime.parse("2026-01-02T10:00:00"));
    Entry second = entry("entry-1", LocalDateTime.parse("2026-01-01T10:00:00"));

    EntryProcessor entryProcessor = processor(state);
    entryProcessor.processEntry(first);
    entryProcessor.processEntry(second);

    assertThat(state.entries).containsEntry("entry-1", first);
    assertThat(state.entries).hasSize(1);
    assertThat(state.historicos)
        .extracting(HistoricoEntry::getEntryOpcion)
        .containsExactly(EntryOpcion.INSERTAR, EntryOpcion.RECHAZAR);
  }

  @Test
  void compares_the_same_entry_by_instant_during_the_autumn_clock_change() {
    Entry earlier = entry("entry-1", LocalDateTime.parse("2026-10-25T02:55:00"));
    earlier.setUpdatedOffsetSeconds(7200);
    Entry later = entry("entry-1", LocalDateTime.parse("2026-10-25T02:10:00"));
    later.setUpdatedOffsetSeconds(3600);
    FakeState state = new FakeState(Map.of());

    processor(state).processEntries(List.of(earlier, later));

    assertThat(state.entries).containsEntry("entry-1", later);
    assertThat(state.historicos)
        .extracting(HistoricoEntry::getEntryOpcion)
        .containsExactly(EntryOpcion.INSERTAR, EntryOpcion.RECHAZAR);
  }

  @Test
  void processes_an_unknown_entry_older_than_a_different_stored_entry() {
    LocalDateTime cursorUpdated = LocalDateTime.parse("2026-01-02T10:00:00");
    FakeState state = new FakeState(Map.of("otro-expediente", cursorUpdated.plusDays(1)));
    Entry unknownAtCursorTime = entry("entry-unknown", cursorUpdated);

    processor(state).processEntries(List.of(unknownAtCursorTime));

    assertThat(state.entries).containsEntry("entry-unknown", unknownAtCursorTime);
    assertThat(state.historicos)
        .extracting(HistoricoEntry::getEntryOpcion)
        .containsExactly(EntryOpcion.INSERTAR);
  }

  private static EntryProcessor processor(FakeState state) {
    OpenDataExecutionContext context = new OpenDataExecutionContext();
    context.setTipoSindicacion(TipoSindicacion.MAYORES);
    return new EntryProcessor(context, ignored -> true, state);
  }

  private static Entry entry(String entryId, LocalDateTime updated) {
    Log log = new Log(LugarImportacion.INTERNET, TipoSindicacion.MAYORES);

    Feed feed = new Feed();
    feed.setMiLog(log);
    feed.setLinkSelf("feed-link");

    ContractFolderStatus status = new ContractFolderStatus();
    status.setIdPlataforma("platform-1");
    status.setNif("nif-1");

    Entry entry = new Entry();
    entry.setEntryId(entryId);
    entry.setUpdated(updated);
    entry.setFeed(feed);
    entry.setLink("entry-link");
    entry.setTitle("entry-title");
    entry.setSummary("entry-summary");
    entry.getContractFolderStatusList().add(status);

    return entry;
  }

  private static final class FakeState implements EntryProcessor.EntryState {
    private final Map<String, LocalDateTime> snapshots;
    private final Map<String, Entry> entries = new HashMap<>();
    private final List<HistoricoEntry> historicos = new ArrayList<>();

    private FakeState(Map<String, LocalDateTime> snapshots) {
      this.snapshots = snapshots;
    }

    @Override
    public boolean shouldApplyFilters() {
      return false;
    }

    @Override
    public Optional<Instant> getEntryUpdated(String entryId) {
      Entry entry = entries.get(entryId);
      if (entry != null) {
        return Optional.ofNullable(entry.updatedInstant());
      }
      return Optional.ofNullable(AtomTimestamp.instant(snapshots.get(entryId), null));
    }

    @Override
    public void putEntry(String entryId, Entry entry) {
      entries.put(entryId, entry);
    }

    @Override
    public void addHistorico(HistoricoEntry historicoEntry) {
      historicos.add(historicoEntry);
    }
  }
}
