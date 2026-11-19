package local.jarios.repositories;

import java.time.Instant;
import java.time.LocalDateTime;
import local.jarios.helpers.AtomTimestamp;

public record EntrySnapshot(String entryId, LocalDateTime updated, Integer updatedOffsetSeconds) {
  public EntrySnapshot(String entryId, LocalDateTime updated) {
    this(entryId, updated, null);
  }

  public Instant updatedInstant() {
    return AtomTimestamp.instant(updated, updatedOffsetSeconds);
  }
}
