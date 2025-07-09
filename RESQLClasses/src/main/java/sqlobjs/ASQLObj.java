package sqlobjs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import helpers.HelperSQL;

public abstract class ASQLObj {

    protected static Map<String, Object> attributes;

    public ASQLObj(JsonNode json) {
        this.attributes = new HashMap<>();
        json.fieldNames().forEachRemaining(field
                -> attributes.put(field, json.get(field))
        );
        populateAttrsFromJSONNode(json);
    }

    private void populateAttrsFromJSONNode(JsonNode attributes) {
        Class<?> cls = this.getClass();

        for (Field field : cls.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            String fieldName = HelperSQL.camelToSnake(field.getName());
            JsonNode valueNode = attributes.get(fieldName);

            if (valueNode == null || valueNode.isNull()) {
                continue;
            }

            String rawValue = valueNode.asText();

            field.setAccessible(true);
            Class<?> type = field.getType();

            try {
                if (rawValue.isBlank() || rawValue.equalsIgnoreCase("null")) {
                    continue;
                }

                if (type == String.class) {
                    field.set(this, rawValue);
                } else if (type == int.class || type == Integer.class) {
                    field.set(this, Integer.valueOf(rawValue));
                } else if (type == double.class || type == Double.class) {
                    field.set(this, Double.valueOf(rawValue));
                } else if (type == Date.class) {
                    System.out.println("DATE INSERTED: " + rawValue);

                    field.set(this, Date.valueOf(rawValue));
                } else {
                    ObjectMapper mapper = new ObjectMapper();
                    Object value = mapper.treeToValue(valueNode, type);
                    field.set(this, value);
                }

            } catch (Exception e) {
                System.out.printf("Failed to set field '%s' from JSON: %s%n",
                        fieldName, e.getMessage());
            }
        }
    }

    public void postgressInsert(Connection conn, String tableName) throws Exception {
        String sql = insertBySQLBuilder(this.getClass(), tableName);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            postgressBatchInsert(stmt);
            stmt.executeUpdate();
        }
    }

    public void postgressBatchInsert(PreparedStatement stmt)
            throws IllegalAccessException, SQLException {
        Field[] fields = this.getClass().getDeclaredFields();

        int index = 1;

        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                Object value = field.get(this);

                if (value instanceof Integer integer) {
                    stmt.setInt(index, integer);
                } else if (value instanceof Double aDouble) {
                    stmt.setDouble(index, aDouble);
                } else if (value instanceof String string) {
                    stmt.setString(index, string);
                } else if (value instanceof Date date) {
                    stmt.setDate(index, date);
                } else if (value == null) {
                    stmt.setObject(index, null);
                } else {
                    stmt.setObject(index, value);
                }

                index++;
            }
        }
    }

    public static String insertBySQLBuilder(Class<?> cls, String tableName) {
        Field[] classFields = cls.getDeclaredFields();

        StringJoiner columns = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");

        for (Field field : classFields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            columns.add(HelperSQL.camelToSnake(field.getName()));
            placeholders.add("?");
        }

        return String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName, columns.toString(), placeholders.toString());
    }

    @Override
    public String toString() {
        return attributes.toString();
    }

    public Object get(String key) {
        return attributes.get(key);
    }

    public Integer getInt(String key) {
        return (Integer) attributes.get(key);
    }

    public String getString(String key) {
        return (String) attributes.get(key);
    }
}
