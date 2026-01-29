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
import local.jarios.common.util.Constantes;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa el estado de un expediente de contratación dentro del ciclo de vida de la
 * licitación pública.
 *
 * @author Juan
 * @version 1.0
 * @since 04/06/2024
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "contract_folder_status")
public class ContractFolderStatus extends AuditableCreatedAt {

  /**
   * Identificador único del estado del expediente de contratación en formato UUID. Se genera
   * automáticamente al persistir.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Contract Folder Status
  @Column(name = "contract_folder_id", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String contractFolderId;

  @Column(name = "contract_folder_status_code", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String contractFolderStatusCode;

  // Located Conctracting party
  @Column(name = "contracting_party_type_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String contractingPartyTypeCode;

  @Column(name = "buyer_profile_uri_id", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String buyerProfileUriId;

  // Party
  @Column(name = "web_site_uri", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String webSiteUri;

  @Column(name = "party_name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String partyName;

  // Address
  @Column(name = "address_line", columnDefinition = "TEXT")
  private String addressLine;

  @Column(name = "city_name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String cityName;

  @Column(name = "postal_zone", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String postalZone;

  // Contact
  @Column(name = "contact_name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String contactName;

  @Column(name = "contact_telephone", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String contactTelephone;

  @Column(name = "contact_electronic_mail", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String contactElectronicMail;

  // Party Identification
  @Column(name = "id_oc_plat", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String idOcPlat;

  @Column(name = "id_plataforma", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String idPlataforma;

  @Column(name = "dir3", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String dir3;

  @Column(name = "nif", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String nif;

  @Column(name = "otros", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String otros;

  // =========================================================================
  // RELACIONES PADRE
  // =========================================================================
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "entry_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_contractfolderstatus_entry",
          foreignKeyDefinition = "FOREIGN KEY (entry_id) REFERENCES entry(id) ON DELETE CASCADE"))
  private Entry entry;

  // =========================================================================
  // RELACIONES HIJAS
  // =========================================================================
  @OneToOne(mappedBy = "technicalDocumentReference", cascade = CascadeType.ALL, orphanRemoval = true)
  private DocumentReference technicalDocumentReference;

  @OneToOne(mappedBy = "legalDocumentReference", cascade = CascadeType.ALL, orphanRemoval = true)
  private DocumentReference legalDocumentReference;

  @OneToMany(
      mappedBy = "additionalDocumentReference",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY
  )
  private List<DocumentReference> additionalDocumentReferenceList = new ArrayList<>();

  @OneToMany(
      mappedBy = "generalDocumentReference",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY
  )
  private List<DocumentReference> generalDocumentReferenceList = new ArrayList<>();

  @OneToMany(
      mappedBy = "contractFolderStatus",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY
  )
  private List<ContractModification> contractModificationList = new ArrayList<>();

  @OneToMany(
      mappedBy = "contractFolderStatus",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY
  )
  private List<NoticeInfo> valideNoticeInfoList = new ArrayList<>();

  @OneToOne(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true)
  private ProcurementProject procurementProject;

  @OneToMany(
      mappedBy = "contractFolderStatus",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY
  )
  private List<ProcurementProjectLot> procurementProjectLotList = new ArrayList<>();

  @OneToOne(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true)
  private TenderingProcess tenderingProcess;

  @OneToOne(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true)
  private TenderingTerms tenderingTerms;

  @OneToMany(
      mappedBy = "contractFolderStatus",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY
  )
  private List<TenderResult> tenderResultList = new ArrayList<>();

  @OneToMany(
      mappedBy = "contractFolderStatus",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY
  )
  private List<Uuid> uuidList = new ArrayList<>();

  /**
   * Devuelve una representación en cadena del estado del expediente de contratación, mostrando los
   * valores principales.
   *
   * @return cadena con {@code contractFolderId} y {@code contractFolderStatusCode}.
   */
  @Override
  public String toString() {
    return "ContractFolderStatus: " +
        "[contractFolderId='" + contractFolderId + "', " +
        "contractFolderStatusCode='" + contractFolderStatusCode + "']";
  }
}
