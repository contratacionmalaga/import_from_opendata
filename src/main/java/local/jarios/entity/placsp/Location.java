package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Description:
 * Author: juan
 * Date: 20/03/2025
 * Team:
 */

@Setter
@Getter
@Entity
@Table(
        name = "location"
)
public class Location extends Auditable {

    //
    // PROPIEDADES DEL MODELO
    //

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "country_subentity", length = Constantes.TAMANO_MAXIMO_CAMPO_250)
    private String countrySubentity;

    @Column(name = "country_subentity_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String countrySubentityCode;

    //
    // RELACIONES CON ENTIDADES PADRES DE LA QUE ESTA DEPENDE
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "procurement_project_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_procurementproject_realizedlocation",
                    foreignKeyDefinition =
                            "FOREIGN KEY (procurement_project_id) " +
                            "REFERENCES procurement_project(id) ON DELETE CASCADE"))
    private ProcurementProject procurementProject;

    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "party_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_party_location",
                    foreignKeyDefinition =
                            "FOREIGN KEY (party_id) " +
                            "REFERENCES party(id) ON DELETE CASCADE"))
    private Party party;

    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "winning_party_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_winningparty_location",
                    foreignKeyDefinition =
                            "FOREIGN KEY (winning_party_id) " +
                            "REFERENCES winning_party(id) ON DELETE CASCADE"))
    private WinningParty winningParty;

    //
    // RELACIONES CON ENTIDADES HIJAS DEPENDIENTE DE ESTA
    //
    @OneToOne(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    private Address address;

    @Override
    public String toString() {

        return ToStringUtil.autoToString(this);
    }

    //
    // CONSTRUCTOR DE LA CLASE EN EL QUE SE GENERA EL UUID VERSIÓN 7
    //
    public Location() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}


