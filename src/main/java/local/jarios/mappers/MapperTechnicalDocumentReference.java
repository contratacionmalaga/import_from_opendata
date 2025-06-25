package local.jarios.mappers;

import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.entity.placsp.TechnicalDocumentReference;
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
public final class MapperTechnicalDocumentReference {

    private MapperTechnicalDocumentReference() { }

    public static TechnicalDocumentReference getTechnicalDocumentReferenceFromDocumentReferenceType (
            ContractFolderStatus contractFolderStatus,
            DocumentReferenceType documentReferenceType) {

        //
        TechnicalDocumentReference technicalDocumentReference = new TechnicalDocumentReference();
        technicalDocumentReference.setContractFolderStatus(contractFolderStatus);

        Optional.ofNullable(documentReferenceType)
                .ifPresent(docRefType -> technicalDocumentReference.setDocumentReference(
                        MapperDocumentReference.getDocumentReferenceFromType(
                                null,
                                null,
                                null,
                                technicalDocumentReference,
                                docRefType)));

        log.debug(technicalDocumentReference.toString());

        //
        return technicalDocumentReference;
    }
}
