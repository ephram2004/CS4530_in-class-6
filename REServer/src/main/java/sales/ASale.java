package sales;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import helpers.HelperSQL;

public abstract class ASale {

    protected static Map<String, Object> attributes;

    public ASale(JsonNode json) {
        this.attributes = new HashMap<>();
        json.fieldNames().forEachRemaining(field ->
            attributes.put(field, json.get(field))
        );

        populateAttrsFromJSONNode(json);
    }

    public void postgressInsert(Connection conn, String tableName) throws Exception {
        Field[] fields = this.getClass().getDeclaredFields();
        String sql = HelperSQL.insertBySQLBuilder(this.getClass(), tableName);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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

            stmt.executeUpdate();
        }
    }

    @Override
    public String toString() {
        return attributes.toString();
    }

    private void populateAttrsFromJSONNode(JsonNode attributes) {
        Class<?> cls = this.getClass();

        for (Field field : cls.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            String fieldName = field.getName();
            JsonNode valueNode = attributes.get(fieldName);

            if (valueNode == null || valueNode.isNull()) {
                continue;
            }

            field.setAccessible(true);

            try {
                Class<?> type = field.getType();

                if (type == String.class) {
                    field.set(this, valueNode.asText());
                } else if (type == Integer.class || type == int.class) {
                    field.set(this, valueNode.asInt());
                } else if (type == Double.class || type == double.class) {
                    field.set(this, valueNode.asDouble());
                } else if (type == Date.class) {
                    field.set(this, Date.valueOf(valueNode.asText()));
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
}
