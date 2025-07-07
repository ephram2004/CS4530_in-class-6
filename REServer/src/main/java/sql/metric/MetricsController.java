package sql.metric;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.http.Context;

public class MetricsController {
    private MetricsDAO metrics;

    public MetricsController(MetricsDAO metrics) {
        this.metrics = metrics;
    }

    public void addOrIncrementNumAccessed(Context ctx) {
        try {
            ctx.result("Number of visits metric Created");
            ctx.status(201);
        } catch (Exception e) {
            ctx.result("Failed to add metric: " + e.getMessage());
            ctx.status(400);
        }
    }
}
