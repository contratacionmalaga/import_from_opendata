package local.jarios.entity.auxiliares;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import local.jarios.common.util.TamanoCampos;
import local.jarios.core.enums.LugarImportacion;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.entity.atom.Feed;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "log")
public class Log extends AuditableCreatedAt {

  // Primary Key
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Enums
  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_sindicacion", length = TamanoCampos.TAMANO_50)
  private TipoSindicacion tipoSindicacion;

  @Enumerated(EnumType.STRING)
  @Column(name = "lugar_importacion", length = TamanoCampos.TAMANO_50)
  private LugarImportacion lugarImportacion;

  @Column(name = "n_registros")
  private int nRegistros;

  @Column(name = "fecha_generacion")
  private Date fechaGeneracion;

  // Relaciones uno a uno
  @OneToOne(mappedBy = "miLog", cascade = CascadeType.ALL, orphanRemoval = true)
  private Estadistica estadistica;

  @OneToOne(mappedBy = "miLog", cascade = CascadeType.ALL, orphanRemoval = true)
  private Configuracion configuracion;

  // Relaciones uno a muchos
  @OneToMany(mappedBy = "miLog", orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Feed> feedList = new ArrayList<>();

  @OneToMany(mappedBy = "miLog", orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Historico> historicoList = new ArrayList<>();

  @OneToMany(mappedBy = "miLog", orphanRemoval = true, fetch = FetchType.LAZY)
  private List<OrganoContratacion> organoContratacionList = new ArrayList<>();

  // Constructor principal
  public Log(
      LugarImportacion lugarImportacion,
      TipoSindicacion tipoSindicacion,
      int nRegistros,
      Date fechaGeneracion) {
    this.lugarImportacion = lugarImportacion;
    this.tipoSindicacion = tipoSindicacion;
    this.nRegistros = nRegistros;
    this.fechaGeneracion = fechaGeneracion;
  }

  // Constructor principal
  public Log(LugarImportacion lugarImportacion, TipoSindicacion tipoSindicacion) {
    this.lugarImportacion = lugarImportacion;
    this.tipoSindicacion = tipoSindicacion;
  }
}
