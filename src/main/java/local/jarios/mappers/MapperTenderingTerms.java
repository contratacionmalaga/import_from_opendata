package local.jarios.mappers;

import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.entity.placsp.ProcurementProjectLot;
import local.jarios.entity.placsp.TenderingTerms;
import local.jarios.helpers.ComunHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.DocumentReferenceType;
import org.dgpe.codice.common.caclib.InvoicingTermsType;
import org.dgpe.codice.common.caclib.TenderingTermsType;
import org.dgpe.codice.common.cbclib.*;
import org.oasis.ubl.common.udt.IndicatorType;

import java.util.Optional;

/**
 * Clase utilitaria para mapear objetos del modelo Codice {@link TenderingTermsType}
 * a la entidad persistente {@link TenderingTerms}.
 * <p>
 * Proporciona métodos estáticos para transformar instancias del modelo Codice
 * en entidades JPA que representan las condiciones de licitación dentro del sistema.
 * </p>
 * <p>
 * Esta clase es final y no instanciable.
 * </p>
 *
 * <p><b>Autor:</b> Juan Antonio</p>
 * <p><b>Fecha:</b> 11/04/2024</p>
 * <p><b>Equipo:</b> Juan Antonio</p>
 */
@Slf4j
public final class MapperTenderingTerms {

    private MapperTenderingTerms() { }

    /**
     * Crea una instancia de {@link TenderingTerms} a partir de un objeto {@link TenderingTermsType}
     * y la asocia a un {@link ContractFolderStatus} y un {@link ProcurementProjectLot}.
     * <p>
     * El método mapea todos los campos disponibles en {@code tenderingTermsType} hacia la entidad persistente,
     * aplicando las correspondientes transformaciones y limitaciones de tamaño.
     * </p>
     *
     * @param contractFolderStatus Entidad padre {@link ContractFolderStatus} a la que se asocia el {@link TenderingTerms}.
     * @param procurementProjectLot Entidad {@link ProcurementProjectLot} asociada al {@link TenderingTerms}.
     * @param tenderingTermsType Objeto fuente con datos del modelo Codice para mapear.
     * @return Instancia de {@link TenderingTerms} con los datos mapeados y lista para persistencia.
     */
    public static TenderingTerms getTenderingTermsFromType(
            ContractFolderStatus contractFolderStatus,
            ProcurementProjectLot procurementProjectLot,
            TenderingTermsType tenderingTermsType) {

        var tenderingTerms = new TenderingTerms();
        tenderingTerms.setContractFolderStatus(contractFolderStatus);
        tenderingTerms.setProcurementProjectLot(procurementProjectLot);

        Optional.ofNullable(tenderingTermsType.getRequiredCurriculaIndicator())
                .map(IndicatorType::isValue)
                .ifPresent(tenderingTerms::setRequiredCurriculaIndicator);

        Optional.ofNullable(tenderingTermsType.getVariantConstraintIndicator())
                .map(IndicatorType::isValue)
                .ifPresent(tenderingTerms::setVariantConstraintIndicator);

        tenderingTerms.setPriceRevisionFormulaDescription(
                ComunHelper.limitarRegistro(
                        MapperStringFromList.getStringFromListPriceRevisionFormulaDescriptionType(
                                tenderingTermsType.getPriceRevisionFormulaDescription()),
                        Constantes.TAMANO_MAXIMO_CAMPO_500));

        tenderingTerms.setFundingProgramCode(
                ComunHelper.limitarRegistro(
                        MapperStringFromList.getStringFromListFundingProgramCodeType(
                                tenderingTermsType.getFundingProgramCode()),
                        Constantes.TAMANO_MAXIMO_CAMPO_500));

        tenderingTerms.setFundingProgram(
                ComunHelper.limitarRegistro(
                        MapperStringFromList.getStringFromListFundingProgramType(
                                tenderingTermsType.getFundingProgram()),
                        Constantes.TAMANO_MAXIMO_CAMPO_500));

        Optional.ofNullable(tenderingTermsType.getProcurementNationalLegislationCode())
                .map(ProcurementNationalLegislationCodeType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(tenderingTerms::setProcurementNationalLegislationCode);

        Optional.ofNullable(tenderingTermsType.getProcurementLegislationDocumentReference())
                .map(DocumentReferenceType::getID)
                .map(IDType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(tenderingTerms::setProcurementLegislationDocumentReference);

        Optional.ofNullable(tenderingTermsType.getReceivedAppealQuantity())
                .map(qty -> qty.getValue().doubleValue())
                .ifPresent(tenderingTerms::setReceivedAppealQuantity);

        Optional.ofNullable(tenderingTermsType.getEorderingIndicator())
                .map(EorderingIndicatorType::isValue)
                .ifPresent(tenderingTerms::setEorderingIndicator);

        Optional.ofNullable(tenderingTermsType.getEpaymentMeansIndicator())
                .map(EpaymentMeansIndicatorType::isValue)
                .ifPresent(tenderingTerms::setEpaymentMeansIndicator);

        Optional.ofNullable(tenderingTermsType.getInvoicingTerms())
                .map(InvoicingTermsType::getElectronicInvoicingIndicator)
                .map(ElectronicInvoicingIndicatorType::isValue)
                .ifPresent(tenderingTerms::setElectronicInvoicingIndicator);

        Optional.ofNullable(tenderingTermsType.getAwardingTerms())
                .ifPresent(awardTerms -> tenderingTerms.setAwardingTerms(
                        MapperAwardingTerms.getAwardingTerms(tenderingTerms, awardTerms)));

        tenderingTerms.setListAllowedSubcontractTerms(
                MapperSubcontractTerms.getListSubcontractTerms(
                        tenderingTerms, null, tenderingTermsType.getAllowedSubcontractTerms()));

        Optional.ofNullable(tenderingTermsType.getContractExecutionRequirement())
                .ifPresent(requirements -> tenderingTerms.setListContractExecutionRequirement(
                        MapperContractExecutionRequirement.getListContractExcecutionRequirement(
                                tenderingTerms, requirements)));

        tenderingTerms.setListFinancialGuarantee(
                MapperFinancialGuarantee.getListFinancialGuarantee(
                        tenderingTerms, tenderingTermsType.getRequiredFinancialGuarantee()));

        Optional.ofNullable(tenderingTermsType.getTendererQualificationRequest())
                .ifPresent(req -> tenderingTerms.setTendererQualificationRequest(
                        MapperTendererQualificationRequest.getTendererQualificationRequest(tenderingTerms, req)));

        Optional.ofNullable(tenderingTermsType.getTenderRecipientParty())
                .ifPresent(party -> tenderingTerms.setTenderRecipientParty(
                        MapperTenderRecipientParty.getTenderRecipientParty(tenderingTerms, party)));

        return tenderingTerms;
    }

    // OneToOne de TenderingTerms
}
