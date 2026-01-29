package local.jarios.interfaces;

import local.jarios.entity.auxiliares.OrganoContratacion;

/**
 * Interfaz que indica que una entidad puede ser comparada por su contenido (sin contar su ID).
 */
public interface ComparaContenido {

  /**
   * Compara dos objetos del tipo OrganoContratacion por contenido.
   *
   * @param other Objeto con el que realiza la comparación
   * @return booleano indicando si son iguales los objetos
   */
  boolean equalsPorContenido(OrganoContratacion other);
}
