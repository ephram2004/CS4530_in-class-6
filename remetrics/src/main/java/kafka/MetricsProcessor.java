package kafka;

public interface MetricsProcessor {
    void incrementMetric(String metricName, String metricId);
} 