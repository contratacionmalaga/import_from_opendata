package local.jarios.entity.atom;

import jakarta.persistence.*;
import local.jarios.common.util.Constantes;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.interfaces.HasIdEntry;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "entry",
        indexes = {
                @Index(name = "idx_unique_entry", columnList = "id_entry", unique = true)
        }
)
public class Entry extends Auditable implements HasIdEntry<Entry> {

    // Primary Key
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Campos de entrada
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

    // Relaciones
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "feed_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_entry_feed",
                    foreignKeyDefinition = "FOREIGN KEY (feed_id) REFERENCES feed(id) ON DELETE CASCADE")
    )
    private Feed feed;

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ContractFolderStatus> listContractFolderStatus = new ArrayList<>();

    // Representaciones en texto
    @Override
    public String toString() {
        return "Entry: [" +
                "idEntry='" + idEntry + "', " +
                "link='" + link + "', " +
                "summary='" + summary + "', " +
                "title='" + title + "', " +
                "updated='" + updated + "']";
    }

    public String toStringResumido() {
        return "[idEntry='" + idEntry + "', " +
                "updated='" + updated + "', " +
                "id='" + id + "']";
    }
}
