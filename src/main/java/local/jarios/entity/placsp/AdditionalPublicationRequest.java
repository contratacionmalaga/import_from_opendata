package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa una solicitud adicional de publicación relacionada con
 * la importación de ficheros Excel desde Internet.
 * <p>
 * Esta entidad persiste la información sobre la agencia solicitante,
 * la fecha y hora de envío, y su estado actual dentro del proceso
 * de publicación adicional.
 * </p>
 * <p>
 * Hereda campos auditables comunes a todas las entidades que
 * implementan {@link Auditable}.
 * </p>
 *
 * @author Juan Antonio
 * @since 04/06/2024
 */
@Setter
@Getter
@Entity
@Table(name = "additional_publication_request")
public class AdditionalPublicationRequest extends Auditable {

    /**
     * Constructor por defecto.
     * <p>
     * Requerido por JPA para la correcta creación de proxies
     * y por Lombok para la inicialización básica.
     * </p>
     */
    public AdditionalPublicationRequest() {
        // Constructor vacío requerido por JPA
    }

    /**
     * Identificador único universal (UUID) de la solicitud.
     * <p>
     * Clave primaria, generado automáticamente.
     * No se puede actualizar ni ser nulo.
     * </p>
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Identificador de la agencia que realiza la solicitud.
     * <p>
     * Cadena con longitud máxima definida por la constante
     * {@link Constantes#TAMANO_MAXIMO_CAMPO_500}.
     * </p>
     */
    @Column(name = "agency_id", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String agencyId;

    /**
     * Fecha y hora en que se envió la solicitud.
     * <p>
     * Representado como un objeto {@link LocalDateTime}.
     * </p>
     */
    @Column(name = "send_date_time")
    private LocalDateTime sendDateTime;

    /**
     * Estado actual de la solicitud.
     * <p>
     * Relación muchos a uno con la entidad {@link AdditionalPublicationStatus}.
     * La carga se realiza de forma perezosa (lazy) y con cascada completa (ALL).
     * Al eliminar esta entidad, el estado asociado también será eliminado en cascada.
     * </p>
     */
    @ManyToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "additional_publication_status_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_additionalpublicationrequest_additionalpublicationstatus",
                    foreignKeyDefinition =
                            "FOREIGN KEY (additional_publication_status_id) " +
                            "REFERENCES additional_publication_status(id) ON DELETE CASCADE"))
    private AdditionalPublicationStatus additionalPublicationStatus;

    /**
     * Representación textual de la entidad.
     * <p>
     * Devuelve una cadena con los valores más relevantes para facilitar
     * la depuración y el registro en logs.
     * </p>
     *
     * @return Cadena con los campos {@code agencyId} y {@code sendDateTime}.
     */
    @Override
    public String toString() {
        return "AdditionalPublicationRequest: " +
                "[agencyId='" + agencyId + "', " +
                "sendDateTime='" + sendDateTime + "']";
    }
}
