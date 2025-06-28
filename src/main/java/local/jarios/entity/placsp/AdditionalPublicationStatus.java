package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
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
        name = "additional_publication_status"
)
public class AdditionalPublicationStatus extends Auditable {

    //
    // PROPIEDADES DEL MODELO
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "publication_median_ame", length = Constantes.TAMANO_MAXIMO_CAMPO_250)
    private String publicationMediaName;

    //
    // RELACIONES CON ENTIDADES PADRES DE LA QUE ESTA DEPENDE
    //
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "notice_info_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_aps_noticeinfo",
                    foreignKeyDefinition =
                            "FOREIGN KEY (notice_info_id) " +
                            "REFERENCES notice_info(id) ON DELETE CASCADE"))
    private NoticeInfo noticeInfo;

    //
    // RELACIONES CON ENTIDADES HIJAS DEPENDIENTE DE ESTA
    //
    @OneToMany(mappedBy = "additionalPublicationStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdditionalPublicationRequest> additionalPublicationRequestList= new ArrayList<>();

    @OneToMany(mappedBy = "additionalPublicationStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdditionalPublicationDocumentReference>
            additionalPublicationDocumentReferenceList = new ArrayList<>();

    @Override
    public String toString() {

        return "AdditionalPublicationRequest: " +
                "[publicationMediaName='" + publicationMediaName + "']";
    }

    //
    // CONSTRUCTOR DE LA CLASE EN EL QUE SE GENERA EL UUID VERSIÓN 7
    //
    public AdditionalPublicationStatus() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
