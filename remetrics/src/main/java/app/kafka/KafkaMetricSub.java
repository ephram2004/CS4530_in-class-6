package app.kafka;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.*;
import credentials.Credentials;
import io.micrometer.core.instrument.Metrics;
import sql.metric.MetricsController;
import sql.metric.MetricsDAO;

public class KafkaMetricSub {
    public void run(MetricsController metricsController) {

        Properties props = new Properties();
        props.put("bootstrap.servers", Credentials.get("POSTGRES_IP") + ":9092");
        props.put("group.id", "analytics-server");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("metrics"));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                String key = record.key();
                String metricName = key.split(":")[0];
                String metricId = key.split(":")[1];

                System.out.println("✅ Received sale access event for ID: " + key);
                metricsController.incrementNumAccessed(metricName, metricId);
            }
        }
    }
}
