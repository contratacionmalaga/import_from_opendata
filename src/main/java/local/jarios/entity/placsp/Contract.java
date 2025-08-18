package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad que representa un <b>contrato formalizado</b> dentro del sistema PLACSP.
 * <p>
 * Permite registrar la información básica del contrato como su identificador,
 * la fecha de emisión y la relación con el {@link TenderResult} del que se origina.
 * </p>
 *
 * <p>
 * Se almacena en la tabla <b>contract</b> y hereda de {@link Auditable},
 * aportando campos de auditoría (creación, modificación, usuario, etc.).
 * </p>
 *
 * <h2>Norma de referencia</h2>
 * <ul>
 *     <li><b>4.53 Contrato</b>: representa el objeto del contrato resultante
 *     del procedimiento de licitación, conteniendo su identificación y fecha
 *     de formalización.</li>
 * </ul>
 *
 * @author Juan
 * @version 1.0
 * @since 06/07/2024
 */
@Setter
@Getter
@Entity
@Table(name = "contract")
public class Contract extends Auditable {

    /**
     * Constructor por defecto.
     * <p>
     * Requerido por JPA para la correcta creación de proxies
     * y por Lombok para la inicialización básica.
     * </p>
     */
    public Contract() {
        // Constructor vacío requerido por JPA
    }

    // =========================================================================
    // PROPIEDADES DE LA ENTIDAD
    // =========================================================================

    /**
     * Identificador único de la entidad Contract.
     * Generado automáticamente como UUID.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Identificador externo del contrato.
     * <p>
     * Longitud máxima: {@link Constantes#TAMANO_MAXIMO_CAMPO_50}.
     * </p>
     */
    @Column(name = "id_contract", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String idContract;

    /**
     * Fecha de emisión o formalización del contrato.
     */
    @Column(name = "issue_date")
    private LocalDate issueDate;

    // =========================================================================
    // RELACIONES CON ENTIDADES PADRES
    // =========================================================================

    /**
     * Relación uno a uno con el resultado de la licitación
     * ({@link TenderResult}) del que deriva este contrato.
     * <p>
     * Eliminación en cascada: si se elimina el tender result asociado,
     * también se elimina el contrato.
     * </p>
     */
    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tender_result_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_contract_tenderresult",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tender_result_id) " +
                                    "REFERENCES tender_result(id) ON DELETE CASCADE"))
    private TenderResult tenderResult;

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    /**
     * Representación textual simplificada de la entidad.
     *
     * @return cadena con el identificador del contrato y su fecha de emisión.
     */
    @Override
    public String toString() {
        return "Contract: " +
                "[idContract='" + idContract + "', " +
                "issueDate='" + issueDate + "']";
    }
}
