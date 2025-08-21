package local.jarios.entity.placsp;

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
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa el estado de un expediente de contratación
 * dentro del ciclo de vida de la licitación pública.
 * <p>
 * Un expediente de contratación (contract folder) contiene toda la
 * información relativa al proceso de licitación, incluyendo documentos
 * técnicos y legales, proyectos de contratación, lotes, términos de
 * licitación, avisos, resultados, modificaciones, etc.
 * </p>
 *
 * <p>
 * Cada registro se almacena en la tabla <b>contract_folder_status</b>.
 * </p>
 *
 * <p>
 * Hereda de {@link Auditable}, por lo que incluye los metadatos de auditoría
 * (fecha de creación, última modificación, usuario, etc.).
 * </p>
 *
 * @author Juan
 * @version 1.0
 * @since 04/06/2024
 */
@Setter
@Getter
@Entity
@Table(name = "contract_folder_status")
public class ContractFolderStatus extends Auditable {

  /**
   * Identificador único del estado del expediente de contratación
   * en formato UUID. Se genera automáticamente al persistir.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;
  /**
   * Identificador del expediente de contratación al que se refiere.
   * <p>
   * Limitado a {@link Constantes#TAMANO_MAXIMO_CAMPO_50} caracteres.
   * </p>
   */
  @Column(name = "contract_folder_id", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String contractFolderId;
  /**
   * Código que describe el estado actual del expediente de contratación.
   * <p>
   * Campo obligatorio.
   * </p>
   */
  @Column(name = "contract_folder_status_code", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String contractFolderStatusCode;
  /**
   * Referencia a la entrada de origen en el sistema Atom.
   * Relación muchos-a-uno con {@link Entry}.
   */
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
  // RELACIONES PRINCIPALES
  // =========================================================================
  /**
   * Documento técnico asociado al expediente.
   * Relación uno-a-uno con {@link TechnicalDocumentReference}.
   */
  @OneToOne(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true)
  private TechnicalDocumentReference technicalDocumentReference;
  /**
   * Documento legal asociado al expediente.
   * Relación uno-a-uno con {@link LegalDocumentReference}.
   */
  @OneToOne(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true)
  private LegalDocumentReference legalDocumentReference;
  /**
   * Documentos adicionales relacionados con el expediente.
   * Relación uno-a-muchos con {@link AdditionalDocumentReference}.
   */
  @OneToMany(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<AdditionalDocumentReference> listAdditionalDocumentReference = new ArrayList<>();
  /**
   * Documentos generales asociados al expediente.
   * Relación uno-a-muchos con {@link GeneralDocument}.
   */
  @OneToMany(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<GeneralDocument> listGeneralDocument = new ArrayList<>();
  /**
   * Modificaciones realizadas al contrato relacionado con el expediente.
   * Relación uno-a-muchos con {@link ContractModification}.
   */
  @OneToMany(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<ContractModification> listContractModification = new ArrayList<>();
  /**
   * Parte contratante (entidad adjudicadora) asociada al expediente.
   * Relación uno-a-uno con {@link LocatedContractingParty}.
   */
  @OneToOne(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true)
  private LocatedContractingParty locatedContractingParty;
  /**
   * Información de avisos o publicaciones relacionadas con el expediente.
   * Relación uno-a-muchos con {@link NoticeInfo}.
   */
  @OneToMany(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<NoticeInfo> listNoticeInfo = new ArrayList<>();
  /**
   * Proyecto de contratación asociado al expediente.
   * Relación uno-a-uno con {@link ProcurementProject}.
   */
  @OneToOne(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true)
  private ProcurementProject procurementProject;
  /**
   * Lotes de contratación incluidos en el expediente.
   * Relación uno-a-muchos con {@link ProcurementProjectLot}.
   */
  @OneToMany(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<ProcurementProjectLot> listProcurementProjectLot = new ArrayList<>();
  /**
   * Proceso de licitación asociado al expediente.
   * Relación uno-a-uno con {@link TenderingProcess}.
   */
  @OneToOne(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true)
  private TenderingProcess tenderingProcess;
  /**
   * Términos y condiciones de licitación asociados al expediente.
   * Relación uno-a-uno con {@link TenderingTerms}.
   */
  @OneToOne(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true)
  private TenderingTerms tenderingTerms;
  /**
   * Resultados de la licitación del expediente.
   * Relación uno-a-muchos con {@link TenderResult}.
   */
  @OneToMany(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<TenderResult> listTenderResult = new ArrayList<>();
  /**
   * Identificadores únicos adicionales asociados al expediente.
   * Relación uno-a-muchos con {@link Uuid}.
   */
  @OneToMany(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Uuid> listUuid = new ArrayList<>();

  /**
   * Constructor por defecto.
   * <p>
   * Requerido por JPA para la correcta creación de proxies
   * y por Lombok para la inicialización básica.
   * </p>
   */
  public ContractFolderStatus() {
    // Constructor vacío requerido por JPA
  }

  /**
   * Devuelve una representación en cadena del estado del expediente
   * de contratación, mostrando los valores principales.
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
