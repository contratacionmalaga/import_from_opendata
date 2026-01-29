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
import local.jarios.common.util.Constantes;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.EntryOpcion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "historico")
public class Historico extends AuditableCreatedAt {

  // Primary Key
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Entry-related fields
  @Column(name = "feed_linkself", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String feedLinkSelf;

  @Column(name = "entry_id", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String entryId;

  @Column(name = "entry_link", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String entryLink;

  @Column(name = "entry_summary", columnDefinition = "TEXT")
  private String entrySummary;

  @Column(name = "entry_title", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
  private String entryTitle;

  @Column(name = "entry_updated")
  private LocalDateTime entryUpdated;

  @Enumerated(EnumType.STRING)
  @Column(name = "opcion")
  private EntryOpcion entryOpcion;

  @Column(name = "motivo", columnDefinition = "TEXT")
  private String entryMotivo;

  @Column(name = "id_plataforma", length = TamanoCampos.TAMANO_50)
  private String idPlataforma;

  @Column(name = "nif", length = TamanoCampos.TAMANO_50)
  private String nif;

  // Relationship
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "log_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_historico_milog",
          foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE")
  )
  private Log miLog;

  // Constructor principal
  public Historico(Entry entry, EntryOpcion opcion, String motivo) {
    this.miLog = entry.getFeed().getMiLog();
    this.feedLinkSelf = entry.getFeed().getLinkSelf();
    this.entryId = entry.getEntryId();
    this.entryLink = entry.getLink();
    this.entrySummary = entry.getSummary();
    this.entryTitle = entry.getTitle();
    this.entryUpdated = entry.getUpdated();
    this.entryOpcion = opcion;
    this.entryMotivo = motivo;
    this.nif = entry.getNifFromEntry();
    this.idPlataforma = entry.getIdPlataformaFromEntry();
  }

  // String representaciones
  @Override
  public String toString() {
    return "Historico: [" +
        "feedLinkSelf='" + feedLinkSelf + "', " +
        "entryId='" + entryId + "', " +
        "entryLink='" + entryLink + "', " +
        "entrySummary='" + entrySummary + "', " +
        "entryTitle='" + entryTitle + "', " +
        "entryUpdated='" + entryUpdated + "', " +
        "entryOpcion='" + entryOpcion + "', " +
        "entryMotivo='" + entryMotivo + "']";
  }
}
