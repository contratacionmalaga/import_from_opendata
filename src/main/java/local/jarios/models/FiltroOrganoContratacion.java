package local.jarios.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class FiltroOrganoContratacion {

    private String idPlataforma;
    private String nombreOrganoContratacion;

    public String toString() {
        return idPlataforma + "; " + nombreOrganoContratacion;
    }
}
