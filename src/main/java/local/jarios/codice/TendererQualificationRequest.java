package local.jarios.codice;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Description: Importaciones de Ficheros Excel desde Internet Author: Juan Antonio Date: 04/06/2024
 * Team: Juan Antonio
 */

@Setter
@Getter
@NoArgsConstructor
// 4.30 Requisitos de participación
// Capacidades requeridas a los licitadores durante el proceso de licitación.
// Puede aparecer tanto a nivel de lote como para toda la licitación
public class TendererQualificationRequest {

  private String personalSituation;
  private String description;
  private BigDecimal employeeQuantity;
  private String employeeQuantityDescription;

// 4.30.1 Criterio de evaluación (Solvencias)
//     Técnicos
//     Económico - Financiera
//  @OneToMany(mappedBy = "tendererQualificationRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//  private List<EvaluationCriteria> evaluationCriteria = new ArrayList<>();
//
//  // Clasificación empresarial solicitada: Especifica las Clasificaciones requeridas para los Licitadores
//  @OneToMany(mappedBy = "tendererQualificationRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//  private List<ClassificationScheme> requiredBusinessClassificationScheme = new ArrayList<>();
//
//  // Clasificación empresarial solicitada: Especifica las Clasificaciones requeridas para los Licitadores
//  @OneToMany(mappedBy = "tendererQualificationRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//  private List<TendererRequirement> specificTendererRequirement = new ArrayList<>();

  @Override
  public String toString() {

    return "TendererQualificationRequest: " +
        "[personalSituation='" + personalSituation + "', " +
        "description='" + description + "', " +
        "employeeQuantity='" + employeeQuantity + "', " +
        "employeeQuantityDescription='" + employeeQuantityDescription + "']";
  }
}
