package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record ContractFolderStatusAdapter(boolean imprimirHijos)
        implements JsonSerializer<ContractFolderStatus> {

    @Override
    public JsonElement serialize(
            ContractFolderStatus contractFolderStatus,
            Type typeOfSrc,
            JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("ContractFolderId", contractFolderStatus.getContractFolderId());
        jsonObject.addProperty("ContractFolderStatusCode", contractFolderStatus.getContractFolderStatusCode());

        if (imprimirHijos) {

            JsonSerializationHelper.addIfNotEmpty(jsonObject, "Uuid", contractFolderStatus.getListUuid(), context);
            JsonSerializationHelper.addIfNotNull(jsonObject, "LocatedContractingParty", contractFolderStatus.getLocatedContractingParty(), context);
            JsonSerializationHelper.addIfNotNull(jsonObject, "ProcurementProject", contractFolderStatus.getProcurementProject(), context);
            JsonSerializationHelper.addIfNotEmpty(jsonObject, "ProcurementProjectLot", contractFolderStatus.getListProcurementProjectLot(), context);
            JsonSerializationHelper.addIfNotNull(jsonObject, "TenderingTerms", contractFolderStatus.getTenderingTerms(), context);
            JsonSerializationHelper.addIfNotNull(jsonObject, "TenderingProcess", contractFolderStatus.getTenderingProcess(), context);
            JsonSerializationHelper.addIfNotNull(jsonObject, "LegalDocumentReference", contractFolderStatus.getLegalDocumentReference(), context);
            JsonSerializationHelper.addIfNotNull(jsonObject, "TechnicalDocumentReference", contractFolderStatus.getTechnicalDocumentReference(), context);
            JsonSerializationHelper.addIfNotEmpty(jsonObject, "AdditionalDocumentReference", contractFolderStatus.getListAdditionalDocumentReference(), context);
            JsonSerializationHelper.addIfNotEmpty(jsonObject, "NoticeInfo", contractFolderStatus.getListNoticeInfo(), context);
            JsonSerializationHelper.addIfNotEmpty(jsonObject, "GeneralDocument", contractFolderStatus.getListGeneralDocument(), context);
            JsonSerializationHelper.addIfNotEmpty(jsonObject, "TenderResult", contractFolderStatus.getListTenderResult(), context);
            JsonSerializationHelper.addIfNotEmpty(jsonObject, "ContractModification", contractFolderStatus.getListContractModification(), context);

        }

        return jsonObject;
    }
}
