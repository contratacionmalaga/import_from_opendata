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
import local.jarios.common.util.Constantes;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "deleted_entry"
)
public class DeletedEntry extends Auditable {

  // Primary Key
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Campos de entrada
  @Column(name = "updated")
  private LocalDateTime updated;

  @Column(name = "ref", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String ref;

  @Column(name = "comment", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String comment;

  // Relaciones
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "feed_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_deletedentry_feed",
          foreignKeyDefinition = "FOREIGN KEY (feed_id) REFERENCES feed(id) ON DELETE CASCADE")
  )
  private Feed feed;

  // Representaciones en texto
  @Override
  public String toString() {
    return "DeletedEntry: [" +
        "updated='" + updated + "', " +
        "ref='" + ref + "', " +
        "comment='" + comment + "']";
  }
}
