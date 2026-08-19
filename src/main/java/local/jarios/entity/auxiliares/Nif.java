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
import java.util.List;
import java.util.UUID;
import local.jarios.common.util.TamanoCampos;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
@Table(name = "filtros_nifs")
@Slf4j
public class Nif extends AuditableCreatedAt {

  /** Identificador único del órgano de contratación. */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  /** Identificador único de la plataforma. */
  @JsonIgnore
  @Column(name = "nif", nullable = false, length = TamanoCampos.TAMANO_100)
  private String nif;

  // Relationship
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "log_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey =
          @ForeignKey(
              name = "fk_filtros_nifs_log",
              foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE"))
  private Log miLog;

  /**
   * Constructor.
   *
   * @param nif Valor asociado.
   */
  public Nif(String nif) {
    this.nif = nif;
  }

  /**
   * Imprime la lista de Nifs.
   *
   * @param nifList Lista a imprimir.
   */
  public static void imprimirLista(List<String> nifList) {

    nifList.forEach(nif -> log.info("  {}", nif));
  }
}
