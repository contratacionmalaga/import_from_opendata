package local.jarios.entity.auxiliares;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;


/**
 * Entidad que representa el personal activo en el sistema. Esta clase contiene información personal
 * y organizativa del empleado, incluyendo código personal, datos personales, unidad organizativa,
 * puesto y contactos. Implementa interfaces para manejo de actualización, comparación por
 * contenido, y gestión de identificadores y estados de borrado lógico. Author: Juan Antonio Date:
 * 04/06/2024 Team: Contratacion Electrónica
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "organos_contratacion"
)
@Slf4j
public class OrganoContratacion extends AuditableCreatedAt {

  /**
   * Identificador único del órgano de contratación.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  /**
   * Identificador único de la plataforma.
   */
  @JsonIgnore
  @Column(name = "id_plataforma", nullable = false, length = TamanoCampos.TAMANO_100)
  private String idPlataforma;

  /**
   * Nombre del órgano de contratación.
   */
  @Column(name = "nombre_oc", nullable = false, length = TamanoCampos.TAMANO_500)
  private String nombreOc;

  /**
   * Ubicación del órgano.
   */
  @Column(name = "ubicacion", length = TamanoCampos.TAMANO_500)
  private String ubicacion;

  /**
   * Primera dependencia jerárquica del órgano.
   */
  @Column(name = "dependencia1", length = TamanoCampos.TAMANO_500)
  private String dependencia1;

  /**
   * Segunda dependencia jerárquica del órgano.
   */
  @Column(name = "dependencia2", length = TamanoCampos.TAMANO_500)
  private String dependencia2;

  /**
   * NIF del órgano.
   */
  @Column(name = "nif", length = TamanoCampos.TAMANO_500)
  private String nif;

  /**
   * Código DIR3 asociado.
   */
  @Column(name = "dir3", length = TamanoCampos.TAMANO_500)
  private String dir3;

  /**
   * Código postal.
   */
  @Column(name = "codigo_postal", length = TamanoCampos.TAMANO_15)
  private String codigoPostal;

  /**
   * Código postal.
   */
  @Column(name = "nuts_provincia", length = TamanoCampos.TAMANO_15)
  private String nutsProvincia;

  /**
   * Medio propio asociado al órgano.
   */
  @Column(name = "medio_propio", length = TamanoCampos.TAMANO_100)
  private String medioPropio;

  /**
   * Estado activo/inactivo.
   */
  @Column(name = "activo", length = TamanoCampos.TAMANO_100)
  private String activo;

  // Relationship
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "log_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_organocontratacion_log",
          foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE")
  )
  private Log miLog;

  public static void imprimirLista(List<OrganoContratacion> organoContratacionList) {

    organoContratacionList.stream()
        .sorted(Comparator.comparing(OrganoContratacion::getNombreOc,
                                     Comparator.nullsLast(String::compareToIgnoreCase)))
        .forEach(organoContratacion ->
                     log.info(
                         "  {} - {} - {}",
                         organoContratacion.getNombreOc(),
                         organoContratacion.getNif(),
                         organoContratacion.getIdPlataforma()
                     )
        );
  }

  /**
   * Representación en texto del objeto.
   *
   * @return una cadena con los valores de los campos principales
   */
  @Override
  public String toString() {
    StringJoiner sj = new StringJoiner(", ", "OrganoContratacion: [", "]");

    sj.add("id='" + id + "'")
        .add("idPlataforma='" + idPlataforma + "'")
        .add("nombreOc='" + nombreOc + "'")
        .add("ubicacion='" + ubicacion + "'")
        .add("dependencia1='" + dependencia1 + "'")
        .add("dependencia2='" + dependencia2 + "'")
        .add("nif='" + nif + "'")
        .add("dir3='" + dir3 + "'")
        .add("codigoPostal='" + codigoPostal + "'")
        .add("medioPropio='" + medioPropio + "'")
        .add("activo='" + activo + "'")
        .add("nutsProvincia='" + nutsProvincia + "'");

    return sj.toString();
  }

  public String toStringResumido() {
    StringJoiner sj = new StringJoiner(", ", "OrganoContratacion: [", "]");

    sj.add("idPlataforma='" + idPlataforma + "'")
        .add("nombreOc='" + nombreOc + "'")
        .add("nif='" + nif + "'")
        .add("codigoPostal='" + codigoPostal + "'")
        .add("nutsProvincia='" + nutsProvincia + "'");

    return sj.toString();
  }
}
