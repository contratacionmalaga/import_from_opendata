package local.jarios.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.repositories.EntrySnapshot;
import org.junit.jupiter.api.Test;

class DeletedEntryScopeResolverTest {

  @Test
  void filtered_local_import_keeps_only_deleted_entries_from_current_import_entries() {
    OpenDataExecutionContext context = new OpenDataExecutionContext();
    context.setAplicarFiltros(true);

    Entry importedEntry = entry("entry-current", "current");
    context.setMapEntriesToBaseDatos(Map.of(importedEntry.getEntryId(), importedEntry));

    Feed feed =
        feed(deletedEntry("entry-current", "current"), deletedEntry("entry-other", "other"));
    context.getConjuntoFeedsFromAtoms().add(feed);
    context.getMapDeletedEntriesFromAtoms().put("entry-current", feed.getDeletedEntryList().get(0));
    context.getMapDeletedEntriesFromAtoms().put("entry-other", feed.getDeletedEntryList().get(1));

    DeletedEntryScopeResolver.retainDeletedEntriesInImportScope(context);

    assertThat(feed.getDeletedEntryList())
        .extracting(DeletedEntry::getRef)
        .containsExactly("entry-current");
    assertThat(context.getMapDeletedEntriesFromAtoms()).containsOnlyKeys("entry-current");
  }

  @Test
  void filtered_internet_import_keeps_deleted_entries_from_current_import_and_existing_entries() {
    OpenDataExecutionContext context = new OpenDataExecutionContext();
    context.setAplicarFiltros(true);

    Entry importedEntry = entry("entry-current", "current");
    context.setMapEntriesToBaseDatos(Map.of(importedEntry.getEntryId(), importedEntry));
    context
        .getMapEntrySnapshotsFromBaseDatos()
        .put(
            "entry-existing",
            new EntrySnapshot("entry-existing", LocalDateTime.parse("2026-01-01T10:00:00")));

    Feed feed =
        feed(
            deletedEntry("entry-current", "current"),
            deletedEntry("entry-existing", "existing"),
            deletedEntry("entry-other", "other"));
    context.getConjuntoFeedsFromAtoms().add(feed);

    DeletedEntryScopeResolver.retainDeletedEntriesInImportScope(context);

    assertThat(feed.getDeletedEntryList())
        .extracting(DeletedEntry::getRef)
        .containsExactly("entry-current", "entry-existing");
    assertThat(context.getMapDeletedEntriesFromAtoms())
        .containsOnlyKeys("entry-current", "entry-existing");
  }

  @Test
  void filtered_import_can_match_current_import_deleted_entries_by_short_ref() {
    OpenDataExecutionContext context = new OpenDataExecutionContext();
    context.setAplicarFiltros(true);

    Entry importedEntry = entry("https://example.test/licitacion-current", "licitacion-current");
    context.setMapEntriesToBaseDatos(Map.of(importedEntry.getEntryId(), importedEntry));

    Feed feed = feed(deletedEntry("different-full-ref", "licitacion-current"));
    context.getConjuntoFeedsFromAtoms().add(feed);

    DeletedEntryScopeResolver.retainDeletedEntriesInImportScope(context);

    assertThat(feed.getDeletedEntryList())
        .extracting(DeletedEntry::getRefCorto)
        .containsExactly("licitacion-current");
    assertThat(context.getMapDeletedEntriesFromAtoms()).containsOnlyKeys("different-full-ref");
  }

  @Test
  void unfiltered_import_keeps_previous_deleted_entry_scope_unchanged() {
    OpenDataExecutionContext context = new OpenDataExecutionContext();
    context.setAplicarFiltros(false);

    Feed feed =
        feed(deletedEntry("entry-current", "current"), deletedEntry("entry-other", "other"));
    context.getConjuntoFeedsFromAtoms().add(feed);
    context.getMapDeletedEntriesFromAtoms().put("entry-current", feed.getDeletedEntryList().get(0));
    context.getMapDeletedEntriesFromAtoms().put("entry-other", feed.getDeletedEntryList().get(1));

    DeletedEntryScopeResolver.retainDeletedEntriesInImportScope(context);

    assertThat(feed.getDeletedEntryList())
        .extracting(DeletedEntry::getRef)
        .containsExactly("entry-current", "entry-other");
    assertThat(context.getMapDeletedEntriesFromAtoms())
        .containsOnlyKeys("entry-current", "entry-other");
  }

  private static Entry entry(String entryId, String entryIdCorto) {
    Entry entry = new Entry();
    entry.setEntryId(entryId);
    entry.setEntryIdCorto(entryIdCorto);
    return entry;
  }

  private static Feed feed(DeletedEntry... deletedEntries) {
    Feed feed = new Feed();
    feed.setLinkSelf("feed-link");
    feed.setDeletedEntryList(List.of(deletedEntries));
    return feed;
  }

  private static DeletedEntry deletedEntry(String ref, String refCorto) {
    DeletedEntry deletedEntry = new DeletedEntry();
    deletedEntry.setRef(ref);
    deletedEntry.setRefCorto(refCorto);
    return deletedEntry;
  }
}
