package kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class MetricsKafkaListener {
    
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String METRICS_TOPIC = "real-estate-metrics";
    private static final String METRICS_GROUP_ID = "metrics-consumer-group";
    
    private final KafkaConsumer<String, String> consumer;
    private final ObjectMapper mapper;
    private final MetricsProcessor metricsProcessor;
    private volatile boolean running = true;
    
    public MetricsKafkaListener(MetricsProcessor metricsProcessor) {
        this.metricsProcessor = metricsProcessor;
        this.mapper = new ObjectMapper();
        
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, METRICS_GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        
        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList(METRICS_TOPIC));
    }
    
    public void startListening() {
        System.out.println("Starting Kafka metrics listener...");
        
        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        JsonNode event = mapper.readTree(record.value());
                        String metricName = event.get("metric_name").asText();
                        String metricId = event.get("metric_id").asText();
                        String action = event.get("action").asText();
                        
                        System.out.println("Received metric event: " + metricName + "/" + metricId + " - " + action);
                        
                        if ("access".equals(action)) {
                            metricsProcessor.incrementMetric(metricName, metricId);
                        }
                        
                    } catch (Exception e) {
                        System.err.println("Error processing metric event: " + e.getMessage());
                    }
                }
            }
        } finally {
            consumer.close();
        }
    }
    
    public void stop() {
        running = false;
    }
} 