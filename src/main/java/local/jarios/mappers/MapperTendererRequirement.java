package local.jarios.mappers;

import local.jarios.entity.placsp.TendererQualificationRequest;
import local.jarios.entity.placsp.TendererRequirement;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.TendererRequirementType;

import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperTendererRequirement {

    private MapperTendererRequirement() { }

    public static List<TendererRequirement> getListTendererRequirement (
            TendererQualificationRequest tendererQualificationRequest,
            List<TendererRequirementType> listTendererRequirementType) {

        //
        return Optional.ofNullable(listTendererRequirementType)
                .orElseGet(List::of)
                .stream()
                .map(tendererRequirementType -> getTendererRequirement(tendererQualificationRequest, tendererRequirementType))
                .toList();
    }

    private static TendererRequirement getTendererRequirement (
            TendererQualificationRequest tendererQualificationRequest,
            TendererRequirementType tendererRequirementType) {

        //
        TendererRequirement tendererRequirement = new TendererRequirement();
        tendererRequirement.setTendererQualificationRequest(tendererQualificationRequest);

        //
        tendererRequirement.setDescription(
                StringHelper.eliminarCaracteres(
                        MapperStringFromList.getStringFromListDescriptionType(
                                tendererRequirementType.getDescription())));

        Optional.ofNullable(tendererRequirementType.getRequirementTypeCode())
                .map(code -> ComunHelper.limitarRegistro(
                        code.getValue(),
                        Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(tendererRequirement::setRequirementTypeCode);

        //
        return tendererRequirement;
    }
}
