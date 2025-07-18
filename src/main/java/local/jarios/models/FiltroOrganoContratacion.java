package local.jarios.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Modelo de filtro para identificar un órgano de contratación.
 * <p>
 * Contiene el identificador de la plataforma y el nombre del órgano de contratación.
 * Utilizado principalmente para búsquedas o filtrados en la aplicación.
 * </p>
 *
 * <p><b>Autor:</b> Juan Antonio</p>
 * <p><b>Fecha:</b> [Fecha actual o de creación]</p>
 */
@Getter
@Setter
@AllArgsConstructor
public final class FiltroOrganoContratacion {

    /**
     * Identificador único de la plataforma asociada al órgano de contratación.
     */
    private String idPlataforma;

    /**
     * Nombre descriptivo del órgano de contratación.
     */
    private String nombreOrganoContratacion;

    /**
     * Representación en texto del filtro, combinando idPlataforma y nombreOrganoContratacion,
     * separados por punto y coma.
     *
     * @return Cadena con formato "idPlataforma; nombreOrganoContratacion".
     */
    @Override
    public String toString() {
        return idPlataforma + "; " + nombreOrganoContratacion;
    }
}
