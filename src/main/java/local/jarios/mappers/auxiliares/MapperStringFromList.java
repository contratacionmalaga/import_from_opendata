package local.jarios.mappers.auxiliares;

import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.AddressLineType;
import org.dgpe.codice.common.caclib.PartyNameType;
import org.dgpe.codice.common.cbclib.DescriptionType;
import org.dgpe.codice.common.cbclib.EmployeeQuantityDescriptionType;
import org.dgpe.codice.common.cbclib.FundingProgramCodeType;
import org.dgpe.codice.common.cbclib.FundingProgramType;
import org.dgpe.codice.common.cbclib.LimitationDescriptionType;
import org.dgpe.codice.common.cbclib.LotsCombinationContractingAuthorityRightsType;
import org.dgpe.codice.common.cbclib.NameType;
import org.dgpe.codice.common.cbclib.NoteType;
import org.dgpe.codice.common.cbclib.OptionsDescriptionType;
import org.dgpe.codice.common.cbclib.PersonalSituationType;
import org.dgpe.codice.common.cbclib.PriceRevisionFormulaDescriptionType;
import org.w3._2005.atom.LinkType;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: Juan Antonio Author: juan Date: 06/07/2024 Team: Juan Antonio
 */
@Slf4j
public final class MapperStringFromList {

  private MapperStringFromList() {
  }

  // Método genérico para concatenar valores extraídos por un mapper de cada elemento
  private static <T> String joinValues(List<T> list, Function<T, String> mapper) {
    // Asumo que la lista nunca es null
    // if (list == null) return "";
    return list.stream()
        .map(mapper)
        .filter(Objects::nonNull)
        .collect(Collectors.joining());
  }

  public static String getStringFromListObject(List<Object> listObject) {

    return listObject.stream()
        .map(Object::toString)
        .collect(Collectors.joining());
  }

  public static String getStringFromListDescriptionType(List<DescriptionType> list) {

    return list.isEmpty() ? "" : joinValues(list, DescriptionType::getValue);
  }

  public static String getStringFromListOptionsDescriptionType(List<OptionsDescriptionType> list) {

    return list.isEmpty() ? "" : joinValues(list, OptionsDescriptionType::getValue);
  }

  public static String getStringFromListLimitationDescriptionType(List<LimitationDescriptionType> list) {

    return list.isEmpty() ? "" : joinValues(list, LimitationDescriptionType::getValue);
  }

  public static String getStringFromListNameType(List<NameType> list) {

    return list.isEmpty() ? "" : joinValues(list, NameType::getValue);
  }

  public static String getStringFromListLinkType(List<LinkType> list) {

    return list.isEmpty() ? "" : joinValues(list, LinkType::getHref);
  }

  public static String getStringFromListAddressLineType(List<AddressLineType> list) {

    return list.isEmpty() ? "" :
        joinValues(list, AddressLineType ->
            AddressLineType.getLine() != null ? AddressLineType.getLine().getValue() : null);
  }

  public static String getStringFromListFundingProgramCodeType(List<FundingProgramCodeType> list) {

    return list.isEmpty() ? "" : joinValues(list, FundingProgramCodeType::getValue);
  }

  public static String getStringFromListNoteType(List<NoteType> list) {

    return list.isEmpty() ? "" : joinValues(list, NoteType::getValue);
  }

  public static String getStringFromListFundingProgramType(List<FundingProgramType> list) {

    return list.isEmpty() ? "" : joinValues(list, FundingProgramType::getValue);
  }

  public static String getStringFromListPartyNameType(List<PartyNameType> list) {

    return list.isEmpty() ? "" :
        joinValues(list, PartyNameType ->
            PartyNameType.getName() != null ? PartyNameType.getName().getValue() : null);
  }

  public static String getStringFromListPriceRevisionFormulaDescriptionType(List<PriceRevisionFormulaDescriptionType> list) {

    return list.isEmpty() ? "" : joinValues(list, PriceRevisionFormulaDescriptionType::getValue);
  }

  public static String getStringFromListLotsCombinationContractingAuthorityRightsType(List<LotsCombinationContractingAuthorityRightsType> list) {

    return list.isEmpty() ? "" : joinValues(list,
                                            LotsCombinationContractingAuthorityRightsType::getValue);
  }

  public static String getStringFromListPersonalSituationType(List<PersonalSituationType> list) {

    return list.isEmpty() ? "" : joinValues(list, PersonalSituationType::getValue);
  }

  public static String getStringFromListEmployeeQuantityDescriptionType(List<EmployeeQuantityDescriptionType> list) {

    return list.isEmpty() ? "" : joinValues(list, EmployeeQuantityDescriptionType::getValue);
  }
}
