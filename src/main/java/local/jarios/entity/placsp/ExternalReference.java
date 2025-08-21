package local.jarios.entity.placsp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import local.jarios.common.util.Constantes;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(
    name = "external_reference"
)

public class ExternalReference extends Auditable {

  //
  //
  //
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "uri", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
  private String uri;

  @Column(name = "document_hash", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String documentHash;

  @Column(name = "filename", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String filename;

  //
  //
  //
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "attachment_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_externalreference_attachment",
          foreignKeyDefinition = "FOREIGN KEY (attachment_id) REFERENCES attachment(id) ON DELETE CASCADE"))
  private Attachment attachment;

  @Override
  public String toString() {

    return "ExternalReference: " +
        "[uri='" + uri + "', " +
        "documentHash='" + documentHash + "', " +
        "filename='" + filename + "']";
  }
}

