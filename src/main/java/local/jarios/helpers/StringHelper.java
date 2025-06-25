package local.jarios.helpers;

import local.jarios.common.util.Constantes;
import org.apache.commons.text.StringEscapeUtils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author Juan Antonio
 */
public final class StringHelper {

    // Definimos el patrón como static final para evitar compilarlo repetidamente
    private static final Pattern PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private StringHelper() { }

    public static String eliminarCaracteres(String cadena) {

        //  Decodifico la cadena
        String cadenaDecodificada = StringEscapeUtils.unescapeHtml4(cadena);

        String resultado = cadenaDecodificada.replace(
                Constantes.RETORNO_CARRO_HTML,
                Constantes.CADENA_VACIA);

        // Elimino los retornos de carro
        resultado = resultado.replace(
                Constantes.RETORNO_CARRO,
                Constantes.CADENA_VACIA);

        // Elimino los saltos de línea
        resultado = resultado.replace(
                Constantes.SALTO_LINEA,
                Constantes.CADENA_VACIA);

        return resultado;

    }
    public static boolean contieneCadena(String cadenaPrincipal, String cadenaBuscada) {

        // Normalizar las cadenas y convertir a minúsculas
        String cadenaPrincipalNormalizada = normalizarCadena(cadenaPrincipal);
        String cadenaBuscadaNormalizada = normalizarCadena(cadenaBuscada);

        // Verificar si la cadenaBuscada está contenida en la cadenaPrincipal
        return cadenaPrincipalNormalizada.contains(cadenaBuscadaNormalizada);
    }

    private static String normalizarCadena(String cadena) {

        if (cadena == null) {
            return "";
        }

        // Convertir la cadena a minúsculas y eliminar acentos
        String sinAcentos = Normalizer.normalize(cadena, Normalizer.Form.NFD);

        // Utilizamos Locale.ROOT para evitar variaciones de configuración regional
        return PATTERN.matcher(sinAcentos).replaceAll("").toLowerCase(Locale.ROOT);
    }

    /**
     * Función encargada de limitar el mensaje que se muestra en una línea de log
     * @param mensaje String que se limitará
     * @return String con el valor truncado
     */
    public static String limitarLineaLog(String mensaje) {

        //
        final var TAMANO_MAXIMO_LINEA_LOG = 50;

        if ((mensaje == null) || (mensaje.isEmpty())) {

            return "";

        } else {

            //
            if (mensaje.length() > TAMANO_MAXIMO_LINEA_LOG) {

                //
                mensaje = mensaje.substring(0, TAMANO_MAXIMO_LINEA_LOG - 1);
            }

            //
            return mensaje;

        }
    }
}
