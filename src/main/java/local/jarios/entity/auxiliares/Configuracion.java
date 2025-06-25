package local.jarios.entity.auxiliares;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.entity.Log;
import local.jarios.common.util.Constantes;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;


@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "configuracion",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_configuracion_log",
                columnNames = "log_id"
        )
)
@Slf4j
public class Configuracion extends Auditable {

    //
    //
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "path", length = Constantes.TAMANO_MAXIMO_CAMPO_250)
    private String path;

    @Column(name = "filename", length = Constantes.TAMANO_MAXIMO_CAMPO_250)
    private String filename;

    @Column(name = "url", length = Constantes.TAMANO_MAXIMO_CAMPO_250)
    private String url;

    @Column(name = "filtro_fecha_inicio_lectura")
    private String filtroFechaInicioLectura;

    @Column(name = "filtro_fecha_fin_lectura")
    private String filtroFechaFinLectura;

    @Column(name = "filtro_sql", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
    private String filtroSql;

    @Column(name = "filtro_objeto")
    private String filtroObjeto;

    @Column(name = "filtro_nuts")
    private String filtroNuts;

    //
    //
    //
    @OneToOne(
            cascade = CascadeType.ALL)
    @JoinColumn(
            name = "log_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_configuracion_log",
                    foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE"))
    private Log miLog;


    //
    //
    //
    public Configuracion(Log miLog) {

        this.id = Generators.timeBasedEpochGenerator().generate();
        this.miLog = miLog;

        // Cargar propiedades una única vez
        PropertiesManagerService props = PropertiesManagerServiceImpl.getInstance();

        switch (miLog.getLugarImportacion()) {
            case INTERNET -> {
                this.path = "";
                this.filename = "";
                this.url = props.getProperty(Constantes.FILTER_PROPERTIES, Constantes.CONFIG_URL);
            }
            case LOCAL -> {
                this.path = props.getProperty(Constantes.FILTER_PROPERTIES, Constantes.CONFIG_PATH);
                this.filename = props.getProperty(Constantes.FILTER_PROPERTIES, Constantes.CONFIG_FILENAME);
                this.url = "";
            }
        }

        this.filtroFechaInicioLectura = props.getProperty(Constantes.FILTER_PROPERTIES, Constantes.FILTRO_FECHAINICIALLECTURA);
        this.filtroFechaFinLectura = props.getProperty(Constantes.FILTER_PROPERTIES, Constantes.FILTRO_FECHAFINALLECTURA);
        this.filtroSql = props.getProperty(Constantes.FILTER_PROPERTIES, Constantes.FILTRO_SQL);
        this.filtroObjeto = props.getProperty(Constantes.FILTER_PROPERTIES, Constantes.FILTRO_OBJETO);
        this.filtroNuts = props.getProperty(Constantes.FILTER_PROPERTIES, Constantes.FILTRO_NUTS);
    }
}
