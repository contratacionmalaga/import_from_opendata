package local.jarios.entity;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Description: LogEntity
 * Author: Juan Antonio
 * Date: 04/06/2024
 * Team: Juan Antonio
 */

@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(
        name = "log"
)

public class Log extends Auditable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipoSindicacion", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private TipoSindicacion tipoSindicacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "lugarImportacion", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private LugarImportacion lugarImportacion;

    @OneToOne(mappedBy = "miLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private Estadistica estadistica;

    @OneToOne(mappedBy = "miLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private Configuracion configuracion;

    @OneToMany(mappedBy = "miLog", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrganoContratacion> listOrganoContratacion = new ArrayList<>();

    @OneToMany(mappedBy = "miLog", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Feed> listFeed = new ArrayList<>();

    // Constructor con dos parámetros
    public Log(LugarImportacion lugarImportacion, TipoSindicacion tipoSindicacion) {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();

        //
        this.lugarImportacion = lugarImportacion;

        //
        this.tipoSindicacion = tipoSindicacion;
    }

    @Override
    public String toString() {

        return "(TipoSindicacion: " + tipoSindicacion + "; LugarImportacion: " + lugarImportacion + ")";
    }
}
