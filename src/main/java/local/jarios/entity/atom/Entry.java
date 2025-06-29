package local.jarios.entity.atom;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.common.util.Constantes;
import local.jarios.interfaces.HasIdEntry;
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
        name = "entry",
        indexes = {
                @Index(name = "idx_unique_entry", columnList = "id_entry", unique = true)
        }
)

public class Entry extends Auditable implements HasIdEntry<Entry> {

    //
    //
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private UUID id;

    @Column(name = "id_entry", nullable = false, unique = true, length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String idEntry;

    @Column(name = "link", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String link;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "title", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
    private String title;

    @Column(name = "updated")
    private LocalDateTime updated;

    //
    //
    //
    @ManyToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "feed_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_entry_feed",
                    foreignKeyDefinition = "FOREIGN KEY (feed_id) REFERENCES feed(id) ON DELETE CASCADE"))
    private Feed feed;

    //
    //
    //
    @OneToMany(mappedBy = "entry",  cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ContractFolderStatus> listContractFolderStatus = new ArrayList<>();

    @Override
    public String toString() {

        return "Entry: " +
                "[idEntry='" + idEntry + "', " +
                "link='" + link + "', " +
                "summary='" + summary + "', " +
                "title='" + title + "', " +
                "updated='" + updated + "]'";
    }

    //
    //
    //
    public Entry() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
