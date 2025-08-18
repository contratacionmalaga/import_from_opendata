package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Entidad que representa una referencia documental asociada a la importación
 * de ficheros Excel desde Internet.
 * <p>
 * La clase permite modelar la relación de un documento principal con diferentes
 * tipos de referencias documentales adicionales, técnicas, legales o generales,
 * así como su posible adjunto asociado.
 * </p>
 *
 * <p>
 * Hereda de {@link Auditable}, lo que permite registrar metadatos de auditoría
 * (como fechas de creación, actualización o usuario responsable).
 * </p>
 *
 * <p>
 * Cada instancia de esta entidad se corresponde con un registro en la tabla
 * <b>document_reference</b> de la base de datos.
 * </p>
 *
 * @author Juan Antonio
 * @version 1.0
 * @since 04/06/2024
 */
@Setter
@Getter
@Entity
@Table(name = "document_reference")
public class DocumentReference extends Auditable {

    /**
     * Constructor por defecto.
     * <p>
     * Requerido por JPA para la correcta creación de proxies
     * y por Lombok para la inicialización básica.
     * </p>
     */
    public DocumentReference() {
        // Constructor vacío requerido por JPA
    }

    /**
     * Identificador único de la entidad en formato UUID.
     * Se genera automáticamente al persistir la entidad.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Identificador del documento de referencia en el sistema origen.
     * Campo obligatorio.
     */
    @Column(name = "id_document_reference", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String idDocumentReference;

    /**
     * Código que indica el tipo de documento referenciado (ej. contrato, anexo, etc.).
     */
    @Column(name = "document_type_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String documentTypeCode;

    /**
     * Relación uno a uno con la entidad {@link AdditionalDocumentReference}.
     * <p>
     * Representa documentación adicional vinculada a la referencia principal.
     * </p>
     */
    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "additional_document_reference_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_documentreference_additionaldocumentreference",
                    foreignKeyDefinition = "FOREIGN KEY (additional_document_reference_id) " +
                            "REFERENCES additional_document_reference(id) ON DELETE CASCADE"))
    private AdditionalDocumentReference additionalDocumentReference;

    /**
     * Relación uno a uno con la entidad {@link TechnicalDocumentReference}.
     * <p>
     * Representa documentación técnica asociada a la referencia.
     * </p>
     */
    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "technical_document_reference_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_documentreference_technicaldocumentreference",
                    foreignKeyDefinition = "FOREIGN KEY (technical_document_reference_id) " +
                            "REFERENCES technical_document_reference(id) ON DELETE CASCADE"))
    private TechnicalDocumentReference technicalDocumentReference;

    /**
     * Relación uno a uno con la entidad {@link LegalDocumentReference}.
     * <p>
     * Representa documentación legal asociada a la referencia.
     * </p>
     */
    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "legal_document_reference_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_documentreference_legaldocumentreference",
                    foreignKeyDefinition = "FOREIGN KEY (legal_document_reference_id) " +
                            "REFERENCES legal_document_reference(id) ON DELETE CASCADE"))
    private LegalDocumentReference legalDocumentReference;

    /**
     * Relación uno a uno con la entidad {@link GeneralDocumentDocumentReference}.
     * <p>
     * Representa referencias generales de documentación.
     * </p>
     */
    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "general_document_document_reference_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_documentreference_generaldocumentdocumentreference",
                    foreignKeyDefinition = "FOREIGN KEY (general_document_document_reference_id) " +
                            "REFERENCES general_document_document_reference(id) ON DELETE CASCADE"))
    private GeneralDocumentDocumentReference generalDocumentDocumentReference;

    /**
     * Relación uno a uno con la entidad {@link Attachment}.
     * <p>
     * Representa un adjunto asociado al documento de referencia.
     * Esta relación está mapeada inversamente desde {@link Attachment}.
     * </p>
     */
    @OneToOne(mappedBy = "documentReference", cascade = CascadeType.MERGE, orphanRemoval = true)
    private Attachment attachment;

    /**
     * Devuelve una representación en cadena del objeto con los datos
     * principales de la entidad.
     *
     * @return cadena con los valores de {@code idDocumentReference} y {@code documentTypeCode}.
     */
    @Override
    public String toString() {
        return "DocumentReference: " +
                "[idDocumentReference='" + idDocumentReference + "', " +
                "documentTypeCode='" + documentTypeCode + "']";
    }
}
