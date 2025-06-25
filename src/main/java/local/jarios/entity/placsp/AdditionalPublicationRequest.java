package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
        name = "additional_publication_request"
)
public class AdditionalPublicationRequest extends Auditable {

    //
    // PROPIEDADES DEL MODELO
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "agency_id", length = Constantes.TAMANO_MAXIMO_CAMPO_250)
    private String agencyId;

    @Column(name = "send_date_time")
    private LocalDateTime sendDateTime;

    //
    // RELACIONES CON ENTIDADES PADRES DE LA QUE ESTA DEPENDE
    //
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "additional_publication_status_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_apr_additionalpublicationstatus",
                    foreignKeyDefinition =
                            "FOREIGN KEY (additional_publication_status_id) " +
                            "REFERENCES additional_publication_status(id) ON DELETE CASCADE"))
    private AdditionalPublicationStatus additionalPublicationStatus;

    @Override
    public String toString() {

        return ToStringUtil.autoToString(this);
    }

    //
    // CONSTRUCTOR DE LA CLASE EN EL QUE SE GENERA EL UUID VERSIÓN 7
    //
    public AdditionalPublicationRequest() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
