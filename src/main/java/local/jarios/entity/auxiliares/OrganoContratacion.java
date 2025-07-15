package local.jarios.entity.auxiliares;

import jakarta.persistence.*;
import local.jarios.entity.Log;
import local.jarios.common.util.TamanoCampos;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Description:
 * Author: juan
 * Date: 16/03/2025
 * Team:
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "organo_contratacion"
)
public class OrganoContratacion extends Auditable {

    //
    //
    //
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "id_plataforma", nullable = false, length = TamanoCampos.TAMANO_15)
    private String idPlataforma;

    @Column(name = "nombre", nullable = false, length = TamanoCampos.TAMANO_2500)
    private String nombre;

    //
    //
    //
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "log_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_organocontratacion_log",
                    foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE"))
    private Log miLog;

    //
    //
    //
    public OrganoContratacion(Log miLog, String idPlataforma, String organoContratacion) {

        this.miLog = miLog;
        this.idPlataforma = idPlataforma;
        this.nombre = organoContratacion;
    }
}

