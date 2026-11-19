package local.jarios.entity.atom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import local.jarios.common.util.TamanoCampos;
import local.jarios.helpers.AtomIdentity;
import local.jarios.helpers.AtomTimestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "deleted_entry",
    indexes = {@Index(name = "uk_deleted_entry_atom_key", columnList = "ref_atom_key", unique = true)})
public class DeletedEntry {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "updated")
  private LocalDateTime updated;

  @Column(name = "updated_offset_seconds")
  private Integer updatedOffsetSeconds;

  @Column(name = "ref", nullable = false, updatable = false, length = TamanoCampos.TAMANO_2500)
  private String ref;

  @Column(name = "ref_atom_key", nullable = false, updatable = false, length = 64)
  private String refAtomKey;

  @Column(name = "ref_corto", length = TamanoCampos.TAMANO_50)
  private String refCorto;

  @Column(name = "comment", length = TamanoCampos.TAMANO_50)
  private String comment;

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

  public Instant updatedInstant() {
    return AtomTimestamp.instant(updated, updatedOffsetSeconds);
  }

  @PrePersist
  private void indexAtomRef() {
    refAtomKey = AtomIdentity.key(ref);
  }
}
