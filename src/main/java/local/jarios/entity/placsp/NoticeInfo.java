package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
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
        name = "notice_info"
)
public class NoticeInfo extends Auditable {

    //
    //
    //
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "notice_type_code", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String noticeTypeCode;

    //
    //
    //
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_folder_status_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_noticeinfo_contractfolderstatus",
                    foreignKeyDefinition =
                            "FOREIGN KEY (contract_folder_status_id) " +
                            "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
    private ContractFolderStatus contractFolderStatus;

    //
    //
    //
    @OneToMany(mappedBy = "noticeInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AdditionalPublicationStatus> listAdditionalPublicationStatus = new ArrayList<>();

    @Override
    public String toString() {

        return "NoticeInfo: " +
                "[noticeTypeCode='" + noticeTypeCode + "']";
    }
}
