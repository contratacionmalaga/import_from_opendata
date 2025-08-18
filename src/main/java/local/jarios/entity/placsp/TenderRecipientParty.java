package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Description: Importaciones de Ficheros Excel desde Internet
 * Author: Juan Antonio
 * Date: 04/06/2024
 * Team: Juan Antonio
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "tender_recipient_party"
)
public class TenderRecipientParty extends Auditable {

    //
    //
    //
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "endpoint_id", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String endpointId;

    //
    //
    //
    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tendering_terms_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_tenderrecipientparty_tenderingterms",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tendering_terms_id) " +
                            "REFERENCES tendering_terms(id) ON DELETE CASCADE"))
    private TenderingTerms tenderingTerms;

    @Override
    public String toString() {

        return "TenderRecipientParty: " +
                "[endpointId='" + endpointId + "']";
    }
}
