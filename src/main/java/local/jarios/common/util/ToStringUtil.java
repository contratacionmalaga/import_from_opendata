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
                sb.append(fields[i].getName()).append("='").append(value).append("'");
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
