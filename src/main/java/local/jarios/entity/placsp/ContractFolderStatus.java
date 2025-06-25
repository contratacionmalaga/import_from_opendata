package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Description: Importaciones de Ficheros Excel desde Internet
 * Author: Juan Antonio
 * Date: 04/06/2024
 * Team: Juan Antonio
 */

@Setter
@Getter
@Entity
@Table(
        name = "contract_folder_status"
)

public class ContractFolderStatus extends Auditable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "contract_folder_id", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String contractFolderId;

    @Column(name = "contract_folder_status_code", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String contractFolderStatusCode;

    //
    //
    //
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "entry_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_contractfolderstatus_entry",
                    foreignKeyDefinition = "FOREIGN KEY (entry_id) REFERENCES entry(id) ON DELETE CASCADE"))
    private Entry entry;

    //
    //
    //

    @OneToOne(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private TechnicalDocumentReference technicalDocumentReference;

    @OneToOne(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private LegalDocumentReference legalDocumentReference;

    @OneToMany(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AdditionalDocumentReference> listAdditionalDocumentReference = new ArrayList<>();

    @OneToMany(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GeneralDocument> listGeneralDocument = new ArrayList<>();

    @OneToMany(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ContractModification> listContractModification = new ArrayList<>();

    @OneToOne(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private LocatedContractingParty locatedContractingParty;

    @OneToMany(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NoticeInfo> listNoticeInfo = new ArrayList<>();

    @OneToOne(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProcurementProject procurementProject;

    @OneToMany(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProcurementProjectLot> listProcurementProjectLot = new ArrayList<>();

    @OneToOne(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private TenderingProcess tenderingProcess;

    @OneToOne(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private TenderingTerms tenderingTerms;

    @OneToMany(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TenderResult> listTenderResult = new ArrayList<>();

    @OneToMany(mappedBy = "contractFolderStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Uuid> listUuid = new ArrayList<>();

    @Override
    public String toString() {

        return ToStringUtil.autoToString(this);
    }

    //
    //
    //
    public ContractFolderStatus() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
