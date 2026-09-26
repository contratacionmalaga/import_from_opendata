package local.jarios.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import local.jarios.testsupport.jpa.AbstractJpaConventionTest;
import org.junit.jupiter.api.Test;

class EntryAuditScopeTest extends AbstractJpaConventionTest {

  @Test
  void only_entry_uses_created_and_updated_at_audit_fields() {
    List<Class<?>> auditedEntities =
        getEntityClasses().stream().filter(AuditableCreatedAt.class::isAssignableFrom).toList();

    assertThat(auditedEntities).containsExactly(Entry.class);
  }
}
