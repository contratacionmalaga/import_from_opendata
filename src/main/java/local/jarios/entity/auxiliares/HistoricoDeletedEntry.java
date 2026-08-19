package local.jarios.entity.auxiliares;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.enums.DeletedEntryOpcion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "historico_deleted_entry")
public class HistoricoDeletedEntry extends AuditableCreatedAt {

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "feed_linkself", length = TamanoCampos.TAMANO_500)
  private String feedLinkSelf;

  @Column(name = "ref", length = TamanoCampos.TAMANO_500)
  private String ref;

  @Column(name = "ref_corto", length = TamanoCampos.TAMANO_50)
  private String refCorto;

  @Column(name = "previous_updated")
  private LocalDateTime previousUpdated;

  @Column(name = "incoming_updated")
  private LocalDateTime incomingUpdated;

  @Enumerated(EnumType.STRING)
  @Column(name = "opcion", length = TamanoCampos.TAMANO_50)
  private DeletedEntryOpcion opcion;

  @Column(name = "motivo", columnDefinition = "TEXT")
  private String motivo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "deleted_entry_id",
      referencedColumnName = "id",
      foreignKey =
          @ForeignKey(
              name = "fk_historicodeletedentry_deletedentry",
              foreignKeyDefinition =
                  "FOREIGN KEY (deleted_entry_id) REFERENCES deleted_entry(id) ON DELETE SET NULL"))
  private DeletedEntry deletedEntry;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "log_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey =
          @ForeignKey(
              name = "fk_historicodeletedentry_milog",
              foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE"))
  private Log miLog;

  public HistoricoDeletedEntry(
      DeletedEntry deletedEntry, DeletedEntry incoming, DeletedEntryOpcion opcion, String motivo) {
    this.deletedEntry = deletedEntry;
    this.feedLinkSelf = incoming.getFeed() == null ? null : incoming.getFeed().getLinkSelf();
    this.ref = incoming.getRef();
    this.refCorto = incoming.getRefCorto();
    this.previousUpdated = deletedEntry == null ? null : deletedEntry.getUpdated();
    this.incomingUpdated = incoming.getUpdated();
    this.opcion = opcion;
    this.motivo = motivo;
  }
}
