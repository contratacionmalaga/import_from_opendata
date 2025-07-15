package local.jarios.entity.auxiliares;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.entity.Log;
import local.jarios.exceptions.MiUnknownHostException;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.LocalDateTimeHelper;
import local.jarios.common.util.TamanoCampos;
import local.jarios.helpers.StringHelper;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@Entity
@Table(
        name = "estadistica",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_estadistica_log",
                columnNames = "log_id")
)
public class Estadistica extends Auditable {

    //
    //
    //
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "equipo", nullable = false, length = TamanoCampos.TAMANO_100)
    private String equipo;

    @Column(name = "nFicheros", nullable = false)
    private int nFicheros;

    @Column(name = "nEntryLeidos", nullable = false)
    private int nEntryLeidos;

    @Column(name = "nEntryProcesados", nullable = false)
    private int nEntryProcesados;

    @Column(name = "nEntryGrabados", nullable = false)
    private int nEntryGrabados;

    @Column(name = "nEntryActualizados", nullable = false)
    private int nEntryActualizados;

    @Column(name = "nEntryRechazados", nullable = false)
    private int nEntryRechazados;

    @Column(name = "nEntryBorradosEnMap", nullable = false)
    private int nEntryBorradosEnMap;

    @Column(name = "fechaHoraInicial", nullable = false)
    private LocalDateTime fechaHoraInicial;

    @Column(name = "fechaHoraFinal", nullable = false)
    private LocalDateTime fechaHoraFinal;

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

        this.id = Generators.timeBasedEpochGenerator().generate();
        this.miLog = miLog;
        this.fechaHoraInicial = LocalDateTimeHelper.getLocalDateTimeNow();
        this.equipo = ComunHelper.getHostName();
    }

    //
    public void aumentarNFicheros() {

        nFicheros++;
    }

    //
    public void aumentarNEntryProcesados() {

        nEntryProcesados++;
    }

    //
    public void aumentarNEntryGrabados() {

        nEntryGrabados++;
    }

    //
    public void aumentarNEntryActualizados() {

        nEntryActualizados++;
    }

    //
    public void aumentarNEntryRechazados() {

        nEntryRechazados++;
    }

    //
    public void aumentarNEntryBorradosEnMap() {

        nEntryBorradosEnMap++;
    }

    public String toStringReducido() {

        return "Estadistica: [" +
                    "nFicheros='" + StringHelper.getNumeroConFormato(nFicheros) + "', " +
                    "nEntryLeidos='" + StringHelper.getNumeroConFormato(nEntryLeidos) + "', " +
                    "nEntryProcesados='" + StringHelper.getNumeroConFormato(nEntryProcesados) + "', " +
                    "nEntryGrabados='" + StringHelper.getNumeroConFormato(nEntryGrabados) + "', " +
                    "nEntryActualizados='" + StringHelper.getNumeroConFormato(nEntryActualizados) + "', " +
                    "nEntryRechazados='" + StringHelper.getNumeroConFormato(nEntryRechazados) + "', " +
                    "nEntryBorradosEnMap='" + StringHelper.getNumeroConFormato(nEntryBorradosEnMap) + "'" +
                "]";
    }
}
