package sql.metric;

import java.util.Optional;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;

public class MetricsDAO {

    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private static final String METRIC_PREFIX = "metrics";
    private static final ObjectMapper mapper = new ObjectMapper();

    public void addOrIncrementNumAccessed(Metric metric) {
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
            String key = METRIC_PREFIX + ":" + metric.getMetricName() + ":" + metric.getMetricID();
            if (jedis.exists(key)) {
                jedis.hincrBy(key, "num_accessed", 1);
            } else {
                jedis.hset(key, "metric_name", metric.getMetricName());
                jedis.hset(key, "metric_id", metric.getMetricID());
                jedis.hset(key, "num_accessed", "1");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to increment metric: " + e.getMessage());
        }
    }

    public Optional<Metric> findMetricByID(String metricID, String metricName) {
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
            String key = METRIC_PREFIX + ":" + metricName + ":" + metricID;
            Map<String, String> data = jedis.hgetAll(key);
            if (data == null || data.isEmpty()) return Optional.empty();

            JsonNode node = mapper.valueToTree(data);
            return Optional.of(new Metric(node));
        } catch (Exception e) {
            System.err.println("❌ Failed to fetch metric: " + e.getMessage());
            return Optional.empty();
        }
    }
}