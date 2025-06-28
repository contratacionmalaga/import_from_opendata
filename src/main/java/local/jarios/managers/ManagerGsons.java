package local.jarios.managers;

import com.google.gson.GsonBuilder;
import local.jarios.adapters.*;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.placsp.Measure;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.placsp.*;

public final class ManagerGsons {

    private ManagerGsons() {}

    /**
     *
     * @param objeto Objeto que se imprimirá
     * @return Devuelve un String con los datos
     */
    public static String objectToJsonPretty(Object objeto, boolean imprimirHijos) {

        return getJson (objeto, imprimirHijos);
    }

    /**
     *
     * @param objeto Objeto que se imprimirá
     * @param imprimirHijos Indica si se imprimirán los hijos asociados al objeto
     * @return Devuelve un String con los datos
     */
    private static String getJson (Object objeto, boolean imprimirHijos) {

        var gsonBuilder = new GsonBuilder();

        gsonBuilder.setPrettyPrinting().disableHtmlEscaping();

        // AdditionalPublicationDocumentReferenceAdapter
        gsonBuilder.registerTypeAdapter(AdditionalPublicationDocumentReference.class,
                new AdditionalPublicationDocumentReferenceAdapter(imprimirHijos));

        // AdditionalPublicationRequest
        gsonBuilder.registerTypeAdapter(AdditionalPublicationRequest.class,
                new AdditionalPublicationRequestAdapter());

        // AdditionalPublicationStatus
        gsonBuilder.registerTypeAdapter(AdditionalPublicationStatus.class,
                new AdditionalPublicationStatusAdapter(imprimirHijos));

        // Address
        gsonBuilder.registerTypeAdapter(Address.class,
                new AddressAdapter(imprimirHijos));

        // AdditionalDocumentReference
        gsonBuilder.registerTypeAdapter(AdditionalDocumentReference.class,
                new AdditionalDocumentReferenceAdapter(imprimirHijos));

        // AgentParty
        gsonBuilder.registerTypeAdapter(AgentParty.class,
                new AgentPartyAdapter(imprimirHijos));

        // Attachment
        gsonBuilder.registerTypeAdapter(Attachment.class,
                new AttachmentAdapter(imprimirHijos));

        // AuctionTerms
        gsonBuilder.registerTypeAdapter(AuctionTerms.class,
                new AuctionTermsAdapter());

        // AwardedTenderedProject
        gsonBuilder.registerTypeAdapter(TenderedProject.class,
                new AwardedTenderedProjectAdapter(imprimirHijos));

        // AwardingCriteria
        gsonBuilder.registerTypeAdapter(AwardingCriteria.class,
                new AwardingCriteriaAdapter());

        // AwardingTerms
        gsonBuilder.registerTypeAdapter(AwardingTerms.class,
                new AwardingTermsAdapter(imprimirHijos));

        // BudgetAmount
        gsonBuilder.registerTypeAdapter(BudgetAmount.class,
                new BudgetAmountAdapter());

        // ClassificationScheme
        gsonBuilder.registerTypeAdapter(ClassificationScheme.class,
                new ClassificationSchemeAdapter(imprimirHijos));

        // ClassificationCategory
        gsonBuilder.registerTypeAdapter(ClassificationCategory.class,
                new ClassificationCategoryAdapter());

        // CommodityClassification
        gsonBuilder.registerTypeAdapter(CommodityClassification.class,
                new CommodityClassificationAdapter());

        // Configuracion
        gsonBuilder.registerTypeAdapter(Configuracion.class,
                new ConfiguracionAdapter());

        // Contact
        gsonBuilder.registerTypeAdapter(Contact.class,
                new ContactAdapter());

        // Contract
        gsonBuilder.registerTypeAdapter(Contract.class,
                new ContractAdapter());

        // ContractExtension
        gsonBuilder.registerTypeAdapter(ContractExtension.class,
                new ContractExtensionAdapter(imprimirHijos));

        // ContractExecutionRequirement
        gsonBuilder.registerTypeAdapter(ContractExecutionRequirement.class,
                new ContractExecutionRequirementAdapter());

        // ContractFolderStatus
        gsonBuilder.registerTypeAdapter(ContractFolderStatus.class,
                new ContractFolderStatusAdapter(imprimirHijos));

        // ContractModification
        gsonBuilder.registerTypeAdapter(ContractModification.class,
                new ContractModificationAdapter(imprimirHijos));

        // Country
        gsonBuilder.registerTypeAdapter(Country.class,
                new CountryAdapter());

        // DocumentReference
        gsonBuilder.registerTypeAdapter(DocumentReference.class,
                new DocumentReferenceAdapter(imprimirHijos));

        // DurationMeasure
        gsonBuilder.registerTypeAdapter(Measure.class,
                new MeasureAdapter());

        // EconomicOperatorShortList
        gsonBuilder.registerTypeAdapter(EconomicOperatorShortList.class,
                new EconomicOperatorShortListAdapter());

        // Entry
        gsonBuilder.registerTypeAdapter(Entry.class,
                new EntryAdapter(imprimirHijos));

        // Estadistica
        gsonBuilder.registerTypeAdapter(Estadistica.class,
                new EstadisticaAdapter());

        // EvaluationCriteria
        gsonBuilder.registerTypeAdapter(EvaluationCriteria.class,
                new EvaluationCriteriaAdapter());

        // ExternalReference
        gsonBuilder.registerTypeAdapter(ExternalReference.class,
                new ExternalReferenceAdapter());

        // Feed
        gsonBuilder.registerTypeAdapter(Feed.class,
                new FeedAdapter(imprimirHijos));

        // FinancialGuarantee
        gsonBuilder.registerTypeAdapter(FinancialGuarantee.class,
                new FinancialGuaranteeAdapter());

        // GeneralDocument
        gsonBuilder.registerTypeAdapter(GeneralDocument.class,
                new GeneralDocumentAdapter());

        // GeneralDocumentDocumentReference
        gsonBuilder.registerTypeAdapter(GeneralDocumentDocumentReference.class,
                new GeneralDocumentDocumentReferenceAdapter());

        // LegalDocumentReference
        gsonBuilder.registerTypeAdapter(LegalDocumentReference.class,
                new LegalDocumentReferenceAdapter(imprimirHijos));

        // LegalMonetaryTotal
        gsonBuilder.registerTypeAdapter(LegalMonetaryTotal.class,
                new LegalMonetaryTotalAdapter());

        // LocatedContractingParty
        gsonBuilder.registerTypeAdapter(LocatedContractingParty.class,
                new LocatedContractingPartyAdapter(imprimirHijos));

        // Location
        gsonBuilder.registerTypeAdapter(Location.class,
                new LocationAdapter(imprimirHijos));

        // Log
        gsonBuilder.registerTypeAdapter(Log.class,
                new LogAdapter(imprimirHijos));

        // NoticeInfo
        gsonBuilder.registerTypeAdapter(NoticeInfo.class,
                new NoticeInfoAdapter(imprimirHijos));

        // Party
        gsonBuilder.registerTypeAdapter(Party.class,
                new PartyAdapter(imprimirHijos));

        // PartyIdentification
        gsonBuilder.registerTypeAdapter(PartyIdentification.class,
                new PartyIdentificationAdapter());

        // Period
        gsonBuilder.registerTypeAdapter(Period.class,
                new PeriodAdapter());

        // ProcessJustification
        gsonBuilder.registerTypeAdapter(ProcessJustification.class,
                new ProcessJustificationAdapter());

        // ProcurementProject
        gsonBuilder.registerTypeAdapter(ProcurementProject.class,
                new ProcurementProjectAdapter(imprimirHijos));

        // ProcurementProjectLot
        gsonBuilder.registerTypeAdapter(ProcurementProjectLot.class,
                new ProcurementProjectLotAdapter(imprimirHijos));

        // SubcontractTerms
        gsonBuilder.registerTypeAdapter(SubcontractTerms.class,
                new SubcontractTermsAdapter());

        // TechnicalDocumentReference
        gsonBuilder.registerTypeAdapter(TechnicalDocumentReference.class,
                new TechnicalDocumentReferenceAdapter(imprimirHijos));

        // TenderRecipientParty
        gsonBuilder.registerTypeAdapter(TenderRecipientParty.class,
                new TenderRecipientPartyAdapter());

        // TendererQualificationRequest
        gsonBuilder.registerTypeAdapter(TendererQualificationRequest.class,
                new TendererQualificationRequestAdapter(imprimirHijos));

        // TendererRequirement
        gsonBuilder.registerTypeAdapter(TendererRequirement.class,
                new TendererRequirementAdapter());

        // TenderingProcess
        gsonBuilder.registerTypeAdapter(TenderingProcess.class,
                new TenderingProcessAdapter(imprimirHijos));

        // TenderingTerms
        gsonBuilder.registerTypeAdapter(TenderingTerms.class,
                new TenderingTermsAdapter(imprimirHijos));

        // TenderResult
        gsonBuilder.registerTypeAdapter(TenderResult.class,
                new TenderResultAdapter(imprimirHijos));

        // Uuid
        gsonBuilder.registerTypeAdapter(Uuid.class,
                new UuidAdapter());

        // WinningParty
        gsonBuilder.registerTypeAdapter(WinningParty.class,
                new WinningPartyAdapter(imprimirHijos));

        //
        var gson = gsonBuilder.create();

        //
        return gson.toJson(objeto);
    }
}
