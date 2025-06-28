package local.jarios.mappers;

import local.jarios.common.util.Constantes;
import local.jarios.entity.placsp.*;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.DocumentReferenceType;
import org.dgpe.codice.common.cbclib.DocumentTypeCodeType;

import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperDocumentReference {

    private MapperDocumentReference() { /* CONSTRUCTOR VACÍO */ }

    public static DocumentReference getDocumentReferenceFromType(
            AdditionalDocumentReference additionalDocumentReference,
            GeneralDocumentDocumentReference generalDocumentDocument,
            LegalDocumentReference legalDocumentReference,
            TechnicalDocumentReference technicalDocumentReference,
            DocumentReferenceType documentReferenceType) {

        //
        DocumentReference documentReference = new DocumentReference();

        //
        documentReference.setAdditionalDocumentReference(additionalDocumentReference);
        documentReference.setGeneralDocumentDocumentReference(generalDocumentDocument);
        documentReference.setLegalDocumentReference(legalDocumentReference);
        documentReference.setTechnicalDocumentReference(technicalDocumentReference);
        documentReference.setIdDocumentReference(documentReferenceType.getID().getValue());

        //
        String docTypeCode = Optional.ofNullable(documentReferenceType.getDocumentTypeCode())
                                     .map(DocumentTypeCodeType::getValue)
                                     .orElse(Constantes.CADENA_VACIA);
        documentReference.setDocumentTypeCode(docTypeCode);

        Optional.ofNullable(documentReferenceType.getAttachment())
                .ifPresent(attachmentType -> documentReference.setAttachment(
                        MapperAttachment.getAttachment(documentReference, null, attachmentType)));

        log.debug(documentReference.toString());
        return documentReference;
    }
}
