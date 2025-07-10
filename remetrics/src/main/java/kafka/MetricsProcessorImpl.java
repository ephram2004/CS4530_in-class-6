package kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import sql.metric.Metric;
import sql.metric.MetricsDAO;

import java.sql.SQLException;

public class MetricsProcessorImpl implements MetricsProcessor {
    
    private final MetricsDAO metricsDAO;
    private final ObjectMapper mapper;
    
    public MetricsProcessorImpl(MetricsDAO metricsDAO) {
        this.metricsDAO = metricsDAO;
        this.mapper = new ObjectMapper();
    }
    
    @Override
    public void incrementMetric(String metricName, String metricId) {
        try {
            ObjectNode node = mapper.createObjectNode();
            node.put("metric_name", metricName);
            node.put("metric_id", metricId);
            node.put("num_accessed", 0); // Will be incremented by DAO
            
            Metric metric = new Metric(node);
            metricsDAO.addOrIncrementNumAccessed(metric);
            
            System.out.println("Incremented metric: " + metricName + "/" + metricId);
            
        } catch (SQLException e) {
            System.err.println("Error incrementing metric: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing metric: " + e.getMessage());
        }
    }
} 