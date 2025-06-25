package local.jarios.helpers;

import local.jarios.exceptions.MiUnknownHostException;
import local.jarios.managers.ManagerGsons;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * @author Juan Antonio
 */
@Slf4j
public final class ComunHelper {

    /**
     * CONSTRUCTOR PRIVADO DE LA CLASE PUESTO QUE ESTA FINAL
     */
    private ComunHelper() { }

    /**
     * Devuelve el nombre del equipo que está ejecutando el código
     * @return String con el nombre del Equipo
     * @throws MiUnknownHostException Excepción en caso de no poder acceder
     */
    public static String getHostName () throws MiUnknownHostException {

        //
        try {
            //
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            //
            log.info(ex.getMessage());
            //
            throw new MiUnknownHostException(ex);
        }
    }

    public static String limitarRegistro(String campo, int tamanoMaximo) {

        //
        if (campo == null || campo.isBlank()) {

            //
            return Constantes.CADENA_VACIA;

        } else {

            //
            if (campo.length() > tamanoMaximo) {

                //
                campo = campo.substring(0, tamanoMaximo);
            }

            //
            return campo;
        }
    }

    /**
     * Función encargada de imprimir un objeto
     *
     * @param object El objeto que voy a imprimir
     */
    public static void imprimir(Object object) {

        // Imprimiendo el objeto
        Arrays
                .stream(
                        ManagerGsons
                                .objectToJsonPretty(object, true)
                                .split(Constantes.CR))
                .forEach(log::info);
    }

    /**
     * Método que devuelve un String con el formato de duración establecido
     * @param fechaHoraInicial Timestamp con la fecha inicial
     * @param fechaHoraFinal Timestamp con la fecha final
     * @return Cadena de texto con la duración en el formato establecido
     */
    public static String calcularTiempoEjecucion(LocalDateTime fechaHoraInicial, LocalDateTime fechaHoraFinal) {

        // Defino las variables locales y le asigno los valores que utilizaré
        int milesimas = 1000;
        int minutos = 60;
        int segundos = 60;
        String formatoDuracion = "%sh %sm %ss %sml";

        // Calculamos la diferencia en milisegundos
        long diffInMillis = fechaHoraFinal.getTime() - fechaHoraInicial.getTime();

        // Calculamos las horas, minutos, segundos y milisegundos
        long hours = diffInMillis / (milesimas * segundos * minutos);
        long minutes = (diffInMillis % (milesimas * segundos * minutos)) / (milesimas * segundos);
        long seconds = (diffInMillis % (milesimas * segundos)) / milesimas;
        long milliseconds = diffInMillis % milesimas;

        // Devolvemos el tiempo transcurrido en formato "hh:mm:ss:SSS"
        return String.format(formatoDuracion, hours, minutes, seconds, milliseconds);
    }

    public static String getFechaHoraFormateada(LocalDateTime fechaHora) {

        // Usar LocalDateTime.now() si el timestamp es null
        LocalDateTime fecha = (fechaHora != null) ? fechaHora.toLocalDateTime() : LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return fecha.format(formatter);
    }
}
