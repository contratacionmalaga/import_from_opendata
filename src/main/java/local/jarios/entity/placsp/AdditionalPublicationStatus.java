package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa el estado adicional de publicación en el proceso de importación de ficheros Excel desde Internet.
 * <p>
 * Esta entidad contiene información sobre el medio de publicación y mantiene las relaciones con la información
 * del aviso relacionado, así como con las solicitudes y referencias de documentos de publicación asociadas.
 * </p>
 * <p>
 * Hereda campos auditables comunes de {@link Auditable}.
 * </p>
 *
 * @author Juan Antonio
 * @since 04/06/2024
 */
@Setter
@Getter
@Entity
@Table(name = "additional_publication_status")
public class AdditionalPublicationStatus extends Auditable {

    /**
     * Constructor por defecto.
     * <p>
     * Requerido por JPA para la correcta creación de proxies
     * y por Lombok para la inicialización básica.
     * </p>
     */
    public AdditionalPublicationStatus() {
        // Constructor vacío requerido por JPA
    }

    /**
     * Identificador único universal (UUID) del estado de publicación.
     * <p>
     * Clave primaria, generado automáticamente.
     * No puede ser actualizado ni nulo.
     * </p>
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Nombre del medio de publicación.
     * <p>
     * Cadena con longitud máxima definida por {@link Constantes#TAMANO_MAXIMO_CAMPO_500}.
     * </p>
     */
    @Column(name = "publication_media_name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String publicationMediaName;

    /**
     * Información del aviso asociado a este estado de publicación.
     * <p>
     * Relación muchos a uno con {@link NoticeInfo}.
     * La carga es perezosa y se propaga en cascada todas las operaciones.
     * La eliminación en cascada se asegura con la definición de clave foránea.
     * </p>
     */
    @ManyToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "notice_info_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_additionalpublicationstatus_noticeinfo",
                    foreignKeyDefinition = "FOREIGN KEY (notice_info_id) REFERENCES notice_info(id) ON DELETE CASCADE"))
    private NoticeInfo noticeInfo;

    /**
     * Lista de solicitudes de publicación adicionales asociadas a este estado.
     * <p>
     * Relación uno a muchos con {@link AdditionalPublicationRequest}.
     * Operaciones en cascada para todas las acciones, y eliminación en cascada (orphan removal) al desvincular.
     * </p>
     */
    @OneToMany(mappedBy = "additionalPublicationStatus", cascade = CascadeType.MERGE, orphanRemoval = true)
    private List<AdditionalPublicationRequest> additionalPublicationRequestList = new ArrayList<>();

    /**
     * Lista de referencias de documentos de publicación adicionales asociadas a este estado.
     * <p>
     * Relación uno a muchos con {@link AdditionalPublicationDocumentReference}.
     * Operaciones en cascada para todas las acciones, y eliminación en cascada (orphan removal) al desvincular.
     * </p>
     */
    @OneToMany(mappedBy = "additionalPublicationStatus", cascade = CascadeType.MERGE, orphanRemoval = true)
    private List<AdditionalPublicationDocumentReference> additionalPublicationDocumentReferenceList = new ArrayList<>();

    /**
     * Representación textual de la entidad.
     * <p>
     * Devuelve una cadena con el nombre del medio de publicación para facilitar
     * la depuración y el registro en logs.
     * </p>
     *
     * @return Cadena con el campo {@code publicationMediaName}.
     */
    @Override
    public String toString() {
        return "AdditionalPublicationStatus: " +
                "[publicationMediaName='" + publicationMediaName + "']";
    }
}
