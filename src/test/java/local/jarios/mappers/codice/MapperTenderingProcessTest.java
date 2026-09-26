package local.jarios.mappers.codice;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import javax.xml.datatype.DatatypeFactory;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.codice.TenderingProcess;
import org.dgpe.codice.common.caclib.PeriodType;
import org.dgpe.codice.common.caclib.TenderingProcessType;
import org.dgpe.codice.common.cbclib.EndDateType;
import org.dgpe.codice.common.cbclib.EndTimeType;
import org.dgpe.codice.common.cbclib.OriginalContractingSystemDPSCategoryIDType;
import org.dgpe.codice.common.cbclib.OriginalContractingSystemDescriptionType;
import org.dgpe.codice.common.cbclib.OriginalContractingSystemIDType;
import org.dgpe.codice.common.cbclib.OriginalContractingSystemLotDescriptionType;
import org.dgpe.codice.common.cbclib.OriginalContractingSystemLotIDType;
import org.junit.jupiter.api.Test;

class MapperTenderingProcessTest {

  @Test
  void maps_each_deadline_from_its_own_atom_period() throws Exception {
    TenderingProcessType source = new TenderingProcessType();
    source.setDocumentAvailabilityPeriod(period("2026-10-01", "09:15:00"));
    source.setTenderSubmissionDeadlinePeriod(period("2026-10-15", "14:30:00"));
    source.setParticipationRequestReceptionPeriod(period("2026-09-28", "18:45:00"));

    TenderingProcess result =
        MapperTenderingProcess.getTenderingProcessFromType(null, null, source);

    assertThat(result.getDocumentAvailabilityPeriod())
        .isEqualTo(LocalDateTime.of(2026, 10, 1, 9, 15));
    assertThat(result.getTenderSubmissionDeadlinePeriod())
        .isEqualTo(LocalDateTime.of(2026, 10, 15, 14, 30));
    assertThat(result.getParticipationRequestReceptionPeriod())
        .isEqualTo(LocalDateTime.of(2026, 9, 28, 18, 45));
  }

  @Test
  void maps_original_contracting_system_and_lot_fields_without_using_dps_categories() {
    TenderingProcessType source = new TenderingProcessType();
    source.setOriginalContractingSystemID(identifier("SYSTEM-42"));
    source.setOriginalContractingSystemLotID(lotIdentifier("LOT-7"));
    source.getOriginalContractingSystemDescription().add(description("Sistema original"));
    source.getOriginalContractingSystemDescription().add(description("Segundo idioma"));
    source.getOriginalContractingSystemLotDescription().add(lotDescription("Lote principal"));
    source.getOriginalContractingSystemLotDescription().add(lotDescription("Detalle de lote"));
    source.getOriginalContractingSystemDPSCategoryID().add(dpsCategory("DPS-99"));

    TenderingProcess result =
        MapperTenderingProcess.getTenderingProcessFromType(null, null, source);

    assertThat(result.getOriginalContractingSystemId()).isEqualTo("SYSTEM-42");
    assertThat(result.getOriginalContractingSystemDescription())
        .isEqualTo("Sistema original\nSegundo idioma");
    assertThat(result.getOriginalContractingSystemLotId()).isEqualTo("LOT-7");
    assertThat(result.getOriginalContractingSystemLotDescription())
        .isEqualTo("Lote principal\nDetalle de lote");
  }

  @Test
  void limits_original_identifiers_to_the_database_capacity() {
    TenderingProcessType source = new TenderingProcessType();
    String tooLongIdentifier = "x".repeat(TamanoCampos.TAMANO_50 + 1);
    source.setOriginalContractingSystemID(identifier(tooLongIdentifier));
    source.setOriginalContractingSystemLotID(lotIdentifier(tooLongIdentifier));

    TenderingProcess result =
        MapperTenderingProcess.getTenderingProcessFromType(null, null, source);

    assertThat(result.getOriginalContractingSystemId()).hasSize(TamanoCampos.TAMANO_50);
    assertThat(result.getOriginalContractingSystemLotId()).hasSize(TamanoCampos.TAMANO_50);
  }

  private static OriginalContractingSystemIDType identifier(String value) {
    OriginalContractingSystemIDType type = new OriginalContractingSystemIDType();
    type.setValue(value);
    return type;
  }

  private static PeriodType period(String date, String time) throws Exception {
    PeriodType period = new PeriodType();
    EndDateType endDate = new EndDateType();
    endDate.setValue(DatatypeFactory.newInstance().newXMLGregorianCalendar(date));
    period.setEndDate(endDate);
    EndTimeType endTime = new EndTimeType();
    endTime.setValue(DatatypeFactory.newInstance().newXMLGregorianCalendar(time));
    period.setEndTime(endTime);
    return period;
  }

  private static OriginalContractingSystemLotIDType lotIdentifier(String value) {
    OriginalContractingSystemLotIDType type = new OriginalContractingSystemLotIDType();
    type.setValue(value);
    return type;
  }

  private static OriginalContractingSystemDescriptionType description(String value) {
    OriginalContractingSystemDescriptionType type = new OriginalContractingSystemDescriptionType();
    type.setValue(value);
    return type;
  }

  private static OriginalContractingSystemLotDescriptionType lotDescription(String value) {
    OriginalContractingSystemLotDescriptionType type =
        new OriginalContractingSystemLotDescriptionType();
    type.setValue(value);
    return type;
  }

  private static OriginalContractingSystemDPSCategoryIDType dpsCategory(String value) {
    OriginalContractingSystemDPSCategoryIDType type =
        new OriginalContractingSystemDPSCategoryIDType();
    type.setValue(value);
    return type;
  }
}
