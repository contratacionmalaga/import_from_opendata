package local.jarios.entity.atom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "deleted_entry")
public class DeletedEntry extends AuditableCreatedAt {

  // Primary Key
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Campos de entrada
  @Column(name = "updated")
  private LocalDateTime updated;

  @Column(name = "ref", length = TamanoCampos.TAMANO_500)
  private String ref;

  @Column(name = "ref_corto", length = TamanoCampos.TAMANO_50)
  private String refCorto;

  @Column(name = "comment", length = TamanoCampos.TAMANO_50)
  private String comment;

  // Relaciones
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "feed_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey =
          @ForeignKey(
              name = "fk_deletedentry_feed",
              foreignKeyDefinition = "FOREIGN KEY (feed_id) REFERENCES feed(id) ON DELETE CASCADE"))
  private Feed feed;
}
