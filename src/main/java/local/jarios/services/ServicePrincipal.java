package local.jarios.services;

import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.exceptions.MiServiceException;

import java.util.Map;

/**
 * Interfaz que define los métodos principales para la persistencia y recuperación
 * de información relacionada con la importación de datos abiertos.
 */
public interface ServicePrincipal {

  /**
   * Persiste un objeto de log en el sistema.
   *
   * @param miLog el objeto de log que se desea almacenar.
   */
  void persistirEnBaseDatos(Log miLog) throws MiServiceException;

  /**
   * Obtiene el feed más reciente correspondiente a un tipo específico de sindicación.
   *
   * @return el feed más reciente disponible para el tipo indicado.
   */
  Map<String, Entry> getMapEntriesEnBaseDatos() throws MiServiceException;
}

