package local.jarios.entity.atom;

import jakarta.persistence.CascadeType;
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
import local.jarios.common.util.Constantes;
import local.jarios.entity.Log;
import local.jarios.entity.auxiliares.Auditable;
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
@Table(name = "feed")
public class Feed extends Auditable {

  // Primary Key
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Campos del Feed
  @Column(name = "link_first", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
  private String linkFirst;

  @Column(name = "link_prev", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
  private String linkPrev;

  @Column(name = "link_self", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
  private String linkSelf;

  @Column(name = "link_next", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
  private String linkNext;

  @Column(name = "updated")
  private LocalDateTime updated;

  // Relaciones
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "log_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_feed_milog",
          foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE")
  )
  private Log miLog;

  @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Entry> listEntry = new ArrayList<>();

  @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<DeletedEntry> listDeletedEntry = new ArrayList<>();

  // Representaciones en texto
  @Override
  public String toString() {
    return "Feed: [" +
        "linkFirst='" + linkFirst + "', " +
        "linkPrev='" + linkPrev + "', " +
        "linkSelf='" + linkSelf + "', " +
        "linkNext='" + linkNext + "', " +
        "updated='" + updated +
        "']";
  }

  public String toStringResumido() {
    return "Feed: [" +
        "linkSelf='" + linkSelf + "', " +
        "updated='" + updated +
        "']";
  }
}
