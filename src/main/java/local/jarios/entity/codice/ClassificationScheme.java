package local.jarios.entity.codice;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa un esquema de clasificación utilizado para definir requisitos de participación en un
 * proceso de licitación.
 *
 * <p>Este esquema puede incluir varias categorías de clasificación y se asocia a una solicitud de
 * cualificación del licitador.
 *
 * <p>Puede aparecer tanto a nivel de lote como para toda la licitación.
 *
 * <p><b>Author:</b> Juan Antonio
 *
 * <p><b>Date:</b> 04/06/2024
 *
 * <p><b>Team:</b> Juan Antonio
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "classification_scheme")
public class ClassificationScheme extends AuditableCreatedAt {

  /**
   * Identificador único universal (UUID) del esquema de clasificación.
   *
   * <p>Clave primaria generada automáticamente. No puede ser actualizada ni ser nula.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  /** UUID externo del esquema, que puede usarse para referencia adicional. */
  @Column(name = "classification_scheme_id", length = TamanoCampos.TAMANO_500)
  private String classificationSchemeId;

  /**
   * Solicitud de cualificación del licitador asociada a este esquema de clasificación.
   *
   * <p>Relación muchos a uno con la entidad {@link TendererQualificationRequest}. La eliminación en
   * cascada está configurada en la clave foránea.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "tenderer_qualification_request_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey =
          @ForeignKey(
              name = "fk_classificationscheme_tendererqualificationrequest",
              foreignKeyDefinition =
                  "FOREIGN KEY (tenderer_qualification_request_id) "
                      + "REFERENCES tenderer_qualification_request(id) ON DELETE CASCADE"))
  private TendererQualificationRequest tendererQualificationRequest;

  /**
   * Lista de categorías de clasificación que forman parte de este esquema.
   *
   * <p>Relación uno a muchos con la entidad {@link ClassificationCategory}. La eliminación en
   * cascada y la remoción huérfana están habilitadas.
   */
  @OneToMany(mappedBy = "classificationScheme", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ClassificationCategory> classificationCategory;

  @Override
  public String toString() {
    return "ClassificationScheme: " + "[classificationSchemeId='" + classificationSchemeId + "]";
  }
}
