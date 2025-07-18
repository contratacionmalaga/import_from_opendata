package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Entidad que representa una referencia adicional de documento,
 * típicamente utilizada para importar ficheros Excel desde Internet
 * y asociarlos a un estado de carpeta de contrato.
 *
 * <p>Hereda propiedades auditables comunes como creación y modificación.</p>
 *
 * @author Juan Antonio
 * @since 04/06/2024
 */
@Setter
@Getter
@Entity
@Table(name = "additional_document_reference")
public class AdditionalDocumentReference extends Auditable {

    /**
     * Identificador único de esta entidad, generado automáticamente como UUID.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Relación de muchos a uno con {@link ContractFolderStatus}.
     * <p>Indica el estado de la carpeta del contrato al que está asociada esta referencia adicional.</p>
     * <p>Al eliminar el estado de carpeta de contrato, se eliminarán las referencias asociadas gracias a {@code ON DELETE CASCADE}.</p>
     */
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_folder_status_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_aditionaldocumentreference_contractfolderstatus",
                    foreignKeyDefinition = "FOREIGN KEY (contract_folder_status_id) REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
    private ContractFolderStatus contractFolderStatus;

    /**
     * Relación uno a uno con {@link DocumentReference}.
     * <p>Esta referencia adicional tiene una referencia documental asociada,
     * que se mantiene sincronizada mediante cascada y eliminación en órfano.</p>
     */
    @OneToOne(mappedBy = "additionalDocumentReference", cascade = CascadeType.ALL, orphanRemoval = true)
    private DocumentReference documentReference;
}
