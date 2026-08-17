package local.jarios.interfaces;

import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.auxiliares.OrganoContratacion;

/**
 * Interfaz genérica que define el contrato para entidades actualizables.
 *
 * <p>Las entidades que implementen esta interfaz deben proporcionar un identificador único, un
 * método para actualizar sus datos a partir de otra instancia del mismo tipo, así como la gestión
 * de un registro de log para trazabilidad.
 *
 * @author Juan
 * @since 01/03/2025
 */
public interface EsActualizable {

  /**
   * Actualiza los atributos del objeto actual usando los valores de otro objeto del mismo tipo.
   *
   * <p>Este método no debe modificar campos inmutables como el identificador o claves únicas.
   *
   * @param otro Objeto desde el cual se copiarán los valores para actualizar el actual.
   */
  void actualizarCon(OrganoContratacion otro);

  /**
   * Asocia un objeto de tipo {@link Log} a esta entidad para fines de trazabilidad.
   *
   * @param miLog El objeto de log a asociar.
   */
  void setMiLog(Log miLog);
}
