package local.jarios.entity;

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
import local.jarios.common.util.Constantes;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "log")
public class Log extends Auditable {

  // Primary Key
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Enums
  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_sindicacion", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private TipoSindicacion tipoSindicacion;

  @Enumerated(EnumType.STRING)
  @Column(name = "lugar_importacion", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private LugarImportacion lugarImportacion;

  // Relaciones uno a uno
  @OneToOne(mappedBy = "miLog", cascade = CascadeType.ALL, orphanRemoval = true)
  private Estadistica estadistica;

  @OneToOne(mappedBy = "miLog", cascade = CascadeType.ALL, orphanRemoval = true)
  private Configuracion configuracion;

  // Relaciones uno a muchos
  @OneToMany(mappedBy = "miLog", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<OrganoContratacion> listOrganoContratacion = new ArrayList<>();

  @OneToMany(mappedBy = "miLog", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Feed> listFeed = new ArrayList<>();

  @OneToMany(mappedBy = "miLog", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Historico> listHistorio = new ArrayList<>();

  // Constructor principal
  public Log(LugarImportacion lugarImportacion, TipoSindicacion tipoSindicacion) {
    this.lugarImportacion = lugarImportacion;
    this.tipoSindicacion = tipoSindicacion;
  }

  @Override
  public String toString() {
    return "Log: [" +
        "tipoSindicacion='" + tipoSindicacion + "', " +
        "lugarImportacion=" + lugarImportacion +
        "]";
  }
}
