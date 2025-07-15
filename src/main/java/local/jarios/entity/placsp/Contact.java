package local.jarios.entity.placsp;

import jakarta.persistence.*;
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
public class Contact extends Auditable {

    //
    // PROPIEDADES DE LA ENTIDAD
    //
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String name;

    @Column(name = "telephone", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String telephone;

    @Column(name = "telefax", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String telefax;

    @Column(name = "electronic_mail", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String electronicMail;

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
                    name = "fk_party_contact",
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
                    name = "fk_winningparty_contact",
                    foreignKeyDefinition =
                            "FOREIGN KEY (winning_party_id) " +
                            "REFERENCES winning_party(id) ON DELETE CASCADE"))
    private WinningParty winningParty;

    @Override
    public String toString() {

        return "Contact: " +
                "[name='" + name + "', " +
                "[telephone='" + telephone + "', " +
                "[telefax='" + telefax + "', " +
                "[electronicMail='" + electronicMail + "']";
    }
}
