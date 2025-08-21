package local.jarios.entity.placsp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import local.jarios.common.util.Constantes;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.enums.TipoSolvencia;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Entidad que representa los criterios de evaluación técnica aplicados a un
 * operador económico para determinar su solvencia y capacidad de participar
 * en un contrato o licitación.
 * <p>
 * Estos criterios permiten definir requisitos técnicos mínimos, umbrales
 * de participación y el tipo de solvencia esperada. También pueden asociarse
 * a nivel de lote dentro de un proceso de contratación pública.
 * </p>
 *
 * <p>
 * Cada instancia de esta entidad se corresponde con un registro en la tabla
 * <b>evaluation_criteria</b> de la base de datos.
 * </p>
 *
 * <p>
 * Hereda de {@link Auditable}, por lo que incluye metadatos de auditoría
 * (fecha de creación, última modificación, usuario, etc.).
 * </p>
 *
 * @author Juan
 * @version 1.0
 * @since 04/06/2024
 */
@Setter
@Getter
@Entity
@Table(name = "evaluation_criteria")
public class EvaluationCriteria extends Auditable {

  /**
   * Identificador único del criterio de evaluación en formato UUID.
   * Se genera automáticamente al persistir la entidad.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;
  /**
   * Código que tipifica el criterio de solvencia.
   * <p>
   * El código se limita a un máximo de {@link Constantes#TAMANO_MAXIMO_CAMPO_50} caracteres.
   * </p>
   */
  @Column(name = "evaluation_criteria_type_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String evaluationCriteriaTypeCode;
  /**
   * Descripción detallada del criterio de evaluación.
   * <p>
   * Se almacena como texto largo (tipo {@code TEXT} en la base de datos).
   * </p>
   */
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;
  /**
   * Cantidad umbral que debe cumplir el operador económico
   * para superar este criterio de evaluación.
   */
  @Column(name = "threshold_quantity")
  private Double thresholdQuantity;
  /**
   * Tipo de solvencia evaluada, expresada como un valor
   * del enumerado {@link TipoSolvencia}.
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_solvencia")
  private TipoSolvencia tipoSolvencia;
  /**
   * Relación con la solicitud de calificación del licitador
   * ({@link TendererQualificationRequest}) a la que pertenece este criterio.
   * <p>
   * La relación está configurada con eliminación en cascada
   * para mantener la integridad referencial.
   * </p>
   */
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "tenderer_qualification_request_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_evaluationcriteria_tendererqualificationrequest",
          foreignKeyDefinition =
              "FOREIGN KEY (tenderer_qualification_request_id) " +
                  "REFERENCES tenderer_qualification_request(id) ON DELETE CASCADE"))
  private TendererQualificationRequest tendererQualificationRequest;

  /**
   * Constructor por defecto.
   * <p>
   * Requerido por JPA para la correcta creación de proxies
   * y por Lombok para la inicialización básica.
   * </p>
   */
  public EvaluationCriteria() {
    // Constructor vacío requerido por JPA
  }

  /**
   * Devuelve una representación en cadena del criterio de evaluación,
   * mostrando los valores principales de sus atributos.
   *
   * @return cadena con los valores de {@code evaluationCriteriaTypeCode},
   * {@code description}, {@code thresholdQuantity} y {@code tipoSolvencia}.
   */
  @Override
  public String toString() {
    return "EvaluationCriteria: " +
        "[evaluationCriteriaTypeCode='" + evaluationCriteriaTypeCode + "', " +
        "description='" + description + "', " +
        "thresholdQuantity='" + thresholdQuantity + "', " +
        "tipoSolvencia='" + tipoSolvencia + "']";
  }
}
