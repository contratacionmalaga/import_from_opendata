package local.jarios.mappers;

import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.entity.placsp.LegalDocumentReference;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.DocumentReferenceType;

import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperLegalDocumentReference {

    private MapperLegalDocumentReference() { }

    public static LegalDocumentReference getLegalDocumentReferenceFromDocumentReferenceType (
            ContractFolderStatus contractFolderStatus,
            DocumentReferenceType documentReferenceType) {

        //
        var legalDocumentReference = new LegalDocumentReference();
        legalDocumentReference.setContractFolderStatus(contractFolderStatus);

        Optional.ofNullable(documentReferenceType)
                .map(docRefType -> MapperDocumentReference.getDocumentReferenceFromType(
                        null,
                        null,
                        legalDocumentReference,
                        null,
                        docRefType))
                .ifPresent(legalDocumentReference::setDocumentReference);

        return legalDocumentReference;
    }
}
