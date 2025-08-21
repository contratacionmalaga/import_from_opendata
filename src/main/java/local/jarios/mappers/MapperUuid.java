package local.jarios.mappers;

import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.entity.placsp.Uuid;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.cbclib.UUIDType;

import java.util.List;

/**
 * Clase utilitaria para mapear una lista de objetos {@link UUIDType}
 * a una lista de entidades {@link Uuid}.
 * <p>
 * Esta clase proporciona un método estático que crea una lista de objetos
 * {@link Uuid} asociados a una entidad {@link ContractFolderStatus}.
 * </p>
 * <p>
 * Cada objeto {@link UUIDType} se transforma en un objeto {@link Uuid} copiando
 * su valor y el esquema de nombre.
 * </p>
 * <p>
 * La clase es estática y no instanciable.
 * </p>
 *
 * <p><b>Autor:</b> Juan Antonio</p>
 * <p><b>Fecha:</b> 11/04/2024</p>
 * <p><b>Equipo:</b> Juan Antonio</p>
 */
@Slf4j
public final class MapperUuid {

  private MapperUuid() {
  }

  /**
   * Convierte una lista de {@link UUIDType} en una lista de {@link Uuid},
   * asociando cada instancia al {@link ContractFolderStatus} proporcionado.
   *
   * @param contractFolderStatus entidad padre a la que se asocian los UUIDs.
   * @param listUuidType         lista de objetos {@link UUIDType} a convertir.
   * @return lista de objetos {@link Uuid} generados.
   */
  public static List<Uuid> getListUuidFromType(
      ContractFolderStatus contractFolderStatus,
      List<UUIDType> listUuidType) {

    return listUuidType.stream()
        .map(uuidType -> {
          var uuid = new Uuid();
          uuid.setContractFolderStatus(contractFolderStatus);
          uuid.setUuid(uuidType.getValue());
          uuid.setSchemeName(uuidType.getSchemeName());
          return uuid;
        })
        .toList();
  }
}
