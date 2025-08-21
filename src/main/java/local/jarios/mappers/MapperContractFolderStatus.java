package local.jarios.mappers;

import ext.place.codice.common.caclib.ContractFolderStatusType;
import ext.place.codice.common.cbclib.ContractFolderStatusCodeType;
import local.jarios.common.util.Constantes;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.helpers.ComunHelper;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.cbclib.ContractFolderIDType;
import org.w3._2005.atom.EntryType;

import javax.xml.bind.JAXBElement;
import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperContractFolderStatus {

  private MapperContractFolderStatus() {
  }

  public static List<ContractFolderStatus> getListContractFolderStatusFromListType(
      Entry entry, EntryType entryType) {

    return entryType.getAny().stream()
        .filter(JAXBElement.class::isInstance)
        .map(JAXBElement.class::cast)
        .filter(elem -> elem.getDeclaredType().equals(ContractFolderStatusType.class))
        .map(elem -> {
          @SuppressWarnings("unchecked")
          JAXBElement<ContractFolderStatusType> typedElem = (JAXBElement<ContractFolderStatusType>) elem;
          return getContractFolderStatusFromType(entry, typedElem);
        })
        .toList();
  }

  private static ContractFolderStatus getContractFolderStatusFromType(
      Entry entry, JAXBElement<ContractFolderStatusType> jaxbElement) {

    //
    ContractFolderStatusType contractFolderStatusType = jaxbElement.getValue();
    ContractFolderStatus contractFolderStatus = new ContractFolderStatus();

    contractFolderStatus.setEntry(entry);

    Optional.ofNullable(contractFolderStatusType.getContractFolderStatusCode())
        .map(ContractFolderStatusCodeType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(contractFolderStatus::setContractFolderStatusCode);

    Optional.ofNullable(contractFolderStatusType.getContractFolderID())
        .map(ContractFolderIDType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(contractFolderStatus::setContractFolderId);

    Optional.ofNullable(contractFolderStatusType.getProcurementProject())
        .ifPresent(pp -> contractFolderStatus.setProcurementProject(
            MapperProcurementProject.getProcurementProjectFromType(
                contractFolderStatus, null, null, pp)));

    Optional.ofNullable(contractFolderStatusType.getTenderingProcess())
        .ifPresent(tp -> contractFolderStatus.setTenderingProcess(
            MapperTenderingProcess.getTenderingProcessFromType(
                contractFolderStatus, null, tp)));

    // LocatedContractingParty no es nullable según el original, pero si quieres ser más seguro:
    Optional.ofNullable(contractFolderStatusType.getLocatedContractingParty())
        .ifPresent(lcp -> contractFolderStatus.setLocatedContractingParty(
            MapperLocatedContractingParty.getLocatedContractingPartyFromType(
                contractFolderStatus, null, lcp)));

    Optional.ofNullable(contractFolderStatusType.getTenderingTerms())
        .ifPresent(tt -> contractFolderStatus.setTenderingTerms(
            MapperTenderingTerms.getTenderingTermsFromType(
                contractFolderStatus, null, tt)));

    Optional.ofNullable(contractFolderStatusType.getTechnicalDocumentReference())
        .ifPresent(tdr -> contractFolderStatus.setTechnicalDocumentReference(
            MapperTechnicalDocumentReference.getTechnicalDocumentReferenceFromDocumentReferenceType(
                contractFolderStatus, tdr)));

    Optional.ofNullable(contractFolderStatusType.getLegalDocumentReference())
        .ifPresent(ldr -> contractFolderStatus.setLegalDocumentReference(
            MapperLegalDocumentReference.getLegalDocumentReferenceFromDocumentReferenceType(
                contractFolderStatus, ldr)));

    contractFolderStatus.setListAdditionalDocumentReference(
        MapperAdditionalDocumentReference.getListAdditionalDocumentReferenceFromType(
            contractFolderStatus,
            contractFolderStatusType.getAdditionalDocumentReference()));

    contractFolderStatus.setListGeneralDocument(
        MapperGeneralDocument.getListGeneralDocumentFromType(
            contractFolderStatus,
            null,
            contractFolderStatusType.getGeneralDocument()));

    contractFolderStatus.setListContractModification(
        MapperContractModification.getListContractModificatoinFromType(
            contractFolderStatus,
            contractFolderStatusType.getContractModification()));

    contractFolderStatus.setListUuid(
        MapperUuid.getListUuidFromType(
            contractFolderStatus,
            contractFolderStatusType.getUUID()));

    contractFolderStatus.setListNoticeInfo(
        MapperNoticeInfo.getListNoticeInfoFromType(
            contractFolderStatus, null, contractFolderStatusType.getValidNoticeInfo()));

    contractFolderStatus.setListTenderResult(
        MapperTenderResult.getListTenderResultFromType(
            contractFolderStatus,
            contractFolderStatusType.getTenderResult()));

    contractFolderStatus.setListProcurementProjectLot(
        MapperProcurementProjectLot.getListProcurementProjectLotFromType(
            contractFolderStatus,
            contractFolderStatusType.getProcurementProjectLot()));

    return contractFolderStatus;
  }
}
