package local.jarios.mappers;

import local.jarios.entity.placsp.Attachment;
import local.jarios.entity.placsp.ExternalReference;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ExternalReferenceType;
import org.dgpe.codice.common.cbclib.DocumentHashType;
import org.dgpe.codice.common.cbclib.FileNameType;
import org.dgpe.codice.common.cbclib.URIType;

import java.util.Optional;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperExternalReference {

    private MapperExternalReference() { }

    public static ExternalReference getExternalReference (
            Attachment attachment,
            ExternalReferenceType externalReferenceType) {

        //
        var externalReference = new ExternalReference();
        externalReference.setAttachment(attachment);

        Optional.ofNullable(externalReferenceType.getDocumentHash())
                .map(DocumentHashType::getValue)
                .ifPresent(externalReference::setDocumentHash);

        Optional.ofNullable(externalReferenceType.getURI())
                .map(URIType::getValue)
                .ifPresent(externalReference::setUri);

        Optional.ofNullable(externalReferenceType.getFileName())
                .map(FileNameType::getValue)
                .ifPresent(externalReference::setFilename);

        log.debug(externalReference.toString());
        return externalReference;
    }
}
