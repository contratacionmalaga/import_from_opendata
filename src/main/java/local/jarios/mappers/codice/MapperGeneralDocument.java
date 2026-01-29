package local.jarios.mappers.codice;

import ext.place.codice.common.caclib.GeneralDocumentType;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.DocumentReference;
import local.jarios.enums.TipoDocumento;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Subtipos del modelo Codice Author: juan Date: 11/04/2024 Team: Juan Antonio
 */
@Slf4j
public final class MapperGeneralDocument {

  private MapperGeneralDocument() {
  }

  public static List<DocumentReference> getListGeneralDocumentFromType(
      ContractFolderStatus contractFolderStatus,
      TipoDocumento tipoDocumento,
      List<GeneralDocumentType> generalDocumentTypeList) {

    if (generalDocumentTypeList == null || generalDocumentTypeList.isEmpty()) {
      return List.of();
    }

    List<DocumentReference> result = new ArrayList<>();

    for (GeneralDocumentType gdt : generalDocumentTypeList) {
      if (gdt == null || gdt.getGeneralDocumentDocumentReference() == null) {
        continue;
      }

      DocumentReference dr =
          MapperDocumentReference.getDocumentReferenceFromType(
              gdt.getGeneralDocumentDocumentReference());

      dr.setGeneralDocumentReference(contractFolderStatus);
      dr.setTipoDocumento(tipoDocumento);

      result.add(dr);
    }

    return result;
  }
}
