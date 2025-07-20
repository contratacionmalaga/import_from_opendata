package local.jarios;

import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiParseException;
import local.jarios.helpers.FeedHelper;
import local.jarios.helpers.TipoSindicacionHelper;

/**
 * Implementación concreta del proceso de importación de datos para fuentes locales.
 * <p>
 * Esta clase extiende {@link AbstractOpenData} y define el comportamiento específico
 * para la sindicación de datos desde fuentes locales. Sobrescribe los métodos necesarios
 * para identificar el tipo de sindicación, el lugar de importación, y cómo se parsean
 * los feeds.
 * </p>
 *
 * <p>
 * El punto de entrada {@code main} permite ejecutar el proceso completo llamando a
 * {@link AbstractOpenData#procesar()}.
 * </p>
 *
 * @author Juan Antonio
 * @see AbstractOpenData
 * @see FeedHelper
 * @see TipoSindicacionHelper
 */
public class OpenDataLocal extends AbstractOpenData {

    /**
     * Obtiene el tipo de sindicación correspondiente a fuentes locales.
     *
     * @return el tipo de sindicación local definido en {@link TipoSindicacionHelper}.
     */
    @Override
    protected TipoSindicacion getTipoSindicacion() {

        //
        TipoSindicacionHelper tipoSindicacionHelper = new TipoSindicacionHelper();

        //
        return tipoSindicacionHelper.getTipoSindicacionLocal();
    }

    /**
     * Indica que el lugar de importación de esta clase es {@link LugarImportacion#LOCAL}.
     *
     * @return el valor {@code LugarImportacion.LOCAL}.
     */
    @Override
    protected LugarImportacion getLugarImportacion() {

        //
        return LugarImportacion.LOCAL;
    }

    /**
     * Realiza el parsing de los feeds Atom obtenidos localmente y actualiza la estadística.
     *
     * @param tipoSindicacion objeto que representa el contexto de la importación actual.
     * @param newestEntry objeto para registrar métricas del proceso.
     * @throws MiParseException si ocurre un error durante el parsing de los feeds.
     */
    @Override
    protected void parsearAtomsFeeds(Log miLog, TipoSindicacion tipoSindicacion, Entry newestEntry)
            throws MiParseException {

        FeedHelper.parsearFeedsDesdeLocal(miLog, tipoSindicacion);
    }

    /**
     * Punto de entrada para ejecutar el proceso completo de importación desde fuentes locales.
     * Crea una instancia de esta clase y llama al método {@code procesar()} heredado.
     *
     * @param args argumentos de la línea de comandos (no se utilizan).
     */
    public static void main(String[] args) {
        new OpenDataLocal().procesar();
    }
}
