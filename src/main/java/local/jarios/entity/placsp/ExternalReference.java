package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Getter
@Setter
@Entity
@Table(
        name = "external_reference"
)

public class ExternalReference extends Auditable {

    //
    //
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "uri", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
    private String uri;

    @Column(name = "document_hash", length = Constantes.TAMANO_MAXIMO_CAMPO_250)
    private String documentHash;

    @Column(name = "file_name", length = Constantes.TAMANO_MAXIMO_CAMPO_250)
    private String fileName;

    //
    //
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "attachment_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_externalreference_attachment",
                    foreignKeyDefinition = "FOREIGN KEY (attachment_id) REFERENCES attachment(id) ON DELETE CASCADE"))
    private Attachment attachment;

    @Override
    public String toString() {

        return ToStringUtil.autoToString(this);
    }

    //
    //
    //
    public ExternalReference() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}

