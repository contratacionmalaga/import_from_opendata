package local.jarios.enums;

import lombok.Getter;

/** Tipo de documento. */
@Getter
public enum TipoDocumento {

  /** Documento PCAP */
  PCAP,

  /** Documento PPT */
  PPT,

  /** Documento ADDICIONAL */
  ADDICIONAL,

  /** Documento GENERAL */
  GENERAL,

  /** Documento UNKNOWN */
  UNKNOWN;

  /** Constructor de la clase */
  TipoDocumento() {
    // VACÍO
  }
}
