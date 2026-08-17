package local.jarios.entity.atom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import local.jarios.entity.auxiliares.Log;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "feed")
public class Feed extends AuditableCreatedAt {

  // Primary Key
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Campos del Feed
  @Column(name = "link_first", length = TamanoCampos.TAMANO_2500)
  private String linkFirst;

  @Column(name = "link_prev", length = TamanoCampos.TAMANO_2500)
  private String linkPrev;

  @Column(name = "link_self", nullable = false, length = TamanoCampos.TAMANO_2500)
  private String linkSelf;

  @Column(name = "link_next", length = TamanoCampos.TAMANO_2500)
  private String linkNext;

  @Column(name = "updated")
  private LocalDateTime updated;

  // Relaciones
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "log_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey =
          @ForeignKey(
              name = "fk_feed_milog",
              foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE"))
  private Log miLog;

  @OneToMany(mappedBy = "feed", orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Entry> entryList = new ArrayList<>();

  @OneToMany(mappedBy = "feed", orphanRemoval = true, fetch = FetchType.LAZY)
  private List<DeletedEntry> deletedEntryList = new ArrayList<>();
}
