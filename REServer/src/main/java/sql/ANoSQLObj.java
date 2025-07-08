package sql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import helpers.HelperSQL;
import helpers.RedisJsonCommand;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.json.commands.RedisJsonCommands;
import redis.clients.jedis.util.SafeEncoder;

public abstract class ANoSQLObj {

    protected static Map<String, Object> attributes;

    protected static JedisPool jedisPool = new JedisPool("localhost", 6379);

    // json --> java fields
    public ANoSQLObj(JsonNode json) {
        this.attributes = new HashMap<>();
        json.fieldNames().forEachRemaining(field -> attributes.put(field, json.get(field)));

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
            JsonNode valueNode = attributes.get(HelperSQL.camelToSnake(fieldName));

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
    public void saveToRedis(String redisKeyPrefix, long idFieldName) throws Exception {
        String redisKey = redisKeyPrefix + ":" + idFieldName;

        // Convert this Java object to a snake_case JSON map
        Map<String, Object> jsonMap = new HashMap<>();
        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            Object value = field.get(this);
            if (value != null) {
                jsonMap.put(HelperSQL.camelToSnake(field.getName()), value);
            }
        }

        // Convert to JSON string
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(jsonMap);

        System.out.println("Saving JSON to Redis: " + redisKey + " with JSON: " + jsonString);

        // Send JSON.SET command using low-level pipeline (same as loader)
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.sendCommand(
                    RedisJsonCommand.JSON_SET,
                    SafeEncoder.encode(redisKey),
                    SafeEncoder.encode("."),
                    SafeEncoder.encode(jsonString));
        } catch (Exception e) {
            System.err.println("Failed to save as JSON: " + e.getMessage());
        }
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
