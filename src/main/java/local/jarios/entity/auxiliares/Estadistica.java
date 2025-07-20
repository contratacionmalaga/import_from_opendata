package local.jarios.entity.auxiliares;

import jakarta.persistence.*;
import local.jarios.entity.Log;
import local.jarios.exceptions.MiUnknownHostException;
import local.jarios.helpers.ComunHelper;
import local.jarios.common.util.TamanoCampos;
import local.jarios.helpers.StringHelper;
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
        name = "estadistica"
)
public class Estadistica extends Auditable {

    /**
     * Identificador
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Equipo desde el que se realiza la importación
     */
    @Column(name = "equipo", nullable = false, length = TamanoCampos.TAMANO_100)
    private String equipo;

    /**
     * Número de registros históricos que se deben crear.
     */
    @Column(name = "nRegistrosHistoricosInsertar")
    private Long nRegistrosHistoricosInsertar;

    /**
     * Número de registros históricos que se deben eliminar.
     */
    @Column(name = "nRegistrosHistoricosEliminar")
    private Long nRegistrosHistoricosEliminar;

    /**
     * Número de registros históricos que se deben actualizar.
     */
    @Column(name = "nRegistrosHistoricosActualizar")
    private Long nRegistrosHistoricosActualizar;

    /**
     * Número de registros históricos que se deben actualizar.
     */
    @Column(name = "nRegistrosHistoricosRechazar")
    private Long nRegistrosHistoricosRechazar;

    /**
     * Duración
     */
    @Column(name = "duracion", nullable = false, length = TamanoCampos.TAMANO_100)
    private String duracion;

    //
    //
    //
    @OneToOne(
            cascade = CascadeType.ALL)
    @JoinColumn(
            name = "log_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_estadistica_log",
                    foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE"))
    private Log miLog;

    //
    //
    //
    public Estadistica(Log miLog) throws MiUnknownHostException {

        this.miLog = miLog;
        this.equipo = ComunHelper.getHostName();
    }

    public String toStringReducido() {

        String nInsertar = StringHelper.getNumeroConFormato(Math.toIntExact(nRegistrosHistoricosInsertar));
        String nEliminar = StringHelper.getNumeroConFormato(Math.toIntExact(nRegistrosHistoricosEliminar));
        String nActualizar = StringHelper.getNumeroConFormato(Math.toIntExact(nRegistrosHistoricosActualizar));
        String nRechazar = StringHelper.getNumeroConFormato(Math.toIntExact(nRegistrosHistoricosRechazar));

        return "Estadistica: [" +
                    "nRegistrosHistoricosInsertar='" + nInsertar + "', " +
                    "nRegistrosHistoricosEliminar='" + nEliminar + "', " +
                    "nRegistrosHistoricosActualizar='" + nActualizar + "', " +
                    "nRegistrosHistoricosRechazar='" + nRechazar + "', " +
                    "duracion='" + duracion +
                "']";
    }
}
