package local.jarios.entity.auxiliares;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import local.jarios.common.util.TamanoCampos;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Description: Author: juan Date: 16/03/2025 Team:
 */
@Setter
@Getter
@NoArgsConstructor
@Table(
    name = "organo_contratacion"
)
public class Provincia {

  @Column(name = "provincia", nullable = false, length = TamanoCampos.TAMANO_50)
  private String provincia;

  @Column(name = "codigo_ine", nullable = false, length = TamanoCampos.TAMANO_15)
  private String codigo_ine;

  @Column(name = "codigo_nuts", nullable = false, length = TamanoCampos.TAMANO_15)
  private String codigo_nuts;
  //
  //
  //
  public Provincia(String provincia, String codigo_ine, String codigo_nuts) {

    this.provincia = provincia;
    this.codigo_ine = codigo_ine;
    this.codigo_nuts = codigo_nuts;
  }

  @Override
  public String toString() {

    return "Provincia: [" +
        "provincia='" + provincia + "', " +
        "codigo_ine='" + codigo_ine + "', " +
        "codigo_nuts='" + codigo_nuts + "']";
  }
}

