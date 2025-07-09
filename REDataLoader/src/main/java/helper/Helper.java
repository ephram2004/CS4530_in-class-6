package helper;

import java.sql.Date;

public class Helper {

    public static Integer parseIntSafe(String value) {
        try {
            return (value == null || value.isBlank()) ? null : Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double parseDoubleSafe(String value) {
        try {
            return (value == null || value.isBlank()) ? null : Double.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Date parseDateSafe(String value) {
        try {
            return (value == null || value.isBlank()) ? null : Date.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String parseStringSafe(String value) {
        try {
            return (value == null || value.isBlank()) ? null : value;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
