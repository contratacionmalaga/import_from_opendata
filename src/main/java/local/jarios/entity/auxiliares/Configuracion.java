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
import java.time.LocalDateTime;
import java.util.UUID;
import local.jarios.common.util.TamanoCampos;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "configuracion")
@Slf4j
public class Configuracion extends AuditableCreatedAt {

  /** Identificador. */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  /** Fecha del fichero excel asociado a los órganos de contratación. */
  @Column(name = "filtro_fecha_inicio_lectura")
  private LocalDateTime filtroFechaInicioLectura;

  /** Fecha del fichero excel asociado a los órganos de contratación. */
  @Column(name = "filtro_fecha_fin_lectura")
  private LocalDateTime filtroFechaFinLectura;

  /** Fecha del fichero excel asociado a los órganos de contratación. */
  @Column(name = "filtro_codigos_postales", length = TamanoCampos.TAMANO_2500)
  private String filtroCodigosPostales;

  @Column(name = "filtro_nif", length = TamanoCampos.TAMANO_2500)
  private String filtroNifs;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "log_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey =
          @ForeignKey(
              name = "fk_configuracion_milog",
              foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE"))
  private Log miLog;

  /**
   * Constructor de la clase.
   *
   * @param miLog Log asociado.
   */
  public Configuracion(Log miLog, OpenDataExecutionContext context) {

    this.miLog = miLog;
    this.filtroFechaInicioLectura = context.getFiltroFechaInicial();
    this.filtroFechaFinLectura = context.getFiltroFechaFinal();
    this.filtroCodigosPostales = context.getFiltroCodigosPostales();
    this.filtroNifs = context.getFiltroNifs();
  }
}
