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
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.EntryOpcion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "historico_entry")
public class HistoricoEntry extends AuditableCreatedAt {

  // Primary Key
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Entry-related fields
  @Column(name = "feed_linkself", length = TamanoCampos.TAMANO_500)
  private String feedLinkSelf;

  @Column(name = "entry_id", length = TamanoCampos.TAMANO_500)
  private String entryId;

  @Column(name = "entry_link", length = TamanoCampos.TAMANO_500)
  private String entryLink;

  @Column(name = "entry_summary", columnDefinition = "TEXT")
  private String entrySummary;

  @Column(name = "entry_title", length = TamanoCampos.TAMANO_2500)
  private String entryTitle;

  @Column(name = "entry_updated")
  private LocalDateTime entryUpdated;

  @Enumerated(EnumType.STRING)
  @Column(name = "opcion")
  private EntryOpcion entryOpcion;

  @Column(name = "motivo", columnDefinition = "TEXT")
  private String entryMotivo;

  @Column(name = "id_plataforma", length = TamanoCampos.TAMANO_500)
  private String idPlataforma;

  @Column(name = "nif", length = TamanoCampos.TAMANO_500)
  private String nif;

  // Relationship
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "log_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey =
          @ForeignKey(
              name = "fk_historicoentry_milog",
              foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE"))
  private Log miLog;

  // Constructor principal
  public HistoricoEntry(
      Entry entry, EntryOpcion opcion, String motivo, TipoSindicacion tipoSindicacion) {
    this.feedLinkSelf = entry.getFeed().getLinkSelf();
    this.entryId = entry.getEntryId();
    this.entryLink = entry.getLink();
    this.entrySummary = entry.getSummary();
    this.entryTitle = entry.getTitle();
    this.entryUpdated = entry.getUpdated();
    this.entryOpcion = opcion;
    this.entryMotivo = motivo;
    this.nif = entry.getNifFromEntry(tipoSindicacion);
    this.idPlataforma = entry.getIdPlataformaFromEntry(tipoSindicacion);
  }
}
