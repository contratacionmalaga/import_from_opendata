package local.jarios.entity.auxiliares;

import jakarta.persistence.*;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
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
        name = "configuracion"
)
@Slf4j
public class Configuracion extends Auditable {

    //
    //
    //
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "path", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String path;

    @Column(name = "filename", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String filename;

    @Column(name = "url", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
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

        this.filtroFechaInicioLectura = props.getProperty(PropertiesFiles.FILTER, PropertiesKeys.FILTER_FECHAINICIALLECTURA);
        this.filtroFechaFinLectura = props.getProperty(PropertiesFiles.FILTER, PropertiesKeys.FILTER_FECHAFINALLECTURA);
        this.filtroSql = props.getProperty(PropertiesFiles.FILTER, PropertiesKeys.FILTER_SQL);
        this.filtroObjeto = props.getProperty(PropertiesFiles.FILTER, PropertiesKeys.FILTER_OBJETO);
        this.filtroNuts = props.getProperty(PropertiesFiles.FILTER, PropertiesKeys.FILTER_NUTS);
    }

    @Override
    public String toString() {

        return "Configuración: [" +
                "path='" + path + "', " +
                "filename='" + filename + "', " +
                "url='" + url + "', " +
                "filtroFechaInicioLectura='" + filtroFechaInicioLectura + "', " +
                "filtroFechaFinLectura='" + filtroFechaFinLectura + "', " +
                "filtroSql='" + filtroSql + "', " +
                "filtroObjeto='" + filtroObjeto + "', " +
                "filtroNuts='" + filtroNuts + "']";

    }
}
