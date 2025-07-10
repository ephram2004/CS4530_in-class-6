package kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class MetricsKafkaProducer {
    
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String METRICS_TOPIC = "real-estate-metrics";
    
    private final KafkaProducer<String, String> producer;
    private final ObjectMapper mapper;
    
    public MetricsKafkaProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        
        this.producer = new KafkaProducer<>(props);
        this.mapper = new ObjectMapper();
    }
    
    public void emitMetricEvent(String metricName, String metricId, String action) {
        try {
            ObjectNode event = mapper.createObjectNode();
            event.put("metric_name", metricName);
            event.put("metric_id", metricId);
            event.put("action", action);
            event.put("timestamp", System.currentTimeMillis());
            
            String eventJson = mapper.writeValueAsString(event);
            ProducerRecord<String, String> record = new ProducerRecord<>(METRICS_TOPIC, eventJson);
            
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("Failed to send metric event: " + exception.getMessage());
                } else {
                    System.out.println("Metric event sent: " + eventJson);
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error creating metric event: " + e.getMessage());
        }
    }
    
    public void close() {
        producer.close();
    }
} 