package local.jarios.entity.auxiliares;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import local.jarios.common.util.TamanoCampos;
import local.jarios.core.enums.LugarImportacion;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.entity.atom.Feed;
import local.jarios.helpers.LocalDateTimeHelper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "log")
public class Log {

  // Primary Key
  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
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

  /** Fecha técnica en la que se persiste este registro de importación. */
  @JsonIgnore
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  // Relaciones uno a uno
  @OneToOne(mappedBy = "miLog", cascade = CascadeType.ALL, orphanRemoval = true)
  private Estadistica estadistica;

  @OneToOne(mappedBy = "miLog", cascade = CascadeType.ALL, orphanRemoval = true)
  private Configuracion configuracion;

  // Relaciones uno a muchos
  @OneToMany(mappedBy = "miLog", orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Feed> feedList = new ArrayList<>();

  @OneToMany(mappedBy = "miLog", orphanRemoval = true, fetch = FetchType.LAZY)
  private List<HistoricoEntry> historicoList = new ArrayList<>();

  @OneToMany(mappedBy = "miLog", orphanRemoval = true, fetch = FetchType.LAZY)
  private List<HistoricoDeletedEntry> historicoDeletedEntryList = new ArrayList<>();

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

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTimeHelper.getLocalDateTimeNow();
    }
  }
}
