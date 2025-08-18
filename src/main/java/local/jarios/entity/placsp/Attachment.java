package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Representa un archivo adjunto dentro del sistema.
 * <p>
 * Esta entidad extiende de {@link Auditable} para incluir datos de auditoría.
 * </p>
 * <p>
 * Mantiene relaciones uno a uno con {@link DocumentReference},
 * {@link AdditionalPublicationDocumentReference} y {@link ExternalReference}.
 * </p>
 *
 * <p><b>Author:</b> Juan Antonio</p>
 * <p><b>Date:</b> 06/07/2024</p>
 * <p><b>Team:</b> Juan Antonio</p>
 */
@Setter
@Getter
@Entity
@Table(name = "attachment")
public class Attachment extends Auditable {

    /**
     * Constructor por defecto.
     * <p>
     * Requerido por JPA para la correcta creación de proxies
     * y por Lombok para la inicialización básica.
     * </p>
     */
    public Attachment() {
        // Constructor vacío requerido por JPA
    }

    /**
     * Identificador único universal (UUID) del adjunto.
     * <p>
     * Clave primaria generada automáticamente.
     * No puede ser actualizada ni ser nula.
     * </p>
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Referencia al documento asociado al adjunto.
     * <p>
     * Relación uno a uno con {@link DocumentReference}.
     * Se aplica cascada completa y carga perezosa.
     * La eliminación en cascada se asegura mediante la clave foránea.
     * </p>
     */
    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "document_reference_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_attachment_documentreference",
                    foreignKeyDefinition =
                            "FOREIGN KEY (document_reference_id) " +
                            "REFERENCES document_reference(id) ON DELETE CASCADE"))
    private DocumentReference documentReference;

    /**
     * Referencia al documento de publicación adicional asociado al adjunto.
     * <p>
     * Relación uno a uno con {@link AdditionalPublicationDocumentReference}.
     * Se aplica cascada completa y carga perezosa.
     * La eliminación en cascada se asegura mediante la clave foránea.
     * </p>
     */
    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "additional_publication_document_reference_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_attachment_additionalpublicationdocumentreference",
                    foreignKeyDefinition = "FOREIGN KEY (additional_publication_document_reference_id) REFERENCES additional_publication_document_reference(id) ON DELETE CASCADE"))
    private AdditionalPublicationDocumentReference additionalPublicationDocumentReference;

    /**
     * Referencia al documento de publicación adicional asociado al adjunto.
     * <p>
     * Relación uno a uno con {@link AdditionalPublicationDocumentReference}.
     * Se aplica cascada completa y carga perezosa.
     * La eliminación en cascada se asegura mediante la clave foránea.
     * </p>
     */
    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "preliminary_market_consultation_status_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_attachment_preliminarymarketconsultationstatus",
                    foreignKeyDefinition = "FOREIGN KEY (preliminary_market_consultation_status_id) REFERENCES preliminary_market_consultation_status(id) ON DELETE CASCADE"))
    private PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus;

    /**
     * Referencia externa asociada a este adjunto.
     * <p>
     * Relación uno a uno mapeada por el atributo {@code attachment} en {@link ExternalReference}.
     * Se aplican cascada completa y eliminación de huérfanos.
     * </p>
     */
    @OneToOne(mappedBy = "attachment", cascade = CascadeType.MERGE, orphanRemoval = true)
    private ExternalReference externalReference;

    @Override
    public String toString() {

        return "Attachment: []";
    }
}
