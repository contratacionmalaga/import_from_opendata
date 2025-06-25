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
        name = "document_reference"
)
public class DocumentReference extends Auditable {

    //
    //
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "id_document_reference", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_250)
    private String idDocumentReference;

    @Column(name = "document_type_code", length = Constantes.TAMANO_MAXIMO_CAMPO_5)
    private String documentTypeCode;

    //
    //
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "additional_document_reference_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_documentreference_additionaldocumentreference",
                    foreignKeyDefinition =
                            "FOREIGN KEY (additional_document_reference_id) " +
                            "REFERENCES additional_document_reference(id) ON DELETE CASCADE"))
    private AdditionalDocumentReference additionalDocumentReference;

    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "technical_document_reference_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_documentreference_technicaldocumentreference",
                    foreignKeyDefinition =
                            "FOREIGN KEY (technical_document_reference_id) " +
                            "REFERENCES technical_document_reference(id) ON DELETE CASCADE"))
    private TechnicalDocumentReference technicalDocumentReference;

    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "legal_document_reference_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_documentreference_legaldocumentreference",
                    foreignKeyDefinition =
                            "FOREIGN KEY (legal_document_reference_id) " +
                            "REFERENCES legal_document_reference(id) ON DELETE CASCADE"))
    private LegalDocumentReference legalDocumentReference;

    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "general_document_document_reference_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_documentreference_generaldocumentdocumentreference",
                    foreignKeyDefinition =
                            "FOREIGN KEY (general_document_document_reference_id) " +
                            "REFERENCES general_document_document_reference(id) ON DELETE CASCADE"))
    private GeneralDocumentDocumentReference generalDocumentDocumentReference;

    //
    //
    //
    @OneToOne(mappedBy = "documentReference", cascade = CascadeType.ALL, orphanRemoval = true)
    private Attachment attachment;

    @Override
    public String toString() {

        return ToStringUtil.autoToString(this);
    }
    //
    //
    //
    public DocumentReference() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
