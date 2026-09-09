package local.jarios.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import local.jarios.entity.auxiliares.HistoricoEntry;
import local.jarios.enums.EntryOpcion;
import org.junit.jupiter.api.Test;

class RepositoryImplUpdateDetectionTest {

  @Test
  void extracts_only_actualizar_entry_ids_for_replacement() {
    HistoricoEntry actualizar = historicoEntry("entry-update", EntryOpcion.ACTUALIZAR);
    HistoricoEntry insertar = historicoEntry("entry-insert", EntryOpcion.INSERTAR);
    HistoricoEntry rechazar = historicoEntry("entry-reject", EntryOpcion.RECHAZAR);
    HistoricoEntry blank = historicoEntry(" ", EntryOpcion.ACTUALIZAR);

    assertThat(
            RepositoryImpl.entryIdsForOption(
                List.of(actualizar, insertar, rechazar, blank), EntryOpcion.ACTUALIZAR))
        .containsExactly("entry-update");
  }

  private static HistoricoEntry historicoEntry(String entryId, EntryOpcion opcion) {
    HistoricoEntry historicoEntry = new HistoricoEntry();
    historicoEntry.setEntryId(entryId);
    historicoEntry.setEntryOpcion(opcion);
    return historicoEntry;
  }
}
