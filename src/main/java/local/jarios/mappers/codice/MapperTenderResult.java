package local.jarios.mappers.codice;

import local.jarios.common.util.Constantes;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.TenderResult;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.GregorianCalendarHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.PartyIdentificationType;
import org.dgpe.codice.common.caclib.TenderResultType;
import org.dgpe.codice.common.cbclib.AbnormallyLowTendersIndicatorType;
import org.dgpe.codice.common.cbclib.CityNameType;
import org.dgpe.codice.common.cbclib.IDType;
import org.dgpe.codice.common.cbclib.IssueDateType;
import org.dgpe.codice.common.cbclib.PostalZoneType;
import org.dgpe.codice.common.cbclib.ResultCodeType;
import org.dgpe.codice.common.cbclib.SMEAwardedIndicatorType;
import org.dgpe.codice.common.cbclib.StartDateType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public final class MapperTenderResult {

  private MapperTenderResult() {
  }

  public static List<TenderResult> getListTenderResultFromType(
      ContractFolderStatus contractFolderStatus,
      List<TenderResultType> listTenderResultType) {

    Objects.requireNonNull(contractFolderStatus, "contractFolderStatus no puede ser null");
    if (listTenderResultType == null || listTenderResultType.isEmpty()) {
      return List.of();
    }

    return listTenderResultType.stream()
        .filter(Objects::nonNull)
        .map(tr -> getTenderResultFromType(contractFolderStatus, tr))
        .toList();
  }

  private static TenderResult getTenderResultFromType(
      ContractFolderStatus contractFolderStatus,
      TenderResultType tenderResultType) {

    TenderResult tenderResult = new TenderResult();
    tenderResult.setContractFolderStatus(contractFolderStatus);

    mapCamposBasicos(tenderResult, tenderResultType);
    mapWinningParty(tenderResult, tenderResultType);
    mapContract(tenderResult, tenderResultType);
    mapAwardedTenderedProject(tenderResult, tenderResultType);

    return tenderResult;
  }

  private static void mapCamposBasicos(TenderResult tr, TenderResultType src) {

    Optional.ofNullable(src.getResultCode())
        .map(ResultCodeType::getValue)
        .map(v -> ComunHelper.limitarRegistro(v, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(tr::setResultCode);

    Optional.ofNullable(src.getReceivedTenderQuantity())
        .map(q -> q.getValue().doubleValue())
        .ifPresent(tr::setReceivedTenderQuantity);

    tr.setDescriptionTenderResult(
        StringHelper.eliminarCaracteres(
            MapperStringFromList.getStringFromListDescriptionType(src.getDescription())
        )
    );

    Optional.ofNullable(src.getAwardDate())
        .map(d -> GregorianCalendarHelper.getDateFromXMLGregorianCalendar(d.getValue()))
        .ifPresent(tr::setAwardDate);

    Optional.ofNullable(src.getLowerTenderAmount())
        .map(a -> a.getValue().doubleValue())
        .ifPresent(tr::setLowerTenderAmountQuantity);

    Optional.ofNullable(src.getHigherTenderAmount())
        .map(a -> a.getValue().doubleValue())
        .ifPresent(tr::setHigherTenderAmountQuantity);

    Optional.ofNullable(src.getAbnormallyLowTendersIndicator())
        .map(AbnormallyLowTendersIndicatorType::isValue)
        .ifPresent(tr::setAbnormallyLowTendersIndicator);

    Optional.ofNullable(src.getSMEsReceivedTenderQuantity())
        .map(q -> q.getValue().doubleValue())
        .ifPresent(tr::setSMEsReceivedTenderQuantity);

    Optional.ofNullable(src.getSMEAwardedIndicator())
        .map(SMEAwardedIndicatorType::isValue)
        .ifPresent(tr::setSMEAwardedIndicator);

    Optional.ofNullable(src.getEUNationalsReceivedTenderQuantity())
        .map(q -> q.getValue().doubleValue())
        .ifPresent(tr::setEUNationalsReceivedTenderQuantity);

    Optional.ofNullable(src.getNonEUNationalsReceivedTenderQuantity())
        .map(q -> q.getValue().doubleValue())
        .ifPresent(tr::setNonEUNationalsReceivedTenderQuantity);

    Optional.ofNullable(src.getStartDate())
        .map(StartDateType::getValue)
        .map(GregorianCalendarHelper::getDateFromXMLGregorianCalendar)
        .ifPresent(tr::setStartDate);

    Optional.ofNullable(src.getAwardedOwnerNationalityCode())
        .map(c -> ComunHelper.limitarRegistro(
            c.getValue(),
            Constantes.TAMANO_MAXIMO_CAMPO_50)
        )
        .ifPresent(tr::setAwardedOwnerNationalityCode);
  }

  private static void mapWinningParty(TenderResult tr, TenderResultType src) {
    Optional.ofNullable(src.getWinningParty())
        .ifPresent(wp -> {
          mapWinningPartyName(tr, wp);
          mapWinningPartyPhysicalLocation(tr, wp);
          mapWinningPartyIdentification(tr, wp.getPartyIdentification());
        });
  }

  private static void mapWinningPartyName(
      TenderResult tr, org.dgpe.codice.common.caclib.PartyType wp
  ) {
    Optional.ofNullable(wp.getPartyName())
        .map(MapperStringFromList::getStringFromListPartyNameType)
        .map(StringHelper::eliminarCaracteres)
        .map(v -> ComunHelper.limitarRegistro(v, Constantes.TAMANO_MAXIMO_CAMPO_500))
        .ifPresent(tr::setPartyName);
  }

  private static void mapWinningPartyPhysicalLocation(
      TenderResult tr, org.dgpe.codice.common.caclib.PartyType wp
  ) {
    Optional.ofNullable(wp.getPhysicalLocation())
        .ifPresent(pl -> {

          Optional.ofNullable(pl.getCountrySubentityCode())
              .map(c -> ComunHelper.limitarRegistro(
                  c.getValue(),
                  Constantes.TAMANO_MAXIMO_CAMPO_50)
              )
              .ifPresent(tr::setCountrySubentityCode);

          Optional.ofNullable(pl.getCountrySubentity())
              .map(c -> ComunHelper.limitarRegistro(
                  c.getValue(),
                  Constantes.TAMANO_MAXIMO_CAMPO_500)
              )
              .ifPresent(tr::setCountrySubentity);

          Optional.ofNullable(pl.getAddress())
              .ifPresent(addr -> {
                Optional.ofNullable(addr.getCityName())
                    .map(CityNameType::getValue)
                    .map(v -> ComunHelper.limitarRegistro(
                        v,
                        Constantes.TAMANO_MAXIMO_CAMPO_500)
                    )
                    .ifPresent(tr::setCityName);

                Optional.ofNullable(addr.getPostalZone())
                    .map(PostalZoneType::getValue)
                    .map(v -> ComunHelper.limitarRegistro(
                        v,
                        Constantes.TAMANO_MAXIMO_CAMPO_500)
                    )
                    .ifPresent(tr::setPostalZone);
              });
        });
  }

  private static void mapWinningPartyIdentification(
      TenderResult tr, List<PartyIdentificationType> list
  ) {
    if (list == null || list.isEmpty()) return;

    for (PartyIdentificationType pit : list) {
      if (pit == null || pit.getID() == null) continue;

      IDType id = pit.getID();
      String value = id.getValue();
      String scheme = id.getSchemeName();

      if (value == null || value.isBlank() || scheme == null) continue;

      String limited = ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50);

      switch (scheme) {
        case Constantes.IDPLATAFORMA -> tr.setIdPlataforma(limited);
        case Constantes.NIF -> tr.setNif(limited);
        case Constantes.OTROS -> {
          tr.setOtros(limited);
          tr.setNif(limited); // si lo necesitas
        }
        default -> {
          // opcional: log.debug("SchemeName no reconocido: {}", scheme);
        }
      }
    }
  }

  private static void mapContract(TenderResult tr, TenderResultType src) {
    Optional.ofNullable(src.getContract())
        .ifPresent(c -> {
          Optional.ofNullable(c.getID())
              .map(IDType::getValue)
              .map(v -> ComunHelper.limitarRegistro(v, Constantes.TAMANO_MAXIMO_CAMPO_50))
              .ifPresent(tr::setIdContract);

          Optional.ofNullable(c.getIssueDate())
              .map(IssueDateType::getValue)
              .map(GregorianCalendarHelper::getDateFromXMLGregorianCalendar)
              .ifPresent(tr::setIssueDate);
        });
  }

  private static void mapAwardedTenderedProject(TenderResult tr, TenderResultType src) {
    Optional.ofNullable(src.getAwardedTenderedProject())
        .ifPresent(atp -> {

          Optional.ofNullable(atp.getProcurementProjectLotID())
              .map(id -> ComunHelper.limitarRegistro(
                  id.getValue(),
                  Constantes.TAMANO_MAXIMO_CAMPO_50)
              )
              .ifPresent(tr::setProcurementProjectLotId);

          Optional.ofNullable(atp.getLegalMonetaryTotal())
              .ifPresent(lmt -> {
                Optional.ofNullable(lmt.getPayableAmount())
                    .map(a -> a.getValue().doubleValue())
                    .ifPresent(tr::setPayableAmount);

                Optional.ofNullable(lmt.getTaxExclusiveAmount())
                    .map(a -> a.getValue().doubleValue())
                    .ifPresent(tr::setTaxExclusiveAmount);

                Optional.ofNullable(lmt.getTaxInclusiveAmount())
                    .map(a -> a.getValue().doubleValue())
                    .ifPresent(tr::setTaxInclusiveAmount);
              });
        });
  }
}


