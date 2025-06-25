package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Description: Importaciones de Ficheros Excel desde Internet
 * Author: Juan Antonio
 * Date: 04/06/2024
 * Team: Juan Antonio
 */

@Setter
@Getter
@Entity
@Table(
        name = "uuid"
)

//
public class Uuid extends Auditable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;


    @Column(name = "scheme_name", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String schemeName;

    @Column(name = "uuid", length = Constantes.TAMANO_MAXIMO_CAMPO_250)
    private String uuid;

    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_folder_status_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_uuid_contractfolderstatus",
                    foreignKeyDefinition =
                            "FOREIGN KEY (contract_folder_status_id) " +
                            "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
    private ContractFolderStatus contractFolderStatus;


    @Override
    public String toString() {

        return ToStringUtil.autoToString(this);
    }

    public Uuid() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
