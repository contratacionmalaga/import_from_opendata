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
import local.jarios.common.util.Constantes;
import local.jarios.common.util.TamanoCampos;
import local.jarios.exceptions.MiUnknownHostException;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.StringHelper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Description: Importaciones de Ficheros Excel desde Internet Author: Juan Antonio Date: 04/06/2024
 * Team: Juan Antonio
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
    name = "estadistica"
)
public class Estadistica extends AuditableCreatedAt {

  /**
   * Identificador.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  /**
   * Equipo desde el que se realiza la importación.
   */
  @Column(name = "equipo", nullable = false, length = TamanoCampos.TAMANO_100)
  private String equipo;

  /**
   * Número de registros históricos que se deben crear.
   */
  @Column(name = "n_registros_historicos_insertar")
  private Long numRegistrosHistoricosInsertar;

  /**
   * Número de registros históricos que se deben eliminar.
   */
  @Column(name = "n_registros_historicos_eliminar")
  private Long numRegistrosHistoricosEliminar;

  /**
   * Número de registros históricos que se deben actualizar.
   */
  @Column(name = "n_registros_historicos_actualizar")
  private Long numRegistrosHistoricosActualizar;

  /**
   * Número de registros históricos que se deben actualizar.
   */
  @Column(name = "n_registros_historicos_rechazar")
  private Long numRegistrosHistoricosRechazar;

  /**
   * Número de ficheros atoms procesado.
   */
  @Column(name = "total_historicos")
  private Long totalHistoricos;

  /**
   * Número de ficheros atoms procesado.
   */
  @Column(name = "n_ficheros_atoms")
  private Long numFicherosAtoms;

  /**
   * Número de ficheros atoms procesado.
   */
  @Column(name = "n_organos_contratacion_filtro")
  private Long numOrganosContratacionFiltro;

  /**
   * Número de ficheros atoms procesado.
   */
  @Column(name = "n_nifs_filtro")
  private Long numNifsFiltro;

  /**
   * Número de ficheros atoms procesado.
   */
  @Column(name = "n_deleted_entries")
  private Long numDeletedEntries;

  /**
   * Duración total del proceso de importación en formato legible.
   */
  @Column(name = "duracion_parseo", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String duracionParseo;

  /**
   * Duración total del proceso de importación en formato legible.
   */
  @Column(name = "duracion_persistencia", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String duracionPersistencia;

  /**
   * Relación UNO a UNO
   */
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

  /**
   * Creación del objeto
   *
   * @param miLog Log
   * @throws MiUnknownHostException excepción
   */

  public Estadistica(Log miLog) throws MiUnknownHostException {

    this.miLog = miLog;
    this.equipo = ComunHelper.getHostName();
  }

  /**
   *
   * @return cadena de caracteres
   */
  @Override
  public String toString() {

    String nInsertar = numRegistrosHistoricosInsertar == null ? "0" :
        StringHelper.getNumeroConFormato(Math.toIntExact(numRegistrosHistoricosInsertar));
    String nEliminar = numRegistrosHistoricosEliminar == null ? "0" :
        StringHelper.getNumeroConFormato(Math.toIntExact(numRegistrosHistoricosEliminar));
    String nActualizar = numRegistrosHistoricosActualizar == null ? "0" :
        StringHelper.getNumeroConFormato(Math.toIntExact(numRegistrosHistoricosActualizar));
    String nRechazar = numRegistrosHistoricosRechazar == null ? "0" :
        StringHelper.getNumeroConFormato(Math.toIntExact(numRegistrosHistoricosRechazar));

    return "Estadistica: [" +
        "nRegistrosHistoricosInsertar='" + nInsertar + "', " +
        "nRegistrosHistoricosEliminar='" + nEliminar + "', " +
        "nRegistrosHistoricosActualizar='" + nActualizar + "', " +
        "nRegistrosHistoricosRechazar='" + nRechazar + "', " +
        "']";
  }
}
