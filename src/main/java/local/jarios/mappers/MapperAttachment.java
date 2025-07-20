package local.jarios.mappers;

import local.jarios.entity.placsp.AdditionalPublicationDocumentReference;
import local.jarios.entity.placsp.Attachment;
import local.jarios.entity.placsp.DocumentReference;
import local.jarios.entity.placsp.PreliminaryMarketConsultationStatus;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.AttachmentType;

import java.util.Optional;

/**
 * Mapper para transformar objetos {@link AttachmentType} del modelo Codice
 * a entidades internas {@link Attachment}.
 *
 * <p>Permite construir una instancia de {@link Attachment} a partir
 * de los objetos {@link DocumentReference}, {@link AdditionalPublicationDocumentReference}
 * y el tipo {@link AttachmentType} proporcionado.</p>
 *
 * @author juan
 */
@Slf4j
public final class MapperAttachment {

    private MapperAttachment() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Construye un objeto {@link Attachment} a partir de los datos proporcionados.
     *
     * @param documentReference Referencia al documento principal, puede ser null.
     * @param additionalPublicationDocumentReference Referencia a publicación adicional, puede ser null.
     * @param attachmentType Objeto {@link AttachmentType} fuente de datos para el mapeo.
     * @return Instancia de {@link Attachment} construida.
     */
    public static Attachment getAttachmentFromType(
            DocumentReference documentReference,
            AdditionalPublicationDocumentReference additionalPublicationDocumentReference,
            PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus,
            AttachmentType attachmentType) {

        //
        Attachment attachment = new Attachment();
        attachment.setDocumentReference(documentReference);
        attachment.setAdditionalPublicationDocumentReference(additionalPublicationDocumentReference);
        attachment.setPreliminaryMarketConsultationStatus(preliminaryMarketConsultationStatus);

        //
        Optional.ofNullable(attachmentType.getExternalReference())
                .map(extRef -> MapperExternalReference.getExternalReference(attachment, extRef))
                .ifPresent(attachment::setExternalReference);

        return attachment;
    }
}
