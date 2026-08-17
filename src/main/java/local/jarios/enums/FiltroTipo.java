package local.jarios.enums;

import local.jarios.common.util.Mensajes;
import lombok.Getter;

/** Description: Author: juan Date: 13/10/2025 Team: */
@Getter
public enum FiltroTipo {
  FECHAS(Mensajes.ENTRY_NO_FILTRO_FECHAS),
  NUTS(Mensajes.ENTRY_NO_FILTRO_NUTS),
  OBJETO(Mensajes.ENTRY_NO_FILTRO_OBJETO),
  ORGANOS_CONTRATACION(Mensajes.ENTRY_NO_FILTRO_ORGANOS_CONTRATACION);

  private final String mensajeError;

  FiltroTipo(String mensajeError) {
    this.mensajeError = mensajeError;
  }
}
