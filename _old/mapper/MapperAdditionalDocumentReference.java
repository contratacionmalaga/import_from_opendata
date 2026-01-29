package local.jarios.mappers.codice;

import local.jarios.entity.codice.AdditionalDocumentReference;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.DocumentReference;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.DocumentReferenceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Subtipos del modelo Codice.
 * <b>Autor:</b> Juan Antonio
 * <b>Fecha:</b> 06/07/2024
 * <b>Equipo:</b> Juan Antonio
 */
@Slf4j
public final class MapperAdditionalDocumentReference {

  private MapperAdditionalDocumentReference() {
  }

  public static List<AdditionalDocumentReference> getListAdditionalDocumentReferenceFromType(
      ContractFolderStatus contractFolderStatus,
      List<DocumentReferenceType> listDocumentReferenceType) {

    //
    List<AdditionalDocumentReference> listAdditionalDocumentReference = new ArrayList<>();

    //
    for (DocumentReferenceType documentReferenceType : listDocumentReferenceType) {

      //
      AdditionalDocumentReference additionalDocumentReference = new AdditionalDocumentReference();

      //
      additionalDocumentReference.setContractFolderStatus(contractFolderStatus);

      //
      DocumentReference documentReference = MapperDocumentReference
          .getDocumentReferenceFromType(
              additionalDocumentReference,
              null,
              null,
              null,
              documentReferenceType);

      //
      additionalDocumentReference.setDocumentReference(documentReference);

      //
      listAdditionalDocumentReference.add(additionalDocumentReference);

    }

    return listAdditionalDocumentReference;
  }
}
