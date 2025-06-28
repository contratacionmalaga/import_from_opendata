package local.jarios.mappers;

import local.jarios.entity.placsp.AdditionalPublicationDocumentReference;
import local.jarios.entity.placsp.Attachment;
import local.jarios.entity.placsp.DocumentReference;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.AttachmentType;

import java.util.Optional;


/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperAttachment {

    private MapperAttachment() {/*  */}

    public static Attachment getAttachment (
            DocumentReference documentReference,
            AdditionalPublicationDocumentReference additionalPublicationDocumentReference,
            AttachmentType attachmentType) {

        //
        Attachment attachment = new Attachment();

        //
        attachment.setDocumentReference(documentReference);

        //
        attachment.setAdditionalPublicationDocumentReference(additionalPublicationDocumentReference);

        //
        Optional.ofNullable(attachmentType.getExternalReference())
                .map(extRef -> MapperExternalReference.getExternalReference(attachment, extRef))
                .ifPresent(attachment::setExternalReference);

        //
        return attachment;

    }
}
