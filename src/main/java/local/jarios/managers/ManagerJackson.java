package local.jarios.managers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Clase de utilidad para la serialización de objetos Java a formato JSON
 * utilizando la librería <b>Jackson</b>.
 *
 * <p>Su principal propósito es ofrecer un método estático para convertir
 * cualquier objeto Java en una representación JSON con <i>pretty printing</i>
 * y soporte completo para clases de fecha y hora de Java 8
 * ({@link java.time.LocalDateTime}, {@link java.time.LocalDate}, etc.).</p>
 *
 * <h2>Características principales:</h2>
 * <ul>
 *   <li>Registro automático del módulo {@link JavaTimeModule} para manejar correctamente
 *       las clases de fecha y hora modernas.</li>
 *   <li>Salida con formato legible (activación de {@link SerializationFeature#INDENT_OUTPUT}).</li>
 *   <li>Evita errores de serialización en objetos sin propiedades
 *       ({@link SerializationFeature#FAIL_ON_EMPTY_BEANS} = false).</li>
 *   <li>Exclusión de valores {@code null} en la salida JSON
 *       mediante {@link JsonInclude.Include#NON_NULL}.</li>
 * </ul>
 *
 * <p>Esta clase es <b>final</b> y tiene un constructor privado para impedir su instanciación,
 * promoviendo así su uso exclusivamente como utilidad estática.</p>
 *
 * <h2>Ejemplo de uso:</h2>
 * <pre>{@code
 * Persona persona = new Persona("Juan", 30, null);
 * String json = ManagerJackson.objectToJsonPretty(persona);
 *
 * // Resultado:
 * {
 *   "nombre" : "Juan",
 *   "edad" : 30
 * }
 * }</pre>
 *
 * @author Juan Antonio
 * @since 2024
 */
public final class ManagerJackson {

    /** Instancia de {@link ObjectMapper} configurada de forma global. */
    private static final ObjectMapper objectMapper;

    // Bloque de inicialización estático
    static {
        objectMapper = new ObjectMapper();

        // Registro del módulo para manejar java.time.LocalDateTime, LocalDate, etc.
        objectMapper.registerModule(new JavaTimeModule());

        // Configuración de impresión bonita (pretty printing)
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // No fallar si encuentra objetos vacíos (sin propiedades)
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // No incluir valores nulos en el JSON
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * Constructor privado para evitar instanciación de la clase de utilidad.
     */
    private ManagerJackson() { }

    /**
     * Convierte un objeto Java a una cadena JSON con formato legible (<i>pretty print</i>).
     *
     * <p>Si ocurre un error durante el proceso de serialización, se lanza una
     * {@link RuntimeException} con el detalle de la causa.</p>
     *
     * @param objeto Objeto Java a serializar.
     * @return Cadena JSON con formato legible.
     * @throws RuntimeException si ocurre un error durante la serialización.
     */
    public static String objectToJsonPretty(Object objeto) {
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar objeto a JSON", e);
        }
    }
}
