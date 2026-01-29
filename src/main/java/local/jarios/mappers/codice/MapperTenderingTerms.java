package local.jarios.mappers.codice;

import local.jarios.codice.TendererQualificationRequest;
import local.jarios.codice.TendererQualificationRequestMapper;
import local.jarios.common.util.Constantes;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.ProcurementProjectLot;
import local.jarios.entity.codice.TenderingTerms;
import local.jarios.helpers.ComunHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import org.dgpe.codice.common.caclib.DocumentReferenceType;
import org.dgpe.codice.common.caclib.InvoicingTermsType;
import org.dgpe.codice.common.caclib.TenderingTermsType;
import org.dgpe.codice.common.cbclib.ElectronicInvoicingIndicatorType;
import org.dgpe.codice.common.cbclib.EorderingIndicatorType;
import org.dgpe.codice.common.cbclib.EpaymentMeansIndicatorType;
import org.dgpe.codice.common.cbclib.IDType;
import org.dgpe.codice.common.cbclib.ProcurementNationalLegislationCodeType;
import org.oasis.ubl.common.udt.IndicatorType;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class MapperTenderingTerms {

  private MapperTenderingTerms() {
  }

  public static TenderingTerms getTenderingTermsFromType(
      ContractFolderStatus contractFolderStatus,
      ProcurementProjectLot procurementProjectLot,
      TenderingTermsType tenderingTermsType
  ) {

    var tenderingTerms = new TenderingTerms();
    tenderingTerms.setContractFolderStatus(contractFolderStatus);
    tenderingTerms.setProcurementProjectLot(procurementProjectLot);

    if (tenderingTermsType == null) {
      // Valores por defecto coherentes
      tenderingTerms.setAwardingCriteriaList(List.of());
      tenderingTerms.setListFinancialGuarantee(List.of()); // ajusta si tu mapper devuelve null
      applyTendererQualificationRequest(tenderingTerms, null);
      return tenderingTerms;
    }

    // === Indicators / booleans (manteniendo "seteamos incluso null") ===
    tenderingTerms.setRequiredCurriculaIndicator(
        mapNullable(tenderingTermsType.getRequiredCurriculaIndicator(), IndicatorType::isValue)
    );

    tenderingTerms.setVariantConstraintIndicator(
        mapNullable(tenderingTermsType.getVariantConstraintIndicator(), IndicatorType::isValue)
    );

    tenderingTerms.setEorderingIndicator(
        mapNullable(tenderingTermsType.getEorderingIndicator(), EorderingIndicatorType::isValue)
    );

    tenderingTerms.setEpaymentMeansIndicator(
        mapNullable(tenderingTermsType.getEpaymentMeansIndicator(),
                    EpaymentMeansIndicatorType::isValue)
    );

    tenderingTerms.setElectronicInvoicingIndicator(
        mapChainNullable(
            tenderingTermsType.getInvoicingTerms(),
            InvoicingTermsType::getElectronicInvoicingIndicator,
            ElectronicInvoicingIndicatorType::isValue
        )
    );

    // === Strings (listas) con limit + normalización ===
    tenderingTerms.setPriceRevisionFormulaDescription(
        limitedString(
            MapperStringFromList.getStringFromListPriceRevisionFormulaDescriptionType(
                tenderingTermsType.getPriceRevisionFormulaDescription()
            ),
            Constantes.TAMANO_MAXIMO_CAMPO_500
        )
    );

    tenderingTerms.setFundingProgramCode(
        limitedString(
            MapperStringFromList.getStringFromListFundingProgramCodeType(
                tenderingTermsType.getFundingProgramCode()
            ),
            Constantes.TAMANO_MAXIMO_CAMPO_500
        )
    );

    tenderingTerms.setFundingProgram(
        limitedString(
            MapperStringFromList.getStringFromListFundingProgramType(
                tenderingTermsType.getFundingProgram()
            ),
            Constantes.TAMANO_MAXIMO_CAMPO_500
        )
    );

    // === Codes / IDs ===
    tenderingTerms.setProcurementNationalLegislationCode(
        limitedString(
            mapNullable(tenderingTermsType.getProcurementNationalLegislationCode(),
                        ProcurementNationalLegislationCodeType::getValue),
            Constantes.TAMANO_MAXIMO_CAMPO_50
        )
    );

    tenderingTerms.setProcurementLegislationDocumentReference(
        limitedString(
            mapChainNullable(
                tenderingTermsType.getProcurementLegislationDocumentReference(),
                DocumentReferenceType::getID,
                IDType::getValue
            ),
            Constantes.TAMANO_MAXIMO_CAMPO_50
        )
    );

    // === Numbers ===
    tenderingTerms.setReceivedAppealQuantity(
        Optional.ofNullable(tenderingTermsType.getReceivedAppealQuantity())
            .map(qty -> qty.getValue() == null ? null : qty.getValue().doubleValue())
            .orElse(null)
    );

    // === Awarding criteria ===
    tenderingTerms.setAwardingCriteriaList(
        Optional.ofNullable(tenderingTermsType.getAwardingTerms())
            .map(awardTerms -> MapperAwardingCriteria.getListAwardingCriteria(
                tenderingTerms,
                awardTerms.getAwardingCriteria()
            ))
            .orElse(List.of()) // si necesitas null: cambia a .orElse(null)
    );

    // === Financial guarantee ===
    // Si MapperFinancialGuarantee puede devolver null, y quieres lista vacía:
    var guarantees = MapperFinancialGuarantee.getListFinancialGuarantee(
        tenderingTerms,
        tenderingTermsType.getRequiredFinancialGuarantee()
    );
    tenderingTerms.setListFinancialGuarantee(guarantees == null ? List.of() : guarantees);

    // === TendererQualificationRequest ===
    var tqr = Optional.ofNullable(tenderingTermsType.getTendererQualificationRequest())
        .map(TendererQualificationRequestMapper::getTendererQualificationRequest)
        .orElse(null);

    applyTendererQualificationRequest(tenderingTerms, tqr);

    return tenderingTerms;
  }

  private static void applyTendererQualificationRequest(TenderingTerms tenderingTerms, TendererQualificationRequest req) {
    if (req == null) {
      tenderingTerms.setPersonalSituation(null);
      tenderingTerms.setDescription(null);
      tenderingTerms.setEmployeeQuantity(null);
      tenderingTerms.setEmployeeQuantityDescription(null);
      return;
    }

    tenderingTerms.setPersonalSituation(req.getPersonalSituation());
    tenderingTerms.setDescription(req.getDescription());
    tenderingTerms.setEmployeeQuantity(req.getEmployeeQuantity());
    tenderingTerms.setEmployeeQuantityDescription(req.getEmployeeQuantityDescription());
  }

  // ---------- Helpers (pequeños, testables, sin duplicidad) ----------

  private static String limitedString(String value, int max) {
    if (value == null) return null;
    var trimmed = value.trim();
    if (trimmed.isBlank()) return null;
    return ComunHelper.limitarRegistro(trimmed, max);
  }

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

  private static <A, B, C, D> D mapChainNullable(A a, Function<A, B> ab, Function<B, C> bc, Function<C, D> cd) {
    if (a == null) return null;
    var b = ab.apply(a);
    if (b == null) return null;
    var c = bc.apply(b);
    return c == null ? null : cd.apply(c);
  }
}
