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
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;


@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
    name = "configuracion"
)
@Slf4j
public class Configuracion extends AuditableCreatedAt {

  /**
   * Identificador.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  /**
   * Fecha del fichero excel asociado a los órganos de contratación.
   */
  @Column(name = "path", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String path;

  /**
   * Fecha del fichero excel asociado a los órganos de contratación.
   */
  @Column(name = "filename", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String filename;

  /**
   * Fecha del fichero excel asociado a los órganos de contratación.
   */
  @Column(name = "url", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String url;

  /**
   * Fecha del fichero excel asociado a los órganos de contratación.
   */
  @Column(name = "filtro_fecha_inicio_lectura")
  private LocalDateTime filtroFechaInicioLectura;

  /**
   * Fecha del fichero excel asociado a los órganos de contratación.
   */
  @Column(name = "filtro_fecha_fin_lectura")
  private LocalDateTime filtroFechaFinLectura;

  /**
   * Fecha del fichero excel asociado a los órganos de contratación.
   */
  @Column(name = "filtro_codigos_postales", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
  private String filtroCodigosPostales;

  @Column(name = "filtro_nif", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
  private String filtroNifs;


  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "log_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_configuracion_milog",
          foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE"))
  private Log miLog;

  /**
   * Constructor de la clase.
   *
   * @param miLog Log asociado.
   */
  public Configuracion(Log miLog) {

    this.miLog = miLog;

    // Cargar propiedades una única vez
    PropertiesManagerService props = PropertiesManagerServiceImpl.getInstance();

    switch (miLog.getLugarImportacion()) {
      case INTERNET -> {
        this.path = Constantes.CADENA_VACIA;
        this.filename = Constantes.CADENA_VACIA;
        this.url = props.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_URL);
      }
      case LOCAL -> {
        this.path = props.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_PATH);
        this.filename = props.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_FILENAME);
        this.url = Constantes.CADENA_VACIA;
      }
    }

    this.filtroFechaInicioLectura = VariablesGlobales.getFiltroFechaInicial();
    this.filtroFechaFinLectura = VariablesGlobales.getFiltroFechaFinal();
    this.filtroCodigosPostales = props.getProperty(
        PropertiesFiles.FILTER,
        PropertiesKeys.FILTER_CODIGOS_POSTALES);
    this.filtroNifs = props.getProperty(
        PropertiesFiles.FILTER,
        PropertiesKeys.FILTER_NIFS);
  }

  @Override
  public String toString() {

    return "Configuracion: [" +
        "path='" + path + "', " +
        "filename='" + filename + "', " +
        "url='" + url + "', " +
        "filtroFechaInicioLectura='" + filtroFechaInicioLectura + "', " +
        "filtroFechaFinLectura='" + filtroFechaFinLectura + "', " +
        "filtroCodigosPostales='" + filtroCodigosPostales +
        "filtroNifs='" + filtroNifs + "']";
  }
}
