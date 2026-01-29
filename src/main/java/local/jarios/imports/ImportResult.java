package local.jarios.imports;

import java.sql.Date;
import java.util.List;

/**
 * Clase que encapsula el resultado de la importación: la lista de entidades y la fecha de
 * generación del fichero.
 *
 * @param <T>             tipo de entidad contenida en el resultado
 * @param lista           lista de entidades importadas
 * @param fechaGeneracion fecha y hora de generación del fichero
 */
public record ImportResult<T>(List<T> lista, Date fechaGeneracion) {
}
