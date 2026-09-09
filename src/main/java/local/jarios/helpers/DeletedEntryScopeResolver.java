package local.jarios.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.repositories.EntrySnapshot;

public final class DeletedEntryScopeResolver {

  private DeletedEntryScopeResolver() {}

  public static void retainDeletedEntriesInImportScope(OpenDataExecutionContext context) {
    Objects.requireNonNull(context, "context");

    if (!context.isAplicarFiltros()) {
      return;
    }

    Set<String> allowedEntryIds = allowedEntryIds(context);
    Set<String> allowedShortEntryIds = allowedShortEntryIds(context);

    context.getMapDeletedEntriesFromAtoms().clear();

    for (Feed feed : context.getConjuntoFeedsFromAtoms()) {
      List<DeletedEntry> scopedDeletedEntries =
          scopedDeletedEntries(feed.getDeletedEntryList(), allowedEntryIds, allowedShortEntryIds);

      feed.setDeletedEntryList(scopedDeletedEntries);

      for (DeletedEntry deletedEntry : scopedDeletedEntries) {
        if (deletedEntry.getRef() != null) {
          context.getMapDeletedEntriesFromAtoms().put(deletedEntry.getRef(), deletedEntry);
        }
      }
    }
  }

  private static Set<String> allowedEntryIds(OpenDataExecutionContext context) {
    Set<String> entryIds = new HashSet<>();
    entryIds.addAll(safeMap(context.getMapEntriesToBaseDatos()).keySet());

    for (EntrySnapshot snapshot : safeMap(context.getMapEntrySnapshotsFromBaseDatos()).values()) {
      if (snapshot != null && snapshot.entryId() != null && !snapshot.entryId().isBlank()) {
        entryIds.add(snapshot.entryId());
      }
    }

    return entryIds;
  }

  private static Set<String> allowedShortEntryIds(OpenDataExecutionContext context) {
    Set<String> shortEntryIds = new HashSet<>();

    for (Entry entry : safeMap(context.getMapEntriesToBaseDatos()).values()) {
      if (entry != null && entry.getEntryIdCorto() != null && !entry.getEntryIdCorto().isBlank()) {
        shortEntryIds.add(entry.getEntryIdCorto());
      }
    }

    return shortEntryIds;
  }

  private static List<DeletedEntry> scopedDeletedEntries(
      List<DeletedEntry> deletedEntries,
      Set<String> allowedEntryIds,
      Set<String> allowedShortEntryIds) {
    if (deletedEntries == null || deletedEntries.isEmpty()) {
      return List.of();
    }

    List<DeletedEntry> scopedDeletedEntries = new ArrayList<>();
    for (DeletedEntry deletedEntry : deletedEntries) {
      if (isAllowed(deletedEntry, allowedEntryIds, allowedShortEntryIds)) {
        scopedDeletedEntries.add(deletedEntry);
      }
    }

    return scopedDeletedEntries;
  }

  private static <K, V> Map<K, V> safeMap(Map<K, V> map) {
    return map == null ? Map.of() : map;
  }

  private static boolean isAllowed(
      DeletedEntry deletedEntry, Set<String> allowedEntryIds, Set<String> allowedShortEntryIds) {
    if (deletedEntry == null) {
      return false;
    }

    String ref = deletedEntry.getRef();
    if (ref != null && allowedEntryIds.contains(ref)) {
      return true;
    }

    String refCorto = deletedEntry.getRefCorto();
    return refCorto != null && allowedShortEntryIds.contains(refCorto);
  }
}
