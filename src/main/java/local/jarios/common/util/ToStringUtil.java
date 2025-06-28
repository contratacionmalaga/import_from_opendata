package local.jarios.common.util;

import java.lang.reflect.Field;

public class ToStringUtil {

    public static String autoToString(Object obj) {
        StringBuilder sb = new StringBuilder();
        sb.append(obj.getClass().getSimpleName()).append(": [");

        Field[] fields = obj.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            try {
                Object value = fields[i].get(obj);

                // Evitar recursión infinita si el campo es entidad relacionada
                // (por ejemplo, con anotaciones JPA o tipo conocido)
                if (value != null) {
                    String className = value.getClass().getName();

                    // Ejemplo: si el campo es una entidad JPA o paquete que causa ciclo
                    if (className.startsWith("local.jarios.entity") && value != obj) {
                        sb.append(fields[i].getName()).append("='").append(value.getClass().getSimpleName()).append(" (omitted)'");
                    } else {
                        sb.append(fields[i].getName()).append("='").append(value).append("'");
                    }
                } else {
                    sb.append(fields[i].getName()).append("='null'");
                }

            } catch (IllegalAccessException e) {
                sb.append(fields[i].getName()).append("='<access denied>'");
            }
            if (i < fields.length - 1) {
                sb.append(", ");
            }
        }

        sb.append("]");
        return sb.toString();
    }
}
