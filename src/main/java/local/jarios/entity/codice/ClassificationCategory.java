package local.jarios.entity.codice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa una categoría de clasificación utilizada para definir requisitos de participación en
 * un proceso de licitación.
 *
 * <p>Esta entidad corresponde a capacidades requeridas a los licitadores, que pueden aplicarse
 * tanto a nivel de lote como para toda la licitación.
 *
 * <p>Cada categoría está asociada a un esquema de clasificación específico, definido en {@link
 * ClassificationScheme}.
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
@Table(name = "classification_category")
public class ClassificationCategory extends AuditableCreatedAt {

  /**
   * Identificador único universal (UUID) de la categoría de clasificación.
   *
   * <p>Clave primaria generada automáticamente. No puede ser actualizada ni ser nula.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  /** Código que identifica el valor de la categoría de clasificación. */
  @Column(name = "code_value", length = TamanoCampos.TAMANO_50)
  private String codeValue;

  /**
   * Esquema de clasificación al que pertenece esta categoría.
   *
   * <p>Relación muchos a uno con la entidad {@link ClassificationScheme}. La eliminación en cascada
   * está configurada en la clave foránea.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "classification_scheme_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey =
          @ForeignKey(
              name = "fk_classificationcategory_classificationscheme",
              foreignKeyDefinition =
                  "FOREIGN KEY (classification_scheme_id) "
                      + "REFERENCES classification_scheme(id) ON DELETE CASCADE"))
  private ClassificationScheme classificationScheme;

  @Override
  public String toString() {
    return "ClassificationCategory: " + "[codeValue='" + codeValue + "']";
  }
}
