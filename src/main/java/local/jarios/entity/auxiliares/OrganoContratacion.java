package local.jarios.entity.auxiliares;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.Log;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Description: Author: juan Date: 16/03/2025 Team:
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
  private String id_plataforma;

  @Column(name = "nombre_oc", nullable = false, length = TamanoCampos.TAMANO_2500)
  private String nombre_oc;

  @Column(name = "codigo_postal", nullable = false, length = TamanoCampos.TAMANO_2500)
  private String codigo_postal;

  @Column(name = "codigo_nuts", nullable = false, length = TamanoCampos.TAMANO_2500)
  private String codigo_nuts;

  //
  //
  //
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "log_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_organocontratacion_milog",
          foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE"))
  private Log miLog;

  //
  //
  //
  public OrganoContratacion(String idPlataforma, String nombreOc, String codigo_postal) {

    this.id_plataforma = idPlataforma;
    this.nombre_oc = nombreOc;
    this.codigo_postal = codigo_postal;
  }

  //
  //
  //
  public OrganoContratacion(Log miLog, String idPlataforma, String nombreOc, String codigo_postal) {

    this.miLog = miLog;
    this.id_plataforma = idPlataforma;
    this.nombre_oc = nombreOc;
    this.codigo_postal = codigo_postal;
  }

  @Override
  public String toString() {

    return "OrganoContratacion: [" +
        "idPlataforma='" + id_plataforma + "', " +
        "nombreOc='" + nombre_oc + "', " +
        "codigo_postal='" + codigo_postal + "', " +
        "codigo_nuts='" + codigo_nuts + "']";
  }
}

