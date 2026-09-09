package local.jarios.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Table;
import local.jarios.entity.auxiliares.Nif;
import local.jarios.entity.auxiliares.OrganoContratacion;
import org.junit.jupiter.api.Test;

class FilterTableNameTest {

  @Test
  void filterEntitiesUseExplicitFilterTableNames() {
    assertThat(Nif.class.getAnnotation(Table.class).name()).isEqualTo("filtros_nifs");
    assertThat(OrganoContratacion.class.getAnnotation(Table.class).name())
        .isEqualTo("filtros_organos_contratacion");
  }
}
