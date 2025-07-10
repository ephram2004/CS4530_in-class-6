package kafka;

import java.util.Properties;
import org.apache.kafka.clients.producer.*;
import credentials.Credentials;

public class REKafka {
    private static final Producer<String, String> producer;

    static {
        Properties props = new Properties();
        props.put("bootstrap.servers", "10.0.100.74:9092");
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
    }

    public static void send(String topic, String key, String value) {
        producer.send(new ProducerRecord<>(topic, key, value));
    }
}
