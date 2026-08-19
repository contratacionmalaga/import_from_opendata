package local.jarios.helpers;

import static org.assertj.core.api.Assertions.assertThat;

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
  void updates_entry_when_duplicate_in_memory_is_newer() {
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
        .containsExactly(EntryOpcion.INSERTAR, EntryOpcion.ACTUALIZAR);
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
    public Entry getNewestEntry() {
      return null;
    }

    @Override
    public boolean shouldApplyFilters() {
      return false;
    }

    @Override
    public boolean shouldCompareWithExisting() {
      return true;
    }

    @Override
    public Optional<LocalDateTime> getEntryUpdated(String entryId) {
      Entry entry = entries.get(entryId);
      if (entry != null) {
        return Optional.ofNullable(entry.getUpdated());
      }
      return Optional.ofNullable(snapshots.get(entryId));
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
