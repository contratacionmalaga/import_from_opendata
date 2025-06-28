package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */

@Setter
@Getter
@Entity
@Table(
        name = "contract"
)

public class Contract extends Auditable {

        //
        //
        //
        @Id
        @Column(name = "id", updatable = false, nullable = false)
        private UUID id;

        @Column(name = "id_contract", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
        private String idContract;

        @Column(name = "issue_date")
        private LocalDate issueDate;

        //
        //
        //
        @OneToOne(
                cascade = CascadeType.ALL,
                fetch = FetchType.LAZY)
        @JoinColumn(
                name = "tender_result_id",
                referencedColumnName = "id",
                foreignKey = @ForeignKey(
                        name = "fk_contract_tenderresult",
                        foreignKeyDefinition =
                                "FOREIGN KEY (tender_result_id) " +
                                "REFERENCES tender_result(id) ON DELETE CASCADE"))
        private TenderResult tenderResult;


        @Override
        public String toString() {

                return "Contract: " +
                        "[idContract='" + idContract + "', " +
                        "[issueDate='" + issueDate + "']";
        }
        //
        //
        //
        public Contract() {

                //
                this.id = Generators.timeBasedEpochGenerator().generate();
        }
}
