package local.jarios.entity.codice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Description: Importaciones de Ficheros Excel desde Internet Author: Juan Antonio Date: 04/06/2024
 * Team: Juan Antonio
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "uuid")

//
public class Uuid extends AuditableCreatedAt {

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "scheme_name", nullable = false, length = TamanoCampos.TAMANO_50)
  private String schemeName;

  @Column(name = "uuid", length = TamanoCampos.TAMANO_500)
  private String uuid;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "contract_folder_status_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey =
          @ForeignKey(
              name = "fk_uuid_contractfolderstatus",
              foreignKeyDefinition =
                  "FOREIGN KEY (contract_folder_status_id) "
                      + "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
  private ContractFolderStatus contractFolderStatus;
}
