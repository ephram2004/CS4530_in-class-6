package sql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.HashMap;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import helpers.HelperSQL;

public abstract class ANoSQLObj {

    protected static Map<String, Object> attributes;

    protected static JedisPool jedisPool = new JedisPool("localhost", 6379); 

    // json --> java fields
    public ANoSQLObj(JsonNode json) {
        this.attributes = new HashMap<>();
        json.fieldNames().forEachRemaining(field
                -> attributes.put(field, json.get(field))
        );

        populateAttrsFromJSONNode(json);
    }

    // populate attributes from JSON node
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

    // save object to redis
    public void saveToRedis(String redisKeyPrefix, String idFieldName) throws Exception {
        Class<?> cls = this.getClass(); // get subclass 
        Field[] fields = cls.getDeclaredFields();
        Map<String, String> fieldValsMap = new HashMap<>(); // all field vals as string

        String redisKey = redisKeyPrefix + ":" + getFieldValueAsString(idFieldName, cls);

        // go thru fields & serialize to string map 
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) continue;
    
            field.setAccessible(true);
            Object value = field.get(this);

            if (value == null) {
                fieldValsMap.put(field.getName(), value.toString());
            }
        }

        // store to redis with hset
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(redisKey, fieldValsMap);
        }
    }

    // get val of field used as the redis id
    private String getFieldValueAsString(String fieldName, Class<?> cls) throws Exception {
        Field field = cls.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(this);
        return value != null ? value.toString() : "null";
    }

    // accessors for attributes map 
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
