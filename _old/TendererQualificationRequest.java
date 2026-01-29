package local.jarios.entity.codice;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
    name = "tenderer_qualification_request"
)

// 4.30 Requisitos de participación
// Capacidades requeridas a los licitadores durante el proceso de licitación.
// Puede aparecer tanto a nivel de lote como para toda la licitación
public class TendererQualificationRequest extends AuditableCreatedAt {

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Título habilitante: Descripción textual de los requisitos específicos del operador económico para poder
  // participar en la licitación.
  @Column(name = "personal_situation", columnDefinition = "TEXT")
  private String personalSituation;

  // Solvencia requerida: Descripción textual de la información y trámites necesarios para evaluar si se cumplen
  // los requisitos de capacidad.
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "employee_quantity")
  private BigDecimal employeeQuantity;

  @Column(name = "employee_quantity_description", columnDefinition = "TEXT")
  private String employeeQuantityDescription;

  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "tendering_terms_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_tendererqualificationrequest_tenderingterms",
          foreignKeyDefinition =
              "FOREIGN KEY (tendering_terms_id) " +
                  "REFERENCES tendering_terms(id) ON DELETE CASCADE"))
  private TenderingTerms tenderingTerms;

  // 4.30.1 Criterio de evaluación (Solvencias)
  //     Técnicos
  //     Económico - Financiera
  @OneToMany(mappedBy = "tendererQualificationRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<EvaluationCriteria> evaluationCriteria = new ArrayList<>();

  // Clasificación empresarial solicitada: Especifica las Clasificaciones requeridas para los Licitadores
  @OneToMany(mappedBy = "tendererQualificationRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<ClassificationScheme> requiredBusinessClassificationScheme = new ArrayList<>();

  // Clasificación empresarial solicitada: Especifica las Clasificaciones requeridas para los Licitadores
  @OneToMany(mappedBy = "tendererQualificationRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<TendererRequirement> specificTendererRequirement = new ArrayList<>();

  @Override
  public String toString() {

    return "TendererQualificationRequest: " +
        "[personalSituation='" + personalSituation + "', " +
        "description='" + description + "', " +
        "employeeQuantity='" + employeeQuantity + "', " +
        "employeeQuantityDescription='" + employeeQuantityDescription + "']";
  }
}
