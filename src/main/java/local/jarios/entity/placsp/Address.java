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
        name = "address"
)
public class Address extends Auditable {

    //
    // PROPIEDADES DEL MODELO
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "address_line", nullable = false, columnDefinition = "TEXT")
    private String addressLine;

    @Column(name = "city_name", length = Constantes.TAMANO_MAXIMO_CAMPO_250)
    private String cityName;

    @Column(name = "postal_zone", length = Constantes.TAMANO_MAXIMO_CAMPO_250)
    private String postalZone;

    @Column(name = "country_subentity_code", length = Constantes.TAMANO_MAXIMO_CAMPO_5)
    private String countrySubentityCode;

    @Column(name = "country_subentity", length = Constantes.TAMANO_MAXIMO_CAMPO_250)
    private String countrySubentity;

    //
    // RELACIONES CON ENTIDADES PADRES DE LA QUE ESTA DEPENDE
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "party_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_location_party",
                    foreignKeyDefinition = "FOREIGN KEY (party_id) REFERENCES party(id) ON DELETE CASCADE"))
    private Party party;

    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "location_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_location_address",
                    foreignKeyDefinition = "FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE CASCADE"))
    private Location location;

    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "winningparty_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_winningparty_addres",
                    foreignKeyDefinition = "FOREIGN KEY (winningparty_id) REFERENCES winning_party(id) ON DELETE CASCADE"))
    private WinningParty winningParty;

    //
    // RELACIONES CON ENTIDADES HIJAS DEPENDIENTE DE ESTA
    //
    @OneToOne(mappedBy = "address", cascade = CascadeType.ALL, orphanRemoval = true)
    private Country country;

    @Override
    public String toString() {

        return "Address: " +
                "[addressLine='" + addressLine + "', " +
                "cityName='" + cityName + "', " +
                "postalZone='" + postalZone + "', " +
                "countrySubentityCode='" + countrySubentityCode + "', " +
                "countrySubentity='" + countrySubentity + "']";
    }

    //
    // CONSTRUCTOR DE LA CLASE EN EL QUE SE GENERA EL UUID VERSIÓN 7
    //
    public Address() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
