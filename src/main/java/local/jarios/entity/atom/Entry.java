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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.PreliminaryMarketConsultationStatus;
import local.jarios.enums.TipoSindicacion;
import local.jarios.interfaces.HasIdEntry;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "entry",
    indexes = {
        @Index(name = "idx_unique_entry", columnList = "entry_id", unique = true)
    }
)
public class Entry extends AuditableCreatedAt implements HasIdEntry<Entry> {

  // Primary Key
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Campos de entrada
  @Column(name = "entry_id", nullable = false, unique = true, length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String entryId;

  // Campos de entrada
  @Column(name = "entry_id_corto", nullable = false, unique = true, length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String entryIdCorto;

  @Column(name = "link", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String link;

  @Column(name = "summary", columnDefinition = "TEXT")
  private String summary;

  @Column(name = "title", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
  private String title;

  @Column(name = "updated", nullable = false)
  private LocalDateTime updated;

  // Relaciones
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "feed_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_entry_feed",
          foreignKeyDefinition = "FOREIGN KEY (feed_id) REFERENCES feed(id) ON DELETE CASCADE")
  )
  private Feed feed;

  @OneToMany(mappedBy = "entry", orphanRemoval = true, fetch = FetchType.LAZY)
  private List<ContractFolderStatus> contractFolderStatusList = new ArrayList<>();

  @OneToMany(mappedBy = "entry", orphanRemoval = true, fetch = FetchType.LAZY)
  private List<PreliminaryMarketConsultationStatus> preliminaryMarketConsultationStatusList = new ArrayList<>();

  // Representaciones en texto
  @Override
  public String toString() {
    return "Entry: [" + entryId + ", " + updated + ", " + getNifFromEntry() + ", " + getIdPlataformaFromEntry() + "]";
  }

  /**
   * Obtiene el IdPlataforma asociado a un Entry
   *
   * @return Devuelve el identificador
   */
  public String getIdPlataformaFromEntry() {

    if (VariablesGlobales.getTipoSindicacion() == TipoSindicacion.CPM) {
      return this.getPreliminaryMarketConsultationStatusList().stream()
          .map(PreliminaryMarketConsultationStatus::getIdPlataforma)
          .filter(id -> id != null && !id.isBlank())
          .findFirst()
          .orElse("No se encontró idPlataforma para el entry: " + this.getEntryId());
    }

    // No CPM: si ya tienes el id en ContractFolderStatus, úsalo directamente
    return this.getContractFolderStatusList().stream()
        .map(ContractFolderStatus::getIdPlataforma) // probablemente String
        .filter(id -> id != null && !id.isBlank())
        .findFirst()
        .orElse("No se encontró idPlataforma para el entry: " + this.getEntryId());
  }

  /**
   * Obtiene el IdPlataforma asociado a un Entry
   *
   * @return Devuelve el identificador
   */
  public String getNifFromEntry() {

    if (VariablesGlobales.getTipoSindicacion() == TipoSindicacion.CPM) {
      return this.getPreliminaryMarketConsultationStatusList().stream()
          .map(PreliminaryMarketConsultationStatus::getNif)
          .filter(id -> id != null && !id.isBlank())
          .findFirst()
          .orElseThrow(() -> new IllegalStateException(
              "No se encontró Nif asociado al entry: " + this.getEntryId()
          ));
    }

    // No CPM: si ya tienes el id en ContractFolderStatus, úsalo directamente
    return this.getContractFolderStatusList().stream()
        .map(ContractFolderStatus::getNif) // probablemente String
        .filter(id -> id != null && !id.isBlank())
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "No se encontró Nif asociado al entry: " + this.getEntryId()
        ));
  }
}
