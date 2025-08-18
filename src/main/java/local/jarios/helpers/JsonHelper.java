package local.jarios.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Clase de utilidad para la conversión de objetos Java a su representación en formato JSON,
 * utilizando la librería <b>Jackson</b>.
 *
 * <p>Proporciona métodos estáticos que permiten serializar tanto mapas de cadenas como objetos
 * genéricos a JSON, con soporte adicional para clases de fecha y hora de Java 8
 * ({@link java.time.LocalDateTime}, {@link java.time.LocalDate}, etc.).</p>
 *
 * <p>Esta clase es <b>final</b> y posee un constructor privado para evitar su instanciación,
 * promoviendo así su uso exclusivamente como utilidad estática.</p>
 *
 * <h2>Características principales:</h2>
 * <ul>
 *   <li>Soporte para <b>JavaTime</b> a través del registro del módulo {@link JavaTimeModule}.</li>
 *   <li>Serialización de fechas en formato legible (no como <i>timestamps</i>).</li>
 *   <li>Manejo seguro de errores: en caso de problemas de serialización, retorna un JSON vacío.</li>
 *   <li>Compatibilidad con <i>pretty printing</i> para generar salidas legibles.</li>
 * </ul>
 *
 * <p><b>Ejemplo de uso:</b></p>
 *
 * <pre>{@code
 * Map<String, String> datos = Map.of("usuario", "Juan", "rol", "ADMIN");
 * String json = JsonHelper.serializeMapToJson(datos);
 *
 * Persona persona = new Persona("Ana", 25);
 * String jsonPretty = JsonHelper.toJsonPretty(persona);
 * }</pre>
 *
 * @author Juan Antonio
 * @since 2024
 */
@Slf4j
public final class JsonHelper {

    /**
     * Instancia singleton de {@link ObjectMapper} configurada para toda la clase.
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    // Bloque estático de configuración inicial
    static {
        // Registro del módulo para soporte de clases de fecha y hora de Java 8
        mapper.registerModule(new JavaTimeModule());
        // Evita la serialización de fechas como números (timestamps)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Constructor privado para evitar instanciación de la clase de utilidad.
     */
    private JsonHelper() {
    }

    /**
     * Convierte un {@link Map} de cadenas clave-valor en una cadena JSON.
     *
     * <p>Si el mapa proporcionado es {@code null}, devuelve la constante
     * {@link Constantes#JSON_VACIO}. En caso de error durante la serialización,
     * el error se registra en el log y también se devuelve un JSON vacío.</p>
     *
     * @param map Mapa de cadenas a serializar en formato JSON.
     * @return Cadena JSON resultante o {@link Constantes#JSON_VACIO} en caso de error o {@code null}.
     */
    public static String serializeMapToJson(Map<String, String> map) {
        if (map == null) {
            return Constantes.JSON_VACIO;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException ex) {
            log.error("Error serializando el mapa a JSON", ex);
            return Constantes.JSON_VACIO;
        }
    }

    /**
     * Serializa un objeto genérico a una cadena JSON con formato legible (<i>pretty printing</i>).
     *
     * <p>Utiliza el {@link ObjectMapper} configurado en esta clase con soporte para
     * {@link JavaTimeModule} y formato de fecha legible.</p>
     *
     * @param obj Objeto a serializar.
     * @return Cadena JSON con formato legible.
     * @throws RuntimeException si ocurre un error durante la serialización.
     */
    public static String toJsonPretty(Object obj) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando a JSON", e);
        }
    }
}
