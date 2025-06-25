package local.jarios.mappers;

import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.entity.placsp.Uuid;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.cbclib.UUIDType;

import java.util.List;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperUuid {

    private MapperUuid() { }

    public static List<Uuid> getListUuidFromType(
            ContractFolderStatus contractFolderStatus,
            List<UUIDType> listUuidType) {

        //
        return listUuidType.stream()
                .map(uuidType -> {
                    var uuid = new Uuid();
                    uuid.setContractFolderStatus(contractFolderStatus);
                    uuid.setUuid(uuidType.getValue());
                    uuid.setSchemeName(uuidType.getSchemeName());
                    log.debug(uuid.toString());
                    return uuid;
                })
                .toList();
    }
}
