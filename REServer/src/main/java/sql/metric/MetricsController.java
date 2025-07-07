package sql.metric;

import java.sql.SQLException;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class MetricsController {

    private final MetricsDAO metrics;

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

    public void incrementNumAccessed(String metricName, String metricID) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            ObjectNode node = mapper.createObjectNode();
            node.put("metricName", metricName);
            node.put("metricID", metricID);
            node.put("numAccessed", 0);

            metrics.addOrIncrementNumAccessed(new Metric(node));

            System.out.println("Number of visits metric updated");
        } catch (Exception e) {
            System.out.println("Failed to add metric: " + e.getMessage());
        }
    }

    @OpenApi(summary = "Get metric attribute",
            operationId = "getMetricByID",
            path = "/{metricName}/{metricID}/{attrName}",
            methods = HttpMethod.GET,
            tags = {"Metrics"},
            responses
            = {
                @OpenApiResponse(status = "200",
                        content = {
                            @OpenApiContent(from = Metric[].class)}),
                @OpenApiResponse(status = "404")
            })
    public void getMetricByID(Context ctx, String metricName, String metricID, String attrName) {
        try {
            Optional<Metric> metric = metrics.findMetricByID(metricName, metricID);
            metric.map(ctx::json).orElseGet(() -> {
                ctx.result("Metric not found");
                ctx.status(404);
                return ctx;
            });
        } catch (SQLException e) {
            ctx.result("Error retrieving metric: " + e.getMessage());
            ctx.status(400);
        }
    }
}
