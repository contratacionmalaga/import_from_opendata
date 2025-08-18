package local.jarios.entity.auxiliares;

import jakarta.persistence.*;
import local.jarios.common.util.Constantes;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.EntryOpcion;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "historico")
public class Historico extends Auditable {

    // Primary Key
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Entry-related fields
    @Column(name = "feed_linkself", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String feedLinkSelf;

    @Column(name = "entry_id", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String entryId;

    @Column(name = "entry_link", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String entryLink;

    @Column(name = "entry_summary", columnDefinition = "TEXT")
    private String entrySummary;

    @Column(name = "entry_title", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
    private String entryTitle;

    @Column(name = "entry_updated")
    private LocalDateTime entryUpdated;

    @Enumerated(EnumType.STRING)
    @Column(name = "opcion")
    private EntryOpcion entryOpcion;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String entryMotivo;

    // Relationship
    @ManyToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "log_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_historico_milog",
                    foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE")
    )
    private Log miLog;

    // Constructor principal
    public Historico(Entry entry, EntryOpcion opcion, String motivo) {
        this.miLog = entry.getFeed().getMiLog();
        this.feedLinkSelf = entry.getFeed().getLinkSelf();
        this.entryId = entry.getIdEntry();
        this.entryLink = entry.getLink();
        this.entrySummary = entry.getSummary();
        this.entryTitle = entry.getTitle();
        this.entryUpdated = entry.getUpdated();
        this.entryOpcion = opcion;
        this.entryMotivo = motivo;
    }

    // String representaciones
    @Override
    public String toString() {
        return "Historico: [" +
                "entryId='" + entryId + "', " +
                "feedLinkSelf='" + feedLinkSelf + "', " +
                "entryUpdated='" + entryUpdated + "', " +
                "entryOpcion='" + entryOpcion + "', " +
                "entryMotivo='" + entryMotivo + "']";
    }
}
