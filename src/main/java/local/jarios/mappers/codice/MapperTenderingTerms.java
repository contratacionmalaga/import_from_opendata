package local.jarios.mappers.codice;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.codice.ContractExecutionRequirement;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.EvaluationCriteria;
import local.jarios.entity.codice.FinancialGuarantee;
import local.jarios.entity.codice.ProcurementProjectLot;
import local.jarios.entity.codice.TendererQualificationRequest;
import local.jarios.entity.codice.TenderingTerms;
import local.jarios.enums.TipoSolvencia;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import org.dgpe.codice.common.caclib.DocumentReferenceType;
import org.dgpe.codice.common.caclib.InvoicingTermsType;
import org.dgpe.codice.common.caclib.SubcontractTermsType;
import org.dgpe.codice.common.caclib.TendererQualificationRequestType;
import org.dgpe.codice.common.caclib.TenderingTermsType;
import org.dgpe.codice.common.cbclib.ElectronicInvoicingIndicatorType;
import org.dgpe.codice.common.cbclib.EorderingIndicatorType;
import org.dgpe.codice.common.cbclib.EpaymentMeansIndicatorType;
import org.dgpe.codice.common.cbclib.IDType;
import org.dgpe.codice.common.cbclib.ProcurementNationalLegislationCodeType;
import org.oasis.ubl.common.udt.IndicatorType;
import un.unece.uncefact.data.specification.corecomponenttypeschemamodule._2.QuantityType;

public final class MapperTenderingTerms {

  private MapperTenderingTerms() {}

  public static TenderingTerms getTenderingTermsFromType(
      ContractFolderStatus contractFolderStatus,
      ProcurementProjectLot procurementProjectLot,
      TenderingTermsType tenderingTermsType) {

    TenderingTerms tenderingTerms = new TenderingTerms();
    tenderingTerms.setContractFolderStatus(contractFolderStatus);
    tenderingTerms.setProcurementProjectLot(procurementProjectLot);

    if (tenderingTermsType == null) {
      tenderingTerms.setAwardingCriteriaList(new ArrayList<>());
      tenderingTerms.setListContractExecutionRequirement(new ArrayList<>());
      tenderingTerms.setListFinancialGuarantee(new ArrayList<>());
      tenderingTerms.setTendererQualificationRequest(null);
      tenderingTerms.setSubcontractTermsRate(null);
      tenderingTerms.setSubcontractTermsDescription(null);

      return tenderingTerms;
    }

    // === Indicators / booleans (manteniendo "seteamos incluso null") ===
    tenderingTerms.setRequiredCurriculaIndicator(
        mapNullable(tenderingTermsType.getRequiredCurriculaIndicator(), IndicatorType::isValue));

    tenderingTerms.setVariantConstraintIndicator(
        mapNullable(tenderingTermsType.getVariantConstraintIndicator(), IndicatorType::isValue));

    tenderingTerms.setEorderingIndicator(
        mapNullable(tenderingTermsType.getEorderingIndicator(), EorderingIndicatorType::isValue));

    tenderingTerms.setEpaymentMeansIndicator(
        mapNullable(
            tenderingTermsType.getEpaymentMeansIndicator(), EpaymentMeansIndicatorType::isValue));

    tenderingTerms.setElectronicInvoicingIndicator(
        mapChainNullable(
            tenderingTermsType.getInvoicingTerms(),
            InvoicingTermsType::getElectronicInvoicingIndicator,
            ElectronicInvoicingIndicatorType::isValue));

    // === Strings (listas) con limit + normalización ===
    tenderingTerms.setPriceRevisionFormulaDescription(
        StringHelper.normalizeAndLimit(
            MapperStringFromList.getStringFromListPriceRevisionFormulaDescriptionType(
                tenderingTermsType.getPriceRevisionFormulaDescription()),
            TamanoCampos.TAMANO_500));

    tenderingTerms.setFundingProgramCode(
        StringHelper.normalizeAndLimit(
            MapperStringFromList.getStringFromListFundingProgramCodeType(
                tenderingTermsType.getFundingProgramCode()),
            TamanoCampos.TAMANO_500));

    tenderingTerms.setFundingProgram(
        StringHelper.normalizeAndLimit(
            MapperStringFromList.getStringFromListFundingProgramType(
                tenderingTermsType.getFundingProgram()),
            TamanoCampos.TAMANO_500));

    // === Codes / IDs ===
    tenderingTerms.setProcurementNationalLegislationCode(
        StringHelper.normalizeAndLimit(
            mapNullable(
                tenderingTermsType.getProcurementNationalLegislationCode(),
                ProcurementNationalLegislationCodeType::getValue),
            TamanoCampos.TAMANO_50));

    tenderingTerms.setProcurementLegislationDocumentReference(
        StringHelper.normalizeAndLimit(
            mapChainNullable(
                tenderingTermsType.getProcurementLegislationDocumentReference(),
                DocumentReferenceType::getID,
                IDType::getValue),
            TamanoCampos.TAMANO_50));

    // === Numbers ===
    tenderingTerms.setReceivedAppealQuantity(
        Optional.ofNullable(tenderingTermsType.getReceivedAppealQuantity())
            .map(qty -> qty.getValue() == null ? null : qty.getValue().doubleValue())
            .orElse(null));

    // === Awarding criteria ===
    tenderingTerms.setAwardingCriteriaList(
        Optional.ofNullable(tenderingTermsType.getAwardingTerms())
            .map(
                awardTerms ->
                    MapperAwardingCriteria.getListAwardingCriteria(
                        tenderingTerms, awardTerms.getAwardingCriteria()))
            .orElse(new ArrayList<>()));

    // === Contract execution requirement ===
    List<ContractExecutionRequirement> contractExecutionRequirementList =
        Optional.of(
                MapperContractExecutionRequirement.getListContractExecutionRequirement(
                    tenderingTerms, tenderingTermsType.getContractExecutionRequirement()))
            .orElse(new ArrayList<>());

    tenderingTerms.setListContractExecutionRequirement(contractExecutionRequirementList);

    // === Financial guarantee ===
    // Si MapperFinancialGuarantee puede devolver null, y quieres lista vacía:
    List<FinancialGuarantee> guaranteeList =
        Optional.of(
                MapperFinancialGuarantee.getListFinancialGuarantee(
                    tenderingTerms, tenderingTermsType.getRequiredFinancialGuarantee()))
            .orElse(new ArrayList<>());
    tenderingTerms.setListFinancialGuarantee(guaranteeList);

    applyAllowedSubcontractTerms(tenderingTerms, tenderingTermsType.getAllowedSubcontractTerms());

    tenderingTerms.setTendererQualificationRequest(
        getTendererQualificationRequest(
            tenderingTerms, tenderingTermsType.getTendererQualificationRequest()));

    return tenderingTerms;
  }

  private static void applyAllowedSubcontractTerms(
      TenderingTerms tenderingTerms,
      java.util.List<? extends org.dgpe.codice.common.caclib.SubcontractTermsType>
          allowedSubcontractTerms) {
    if (allowedSubcontractTerms == null || allowedSubcontractTerms.isEmpty()) {
      tenderingTerms.setSubcontractTermsRate(null);
      tenderingTerms.setSubcontractTermsDescription(null);
      return;
    }

    SubcontractTermsType firstSubcontractTerms = allowedSubcontractTerms.getFirst();

    tenderingTerms.setSubcontractTermsRate(
        Optional.ofNullable(firstSubcontractTerms.getRate())
            .map(rate -> rate.getValue() == null ? null : rate.getValue().doubleValue())
            .orElse(null));

    tenderingTerms.setSubcontractTermsDescription(
        StringHelper.normalizeAndLimit(
            MapperStringFromList.getStringFromListDescriptionType(
                firstSubcontractTerms.getDescription()),
            TamanoCampos.TAMANO_500));
  }

  private static TendererQualificationRequest getTendererQualificationRequest(
      TenderingTerms tenderingTerms,
      TendererQualificationRequestType tendererQualificationRequestType) {
    if (tendererQualificationRequestType == null) {
      return null;
    }

    TendererQualificationRequest tendererQualificationRequest = new TendererQualificationRequest();
    tendererQualificationRequest.setTenderingTerms(tenderingTerms);

    tendererQualificationRequest.setPersonalSituation(
        StringHelper.eliminarCaracteres(
            MapperStringFromList.getStringFromListPersonalSituationType(
                tendererQualificationRequestType.getPersonalSituation())));

    tendererQualificationRequest.setDescription(
        StringHelper.eliminarCaracteres(
            MapperStringFromList.getStringFromListDescriptionType(
                tendererQualificationRequestType.getDescription())));

    tendererQualificationRequest.setEmployeeQuantityDescription(
        StringHelper.eliminarCaracteres(
            MapperStringFromList.getStringFromListEmployeeQuantityDescriptionType(
                tendererQualificationRequestType.getEmployeeQuantityDescription())));

    tendererQualificationRequest.setEmployeeQuantity(
        Optional.ofNullable(tendererQualificationRequestType.getEmployeeQuantity())
            .map(QuantityType::getValue)
            .orElse(null));

    List<EvaluationCriteria> listFinancialEvaluationCriteria =
        MapperEvaluationCriteria.getListEvaluationCriteria(
            tendererQualificationRequest,
            tendererQualificationRequestType.getFinancialEvaluationCriteria(),
            TipoSolvencia.ECONOMICA);

    List<EvaluationCriteria> listTechnicalEvaluationCriteria =
        MapperEvaluationCriteria.getListEvaluationCriteria(
            tendererQualificationRequest,
            tendererQualificationRequestType.getTechnicalEvaluationCriteria(),
            TipoSolvencia.TECNICA);

    List<EvaluationCriteria> evaluationCriteriaList = new ArrayList<>();

    evaluationCriteriaList.addAll(listFinancialEvaluationCriteria);
    evaluationCriteriaList.addAll(listTechnicalEvaluationCriteria);

    tendererQualificationRequest.setEvaluationCriteria(evaluationCriteriaList);

    tendererQualificationRequest.setRequiredBusinessClassificationScheme(
        MapperClassificationScheme.getListClassificationScheme(
            tendererQualificationRequest,
            tendererQualificationRequestType.getRequiredBusinessClassificationScheme()));

    tendererQualificationRequest.setSpecificTendererRequirement(
        MapperTendererRequirement.getListTendererRequirement(
            tendererQualificationRequest,
            tendererQualificationRequestType.getSpecificTendererRequirement()));

    return tendererQualificationRequest;
  }

  // ---------- Helpers (pequeños, testables, sin duplicidad) ----------
  private static <T, R> R mapNullable(T value, Function<T, R> mapper) {
    return value == null ? null : mapper.apply(value);
  }

  @SafeVarargs
  private static <T> Object mapChainNullable(T start, Function<?, ?>... steps) {
    Object current = start;
    for (var step : steps) {
      if (current == null) return null;
      @SuppressWarnings("unchecked")
      var fn = (Function<Object, Object>) step;
      current = fn.apply(current);
    }
    return current;
  }

  private static <A, B, C> C mapChainNullable(A a, Function<A, B> ab, Function<B, C> bc) {
    if (a == null) return null;
    var b = ab.apply(a);
    return b == null ? null : bc.apply(b);
  }

  private static <A, B, C, D> D mapChainNullable(
      A a, Function<A, B> ab, Function<B, C> bc, Function<C, D> cd) {
    if (a == null) return null;
    var b = ab.apply(a);
    if (b == null) return null;
    var c = bc.apply(b);
    return c == null ? null : cd.apply(c);
  }
}
