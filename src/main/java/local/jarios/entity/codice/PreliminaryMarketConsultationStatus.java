package local.jarios.entity.codice;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.atom.Entry;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

/**
 * Description: Importaciones de Ficheros Excel desde Internet Author: Juan Antonio Date: 04/06/2024
 * Team: Juan Antonio
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "preliminary_market_consultation_status")
public class PreliminaryMarketConsultationStatus {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  /** Estado de la consulta 6.1 */
  @Column(name = "preliminary_market_consultation_id", length = TamanoCampos.TAMANO_50)
  private String preliminaryMarketConsultationID;

  /** Número de expediente de la consulta 6.2 */
  @Column(name = "preliminary_market_consultation_status_code", length = TamanoCampos.TAMANO_50)
  private String preliminaryMarketConsultationStatusCode;

  /** Objeto de la consulta 6.3 */
  @Column(name = "consultation_name", nullable = false, columnDefinition = "TEXT")
  private String consultationName;

  /** Tipo de la consulta 6.4 */
  @Column(name = "condition_type_code", nullable = false, columnDefinition = "TEXT")
  private String conditionTypeCode;

  /** Condiciones o términos de envío de la consulta 6.6 */
  @Column(name = "conditions_text", columnDefinition = "TEXT")
  private String conditionsText;

  /** Participantes de la consulta 6.7 */
  @Column(name = "party_selection_reason_text", columnDefinition = "TEXT")
  private String partySelectionReasonText;

  /** Motivo de la selección de participante de la consulta 6.8 */
  @Column(name = "condition_type_reason_text", columnDefinition = "TEXT")
  private String conditionTypeReasonText;

  /** Fecha de inicio de la consulta 6.9 */
  @Column(name = "planned_date")
  private LocalDate plannedDate;

  /** Fecha límite de respueta de la 6.10 */
  @Column(name = "limit_date")
  private LocalDate limitDate;

  // LocatedContractingParty
  @Column(name = "contracting_party_type_code", length = TamanoCampos.TAMANO_50)
  private String contractingPartyTypeCode;

  @Column(name = "buyer_profile_uri_id", length = TamanoCampos.TAMANO_500)
  private String buyerProfileUriId;

  // Party
  @Column(name = "web_site_uri", length = TamanoCampos.TAMANO_500)
  private String webSiteUri;

  @Column(name = "party_name", length = TamanoCampos.TAMANO_500)
  private String partyName;

  // Party Identification
  @Column(name = "id_plataforma", length = TamanoCampos.TAMANO_50)
  private String idPlataforma;

  @Column(name = "dir3", length = TamanoCampos.TAMANO_50)
  private String dir3;

  @Column(name = "nif", length = TamanoCampos.TAMANO_50)
  private String nif;

  // Los arrays conservan el orden al comparar los valores JSON en las actualizaciones.
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "parent_located_parties")
  private ParentLocatedParty[] parentLocatedParties = new ParentLocatedParty[0];

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "postal_address_lines")
  private String[] postalAddressLines = new String[0];

  @Column(name = "postal_city_name", columnDefinition = "TEXT")
  private String postalCityName;

  @Column(name = "postal_zone", length = TamanoCampos.TAMANO_50)
  private String postalZone;

  @Column(name = "postal_country_code", length = TamanoCampos.TAMANO_50)
  private String postalCountryCode;

  @Column(name = "postal_country_name", columnDefinition = "TEXT")
  private String postalCountryName;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "languages")
  private Language[] languages = new Language[0];

  @Column(name = "submission_uri", columnDefinition = "TEXT")
  private String submissionUri;

  //
  //
  //
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "entry_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey =
          @ForeignKey(
              name = "pmcs_entry",
              foreignKeyDefinition =
                  "FOREIGN KEY (entry_id) REFERENCES entry(id) ON DELETE CASCADE"))
  private Entry entry;

  @OneToMany(
      mappedBy = "generalDocumentReferencePmcs",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<DocumentReference> generalDocumentReferenceList = new ArrayList<>();

  @OneToMany(
      mappedBy = "preliminaryMarketConsultationStatus",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<NoticeInfo> valideNoticeInfoList = new ArrayList<>();

  @OneToOne(
      mappedBy = "preliminaryMarketConsultationStatus",
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private ProcurementProject procurementProject;

  @OneToOne(
      mappedBy = "preliminaryMarketConsultationStatus",
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private TenderingProcess tenderingProcess;

  public record ParentLocatedParty(List<String> names, List<PartyIdentifier> identifiers)
      implements Serializable {}

  public record PartyIdentifier(String schemeName, String value) implements Serializable {}

  public record Language(String id, String name, String localeCode) implements Serializable {}
}
