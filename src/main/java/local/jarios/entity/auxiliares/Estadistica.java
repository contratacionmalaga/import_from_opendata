package local.jarios.entity.auxiliares;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.Log;
import local.jarios.exceptions.MiUnknownHostException;
import local.jarios.helpers.ComunHelper;
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
  @Column(name = "n_registros_historicos_insertar")
  private Long nRegistrosHistoricosInsertar;

  /**
   * Número de registros históricos que se deben eliminar.
   */
  @Column(name = "n_registros_historicos_eliminar")
  private Long nRegistrosHistoricosEliminar;

  /**
   * Número de registros históricos que se deben actualizar.
   */
  @Column(name = "n_registros_historicos_actualizar")
  private Long nRegistrosHistoricosActualizar;

  /**
   * Número de registros históricos que se deben actualizar.
   */
  @Column(name = "n_registros_historicos_rechazar")
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
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "log_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_estadistica_milog",
          foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE"))
  private Log miLog;

  //
  //
  //
  public Estadistica(Log miLog) throws MiUnknownHostException {

    this.miLog = miLog;
    this.equipo = ComunHelper.getHostName();
  }

  @Override
  public String toString() {

    String nInsertar = nRegistrosHistoricosInsertar == null ? "0" :
        StringHelper.getNumeroConFormato(Math.toIntExact(nRegistrosHistoricosInsertar));
    String nEliminar = nRegistrosHistoricosEliminar == null ? "0" :
        StringHelper.getNumeroConFormato(Math.toIntExact(nRegistrosHistoricosEliminar));
    String nActualizar = nRegistrosHistoricosActualizar == null ? "0" :
        StringHelper.getNumeroConFormato(Math.toIntExact(nRegistrosHistoricosActualizar));
    String nRechazar = nRegistrosHistoricosRechazar == null ? "0" :
        StringHelper.getNumeroConFormato(Math.toIntExact(nRegistrosHistoricosRechazar));

    return "Estadistica: [" +
        "nRegistrosHistoricosInsertar='" + nInsertar + "', " +
        "nRegistrosHistoricosEliminar='" + nEliminar + "', " +
        "nRegistrosHistoricosActualizar='" + nActualizar + "', " +
        "nRegistrosHistoricosRechazar='" + nRechazar + "', " +
        "duracion='" + duracion +
        "']";
  }
}
