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
        name = "country"
)
public class Country extends Auditable {

    //
    // PROPIEDADES DEL MODELO
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "identification_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String identificationCode;

    @Column(name = "name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String name;

    //
    // RELACIONES CON ENTIDADES PADRES DE LA QUE ESTA DEPENDE
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "address_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_address_country",
                    foreignKeyDefinition = "FOREIGN KEY (address_id) REFERENCES address(id) ON DELETE CASCADE"))
    private Address address;

    @Override
    public String toString() {

        return "Country: " +
                "[identificationCode='" + identificationCode + "', " +
                "name='" + name + "']";
    }
    //
    // CONSTRUCTOR DE LA CLASE EN EL QUE SE GENERA EL UUID VERSIÓN 7
    //
    public Country() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
