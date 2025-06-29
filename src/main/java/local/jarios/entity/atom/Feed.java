package local.jarios.entity.atom;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.Log;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Description: Importaciones de Ficheros Excel desde Internet
 * Author: Juan Antonio
 * Date: 04/06/2024
 * Team: Juan Antonio
 */

@Setter
@Getter
@Entity
@Table(
        name = "feed"
)

public class Feed extends Auditable {

    //
    //
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "link_first", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
    private String linkFirst;

    @Column(name = "link_prev", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
    private String linkPrev;

    @Column(name = "link_self", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
    private String linkSelf;

    @Column(name = "link_next", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
    private String linkNext;

    @Column(name = "updated")
    private LocalDateTime updated;

    @Override
    public String toString() {

        return "Feed: " +
                "[linkFirst='" + linkFirst + "', " +
                "linkPrev='" + linkPrev + "', " +
                "linkSelf='" + linkSelf + "', " +
                "linkNext='" + linkNext + "', " +
                "updated='" + updated + "]'";
    }
    //
    //
    //
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "log_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_feed_log",
                    foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE"))
    private Log miLog;

    //
    //
    //
    @OneToMany(mappedBy = "feed", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Entry> listEntry = new ArrayList<>();

    //
    //
    //
    public Feed() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
