package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
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
        name = "contract_modification"
)

// 4.38 Modificaciones del contrato
public class ContractModification extends Auditable {

    //
    //
    //
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // 4.38.1 Número de contrato
    // Número de contrato sobre el que se realiza la modificación.
    @Column(name = "contract_id", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String contractId;

    // 4.38.2 Número de la modificación
    // Un mismo contrato podrá ser objeto de varias modificaciones, por lo que se indicará un número de modificación
    // para cada una de las modificaciones que se vayan produciendo para el mismo contrato
    @Column(name = "id_contract_modification", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String idContractModification;

    // Fecha de la formalización del contrato
    @Column(name = "issue_date")
    private LocalDate issueDate;

    // Notas asociadas a la formalización del contrato
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Identificador del lote en caso de ser una licitación por lotes
    @Column(name = "contract_modification_lot_id", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String contractModificationLotId;

    //
    // RELACIONES CON ENTIDADES PADRES DE LA QUE ESTA DEPENDE
    //

    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_folder_status_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_documentreference_contractfolderstatus",
                    foreignKeyDefinition =
                            "FOREIGN KEY (contract_folder_status_id) " +
                            "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
    private ContractFolderStatus contractFolderStatus;

    //
    // RELACIONES CON ENTIDADES PADRES DE LA QUE ESTA DEPENDE
    //

    // 4.38.3 Importe sin impuestos de la modificación
    // Importe positivo o negativo dependiendo de si la modificación da como resultado un incremento
    //     o un decremento del importe total del contrato
    @OneToOne(mappedBy = "contractModificationLegalMonetaryTotal", cascade = CascadeType.ALL, orphanRemoval = true)
    private LegalMonetaryTotal contractModificationLegalMonetaryTotal;

    // 4.38.4 Importe sin impuestos del contrato tras la modificación
    // Importe sin impuestos del contrato tras la modificación
    // Suma del importe inicial del contrato más el importe de sus modificaciones
    @OneToOne(mappedBy = "contractModificationFinalLegalMonetaryTotal", cascade = CascadeType.ALL, orphanRemoval = true)
    private LegalMonetaryTotal contractModificationFinalLegalMonetaryTotal;

    @OneToOne(mappedBy = "contractModification", cascade = CascadeType.ALL, orphanRemoval = true)
    private Measure finalDurationMeasure;

    @Override
    public String toString() {

        return "ContractModification: " +
                "[contractId='" + contractId + "', " +
                "issueDate='" + issueDate + "', " +
                "note='" + note + "', " +
                "contractModificationLotId='" + contractModificationLotId + "', " +
                "idContractModification='" + idContractModification + "']";
    }
}
