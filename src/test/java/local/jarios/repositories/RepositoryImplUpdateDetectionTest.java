package local.jarios.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.enums.EntryOpcion;
import org.junit.jupiter.api.Test;

class RepositoryImplUpdateDetectionTest {

  @Test
  void extracts_only_actualizar_entry_ids_for_replacement() {
    Historico actualizar = historico("entry-update", EntryOpcion.ACTUALIZAR);
    Historico insertar = historico("entry-insert", EntryOpcion.INSERTAR);
    Historico rechazar = historico("entry-reject", EntryOpcion.RECHAZAR);
    Historico blank = historico(" ", EntryOpcion.ACTUALIZAR);

    assertThat(
            RepositoryImpl.entryIdsForOption(
                List.of(actualizar, insertar, rechazar, blank), EntryOpcion.ACTUALIZAR))
        .containsExactly("entry-update");
  }

  private static Historico historico(String entryId, EntryOpcion opcion) {
    Historico historico = new Historico();
    historico.setEntryId(entryId);
    historico.setEntryOpcion(opcion);
    return historico;
  }
}
