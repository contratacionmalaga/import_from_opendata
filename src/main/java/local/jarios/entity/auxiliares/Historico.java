package local.jarios.entity.auxiliares;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import local.jarios.common.util.Constantes;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.EntryOpcion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;


@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "historico"
)
@Slf4j
public class Historico extends Auditable {

    //
    //
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

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
                    name = "fk_historico_log",
                    foreignKeyDefinition = "FOREIGN KEY (log_id) REFERENCES log(id) ON DELETE CASCADE"))
    private Log miLog;

    //
    //
    //
    public Historico(Entry entry, EntryOpcion opcion, String motivo) {

        this.id = Generators.timeBasedEpochGenerator().generate();
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

    @Override
    public String toString() {
        return "Historico: [id='" + id + "', '" +
                    "miLog='" + miLog + "', '" +
                    "feedLinkSelf='" + feedLinkSelf + "', '" +
                    "entryId='" + entryId + "', '" +
                    "entryLink='" + entryLink + "', '" +
                    "entrySummary='" + entrySummary + "', '" +
                    "entryTitle='" + entryTitle + "', '" +
                    "entryUpdated='" + entryUpdated + "', '" +
                    "entryOpcion='" + entryOpcion + "', '" +
                    "entryMotivo='" + entryMotivo + "]'";
    }
}
