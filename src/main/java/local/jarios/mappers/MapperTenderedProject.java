package local.jarios.mappers;

import local.jarios.entity.placsp.TenderResult;
import local.jarios.entity.placsp.TenderedProject;
import local.jarios.helpers.ComunHelper;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.TenderedProjectType;

import java.util.Optional;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperTenderedProject {

    private MapperTenderedProject() { }

    public static TenderedProject getTenderedProjectFromType(
            TenderResult tenderResult,
            TenderedProjectType tenderedProjectType) {

        //
        TenderedProject tenderedProject = new TenderedProject();
        tenderedProject.setTenderResult(tenderResult);

        Optional.ofNullable(tenderedProjectType.getProcurementProjectLotID())
                .map(id -> ComunHelper.limitarRegistro(id.getValue(), Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(tenderedProject::setProcurementProjectLotId);

        Optional.ofNullable(tenderedProjectType.getLegalMonetaryTotal())
                .ifPresent(legalMonetaryTotal -> tenderedProject.setLegalMonetaryTotal(
                        MapperLegalMonetaryTotal.getLegalMonetaryTotalFromType(
                                tenderedProject,
                                null,
                                null,
                                legalMonetaryTotal)));

        //
        return tenderedProject;
    }
}
