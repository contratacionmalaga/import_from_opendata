package local.jarios.repositories;

import java.time.LocalDateTime;

public record EntrySnapshot(String entryId, LocalDateTime updated) {}
