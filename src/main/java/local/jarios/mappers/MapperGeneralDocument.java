package local.jarios.mappers;

import ext.place.codice.common.caclib.GeneralDocumentType;
import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.entity.placsp.GeneralDocument;
import local.jarios.entity.placsp.GeneralDocumentDocumentReference;
import local.jarios.entity.placsp.PreliminaryMarketConsultationStatus;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.DocumentReferenceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperGeneralDocument {

    private MapperGeneralDocument() { }

    public static List<GeneralDocument> getListGeneralDocumentFromType(
            ContractFolderStatus contractFolderStatus,
            PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus,
            List<GeneralDocumentType> listGeneralDocumentType) {

        //
        List<GeneralDocument> listGeneralDocument = new ArrayList<>();

        for (GeneralDocumentType generalDocumentType : listGeneralDocumentType) {

            //
            GeneralDocument generalDocument = new GeneralDocument();
            generalDocument.setContractFolderStatus(contractFolderStatus);
            generalDocument.setPreliminaryMarketConsultationStatus(preliminaryMarketConsultationStatus);

            //
            GeneralDocumentDocumentReference documentReference =
                    getGeneralDocumentDocumentFromType(
                            generalDocument,
                            generalDocumentType.getGeneralDocumentDocumentReference());

            generalDocument.setGeneralDocumentDocumentReference(documentReference);
            listGeneralDocument.add(generalDocument);
        }

        return listGeneralDocument;
    }

    private static GeneralDocumentDocumentReference getGeneralDocumentDocumentFromType (
            GeneralDocument generalDocument,
            DocumentReferenceType documentReferenceType) {

        //
        GeneralDocumentDocumentReference generalDocumentDocumentReference = new GeneralDocumentDocumentReference();
        generalDocumentDocumentReference.setGeneralDocument(generalDocument);

        //
        generalDocumentDocumentReference.setDocumentReference(
                MapperDocumentReference.getDocumentReferenceFromType(
                        null,
                        generalDocumentDocumentReference,
                        null,
                        null,
                        documentReferenceType));

        //
        return generalDocumentDocumentReference;
    }
}
